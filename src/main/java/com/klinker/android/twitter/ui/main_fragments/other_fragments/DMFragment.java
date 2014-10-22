/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.twitter.ui.main_fragments.other_fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.CursorListLoader;
import com.klinker.android.twitter.adapters.DirectMessageListArrayAdapter;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.sq_lite.DMDataSource;
import com.klinker.android.twitter.data.sq_lite.DMSQLiteHelper;
import com.klinker.android.twitter.services.DirectMessageRefreshService;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.ui.main_fragments.MainFragment;
import com.klinker.android.twitter.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.TwitterException;
import twitter4j.User;

public class DMFragment extends MainFragment {

    public static final int DM_REFRESH_ID = 125;

    public DirectMessageListArrayAdapter arrayAdapter;

    public BroadcastReceiver updateDM = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getCursorAdapter(false);
        }
    };

    @Override
    public void setBuilder() {
        // we don't want one at all.
        // The array adapter will take care of it for us this time
    }

    @Override
    public void setUpListScroll() {
        final boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            int mLastFirstVisibleItem = 0;

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (i == SCROLL_STATE_IDLE) {
                    MainActivity.sendHandler.removeCallbacks(MainActivity.hideSend);
                    MainActivity.sendHandler.postDelayed(MainActivity.showSend, 600);
                } else {
                    MainActivity.sendHandler.removeCallbacks(MainActivity.showSend);
                    MainActivity.sendHandler.postDelayed(MainActivity.hideSend, 300);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, final int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (DrawerActivity.settings.uiExtras) {
                    // show and hide the action bar
                    if (firstVisibleItem != 0) {
                        if (MainActivity.canSwitch) {
                            // used to show and hide the action bar
                            if (firstVisibleItem < 3) {

                            } else if (firstVisibleItem < mLastFirstVisibleItem) {
                                if(!landscape && !isTablet) {
                                    actionBar.hide();
                                }
                                if (!isToastShowing && DrawerActivity.settings.useToast) {
                                    showToastBar(firstVisibleItem + " " + fromTop, jumpToTop, 400, false, toTopListener);
                                }
                            } else if (firstVisibleItem > mLastFirstVisibleItem) {
                                if(!landscape && !isTablet) {
                                    actionBar.show();
                                }
                                if (isToastShowing && !infoBar && DrawerActivity.settings.useToast) {
                                    hideToastBar(400);
                                }
                            }

                            mLastFirstVisibleItem = firstVisibleItem;
                        }
                    } else {
                        if(!landscape && !isTablet) {
                            actionBar.show();
                        }
                        if (!infoBar && DrawerActivity.settings.useToast) {
                            hideToastBar(400);
                        }
                    }

                    if (isToastShowing && !infoBar && DrawerActivity.settings.useToast) {
                        updateToastText(firstVisibleItem + " " + fromTop);
                    }

                    if (MainActivity.translucent && actionBar.isShowing()) {
                        showStatusBar();
                    } else if (MainActivity.translucent) {
                        hideStatusBar();
                    }
                }
            }

        });
    }

    @Override
    public void onRefreshStarted() {
        new AsyncTask<Void, Void, Void>() {

            private boolean update;
            private int numberNew;

            @Override
            protected void onPreExecute() {
                DrawerActivity.canSwitch = false;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    twitter = Utils.getTwitter(context, DrawerActivity.settings);

                    User user = twitter.verifyCredentials();
                    long lastId = sharedPrefs.getLong("last_direct_message_id_" + currentAccount, 0);
                    Paging paging;
                    if (lastId != 0) {
                        paging = new Paging(1).sinceId(lastId);
                    } else {
                        paging = new Paging(1, 500);
                    }

                    List<DirectMessage> dm = twitter.getDirectMessages(paging);
                    List<DirectMessage> sent = twitter.getSentDirectMessages(paging);

                    if (dm.size() != 0) {
                        sharedPrefs.edit().putLong("last_direct_message_id_" + currentAccount, dm.get(0).getId()).commit();
                        update = true;
                        numberNew = dm.size();
                    } else {
                        update = false;
                        numberNew = 0;
                    }

                    DMDataSource dataSource = DMDataSource.getInstance(context);

                    for (DirectMessage directMessage : dm) {
                        try {
                            dataSource.createDirectMessage(directMessage, currentAccount);
                        } catch (IllegalStateException e) {
                            dataSource = DMDataSource.getInstance(context);
                            dataSource.createDirectMessage(directMessage, currentAccount);
                        }
                    }

                    for (DirectMessage directMessage : sent) {
                        try {
                            dataSource.createDirectMessage(directMessage, currentAccount);
                        } catch (Exception e) {
                            dataSource = DMDataSource.getInstance(context);
                            dataSource.createDirectMessage(directMessage, currentAccount);
                        }
                    }

                } catch (TwitterException e) {
                    // Error in updating status
                    Log.d("Twitter Update Error", e.getMessage());
                }


                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                long now = new Date().getTime();
                long alarm = now + DrawerActivity.settings.dmRefresh;

                Log.v("alarm_date", "direct message " + new Date(alarm).toString());

                PendingIntent pendingIntent = PendingIntent.getService(context, DM_REFRESH_ID, new Intent(context, DirectMessageRefreshService.class), 0);

                if (DrawerActivity.settings.dmRefresh != 0)
                    am.setRepeating(AlarmManager.RTC_WAKEUP, alarm, DrawerActivity.settings.dmRefresh, pendingIntent);
                else
                    am.cancel(pendingIntent);


                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                try {
                    if (update) {
                        getCursorAdapter(false);

                        CharSequence text = numberNew == 1 ?  numberNew +  " " + getResources().getString(R.string.new_direct_message) :  numberNew + " " + getResources().getString(R.string.new_direct_messages);
                        showToastBar(text + "", jumpToTop, 400, true, toTopListener);

                        int size = toDP(5) + mActionBarSize + (DrawerActivity.translucent ? DrawerActivity.statusBarHeight : 0);
                        listView.setSelectionFromTop(numberNew + (MainActivity.isPopup || landscape || MainActivity.settings.jumpingWorkaround ? 1 : 2), size);
                    } else {
                        getCursorAdapter(false);

                        CharSequence text = getResources().getString(R.string.no_new_direct_messages);
                        showToastBar(text + "", allRead, 400, true, toTopListener);
                    }
                    refreshLayout.setRefreshing(false);
                } catch (IllegalStateException e) {
                    // fragment not attached to activity
                }

                DrawerActivity.canSwitch = true;
            }
        }.execute();
    }

    @Override
    public void getCursorAdapter(boolean showSpinner) {
        new GetCursorAdapter().execute();
    }

    class GetCursorAdapter extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... args) {
            Log.v("direct_message", "getting adapter");

            Cursor cursor;
            try {
                cursor = DMDataSource.getInstance(getActivity()).getCursor(DrawerActivity.settings.currentAccount);
            } catch (Exception e) {
                DMDataSource.dataSource = null;
                return "";
            }

            ArrayList<com.klinker.android.twitter.data.DirectMessage> messageList = new ArrayList<com.klinker.android.twitter.data.DirectMessage>();
            ArrayList<String> names = new ArrayList<String>();

            try {
                if (cursor.moveToLast()) {
                    do {
                        String screenname = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_SCREEN_NAME));
                        String otherName = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_RETWEETER));

                        if (!names.contains(screenname) && !screenname.equals(DrawerActivity.settings.myScreenName)) {
                            Log.v("direct_message", "adding screenname: " + screenname);
                            names.add(screenname);

                            String name = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_NAME));
                            String message = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TEXT));
                            String profilePic = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_PRO_PIC));

                            messageList.add(new com.klinker.android.twitter.data.DirectMessage(name, screenname, message, profilePic));
                        } else if (screenname.equals(DrawerActivity.settings.myScreenName) && !names.contains(otherName)) {

                            names.add(otherName);

                            String name = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_EXTRA_TWO));
                            String message = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TEXT));
                            String profilePic = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_EXTRA_ONE));

                            messageList.add(new com.klinker.android.twitter.data.DirectMessage(name, otherName, message, profilePic));
                        }
                    } while (cursor.moveToPrevious());
                }
            } catch (Exception e) {
                DMDataSource.dataSource = null;
                getCursorAdapter(false);
                return null;
            }

            cursor.close();

            arrayAdapter = new DirectMessageListArrayAdapter(context, messageList);

            return null;
        }

        protected void onPostExecute(String file_url) {

            try {
                spinner.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            } catch (Exception e) { }

            try {
                listView.setAdapter(arrayAdapter);
            } catch (Exception e) {

            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (sharedPrefs.getBoolean("refresh_me_dm", false)) {
            getCursorAdapter(false);
        }

        sharedPrefs.edit().putInt("dm_unread_" + currentAccount, 0).commit();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.twitter.UPDATE_DM");
        context.registerReceiver(updateDM, filter);

        sharedPrefs.edit().putBoolean("refresh_me_dm", false).commit();
    }

    @Override
    public void onPause() {

        context.unregisterReceiver(updateDM);

        super.onPause();
    }
}