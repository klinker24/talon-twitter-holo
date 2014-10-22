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

package com.klinker.android.twitter.ui.profile_viewer.fragments.sub_fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.PicturesArrayAdapter;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.TweetLinkUtils;
import com.klinker.android.twitter.utils.Utils;

import org.lucasr.smoothie.AsyncListView;

import java.util.ArrayList;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;

public class ProfilePicturesFragment extends Fragment {

    public View layout;
    public Context context;
    public AppSettings settings;
    public SharedPreferences sharedPrefs;

    public AsyncListView listView;
    public LinearLayout spinner;

    public String screenName;

    public ProfilePicturesFragment() {
        this.screenName = "";
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        screenName = getArguments().getString("screen_name");

        settings = AppSettings.getInstance(context);
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        inflater = LayoutInflater.from(context);

        layout = inflater.inflate(R.layout.pictures_fragment, null);

        listView = (AsyncListView) layout.findViewById(R.id.listView);
        spinner = (LinearLayout) layout.findViewById(R.id.spinner);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;

                if(lastItem == totalItemCount && canRefresh) {
                    getMore();
                }
            }
        });

        final LinearLayout getTweets = (LinearLayout) layout.findViewById(R.id.load_tweets);
        final Button getPics = (Button) layout.findViewById(R.id.get_pics);
        getPics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTweets.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);
                doSearch();
            }
        });

        return layout;
    }

    public ArrayList<Status> tweets = new ArrayList<Status>();
    public ArrayList<String> pics = new ArrayList<String>();
    public ArrayList<Status> tweetsWithPics = new ArrayList<Status>();
    public Paging paging = new Paging(1, 60);
    public boolean hasMore = true;
    public boolean canRefresh = false;
    public PicturesArrayAdapter adapter;

    public void doSearch() {
        spinner.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter = Utils.getTwitter(context, settings);

                    ResponseList<Status> result;
                    try {
                        result = twitter.getUserTimeline(screenName, paging);
                    } catch (OutOfMemoryError e) {
                        return;
                    }

                    tweets.clear();

                    for (twitter4j.Status status : result) {
                        tweets.add(status);
                    }

                    if (result.size() > 17) {
                        hasMore = true;
                    } else {
                        hasMore = false;
                    }

                    for (Status s : tweets) {
                        String[] links = TweetLinkUtils.getLinksInStatus(s);
                        if (!links[1].equals("")) {
                            pics.add(links[1]);
                            tweetsWithPics.add(s);
                        }
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new PicturesArrayAdapter(context, pics, tweetsWithPics);
                            listView.setAdapter(adapter);
                            listView.setVisibility(View.VISIBLE);

                            spinner.setVisibility(View.GONE);
                            canRefresh = true;

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setVisibility(View.GONE);
                            canRefresh = false;
                        }
                    });

                }

            }
        }).start();
    }

    public void getMore() {
        canRefresh = false;
        spinner.setVisibility(View.VISIBLE);

        if (destroyed) {
            hasMore = false;
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter = Utils.getTwitter(context, settings);

                    paging.setPage(paging.getPage() + 1);

                    ResponseList<Status> result = twitter.getUserTimeline(screenName, paging);

                    tweets.clear();

                    for (Status status : result) {
                        tweets.add(status);
                    }

                    if (result.size() > 17) {
                        hasMore = true;
                    } else {
                        hasMore = false;
                    }

                    boolean update = false;

                    for (Status s : tweets) {
                        String[] links = TweetLinkUtils.getLinksInStatus(s);
                        if (!links[1].equals("")) {
                            pics.add(links[1]);
                            tweetsWithPics.add(s);
                            update = true;
                        }
                    }

                    if (update) {
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                canRefresh = true;

                                spinner.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        canRefresh = true;
                    }

                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            canRefresh = false;
                            hasMore = false;

                            try {
                                adapter.notifyDataSetChanged();
                            } catch (Exception e) {

                            }

                            spinner.setVisibility(View.GONE);
                        }
                    });

                }

            }
        }).start();
    }

    public boolean destroyed = false;

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }
}
