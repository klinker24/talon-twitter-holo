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

import com.klinker.android.twitter.R;

import java.util.GregorianCalendar;

import twitter4j.Status;
import twitter4j.User;

public class InteractionsDataSource {

    // provides access to the database
    public static InteractionsDataSource dataSource = null;

    /*

    This is used so that we don't have to open and close the database on different threads or fragments
    every time. This will facilitate it between all of them to avoid Illegal State Exceptions.

     */
    public static InteractionsDataSource getInstance(Context context) {

        // if the datasource isn't open or it the object is null
        if (dataSource == null ||
                dataSource.getDatabase() == null ||
                !dataSource.getDatabase().isOpen()) {

            dataSource = new InteractionsDataSource(context); // create the database
            dataSource.open(); // open the database
        }

        return dataSource;
    }

    // Database fields
    private SQLiteDatabase database;
    private InteractionsSQLiteHelper dbHelper;
    public String[] allColumns = {InteractionsSQLiteHelper.COLUMN_ID, InteractionsSQLiteHelper.COLUMN_UNREAD, InteractionsSQLiteHelper.COLUMN_TWEET_ID, InteractionsSQLiteHelper.COLUMN_ACCOUNT, InteractionsSQLiteHelper.COLUMN_TYPE,
            InteractionsSQLiteHelper.COLUMN_TEXT, InteractionsSQLiteHelper.COLUMN_TITLE, InteractionsSQLiteHelper.COLUMN_PRO_PIC,
            InteractionsSQLiteHelper.COLUMN_TIME, InteractionsSQLiteHelper.COLUMN_USERS };

    public static final int TYPE_FOLLOWER = 0;
    public static final int TYPE_RETWEET = 1;
    public static final int TYPE_FAVORITE = 2;
    public static final int TYPE_MENTION = 3;
    public static final int TYPE_FAV_USER = 4;

