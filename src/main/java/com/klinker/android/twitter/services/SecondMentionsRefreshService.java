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

package com.klinker.android.twitter.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.klinker.android.twitter.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.NotificationUtils;
import com.klinker.android.twitter.utils.Utils;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class SecondMentionsRefreshService extends IntentService {

    SharedPreferences sharedPrefs;

    public SecondMentionsRefreshService() {
        super("SecondMentionsRefreshService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        Context context = getApplicationContext();
        AppSettings settings = AppSettings.getInstance(context);

        // if they have mobile data on and don't want to sync over mobile data
        if (Utils.getConnectionStatus(context) && !settings.syncMobile) {
            return;
        }

        boolean update = false;
        int numberNew = 0;

        try {
            Twitter twitter = Utils.getSecondTwitter(context);

            int currentAccount = sharedPrefs.getInt("current_account", 1);

            if(currentAccount == 1) {
                currentAccount = 2;
            } else {
                currentAccount = 1;
            }

            MentionsDataSource dataSource = MentionsDataSource.getInstance(context);

            long lastId = dataSource.getLastIds(currentAccount)[0];
            Paging paging;
            paging = new Paging(1, 200);
            if (lastId > 0) {
                paging.sinceId(lastId);
            }

            List<Status> statuses = twitter.getMentionsTimeline(paging);

            numberNew = MentionsDataSource.getInstance(context).insertTweets(statuses, currentAccount);

            if (numberNew > 0) {
                if (settings.notifications && settings.mentionsNot) {
                    NotificationUtils.notifySecondMentions(context, currentAccount);
                }

                sendBroadcast(new Intent("com.klinker.android.twitter.REFRESH_SECOND_MENTIONS"));
            }

        } catch (TwitterException e) {
            // Error in updating status
            Log.d("Twitter Update Error", e.getMessage());
        }
    }
}