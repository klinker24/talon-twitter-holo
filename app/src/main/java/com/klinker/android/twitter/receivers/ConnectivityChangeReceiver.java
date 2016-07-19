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

package com.klinker.android.twitter.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;

import com.klinker.android.twitter.services.CatchupPull;
import com.klinker.android.twitter.services.TimelineRefreshService;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Log.v("talon_pull", "connectivity change: just starting receiver");

        AppSettings settings = AppSettings.getInstance(context);

        // we don't want to do anything here if talon pull isn't on
        if (!settings.pushNotifications) {
            Log.v("talon_pull", "connectivity change: stopping the receiver very early");
            return;
        }

        if (Utils.hasInternetConnection(context)) {
            Log.v("talon_pull", "connectivity change: network is available and talon pull is on");

            // we want to turn off the live streaming/talon pull because it is just wasting battery not working/looking for connection
            context.sendBroadcast(new Intent("com.klinker.android.twitter.STOP_PUSH_SERVICE"));

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            long now = Calendar.getInstance().getTimeInMillis();
            long alarm = now + 60000; // schedule it to begin in 1 min

            PendingIntent pendingIntent = PendingIntent.getService(context, 236, new Intent(context, CatchupPull.class), 0);

            am.cancel(pendingIntent); // cancel the old one, then start the new one in 1 min
            am.set(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);

        } else {
            Log.v("talon_pull", "connectivity change: network not available but talon pull is on");

            // we want to turn off the live streaming/talon pull because it is just wasting battery not working/looking for connection
            context.sendBroadcast(new Intent("com.klinker.android.twitter.STOP_PUSH_SERVICE"));
        }
    }
}