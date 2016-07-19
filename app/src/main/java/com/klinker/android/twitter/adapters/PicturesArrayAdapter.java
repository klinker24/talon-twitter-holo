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

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.manipulations.photo_viewer.PhotoViewerActivity;
import com.klinker.android.twitter.manipulations.widgets.NetworkedCacheableImageView;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.tweet_viewer.TweetPager;
import com.klinker.android.twitter.utils.TweetLinkUtils;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.User;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class PicturesArrayAdapter extends ArrayAdapter<String> {

    protected Context context;

    private ArrayList<String> text;
    private ArrayList<Status> statuses;

    private LayoutInflater inflater;
    private AppSettings settings;

    private Handler handler;

    public static class ViewHolder {
        public NetworkedCacheableImageView iv;
        public String url;
    }

    public PicturesArrayAdapter(Context context, ArrayList<String> text, ArrayList<Status> statuses) {
        super(context, R.layout.picture);

        this.context = context;
        this.text = text;
        this.statuses = statuses;

        settings = AppSettings.getInstance(context);
        inflater = LayoutInflater.from(context);

        handler = new Handler();
    }

    @Override
    public int getCount() {
        try {
            return text.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public View newView(ViewGroup viewGroup) {
        View v;
        final ViewHolder holder;

        v = inflater.inflate(R.layout.picture, viewGroup, false);

        holder = new ViewHolder();

        holder.iv = (NetworkedCacheableImageView) v.findViewById(R.id.picture);

        v.setTag(holder);
        return v;
    }

    public void bindView(final View view, Context mContext, final String url, final Status status) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        Log.v("talon_picture", "text: " + status.getText());

        Status thisStatus;

        String retweeter;
        final long time = status.getCreatedAt().getTime();
        long originalTime = 0;

        if (status.isRetweet()) {
            retweeter = status.getUser().getScreenName();

            thisStatus = status.getRetweetedStatus();
            originalTime = thisStatus.getCreatedAt().getTime();
        } else {
            retweeter = "";

            thisStatus = status;
        }
        final String fRetweeter = retweeter;

        final long fOriginalTime = originalTime;

        User user = thisStatus.getUser();

        final long id = thisStatus.getId();
        final String profilePic = user.getBiggerProfileImageURL();
        String tweetTexts = thisStatus.getText();
        final String name = user.getName();
        final String screenname = user.getScreenName();

        String[] html = TweetLinkUtils.getLinksInStatus(thisStatus);
        final String tweetText = html[0];
        final String picUrl = html[1];
        final String otherUrl = html[2];
        final String hashtags = html[3];
        final String users = html[4];

        holder.url = url;

        holder.iv.loadImage(url, false, new NetworkedCacheableImageView.OnImageLoadedListener() {
            @Override
            public void onImageLoaded(CacheableBitmapDrawable result) {
                holder.iv.setBackgroundDrawable(null);
            }
        });

        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link;

                boolean displayPic = !picUrl.equals("");
                if (displayPic) {
                    link = picUrl;
                } else {
                    link = otherUrl.split("  ")[0];
                }

                Log.v("tweet_page", "clicked");
                Intent viewTweet = new Intent(context, TweetPager.class);
                viewTweet.putExtra("name", name);
                viewTweet.putExtra("screenname", screenname);
                viewTweet.putExtra("time", time);
                viewTweet.putExtra("tweet", tweetText);
                viewTweet.putExtra("retweeter", fRetweeter);
                viewTweet.putExtra("webpage", link);
                viewTweet.putExtra("other_links", otherUrl);
                viewTweet.putExtra("picture", displayPic);
                viewTweet.putExtra("tweetid", id);
                viewTweet.putExtra("proPic", profilePic);
                viewTweet.putExtra("users", users);
                viewTweet.putExtra("hashtags", hashtags);

                context.startActivity(viewTweet);
            }
        });

    }

    public void bindView(final View view, Context mContext, final String url) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.iv.loadImage(url, false, new NetworkedCacheableImageView.OnImageLoadedListener() {
            @Override
            public void onImageLoaded(CacheableBitmapDrawable result) {
                holder.iv.setBackgroundDrawable(null);
            }
        });

        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewImage = new Intent(context, PhotoViewerActivity.class);
                viewImage.putExtra("url", url);
                context.startActivity(viewImage);
            }
        });

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v;
        if (convertView == null) {

            v = newView(parent);

        } else {
            v = convertView;

            final ViewHolder holder = (ViewHolder) v.getTag();
            holder.iv.setImageDrawable(null);

            if (settings.theme == AppSettings.THEME_LIGHT) {
                holder.iv.setBackgroundResource(R.drawable.rect_border_light);
            } else {
                holder.iv.setBackgroundResource(R.drawable.rect_border_dark);
            }
        }

        if (statuses != null) {
            bindView(v, context, text.get(position), statuses.get(position));
        } else {
            bindView(v, context, text.get(position));
        }

        return v;
    }

    public String getElement(int pos) {
        return text.get(pos);
    }
}
