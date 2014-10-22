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


public class HashtagDataSource {

    // provides access to the database
    public static HashtagDataSource dataSource = null;

    /**
     * This is used so that we don't have to open and close the database on different threads or fragments
     * every time. This will facilitate it between all of them to avoid Illegal State Exceptions.
     */
    public static HashtagDataSource getInstance(Context context) {

        // if the datasource isn't open or it the object is null
        if (dataSource == null ||
                dataSource.getDatabase() == null ||
                !dataSource.getDatabase().isOpen()) {

            dataSource = new HashtagDataSource(context); // create the database
            dataSource.open(); // open the database
        }

        return dataSource;
    }

    private SQLiteDatabase database;
    private HashtagSQLiteHelper dbHelper;
    public String[] allColumns = { HashtagSQLiteHelper.COLUMN_ID, HashtagSQLiteHelper.COLUMN_TAG };

    public HashtagDataSource(Context context) {
        dbHelper = new HashtagSQLiteHelper(context);
    }

    public void open() throws SQLException {

        try {
            database = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
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

    public HashtagSQLiteHelper getHelper() {
        return dbHelper;
    }

    public synchronized void createTag(String tag) {
        ContentValues values = new ContentValues();

        values.put(HashtagSQLiteHelper.COLUMN_TAG, tag);

        if (database == null || !database.isOpen()) {
            open();
        }

        try {
            database.insert(HashtagSQLiteHelper.TABLE_HASHTAGS, null, values);
        } catch (Exception e) {

        }
    }

    public synchronized void deleteTag(String tag) {
        try {
            database.delete(HashtagSQLiteHelper.TABLE_HASHTAGS, HashtagSQLiteHelper.COLUMN_TAG
                    + " = ?", new String[] {tag});
        } catch (Exception e) {
            open();
            try {
                database.delete(HashtagSQLiteHelper.TABLE_HASHTAGS, HashtagSQLiteHelper.COLUMN_TAG
                        + " = ?", new String[] {tag});
            } catch (SQLException x) {
                // something was wrong with the syntax on on of the tags '#2point8' was the bad one
                // log just said error compiling with syntax error at 'point8'

                // seems like it is being reported by any tweets with a '#' then a number immediately following it

                // do not know what would cause this just yet or why it didn't happen on mine
                // the auto complete works for it as well. not sure what is going on
            }
        }
    }

    public synchronized void deleteAllTags(int account) {

        try {
            database.delete(HashtagSQLiteHelper.TABLE_HASHTAGS, null, null);
        } catch (Exception e) {
            open();
            database.delete(HashtagSQLiteHelper.TABLE_HASHTAGS, null, null);
        }
    }

    public synchronized Cursor getCursor(String tag) {

        Cursor cursor;
        try {
            cursor = database.query(HashtagSQLiteHelper.TABLE_HASHTAGS,
                    allColumns,
                    HashtagSQLiteHelper.COLUMN_TAG + " LIKE '%" + tag + "%'",
                    null, null, null, null);
        } catch (Exception e) {
            open();
            cursor = database.query(HashtagSQLiteHelper.TABLE_HASHTAGS,
                    allColumns,
                    HashtagSQLiteHelper.COLUMN_TAG + " LIKE '%" + tag + "%'",
                    null, null, null, null);
        }

        return cursor;
    }
}

