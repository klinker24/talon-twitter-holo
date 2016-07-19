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
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.util.Log;
import com.klinker.android.twitter.data.sq_lite.HomeDataSource;
import com.klinker.android.twitter.data.sq_lite.InteractionsDataSource;
import com.klinker.android.twitter.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter.settings.AppSettings;

public class MarkReadSecondAccService extends IntentService {

    SharedPreferences sharedPrefs;

    public MarkReadSecondAccService() {
        super("MarkReadSecAccService");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        Log.v("talon_mark_read", "running the mark read service for account 2");

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);

        // clear custom light flow broadcast
        Intent lightFlow = new Intent("com.klinker.android.twitter.CLEARED_NOTIFICATION");
        this.sendBroadcast(lightFlow);

        sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        final Context context = getApplicationContext();
        int currentAccount = AppSettings.getInstance(context).currentAccount;

        if (currentAccount == 1) {
            currentAccount = 2;
        } else {
            currentAccount = 1;
        }

        // we can just mark everything as read because it isnt taxing at all and won't do anything in the mentions if there isn't one
        // and the shared prefs are easy.
        // this is only called from the notification and there will only ever be one thing that is unread when this button is available

        // delay this so that if switching account, it will start at the right place
        MentionsDataSource.getInstance(context).markAllRead(currentAccount);
        HomeDataSource.getInstance(context).markAllRead(currentAccount);
        InteractionsDataSource.getInstance(context).markAllRead(currentAccount);

        sharedPrefs.edit().putInt("dm_unread_" + currentAccount, 0).commit();
    }

}