    public InteractionsDataSource(Context context) {
        dbHelper = new InteractionsSQLiteHelper(context);
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

    public InteractionsSQLiteHelper getHelper() {
        return dbHelper;
    }

    public synchronized void createMention(Context context, Status status, int account) {
        ContentValues values = new ContentValues();
        long id = status.getId();
        long time = new GregorianCalendar().getTime().getTime(); // current time
        int type = TYPE_MENTION;

        User user = status.getUser();
        String users = "@" + user.getScreenName() + " ";
        String text = status.getText();
        String title = context.getResources().getString(R.string.mentioned_by) + " <b>@" + user.getScreenName() + "</b>";

        values.put(InteractionsSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(InteractionsSQLiteHelper.COLUMN_TEXT, text);
        values.put(InteractionsSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(InteractionsSQLiteHelper.COLUMN_PRO_PIC, user.getOriginalProfileImageURL());
        values.put(InteractionsSQLiteHelper.COLUMN_TIME, time);
        values.put(InteractionsSQLiteHelper.COLUMN_UNREAD, 1);
        values.put(InteractionsSQLiteHelper.COLUMN_USERS, users);
        values.put(InteractionsSQLiteHelper.COLUMN_TYPE, type);
        values.put(InteractionsSQLiteHelper.COLUMN_TITLE, title);

        try {
            database.insert(InteractionsSQLiteHelper.TABLE_INTERACTIONS, null, values);
        } catch (Exception e) {
            open();
            database.insert(InteractionsSQLiteHelper.TABLE_INTERACTIONS, null, values);
        }
    }

    public synchronized void createFavoriteUserInter(Context context, Status status, int account) {
        ContentValues values = new ContentValues();
        long id = status.getId();
        long time = new GregorianCalendar().getTime().getTime(); // current time
        int type = TYPE_FAV_USER;

        User user = status.getUser();
        String users = "@" + user.getScreenName() + " ";
        String text = status.getText();
        String title = "<b>@" + user.getScreenName() + "</b> " + context.getResources().getString(R.string.tweeted);

        values.put(InteractionsSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(InteractionsSQLiteHelper.COLUMN_TEXT, text);
        values.put(InteractionsSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(InteractionsSQLiteHelper.COLUMN_PRO_PIC, user.getOriginalProfileImageURL());
        values.put(InteractionsSQLiteHelper.COLUMN_TIME, time);
        values.put(InteractionsSQLiteHelper.COLUMN_UNREAD, 1);
        values.put(InteractionsSQLiteHelper.COLUMN_USERS, users);
        values.put(InteractionsSQLiteHelper.COLUMN_TYPE, type);
        values.put(InteractionsSQLiteHelper.COLUMN_TITLE, title);

        try {
            database.insert(InteractionsSQLiteHelper.TABLE_INTERACTIONS, null, values);
        } catch (Exception e) {
            open();
            database.insert(InteractionsSQLiteHelper.TABLE_INTERACTIONS, null, values);
        }
    }

    public synchronized void createInteraction(Context context, User source, Status status, int account, int type) {
        ContentValues values = new ContentValues();
        long id;
        if (status != null) {
            id = status.getId();
        } else {
            id = 0; // 0 will be used for whenever it is just a follow
        }
        long time = new GregorianCalendar().getTime().getTime(); // current time

        String users = "@" + source.getScreenName() + " ";

        String text = "";

        String title = "";

        switch (type) {
            case TYPE_FAVORITE:
                title = "<b>@" + source.getScreenName() + "</b> " + context.getResources().getString(R.string.favorited);
                text = status.getText();
                break;
            case TYPE_RETWEET:
                title = "<b>@" + source.getScreenName() + "</b> " + context.getResources().getString(R.string.retweeted);
                text = status.getRetweetedStatus().getText();
                break;
            case TYPE_FOLLOWER:
                title = "<b>@" + source.getScreenName() + "</b> " + context.getResources().getString(R.string.followed);
                break;
        }

        values.put(InteractionsSQLiteHelper.COLUMN_ACCOUNT, account);
        values.put(InteractionsSQLiteHelper.COLUMN_TEXT, text);
        values.put(InteractionsSQLiteHelper.COLUMN_TWEET_ID, id);
        values.put(InteractionsSQLiteHelper.COLUMN_PRO_PIC, source.getOriginalProfileImageURL());
        values.put(InteractionsSQLiteHelper.COLUMN_TIME, time);
        values.put(InteractionsSQLiteHelper.COLUMN_UNREAD, 1);
        values.put(InteractionsSQLiteHelper.COLUMN_USERS, users);
        values.put(InteractionsSQLiteHelper.COLUMN_TYPE, type);
        values.put(InteractionsSQLiteHelper.COLUMN_TITLE, title);

        try {
            database.insert(InteractionsSQLiteHelper.TABLE_INTERACTIONS, null, values);
        } catch (Exception e) {
            open();
            database.insert(InteractionsSQLiteHelper.TABLE_INTERACTIONS, null, values);
        }
    }

    public synchronized void updateInteraction(Context context, User source, Status status, int account, int type) {
        if (type == TYPE_RETWEET) {
            status = status.getRetweetedStatus();
        }
        Cursor cursor = interactionExists(status.getId(), account);

        if (cursor.getCount() > 0) { // it does exist
            // i want to do the updating stuff

            if (cursor.moveToFirst()) {
                String users = cursor.getString(cursor.getColumnIndex(InteractionsSQLiteHelper.COLUMN_USERS));
                String title = "";

                users += "@" + source.getScreenName() + " ";

                String[] x = users.split(" ");

                if (type == TYPE_RETWEET) { // retweet
                    title = x.length + " " + context.getResources().getString(R.string.new_retweets);
                } else { // favorite
                    title = x.length + " " + context.getResources().getString(R.string.new_favorites);
                }

                ContentValues cv = new ContentValues();
                cv.put(InteractionsSQLiteHelper.COLUMN_UNREAD, 1);
                cv.put(InteractionsSQLiteHelper.COLUMN_USERS, users);
                cv.put(InteractionsSQLiteHelper.COLUMN_TITLE, title);
                cv.put(InteractionsSQLiteHelper.COLUMN_PRO_PIC, source.getOriginalProfileImageURL());
                cv.put(InteractionsSQLiteHelper.COLUMN_TIME, new GregorianCalendar().getTimeInMillis());

                try {
                    database.update(InteractionsSQLiteHelper.TABLE_INTERACTIONS, cv, InteractionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {status.getId() + ""});
                } catch (Exception e) {
                    open();
                    database.update(InteractionsSQLiteHelper.TABLE_INTERACTIONS, cv, InteractionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[] {status.getId() + ""});
                }
            }
        } else {
            // this creates the entry
            createInteraction(context, source, status, account, type);
        }
    }

    public synchronized Cursor interactionExists(long tweetId, int account) {

        Cursor cursor;
        try {
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + InteractionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[]{account + "", tweetId + ""}, null, null, InteractionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + InteractionsSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[]{account + "", tweetId + ""}, null, null, InteractionsSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        }

        return cursor;
    }

    public synchronized void deleteInteraction(long id) {

        try {
            database.delete(InteractionsSQLiteHelper.TABLE_INTERACTIONS, InteractionsSQLiteHelper.COLUMN_ID
                    + " = " + id, null);
        } catch (Exception e) {
            open();
            database.delete(InteractionsSQLiteHelper.TABLE_INTERACTIONS, InteractionsSQLiteHelper.COLUMN_ID
                    + " = " + id, null);
        }
    }

    public synchronized void deleteAllInteractions(int account) {

        try {
            database.delete(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        } catch (Exception e) {
            open();
            database.delete(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null);
        }
    }

    public synchronized Cursor getCursor(int account) {

        Cursor cursor;
        try {
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null, null, null, InteractionsSQLiteHelper.COLUMN_TIME + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null, null, null, InteractionsSQLiteHelper.COLUMN_TIME + " ASC");
        }

        return cursor;
    }

    public synchronized Cursor getBackwordCursor(int account) {

        Cursor cursor;
        try {
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null, null, null, InteractionsSQLiteHelper.COLUMN_TIME + " DESC");
        } catch (Exception e) {
            open();
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = " + account, null, null, null, InteractionsSQLiteHelper.COLUMN_TIME + " DESC");
        }

        return cursor;
    }

    public synchronized Cursor getUnreadCursor(int account) {

        Cursor cursor;
        try {
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + InteractionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[]{account + "", "1"}, null, null, InteractionsSQLiteHelper.COLUMN_TIME + " ASC");
        } catch (Exception e) {
            open();
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + InteractionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[]{account + "", "1"}, null, null, InteractionsSQLiteHelper.COLUMN_TIME + " ASC");
        }

        return cursor;
    }

    public synchronized Cursor getUnreadBackwordCursor(int account) {

        Cursor cursor;
        try {
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + InteractionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[]{account + "", "1"}, null, null, InteractionsSQLiteHelper.COLUMN_TIME + " DESC");
        } catch (Exception e) {
            open();
            cursor = database.query(InteractionsSQLiteHelper.TABLE_INTERACTIONS,
                    allColumns, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + InteractionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[]{account + "", "1"}, null, null, InteractionsSQLiteHelper.COLUMN_TIME + " DESC");
        }

        return cursor;
    }

    public synchronized int getUnreadCount(int account) {

        Cursor cursor = getUnreadCursor(account);

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    public synchronized void markRead(int account, int position) {
        Cursor cursor = getUnreadBackwordCursor(account);

        if (cursor.moveToPosition(position)) {
            long id = cursor.getLong(cursor.getColumnIndex(InteractionsSQLiteHelper.COLUMN_ID));
            ContentValues cv = new ContentValues();
            cv.put(InteractionsSQLiteHelper.COLUMN_UNREAD, 0);

            try {
                database.update(InteractionsSQLiteHelper.TABLE_INTERACTIONS, cv, InteractionsSQLiteHelper.COLUMN_ID + " = ?", new String[] {id + ""});
            } catch (Exception e) {
                open();
                database.update(InteractionsSQLiteHelper.TABLE_INTERACTIONS, cv, InteractionsSQLiteHelper.COLUMN_ID + " = ?", new String[] {id + ""});
            }
        }

        cursor.close();
    }

    public synchronized String getUsers(int account, int position, boolean unread) {
        Cursor cursor;

        if (unread) {
            cursor = getUnreadBackwordCursor(account);
        } else {
            cursor = getBackwordCursor(account);
        }

        String users = "";

        if (cursor.moveToPosition(position)) {
            users = cursor.getString(cursor.getColumnIndex(InteractionsSQLiteHelper.COLUMN_USERS));
        }

        cursor.close();

        return users;
    }

    public synchronized void markAllRead(int account) {

        ContentValues cv = new ContentValues();
        cv.put(InteractionsSQLiteHelper.COLUMN_UNREAD, 0);

        try {
            database.update(InteractionsSQLiteHelper.TABLE_INTERACTIONS, cv, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + InteractionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[] {account + "", "1"});
        } catch (Exception e) {
            open();
            database.update(InteractionsSQLiteHelper.TABLE_INTERACTIONS, cv, InteractionsSQLiteHelper.COLUMN_ACCOUNT + " = ? AND " + InteractionsSQLiteHelper.COLUMN_UNREAD + " = ?", new String[] {account + "", "1"});
        }
    }

}
