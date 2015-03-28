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

package com.klinker.android.twitter.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.klinker.android.twitter.APIKeys;
import com.klinker.android.twitter.utils.EmojiUtils;

import java.util.Calendar;

public class AppSettings {

    public static AppSettings settings;

    public static AppSettings getInstance(Context context) {
        if (settings == null) {
            settings = new AppSettings(context);
        }
        return settings;
    }

    public static void invalidate() {
        settings = null;
    }

    public SharedPreferences sharedPrefs;

    public static String TWITTER_CONSUMER_KEY = APIKeys.TWITTER_CONSUMER_KEY;
    public static String TWITTER_CONSUMER_SECRET = APIKeys.TWITTER_CONSUMER_SECRET;

    public static String YOUTUBE_API_KEY = APIKeys.YOUTUBE_API_KEY;

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_BLACK = 2;

    public static final int WIDGET_LIGHT = 0;
    public static final int WIDGET_DARK = 1;
    public static final int WIDGET_TRANS_LIGHT = 2;
    public static final int WIDGET_TRANS_BLACK = 3;

    public static final int PAGE_TWEET = 0;
    public static final int PAGE_WEB = 1;
    public static final int PAGE_CONVO = 2;

    public static final int PAGE_TYPE_NONE = 0;
    public static final int PAGE_TYPE_PICS = 1;
    public static final int PAGE_TYPE_LINKS = 2;
    public static final int PAGE_TYPE_LIST = 3;
    public static final int PAGE_TYPE_FAV_USERS = 4;
    public static final int PAGE_TYPE_HOME = 5;
    public static final int PAGE_TYPE_MENTIONS = 6;
    public static final int PAGE_TYPE_DMS = 7;
    public static final int PAGE_TYPE_SECOND_MENTIONS = 8;
    public static final int PAGE_TYPE_WORLD_TRENDS = 9;
    public static final int PAGE_TYPE_LOCAL_TRENDS = 10;
    public static final int PAGE_TYPE_SAVED_SEARCH = 11;
    public static final int PAGE_TYPE_ACTIVITY = 12;
    public static final int PAGE_TYPE_FAVORITE_STATUS = 13;

    public static final int LAYOUT_TALON = 0;
    public static final int LAYOUT_HANGOUT = 1;
    public static final int LAYOUT_FULL_SCREEN = 2;

    public String authenticationToken;
    public String authenticationTokenSecret;
    public String secondAuthToken;
    public String secondAuthTokenSecret;
    public String myScreenName;
    public String secondScreenName;
    public String myName;
    public String myBackgroundUrl;
    public String myProfilePicUrl;
    public String secondProfilePicUrl;
    public String favoriteUserNames;

    public boolean isTwitterLoggedIn;
    public boolean reverseClickActions;
    public boolean advanceWindowed;
    public boolean notifications;
    public boolean led;
    public boolean vibrate;
    public boolean sound;
    public boolean refreshOnStart;
    public boolean autoTrim;
    public boolean uiExtras;
    public boolean wakeScreen;
    public boolean nightMode;
    public boolean militaryTime;
    public boolean syncMobile;
    public boolean useEmoji;
    public boolean inlinePics;
    public boolean extraPages;
    public boolean fullScreenBrowser;
    public boolean favoriteUserNotifications;
    public boolean syncSecondMentions;
    public boolean displayScreenName;
    public boolean liveStreaming;
    public boolean pushNotifications;
    public boolean inAppBrowser;
    public boolean showBoth;
    public boolean preferRT;
    public boolean absoluteDate;
    public boolean useToast;
    public boolean autoInsertHashtags;
    public boolean alwaysCompose;
    public boolean twitlonger;
    public boolean twitpic;
    public boolean tweetmarker;
    public boolean tweetmarkerManualOnly;
    public boolean jumpingWorkaround;
    public boolean floatingCompose;
    public boolean openKeyboard;
    public boolean alwaysMobilize;
    public boolean mobilizeOnData;
    public boolean preCacheImages;
    public boolean crossAccActions;
    public boolean useInteractionDrawer;

