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
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;
import android.util.Log;

import com.klinker.android.twitter.utils.TweetLinkUtils;

import java.util.List;

import twitter4j.Status;


public class ListDataSource {

    // provides access to the database
    public static ListDataSource dataSource = null;

    /*

    This is used so that we don't have to open and close the database on different threads or fragments
    every time. This will facilitate it between all of them to avoid Illegal State Exceptions.

     */
    public static ListDataSource getInstance(Context context) {

        // if the datasource isn't open or it the object is null
        if (dataSource == null ||
                dataSource.getDatabase() == null ||
                !dataSource.getDatabase().isOpen()) {

            dataSource = new ListDataSource(context); // create the database
            dataSource.open(); // open the database
        }

        return dataSource;
    }

    // Database fields
    private SQLiteDatabase database;
    private ListSQLiteHelper dbHelper;
    private Context context;
    private int timelineSize;
    private boolean noRetweets;
    private SharedPreferences sharedPreferences;
    public static String[] allColumns = { ListSQLiteHelper.COLUMN_ID,
            ListSQLiteHelper.COLUMN_TWEET_ID,
            ListSQLiteHelper.COLUMN_ACCOUNT,
            ListSQLiteHelper.COLUMN_TEXT,
            ListSQLiteHelper.COLUMN_NAME,
            ListSQLiteHelper.COLUMN_PRO_PIC,
            ListSQLiteHelper.COLUMN_SCREEN_NAME,
            ListSQLiteHelper.COLUMN_TIME,
            ListSQLiteHelper.COLUMN_PIC_URL,
            ListSQLiteHelper.COLUMN_RETWEETER,
            ListSQLiteHelper.COLUMN_URL,
            ListSQLiteHelper.COLUMN_USERS,
            ListSQLiteHelper.COLUMN_HASHTAGS };

    public ListDataSource(Context context) {
        dbHelper = new ListSQLiteHelper(context);
        this.context = context;
        sharedPreferences = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        timelineSize = Integer.parseInt(sharedPreferences.getString("timeline_size", "1000"));
        noRetweets = sharedPreferences.getBoolean("ignore_retweets", false);
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

    public ListSQLiteHelper getHelper() {
        return dbHelper;
    }

    public synchronized void createTweet(Status status, long listId) {
        ContentValues values = new ContentValues();
        String originalName = "";
        long time = status.getCreatedAt().getTime();
        long id = status.getId();

        if(status.isRetweet()) {
            originalName = status.getUser().getScreenName();
            status = status.getRetweetedStatus();
        }

        String[] html = TweetLinkUtils.getLinksInStatus(status);
        String text = html[0];
        String media = html[1];
        String url = html[2];
        String hashtags = html[3];
        String users = html[4];

        if (media.contains("/tweet_video/")) {
            media = media.replace("tweet_video", "tweet_video_thumb").replace(".mp4", ".png");
        }

        values.put(ListSQLiteHelper.COLUMN_TEXT, text);
        values.put(ListSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(ListSQLiteHelper.COLUMN_NAME, status.getUser().getName());
        values.put(ListSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getOriginalProfileImageURL());
        values.put(ListSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
        values.put(ListSQLiteHelper.COLUMN_TIME, time);
        values.put(ListSQLiteHelper.COLUMN_RETWEETER, originalName);
        values.put(ListSQLiteHelper.COLUMN_PIC_URL, media);
        values.put(ListSQLiteHelper.COLUMN_URL, url);
        values.put(ListSQLiteHelper.COLUMN_USERS, users);
        values.put(ListSQLiteHelper.COLUMN_HASHTAGS, hashtags);
        values.put(ListSQLiteHelper.COLUMN_LIST_ID, listId);
        values.put(ListSQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, url));


        try {
            database.insert(ListSQLiteHelper.TABLE_HOME, null, values);
        } catch (Exception e) {
            open();
            database.insert(ListSQLiteHelper.TABLE_HOME, null, values);
        }
    }

    public int insertTweets(List<Status> statuses, long listId) {

        ContentValues[] valueses = new ContentValues[statuses.size()];

        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);

            ContentValues values = new ContentValues();
            String originalName = "";
            long time = status.getCreatedAt().getTime();
            long id = status.getId();

            if(status.isRetweet()) {
                originalName = status.getUser().getScreenName();
                status = status.getRetweetedStatus();
            }

            String[] html = TweetLinkUtils.getLinksInStatus(status);
            String text = html[0];
            String media = html[1];
            String url = html[2];
            String hashtags = html[3];
            String users = html[4];

            if (media.contains("/tweet_video/")) {
                media = media.replace("tweet_video", "tweet_video_thumb").replace(".mp4", ".png");
            }

            values.put(ListSQLiteHelper.COLUMN_TEXT, text);
            values.put(ListSQLiteHelper.COLUMN_TWEET_ID, id);
            values.put(ListSQLiteHelper.COLUMN_NAME, status.getUser().getName());
            values.put(ListSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getOriginalProfileImageURL());
            values.put(ListSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
            values.put(ListSQLiteHelper.COLUMN_TIME, time);
            values.put(ListSQLiteHelper.COLUMN_RETWEETER, originalName);
            values.put(ListSQLiteHelper.COLUMN_PIC_URL, media);
            values.put(ListSQLiteHelper.COLUMN_URL, url);
            values.put(ListSQLiteHelper.COLUMN_USERS, users);
            values.put(ListSQLiteHelper.COLUMN_HASHTAGS, hashtags);
            values.put(ListSQLiteHelper.COLUMN_LIST_ID, listId);
            values.put(ListSQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, url));

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
                    rowId = database.insert(ListSQLiteHelper.TABLE_HOME, null, values);
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
            database.delete(ListSQLiteHelper.TABLE_HOME, ListSQLiteHelper.COLUMN_TWEET_ID
                    + " = " + id, null);
        } catch (Exception e) {
            open();
            database.delete(ListSQLiteHelper.TABLE_HOME, ListSQLiteHelper.COLUMN_TWEET_ID
                    + " = " + id, null);
        }
    }

