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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.ListsArrayAdapter;
import com.klinker.android.twitter.adapters.SavedSearchArrayAdapter;
import com.klinker.android.twitter.adapters.TrendsArrayAdapter;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.Utils;

import org.lucasr.smoothie.AsyncListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Twitter;
import twitter4j.UserList;


public class SavedSearchesActivity extends DrawerActivity {

    public static AsyncListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.context = this;
        SavedSearchesActivity.context = this;
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        settings = AppSettings.getInstance(this);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setUpTheme();
        setContentView(R.layout.twitter_lists_page);
        setUpDrawer(5, getResources().getString(R.string.saved_searches));

        actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.saved_searches));

        listView = (AsyncListView) findViewById(R.id.listView);
        View viewHeader = getLayoutInflater().inflate(R.layout.ab_header, null);
        listView.addHeaderView(viewHeader, null, false);
        listView.setHeaderDividersEnabled(false);


        nothing = (LinearLayout) findViewById(R.id.no_content);
        spinner = (LinearLayout) findViewById(R.id.list_progress);

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

        getSearches();

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

        return super.onOptionsItemSelected(item);
    }

    public static SavedSearchArrayAdapter adapter;
    public static LinearLayout nothing;
    public static LinearLayout spinner;
    public static Context context;

    public static void getSearches() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);

                    final ResponseList<SavedSearch> searches = twitter.getSavedSearches();

                    Collections.sort(searches, new Comparator<SavedSearch>() {
                        public int compare(SavedSearch result1, SavedSearch result2) {
                            return result1.getQuery().compareTo(result2.getQuery());
                        }
                    });

                    final ArrayList<String> searchNames = new ArrayList<String>();

                    for (SavedSearch sear : searches) {
                        searchNames.add(sear.getQuery());
                    }

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (searchNames.size() > 0) {
                                adapter = new SavedSearchArrayAdapter(context, searchNames);
                                listView.setAdapter(adapter);
                                listView.setVisibility(View.VISIBLE);
                            } else {
                                try {
                                    nothing.setVisibility(View.VISIBLE);
                                } catch (Exception e) {

                                }
                                listView.setVisibility(View.GONE);
                            }

                            spinner.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            mDrawerToggle.onConfigurationChanged(newConfig);
        } catch (Exception e) { }

        overridePendingTransition(0,0);
        finish();
        Intent restart = new Intent(context, SavedSearchesActivity.class);
        restart.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        overridePendingTransition(0, 0);
        startActivity(restart);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final int DISMISS = 0;
        final int SEARCH = 1;
        final int COMPOSE = 2;
        final int NOTIFICATIONS = 3;
        final int DM = 4;
        final int SETTINGS = 5;
        final int TOFIRST = 6;

        menu.getItem(NOTIFICATIONS).setVisible(false);

        return true;
    }
}