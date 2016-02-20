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

package com.klinker.android.twitter.ui.drawer_activities.lists;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.*;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.ArrayListLoader;
import com.klinker.android.twitter.adapters.TimelineArrayAdapter;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.manipulations.widgets.swipe_refresh_layout.FullScreenSwipeRefreshLayout;
import com.klinker.android.twitter.manipulations.widgets.swipe_refresh_layout.SwipeProgressBar;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.ui.setup.LoginActivity;
import com.klinker.android.twitter.utils.Utils;

import org.lucasr.smoothie.AsyncListView;
import org.lucasr.smoothie.ItemManager;

import java.util.ArrayList;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import uk.co.senab.bitmapcache.BitmapLruCache;

public class ChoosenListActivity extends Activity {

    public AppSettings settings;
    private Context context;
    private SharedPreferences sharedPrefs;

    private ActionBar actionBar;

    private AsyncListView listView;

    private long listId;
    private String listName;

    private FullScreenSwipeRefreshLayout mPullToRefreshLayout;
    private LinearLayout spinner;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        context = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        settings = AppSettings.getInstance(this);

        if (settings.advanceWindowed) {
            setUpWindow();
        }

        Utils.setUpPopupTheme(this, settings);

        actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.lists));

        setContentView(R.layout.ptr_list_layout);

        if (!settings.isTwitterLoggedIn) {
            Intent login = new Intent(context, LoginActivity.class);
            startActivity(login);
            finish();
        }

        mPullToRefreshLayout = (FullScreenSwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        spinner = (LinearLayout) findViewById(R.id.list_progress);

        mPullToRefreshLayout.setOnRefreshListener(new FullScreenSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshStarted();
            }
        });
        if (settings.addonTheme) {
            mPullToRefreshLayout.setColorScheme(settings.accentInt,
                    SwipeProgressBar.COLOR2,
                    settings.accentInt,
                    SwipeProgressBar.COLOR3);
        } else {
            if (settings.theme != AppSettings.THEME_LIGHT) {
                mPullToRefreshLayout.setColorScheme(context.getResources().getColor(R.color.app_color),
                        SwipeProgressBar.COLOR2,
                        context.getResources().getColor(R.color.app_color),
                        SwipeProgressBar.COLOR3);
            } else {
                mPullToRefreshLayout.setColorScheme(context.getResources().getColor(R.color.app_color),
                        getResources().getColor(R.color.light_ptr_1),
                        context.getResources().getColor(R.color.app_color),
                        getResources().getColor(R.color.light_ptr_2));
            }
        }

        listView = (AsyncListView) findViewById(R.id.listView);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == totalItemCount && canRefresh) {
                    getLists();
                }
            }
        });

        BitmapLruCache cache = App.getInstance(context).getBitmapCache();
        ArrayListLoader loader = new ArrayListLoader(cache, context);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(50);
        builder.setThreadPoolSize(4);

        listView.setItemManager(builder.build());

        listName = getIntent().getStringExtra("list_name");

        listId = Long.parseLong(getIntent().getStringExtra("list_id"));
        actionBar.setTitle(listName);

        getLists();

        Utils.setActionBar(context);

    }

    public void setUpWindow() {

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        params.dimAmount = .75f;  // set it higher if you want to dim behind the window
        getWindow().setAttributes(params);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .9), (int) (height * .8));
        } else {
            getWindow().setLayout((int) (width * .7), (int) (height * .8));
        }

    }

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }

    public void onRefreshStarted() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);

                    paging.setPage(1);

                    ResponseList<twitter4j.Status> lists = twitter.getUserListStatuses(listId, paging);

                    statuses.clear();

                    for (Status status : lists) {
                        statuses.add(status);
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new TimelineArrayAdapter(context, statuses);
                            listView.setAdapter(adapter);
                            listView.setVisibility(View.VISIBLE);

                            spinner.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();

                }

                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    public Paging paging = new Paging(1, 20);
    private ArrayList<Status> statuses = new ArrayList<Status>();
    private TimelineArrayAdapter adapter;
    private boolean canRefresh = false;

    public void getLists() {
        canRefresh = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);

                    ResponseList<twitter4j.Status> lists = twitter.getUserListStatuses(listId, paging);

                    paging.setPage(paging.getPage() + 1);

                    for (Status status : lists) {
                        statuses.add(status);
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (adapter == null) {
                                adapter = new TimelineArrayAdapter(context, statuses);
                                listView.setAdapter(adapter);
                                listView.setVisibility(View.VISIBLE);
                            } else {
                                adapter.notifyDataSetChanged();
                            }

                            spinner.setVisibility(View.GONE);
                            canRefresh = true;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setVisibility(View.GONE);
                            canRefresh = false;
                        }
                    });
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setVisibility(View.GONE);
                            canRefresh = false;
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.choosen_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_to_first:
                try {
                    listView.setSelectionFromTop(0,0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            default:
                return true;
        }
    }
}
