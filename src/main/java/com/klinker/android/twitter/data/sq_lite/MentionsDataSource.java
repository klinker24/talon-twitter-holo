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

package com.klinker.android.twitter.data.sq_lite;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.preference.PreferenceManager;

import android.text.TextUtils;
import com.klinker.android.twitter.utils.TweetLinkUtils;

import java.util.List;

import twitter4j.Status;

public class MentionsDataSource {

    // provides access to the database
    public static MentionsDataSource dataSource = null;

    /*

    This is used so that we don't have to open and close the database on different threads or fragments
    every time. This will facilitate it between all of them to avoid Illegal State Exceptions.

     */
    public static MentionsDataSource getInstance(Context context) {

        // if the datasource isn't open or it the object is null
        if (dataSource == null ||
                dataSource.getDatabase() == null ||
                !dataSource.getDatabase().isOpen()) {

            dataSource = new MentionsDataSource(context); // create the database
            dataSource.open(); // open the database
        }

        return dataSource;
    }

    // Database fields
    private SQLiteDatabase database;
    private MentionsSQLiteHelper dbHelper;
    private SharedPreferences sharedPrefs;
    public String[] allColumns = {MentionsSQLiteHelper.COLUMN_ID, MentionsSQLiteHelper.COLUMN_UNREAD, MentionsSQLiteHelper.COLUMN_TWEET_ID, MentionsSQLiteHelper.COLUMN_ACCOUNT, MentionsSQLiteHelper.COLUMN_TYPE,
            MentionsSQLiteHelper.COLUMN_TEXT, MentionsSQLiteHelper.COLUMN_NAME, MentionsSQLiteHelper.COLUMN_PRO_PIC,
            MentionsSQLiteHelper.COLUMN_SCREEN_NAME, MentionsSQLiteHelper.COLUMN_TIME, MentionsSQLiteHelper.COLUMN_PIC_URL,
            MentionsSQLiteHelper.COLUMN_RETWEETER, MentionsSQLiteHelper.COLUMN_URL, HomeSQLiteHelper.COLUMN_USERS, HomeSQLiteHelper.COLUMN_HASHTAGS, MentionsSQLiteHelper.COLUMN_ANIMATED_GIF,
            MentionsSQLiteHelper.COLUMN_CONVERSATION};

