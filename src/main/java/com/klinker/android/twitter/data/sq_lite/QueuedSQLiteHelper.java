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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class QueuedSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_QUEUED = "queued";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ACCOUNT = "account";
    public static final String COLUMN_TEXT = "_text";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_ALARM_ID = "alarm_id";
    public static final String COLUMN_TYPE = "type";

    private static final String DATABASE_NAME = "queued.db";
    private static final int DATABASE_VERSION = 1;

    public static final int TYPE_SCHEDULED = 0;
    public static final int TYPE_DRAFT = 1;
    public static final int TYPE_QUEUED_TWEET = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_QUEUED + "(" + COLUMN_ID
            + " integer primary key, " + COLUMN_ACCOUNT
            + " integer account num, " + COLUMN_TEXT
            + " text send, " + COLUMN_TIME
            + " integer send tweet, " + COLUMN_TYPE
            + " integer type of queued tweet, " + COLUMN_ALARM_ID
            + " integer alarm identifier); ";

    public QueuedSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(HomeSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUEUED);
        onCreate(db);
    }

}