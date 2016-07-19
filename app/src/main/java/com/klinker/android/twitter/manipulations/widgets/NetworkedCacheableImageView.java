package com.klinker.android.twitter.manipulations.widgets;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.utils.SDK11;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

import com.klinker.android.twitter.utils.Utils;
import com.klinker.android.twitter.utils.api_helper.TwitterDMPicHelper;
import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;
import uk.co.senab.bitmapcache.CacheableImageView;

/**
 * Simple extension of CacheableImageView which allows downloading of Images of the Internet.
 *
 * This code isn't production quality, but works well enough for this sample.s
 *
 * @author Chris Banes
 */
public class NetworkedCacheableImageView extends CacheableImageView {

    public static final int BLUR = 1;
    public static final int CIRCLE = 2;
    public static final int THUMBNAIL = 3;

    public interface OnImageLoadedListener {
        void onImageLoaded(CacheableBitmapDrawable result);
    }

    private static class ImageUrlAsyncTask
            extends AsyncTask<String, Void, CacheableBitmapDrawable> {

        private final BitmapLruCache mCache;

        private final WeakReference<ImageView> mImageViewRef;
        private final OnImageLoadedListener mListener;

        private final BitmapFactory.Options mDecodeOpts;
        private String url;

        private int transform;
        private Context context;
        private boolean fromCache;

        ImageUrlAsyncTask(Context context, ImageView imageView, BitmapLruCache cache,
                          BitmapFactory.Options decodeOpts, OnImageLoadedListener listener) {
            this.context = context;
            mCache = cache;
            mImageViewRef = new WeakReference<ImageView>(imageView);
            mListener = listener;
            mDecodeOpts = decodeOpts;
            transform = 0;
            fromCache = true;
        }

        ImageUrlAsyncTask(Context context, ImageView imageView, BitmapLruCache cache,
                          BitmapFactory.Options decodeOpts, OnImageLoadedListener listener, int transform) {
            this.context = context;
            mCache = cache;
            mImageViewRef = new WeakReference<ImageView>(imageView);
            mListener = listener;
            mDecodeOpts = decodeOpts;
            this.transform = transform;
            fromCache = true;
        }

        ImageUrlAsyncTask(Context context, ImageView imageView, BitmapLruCache cache,
                          BitmapFactory.Options decodeOpts, OnImageLoadedListener listener, int transform, boolean fromCache) {
            this.context = context;
            mCache = cache;
            mImageViewRef = new WeakReference<ImageView>(imageView);
            mListener = listener;
            mDecodeOpts = decodeOpts;
            this.transform = transform;
            this.fromCache = fromCache;
        }

        @Override
        protected CacheableBitmapDrawable doInBackground(String... params) {
            try {
                // Return early if the ImageView has disappeared.
                if (null == mImageViewRef.get()) {
                    return null;
                }

                url = params[0];

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
                            url = expandedURL;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Log.v("talon_images", "url: " + url);


                // Now we're not on the main thread we can check all caches
                CacheableBitmapDrawable result;

                if (fromCache) {
                    result = mCache.get(url, mDecodeOpts);
                } else {
                    Log.v("from_cache", "not from cache");
                    result = null;
                }

                if (null == result) {

                    Bitmap b;
                    if (url.contains("ton.twitter.com")) {
                        // it is a direct message picture
                        TwitterDMPicHelper helper = new TwitterDMPicHelper();
                        b = helper.getDMPicture(url, Utils.getTwitter(context, AppSettings.getInstance(context)));
                    } else {

                        // The bitmap isn't cached so download from the web
                        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                        InputStream is = new BufferedInputStream(conn.getInputStream());


                        b = decodeSampledBitmapFromResourceMemOpt(is, 500, 500);

                        if (b != null) {
                            if (transform == CIRCLE) {
                                b = ImageUtils.getCircle(b, context);
                            } else if (transform == BLUR) {
                                b = ImageUtils.blur(b);
                            } else if (transform == THUMBNAIL) {
                                b = ImageUtils.overlayPlay(b, context);
                            }

                            // Add to cache
                            try {
                                if (fromCache) {
                                    result = mCache.put(url, b);
                                } else {
                                    result = mCache.put("no_cache", b);
                                }
                            } catch (NullPointerException e) {
                                // the bitmap couldn't be found
                            }
                        } else {
                            return null;
                        }
                    }

                } else {
                    Log.d("ImageUrlAsyncTask", "Got from Cache: " + url);
                }

                return result;

            } catch (IOException e) {
                Log.e("ImageUrlAsyncTask", e.toString());
            } catch (OutOfMemoryError e) {
                Log.v("ImageUrlAsyncTask", "Out of memory error here");
            }

            return null;
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

        @Override
        protected void onPostExecute(CacheableBitmapDrawable result) {
            super.onPostExecute(result);

            try {
                final ImageView iv = mImageViewRef.get();
                if (null != iv && iv.getVisibility() != View.GONE) {
                    iv.setImageDrawable(result);
                    iv.setTag(url);
                    Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);

                    iv.startAnimation(fadeInAnimation);
                }

                if (null != mListener) {
                    mListener.onImageLoaded(result);
                }
            } catch (Exception e) {

            }

            context.sendBroadcast(new Intent("com.klinker.android.twitter.IMAGE_LOADED"));
        }
    }

