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

package com.klinker.android.twitter.ui.drawer_activities;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.InteractionsCursorAdapter;
import com.klinker.android.twitter.adapters.MainDrawerArrayAdapter;
import com.klinker.android.twitter.adapters.TimelinePagerAdapter;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.sq_lite.DMDataSource;
import com.klinker.android.twitter.data.sq_lite.FavoriteUsersDataSource;
import com.klinker.android.twitter.data.sq_lite.HomeDataSource;
import com.klinker.android.twitter.data.sq_lite.InteractionsDataSource;
import com.klinker.android.twitter.data.sq_lite.ListDataSource;
import com.klinker.android.twitter.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter.listeners.InteractionClickListener;
import com.klinker.android.twitter.listeners.MainDrawerClickListener;
import com.klinker.android.twitter.settings.SettingsActivity;
import com.klinker.android.twitter.ui.drawer_activities.lists.ListsActivity;
import com.klinker.android.twitter.ui.search.SearchPager;
import com.klinker.android.twitter.utils.MySuggestionsProvider;
import com.klinker.android.twitter.manipulations.widgets.NetworkedCacheableImageView;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.compose.ComposeActivity;
import com.klinker.android.twitter.ui.compose.ComposeDMActivity;
import com.klinker.android.twitter.ui.setup.LoginActivity;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.profile_viewer.ProfilePager;
import com.klinker.android.twitter.manipulations.widgets.ActionBarDrawerToggle;
import com.klinker.android.twitter.manipulations.widgets.HoloTextView;
import com.klinker.android.twitter.manipulations.widgets.NotificationDrawerLayout;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.utils.Utils;

import com.klinker.android.twitter.utils.XmlFaqUtils;
import de.timroes.android.listview.EnhancedListView;
import uk.co.senab.bitmapcache.BitmapLruCache;

import org.lucasr.smoothie.AsyncListView;

import java.lang.reflect.Field;
import java.util.*;

public abstract class DrawerActivity extends Activity {

    public static AppSettings settings;
    public Activity context;
    public SharedPreferences sharedPrefs;

    public ActionBar actionBar;

    public static ViewPager mViewPager;
    public TimelinePagerAdapter mSectionsPagerAdapter;

    public NotificationDrawerLayout mDrawerLayout;
    public InteractionsCursorAdapter notificationAdapter;
    public LinearLayout mDrawer;
    public ListView drawerList;
    public EnhancedListView notificationList;
    public ActionBarDrawerToggle mDrawerToggle;

    public AsyncListView listView;

    public boolean logoutVisible = false;
    public static boolean translucent;

    public static boolean canSwitch = true;

    public static View statusBar;
    public static int statusBarHeight;
    public static int navBarHeight;

    public int openMailResource;
    public int closedMailResource;
    public static HoloTextView oldInteractions;
    public ImageView readButton;

    private NetworkedCacheableImageView backgroundPic;
    private NetworkedCacheableImageView profilePic;

    public MainDrawerArrayAdapter adapter;

