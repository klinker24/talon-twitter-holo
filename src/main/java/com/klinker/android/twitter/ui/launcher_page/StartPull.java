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

package com.klinker.android.twitter.ui.launcher_page;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import com.klinker.android.twitter.services.TalonPullNotificationService;
import com.klinker.android.twitter.settings.AppSettings;

public class StartPull extends IntentService {

    public StartPull() {
        super("StartPull");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.v("talon_launcher", "starting pull from launcher service");

        SharedPreferences sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        sharedPrefs.edit()
                .putBoolean("launcher_frag_switch", true)
                .putInt("current_account", intent.getIntExtra("current_account", 1))
                .commit();

        AppSettings.invalidate();

        if (AppSettings.getInstance(this).pushNotifications && !TalonPullNotificationService.isRunning) {
            startService(new Intent(this, TalonPullNotificationService.class));
        }
    }
}
