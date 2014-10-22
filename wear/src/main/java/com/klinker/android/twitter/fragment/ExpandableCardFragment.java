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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.activity.TextSizeActivity;
import com.klinker.android.twitter.activity.WearTransactionActivity;
import com.klinker.android.twitter.transaction.KeyProperties;

import java.io.File;

public class ExpandableCardFragment extends CardFragment {

    private static final String ARG_TITLE = "CardFragment_title";
    private static final String ARG_TEXT = "CardFragment_text";
    private static final String ARG_AUTHOR = "CardFragment_author";
    private static final String ARG_ID = "CardFragment_id";
    private static final int MAX_IMAGE_ATTEMPTS = 10;

    private float currentExpansionFactor = 1.2f;
    private Handler handler;

    private TextView text;

    public static ExpandableCardFragment create(CharSequence title, CharSequence text, long articleId) {
        ExpandableCardFragment fragment = new ExpandableCardFragment();
        Bundle args = new Bundle();
        if (title != null) {
            args.putCharSequence(ARG_TITLE, title);
        }

        if (text != null) {
            String t = text.toString();
            String[] info = t.split(KeyProperties.DIVIDER);
            if (info.length > 1) {
                args.putCharSequence(ARG_TEXT, info[1]);
            }
            args.putString(ARG_AUTHOR, info[0]);
        }

        if (articleId != -1) {
            args.putLong(ARG_ID, articleId);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        handler = new Handler();

        View view = inflater.inflate(R.layout.card_expandable, container, false);
        final TextView title = (TextView) view.findViewById(R.id.title);
        text = (TextView) view.findViewById(R.id.text);
        Bundle args = this.getArguments();

        if (args != null) {
            if (args.containsKey(ARG_TITLE) && title != null) {
                title.setText(args.getCharSequence(ARG_TITLE));
            }

            if (args.containsKey(ARG_TEXT)) {
                if (text != null) {
                    text.setText(args.getCharSequence(ARG_TEXT));
                }
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
                File image = new File(getActivity().getCacheDir(), getArguments().getString(ARG_AUTHOR));
                checkExisting(image, title, 0);
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

    public void checkExisting(File f, final TextView title, int attempts) {
        if (f.exists()) {
            try {
                Bitmap image = BitmapFactory.decodeFile(f.getPath());
                final BitmapDrawable drawable = new BitmapDrawable(getResources(), image);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        title.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
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
                ((WearTransactionActivity) getActivity()).sendImageRequest(getArguments().getString(ARG_AUTHOR));
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }

            if (attempts < MAX_IMAGE_ATTEMPTS) {
                checkExisting(f, title, attempts + 1);
            }
        }
    }

}