    // notifications
    public boolean timelineNot;
    public boolean mentionsNot;
    public boolean dmsNot;
    public boolean followersNot;
    public boolean favoritesNot;
    public boolean retweetNot;
    public boolean activityNot;
    public String ringtone;

    // theme stuff
    public boolean addonTheme;
    public String addonThemePackage;
    public boolean roundContactImages;
    public int backgroundColor;
    public boolean translateProfileHeader;
    public boolean nameAndHandleOnTweet = false;
    public boolean combineProPicAndImage = false;
    public boolean sendToComposeWindow = false;
    public boolean showTitleStrip = true;
    public String accentColor;
    public int accentInt;
    public int pagerTitleInt;
    public Drawable actionBar = null;
    public Drawable customBackground = null;

    public int theme;
    public int layout;
    public int currentAccount;
    public int textSize;
    public int maxTweetsRefresh;
    public int timelineSize;
    public int mentionsSize;
    public int dmSize;
    public int pageToOpen;
    public int numberOfAccounts;

    public long timelineRefresh;
    public long mentionsRefresh;
    public long dmRefresh;
    public long activityRefresh;
    public long myId;


    public AppSettings(Context context) {

        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        // Strings
        if (sharedPrefs.getInt("current_account", 1) == 1) {
            authenticationToken = sharedPrefs.getString("authentication_token_1", "none");
            authenticationTokenSecret = sharedPrefs.getString("authentication_token_secret_1", "none");
            secondAuthToken = sharedPrefs.getString("authentication_token_2", "none");
            secondAuthTokenSecret = sharedPrefs.getString("authentication_token_secret_2", "none");
            myScreenName = sharedPrefs.getString("twitter_screen_name_1", "");
            secondScreenName = sharedPrefs.getString("twitter_screen_name_2", "");
            myName = sharedPrefs.getString("twitter_users_name_1", "");
            myBackgroundUrl = sharedPrefs.getString("twitter_background_url_1", "");
            myProfilePicUrl = sharedPrefs.getString("profile_pic_url_1", "");
            secondProfilePicUrl = sharedPrefs.getString("profile_pic_url_2", "");
            favoriteUserNames = sharedPrefs.getString("favorite_user_names_1", "");
            myId = sharedPrefs.getLong("twitter_id_1", 0);
        } else {
            authenticationToken = sharedPrefs.getString("authentication_token_2", "none");
            authenticationTokenSecret = sharedPrefs.getString("authentication_token_secret_2", "none");
            secondAuthToken = sharedPrefs.getString("authentication_token_1", "none");
            secondAuthTokenSecret = sharedPrefs.getString("authentication_token_secret_1", "none");
            myScreenName = sharedPrefs.getString("twitter_screen_name_2", "");
            secondScreenName = sharedPrefs.getString("twitter_screen_name_1", "");
            myName = sharedPrefs.getString("twitter_users_name_2", "");
            myBackgroundUrl = sharedPrefs.getString("twitter_background_url_2", "");
            myProfilePicUrl = sharedPrefs.getString("profile_pic_url_2", "");
            secondProfilePicUrl = sharedPrefs.getString("profile_pic_url_1", "");
            favoriteUserNames = sharedPrefs.getString("favorite_user_names_2", "");
            myId = sharedPrefs.getLong("twitter_id_2", 0);
        }

        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Booleans
        isTwitterLoggedIn = sharedPrefs.getBoolean("is_logged_in_1", false) || sharedPrefs.getBoolean("is_logged_in_2", false) ||
                defaultPrefs.getBoolean("is_logged_in_1", false) || defaultPrefs.getBoolean("is_logged_in_2", false);
        reverseClickActions = sharedPrefs.getBoolean("reverse_click_option", true);
        advanceWindowed = sharedPrefs.getBoolean("advance_windowed", true);
        notifications = sharedPrefs.getBoolean("notifications", true);
        led = sharedPrefs.getBoolean("led", true);
        sound = sharedPrefs.getBoolean("sound", true);
        vibrate = sharedPrefs.getBoolean("vibrate", true);
        refreshOnStart = sharedPrefs.getBoolean("refresh_on_start", false);
        autoTrim = sharedPrefs.getBoolean("auto_trim", true);
        uiExtras = sharedPrefs.getBoolean("ui_extras", true);
        wakeScreen = sharedPrefs.getBoolean("wake", true);
        militaryTime = sharedPrefs.getBoolean("military_time", false);
        syncMobile = sharedPrefs.getBoolean("sync_mobile_data", true);
        inlinePics = sharedPrefs.getBoolean("inline_pics", true);
        extraPages = sharedPrefs.getBoolean("extra_pages", true);
        fullScreenBrowser = sharedPrefs.getBoolean("full_screen_browser", true);
        favoriteUserNotifications = sharedPrefs.getBoolean("favorite_users_notifications", true);
        syncSecondMentions = sharedPrefs.getBoolean("sync_second_mentions", true);
        displayScreenName = sharedPrefs.getBoolean("display_screen_name", false);
        liveStreaming = sharedPrefs.getBoolean("live_streaming", true);
        pushNotifications = sharedPrefs.getBoolean("push_notifications", true);
        inAppBrowser = sharedPrefs.getBoolean("inapp_browser", true);
        showBoth = sharedPrefs.getBoolean("both_handle_name", false);
        timelineNot = sharedPrefs.getBoolean("timeline_notifications", true);
        mentionsNot = sharedPrefs.getBoolean("mentions_notifications", true);
        dmsNot = sharedPrefs.getBoolean("direct_message_notifications", true);
        favoritesNot = sharedPrefs.getBoolean("favorite_notifications", true);
        retweetNot = sharedPrefs.getBoolean("retweet_notifications", true);
        followersNot = sharedPrefs.getBoolean("follower_notifications", true);
        preferRT = sharedPrefs.getBoolean("prefer_rt", false);
        absoluteDate = sharedPrefs.getBoolean("absolute_date", false);
        useToast = sharedPrefs.getBoolean("use_toast", true);
        autoInsertHashtags = sharedPrefs.getBoolean("auto_insert_hashtags", false);
        alwaysCompose = sharedPrefs.getBoolean("always_compose", false);
        twitlonger = sharedPrefs.getBoolean("twitlonger", true);
        twitpic = sharedPrefs.getBoolean("twitpic", false);
        jumpingWorkaround = sharedPrefs.getBoolean("jumping_workaround", false);
        floatingCompose = sharedPrefs.getBoolean("floating_compose", true);
        openKeyboard = sharedPrefs.getBoolean("open_keyboard", false);
        preCacheImages = sharedPrefs.getBoolean("pre_cache_images", false);
        crossAccActions = sharedPrefs.getBoolean("fav_rt_multiple_accounts", true);
        activityNot = sharedPrefs.getBoolean("activity_notifications", true);
        useInteractionDrawer = sharedPrefs.getBoolean("interaction_drawer", true);

        // set up tweetmarker
        String val = sharedPrefs.getString("tweetmarker_options", "0");
        if (val.equals("0")) {
            tweetmarker = false;
            tweetmarkerManualOnly = false;
        } else if (val.equals("1")) {
            tweetmarker = true;
            tweetmarkerManualOnly = false;
        } else {
            tweetmarkerManualOnly = true;
            tweetmarker = true;
        }

        // set up the mobilized (plain text) browser
        alwaysMobilize = sharedPrefs.getBoolean("mobilized_browser", false) &&
                !sharedPrefs.getBoolean("mobilize_on_data_only", true);
        mobilizeOnData = sharedPrefs.getBoolean("mobilized_browser", false) &&
                sharedPrefs.getBoolean("mobilize_on_data_only", true);

        if (!uiExtras) {
            floatingCompose = false;
        }

        ringtone = defaultPrefs.getString("ringtone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());

        if (!pushNotifications) {
            liveStreaming = false;
            useInteractionDrawer = false;
        }

        if (liveStreaming) {
            refreshOnStart = false;
        }

        // if they have the keyboard trial installed, then go from their preference
        if (EmojiUtils.checkEmojisEnabled(context)) {
            useEmoji = sharedPrefs.getBoolean("use_emojis", false);
        } else { // otherwise it is false
            useEmoji = false;
        }

        // Integers
        theme = Integer.parseInt(sharedPrefs.getString("theme", "1")); // default is dark
        layout = Integer.parseInt(sharedPrefs.getString("layout", "0")); // default is talon
        currentAccount = sharedPrefs.getInt("current_account", 1);
        textSize = Integer.parseInt(sharedPrefs.getString("text_size", "14"));
        maxTweetsRefresh = Integer.parseInt(sharedPrefs.getString("max_tweets", "1"));
        timelineSize = Integer.parseInt(sharedPrefs.getString("timeline_size", "500"));
        mentionsSize = Integer.parseInt(sharedPrefs.getString("mentions_size", "100"));
        dmSize = Integer.parseInt(sharedPrefs.getString("dm_size", "100"));
        pageToOpen = Integer.parseInt(sharedPrefs.getString("viewer_page", "0"));

        // Longs
        timelineRefresh = Long.parseLong(sharedPrefs.getString("timeline_sync_interval", "0"));
        mentionsRefresh = Long.parseLong(sharedPrefs.getString("mentions_sync_interval", "0"));
        dmRefresh = Long.parseLong(sharedPrefs.getString("dm_sync_interval", "0"));
        activityRefresh = Long.parseLong(sharedPrefs.getString("activity_sync_interval", "0"));

        if (sharedPrefs.getBoolean("night_mode", false)) {
            int nightStartHour = sharedPrefs.getInt("night_start_hour", 22);
            int nightStartMin = sharedPrefs.getInt("night_start_min", 0);
            int dayStartHour = sharedPrefs.getInt("day_start_hour", 6);
            int dayStartMin = sharedPrefs.getInt("day_start_min", 0);

            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minutes = c.get(Calendar.MINUTE);

            int dayStartMinutes = dayStartHour * 60 + dayStartMin;
            int nightStartMinutes = nightStartHour * 60 + nightStartMin;
            int currentMinutes = hour * 60 + minutes;

            if ((nightStartHour > dayStartHour && !(currentMinutes > dayStartMinutes && nightStartMinutes > currentMinutes)) ||
                    (nightStartHour < dayStartHour && (currentMinutes < dayStartMinutes && nightStartMinutes < currentMinutes))) {
                nightMode = true;
                theme = sharedPrefs.getInt("night_theme", 1);
            }
        }

        if (sharedPrefs.getBoolean("quiet_hours", false)) {
            int quietStartHour = sharedPrefs.getInt("quiet_start_hour", 22);
            int quietStartMin = sharedPrefs.getInt("quiet_start_min", 0);
            int quietEndHour = sharedPrefs.getInt("quiet_end_hour", 6);
            int quietEndMin = sharedPrefs.getInt("quiet_end_min", 0);

            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minutes = c.get(Calendar.MINUTE);

            int quietEndMinutes = quietEndHour * 60 + quietEndMin;
            int quietStartMinutes = quietStartHour * 60 + quietStartMin;
            int currentMinutes = hour * 60 + minutes;

            if (!(currentMinutes > quietEndMinutes && quietStartMinutes > currentMinutes)) {
                notifications = false;
                //Log.v("quiet_hours", "quiet hours set now");
            }
        }

        // theme stuff
        if (layout == LAYOUT_TALON) {
            roundContactImages = true;
        } else {
            roundContactImages = false;
        }

        if (sharedPrefs.getBoolean("addon_themes", false)) {
            addonTheme = true;
            addonThemePackage = sharedPrefs.getString("addon_theme_package", null);

            try {
                Bundle metaData = context.getPackageManager().getApplicationInfo(addonThemePackage, PackageManager.GET_META_DATA).metaData;

                roundContactImages = metaData.getBoolean("talon_theme_round_contact_pictures");
                translateProfileHeader = metaData.getBoolean("talon_theme_contracting_user_backgrounds");
                backgroundColor = Color.parseColor(metaData.getString("talon_theme_background_color"));
                try {
                    accentColor = metaData.getString("talon_theme_accent_color");
                    accentInt = Color.parseColor(accentColor);
                    accentColor = accentColor.replace("#", "");
                } catch (Exception e) {
                    // no accent color attribute
                    accentColor = "#FF8800";
                    accentInt = Color.parseColor(accentColor);
                    accentColor = accentColor.replace("#", "");
                }

                try {
                    nameAndHandleOnTweet = metaData.getBoolean("force_name_and_handle_on_tweet");
                } catch (Exception e) {
                    nameAndHandleOnTweet = false;
                }

                try {
                    combineProPicAndImage = metaData.getBoolean("tweet_pager_combine_pro_pic_and_image");
                } catch (Exception e) {
                    combineProPicAndImage = false;
                }

                try {
                    sendToComposeWindow = metaData.getBoolean("tweet_pager_send_to_compose_window");
                } catch (Exception e) {
                    sendToComposeWindow = false;
                }

                try {
                    pagerTitleInt = Color.parseColor(metaData.getString("pager_title_strip_color"));
                } catch (Exception e) {
                    pagerTitleInt = accentInt;
                }

                try {
                    showTitleStrip = metaData.getBoolean("display_tweet_or_profile_title_strip");
                } catch (Exception e) {
                    showTitleStrip = true;
                }

                Log.v("color_for_theme", accentColor);

                String theme = metaData.getString("talon_theme_base");
                if (theme.equals("dark")) {
                    this.theme = THEME_DARK;
                } else if (theme.equals("black")) {
                    this.theme = THEME_BLACK;
                } else {
                    this.theme = THEME_LIGHT;
                }

                Resources res = context.getPackageManager().getResourcesForApplication(addonThemePackage);
                try {
                    actionBar = res.getDrawable(res.getIdentifier("ab_background", "drawable", addonThemePackage));
                } catch (Exception e) {
                    actionBar = null;
                }
                try {
                    customBackground = res.getDrawable(res.getIdentifier("wallpaper", "drawable", addonThemePackage));
                } catch (Exception e) {
                    customBackground = null;
                } catch (OutOfMemoryError e) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }

            } catch (Exception e) {
                e.printStackTrace();
                sharedPrefs.edit().putBoolean("addon_themes", false).putString("addon_theme_package", null).commit();
                actionBar = null;
            }
        } else {
            addonTheme = false;
            addonThemePackage = null;
            translateProfileHeader = true;
        }

        if (sharedPrefs.getBoolean("is_logged_in_1", false)) {
            numberOfAccounts++;
        }
        if (sharedPrefs.getBoolean("is_logged_in_2", false)) {
            numberOfAccounts++;
        }

        if(numberOfAccounts != 2) {
            syncSecondMentions = false;
            crossAccActions = false;
        }
    }

