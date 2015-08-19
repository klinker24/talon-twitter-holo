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

package com.klinker.android.twitter.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.sq_lite.DMDataSource;
import com.klinker.android.twitter.data.sq_lite.FavoriteUsersDataSource;
import com.klinker.android.twitter.data.sq_lite.HomeContentProvider;
import com.klinker.android.twitter.data.sq_lite.HomeDataSource;
import com.klinker.android.twitter.data.sq_lite.InteractionsDataSource;
import com.klinker.android.twitter.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.compose.WidgetCompose;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.utils.NotificationUtils;
import com.klinker.android.twitter.utils.TweetLinkUtils;
import com.klinker.android.twitter.utils.redirects.RedirectToPopup;
import com.klinker.android.twitter.utils.Utils;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.DirectMessage;
import twitter4j.IDs;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;
import twitter4j.UserStreamListener;
import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class TalonPullNotificationService extends Service {

    public static final int FOREGROUND_SERVICE_ID = 11;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public TwitterStream pushStream;
    public Context mContext;
    public BitmapLruCache mCache;
    public AppSettings settings;
    public SharedPreferences sharedPreferences;

    public NotificationCompat.Builder mBuilder;

    public static boolean shuttingDown = false;
    public static boolean isRunning = false;

    public boolean thisInstanceOn = true;

    public boolean showNotification;

    public ArrayList<Long> ids;
    public ArrayList<Long> blockedIds;

    @Override
    public void onCreate() {
        super.onCreate();

        if (TalonPullNotificationService.isRunning) {
            stopSelf();
            return;
        }

        TalonPullNotificationService.isRunning = true;

        settings = AppSettings.getInstance(this);

        mCache = App.getInstance(this).getBitmapCache();

        sharedPreferences = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        showNotification = sharedPreferences.getBoolean("show_pull_notification", true);
        pullUnread = sharedPreferences.getInt("pull_unread", 0);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent stop = new Intent(this, StopPull.class);
        PendingIntent stopPending = PendingIntent.getService(this, 0, stop, 0);

        Intent popup = new Intent(this, RedirectToPopup.class);
        popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        popup.putExtra("from_notification", true);
        PendingIntent popupPending = PendingIntent.getActivity(this, 0, popup, 0);

        Intent compose = new Intent(this, WidgetCompose.class);
        popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent composePending = PendingIntent.getActivity(this, 0, compose, 0);

        String text;

        int count = 0;

        if (sharedPreferences.getBoolean("is_logged_in_1", false)) {
            count++;
        }
        if (sharedPreferences.getBoolean("is_logged_in_2", false)) {
            count++;
        }

        boolean multAcc = false;
        if (count == 2) {
            multAcc = true;
        }

        if (settings.liveStreaming && settings.timelineNot) {
            text = getResources().getString(R.string.new_tweets_upper) + ": " + pullUnread;
        } else {
            text = getResources().getString(R.string.listening_for_mentions) + "...";
        }

        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.color.transparent)
                        .setContentTitle(getResources().getString(R.string.talon_pull) + (multAcc ? " - @" + settings.myScreenName : ""))
                        .setContentText(text)
                        .setOngoing(true)
                        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_stat_icon));


        if (getApplicationContext().getResources().getBoolean(R.bool.expNotifications)) {
            mBuilder.addAction(R.drawable.ic_cancel_dark, getApplicationContext().getResources().getString(R.string.stop), stopPending);
            mBuilder.addAction(R.drawable.ic_popup, getResources().getString(R.string.popup), popupPending);
            mBuilder.addAction(R.drawable.ic_send_dark, getResources().getString(R.string.tweet), composePending);
        }

        try {
            mBuilder.setWhen(0);
        } catch (Exception e) { }

        mBuilder.setContentIntent(pendingIntent);

        // priority flag is only available on api level 16 and above
        if (getResources().getBoolean(R.bool.expNotifications)) {
            mBuilder.setPriority(Notification.PRIORITY_MIN);
        }

        mContext = getApplicationContext();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.twitter.STOP_PUSH");
        registerReceiver(stopPush, filter);

        filter = new IntentFilter();
        filter.addAction("com.klinker.android.twitter.START_PUSH");
        registerReceiver(startPush, filter);

        filter = new IntentFilter();
        filter.addAction("com.klinker.android.twitter.STOP_PUSH_SERVICE");
        registerReceiver(stopService, filter);

        if (settings.liveStreaming && settings.timelineNot) {
            filter = new IntentFilter();
            filter.addAction("com.klinker.android.twitter.UPDATE_NOTIF");
            registerReceiver(updateNotification, filter);

            filter = new IntentFilter();
            filter.addAction("com.klinker.android.twitter.NEW_TWEET");
            registerReceiver(updateNotification, filter);

            filter = new IntentFilter();
            filter.addAction("com.klinker.android.twitter.CLEAR_PULL_UNREAD");
            registerReceiver(clearPullUnread, filter);
        }

        Thread start = new Thread(new Runnable() {
            @Override
            public void run() {
                // get the ids of everyone you follow
                try {
                    Log.v("getting_ids", "started getting ids, mine: " + settings.myId);
                    Twitter twitter = Utils.getTwitter(mContext, settings);
                    long currCursor = -1;
                    IDs idObject;

                    ids = new ArrayList<Long>();
                    do {
                        idObject = twitter.getFriendsIDs(settings.myId, currCursor);

                        long[] lIds = idObject.getIDs();
                        for (int i = 0; i < lIds.length; i++) {
                            ids.add(lIds[i]);
                        }
                    } while ((currCursor = idObject.getNextCursor()) != 0);
                    ids.add(settings.myId);

                    currCursor = -1;
                    blockedIds = new ArrayList<Long>();
                    do {
                        idObject = twitter.getBlocksIDs(currCursor);

                        long[] lIds = idObject.getIDs();
                        for (int i = 0; i < lIds.length; i++) {
                            blockedIds.add(lIds[i]);
                        }
                    } while ((currCursor = idObject.getNextCursor()) != 0);

                    idsLoaded = true;

                    if (showNotification)
                        startForeground(FOREGROUND_SERVICE_ID, mBuilder.build());

                    mContext.sendBroadcast(new Intent("com.klinker.android.twitter.START_PUSH"));
                } catch (Exception e) {
                    e.printStackTrace();
                    TalonPullNotificationService.isRunning = false;

                    pullUnread = 0;

                    Thread stop = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TalonPullNotificationService.shuttingDown = true;
                            try {
                                //pushStream.removeListener(userStream);
                            } catch (Exception x) {

                            }
                            try {
                                pushStream.cleanUp();
                                pushStream.shutdown();
                                Log.v("twitter_stream_push", "stopping push notifications");
                            } catch (Exception e) {
                                // it isn't running
                                e.printStackTrace();
                                // try twice to shut it down i guess
                                try {
                                    Thread.sleep(2000);
                                    pushStream.cleanUp();
                                    pushStream.shutdown();
                                    Log.v("twitter_stream_push", "stopping push notifications");
                                } catch (Exception x) {
                                    // it isn't running
                                    x.printStackTrace();
                                }
                            }

                            TalonPullNotificationService.shuttingDown = false;
                        }
                    });

                    stop.setPriority(Thread.MAX_PRIORITY);
                    stop.start();

                    stopSelf();
                } catch (OutOfMemoryError e) {
                    TalonPullNotificationService.isRunning = false;

                    Thread stop = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TalonPullNotificationService.shuttingDown = true;
                            try {
                                //pushStream.removeListener(userStream);
                            } catch (Exception x) {

                            }
                            try {
                                pushStream.cleanUp();
                                pushStream.shutdown();
                                Log.v("twitter_stream_push", "stopping push notifications");
                            } catch (Exception e) {
                                // it isn't running
                                e.printStackTrace();
                                // try twice to shut it down i guess
                                try {
                                    Thread.sleep(2000);
                                    pushStream.cleanUp();
                                    pushStream.shutdown();
                                    Log.v("twitter_stream_push", "stopping push notifications");
                                } catch (Exception x) {
                                    // it isn't running
                                    x.printStackTrace();
                                }
                            }

                            TalonPullNotificationService.shuttingDown = false;
                        }
                    });

                    stop.setPriority(Thread.MAX_PRIORITY);
                    stop.start();

                    pullUnread = 0;

                    stopSelf();
                }

            }
        });

        start.setPriority(Thread.MAX_PRIORITY - 1);
        start.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public boolean idsLoaded = false;

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(startPush);
        } catch (Exception e) { }
        try {
            unregisterReceiver(stopPush);
        } catch (Exception e) { }
        try {
            unregisterReceiver(stopService);
        } catch (Exception e) { }
        try {
            unregisterReceiver(updateNotification);
        } catch (Exception e) { }
        try {
            unregisterReceiver(clearPullUnread);
        } catch (Exception e) { }

        super.onDestroy();
    }

    public BroadcastReceiver stopPush = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        pushStream.cleanUp();
                        pushStream.shutdown();
                        Log.v("twitter_stream_push", "stopping push notifications");
                    } catch (Exception e) {
                        // it isn't running
                    }

                    pullUnread = 0;
                }
            });

            thisInstanceOn = false;
        }
    };

    public int pullUnread = 0;

    public BroadcastReceiver updateNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.v("twitter_stream_push", "updating notification");

            mBuilder.setContentText(getResources().getString(R.string.new_tweets_upper) + ": " + pullUnread);

            if (showNotification)
                startForeground(FOREGROUND_SERVICE_ID, mBuilder.build());
        }
    };

    public BroadcastReceiver clearPullUnread = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            pullUnread = 0;
            sharedPreferences.edit().putInt("pull_unread", 0).commit();

            mBuilder.setContentText(getResources().getString(R.string.new_tweets_upper) + ": " + pullUnread);

            if (showNotification)
                startForeground(FOREGROUND_SERVICE_ID, mBuilder.build());
        }
    };

    public BroadcastReceiver stopService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Thread stop = new Thread(new Runnable() {
                @Override
                public void run() {
                    TalonPullNotificationService.shuttingDown = true;
                    try {
                        pushStream.cleanUp();
                        pushStream.shutdown();
                        Log.v("twitter_stream_push", "stopping push notifications");
                    } catch (Exception e) {
                        // it isn't running
                        e.printStackTrace();
                        // try twice to shut it down i guess
                        try {
                            Thread.sleep(2000);
                            pushStream.cleanUp();
                            pushStream.shutdown();
                            Log.v("twitter_stream_push", "stopping push notifications");
                        } catch (Exception x) {
                            // it isn't running
                            x.printStackTrace();
                        }
                    }

                    TalonPullNotificationService.shuttingDown = false;
                }
            });

            stop.setPriority(Thread.MAX_PRIORITY);
            stop.start();

            TalonPullNotificationService.isRunning = false;
            thisInstanceOn = false;

            sharedPreferences.edit().putInt("pull_unread", pullUnread).commit();
            pullUnread = 0;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //pushStream.removeListener(userStream);
                    } catch (Exception x) {

                    }
                }
            }).start();
            stopSelf();

        }
    };

    public BroadcastReceiver startPush = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            thisInstanceOn = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (TalonPullNotificationService.shuttingDown) {
                        try {
                            Thread.sleep(1500);
                        } catch (Exception e) {

                        }
                    }

                    settings = AppSettings.getInstance(mContext);
                    pushStream = Utils.getStreamingTwitter(mContext, settings);

                    String myName = settings.myScreenName;
                    Log.v("twitter_stream_push", "my id: " + myName + "");

                    pushStream.addListener(userStream);
                    //pushStream.filter(new FilterQuery().track(new String[]{myName}));
                    pushStream.user(new String[] {myName});
                    Log.v("twitter_stream_push", "started push notifications");
                }
            }).start();

        }
    };

    public UserStreamListener userStream = new UserStreamListener() {
        @Override
        public void onStatus(final Status status) {

            if (!thisInstanceOn || isUserBlocked(status.getUser().getId())) {
                return;
            }

            UserMentionEntity[] entities = status.getUserMentionEntities();
            ArrayList<String> names = new ArrayList<String>();
            for (UserMentionEntity e : entities) {
                names.add(e.getScreenName());
            }
            if(names.contains(settings.myScreenName)) {
                Log.v("twitter_stream_push", "onStatus @" + status.getUser().getScreenName() + " - " + status.getText());

                if (!status.isRetweet()) { // it is a normal mention

                    MentionsDataSource mentions = MentionsDataSource.getInstance(mContext);

                    if (!mentions.tweetExists(status.getId(), sharedPreferences.getInt("current_account", 1))) {
                        mentions.createTweet(status, sharedPreferences.getInt("current_account", 1));
                    }
                    InteractionsDataSource.getInstance(mContext).createMention(mContext, status, sharedPreferences.getInt("current_account", 1));
                    sharedPreferences.edit().putBoolean("new_notification", true).commit();
                    sharedPreferences.edit().putBoolean("refresh_me_mentions", true).commit();

                    if(settings.notifications && settings.mentionsNot && !sharedPreferences.getString("muted_users", "").contains(status.getUser().getScreenName())) {
                        NotificationUtils.refreshNotification(mContext);
                    }

                    mContext.sendBroadcast(new Intent("com.klinker.android.twitter.NEW_MENTION"));

                } else { // it is a retweet
                    if (!status.getUser().getScreenName().equals(settings.myScreenName) && status.getRetweetedStatus().getUser().getScreenName().equals(settings.myScreenName)) {
                        if (settings.retweetNot) {
                            int newRetweets = sharedPreferences.getInt("new_retweets", 0);
                            newRetweets++;
                            sharedPreferences.edit().putInt("new_retweets", newRetweets).commit();
                        }

                        InteractionsDataSource.getInstance(mContext).updateInteraction(mContext, status.getUser(), status, sharedPreferences.getInt("current_account", 1), InteractionsDataSource.TYPE_RETWEET);
                        sharedPreferences.edit().putBoolean("new_notification", true).commit();

                        if(settings.notifications && settings.retweetNot) {
                            NotificationUtils.newInteractions(status.getUser(), mContext, sharedPreferences, " " + getResources().getString(R.string.retweeted));
                        }
                    }
                }
            }

            if (settings.liveStreaming && idsLoaded) {
                Long mId = status.getUser().getId();
                if (ids.contains(mId)) {
                    int currentAccount = sharedPreferences.getInt("current_account", 1);
                    HomeDataSource home = HomeDataSource.getInstance(mContext);
                    if (!home.tweetExists(status.getId(), currentAccount)) {
                        //HomeContentProvider.insertTweet(status, currentAccount, mContext);
                        home.createTweet(status, currentAccount);
                        sharedPreferences.edit().putLong("account_" + currentAccount + "_lastid", status.getId()).commit();
                        getContentResolver().notifyChange(HomeContentProvider.STREAM_NOTI, null);
                        getContentResolver().notifyChange(HomeContentProvider.CONTENT_URI, null);
                    }

                    pullUnread++;
                    sharedPreferences.edit().putInt("pull_unread", pullUnread).commit();
                    mContext.sendBroadcast(new Intent("com.klinker.android.twitter.NEW_TWEET"));
                    mContext.sendBroadcast(new Intent("com.klinker.android.twitter.UPDATE_NOTIF"));
                    mContext.sendBroadcast(new Intent("com.klinker.android.talon.UPDATE_WIDGET"));

                    sharedPreferences.edit().putBoolean("refresh_me", true).commit();

                    boolean favUser = FavoriteUsersDataSource.getInstance(mContext).isFavUser(sharedPreferences.getInt("current_account", 1), status.getUser().getScreenName());
                    if (favUser && settings.favoriteUserNotifications && settings.notifications) {
                        NotificationUtils.favUsersNotification(sharedPreferences.getInt("current_account", 1), mContext);
                    }

                    if (favUser) {
                        InteractionsDataSource.getInstance(mContext).createFavoriteUserInter(mContext, status, sharedPreferences.getInt("current_account", 1));
                        sharedPreferences.edit().putBoolean("new_notification", true).commit();
                    }

                    if (settings.preCacheImages) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                downloadImages(status);
                            }
                        }).start();
                    }
                }
            }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            /*HomeDataSource.getInstance(mContext)
                    .deleteTweet(statusDeletionNotice.getStatusId());
            sharedPreferences.edit().putBoolean("refresh_me", true).commit();*/
        }

        @Override
        public void onDeletionNotice(long directMessageId, long userId) {

        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {

        }

        @Override
        public void onStallWarning(StallWarning warning) {

        }

        @Override
        public void onFriendList(long[] friendIds) {

        }

        @Override
        public void onFavorite(User source, User target, Status favoritedStatus) {

            if (!thisInstanceOn || isUserBlocked(source.getId())) {
                return;
            }

            if(!source.getScreenName().equals(settings.myScreenName) && target.getScreenName().equals(settings.myScreenName)) {
                AppSettings settings = new AppSettings(mContext);

                Log.v("twitter_stream_push", "onFavorite source:@"
                        + source.getScreenName() + " target:@"
                        + target.getScreenName() + " @"
                        + favoritedStatus.getUser().getScreenName() + " - "
                        + favoritedStatus.getText());

                InteractionsDataSource.getInstance(mContext).updateInteraction(mContext,
                        source,
                        favoritedStatus,
                        sharedPreferences.getInt("current_account", 1),
                        InteractionsDataSource.TYPE_FAVORITE);

                sharedPreferences.edit().putBoolean("new_notification", true).commit();

                if (settings.favoritesNot) {
                    int newFavs = sharedPreferences.getInt("new_favorites", 0);
                    newFavs++;
                    sharedPreferences.edit().putInt("new_favorites", newFavs).commit();

                    if(settings.notifications) {
                        NotificationUtils.newInteractions(source, mContext, sharedPreferences, " " + getResources().getString(R.string.favorited));
                    }
                }
            }
        }

        @Override
        public void onQuotedTweet(User source, User target, Status status) {
            if (!thisInstanceOn || isUserBlocked(source.getId())) {
                return;
            }

            if(!source.getScreenName().equals(settings.myScreenName) && target.getScreenName().equals(settings.myScreenName)) {
                AppSettings settings = new AppSettings(mContext);

                Log.v("twitter_stream_push", "onQuote source:@"
                        + source.getScreenName() + " target:@"
                        + target.getScreenName() + " @"
                        + status.getUser().getScreenName() + " - "
                        + status.getText());

                InteractionsDataSource.getInstance(mContext).updateInteraction(mContext,
                        source,
                        status,
                        sharedPreferences.getInt("current_account", 1),
                        InteractionsDataSource.TYPE_QUOTED_TWEET);

                sharedPreferences.edit().putBoolean("new_notification", true).commit();

                if (settings.mentionsNot) {
                    int newQuotes = sharedPreferences.getInt("new_quotes", 0);
                    newQuotes++;
                    sharedPreferences.edit().putInt("new_quotes", newQuotes).commit();

                    if(settings.notifications) {
                        NotificationUtils.newInteractions(source, mContext, sharedPreferences, " " + getResources().getString(R.string.quoted));
                    }
                }
            }
        }

        @Override
        public void onUnfavorite(User source, User target, Status unfavoritedStatus) {

        }

        @Override
        public void onFollow(User source, User followedUser) {

            if (!thisInstanceOn) {
                return;
            }

            Log.v("twitter_stream_push", "onFollow source:@"
                    + source.getScreenName() + " target:@"
                    + followedUser.getScreenName());

            if (followedUser.getScreenName().equals(settings.myScreenName)) {

                AppSettings settings = new AppSettings(mContext);

                InteractionsDataSource.getInstance(mContext).createInteraction(mContext,
                        source,
                        null,
                        sharedPreferences.getInt("current_account", 1),
                        InteractionsDataSource.TYPE_FOLLOWER);

                sharedPreferences.edit().putBoolean("new_notification", true).commit();

                if (settings.followersNot) {
                    int newFollows = sharedPreferences.getInt("new_follows", 0);
                    newFollows++;
                    sharedPreferences.edit().putInt("new_follows", newFollows).commit();

                    if (settings.notifications) {
                        NotificationUtils.newInteractions(source, mContext, sharedPreferences, " " + getResources().getString(R.string.followed));
                    }
                }
            }
        }

        @Override
        public void onUnfollow(User user, User user2) {

        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {

            if (!thisInstanceOn) {
                return;
            }

            Log.v("twitter_stream_push", "onDirectMessage text:"
                    + directMessage.getText());

            AppSettings settings = new AppSettings(mContext);

            DMDataSource.getInstance(mContext).createDirectMessage(directMessage, sharedPreferences.getInt("current_account", 1));

            int numUnread = sharedPreferences.getInt("dm_unread_" + sharedPreferences.getInt("current_account", 1), 0);
            numUnread++;
            sharedPreferences.edit().putInt("dm_unread_" + sharedPreferences.getInt("current_account", 1), numUnread).commit();
            sharedPreferences.edit().putBoolean("refresh_me_dm", true).commit();


            sharedPreferences.edit().putLong("last_direct_message_id_" + sharedPreferences.getInt("current_account", 1), directMessage.getId()).commit();

            if (!directMessage.getSender().getScreenName().equals(settings.myScreenName) &&
                    settings.notifications &&
                    settings.dmsNot) {

                NotificationUtils.refreshNotification(mContext);
            }

            mContext.sendBroadcast(new Intent("com.klinker.android.twitter.NEW_DIRECT_MESSAGE"));
        }

        @Override
        public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {

        }

        @Override
        public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {

        }

        @Override
        public void onUserListSubscription(User subscriber, User listOwner, UserList list) {

        }

        @Override
        public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {

        }

        @Override
        public void onUserListCreation(User listOwner, UserList list) {

        }

        @Override
        public void onUserListUpdate(User listOwner, UserList list) {

        }

        @Override
        public void onUserListDeletion(User listOwner, UserList list) {

        }

        @Override
        public void onUserProfileUpdate(User updatedUser) {

        }

        @Override
        public void onUserSuspension(long suspendedUser) {

        }

        @Override
        public void onUserDeletion(long deletedUser) {

        }

        @Override
        public void onBlock(User source, User blockedUser) {

        }

        @Override
        public void onUnblock(User source, User unblockedUser) {

        }

        @Override
        public void onRetweetedRetweet(User user, User user1, Status status) {

        }

        @Override
        public void onFavoritedRetweet(User user, User user1, Status status) {

        }

        @Override
        public void onException(Exception ex) {
            ex.printStackTrace();
            Log.v("twitter_stream_push", "onException:" + ex.getMessage());

            // schedule an alarm to try to restart again since this one failed, probably no data connection
            /*AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            long now = Calendar.getInstance().getTimeInMillis();
            long alarm = now + 300000; // schedule it to begin in 5 mins

            PendingIntent pendingIntent = PendingIntent.getService(mContext, 236, new Intent(mContext, CatchupPull.class), 0);

            am.cancel(pendingIntent); // cancel the old one, then start the new one in 1 min
            am.set(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);*/

            //pushStream.clearListeners();
            //pushStream.shutdown();
            //pushStream.cleanUp();
        }
    };

    public void downloadImages(Status status) {
        String profilePic = status.getUser().getBiggerProfileImageURL();
        String imageUrl = TweetLinkUtils.getLinksInStatus(status)[1];

        CacheableBitmapDrawable wrapper = null;
        try {
            wrapper = mCache.get(profilePic);
        } catch (OutOfMemoryError e) {

        }

        if (wrapper == null) {

            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(profilePic).openConnection();
                InputStream is = new BufferedInputStream(conn.getInputStream());

                Bitmap image = decodeSampledBitmapFromResourceMemOpt(is, 500, 500);


                try {
                    is.close();
                } catch (Exception e) {

                }
                try {
                    conn.disconnect();
                } catch (Exception e) {

                }
                
                if (settings.roundContactImages) {
                    image = ImageUtils.getCircle(image, this);
                }

                mCache.put(profilePic, image);
            } catch (Throwable e) {

            }
        }

        if (!imageUrl.equals("")) {
            try {
                wrapper = mCache.get(imageUrl);
            } catch (OutOfMemoryError e) {
                wrapper = null;
            }
            if (wrapper == null) {
                try {
                    if (!imageUrl.contains(" ")) {
                        HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                        InputStream is = new BufferedInputStream(conn.getInputStream());

                        Bitmap image = decodeSampledBitmapFromResourceMemOpt(is, 1000, 1000);

                        try {
                            is.close();
                        } catch (Exception e) {

                        }
                        try {
                            conn.disconnect();
                        } catch (Exception e) {

                        }

                        mCache.put(imageUrl, image);
                    } else {
                        String[] pics = imageUrl.split(" ");
                        Bitmap[] bitmaps = new Bitmap[pics.length];

                        // need to download all of them, then combine them
                        for (int i = 0; i < pics.length; i++) {
                            String s = pics[i];

                            // The bitmap isn't cached so download from the web
                            HttpURLConnection conn = (HttpURLConnection) new URL(s).openConnection();
                            InputStream is = new BufferedInputStream(conn.getInputStream());

                            Bitmap b = decodeSampledBitmapFromResourceMemOpt(is, 1000, 1000);

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
                                mCache.put(s, b);

                                // throw it into our bitmap array for later
                                bitmaps[i] = b;
                            } catch (Exception e) {

                            }
                        }

                        // now that we have all of them, we need to put them together
                        Bitmap combined = ImageUtils.combineBitmaps(this, bitmaps);

                        try {
                            mCache.put(imageUrl, combined);
                        } catch (Exception e) {

                        }
                    }
                } catch (Throwable e) {

                }
            }
        }
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

    public boolean isUserBlocked(Long userId) {
        try {
            return blockedIds.contains(userId);
        } catch (Exception e) {
            return false;
        }
    }
}