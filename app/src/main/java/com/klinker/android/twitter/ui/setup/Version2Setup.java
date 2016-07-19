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

package com.klinker.android.twitter.ui.setup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.sq_lite.DMDataSource;
import com.klinker.android.twitter.data.sq_lite.DMSQLiteHelper;
import com.klinker.android.twitter.data.sq_lite.HomeDataSource;
import com.klinker.android.twitter.data.sq_lite.HomeSQLiteHelper;
import com.klinker.android.twitter.data.sq_lite.ListDataSource;
import com.klinker.android.twitter.data.sq_lite.ListSQLiteHelper;
import com.klinker.android.twitter.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter.data.sq_lite.MentionsSQLiteHelper;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.utils.TweetLinkUtils;
import com.klinker.android.twitter.utils.IOUtils;
import com.klinker.android.twitter.utils.Utils;

public class Version2Setup extends Activity {

    private Context context;
    private SharedPreferences sharedPrefs;

    private Button contButton;
    private TextSwitcher title;
    private TextSwitcher summary;
    private TextSwitcher progDescription;
    private ProgressBar progressBar;
    private LinearLayout main;

    private AppSettings settings;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        context = this;
        settings = AppSettings.getInstance(context);

        Utils.setUpTheme(context, settings);
        setContentView(R.layout.login_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        contButton = (Button) findViewById(R.id.btnLoginTwitter);
        title = (TextSwitcher) findViewById(R.id.welcome);
        summary = (TextSwitcher) findViewById(R.id.info);
        progDescription = (TextSwitcher) findViewById(R.id.progress_desc);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        main = (LinearLayout) findViewById(R.id.mainLayout);

        contButton.setText(getString(R.string.start));

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);

        title.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(Version2Setup.this);
                myText.setTextSize(30);
                return myText;
            }
        });

        // set the animation type of textSwitcher
        title.setInAnimation(in);
        title.setOutAnimation(out);

        summary.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(Version2Setup.this);
                myText.setTextSize(17);
                return myText;
            }
        });

        // set the animation type of textSwitcher
        summary.setInAnimation(in);
        summary.setOutAnimation(out);

        progDescription.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(Version2Setup.this);
                myText.setTextSize(17);
                return myText;
            }
        });

        // set the animation type of textSwitcher
        progDescription.setInAnimation(in);
        progDescription.setOutAnimation(out);

        title.setText(getResources().getString(R.string.version_two));
        summary.setText(getResources().getString(R.string.setup_version_two));

        progressBar.setProgress(100);

        contButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // start the process
                if (contButton.getText().toString().equals(getString(R.string.start))) {
                    new Cleanup().execute();
                    contButton.setEnabled(false);
                } else { // we are done here, go back to the main screen

                    Intent timeline = new Intent(context, MainActivity.class);
                    timeline.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    sharedPrefs.edit().putBoolean("should_refresh", false).commit();
                    sharedPrefs.edit().putBoolean("refresh_me", true).commit();
                    sharedPrefs.edit().putBoolean("refresh_me_mentions", true).commit();
                    sharedPrefs.edit().putBoolean("refresh_me_dm", true).commit();
                    sharedPrefs.edit().putBoolean("setup_v_two", true).commit();
                    AppSettings.invalidate();
                    startActivity(timeline);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
       // don't want the user to exit! so don't do anything here.
    }

    class Cleanup extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            progDescription.setText(getString(R.string.cleaning_databases));
            progressBar.setIndeterminate(true);
            summary.setText("");

            progDescription.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... urls) {

            IOUtils.trimDatabase(context, 1);
            IOUtils.trimDatabase(context, 2);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progDescription.setText(getResources().getString(R.string.setting_up_timeline));
                }
            });

            HomeDataSource home = HomeDataSource.getInstance(context);
            Cursor cursor = home.getCursor(1);
            if (cursor.moveToFirst()) {
                long id;
                String text;
                do {
                    id = cursor.getLong(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID));
                    text = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TEXT));
                    home.removeHTML(id, TweetLinkUtils.removeColorHtml(text, settings));
                } while (cursor.moveToNext());
            }
            cursor.close();

            cursor = home.getCursor(2);
            if (cursor.moveToFirst()) {
                long id;
                String text;
                do {
                    id = cursor.getLong(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID));
                    text = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TEXT));
                    home.removeHTML(id, TweetLinkUtils.removeColorHtml(text, settings));
                } while (cursor.moveToNext());
            }
            cursor.close();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progDescription.setText(getResources().getString(R.string.setting_up_mentions));
                }
            });

            MentionsDataSource mentions = MentionsDataSource.getInstance(context);
            cursor = mentions.getCursor(1);
            if (cursor.moveToFirst()) {
                long id;
                String text;
                do {
                    id = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));
                    text = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TEXT));
                    mentions.removeHTML(id, TweetLinkUtils.removeColorHtml(text, settings));
                } while (cursor.moveToNext());
            }
            cursor.close();

            cursor = mentions.getCursor(2);
            if (cursor.moveToFirst()) {
                long id;
                String text;
                do {
                    id = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));
                    text = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TEXT));
                    mentions.removeHTML(id, TweetLinkUtils.removeColorHtml(text, settings));
                } while (cursor.moveToNext());
            }
            cursor.close();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progDescription.setText(getResources().getString(R.string.setting_up_dms));
                }
            });

            DMDataSource dm = DMDataSource.getInstance(context);
            cursor = dm.getCursor(1);
            if (cursor.moveToFirst()) {
                long id;
                String text;
                do {
                    id = cursor.getLong(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TWEET_ID));
                    text = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TEXT));
                    dm.removeHTML(id, TweetLinkUtils.removeColorHtml(text, settings));
                } while (cursor.moveToNext());
            }
            cursor.close();

            cursor = dm.getCursor(2);
            if (cursor.moveToFirst()) {
                long id;
                String text;
                do {
                    id = cursor.getLong(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TWEET_ID));
                    text = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TEXT));
                    dm.removeHTML(id, TweetLinkUtils.removeColorHtml(text, settings));
                } while (cursor.moveToNext());
            }
            cursor.close();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progDescription.setText(getResources().getString(R.string.setting_up_lists));
                }
            });

            ListDataSource lists = ListDataSource.getInstance(context);
            cursor = lists.getWholeCursor();
            if (cursor.moveToFirst()) {
                long id;
                String text;
                do {
                    id = cursor.getLong(cursor.getColumnIndex(ListSQLiteHelper.COLUMN_TWEET_ID));
                    text = cursor.getString(cursor.getColumnIndex(ListSQLiteHelper.COLUMN_TEXT));
                    lists.removeHTML(id, TweetLinkUtils.removeColorHtml(text, settings));
                } while (cursor.moveToNext());
            }
            cursor.close();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progDescription.setText(getResources().getString(R.string.cleaning_cache));
                }
            });

            IOUtils.trimCache(context);

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            progDescription.setText("");
            summary.setText(getString(R.string.version_two_setup_done));
            contButton.setText(getString(R.string.back_to_timeline));
            contButton.setEnabled(true);
            progressBar.setIndeterminate(false);
            progressBar.setProgress(100);

            progDescription.setVisibility(View.INVISIBLE);
        }
    }
}
