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

package com.klinker.android.twitter.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.sq_lite.HomeContentProvider;
import com.klinker.android.twitter.utils.IOUtils;
import com.klinker.android.twitter.utils.redirects.RedirectToPlayStore;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

public class TrimDataService extends IntentService {

    SharedPreferences sharedPrefs;

    public static final int TRIM_ID = 161;

    public TrimDataService() {
        super("TrimDataService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.v("trimming_database", "trimming database from service");
        IOUtils.trimDatabase(getApplicationContext(), 1); // trims first account
        IOUtils.trimDatabase(getApplicationContext(), 2); // trims second account

        getContentResolver().notifyChange(HomeContentProvider.CONTENT_URI, null);

        setNextTrim(this);

        //checkForUpdate();
    }

    public void setNextTrim(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long now = new Date().getTime();
        long alarm = now + AlarmManager.INTERVAL_DAY;

        Log.v("alarm_date", "auto trim " + new Date(alarm).toString());

        PendingIntent pendingIntent = PendingIntent.getService(context, TRIM_ID, new Intent(context, TrimDataService.class), 0);

        am.set(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);
    }

    public void checkForUpdate() {
        String onlineVersion = getVersion();

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String currentVersion = pInfo.versionName;

            if (!onlineVersion.contains(currentVersion)) {
                notifyNewVersion(onlineVersion);
            }
        } catch (Exception e) {

        }
    }

    public void notifyNewVersion(String version) {
        Intent goToStore = new Intent(this, RedirectToPlayStore.class);
        PendingIntent storePending = PendingIntent.getActivity(this, 0, goToStore, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Talon Version " + version);
        builder.setContentText("Click to update.");
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher));
        builder.setSmallIcon(R.drawable.ic_stat_icon);
        builder.setContentIntent(storePending);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        notificationManager.notify(552, builder.build());
    }

    public Document getDoc() {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("https://play.google.com/store/apps/details?id=com.klinker.android.twitter");
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
                Log.v("talon_version", line);
            }

            String docHtml = sb.toString();

            is.close();

            return Jsoup.parse(docHtml);
        } catch (Exception e) {
            return null;
        }
    }

    public String getVersion() {
        try {
            Document doc = getDoc();

            if(doc != null) {
                Elements elements = doc.getElementsByAttributeValue("itemprop", "softwareVersion");

                Log.v("talon_version", "elements size: " + elements.size());
                for (Element e : elements) {
                    Log.v("talon_version", e.val());
                    return e.val();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return null;
    }
}
