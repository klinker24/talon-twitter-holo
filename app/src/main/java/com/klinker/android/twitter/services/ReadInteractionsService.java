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

public class ReadInteractionsService extends IntentService {

    SharedPreferences sharedPrefs;

    public ReadInteractionsService() {
        super("MarkReadService");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        // clear custom light flow broadcast
        Intent lightFlow = new Intent("com.klinker.android.twitter.CLEARED_NOTIFICATION");
        this.sendBroadcast(lightFlow);

        sharedPrefs.edit().putBoolean("new_notification", false).commit();
        sharedPrefs.edit().putInt("new_retweets", 0).commit();
        sharedPrefs.edit().putInt("new_favorites", 0).commit();
        sharedPrefs.edit().putInt("new_follows", 0).commit();
        sharedPrefs.edit().putInt("new_quotes", 0).commit();
        sharedPrefs.edit().putString("old_interaction_text", "").commit();
    }

}
