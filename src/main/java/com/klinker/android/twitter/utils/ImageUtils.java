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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.klinker.android.launcher.api.ResourceHelper;
import com.klinker.android.twitter.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;


public class ImageUtils {

    public static Bitmap getCircle(Bitmap currentImage, Context context) {
        int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, context.getResources().getDisplayMetrics());

        Bitmap bitmap;

        if (currentImage == null) {
            return null;
        }

        if (currentImage.getWidth() >= currentImage.getHeight()){
            bitmap = Bitmap.createBitmap(
                    currentImage,
                    currentImage.getWidth() / 2 - currentImage.getHeight() / 2,
                    0,
                    currentImage.getHeight(),
                    currentImage.getHeight()
            );
        } else {
            bitmap = Bitmap.createBitmap(
                    currentImage,
                    0,
                    currentImage.getHeight()/2 - currentImage.getWidth()/2,
                    currentImage.getWidth(),
                    currentImage.getWidth()
            );
        }

        Bitmap output;

        try {
            output = Bitmap.createBitmap(scale, scale, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        Rect rect = new Rect(0, 0, scale, scale);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(scale / 2, scale / 2, (scale / 2) - (scale / 25), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        try {
            canvas.drawBitmap(bitmap, null, rect, paint);
        } catch (Exception e) {
            // bitmap is null i guess
        }

        ResourceHelper helper = new ResourceHelper(context, "com.klinker.android.twitter");
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(helper.getDimension("contact_picture_border"));

        try {
            TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.circle_border});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            paint.setColor(context.getResources().getColor(resource));
        } catch (Exception e) {
            paint.setColor(helper.getColor("circle_outline_dark"));
        }

        canvas.drawCircle(scale / 2, scale / 2, (scale / 2) - (scale / 25), paint);

        return output;
    }

    public static Bitmap getSizedCircle(Bitmap currentImage, Context context, int dp) {

        int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());

        if (currentImage == null) {
            return null;
        }

        if (currentImage.getWidth() >= currentImage.getHeight()) {
            currentImage = Bitmap.createBitmap(
                    currentImage,
                    currentImage.getWidth()/2 - currentImage.getHeight()/2,
                    0,
                    currentImage.getHeight(),
                    currentImage.getHeight()
            );
        } else {
            currentImage = Bitmap.createBitmap(
                    currentImage,
                    0,
                    currentImage.getHeight()/2 - currentImage.getWidth()/2,
                    currentImage.getWidth(),
                    currentImage.getWidth()
            );
        }

        Bitmap output;

        try {
            output = Bitmap.createBitmap(scale, scale, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        Rect rect = new Rect(0, 0, scale, scale);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(scale / 2, scale / 2, (scale / 2) - (scale / 25), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        try {
            canvas.drawBitmap(currentImage, null, rect, paint);
        } catch (Exception e) {
            // bitmap is null i guess
        }

        ResourceHelper helper = new ResourceHelper(context, "com.klinker.android.twitter");
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(helper.getDimension("contact_picture_border"));

        try {
            TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.circle_border});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            paint.setColor(context.getResources().getColor(resource));
        } catch (Exception e) {
            paint.setColor(helper.getColor("circle_outline_dark"));
        }

        canvas.drawCircle(scale / 2, scale / 2, (scale / 2) - (scale / 25), paint);

        return resizeImage(context, output, dp);
    }

    public static Bitmap getBiggerCircle(Bitmap currentImage, Context context) {
        int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, context.getResources().getDisplayMetrics());

        Bitmap bitmap;

        if (currentImage.getWidth() >= currentImage.getHeight()) {
            bitmap = Bitmap.createBitmap(
                    currentImage,
                    currentImage.getWidth() / 2 - currentImage.getHeight() / 2,
                    0,
                    currentImage.getHeight(),
                    currentImage.getHeight()
            );
        } else {
            bitmap = Bitmap.createBitmap(
                    currentImage,
                    0,
                    currentImage.getHeight()/2 - currentImage.getWidth()/2,
                    currentImage.getWidth(),
                    currentImage.getWidth()
            );
        }

