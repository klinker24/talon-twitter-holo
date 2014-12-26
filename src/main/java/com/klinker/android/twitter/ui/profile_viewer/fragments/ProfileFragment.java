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

package com.klinker.android.twitter.ui.profile_viewer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.ArrayListLoader;
import com.klinker.android.twitter.adapters.FollowersArrayAdapter;
import com.klinker.android.twitter.adapters.PeopleArrayAdapter;
import com.klinker.android.twitter.adapters.TimelineArrayAdapter;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.manipulations.photo_viewer.PhotoViewerActivity;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.utils.Utils;
import com.klinker.android.twitter.utils.text.TextUtils;

import org.lucasr.smoothie.AsyncListView;
import org.lucasr.smoothie.ItemManager;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;
import uk.co.senab.bitmapcache.BitmapLruCache;

public class ProfileFragment extends Fragment {

    private SharedPreferences sharedPrefs;

    private static final int BTN_TWEET = 0;
    private static final int BTN_FOLLOWERS = 1;
    private static final int BTN_FOLLOWING = 2;

    private int current = BTN_TWEET;

    private Context context;
    private AppSettings settings;

    private User thisUser;

    private Button tweetsBtn;
    private Button followersBtn;
    private Button followingBtn;

    private String screenName;
    private boolean isMyProfile;


    private ItemManager.Builder builder;

    private long currentFollowers = -1;
    private long currentFollowing = -1;
    private ArrayList<User> followers;
    private ArrayList<User> following;
    private boolean canRefresh = true;

    private ImageView background;
    private ImageView profilePicture;
    private ProgressBar spinner;

    public BitmapLruCache mCache;

    public ArrayList<Long> followingIds;
    public boolean finishedGettingIds = false;

    public View layout;

    public ProfileFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        mCache = App.getInstance(context).getBitmapCache();

        settings = AppSettings.getInstance(context);

        screenName = getArguments().getString("screen_name");
        isMyProfile = settings.myScreenName.equals(screenName);

        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        inflater = LayoutInflater.from(context);

        layout = inflater.inflate(R.layout.list_fragment, null);
        LinearLayout spin = (LinearLayout) layout.findViewById(R.id.spinner);
        spin.setVisibility(View.GONE);

        AsyncListView listView = (AsyncListView) layout.findViewById(R.id.listView);
        listView.setVisibility(View.VISIBLE);
        BitmapLruCache cache = App.getInstance(context).getBitmapCache();
        ArrayListLoader loader = new ArrayListLoader(cache, context);

        builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(50);
        builder.setThreadPoolSize(4);

        listView.setItemManager(builder.build());

        View header;
        boolean fromAddon = false;

