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

package com.klinker.android.twitter.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.MainDrawerArrayAdapter;
import com.klinker.android.twitter.adapters.TimelinePagerAdapter;
import com.klinker.android.twitter.data.sq_lite.DMDataSource;
import com.klinker.android.twitter.data.sq_lite.FavoriteUsersDataSource;
import com.klinker.android.twitter.data.sq_lite.FollowersDataSource;
import com.klinker.android.twitter.data.sq_lite.FollowersSQLiteHelper;
import com.klinker.android.twitter.data.sq_lite.HomeDataSource;
import com.klinker.android.twitter.data.sq_lite.InteractionsDataSource;
import com.klinker.android.twitter.data.sq_lite.ListDataSource;
import com.klinker.android.twitter.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter.services.CatchupPull;
import com.klinker.android.twitter.services.TalonPullNotificationService;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.compose.ComposeActivity;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.ui.setup.LoginActivity;
import com.klinker.android.twitter.ui.setup.TutorialActivity;
import com.klinker.android.twitter.ui.setup.Version2Setup;
import com.klinker.android.twitter.utils.IOUtils;
import com.klinker.android.twitter.utils.MySuggestionsProvider;
import com.klinker.android.twitter.utils.NotificationUtils;
import com.klinker.android.twitter.utils.UpdateUtils;

import java.lang.reflect.Field;
import java.util.Calendar;

public class MainActivity extends DrawerActivity {

    public static boolean isPopup;
    public static Context sContext;

    public static ImageButton sendButton;
    public static LinearLayout sendLayout;
    public static boolean showIsRunning = false;
    public static boolean hideIsRunning = false;
    public static Handler sendHandler;
    public static Runnable showSend = new Runnable() {
        @Override
        public void run() {
            if (settings.floatingCompose && sendLayout.getVisibility() == View.GONE && !showIsRunning) {
                Animation anim = AnimationUtils.loadAnimation(sContext, R.anim.slide_in_left);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        showIsRunning = true;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        sendLayout.setVisibility(View.VISIBLE);
                        showIsRunning = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                anim.setDuration(300);
                sendLayout.startAnimation(anim);
            }
        }
    };
    public static Runnable hideSend = new Runnable() {
        @Override
        public void run() {
            if (settings.floatingCompose && sendLayout.getVisibility() == View.VISIBLE && !hideIsRunning) {
                Animation anim = AnimationUtils.loadAnimation(sContext, R.anim.slide_out_right);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        hideIsRunning = true;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        sendLayout.setVisibility(View.GONE);
                        hideIsRunning = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                anim.setDuration(300);
                sendLayout.startAnimation(anim);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.sendHandler = new Handler();

        context = this;
        sContext = this;
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        DrawerActivity.settings = AppSettings.getInstance(context);

        try {
            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        } catch (Exception e) {

        }

        sharedPrefs.edit().putBoolean("refresh_me", getIntent().getBooleanExtra("from_notification", false)).commit();

        setUpTheme();
        setUpWindow();
        setContentView(R.layout.main_activity);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        setUpDrawer(0, getResources().getString(R.string.timeline));

        MainActivity.sendLayout = (LinearLayout) findViewById(R.id.send_layout);
        MainActivity.sendHandler.postDelayed(showSend, 1000);
        MainActivity.sendButton = (ImageButton) findViewById(R.id.send_button);
        MainActivity.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent compose = new Intent(context, ComposeActivity.class);
                startActivity(compose);
            }
        });

        actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.timeline));

        if (!settings.isTwitterLoggedIn) {
            Intent login = new Intent(context, LoginActivity.class);
            startActivity(login);
        } /*else if (!sharedPrefs.getBoolean("setup_v_two", false) && !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("setup_v_two", false)) {
            Intent setupV2 = new Intent(context, Version2Setup.class);
            startActivity(setupV2);
        }*/

        mSectionsPagerAdapter = new TimelinePagerAdapter(getFragmentManager(), context, sharedPrefs, getIntent().getBooleanExtra("from_launcher", false));

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mViewPager.setCurrentItem(mSectionsPagerAdapter.getCount() - 3);

        if (getIntent().getBooleanExtra("from_launcher", false)) {
            actionBar.setTitle(mSectionsPagerAdapter.getPageTitle(getIntent().getIntExtra("launcher_page", 0)));
        }

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (!actionBar.isShowing()) {
                    actionBar.show();

                    if (translucent) {
                        statusBar.setVisibility(View.VISIBLE);
                    }
                }
                MainActivity.sendHandler.post(showSend);
            }

            public void onPageSelected(int position) {

                String title = "" + mSectionsPagerAdapter.getPageTitle(position);

                if (title.equals(getResources().getString(R.string.mentions))) {
                    MainDrawerArrayAdapter.current = 1;
                } else if (title.equals(getResources().getString(R.string.direct_messages))) {
                    MainDrawerArrayAdapter.current = 2;
                } else if (title.equals(getResources().getString(R.string.timeline))) {
                    MainDrawerArrayAdapter.current = 0;
                } else {
                    MainDrawerArrayAdapter.current = -1;
                }

                drawerList.invalidateViews();

                actionBar.setTitle(title);
            }
        });

        mViewPager.setOffscreenPageLimit(4);

        if (getIntent().getBooleanExtra("tutorial", false) && !sharedPrefs.getBoolean("done_tutorial", false)) {
            getIntent().putExtra("tutorial", false);
            sharedPrefs.edit().putBoolean("done_tutorial", true).commit();
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        Log.v("tutorial_activity", "close drawer");
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        unregisterReceiver(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new IntentFilter(TutorialActivity.ACTION_CLOSE_DRAWER));

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        Log.v("tutorial_activity", "open drawer");
                        mDrawerLayout.openDrawer(Gravity.LEFT);
                        unregisterReceiver(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new IntentFilter(TutorialActivity.ACTION_OPEN_DRAWER));

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        Log.v("tutorial_activity", "page left");
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
                        unregisterReceiver(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new IntentFilter(TutorialActivity.ACTION_PAGE_LEFT));

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        Log.v("tutorial_activity", "page right");
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                        unregisterReceiver(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new IntentFilter(TutorialActivity.ACTION_PAGE_RIGHT));

            startActivity(new Intent(context, TutorialActivity.class));
            overridePendingTransition(0, 0);
        }

        setLauncherPage();

        if (getIntent().getBooleanExtra("from_drawer", false)) {
            mViewPager.setCurrentItem(getIntent().getIntExtra("page_to_open", 3));
        }
    }

    public void setLauncherPage() {
        // do nothing here
    }

    public void setUpWindow() {
        // nothing here, will be overrode
        MainActivity.isPopup = false;

        if ((getIntent().getFlags() & 0x00002000) != 0) {
            MainActivity.isPopup = true;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            mDrawerToggle.onConfigurationChanged(newConfig);
        } catch (Exception e) { }

        context.sendBroadcast(new Intent("com.klinker.android.twitter.MARK_POSITION"));
        sharedPrefs.edit().putBoolean("refresh_me", true).commit();

        sharedPrefs.edit().putBoolean("open_a_page", true).commit();
        sharedPrefs.edit().putInt("open_what_page", mViewPager.getCurrentItem()).commit();

        overridePendingTransition(0, 0);
        finish();
        Intent restart = new Intent(context, MainActivity.class);
        restart.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        restart.putExtra("open_a_page", true);
        restart.putExtra("open_what_page", mViewPager.getCurrentItem());
        overridePendingTransition(0, 0);
        sharedPrefs.edit().putBoolean("should_refresh", false).commit();
        startActivity(restart);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity.showIsRunning = false;
        MainActivity.hideIsRunning = false;
        MainActivity.sendHandler.postDelayed(showSend, 1000);

        if (sharedPrefs.getBoolean("open_a_page", false)) {
            sharedPrefs.edit().putBoolean("open_a_page", false).commit();
            int page = sharedPrefs.getInt("open_what_page", 3);
            String title = "" + mSectionsPagerAdapter.getPageTitle(page);
            actionBar.setTitle(title);
            mViewPager.setCurrentItem(page);
        }

        if (sharedPrefs.getBoolean("open_interactions", false)) {
            sharedPrefs.edit().putBoolean("open_interactions", false).commit();
            mDrawerLayout.openDrawer(Gravity.END);
        }
    }

    @Override
    public void onDestroy() {
        try {
            HomeDataSource.getInstance(context).close();
        } catch (Exception e) { }
        try {
            MentionsDataSource.getInstance(context).close();
        } catch (Exception e) { }
        try {
            DMDataSource.getInstance(context).close();
        } catch (Exception e) { }
        try {
            ListDataSource.getInstance(context).close();
        } catch (Exception e) { }
        try {
            FollowersDataSource.getInstance(context).close();
        } catch (Exception e) { }
        try {
            FavoriteUsersDataSource.getInstance(context).close();
        } catch (Exception e) { }
        try {
            InteractionsDataSource.getInstance(context).close();
        } catch (Exception e) { }

        super.onDestroy();
    }

    public static boolean caughtstarting = false;

    @Override
    public void onStart() {
        super.onStart();

        MainActivity.isPopup = false;

        Log.v("talon_starting", "main activity starting");

        sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        // check for night mode switching
        int theme = AppSettings.getCurrentTheme(sharedPrefs);

        if (!getWindow().hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY) ||
                sharedPrefs.getBoolean("launcher_frag_switch", false) ||
                (theme != settings.theme && !settings.addonTheme)) {

            sharedPrefs.edit().putBoolean("launcher_frag_switch", false)
                              .putBoolean("dont_refresh", true).commit();

            AppSettings.invalidate();

            Log.v("talon_theme", "no action bar overlay found, recreating");

            finish();
            overridePendingTransition(0, 0);
            startActivity(getRestartIntent());
            overridePendingTransition(0, 0);

            MainActivity.caughtstarting = true;

            // return so that it doesn't start the background refresh, that is what caused the dups.
            sharedPrefs.edit().putBoolean("dont_refresh_on_start", true).commit();
            return;
        } else {
            sharedPrefs.edit().putBoolean("dont_refresh", false)
                              .putBoolean("should_refresh", true).commit();

        }

        if(DrawerActivity.settings.pushNotifications) {
            if (!TalonPullNotificationService.isRunning) {
                context.startService(new Intent(context, TalonPullNotificationService.class));
            }
        } else {
            context.sendBroadcast(new Intent("com.klinker.android.twitter.STOP_PUSH_SERVICE"));
        }

        // cancel the alarm to start the catchup service
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 236, new Intent(context, CatchupPull.class), 0);
        am.cancel(pendingIntent); // cancel the old one, then start the new one in 1 min

        // clear the pull unread
        sharedPrefs.edit().putInt("pull_unread", 0).commit();

        UpdateUtils.checkUpdate(this);

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
                NotificationUtils.refreshNotification(context);
            }
        }).start();*/
    }

    public Intent getRestartIntent() {
        return new Intent(context, MainActivity.class);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (settings.floatingCompose) {
            menu.getItem(2).setVisible(false); // hide the compose button here
        }

        if (settings.tweetmarkerManualOnly) {
            menu.getItem(7).setVisible(true);
        }

        return true;
    }

}