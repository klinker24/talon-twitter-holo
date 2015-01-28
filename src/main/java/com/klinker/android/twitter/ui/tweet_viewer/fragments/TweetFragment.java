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

package com.klinker.android.twitter.ui.tweet_viewer.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.AutoCompleteHashtagAdapter;
import com.klinker.android.twitter.adapters.AutoCompletePeopleAdapter;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.sq_lite.FollowersDataSource;
import com.klinker.android.twitter.data.sq_lite.HashtagDataSource;
import com.klinker.android.twitter.manipulations.ExpansionAnimation;
import com.klinker.android.twitter.manipulations.widgets.HoloEditText;
import com.klinker.android.twitter.services.SendTweet;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.compose.ComposeActivity;
import com.klinker.android.twitter.ui.compose.ComposeSecAccActivity;
import com.klinker.android.twitter.ui.profile_viewer.ProfilePager;
import com.klinker.android.twitter.ui.tweet_viewer.ViewPictures;
import com.klinker.android.twitter.ui.tweet_viewer.ViewRetweeters;
import com.klinker.android.twitter.manipulations.EmojiKeyboard;
import com.klinker.android.twitter.manipulations.photo_viewer.PhotoViewerActivity;
import com.klinker.android.twitter.manipulations.QustomDialogBuilder;
import com.klinker.android.twitter.utils.EmojiUtils;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.utils.Utils;
import com.klinker.android.twitter.utils.api_helper.TwitterMultipleImageHelper;
import com.klinker.android.twitter.utils.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.*;
import uk.co.senab.photoview.PhotoViewAttacher;

public class TweetFragment extends Fragment {

    public AppSettings settings;
    public Context context;
    public SharedPreferences sharedPrefs;
    public View layout;

    private TextView timetv;
    private ImageView pictureIv;
    private ImageButton emojiButton;
    private EmojiKeyboard emojiKeyboard;
    private PhotoViewAttacher mAttacher;

    private ImageView attachImage;
    private String attachedUri = "";

    private String name;
    private String screenName;
    private String tweet;
    private long time;
    private String retweeter;
    private String webpage;
    private String proPic;
    private boolean picture;
    private long tweetId;
    private String[] users;
    private String[] hashtags;
    private String[] otherLinks;
    private boolean isMyTweet;
    private boolean secondAcc;

    private ListPopupWindow userAutocomplete;
    private ListPopupWindow hashtagAutocomplete;

    private boolean addonTheme;

    private TextView charRemaining;

    final Pattern p = Patterns.WEB_URL;

    boolean canUseExpand = true;

    private Handler countHandler;
    private Runnable getCount = new Runnable() {
        @Override
        public void run() {
            String text = reply.getText().toString();

            if (!text.contains("http")) { // no links, normal tweet
                try {
                    charRemaining.setText(140 - reply.getText().length() - (attachedUri.equals("") ? 0 : 23) + "");
                } catch (Exception e) {
                    charRemaining.setText("0");
                }
            } else {
                int count = text.length();
                Matcher m = p.matcher(text);
                while(m.find()) {
                    String url = m.group();
                    count -= url.length(); // take out the length of the url
                    count += 23; // add 23 for the shortened url
                }

                if (!attachedUri.equals("")) {
                    count += 23;
                }

                charRemaining.setText(140 - count + "");
            }
        }
    };

    public void setFromBundle() {
        Bundle b = getArguments();

        settings = AppSettings.getInstance(getActivity());

        name = b.getString("name");
        screenName = b.getString("screen_name");
        tweet = b.getString("tweet");
        time = b.getLong("time");
        retweeter = b.getString("retweeter");
        webpage = b.getString("webpage");
        proPic = b.getString("pro_pic");
        picture = b.getBoolean("picture");
        tweetId = b.getLong("tweet_id");
        users = b.getStringArray("users");
        hashtags = b.getStringArray("hashtags");
        isMyTweet = b.getBoolean("is_my_tweet");
        otherLinks = b.getStringArray("links");
        secondAcc = b.getBoolean("second_account");
    }

    public TweetFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setFromBundle();

        countHandler = new Handler();

        layout = inflater.inflate(R.layout.tweet_fragment, null, false);
        addonTheme = false;

        if(settings == null) {
            settings = AppSettings.getInstance(context);
        }