        if(!settings.addonTheme) {
            header = inflater.inflate(R.layout.user_profile_header, null);
        } else {
            try {
                Context viewContext = null;
                Resources res = context.getPackageManager().getResourcesForApplication(settings.addonThemePackage);

                try {
                    viewContext = context.createPackageContext(settings.addonThemePackage, Context.CONTEXT_IGNORE_SECURITY);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (res != null && viewContext != null) {
                    int id = res.getIdentifier("user_profile_header", "layout", settings.addonThemePackage);
                    header = LayoutInflater.from(viewContext).inflate(res.getLayout(id), null);
                    fromAddon = true;
                } else {
                    header = inflater.inflate(R.layout.user_profile_header, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                header = inflater.inflate(R.layout.user_profile_header, null);
            }
        }

        listView.addHeaderView(header);
        listView.setAdapter(new TimelineArrayAdapter(context, new ArrayList<Status>(0)));

        followers = new ArrayList<User>();
        following = new ArrayList<User>();

        setUpUI(fromAddon, header, layout);

        return layout;
    }

    public TextView verified;

    public void setUpUI(boolean fromAddon, View listHeader, View layout) {
        TextView mstatement;
        TextView mscreenname;
        AsyncListView mlistView;
        ImageView mheader;

        if (fromAddon) {
            try {
                Resources res = context.getPackageManager().getResourcesForApplication(settings.addonThemePackage);

                spinner = (ProgressBar) listHeader.findViewById(res.getIdentifier("progress_bar", "id", settings.addonThemePackage));
                verified = (TextView) listHeader.findViewById(res.getIdentifier("verified_text", "id", settings.addonThemePackage));
                tweetsBtn = (Button) listHeader.findViewById(res.getIdentifier("tweets", "id", settings.addonThemePackage));
                followersBtn = (Button) listHeader.findViewById(res.getIdentifier("followers", "id", settings.addonThemePackage));
                followingBtn = (Button) listHeader.findViewById(res.getIdentifier("following", "id", settings.addonThemePackage));
                background = (ImageView) listHeader.findViewById(res.getIdentifier("background_image", "id", settings.addonThemePackage));
                profilePicture = (ImageView) listHeader.findViewById(res.getIdentifier("profile_pic", "id", settings.addonThemePackage));
                mstatement = (TextView) listHeader.findViewById(res.getIdentifier("user_statement", "id", settings.addonThemePackage));
                mscreenname = (TextView) listHeader.findViewById(res.getIdentifier("username", "id", settings.addonThemePackage));
                mlistView = (AsyncListView) layout.findViewById(R.id.listView);
                mheader = (ImageView) listHeader.findViewById(res.getIdentifier("background_image", "id", settings.addonThemePackage));
            } catch (Exception e) {
                spinner = (ProgressBar) listHeader.findViewById(R.id.progress_bar);
                verified = (TextView) listHeader.findViewById(R.id.verified_text);
                tweetsBtn = (Button) listHeader.findViewById(R.id.tweets);
                followersBtn = (Button) listHeader.findViewById(R.id.followers);
                followingBtn = (Button) listHeader.findViewById(R.id.following);
                background = (ImageView) listHeader.findViewById(R.id.background_image);
                profilePicture = (ImageView) listHeader.findViewById(R.id.profile_pic);
                mstatement = (TextView) listHeader.findViewById(R.id.user_statement);
                mscreenname = (TextView) listHeader.findViewById(R.id.username);
                mlistView = (AsyncListView) layout.findViewById(R.id.listView);
                mheader = (ImageView) listHeader.findViewById(R.id.background_image);
            }
        } else {
            spinner = (ProgressBar) listHeader.findViewById(R.id.progress_bar);
            verified = (TextView) listHeader.findViewById(R.id.verified_text);
            tweetsBtn = (Button) listHeader.findViewById(R.id.tweets);
            followersBtn = (Button) listHeader.findViewById(R.id.followers);
            followingBtn = (Button) listHeader.findViewById(R.id.following);
            background = (ImageView) listHeader.findViewById(R.id.background_image);
            profilePicture = (ImageView) listHeader.findViewById(R.id.profile_pic);
            mstatement = (TextView) listHeader.findViewById(R.id.user_statement);
            mscreenname = (TextView) listHeader.findViewById(R.id.username);
            mlistView = (AsyncListView) layout.findViewById(R.id.listView);
            mheader = (ImageView) listHeader.findViewById(R.id.background_image);
        }

        final TextView statement = mstatement;
        final TextView screenname = mscreenname;
        final AsyncListView listView = mlistView;
        final ImageView header = mheader;

        spinner.setVisibility(View.VISIBLE);

        statement.setTextSize(settings.textSize);
        screenname.setTextSize(settings.textSize + 1);

        getData(statement, listView);

        screenname.setText("@" + screenName);

        tweetsBtn.setText(getResources().getString(R.string.tweets));
        tweetsBtn.setTextSize(settings.textSize - 1);
        tweetsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current != BTN_TWEET) {
                    current = BTN_TWEET;
                    currentFollowing = -1;
                    currentFollowers = -1;
                    hasMore = true;

                    listView.setItemManager(builder.build());
                    listView.setAdapter(timelineAdapter);

                    getTimeline(thisUser, listView);
                }
            }
        });

        followersBtn.setText(getResources().getString(R.string.followers));
        followersBtn.setTextSize(settings.textSize - 1);
        followersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current != BTN_FOLLOWERS) {
                    current = BTN_FOLLOWERS;
                    hasMore = false;

                    listView.setItemManager(null);
                    listView.setAdapter(followersAdapter);

                    getFollowers(thisUser, listView);
                }
            }
        });

        followingBtn.setText(getResources().getString(R.string.following));
        followingBtn.setTextSize(settings.textSize - 1);
        followingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current != BTN_FOLLOWING) {
                    current = BTN_FOLLOWING;
                    hasMore = false;

                    listView.setItemManager(null);
                    listView.setAdapter(new PeopleArrayAdapter(context, following));

                    getFollowing(thisUser, listView);
                }
            }
        });

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(spinner.getVisibility() == View.GONE && thisUser != null) {
                    startActivity(new Intent(context, PhotoViewerActivity.class).putExtra("url", thisUser.getProfileBannerURL()));
                } else {
                    // it isn't ready to be opened just yet
                }
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spinner.getVisibility() == View.GONE) {
                    try {
                        startActivity(new Intent(context, PhotoViewerActivity.class)
                                .putExtra("url", thisUser.getOriginalProfileImageURL())
                                .putExtra("from_cache", false));
                    } catch (Exception e) {
                        // this user doesn't exist...
                    }
                } else {
                    // it isn't ready to be opened just yet
                }
            }
        });

        canRefresh = false;

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == totalItemCount && hasMore) {
                    // Last item is fully visible.
                    if (current == BTN_FOLLOWING && canRefresh) {
                        getFollowing(thisUser, listView);
                    } else if (current == BTN_FOLLOWERS && canRefresh) {
                        getFollowers(thisUser, listView);
                    } else if (current == BTN_TWEET && canRefresh) {
                        getTimeline(thisUser, listView);
                    }

                    canRefresh = false;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            canRefresh = true;
                        }
                    }, 4000);
                }

                if(visibleItemCount == 0) return;
                if(firstVisibleItem != 0) return;

                if (settings.translateProfileHeader) {
                    header.setTranslationY(-listView.getChildAt(0).getTop() / 2);
                }
            }
        });
    }

    public boolean hasMore = true;

    public void getURL(final TextView statement, final User user) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                String url = "";
                try {
                    URL address = new URL(user.getURL());
                    connection = (HttpURLConnection) address.openConnection(Proxy.NO_PROXY);
                    connection.setConnectTimeout(1000);
                    connection.setInstanceFollowRedirects(false);
                    connection.setReadTimeout(1000);
                    connection.connect();
                    String expandedURL = connection.getHeaderField("Location");
                    if(expandedURL != null) {
                        url = expandedURL;
                    } else {
                        url = user.getURL();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    url = user.getURL();
                }

                if (url != null) {
                    final String fUrl = url;

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statement.append("\n" + fUrl);

                            // don't open in external browser
                            TextUtils.linkifyText(context, statement, null, true, "", false);
                        }
                    });
                }

                getFollowingStatus(statement, user);


            }
        }).start();
    }

    public void getFollowingStatus(final TextView statement, final User user) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (user.getScreenName().equals(settings.myScreenName)) {
                        return;
                    }
                    final String followingStatus = Utils.getTwitter(context, settings).showFriendship(settings.myScreenName, thisUser.getScreenName()).isTargetFollowingSource() ?
                            getResources().getString(R.string.follows_you) : getResources().getString(R.string.not_following_you);

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statement.append("\n\n" + followingStatus);
                        }
                    });
                } catch (Exception e) {

                }
            }
        }).start();
    }

    public void getData(final TextView statement, final AsyncListView listView) {

        Thread getData = new Thread(new Runnable() {
            @Override
            public void run() {
                Twitter twitter =  Utils.getTwitter(context, settings);
                try {
                    Log.v("talon_profile", "start of load time: " + Calendar.getInstance().getTimeInMillis());
                    if (!isMyProfile) {
                        thisUser = twitter.showUser(screenName);
                    } else {
                        if (settings.myId == 0) {
                            try {
                                thisUser = twitter.showUser(settings.myScreenName);
                            } catch (Exception e) {
                                // the user has changed their screen name, so look for the id
                                ((Activity)context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, context.getResources().getString(R.string.changed_screenname) , Toast.LENGTH_LONG).show();

                                        spinner.setVisibility(View.GONE);
                                        canRefresh = false;
                                        hasMore = false;
                                    }
                                });
                            }
                            return;
                        } else {
                            thisUser = twitter.showUser(settings.myId);
                        }


                        // update the profile picture url and the background url in shared prefs
                        int currentAccount = sharedPrefs.getInt("current_account", 1);

                        SharedPreferences.Editor e = sharedPrefs.edit();
                        e.putString("twitter_users_name_" + currentAccount, thisUser.getName()).commit();
                        e.putString("twitter_screen_name_" + currentAccount, thisUser.getScreenName()).commit();
                        e.putLong("twitter_id_" + currentAccount, thisUser.getId()).commit();
                        e.putString("profile_pic_url_" + currentAccount, thisUser.getOriginalProfileImageURL());
                        e.putString("twitter_background_url_" + currentAccount, thisUser.getProfileBannerURL());
                        e.commit();
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (thisUser.isVerified()) {
                                    if (settings.addonTheme) {
                                        verified.setVisibility(View.VISIBLE);
                                        verified.setText(getResources().getString(R.string.verified));
                                    } else {
                                        verified.setVisibility(View.VISIBLE);
                                        verified.setText(getResources().getString(R.string.verified));
                                    }
                                }

                                String state = thisUser.getDescription() + "\n";
                                String loca = thisUser.getLocation();

                                if (!loca.equals("")) {
                                    state += "\n" + thisUser.getLocation();
                                }

                                if (state.equals("")) {
                                    statement.setText(getResources().getString(R.string.no_description));
                                } else {
                                    statement.setText(state);
                                }

                                if (!settings.addonTheme) {
                                    statement.setLinkTextColor(getResources().getColor(R.color.app_color));
                                } else {
                                    statement.setLinkTextColor(settings.accentInt);
                                }

                                tweetsBtn.setText(getResources().getString(R.string.tweets) + "\n" + "(" + thisUser.getStatusesCount() + ")");
                                followersBtn.setText(getResources().getString(R.string.followers) + "\n" + "(" + thisUser.getFollowersCount() + ")");
                                followingBtn.setText(getResources().getString(R.string.following) + "\n" + "(" + thisUser.getFriendsCount() + ")");

                                getURL(statement, thisUser);
                                getTimeline(thisUser, listView);
                            } catch (Exception e) {
                                // Illegal state because the activity is destroyed
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (thisUser != null && thisUser.isProtected()) {
                                    Toast.makeText(context, getResources().getString(R.string.protected_account), Toast.LENGTH_SHORT).show();
                                    if (settings.roundContactImages) {
                                        ImageUtils.loadSizedCircleImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache, 96);
                                    } else {
                                        ImageUtils.loadImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache);
                                    }

                                    String url = thisUser.getProfileBannerURL();
                                    ImageUtils.loadImage(context, background, url, mCache);
                                } else {
                                    Toast.makeText(context, getResources().getString(R.string.error_loading_timeline), Toast.LENGTH_SHORT).show();
                                }
                                spinner.setVisibility(View.GONE);
                            } catch (Exception e) {
                                // not attached to activity
                            }
                            canRefresh = false;
                            hasMore = false;
                        }
                    });
                }
            }
        });

        getData.setPriority(Thread.MAX_PRIORITY);
        getData.start();
    }

    public PeopleArrayAdapter followersAdapter;

    public void getFollowers(final User user, final AsyncListView listView) {
        spinner.setVisibility(View.VISIBLE);
        canRefresh = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);

                    try {
                        if (followingIds == null && user.getId() == settings.myId) {
                            long currCursor = -1;
                            IDs idObject;
                            int rep = 0;

                            do {
                                // gets 5000 ids at a time
                                idObject = twitter.getFriendsIDs(settings.myId, currCursor);

                                long[] lIds = idObject.getIDs();
                                followingIds = new ArrayList<Long>();
                                for (int i = 0; i < lIds.length; i++) {
                                    followingIds.add(lIds[i]);
                                }

                                rep++;
                            } while ((currCursor = idObject.getNextCursor()) != 0 && rep < 3);
                        }
                    } catch (Throwable t) {
                        followingIds = null;
                    }

                    PagableResponseList<User> friendsPaging = twitter.getFollowersList(user.getId(), currentFollowers);

                    for (int i = 0; i < friendsPaging.size(); i++) {
                        followers.add(friendsPaging.get(i));
                    }

                    if (friendsPaging.size() > 17) {
                        hasMore = true;
                    } else {
                        hasMore = false;
                    }

                    currentFollowers = friendsPaging.getNextCursor();

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (followersAdapter == null) {
                                if (followingIds == null) {
                                    // we will do a normal array adapter
                                    followersAdapter = new PeopleArrayAdapter(context, followers);
                                } else {
                                    followersAdapter = new FollowersArrayAdapter(context, followers, followingIds);
                                }
                                listView.setAdapter(followersAdapter);
                            } else {
                                followersAdapter.notifyDataSetChanged();
                            }

                            if(settings.roundContactImages) {
                                ImageUtils.loadSizedCircleImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache, 96);
                            } else {
                                ImageUtils.loadImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache);
                            }

                            String url = user.getProfileBannerURL();
                            ImageUtils.loadImage(context, background, url, mCache);

                            canRefresh = true;
                            spinner.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (user != null && user.isProtected()) {
                                Toast.makeText(context, getResources().getString(R.string.protected_account), Toast.LENGTH_SHORT).show();
                                if(settings.roundContactImages) {
                                    ImageUtils.loadSizedCircleImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache, 96);
                                } else {
                                    ImageUtils.loadImage(context, profilePicture, user.getOriginalProfileImageURL(), mCache);
                                }

                                String url = user.getProfileBannerURL();
                                ImageUtils.loadImage(context, background, url, mCache);
                            } else {
                                Toast.makeText(context, getResources().getString(R.string.error_loading_timeline), Toast.LENGTH_SHORT).show();
                            }
                            spinner.setVisibility(View.GONE);
                            canRefresh = false;
                            hasMore = false;
                        }
                    });
                }
            }
        }).start();
    }

    public PeopleArrayAdapter followingAdapter;

    public void getFollowing(final User user, final AsyncListView listView) {
        spinner.setVisibility(View.VISIBLE);
        canRefresh = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);

                    PagableResponseList<User> friendsPaging;
                    try {
                        friendsPaging = twitter.getFriendsList(user.getId(), currentFollowing);
                    } catch (OutOfMemoryError e) {
                        return;
                    }

                    for (int i = 0; i < friendsPaging.size(); i++) {
                        following.add(friendsPaging.get(i));
                        Log.v("friends_list", friendsPaging.get(i).getName());
                    }

                    if (friendsPaging.size() > 17) {
                        hasMore = true;
                    } else {
                        hasMore = false;
                    }

                    currentFollowing = friendsPaging.getNextCursor();

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (followingAdapter == null) {
                                followingAdapter = new PeopleArrayAdapter(context, following);
                                listView.setAdapter(followingAdapter);
                            } else {
                                followingAdapter.notifyDataSetChanged();
                            }

                            if(settings.roundContactImages) {
                                ImageUtils.loadSizedCircleImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache, 96);
                            } else {
                                ImageUtils.loadImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache);
                            }

                            String url = user.getProfileBannerURL();
                            ImageUtils.loadImage(context, background, url, mCache);

                            canRefresh = true;
                            spinner.setVisibility(View.GONE);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (user != null && user.isProtected()) {
                                    Toast.makeText(context, getResources().getString(R.string.protected_account), Toast.LENGTH_SHORT).show();
                                    if (settings.roundContactImages) {
                                        ImageUtils.loadSizedCircleImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache, 96);
                                    } else {
                                        ImageUtils.loadImage(context, profilePicture, user.getOriginalProfileImageURL(), mCache);
                                    }

                                    String url = user.getProfileBannerURL();
                                    ImageUtils.loadImage(context, background, url, mCache);
                                } else {
                                    Toast.makeText(context, getResources().getString(R.string.error_loading_timeline), Toast.LENGTH_SHORT).show();
                                }
                                spinner.setVisibility(View.GONE);
                            } catch (Exception e) {
                                // fragment not attached
                            }
                            canRefresh = false;
                            hasMore = false;
                        }
                    });
                }
            }
        }).start();
    }

    public Paging timelinePaging = new Paging(1, 20);
    public ArrayList<Status> timelineStatuses = new ArrayList<Status>();
    public TimelineArrayAdapter timelineAdapter;

    public void getTimeline(final User user, final AsyncListView listView) {
        spinner.setVisibility(View.VISIBLE);
        canRefresh = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);

                    List<twitter4j.Status> statuses = twitter.getUserTimeline(user.getId(), timelinePaging);
                    timelinePaging.setPage(timelinePaging.getPage() + 1);

                    for (twitter4j.Status s : statuses) {
                        timelineStatuses.add(s);
                    }

                    if (statuses.size() > 17) {
                        hasMore = true;
                    } else {
                        hasMore = false;
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (timelineAdapter != null) {
                                timelineAdapter.notifyDataSetChanged();
                            } else {
                                timelineAdapter= new TimelineArrayAdapter(context, timelineStatuses, screenName);
                                listView.setItemManager(builder.build());
                                listView.setAdapter(timelineAdapter);
                            }

                            if(settings.roundContactImages) {
                                ImageUtils.loadSizedCircleImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache, 96);
                            } else {
                                ImageUtils.loadImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache);
                            }

                            String url = user.getProfileBannerURL();
                            ImageUtils.loadImage(context, background, url, mCache);

                            spinner.setVisibility(View.GONE);
                            canRefresh = true;
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (user != null && user.isProtected()) {
                                    Toast.makeText(context, getResources().getString(R.string.protected_account), Toast.LENGTH_SHORT).show();
                                    if (settings.roundContactImages) {
                                        ImageUtils.loadSizedCircleImage(context, profilePicture, thisUser.getOriginalProfileImageURL(), mCache, 96);
                                    } else {
                                        ImageUtils.loadImage(context, profilePicture, user.getOriginalProfileImageURL(), mCache);
                                    }

                                    String url = user.getProfileBannerURL();
                                    ImageUtils.loadImage(context, background, url, mCache);
                                } else {
                                    Toast.makeText(context, getResources().getString(R.string.error_loading_timeline), Toast.LENGTH_SHORT).show();
                                }
                                spinner.setVisibility(View.GONE);
                            } catch (Exception e) {
                                // not attached
                            }
                            canRefresh = false;
                            hasMore = false;
                        }
                    });
                } catch (OutOfMemoryError x) {
                    x.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(context, context.getResources().getString(R.string.error_loading_timeline), Toast.LENGTH_SHORT).show();
                                spinner.setVisibility(View.GONE);
                            } catch (Exception e) {
                                // not attached
                            }

                            canRefresh = false;
                            hasMore = false;
                        }
                    });
                }
            }
        }).start();
    }
}
