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
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;
import android.util.Log;

import com.klinker.android.twitter.adapters.CursorListLoader;
import com.klinker.android.twitter.utils.TweetLinkUtils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;

public class HomeDataSource {

    // provides access to the database
    public static HomeDataSource dataSource = null;

    /*

    This is used so that we don't have to open and close the database on different threads or fragments
    every time. This will facilitate it between all of them to avoid Illegal State Exceptions.

     */
    public static HomeDataSource getInstance(Context context) {

        // if the datasource isn't open or it the object is null
        if (dataSource == null ||
                dataSource.getDatabase() == null ||
                !dataSource.getDatabase().isOpen()) {

            dataSource = new HomeDataSource(context); // create the database
            dataSource.open(); // open the database
        }

        return dataSource;
    }

    // Database fields
    private SQLiteDatabase database;
    private HomeSQLiteHelper dbHelper;
    private Context context;
    private int timelineSize;
    private boolean noRetweets;
    private SharedPreferences sharedPreferences;
    public static String[] allColumns = { HomeSQLiteHelper.COLUMN_ID, HomeSQLiteHelper.COLUMN_TWEET_ID, HomeSQLiteHelper.COLUMN_ACCOUNT, HomeSQLiteHelper.COLUMN_TYPE,
            HomeSQLiteHelper.COLUMN_TEXT, HomeSQLiteHelper.COLUMN_NAME, HomeSQLiteHelper.COLUMN_PRO_PIC,
            HomeSQLiteHelper.COLUMN_SCREEN_NAME, HomeSQLiteHelper.COLUMN_TIME, HomeSQLiteHelper.COLUMN_PIC_URL,
            HomeSQLiteHelper.COLUMN_RETWEETER, HomeSQLiteHelper.COLUMN_URL, HomeSQLiteHelper.COLUMN_USERS, HomeSQLiteHelper.COLUMN_HASHTAGS,
            HomeSQLiteHelper.COLUMN_CURRENT_POS, HomeSQLiteHelper.COLUMN_ANIMATED_GIF
    };

