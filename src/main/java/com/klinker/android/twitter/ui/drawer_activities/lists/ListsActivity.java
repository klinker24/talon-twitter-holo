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

package com.klinker.android.twitter.ui.drawer_activities.lists;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.ListsArrayAdapter;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.setup.LoginActivity;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.manipulations.widgets.HoloEditText;
import com.klinker.android.twitter.utils.Utils;

import org.lucasr.smoothie.AsyncListView;

import java.util.Collections;
import java.util.Comparator;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.UserList;

public class ListsActivity extends DrawerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        settings = AppSettings.getInstance(this);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setUpTheme();
        setContentView(R.layout.twitter_lists_page);
        setUpDrawer(4, getResources().getString(R.string.lists));

        actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.lists));


        if (!settings.isTwitterLoggedIn) {
            Intent login = new Intent(context, LoginActivity.class);
            startActivity(login);
            finish();
        }

        listView = (AsyncListView) findViewById(R.id.listView);
        View viewHeader = getLayoutInflater().inflate(R.layout.ab_header, null);
        listView.addHeaderView(viewHeader, null, false);
        listView.setHeaderDividersEnabled(false);

        if (DrawerActivity.translucent) {
            if (Utils.hasNavBar(context)) {
                View footer = new View(context);
                footer.setOnClickListener(null);
                footer.setOnLongClickListener(null);
                ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, Utils.getNavBarHeight(context));
                footer.setLayoutParams(params);
                listView.addFooterView(footer);
                listView.setFooterDividersEnabled(false);
            }
        }

        getLists();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final int NOTIFICATIONS = 3;
        menu.getItem(NOTIFICATIONS).setVisible(false);

        return true;
    }

    private boolean clicked = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        } catch (Exception e) {

        }

        switch (item.getItemId()) {
            case R.id.menu_add_list:
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.create_list_dialog);
                dialog.setTitle(getResources().getString(R.string.create_new_list) + ":");

                final HoloEditText name = (HoloEditText) dialog.findViewById(R.id.name);
                final HoloEditText description = (HoloEditText) dialog.findViewById(R.id.description);

                Button cancel = (Button) dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!clicked) {
                            dialog.dismiss();
                        }
                        clicked = true;
                    }
                });

                Button privateBtn = (Button) dialog.findViewById(R.id.private_btn);
                privateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!clicked) {
                            new CreateList(name.getText().toString(), false, description.getText().toString()).execute();
                            dialog.dismiss();
                        }
                        clicked = true;
                    }
                });

                Button publicBtn = (Button) dialog.findViewById(R.id.public_btn);
                publicBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!clicked) {
                            new CreateList(name.getText().toString(), true, description.getText().toString()).execute();
                            dialog.dismiss();
                        }
                        clicked = true;
                    }
                });

                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getLists() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);

                    final ResponseList<UserList> lists;
                    try {
                        lists = twitter.getUserLists(settings.myScreenName);
                    } catch (OutOfMemoryError e) {
                        return;
                    }

                    Collections.sort(lists, new Comparator<UserList>() {
                        public int compare(UserList result1, UserList result2) {
                            return result1.getName().compareTo(result2.getName());
                        }
                    });

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(lists.size() > 0) {
                                listView.setAdapter(new ListsArrayAdapter(context, lists));
                                listView.setVisibility(View.VISIBLE);
                            } else {
                                LinearLayout nothing = (LinearLayout) findViewById(R.id.no_content);
                                try {
                                    nothing.setVisibility(View.VISIBLE);
                                } catch (Exception e) {

                                }
                                listView.setVisibility(View.GONE);
                            }

                            LinearLayout spinner = (LinearLayout) findViewById(R.id.list_progress);
                            spinner.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout nothing = (LinearLayout) findViewById(R.id.no_content);
                            nothing.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);

                            LinearLayout spinner = (LinearLayout) findViewById(R.id.list_progress);
                            spinner.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    class CreateList extends AsyncTask<String, Void, Boolean> {

        String name;
        String description;
        boolean publicList;

        public CreateList(String name, boolean publicList, String description) {
            this.name = name;
            this.publicList = publicList;
            this.description = description;
        }

        protected Boolean doInBackground(String... urls) {
            try {
                Twitter twitter =  Utils.getTwitter(context, settings);

                twitter.createUserList(name, publicList, description);

                return true;
            } catch (Exception e) {
                return false;
            }
        }

        protected void onPostExecute(Boolean created) {
            if (created) {
                recreate();
            } else {
                Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            mDrawerToggle.onConfigurationChanged(newConfig);
        } catch (Exception e) { }

        overridePendingTransition(0,0);
        finish();
        Intent restart = new Intent(context, ListsActivity.class);
        restart.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        overridePendingTransition(0, 0);
        startActivity(restart);
    }
}
