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

package com.klinker.android.twitter.ui.drawer_activities.discover;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.ArrayListLoader;
import com.klinker.android.twitter.adapters.TimelineArrayAdapter;
import com.klinker.android.twitter.adapters.TrendsArrayAdapter;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.utils.Utils;

import org.lucasr.smoothie.AsyncListView;
import org.lucasr.smoothie.ItemManager;

import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Twitter;
import uk.co.senab.bitmapcache.BitmapLruCache;

public class NearbyTweets extends Fragment implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationClient mLocationClient;
    private boolean connected = false;

    private Context context;
    private AppSettings settings;

    private AsyncListView listView;
    private View layout;

    private SharedPreferences sharedPrefs;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        settings = AppSettings.getInstance(context);

        layout = inflater.inflate(R.layout.profiles_list, null);

        listView = (AsyncListView) layout.findViewById(R.id.listView);

        BitmapLruCache cache = App.getInstance(context).getBitmapCache();
        ArrayListLoader loader = new ArrayListLoader(cache, context);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(10);
        builder.setThreadPoolSize(2);

        listView.setItemManager(builder.build());

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;

                if(lastItem == totalItemCount && canRefresh) {
                    getMore();
                }
            }
        });

        if (DrawerActivity.translucent) {
            if (Utils.hasNavBar(context)) {
                View footer = new View(context);
                footer.setOnClickListener(null);
                footer.setOnLongClickListener(null);
                ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, Utils.getNavBarHeight(context));
                footer.setLayoutParams(params);
                listView.addFooterView(footer);
                listView.setFooterDividersEnabled(false);
            }
        }

        mLocationClient = new LocationClient(context, this, this);

        getTweets();

        return layout;
    }

    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
    }

    @Override
    public void onDisconnected() {
        connected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    public Query query;
    public boolean hasMore = true;
    public ArrayList<Status> statuses = new ArrayList<Status>();
    public TimelineArrayAdapter adapter;

    public void getTweets() {

        canRefresh = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, DrawerActivity.settings);

                    boolean manualLoc = sharedPrefs.getBoolean("manually_config_location", false);

                    int i = 0;
                    while (!connected && i < 5 && !manualLoc) {
                        try {
                            Thread.sleep(1500);
                        } catch (Exception e) {

                        }

                        i++;
                    }

                    double latitude = -1;
                    double longitude = -1;

                    if (manualLoc) {
                        // need to query yahoos api for the location...
                        double[] loc = getLocationFromYahoo(sharedPrefs.getInt("woeid", 2379574));
                        latitude = loc[0];
                        longitude = loc[1];
                    } else {
                        // set it from the location client
                        Location location = mLocationClient.getLastLocation();
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }

                    query = new Query();
                    query.setGeoCode(new GeoLocation(latitude,longitude), 10, Query.MILES);

                    QueryResult result = twitter.search(query);

                    if (result.hasNext()) {
                        hasMore = true;
                        query = result.nextQuery();
                    } else {
                        hasMore = false;
                    }

                    for(Status s : result.getTweets()){
                        statuses.add(s);
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new TimelineArrayAdapter(context, statuses);
                            listView.setAdapter(adapter);
                            listView.setVisibility(View.VISIBLE);

                            LinearLayout spinner = (LinearLayout) layout.findViewById(R.id.list_progress);
                            spinner.setVisibility(View.GONE);
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            } catch (IllegalStateException e) {
                                // not attached to activity
                            }
                        }
                    });
                }

                canRefresh = true;
            }
        }).start();
    }

    public boolean canRefresh = true;

    public void getMore() {
        if (hasMore) {
            canRefresh = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Twitter twitter = Utils.getTwitter(context, settings);
                        QueryResult result = twitter.search(query);

                        for (twitter4j.Status status : result.getTweets()) {
                            statuses.add(status);
                        }

                        if (result.hasNext()) {
                            query = result.nextQuery();
                            hasMore = true;
                        } else {
                            hasMore = false;
                        }

                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                canRefresh = true;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                canRefresh = false;
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }

    private double[] getLocationFromYahoo(int woeid) {
        double[] loc = new double[] {-1, -1};

        try {
            String url = "http://where.yahooapis.com/v1/place/" + woeid + "?appid=.DuZKdDV34EQ.TNLpvgTtFuMf5VruNTzx4Ti7F60XHVyV2zEbulKVjZKvRWBAiYZ";
            HttpURLConnection connection = (HttpURLConnection) new URL(url)
                    .openConnection();

            XmlPullParserFactory factory = null;
            try {
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
            } catch (XmlPullParserException e) {

            }

            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(connection.getInputStream()));

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG
                        && "latitude".equals(xpp.getName()) &&
                        loc[0] == -1) {

                    try {
                        loc[0] = Double.parseDouble(xpp.nextText());
                    } catch (Exception e) {

                    }
                } else if (eventType == XmlPullParser.START_TAG
                        && "longitude".equals(xpp.getName()) &&
                        loc[1] == -1) {

                    try {
                        loc[1] = Double.parseDouble(xpp.nextText());
                    } catch (Exception e) {

                    }
                }
                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            loc[0] = -1;
            loc[1] = -1;
        }

        Log.v("talon_loc", "lat: " + loc[0] + " long: " + loc[1]);

        return loc;
    }

}