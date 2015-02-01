package com.klinker.android.twitter.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.util.Log;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.sq_lite.ActivityDataSource;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.redirects.RedirectToActivity;
import com.klinker.android.twitter.utils.redirects.SwitchAccountsRedirect;
import twitter4j.*;

import java.util.*;

public class ActivityUtils {

    private static String TAG = "ActivityUtils";

    public static final int NOTIFICATON_ID = 434;
    public static final int SECOND_NOTIFICATION_ID = 435;

    private Context context;
    private AppSettings settings;
    private SharedPreferences sharedPrefs;
    private boolean useSecondAccount = false;
    private int currentAccount;
    private long lastRefresh;
    private long originalTime; // if the tweets came before this time, then we don't want to show them in activity because it would just get blown up.

    private List<String> notificationItems = new ArrayList<String>();
    private String notificationTitle = "";

    public ActivityUtils(Context context) {
        init(context);
    }

    public ActivityUtils(Context context, boolean useSecondAccount) {
        this.useSecondAccount = useSecondAccount;
        init(context);
    }

    public void init(Context context) {
        this.context = context;
        this.sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        this.settings = AppSettings.getInstance(context);
        this.currentAccount = sharedPrefs.getInt("current_account", 1);

        if (useSecondAccount) {
            if (currentAccount == 1) {
                currentAccount = 2;
            } else {
                currentAccount = 1;
            }
        }

        this.lastRefresh = sharedPrefs.getLong("last_activity_refresh_" + currentAccount, 0l);

        if (lastRefresh == 0l) { // first time...
            sharedPrefs.edit().putLong("original_activity_refresh_" + currentAccount, Calendar.getInstance().getTimeInMillis()).commit();
        }

        this.originalTime = sharedPrefs.getLong("original_activity_refresh_" + currentAccount, 0l);

        this.notificationTitle = context.getString(R.string.new_activity);
    }

    /**
     * Refresh the new followers, mentions, number of favorites, and retweeters
     * @return boolean if there was something new
     */
    public boolean refreshActivity() {
        boolean newActivity = false;
        Twitter twitter;

        if (!useSecondAccount) {
            twitter = Utils.getTwitter(context, settings);
        } else {
            twitter = Utils.getSecondTwitter(context);
        }

        if (getMentions(twitter)) {
            newActivity = true;
        }

        if (getFollowers(twitter)) {
            newActivity = true;
        }

        List<Status> myTweets = getMyTweets(twitter);
        if (myTweets != null) {
            if (getRetweets(twitter, myTweets)) {
                newActivity = true;
            }

            if (getFavorites(myTweets)) {
                newActivity = true;
            }
        }

        return newActivity;
    }

    public void postNotification() {
        postNotification(NOTIFICATON_ID);
    }

    public void postNotification(int id) {

        if (notificationItems.size() == 0) {
            return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(notificationTitle);
        mBuilder.setSmallIcon(R.drawable.ic_stat_icon);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_action_notification_dark));

        if (notificationItems.size() > 1) {
            // inbox style
            NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();

            try {
                inbox.setBigContentTitle(notificationTitle);
            } catch (Exception e) {

            }

            if (notificationItems.size() <= 5) {
                for (String s : notificationItems) {
                    inbox.addLine(Html.fromHtml(s));
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    inbox.addLine(Html.fromHtml(notificationItems.get(i)));
                }

                int extra = notificationItems.size() - 5;
                if (extra > 1) {
                    inbox.setSummaryText("+" + extra + " " + context.getString(R.string.items));
                } else {
                    inbox.setSummaryText("+" + extra + " " + context.getString(R.string.item));
                }
            }

            mBuilder.setStyle(inbox);
            mBuilder.setContentText(notificationItems.size() + " " + context.getString(R.string.items));
        } else {
            // big text style
            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText(Html.fromHtml(notificationItems.get(0)));
            bigText.setBigContentTitle(notificationTitle);

            mBuilder.setStyle(bigText);
            mBuilder.setContentText(Html.fromHtml(notificationItems.get(0)));
        }

