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

package com.klinker.android.twitter.ui.tweet_viewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class NotiTweetPager extends TweetPager {

    @Override
    public void getFromIntent() {
        SharedPreferences sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        name = sharedPrefs.getString("fav_user_tweet_name", "");
        screenName = sharedPrefs.getString("fav_user_tweet_screenname", "");
        tweet = sharedPrefs.getString("fav_user_tweet_text", "");
        time = sharedPrefs.getLong("fav_user_tweet_time", 0);
        retweeter = sharedPrefs.getString("fav_user_tweet_retweeter", "");
        webpage = sharedPrefs.getString("fav_user_tweet_webpage", "");
        tweetId = sharedPrefs.getLong("fav_user_tweet_tweet_id", 0);
        picture = sharedPrefs.getBoolean("fav_user_tweet_picture", false);
        proPic = sharedPrefs.getString("fav_user_tweet_pro_pic", "");

        try {
            users = sharedPrefs.getString("fav_user_tweet_users", "").split("  ");
        } catch (Exception e) {
            users = null;
        }

        try {
            hashtags = sharedPrefs.getString("fav_user_tweet_hashtags", "").split("  ");
        } catch (Exception e) {
            hashtags = null;
        }

        try {
            linkString = sharedPrefs.getString("fav_user_tweet_links", "");
            otherLinks = linkString.split("  ");
        } catch (Exception e) {
            otherLinks = null;
        }

        if (screenName.equals(settings.myScreenName)) {
            isMyTweet = true;
        } else if (screenName.equals(retweeter)) {
            isMyRetweet = true;
        }

        tweet = restoreLinks(tweet);
    }
}
