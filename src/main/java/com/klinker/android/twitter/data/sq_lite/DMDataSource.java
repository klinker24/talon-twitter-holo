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
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.klinker.android.twitter.utils.TweetLinkUtils;

import twitter4j.DirectMessage;
import twitter4j.MediaEntity;
import twitter4j.URLEntity;

public class DMDataSource {

    // provides access to the database
    public static DMDataSource dataSource = null;

    /*

    This is used so that we don't have to open and close the database on different threads or fragments
    every time. This will facilitate it between all of them to avoid Illegal State Exceptions.

     */
    public static DMDataSource getInstance(Context context) {

        // if the datasource isn't open or it the object is null
        if (dataSource == null ||
                dataSource.getDatabase() == null ||
                !dataSource.getDatabase().isOpen()) {

            dataSource = new DMDataSource(context); // create the database
            dataSource.open(); // open the database
        }

        return dataSource;
    }

    // Database fields
    private SQLiteDatabase database;
    private DMSQLiteHelper dbHelper;
    public String[] allColumns = {DMSQLiteHelper.COLUMN_ID, DMSQLiteHelper.COLUMN_TWEET_ID, DMSQLiteHelper.COLUMN_ACCOUNT, DMSQLiteHelper.COLUMN_TYPE,
            DMSQLiteHelper.COLUMN_TEXT, DMSQLiteHelper.COLUMN_NAME, DMSQLiteHelper.COLUMN_PRO_PIC,
            DMSQLiteHelper.COLUMN_SCREEN_NAME, DMSQLiteHelper.COLUMN_TIME, DMSQLiteHelper.COLUMN_PIC_URL, DMSQLiteHelper.COLUMN_RETWEETER,
            DMSQLiteHelper.COLUMN_URL, HomeSQLiteHelper.COLUMN_USERS, HomeSQLiteHelper.COLUMN_HASHTAGS, DMSQLiteHelper.COLUMN_EXTRA_ONE, DMSQLiteHelper.COLUMN_EXTRA_TWO };

