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

package com.klinker.android.twitter.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ThemeConfigurationReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.klinker.android.SET_THEME";
    public static final String ARG_PACKAGE_NAME = "com.klinker.android.THEME_PACKAGE_NAME";
    private static final String TAG = "ThemeConfigurationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String themePackage = intent.getStringExtra(ARG_PACKAGE_NAME);

        if (themePackage == null) {
            return;
        }

        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(themePackage, PackageManager.GET_META_DATA);
            Bundle metaData = app.metaData;

            String themeType = metaData.getString("talon_theme");
            if (themeType == null) {
            	return;
            }

            if (themeType.startsWith("version")) {
                context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                        Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE).edit()
                        .putString("addon_theme_package", themePackage)
                        .putBoolean("addon_themes", true);

                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putString("addon_theme_package", themePackage)
                        .commit();
                Toast.makeText(context, "Theme set!", Toast.LENGTH_SHORT).show();
                Log.v(TAG, "successfully saved theme as package name " + themePackage);

                // force kill the process
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        } catch (Exception e) {
            Toast.makeText(context, "Something went wrong.", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "problem setting theme", e);
        }
    }
}
