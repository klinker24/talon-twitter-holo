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
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.sq_lite.QueuedDataSource;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.compose.RetryCompose;
import com.klinker.android.twitter.utils.IOUtils;
import com.klinker.android.twitter.utils.Utils;
import com.klinker.android.twitter.utils.api_helper.TwitLongerHelper;
import com.klinker.android.twitter.utils.api_helper.TwitPicHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import twitter4j.Twitter;


public class SendTweet extends Service {

    public String message = "";
    public String attachedUri = "";
    public boolean pwiccer = false;
    public long tweetId = 0l;
    public int remainingChars = 0;
    public boolean secondAcc;

    public boolean finished = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int i, int x) {

        final Context context = this;
        final AppSettings settings = AppSettings.getInstance(this);

        try {
            if (intent == null) {
                return START_NOT_STICKY;
            }
        } catch (Exception e) {
            // null pointer... what the hell
        }
        // set up the tweet from the intent
        message = intent.getStringExtra("message");
        tweetId = intent.getLongExtra("tweet_id", 0l);
        remainingChars = intent.getIntExtra("char_remaining", 0);
        pwiccer = intent.getBooleanExtra("pwiccer", false);
        attachedUri = intent.getStringExtra("attached_uri");
        secondAcc = intent.getBooleanExtra("second_account", false);

        if (attachedUri == null) {
            attachedUri = "";
        }

        sendingNotification();

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean sent = sendTweet(settings, context);

                if (sent) {
                    finishedTweetingNotification();
                } else {
                    makeFailedNotification(message, settings);
                }

                finished = true;

                stopSelf();
            }
        }).start();

        // if it takes longer than 2 mins to preform the sending, then something is wrong and we will just shut it down.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!finished) {
                    stopForeground(true);
                    makeFailedNotification(message, settings);
                    stopSelf();
                }
            }
        }, 120000);

        return START_STICKY;
    }

    public Twitter getTwitter() {
        if (secondAcc) {
            return Utils.getSecondTwitter(this);
        } else {
            return Utils.getTwitter(this, AppSettings.getInstance(this));
        }
    }

    public boolean sendTweet(AppSettings settings, Context context) {
        try {
            Twitter twitter =  getTwitter();

            if (remainingChars < 0 && !pwiccer) {
                // twitlonger goes here
                TwitLongerHelper helper = new TwitLongerHelper(message, twitter);
                helper.setInReplyToStatusId(tweetId);

                return helper.createPost() != 0;
            } else {
                twitter4j.StatusUpdate reply = new twitter4j.StatusUpdate(message);
                reply.setInReplyToStatusId(tweetId);

                if (!attachedUri.equals("")) {

                    File outputDir = context.getCacheDir(); // context being the Activity pointer
                    File f = File.createTempFile("compose", "picture", outputDir);

                    Bitmap bitmap = getBitmapToSend(Uri.parse(attachedUri), context);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    byte[] bitmapdata = bos.toByteArray();

                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();

                    if (!settings.twitpic) {
                        reply.setMedia(f);
                        twitter.updateStatus(reply);
                        return true;
                    } else {
                        TwitPicHelper helper = new TwitPicHelper(twitter, message, f, context);
                        helper.setInReplyToStatusId(tweetId);
                        return helper.createPost() != 0;
                    }
                } else {
                    // no picture
                    twitter.updateStatus(reply);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Bitmap getBitmapToSend(Uri uri, Context context) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        int reqWidth = 750;
        int reqHeight = 750;

        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = input.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap b = BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            ExifInterface exif = new ExifInterface(IOUtils.getPath(uri, context));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            return rotateBitmap(b, orientation);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Log.v("talon_composing_image", "rotation: " + orientation);

        try{
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }
            try {
                Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return bmRotated;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public int calculateInSampleSize(BitmapFactory.Options opt, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = opt.outHeight;
        final int width = opt.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
        //NotificationManager mNotificationManager =
                //(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.notify(6, mBuilder.build());
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
        // sometimes it just would keep making the notification for some reason...
        // so delay it to insure it clears everything correctly
        try {
            Thread.sleep(500);
        } catch (Exception e) {

        }

        try {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(MainActivity.sContext)
                            .setSmallIcon(R.drawable.ic_stat_icon)
                            .setContentTitle(getResources().getString(R.string.tweet_success))
                            .setOngoing(false)
                            .setTicker(getResources().getString(R.string.tweet_success));

            if (AppSettings.getInstance(getApplicationContext()).vibrate) {
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
