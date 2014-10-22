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

package com.klinker.android.twitter.utils.redirects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.launcher_page.LauncherPopup;


public class RedirectToLauncherPopup extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE)
                .edit()
                .putInt("current_account",
                        getIntent().getIntExtra("current_account", 1))
                .commit();

        AppSettings.invalidate();

        Intent popup = new Intent(this, LauncherPopup.class);
        popup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        popup.putExtra("launcher_page", getIntent().getIntExtra("launcher_page", 0));
        popup.putExtra("from_launcher", true);
        finish();

        startActivity(popup);
    }
}
