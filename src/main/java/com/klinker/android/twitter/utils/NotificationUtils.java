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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.text.Html;
import android.util.Log;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.sq_lite.*;
import com.klinker.android.twitter.receivers.NotificationDeleteReceiverOne;
import com.klinker.android.twitter.receivers.NotificationDeleteReceiverTwo;
import com.klinker.android.twitter.services.MarkReadSecondAccService;
import com.klinker.android.twitter.services.MarkReadService;
import com.klinker.android.twitter.services.ReadInteractionsService;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.compose.ComposeDMActivity;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.compose.NotificationCompose;
import com.klinker.android.twitter.ui.compose.NotificationComposeSecondAcc;
import com.klinker.android.twitter.ui.compose.NotificationDMCompose;
import com.klinker.android.twitter.ui.tweet_viewer.NotiTweetPager;
import com.klinker.android.twitter.utils.redirects.RedirectToDMs;
import com.klinker.android.twitter.utils.redirects.RedirectToDrawer;
import com.klinker.android.twitter.utils.redirects.RedirectToMentions;
import com.klinker.android.twitter.utils.redirects.RedirectToPopup;
import com.klinker.android.twitter.utils.redirects.SwitchAccountsRedirect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import twitter4j.User;
import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class NotificationUtils {

    // Key for the string that's delivered in the action's intent
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    public static void refreshNotification(Context context) {
        refreshNotification(context, false);
    }

    public static void refreshNotification(Context context, boolean noTimeline) {
        AppSettings settings = AppSettings.getInstance(context);

        SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        int currentAccount = sharedPrefs.getInt("current_account", 1);

        //int[] unreadCounts = new int[] {4, 1, 2}; // for testing
        int[] unreadCounts = getUnreads(context);

        int timeline = unreadCounts[0];
        int realTimelineCount = timeline;

        // if they don't want that type of notification, simply set it to zero
        if (!settings.timelineNot || (settings.pushNotifications && settings.liveStreaming) || noTimeline) {
            unreadCounts[0] = 0;
        }
        if (!settings.mentionsNot) {
            unreadCounts[1] = 0;
        }
        if (!settings.dmsNot) {
            unreadCounts[2] = 0;
        }

        if (unreadCounts[0] == 0 && unreadCounts[1] == 0 && unreadCounts[2] == 0) {

        } else {
            Intent markRead = new Intent(context, MarkReadService.class);
            PendingIntent readPending = PendingIntent.getService(context, 0, markRead, 0);

            String shortText = getShortText(unreadCounts, context, currentAccount);
            String longText = getLongText(unreadCounts, context, currentAccount);
            // [0] is the full title and [1] is the screenname
            String[] title = getTitle(unreadCounts, context, currentAccount);
            boolean useExpanded = useExp(context);
            boolean addButton = addBtn(unreadCounts);

            if (title == null) {
                return;
            }

            Intent resultIntent;

            if (unreadCounts[1] != 0 && unreadCounts[0] == 0) {
                // it is a mention notification (could also have a direct message)
                resultIntent = new Intent(context, RedirectToMentions.class);
            } else if (unreadCounts[2] != 0 && unreadCounts[0] == 0 && unreadCounts[1] == 0) {
                // it is a direct message
                resultIntent = new Intent(context, RedirectToDMs.class);
            } else {
                resultIntent = new Intent(context, MainActivity.class);
            }

            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0 );

            NotificationCompat.Builder mBuilder;

            Intent deleteIntent = new Intent(context, NotificationDeleteReceiverOne.class);

            mBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(title[0])
                    .setContentText(TweetLinkUtils.removeColorHtml(shortText, settings))
                    .setSmallIcon(R.drawable.ic_stat_icon)
                    .setLargeIcon(getIcon(context, unreadCounts, title[1]))
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setTicker(TweetLinkUtils.removeColorHtml(shortText, settings))
                    .setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, 0))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            if (unreadCounts[1] > 1 && unreadCounts[0] == 0 && unreadCounts[2] == 0) {
                // inbox style notification for mentions
                mBuilder.setStyle(getMentionsInboxStyle(unreadCounts[1],
                        currentAccount,
                        context,
                        TweetLinkUtils.removeColorHtml(shortText, settings)));
            } else if (unreadCounts[2] > 1 && unreadCounts[0] == 0 && unreadCounts[1] == 0) {
                // inbox style notification for direct messages
                mBuilder.setStyle(getDMInboxStyle(unreadCounts[1],
                        currentAccount,
                        context,
                        TweetLinkUtils.removeColorHtml(shortText, settings)));
            } else  {
                // big text style for an unread count on timeline, mentions, and direct messages
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(settings.addonTheme ?
                        longText.replaceAll("FF8800", settings.accentColor) : longText)));
            }

            // Pebble notification
            if(sharedPrefs.getBoolean("pebble_notification", false)) {
                sendAlertToPebble(context, title[0], shortText);
            }

            // Light Flow notification
            sendToLightFlow(context, title[0], shortText);

            int homeTweets = unreadCounts[0];
            int mentionsTweets = unreadCounts[1];
            int dmTweets = unreadCounts[2];

            int newC = 0;

            if (homeTweets > 0) {
                newC++;
            }
            if (mentionsTweets > 0) {
                newC++;
            }
            if (dmTweets > 0) {
                newC++;
            }

            if (settings.notifications && newC > 0) {

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

                if (settings.led)
                    mBuilder.setLights(0xFFFFFF, 1000, 1000);

                // Get an instance of the NotificationManager service
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(context);

                if (addButton) { // the reply and read button should be shown
                    Intent reply;
                    if (unreadCounts[1] == 1) {
                        reply = new Intent(context, NotificationCompose.class);
                    } else {
                        reply = new Intent(context, NotificationDMCompose.class);
                    }

                    Log.v("username_for_noti", title[1]);
                    sharedPrefs.edit().putString("from_notification", "@" + title[1] + " " + title[2]).commit();
                    MentionsDataSource data = MentionsDataSource.getInstance(context);
                    long id = data.getLastIds(currentAccount)[0];
                    PendingIntent replyPending = PendingIntent.getActivity(context, 0, reply, 0);
                    sharedPrefs.edit().putLong("from_notification_long", id).commit();
                    sharedPrefs.edit().putString("from_notification_text", "@" + title[1] + ": " + TweetLinkUtils.removeColorHtml(shortText, settings)).commit();

                    // Create the remote input
                    RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                            .setLabel("@" + title[1] + " ")
                            .build();

                    // Create the notification action
                    NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_action_reply_dark,
                            context.getResources().getString(R.string.noti_reply), replyPending)
                            .addRemoteInput(remoteInput)
                            .build();

                    NotificationCompat.Action.Builder action = new NotificationCompat.Action.Builder(
                            R.drawable.ic_action_read_dark,
                            context.getResources().getString(R.string.mark_read), readPending);

                    mBuilder.addAction(replyAction);
                    mBuilder.addAction(action.build());
                } else { // otherwise, if they can use the expanded notifications, the popup button will be shown
                    Intent popup = new Intent(context, RedirectToPopup.class);
                    popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    popup.putExtra("from_notification", true);

                    PendingIntent popupPending = PendingIntent.getActivity(context, 0, popup, 0);

                    NotificationCompat.Action.Builder action = new NotificationCompat.Action.Builder(
                            R.drawable.ic_popup,
                            context.getResources().getString(R.string.popup), popupPending);

                    mBuilder.addAction(action.build());
                }

                // Build the notification and issues it with notification manager.
                notificationManager.notify(1, mBuilder.build());

                // if we want to wake the screen on a new message
                if (settings.wakeScreen) {
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    final PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                    wakeLock.acquire(5000);
                }
            }

            // if there are unread tweets on the timeline, check them for favorite users
            if (settings.favoriteUserNotifications && realTimelineCount > 0) {
                favUsersNotification(currentAccount, context);
            }
        }

        try {

            ContentValues cv = new ContentValues();

            cv.put("tag", "com.klinker.android.twitter/com.klinker.android.twitter.ui.MainActivity");

            // add the direct messages and mentions
            cv.put("count", unreadCounts[1] + unreadCounts[2]);

            context.getContentResolver().insert(Uri
                            .parse("content://com.teslacoilsw.notifier/unread_count"),
                    cv);

        } catch (IllegalArgumentException ex) {

            /* Fine, TeslaUnread is not installed. */

        } catch (Exception ex) {

            /* Some other error, possibly because the format
               of the ContentValues are incorrect.

                Log but do not crash over this. */

            ex.printStackTrace();

        }
    }

    public static boolean addBtn(int[] unreadCount) {
        int homeTweets = unreadCount[0];
        int mentionsTweets = unreadCount[1];
        int dmTweets = unreadCount[2];

        return ((mentionsTweets == 1 && dmTweets == 0) || (dmTweets == 1 && mentionsTweets == 0)) && homeTweets == 0;
    }

    public static boolean useExp(Context context) {
        if (context.getResources().getBoolean(R.bool.expNotifications)) {
            return true;
        } else {
            return false;
        }
    }

    public static int[] getUnreads(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        int currentAccount = sharedPrefs.getInt("current_account", 1);

        HomeDataSource data = HomeDataSource.getInstance(context);
        int homeTweets = data.getUnreadCount(currentAccount);

        MentionsDataSource mentions = MentionsDataSource.getInstance(context);
        int mentionsTweets = mentions.getUnreadCount(currentAccount);

        int dmTweets = sharedPrefs.getInt("dm_unread_" + currentAccount, 0);

        return new int[] {homeTweets, mentionsTweets, dmTweets};
    }

    public static String[] getTitle(int[] unreadCount, Context context, int currentAccount) {
        String text = "";
        String name = null;
        String names = "";
        int homeTweets = unreadCount[0];
        int mentionsTweets = unreadCount[1];
        int dmTweets = unreadCount[2];

        // they only have a new mention
        if (mentionsTweets == 1 && homeTweets == 0 && dmTweets == 0) {
            MentionsDataSource mentions = MentionsDataSource.getInstance(context);
            name = mentions.getNewestName(currentAccount);
            String n = mentions.getNewestNames(currentAccount);
            SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
            for (String s : n.split("  ")) {
                if (!s.equals("") &&
                        !sharedPrefs.getString("twitter_screen_name_" + currentAccount, "").equals(s) &&
                        !s.equals(name)) {
                    names += "@" + s + " ";
                }
            }
            text = context.getResources().getString(R.string.mentioned_by) + " @" + name;

            // if they are muted, and you don't want them to show muted mentions
            // then just quit
            if (sharedPrefs.getString("muted_users", "").contains(name) &&
                    !sharedPrefs.getBoolean("show_muted_mentions", false)) {
                return null;
            }
        } else if (homeTweets == 0 && mentionsTweets == 0 && dmTweets == 1) { // they have 1 new direct message
            DMDataSource dm = DMDataSource.getInstance(context);
            name = dm.getNewestName(currentAccount);
            text = context.getResources().getString(R.string.message_from) + " @" + name;
        } else { // other cases we will just put talon
            text = context.getResources().getString(R.string.app_name);
        }

        return new String[] {text, name, names};
    }

    public static String getShortText(int[] unreadCount, Context context, int currentAccount) {
        String text = "";
        int homeTweets = unreadCount[0];
        int mentionsTweets = unreadCount[1];
        int dmTweets = unreadCount[2];

        if (mentionsTweets == 1 && homeTweets == 0 && dmTweets == 0) { // display the new mention
            MentionsDataSource mentions = MentionsDataSource.getInstance(context);
            text = mentions.getNewestMessage(currentAccount);
        } else if (dmTweets == 1 && mentionsTweets == 0 && homeTweets == 0) { // display the new message
            DMDataSource dm = DMDataSource.getInstance(context);
            text = dm.getNewestMessage(currentAccount);
        } else if (homeTweets > 0 && mentionsTweets == 0 && dmTweets == 0) { // it is just tweets being displayed, so put new out front
            text = homeTweets + " " + (homeTweets == 1 ? context.getResources().getString(R.string.new_tweet) : context.getResources().getString(R.string.new_tweets));
        } else {
            // home tweets
            if(homeTweets > 0) {
                text += homeTweets + " " + (homeTweets == 1 ? context.getResources().getString(R.string.tweet) : context.getResources().getString(R.string.tweets)) +
                        (mentionsTweets > 0 || dmTweets > 0 ? ", " : "");
            }

            // mentions
            if(mentionsTweets > 0) {
                text += mentionsTweets + " " + (mentionsTweets == 1 ? context.getResources().getString(R.string.mention) : context.getResources().getString(R.string.mentions)) +
                        (dmTweets > 0 ? ", " : "");
            }

            // direct messages
            if (dmTweets > 0) {
                text += dmTweets + " " + (dmTweets == 1 ? context.getResources().getString(R.string.message) : context.getResources().getString(R.string.messages));
            }
        }

        return text;
    }

    public static String getLongText(int[] unreadCount, Context context, int currentAccount) {

        String body = "";
        int homeTweets = unreadCount[0];
        int mentionsTweets = unreadCount[1];
        int dmTweets = unreadCount[2];

        if (mentionsTweets == 1 && homeTweets == 0 && dmTweets == 0) { // display the new mention
            MentionsDataSource mentions = MentionsDataSource.getInstance(context);
            body = mentions.getNewestMessage(currentAccount);
        } else if (dmTweets == 1 && mentionsTweets == 0 && homeTweets == 0) { // display the new message
            DMDataSource dm = DMDataSource.getInstance(context);
            body = dm.getNewestMessage(currentAccount);
        } else {
            if (homeTweets > 0) {
                body += "<b>" + context.getResources().getString(R.string.timeline) + ": </b>" + homeTweets + " " + (homeTweets == 1 ? context.getResources().getString(R.string.new_tweet) : context.getResources().getString(R.string.new_tweets)) + (mentionsTweets > 0 || dmTweets > 0 ? "<br>" : "");
            }

            if (mentionsTweets > 0) {
                body += "<b>" + context.getResources().getString(R.string.mentions) + ": </b>" + mentionsTweets + " " + (mentionsTweets == 1 ? context.getResources().getString(R.string.new_mention) : context.getResources().getString(R.string.new_mentions)) + (dmTweets > 0 ? "<br>" : "");
            }

            if (dmTweets > 0) {
                body += "<b>" + context.getResources().getString(R.string.direct_messages) + ": </b>" + dmTweets + " " + (dmTweets == 1 ? context.getResources().getString(R.string.new_message) : context.getResources().getString(R.string.new_messages));
            }
        }
        return body;
    }

    public static String getLongTextNoHtml(int[] unreadCount, Context context, int currentAccount) {

        String body = "";
        int homeTweets = unreadCount[0];
        int mentionsTweets = unreadCount[1];
        int dmTweets = unreadCount[2];

        if (mentionsTweets == 1 && homeTweets == 0 && dmTweets == 0) { // display the new mention
            MentionsDataSource mentions = MentionsDataSource.getInstance(context);
            body = mentions.getNewestMessage(currentAccount);
        } else if (dmTweets == 1 && mentionsTweets == 0 && homeTweets == 0) { // display the new message
            DMDataSource dm = DMDataSource.getInstance(context);
            body = dm.getNewestMessage(currentAccount);
        } else {
            if (homeTweets > 0) {
                body += context.getResources().getString(R.string.timeline) + ": " + homeTweets + " " + (homeTweets == 1 ? context.getResources().getString(R.string.new_tweet) : context.getResources().getString(R.string.new_tweets)) + (mentionsTweets > 0 || dmTweets > 0 ? "\n" : "");
            }

            if (mentionsTweets > 0) {
                body += context.getResources().getString(R.string.mentions) + ": " + mentionsTweets + " " + (mentionsTweets == 1 ? context.getResources().getString(R.string.new_mention) : context.getResources().getString(R.string.new_mentions)) + (dmTweets > 0 ? "\n" : "");
            }

            if (dmTweets > 0) {
                body += context.getResources().getString(R.string.direct_messages) + ": " + dmTweets + " " + (dmTweets == 1 ? context.getResources().getString(R.string.new_message) : context.getResources().getString(R.string.new_messages));
            }
        }
        return body;
    }

    public static Bitmap getIcon(Context context, int[] unreadCount, String screenname) {

        int homeTweets = unreadCount[0];
        int mentionsTweets = unreadCount[1];
        int dmTweets = unreadCount[2];

        boolean customPic = (mentionsTweets == 1 && homeTweets == 0 && dmTweets == 0) ||
                (dmTweets == 1 && homeTweets == 0 && mentionsTweets == 0);

        if (screenname != null && customPic) {
            BitmapLruCache mCache = App.getInstance(context).getBitmapCache();
            Log.v("notifications_talon", "in screenname");
            String url;
            try {
                url = Utils.getTwitter(context, AppSettings.getInstance(context)).showUser(screenname).getBiggerProfileImageURL();
                CacheableBitmapDrawable wrapper = mCache.get(url + "_notification");

                Log.v("notifications_talon", "got wrapper");

                if (wrapper == null) {

                    Log.v("notifications_talon", "wrapper null");
                    URL mUrl = new URL(url);
                    Bitmap image = BitmapFactory.decodeStream(mUrl.openConnection().getInputStream());
                    image = ImageUtils.notificationResize(context, image);
                    mCache.put(url + "_notification", image);
                    return image;
                } else {
                    return wrapper.getBitmap();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_icon);
    }

    public static void favUsersNotification(int account, Context context) {

        ArrayList<String[]> tweets = new ArrayList<String[]>();

        HomeDataSource data = HomeDataSource.getInstance(context);
        Cursor cursor = data.getUnreadCursor(account);

        FavoriteUsersDataSource favs = FavoriteUsersDataSource.getInstance(context);

        if(cursor.moveToFirst()) {
            do {
                String screenname = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_SCREEN_NAME));

                if (favs.isFavUser(account, screenname)) {
                    String name = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_NAME));
                    String text = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TEXT));
                    String time = cursor.getLong(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TIME)) + "";
                    String picUrl = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_PIC_URL));
                    String otherUrl = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_URL));
                    String users = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_USERS));
                    String hashtags = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_HASHTAGS));
                    String id = cursor.getLong(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID)) + "";
                    String profilePic = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_PRO_PIC));
                    String otherUrls = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_URL));
                    String userss = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_USERS));
                    String hashtagss = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_HASHTAGS));
                    String retweeter;
                    try {
                        retweeter = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_RETWEETER));
                    } catch (Exception e) {
                        retweeter = "";
                    }
                    String link = "";

                    boolean displayPic = !picUrl.equals("") && !picUrl.contains("youtube");
                    if (displayPic) {
                        link = picUrl;
                    } else {
                        link = otherUrls.split("  ")[0];
                    }

                    tweets.add(new String[] {
                            name,
                            text,
                            screenname,
                            time,
                            retweeter,
                            link,
                            displayPic ? "true" : "false",
                            id,
                            profilePic,
                            userss,
                            hashtagss,
                            otherUrls
                    });
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        if (tweets.size() > 0) {
            if (tweets.size() == 1) {
                makeFavsNotificationToActivity(tweets, context);
            } else {
                AppSettings settings = AppSettings.getInstance(context);
                makeFavsNotification(tweets, context, settings.liveStreaming || settings.pushNotifications);
            }
        }
    }

    public static void makeFavsNotificationToActivity(ArrayList<String[]> tweets, Context context) {

        SharedPreferences.Editor e = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE).edit();

        e.putString("fav_user_tweet_name", tweets.get(0)[0]);
        e.putString("fav_user_tweet_text", tweets.get(0)[1]);
        e.putString("fav_user_tweet_screenname", tweets.get(0)[2]);
        e.putLong("fav_user_tweet_time", Long.parseLong(tweets.get(0)[3]));
        e.putString("fav_user_tweet_retweeter", tweets.get(0)[4]);
        e.putString("fav_user_tweet_webpage", tweets.get(0)[5]);
        e.putBoolean("fav_user_tweet_picture", tweets.get(0)[6].equals("true") ? true : false);
        e.putLong("fav_user_tweet_tweet_id", Long.parseLong(tweets.get(0)[7]));
        e.putString("fav_user_tweet_pro_pic", tweets.get(0)[8]);
        e.putString("fav_user_tweet_users", tweets.get(0)[9]);
        e.putString("fav_user_tweet_hashtags", tweets.get(0)[10]);
        e.putString("fav_user_tweet_links", tweets.get(0)[11]);
        e.commit();

        makeFavsNotification(tweets, context, false);
    }

    public static void makeFavsNotification(ArrayList<String[]> tweets, Context context, boolean toDrawer) {
        String shortText;
        String longText;
        String title;
        int smallIcon = R.drawable.ic_stat_icon;
        Bitmap largeIcon;

        Intent resultIntent;

        if (toDrawer) {
            resultIntent = new Intent(context, RedirectToDrawer.class);
        } else {
            resultIntent = new Intent(context, NotiTweetPager.class);
        }

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0 );

        NotificationCompat.InboxStyle inbox = null;

        if (tweets.size() == 1) {
            title = tweets.get(0)[0];
            shortText = tweets.get(0)[1];
            longText = shortText;

            largeIcon = getImage(context, tweets.get(0)[2]);
        } else {
            inbox = new NotificationCompat.InboxStyle();

            title = context.getResources().getString(R.string.favorite_users);
            shortText = tweets.size() + " " + context.getResources().getString(R.string.fav_user_tweets);
            longText = "";

            try {
                inbox.setBigContentTitle(shortText);
            } catch (Exception e) {

            }

            if (tweets.size() <= 5) {
                for (String[] s : tweets) {
                    inbox.addLine(Html.fromHtml("<b>" + s[0] + ":</b> " + s[1]));
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    inbox.addLine(Html.fromHtml("<b>" + tweets.get(i)[0] + ":</b> " + tweets.get(i)[1]));
                }

                inbox.setSummaryText("+" + (tweets.size() - 5) + " " + context.getString(R.string.tweets));
            }

            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.drawer_user_dark);
        }

        NotificationCompat.Builder mBuilder;

        AppSettings settings = AppSettings.getInstance(context);

        if (shortText.contains("@" + settings.myScreenName)) {
            // return because there is a mention notification for this already
            return;
        }

        Intent deleteIntent = new Intent(context, NotificationDeleteReceiverOne.class);

        mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(TweetLinkUtils.removeColorHtml(shortText, settings))
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, 0))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (inbox == null) {
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(settings.addonTheme ? longText.replaceAll("FF8800", settings.accentColor) : longText)));
        } else {
            mBuilder.setStyle(inbox);
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

        if (settings.led)
            mBuilder.setLights(0xFFFFFF, 1000, 1000);

        if (settings.notifications) {

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);

            notificationManager.notify(2, mBuilder.build());

            // if we want to wake the screen on a new message
            if (settings.wakeScreen) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                final PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                wakeLock.acquire(5000);
            }

            // Pebble notification
            if(context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE).getBoolean("pebble_notification", false)) {
                sendAlertToPebble(context, title, shortText);
            }

            // Light Flow notification
            sendToLightFlow(context, title, shortText);
        }
    }

    public static Bitmap getImage(Context context, String screenname) {
        BitmapLruCache mCache = App.getInstance(context).getBitmapCache();
        String url;
        try {
            url = Utils.getTwitter(context, AppSettings.getInstance(context)).showUser(screenname).getBiggerProfileImageURL();
            CacheableBitmapDrawable wrapper = mCache.get(url + "_notification");

            if (wrapper == null) {

                // The bitmap isn't cached so download from the web
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                InputStream is = new BufferedInputStream(conn.getInputStream());

                Bitmap image = BitmapFactory.decodeStream(is);
                image = ImageUtils.notificationResize(context, image);

                mCache.put(url + "_notification", image);
                return image;
            } else {
                return wrapper.getBitmap();
            }
        } catch (Exception e) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.drawer_user_dark);
        }
    }

    public static void notifySecondDMs(Context context, int secondAccount) {
        DMDataSource data = DMDataSource.getInstance(context);

        SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        int numberNew = sharedPrefs.getInt("dm_unread_" + secondAccount, 0);

        int smallIcon = R.drawable.ic_stat_icon;
        Bitmap largeIcon;

        Intent resultIntent = new Intent(context, SwitchAccountsRedirect.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0 );

        NotificationCompat.Builder mBuilder;

        String title = context.getResources().getString(R.string.app_name) + " - " + context.getResources().getString(R.string.sec_acc);
        String name;
        String message;
        String messageLong;

        NotificationCompat.InboxStyle inbox = null;
        if (numberNew == 1) {
            name = data.getNewestName(secondAccount);

            // if they are muted, and you don't want them to show muted mentions
            // then just quit
            if (sharedPrefs.getString("muted_users", "").contains(name) &&
                    !sharedPrefs.getBoolean("show_muted_mentions", false)) {
                return;
            }

            message = context.getResources().getString(R.string.mentioned_by) + " @" + name;
            messageLong = "<b>@" + name + "</b>: " + data.getNewestMessage(secondAccount);
            largeIcon = getImage(context, name);
        } else { // more than one dm
            message = numberNew + " " + context.getResources().getString(R.string.new_mentions);
            messageLong = "<b>" + context.getResources().getString(R.string.mentions) + "</b>: " + numberNew + " " + context.getResources().getString(R.string.new_mentions);
            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.drawer_user_dark);

            inbox = getDMInboxStyle(numberNew, secondAccount, context, message);
        }

        Intent markRead = new Intent(context, MarkReadSecondAccService.class);
        PendingIntent readPending = PendingIntent.getService(context, 0, markRead, 0);

        AppSettings settings = AppSettings.getInstance(context);

        Intent deleteIntent = new Intent(context, NotificationDeleteReceiverTwo.class);

        mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(TweetLinkUtils.removeColorHtml(message, settings))
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                .setContentIntent(resultPendingIntent)
                .setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, 0))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (inbox == null) {
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(settings.addonTheme ? messageLong.replaceAll("FF8800", settings.accentColor) : messageLong)));
        } else {
            mBuilder.setStyle(inbox);
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

        if (settings.led)
            mBuilder.setLights(0xFFFFFF, 1000, 1000);

        if (settings.notifications) {

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);

            notificationManager.notify(9, mBuilder.build());

            // if we want to wake the screen on a new message
            if (settings.wakeScreen) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                final PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                wakeLock.acquire(5000);
            }

            // Pebble notification
            if(sharedPrefs.getBoolean("pebble_notification", false)) {
                sendAlertToPebble(context, title, messageLong);
            }

            // Light Flow notification
            sendToLightFlow(context, title, messageLong);
        }
    }

    public static void notifySecondMentions(Context context, int secondAccount) {
        MentionsDataSource data = MentionsDataSource.getInstance(context);
        int numberNew = data.getUnreadCount(secondAccount);

        int smallIcon = R.drawable.ic_stat_icon;
        Bitmap largeIcon;

        Intent resultIntent = new Intent(context, SwitchAccountsRedirect.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0 );

        NotificationCompat.Builder mBuilder;

        String title = context.getResources().getString(R.string.app_name) + " - " + context.getResources().getString(R.string.sec_acc);;
        String name = null;
        String message;
        String messageLong;

        String tweetText = null;
        NotificationCompat.Action replyAction = null;
        if (numberNew == 1) {
            name = data.getNewestName(secondAccount);

            SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
            // if they are muted, and you don't want them to show muted mentions
            // then just quit
            if (sharedPrefs.getString("muted_users", "").contains(name) &&
                    !sharedPrefs.getBoolean("show_muted_mentions", false)) {
                return;
            }

            message = context.getResources().getString(R.string.mentioned_by) + " @" + name;
            tweetText = data.getNewestMessage(secondAccount);
            messageLong = "<b>@" + name + "</b>: " + tweetText;
            largeIcon = getImage(context, name);

            Intent reply = new Intent(context, NotificationComposeSecondAcc.class);

            sharedPrefs.edit().putString("from_notification_second", "@" + name).commit();
            long id = data.getLastIds(secondAccount)[0];
            PendingIntent replyPending = PendingIntent.getActivity(context, 0, reply, 0);
            sharedPrefs.edit().putLong("from_notification_long_second", id).commit();
            sharedPrefs.edit().putString("from_notification_text_second", "@" + name + ": " + TweetLinkUtils.removeColorHtml(tweetText, AppSettings.getInstance(context))).commit();

            // Create the remote input
            RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                    .setLabel("@" + name + " ")
                    .build();

            // Create the notification action
            replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_action_reply_dark,
                    context.getResources().getString(R.string.noti_reply), replyPending)
                    .addRemoteInput(remoteInput)
                    .build();

        } else { // more than one mention
            message = numberNew + " " + context.getResources().getString(R.string.new_mentions);
            messageLong = "<b>" + context.getResources().getString(R.string.mentions) + "</b>: " + numberNew + " " + context.getResources().getString(R.string.new_mentions);
            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.drawer_user_dark);
        }

        Intent markRead = new Intent(context, MarkReadSecondAccService.class);
        PendingIntent readPending = PendingIntent.getService(context, 0, markRead, 0);

        AppSettings settings = AppSettings.getInstance(context);

        Intent deleteIntent = new Intent(context, NotificationDeleteReceiverTwo.class);

        mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(TweetLinkUtils.removeColorHtml(message, settings))
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, 0))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (numberNew == 1) {
            mBuilder.addAction(replyAction);
            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(Html.fromHtml(settings.addonTheme ? messageLong.replaceAll("FF8800", settings.accentColor) : messageLong)));
        } else {
            NotificationCompat.InboxStyle inbox = getMentionsInboxStyle(numberNew,
                    secondAccount,
                    context,
                    TweetLinkUtils.removeColorHtml(message, settings));

            mBuilder.setStyle(inbox);
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

        if (settings.led)
            mBuilder.setLights(0xFFFFFF, 1000, 1000);

        if (settings.notifications) {

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);

            notificationManager.notify(9, mBuilder.build());

            // if we want to wake the screen on a new message
            if (settings.wakeScreen) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                final PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                wakeLock.acquire(5000);
            }

            // Pebble notification
            if(context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE).getBoolean("pebble_notification", false)) {
                sendAlertToPebble(context, title, messageLong);
            }

            // Light Flow notification
            sendToLightFlow(context, title, messageLong);
        }
    }

    private static NotificationCompat.InboxStyle getMentionsInboxStyle(int numberNew, int accountNumber, Context context, String title) {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        Cursor cursor = MentionsDataSource.getInstance(context).getCursor(accountNumber);
        if (!cursor.moveToLast()) {
            return style;
        }

        AppSettings settings = AppSettings.getInstance(context);

        if (numberNew > 5) {
            if (numberNew - 5 == 1) {
                style.setSummaryText("+" + (numberNew - 5) + " " + context.getString(R.string.new_mention));
            } else {
                style.setSummaryText("+" + (numberNew - 5) + " " + context.getString(R.string.new_mentions));
            }

            for (int i = 0; i < 5; i++) {
                String handle = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_SCREEN_NAME));
                String text = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TEXT));
                String longText = "<b>@" + handle + "</b>: " + text;

                style.addLine(Html.fromHtml(settings.addonTheme ? longText.replaceAll("FF8800", settings.accentColor) : longText));

                cursor.moveToPrevious();
            }
        } else {

            for (int i = 0; i <numberNew; i++) {
                String handle = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_SCREEN_NAME));
                String text = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TEXT));
                String longText = "<b>@" + handle + "</b>: " + text;

                style.addLine(Html.fromHtml(settings.addonTheme ? longText.replaceAll("FF8800", settings.accentColor) : longText));

                cursor.moveToPrevious();
            }
        }

        style.setBigContentTitle(title);

        return style;
    }

    private static NotificationCompat.InboxStyle getDMInboxStyle(int numberNew, int accountNumber, Context context, String title) {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        Cursor cursor = DMDataSource.getInstance(context).getCursor(accountNumber);
        if (!cursor.moveToLast()) {
            return style;
        }

        AppSettings settings = AppSettings.getInstance(context);

        if (numberNew > 5) {
            if (numberNew - 5 == 1) {
                style.setSummaryText("+" + (numberNew - 5) + " " + context.getString(R.string.new_direct_message));
            } else {
                style.setSummaryText("+" + (numberNew - 5) + " " + context.getString(R.string.new_direct_messages));
            }

            for (int i = 0; i < 5; i++) {
                String handle = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_SCREEN_NAME));
                String text = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TEXT));
                String longText = "<b>@" + handle + "</b>: " + text;

                style.addLine(Html.fromHtml(settings.addonTheme ? longText.replaceAll("FF8800", settings.accentColor) : longText));

                cursor.moveToPrevious();
            }
        } else {

            for (int i = 0; i <numberNew; i++) {
                String handle = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_SCREEN_NAME));
                String text = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TEXT));
                String longText = "<b>@" + handle + "</b>: " + text;

                style.addLine(Html.fromHtml(settings.addonTheme ? longText.replaceAll("FF8800", settings.accentColor) : longText));

                cursor.moveToPrevious();
            }
        }

        style.setBigContentTitle(title);

        return style;
    }

    // type is either " retweeted your status", " favorited your status", or " followed you"
    public static void newInteractions(User interactor, Context context, SharedPreferences sharedPrefs, String type) {
        String title = "";
        String text = "";
        String smallText = "";
        Bitmap icon = null;

        AppSettings settings = AppSettings.getInstance(context);

        Intent resultIntent = new Intent(context, RedirectToDrawer.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0 );

        int newFollowers = sharedPrefs.getInt("new_followers", 0);
        int newRetweets = sharedPrefs.getInt("new_retweets", 0);
        int newFavorites = sharedPrefs.getInt("new_favorites", 0);
        int newQuotes = sharedPrefs.getInt("new_quotes", 0);

        // set title
        if (newFavorites + newRetweets + newFollowers > 1) {
            title = context.getResources().getString(R.string.new_interactions);
        } else {
            title = context.getResources().getString(R.string.new_interaction_upper);
        }

        // set text
        String currText = sharedPrefs.getString("old_interaction_text", "");
        if (!currText.equals("")) {
            currText += "<br>";
        }
        if(settings.displayScreenName) {
            text = currText + "<b>" + interactor.getScreenName() + "</b> " + type;
        } else {
            text = currText + "<b>" + interactor.getName() + "</b> " + type;
        }
        sharedPrefs.edit().putString("old_interaction_text", text).commit();

        // set icon
        int types = 0;
        if (newFavorites > 0) {
            types++;
        }
        if(newFollowers > 0) {
            types++;
        }
        if (newRetweets > 0) {
            types++;
        }
        if (newQuotes > 0) {
            types++;
        }

        if (types > 1) {
            icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_icon);
        } else {
            if (newFavorites > 0) {
                icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_important_dark);
            } else if (newRetweets > 0) {
                icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_repeat_dark);
            } else {
                icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.drawer_user_dark);
            }
        }

        // set shorter text
        int total = newFavorites + newFollowers + newRetweets + newQuotes;
        if (total > 1) {
            smallText = total + " " + context.getResources().getString(R.string.new_interactions_lower);
        } else {
            smallText = text;
        }

        Intent markRead = new Intent(context, ReadInteractionsService.class);
        PendingIntent readPending = PendingIntent.getService(context, 0, markRead, 0);

        Intent deleteIntent = new Intent(context, NotificationDeleteReceiverOne.class);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(Html.fromHtml(settings.addonTheme ? smallText.replaceAll("FF8800", settings.accentColor) : smallText))
                .setSmallIcon(R.drawable.ic_stat_icon)
                .setLargeIcon(icon)
                .setContentIntent(resultPendingIntent)
                .setTicker(title)
                .setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, 0))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if(context.getResources().getBoolean(R.bool.expNotifications)) {
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(settings.addonTheme ? text.replaceAll("FF8800", settings.accentColor) : text)));
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

        if (settings.led)
            mBuilder.setLights(0xFFFFFF, 1000, 1000);

        if (settings.notifications) {

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);

            notificationManager.notify(4, mBuilder.build());

            // if we want to wake the screen on a new message
            if (settings.wakeScreen) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                final PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                wakeLock.acquire(5000);
            }

            // Pebble notification
            if(sharedPrefs.getBoolean("pebble_notification", false)) {
                sendAlertToPebble(context, title, text);
            }

            // Light Flow notification
            sendToLightFlow(context, title, text);
        }
    }


    public static void sendAlertToPebble(Context context, String title, String body) {
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

        final Map data = new HashMap();
        data.put("title", TweetLinkUtils.removeColorHtml(title.replaceAll("<b>", "").replaceAll("</b>", ""), AppSettings.getInstance(context)));
        data.put("body", TweetLinkUtils.removeColorHtml(body.replaceAll("<b>", "").replaceAll("</b>", ""), AppSettings.getInstance(context)));
        final JSONObject jsonData = new JSONObject(data);
        final String notificationData = new JSONArray().put(jsonData).toString();

        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", "talon_for_twitter");
        i.putExtra("notificationData", notificationData);

        Log.v("talon_pebble", "About to send a modal alert to Pebble: " + notificationData);
        context.sendBroadcast(i);
    }

    public static void sendToLightFlow(Context context, String title, String message) {
        Intent data = new Intent("com.klinker.android.twitter.NEW_NOTIFICATION");
        data.putExtra("title", TweetLinkUtils.removeColorHtml(title.replaceAll("<b>", "").replaceAll("</b>", ""), AppSettings.getInstance(context)));
        data.putExtra("message", TweetLinkUtils.removeColorHtml(message.replaceAll("<b>", "").replaceAll("</b>", ""), AppSettings.getInstance(context)));

        context.sendBroadcast(data);
    }

    public static final boolean TEST_NOTIFICATION = false;

    public static void sendTestNotification(Context context) {

        if (!TEST_NOTIFICATION) {
            return;
        }

        AppSettings settings = AppSettings.getInstance(context);

        SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

            Intent markRead = new Intent(context, MarkReadService.class);
            PendingIntent readPending = PendingIntent.getService(context, 0, markRead, 0);

            String shortText = "Test Talon";
            String longText = "Here is a test for Talon's notifications";

            Intent resultIntent = new Intent(context, RedirectToMentions.class);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0 );

            NotificationCompat.Builder mBuilder;

            Intent deleteIntent = new Intent(context, NotificationDeleteReceiverOne.class);

            mBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(shortText)
                    .setContentText(longText)
                    .setSmallIcon(R.drawable.ic_stat_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setTicker(shortText)
                    .setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, 0))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            // Pebble notification
            if(sharedPrefs.getBoolean("pebble_notification", false)) {
                sendAlertToPebble(context, shortText, shortText);
            }

            // Light Flow notification
            sendToLightFlow(context, shortText, shortText);

            if (settings.vibrate) {
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            }

            if (settings.sound) {
                try {
                    mBuilder.setSound(Uri.parse(settings.ringtone));
                } catch (Exception e) {
                    e.printStackTrace();
                    mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                }
            }

            if (settings.led)
                mBuilder.setLights(0xFFFFFF, 1000, 1000);

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);

            Intent reply = new Intent(context, NotificationCompose.class);
            MentionsDataSource data = MentionsDataSource.getInstance(context);
            PendingIntent replyPending = PendingIntent.getActivity(context, 0, reply, 0);

            RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                    .setLabel("@" + "lukeklinker" + " ")
                    .build();

            // Create the notification action
            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_action_reply_dark,
                    context.getResources().getString(R.string.noti_reply), replyPending)
                    .addRemoteInput(remoteInput)
                    .build();

            NotificationCompat.Action.Builder action = new NotificationCompat.Action.Builder(
                    R.drawable.ic_action_read_dark,
                    context.getResources().getString(R.string.mark_read), readPending);

            mBuilder.addAction(replyAction);
            mBuilder.addAction(action.build());


            // Build the notification and issues it with notification manager.
            notificationManager.notify(1, mBuilder.build());

            // if we want to wake the screen on a new message
            if (settings.wakeScreen) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                final PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                wakeLock.acquire(5000);
            }
    }
}