        if (settings.addonTheme) {
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
                    int id = res.getIdentifier("tweet_fragment", "layout", settings.addonThemePackage);
                    layout = LayoutInflater.from(viewContext).inflate(res.getLayout(id), null);
                    addonTheme = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                layout = inflater.inflate(R.layout.tweet_fragment, null);
            }
        }
        setUIElements(layout);

        return layout;
    }

    private EditText reply;
    private ImageButton replyButton;
    public ImageView profilePic;

    public void setUIElements(final View layout) {
        TextView nametv;
        TextView screennametv;
        TextView tweettv;
        ImageButton attachButton;
        ImageButton at;
        ImageButton quote = null;
        ImageButton viewRetweeters = null;
        final TextView retweetertv;
        final LinearLayout background;
        final ImageButton expand;
        final View favoriteButton;
        final View retweetButton;
        final TextView favoriteCount;
        final TextView retweetCount;
        final ImageButton overflow;
        final LinearLayout buttons;

        if (!addonTheme) {
            nametv = (TextView) layout.findViewById(R.id.name);
            screennametv = (TextView) layout.findViewById(R.id.screen_name);
            tweettv = (TextView) layout.findViewById(R.id.tweet);
            retweetertv = (TextView) layout.findViewById(R.id.retweeter);
            background = (LinearLayout) layout.findViewById(R.id.linLayout);
            expand = (ImageButton) layout.findViewById(R.id.expand);
            profilePic = (ImageView) layout.findViewById(R.id.profile_pic_contact);
            favoriteButton = (ImageButton) layout.findViewById(R.id.favorite);
            quote = (ImageButton) layout.findViewById(R.id.quote_button);
            retweetButton = (ImageButton) layout.findViewById(R.id.retweet);
            favoriteCount = (TextView) layout.findViewById(R.id.fav_count);
            retweetCount = (TextView) layout.findViewById(R.id.retweet_count);
            reply = (EditText) layout.findViewById(R.id.reply);
            replyButton = (ImageButton) layout.findViewById(R.id.reply_button);
            attachButton = (ImageButton) layout.findViewById(R.id.attach_button);
            overflow = (ImageButton) layout.findViewById(R.id.overflow_button);
            buttons = (LinearLayout) layout.findViewById(R.id.buttons);
            charRemaining = (TextView) layout.findViewById(R.id.char_remaining);
            at = (ImageButton) layout.findViewById(R.id.at_button);
            emojiButton = (ImageButton) layout.findViewById(R.id.emoji);
            emojiKeyboard = (EmojiKeyboard) layout.findViewById(R.id.emojiKeyboard);
            timetv = (TextView) layout.findViewById(R.id.time);
            pictureIv = (ImageView) layout.findViewById(R.id.imageView);
            attachImage = (ImageView) layout.findViewById(R.id.attach);
            viewRetweeters = (ImageButton) layout.findViewById(R.id.view_retweeters);
        } else {
            Resources res;
            try {
                res = context.getPackageManager().getResourcesForApplication(settings.addonThemePackage);
            } catch (Exception e) {
                res = null;
            }

            nametv = (TextView) layout.findViewById(res.getIdentifier("name", "id", settings.addonThemePackage));
            screennametv = (TextView) layout.findViewById(res.getIdentifier("screen_name", "id", settings.addonThemePackage));
            tweettv = (TextView) layout.findViewById(res.getIdentifier("tweet", "id", settings.addonThemePackage));
            retweetertv = (TextView) layout.findViewById(res.getIdentifier("retweeter", "id", settings.addonThemePackage));
            background = (LinearLayout) layout.findViewById(res.getIdentifier("linLayout", "id", settings.addonThemePackage));
            expand = (ImageButton) layout.findViewById(res.getIdentifier("expand", "id", settings.addonThemePackage));
            profilePic = (ImageView) layout.findViewById(res.getIdentifier("profile_pic", "id", settings.addonThemePackage));
            favoriteButton = layout.findViewById(res.getIdentifier("favorite", "id", settings.addonThemePackage));
            retweetButton = layout.findViewById(res.getIdentifier("retweet", "id", settings.addonThemePackage));
            favoriteCount = (TextView) layout.findViewById(res.getIdentifier("fav_count", "id", settings.addonThemePackage));
            retweetCount = (TextView) layout.findViewById(res.getIdentifier("retweet_count", "id", settings.addonThemePackage));
            reply = (EditText) layout.findViewById(res.getIdentifier("reply", "id", settings.addonThemePackage));
            replyButton = (ImageButton) layout.findViewById(res.getIdentifier("reply_button", "id", settings.addonThemePackage));
            attachButton = (ImageButton) layout.findViewById(res.getIdentifier("attach_button", "id", settings.addonThemePackage));
            overflow = (ImageButton) layout.findViewById(res.getIdentifier("overflow_button", "id", settings.addonThemePackage));
            buttons = (LinearLayout) layout.findViewById(res.getIdentifier("buttons", "id", settings.addonThemePackage));
            charRemaining = (TextView) layout.findViewById(res.getIdentifier("char_remaining", "id", settings.addonThemePackage));
            at = (ImageButton) layout.findViewById(res.getIdentifier("at_button", "id", settings.addonThemePackage));
            emojiButton = null;
            emojiKeyboard = null;
            timetv = (TextView) layout.findViewById(res.getIdentifier("time", "id", settings.addonThemePackage));
            pictureIv = (ImageView) layout.findViewById(res.getIdentifier("imageView", "id", settings.addonThemePackage));
            attachImage = (ImageView) layout.findViewById(res.getIdentifier("attach", "id", settings.addonThemePackage));
            try {
                viewRetweeters = (ImageButton) layout.findViewById(res.getIdentifier("view_retweeters", "id", settings.addonThemePackage));
            } catch (Exception e) {
                // it doesn't exist in the theme;
            }
            try {
                quote = (ImageButton) layout.findViewById(res.getIdentifier("quote_button", "id", settings.addonThemePackage));
            } catch (Exception e) {
                // didn't exist when the theme was created.
            }
        }

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        if (reply != null) {
            userAutocomplete = new ListPopupWindow(context);
            userAutocomplete.setAnchorView(layout.findViewById(R.id.prompt_pos));
            userAutocomplete.setHeight(Utils.toDP(100, context));
            userAutocomplete.setWidth((int) (width * .75));
            userAutocomplete.setAdapter(new AutoCompletePeopleAdapter(context,
                    FollowersDataSource.getInstance(context).getCursor(settings.currentAccount, reply.getText().toString()), reply));
            userAutocomplete.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);

            userAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    userAutocomplete.dismiss();
                }
            });

            hashtagAutocomplete = new ListPopupWindow(context);
            hashtagAutocomplete.setAnchorView(layout.findViewById(R.id.prompt_pos));
            hashtagAutocomplete.setHeight(Utils.toDP(100, context));
            hashtagAutocomplete.setWidth((int) (width * .75));
            hashtagAutocomplete.setAdapter(new AutoCompleteHashtagAdapter(context,
                    HashtagDataSource.getInstance(context).getCursor(reply.getText().toString()), reply));
            hashtagAutocomplete.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);

            hashtagAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    hashtagAutocomplete.dismiss();
                }
            });

            reply.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    String searchText = reply.getText().toString();

                    try {
                        if (searchText.substring(searchText.length() - 1, searchText.length()).equals("@")) {
                            userAutocomplete.show();

                        } else if (searchText.substring(searchText.length() - 1, searchText.length()).equals(" ")) {
                            userAutocomplete.dismiss();
                        } else if (userAutocomplete.isShowing()) {
                            String[] split = reply.getText().toString().split(" ");
                            String adapterText;
                            if (split.length > 1) {
                                adapterText = split[split.length - 1];
                            } else {
                                adapterText = split[0];
                            }
                            adapterText = adapterText.replace("@", "");
                            userAutocomplete.setAdapter(new AutoCompletePeopleAdapter(context,
                                    FollowersDataSource.getInstance(context).getCursor(settings.currentAccount, adapterText), reply));
                        }

                        if (searchText.substring(searchText.length() - 1, searchText.length()).equals("#")) {
                            hashtagAutocomplete.show();

                        } else if (searchText.substring(searchText.length() - 1, searchText.length()).equals(" ")) {
                            hashtagAutocomplete.dismiss();
                        } else if (hashtagAutocomplete.isShowing()) {
                            String[] split = reply.getText().toString().split(" ");
                            String adapterText;
                            if (split.length > 1) {
                                adapterText = split[split.length - 1];
                            } else {
                                adapterText = split[0];
                            }
                            adapterText = adapterText.replace("#", "");
                            hashtagAutocomplete.setAdapter(new AutoCompleteHashtagAdapter(context,
                                    HashtagDataSource.getInstance(context).getCursor(adapterText), reply));
                        }
                    } catch (Exception e) {
                        // there is no text
                        try {
                            userAutocomplete.dismiss();
                        } catch (Exception x) {
                            // something went really wrong i guess haha
                        }

                        try {
                            hashtagAutocomplete.dismiss();
                        } catch (Exception x) {

                        }
                    }

                }
            });
        }

        nametv.setTextSize(settings.textSize +2);
        screennametv.setTextSize(settings.textSize);
        tweettv.setTextSize(settings.textSize);
        timetv.setTextSize(settings.textSize - 3);
        retweetertv.setTextSize(settings.textSize - 3);
        favoriteCount.setTextSize(13);
        retweetCount.setTextSize(13);
        if (reply != null) {
            reply.setTextSize(settings.textSize);
        }

        if (settings.addonTheme) {
            try {
                Resources resourceAddon = context.getPackageManager().getResourcesForApplication(settings.addonThemePackage);
                int back = resourceAddon.getIdentifier("reply_entry_background", "drawable", settings.addonThemePackage);
                reply.setBackgroundDrawable(resourceAddon.getDrawable(back));
            } catch (Exception e) {
                // theme does not include a reply entry box
            }
        }

        if (viewRetweeters != null) {
            viewRetweeters.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //open up the activity to see who retweeted it
                    Intent viewRetweeters = new Intent(context, ViewRetweeters.class);
                    viewRetweeters.putExtra("id", tweetId);
                    startActivity(viewRetweeters);
                }
            });
        }

        if (quote != null) {
            quote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = tweet;

                    if (!settings.preferRT) {
                        text = "\"@" + screenName + ": " + text + "\" ";
                    } else {
                        text = " RT @" + screenName + ": " + text;
                    }

                    Intent intent;
                    if (!secondAcc) {
                        intent = new Intent(context, ComposeActivity.class);
                    } else {
                        intent = new Intent(context, ComposeSecAccActivity.class);
                    }

                    intent.putExtra("user", text);
                    intent.putExtra("id", tweetId);

                    startActivity(intent);
                }
            });
        }

        if (overflow!= null) {
            overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (buttons.getVisibility() == View.VISIBLE) {

                        Animation ranim = AnimationUtils.loadAnimation(context, R.anim.compose_rotate_back);
                        ranim.setFillAfter(true);
                        overflow.startAnimation(ranim);

                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
                        anim.setDuration(300);
                        buttons.startAnimation(anim);

                        buttons.setVisibility(View.GONE);
                    } else {
                        buttons.setVisibility(View.VISIBLE);

                        Animation ranim = AnimationUtils.loadAnimation(context, R.anim.compose_rotate);
                        ranim.setFillAfter(true);
                        overflow.startAnimation(ranim);

                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                        anim.setDuration(300);
                        buttons.startAnimation(anim);
                    }
                }
            });
        }

        if (settings.theme == 0 && !addonTheme) {
            nametv.setTextColor(getResources().getColor(android.R.color.black));
            nametv.setShadowLayer(0,0,0, getResources().getColor(android.R.color.transparent));
            screennametv.setTextColor(getResources().getColor(android.R.color.black));
            screennametv.setShadowLayer(0,0,0, getResources().getColor(android.R.color.transparent));
        }

        View.OnClickListener profile = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewProfile = new Intent(context, ProfilePager.class);
                viewProfile.putExtra("name", name);
                viewProfile.putExtra("screenname", screenName);
                viewProfile.putExtra("proPic", proPic);
                viewProfile.putExtra("tweetid", tweetId);
                viewProfile.putExtra("retweet", retweetertv.getVisibility() == View.VISIBLE);

                context.startActivity(viewProfile);
            }
        };

        if (picture && settings.combineProPicAndImage) {
            profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, PhotoViewerActivity.class).putExtra("url", webpage));
                }
            });
        } else {
            profilePic.setOnClickListener(profile);
        }

        nametv.setOnClickListener(profile);
        screennametv.setOnClickListener(profile);

        if(picture && pictureIv != null) { // if there is a picture already loaded
            Log.v("talon_picture_loading", "picture load started");

            mAttacher = new PhotoViewAttacher(pictureIv);
            mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    context.startActivity(new Intent(context, PhotoViewerActivity.class).putExtra("url", webpage));
                }
            });

            pictureIv.setVisibility(View.VISIBLE);
            ImageUtils.loadImage(context, pictureIv, webpage, App.getInstance(context).getBitmapCache());

            final Handler expansionHandler = new Handler();
            if (expand != null) {
                expand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!canUseExpand) {
                            return;
                        } else {
                            canUseExpand = false;
                        }
                        if(background.getVisibility() == View.VISIBLE) {
                            Animation ranim = AnimationUtils.loadAnimation(context, R.anim.drawer_rotate);
                            ranim.setFillAfter(true);
                            expand.startAnimation(ranim);
                            expansionHandler.removeCallbacksAndMessages(null);
                            expansionHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    canUseExpand = true;
                                }
                            }, 400);
                        } else {
                            Animation ranim = AnimationUtils.loadAnimation(context, R.anim.drawer_rotate_back);
                            ranim.setFillAfter(true);
                            expand.startAnimation(ranim);
                            expansionHandler.removeCallbacksAndMessages(null);
                            expansionHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    canUseExpand = true;
                                }
                            }, 400);
                        }

                        ExpansionAnimation expandAni = new ExpansionAnimation(background, 300);
                        background.startAnimation(expandAni);
                    }
                });
            }

        } else {
            if (expand != null) {
                expand.setVisibility(View.GONE);
            }
        }

        nametv.setText(name);
        screennametv.setText("@" + screenName);
        tweettv.setText(tweet);
        tweettv.setTextIsSelectable(true);

        if (settings.useEmoji && (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || EmojiUtils.ios)) {
            if (EmojiUtils.emojiPattern.matcher(tweet).find()) {
                final Spannable span = EmojiUtils.getSmiledText(context, Html.fromHtml(tweet.replaceAll("\n", "<br/>")));
                tweettv.setText(span);
            }
        }

        //Date tweetDate = new Date(time);
        String timeDisplay;

        if (!settings.militaryTime) {
            timeDisplay = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(time) + " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(time);
        } else {
            timeDisplay = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN).format(time) + " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(time);
        }

        timetv.setText(timeDisplay);
        timetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = "twitter.com/" + screenName + "/status/" + tweetId;
                Uri weburi = Uri.parse("http://" + data);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, weburi);
                launchBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchBrowser);
            }
        });

        timetv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (status != null) {
                    // we allow them to mute the client
                    final String client = android.text.Html.fromHtml(status.getSource()).toString();
                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.mute_client) + "?")
                            .setMessage(client)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String current = sharedPrefs.getString("muted_clients", "");
                                    sharedPrefs.edit().putString("muted_clients", current + client + "   ").commit();
                                    sharedPrefs.edit().putBoolean("refresh_me", true).commit();

                                    dialogInterface.dismiss();

                                    ((Activity) context).finish();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();
                } else {
                    // tell them the client hasn't been found
                    Toast.makeText(context, R.string.client_not_found, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        if (retweeter.length() > 0 ) {
            retweetertv.setText(getResources().getString(R.string.retweeter) + retweeter);
            retweetertv.setVisibility(View.VISIBLE);
            isRetweet = true;
        }

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFavorited || !settings.crossAccActions) {
                    favoriteStatus(favoriteCount, favoriteButton, tweetId, secondAcc ? TYPE_ACC_TWO : TYPE_ACC_ONE);
                } else if (settings.crossAccActions) {
                    // dialog for favoriting
                    String[] options = new String[3];

                    options[0] = "@" + settings.myScreenName;
                    options[1] = "@" + settings.secondScreenName;
                    options[2] = context.getString(R.string.both_accounts);

                    new AlertDialog.Builder(context)
                            .setItems(options, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int item) {
                                    favoriteStatus(favoriteCount, favoriteButton, tweetId, item + 1);
                                }
                            })
                            .create().show();
                }
            }
        });

        retweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!settings.crossAccActions) {
                    retweetStatus(retweetCount, tweetId, retweetButton, secondAcc ? TYPE_ACC_TWO : TYPE_ACC_ONE);
                } else {
                    // dialog for favoriting
                    String[] options = new String[3];

                    options[0] = "@" + settings.myScreenName;
                    options[1] = "@" + settings.secondScreenName;
                    options[2] = context.getString(R.string.both_accounts);

                    new AlertDialog.Builder(context)
                            .setItems(options, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int item) {
                                    retweetStatus(retweetCount, tweetId, retweetButton, item + 1);
                                }
                            })
                            .create().show();
                }
            }
        });

        retweetButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getResources().getString(R.string.remove_retweet))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new RemoveRetweet(tweetId, retweetButton).execute();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
                return false;
            }
        });

        //profilePic.loadImage(proPic, false, null);
        if (settings.addonTheme && settings.combineProPicAndImage && picture) {
            ImageUtils.loadImage(context, profilePic, webpage, App.getInstance(context).getBitmapCache());
        } else {
            ImageUtils.loadImage(context, profilePic, proPic, App.getInstance(context).getBitmapCache());
        }

        getInfo(favoriteButton, favoriteCount, retweetCount, tweetId, retweetButton);

        String text = tweet;
        String extraNames = "";

        String screenNameToUse;

        if (secondAcc) {
            screenNameToUse = settings.secondScreenName;
        } else {
            screenNameToUse = settings.myScreenName;
        }

        if (text.contains("@")) {
            for (String s : users) {
                if (!s.equals(screenNameToUse) && !extraNames.contains(s)  && !s.equals(screenName)) {
                    extraNames += "@" + s + " ";
                }
            }
        }

        if (retweeter != null && !retweeter.equals("") && !retweeter.equals(screenNameToUse) && !extraNames.contains(retweeter)) {
            extraNames += "@" + retweeter + " ";
        }

        String sendString = "";
        if (!screenName.equals(screenNameToUse)) {
            if (reply != null) {
                reply.setText("@" + screenName + " " + extraNames);
            }
            sendString = "@" + screenName + " " + extraNames;
        } else {
            if (reply != null) {
                reply.setText(extraNames);
            }
            sendString = extraNames;
        }

        if (settings.autoInsertHashtags && hashtags != null) {
            for (String s : hashtags) {
                if (!s.equals("")) {
                    if (reply != null) {
                        reply.append("#" + s + " ");
                    }
                    sendString += "#" + s + " ";
                }
            }
        }

        final String fSendString = sendString;
        if (reply != null) {
            reply.setSelection(reply.getText().length());
        }

        if (!settings.sendToComposeWindow) {
            replyButton.setEnabled(false);
            replyButton.setAlpha(.4f);
        }

        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!settings.sendToComposeWindow) {
                    try {
                        if (Integer.parseInt(charRemaining.getText().toString()) >= 0 || settings.twitlonger) {
                            if (Integer.parseInt(charRemaining.getText().toString()) < 0) {
                                new AlertDialog.Builder(context)
                                        .setTitle(context.getResources().getString(R.string.tweet_to_long))
                                        .setMessage(context.getResources().getString(R.string.select_shortening_service))
                                        .setPositiveButton(R.string.twitlonger, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                replyToStatus(reply, tweetId, Integer.parseInt(charRemaining.getText().toString()));
                                            }
                                        })
                                        .setNeutralButton(R.string.pwiccer, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                try {
                                                    Intent pwiccer = new Intent("com.t3hh4xx0r.pwiccer.requestImagePost");
                                                    pwiccer.putExtra("POST_CONTENT", reply.getText().toString());
                                                    startActivityForResult(pwiccer, 420);
                                                } catch (Throwable e) {
                                                    // open the play store here
                                                    // they don't have pwiccer installed
                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.t3hh4xx0r.pwiccer&hl=en")));
                                                }
                                            }
                                        })
                                        .setNegativeButton(R.string.edit, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .create()
                                        .show();
                            } else {
                                replyToStatus(reply, tweetId, Integer.parseInt(charRemaining.getText().toString()));
                            }
                        } else {
                            Toast.makeText(context, getResources().getString(R.string.tweet_to_long), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent compose;
                    if (!secondAcc) {
                        compose = new Intent(context, ComposeActivity.class);
                    } else {
                        compose = new Intent(context, ComposeSecAccActivity.class);
                    }

                    if (fSendString.length() > 0) {
                        compose.putExtra("user", fSendString.substring(0, fSendString.length() - 1)); // for some reason it puts a extra space here
                    }

                    compose.putExtra("id", tweetId);
                    compose.putExtra("reply_to_text", "@" + screenName + ": " + tweet);

                    startActivity(compose);
                }
            }
        });

        if (attachButton != null) {
            attachButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attachClick();

                    overflow.performClick();
                }
            });
        }

        if (settings.openKeyboard) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (reply != null) {
                        reply.requestFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInputFromWindow(reply.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                    }
                    }
            }, 500);
        }

        if (charRemaining != null) {
            charRemaining.setText(140 - reply.getText().length() + "");
        }

        if (reply != null) {
            reply.setHint(context.getResources().getString(R.string.reply));
            reply.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!replyButton.isEnabled()) {
                        replyButton.setEnabled(true);
                        replyButton.setAlpha(1.0f);
                    }
                    countHandler.removeCallbacks(getCount);
                    countHandler.postDelayed(getCount, 200);
                }
            });
        }


        if (!settings.useEmoji || emojiButton == null) {
            try {
                emojiButton.setVisibility(View.GONE);
            } catch (Exception e) {
                // it is a custom layout, so the emoji isn't gonna work :(
            }
        } else {
            emojiKeyboard.setAttached((HoloEditText) reply);

            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (emojiKeyboard.isShowing()) {
                        emojiKeyboard.setVisibility(false);

                        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.emoji_button});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        emojiButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_emoji_keyboard_dark));
                    }
                }
            });

            emojiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (emojiKeyboard.isShowing()) {
                        emojiKeyboard.setVisibility(false);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager imm = (InputMethodManager)context.getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.showSoftInput(reply, 0);
                            }
                        }, 250);

                        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.emoji_button});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        emojiButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_emoji_keyboard_dark));
                    } else {
                        InputMethodManager imm = (InputMethodManager)context.getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(reply.getWindowToken(), 0);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                emojiKeyboard.setVisibility(true);
                            }
                        }, 250);

                        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.keyboardButton});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        emojiButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_light));
                    }
                }
            });
        }

        if (at != null) {
            at.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(context, sharedPrefs.getInt("current_account", 1)).
                            setTitle(getResources().getString(R.string.type_user)).
                            setTitleColor(getResources().getColor(R.color.app_color)).
                            setDividerColor(getResources().getColor(R.color.app_color));

                    qustomDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    qustomDialogBuilder.setPositiveButton(getResources().getString(R.string.add_user), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reply.append(qustomDialogBuilder.text.getText().toString());
                        }
                    });

                    qustomDialogBuilder.show();

                    overflow.performClick();
                }
            });
        }

        // last bool is whether it should open in the external browser or not
        TextUtils.linkifyText(context, retweetertv, null, true, "", true);
        TextUtils.linkifyText(context, tweettv, null, true, "", true);

    }

    public void attachClick() {
        context.sendBroadcast(new Intent("com.klinker.android.twitter.ATTACH_BUTTON"));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //builder.setTitle(getResources().getString(R.string.open_what) + "?");
        builder.setItems(R.array.attach_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if(item == 0) { // take picture
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory() + "/Talon/", "photoToTweet.jpg");

                    if (!f.exists()) {
                        try {
                            f.getParentFile().mkdirs();
                            f.createNewFile();
                        } catch (IOException e) {

                        }
                    }

                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(captureIntent, CAPTURE_IMAGE);
                } else { // attach picture
                    if (attachedUri == null || attachedUri.equals("")) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        try {
                            startActivityForResult(Intent.createChooser(photoPickerIntent,
                                    "Select Picture"), SELECT_PHOTO);
                        } catch (Throwable t) {
                            // no app to preform this..? hmm, tell them that I guess
                            Toast.makeText(context, "No app available to select pictures!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        attachedUri = "";

                        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.attachButton});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        attachImage.setImageDrawable(context.getResources().getDrawable(resource));

                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");

                        try {
                            startActivityForResult(Intent.createChooser(photoPickerIntent,
                                    "Select Picture"), SELECT_PHOTO);
                        } catch (Throwable t) {
                            // no app to preform this..? hmm, tell them that I guess
                            Toast.makeText(context, "No app available to select pictures!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        builder.create().show();
    }

    public Twitter getTwitter() {
        if (secondAcc) {
            return Utils.getSecondTwitter(context);
        } else {
            return Utils.getTwitter(context, settings);
        }
    }

    private boolean isFavorited = false;
    private boolean isRetweet = false;

    public void getFavoriteCount(final TextView favs, final View favButton, final long tweetId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  getTwitter();
                    twitter4j.Status status = twitter.showStatus(tweetId);
                    if (status.isRetweet()) {
                        twitter4j.Status retweeted = status.getRetweetedStatus();
                        status = retweeted;
                    }

                    final twitter4j.Status fStatus = status;
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            favs.setText(" " + fStatus.getFavoriteCount());

                            if (fStatus.isFavorited()) {
                                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.favoritedButton});
                                int resource = a.getResourceId(0, 0);
                                a.recycle();

                                if (favButton instanceof ImageButton) {
                                    if (!settings.addonTheme) {
                                        ((ImageButton)favButton).setColorFilter(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        ((ImageButton)favButton).setColorFilter(settings.accentInt);
                                    }

                                    ((ImageButton)favButton).setImageDrawable(context.getResources().getDrawable(resource));
                                } else if (favButton instanceof LinearLayout) {
                                    if (!settings.addonTheme) {
                                        favButton.setBackgroundColor(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        favButton.setBackgroundColor(settings.accentInt);
                                    }
                                }
                                isFavorited = true;
                            } else {
                                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.notFavoritedButton});
                                int resource = a.getResourceId(0, 0);
                                a.recycle();

                                if (favButton instanceof ImageButton) {
                                    ((ImageButton)favButton).setImageDrawable(context.getResources().getDrawable(resource));
                                    isFavorited = false;

                                    ((ImageButton)favButton).clearColorFilter();
                                } else {
                                    favButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                                }
                            }
                        }
                    });
                } catch (Exception e) {

                }
            }
        }).start();
    }

    class RemoveRetweet extends AsyncTask<String, Void, Boolean> {

        private long tweetId;
        private View retweetButton;

        public RemoveRetweet(long tweetId, View retweetButton) {
            this.tweetId = tweetId;
            this.retweetButton = retweetButton;
        }

        protected void onPreExecute() {
            Toast.makeText(context, getResources().getString(R.string.removing_retweet), Toast.LENGTH_SHORT).show();
        }

        protected Boolean doInBackground(String... urls) {
            try {
                Twitter twitter =  getTwitter();
                ResponseList<twitter4j.Status> retweets = twitter.getRetweets(tweetId);
                for (twitter4j.Status retweet : retweets) {
                    if(retweet.getUser().getId() == settings.myId)
                        twitter.destroyStatus(retweet.getId());
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean deleted) {

            if (retweetButton instanceof ImageButton) {
                ((ImageButton)retweetButton).clearColorFilter();
            } else {
                retweetButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }

            try {
                if (deleted) {
                    Toast.makeText(context, getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // user has gone away from the window
            }
        }
    }

    private Status status = null;

    public void getInfo(final View favButton, final TextView favCount, final TextView retweetCount, final long tweetId, final View retweetButton) {

        Thread getInfo = new Thread(new Runnable() {
            @Override
            public void run() {
                String location = "";
                String via = "";
                long realTime = 0;
                boolean retweetedByMe = false;
                try {
                    Twitter twitter =  getTwitter();

                    TwitterMultipleImageHelper helper = new TwitterMultipleImageHelper();
                    status = twitter.showStatus(tweetId);

                    ArrayList<String> i = new ArrayList<String>();

                    if (picture) {
                        i = helper.getImageURLs(status, twitter);
                    }

                    final ArrayList<String> images = i;

                    GeoLocation loc = status.getGeoLocation();
                    try {
                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                        if (addresses.size() > 0) {
                            Address address = addresses.get(0);
                            location += address.getLocality() + ", " + address.getCountryName();
                        } else {
                            location = "";
                        }
                    } catch (Exception x) {
                        location = "";
                    }

                    via = android.text.Html.fromHtml(status.getSource()).toString();

                    final String sfavCount;
                    if (status.isRetweet()) {
                        twitter4j.Status status2 = status.getRetweetedStatus();
                        via = android.text.Html.fromHtml(status2.getSource()).toString();
                        realTime = status2.getCreatedAt().getTime();
                        sfavCount = status2.getFavoriteCount() + "";
                    } else {
                        realTime = status.getCreatedAt().getTime();
                        sfavCount = status.getFavoriteCount() + "";
                    }

                    retweetedByMe = status.isRetweetedByMe();
                    final String retCount = "" + status.getRetweetCount();

                    final String timeDisplay;

                    if (!settings.militaryTime) {
                        timeDisplay = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(realTime) + " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(realTime);
                    } else {
                        timeDisplay = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN).format(realTime) + " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(realTime);
                    }
                    final String fVia = " " + getResources().getString(R.string.via) + " " + via;
                    final String fLoc = location.equals("") ? "" : "\n" + location;

                    final boolean fRet = retweetedByMe;
                    final long fTime = realTime;
                    final Status fStatus = status;
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // you can't retweet a protected account
                            if (status.getUser().isProtected()) {
                                retweetButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(context, getString(R.string.protected_account), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            retweetCount.setText(" " + retCount);

                            if (retweetButton instanceof ImageButton) {
                                if (fRet) {
                                    if (!settings.addonTheme) {
                                        ((ImageButton)retweetButton).setColorFilter(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        ((ImageButton)retweetButton).setColorFilter(settings.accentInt);
                                    }
                                } else {
                                    ((ImageButton)retweetButton).clearColorFilter();
                                }
                            } else {
                                if (fRet) {
                                    if (!settings.addonTheme) {
                                        retweetButton.setBackgroundColor(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        retweetButton.setBackgroundColor(settings.accentInt);
                                    }
                                } else {
                                    retweetButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                                }
                            }

                            timetv.setText(timeDisplay + fVia);
                            timetv.append(fLoc);

                            favCount.setText(" " + sfavCount);

                            if (favButton instanceof ImageButton) {
                                if (fStatus.isFavorited()) {
                                    TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.favoritedButton});
                                    int resource = a.getResourceId(0, 0);
                                    a.recycle();

                                    if (!settings.addonTheme) {
                                        ((ImageButton)favButton).setColorFilter(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        ((ImageButton)favButton).setColorFilter(settings.accentInt);
                                    }

                                    ((ImageButton)favButton).setImageDrawable(context.getResources().getDrawable(resource));
                                    isFavorited = true;
                                } else {
                                    TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.notFavoritedButton});
                                    int resource = a.getResourceId(0, 0);
                                    a.recycle();

                                    ((ImageButton)favButton).setImageDrawable(context.getResources().getDrawable(resource));
                                    isFavorited = false;

                                    ((ImageButton)favButton).clearColorFilter();
                                }
                            } else {
                                if (fStatus.isFavorited()) {
                                    TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.favoritedButton});
                                    int resource = a.getResourceId(0, 0);
                                    a.recycle();

                                    if (!settings.addonTheme) {
                                        favButton.setBackgroundColor(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        favButton.setBackgroundColor(settings.accentInt);
                                    }

                                    isFavorited = true;
                                } else {
                                    isFavorited = false;

                                    favButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                                }
                            }

                            for (String s : images) {
                                Log.v("talon_image", s);
                            }
                            if (images.size() > 1) {
                                Log.v("talon_images", "size: " + images.size());
                                try {
                                    mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                                        @Override
                                        public void onViewTap(View view, float x, float y) {
                                            Intent viewPics = new Intent(context, ViewPictures.class);
                                            viewPics.putExtra("images", images);
                                            startActivity(viewPics);
                                        }
                                    });
                                } catch (Exception  e) {
                                    // addon theme without the attacher
                                    profilePic.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent viewPics = new Intent(context, ViewPictures.class);
                                            viewPics.putExtra("images", images);
                                            startActivity(viewPics);
                                        }
                                    });
                                }
                            }


                        }
                    });
                } catch (Exception e) {

                }
            }
        });

        getInfo.setPriority(Thread.MAX_PRIORITY);
        getInfo.start();
    }

    public void getRetweetCount(final TextView retweetCount, final long tweetId, final View retweetButton) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean retweetedByMe;
                try {
                    Twitter twitter =  getTwitter();
                    twitter4j.Status status = twitter.showStatus(tweetId);

                    retweetedByMe = status.isRetweetedByMe();
                    final String retCount = "" + status.getRetweetCount();


                    final boolean fRet = retweetedByMe;
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                retweetCount.setText(" " + retCount);

                                if (retweetButton instanceof ImageButton) {
                                    if (fRet) {
                                        if (!settings.addonTheme) {
                                            ((ImageButton) retweetButton).setColorFilter(context.getResources().getColor(R.color.app_color));
                                        } else {
                                            ((ImageButton) retweetButton).setColorFilter(settings.accentInt);
                                        }
                                    } else {
                                        ((ImageButton) retweetButton).clearColorFilter();
                                    }
                                } else {
                                    if (fRet) {
                                        if (!settings.addonTheme) {
                                            retweetButton.setBackgroundColor(context.getResources().getColor(R.color.app_color));
                                        } else {
                                            retweetButton.setBackgroundColor(settings.accentInt);
                                        }
                                    } else {
                                        retweetButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                                    }
                                }
                            } catch (Exception x) {
                                // not attached to activity
                            }
                        }
                    });
                } catch (Exception e) {

                }
            }
        }).start();
    }

    private final int TYPE_ACC_ONE = 1;
    private final int TYPE_ACC_TWO = 2;
    private final int TYPE_BOTH_ACC = 3;

    public void favoriteStatus(final TextView favs, final View favButton, final long tweetId, final int type) {
        if (isFavorited) {
            Toast.makeText(context, getResources().getString(R.string.removing_favorite), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, getResources().getString(R.string.favoriting_status), Toast.LENGTH_SHORT).show();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Twitter twitter = null;
                    Twitter secTwitter = null;
                    if (type == TYPE_ACC_ONE) {
                        twitter = Utils.getTwitter(context, settings);
                    } else if (type == TYPE_ACC_TWO) {
                        secTwitter = Utils.getSecondTwitter(context);
                    } else {
                        twitter = Utils.getTwitter(context, settings);
                        secTwitter = Utils.getSecondTwitter(context);
                    }

                    if (isFavorited && twitter != null) {
                        twitter.destroyFavorite(tweetId);
                    } else if (twitter != null) {
                        try {
                            twitter.createFavorite(tweetId);
                        } catch (TwitterException e) {
                            // already been favorited by this account
                        }
                    }

                    if (secTwitter != null) {
                        secTwitter.createFavorite(tweetId);
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(context, getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
                                getFavoriteCount(favs, favButton, tweetId);
                            } catch (Exception e) {
                                // they quit out of the activity
                            }
                        }
                    });
                } catch (Exception e) {

                }
            }
        }).start();
    }

    public void retweetStatus(final TextView retweetCount, final long tweetId, final View retweetButton, final int type) {
        Toast.makeText(context, getResources().getString(R.string.retweeting_status), Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // if they have a protected account, we want to still be able to retweet their retweets
                    long idToRetweet = tweetId;
                    if (status != null && status.isRetweet()) {
                        idToRetweet = status.getRetweetedStatus().getId();
                    }

                    Twitter twitter = null;
                    Twitter secTwitter = null;
                    if (type == TYPE_ACC_ONE) {
                        twitter = Utils.getTwitter(context, settings);
                    } else if (type == TYPE_ACC_TWO) {
                        secTwitter = Utils.getSecondTwitter(context);
                    } else {
                        twitter = Utils.getTwitter(context, settings);
                        secTwitter = Utils.getSecondTwitter(context);
                    }

                    if (twitter != null) {
                        try {
                            twitter.retweetStatus(tweetId);
                        } catch (TwitterException e) {

                        }
                    }

                    if (secTwitter != null) {
                        secTwitter.retweetStatus(tweetId);
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(context, getResources().getString(R.string.retweet_success), Toast.LENGTH_SHORT).show();
                                getRetweetCount(retweetCount, tweetId, retweetButton);
                            } catch (Exception e) {

                            }
                        }
                    });
                } catch (Exception e) {

                }
            }
        }).start();
    }

    public void removeKeyboard(EditText reply) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(reply.getWindowToken(), 0);
    }

    public void replyToStatus(EditText message, long tweetId, int remainingChars) {
        Intent intent = new Intent(context, SendTweet.class);
        intent.putExtra("message", message.getText().toString());
        intent.putExtra("tweet_id", tweetId);
        intent.putExtra("char_remaining", remainingChars);
        intent.putExtra("pwiccer", pwiccer);
        intent.putExtra("attached_uri", attachedUri);
        intent.putExtra("second_account", secondAcc);

        context.startService(intent);

        removeKeyboard(message);
        ((Activity)context).finish();
    }

    private Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        int reqWidth = 150;
        int reqHeight = 150;

        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = input.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public Bitmap getBitmapToSend(Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        int reqWidth = 750;
        int reqHeight = 750;

        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = input.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    private static final int SELECT_PHOTO = 100;
    private static final int CAPTURE_IMAGE = 101;
    private static final int PWICCER = 420;

    public boolean pwiccer = false;

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == ((Activity)context).RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();

                    try {
                        attachImage.setImageBitmap(getThumbnail(selectedImage));
                        attachedUri = selectedImage.toString();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                    } catch (IOException e) {
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                    }

                    attachImage.setVisibility(View.VISIBLE);
                }
                break;
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Talon/", "photoToTweet.jpg"));

                    try {
                        attachImage.setImageBitmap(getThumbnail(selectedImage));
                        attachedUri = selectedImage.toString();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                    } catch (IOException e) {
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                    }

                    attachImage.setVisibility(View.VISIBLE);
                }
                break;

            case PWICCER:
                if (resultCode == Activity.RESULT_OK) {
                    String path = imageReturnedIntent.getStringExtra("RESULT");
                    attachedUri = Uri.fromFile(new File(path)).toString();

                    try {
                        attachImage.setImageBitmap(getThumbnail(Uri.parse(attachedUri)));
                    } catch (FileNotFoundException e) {
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                    } catch (IOException e) {
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                    }

                    String currText = imageReturnedIntent.getStringExtra("RESULT_TEXT");
                    if (currText != null) {
                        reply.setText(currText);
                        charRemaining.setText("0");
                    }

                    Log.v("talon_pwiccer", "length = " + currText.length());
                    Log.v("talon_pwiccer", currText);

                    pwiccer = true;

                    replyButton.performClick();
                } else {
                    Toast.makeText(context, "Pwiccer failed to generate image! Is it installed?", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public Bitmap decodeSampledBitmapFromResourceMemOpt(
            InputStream inputStream, int reqWidth, int reqHeight) {

        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = inputStream.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options opt, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = opt.outHeight;
        final int width = opt.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
