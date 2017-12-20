package com.klinker.android.twitter.activities.main_fragments.other_fragments;
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import com.klinker.android.twitter.activities.main_fragments.home_fragments.HomeExtensionFragment;
import com.klinker.android.twitter.data.sq_lite.SavedTweetsDataSource;

public class SavedTweetsFragment extends HomeExtensionFragment {

    public static final String REFRESH_ACTION = "com.klinker.android.twitter.SAVED_TWEETS_REFRESHED";

    public BroadcastReceiver resetLists = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getCursorAdapter(true);
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(REFRESH_ACTION);
        context.registerReceiver(resetLists, filter);
    }

    @Override
    public Cursor getCursor() {
        return SavedTweetsDataSource.getInstance(context).getCursor(settings.currentAccount);
    }
}