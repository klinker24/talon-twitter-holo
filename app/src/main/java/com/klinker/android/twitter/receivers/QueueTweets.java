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
import android.util.Log;

import com.klinker.android.twitter.data.sq_lite.QueuedDataSource;
import com.klinker.android.twitter.services.CatchupPull;
import com.klinker.android.twitter.services.SendQueue;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.Utils;

import java.util.Calendar;

public class QueueTweets extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Log.v("talon_queued", "connectivity change: just starting receiver");

        AppSettings settings = AppSettings.getInstance(context);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long now = Calendar.getInstance().getTimeInMillis();
        long alarm = now + 10000; // schedule it to begin in 20 seconds

        PendingIntent pendingIntent = PendingIntent.getService(context, 253, new Intent(context, SendQueue.class), 0);

        am.cancel(pendingIntent); // cancel the old one, then start the new one in 1 min

        if (Utils.hasInternetConnection(context) &&
                QueuedDataSource.getInstance(context).getQueuedTweets(settings.currentAccount).length > 0) {
            Log.v("talon_queued", "setting alarm for queue");
            am.set(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);
        }
    }
}