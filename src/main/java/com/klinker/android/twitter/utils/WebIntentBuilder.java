package com.klinker.android.twitter.utils;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.BrowserActivity;
import com.klinker.android.twitter.ui.PlainTextBrowserActivity;
import com.klinker.android.twitter.utils.Utils;

import java.lang.reflect.Method;

/**
 * Used to handle URLs.
 *
 * Will redirect some to the external browser automatically (play store, youtube, twitter, periscope, meerkat)
 * Others will attempt to load a custom tab
 * If the tab fails, then it will open up the in app browser intent.
 *
 * This does not do anything with warming up the custom intent or starting a session.
 * It will simply display the page.
 */
public class WebIntentBuilder {

    private static final String PLAY_STORE = "play.google.com";
    private static final String YOUTUBE = "youtu";
    private static final String TWITTER = "twitter.com";
    private static final String PERISCOPE = "periscope";
    private static final String MEERKAT = "mkr.tv";

    private static final String[] ALWAYS_EXTERNAL = new String[] {
            PLAY_STORE,
            YOUTUBE,
            TWITTER,
            PERISCOPE,
            MEERKAT
    };

    private Context context;
    private AppSettings settings;
    private boolean mobilizedBrowser;

    private Intent intent;
    private String webpage;
    private boolean forceExternal;

    public WebIntentBuilder(Context context) {
        this.context = context;
        this.settings = AppSettings.getInstance(context);
        this.mobilizedBrowser = settings.alwaysMobilize ||
                (settings.mobilizeOnData && Utils.getConnectionStatus(context));
    }

    public WebIntentBuilder setUrl(String url) {
        this.webpage = url;
        return this;
    }

    public WebIntentBuilder setShouldForceExternal(boolean forceExternal) {
        this.forceExternal = forceExternal;
        return this;
    }

    public WebIntentBuilder build() {
        if (webpage == null) {
            throw new RuntimeException("URL cannot be null.");
        }

        if (forceExternal || !settings.inAppBrowser || shouldAlwaysForceExternal(webpage)) {
            // request the external browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webpage));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (!mobilizedBrowser) {
            intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName(CHROME_PACKAGE, "org.chromium.chrome.browser.customtabs.CustomTabActivity"));
            intent.setData(Uri.parse(webpage));

            // request a chrome custom tab
            Bundle extras = new Bundle();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                extras.putBinder(EXTRA_CUSTOM_TABS_SESSION, null);
            } else {
                try {
                    Method putBinderMethod =
                            Bundle.class.getMethod("putIBinder", String.class, IBinder.class);
                    putBinderMethod.invoke(extras, EXTRA_CUSTOM_TABS_SESSION, null);
                } catch (Exception e) {

                }
            }

            intent.putExtras(extras);

            int color = context.getResources().getColor(R.color.action_bar_light);
            switch (settings.theme) {
                case AppSettings.THEME_BLACK:
                    color = context.getResources().getColor(R.color.action_bar_black);
                    break;
                case AppSettings.THEME_DARK:
                    color = context.getResources().getColor(R.color.action_bar_dark);
                    break;
            }

            intent.putExtra(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, color);
        } else {
            // fallback to in app browser
            intent = new Intent(context, mobilizedBrowser ?
                    PlainTextBrowserActivity.class : BrowserActivity.class);
            intent.putExtra("url", webpage);
            intent.setFlags(0);
        }

        return this;
    }

    public void start() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.getBoolean("shown_disclaimer_for_custom_tabs_2", false)) {
            startActivity();
        } else {
            sharedPreferences.edit().putBoolean("shown_disclaimer_for_custom_tabs_2", true).commit();
            new AlertDialog.Builder(context)
                    .setTitle(R.string.custom_tab_title)
                    .setMessage(R.string.custom_tab_message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity();
                        }
                    })
                    .setNegativeButton(R.string.learn_more, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://android-developers.blogspot.com/2015/09/chrome-custom-tabs-smooth-transition.html"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    })
                    .create().show();
        }
    }

    private void startActivity() {
        if (isChromeInstalled()) {
            context.startActivity(intent);
        } else {
            intent = new Intent(context, mobilizedBrowser ?
                    PlainTextBrowserActivity.class : BrowserActivity.class);
            intent.putExtra("url", webpage);
            intent.setFlags(0);

            context.startActivity(intent);
        }
    }

    final String CHROME_PREF = "is_chrome_installed";
    private boolean isChromeInstalled() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.contains(CHROME_PREF)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isPackageInstalled(sharedPreferences);
                }
            }).start();

            return sharedPreferences.getBoolean(CHROME_PREF, true);
        } else {
            return isPackageInstalled(sharedPreferences);
        }
    }

    private boolean isPackageInstalled(SharedPreferences sharedPreferences) {
        PackageManager pm = context.getPackageManager();

        boolean installed = true;
        try {
            pm.getPackageInfo(CHROME_PACKAGE,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }

        sharedPreferences.edit().putBoolean(CHROME_PREF, installed).commit();
        return installed;
    }

    private boolean shouldAlwaysForceExternal(String url) {
        for (String s : ALWAYS_EXTERNAL)
            if (url.contains(s))
                return true;

        return false;
    }

    /**
     * Chrome Custom Tab Extras
     */

    private static final String CHROME_PACKAGE = "com.android.chrome";

    // REQUIRED. Must use an extra bundle with this. Even if the contense is null.
    private static final String EXTRA_CUSTOM_TABS_SESSION = "android.support.customtabs.extra.SESSION";

    // Optional. specify an integer color
    private static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";

    // Optional. Key that specifies the PendingIntent to launch when the action button
    // or menu item was tapped. Chrome will be calling PendingIntent#send() on
    // taps after adding the url as data. The client app can call Intent#getDataString() to get the url.
    public static final String KEY_CUSTOM_TABS_PENDING_INTENT = "android.support.customtabs.customaction.PENDING_INTENT";

    // Optional. Use a bundle for parameters if an the action button is specified.
    public static final String EXTRA_CUSTOM_TABS_ACTION_BUTTON_BUNDLE = "android.support.customtabs.extra.ACTION_BUNDLE_BUTTON";

}
