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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.sq_lite.HomeDataSource;
import com.klinker.android.twitter.data.sq_lite.HomeSQLiteHelper;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.transaction.KeyProperties;
import com.klinker.android.twitter.ui.launcher_page.HandleScrollService;
import com.klinker.android.twitter.utils.IOUtils;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.utils.Utils;
import com.klinker.android.twitter.utils.WearableUtils;
import com.klinker.android.twitter.utils.api_helper.TweetMarkerHelper;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import uk.co.senab.bitmapcache.BitmapLruCache;

public class TweetWearableService extends WearableListenerService {

    private static final String TAG = "TweetWearableService";
    private static final int MAX_ARTICLES_TO_SYNC = 200;

    private Handler markReadHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "created wearable service");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        final WearableUtils wearableUtils = new WearableUtils();

        final BitmapLruCache cache = App.getInstance(this).getBitmapCache();

        if (markReadHandler == null) {
            markReadHandler = new Handler();
        }

        final String message = new String(messageEvent.getData());
        Log.d(TAG, "got message: " + message);

        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        if (message.equals(KeyProperties.GET_DATA_MESSAGE)) {
            AppSettings settings = AppSettings.getInstance(this);

            Cursor tweets = HomeDataSource.getInstance(this).getWearCursor(settings.currentAccount);
            PutDataMapRequest dataMap = PutDataMapRequest.create(KeyProperties.PATH);
            ArrayList<String> names = new ArrayList<String>();
            ArrayList<String> screennames = new ArrayList<String>();
            ArrayList<String> bodies = new ArrayList<String>();
            ArrayList<String> ids = new ArrayList<String>();

            if (tweets != null && tweets.moveToLast()) {
                do {
                    String name = tweets.getString(tweets.getColumnIndex(HomeSQLiteHelper.COLUMN_NAME));
                    String screenname = tweets.getString(tweets.getColumnIndex(HomeSQLiteHelper.COLUMN_SCREEN_NAME));
                    String pic = tweets.getString(tweets.getColumnIndex(HomeSQLiteHelper.COLUMN_PRO_PIC));
                    String body = tweets.getString(tweets.getColumnIndex(HomeSQLiteHelper.COLUMN_TEXT));
                    long id = tweets.getLong(tweets.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID));

                    String retweeter;
                    try {
                        retweeter = tweets.getString(tweets.getColumnIndex(HomeSQLiteHelper.COLUMN_RETWEETER));
                    } catch (Exception e) {
                        retweeter = "";
                    }

                    screennames.add(screenname);
                    names.add(name);
                    if (TextUtils.isEmpty(retweeter)) {
                        body = pic + KeyProperties.DIVIDER + body + KeyProperties.DIVIDER;
                    } else {
                        body = pic + KeyProperties.DIVIDER +
                                body +
                                "<br><br>" + getString(R.string.retweeter) + retweeter +
                                KeyProperties.DIVIDER;
                    }
                    bodies.add(Html.fromHtml(body.replace("<p>", KeyProperties.LINE_BREAK)).toString());
                    ids.add(id + "");
                } while (tweets.moveToPrevious() && tweets.getCount() - tweets.getPosition() < MAX_ARTICLES_TO_SYNC);
                tweets.close();
            }

            dataMap.getDataMap().putStringArrayList(KeyProperties.KEY_USER_NAME, names);
            dataMap.getDataMap().putStringArrayList(KeyProperties.KEY_USER_SCREENNAME, screennames);
            dataMap.getDataMap().putStringArrayList(KeyProperties.KEY_TWEET, bodies);
            dataMap.getDataMap().putStringArrayList(KeyProperties.KEY_ID, ids);

            // light background with orange accent or theme color accent
            dataMap.getDataMap().putInt(KeyProperties.KEY_PRIMARY_COLOR, Color.parseColor("#dddddd"));
            if (settings.addonTheme) {
                dataMap.getDataMap().putInt(KeyProperties.KEY_ACCENT_COLOR, settings.accentInt);
            } else {
                dataMap.getDataMap().putInt(KeyProperties.KEY_ACCENT_COLOR, getResources().getColor(R.color.orange_primary_color));
            }
            dataMap.getDataMap().putLong(KeyProperties.KEY_DATE, System.currentTimeMillis());

            for (String node : wearableUtils.getNodes(googleApiClient)) {
                byte[] bytes = dataMap.asPutDataRequest().getData();
                Wearable.MessageApi.sendMessage(googleApiClient, node, KeyProperties.PATH, bytes);
                Log.v(TAG, "sent " + bytes.length + " bytes of data to node " + node);
            }
        } else if (message.startsWith(KeyProperties.MARK_READ_MESSAGE)) {
            markReadHandler.removeCallbacksAndMessages(null);
            markReadHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String[] messageContent = message.split(KeyProperties.DIVIDER);

                    final long id = Long.parseLong(messageContent[1]);

                    final AppSettings settings = AppSettings.getInstance(TweetWearableService.this);

                    try {
                        HomeDataSource.getInstance(TweetWearableService.this).markPosition(settings.currentAccount, id);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }

                    sendBroadcast(new Intent("com.klinker.android.twitter.CLEAR_PULL_UNREAD"));

                    final SharedPreferences sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                            Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

                    // mark tweetmarker if they use it
                    if (AppSettings.getInstance(TweetWearableService.this).tweetmarker) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                TweetMarkerHelper helper = new TweetMarkerHelper(settings.currentAccount,
                                        sharedPrefs.getString("twitter_screen_name_" + settings.currentAccount, ""),
                                        Utils.getTwitter(TweetWearableService.this, settings),
                                        sharedPrefs);

                                helper.sendCurrentId("timeline", id);

                                startService(new Intent(TweetWearableService.this, HandleScrollService.class));
                            }
                        }).start();
                    } else {
                        startService(new Intent(TweetWearableService.this, HandleScrollService.class));
                    }
                }
            }, 5000);

        } else if (message.startsWith(KeyProperties.REQUEST_FAVORITE)) {
            final long tweetId = Long.parseLong(message.split(KeyProperties.DIVIDER)[1]);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Utils.getTwitter(TweetWearableService.this, AppSettings.getInstance(TweetWearableService.this)).createFavorite(tweetId);
                    } catch (Exception e) {
                    }
                }
            }).start();
        } else if (message.startsWith(KeyProperties.REQUEST_COMPOSE)) {
            final String status = message.split(KeyProperties.DIVIDER)[1];

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Utils.getTwitter(TweetWearableService.this, AppSettings.getInstance(TweetWearableService.this)).updateStatus(status);
                    } catch (Exception e) { }
                }
            }).start();
        } else if (message.startsWith(KeyProperties.REQUEST_RETWEET)) {
            final long tweetId = Long.parseLong(message.split(KeyProperties.DIVIDER)[1]);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Utils.getTwitter(TweetWearableService.this, AppSettings.getInstance(TweetWearableService.this)).retweetStatus(tweetId);
                    } catch (Exception e) { }
                }
            }).start();
        } else if (message.startsWith(KeyProperties.REQUEST_IMAGE)) {
            final String url = message.split(KeyProperties.DIVIDER)[1];
            Bitmap image = null;

            try {
                cache.get(url).getBitmap();
            } catch (Exception e) {

            }

            if (image != null) {
                image = adjustImage(image);

                sendImage(image, url, wearableUtils, googleApiClient);
            } else {
                // download it
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                            InputStream is = new BufferedInputStream(conn.getInputStream());

                            Bitmap image = decodeSampledBitmapFromResourceMemOpt(is, 500, 500);

                            try {
                                is.close();
                            } catch (Exception e) {

                            }
                            try {
                                conn.disconnect();
                            } catch (Exception e) {

                            }

                            cache.put(url, image);
                            image = adjustImage(image);

                            sendImage(image, url, wearableUtils, googleApiClient);
                        } catch (Exception e) {

                        }
                    }
                }).start();
            }
        } else {
            Log.e(TAG, "message not recognized");
        }
    }

    public void sendImage(Bitmap image, String url, WearableUtils wearableUtils, GoogleApiClient googleApiClient) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(KeyProperties.PATH);
        byte[] bytes = new IOUtils().convertToByteArray(image);
        dataMap.getDataMap().putByteArray(KeyProperties.KEY_IMAGE_DATA, bytes);
        dataMap.getDataMap().putString(KeyProperties.KEY_IMAGE_NAME, url);
        for (String node : wearableUtils.getNodes(googleApiClient)) {
            Wearable.MessageApi.sendMessage(googleApiClient, node, KeyProperties.PATH, dataMap.asPutDataRequest().getData());
            Log.v(TAG, "sent " + bytes.length + " bytes of data to node " + node);
        }
    }
    
    public Bitmap adjustImage(Bitmap b) {
        if (b == null) {
            return null;
        }

        if (b.getWidth() >= b.getHeight()){
            b = Bitmap.createBitmap(
                    b,
                    b.getWidth() / 2 - b.getHeight() / 2,
                    0,
                    b.getHeight(),
                    b.getHeight()
            );
        } else {
            b = Bitmap.createBitmap(
                    b,
                    0,
                    b.getHeight()/2 - b.getWidth()/2,
                    b.getWidth(),
                    b.getWidth()
            );
        }
        
        b = Bitmap.createScaledBitmap(b, Utils.toDP(40, this), Utils.toDP(40, this), true);

        return b;
    }

    public Bitmap decodeSampledBitmapFromResourceMemOpt(
            InputStream inputStream, int reqWidth, int reqHeight) {

        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = inputStream.read(buffer)) > -1) {
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

            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options opt, int reqWidth, int reqHeight) {
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
}