        if (useSecondAccount) {
            mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, SwitchAccountsRedirect.class), 0));
        } else {
            mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, RedirectToActivity.class), 0));
        }

        if (settings.vibrate) {
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        if (settings.sound) {
            try {
                mBuilder.setSound(Uri.parse(settings.ringtone));
            } catch (Exception e) {
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }
        }

        if (settings.led) {
            mBuilder.setLights(0xFFFFFF, 1000, 1000);
        }

        if (settings.wakeScreen) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            final PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
            wakeLock.acquire(5000);
        }


        // Pebble notification
        if(sharedPrefs.getBoolean("pebble_notification", false)) {
            NotificationUtils.sendAlertToPebble(context, notificationTitle, notificationItems.get(0));
        }

        // Light Flow notification
        NotificationUtils.sendToLightFlow(context, notificationTitle, notificationItems.get(0));

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        notificationManager.notify(id, mBuilder.build());
    }

    public void commitLastRefresh(long id) {
        sharedPrefs.edit().putLong("last_activity_refresh_" + currentAccount, id).commit();
    }

    public void insertMentions(List<Status> mentions) {
        try {
            List<String> notis = ActivityDataSource.getInstance(context).insertMentions(mentions, currentAccount);
            notificationItems.addAll(notis);
        } catch (Throwable t) {

        }
    }

    public void insertFollowers(List<User> users) {
        try {
            String noti = ActivityDataSource.getInstance(context).insertNewFollowers(users, currentAccount);
            notificationItems.add(noti);
        } catch (Throwable t) {

        }
    }

    public boolean tryInsertRetweets(Status status, Twitter twitter) {
        try {
            String noti = ActivityDataSource.getInstance(context).insertRetweeters(status, currentAccount, twitter);

            if (noti != null) {
                notificationItems.add(noti);
                return true;
            } else {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean tryInsertFavorites(Status status) {
        try {
            String noti = ActivityDataSource.getInstance(context).insertFavoriteCount(status, currentAccount);

            if (noti != null) {
                notificationItems.add(noti);
                return true;
            } else {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    public List<Status> getMyTweets(Twitter twitter) {
        try {
            Paging paging = new Paging(1, 20);
            return twitter.getUserTimeline(paging);
        } catch (TwitterException e) {
            return null;
        }
    }

    public boolean getMentions(Twitter twitter) {
        boolean newActivity = false;

        try {
            if (lastRefresh != 0l) {
                Paging paging = new Paging(1, 50, lastRefresh);
                List<Status> mentions = twitter.getMentionsTimeline(paging);

                if (mentions.size() > 0) {
                    insertMentions(mentions);
                    commitLastRefresh(mentions.get(0).getId());
                    newActivity = true;
                }
            } else {
                Paging paging = new Paging(1, 1);
                List<Status> lastMention = twitter.getMentionsTimeline(paging);

                if (lastMention.size() > 0) {
                    commitLastRefresh(lastMention.get(0).getId());
                }
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return newActivity;
    }

    public boolean getFollowers(Twitter twitter) {
        boolean newActivity = false;

        try {
            List<User> followers = twitter.getFollowersList(AppSettings.getInstance(context).myId, -1, 200);
            User me = twitter.verifyCredentials();

            int oldFollowerCount = sharedPrefs.getInt("activity_follower_count_" + currentAccount, 0);
            Set<String> latestFollowers = sharedPrefs.getStringSet("activity_latest_followers_" + currentAccount, new HashSet<String>());

            Log.v(TAG, "followers set size: " + latestFollowers.size());
            Log.v(TAG, "old follower count: " + oldFollowerCount);
            Log.v(TAG, "current follower count: " + me.getFollowersCount());

            List<User> newFollowers = new ArrayList<User>();
            if (latestFollowers.size() != 0) {
                for (int i = 0; i < followers.size(); i++) {
                    if (!latestFollowers.contains(followers.get(i).getScreenName())) {
                        Log.v(TAG, "inserting @" + followers.get(i).getScreenName() + " as new follower");
                        newFollowers.add(followers.get(i));
                        newActivity = true;
                    } else {
                        break;
                    }
                }
            }

            insertFollowers(newFollowers);

            latestFollowers.clear();
            for (int i = 0; i < 50; i++) {
                if (i < followers.size()) {
                    latestFollowers.add(followers.get(i).getScreenName());
                } else {
                    break;
                }
            }

            SharedPreferences.Editor e = sharedPrefs.edit();
            e.putStringSet("activity_latest_followers_" + currentAccount, latestFollowers);
            e.putInt("activity_follower_count_" + currentAccount, me.getFollowersCount());
            e.commit();
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return newActivity;
    }

    public boolean getRetweets(Twitter twitter, List<Status> statuses) {
        boolean newActivity = false;

        for (Status s : statuses) {
            if (s.getCreatedAt().getTime() > originalTime && tryInsertRetweets(s, twitter)) {
                newActivity = true;
            }
        }

        return newActivity;
    }

    public boolean getFavorites(List<Status> statuses) {
        boolean newActivity = false;

        for (Status s : statuses) {
            if (s.getCreatedAt().getTime() > originalTime && tryInsertFavorites(s)) {
                newActivity = true;
            }
        }

        return newActivity;
    }
}
