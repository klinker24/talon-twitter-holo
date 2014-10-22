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

package com.klinker.android.twitter.listeners;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.MainDrawerArrayAdapter;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.drawer_activities.FavoriteUsersActivity;
import com.klinker.android.twitter.ui.drawer_activities.FavoritesActivity;
import com.klinker.android.twitter.ui.drawer_activities.RetweetActivity;
import com.klinker.android.twitter.ui.drawer_activities.SavedSearchesActivity;
import com.klinker.android.twitter.ui.drawer_activities.discover.DiscoverPager;
import com.klinker.android.twitter.ui.drawer_activities.lists.ListsActivity;
import com.klinker.android.twitter.manipulations.widgets.NotificationDrawerLayout;

public class MainDrawerClickListener implements AdapterView.OnItemClickListener {

    private Context context;
    private NotificationDrawerLayout drawer;
    private ViewPager viewPager;
    private boolean noWait;
    private int extraPages = 0;

    private SharedPreferences sharedPreferences;

    public MainDrawerClickListener(Context context, NotificationDrawerLayout drawer, ViewPager viewPager) {
        this.context = context;
        this.drawer = drawer;
        this.viewPager = viewPager;
        this.noWait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ||
                context.getResources().getBoolean(R.bool.isTablet);
        sharedPreferences = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        int currentAccount = sharedPreferences.getInt("current_account", 1);

        int page1Type = sharedPreferences.getInt("account_" + currentAccount + "_page_1", AppSettings.PAGE_TYPE_NONE);
        int page2Type = sharedPreferences.getInt("account_" + currentAccount + "_page_2", AppSettings.PAGE_TYPE_NONE);

        if (page1Type != AppSettings.PAGE_TYPE_NONE) {
            extraPages++;
        }

        if (page2Type != AppSettings.PAGE_TYPE_NONE) {
            extraPages++;
        }

        Log.v("talon_lists", extraPages + " extra pages");

    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        context.sendBroadcast(new Intent("com.klinker.android.twitter.MARK_POSITION"));
        if (i < 3) {
            if (MainDrawerArrayAdapter.current < 3) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            drawer.closeDrawer(Gravity.START);
                        } catch (Exception e) {
                            // landscape mode
                        }
                    }
                }, noWait ? 0 : 300);

                viewPager.setCurrentItem(i + extraPages, true);
            } else {
                final int pos = i;
                try {
                    drawer.closeDrawer(Gravity.START);
                } catch (Exception e) {
                    // landscape mode
                }

                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("page_to_open", pos + extraPages);
                intent.putExtra("from_drawer", true);

                sharedPreferences.edit().putBoolean("should_refresh", false).commit();

                final Intent fIntent = intent;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            context.startActivity(fIntent);
                            ((Activity) context).overridePendingTransition(0, 0);
                            ((Activity) context).finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, noWait ? 0 : 400);

            }
        } else {
            final int pos = i;
            try {
                drawer.closeDrawer(Gravity.START);
            } catch (Exception e) {
                // landscape mode
            }
            Intent intent = null;

            switch (pos) {
                case 3:
                    intent = new Intent(context, DiscoverPager.class);
                    break;
                case 4:
                    intent = new Intent(context, ListsActivity.class);
                    break;
                case 5:
                    intent = new Intent(context, FavoriteUsersActivity.class);
                    break;
                case 6:
                    intent = new Intent(context, RetweetActivity.class);
                    break;
                case 7:
                    intent = new Intent(context, FavoritesActivity.class);
                    break;
                case 8:
                    intent = new Intent(context, SavedSearchesActivity.class);
                    break;
            }

            final Intent fIntent = intent;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        context.startActivity(fIntent);
                        ((Activity)context).finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, noWait ? 0 : 400);

        }

    }
}
