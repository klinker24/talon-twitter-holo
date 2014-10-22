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

package com.klinker.android.twitter.ui.scheduled_tweets;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.ScheduledArrayAdapter;
import com.klinker.android.twitter.data.ScheduledTweet;
import com.klinker.android.twitter.data.sq_lite.QueuedDataSource;
import com.klinker.android.twitter.services.SendScheduledTweet;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.Utils;

import java.util.ArrayList;

public class ViewScheduledTweets extends Activity {

    public final static String EXTRA_TIME = "com.klinker.android.twitter.scheduled.TIME";
    public final static String EXTRA_TEXT = "com.klinker.android.twitter.scheduled.TEXT";
    public final static String EXTRA_ALARM_ID = "com.klinker.android..twitter.scheduled.ALARM_ID";

    public static Context context;
    public ListView listView;
    public Button addNew;
    public SharedPreferences sharedPrefs;
    public ArrayList<ScheduledTweet> tweets;
    private ScheduledArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scheduled_tweet_viewer);

        Utils.setUpTheme(this, AppSettings.getInstance(this));

        final String text = getIntent().getStringExtra("text");

        listView = (ListView) findViewById(R.id.smsListView);
        addNew = (Button) findViewById(R.id.addNewButton);

        context = this;

        tweets = QueuedDataSource.getInstance(context).getScheduledTweets(AppSettings.getInstance(context).currentAccount);

        adapter = new ScheduledArrayAdapter(this, tweets);
        listView.setAdapter(adapter);
        listView.setStackFromBottom(false);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                new AlertDialog.Builder(context)
                        .setMessage(context.getResources().getString(R.string.delete) + " " + getResources().getString(R.string.tweet) + "?")
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                cancelAlarm(tweets.get(i).alarmId);

                                QueuedDataSource.getInstance(context).deleteScheduledTweet(tweets.get(i).alarmId);

                                tweets.remove(i);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(context.getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent edit = new Intent(context, NewScheduledTweet.class);
                                edit.putExtra(EXTRA_TEXT, tweets.get(i).text);
                                edit.putExtra(EXTRA_TIME, tweets.get(i).time + "");

                                QueuedDataSource.getInstance(context).deleteScheduledTweet(tweets.get(i).alarmId);
                                cancelAlarm(tweets.get(i).alarmId);

                                tweets.remove(i);
                                adapter.notifyDataSetChanged();

                                startActivity(edit);

                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        addNew.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, NewScheduledTweet.class);
                if (getIntent().getBooleanExtra("has_text", false)) {
                    intent.putExtra("has_text", true);
                    intent.putExtra("text", text);
                    getIntent().putExtra("has_text", false);
                }
                startActivity(intent);
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();

        tweets = QueuedDataSource.getInstance(context).getScheduledTweets(AppSettings.getInstance(context).currentAccount);
        adapter = new ScheduledArrayAdapter((Activity)context, tweets);
        listView.setAdapter(adapter);
    }


    public void cancelAlarm(int alarmId) {
        Intent serviceIntent = new Intent(getApplicationContext(), SendScheduledTweet.class);

        PendingIntent pi = getDistinctPendingIntent(serviceIntent, alarmId);

        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        am.cancel(pi);
    }

    protected PendingIntent getDistinctPendingIntent(Intent intent, int requestId) {
        PendingIntent pi =
                PendingIntent.getService(
                        this,         //context
                        requestId,    //request id
                        intent,       //intent to be delivered
                        0);

        return pi;
    }
}