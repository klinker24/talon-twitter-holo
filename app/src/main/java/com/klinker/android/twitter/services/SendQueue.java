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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Patterns;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.sq_lite.QueuedDataSource;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.compose.RetryCompose;
import com.klinker.android.twitter.utils.Utils;
import com.klinker.android.twitter.utils.api_helper.TwitLongerHelper;

import java.util.regex.Matcher;

import twitter4j.Twitter;


public class SendQueue extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int i, int x) {

        Log.v("talon_queued", "starting to send queued tweets");

        final Context context = this;
        final AppSettings settings = AppSettings.getInstance(this);

        try {
            if (intent == null) {
                return START_NOT_STICKY;
            }
        } catch (Exception e) {
            // null pointer... what the hell...
        }

        final String[] queued = QueuedDataSource.getInstance(context)
                .getQueuedTweets(AppSettings.getInstance(context).currentAccount);


        new Thread(new Runnable() {
            @Override
            public void run() {

                for (String s : queued) {

                    sendingNotification();
                    boolean sent = sendTweet(settings, context, s);

                    if (sent) {
                        finishedTweetingNotification();
                        QueuedDataSource.getInstance(context).deleteQueuedTweet(s);
                    } else {
                        makeFailedNotification(s, settings);
                    }
                }

                stopSelf();
            }
        }).start();

        return START_STICKY;
    }

    public boolean sendTweet(AppSettings settings, Context context, String message) {
        try {
            Twitter twitter =  Utils.getTwitter(context, settings);

            int size = getCount(message);

            Log.v("talon_queued", "sending: " + message);

            if (size > 140 && settings.twitlonger) {
                // twitlonger goes here
                TwitLongerHelper helper = new TwitLongerHelper(message, twitter);

                return helper.createPost() != 0;
            } else if (size <= 140) {
                twitter4j.StatusUpdate reply = new twitter4j.StatusUpdate(message);
                twitter.updateStatus(reply);
            } else {
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getCount(String text) {
        if (!text.contains("http")) { // no links, normal tweet
            return text.length();
        } else {
            int count = text.length();
            Matcher m = Patterns.WEB_URL.matcher(text);

            while(m.find()) {
                String url = m.group();
                count -= url.length(); // take out the length of the url
                count += 23; // add 23 for the shortened url
            }

            return count;
        }
    }

    public void sendingNotification() {
        // first we will make a notification to let the user know we are tweeting
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_icon)
                        .setContentTitle(getResources().getString(R.string.sending_tweet))
                                //.setTicker(getResources().getString(R.string.sending_tweet))
                        .setOngoing(true)
                        .setProgress(100, 0, true);

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        0
                );

        mBuilder.setContentIntent(resultPendingIntent);

        startForeground(6, mBuilder.build());
    }

    public void makeFailedNotification(String text, AppSettings settings) {
        try {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_icon)
                            .setContentTitle(getResources().getString(R.string.tweet_failed))
                            .setContentText(getResources().getString(R.string.tap_to_retry));

            Intent resultIntent = new Intent(this, RetryCompose.class);
            QueuedDataSource.getInstance(this).createDraft(text, settings.currentAccount);
            resultIntent.setAction(Intent.ACTION_SEND);
            resultIntent.setType("text/plain");
            resultIntent.putExtra(Intent.EXTRA_TEXT, text);
            resultIntent.putExtra("failed_notification", true);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            0
                    );

            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(5, mBuilder.build());
        } catch (Exception e) {

        }
    }

    public void finishedTweetingNotification() {
        try {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(MainActivity.sContext)
                            .setSmallIcon(R.drawable.ic_stat_icon)
                            .setContentTitle(getResources().getString(R.string.tweet_success))
                            .setOngoing(false)
                            .setTicker(getResources().getString(R.string.tweet_success));

            if (AppSettings.getInstance(this).vibrate) {
                Log.v("talon_vibrate", "vibrate on compose");
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = { 0, 50, 500 };
                v.vibrate(pattern, -1);
            }

            stopForeground(true);

            NotificationManager mNotificationManager =
                    (NotificationManager) MainActivity.sContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(6, mBuilder.build());
            // cancel it immediately, the ticker will just go off
            mNotificationManager.cancel(6);
        } catch (Exception e) {
            // not attached to activity
        }
    }
}
