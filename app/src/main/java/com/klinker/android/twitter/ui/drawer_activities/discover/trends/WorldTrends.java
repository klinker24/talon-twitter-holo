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

package com.klinker.android.twitter.ui.drawer_activities.discover.trends;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.TrendsArrayAdapter;
import com.klinker.android.twitter.data.sq_lite.HashtagDataSource;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.utils.Utils;

import org.lucasr.smoothie.AsyncListView;

import java.util.ArrayList;

import twitter4j.Trend;
import twitter4j.Twitter;

public class WorldTrends extends Fragment {

    private Context context;
    private SharedPreferences sharedPrefs;
    private AppSettings settings;

    private AsyncListView listView;
    private View layout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        settings = AppSettings.getInstance(context);

        layout = inflater.inflate(R.layout.trends_list_view, null);

        listView = (AsyncListView) layout.findViewById(R.id.listView);

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

        getTrends();

        return layout;
    }

    public void getTrends() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);

                    twitter4j.Trends trends = twitter.getPlaceTrends(1);
                    final ArrayList<String> currentTrends = new ArrayList<String>();

                    for(Trend t: trends.getTrends()){
                        String name = t.getName();
                        currentTrends.add(name);
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(currentTrends != null) {
                                listView.setAdapter(new TrendsArrayAdapter(context, currentTrends));
                            }

                            listView.setVisibility(View.VISIBLE);

                            LinearLayout spinner = (LinearLayout) layout.findViewById(R.id.list_progress);
                            spinner.setVisibility(View.GONE);
                        }
                    });

                    HashtagDataSource source = HashtagDataSource.getInstance(context);

                    for (String s : currentTrends) {
                        if (s.contains("#")) {
                            // we want to add it to the userAutoComplete
                            Log.v("talon_hashtag", "adding: " + s);

                            // could be much more efficient by querying and checking first, but I
                            // just didn't feel like it when there is only ever 10 of them here
                            source.deleteTag(s);

                            // add it to the userAutoComplete database
                            source.createTag(s);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }
}