    public void setUpDrawer(int number, final String actName) {

        int currentAccount = sharedPrefs.getInt("current_account", 1);
        for (int i = 0; i < TimelinePagerAdapter.MAX_EXTRA_PAGES; i++) {
            String pageIdentifier = "account_" + currentAccount + "_page_" + (i + 1);
            int type = sharedPrefs.getInt(pageIdentifier, AppSettings.PAGE_TYPE_NONE);

            if (type != AppSettings.PAGE_TYPE_NONE) {
                number++;
            }
        }

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        actionBar = getActionBar();

        adapter = new MainDrawerArrayAdapter(context);
        MainDrawerArrayAdapter.setCurrent(context, number);

        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.drawerIcon});
        int resource = a.getResourceId(0, 0);
        a.recycle();

        a = context.getTheme().obtainStyledAttributes(new int[] {R.attr.read_button});
        openMailResource = a.getResourceId(0,0);
        a.recycle();

        a = context.getTheme().obtainStyledAttributes(new int[] {R.attr.unread_button});
        closedMailResource = a.getResourceId(0,0);
        a.recycle();


        mDrawerLayout = (NotificationDrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (LinearLayout) findViewById(R.id.left_drawer);

        HoloTextView name = (HoloTextView) mDrawer.findViewById(R.id.name);
        HoloTextView screenName = (HoloTextView) mDrawer.findViewById(R.id.screen_name);
        backgroundPic = (NetworkedCacheableImageView) mDrawer.findViewById(R.id.background_image);
        profilePic = (NetworkedCacheableImageView) mDrawer.findViewById(R.id.profile_pic_contact);
        final ImageButton showMoreDrawer = (ImageButton) mDrawer.findViewById(R.id.options);
        final LinearLayout logoutLayout = (LinearLayout) mDrawer.findViewById(R.id.logoutLayout);
        final Button logoutDrawer = (Button) mDrawer.findViewById(R.id.logoutButton);
        drawerList = (ListView) mDrawer.findViewById(R.id.drawer_list);
        notificationList = (EnhancedListView) findViewById(R.id.notificationList);

        try {
            mDrawerLayout = (NotificationDrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_rev, Gravity.END);

            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    resource,  /* nav drawer icon to replace 'Up' caret */
                    R.string.app_name,  /* "open drawer" description */
                    R.string.app_name  /* "close drawer" description */
            ) {

                public void onDrawerClosed(View view) {

                    actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

                    if (logoutVisible) {
                        Animation ranim = AnimationUtils.loadAnimation(context, R.anim.drawer_rotate_back);
                        ranim.setFillAfter(true);
                        showMoreDrawer.startAnimation(ranim);

                        logoutLayout.setVisibility(View.GONE);
                        drawerList.setVisibility(View.VISIBLE);

                        logoutVisible = false;
                    }

                    if (MainDrawerArrayAdapter.current > adapter.pageTypes.size()) {
                        actionBar.setTitle(actName);
                    } else {
                        int position = mViewPager.getCurrentItem();
                        String title = "";
                        try {
                            title = "" + mSectionsPagerAdapter.getPageTitle(position);
                        } catch (NullPointerException e) {
                            title = "";
                        }
                        actionBar.setTitle(title);
                    }

                    try {
                        if (oldInteractions.getText().toString().equals(getResources().getString(R.string.new_interactions))) {
                            oldInteractions.setText(getResources().getString(R.string.old_interactions));
                            readButton.setImageResource(openMailResource);
                            notificationList.enableSwipeToDismiss();
                            notificationAdapter = new InteractionsCursorAdapter(context, InteractionsDataSource.getInstance(context).getUnreadCursor(DrawerActivity.settings.currentAccount));
                            notificationList.setAdapter(notificationAdapter);
                        }
                    } catch (Exception e) {
                        // don't have talon pull on
                    }

                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    actionBar.setTitle(getResources().getString(R.string.app_name));
                    actionBar.setIcon(R.mipmap.ic_launcher);

                    try {
                        notificationAdapter = new InteractionsCursorAdapter(context,
                                InteractionsDataSource.getInstance(context).getUnreadCursor(settings.currentAccount));
                        notificationList.setAdapter(notificationAdapter);
                        notificationList.enableSwipeToDismiss();
                        oldInteractions.setText(getResources().getString(R.string.old_interactions));
                        readButton.setImageResource(openMailResource);
                        sharedPrefs.edit().putBoolean("new_notification", false).commit();
                    } catch (Exception e) {
                        // don't have talon pull on
                    }

                    invalidateOptionsMenu();
                }

                public void onDrawerSlide(View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, slideOffset);

                    if (!actionBar.isShowing()) {
                        actionBar.show();
                    }

                    if (translucent) {
                        statusBar.setVisibility(View.VISIBLE);
                    }
                }
            };

            mDrawerLayout.setDrawerListener(mDrawerToggle);
        } catch (Exception e) {
            // landscape mode
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        showMoreDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(logoutLayout.getVisibility() == View.GONE) {
                    Animation ranim = AnimationUtils.loadAnimation(context, R.anim.drawer_rotate);
                    ranim.setFillAfter(true);
                    showMoreDrawer.startAnimation(ranim);

                    Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            drawerList.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    anim.setDuration(300);
                    drawerList.startAnimation(anim);

                    Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                    anim2.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            logoutLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    anim2.setDuration(300);
                    logoutLayout.startAnimation(anim2);

                    logoutVisible = true;
                } else {
                    Animation ranim = AnimationUtils.loadAnimation(context, R.anim.drawer_rotate_back);
                    ranim.setFillAfter(true);
                    showMoreDrawer.startAnimation(ranim);

                    Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            drawerList.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    anim.setDuration(300);
                    drawerList.startAnimation(anim);

                    Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.fade_out);
                    anim2.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            logoutLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    anim2.setDuration(300);
                    logoutLayout.startAnimation(anim2);

                    logoutVisible = false;
                }
            }
        });

        logoutDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutFromTwitter();
            }
        });

        final String sName = settings.myName;
        final String sScreenName = settings.myScreenName;
        final String backgroundUrl = settings.myBackgroundUrl;
        final String profilePicUrl = settings.myProfilePicUrl;

        final BitmapLruCache mCache = App.getInstance(context).getProfileCache();

        if (!backgroundUrl.equals("")) {
            backgroundPic.loadImage(backgroundUrl, false, null);
            //ImageUtils.loadImage(context, backgroundPic, backgroundUrl, mCache);
        } else {
            backgroundPic.setImageDrawable(getResources().getDrawable(R.drawable.default_header_background));
        }

        backgroundPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mDrawerLayout.closeDrawer(Gravity.START);
                } catch (Exception e) {

                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent viewProfile = new Intent(context, ProfilePager.class);
                        viewProfile.putExtra("name", sName);
                        viewProfile.putExtra("screenname", sScreenName);
                        viewProfile.putExtra("proPic", profilePicUrl);
                        viewProfile.putExtra("tweetid", 0);
                        viewProfile.putExtra("retweet", false);
                        viewProfile.putExtra("long_click", false);

                        context.startActivity(viewProfile);
                    }
                }, 400);
            }
        });

        backgroundPic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                try {
                    mDrawerLayout.closeDrawer(Gravity.START);
                } catch (Exception e) {

                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent viewProfile = new Intent(context, ProfilePager.class);
                        viewProfile.putExtra("name", sName);
                        viewProfile.putExtra("screenname", sScreenName);
                        viewProfile.putExtra("proPic", profilePicUrl);
                        viewProfile.putExtra("tweetid", 0);
                        viewProfile.putExtra("retweet", false);
                        viewProfile.putExtra("long_click", true);

                        context.startActivity(viewProfile);
                    }
                }, 400);

                return false;
            }
        });

        try {
            name.setText(sName);
            screenName.setText("@" + sScreenName);
            name.setTextSize(15);
            screenName.setTextSize(15);
        } catch (Exception e) {
            // 7 inch tablet in portrait
        }

        try {
            if(settings.roundContactImages) {
                //profilePic.loadImage(profilePicUrl, false, null, NetworkedCacheableImageView.CIRCLE);
                ImageUtils.loadCircleImage(context, profilePic, profilePicUrl, mCache);
            } else {
                profilePic.loadImage(profilePicUrl, false, null);
                ImageUtils.loadImage(context, profilePic, profilePicUrl, mCache);
            }
        } catch (Exception e) {
            // empty path again
        }

        drawerList.setAdapter(adapter);

        drawerList.setOnItemClickListener(new MainDrawerClickListener(context, mDrawerLayout, mViewPager));

        // set up for the second account
        int count = 0; // number of accounts logged in

        if (sharedPrefs.getBoolean("is_logged_in_1", false)) {
            count++;
        }

        if (sharedPrefs.getBoolean("is_logged_in_2", false)) {
            count++;
        }

        RelativeLayout secondAccount = (RelativeLayout) findViewById(R.id.second_profile);
        HoloTextView name2 = (HoloTextView) findViewById(R.id.name_2);
        HoloTextView screenname2 = (HoloTextView) findViewById(R.id.screen_name_2);
        NetworkedCacheableImageView proPic2 = (NetworkedCacheableImageView) findViewById(R.id.profile_pic_2);

        name2.setTextSize(15);
        screenname2.setTextSize(15);

        final int current = sharedPrefs.getInt("current_account", 1);

        // make a second account
        if(count == 1){
            name2.setText(getResources().getString(R.string.new_account));
            screenname2.setText(getResources().getString(R.string.tap_to_setup));
            secondAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (canSwitch) {
                        if (current == 1) {
                            sharedPrefs.edit().putInt("current_account", 2).commit();
                        } else {
                            sharedPrefs.edit().putInt("current_account", 1).commit();
                        }
                        context.sendBroadcast(new Intent("com.klinker.android.twitter.STOP_PUSH_SERVICE"));
                        context.sendBroadcast(new Intent("com.klinker.android.twitter.MARK_POSITION"));

                        Intent login = new Intent(context, LoginActivity.class);
                        AppSettings.invalidate();
                        finish();
                        startActivity(login);
                    }
                }
            });
        } else { // switch accounts
            if (current == 1) {
                name2.setText(sharedPrefs.getString("twitter_users_name_2", ""));
                screenname2.setText("@" + sharedPrefs.getString("twitter_screen_name_2", ""));
                try {
                    if(settings.roundContactImages) {
                        //proPic2.loadImage(sharedPrefs.getString("profile_pic_url_2", ""), true, null, NetworkedCacheableImageView.CIRCLE);
                        ImageUtils.loadCircleImage(context, proPic2, sharedPrefs.getString("profile_pic_url_2", ""), mCache);
                    } else {
                        //proPic2.loadImage(sharedPrefs.getString("profile_pic_url_2", ""), true, null);
                        ImageUtils.loadImage(context, proPic2, sharedPrefs.getString("profile_pic_url_2", ""), mCache);
                    }
                } catch (Exception e) {

                }

                secondAccount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (canSwitch) {
                            context.sendBroadcast(new Intent("com.klinker.android.twitter.STOP_PUSH_SERVICE"));
                            context.sendBroadcast(new Intent("com.klinker.android.twitter.MARK_POSITION").putExtra("current_account", current));

                            Toast.makeText(context, "Preparing to switch", Toast.LENGTH_SHORT).show();

                            // we want to wait a second so that the mark position broadcast will work
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {

                                    }
                                    sharedPrefs.edit().putInt("current_account", 2).commit();
                                    sharedPrefs.edit().remove("new_notifications").remove("new_retweets").remove("new_favorites").remove("new_follows").commit();
                                    AppSettings.invalidate();
                                    finish();
                                    Intent next = new Intent(context, MainActivity.class);
                                    startActivity(next);
                                }
                            }).start();

                        }
                    }
                });
            } else {
                name2.setText(sharedPrefs.getString("twitter_users_name_1", ""));
                screenname2.setText("@" + sharedPrefs.getString("twitter_screen_name_1", ""));
                try {
                    if(settings.roundContactImages) {
                        //proPic2.loadImage(sharedPrefs.getString("profile_pic_url_1", ""), true, null, NetworkedCacheableImageView.CIRCLE);
                        ImageUtils.loadCircleImage(context, proPic2, sharedPrefs.getString("profile_pic_url_1", ""), mCache);
                    } else {
                        //proPic2.loadImage(sharedPrefs.getString("profile_pic_url_1", ""), true, null);
                        ImageUtils.loadImage(context, proPic2, sharedPrefs.getString("profile_pic_url_1", ""), mCache);
                    }
                } catch (Exception e) {

                }
                secondAccount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (canSwitch) {
                            context.sendBroadcast(new Intent("com.klinker.android.twitter.STOP_PUSH_SERVICE"));
                            context.sendBroadcast(new Intent("com.klinker.android.twitter.MARK_POSITION").putExtra("current_account", current));

                            Toast.makeText(context, "Preparing to switch", Toast.LENGTH_SHORT).show();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {

                                    }

                                    sharedPrefs.edit().putInt("current_account", 1).commit();
                                    sharedPrefs.edit().remove("new_notifications").remove("new_retweets").remove("new_favorites").remove("new_follows").commit();
                                    AppSettings.invalidate();
                                    finish();
                                    Intent next = new Intent(context, MainActivity.class);
                                    startActivity(next);
                                }
                            }).start();
                        }
                    }
                });
            }
        }

        statusBar = findViewById(R.id.activity_status_bar);

        statusBarHeight = Utils.getStatusBarHeight(context);
        navBarHeight = Utils.getNavBarHeight(context);

        try {
            RelativeLayout.LayoutParams statusParams = (RelativeLayout.LayoutParams) statusBar.getLayoutParams();
            statusParams.height = statusBarHeight;
            statusBar.setLayoutParams(statusParams);
        } catch (Exception e) {
            try {
                LinearLayout.LayoutParams statusParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
                statusParams.height = statusBarHeight;
                statusBar.setLayoutParams(statusParams);
            } catch (Exception x) {
                // in the trends
            }
        }

        View navBarSeperater = findViewById(R.id.nav_bar_seperator);

        if (translucent && Utils.hasNavBar(context)) {
            try {
                RelativeLayout.LayoutParams navParams = (RelativeLayout.LayoutParams) navBarSeperater.getLayoutParams();
                navParams.height = navBarHeight;
                navBarSeperater.setLayoutParams(navParams);
            } catch (Exception e) {
                try {
                    LinearLayout.LayoutParams navParams = (LinearLayout.LayoutParams) navBarSeperater.getLayoutParams();
                    navParams.height = navBarHeight;
                    navBarSeperater.setLayoutParams(navParams);
                } catch (Exception x) {
                    // in the trends
                }
            }
        }

        if (translucent) {
            if (Utils.hasNavBar(context)) {
                View footer = new View(context);
                footer.setOnClickListener(null);
                footer.setOnLongClickListener(null);
                ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, Utils.getNavBarHeight(context));
                footer.setLayoutParams(params);
                drawerList.addFooterView(footer);
                drawerList.setFooterDividersEnabled(false);
            }

            View drawerStatusBar = findViewById(R.id.drawer_status_bar);
            LinearLayout.LayoutParams status2Params = (LinearLayout.LayoutParams) drawerStatusBar.getLayoutParams();
            status2Params.height = statusBarHeight;
            drawerStatusBar.setLayoutParams(status2Params);
            drawerStatusBar.setVisibility(View.VISIBLE);

            statusBar.setVisibility(View.VISIBLE);

            drawerStatusBar = findViewById(R.id.drawer_status_bar_2);
            status2Params = (LinearLayout.LayoutParams) drawerStatusBar.getLayoutParams();
            status2Params.height = statusBarHeight;
            drawerStatusBar.setLayoutParams(status2Params);
            drawerStatusBar.setVisibility(View.VISIBLE);
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE || getResources().getBoolean(R.bool.isTablet)) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if(!settings.pushNotifications) {
            try {
                mDrawerLayout.setDrawerLockMode(NotificationDrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);
            } catch (Exception e) {
                // no drawer?
            }
        } else {
            mDrawerLayout.setDrawerRightEdgeSize(this, .1f);

            try {
                if (Build.VERSION.SDK_INT < 18 && DrawerActivity.settings.uiExtras) {
                    View viewHeader2 = ((Activity)context).getLayoutInflater().inflate(R.layout.ab_header, null);
                    notificationList.addHeaderView(viewHeader2, null, false);
                    notificationList.setHeaderDividersEnabled(false);
                }
            } catch (Exception e) {
                // i don't know why it does this to be honest...
            }

            notificationAdapter = new InteractionsCursorAdapter(context,
                    InteractionsDataSource.getInstance(context).getUnreadCursor(DrawerActivity.settings.currentAccount));
            try {
                notificationList.setAdapter(notificationAdapter);
            } catch (Exception e) {

            }

            View viewHeader = ((Activity)context).getLayoutInflater().inflate(R.layout.interactions_footer_1, null);
            notificationList.addFooterView(viewHeader, null, false);
            oldInteractions = (HoloTextView) findViewById(R.id.old_interactions_text);
            readButton = (ImageView) findViewById(R.id.read_button);

            LinearLayout footer = (LinearLayout) viewHeader.findViewById(R.id.footer);
            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (oldInteractions.getText().toString().equals(getResources().getString(R.string.old_interactions))) {
                        oldInteractions.setText(getResources().getString(R.string.new_interactions));
                        readButton.setImageResource(closedMailResource);

                        notificationList.disableSwipeToDismiss();

                        notificationAdapter = new InteractionsCursorAdapter(context,
                                InteractionsDataSource.getInstance(context).getCursor(DrawerActivity.settings.currentAccount));
                    } else {
                        oldInteractions.setText(getResources().getString(R.string.old_interactions));
                        readButton.setImageResource(openMailResource);

                        notificationList.enableSwipeToDismiss();

                        notificationAdapter = new InteractionsCursorAdapter(context,
                                InteractionsDataSource.getInstance(context).getUnreadCursor(DrawerActivity.settings.currentAccount));
                    }

                    notificationList.setAdapter(notificationAdapter);
                }
            });

            if (DrawerActivity.translucent) {
                if (Utils.hasNavBar(context)) {
                    View nav= new View(context);
                    nav.setOnClickListener(null);
                    nav.setOnLongClickListener(null);
                    ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, Utils.getNavBarHeight(context));
                    nav.setLayoutParams(params);
                    notificationList.addFooterView(nav);
                    notificationList.setFooterDividersEnabled(false);
                }
            }

            notificationList.setDismissCallback(new EnhancedListView.OnDismissCallback() {
                @Override
                public EnhancedListView.Undoable onDismiss(EnhancedListView listView, int position) {
                    Log.v("talon_interactions_delete", "position to delete: " + position);
                    InteractionsDataSource data = InteractionsDataSource.getInstance(context);
                    data.markRead(settings.currentAccount, position);
                    notificationAdapter = new InteractionsCursorAdapter(context, data.getUnreadCursor(DrawerActivity.settings.currentAccount));
                    notificationList.setAdapter(notificationAdapter);

                    oldInteractions.setText(getResources().getString(R.string.old_interactions));
                    readButton.setImageResource(openMailResource);

                    if (notificationAdapter.getCount() == 0) {
                        setNotificationFilled(false);
                    }

                    return null;
                }
            });

            notificationList.enableSwipeToDismiss();
            notificationList.setSwipeDirection(EnhancedListView.SwipeDirection.START);

            notificationList.setOnItemClickListener(new InteractionClickListener(context, mDrawerLayout, mViewPager));
        }
    }

    public void setUpTheme() {

        actionBar = getActionBar();

        if (!getResources().getBoolean(R.bool.isTablet)) {
            actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        } else {
            actionBar.setIcon(R.mipmap.ic_launcher);
        }

        if (Build.VERSION.SDK_INT > 18 && settings.uiExtras && (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE || getResources().getBoolean(R.bool.isTablet)) && !MainActivity.isPopup) {
            translucent = true;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            try {
                int immersive = android.provider.Settings.System.getInt(getContentResolver(), "immersive_mode");

                if (immersive == 1) {
                    translucent = false;
                }
            } catch (Exception e) {
            }
        } else {
            translucent = false;
        }

        Utils.setUpTheme(context, settings);

        /*if (settings.addonTheme) {
            getWindow().getDecorView().setBackgroundColor(settings.backgroundColor);
        } else {
            TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.windowBackground});
            int resource = a.getResourceId(0, 0);
            a.recycle();

            getWindow().getDecorView().setBackgroundResource(resource);
        }*/

        // this is a super hacky workaround for the theme problems that some people were having... but it works ok
        int actionBarTitleId = 0;
        TextView title = null;
        try {
            actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        } catch (Exception e) {
            // just in case
        }

        if (actionBarTitleId > 0) {
            title = (TextView) findViewById(actionBarTitleId);
        }

        switch (settings.theme) {
            case AppSettings.THEME_LIGHT:
                getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_light_holo));
                if (title != null) {
                    title.setTextColor(Color.BLACK);
                }
                break;
            case AppSettings.THEME_DARK:
                getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_dark));
                if (title != null) {
                    title.setTextColor(Color.WHITE);
                }
                break;
            case AppSettings.THEME_BLACK:
                getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_black));
                if (title != null) {
                    title.setTextColor(Color.WHITE);
                }
                break;
        }

        Utils.setActionBar(context, true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        try {
            mDrawerToggle.syncState();
        } catch (Exception e) {
            // landscape mode
        }
    }

    private void logoutFromTwitter() {

        context.sendBroadcast(new Intent("com.klinker.android.STOP_PUSH_SERVICE"));

        int currentAccount = sharedPrefs.getInt("current_account", 1);
        boolean login1 = sharedPrefs.getBoolean("is_logged_in_1", false);
        boolean login2 = sharedPrefs.getBoolean("is_logged_in_2", false);

        // Delete the data for the logged out account
        SharedPreferences.Editor e = sharedPrefs.edit();
        e.remove("authentication_token_" + currentAccount);
        e.remove("authentication_token_secret_" + currentAccount);
        e.remove("is_logged_in_" + currentAccount);
        e.remove("new_notification");
        e.remove("new_retweets");
        e.remove("new_favorites");
        e.remove("new_follows");
        e.remove("current_position_" + currentAccount);
        e.commit();

        HomeDataSource homeSources = HomeDataSource.getInstance(context);
        homeSources.deleteAllTweets(currentAccount);

        MentionsDataSource mentionsSources = MentionsDataSource.getInstance(context);
        mentionsSources.deleteAllTweets(currentAccount);

        DMDataSource dmSource = DMDataSource.getInstance(context);
        dmSource.deleteAllTweets(currentAccount);

        FavoriteUsersDataSource favs = FavoriteUsersDataSource.getInstance(context);
        favs.deleteAllUsers(currentAccount);

        InteractionsDataSource inters = InteractionsDataSource.getInstance(context);
        inters.deleteAllInteractions(currentAccount);

        try {
            long account1List1 = sharedPrefs.getLong("account_" + currentAccount + "_list_1", 0l);
            long account1List2 = sharedPrefs.getLong("account_" + currentAccount + "_list_2", 0l);

            ListDataSource list = ListDataSource.getInstance(context);
            list.deleteAllTweets(account1List1);
            list.deleteAllTweets(account1List2);
        } catch (Exception x) {

        }

        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                MySuggestionsProvider.AUTHORITY, MySuggestionsProvider.MODE);
        suggestions.clearHistory();

        AppSettings.invalidate();

        if (currentAccount == 1 && login2) {
            e.putInt("current_account", 2).commit();
            finish();
            Intent next = new Intent(context, MainActivity.class);
            startActivity(next);
        } else if (currentAccount == 2 && login1) {
            e.putInt("current_account", 1).commit();
            finish();
            Intent next = new Intent(context, MainActivity.class);
            startActivity(next);
        } else { // only the one account
            e.putInt("current_account", 1).commit();
            finish();
            Intent login = new Intent(context, LoginActivity.class);
            startActivity(login);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        if (sharedPrefs.getBoolean("remake_me", false) && !MainActivity.isPopup) {
            sharedPrefs.edit().putBoolean("remake_me", false).commit();
            recreate();

            sharedPrefs.edit().putBoolean("launcher_frag_switch", false)
                              .putBoolean("dont_refresh", true).commit();

            return;
        }

        cancelTeslaUnread();

        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // cancels the notifications when the app is opened
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        SharedPreferences.Editor e = sharedPrefs.edit();
        e.putInt("new_followers", 0);
        e.putInt("new_favorites", 0);
        e.putInt("new_retweets", 0);
        e.putString("old_interaction_text", "");
        e.commit();

        DrawerActivity.settings = AppSettings.getInstance(context);
    }

    private SearchView searchView;
    private MenuItem searchItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        try {
            searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
            // Assumes current activity is the searchable activity
            searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchPager.class)));
            searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default

            int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
            ImageView view = (ImageView) searchView.findViewById(searchImgId);
            view.setImageResource(settings.theme == AppSettings.THEME_LIGHT ? R.drawable.ic_action_search_light : R.drawable.ic_action_search_dark);

        } catch (Exception e) {

        }

        try {
            Field searchField = SearchView.class.getDeclaredField("mCloseButton");
            searchField.setAccessible(true);
            closeBtn = (ImageView) searchField.get(searchView);

            int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            EditText searchPlate = (EditText) searchView.findViewById(searchPlateId);
            searchPlate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        closeBtn.callOnClick();
                    }
                }
            });

        } catch (Exception e) {

        }

        return super.onCreateOptionsMenu(menu);
    }

    private ImageView closeBtn;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        final int DISMISS = 0;
        final int SEARCH = 1;
        final int COMPOSE = 2;
        final int NOTIFICATIONS = 3;
        final int DM = 4;
        final int SETTINGS = 5;
        final int TOFIRST = 6;
        final int TWEETMARKER = 7;

        try {
            searchItem = menu.findItem(R.id.menu_search);
        } catch (Exception e) {
            searchItem = null;
        }

        menu.getItem(TWEETMARKER).setVisible(false);

        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT) || sharedPrefs.getBoolean("open_interactions", false)) {
            menu.getItem(DISMISS).setVisible(true);

            menu.getItem(SEARCH).setVisible(false);
            menu.getItem(COMPOSE).setVisible(false);
            menu.getItem(DM).setVisible(false);
            menu.getItem(TOFIRST).setVisible(false);

            if (settings.pushNotifications) {
                menu.getItem(NOTIFICATIONS).setVisible(true);
            } else {
                menu.getItem(NOTIFICATIONS).setVisible(false);
            }

        } else {
            menu.getItem(DISMISS).setVisible(false);

            menu.getItem(SEARCH).setVisible(true);
            menu.getItem(COMPOSE).setVisible(true);
            menu.getItem(DM).setVisible(true);

            if (!settings.pushNotifications) {
                menu.getItem(NOTIFICATIONS).setVisible(false);
            } else {
                if (settings.floatingCompose || getResources().getBoolean(R.bool.isTablet)) {
                    menu.getItem(NOTIFICATIONS).setVisible(true);
                } else {
                    menu.getItem(NOTIFICATIONS).setVisible(false);
                }
            }
        }

        // to first button in overflow instead of the toast
        if (MainDrawerArrayAdapter.current > adapter.pageTypes.size() || (settings.uiExtras && settings.useToast)) {
            menu.getItem(TOFIRST).setVisible(false);
        } else {
            menu.getItem(TOFIRST).setVisible(true);
        }

        if (MainActivity.isPopup) {
            menu.getItem(SETTINGS).setVisible(false); // hide the settings button if the popup is up
            menu.getItem(SEARCH).setVisible(false); // hide the search button in popup

            // disable the left drawer so they can't switch activities in the popup.
            // causes problems with the layouts
            mDrawerLayout.setDrawerLockMode(NotificationDrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }

        noti = menu.getItem(NOTIFICATIONS);

        if (InteractionsDataSource.getInstance(context).getUnreadCount(settings.currentAccount) > 0) {
            setNotificationFilled(true);
        } else {
            setNotificationFilled(false);
        }

        return true;
    }

    public MenuItem noti;
    public void setNotificationFilled(boolean isFilled) {
        if (isFilled) {
            TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.notification_button});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            noti.setIcon(resource);
        } else {
            TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.notification_button_empty});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            noti.setIcon(resource);
        }
    }

    public static final int SETTINGS_RESULT = 101;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                }
                return true;
            }
        } catch (Exception e) {
            // landscape
        }

        switch (item.getItemId()) {
            case R.id.menu_search:
                overridePendingTransition(0,0);
                finish();
                overridePendingTransition(0,0);
                return super.onOptionsItemSelected(item);

            case R.id.menu_compose:
                Intent compose = new Intent(context, ComposeActivity.class);
                sharedPrefs.edit().putBoolean("from_notification_bool", false).commit();
                startActivity(compose);
                return super.onOptionsItemSelected(item);

            case R.id.menu_direct_message:
                Intent dm = new Intent(context, ComposeDMActivity.class);
                sharedPrefs.edit().putBoolean("from_notification_bool", false).commit();
                startActivity(dm);
                return super.onOptionsItemSelected(item);

            case R.id.menu_settings:
                context.sendBroadcast(new Intent("com.klinker.android.twitter.MARK_POSITION"));
                Intent settings = new Intent(context, SettingsActivity.class);
                finish();
                sharedPrefs.edit().putBoolean("should_refresh", false).commit();
                overridePendingTransition(R.anim.slide_in_left, R.anim.activity_zoom_exit);
                startActivity(settings);
                return super.onOptionsItemSelected(item);

            case R.id.menu_dismiss:
                InteractionsDataSource data = InteractionsDataSource.getInstance(context);
                data.markAllRead(DrawerActivity.settings.currentAccount);
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
                notificationAdapter = new InteractionsCursorAdapter(context, data.getUnreadCursor(DrawerActivity.settings.currentAccount));
                notificationList.setAdapter(notificationAdapter);

                return super.onOptionsItemSelected(item);

            case R.id.menu_notifications:
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }

                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                }

                return super.onOptionsItemSelected(item);

            case R.id.menu_to_first:
                context.sendBroadcast(new Intent("com.klinker.android.twitter.TOP_TIMELINE"));
                return super.onOptionsItemSelected(item);

            case R.id.menu_tweetmarker:
                context.sendBroadcast(new Intent("com.klinker.android.twitter.TWEETMARKER"));
                return super.onOptionsItemSelected(item);

            case R.id.menu_get_help:
                new AlertDialog.Builder(context)
                        .setTitle(R.string.faq)
                        .setMessage(R.string.faq_first)
                        .setPositiveButton("FAQ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                XmlFaqUtils.showFaqDialog(context);
                            }
                        })
                        .setNegativeButton(R.string.contact, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showContactUsDialog();
                            }
                        })
                        .setNeutralButton(R.string.follow, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showFollowDialog();
                            }
                        })
                        .create()
                        .show();
                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showContactUsDialog() {
        new AlertDialog.Builder(context)
                .setItems(new CharSequence[] {"Twitter", "Google+", "Email"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            final Intent tweet = new Intent(context, ComposeActivity.class);
                            new AlertDialog.Builder(context)
                                    .setItems(new CharSequence[] {"@TalonAndroid", "@lukeklinker"}, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0) {
                                                tweet.putExtra("user", "@TalonAndroid");
                                            } else {
                                                tweet.putExtra("user", "@lukeklinker");
                                            }
                                            startActivity(tweet);
                                        }
                                    })
                                    .create()
                                    .show();
                        } else if (i == 1) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://goo.gl/KCXlZk")));
                        } else {
                            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"support@klinkerapps.com"});
                            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Talon (Classic)");
                            emailIntent.setType("plain/text");

                            startActivity(emailIntent);
                        }
                    }
                })
                .create()
                .show();
    }

    public void showFollowDialog() {
        new AlertDialog.Builder(context)
                .setItems(new CharSequence[] {"@TalonAndroid", "@lukeklinker", "Luke's Google+"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0) {
                            // talon
                            Intent profile = new Intent(context, ProfilePager.class);
                            profile.putExtra("screenname", "TalonAndroid");
                            profile.putExtra("proPic", "");
                            startActivity(profile);
                        } else if (i == 1) {
                            // luke (twitter)
                            Intent profile = new Intent(context, ProfilePager.class);
                            profile.putExtra("screenname", "lukeklinker");
                            profile.putExtra("proPic", "");
                            startActivity(profile);
                        } else {
                            // luke (google+)
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com/+LukeKlinker")));
                        }

                    }
                })
                .create()
                .show();
    }

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }

    public void showStatusBar() {
        DrawerActivity.statusBar.setVisibility(View.VISIBLE);
    }

    public void hideStatusBar() {
        DrawerActivity.statusBar.setVisibility(View.GONE);
    }

    public void cancelTeslaUnread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentValues cv = new ContentValues();
                    cv.put("tag", "com.klinker.android.twitter/com.klinker.android.twitter.ui.MainActivity");
                    cv.put("count", 0); // back to zero

                    context.getContentResolver().insert(Uri
                                    .parse("content://com.teslacoilsw.notifier/unread_count"),
                            cv);
                } catch (IllegalArgumentException ex) {
                    /* Fine, TeslaUnread is not installed. */
                } catch (Exception ex) {
                    /* Some other error, possibly because the format
                       of the ContentValues are incorrect.

                        Log but do not crash over this. */
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (closeBtn != null) {
            closeBtn.callOnClick();
        }
    }
}
