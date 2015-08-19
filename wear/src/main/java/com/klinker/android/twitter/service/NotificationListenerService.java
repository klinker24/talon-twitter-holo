/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.twitter.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.activity.WearActivity;
import com.klinker.android.twitter.transaction.KeyProperties;

public class NotificationListenerService extends WearableListenerService {

    private static final String TAG = "NotificationListenerService";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "received message");
        if (messageEvent.getPath().equals(KeyProperties.PATH_NOTIFICATION)) {
            final DataMap map = DataMap.fromByteArray(messageEvent.getData());

            Intent notificationIntent = new Intent(this, WearActivity.class);
            PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(map.getString(KeyProperties.KEY_USER_NAME))
                    .setContentText(map.getString(KeyProperties.KEY_TWEET))
                    .setTicker(map.getString(KeyProperties.KEY_TWEET))
                    .addAction(R.drawable.ic_logo, getString(R.string.view_articles), notificationPendingIntent)
                    .build();
            notification.defaults = Notification.DEFAULT_ALL;

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, notification);
        }
    }
}