        Bitmap output;

        try {
            output = Bitmap.createBitmap(scale,
                    scale, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        Rect rect = new Rect(0, 0, scale, scale);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(scale / 2, scale / 2, (scale / 2) - (scale / 25), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        try {
            canvas.drawBitmap(bitmap, null, rect, paint);
        } catch (Exception e) {
            // bitmap is null i guess

        }
        
        ResourceHelper helper = new ResourceHelper(context, "com.klinker.android.twitter");
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(helper.getDimension("contact_picture_border"));

        try {
            TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.circle_border});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            paint.setColor(context.getResources().getColor(resource));
        } catch (Exception e) {
            paint.setColor(helper.getColor("circle_outline_dark"));
        }

        canvas.drawCircle(scale / 2, scale / 2, (scale / 2) - (scale / 25), paint);

        return output;
    }

    public static Bitmap cropSquare(Bitmap currentImage) {
        if (currentImage.getWidth() >= currentImage.getHeight()) {
            currentImage = Bitmap.createBitmap(
                    currentImage,
                    currentImage.getWidth() / 2 - currentImage.getHeight() / 2,
                    0,
                    currentImage.getHeight(),
                    currentImage.getHeight()
            );
        } else {
            currentImage = Bitmap.createBitmap(
                    currentImage,
                    0,
                    currentImage.getHeight()/2 - currentImage.getWidth()/2,
                    currentImage.getWidth(),
                    currentImage.getWidth()
            );
        }

        return currentImage;
    }
    public static Bitmap notificationResize(Context context, Bitmap currentImage) {
        try {
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, context.getResources().getDisplayMetrics());

            return Bitmap.createScaledBitmap(currentImage, scale, scale, true);
        } catch (OutOfMemoryError e) {
            return currentImage;
        }
    }

    public static Bitmap resizeImage(Context context, Bitmap currentImage, int dp) {
        try {
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());

            return Bitmap.createScaledBitmap(currentImage, scale, scale, true);
        } catch (OutOfMemoryError e) {
            return currentImage;
        }
    }

    public static Bitmap blur(Bitmap sentBitmap) {

        int radius = 4;

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }

            stackpointer = radius;

            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }

                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }

            yw += w;
        }

        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;

            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }

            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }

                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    public static Bitmap overlayPlay(Bitmap bmp1, Context context) {
        Bitmap bmp2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_play, null);

        Bitmap bmOverlay = Bitmap.createBitmap( bmp1.getWidth(), bmp1.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bmOverlay);
        canvas2.drawBitmap(bmp1, 0, 0, null);
        canvas2.drawBitmap(bmp2, Utils.toDP(64, context), Utils.toDP(64, context), null);
        return bmOverlay;
    }

    public static void loadImage(Context context, final ImageView iv, String url, BitmapLruCache mCache) {

        if (url == null) {
            return;
        }

        BitmapDrawable wrapper = mCache.getFromMemoryCache(url);

        if (null != wrapper && iv.getVisibility() != View.GONE) {
            // The cache has it, so just display it
            iv.setImageDrawable(wrapper);

            try {
                Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                iv.startAnimation(fadeInAnimation);
            } catch (Exception e) {

            }
        } else {
            // Memory Cache doesn't have the URL, do threaded request...
            iv.setImageDrawable(null);

            imageUrlAsyncTask(context, iv, mCache, false, url);

        }
    }

    private static void imageUrlAsyncTask(final Context context, final ImageView imageView, final BitmapLruCache mCache, final boolean profile, final String url) {

        final WeakReference<ImageView> mImageViewRef = new WeakReference<ImageView>(imageView);

        Thread imageDownload = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Return early if the ImageView has disappeared.
                    if (null == mImageViewRef.get()) {
                        return;
                    }

                    // Now we're not on the main thread we can check all caches
                    CacheableBitmapDrawable result;

                    result = mCache.get(url, null);

                    if (null == result || profile) {

                        String mUrl = url;

                        if (url.contains("twitpic")) {
                            try {
                                URL address = new URL(url);
                                HttpURLConnection connection = (HttpURLConnection) address.openConnection(Proxy.NO_PROXY);
                                connection.setConnectTimeout(1000);
                                connection.setInstanceFollowRedirects(false);
                                connection.setReadTimeout(1000);
                                connection.connect();
                                String expandedURL = connection.getHeaderField("Location");
                                if(expandedURL != null) {
                                    mUrl = expandedURL;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // The bitmap isn't cached so download from the web
                        HttpURLConnection conn = (HttpURLConnection) new URL(mUrl).openConnection();
                        InputStream is = new BufferedInputStream(conn.getInputStream());

                        Bitmap b = decodeSampledBitmapFromResourceMemOpt(is, 500, 500);

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
                            result = mCache.put(mUrl, b);
                        } catch (Exception e) {
                            result = null;
                        }
                    }

                    final CacheableBitmapDrawable fResult = result;
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final ImageView iv = mImageViewRef.get();

                                if (null != iv && iv.getVisibility() != View.GONE) {
                                    iv.setImageDrawable(fResult);
                                    Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);

                                    iv.startAnimation(fadeInAnimation);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (IOException e) {
                    Log.e("ImageUrlAsyncTask", e.toString());
                } catch (OutOfMemoryError e) {
                    Log.v("ImageUrlAsyncTask", "Out of memory error here");
                } catch (Exception e) {
                    // something else
                    e.printStackTrace();
                }
            }
        });

        imageDownload.setPriority(8);
        imageDownload.start();
    }

    public static Bitmap decodeSampledBitmapFromResourceMemOpt(
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

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
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

    public static void loadCircleImage(Context context, final ImageView iv, String url, BitmapLruCache mCache) {
        BitmapDrawable wrapper = null;
        if (url != null) {
            wrapper = mCache.getFromMemoryCache(url);
        }

        if (null != wrapper && iv.getVisibility() != View.GONE) {
            // The cache has it, so just display it
            Log.v("talon_image_cache", "got image from cache");
            iv.setImageDrawable(wrapper);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);

            iv.startAnimation(fadeInAnimation);
        } else if (url != null) {
            // Memory Cache doesn't have the URL, do threaded request...
            iv.setImageDrawable(null);

            ImageUrlCircleAsyncTask mCurrentTask = new ImageUrlCircleAsyncTask(context, iv, mCache, false);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    SDK11.executeOnThreadPool(mCurrentTask, url);
                } else {
                    mCurrentTask.execute(url);
                }
            } catch (RejectedExecutionException e) {
                // This shouldn't happen, but might.
            }

        }
    }

    public static void loadSizedCircleImage(Context context, final ImageView iv, String url, BitmapLruCache mCache, int dp) {

        // don't want to find the cached one
        iv.setImageDrawable(null);

        ImageUrlCircleSizedAsyncTask mCurrentTask = new ImageUrlCircleSizedAsyncTask(context, iv, mCache, dp);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                SDK11.executeOnThreadPool(mCurrentTask, url);
            } else {
                mCurrentTask.execute(url);
            }
        } catch (RejectedExecutionException e) {
            // This shouldn't happen, but might.
        }
    }

    public static void loadCircleImage(Context context, final ImageView iv, String url, BitmapLruCache mCache, boolean largerProfile) {

        // don't want to find the cached one
        iv.setImageDrawable(null);

        ImageUrlCircleAsyncTask mCurrentTask = new ImageUrlCircleAsyncTask(context, iv, mCache, largerProfile);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                SDK11.executeOnThreadPool(mCurrentTask, url);
            } else {
                mCurrentTask.execute(url);
            }
        } catch (RejectedExecutionException e) {
            // This shouldn't happen, but might.
        }
    }

    private static class ImageUrlCircleAsyncTask
            extends AsyncTask<String, Void, CacheableBitmapDrawable> {

        private final BitmapLruCache mCache;
        private Context context;
        private final WeakReference<ImageView> mImageViewRef;
        private ImageView iv;
        private boolean largeProfile;

        ImageUrlCircleAsyncTask(Context context, ImageView imageView, BitmapLruCache cache, boolean profile) {
            this.context = context;
            mCache = cache;
            mImageViewRef = new WeakReference<ImageView>(imageView);
            iv = imageView;
            largeProfile = profile;
        }

        @Override
        protected CacheableBitmapDrawable doInBackground(String... params) {
            try {
                // Return early if the ImageView has disappeared.
                if (null == mImageViewRef.get()) {
                    return null;
                }
                final String url = params[0];

                // Now we're not on the main thread we can check all caches
                CacheableBitmapDrawable result;

                try {
                    result = mCache.get(url);
                } catch (OutOfMemoryError e) {
                    return null;
                }

                Log.v("talon_image_cache", "result: " + result);

                if (null == result || largeProfile) {
                    Log.d("talon_image_cache", "Downloading: " + url);

                    // The bitmap isn't cached so download from the web
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    InputStream is = new BufferedInputStream(conn.getInputStream());

                    Bitmap b = decodeSampledBitmapFromResourceMemOpt(is, 400, 400);
                    if (largeProfile) {
                        Log.v("talon_picture", "bigger profile image");
                        b = getBiggerCircle(b, context);
                    } else {
                        b = getCircle(b, context);
                    }

                    // Add to cache
                    try {
                        result = mCache.put(url, b);
                    } catch (Exception e) {
                        result = null;
                    }

                    try {
                        is.close();
                    } catch (Exception e) {

                    }
                    try {
                        conn.disconnect();
                    } catch (Exception e) {

                    }

                } else {
                    Log.d("talon_image_cache", "Got from Cache: " + url);
                }

                return result;

            } catch (IOException e) {
                Log.e("talon_image_cache", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(CacheableBitmapDrawable result) {
            super.onPostExecute(result);

            try {
                final ImageView iv = mImageViewRef.get();

                if (null != iv && iv.getVisibility() != View.GONE) {
                    iv.setImageDrawable(result);
                    Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);

                    iv.startAnimation(fadeInAnimation);
                }

            } catch (Exception e) {

            }
        }
    }

    private static class ImageUrlCircleSizedAsyncTask
            extends AsyncTask<String, Void, CacheableBitmapDrawable> {

        private final BitmapLruCache mCache;
        private Context context;
        private final WeakReference<ImageView> mImageViewRef;
        private ImageView iv;
        private int dp;

        ImageUrlCircleSizedAsyncTask(Context context, ImageView imageView, BitmapLruCache cache, int dp) {
            this.context = context;
            mCache = cache;
            mImageViewRef = new WeakReference<ImageView>(imageView);
            iv = imageView;
            this.dp = dp;
        }

        @Override
        protected CacheableBitmapDrawable doInBackground(String... params) {
            try {
                // Return early if the ImageView has disappeared.
                if (null == mImageViewRef.get()) {
                    return null;
                }
                final String url = params[0];

                // Now we're not on the main thread we can check all caches
                CacheableBitmapDrawable result;

                try {
                    result = mCache.get(url + "_profile");
                } catch (OutOfMemoryError e) {
                    return null;
                }

                Log.v("talon_image_cache", "result: " + result);

                if (null == result) {
                    Log.d("talon_image_cache", "Downloading: " + url);

                    // The bitmap isn't cached so download from the web
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    InputStream is = new BufferedInputStream(conn.getInputStream());

                    Bitmap b = decodeSampledBitmapFromResourceMemOpt(is, 500, 500);

                    b = getSizedCircle(b, context, dp);

                    // Add to cache
                    try {
                        result = mCache.put(url + "_profile", b);
                    } catch (Exception e) {
                        result = null;
                    }

                    try {
                        is.close();
                    } catch (Exception e) {

                    }
                    try {
                        conn.disconnect();
                    } catch (Exception e) {

                    }

                } else {
                    Log.d("talon_image_cache", "Got from Cache: " + url);
                }

                return result;

            } catch (IOException e) {
                Log.e("talon_image_cache", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(CacheableBitmapDrawable result) {
            super.onPostExecute(result);

            try {
                final ImageView iv = mImageViewRef.get();

                if (null != iv && iv.getVisibility() != View.GONE) {
                    iv.setImageDrawable(result);
                    Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);

                    iv.startAnimation(fadeInAnimation);
                }

            } catch (Exception e) {

            }
        }
    }

    private static final int GROUP_RES = 1000;

    public static Bitmap combineBitmaps(Context context, Bitmap[] bitmaps) {
        int size = Utils.toDP(GROUP_RES, context);
        // need to make them square
        for (int i = 0; i < bitmaps.length; i++) {
            Bitmap currentImage = bitmaps[i];

            if (currentImage == null) {
                return bitmaps[0];
            }

            if (currentImage.getWidth() >= currentImage.getHeight()){
                currentImage = Bitmap.createBitmap(
                        currentImage,
                        currentImage.getWidth() / 2 - currentImage.getHeight() / 2,
                        0,
                        currentImage.getHeight(),
                        currentImage.getHeight()
                );
            } else {
                currentImage = Bitmap.createBitmap(
                        currentImage,
                        0,
                        currentImage.getHeight()/2 - currentImage.getWidth()/2,
                        currentImage.getWidth(),
                        currentImage.getWidth()
                );
            }

            try {
                bitmaps[i] = Bitmap.createScaledBitmap(currentImage, GROUP_RES, GROUP_RES, true);
            } catch (Exception e) {

            }
        }

        try {
            switch (bitmaps.length) {
                case 2:
                    Bitmap image = Bitmap.createBitmap(GROUP_RES, GROUP_RES, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(image);
                    canvas.drawBitmap(bitmaps[0], 0, 0, null);
                    canvas.drawBitmap(bitmaps[1], GROUP_RES / 2, 0, null);

                    Paint linePaint = new Paint();
                    linePaint.setStrokeWidth(1f);
                    linePaint.setColor(context.getResources().getColor(R.color.circle_outline_dark));

                    canvas.drawLine(GROUP_RES / 2, 0, GROUP_RES / 2, GROUP_RES, linePaint);
                    return image;
                case 3:
                    image = Bitmap.createBitmap(GROUP_RES, GROUP_RES, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(image);
                    canvas.drawBitmap(bitmaps[0], 0, 0, null);
                    canvas.drawBitmap(bitmaps[1], GROUP_RES / 2, 0, null);
                    canvas.drawBitmap(bitmaps[2], GROUP_RES / 2, GROUP_RES / 2, null);

                    linePaint = new Paint();
                    linePaint.setStrokeWidth(1f);
                    linePaint.setColor(context.getResources().getColor(R.color.circle_outline_dark));

                    canvas.drawLine(GROUP_RES / 2, 0, GROUP_RES / 2, GROUP_RES, linePaint);
                    canvas.drawLine(GROUP_RES / 2, GROUP_RES / 2, GROUP_RES, GROUP_RES / 2, linePaint);
                    return image;
                case 4:
                    image = Bitmap.createBitmap(GROUP_RES, GROUP_RES, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(image);
                    canvas.drawBitmap(bitmaps[0], 0, 0, null);
                    canvas.drawBitmap(bitmaps[1], GROUP_RES / 2, 0, null);
                    canvas.drawBitmap(bitmaps[2], GROUP_RES / 2, GROUP_RES / 2, null);
                    canvas.drawBitmap(bitmaps[3], 0, GROUP_RES / 2, null);

                    linePaint = new Paint();
                    linePaint.setStrokeWidth(1f);
                    linePaint.setColor(context.getResources().getColor(R.color.circle_outline_dark));

                    canvas.drawLine(GROUP_RES / 2, 0, GROUP_RES / 2, GROUP_RES, linePaint);
                    canvas.drawLine(0, GROUP_RES / 2, GROUP_RES, GROUP_RES / 2, linePaint);
                    return image;
            }
        } catch (Exception e) {
            // fall through if an exception occurs and just show the default image
        }

        return bitmaps[0];
    }
}