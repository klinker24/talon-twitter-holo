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

package com.klinker.android.twitter.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.MainActivity;

import java.io.File;


public class UpdateUtils {

    public static void updateToGlobalPrefs(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Settings Update")
                .setMessage("Talon has to update your settings preferences to prepare for some new things. This will override any old settings backups.")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new WriteGlobalSharedPrefs(context).execute();
                    }
                })
                .create()
                .show();
    }

    static class WriteGlobalSharedPrefs extends AsyncTask<String, Void, Boolean> {

        ProgressDialog pDialog;
        Context context;

        public WriteGlobalSharedPrefs(Context context) {
            this.context = context;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Saving...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        protected Boolean doInBackground(String... urls) {
            File des = new File(Environment.getExternalStorageDirectory() + "/Talon/backup.prefs");
            IOUtils.saveSharedPreferencesToFile(des, context);
            IOUtils.loadSharedPreferencesFromFile(des, context);

            return true;
        }

        protected void onPostExecute(Boolean deleted) {
            try {
                pDialog.dismiss();
                Toast.makeText(context, "Save Complete", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // not attached
            }

            SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
            sharedPrefs.edit().putBoolean("version_2_2_7_1", false).commit();

            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("version_2_2_7_1", false).commit();

            ((Activity)context).finish();
            context.startActivity(new Intent(context, MainActivity.class));
            ((Activity)context).overridePendingTransition(0,0);
        }
    }

    public static void versionThreeDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Blur - A Launcher Replacement")
                .setMessage("With version 3.0.0, Talon has added support for our latest project, Blur, which is Klinker Apps launcher.\n\n" +
                        "It does some very cool interfacing with Talon, basically having the full app just one swipe away on your launcher. It has been a great project to work on and I recommend checking it out, it is completely free!\n\n" +
                        "Head over to the Play Store description for Blur to learn more about getting Talon compatible (it is just downloading one extension app).\n\n" +
                        "Hope you like it!")
                .setPositiveButton("Go to Blur!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent web = new Intent(Intent.ACTION_VIEW);
                        web.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.klinker.android.launcher"));
                        context.startActivity(web);
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public static void checkUpdate(final Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        if (sharedPrefs.getBoolean("3.1.5", true)) {
            sharedPrefs.edit().putBoolean("3.1.5", false).commit();

            // want to make sure if tweetmarker was on, it remains on.
            if (sharedPrefs.getBoolean("tweetmarker", false)) {
                sharedPrefs.edit().putString("tweetmarker_options", "1").commit();
                AppSettings.invalidate();
            }
        }
    }
}
