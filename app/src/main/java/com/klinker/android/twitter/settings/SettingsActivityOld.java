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

package com.klinker.android.twitter.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.klinker.android.launcher.api.BaseLauncherPage;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.manipulations.widgets.HoloTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivityOld extends FragmentActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;
    SharedPreferences sharedPrefs;

    private DrawerLayout mDrawerLayout;
    private ListView otherList;
    private ListView settingsList;
    private LinearLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private boolean userKnows;
    public static boolean settingsLinksActive = true;
    public static boolean inOtherLinks = true;

    private String[] linkItems;
    private String[] settingsItems;

    public static ViewPager mViewPager;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_zoom_enter, R.anim.slide_out_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.slide_in_left, R.anim.activity_zoom_exit);

        AppSettings.invalidate();

        setUpTheme();

        setContentView(R.layout.settings_main);

        DrawerArrayAdapter.current = 0;

        linkItems = new String[]{
                getResources().getString(R.string.get_help_settings),
                getResources().getString(R.string.other_apps),
                getResources().getString(R.string.whats_new),
                getResources().getString(R.string.rate_it)
        };

        settingsItems = new String[] {
                getResources().getString(R.string.ui_settings),
                getResources().getString(R.string.timelines_settings),
                getResources().getString(R.string.sync_settings),
                getResources().getString(R.string.notification_settings),
                getResources().getString(R.string.browser_settings),
                getResources().getString(R.string.advanced_settings),
                getResources().getString(R.string.memory_manage)
        };

        sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        if (sharedPrefs.getBoolean("version_1.20_1", true)) {
            // necessary because i didnt start out by using sets
            boolean led = sharedPrefs.getBoolean("led", true);
            boolean sound = sharedPrefs.getBoolean("sound", true);
            boolean vibrate = sharedPrefs.getBoolean("vibrate", true);
            boolean wakeScreen = sharedPrefs.getBoolean("wake", true);
            boolean timelineNot = sharedPrefs.getBoolean("timeline_notifications", true);
            boolean mentionsNot = sharedPrefs.getBoolean("mentions_notifications", true);
            boolean dmsNot = sharedPrefs.getBoolean("direct_message_notifications", true);
            boolean favoritesNot = sharedPrefs.getBoolean("favorite_notifications", true);
            boolean retweetNot = sharedPrefs.getBoolean("retweet_notifications", true);
            boolean followersNot = sharedPrefs.getBoolean("follower_notifications", true);

            Set<String> alert = sharedPrefs.getStringSet("alert_types", new HashSet<String>());
            alert.clear();
            if (vibrate) {
                alert.add("1");
            }
            if (led) {
                alert.add("2");
            }
            if (wakeScreen) {
                alert.add("3");
            }
            if (sound) {
                alert.add("4");
            }
            sharedPrefs.edit().putStringSet("alert_types", alert).commit();

            Set<String> timeline = sharedPrefs.getStringSet("timeline_set", new HashSet<String>());
            timeline.clear();
            if (timelineNot) {
                timeline.add("1");
            }
            if (mentionsNot) {
                timeline.add("2");
            }
            if (dmsNot) {
                timeline.add("3");
            }
            sharedPrefs.edit().putStringSet("timeline_set", timeline).commit();

            Set<String> interactions = sharedPrefs.getStringSet("interactions_set", new HashSet<String>());
            interactions.clear();
            if (favoritesNot) {
                interactions.add("1");
            }
            if (retweetNot) {
                interactions.add("2");
            }
            if (followersNot) {
                interactions.add("3");
            }
            sharedPrefs.edit().putStringSet("interactions_set", interactions).commit();

            sharedPrefs.edit().putBoolean("version_1.20_1", false).commit();

            recreate();
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getFragmentManager(), this, otherList);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        otherList = (ListView) findViewById(R.id.other_list);
        settingsList = (ListView) findViewById(R.id.settings_list);
        mDrawer = (LinearLayout) findViewById(R.id.drawer);

        // Set the adapter for the list view
        otherList.setAdapter(new DrawerArrayAdapter(this,
                new ArrayList<String>(Arrays.asList(linkItems))));
        settingsList.setAdapter(new DrawerArrayAdapter(this,
                new ArrayList<String>(Arrays.asList(settingsItems))));
        // Set the list's click listener
        settingsList.setOnItemClickListener(new SettingsDrawerClickListener(this, mDrawerLayout, settingsList, mViewPager, mDrawer));
        otherList.setOnItemClickListener(new SettingsLinkDrawerClickListener(this, mDrawerLayout, otherList, mViewPager, mDrawer));

        findViewById(R.id.settingsLinks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToSettingsList(true);
                settingsLinksActive = true;
                findViewById(R.id.settingsSelector).setVisibility(View.VISIBLE);
                findViewById(R.id.otherSelector).setVisibility(View.INVISIBLE);
            }
        });

        findViewById(R.id.otherLinks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToSettingsList(false);
                settingsLinksActive = false;
                findViewById(R.id.settingsSelector).setVisibility(View.INVISIBLE);
                findViewById(R.id.otherSelector).setVisibility(View.VISIBLE);
            }
        });

        if (settingsLinksActive) {
            settingsList.setVisibility(View.VISIBLE);
            otherList.setVisibility(View.GONE);
            findViewById(R.id.settingsSelector).setVisibility(View.VISIBLE);
            findViewById(R.id.otherSelector).setVisibility(View.INVISIBLE);
        } else {
            settingsList.setVisibility(View.GONE);
            otherList.setVisibility(View.VISIBLE);
            findViewById(R.id.settingsSelector).setVisibility(View.INVISIBLE);
            findViewById(R.id.otherSelector).setVisibility(View.VISIBLE);
        }

        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.drawerIcon});
        int resource = a.getResourceId(0, 0);
        a.recycle();

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                resource,  /* nav drawer icon to replace 'Up' caret */
                R.string.app_name,  /* "open drawer" description */
                R.string.app_name  /* "close drawer" description */
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        userKnows = sharedPrefs.getBoolean("user_knows_navigation_drawer", false);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                DrawerArrayAdapter.current = position;
                otherList.invalidateViews();
                settingsList.invalidateViews();
            }
        });

        if (!userKnows) {
            mDrawerLayout.openDrawer(mDrawer);
        }

        HoloTextView createdBy = (HoloTextView) findViewById(R.id.created_by);
        HoloTextView versionNumber = (HoloTextView) findViewById(R.id.version_number);

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

            String text = getResources().getString(R.string.created_by) + " Luke Klinker";
            String text2 = getResources().getString(R.string.version) + " " + versionName;
            createdBy.setText(text);
            versionNumber.setText(text2);
        } catch (Exception e) {
            String text = getResources().getString(R.string.created_by) + " Luke Klinker";
            String text2 = getResources().getString(R.string.version) + " 0.00";
            createdBy.setText(text);
            versionNumber.setText(text2);
        }

        LinearLayout description = (LinearLayout) findViewById(R.id.created_by_layout);
        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Klinker+Apps")));
            }
        });

        mDrawerLayout.openDrawer(Gravity.START);
    }

    public void setUpTheme() {

        AppSettings settings = AppSettings.getInstance(this);

        switch (settings.theme) {
            case AppSettings.THEME_LIGHT:
                setTheme(R.style.Theme_TalonLight);
                break;
            case AppSettings.THEME_DARK:
                setTheme(R.style.Theme_TalonDark);
                break;
            case AppSettings.THEME_BLACK:
                setTheme(R.style.Theme_TalonBlack);
                break;
        }

        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.windowBackground});
        int resource = a.getResourceId(0, 0);
        a.recycle();

        getWindow().getDecorView().setBackgroundResource(resource);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                if (mDrawerToggle.onOptionsItemSelected(item)) {
                    if (!userKnows) {
                        userKnows = true;

                        sharedPrefs.edit().putBoolean("user_knows_navigation_drawer", true).commit();
                    }
                    return true;
                }

                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onBackPressed() {
        AppSettings.invalidate();
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }

    private static final int ANIM_TIME = 300;

    private void switchToSettingsList(boolean settings) {
        if (settings && settingsList.getVisibility() != View.VISIBLE) {
            // animate the settings list showing and other list hiding
            Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            Animation out = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
            in.setDuration(ANIM_TIME * 2);
            out.setDuration(ANIM_TIME);
            in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    settingsList.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    otherList.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    otherList.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            settingsList.startAnimation(in);
            otherList.startAnimation(out);
        } else if (!settings && otherList.getVisibility() != View.VISIBLE) {
            // animate the other list showing and settings list hiding
            Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            Animation out = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
            in.setDuration(ANIM_TIME * 2);
            out.setDuration(ANIM_TIME);
            in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    otherList.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    settingsList.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    settingsList.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            settingsList.startAnimation(out);
            otherList.startAnimation(in);
        }
    }
}