    public synchronized void deleteAllTweets(long listNumber) {

        try {
            database.delete(ListSQLiteHelper.TABLE_HOME,
                    ListSQLiteHelper.COLUMN_LIST_ID + " = " + listNumber, null);
        } catch (Exception e) {
            open();
            database.delete(ListSQLiteHelper.TABLE_HOME,
                    ListSQLiteHelper.COLUMN_LIST_ID + " = " + listNumber, null);
        }
    }

    public synchronized Cursor getCursor(long listId) {

        String users = sharedPreferences.getString("muted_users", "");
        String rts = sharedPreferences.getString("muted_rts", "");
        String hashtags = sharedPreferences.getString("muted_hashtags", "");
        String expressions = sharedPreferences.getString("muted_regex", "");

        expressions = expressions.replaceAll("'", "''");

        String where = ListSQLiteHelper.COLUMN_LIST_ID + " = " + listId;

        if (!users.equals("")) {
            String[] split = users.split(" ");
            for (String s : split) {
                where += " AND " + ListSQLiteHelper.COLUMN_SCREEN_NAME + " NOT LIKE '" + s + "'";
            }

            for (String s : split) {
                where += " AND " + ListSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        if (!hashtags.equals("")) {
            String[] split = hashtags.split(" ");
            for (String s : split) {
                where += " AND " + ListSQLiteHelper.COLUMN_HASHTAGS + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!expressions.equals("")) {
            String[] split = expressions.split("   ");
            for (String s : split) {
                where += " AND " + ListSQLiteHelper.COLUMN_TEXT + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (noRetweets) {
            where += " AND " + ListSQLiteHelper.COLUMN_RETWEETER + " = '' OR " + ListSQLiteHelper.COLUMN_RETWEETER + " is NULL";
        } else if (!rts.equals("")) {
            String[] split = rts.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        Cursor cursor;
        String sql = "SELECT COUNT(*) FROM " + ListSQLiteHelper.TABLE_HOME + " WHERE " + where;
        SQLiteStatement statement;
        try {
            statement = database.compileStatement(sql);
        } catch (Exception e) {
            open();
            statement = database.compileStatement(sql);
        }
        long count;
        try {
            count = statement.simpleQueryForLong();
        } catch (Exception e) {
            open();
            count = statement.simpleQueryForLong();
        }
        Log.v("talon_database", "list database has " + count + " entries");
        if (count > 400) {
            try {
                cursor = database.query(ListSQLiteHelper.TABLE_HOME,
                        allColumns,
                        where,
                        null,
                        null,
                        null,
                        ListSQLiteHelper.COLUMN_TWEET_ID + " ASC",
                        (count - 400) + "," + 400);
            } catch (Exception e) {
                open();
                cursor = database.query(ListSQLiteHelper.TABLE_HOME,
                        allColumns,
                        where,
                        null,
                        null,
                        null,
                        ListSQLiteHelper.COLUMN_TWEET_ID + " ASC",
                        (count - 400) + "," + 400);
            }
        } else {
            try {
                cursor = database.query(ListSQLiteHelper.TABLE_HOME,
                        allColumns,
                        where,
                        null,
                        null,
                        null,
                        ListSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            } catch (Exception e) {
                open();
                cursor = database.query(ListSQLiteHelper.TABLE_HOME,
                        allColumns,
                        where,
                        null,
                        null,
                        null,
                        ListSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            }
        }

        return cursor;
    }

    public synchronized Cursor getTrimmingCursor(long listId) {

        String where = ListSQLiteHelper.COLUMN_LIST_ID + " = " + listId;

        Cursor cursor;

        try {
            cursor = database.query(ListSQLiteHelper.TABLE_HOME,
                    allColumns,
                    where,
                    null,
                    null,
                    null,
                    ListSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(ListSQLiteHelper.TABLE_HOME,
                    allColumns,
                    where,
                    null,
                    null,
                    null,
                    ListSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        return cursor;
    }

    public synchronized Cursor getWholeCursor() {

        Cursor cursor;
        try {
            cursor = database.query(ListSQLiteHelper.TABLE_HOME,
                    allColumns, null, null, null, null, ListSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(ListSQLiteHelper.TABLE_HOME,
                    allColumns, null, null, null, null, ListSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        return cursor;
    }

    public synchronized long[] getLastIds(long listId) {
        long id[] = new long[] {0,0,0,0,0};

        Cursor cursor;
        try {
            cursor = getCursor(listId);
        } catch (Exception e) {
            return id;
        }

        try {
            if (cursor.moveToFirst()) {
                int i = 0;
                do {
                    id[i] = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));
                } while (cursor.moveToNext() && i < 5);
            }
        } catch (Exception e) {
        }

        cursor.close();

        return id;
    }

    public synchronized void deleteDups(long list) {

        try {
            database.execSQL("DELETE FROM " + ListSQLiteHelper.TABLE_HOME +
                    " WHERE _id NOT IN (SELECT MIN(_id) FROM " + ListSQLiteHelper.TABLE_HOME +
                    " GROUP BY " + ListSQLiteHelper.COLUMN_TWEET_ID + ") AND " + ListSQLiteHelper.COLUMN_LIST_ID + " = " + list);
        } catch (Exception e) {
            open();
            database.execSQL("DELETE FROM " + ListSQLiteHelper.TABLE_HOME +
                    " WHERE _id NOT IN (SELECT MIN(_id) FROM " + ListSQLiteHelper.TABLE_HOME +
                    " GROUP BY " + ListSQLiteHelper.COLUMN_TWEET_ID + ") AND " + ListSQLiteHelper.COLUMN_LIST_ID + " = " + list);
        }
    }

    public synchronized void removeHTML(long tweetId, String text) {
        ContentValues cv = new ContentValues();
        cv.put(ListSQLiteHelper.COLUMN_TEXT, text);

        if (database == null || !database.isOpen()) {
            open();
        }

        try {
            database.update(ListSQLiteHelper.TABLE_HOME, cv, ListSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});
        } catch (Exception e) {
            close();
            open();
            database.update(ListSQLiteHelper.TABLE_HOME, cv, ListSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});
        }
    }
}
