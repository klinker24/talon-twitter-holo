/*
 * Copyright 2013 Luke Klinker
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

package com.klinker.android.twitter.adapters.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.sq_lite.Recent;
import com.klinker.android.twitter.manipulations.EmojiKeyboard;

import java.util.ArrayList;

public class RecentEmojiAdapter extends BaseEmojiAdapter {

    private ArrayList<Recent> recents;
    private Resources res;

    public RecentEmojiAdapter(Context context, EmojiKeyboard keyboard, ArrayList<Recent> recents) {
        super(context, keyboard);
        this.recents = recents;

        try {
            res = context.getPackageManager().getResourcesForApplication("com.klinker.android.emoji_keyboard_trial");
        } catch (Exception e) {
            try {
                res = context.getPackageManager().getResourcesForApplication("com.klinker.android.emoji_keyboard_trial_ios");
            } catch (Exception f) {
                Log.v("emoji_utils", "no emoji keyboard found");
                return;
            }
        }
    }

    @Override
    public int getCount() {
        try {
            return recents.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
            imageView.setPadding(scale, (int) (scale * 1.2), scale, (int) (scale * 1.2));
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }

        try {
            final RecentEmojiAdapter adapter = this;

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    keyboard.removeRecent(position);
                    adapter.notifyDataSetChanged();
                    return true;
                }
            });

            imageView.setImageDrawable(res.getDrawable(Integer.parseInt(recents.get(position).icon)));
            imageView.setBackgroundResource(R.drawable.pressed_button);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    keyboard.insertEmoji(recents.get(position).text, Integer.parseInt(recents.get(position).icon));
                }
            });
        } catch (Exception e) {
            // most likely, something is messed up between the android and ios emojis, so remove this character
            imageView.performLongClick();
        }

        return imageView;
    }
}