    private final BitmapLruCache mCache;

    private ImageUrlAsyncTask mCurrentTask;

    public NetworkedCacheableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCache = App.getInstance(context).getBitmapCache();
    }

    /**
     * Loads the Bitmap.
     *
     * @param url      - URL of image
     * @param fullSize - Whether the image should be kept at the original size
     * @return true if the bitmap was found in the cache
     */
    public boolean loadImage(String url, final boolean fullSize, OnImageLoadedListener listener) {
        // First check whether there's already a task running, if so cancel it
        if (null != mCurrentTask) {
            mCurrentTask.cancel(true);
        }

        // Check to see if the memory cache already has the bitmap. We can
        // safely do
        // this on the main thread.
        BitmapDrawable wrapper = mCache.getFromMemoryCache(url);

        if (null != wrapper && getVisibility() != View.GONE) {
            // The cache has it, so just display it
            setImageDrawable(wrapper);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);

            startAnimation(fadeInAnimation);

            getContext().sendBroadcast(new Intent("com.klinker.android.twitter.IMAGE_LOADED"));

            if (null != listener) {
                listener.onImageLoaded(null);
            }

            return true;
        } else {
            // Memory Cache doesn't have the URL, do threaded request...
            setImageDrawable(null);

            BitmapFactory.Options decodeOpts = null;

            if (!fullSize) {
                //decodeOpts = new BitmapFactory.Options();
                //decodeOpts.inSampleSize = 2;
            }

            mCurrentTask = new ImageUrlAsyncTask(getContext(), this, mCache, decodeOpts, listener, 0);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    SDK11.executeOnThreadPool(mCurrentTask, url);
                } else {
                    mCurrentTask.execute(url);
                }
            } catch (RejectedExecutionException e) {
                // This shouldn't happen, but might.
            }

            return false;
        }
    }


    public boolean loadImage(String url, final boolean fullSize, OnImageLoadedListener listener, int transform) {
        // First check whether there's already a task running, if so cancel it
        if (null != mCurrentTask) {
            mCurrentTask.cancel(true);
        }

        // Check to see if the memory cache already has the bitmap. We can
        // safely do
        // this on the main thread.
        BitmapDrawable wrapper = mCache.getFromMemoryCache(url);

        if (null != wrapper && getVisibility() != View.GONE) {
            // The cache has it, so just display it
            setImageDrawable(wrapper);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);

            startAnimation(fadeInAnimation);

            getContext().sendBroadcast(new Intent("com.klinker.android.twitter.IMAGE_LOADED"));

            if (null != listener) {
                listener.onImageLoaded(null);
            }

            return true;
        } else {
            // Memory Cache doesn't have the URL, do threaded request...
            setImageDrawable(null);

            BitmapFactory.Options decodeOpts = null;

            if (!fullSize) {
                //decodeOpts = new BitmapFactory.Options();
                //decodeOpts.inSampleSize = 2;
            }

            mCurrentTask = new ImageUrlAsyncTask(getContext(), this, mCache, decodeOpts, listener, transform);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    SDK11.executeOnThreadPool(mCurrentTask, url);
                } else {
                    mCurrentTask.execute(url);
                }
            } catch (RejectedExecutionException e) {
                // This shouldn't happen, but might.
            }

            return false;
        }
    }


    public boolean loadImage(String url, final boolean fullSize, OnImageLoadedListener listener, int transform, boolean fromCache) {
        // First check whether there's already a task running, if so cancel it
        if (null != mCurrentTask) {
            mCurrentTask.cancel(true);
        }

        // Check to see if the memory cache already has the bitmap. We can
        // safely do
        // this on the main thread.
        BitmapDrawable wrapper;
        if (fromCache) {
            wrapper = mCache.getFromMemoryCache(url);
        } else {
            wrapper = null;
        }
        if (null != wrapper && getVisibility() != View.GONE) {
            // The cache has it, so just display it
            setImageDrawable(wrapper);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);

            startAnimation(fadeInAnimation);

            getContext().sendBroadcast(new Intent("com.klinker.android.twitter.IMAGE_LOADED"));

            if (null != listener) {
                listener.onImageLoaded(null);
            }

            return true;
        } else {
            // Memory Cache doesn't have the URL, do threaded request...
            setImageDrawable(null);

            BitmapFactory.Options decodeOpts = null;

            if (!fullSize) {
                //decodeOpts = new BitmapFactory.Options();
                //decodeOpts.inSampleSize = 2;
            }

            mCurrentTask = new ImageUrlAsyncTask(getContext(), this, mCache, decodeOpts, listener, transform, fromCache);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    SDK11.executeOnThreadPool(mCurrentTask, url);
                } else {
                    mCurrentTask.execute(url);
                }
            } catch (RejectedExecutionException e) {
                // This shouldn't happen, but might.
            }

            return false;
        }
    }

}