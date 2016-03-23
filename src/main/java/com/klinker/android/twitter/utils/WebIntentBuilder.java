package com.klinker.android.twitter.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
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
    private static final String PERISCOPE = "periscope.tv";
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

    private SimpleCustomChromeTabsHelper customTabHelper;
    private SimpleCustomChromeTabsHelper.CustomTabsUiBuilder uiBuilder;

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
            customTabHelper = new SimpleCustomChromeTabsHelper((Activity) context);
            customTabHelper.prepareUrl(webpage);
            customTabHelper.setFallback(new SimpleCustomChromeTabsHelper.CustomTabFallback() {
                @Override
                public void onCustomTabsNotAvailableFallback() {
                    intent = new Intent(context, BrowserActivity.class);
                    intent.putExtra("url", webpage);
                    intent.setFlags(0);
                    context.startActivity(intent);
                }
            });

            int color = context.getResources().getColor(R.color.action_bar_light);
            int bitmapRes = R.drawable.ic_action_share_light;
            switch (settings.theme) {
                case AppSettings.THEME_BLACK:
                    color = context.getResources().getColor(R.color.action_bar_black);
                    bitmapRes = R.drawable.ic_action_share_dark;
                    break;
                case AppSettings.THEME_DARK:
                    color = context.getResources().getColor(R.color.action_bar_dark);
                    bitmapRes = R.drawable.ic_action_share_dark;
                    break;
            }

            // add the share action
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, webpage);
            shareIntent.setType("text/plain");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, Intent.createChooser(shareIntent, "Share to:"), 0);

            uiBuilder = new SimpleCustomChromeTabsHelper.CustomTabsUiBuilder();
            uiBuilder.setToolbarColor(color);
            uiBuilder.setActionButton(((BitmapDrawable)context.getResources().getDrawable(bitmapRes)).getBitmap(), "Share link", pendingIntent);
        } else {
            // fallback to in app browser
            intent = new Intent(context, PlainTextBrowserActivity.class);
            intent.putExtra("url", webpage);
            intent.setFlags(0);
        }

        return this;
    }

    public void start() {
        if (customTabHelper != null) {
            customTabHelper.openUrl(webpage, uiBuilder);
        } else {
            context.startActivity(intent);
        }
    }

    private boolean shouldAlwaysForceExternal(String url) {
        for (String s : ALWAYS_EXTERNAL)
            if (url.contains(s))
                return true;

        return false;
    }

}