    public AppSettings(SharedPreferences sharedPrefs, Context context) {

        // Strings
        if (sharedPrefs.getInt("current_account", 1) == 1) {
            authenticationToken = sharedPrefs.getString("authentication_token_1", "none");
            authenticationTokenSecret = sharedPrefs.getString("authentication_token_secret_1", "none");
            secondAuthToken = sharedPrefs.getString("authentication_token_2", "none");
            secondAuthTokenSecret = sharedPrefs.getString("authentication_token_secret_2", "none");
            myScreenName = sharedPrefs.getString("twitter_screen_name_1", "");
            secondScreenName = sharedPrefs.getString("twitter_screen_name_2", "");
            myName = sharedPrefs.getString("twitter_users_name_1", "");
            myBackgroundUrl = sharedPrefs.getString("twitter_background_url_1", "");
            myProfilePicUrl = sharedPrefs.getString("profile_pic_url_1", "");
            favoriteUserNames = sharedPrefs.getString("favorite_user_names_1", "");
            myId = sharedPrefs.getLong("twitter_id_1", 0);

            Log.v("talon_username", myScreenName);
        } else {
            authenticationToken = sharedPrefs.getString("authentication_token_2", "none");
            authenticationTokenSecret = sharedPrefs.getString("authentication_token_secret_2", "none");
            secondAuthToken = sharedPrefs.getString("authentication_token_1", "none");
            secondAuthTokenSecret = sharedPrefs.getString("authentication_token_secret_1", "none");
            myScreenName = sharedPrefs.getString("twitter_screen_name_2", "");
            secondScreenName = sharedPrefs.getString("twitter_screen_name_1", "");
            myName = sharedPrefs.getString("twitter_users_name_2", "");
            myBackgroundUrl = sharedPrefs.getString("twitter_background_url_2", "");
            myProfilePicUrl = sharedPrefs.getString("profile_pic_url_2", "");
            favoriteUserNames = sharedPrefs.getString("favorite_user_names_2", "");
            myId = sharedPrefs.getLong("twitter_id_2", 0);
        }

        // Booleans
        isTwitterLoggedIn = sharedPrefs.getBoolean("is_logged_in_1", false) || sharedPrefs.getBoolean("is_logged_in_2", false);
        reverseClickActions = sharedPrefs.getBoolean("reverse_click_option", true);
        advanceWindowed = sharedPrefs.getBoolean("advance_windowed", true);
        notifications = sharedPrefs.getBoolean("notifications", true);
        led = sharedPrefs.getBoolean("led", true);
        sound = sharedPrefs.getBoolean("sound", true);
        vibrate = sharedPrefs.getBoolean("vibrate", true);
        refreshOnStart = sharedPrefs.getBoolean("refresh_on_start", false);
        autoTrim = sharedPrefs.getBoolean("auto_trim", true);
        uiExtras = sharedPrefs.getBoolean("ui_extras", true);
        wakeScreen = sharedPrefs.getBoolean("wake", true);
        militaryTime = sharedPrefs.getBoolean("military_time", false);
        syncMobile = sharedPrefs.getBoolean("sync_mobile_data", true);
        inlinePics = sharedPrefs.getBoolean("inline_pics", true);
        extraPages = sharedPrefs.getBoolean("extra_pages", true);
        fullScreenBrowser = sharedPrefs.getBoolean("full_screen_browser", true);
        favoriteUserNotifications = sharedPrefs.getBoolean("favorite_users_notifications", true);
        syncSecondMentions = sharedPrefs.getBoolean("sync_second_mentions", true);
        displayScreenName = sharedPrefs.getBoolean("display_screen_name", false);
        liveStreaming = sharedPrefs.getBoolean("live_streaming", true);
        pushNotifications = sharedPrefs.getBoolean("push_notifications", true);
        inAppBrowser = sharedPrefs.getBoolean("inapp_browser", true);
        showBoth = sharedPrefs.getBoolean("both_handle_name", false);
        timelineNot = sharedPrefs.getBoolean("timeline_notifications", true);
        mentionsNot = sharedPrefs.getBoolean("mentions_notifications", true);
        dmsNot = sharedPrefs.getBoolean("direct_message_notifications", true);
        favoritesNot = sharedPrefs.getBoolean("favorite_notifications", true);
        retweetNot = sharedPrefs.getBoolean("retweet_notifications", true);
        followersNot = sharedPrefs.getBoolean("follower_notifications", true);
        preferRT = sharedPrefs.getBoolean("prefer_rt", false);
        absoluteDate = sharedPrefs.getBoolean("absolute_date", false);
        useToast = sharedPrefs.getBoolean("use_toast", true);
        autoInsertHashtags = sharedPrefs.getBoolean("auto_insert_hashtags", false);
        alwaysCompose = sharedPrefs.getBoolean("always_compose", false);
        twitlonger = sharedPrefs.getBoolean("twitlonger", true);
        twitpic = sharedPrefs.getBoolean("twitpic", false);
        jumpingWorkaround = sharedPrefs.getBoolean("jumping_workaround", false);
        floatingCompose = sharedPrefs.getBoolean("floating_compose", true);
        openKeyboard = sharedPrefs.getBoolean("open_keyboard", false);
        preCacheImages = sharedPrefs.getBoolean("pre_cache_images", false);

        // set up tweetmarker
        String val = sharedPrefs.getString("tweetmarker_options", "0");
        if (val.equals("0")) {
            tweetmarker = false;
            tweetmarkerManualOnly = false;
        } else if (val.equals("1")) {
            tweetmarker = true;
            tweetmarkerManualOnly = false;
        } else {
            tweetmarkerManualOnly = true;
            tweetmarker = true;
        }

        // set up the mobilized (plain text) browser
        alwaysMobilize = sharedPrefs.getBoolean("mobilized_browser", false) &&
                !sharedPrefs.getBoolean("mobilize_on_data_only", true);
        mobilizeOnData = sharedPrefs.getBoolean("mobilized_browser", false) &&
                sharedPrefs.getBoolean("mobilize_on_data_only", true);

        if (!uiExtras) {
            floatingCompose = false;
        }

        ringtone = sharedPrefs.getString("ringtone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());

        if (!pushNotifications) {
            liveStreaming = false;
        }

        if (liveStreaming) {
            refreshOnStart = false;
        }

        // if they have the keyboard trial installed, then go from their preference
        if (EmojiUtils.checkEmojisEnabled(context)) {
            useEmoji = sharedPrefs.getBoolean("use_emojis", false);
        } else { // otherwise it is false
            useEmoji = false;
        }

        // Integers
        theme = Integer.parseInt(sharedPrefs.getString("theme", "1")); // default is dark
        layout = Integer.parseInt(sharedPrefs.getString("layout", "0")); // default is talon
        currentAccount = sharedPrefs.getInt("current_account", 1);
        textSize = Integer.parseInt(sharedPrefs.getString("text_size", "14"));
        maxTweetsRefresh = Integer.parseInt(sharedPrefs.getString("max_tweets", "1"));
        timelineSize = Integer.parseInt(sharedPrefs.getString("timeline_size", "500"));
        mentionsSize = Integer.parseInt(sharedPrefs.getString("mentions_size", "100"));
        dmSize = Integer.parseInt(sharedPrefs.getString("dm_size", "100"));
        pageToOpen = Integer.parseInt(sharedPrefs.getString("viewer_page", "0"));


        // Longs
        timelineRefresh = Long.parseLong(sharedPrefs.getString("timeline_sync_interval", "0"));
        mentionsRefresh = Long.parseLong(sharedPrefs.getString("mentions_sync_interval", "0"));
        dmRefresh = Long.parseLong(sharedPrefs.getString("dm_sync_interval", "0"));

        if (sharedPrefs.getBoolean("night_mode", false)) {
            int nightStartHour = sharedPrefs.getInt("night_start_hour", 22);
            int nightStartMin = sharedPrefs.getInt("night_start_min", 0);
            int dayStartHour = sharedPrefs.getInt("day_start_hour", 6);
            int dayStartMin = sharedPrefs.getInt("day_start_min", 0);

            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minutes = c.get(Calendar.MINUTE);

            int dayStartMinutes = dayStartHour * 60 + dayStartMin;
            int nightStartMinutes = nightStartHour * 60 + nightStartMin;
            int currentMinutes = hour * 60 + minutes;

            if ((nightStartHour > dayStartHour && !(currentMinutes > dayStartMinutes && nightStartMinutes > currentMinutes)) ||
                    (nightStartHour < dayStartHour && (currentMinutes < dayStartMinutes && nightStartMinutes < currentMinutes))) {
                nightMode = true;
                theme = sharedPrefs.getInt("night_theme", 1);
            }
        }

        if (sharedPrefs.getBoolean("quiet_hours", false)) {
            int quietStartHour = sharedPrefs.getInt("quiet_start_hour", 22);
            int quietStartMin = sharedPrefs.getInt("quiet_start_min", 0);
            int quietEndHour = sharedPrefs.getInt("quiet_end_hour", 6);
            int quietEndMin = sharedPrefs.getInt("quiet_end_min", 0);

            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minutes = c.get(Calendar.MINUTE);

            int quietEndMinutes = quietEndHour * 60 + quietEndMin;
            int quietStartMinutes = quietStartHour * 60 + quietStartMin;
            int currentMinutes = hour * 60 + minutes;

            if (!(currentMinutes > quietEndMinutes && quietStartMinutes > currentMinutes)) {
                notifications = false;
                //Log.v("quiet_hours", "quiet hours set now");
            }
        }

        // theme stuff
        if (layout == LAYOUT_TALON) {
            roundContactImages = true;
        } else {
            roundContactImages = false;
        }

        if (sharedPrefs.getBoolean("addon_themes", false)) {
            addonTheme = true;
            addonThemePackage = sharedPrefs.getString("addon_theme_package", null);

            try {
                Bundle metaData = context.getPackageManager().getApplicationInfo(addonThemePackage, PackageManager.GET_META_DATA).metaData;

                roundContactImages = metaData.getBoolean("talon_theme_round_contact_pictures");
                translateProfileHeader = metaData.getBoolean("talon_theme_contracting_user_backgrounds");
                backgroundColor = Color.parseColor(metaData.getString("talon_theme_background_color"));
                try {
                    accentColor = metaData.getString("talon_theme_accent_color");
                    accentInt = Color.parseColor(accentColor);
                    accentColor = accentColor.replace("#", "");
                } catch (Exception e) {
                    // no accent color attribute
                    accentColor = "#FF8800";
                    accentInt = Color.parseColor(accentColor);
                    accentColor = accentColor.replace("#", "");
                }

                Log.v("color_for_theme", accentColor);

                String theme = metaData.getString("talon_theme_base");
                if (theme.equals("dark")) {
                    this.theme = THEME_DARK;
                } else if (theme.equals("black")) {
                    this.theme = THEME_BLACK;
                } else {
                    this.theme = THEME_LIGHT;
                }

                Resources res = context.getPackageManager().getResourcesForApplication(addonThemePackage);
                try {
                    actionBar = res.getDrawable(res.getIdentifier("ab_background", "drawable", addonThemePackage));
                } catch (Exception e) {
                    actionBar = null;
                }
                try {
                    customBackground = res.getDrawable(res.getIdentifier("wallpaper", "drawable", addonThemePackage));
                } catch (Exception e) {
                    customBackground = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
                sharedPrefs.edit().putBoolean("addon_themes", false).putString("addon_theme_package", null).commit();
                actionBar = null;
            }
        } else {
            addonTheme = false;
            addonThemePackage = null;
            translateProfileHeader = true;
        }

        int count = 0;
        if (sharedPrefs.getBoolean("is_logged_in_1", false)) {
            count++;
        }
        if (sharedPrefs.getBoolean("is_logged_in_2", false)) {
            count++;
        }

        if(count != 2) {
            syncSecondMentions = false;
        }
    }

    public static int getCurrentTheme(SharedPreferences sharedPrefs) {
        int theme = Integer.parseInt(sharedPrefs.getString("theme", "1")); // default is dark
        if (sharedPrefs.getBoolean("night_mode", false)) {
            int nightStartHour = sharedPrefs.getInt("night_start_hour", 22);
            int nightStartMin = sharedPrefs.getInt("night_start_min", 0);
            int dayStartHour = sharedPrefs.getInt("day_start_hour", 6);
            int dayStartMin = sharedPrefs.getInt("day_start_min", 0);

            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minutes = c.get(Calendar.MINUTE);

            int dayStartMinutes = dayStartHour * 60 + dayStartMin;
            int nightStartMinutes = nightStartHour * 60 + nightStartMin;
            int currentMinutes = hour * 60 + minutes;

            if ((nightStartHour > dayStartHour && !(currentMinutes > dayStartMinutes && nightStartMinutes > currentMinutes)) ||
                    (nightStartHour < dayStartHour && (currentMinutes < dayStartMinutes && nightStartMinutes < currentMinutes))) {
                theme = sharedPrefs.getInt("night_theme", 1);
            }
        }

        return theme;
    }
}
