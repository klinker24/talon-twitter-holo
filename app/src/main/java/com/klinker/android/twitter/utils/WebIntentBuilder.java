package com.klinker.android.twitter.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

import com.klinker.android.twitter.APIKeys;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.activities.BrowserActivity;

import java.util.Random;

import xyz.klinker.android.article.ArticleIntent;

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

    private String webpage;
    private boolean forceExternal;

    private Intent intent;
    private CustomTabsIntent customTab;
    private ArticleIntent articleIntent;

    public WebIntentBuilder(Context context) {
        this.context = context;
        this.settings = AppSettings.getInstance(context);
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

        if (forceExternal || shouldAlwaysForceExternal(webpage) || settings.browserSelection.equals("external")) {
            // request the external browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webpage));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (settings.browserSelection.equals("article")) {
            articleIntent = new ArticleIntent.Builder(context, APIKeys.ARTICLE_API_KEY)
                    .build();
        } else if (settings.browserSelection.equals("custom_tab")) {
            // add the share action
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            String extraText = webpage;
            shareIntent.putExtra(Intent.EXTRA_TEXT, extraText);
            shareIntent.setType("text/plain");
            Random random = new Random();
            PendingIntent pendingIntent = PendingIntent.getActivity(context, random.nextInt(Integer.MAX_VALUE), shareIntent, 0);

            customTab = new CustomTabsIntent.Builder(null)
                    .setShowTitle(true)
                    .setActionButton(((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_action_share_light)).getBitmap(), "Share", pendingIntent)
                    .build();
        } else {
            // fallback to in app browser
            intent = new Intent(context, BrowserActivity.class);
            intent.putExtra("url", webpage);
            intent.setFlags(0);
        }

        return this;
    }

    public void start() {
        if (customTab != null) {
            customTab.launchUrl(context, Uri.parse(webpage));
        } else if (articleIntent != null) {
            articleIntent.launchUrl(context, Uri.parse(webpage));
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
