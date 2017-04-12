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

package com.klinker.android.twitter.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.klinker.android.twitter.activities.scheduled_tweets.ViewScheduledTweets;
import com.klinker.android.twitter.data.sq_lite.QueuedDataSource;
import com.klinker.android.twitter.services.SendScheduledTweet;

import static com.klinker.android.twitter.settings.AppSettings.settings;

public class ScheduledTweet {
    public String text;
    public int alarmId;
    public long time;
    public int account;
    public SharedPreferences sharedPrefs;

    private Context appContext;
    private Context schedTweetContext;

    public ScheduledTweet(Context appContext, Context schedTweetContext, String text, long time, int account) {
        this.appContext = appContext;
        this.schedTweetContext = schedTweetContext;
        sharedPrefs = appContext.getSharedPreferences("com.klinker.android.twitter_world_preferences",0);
        this.text = text;
        this.time = time;
        this.account = account;
    }

    public ScheduledTweet(String text, int alarmId, long time, int account) {
        this.alarmId = alarmId;
        this.text = text;
        this.time = time;
        this.account = account;
    }

    public void createScheduledTweet() {
        if(sharedPrefs == null) {
            sharedPrefs = appContext.getSharedPreferences("com.klinker.android.twitter_world_preferences",0);
        }
        /* We don't trust whats coming from outside ;) */
        this.account = settings.currentAccount;
        alarmId = sharedPrefs.getInt("scheduled_alarm_id", 400);
        alarmId++;

        SharedPreferences.Editor prefEdit = sharedPrefs.edit();
        prefEdit.putInt("scheduled_alarm_id", alarmId);
        prefEdit.commit();

        QueuedDataSource.getInstance(this.schedTweetContext).createScheduledTweet(this);
        createAlarm();
    }

    private void createAlarm() {
        Intent serviceIntent = new Intent(appContext, SendScheduledTweet.class);

        serviceIntent.putExtra(ViewScheduledTweets.EXTRA_TEXT, text);
        /* this->account can also be used instead of settings.currentAccount */
        serviceIntent.putExtra("account", settings.currentAccount);
        serviceIntent.putExtra("alarm_id", alarmId);

        PendingIntent pi = getDistinctPendingIntent(serviceIntent, alarmId);

        AlarmManager am = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, time, pi);
    }

    protected PendingIntent getDistinctPendingIntent(Intent intent, int requestId) {
        PendingIntent pi =
                PendingIntent.getService(schedTweetContext, requestId, intent, 0);
        return pi;
    }

}