    public DMDataSource(Context context) {
        dbHelper = new DMSQLiteHelper(context);
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

    public DMSQLiteHelper getHelper() {
        return dbHelper;
    }

    public synchronized void createDirectMessage(DirectMessage status, int account) {
        ContentValues values = new ContentValues();
        long time = status.getCreatedAt().getTime();

        values.put(DMSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(DMSQLiteHelper.COLUMN_TEXT, TweetLinkUtils.getLinksInStatus(status)[0]);
        values.put(DMSQLiteHelper.COLUMN_TWEET_ID, status.getId());
        values.put(DMSQLiteHelper.COLUMN_NAME, status.getSender().getName());
        values.put(DMSQLiteHelper.COLUMN_PRO_PIC, status.getSender().getOriginalProfileImageURL());
        values.put(DMSQLiteHelper.COLUMN_SCREEN_NAME, status.getSender().getScreenName());
        values.put(DMSQLiteHelper.COLUMN_TIME, time);
        values.put(DMSQLiteHelper.COLUMN_RETWEETER, status.getRecipientScreenName());
        values.put(DMSQLiteHelper.COLUMN_EXTRA_ONE, status.getRecipient().getOriginalProfileImageURL());
        values.put(DMSQLiteHelper.COLUMN_EXTRA_TWO, status.getRecipient().getName());
        values.put(HomeSQLiteHelper.COLUMN_PIC_URL, TweetLinkUtils.getLinksInStatus(status)[1]);

        MediaEntity[] entities = status.getMediaEntities();

        if (entities.length > 0) {
            values.put(DMSQLiteHelper.COLUMN_PIC_URL, entities[0].getMediaURL());
        }

        URLEntity[] urls = status.getURLEntities();
        for (URLEntity url : urls) {
            Log.v("inserting_dm", "url here: " + url.getExpandedURL());
            values.put(DMSQLiteHelper.COLUMN_URL, url.getExpandedURL());
        }

        try {
            database.insert(DMSQLiteHelper.TABLE_DM, null, values);
        } catch (Exception e) {
            open();
            database.insert(DMSQLiteHelper.TABLE_DM, null, values);
        }

    }

    public synchronized void deleteTweet(long tweetId) {
        long id = tweetId;

        try {
            database.delete(DMSQLiteHelper.TABLE_DM, DMSQLiteHelper.COLUMN_TWEET_ID
                    + " = " + id, null);
        } catch (Exception e) {
            open();
            database.delete(DMSQLiteHelper.TABLE_DM, DMSQLiteHelper.COLUMN_TWEET_ID
                    + " = " + id, null);
        }
    }

    public synchronized void deleteAllTweets(int account) {

        try {
            database.delete(DMSQLiteHelper.TABLE_DM, DMSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        } catch (Exception e) {
            open();
            database.delete(DMSQLiteHelper.TABLE_DM, DMSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        }
    }

    public synchronized Cursor getCursor(int account) {

        Cursor cursor;
        try {
            cursor = database.query(true, DMSQLiteHelper.TABLE_DM,
                    allColumns, DMSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", null);
        } catch (Exception e) {
            open();
            cursor = database.query(true, DMSQLiteHelper.TABLE_DM,
                    allColumns, DMSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC", null);
        }
        
        return cursor;
    }

    public synchronized Cursor getConvCursor(String name, int account) {

        Cursor cursor;
        try {
            cursor = database.query(true, DMSQLiteHelper.TABLE_DM,
                    allColumns, DMSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND (" + DMSQLiteHelper.COLUMN_SCREEN_NAME + " = ? OR " + DMSQLiteHelper.COLUMN_RETWEETER + " = ?)", new String[] {name, name}, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " DESC", null);
        } catch (Exception e) {
            open();
            cursor = database.query(true, DMSQLiteHelper.TABLE_DM,
                    allColumns, DMSQLiteHelper.COLUMN_ACCOUNT + " = " + account + " AND (" + DMSQLiteHelper.COLUMN_SCREEN_NAME + " = ? OR " + DMSQLiteHelper.COLUMN_RETWEETER + " = ?)", new String[] {name, name}, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " DESC", null);
        }

        return cursor;
    }

    public synchronized String getNewestName(int account) {

        Cursor cursor = getCursor(account);
        String name = "";

        try {
            if (cursor.moveToLast()) {
                name = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_SCREEN_NAME));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return name;
    }

    public synchronized String getNewestMessage(int account) {

        Cursor cursor = getCursor(account);
        String message = "";

        try {
            if (cursor.moveToLast()) {
                message = cursor.getString(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TEXT));
            }
        } catch (Exception e) {

        }

        cursor.close();

        return message;
    }

    public synchronized void deleteDups(int account) {

        try {
            database.execSQL("DELETE FROM " + DMSQLiteHelper.TABLE_DM + " WHERE _id NOT IN (SELECT MIN(_id) FROM " + DMSQLiteHelper.TABLE_DM + " GROUP BY " + DMSQLiteHelper.COLUMN_TWEET_ID + ") AND " + DMSQLiteHelper.COLUMN_ACCOUNT + " = " + account);
        } catch (Exception e) {
            open();
            database.execSQL("DELETE FROM " + DMSQLiteHelper.TABLE_DM + " WHERE _id NOT IN (SELECT MIN(_id) FROM " + DMSQLiteHelper.TABLE_DM + " GROUP BY " + DMSQLiteHelper.COLUMN_TWEET_ID + ") AND " + DMSQLiteHelper.COLUMN_ACCOUNT + " = " + account);
        }

    }

    public synchronized void removeHTML(long tweetId, String text) {
        ContentValues cv = new ContentValues();
        cv.put(DMSQLiteHelper.COLUMN_TEXT, text);

        if (database == null || !database.isOpen()) {
            open();
        }

        database.update(DMSQLiteHelper.TABLE_DM, cv, DMSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {tweetId + ""});

    }
}
