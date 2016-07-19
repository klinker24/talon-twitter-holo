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

package com.klinker.android.twitter.ui.tweet_viewer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.ArrayListLoader;
import com.klinker.android.twitter.adapters.TimelineArrayAdapter;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.manipulations.widgets.HoloTextView;
import com.klinker.android.twitter.utils.Utils;

import org.lucasr.smoothie.AsyncListView;
import org.lucasr.smoothie.ItemManager;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import uk.co.senab.bitmapcache.BitmapLruCache;

public class ConversationFragment extends Fragment {
    private Context context;
    private View layout;
    private AppSettings settings;
    private long tweetId;

    public ConversationFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    public static boolean isRunning = true;

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        settings = AppSettings.getInstance(getActivity());
        tweetId = getArguments().getLong("tweet_id", 0l);

        isRunning = true;

        layout = inflater.inflate(R.layout.conversation_fragment, null, false);
        final AsyncListView replyList = (AsyncListView) layout.findViewById(R.id.listView);
        final LinearLayout progressSpinner = (LinearLayout) layout.findViewById(R.id.list_progress);
        final HoloTextView none = (HoloTextView) layout.findViewById(R.id.no_conversation);
        none.setText(getResources().getString(R.string.no_tweets));

        BitmapLruCache cache = App.getInstance(context).getBitmapCache();
        ArrayListLoader loader = new ArrayListLoader(cache, context);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(50);
        builder.setThreadPoolSize(4);

        replyList.setItemManager(builder.build());

        getReplies(replyList, tweetId, progressSpinner, none);

        return layout;
    }

    public ArrayList<Status> replies;
    public TimelineArrayAdapter adapter;
    public Status status = null;

    public void getReplies(final ListView listView, final long tweetId, final LinearLayout progressSpinner, final HoloTextView none) {

        Thread getReplies = new Thread(new Runnable() {
            @Override
            public void run() {

                if (!isRunning) {
                    return;
                }

                Twitter twitter = Utils.getTwitter(context, settings);
                replies = new ArrayList<twitter4j.Status>();
                try {
                    status = twitter.showStatus(tweetId);

                    if (status.isRetweet()) {
                        status = status.getRetweetedStatus();
                    }

                    twitter4j.Status replyStatus = twitter.showStatus(status.getInReplyToStatusId());

                    try {
                        while(!replyStatus.getText().equals("")) {
                            if (!isRunning) {
                                return;
                            }
                            replies.add(replyStatus);
                            Log.v("reply_status", replyStatus.getText());

                            replyStatus = twitter.showStatus(replyStatus.getInReplyToStatusId());
                        }

                    } catch (Exception e) {
                        // the list of replies has ended, but we dont want to go to null
                    }



                } catch (TwitterException e) {
                    e.printStackTrace();
                }

                if (status != null && replies.size() > 0) {
                    replies.add(0, status);
                }

                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (replies.size() > 0) {

                                ArrayList<twitter4j.Status> reversed = new ArrayList<twitter4j.Status>();
                                for (int i = replies.size() - 1; i >= 0; i--) {
                                    reversed.add(replies.get(i));
                                }

                                replies = reversed;

                                adapter = new TimelineArrayAdapter(context, replies);
                                listView.setAdapter(adapter);
                                listView.setVisibility(View.VISIBLE);
                                progressSpinner.setVisibility(View.GONE);

                            } else {
                            }
                        } catch (Exception e) {
                            // none and it got the null object
                        }


                        if (status != null) {
                            // everything here worked, so get the discussion on the tweet
                            getDiscussion(listView, tweetId, progressSpinner, none, status);
                        }
                    }
                });
            }
        });

        getReplies.setPriority(7);
        getReplies.start();
    }

    public Query query;

    public void getDiscussion(final ListView listView, final long tweetId, final LinearLayout progressBar, final HoloTextView none, final Status status) {

        Log.v("talon_replies", "getting discussion");

        if (replies.size() == 0) {
            replies.add(status);
        }

        Thread getReplies = new Thread(new Runnable() {
            @Override
            public void run() {

                if (!isRunning) {
                    return;
                }

                ArrayList<twitter4j.Status> all = null;
                Twitter twitter = Utils.getTwitter(context, settings);
                try {
                    Log.v("talon_replies", "looking for discussion");

                    long id = status.getId();
                    String screenname = status.getUser().getScreenName();

                    query = new Query("@" + screenname + " since_id:" + id);

                    Log.v("talon_replies", "query string: " + query.getQuery());

                    try {
                        query.setCount(100);
                    } catch (Throwable e) {
                        // enlarge buffer error?
                        query.setCount(30);
                    }

                    QueryResult result = twitter.search(query);
                    Log.v("talon_replies", "result: " + result.getTweets().size());

                    all = new ArrayList<twitter4j.Status>();

                    do {
                        Log.v("talon_replies", "do loop repetition");
                        if (!isRunning) {
                            return;
                        }
                        List<Status> tweets = result.getTweets();

                        for(twitter4j.Status tweet : tweets){
                            if (tweet.getInReplyToStatusId() == id) {
                                all.add(tweet);
                                Log.v("talon_replies", tweet.getText());
                            }
                        }

                        if (all.size() > 0) {
                            for (int i = all.size() - 1; i >= 0; i--) {
                                Log.v("talon_replies", "inserting into arraylist:" + all.get(i).getText());
                                replies.add(all.get(i));
                            }

                            all.clear();

                            ((Activity)context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    try {
                                        if (replies.size() > 0) {
                                            if (adapter == null || adapter.getCount() == 0) {
                                                adapter = new TimelineArrayAdapter(context, replies);
                                                listView.setAdapter(adapter);
                                                listView.setVisibility(View.VISIBLE);
                                            } else {
                                                Log.v("talon_replies", "notifying adapter change");
                                                adapter.notifyDataSetChanged();
                                            }
                                        } else {
                                            none.setVisibility(View.VISIBLE);
                                        }
                                    } catch (Exception e) {
                                        // none and it got the null object
                                        e.printStackTrace();
                                        listView.setVisibility(View.GONE);
                                        none.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }

                        try {
                            Thread.sleep(250);
                        } catch (Exception e) {
                            // since we are changing the arraylist for the adapter in the background, we need to make sure it
                            // gets updated before continuing
                        }

                        query = result.nextQuery();

                        if (query != null)
                            result = twitter.search(query);

                    } while (query != null);

                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }

                if (replies.size() < 2) {
                    // nothing to show, so tell them that
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            listView.setVisibility(View.GONE);
                            none.setVisibility(View.VISIBLE);
                    }
                });
            }
            }
        });

        getReplies.setPriority(8);
        getReplies.start();

    }
}
