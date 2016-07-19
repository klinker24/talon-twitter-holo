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

package com.klinker.android.twitter.ui.main_fragments.other_fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import com.klinker.android.twitter.adapters.TimeLineCursorAdapter;
import com.klinker.android.twitter.data.sq_lite.MentionsDataSource;
import com.klinker.android.twitter.utils.Utils;
import twitter4j.Twitter;

public class SecondAccMentionsFragment extends MentionsFragment {

    public BroadcastReceiver refreshSecondMentions = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getCursorAdapter(false);
        }
    };

    @Override
    public int getCurrentAccount() {
        if (sharedPrefs.getInt("current_account", 1) == 1) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public Twitter getTwitter() {
        return Utils.getSecondTwitter(context);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.twitter.REFRESH_SECOND_MENTIONS");
        context.registerReceiver(refreshSecondMentions, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        context.unregisterReceiver(refreshSecondMentions);
    }

    @Override
    public void getCursorAdapter(boolean showSpinner) {
        if (showSpinner) {
            try {
                spinner.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            } catch (Exception e) { }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor;
                try {
                    cursor = MentionsDataSource.getInstance(context).getCursor(currentAccount);
                } catch (Exception e) {
                    MentionsDataSource.dataSource = null;
                    getCursorAdapter(true);
                    return;
                }

                try {
                    Log.v("talon_databases", "mentions cursor size: " + cursor.getCount());
                } catch (Exception e) {
                    MentionsDataSource.dataSource = null;
                    getCursorAdapter(true);
                    return;
                }

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor c = null;
                        if (cursorAdapter != null) {
                            c = cursorAdapter.getCursor();
                        }

                        cursorAdapter = new TimeLineCursorAdapter(context, true, cursor);

                        try {
                            spinner.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                        } catch (Exception e) { }

                        attachCursor();

                        if (c != null) {
                            try {
                                c.close();
                            } catch (Exception e) {

                            }
                        }
                    }
                });
            }
        }).start();
    }

    public TimeLineCursorAdapter setAdapter(Cursor c) {
        return new TimeLineCursorAdapter(context, true, c);
    }

    @Override
    public void syncSecondMentions() {
        // we won't do anything here
    }
}