    public HomeDataSource(Context context) {
        dbHelper = new HomeSQLiteHelper(context);
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

    public HomeSQLiteHelper getHelper() {
        return dbHelper;
    }

    public synchronized void createTweet(Status status, int account) {
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

        String source;
        if (status.isRetweet()) {
            source = android.text.Html.fromHtml(status.getRetweetedStatus().getSource()).toString();
        } else {
            source = android.text.Html.fromHtml(status.getSource()).toString();
        }

        if (media.contains("/tweet_video/")) {
            media = media.replace("tweet_video", "tweet_video_thumb").replace(".mp4", ".png");
        }

        values.put(HomeSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(HomeSQLiteHelper.COLUMN_TEXT, text);
        values.put(HomeSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(HomeSQLiteHelper.COLUMN_NAME, status.getUser().getName());
        values.put(HomeSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getOriginalProfileImageURL());
        values.put(HomeSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
        values.put(HomeSQLiteHelper.COLUMN_TIME, time);
        values.put(HomeSQLiteHelper.COLUMN_RETWEETER, originalName);
        values.put(HomeSQLiteHelper.COLUMN_UNREAD, 1);
        values.put(HomeSQLiteHelper.COLUMN_PIC_URL, media);
        values.put(HomeSQLiteHelper.COLUMN_URL, url);
        values.put(HomeSQLiteHelper.COLUMN_USERS, users);
        values.put(HomeSQLiteHelper.COLUMN_HASHTAGS, hashtags);
        values.put(HomeSQLiteHelper.COLUMN_CLIENT_SOURCE, source);
        values.put(HomeSQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, url));

        try {
            database.insert(HomeSQLiteHelper.TABLE_HOME, null, values);
        } catch (Exception e) {
            open();
            database.insert(HomeSQLiteHelper.TABLE_HOME, null, values);
        }
    }

    public synchronized void createTweet(Status status, int account, boolean initial) {
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

        String source;
        if (status.isRetweet()) {
            source = android.text.Html.fromHtml(status.getRetweetedStatus().getSource()).toString();
        } else {
            source = android.text.Html.fromHtml(status.getSource()).toString();
        }

        if (media.contains("/tweet_video/")) {
            media = media.replace("tweet_video", "tweet_video_thumb").replace(".mp4", ".png");
        }

        values.put(HomeSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(HomeSQLiteHelper.COLUMN_TEXT, text);
        values.put(HomeSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(HomeSQLiteHelper.COLUMN_NAME, status.getUser().getName());
        values.put(HomeSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getOriginalProfileImageURL());
        values.put(HomeSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
        values.put(HomeSQLiteHelper.COLUMN_TIME, time);
        values.put(HomeSQLiteHelper.COLUMN_RETWEETER, originalName);
        values.put(HomeSQLiteHelper.COLUMN_UNREAD, 0);
        values.put(HomeSQLiteHelper.COLUMN_PIC_URL, media);
        values.put(HomeSQLiteHelper.COLUMN_URL, url);
        values.put(HomeSQLiteHelper.COLUMN_USERS, users);
        values.put(HomeSQLiteHelper.COLUMN_HASHTAGS, hashtags);
        values.put(HomeSQLiteHelper.COLUMN_CLIENT_SOURCE, source);
        values.put(HomeSQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, url));

        try {
            database.insert(HomeSQLiteHelper.TABLE_HOME, null, values);
        } catch (Exception e) {
            open();
            database.insert(HomeSQLiteHelper.TABLE_HOME, null, values);
        }
    }

    public synchronized int insertTweets(List<Status> statuses, int currentAccount, long[] lastIds) {

        ContentValues[] valueses = new ContentValues[statuses.size()];

        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);
            Long id = status.getId();
            ContentValues values = new ContentValues();

            if (id > lastIds[0]) {
                String originalName = "";
                long mId = status.getId();
                long time = status.getCreatedAt().getTime();

                if (status.isRetweet()) {
                    originalName = status.getUser().getScreenName();
                    status = status.getRetweetedStatus();
                }

                String[] html = TweetLinkUtils.getLinksInStatus(status);
                String text = html[0];
                String media = html[1];
                String url = html[2];
                String hashtags = html[3];
                String users = html[4];

                String source;
                if (status.isRetweet()) {
                    source = android.text.Html.fromHtml(status.getRetweetedStatus().getSource()).toString();
                } else {
                    source = android.text.Html.fromHtml(status.getSource()).toString();
                }

                if (media.contains("/tweet_video/")) {
                    media = media.replace("tweet_video", "tweet_video_thumb").replace(".mp4", ".png");
                }

                values.put(HomeSQLiteHelper.COLUMN_ACCOUNT, currentAccount);
                values.put(HomeSQLiteHelper.COLUMN_TEXT, text);
                values.put(HomeSQLiteHelper.COLUMN_TWEET_ID, mId);
                values.put(HomeSQLiteHelper.COLUMN_NAME, status.getUser().getName());
                values.put(HomeSQLiteHelper.COLUMN_PRO_PIC, status.getUser().getOriginalProfileImageURL());
                values.put(HomeSQLiteHelper.COLUMN_SCREEN_NAME, status.getUser().getScreenName());
                values.put(HomeSQLiteHelper.COLUMN_TIME, time);
                values.put(HomeSQLiteHelper.COLUMN_RETWEETER, originalName);
                values.put(HomeSQLiteHelper.COLUMN_UNREAD, 1);
                values.put(HomeSQLiteHelper.COLUMN_PIC_URL, media);
                values.put(HomeSQLiteHelper.COLUMN_URL, url);
                values.put(HomeSQLiteHelper.COLUMN_USERS, users);
                values.put(HomeSQLiteHelper.COLUMN_HASHTAGS, hashtags);
                values.put(HomeSQLiteHelper.COLUMN_CLIENT_SOURCE, source);
                values.put(HomeSQLiteHelper.COLUMN_ANIMATED_GIF, TweetLinkUtils.getGIFUrl(status, url));

            } else {
                values = null;
            }

            valueses[i] = values;
        }

        ArrayList<ContentValues> vals = new ArrayList<ContentValues>();

        for (ContentValues v : valueses) {
            if (v != null) {
                vals.add(v);
            }
        }

        insertMultiple(valueses);

        return vals.size();
    }

    public synchronized int insertMultiple(ContentValues[] allValues) {
        int rowsAdded = 0;
        long rowId;

        if (database == null || !database.isOpen()) {
            open();
        }

        try {
            database.beginTransaction();

            Log.v("talon_inserting", "starting insert, number of values: " + allValues.length);

            for (int i = 0; i < allValues.length; i++) {
                ContentValues initialValues = allValues[i];

                if (initialValues != null) {
                    try {
                        rowId = database.insert(HomeSQLiteHelper.TABLE_HOME, null, initialValues);
                    } catch (Exception e) {
                        e.printStackTrace();
                        open();
                        try {
                            rowId = database.insert(HomeSQLiteHelper.TABLE_HOME, null, initialValues);
                        } catch (Exception x) {
                            return rowsAdded;
                        }
                    }
                    if (rowId > 0) {
                        rowsAdded++;
                    }
                }

                allValues[i] = null;
            }

            database.setTransactionSuccessful();
        } catch (Exception e)  {
            e.printStackTrace();
            open();

            if (database == null) {
                return 0;
            }

            database.beginTransaction();

            for (ContentValues initialValues : allValues) {
                if (initialValues != null) {
                    try {
                        rowId = database.insert(HomeSQLiteHelper.TABLE_HOME, null, initialValues);
                    } catch (IllegalStateException x) {
                        open();
                        try {
                            rowId = database.insert(HomeSQLiteHelper.TABLE_HOME, null, initialValues);
                        } catch (Exception m) {
                            return rowsAdded;
                        }
                    }
                    if (rowId > 0) {
                        rowsAdded++;
                    }
                }
            }

            database.setTransactionSuccessful();

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
            database.delete(HomeSQLiteHelper.TABLE_HOME, HomeSQLiteHelper.COLUMN_TWEET_ID
                    + " = " + id, null);
        } catch (Exception e) {
            open();
            database.delete(HomeSQLiteHelper.TABLE_HOME, HomeSQLiteHelper.COLUMN_TWEET_ID
                    + " = " + id, null);
        }
    }

    public synchronized void deleteAllTweets(int account) {

        try {
            database.delete(HomeSQLiteHelper.TABLE_HOME,
                    HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        } catch (Exception e) {
            open();
            database.delete(HomeSQLiteHelper.TABLE_HOME,
                    HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        }
    }

    public synchronized Cursor getCursor(int account) {

        String users = sharedPreferences.getString("muted_users", "");
        String rts = sharedPreferences.getString("muted_rts", "");
        String hashtags = sharedPreferences.getString("muted_hashtags", "");
        String expressions = sharedPreferences.getString("muted_regex", "");
        String clients = sharedPreferences.getString("muted_clients", "");
        String where = HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account;

        expressions = expressions.replaceAll("'", "''");

        if (!users.equals("")) {
            String[] split = users.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_SCREEN_NAME + " NOT LIKE '" + s + "'";
            }

            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        if (!hashtags.equals("")) {
            String[] split = hashtags.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_HASHTAGS + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!expressions.equals("")) {
            String[] split = expressions.split("   ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_TEXT + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!clients.equals("")) {
            String[] split = clients.split("   ");
            for (String s : split) {
                where += " AND (" + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " NOT LIKE " + "'%" + s + "%'" + " OR " + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " is NULL)" ;
            }
        }

        if (noRetweets) {
            where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " = '' OR " + HomeSQLiteHelper.COLUMN_RETWEETER + " is NULL";
        } else if (!rts.equals("")) {
            String[] split = rts.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        Cursor cursor;

        String sql = "SELECT COUNT(*) FROM " + HomeSQLiteHelper.TABLE_HOME + " WHERE " + where;
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
            try {
                count = statement.simpleQueryForLong();
            } catch (Exception x) {
                return null;
            }
        }
        Log.v("talon_database", "home database has " + count + " entries");
        if (count > timelineSize) {
            try {
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", (count - timelineSize) + "," + timelineSize);
            } catch (Exception e) {
                open();
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", (count - timelineSize) + "," + timelineSize);
            }
        } else {
            try {
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            } catch (Exception e) {
                open();
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            }
        }

        return cursor;
    }

    public synchronized Cursor getWearCursor(int account) {

        String users = sharedPreferences.getString("muted_users", "");
        String rts = sharedPreferences.getString("muted_rts", "");
        String hashtags = sharedPreferences.getString("muted_hashtags", "");
        String expressions = sharedPreferences.getString("muted_regex", "");
        String clients = sharedPreferences.getString("muted_clients", "");
        String where = HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account;

        expressions = expressions.replaceAll("'", "''");

        if (!users.equals("")) {
            String[] split = users.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_SCREEN_NAME + " NOT LIKE '" + s + "'";
            }

            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        if (!hashtags.equals("")) {
            String[] split = hashtags.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_HASHTAGS + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!expressions.equals("")) {
            String[] split = expressions.split("   ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_TEXT + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!clients.equals("")) {
            String[] split = clients.split("   ");
            for (String s : split) {
                where += " AND (" + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " NOT LIKE " + "'%" + s + "%'" + " OR " + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " is NULL)" ;
            }
        }

        if (noRetweets) {
            where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " = '' OR " + HomeSQLiteHelper.COLUMN_RETWEETER + " is NULL";
        } else if (!rts.equals("")) {
            String[] split = rts.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        Cursor cursor;

        String sql = "SELECT COUNT(*) FROM " + HomeSQLiteHelper.TABLE_HOME + " WHERE " + where;
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
            try {
                count = statement.simpleQueryForLong();
            } catch (Exception x) {
                return null;
            }
        }
        int position = getPosition(sharedPreferences.getInt("current_account", 1));

        try {
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", count - position + "," + position);
        } catch (Exception e) {
            open();
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", count - position + "," + position);
        }

        return cursor;
    }

    public synchronized Cursor getTrimmingCursor(int account) {

        String where = HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account;

        Cursor cursor;

        try {
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        return cursor;
    }

    public synchronized Cursor getWidgetCursor(int account) {

        String users = sharedPreferences.getString("muted_users", "");
        String rts = sharedPreferences.getString("muted_rts", "");
        String hashtags = sharedPreferences.getString("muted_hashtags", "");
        String expressions = sharedPreferences.getString("muted_regex", "");
        String clients = sharedPreferences.getString("muted_clients", "");

        expressions = expressions.replaceAll("'", "''");

        String where = HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account;

        if (!users.equals("")) {
            String[] split = users.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_SCREEN_NAME + " NOT LIKE '" + s + "'";
            }

            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        if (!hashtags.equals("")) {
            String[] split = hashtags.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_HASHTAGS + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!expressions.equals("")) {
            String[] split = expressions.split("   ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_TEXT + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!clients.equals("")) {
            String[] split = clients.split("   ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " NOT LIKE " + "'%" + s + "%'" + " OR " + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " is NULL" ;
            }
        }

        if (noRetweets) {
            where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " = '' OR " + HomeSQLiteHelper.COLUMN_RETWEETER + " is NULL";
        } else if (!rts.equals("")) {
            String[] split = rts.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        Cursor cursor;
        try {
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " DESC", "150");
        } catch (Exception e) {
            open();
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " DESC", "150");
        }

        return cursor;
    }

    public synchronized Cursor getUnreadCursor(int account) {

        String users = sharedPreferences.getString("muted_users", "");
        String rts = sharedPreferences.getString("muted_rts", "");
        String hashtags = sharedPreferences.getString("muted_hashtags", "");
        String expressions = sharedPreferences.getString("muted_regex", "");
        String clients = sharedPreferences.getString("muted_clients", "");

        expressions = expressions.replaceAll("'", "''");

        String where = HomeSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + HomeSQLiteHelper.COLUMN_UNREAD + " = ?";

        if (!users.equals("")) {
            String[] split = users.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_SCREEN_NAME + " NOT LIKE '" + s + "'";
            }

            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        if (!hashtags.equals("")) {
            String[] split = hashtags.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_HASHTAGS + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!expressions.equals("")) {
            String[] split = expressions.split("   ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_TEXT + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!clients.equals("")) {
            String[] split = clients.split("   ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " NOT LIKE " + "'%" + s + "%'" + " OR " + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " is NULL" ;
            }
        }

        if (noRetweets) {
            where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " = '' OR " + HomeSQLiteHelper.COLUMN_RETWEETER + " is NULL";
        } else if (!rts.equals("")) {
            String[] split = rts.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        Cursor cursor;
        try {
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns, where, new String[] {account + "", "1"}, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns, where, new String[] {account + "", "1"}, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        return cursor;
    }

    public synchronized Cursor getSearchCursor(String where) {
        try {
            return database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized Cursor getPicsCursor(int account) {

        String users = sharedPreferences.getString("muted_users", "");
        String rts = sharedPreferences.getString("muted_rts", "");
        String hashtags = sharedPreferences.getString("muted_hashtags", "");
        String expressions = sharedPreferences.getString("muted_regex", "");
        String clients = sharedPreferences.getString("muted_clients", "");

        expressions = expressions.replaceAll("'", "''");

        String where = HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " + HomeSQLiteHelper.COLUMN_PIC_URL + " LIKE '%ht%'";

        if (!users.equals("")) {
            String[] split = users.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_SCREEN_NAME + " NOT LIKE '" + s + "'";
            }

            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        if (!hashtags.equals("")) {
            String[] split = hashtags.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_HASHTAGS + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!expressions.equals("")) {
            String[] split = expressions.split("   ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_TEXT + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!clients.equals("")) {
            String[] split = clients.split("   ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " NOT LIKE " + "'%" + s + "%'" + " OR " + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " is NULL" ;
            }
        }

        if (noRetweets) {
            where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " = '' OR " + HomeSQLiteHelper.COLUMN_RETWEETER + " is NULL";
        } else if (!rts.equals("")) {
            String[] split = rts.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        where += " AND " + HomeSQLiteHelper.COLUMN_PIC_URL + " NOT LIKE " + "'%youtu%'";

        Cursor cursor;

        String sql = "SELECT COUNT(*) FROM " + HomeSQLiteHelper.TABLE_HOME + " WHERE " + where;
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
            try {
                count = statement.simpleQueryForLong();
            } catch (Exception x) {
                return null;
            }
        }
        if (count > 200) {
            try {
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", (count - 200) + "," + 200);
            } catch (Exception e) {
                open();
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", (count - 200) + "," + 200);
            }
        } else {
            try {
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            } catch (Exception e) {
                open();
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            }
        }

        return cursor;
    }

    public synchronized Cursor getLinksCursor(int account) {

        String users = sharedPreferences.getString("muted_users", "");
        String rts = sharedPreferences.getString("muted_rts", "");
        String hashtags = sharedPreferences.getString("muted_hashtags", "");
        String expressions = sharedPreferences.getString("muted_regex", "");
        String clients = sharedPreferences.getString("muted_clients", "");

        expressions = expressions.replaceAll("'", "''");

        String where = HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " + HomeSQLiteHelper.COLUMN_URL + " LIKE '%ht%'";

        if (!users.equals("")) {
            String[] split = users.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_SCREEN_NAME + " NOT LIKE '" + s + "'";
            }

            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        if (!expressions.equals("")) {
            String[] split = expressions.split("   ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_TEXT + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!hashtags.equals("")) {
            String[] split = hashtags.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_HASHTAGS + " NOT LIKE " + "'%" + s + "%'";
            }
        }

        if (!clients.equals("")) {
            String[] split = clients.split("   ");
            for (String s : split) {
                where += " AND (" + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " NOT LIKE " + "'%" + s + "%'" + " OR " + HomeSQLiteHelper.COLUMN_CLIENT_SOURCE + " is NULL)" ;
            }
        }

        if (noRetweets) {
            where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " = '' OR " + HomeSQLiteHelper.COLUMN_RETWEETER + " is NULL";
        } else if (!rts.equals("")) {
            String[] split = rts.split(" ");
            for (String s : split) {
                where += " AND " + HomeSQLiteHelper.COLUMN_RETWEETER + " NOT LIKE '" + s + "'";
            }
        }

        Cursor cursor;

        String sql = "SELECT COUNT(*) FROM " + HomeSQLiteHelper.TABLE_HOME + " WHERE " + where;
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
            try {
                count = statement.simpleQueryForLong();
            } catch (Exception x) {
                return null;
            }
        }
        if (count > 200) {
            try {
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", (count - 200) + "," + 200);
            } catch (Exception e) {
                open();
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", (count - 200) + "," + 200);
            }
        } else {
            try {
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            } catch (Exception e) {
                open();
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            }
        }

        return cursor;
    }

    public synchronized Cursor getFavUsersCursor(int account) {

        String screennames = FavoriteUsersDataSource.getInstance(context).getNames(account);
        String where = HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND (";

        if (!screennames.equals("")) {
            String[] split = screennames.split("  ");
            for (int i = 0; i <split.length; i++) {
                String s = split[i];
                if (i != 0) {
                    where += " OR ";
                }
                where += HomeSQLiteHelper.COLUMN_SCREEN_NAME + " LIKE '" + s + "'";
            }

            for (String s : split) {
                where += " OR " + HomeSQLiteHelper.COLUMN_RETWEETER + " LIKE '" + s + "'";
            }
        } else {
            where += HomeSQLiteHelper.COLUMN_SCREEN_NAME + " = '' OR " + HomeSQLiteHelper.COLUMN_SCREEN_NAME + " is NULL";
        }

        where += ")";

        Cursor cursor;

        String sql = "SELECT COUNT(*) FROM " + HomeSQLiteHelper.TABLE_HOME + " WHERE " + where;
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
            try {
                count = statement.simpleQueryForLong();
            } catch (Exception x) {
                return null;
            }
        }
        if (count > 200) {
            try {
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", (count - 200) + "," + 200);
            } catch (Exception e) {
                open();
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", (count - 200) + "," + 200);
            }
        } else {
            try {
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            } catch (Exception e) {
                open();
                cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                        allColumns, where, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
            }
        }

        return cursor;
    }

    public synchronized int getUnreadCount(int account) {

        /*Cursor cursor = getUnreadCursor(account);

        int count;
        try {
            count = cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
            count = 0;
        }

        cursor.close();

        return count;*/

        return getPosition(account);
    }

    public synchronized void markAllRead(int account) {

        ContentValues cv = new ContentValues();
        cv.put(HomeSQLiteHelper.COLUMN_UNREAD, 0);

        try {
            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + HomeSQLiteHelper.COLUMN_UNREAD + " = ?", new String[] {account + "", "1"});
        } catch (Exception e) {
            open();
            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + HomeSQLiteHelper.COLUMN_UNREAD + " = ?", new String[] {account + "", "1"});
        }

    }

    public synchronized void removeCurrent(int account) {

        ContentValues cv = new ContentValues();
        cv.put(HomeSQLiteHelper.COLUMN_CURRENT_POS, "");

        try {
            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + HomeSQLiteHelper.COLUMN_CURRENT_POS + " = ?", new String[] {account + "", "1"});
        } catch (Exception e) {
            open();
            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + HomeSQLiteHelper.COLUMN_CURRENT_POS + " = ?", new String[] {account + "", "1"});
        }

    }

    public synchronized void markUnreadFilling(int account) {
        ContentValues cv = new ContentValues();
        cv.put(HomeSQLiteHelper.COLUMN_UNREAD, 1);

        // first get the unread cursor to find the first id to mark unread
        Cursor unread = getUnreadCursor(account);

        if (unread.moveToFirst()) {
            // this is the long for the first unread tweet in the list
            long id = unread.getLong(unread.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID));

            Cursor full = getCursor(account);
            if (full.moveToFirst()) {
                boolean startUnreads = false;
                do {
                    long thisId = full.getLong(full.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID));

                    if (thisId == id) {
                        startUnreads = true;
                    }

                    if (startUnreads) {
                        try {
                            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {thisId + ""});
                        } catch (Exception e) {
                            open();
                            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {thisId + ""});
                        }
                    }
                } while (full.moveToNext());
            }
            full.close();
        }

        unread.close();
    }

    public synchronized long[] getLastIds(int account) {
        long id[] = new long[] {0,0,0,0,0};

        Cursor cursor;
        try {
            cursor = getCursor(account);
        } catch (Exception e) {
            return id;
        }

        try {
            if (cursor.moveToLast()) {
                int i = 0;
                do {
                    id[i] = cursor.getLong(cursor.getColumnIndex(MentionsSQLiteHelper.COLUMN_TWEET_ID));
                    i++;
                } while (cursor.moveToPrevious() && i < 5);
            }
        } catch (Exception e) {
        }

        if (cursor != null) {
            cursor.close();
        }

        return id;
    }

    public synchronized boolean tweetExists(long tweetId, int account) {

        Cursor cursor;
        try {
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns,
                    HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " + HomeSQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId,
                    null,
                    null,
                    null,
                    HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC",
                    "1"
            );
        } catch (Exception e) {
            open();
            cursor = database.query(HomeSQLiteHelper.TABLE_HOME,
                    allColumns,
                    HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND " + HomeSQLiteHelper.COLUMN_TWEET_ID + " = " + tweetId,
                    null,
                    null,
                    null,
                    HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC",
                    "1"
            );
        }

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public synchronized void markPosition(int account, long id) {

        ContentValues cv = new ContentValues();
        cv.put(HomeSQLiteHelper.COLUMN_CURRENT_POS, "1");

        ContentValues unread = new ContentValues();
        unread.put(HomeSQLiteHelper.COLUMN_CURRENT_POS, "");

        try {
            database.update(HomeSQLiteHelper.TABLE_HOME, unread, HomeSQLiteHelper.COLUMN_CURRENT_POS + " = ? AND " + HomeSQLiteHelper.COLUMN_ACCOUNT + " = ?", new String[]{"1", account + ""});
            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[]{id + ""});
        } catch (Exception e) {
            open();
            database.update(HomeSQLiteHelper.TABLE_HOME, unread, HomeSQLiteHelper.COLUMN_CURRENT_POS + " = ? AND " + HomeSQLiteHelper.COLUMN_ACCOUNT + " = ?", new String[]{"1", account + ""});
            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[]{id + ""});
        }
    }
    public synchronized void deleteDups(int account) {

        try {
            database.execSQL("DELETE FROM " + HomeSQLiteHelper.TABLE_HOME +
                    " WHERE _id NOT IN (SELECT MIN(_id) FROM " + HomeSQLiteHelper.TABLE_HOME +
                    " GROUP BY " + HomeSQLiteHelper.COLUMN_TWEET_ID + ") AND " + HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account);
        } catch (Exception e) {
            open();
            database.execSQL("DELETE FROM " + HomeSQLiteHelper.TABLE_HOME +
                    " WHERE _id NOT IN (SELECT MIN(_id) FROM " + HomeSQLiteHelper.TABLE_HOME +
                    " GROUP BY " + HomeSQLiteHelper.COLUMN_TWEET_ID + ") AND " + HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + account);

        }

    }

    public synchronized int getPosition(int account, long id) {
        int pos = 0;

        try {
            Cursor cursor = getCursor(account);
            if (cursor.moveToLast()) {
                do {
                    if (cursor.getLong(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID)) == id) {
                        break;
                    } else {
                        pos++;
                    }
                } while (cursor.moveToPrevious());
            }

            cursor.close();
        } catch (Exception e) {
            // we will return -1 and let the shared pref handle it
            return -1;
        }

        return pos;
    }

    public synchronized int getPosition(int account) {
        int pos = 0;

        Cursor cursor = getCursor(account);
        if (cursor.moveToLast()) {
            String s;
            do {
                s = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_CURRENT_POS));
                if (s != null && !s.isEmpty()) {
                    break;
                } else {
                    pos++;
                }
            } while (cursor.moveToPrevious());
        }

        cursor.close();

        return pos;
    }

    public synchronized void removeHTML(long tweetId, String text) {
        ContentValues cv = new ContentValues();
        cv.put(HomeSQLiteHelper.COLUMN_TEXT, text);

        try {
            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});
        } catch (Exception e) {
            close();
            open();
            database.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[]{tweetId + ""});
        }
    }
}
