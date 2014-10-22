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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.sq_lite.*;
import com.klinker.android.twitter.settings.AppSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class IOUtils {

    public static Uri saveImage(Bitmap finalBitmap, String d, Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Talon");
        myDir.mkdirs();
        String fname = d + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Toast.makeText(context, context.getResources().getString(R.string.save_image), Toast.LENGTH_SHORT).show();

        return Uri.fromFile(file);
    }

    public static String getPath(Uri uri, Context context) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        String filePath = null;

        try {
            Cursor cursor = context.getContentResolver().query(
                    uri, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            filePath = uri.getPath();
        }

        if (filePath == null) {
            filePath = uri.getPath();
            Log.v("talon_file_path", filePath);
        }

        return filePath;
    }

    public static boolean loadSharedPreferencesFromFile(File src, Context context) {
        boolean res = false;
        ObjectInputStream input = null;

        try {
            if (!src.getParentFile().exists()) {
                src.getParentFile().mkdirs();
                src.createNewFile();
            }

            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE)
                    .edit();

            prefEdit.clear();

            @SuppressWarnings("unchecked")
            Map<String, ?> entries = (Map<String, ?>) input.readObject();

            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean) {
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                } else if (v instanceof Float) {
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                } else if (v instanceof Integer) {
                    prefEdit.putInt(key, ((Integer) v).intValue());
                } else if (v instanceof Long) {
                    prefEdit.putLong(key, ((Long) v).longValue());
                } else if (v instanceof String) {
                    prefEdit.putString(key, ((String) v));
                }
            }

            prefEdit.commit();

            res = true;
        } catch (Exception e) {

        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {

            }
        }

        return res;
    }

    public static boolean saveSharedPreferencesToFile(File dst, Context context) {
        boolean res = false;
        ObjectOutputStream output = null;

        try {
            if (!dst.getParentFile().exists()) {
                dst.getParentFile().mkdirs();
                dst.createNewFile();
            }

            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

            output.writeObject(pref.getAll());

            res = true;
        } catch (Exception e) {

        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (Exception e) {

            }
        }

        return res;
    }

    public static String readChangelog(Context context) {
        String ret = "";
        try {
            AssetManager assetManager = context.getAssets();
            Scanner in = new Scanner(assetManager.open("changelog.txt"));

            while (in.hasNextLine()) {
                ret += in.nextLine() + "\n";
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {

        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static boolean trimDatabase(Context context, int account) {
        try {
            AppSettings settings = AppSettings.getInstance(context);
            SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

            InteractionsDataSource interactions = InteractionsDataSource.getInstance(context);
            Cursor inters = interactions.getCursor(account);

            if (inters.getCount() > 50) {
                if (inters.moveToPosition(inters.getCount() - 50)) {
                    do {
                        interactions.deleteInteraction(inters.getLong(inters.getColumnIndex(InteractionsSQLiteHelper.COLUMN_ID)));
                    } while (inters.moveToPrevious());
                }
            }

            inters.close();

            HomeDataSource home = HomeDataSource.getInstance(context);

            home.deleteDups(settings.currentAccount);

            Cursor timeline = home.getTrimmingCursor(account);

            Log.v("trimming", "timeline size: " + timeline.getCount());
            Log.v("trimming", "timeline settings size: " + settings.timelineSize);
            if (timeline.getCount() > settings.timelineSize) {

                if(timeline.moveToPosition(timeline.getCount() - settings.timelineSize)) {
                    Log.v("trimming", "in the trim section");
                    do {
                        home.deleteTweet(timeline.getLong(timeline.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID)));
                    } while (timeline.moveToPrevious());
                }
            }

            timeline.close();

            // trimming the lists
            ListDataSource lists = ListDataSource.getInstance(context);

            int account1List1 = sharedPrefs.getInt("account_" + account + "_list_1", 0);
            int account1List2 = sharedPrefs.getInt("account_" + account + "_list_2", 0);

            lists.deleteDups(account1List1);
            lists.deleteDups(account1List2);

            Cursor list1 = lists.getTrimmingCursor(account1List1);

            Log.v("trimming", "lists size: " + list1.getCount());
            Log.v("trimming", "lists settings size: " + 400);
            if (list1.getCount() > 400) {

                if(list1.moveToPosition(list1.getCount() - 400)) {
                    Log.v("trimming", "in the trim section");
                    do {
                        lists.deleteTweet(list1.getLong(list1.getColumnIndex(ListSQLiteHelper.COLUMN_TWEET_ID)));
                    } while (list1.moveToPrevious());
                }
            }
            list1.close();

            Cursor list2 = lists.getTrimmingCursor(account1List2);

            Log.v("trimming", "lists size: " + list2.getCount());
            Log.v("trimming", "lists settings size: " + 400);
            if (list2.getCount() > 400) {

                if(list2.moveToPosition(list2.getCount() - 400)) {
                    Log.v("trimming", "in the trim section");
                    do {
                        lists.deleteTweet(list2.getLong(list2.getColumnIndex(ListSQLiteHelper.COLUMN_TWEET_ID)));
                    } while (list2.moveToPrevious());
                }
            }

            list2.close();

            MentionsDataSource mentions = MentionsDataSource.getInstance(context);

            mentions.deleteDups(settings.currentAccount);

            timeline = mentions.getTrimmingCursor(account);

            Log.v("trimming", "mentions size: " + timeline.getCount());
            Log.v("trimming", "mentions settings size: " + settings.mentionsSize);
            if (timeline.getCount() > settings.mentionsSize) {

                if(timeline.moveToPosition(timeline.getCount() - settings.mentionsSize)) {
                    do {
                        mentions.deleteTweet(timeline.getLong(timeline.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID)));
                    } while (timeline.moveToPrevious());
                }
            }

            timeline.close();

            DMDataSource dm = DMDataSource.getInstance(context);

            dm.deleteDups(settings.currentAccount);

            timeline = dm.getCursor(account);

            Log.v("trimming", "dm size: " + timeline.getCount());
            Log.v("trimming", "dm settings size: " + settings.dmSize);

            if (timeline.getCount() > settings.dmSize) {
                if(timeline.moveToPosition(timeline.getCount() - settings.dmSize)) {
                    do {
                        dm.deleteTweet(timeline.getLong(timeline.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID)));
                    } while (timeline.moveToPrevious());
                }
            }

            timeline.close();

            HashtagDataSource hashtag = HashtagDataSource.getInstance(context);

            timeline = hashtag.getCursor("");

            Log.v("trimming", "hashtag size: " + timeline.getCount());

            if (timeline.getCount() > 300) {
                if(timeline.moveToPosition(timeline.getCount() - 300)) {
                    do {
                        hashtag.deleteTag(timeline.getString(timeline.getColumnIndex(HashtagSQLiteHelper.COLUMN_TAG)));
                    } while (timeline.moveToPrevious());
                }
            }

            timeline.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static long dirSize(File dir) {
        long result = 0;

        Stack<File> dirlist= new Stack<File>();
        dirlist.clear();

        dirlist.push(dir);

        while(!dirlist.isEmpty())
        {
            File dirCurrent = dirlist.pop();

            File[] fileList = dirCurrent.listFiles();
            if (fileList != null) {
                for (int i = 0; i < fileList.length; i++) {

                    if (fileList[i].isDirectory())
                        dirlist.push(fileList[i]);
                    else
                        result += fileList[i].length();
                }
            }
        }

        return result;
    }

    public static String readAsset(Context context, String assetTitle) {
        String ret = "";
        try {
            AssetManager assetManager = context.getAssets();
            Scanner in = new Scanner(assetManager.open(assetTitle));

            while (in.hasNextLine()) {
                ret += in.nextLine() + "\n";
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }
}
