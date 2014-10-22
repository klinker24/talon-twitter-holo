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

package com.klinker.android.twitter.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.drawer_activities.SavedSearchesActivity;
import com.klinker.android.twitter.ui.drawer_activities.discover.trends.SearchedTrendsActivity;
import com.klinker.android.twitter.utils.Utils;

import java.util.ArrayList;

import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Twitter;

public class SavedSearchArrayAdapter extends TrendsArrayAdapter {
    public SavedSearchArrayAdapter(Context context, ArrayList<String> text) {
        super(context, text);
    }

    @Override
    public void bindView(final View view, Context mContext, final String trend) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.text.setText(trend);

        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent search = new Intent(context, SearchedTrendsActivity.class);
                search.setAction(Intent.ACTION_SEARCH);
                search.putExtra(SearchManager.QUERY, trend);
                context.startActivity(search);
            }
        });

        holder.text.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String search = trend;

                new AlertDialog.Builder(context)
                        .setTitle(context.getResources().getString(R.string.delete_saved_search))
                        .setMessage(context.getResources().getString(R.string.cache_dialog_summary))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(context, context.getString(R.string.deleting_search), Toast.LENGTH_SHORT).show();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        int id = -1;
                                        Twitter twitter = Utils.getTwitter(context, AppSettings.getInstance(context));

                                        try {
                                            ResponseList<SavedSearch> searches = twitter.savedSearches().getSavedSearches();

                                            for (int i = 0; i < searches.size(); i++) {
                                                String name = searches.get(i).getName();
                                                Log.v("talon_saved_searches", name);
                                                if (name.equals(search)) {
                                                    id = searches.get(i).getId();
                                                    Log.v("talon_saved_searches", "id to delete: " + id);
                                                }
                                            }

                                            if (id != -1) {
                                                twitter.destroySavedSearch(id);

                                                ((Activity)context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(context, context.getString(R.string.success), Toast.LENGTH_SHORT).show();
                                                        SavedSearchesActivity.getSearches();
                                                    }
                                                });
                                            } else {
                                                ((Activity)context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } catch (Exception e) {
                                            ((Activity)context).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create()
                        .show();

                return false;
            }
        });

    }
}
