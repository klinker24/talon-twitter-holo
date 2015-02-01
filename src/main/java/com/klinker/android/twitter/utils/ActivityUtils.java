package com.klinker.android.twitter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.klinker.android.twitter.data.sq_lite.ActivityDataSource;
import com.klinker.android.twitter.settings.AppSettings;
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

    private String notificationText = "";
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
        this.lastRefresh = sharedPrefs.getLong("last_activity_refresh_" + currentAccount, 0l);

        if (lastRefresh == 0l) { // first time...
            sharedPrefs.edit().putLong("original_activity_refresh_" + currentAccount, Calendar.getInstance().getTimeInMillis()).commit();
        }

        this.originalTime = sharedPrefs.getLong("original_activity_refresh_" + currentAccount, 0l);

        if (useSecondAccount) {
            if (currentAccount == 1) {
                currentAccount = 2;
            } else {
                currentAccount = 1;
            }
        }
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
        // if id = second id, then we will want to switchaccountsredirect, otherwise open to the activity timeline
    }

    public void commitLastRefresh(long id) {
        sharedPrefs.edit().putLong("last_activity_refresh_" + currentAccount, id).commit();
    }

    public void insertMentions(List<Status> mentions) {
        try {
            ActivityDataSource.getInstance(context).insertMentions(mentions, currentAccount);
        } catch (Throwable t) {

        }
    }

    public void insertFollowers(List<User> users) {
        try {
            ActivityDataSource.getInstance(context).insertNewFollowers(users, currentAccount);
        } catch (Throwable t) {

        }
    }

    public boolean tryInsertRetweets(Status status, Twitter twitter) {
        try {
            return ActivityDataSource.getInstance(context).insertRetweeters(status, currentAccount, twitter);
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean tryInsertFavorites(Status status) {
        try {
            return ActivityDataSource.getInstance(context).insertFavoriteCount(status, currentAccount);
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
            if (latestFollowers.size() != 0 &&
                    me.getFollowersCount() > oldFollowerCount) {
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
            for (int i = 0; i < 20; i++) {
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
