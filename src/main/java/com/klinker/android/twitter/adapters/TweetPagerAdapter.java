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

package com.klinker.android.twitter.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.ConversationFragment;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.TweetFragment;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.TweetYouTubeFragment;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.MobilizedFragment;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.WebFragment;
import com.klinker.android.twitter.utils.Utils;

import java.util.ArrayList;

public class TweetPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private AppSettings settings;

    private int pageCount;
    private boolean youtube = false;
    private boolean hasWebpage = false;
    private String video = "";
    private ArrayList<String> webpages;

    private String name;
    private String screenName;
    private String tweet;
    private long time;
    private String retweeter;
    private String webpage;
    private String proPic;
    private boolean picture;
    private long tweetId;
    private String[] users;
    private String[] hashtags;
    private String[] otherLinks;
    private boolean isMyTweet;
    private boolean isMyRetweet;

    private boolean mobilizedBrowser = false;

    public TweetPagerAdapter(FragmentManager fm, Context context,
         String name, String screenName, String tweet, long time, String retweeter, String webpage,
         String proPic, long tweetId, boolean picture, String[] users, String[] hashtags, String[] links,
         boolean isMyTweet, boolean isMyRetweet) {

        super(fm);
        this.context = context;

        settings = AppSettings.getInstance(context);

        this.name = name;
        this.screenName = screenName;
        this.tweet = tweet;
        this.time = time;
        this.retweeter = retweeter;
        this.webpage = webpage;
        this.proPic = proPic;
        this.picture = picture;
        this.tweetId = tweetId;
        this.users = users;
        this.hashtags = hashtags;
        this.isMyRetweet = isMyRetweet;
        this.isMyTweet = isMyTweet;
        this.otherLinks = links;

        webpages = new ArrayList<String>();

        if (links == null) {
            links = new String[0];
        }

        SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        if (links.length > 0 && !links[0].equals("")) {
            for (String s : links) {
                if (s.contains("youtu")) {
                    video = s;
                    youtube = true;
                    break;
                } else {
                    if (!s.contains("pic.twitt")) {
                        webpages.add(s);
                    }
                }
            }

            if (webpages.size() >= 1 && sharedPrefs.getBoolean("inapp_browser_tweet", true)) {
                this.hasWebpage = true;
            } else {
                this.hasWebpage = false;
            }

        } else {
            this.hasWebpage = false;
        }

        pageCount = 2; // tweet, conversations, and Discussion will always be there

        if (this.hasWebpage) {
            pageCount++;
        }
        if(youtube) {
            pageCount++;
        }

        // getconnectionstatus() is true if on mobile data, false otherwise
        mobilizedBrowser = settings.alwaysMobilize || (settings.mobilizeOnData && Utils.getConnectionStatus(context));
    }

    @Override
    public Fragment getItem(int i) {

        if (pageCount == 2) {
            switch (i) {
                case 0:
                    TweetFragment tweetFragment = new TweetFragment();
                    tweetFragment.setArguments(getTweetBundle());
                    return tweetFragment;
                case 1:
                    ConversationFragment conversation = new ConversationFragment();
                    conversation.setArguments(getConvoBundle());
                    return conversation;
            }
        } else if (pageCount == 3 && youtube) {
            switch (i) {
                case 0:
                    TweetYouTubeFragment youTube = new TweetYouTubeFragment();
                    youTube.setArguments(getYouTubeBundle());
                    return youTube;
                case 1:
                    TweetFragment tweetFragment = new TweetFragment();
                    tweetFragment.setArguments(getTweetBundle());
                    return tweetFragment;
                case 2:
                    ConversationFragment conversation = new ConversationFragment();
                    conversation.setArguments(getConvoBundle());
                    return conversation;
            }
        } else if (pageCount == 3) { // no youtube, just a webpage
            switch (i) {
                case 0:
                    Fragment web = mobilizedBrowser ? new MobilizedFragment() : new WebFragment();
                    web.setArguments(getMobilizedBundle());
                    return web;
                case 1:
                    TweetFragment tweetFragment = new TweetFragment();
                    tweetFragment.setArguments(getTweetBundle());
                    return tweetFragment;
                case 2:
                    ConversationFragment conversation = new ConversationFragment();
                    conversation.setArguments(getConvoBundle());
                    return conversation;
            }
        } else { // every page is shown
            switch (i) {
                case 0:
                    TweetYouTubeFragment youTube = new TweetYouTubeFragment();
                    youTube.setArguments(getYouTubeBundle());
                    return youTube;
                case 1:
                    Fragment web = mobilizedBrowser ? new MobilizedFragment() : new WebFragment();
                    web.setArguments(getMobilizedBundle());
                    return web;
                case 2:
                    TweetFragment tweetFragment = new TweetFragment();
                    tweetFragment.setArguments(getTweetBundle());
                    return tweetFragment;
                case 3:
                    ConversationFragment conversation = new ConversationFragment();
                    conversation.setArguments(getConvoBundle());
                    return conversation;
            }
        }

        return null;
    }

    @Override
    public int getCount() {
        return pageCount;
    }

    public boolean getHasWebpage() {
        return hasWebpage;
    }

    public boolean getHasYoutube() {
        return youtube;
    }

    @Override
    public CharSequence getPageTitle(int i) {
        if (pageCount == 2) {
            switch (i) {
                case 0:
                    return context.getResources().getString(R.string.tweet);
                case 1:
                    return context.getResources().getString(R.string.conversation);
            }
        } else if (pageCount == 3 && youtube) {
            switch (i) {
                case 0:
                    return context.getResources().getString(R.string.tweet_youtube);
                case 1:
                    return context.getResources().getString(R.string.tweet);
                case 2:
                    return context.getResources().getString(R.string.conversation);
            }
        } else if (pageCount == 3) { // no youtube, just a hasWebpage
            switch (i) {
                case 0:
                    return context.getResources().getString(R.string.webpage);
                case 1:
                    return context.getResources().getString(R.string.tweet);
                case 2:
                    return context.getResources().getString(R.string.conversation);
            }
        } else { // every page is shown
            switch (i) {
                case 1:
                    return context.getResources().getString(R.string.webpage);
                case 0:
                    return context.getResources().getString(R.string.tweet_youtube);
                case 2:
                    return context.getResources().getString(R.string.tweet);
                case 3:
                    return context.getResources().getString(R.string.conversation);
            }
        }
        return null;
    }

    public Bundle getConvoBundle() {
        Bundle b = new Bundle();
        b.putLong("tweet_id", tweetId);
        return b;
    }

    public Bundle getYouTubeBundle() {
        Bundle b = new Bundle();
        b.putString("url", video);
        return b;
    }

    public Bundle getMobilizedBundle() {
        Bundle b = new Bundle();
        b.putStringArrayList("webpages", webpages);
        return b;
    }

    public Bundle getTweetBundle() {
        Bundle b = new Bundle();

        b.putString("name", name);
        b.putString("screen_name", screenName);
        b.putString("tweet", tweet);
        b.putLong("time", time);
        b.putString("retweeter", retweeter);
        b.putString("webpage", webpage);
        b.putString("pro_pic", proPic);
        b.putBoolean("picture", picture);
        b.putLong("tweet_id", tweetId);
        b.putStringArray("users", users);
        b.putStringArray("hashtags", hashtags);
        b.putBoolean("is_my_tweet", isMyTweet);
        b.putStringArray("links", otherLinks);

        return b;
    }
}