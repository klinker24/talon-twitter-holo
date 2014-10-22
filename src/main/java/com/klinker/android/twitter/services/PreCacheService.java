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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import android.util.Log;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.sq_lite.HomeDataSource;
import com.klinker.android.twitter.data.sq_lite.HomeSQLiteHelper;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.utils.Utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;


public class PreCacheService extends IntentService {

    private static final boolean DEBUG = false;

    SharedPreferences sharedPrefs;

    public PreCacheService() {
        super("PreCacheService");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        if (DEBUG) {
            Log.v("talon_pre_cache", "starting the service, current time: " + Calendar.getInstance().getTime().toString());
        }

        // if they want it only over wifi and they are on mobile data
        if (getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE)
                .getBoolean("pre_cache_wifi_only", false) &&
                Utils.getConnectionStatus(this)) {

            if (DEBUG) {
                Log.v("talon_pre_cache", "quit for connection");
            }

            // just quit because we don't want it to happen
            return;
        }

        BitmapLruCache mCache = App.getInstance(this).getBitmapCache();
        AppSettings settings = AppSettings.getInstance(this);
        Cursor cursor = HomeDataSource.getInstance(this).getUnreadCursor(settings.currentAccount);

        if (cursor.moveToFirst()) {
            if (DEBUG) {
                Log.v("talon_pre_cache", "found database and moved to first picture. cursor size: " + cursor.getCount());
            }
            boolean cont = true;
            do {
                String profilePic = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_PRO_PIC));
                String imageUrl = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_PIC_URL));


                if (!mCache.contains(profilePic)) {

                    try {
                        HttpURLConnection conn = (HttpURLConnection) new URL(profilePic).openConnection();
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

                        if (settings.roundContactImages) {
                            image = ImageUtils.getCircle(image, this);
                        }

                        mCache.put(profilePic, image);
                    } catch (Throwable e) {
                        if (DEBUG) {
                            Log.v("talon_pre_cache", "found an exception while downloading profile pic");
                            e.printStackTrace();
                        }

                        // just stop I guess
                        cont = false;
                    }
                }


                if (!imageUrl.equals("")) {
                    if (!mCache.contains(imageUrl)) {
                        try {
                            if (!imageUrl.contains(" ")) {
                                HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                                InputStream is = new BufferedInputStream(conn.getInputStream());

                                Bitmap image = decodeSampledBitmapFromResourceMemOpt(is, 1000, 1000);

                                try {
                                    is.close();
                                } catch (Exception e) {

                                }
                                try {
                                    conn.disconnect();
                                } catch (Exception e) {

                                }

                                mCache.put(imageUrl, image);
                            } else {
                                String[] pics = imageUrl.split(" ");
                                Bitmap[] bitmaps = new Bitmap[pics.length];

                                // need to download all of them, then combine them
                                for (int i = 0; i < pics.length; i++) {
                                    String s = pics[i];

                                    // The bitmap isn't cached so download from the web
                                    HttpURLConnection conn = (HttpURLConnection) new URL(s).openConnection();
                                    InputStream is = new BufferedInputStream(conn.getInputStream());

                                    Bitmap b = decodeSampledBitmapFromResourceMemOpt(is, 1000, 1000);

                                    try {
                                        is.close();
                                    } catch (Exception e) {

                                    }
                                    try {
                                        conn.disconnect();
                                    } catch (Exception e) {

                                    }

                                    // Add to cache
                                    try {
                                        mCache.put(s, b);

                                        // throw it into our bitmap array for later
                                        bitmaps[i] = b;
                                    } catch (Exception e) {

                                    }
                                }

                                // now that we have all of them, we need to put them together
                                Bitmap combined = ImageUtils.combineBitmaps(this, bitmaps);

                                try {
                                    mCache.put(imageUrl, combined);
                                } catch (Exception e) {

                                }
                            }
                        } catch (Throwable e) {
                            if (DEBUG) {
                                Log.v("talon_pre_cache", "found an exception while downloading image");
                                e.printStackTrace();
                            }

                            // just stop I guess
                            cont = false;
                        }
                    }
                }

            } while (cursor.moveToNext() && cont);

            if (DEBUG) {
                Log.v("talon_pre_cache", "done with service. time: " + Calendar.getInstance().getTime().toString());
            }
        }
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
