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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.net.Uri;
import android.os.Binder;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.util.Log;

import com.klinker.android.twitter.utils.TweetLinkUtils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;

public class HomeContentProvider extends ContentProvider {
    static final String TAG = "HomeTimeline";

    public static final String AUTHORITY = "com.klinker.android.twitter.provider";
    static final String BASE_PATH = "tweet_id";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final Uri STREAM_NOTI = Uri.parse("content://" + AUTHORITY + "/" + "stream");

    private Context context;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        context = getContext();

        return true;
    }

    @Override
    public String getType(Uri uri) {
        String ret = getContext().getContentResolver().getType(System.CONTENT_URI);
        Log.d(TAG, "getType returning: " + ret);
        return ret;
    }

    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert uri: " + uri.toString());

        if (!checkUID(context)) {
            return null;
        }

        SQLiteDatabase db = HomeDataSource.getInstance(getContext()).getDatabase();
        long rowID;
        try {
            rowID = db.insert(HomeSQLiteHelper.TABLE_HOME, null, values);
        } catch (IllegalStateException e) {
            // shouldn't happen here, but might i guess
            HomeDataSource.dataSource = null;
            db = HomeDataSource.getInstance(context).getDatabase();
            rowID = db.insert(HomeSQLiteHelper.TABLE_HOME, null, values);
        }

        getContext().getContentResolver().notifyChange(HomeContentProvider.CONTENT_URI, null);

        return Uri.parse(BASE_PATH + "/" + rowID);
    }

    @Override
    public synchronized int bulkInsert(Uri uri, ContentValues[] allValues) {

        if (checkUID(context)) {
            HomeDataSource dataSource = HomeDataSource.getInstance(context);
            int inserted = dataSource.insertMultiple(allValues);
            context.getContentResolver().notifyChange(HomeContentProvider.CONTENT_URI, null);
            return inserted;
        } else {
            return 0;
        }

    }

    private boolean checkUID(Context context) {
        int callingUid = Binder.getCallingUid();

        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(
                PackageManager.GET_META_DATA);

        int launcherUid = 0;
        int twitterUid = 0;

        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals("com.klinker.android.twitter")){
                //get the UID for the selected app
                twitterUid = packageInfo.uid;
            }
            if(packageInfo.packageName.equals("com.klinker.android.launcher")){
                //get the UID for the selected app
                launcherUid = packageInfo.uid;
            }
        }

        if (callingUid == launcherUid || callingUid == twitterUid) {
            return true;
        } else {
            return false;
        }
    }

    private int insertMultiple(ContentValues[] allValues) {
        int rowsAdded = 0;
        long rowId;
        ContentValues values;

        SQLiteDatabase db = HomeDataSource.getInstance(getContext()).getDatabase();

        try {
            db.beginTransaction();

            for (ContentValues initialValues : allValues) {
                values = initialValues == null ? new ContentValues() : new ContentValues(initialValues);
                try {
                    rowId = db.insert(HomeSQLiteHelper.TABLE_HOME, null, values);
                } catch (IllegalStateException e) {
                    return rowsAdded;
                    //db = HomeDataSource.getInstance(context).getDatabase();
                    //rowId = 0;
                }
                if (rowId > 0)
                    rowsAdded++;
            }

            db.setTransactionSuccessful();
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
                db.endTransaction();
            } catch (Exception e) {
                // shouldn't happen unless it gets caught above from an illegal state
            }
        }

        return rowsAdded;
    }

    // arg[0] is the account
    // arg[1] is the position
    // arg[2] is true if it is position sent, false if it is id number
    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        if (!checkUID(context)) {
            return 0;
        }

        boolean positionSent = Boolean.parseBoolean(selectionArgs[2]);

        if (positionSent) {
            int pos = Integer.parseInt(selectionArgs[1]);
            int account = Integer.parseInt(selectionArgs[0]);

            HomeDataSource dataSource = HomeDataSource.getInstance(context);
            SQLiteDatabase db = dataSource.getDatabase();

            Cursor cursor = dataSource.getCursor(account);

            if (cursor.moveToPosition(pos)) {

                dataSource.removeCurrent(account);

                long tweetId = cursor.getLong(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID));

                ContentValues cv = new ContentValues();
                cv.put(HomeSQLiteHelper.COLUMN_CURRENT_POS, "1");

                ContentValues unread = new ContentValues();
                unread.put(HomeSQLiteHelper.COLUMN_CURRENT_POS, "");

                try {
                    db.update(HomeSQLiteHelper.TABLE_HOME, unread, HomeSQLiteHelper.COLUMN_CURRENT_POS + " = ? AND " + HomeSQLiteHelper.COLUMN_ACCOUNT + " = ?", new String[]{"1", account + ""});
                    db.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[]{tweetId + ""});
                } catch (Exception e) {

                }
            }
        } else {
            long id = Long.parseLong(selectionArgs[1]);
            int account = Integer.parseInt(selectionArgs[0]);
            Log.v("talon_launcher_stuff", "id: " + id);

            HomeDataSource dataSource = HomeDataSource.getInstance(context);
            SQLiteDatabase db = dataSource.getDatabase();

            ContentValues cv = new ContentValues();
            cv.put(HomeSQLiteHelper.COLUMN_CURRENT_POS, "1");

            ContentValues unread = new ContentValues();
            unread.put(HomeSQLiteHelper.COLUMN_CURRENT_POS, "");

            try {
                db.update(HomeSQLiteHelper.TABLE_HOME, unread, HomeSQLiteHelper.COLUMN_CURRENT_POS + " = ? AND " + HomeSQLiteHelper.COLUMN_ACCOUNT + " = ?", new String[]{"1", account + ""});
                db.update(HomeSQLiteHelper.TABLE_HOME, cv, HomeSQLiteHelper.COLUMN_TWEET_ID + " = ?", new String[]{id + ""});
            } catch (Exception e) {

            }
        }

        context.getContentResolver().notifyChange(uri, null);

        return 1;
    }

    @Override
    public synchronized int delete(Uri uri, String id, String[] selectionArgs) {

        if (!checkUID(context)) {
            return 0;
        }
        Log.d(TAG, "delete uri: " + uri.toString());
        SQLiteDatabase db = HomeDataSource.getInstance(getContext()).getDatabase();
        int count;

        String segment = uri.getLastPathSegment();
        count = db.delete(HomeSQLiteHelper.TABLE_HOME, HomeSQLiteHelper.COLUMN_TWEET_ID
                + " = " + id, null);

        if (count > 0) {
            // Notify the Context's ContentResolver of the change
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (!checkUID(context)) {
            return null;
        }

        Log.d(TAG, "query with uri: " + uri.toString());

        //SQLiteDatabase db = helper.getWritableDatabase();

        // A convenience class to help build the query
        HomeDataSource data = HomeDataSource.getInstance(context);
        Cursor c = data.getCursor(Integer.parseInt(selectionArgs[0]));//qb.query(db,
                //projection, HomeSQLiteHelper.COLUMN_ACCOUNT + " = " + selectionArgs[0], null, null, null, HomeSQLiteHelper.COLUMN_TWEET_ID + " ASC");
        c.setNotificationUri(context.getContentResolver(), uri);

        return c;
    }

    public static void insertTweet(Status status, int currentAccount, Context context) {
        ContentValues values = new ContentValues();
        String originalName = "";
        long id = status.getId();
        long time = status.getCreatedAt().getTime();

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

        values.put(HomeSQLiteHelper.COLUMN_ACCOUNT, currentAccount);
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

        context.getContentResolver().insert(HomeContentProvider.CONTENT_URI, values);
    }

    public static void updateCurrent(int currentAccount, Context context, int position) {
        context.getContentResolver().update(HomeContentProvider.CONTENT_URI, new ContentValues(), "",
                new String[] {currentAccount + "", position + "", "true"});
    }

    public static void updateCurrent(int currentAccount, Context context, long id) {
        context.getContentResolver().update(HomeContentProvider.CONTENT_URI, new ContentValues(), "",
                new String[] {currentAccount + "", id + "", "false"});
    }

    public static int insertTweets(List<Status> statuses, int currentAccount, Context context) {
        ContentValues[] valueses = new ContentValues[statuses.size()];

        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);

            ContentValues values = new ContentValues();
            String originalName = "";
            long mId = status.getId();
            long time = status.getCreatedAt().getTime();

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

            valueses[i] = values;
        }

        return context.getContentResolver().bulkInsert(HomeContentProvider.CONTENT_URI, valueses);
    }
}