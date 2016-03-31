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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.manipulations.widgets.ActionBarDrawerToggle;
import com.klinker.android.twitter.settings.AppSettings;

import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Utils {

    public static Twitter getTwitter(Context context, AppSettings settings) {
        if (settings == null) {
            settings = AppSettings.getInstance(context);
        }
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(AppSettings.TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(AppSettings.TWITTER_CONSUMER_SECRET)
                .setOAuthAccessToken(settings.authenticationToken)
                .setOAuthAccessTokenSecret(settings.authenticationTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

    public static Twitter getTwitter(Context context) {
        AppSettings settings = AppSettings.getInstance(context);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(AppSettings.TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(AppSettings.TWITTER_CONSUMER_SECRET)
                .setOAuthAccessToken(settings.authenticationToken)
                .setOAuthAccessTokenSecret(settings.authenticationTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

    public static TwitterStream getStreamingTwitter(Context context, AppSettings settings) {
        settings = AppSettings.getInstance(context);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(AppSettings.TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(AppSettings.TWITTER_CONSUMER_SECRET)
                .setOAuthAccessToken(settings.authenticationToken)
                .setOAuthAccessTokenSecret(settings.authenticationTokenSecret);
        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
        return tf.getInstance();
    }

    public static Twitter getSecondTwitter(Context context) {
        AppSettings settings = AppSettings.getInstance(context);
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(AppSettings.TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(AppSettings.TWITTER_CONSUMER_SECRET)
                .setOAuthAccessToken(settings.secondAuthToken)
                .setOAuthAccessTokenSecret(settings.secondAuthTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time, Context context) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = getCurrentTime();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return diff / SECOND_MILLIS + "s";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return 1 + "m";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return 1 + "h";
        } else if (diff < 24 * HOUR_MILLIS) {
            if (diff / HOUR_MILLIS == 1)
                return 1 + "h";
            else
                return diff / HOUR_MILLIS + "h";
        } else if (diff < 48 * HOUR_MILLIS) {
            return 1 + "d";
        } else {
            return diff / DAY_MILLIS + "d";
        }
    }

    private static long getCurrentTime() {
        return new Date().getTime();
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            Log.v("talon_actionbar", "getting size from dimen");
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        } else {
            return 48;
        }
    }

    public static int getNavBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    public static String getTranslateURL(final String lang) {
        return "https://translate.google.com/m/translate#auto|" +
        			 lang + 
        			 "|";
    }

    public static boolean hasNavBar(Context context) {
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        Point realSize = new Point();
        display.getSize(size);
        display.getRealSize(realSize);

        try {
            return Math.max(size.x, size.y) < Math.max(realSize.x, realSize.y) || (context.getResources().getBoolean(R.bool.isTablet) && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        } catch (Exception e) {
            return false;
        }
    }

    // true if on mobile data
    // false otherwise
    public static boolean getConnectionStatus(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return false;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }

        return false;
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public static int toDP(int px, Context context) {
        try {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
        } catch (Exception e) {
            return px;
        }
    }

    public static boolean isPackageInstalled(Context context, String targetPackage){
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static void setUpTheme(Context context, AppSettings settings) {
        if (settings.layout == AppSettings.LAYOUT_TALON) {
            Log.v("talon_theme", "setting talon theme");
            switch (settings.theme) {
                case AppSettings.THEME_LIGHT:
                    context.setTheme(R.style.Theme_TalonLight);
                    break;
                case AppSettings.THEME_DARK:
                    context.setTheme(R.style.Theme_TalonDark);
                    break;
                case AppSettings.THEME_BLACK:
                    context.setTheme(R.style.Theme_TalonBlack);
                    break;
            }
        } else {
            Log.v("talon_theme", "setting talon theme");
            switch (settings.theme) {
                case AppSettings.THEME_LIGHT:
                    context.setTheme(R.style.Theme_TalonLight_Hangouts);
                    break;
                case AppSettings.THEME_DARK:
                    context.setTheme(R.style.Theme_TalonDark_Hangouts);
                    break;
                case AppSettings.THEME_BLACK:
                    context.setTheme(R.style.Theme_TalonBlack_Hangouts);
                    break;
            }
        }
    }

    public static void setUpPopupTheme(Context context, AppSettings settings) {
        if (settings.layout == AppSettings.LAYOUT_TALON) {
            switch (settings.theme) {
                case AppSettings.THEME_LIGHT:
                    context.setTheme(R.style.Theme_TalonLight_Popup);
                    break;
                case AppSettings.THEME_DARK:
                    context.setTheme(R.style.Theme_TalonDark_Popup);
                    break;
                case AppSettings.THEME_BLACK:
                    context.setTheme(R.style.Theme_TalonBlack_Popup);
                    break;
            }
        } else {
            switch (settings.theme) {
                case AppSettings.THEME_LIGHT:
                    context.setTheme(R.style.Theme_TalonLight_Hangouts_Popup);
                    break;
                case AppSettings.THEME_DARK:
                    context.setTheme(R.style.Theme_TalonDark_Hangouts_Popup);
                    break;
                case AppSettings.THEME_BLACK:
                    context.setTheme(R.style.Theme_TalonBlack_Hangouts_Popup);
                    break;
            }
        }
    }

    public static void setUpNotifTheme(Context context, AppSettings settings) {
        if (settings.layout == AppSettings.LAYOUT_TALON) {
            switch (settings.theme) {
                case AppSettings.THEME_LIGHT:
                    context.setTheme(R.style.Theme_TalonLight_Popup_Notif);
                    break;
                case AppSettings.THEME_DARK:
                    context.setTheme(R.style.Theme_TalonDark_Popup_Notif);
                    break;
                case AppSettings.THEME_BLACK:
                    context.setTheme(R.style.Theme_TalonBlack_Popup_Notif);
                    break;
            }
        } else {
            switch (settings.theme) {
                case AppSettings.THEME_LIGHT:
                    context.setTheme(R.style.Theme_TalonLight_Hangouts_Popup_Notif);
                    break;
                case AppSettings.THEME_DARK:
                    context.setTheme(R.style.Theme_TalonDark_Hangouts_Popup_Notif);
                    break;
                case AppSettings.THEME_BLACK:
                    context.setTheme(R.style.Theme_TalonBlack_Hangouts_Popup_Notif);
                    break;
            }
        }
    }

    public static void setActionBar(Context context) {
        AppSettings settings = AppSettings.getInstance(context);
        if (settings.actionBar != null) {
            //Drawable back = settings.actionBar;
            try {
                ((Activity) context).getActionBar().setBackgroundDrawable(settings.actionBar);
            } catch (Exception e) {
                // on the compose there isnt an action bar
            }
        }

        // we will only do this if it is specified with the function below
        //setWallpaper(settings, context);
    }

    public static void setActionBar(Context context, boolean setWallpaper) {
        setActionBar(context);

        if (setWallpaper) {
            setWallpaper(AppSettings.getInstance(context), context);
        }
    }

    protected static void setWallpaper(AppSettings settings, Context context) {
        if (settings.addonTheme) {
            if (settings.customBackground != null) {
                Log.v("custom_background", "attempting to set custom background");
                try {
                    //Drawable background = settings.customBackground;
                    ((Activity)context).getWindow().setBackgroundDrawable(settings.customBackground);
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.v("custom_background", "error setting custom background");
                }
            } else if (settings.customBackground == null) {
                ((Activity)context).getWindow().setBackgroundDrawable(new ColorDrawable(settings.backgroundColor));
            }
        } else {
            TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.windowBackground});
            int resource = a.getResourceId(0, 0);
            a.recycle();

            ((Activity)context).getWindow().getDecorView().setBackgroundResource(resource);
        }
    }
}
