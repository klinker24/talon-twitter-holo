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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.MainDrawerArrayAdapter;
import com.klinker.android.twitter.adapters.TimelinePagerAdapter;
import com.klinker.android.twitter.data.sq_lite.InteractionsDataSource;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.profile_viewer.ProfilePager;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.manipulations.widgets.HoloTextView;
import com.klinker.android.twitter.manipulations.widgets.NotificationDrawerLayout;

public class InteractionClickListener implements AdapterView.OnItemClickListener {

    private Context context;
    private NotificationDrawerLayout drawer;
    private ViewPager viewPager;
    private int mentionsPage = 0;

    private SharedPreferences sharedPreferences;

    public InteractionClickListener(Context context, NotificationDrawerLayout drawer, ViewPager viewPager) {
        this.context = context;
        this.drawer = drawer;
        this.viewPager = viewPager;

        sharedPreferences = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        int currentAccount = sharedPreferences.getInt("current_account", 1);

        for (int i = 0; i < TimelinePagerAdapter.MAX_EXTRA_PAGES; i++) {
            String pageIdentifier = "account_" + currentAccount + "_page_" + (i + 1);
            int type = sharedPreferences.getInt(pageIdentifier, AppSettings.PAGE_TYPE_NONE);

            if (type == AppSettings.PAGE_TYPE_MENTIONS) {
                mentionsPage = i;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        HoloTextView title = (HoloTextView) view.findViewById(R.id.title);
        String mTitle = title.getText().toString();

        HoloTextView text = (HoloTextView) view.findViewById(R.id.text);
        String mText = text.getText().toString();

        // get the datasource ready to read/write
        InteractionsDataSource data = InteractionsDataSource.getInstance(context);

        if(mTitle.contains(context.getResources().getString(R.string.mentioned_by))) { // this is a mention
            if (MainDrawerArrayAdapter.current < 3) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            drawer.closeDrawer(Gravity.END);
                        } catch (Exception e) {
                            // landscape mode
                        }
                    }
                }, 300);

                viewPager.setCurrentItem((mentionsPage), true);
            } else {
                final int pos = i;
                try {
                    drawer.closeDrawer(Gravity.END);
                } catch (Exception e) {
                    // landscape mode
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.putExtra("page_to_open", mentionsPage);
                        intent.putExtra("from_drawer", true);

                        sharedPreferences.edit().putBoolean("should_refresh", false).commit();

                        try {
                            Thread.sleep(400);
                        } catch (Exception e) {

                        }

                        try {
                            context.startActivity(intent);
                            ((Activity)context).overridePendingTransition(0,0);
                            ((Activity)context).finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        } else if (mTitle.contains(context.getResources().getString(R.string.retweeted)) ||
                mTitle.contains(context.getResources().getString(R.string.favorited)) ||
                mTitle.contains(context.getResources().getString(R.string.new_favorites)) ||
                mTitle.contains(context.getResources().getString(R.string.new_retweets))) { // it is a retweet or favorite

            try {
                drawer.closeDrawer(Gravity.END);
            } catch (Exception e) {
                // landscape mode
            }

            // open up the dialog with the users that retweeted it

            final String[] fItems = data.getUsers(sharedPreferences.getInt("current_account", 1),
                    i,
                    DrawerActivity.oldInteractions.getText().toString().equals(context.getResources().getString(R.string.old_interactions))).split(" ");

            LayoutInflater factory = LayoutInflater.from(context);
            View content = factory.inflate(R.layout.interaction_dialog, null);

            TextView textView = (TextView) content.findViewById(R.id.text);
            textView.setText(mText);

            ListView lv = (ListView) content.findViewById(R.id.list);
            lv.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_selectable_list_item, fItems));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int item, long l) {
                    String touched = fItems[item];

                    Intent user = new Intent(context, ProfilePager.class);
                    user.putExtra("screenname", touched.replace("@", "").replace(" ", ""));
                    user.putExtra("proPic", "");
                    context.startActivity(user);
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(content);

            AlertDialog alert = builder.create();
            alert.show();
        } else if (mTitle.contains(context.getResources().getString(R.string.followed))) { // someone new followed you

            try {
                drawer.closeDrawer(Gravity.END);
            } catch (Exception e) {
                // landscape mode
            }

            // a new follower, open up the followers profile
            String username = mTitle.substring(mTitle.indexOf("@") + 1, mTitle.indexOf(" "));

            Intent user = new Intent(context, ProfilePager.class);
            user.putExtra("screenname", username);
            user.putExtra("proPic", "");
            context.startActivity(user);
        } else if (mTitle.contains(context.getResources().getString(R.string.tweeted))) {
            try {
                drawer.closeDrawer(Gravity.END);
            } catch (Exception e) {
                // landscape mode
            }

            // a new follower, open up the followers profile
            String username = mTitle.substring(mTitle.indexOf("@") + 1, mTitle.indexOf(" "));

            Intent user = new Intent(context, ProfilePager.class);
            user.putExtra("screenname", username);
            user.putExtra("proPic", "");
            context.startActivity(user);
        }

        // mark it read in the sql database
        data.markRead(sharedPreferences.getInt("current_account", 1), i);

        // tell the system to refresh the notifications when the user opens the drawer again
        sharedPreferences.edit().putBoolean("new_notification", true).commit();
    }
}
