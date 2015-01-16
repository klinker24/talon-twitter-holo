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

package com.klinker.android.twitter.ui.tweet_viewer;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Patterns;
import android.view.*;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.TweetPagerAdapter;
import com.klinker.android.twitter.data.sq_lite.HashtagDataSource;
import com.klinker.android.twitter.data.sq_lite.HomeDataSource;
import com.klinker.android.twitter.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter.manipulations.widgets.ActionBarDrawerToggle;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.BrowserActivity;
import com.klinker.android.twitter.ui.compose.ComposeActivity;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.TweetYouTubeFragment;
import com.klinker.android.twitter.utils.IOUtils;
import com.klinker.android.twitter.utils.Utils;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

import twitter4j.Twitter;

public class TweetPager extends YouTubeBaseActivity {

    private TweetPagerAdapter mSectionsPagerAdapter;
    private ViewPager pager;
    public Context context;
    public AppSettings settings;

    public String name;
    public String screenName;
    public String tweet;
    public long time;
    public String retweeter;
    public String webpage;
    public String proPic;
    public String animatedGif;
    public boolean picture;
    public long tweetId;
    public String[] users;
    public String[] hashtags;
    public String[] otherLinks;
    public String linkString;
    public boolean isMyTweet = false;
    public boolean isMyRetweet = true;
    public boolean secondAcc = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);

        try {
            getWindow().requestFeature(Window.FEATURE_PROGRESS);
        } catch (Exception e) {

        }

        context = this;
        settings = AppSettings.getInstance(this);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        getFromIntent();

        // methods for advancing windowed
        boolean settingsVal = settings.advanceWindowed;
        boolean fromWidget = getIntent().getBooleanExtra("from_widget", false);
        final boolean youtube;
        if (webpage != null && linkString != null) {
            youtube =
                    webpage.contains("youtu")
                    || linkString.contains("youtu")
                    || linkString.contains("/photo/1")
                    || linkString.contains(".mp4")
                    || linkString.contains("vine.co/v/");
        } else {
            youtube = true;
        }

        // cases: (youtube will ALWAYS be full screen...)
        // from widget
        // the user set the preference to advance windowed
        // has a webview and want to advance windowed
        if (fromWidget || settingsVal) {
            setUpWindow(youtube);
        }

        setUpTheme();

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        if (getIntent().getBooleanExtra("clicked_youtube", false)) {
            IntentFilter i = new IntentFilter("com.klinker.android.twitter.YOUTUBE_READY");
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    TweetYouTubeFragment.resume();
                    
                    context.unregisterReceiver(this);
                }
            }, i);
        }

        setContentView(R.layout.tweet_pager);
        pager = (ViewPager) findViewById(R.id.pager);
        mSectionsPagerAdapter = new TweetPagerAdapter(getFragmentManager(), context,
                name, screenName, tweet, time, retweeter, webpage, proPic, tweetId,
                picture, users, hashtags, otherLinks, isMyTweet, isMyRetweet, secondAcc, animatedGif);
        pager.setAdapter(mSectionsPagerAdapter);
        pager.setOffscreenPageLimit(5);

        final int numberOfPages = mSectionsPagerAdapter.getCount();

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                if (youtube) {
                    try {
                        switch (numberOfPages) {
                            case 3:
                            case 4:
                                if (i != 0) {
                                    TweetYouTubeFragment.pause();
                                } else {
                                    TweetYouTubeFragment.resume();
                                }
                                break;
                            case 5:
                                if (i != 1) {
                                    TweetYouTubeFragment.pause();
                                } else {
                                    TweetYouTubeFragment.resume();
                                }
                                break;
                        }
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        switch (numberOfPages) {
            case 2:
                if (settings.pageToOpen == AppSettings.PAGE_CONVO) {
                    pager.setCurrentItem(1);
                } else {
                    pager.setCurrentItem(0);
                }
                break;
            case 3:
                if (mSectionsPagerAdapter.getHasWebpage()) {
                    switch (settings.pageToOpen) {
                        case AppSettings.PAGE_CONVO:
                            pager.setCurrentItem(2);
                            break;
                        case AppSettings.PAGE_WEB:
                            pager.setCurrentItem(0);
                            break;
                        default:
                            pager.setCurrentItem(1);
                            break;
                    }
                } else { // no web page
                    switch (settings.pageToOpen) {
                        case AppSettings.PAGE_CONVO:
                            pager.setCurrentItem(2);
                            break;
                        default:
                            pager.setCurrentItem(1);
                            break;
                    }
                }
                break;
            case 4: // webpage and youtube
                switch (settings.pageToOpen) {
                    case AppSettings.PAGE_CONVO:
                        pager.setCurrentItem(3);
                        break;
                    case AppSettings.PAGE_WEB:
                        pager.setCurrentItem(1);
                        break;
                    default:
                        pager.setCurrentItem(0);
                        break;
                }
                break;
        }

        if (getIntent().getBooleanExtra("clicked_youtube", false)) {
            pager.setCurrentItem(0);
        }

        if (settings.addonTheme) {
            PagerTitleStrip strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
            strip.setBackgroundColor(settings.pagerTitleInt);

            if (!settings.showTitleStrip) {
                strip.setVisibility(View.GONE);
            }
        }


    }

    public void setUpTheme() {

        Utils.setUpPopupTheme(context, settings);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        if (settings.addonTheme) {
            getWindow().getDecorView().setBackgroundColor(settings.backgroundColor);
        }

        Utils.setActionBar(context, !settings.advanceWindowed);
    }

    public void setUpWindow(boolean youtube) {

        requestWindowFeature(Window.FEATURE_ACTION_BAR | Window.FEATURE_PROGRESS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        if(!youtube) {
            params.dimAmount = .75f;  // set it higher if you want to dim behind the window
        } else {
            params.dimAmount = 0f;
        }
        getWindow().setAttributes(params);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .9), (int) (height * .8));
        } else {
            getWindow().setLayout((int) (width * .7), (int) (height * .8));
        }
    }

    public void getFromIntent() {
        Intent from = getIntent();

        name = from.getStringExtra("name");
        screenName = from.getStringExtra("screenname");
        tweet = from.getStringExtra("tweet");
        time = from.getLongExtra("time", 0);
        retweeter = from.getStringExtra("retweeter");
        webpage = from.getStringExtra("webpage");
        tweetId = from.getLongExtra("tweetid", 0);
        picture = from.getBooleanExtra("picture", false);
        proPic = from.getStringExtra("proPic");
        secondAcc = from.getBooleanExtra("second_account", false);
        animatedGif = from.getStringExtra("animated_gif");

        try {
            users = from.getStringExtra("users").split("  ");
        } catch (Exception e) {
            users = null;
        }

        try {
            hashtags = from.getStringExtra("hashtags").split("  ");
        } catch (Exception e) {
            hashtags = null;
        }

        try {
            linkString = from.getStringExtra("other_links");
            otherLinks = linkString.split("  ");
        } catch (Exception e) {
            otherLinks = null;
        }

        if (screenName.equals(settings.myScreenName)) {
            isMyTweet = true;
        } else if (screenName.equals(retweeter)) {
            isMyRetweet = true;
        }

        tweet = restoreLinks(tweet);

        if (hashtags != null) {
            // we will add them to the auto complete
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> tags = new ArrayList<String>();
                    if (hashtags != null) {
                        for (String s : hashtags) {
                            if (!s.equals("")) {
                                tags.add("#" + s);
                            }
                        }
                    }

                    HashtagDataSource source = HashtagDataSource.getInstance(context);

                    for (String s : tags) {
                        Log.v("talon_hashtag", "trend: " + s);
                        if (s.contains("#")) {
                            // we want to add it to the auto complete
                            Log.v("talon_hashtag", "adding: " + s);

                            source.deleteTag(s);
                            source.createTag(s);
                        }
                    }
                }
            }).start();
        }
    }

    class DeleteTweet extends AsyncTask<String, Void, Boolean> {

        protected void onPreExecute() {
            finish();
        }

        protected Boolean doInBackground(String... urls) {
            Twitter twitter = Utils.getTwitter(context, settings);

            try {

                HomeDataSource.getInstance(context).deleteTweet(tweetId);
                MentionsDataSource.getInstance(context).deleteTweet(tweetId);

                try {
                    twitter.destroyStatus(tweetId);
                } catch (Exception x) {

                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean deleted) {
            if (deleted) {
                Toast.makeText(context, getResources().getString(R.string.deleted_tweet), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, getResources().getString(R.string.error_deleting), Toast.LENGTH_SHORT).show();
            }

            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("refresh_me", true).commit();
        }
    }

    class MarkSpam extends AsyncTask<String, Void, Boolean> {

        protected void onPreExecute() {
            finish();
        }

        protected Boolean doInBackground(String... urls) {
            Twitter twitter = Utils.getTwitter(context, settings);

            try {
                HomeDataSource.getInstance(context).deleteTweet(tweetId);
                MentionsDataSource.getInstance(context).deleteTweet(tweetId);

                try {
                    twitter.reportSpam(screenName.replace(" ", "").replace("@", ""));
                } catch (Throwable t) {
                    // for somme reason this causes a big "naitive crash" on some devices
                    // with a ton of random letters on play store reports... :/ hmm
                }

                try {
                    twitter.destroyStatus(tweetId);
                } catch (Exception x) {

                }

                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("refresh_me", true).commit();

                return true;
            } catch (Throwable e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private ShareActionProvider mShareActionProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.tweet_activity, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mShareActionProvider.setShareIntent(getShareIntent());

        return super.onCreateOptionsMenu(menu);
    }

    private Intent getShareIntent() {
        String text1 = tweet;
        text1 = "@" + screenName + ": " + text1 + "\n\n" + "https://twitter.com/" + screenName + "/status/" + tweetId;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text1);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tweet by @" + screenName);
        return intent;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        final int MENU_SHARE = 0;
        final int MENU_DELETE_TWEET = 1;
        final int MENU_QUOTE = 2;
        final int MENU_COPY_TEXT = 3;
        final int MENU_SAVE_IMAGE = 4;
        final int MENU_SPAM = 5;

        if (!isMyTweet) {
            menu.getItem(MENU_DELETE_TWEET).setVisible(false);
        } else {
            menu.getItem(MENU_QUOTE).setVisible(false);
            menu.getItem(MENU_SPAM).setVisible(false);
        }

        if (!picture) {
            menu.getItem(MENU_SAVE_IMAGE).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_delete_tweet:
                new DeleteTweet().execute();
                getSharedPreferences("com.klinker.android.twitter_world_preferences",
                        Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE)
                        .edit().putBoolean("just_muted", true).commit();
                return true;

            case R.id.menu_share:
                String text1 = tweet;
                text1 = "@" + screenName + ": " + text1 + "\n\n" + "https://twitter.com/" + screenName + "/status/" + tweetId;
                Log.v("my_text_on_share", text1);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, text1);

                startActivity(share);
                return true;

            case R.id.menu_copy_text:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("tweet_text", tweet);
                clipboard.setPrimaryClip(clip);
                return true;

            /*case R.id.menu_open_web:
                Uri weburi;
                try {
                    weburi = Uri.parse(otherLinks[0]);
                } catch (Exception e) {
                    weburi = Uri.parse(webpage);
                }
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, weburi);
                startActivity(launchBrowser);

                return true;*/

            case R.id.menu_save_image:

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Looper.prepare();

                        try {
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.ic_stat_icon)
                                            .setTicker(getResources().getString(R.string.downloading) + "...")
                                            .setContentTitle(getResources().getString(R.string.app_name))
                                            .setContentText(getResources().getString(R.string.saving_picture) + "...")
                                            .setProgress(100, 100, true)
                                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_save));

                            NotificationManager mNotificationManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(6, mBuilder.build());

                            String url = webpage;
                            if (webpage.contains("insta")) {
                                url = url.substring(0, url.length() - 1) + "l";
                            }
                            URL mUrl = new URL(url);

                            Bitmap bitmap = BitmapFactory.decodeStream(mUrl.openConnection().getInputStream());

                            Random generator = new Random();
                            int n = 1000000;
                            n = generator.nextInt(n);
                            String fname = "Image-" + n;

                            Uri uri = IOUtils.saveImage(bitmap, fname, context);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "image/*");

                            PendingIntent pending = PendingIntent.getActivity(context, 91, intent, 0);

                            mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setContentIntent(pending)
                                            .setSmallIcon(R.drawable.ic_stat_icon)
                                            .setTicker(getResources().getString(R.string.saved_picture) + "...")
                                            .setContentTitle(getResources().getString(R.string.app_name))
                                            .setContentText(getResources().getString(R.string.saved_picture) + "!")
                                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_save));

                            mNotificationManager.notify(6, mBuilder.build());
                        } catch (Exception e) {
                            e.printStackTrace();
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.ic_stat_icon)
                                            .setTicker(getResources().getString(R.string.error) + "...")
                                            .setContentTitle(getResources().getString(R.string.app_name))
                                            .setContentText(getResources().getString(R.string.error) + "...")
                                            .setProgress(100, 100, true)
                                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_save));

                            NotificationManager mNotificationManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(6, mBuilder.build());
                        }
                    }
                }).start();

                return true;

            case R.id.menu_quote:
                String text = tweet;

                if (!settings.preferRT) {
                    text = "\"@" + screenName + ": " + text + "\" ";
                } else {
                    text = " RT @" + screenName + ": " + text;
                }

                Intent quote = new Intent(context, ComposeActivity.class);
                quote.putExtra("user", text);
                quote.putExtra("id", tweetId);

                startActivity(quote);

                return true;

            case R.id.menu_spam:
                new MarkSpam().execute();
                getSharedPreferences("com.klinker.android.twitter_world_preferences",
                        Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE)
                        .edit().putBoolean("just_muted", true).commit();
                return super.onOptionsItemSelected(item);

            /*case R.id.menu_mute_hashtags:
                if (!hashtags[0].equals("")) {
                    ArrayList<String> tags = new ArrayList<String>();
                    if (hashtags != null) {
                        for (String s : hashtags) {
                            if (!s.equals("")) {
                                tags.add("#" + s);
                            }
                        }
                    }

                    final CharSequence[] fItems = new CharSequence[tags.size()];

                    for (int i = 0; i < tags.size(); i++) {
                        fItems[i] = tags.get(i);
                    }

                    final SharedPreferences sharedPreferences = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                            Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

                    if (fItems.length > 1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setItems(fItems, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                String touched = fItems[item] + "";
                                Toast.makeText(context, getResources().getString(R.string.muted) + " " + touched, Toast.LENGTH_SHORT).show();
                                touched = touched.replace("#", "") + " ";

                                String current = sharedPreferences.getString("muted_hashtags", "");
                                sharedPreferences.edit().putString("muted_hashtags", current + touched).commit();
                                sharedPreferences.edit().putBoolean("refresh_me", true).commit();

                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        String touched = fItems[0] + "";
                        Toast.makeText(context, getResources().getString(R.string.muted) + " " + touched, Toast.LENGTH_SHORT).show();
                        touched = touched.replace("#", "") + " ";

                        String current = sharedPreferences.getString("muted_hashtags", "");
                        sharedPreferences.edit().putString("muted_hashtags", current + touched).commit();
                        sharedPreferences.edit().putBoolean("refresh_me", true).commit();

                    }
                } else {
                    Toast.makeText(context, getResources().getString(R.string.no_hashtags), Toast.LENGTH_SHORT).show();
                }

                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("just_muted", true).commit();
                return super.onOptionsItemSelected(item);

            case R.id.menu_share_links:
                if (!otherLinks[0].equals("")) {
                    ArrayList<String> urls = new ArrayList<String>();
                    if (otherLinks != null) {
                        for (String s : otherLinks) {
                            if (!s.equals("")) {
                                urls.add(s);
                            }
                        }
                    }

                    final CharSequence[] fItems = new CharSequence[urls.size()];

                    for (int i = 0; i < urls.size(); i++) {
                        fItems[i] = urls.get(i);
                    }

                    if (fItems.length > 1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setItems(fItems, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                String touched = fItems[item] + "";

                                Intent intent=new Intent(android.content.Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                intent.putExtra(Intent.EXTRA_TEXT, touched);
                                context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.menu_share)));

                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        String touched = fItems[0] + "";

                        Intent intent=new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        intent.putExtra(Intent.EXTRA_TEXT, touched);
                        context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.menu_share)));
                    }
                } else {
                    Toast.makeText(context, getResources().getString(R.string.no_links), Toast.LENGTH_SHORT).show();
                }
                return super.onOptionsItemSelected(item);*/
            case R.id.menu_translate:
                try {
                    String query = tweet.replaceAll(" ", "+");
                    String url = "http://translate.google.com/#auto|en|" + tweet;
                    Uri uri = Uri.parse(url);

                    Intent browser = new Intent(Intent.ACTION_VIEW, uri);
                    browser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(browser);
                } catch (Exception e) {

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String restoreLinks(String text) {
        String full = text;

        String[] split = text.split("\\s");
        String[] otherLink = new String[otherLinks.length];

        for (int i = 0; i < otherLinks.length; i++) {
            otherLink[i] = "" + otherLinks[i];
        }

        for (String s : otherLink) {
            Log.v("talon_links", ":" + s + ":");
        }

        boolean changed = false;
        int otherIndex = 0;

        if (otherLink.length > 0) {
            for (int i = 0; i < split.length; i++) {
                String s = split[i];

                //if (Patterns.WEB_URL.matcher(s).find()) { // we know the link is cut off
                if (s.contains("...")) { // we know the link is cut off
                    String f = s.replace("...", "").replace("http", "");

                    f = stripTrailingPeriods(f);

                    try {
                        if (otherIndex < otherLinks.length) {
                            if (otherLink[otherIndex].substring(otherLink[otherIndex].length() - 1, otherLink[otherIndex].length()).equals("/")) {
                                otherLink[otherIndex] = otherLink[otherIndex].substring(0, otherLink[otherIndex].length() - 1);
                            }
                            f = otherLink[otherIndex].replace("http://", "").replace("https://", "").replace("www.", "");
                            otherLink[otherIndex] = "";
                            otherIndex++;

                            changed = true;
                        }
                    } catch (Exception e) {

                    }

                    if (changed) {
                        split[i] = f;
                    } else {
                        split[i] = s;
                    }
                } else {
                    split[i] = s;
                }

            }
        }

        if (!webpage.equals("")) {
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (s.contains("...")) {
                    s = s.replace("...", "");

                    if (Patterns.WEB_URL.matcher(s).find() && (s.startsWith("t.co/") || s.contains("twitter.com/"))) { // we know the link is cut off
                        String replace = otherLinks[otherLinks.length - 1];
                        if (replace.replace(" ", "").equals("")) {
                            replace = webpage;
                        }
                        split[i] = replace;
                        changed = true;
                    }
                }
            }
        }



        if(changed) {
            full = "";
            for (String p : split) {
                full += p + " ";
            }

            full = full.substring(0, full.length() - 1);
        }

        return full;
    }

    private static String stripTrailingPeriods(String url) {
        try {
            if (url.substring(url.length() - 1, url.length()).equals(".")) {
                return stripTrailingPeriods(url.substring(0, url.length() - 1));
            } else {
                return url;
            }
        } catch (Exception e) {
            return url;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);
    }
}
