package com.klinker.android.twitter.data.sq_lite;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.util.Log;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.utils.FavoriterUtils;
import com.klinker.android.twitter.utils.TweetLinkUtils;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActivityDataSource {

    public static final int TYPE_MENTION = 0;
    public static final int TYPE_NEW_FOLLOWER = 1;
    public static final int TYPE_RETWEETS = 2;
    public static final int TYPE_FAVORITES = 3;

    // provides access to the database
    public static ActivityDataSource dataSource = null;

    public static ActivityDataSource getInstance(Context context) {
        // if the datasource isn't open or it the object is null
        if (dataSource == null ||
                dataSource.getDatabase() == null ||
                !dataSource.getDatabase().isOpen()) {

            dataSource = new ActivityDataSource(context); // create the database
            dataSource.open(); // open the database
        }

        return dataSource;
    }

    // Database fields
    private SQLiteDatabase database;
    private ActivitySQLiteHelper dbHelper;
    private SharedPreferences sharedPrefs;
    private Context context;
    
    public String[] allColumns = {
            ActivitySQLiteHelper.COLUMN_ID, ActivitySQLiteHelper.COLUMN_TITLE, ActivitySQLiteHelper.COLUMN_TWEET_ID, ActivitySQLiteHelper.COLUMN_ACCOUNT, ActivitySQLiteHelper.COLUMN_TYPE,
            ActivitySQLiteHelper.COLUMN_TEXT, ActivitySQLiteHelper.COLUMN_NAME, ActivitySQLiteHelper.COLUMN_PRO_PIC,
            ActivitySQLiteHelper.COLUMN_SCREEN_NAME, ActivitySQLiteHelper.COLUMN_TIME, ActivitySQLiteHelper.COLUMN_PIC_URL,
            ActivitySQLiteHelper.COLUMN_RETWEETER, ActivitySQLiteHelper.COLUMN_URL, HomeSQLiteHelper.COLUMN_USERS, HomeSQLiteHelper.COLUMN_HASHTAGS, ActivitySQLiteHelper.COLUMN_ANIMATED_GIF,
            ActivitySQLiteHelper.COLUMN_CONVERSATION, ActivitySQLiteHelper.COLUMN_FAV_COUNT, ActivitySQLiteHelper.COLUMN_RETWEET_COUNT };

    public ActivityDataSource(Context context) {
        dbHelper = new ActivitySQLiteHelper(context);
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        this.context = context;
    }

    public void open() throws SQLException {

        try {
            database = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            close();
        }
    }

    public void close() {
        try {
            dbHelper.close();
        } catch (Exception e) {

        }
        database = null;
        dataSource = null;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public ActivitySQLiteHelper getHelper() {
        return dbHelper;
    }

    public synchronized String insertMention(Status status, int account) {
        ContentValues values = getMentionValues(status, account);

        try {
            database.insert(ActivitySQLiteHelper.TABLE_ACTIVITY, null, values);
        } catch (Exception e) {
            open();
            database.insert(ActivitySQLiteHelper.TABLE_ACTIVITY, null, values);
        }

        return "<b>" + values.getAsString(ActivitySQLiteHelper.COLUMN_TITLE) + ":</b> " + values.getAsString(ActivitySQLiteHelper.COLUMN_TEXT);
    }

    public synchronized List<String> insertMentions(List<Status> statuses, int account) {

        ContentValues[] valueses = new ContentValues[statuses.size()];

        for (int i = 0; i < statuses.size(); i++) {
            ContentValues values = getMentionValues(statuses.get(i), account);
            valueses[i] = values;
        }

        insertMultiple(valueses);

        List<String> list = new ArrayList<String>();
        for (ContentValues v : valueses) {
            list.add("<b>" + v.getAsString(ActivitySQLiteHelper.COLUMN_TITLE) + ":</b> " + v.getAsString(ActivitySQLiteHelper.COLUMN_TEXT));
        }
        return list;
    }

    public synchronized String insertRetweeters(Status status, int account, Twitter twitter) {
        int retweetCountInDb = retweetExists(status.getId(), account);
        if (retweetCountInDb != -1 && retweetCountInDb < status.getRetweetCount()) {
            // we want to update the current
            ContentValues values = getRetweeterContentValues(status, account, twitter);
            if (values != null) {
                try {
                    database.update(
                            ActivitySQLiteHelper.TABLE_ACTIVITY,
                            values,
                            ActivitySQLiteHelper.COLUMN_TWEET_ID + " = ? AND " + ActivitySQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + ActivitySQLiteHelper.COLUMN_TYPE + " = ?",
                            new String[]{status.getId() + "", account + "", TYPE_RETWEETS + ""}
                    );
                } catch (Exception e) {
                    open();
                    database.update(
                            ActivitySQLiteHelper.TABLE_ACTIVITY,
                            values,
                            ActivitySQLiteHelper.COLUMN_TWEET_ID + " = ? AND " + ActivitySQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + ActivitySQLiteHelper.COLUMN_TYPE + " = ?",
                            new String[]{status.getId() + "", account + "", TYPE_RETWEETS + ""}
                    );
                }
                return addBoldToTitle(values.getAsString(ActivitySQLiteHelper.COLUMN_TEXT));
            } else {
                return null;
            }
        } else if (status.getRetweetCount() > 0 && retweetCountInDb == -1) {
            ContentValues values = getRetweeterContentValues(status, account, twitter);
            if (values != null) {
                try {
                    database.insert(ActivitySQLiteHelper.TABLE_ACTIVITY, null, values);
                } catch (Exception e) {
                    open();
                    database.insert(ActivitySQLiteHelper.TABLE_ACTIVITY, null, values);
                }
                return addBoldToTitle(values.getAsString(ActivitySQLiteHelper.COLUMN_TEXT));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public String addBoldToTitle(String s) {
        int index = s.indexOf(":");
        return "<b>" + s.substring(0, index) + "</b>" + s.substring(index + 1, s.length());
    }

    public synchronized String insertFavoriters(Status status, int account) {
        int favCountInDb = favoriteExists(status.getId(), account);

        if (favCountInDb != -1 && favCountInDb < status.getFavoriteCount()) {
            // we want to update the current
            ContentValues values = getFavoriteValues(status, account);
            try {
                database.update(
                        ActivitySQLiteHelper.TABLE_ACTIVITY,
                        values,
                        ActivitySQLiteHelper.COLUMN_TWEET_ID + " = ? AND " + ActivitySQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + ActivitySQLiteHelper.COLUMN_TYPE + " = ?",
                        new String[]{status.getId() + "", account + "", TYPE_FAVORITES + ""}
                );
            } catch (Exception e) {
                open();
                database.update(
                        ActivitySQLiteHelper.TABLE_ACTIVITY,
                        values,
                        ActivitySQLiteHelper.COLUMN_TWEET_ID + " = ? AND " + ActivitySQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + ActivitySQLiteHelper.COLUMN_TYPE + " = ?",
                        new String[]{status.getId() + "", account + "", TYPE_FAVORITES + ""}
                );
            }

            return addBoldToTitle(values.getAsString(ActivitySQLiteHelper.COLUMN_TEXT));
        } else if (status.getFavoriteCount() > 0 && favCountInDb == -1) {
            // it isn't in the database yet
            ContentValues values = getFavoriteValues(status, account);
            try {
                database.insert(ActivitySQLiteHelper.TABLE_ACTIVITY, null, values);
            } catch (Exception e) {
                open();
                database.insert(ActivitySQLiteHelper.TABLE_ACTIVITY, null, values);
            }

            return addBoldToTitle(values.getAsString(ActivitySQLiteHelper.COLUMN_TEXT));
        } else {
            return null;
        }
    }

    public synchronized String insertNewFollowers(List<User> users, int account) {
        ContentValues values = getNewFollowerValues(users, account);

        try {
            database.insert(ActivitySQLiteHelper.TABLE_ACTIVITY, null, values);
        } catch (Exception e) {
            open();
            database.insert(ActivitySQLiteHelper.TABLE_ACTIVITY, null, values);
        }

        return values.getAsString(ActivitySQLiteHelper.COLUMN_TEXT);
    }

    public ContentValues getMentionValues(Status status, int account) {
        ContentValues values = new ContentValues();
        String originalName = "";
        long id = status.getId();
        long time = status.getCreatedAt().getTime();

        String[] html = TweetLinkUtils.getLinksInStatus(status);
        String text = html[0];
        String media = html[1];
        String otherUrl = html[2];
        String hashtags = html[3];
        String users = html[4];

        if (media.contains("/tweet_video/")) {
            media = media.replace("tweet_video", "tweet_video_thumb").replace(".mp4", ".png");
        }

        values.put(ActivitySQLiteHelper.COLUMN_TITLE, "@" + status.getUser().getScreenName() + " " + context.getString(R.string.mentioned_you));
        values.put(ActivitySQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(ActivitySQLiteHelper.COLUMN_TEXT, text);
        values.put(ActivitySQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(ActivitySQLiteHelper.COLUMN_NAME, status.getUser().getName());
        values.put(ActivitySQLiteHelper.COLUMN_PRO_PIC, status.getUser().getOriginalProfileImageURL());
        values.put(ActivitySQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
        values.put(ActivitySQLiteHelper.COLUMN_TIME, time);
        values.put(ActivitySQLiteHelper.COLUMN_RETWEETER, originalName);
        values.put(ActivitySQLiteHelper.COLUMN_PIC_URL, media);
        values.put(ActivitySQLiteHelper.COLUMN_URL, otherUrl);
        values.put(ActivitySQLiteHelper.COLUMN_PIC_URL, media);
        values.put(ActivitySQLiteHelper.COLUMN_USERS, users);
        values.put(ActivitySQLiteHelper.COLUMN_HASHTAGS, hashtags);
        values.put(ActivitySQLiteHelper.COLUMN_TYPE, TYPE_MENTION);
        values.put(ActivitySQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, otherUrl));
        values.put(HomeSQLiteHelper.COLUMN_CONVERSATION, status.getInReplyToStatusId() == -1 ? 0 : 1);
        values.put(ActivitySQLiteHelper.COLUMN_FAV_COUNT, status.getFavoriteCount());
        values.put(ActivitySQLiteHelper.COLUMN_RETWEET_COUNT, status.getRetweetCount());

        return values;
    }

    public ContentValues getFavoriteValues(Status status, int account) {
        ContentValues values = new ContentValues();
        try {
            long id = status.getId();

            List<User> userList = (new FavoriterUtils()).getFavoriters(context, id);

            String[] html = TweetLinkUtils.getLinksInStatus(status);
            String text = html[0];
            String media = html[1];
            String otherUrl = html[2];
            String hashtags = html[3];
            String users = html[4];

            if (media.contains("/tweet_video/")) {
                media = media.replace("tweet_video", "tweet_video_thumb").replace(".mp4", ".png");
            }

            values.put(ActivitySQLiteHelper.COLUMN_TITLE, buildUsersTitle(userList));
            values.put(ActivitySQLiteHelper.COLUMN_ACCOUNT, account);
            values.put(ActivitySQLiteHelper.COLUMN_TEXT, status.getFavoriteCount() + " " +
                    (status.getFavoriteCount() == 1 ?
                            context.getString(R.string.favorite_lower) :
                            context.getString(R.string.favorites_lower)) +
                    ": " + text);
            values.put(ActivitySQLiteHelper.COLUMN_TWEET_ID, id);
            values.put(ActivitySQLiteHelper.COLUMN_NAME, status.getUser().getName());
            values.put(ActivitySQLiteHelper.COLUMN_PRO_PIC,buildProPicUrl(userList));
            values.put(ActivitySQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
            values.put(ActivitySQLiteHelper.COLUMN_TIME, Calendar.getInstance().getTimeInMillis());
            values.put(ActivitySQLiteHelper.COLUMN_PIC_URL, media);
            values.put(ActivitySQLiteHelper.COLUMN_URL, otherUrl);
            values.put(ActivitySQLiteHelper.COLUMN_PIC_URL, media);
            values.put(ActivitySQLiteHelper.COLUMN_USERS, users);
            values.put(ActivitySQLiteHelper.COLUMN_HASHTAGS, hashtags);
            values.put(ActivitySQLiteHelper.COLUMN_TYPE, TYPE_FAVORITES);
            values.put(ActivitySQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, otherUrl));
            values.put(HomeSQLiteHelper.COLUMN_CONVERSATION, status.getInReplyToStatusId() == -1 ? 0 : 1);
            values.put(ActivitySQLiteHelper.COLUMN_FAV_COUNT, status.getFavoriteCount());
            values.put(ActivitySQLiteHelper.COLUMN_RETWEET_COUNT, status.getRetweetCount());

            return values;
        } catch (Exception e) {
            return null;
        }
    }

    public ContentValues getRetweeterContentValues(Status status, int account, Twitter twitter) {

        try {
            List<Status> retweets = twitter.getRetweets(status.getId());
            List<User> users = new ArrayList<User>();

            for (Status s : retweets) {
                users.add(s.getUser());
            }

            ContentValues values = new ContentValues();

            if (retweets.size() > 0) {

                long id = status.getId();

                String[] html = TweetLinkUtils.getLinksInStatus(status);
                String text = html[0];
                String media = html[1];
                String otherUrl = html[2];
                String hashtags = html[3];
                String userString = html[4];

                if (media.contains("/tweet_video/")) {
                    media = media.replace("tweet_video", "tweet_video_thumb").replace(".mp4", ".png");
                }

                values.put(ActivitySQLiteHelper.COLUMN_TITLE, buildUsersTitle(users));
                values.put(ActivitySQLiteHelper.COLUMN_ACCOUNT, account);
                values.put(ActivitySQLiteHelper.COLUMN_TEXT, status.getRetweetCount() + " " +
                        (status.getRetweetCount() == 1 ?
                                context.getString(R.string.retweet) :
                                context.getString(R.string.retweets)) +
                        ": " + text);
                values.put(ActivitySQLiteHelper.COLUMN_TWEET_ID, status.getId());
                values.put(ActivitySQLiteHelper.COLUMN_NAME, status.getUser().getName());
                values.put(ActivitySQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
                values.put(ActivitySQLiteHelper.COLUMN_PRO_PIC, buildProPicUrl(users));
                values.put(ActivitySQLiteHelper.COLUMN_TIME, retweets.get(0).getCreatedAt().getTime());
                values.put(ActivitySQLiteHelper.COLUMN_PIC_URL, media);
                values.put(ActivitySQLiteHelper.COLUMN_TYPE, TYPE_RETWEETS);
                values.put(ActivitySQLiteHelper.COLUMN_FAV_COUNT, status.getFavoriteCount());
                values.put(ActivitySQLiteHelper.COLUMN_RETWEET_COUNT, status.getRetweetCount());
                values.put(ActivitySQLiteHelper.COLUMN_URL, otherUrl);
                values.put(ActivitySQLiteHelper.COLUMN_PIC_URL, media);
                values.put(ActivitySQLiteHelper.COLUMN_USERS, userString);
                values.put(ActivitySQLiteHelper.COLUMN_HASHTAGS, hashtags);
                values.put(ActivitySQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, otherUrl));
                values.put(HomeSQLiteHelper.COLUMN_CONVERSATION, status.getInReplyToStatusId() == -1 ? 0 : 1);
            } else {
                return null;
            }

            return values;
        } catch (TwitterException e) {
            return null;
        }

    }

    public ContentValues getNewFollowerValues(List<User> users, int account) {
        ContentValues values = new ContentValues();

        values.put(ActivitySQLiteHelper.COLUMN_TITLE, buildUsersTitle(users));
        values.put(ActivitySQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(ActivitySQLiteHelper.COLUMN_TEXT, users.size() + " " +
                (users.size() == 1 ?
                        context.getString(R.string.new_follower_lower) :
                        context.getString(R.string.new_followers_lower)));
        values.put(ActivitySQLiteHelper.COLUMN_PRO_PIC, buildProPicUrl(users));
        values.put(ActivitySQLiteHelper.COLUMN_TIME, Calendar.getInstance().getTimeInMillis());
        values.put(ActivitySQLiteHelper.COLUMN_USERS, buildUserList(users));
        values.put(ActivitySQLiteHelper.COLUMN_TYPE, TYPE_NEW_FOLLOWER);

        return values;
    }

    public String buildProPicUrl(List<User> users) {
        String s = users.get(0).getOriginalProfileImageURL();
        int count = 1;
        for (int i = 1; i < users.size(); i++) {
            s += " " + users.get(i).getOriginalProfileImageURL();
            count++;

            if (count == 4) {
                return s;
            }
        }
        return s;
    }

    public String buildUserList(List<User> users) {
        String s = "@" + users.get(0).getScreenName();
        for (User u : users) {
            if (!s.contains(u.getScreenName())) {
                s += " @" + u.getScreenName();
            }
        }
        return s;
    }

    private String buildUsersTitle(List<User> users) {
        String s = "";

        String and = context.getString(R.string.and);
        if (users.size() > 1) {
            s += "@" + users.get(0).getScreenName();
            for (int i = 1; i < users.size() - 1; i++) {
                s += ", @" + users.get(i).getScreenName();
            }
            s += ", " + and + " @" + users.get(users.size() - 1).getScreenName();
        } else {
            // size equals 1
            s = "@" + users.get(0).getScreenName();
        }

        return s;
    }

    private synchronized int insertMultiple(ContentValues[] allValues) {
        int rowsAdded = 0;
        long rowId;
        ContentValues values;

        if (database == null || !database.isOpen()) {
            open();
        }

        try {
            database.beginTransaction();

            for (ContentValues initialValues : allValues) {
                values = initialValues == null ? new ContentValues() : new ContentValues(initialValues);
                try {
                    rowId = database.insert(ActivitySQLiteHelper.TABLE_ACTIVITY, null, values);
                } catch (IllegalStateException e) {
                    return rowsAdded;
                }
                if (rowId > 0)
                    rowsAdded++;
            }

            database.setTransactionSuccessful();
        } catch (NullPointerException e)  {
            e.printStackTrace();
            return rowsAdded;
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
            return rowsAdded;
        } catch (IllegalStateException e) {
            // caught setting up the transaction I guess, shouldn't ever happen now.
            e.printStackTrace();
            return rowsAdded;
        } finally {
            try {
                database.endTransaction();
            } catch (Exception e) {
                // shouldn't happen unless it gets caught above from an illegal state
            }
        }

        return rowsAdded;
    }

    public synchronized Cursor getCursor(int account) {

        String where = ActivitySQLiteHelper.COLUMN_ACCOUNT + " = " + account;

        Cursor cursor;
        try {
            cursor = database.query(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    allColumns, where, null, null, null, ActivitySQLiteHelper.COLUMN_TIME + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    allColumns, where, null, null, null, ActivitySQLiteHelper.COLUMN_TIME + " ASC");
        }

        return cursor;
    }

    public synchronized long[] getLastIds(int account) {
        long[] ids = new long[] {0, 0};

        Cursor cursor;
        try {
            cursor = getCursor(account);
        } catch (Exception e) {
            return ids;
        }

        try {
            if (cursor.moveToLast()) {
                ids[0] = cursor.getLong(cursor.getColumnIndex(ActivitySQLiteHelper.COLUMN_TWEET_ID));
            }

            if (cursor.moveToPrevious()) {
                ids[1] = cursor.getLong(cursor.getColumnIndex(ActivitySQLiteHelper.COLUMN_TWEET_ID));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return ids;
    }

    public synchronized boolean tweetExists(long tweetId, int account) {
        Cursor cursor;
        try {
            cursor = database.query(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    allColumns, ActivitySQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " + ActivitySQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId, null, null, null, ActivitySQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    allColumns, ActivitySQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " + ActivitySQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId, null, null, null, ActivitySQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Find out if the tweet is in the database
     * @param tweetId
     * @param account
     * @return number of favorites on that tweet
     */
    public synchronized int favoriteExists(long tweetId, int account) {
        Cursor cursor;
        try {
            cursor = database.query(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    allColumns,
                    ActivitySQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " +
                            ActivitySQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId + " AND " +
                            ActivitySQLiteHelper.COLUMN_TYPE + " = " + TYPE_FAVORITES,
                    null,
                    null,
                    null,
                    ActivitySQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    allColumns,
                    ActivitySQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " +
                            ActivitySQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId + " AND " +
                            ActivitySQLiteHelper.COLUMN_TYPE + " = " + TYPE_FAVORITES,
                    null,
                    null,
                    null,
                    ActivitySQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(cursor.getColumnIndex(ActivitySQLiteHelper.COLUMN_FAV_COUNT));
            try {
                cursor.close();
            } catch (Exception e) { }
            return count;
        } else {
            try {
                cursor.close();
            } catch (Exception e) { }
            return -1;
        }
    }

    /**
     * Find out if the tweet is in the database
     * @param tweetId
     * @param account
     * @return number of retweets on that tweet
     */
    public synchronized int retweetExists(long tweetId, int account) {
        Cursor cursor;
        try {
            cursor = database.query(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    allColumns,
                    ActivitySQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " +
                            ActivitySQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId + " AND " +
                            ActivitySQLiteHelper.COLUMN_TYPE + " = " + TYPE_RETWEETS,
                    null,
                    null,
                    null,
                    ActivitySQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    allColumns,
                    ActivitySQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " +
                            ActivitySQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId + " AND " +
                            ActivitySQLiteHelper.COLUMN_TYPE + " = " + TYPE_RETWEETS,
                    null,
                    null,
                    null,
                    ActivitySQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(cursor.getColumnIndex(ActivitySQLiteHelper.COLUMN_RETWEET_COUNT));
            try {
                cursor.close();
            } catch (Exception e) { }
            return count;
        } else {
            try {
                cursor.close();
            } catch (Exception e) { }
            return -1;
        }
    }

    public synchronized void deleteDups(int account) {
        try {
            database.execSQL("DELETE FROM " + ActivitySQLiteHelper.TABLE_ACTIVITY + " WHERE _id NOT IN (SELECT MIN(_id) FROM " + ActivitySQLiteHelper.TABLE_ACTIVITY + " GROUP BY " + ActivitySQLiteHelper.COLUMN_TWEET_ID + ") AND " + ActivitySQLiteHelper.COLUMN_ACCOUNT + " = " + account);
        } catch (Exception e) {
            open();
            database.execSQL("DELETE FROM " + ActivitySQLiteHelper.TABLE_ACTIVITY + " WHERE _id NOT IN (SELECT MIN(_id) FROM " + ActivitySQLiteHelper.TABLE_ACTIVITY + " GROUP BY " + ActivitySQLiteHelper.COLUMN_TWEET_ID + ") AND " + ActivitySQLiteHelper.COLUMN_ACCOUNT + " = " + account);
        }
    }

    public synchronized void deleteItem(long id) {
        try {
            database.delete(HomeSQLiteHelper.TABLE_HOME, ActivitySQLiteHelper.COLUMN_ID
                    + " = ?", new String[] { id + "" });
        } catch (Exception e) {
            open();
            database.delete(HomeSQLiteHelper.TABLE_HOME, ActivitySQLiteHelper.COLUMN_ID
                    + " = ?", new String[] { id + "" });
        }
    }


    public synchronized void deleteAll(int account) {
        try {
            database.delete(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        } catch (Exception e) {
            open();
            database.delete(ActivitySQLiteHelper.TABLE_ACTIVITY,
                    HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        }
    }
}