    public MentionsDataSource(Context context) {
        dbHelper = new MentionsSQLiteHelper(context);
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
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

    public MentionsSQLiteHelper getHelper() {
        return dbHelper;
    }

    public synchronized void createTweet(Status status, int account, boolean initial) {
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

        values.put(MentionsSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(MentionsSQLiteHelper.COLUMN_TEXT, text);
        values.put(MentionsSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(MentionsSQLiteHelper.COLUMN_NAME, status.getUser().getName());
        values.put(MentionsSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getOriginalProfileImageURL());
        values.put(MentionsSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
        values.put(MentionsSQLiteHelper.COLUMN_TIME, time);
        values.put(MentionsSQLiteHelper.COLUMN_RETWEETER, originalName);
        values.put(MentionsSQLiteHelper.COLUMN_UNREAD, 0);
        values.put(MentionsSQLiteHelper.COLUMN_PIC_URL, media);
        values.put(MentionsSQLiteHelper.COLUMN_URL, otherUrl);
        values.put(MentionsSQLiteHelper.COLUMN_PIC_URL, media);
        values.put(MentionsSQLiteHelper.COLUMN_USERS, users);
        values.put(MentionsSQLiteHelper.COLUMN_HASHTAGS, hashtags);
        values.put(MentionsSQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, otherUrl));
        values.put(HomeSQLiteHelper.COLUMN_CONVERSATION, status.getInReplyToScreenName() == null ? 0 : 1);

        try {
            database.insert(MentionsSQLiteHelper.TABLE_MENTIONS, null, values);
        } catch (Exception e) {
            open();
            database.insert(MentionsSQLiteHelper.TABLE_MENTIONS, null, values);
        }
    }

    public synchronized void createTweet(Status status, int account) {
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

        values.put(MentionsSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(MentionsSQLiteHelper.COLUMN_TEXT, text);
        values.put(MentionsSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(MentionsSQLiteHelper.COLUMN_NAME, status.getUser().getName());
        values.put(MentionsSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getOriginalProfileImageURL());
        values.put(MentionsSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
        values.put(MentionsSQLiteHelper.COLUMN_TIME, time);
        values.put(MentionsSQLiteHelper.COLUMN_RETWEETER, originalName);
        values.put(MentionsSQLiteHelper.COLUMN_UNREAD, 1);
        values.put(MentionsSQLiteHelper.COLUMN_PIC_URL, media);
        values.put(MentionsSQLiteHelper.COLUMN_URL, otherUrl);
        values.put(MentionsSQLiteHelper.COLUMN_USERS, users);
        values.put(MentionsSQLiteHelper.COLUMN_HASHTAGS, hashtags);
        values.put(MentionsSQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, otherUrl));
        values.put(HomeSQLiteHelper.COLUMN_CONVERSATION, status.getInReplyToScreenName() == null ? 0 : 1);

        try {
            database.insert(MentionsSQLiteHelper.TABLE_MENTIONS, null, values);
        } catch (Exception e) {
            open();
            database.insert(MentionsSQLiteHelper.TABLE_MENTIONS, null, values);
        }
    }

    public synchronized int insertTweets(List<Status> statuses, int account) {

        ContentValues[] valueses = new ContentValues[statuses.size()];

        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);

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

            values.put(MentionsSQLiteHelper.COLUMN_ACCOUNT, account);
            values.put(MentionsSQLiteHelper.COLUMN_TEXT, text);
            values.put(MentionsSQLiteHelper.COLUMN_TWEET_ID, id);
            values.put(MentionsSQLiteHelper.COLUMN_NAME, status.getUser().getName());
            values.put(MentionsSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getOriginalProfileImageURL());
            values.put(MentionsSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
            values.put(MentionsSQLiteHelper.COLUMN_TIME, time);
            values.put(MentionsSQLiteHelper.COLUMN_RETWEETER, originalName);
            values.put(MentionsSQLiteHelper.COLUMN_UNREAD, 1);
            values.put(MentionsSQLiteHelper.COLUMN_PIC_URL, media);
            values.put(MentionsSQLiteHelper.COLUMN_URL, otherUrl);
            values.put(MentionsSQLiteHelper.COLUMN_USERS, users);
            values.put(MentionsSQLiteHelper.COLUMN_HASHTAGS, hashtags);
            values.put(MentionsSQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, otherUrl));
            values.put(HomeSQLiteHelper.COLUMN_CONVERSATION, status.getInReplyToScreenName() == null ? 0 : 1);

            valueses[i] = values;
        }

        return insertMultiple(valueses);
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
                    rowId = database.insert(MentionsSQLiteHelper.TABLE_MENTIONS, null, values);
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

    public synchronized void deleteTweet(long tweetId) {
        long id = tweetId;

        try {
            database.delete(MentionsSQLiteHelper.TABLE_MENTIONS, MentionsSQLiteHelper.COLUMN_TWEET_ID
                    + " = " + id, null);
        } catch (Exception e) {
            open();
            database.delete(MentionsSQLiteHelper.TABLE_MENTIONS, MentionsSQLiteHelper.COLUMN_TWEET_ID
                    + " = " + id, null);
        }
    }

    public synchronized void deleteAllTweets(int account) {

        try {
            database.delete(MentionsSQLiteHelper.TABLE_MENTIONS,
                    MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        } catch (Exception e) {
            open();
            database.delete(MentionsSQLiteHelper.TABLE_MENTIONS,
                    MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        }
    }

    public synchronized Cursor getCursor(int account) {

        boolean mutedMentions = sharedPrefs.getBoolean("show_muted_mentions", false);
        String users = sharedPrefs.getString("muted_users", "");
        String hashtags = sharedPrefs.getString("muted_hashtags", "");
        String expressions = sharedPrefs.getString("muted_regex", "");

        expressions = expressions.replaceAll("'", "''");

        String where = MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account;

        if (!users.equals("") && !mutedMentions) {
            String[] split = users.split(" ");
            for (String s : split) {
                where += " AND " + MentionsSQLiteHelper.COLUMN_SCREEN_NAME + " NOT LIKE '" + s + "'";
            }
        }

        if (!hashtags.equals("")) {
            String[] split = hashtags.split(" ");
            for (String s : split) {
                where += " AND " + MentionsSQLiteHelper.COLUMN_HASHTAGS + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!expressions.equals("")) {
            String[] split = expressions.split("   ");
            for (String s : split) {
                where += " AND " + MentionsSQLiteHelper.COLUMN_TEXT + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        Cursor cursor;
        try {
            cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                    allColumns, where, null, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                    allColumns, where, null, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        return cursor;
    }

    public synchronized Cursor getTrimmingCursor(int account) {

        String where = MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account;

        Cursor cursor;
        try {
            cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                    allColumns, where, null, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                    allColumns, where, null, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        return cursor;
    }

    public synchronized Cursor getUnreadCursor(int account) {

        boolean mutedMentions = sharedPrefs.getBoolean("show_muted_mentions", false);
        String users = sharedPrefs.getString("muted_users", "");
        String hashtags = sharedPrefs.getString("muted_hashtags", "");
        String expressions = sharedPrefs.getString("muted_regex", "");

        expressions = expressions.replaceAll("'", "''");

        String where = MentionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + MentionsSQLiteHelper.COLUMN_UNREAD + " = ?";

        if (!users.equals("") && !mutedMentions) {
            String[] split = users.split(" ");
            for (String s : split) {
                where += " AND " + MentionsSQLiteHelper.COLUMN_SCREEN_NAME + " NOT LIKE '" + s + "'";
            }
        }

        if (!hashtags.equals("")) {
            String[] split = hashtags.split(" ");
            for (String s : split) {
                where += " AND " + MentionsSQLiteHelper.COLUMN_HASHTAGS + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!expressions.equals("")) {
            String[] split = expressions.split("   ");
            for (String s : split) {
                where += " AND " + MentionsSQLiteHelper.COLUMN_TEXT + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        Cursor cursor;
        try {
            cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                    allColumns, where, new String[] {account + "", "1"}, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                    allColumns, where, new String[] {account + "", "1"}, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        return cursor;
    }

    public synchronized int getUnreadCount(int account) {

        Cursor cursor = getUnreadCursor(account);

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    // true is unread
    // false have been read
    public synchronized void markMultipleRead(int current, int account) {

        Cursor cursor = getUnreadCursor(account);

        try {
            if (cursor.moveToPosition(current)) {
                do {

                    long tweetId = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));

                    ContentValues cv = new ContentValues();
                    cv.put(HomeSQLiteHelper.COLUMN_UNREAD, 0);

                    try {
                        database.update(MentionsSQLiteHelper.TABLE_MENTIONS, cv, MentionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});
                    } catch (Exception e) {
                        open();
                        database.update(MentionsSQLiteHelper.TABLE_MENTIONS, cv, MentionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // there is nothing in the unread array
        }

        cursor.close();
    }

    public synchronized void markAllRead(int account) {

        ContentValues cv = new ContentValues();
        cv.put(MentionsSQLiteHelper.COLUMN_UNREAD, 0);

        try {
            database.update(MentionsSQLiteHelper.TABLE_MENTIONS, cv, MentionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + MentionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[] {account + "", "1"});
        } catch (Exception e) {
            open();
            database.update(MentionsSQLiteHelper.TABLE_MENTIONS, cv, MentionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + MentionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[] {account + "", "1"});
        }
    }

    public synchronized String getNewestName(int account) {

        Cursor cursor = getUnreadCursor(account);
        String name = "";

        try {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_SCREEN_NAME));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return name;
    }

    public synchronized String getNewestNames(int account) {
        Cursor cursor = getUnreadCursor(account);
        String name = "";

        try {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_USERS));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return name;
    }

    public synchronized String getNewestMessage(int account) {

        Cursor cursor = getUnreadCursor(account);
        String message = "";

        try {
            if (cursor.moveToFirst()) {
                message = cursor.getString(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TEXT));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return message;
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
                ids[0] = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));
            }

            if (cursor.moveToPrevious()) {
                ids[1] = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return ids;
    }

    public synchronized boolean tweetExists(long tweetId, int account) {

        Cursor cursor;
        try {
            cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                    allColumns, MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " + MentionsSQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId, null, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(MentionsSQLiteHelper.TABLE_MENTIONS,
                    allColumns, MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " + MentionsSQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId, null, null, null, MentionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public synchronized void deleteDups(int account) {

        try {
            database.execSQL("DELETE FROM " + MentionsSQLiteHelper.TABLE_MENTIONS + " WHERE _id NOT IN (SELECT MIN(_id) FROM " + MentionsSQLiteHelper.TABLE_MENTIONS + " GROUP BY " + MentionsSQLiteHelper.COLUMN_TWEET_ID + ") AND " + MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account);
        } catch (Exception e) {
            open();
            database.execSQL("DELETE FROM " + MentionsSQLiteHelper.TABLE_MENTIONS + " WHERE _id NOT IN (SELECT MIN(_id) FROM " + MentionsSQLiteHelper.TABLE_MENTIONS + " GROUP BY " + MentionsSQLiteHelper.COLUMN_TWEET_ID + ") AND " + MentionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account);
        }
    }

    public synchronized void removeHTML(long tweetId, String text) {
        ContentValues cv = new ContentValues();
        cv.put(MentionsSQLiteHelper.COLUMN_TEXT, text);

        try {
            database.update(MentionsSQLiteHelper.TABLE_MENTIONS, cv, MentionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});
        } catch (Exception e) {
            close();
            open();
            database.update(MentionsSQLiteHelper.TABLE_MENTIONS, cv, MentionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});
        }

    }
}
