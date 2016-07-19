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

package com.klinker.android.twitter.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.sq_lite.HomeDataSource;
import com.klinker.android.twitter.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter.services.WidgetRefreshService;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.compose.WidgetCompose;
import com.klinker.android.twitter.ui.tweet_viewer.TweetActivityWidget;
import com.klinker.android.twitter.utils.redirects.RedirectToDMs;
import com.klinker.android.twitter.utils.redirects.RedirectToMentions;
import com.klinker.android.twitter.utils.redirects.RedirectToTimeline;

public class UnreadWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent updateWidget = new Intent(context, UnreadWidgetService.class);
        context.startService(updateWidget);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.klinker.android.talon.UPDATE_WIDGET")) {
            Intent updateWidget = new Intent(context, UnreadWidgetService.class);
            context.startService(updateWidget);
        } else {
            super.onReceive(context, intent);
        }
    }

    public static class UnreadWidgetService extends IntentService {
        public UnreadWidgetService() {
            super("unread_widget_service");
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        @Override
        protected void onHandleIntent(Intent arg0) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            ComponentName thisAppWidget = new ComponentName(this.getPackageName(), UnreadWidgetProvider.class.getName());
            int[] appWidgetIds = mgr.getAppWidgetIds(thisAppWidget);

            Log.v("talon_unread_widget", "running service");

            int res = 0;
            switch (Integer.parseInt(getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE)
                    .getString("widget_theme", "3"))) {
                case 0:
                    res = R.layout.widget_unread_trans_light;
                    break;
                case 1:
                    res = R.layout.widget_unread_trans_black;
                    break;
                case 2:
                    res = R.layout.widget_unread_trans_light;
                    break;
                case 3:
                    res = R.layout.widget_unread_trans_black;
                    break;
            }

            RemoteViews views = new RemoteViews(this.getPackageName(), res);

            for (int i = 0; i < appWidgetIds.length; i++) {
                Log.v("talon_unread_widget", "in for loop");
                int appWidgetId = appWidgetIds[i];

                Intent quickText = new Intent(this, WidgetCompose.class);
                quickText.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent quickPending = PendingIntent.getActivity(this, 0, quickText, 0);

                Intent intent2 = new Intent(this, WidgetService.class);
                intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent2.setData(Uri.parse(intent2.toUri(Intent.URI_INTENT_SCHEME)));

                Intent openApp = new Intent(this, RedirectToTimeline.class);
                openApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent openAppPending = PendingIntent.getActivity(this, 0, openApp, 0);

                Intent mentions = new Intent(this, RedirectToMentions.class);
                mentions.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                PendingIntent mentionsPending = PendingIntent.getActivity(this, 0, mentions, 0);

                Intent dms = new Intent(this, RedirectToDMs.class);
                dms.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                PendingIntent dmsPending = PendingIntent.getActivity(this, 0, dms, 0);

                views.setOnClickPendingIntent(R.id.launcherIcon, openAppPending);
                views.setOnClickPendingIntent(R.id.replyButton, quickPending);
                views.setOnClickPendingIntent(R.id.timeline, openAppPending);
                views.setOnClickPendingIntent(R.id.mentions, mentionsPending);
                views.setOnClickPendingIntent(R.id.dms, dmsPending);

                // get the counts
                try {
                    SharedPreferences sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                            Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
                    int currentAccount = sharedPrefs.getInt("current_account", 1);
                    String dm = sharedPrefs.getInt("dm_unread_" + currentAccount, 0) + "";
                    String mention = MentionsDataSource.getInstance(this).getUnreadCount(currentAccount) + "";
                    String home = HomeDataSource.getInstance(this).getPosition(currentAccount, sharedPrefs.getLong("current_position_" + currentAccount, 0)) + "";

                    views.setTextViewText(R.id.home_text, home);
                    views.setTextViewText(R.id.mention_text, mention);
                    views.setTextViewText(R.id.dm_text, dm);

                    mgr.updateAppWidget(appWidgetId, views);
                } catch (Exception e) {

                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetList);
            }

            stopSelf();
        }
    }
}