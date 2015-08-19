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

package com.klinker.android.twitter.fragment;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.activity.TextSizeActivity;
import com.klinker.android.twitter.activity.WearTransactionActivity;
import com.klinker.android.twitter.transaction.KeyProperties;

import java.io.File;

public class ExpandableCardFragment extends CardFragment {

    private static final String ARG_USER_NAME = "user_name";
    private static final String ARG_SCREENNAME = "screenname";
    private static final String ARG_TWEET = "tweet_text";
    private static final String ARG_PRO_PIC_URL = "pro_pic_url";
    private static final String ARG_ID = "tweet_id";
    private static final int MAX_IMAGE_ATTEMPTS = 10;

    private float currentExpansionFactor = 1.2f;
    private Handler handler;

    private TextView text;

    public static ExpandableCardFragment create(CharSequence name, CharSequence screenname, CharSequence tweetText, long tweetId) {
        ExpandableCardFragment fragment = new ExpandableCardFragment();
        Bundle args = new Bundle();
        if (name != null) {
            args.putCharSequence(ARG_USER_NAME, name);
        }

        if (screenname != null) {
            args.putCharSequence(ARG_SCREENNAME, screenname);
        }

        if (tweetText != null) {
            String t = tweetText.toString();
            String[] info = t.split(KeyProperties.DIVIDER);
            if (info.length > 1) {
                args.putCharSequence(ARG_TWEET, info[1]);
            }
            args.putString(ARG_PRO_PIC_URL, info[0]);
        }

        if (tweetId != -1) {
            args.putLong(ARG_ID, tweetId);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        handler = new Handler();

        View view = inflater.inflate(R.layout.card_expandable, container, false);
        final TextView name = (TextView) view.findViewById(R.id.name);
        final TextView screenname = (TextView) view.findViewById(R.id.screenname);
        final CircledImageView profilePic = (CircledImageView) view.findViewById(R.id.profile_picture);
        text = (TextView) view.findViewById(R.id.text);

        Bundle args = this.getArguments();

        if (args != null) {
            if (args.containsKey(ARG_USER_NAME) && name != null) {
                name.setText(args.getCharSequence(ARG_USER_NAME));
            }

            if (args.containsKey(ARG_SCREENNAME) && screenname != null) {
                screenname.setText("@" + args.getCharSequence(ARG_SCREENNAME));
            }

            if (args.containsKey(ARG_TWEET) && text != null) {
                text.setText(args.getCharSequence(ARG_TWEET));
            }
        }

        setExpansionFactor(currentExpansionFactor);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentExpansionFactor *= 5;
                setExpansionFactor(currentExpansionFactor);
            }
        });

        Thread loader = new Thread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }

                File image = new File(getActivity().getCacheDir(), getArguments().getString(ARG_PRO_PIC_URL));
                checkExisting(image, profilePic, 0);
            }
        });
        loader.setPriority(Thread.MIN_PRIORITY);
        loader.start();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        text.setTextSize(Integer.parseInt(sharedPrefs.getString(getString(R.string.pref_text_size_key), TextSizeActivity.DEFAULT_TEXT_SIZE + "")));
    }

    public void checkExisting(File f, final CircledImageView profilePic, int attempts) {
        if (f.exists()) {
            try {
                Bitmap image = BitmapFactory.decodeFile(f.getPath());
                final BitmapDrawable drawable = new BitmapDrawable(getResources(), image);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                       profilePic.setImageDrawable(drawable);
                    }
                });
            } catch (Exception e) {
                // the card has scrolled down, we do this on the initial opening of the app,
                // so that fragment is gone and we can't create the handler..

                // the problem really lies in the incomplete methods for the gridviewpager.
                // this force close comes from their end because of my silly workaround for their
                // broken methods haha
            }
        } else {
            if (attempts == 0) {
                ((WearTransactionActivity) getActivity()).sendImageRequest(getArguments().getString(ARG_PRO_PIC_URL));
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }

            if (attempts < MAX_IMAGE_ATTEMPTS) {
                checkExisting(f, profilePic, attempts + 1);
            }
        }
    }

}
