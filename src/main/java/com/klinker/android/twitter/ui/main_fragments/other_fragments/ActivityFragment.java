package com.klinker.android.twitter.ui.main_fragments.other_fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.ActivityCursorAdapter;
import com.klinker.android.twitter.data.sq_lite.ActivityDataSource;
import com.klinker.android.twitter.services.ActivityRefreshService;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.ui.main_fragments.MainFragment;
import com.klinker.android.twitter.utils.ActivityUtils;
import com.klinker.android.twitter.utils.Utils;
import twitter4j.Twitter;

import java.util.Date;

public class ActivityFragment extends MainFragment {

    public static final int ACTIVITY_REFRESH_ID = 131;

    public int unread = 0;

    public BroadcastReceiver refreshActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getCursorAdapter(false);
        }
    };

    public View getLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.activity_fragment, null);
    }

    protected void setSpinner(View layout) {
        spinner = (LinearLayout) layout.findViewById(R.id.no_content);
    }

    @Override
    public void setUpListScroll() {

    }

    public Twitter getTwitter() {
        return Utils.getTwitter(context, DrawerActivity.settings);
    }

    @Override
    public void onRefreshStarted() {
        new AsyncTask<Void, Void, Cursor>() {

            private boolean update = false;
            @Override
            protected void onPreExecute() {
                DrawerActivity.canSwitch = false;
            }

            @Override
            protected Cursor doInBackground(Void... params) {

                ActivityUtils utils = new ActivityUtils(getActivity());

                update = utils.refreshActivity();

                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                long now = new Date().getTime();
                long alarm = now + DrawerActivity.settings.activityRefresh;

                PendingIntent pendingIntent = PendingIntent.getService(context, ACTIVITY_REFRESH_ID, new Intent(context, ActivityRefreshService.class), 0);

                if (DrawerActivity.settings.activityRefresh != 0)
                    am.setRepeating(AlarmManager.RTC_WAKEUP, alarm, DrawerActivity.settings.activityRefresh, pendingIntent);
                else
                    am.cancel(pendingIntent);

                return ActivityDataSource.getInstance(context).getCursor(currentAccount);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {

                Cursor c = null;
                try {
                    c = cursorAdapter.getCursor();
                } catch (Exception e) {

                }

                cursorAdapter = setAdapter(cursor);

                try {
                    listView.setAdapter(cursorAdapter);
                } catch (Exception e) {

                }

                try {
                    if (update) {
                        showToastBar(getString(R.string.new_activity), getString(R.string.ok), 400, true, toTopListener);
                    }
                } catch (Exception e) {
                    // user closed the app before it was done
                }

                refreshLayout.setRefreshing(false);

                DrawerActivity.canSwitch = true;

                try {
                    c.close();
                } catch (Exception e) {

                }
            }
        }.execute();
    }

    public ActivityCursorAdapter setAdapter(Cursor c) {
        return new ActivityCursorAdapter(context, c);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sharedPrefs.getBoolean("refresh_me_activity", false)) {
            getCursorAdapter(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sharedPrefs.edit().putBoolean("refresh_me_activity", false).commit();
                }
            },1000);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.twitter.REFRESH_ACTIVITY");
        filter.addAction("com.klinker.android.twitter.NEW_ACTIVITY");
        context.registerReceiver(refreshActivity, filter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void getCursorAdapter(boolean showSpinner) {
        if (showSpinner) {
            try {
                listView.setVisibility(View.GONE);
            } catch (Exception e) { }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor;
                try {
                    cursor = ActivityDataSource.getInstance(context).getCursor(currentAccount);
                } catch (Exception e) {
                    ActivityDataSource.dataSource = null;
                    getCursorAdapter(true);
                    return;
                }

                try {
                    Log.v("talon_databases", "mentions cursor size: " + cursor.getCount());
                } catch (Exception e) {
                    ActivityDataSource.dataSource = null;
                    getCursorAdapter(true);
                    return;
                }

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor c = null;
                        if (cursorAdapter != null) {
                            c = cursorAdapter.getCursor();
                        }

                        cursorAdapter = new ActivityCursorAdapter(context, cursor);

                        try {
                            listView.setVisibility(View.VISIBLE);
                        } catch (Exception e) { }

                        try {
                            listView.setAdapter(cursorAdapter);
                        } catch (Exception e) {

                        }

                        if (cursor.getCount() == 0) {
                            spinner.setVisibility(View.VISIBLE);
                        } else {
                            spinner.setVisibility(View.GONE);
                        }

                        try {
                            c.close();
                        } catch (Exception e) {

                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onPause() {
        context.unregisterReceiver(refreshActivity);
        super.onPause();
    }
}
