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
import com.klinker.android.twitter.manipulations.EmojiKeyboard;
import com.klinker.android.twitter.utils.EmojiUtils;

public class NatureEmojiAdapter extends BaseEmojiAdapter {

    public NatureEmojiAdapter(Context context, EmojiKeyboard keyboard) {
        super(context, keyboard);

        if (res == null) {
            init(context);
        }
    }

    @Override
    public int getCount() {
        return EmojiUtils.ios ? mEmojiTextsIos.length : mEmojiTexts.length;
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
            imageView.setImageDrawable(res.getDrawable(sIconIds[position]));
        } catch (Exception e) {

        }
        imageView.setBackgroundResource(R.drawable.pressed_button);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard.insertEmoji(EmojiUtils.ios ? mEmojiTextsIos[position] : mEmojiTexts[position], sIconIds[position]);
            }
        });

        return imageView;
    }

    public static final String[] mEmojiTexts = {
            // nature
            "\uD83D\uDC15", "\uD83D\uDC36", "\uD83D\uDC29", "\uD83D\uDC08", "\uD83D\uDC31",
            "\uD83D\uDC00", "\uD83D\uDC01", "\uD83D\uDC2d", "\uD83D\uDC39", "\uD83D\uDC22",
            "\uD83D\uDC07", "\uD83D\uDC30", "\uD83D\uDC13", "\uD83D\uDC14", "\uD83D\uDC23",
            "\uD83D\uDC24", "\uD83D\uDC25", "\uD83D\uDC26", "\uD83D\uDC0f", "\uD83D\uDC11",
            "\uD83D\uDC10", "\uD83D\uDC3a", "\uD83D\uDC03", "\uD83D\uDC02", "\uD83D\uDC04",
            "\uD83D\uDC2e", "\uD83D\uDC34", "\uD83D\uDC17", "\uD83D\uDC16", "\uD83D\uDC37",
            "\uD83D\uDC3d", "\uD83D\uDC38", "\uD83D\uDC0d", "\uD83D\uDC3c", "\uD83D\uDC27",
            "\uD83D\uDC18", "\uD83D\uDC28", "\uD83D\uDC12", "\uD83D\uDC35", "\uD83D\uDC06",
            "\uD83D\uDC2f", "\uD83D\uDC3b", "\uD83D\uDC2a", "\uD83D\uDC2b", "\uD83D\uDC0a",
            "\uD83D\uDC33", "\uD83D\uDC0b", "\uD83D\uDC1f", "\uD83D\uDC20", "\uD83D\uDC21",
            "\uD83D\uDC19", "\uD83D\uDC1a", "\uD83D\uDC2c", "\uD83D\uDC0c", "\uD83D\uDC1b",
            "\uD83D\uDC1c", "\uD83D\uDC1d", "\uD83D\uDC1e", "\uD83D\uDC32", "\uD83D\uDC09",
            "\uD83D\uDC3e", "\uD83C\uDF78", "\uD83C\uDF7A", "\uD83C\uDF7b", "\uD83C\uDF77",
            "\uD83C\uDF79", "\uD83C\uDF76", "\u2615", "\uD83C\uDF75", "\uD83C\uDF7c",
            "\uD83C\uDF74", "\uD83C\uDF68", "\uD83C\uDF67", "\uD83C\uDF66", "\uD83C\uDF65",
            "\uD83C\uDF70", "\uD83C\uDF6a", "\uD83C\uDF6b", "\uD83C\uDF6c", "\uD83C\uDF6d",
            "\uD83C\uDF6e", "\uD83C\uDF6f", "\uD83C\uDF73", "\uD83C\uDF54", "\uD83C\uDF5f",
            "\uD83C\uDF5d", "\uD83C\uDF55", "\uD83C\uDF56", "\uD83C\uDF57", "\uD83C\uDF64",
            "\uD83C\uDF63", "\uD83C\uDF71", "\uD83C\uDF5e", "\uD83C\uDF5c", "\uD83C\uDF59",
            "\uD83C\uDF5a", "\uD83C\uDF5b", "\uD83C\uDF72", "\uD83C\uDF65", "\uD83C\uDF62",
            "\uD83C\uDF61", "\uD83C\uDF58", "\uD83C\uDF60", "\uD83C\uDF4c", "\uD83C\uDF4e",
            "\uD83C\uDF4f", "\uD83C\uDF4a", "\uD83C\uDF4b", "\uD83C\uDF44", "\uD83C\uDF45",
            "\uD83C\uDF46", "\uD83C\uDF47", "\uD83C\uDF48", "\uD83C\uDF49", "\uD83C\uDF50",
            "\uD83C\uDF51", "\uD83C\uDF52", "\uD83C\uDF53", "\uD83C\uDF4d", "\uD83C\uDF30",
            "\uD83C\uDF31", "\uD83C\uDF32", "\uD83C\uDF33", "\uD83C\uDF34", "\uD83C\uDF35",
            "\uD83C\uDF37", "\uD83C\uDF38", "\uD83C\uDF39", "\uD83C\uDF40", "\uD83C\uDF41",
            "\uD83C\uDF42", "\uD83C\uDF43", "\uD83C\uDF3a", "\uD83C\uDF3b", "\uD83C\uDF3c",
            "\uD83C\uDF3d", "\uD83C\uDF3e", "\uD83C\uDF3f", "\u2600", "\uD83C\uDF08",
            "\u26c5", "\u2601", "\uD83C\uDF01", "\uD83C\uDF02", "\u2614",
            "\uD83D\uDCA7", "\u26a1", "\uD83C\uDF00", "\u2744", "\u26c4",
            "\uD83C\uDF19", "\uD83C\uDF1e", "\uD83C\uDF1d", "\uD83C\uDF1a", "\uD83C\uDF1b",
            "\uD83C\uDF1c", "\uD83C\uDF11", "\uD83C\uDF12", "\uD83C\uDF13", "\uD83C\uDF14",
            "\uD83C\uDF15", "\uD83C\uDF16", "\uD83C\uDF17", "\uD83C\uDF18", "\uD83C\uDF91",
            "\uD83C\uDF04", "\uD83C\uDF05", "\uD83C\uDF07", "\uD83C\uDF06", "\uD83C\uDF03",
            "\uD83C\uDF0c", "\uD83C\uDF09", "\uD83C\uDF0a", "\uD83C\uDF0b", "\uD83C\uDF0e",
            "\uD83C\uDF0f", "\uD83C\uDF0d", "\uD83C\uDF10"
    };

    public static final String[] mEmojiTextsIos = {
            // nature
            "\uD83D\uDC15", "\uD83D\uDC36", "\uD83D\uDC29", "\uD83D\uDC08", "\uD83D\uDC31",
            "\uD83D\uDC00", "\uD83D\uDC01", "\uD83D\uDC2d", "\uD83D\uDC39", "\uD83D\uDC22",
            "\uD83D\uDC07", "\uD83D\uDC30", "\uD83D\uDC13", "\uD83D\uDC14", "\uD83D\uDC23",
            "\uD83D\uDC25", "\uD83D\uDC26", "\uD83D\uDC0f", "\uD83D\uDC11",
            "\uD83D\uDC10", "\uD83D\uDC3a", "\uD83D\uDC03", "\uD83D\uDC02", "\uD83D\uDC04",
            "\uD83D\uDC2e", "\uD83D\uDC34", "\uD83D\uDC17", "\uD83D\uDC16", "\uD83D\uDC37",
            "\uD83D\uDC3d", "\uD83D\uDC38", "\uD83D\uDC0d", "\uD83D\uDC3c", "\uD83D\uDC27",
            "\uD83D\uDC18", "\uD83D\uDC28", "\uD83D\uDC12", "\uD83D\uDC35", "\uD83D\uDC06",
            "\uD83D\uDC2f", "\uD83D\uDC3b", "\uD83D\uDC2a", "\uD83D\uDC2b", "\uD83D\uDC0a",
            "\uD83D\uDC33", "\uD83D\uDC0b", "\uD83D\uDC1f", "\uD83D\uDC20", "\uD83D\uDC21",
            "\uD83D\uDC19", "\uD83D\uDC1a", "\uD83D\uDC2c", "\uD83D\uDC0c", "\uD83D\uDC1b",
            "\uD83D\uDC1c", "\uD83D\uDC1d", "\uD83D\uDC1e", "\uD83D\uDC32", "\uD83D\uDC09",
            "\uD83D\uDC3e", "\uD83C\uDF78", "\uD83C\uDF7A", "\uD83C\uDF7b", "\uD83C\uDF77",
            "\uD83C\uDF79", "\uD83C\uDF76", "\u2615", "\uD83C\uDF75", "\uD83C\uDF7c",
            "\uD83C\uDF74", "\uD83C\uDF68", "\uD83C\uDF67", "\uD83C\uDF66", "\uD83C\uDF65",
            "\uD83C\uDF70", "\uD83C\uDF6a", "\uD83C\uDF6b", "\uD83C\uDF6c", "\uD83C\uDF6d",
            "\uD83C\uDF6e", "\uD83C\uDF6f", "\uD83C\uDF73", "\uD83C\uDF54", "\uD83C\uDF5f",
            "\uD83C\uDF5d", "\uD83C\uDF55", "\uD83C\uDF56", "\uD83C\uDF57", "\uD83C\uDF64",
            "\uD83C\uDF63", "\uD83C\uDF71", "\uD83C\uDF5e", "\uD83C\uDF5c", "\uD83C\uDF59",
            "\uD83C\uDF5a", "\uD83C\uDF5b", "\uD83C\uDF72", "\uD83C\uDF65", "\uD83C\uDF62",
            "\uD83C\uDF61", "\uD83C\uDF58", "\uD83C\uDF60", "\uD83C\uDF4c", "\uD83C\uDF4e",
            "\uD83C\uDF4f", "\uD83C\uDF4a", "\uD83C\uDF4b", "\uD83C\uDF44", "\uD83C\uDF45",
            "\uD83C\uDF46", "\uD83C\uDF47", "\uD83C\uDF48", "\uD83C\uDF49", "\uD83C\uDF50",
            "\uD83C\uDF51", "\uD83C\uDF52", "\uD83C\uDF53", "\uD83C\uDF4d", "\uD83C\uDF30",
            "\uD83C\uDF31", "\uD83C\uDF32", "\uD83C\uDF33", "\uD83C\uDF34", "\uD83C\uDF35",
            "\uD83C\uDF37", "\uD83C\uDF38", "\uD83C\uDF39", "\uD83C\uDF40", "\uD83C\uDF41",
            "\uD83C\uDF42", "\uD83C\uDF43", "\uD83C\uDF3a", "\uD83C\uDF3b", "\uD83C\uDF3c",
            "\uD83C\uDF3d", "\uD83C\uDF3e", "\uD83C\uDF3f", "\u2600", "\uD83C\uDF08",
            "\u26c5", "\u2601", "\uD83C\uDF01", "\uD83C\uDF02", "\u2614",
            "\uD83D\uDCA7", "\u26a1", "\uD83C\uDF00", "\u2744", "\u26c4",
            "\uD83C\uDF19", "\uD83C\uDF1e", "\uD83C\uDF1d", "\uD83C\uDF1a", "\uD83C\uDF1b",
            "\uD83C\uDF1c", "\uD83C\uDF11", "\uD83C\uDF12", "\uD83C\uDF13", "\uD83C\uDF14",
            "\uD83C\uDF15", "\uD83C\uDF16", "\uD83C\uDF17", "\uD83C\uDF18", "\uD83C\uDF91",
            "\uD83C\uDF04", "\uD83C\uDF05", "\uD83C\uDF07", "\uD83C\uDF06", "\uD83C\uDF03",
            "\uD83C\uDF0c", "\uD83C\uDF09", "\uD83C\uDF0a", "\uD83C\uDF0b", "\uD83C\uDF0e",
            "\uD83C\uDF0f", "\uD83C\uDF0d", "\uD83C\uDF10"
    };

    private static String pack = "com.klinker.android.emoji_keyboard_trial";
    private static Resources res;
    private static int[] sIconIds;

    public static void init(Context context) {
        try {
            res = context.getPackageManager().getResourcesForApplication(pack);
        } catch (Exception e) {
            try {
                pack = "com.klinker.android.emoji_keyboard_trial_ios";
                res = context.getPackageManager().getResourcesForApplication(pack);
            } catch (Exception f) {
                Log.v("emoji_utils", "no emoji keyboard found");
                return;
            }
        }

        if (!EmojiUtils.ios) {
            sIconIds = new int[]{
                    res.getIdentifier("emoji_u1f415", "drawable", pack),
                    res.getIdentifier("emoji_u1f436", "drawable", pack),
                    res.getIdentifier("emoji_u1f429", "drawable", pack),
                    res.getIdentifier("emoji_u1f408", "drawable", pack),
                    res.getIdentifier("emoji_u1f431", "drawable", pack),
                    res.getIdentifier("emoji_u1f400", "drawable", pack),
                    res.getIdentifier("emoji_u1f401", "drawable", pack),
                    res.getIdentifier("emoji_u1f42d", "drawable", pack),
                    res.getIdentifier("emoji_u1f439", "drawable", pack),
                    res.getIdentifier("emoji_u1f422", "drawable", pack),
                    res.getIdentifier("emoji_u1f407", "drawable", pack),
                    res.getIdentifier("emoji_u1f430", "drawable", pack),
                    res.getIdentifier("emoji_u1f413", "drawable", pack),
                    res.getIdentifier("emoji_u1f414", "drawable", pack),
                    res.getIdentifier("emoji_u1f423", "drawable", pack),
                    res.getIdentifier("emoji_u1f424", "drawable", pack),
                    res.getIdentifier("emoji_u1f425", "drawable", pack),
                    res.getIdentifier("emoji_u1f426", "drawable", pack),
                    res.getIdentifier("emoji_u1f40f", "drawable", pack),
                    res.getIdentifier("emoji_u1f411", "drawable", pack),
                    res.getIdentifier("emoji_u1f410", "drawable", pack),
                    res.getIdentifier("emoji_u1f43a", "drawable", pack),
                    res.getIdentifier("emoji_u1f403", "drawable", pack),
                    res.getIdentifier("emoji_u1f402", "drawable", pack),
                    res.getIdentifier("emoji_u1f404", "drawable", pack),
                    res.getIdentifier("emoji_u1f42e", "drawable", pack),
                    res.getIdentifier("emoji_u1f434", "drawable", pack),
                    res.getIdentifier("emoji_u1f417", "drawable", pack),
                    res.getIdentifier("emoji_u1f416", "drawable", pack),
                    res.getIdentifier("emoji_u1f437", "drawable", pack),
                    res.getIdentifier("emoji_u1f43d", "drawable", pack),
                    res.getIdentifier("emoji_u1f438", "drawable", pack),
                    res.getIdentifier("emoji_u1f40d", "drawable", pack),
                    res.getIdentifier("emoji_u1f43c", "drawable", pack),
                    res.getIdentifier("emoji_u1f427", "drawable", pack),
                    res.getIdentifier("emoji_u1f418", "drawable", pack),
                    res.getIdentifier("emoji_u1f428", "drawable", pack),
                    res.getIdentifier("emoji_u1f412", "drawable", pack),
                    res.getIdentifier("emoji_u1f435", "drawable", pack),
                    res.getIdentifier("emoji_u1f406", "drawable", pack),
                    res.getIdentifier("emoji_u1f42f", "drawable", pack),
                    res.getIdentifier("emoji_u1f43b", "drawable", pack),
                    res.getIdentifier("emoji_u1f42a", "drawable", pack),
                    res.getIdentifier("emoji_u1f42b", "drawable", pack),
                    res.getIdentifier("emoji_u1f40a", "drawable", pack),
                    res.getIdentifier("emoji_u1f433", "drawable", pack),
                    res.getIdentifier("emoji_u1f40b", "drawable", pack),
                    res.getIdentifier("emoji_u1f41f", "drawable", pack),
                    res.getIdentifier("emoji_u1f420", "drawable", pack),
                    res.getIdentifier("emoji_u1f421", "drawable", pack),
                    res.getIdentifier("emoji_u1f419", "drawable", pack),
                    res.getIdentifier("emoji_u1f41a", "drawable", pack),
                    res.getIdentifier("emoji_u1f42c", "drawable", pack),
                    res.getIdentifier("emoji_u1f40c", "drawable", pack),
                    res.getIdentifier("emoji_u1f41b", "drawable", pack),
                    res.getIdentifier("emoji_u1f41c", "drawable", pack),
                    res.getIdentifier("emoji_u1f41d", "drawable", pack),
                    res.getIdentifier("emoji_u1f41e", "drawable", pack),
                    res.getIdentifier("emoji_u1f432", "drawable", pack),
                    res.getIdentifier("emoji_u1f409", "drawable", pack),
                    res.getIdentifier("emoji_u1f43e", "drawable", pack),
                    res.getIdentifier("emoji_u1f378", "drawable", pack),
                    res.getIdentifier("emoji_u1f37a", "drawable", pack),
                    res.getIdentifier("emoji_u1f37b", "drawable", pack),
                    res.getIdentifier("emoji_u1f377", "drawable", pack),
                    res.getIdentifier("emoji_u1f379", "drawable", pack),
                    res.getIdentifier("emoji_u1f376", "drawable", pack),
                    res.getIdentifier("emoji_u2615", "drawable", pack),
                    res.getIdentifier("emoji_u1f375", "drawable", pack),
                    res.getIdentifier("emoji_u1f37c", "drawable", pack),
                    res.getIdentifier("emoji_u1f374", "drawable", pack),
                    res.getIdentifier("emoji_u1f368", "drawable", pack),
                    res.getIdentifier("emoji_u1f367", "drawable", pack),
                    res.getIdentifier("emoji_u1f366", "drawable", pack),
                    res.getIdentifier("emoji_u1f369", "drawable", pack),
                    res.getIdentifier("emoji_u1f370", "drawable", pack),
                    res.getIdentifier("emoji_u1f36a", "drawable", pack),
                    res.getIdentifier("emoji_u1f36b", "drawable", pack),
                    res.getIdentifier("emoji_u1f36c", "drawable", pack),
                    res.getIdentifier("emoji_u1f36d", "drawable", pack),
                    res.getIdentifier("emoji_u1f36e", "drawable", pack),
                    res.getIdentifier("emoji_u1f36f", "drawable", pack),
                    res.getIdentifier("emoji_u1f373", "drawable", pack),
                    res.getIdentifier("emoji_u1f354", "drawable", pack),
                    res.getIdentifier("emoji_u1f35f", "drawable", pack),
                    res.getIdentifier("emoji_u1f35d", "drawable", pack),
                    res.getIdentifier("emoji_u1f355", "drawable", pack),
                    res.getIdentifier("emoji_u1f356", "drawable", pack),
                    res.getIdentifier("emoji_u1f357", "drawable", pack),
                    res.getIdentifier("emoji_u1f364", "drawable", pack),
                    res.getIdentifier("emoji_u1f363", "drawable", pack),
                    res.getIdentifier("emoji_u1f371", "drawable", pack),
                    res.getIdentifier("emoji_u1f35e", "drawable", pack),
                    res.getIdentifier("emoji_u1f35c", "drawable", pack),
                    res.getIdentifier("emoji_u1f359", "drawable", pack),
                    res.getIdentifier("emoji_u1f35a", "drawable", pack),
                    res.getIdentifier("emoji_u1f35b", "drawable", pack),
                    res.getIdentifier("emoji_u1f372", "drawable", pack),
                    res.getIdentifier("emoji_u1f365", "drawable", pack),
                    res.getIdentifier("emoji_u1f362", "drawable", pack),
                    res.getIdentifier("emoji_u1f361", "drawable", pack),
                    res.getIdentifier("emoji_u1f358", "drawable", pack),
                    res.getIdentifier("emoji_u1f360", "drawable", pack),
                    res.getIdentifier("emoji_u1f34c", "drawable", pack),
                    res.getIdentifier("emoji_u1f34e", "drawable", pack),
                    res.getIdentifier("emoji_u1f34f", "drawable", pack),
                    res.getIdentifier("emoji_u1f34a", "drawable", pack),
                    res.getIdentifier("emoji_u1f34b", "drawable", pack),
                    res.getIdentifier("emoji_u1f344", "drawable", pack),
                    res.getIdentifier("emoji_u1f345", "drawable", pack),
                    res.getIdentifier("emoji_u1f346", "drawable", pack),
                    res.getIdentifier("emoji_u1f347", "drawable", pack),
                    res.getIdentifier("emoji_u1f348", "drawable", pack),
                    res.getIdentifier("emoji_u1f349", "drawable", pack),
                    res.getIdentifier("emoji_u1f350", "drawable", pack),
                    res.getIdentifier("emoji_u1f351", "drawable", pack),
                    res.getIdentifier("emoji_u1f352", "drawable", pack),
                    res.getIdentifier("emoji_u1f353", "drawable", pack),
                    res.getIdentifier("emoji_u1f34d", "drawable", pack),
                    res.getIdentifier("emoji_u1f330", "drawable", pack),
                    res.getIdentifier("emoji_u1f331", "drawable", pack),
                    res.getIdentifier("emoji_u1f332", "drawable", pack),
                    res.getIdentifier("emoji_u1f333", "drawable", pack),
                    res.getIdentifier("emoji_u1f334", "drawable", pack),
                    res.getIdentifier("emoji_u1f335", "drawable", pack),
                    res.getIdentifier("emoji_u1f337", "drawable", pack),
                    res.getIdentifier("emoji_u1f338", "drawable", pack),
                    res.getIdentifier("emoji_u1f339", "drawable", pack),
                    res.getIdentifier("emoji_u1f340", "drawable", pack),
                    res.getIdentifier("emoji_u1f341", "drawable", pack),
                    res.getIdentifier("emoji_u1f342", "drawable", pack),
                    res.getIdentifier("emoji_u1f343", "drawable", pack),
                    res.getIdentifier("emoji_u1f33a", "drawable", pack),
                    res.getIdentifier("emoji_u1f33b", "drawable", pack),
                    res.getIdentifier("emoji_u1f33c", "drawable", pack),
                    res.getIdentifier("emoji_u1f33d", "drawable", pack),
                    res.getIdentifier("emoji_u1f33e", "drawable", pack),
                    res.getIdentifier("emoji_u1f33f", "drawable", pack),
                    res.getIdentifier("emoji_u2600", "drawable", pack),
                    res.getIdentifier("emoji_u1f308", "drawable", pack),
                    res.getIdentifier("emoji_u26c5", "drawable", pack),
                    res.getIdentifier("emoji_u2601", "drawable", pack),
                    res.getIdentifier("emoji_u1f301", "drawable", pack),
                    res.getIdentifier("emoji_u1f302", "drawable", pack),
                    res.getIdentifier("emoji_u2614", "drawable", pack),
                    res.getIdentifier("emoji_u1f4a7", "drawable", pack),
                    res.getIdentifier("emoji_u26a1", "drawable", pack),
                    res.getIdentifier("emoji_u1f300", "drawable", pack),
                    res.getIdentifier("emoji_u2744", "drawable", pack),
                    res.getIdentifier("emoji_u26c4", "drawable", pack),
                    res.getIdentifier("emoji_u1f319", "drawable", pack),
                    res.getIdentifier("emoji_u1f31e", "drawable", pack),
                    res.getIdentifier("emoji_u1f31d", "drawable", pack),
                    res.getIdentifier("emoji_u1f31a", "drawable", pack),
                    res.getIdentifier("emoji_u1f31b", "drawable", pack),
                    res.getIdentifier("emoji_u1f31c", "drawable", pack),
                    res.getIdentifier("emoji_u1f311", "drawable", pack),
                    res.getIdentifier("emoji_u1f312", "drawable", pack),
                    res.getIdentifier("emoji_u1f313", "drawable", pack),
                    res.getIdentifier("emoji_u1f314", "drawable", pack),
                    res.getIdentifier("emoji_u1f315", "drawable", pack),
                    res.getIdentifier("emoji_u1f316", "drawable", pack),
                    res.getIdentifier("emoji_u1f317", "drawable", pack),
                    res.getIdentifier("emoji_u1f318", "drawable", pack),
                    res.getIdentifier("emoji_u1f391", "drawable", pack),
                    res.getIdentifier("emoji_u1f304", "drawable", pack),
                    res.getIdentifier("emoji_u1f305", "drawable", pack),
                    res.getIdentifier("emoji_u1f307", "drawable", pack),
                    res.getIdentifier("emoji_u1f306", "drawable", pack),
                    res.getIdentifier("emoji_u1f303", "drawable", pack),
                    res.getIdentifier("emoji_u1f30c", "drawable", pack),
                    res.getIdentifier("emoji_u1f309", "drawable", pack),
                    res.getIdentifier("emoji_u1f30a", "drawable", pack),
                    res.getIdentifier("emoji_u1f30b", "drawable", pack),
                    res.getIdentifier("emoji_u1f30e", "drawable", pack),
                    res.getIdentifier("emoji_u1f30f", "drawable", pack),
                    res.getIdentifier("emoji_u1f30d", "drawable", pack),
                    res.getIdentifier("emoji_u1f310", "drawable", pack)
            };
        } else {
            sIconIds = new int[]{
                    res.getIdentifier("emoji_u1f415", "drawable", pack),
                    res.getIdentifier("emoji_u1f436", "drawable", pack),
                    res.getIdentifier("emoji_u1f429", "drawable", pack),
                    res.getIdentifier("emoji_u1f408", "drawable", pack),
                    res.getIdentifier("emoji_u1f431", "drawable", pack),
                    res.getIdentifier("emoji_u1f400", "drawable", pack),
                    res.getIdentifier("emoji_u1f401", "drawable", pack),
                    res.getIdentifier("emoji_u1f42d", "drawable", pack),
                    res.getIdentifier("emoji_u1f439", "drawable", pack),
                    res.getIdentifier("emoji_u1f422", "drawable", pack),
                    res.getIdentifier("emoji_u1f407", "drawable", pack),
                    res.getIdentifier("emoji_u1f430", "drawable", pack),
                    res.getIdentifier("emoji_u1f413", "drawable", pack),
                    res.getIdentifier("emoji_u1f414", "drawable", pack),
                    res.getIdentifier("emoji_u1f423", "drawable", pack),
                    res.getIdentifier("emoji_u1f425", "drawable", pack),
                    res.getIdentifier("emoji_u1f426", "drawable", pack),
                    res.getIdentifier("emoji_u1f40f", "drawable", pack),
                    res.getIdentifier("emoji_u1f411", "drawable", pack),
                    res.getIdentifier("emoji_u1f410", "drawable", pack),
                    res.getIdentifier("emoji_u1f43a", "drawable", pack),
                    res.getIdentifier("emoji_u1f403", "drawable", pack),
                    res.getIdentifier("emoji_u1f402", "drawable", pack),
                    res.getIdentifier("emoji_u1f404", "drawable", pack),
                    res.getIdentifier("emoji_u1f42e", "drawable", pack),
                    res.getIdentifier("emoji_u1f434", "drawable", pack),
                    res.getIdentifier("emoji_u1f417", "drawable", pack),
                    res.getIdentifier("emoji_u1f416", "drawable", pack),
                    res.getIdentifier("emoji_u1f437", "drawable", pack),
                    res.getIdentifier("emoji_u1f43d", "drawable", pack),
                    res.getIdentifier("emoji_u1f438", "drawable", pack),
                    res.getIdentifier("emoji_u1f40d", "drawable", pack),
                    res.getIdentifier("emoji_u1f43c", "drawable", pack),
                    res.getIdentifier("emoji_u1f427", "drawable", pack),
                    res.getIdentifier("emoji_u1f418", "drawable", pack),
                    res.getIdentifier("emoji_u1f428", "drawable", pack),
                    res.getIdentifier("emoji_u1f412", "drawable", pack),
                    res.getIdentifier("emoji_u1f435", "drawable", pack),
                    res.getIdentifier("emoji_u1f406", "drawable", pack),
                    res.getIdentifier("emoji_u1f42f", "drawable", pack),
                    res.getIdentifier("emoji_u1f43b", "drawable", pack),
                    res.getIdentifier("emoji_u1f42a", "drawable", pack),
                    res.getIdentifier("emoji_u1f42b", "drawable", pack),
                    res.getIdentifier("emoji_u1f40a", "drawable", pack),
                    res.getIdentifier("emoji_u1f433", "drawable", pack),
                    res.getIdentifier("emoji_u1f40b", "drawable", pack),
                    res.getIdentifier("emoji_u1f41f", "drawable", pack),
                    res.getIdentifier("emoji_u1f420", "drawable", pack),
                    res.getIdentifier("emoji_u1f421", "drawable", pack),
                    res.getIdentifier("emoji_u1f419", "drawable", pack),
                    res.getIdentifier("emoji_u1f41a", "drawable", pack),
                    res.getIdentifier("emoji_u1f42c", "drawable", pack),
                    res.getIdentifier("emoji_u1f40c", "drawable", pack),
                    res.getIdentifier("emoji_u1f41b", "drawable", pack),
                    res.getIdentifier("emoji_u1f41c", "drawable", pack),
                    res.getIdentifier("emoji_u1f41d", "drawable", pack),
                    res.getIdentifier("emoji_u1f41e", "drawable", pack),
                    res.getIdentifier("emoji_u1f432", "drawable", pack),
                    res.getIdentifier("emoji_u1f409", "drawable", pack),
                    res.getIdentifier("emoji_u1f43e", "drawable", pack),
                    res.getIdentifier("emoji_u1f378", "drawable", pack),
                    res.getIdentifier("emoji_u1f37a", "drawable", pack),
                    res.getIdentifier("emoji_u1f37b", "drawable", pack),
                    res.getIdentifier("emoji_u1f377", "drawable", pack),
                    res.getIdentifier("emoji_u1f379", "drawable", pack),
                    res.getIdentifier("emoji_u1f376", "drawable", pack),
                    res.getIdentifier("emoji_u2615", "drawable", pack),
                    res.getIdentifier("emoji_u1f375", "drawable", pack),
                    res.getIdentifier("emoji_u1f37c", "drawable", pack),
                    res.getIdentifier("emoji_u1f374", "drawable", pack),
                    res.getIdentifier("emoji_u1f368", "drawable", pack),
                    res.getIdentifier("emoji_u1f367", "drawable", pack),
                    res.getIdentifier("emoji_u1f366", "drawable", pack),
                    res.getIdentifier("emoji_u1f369", "drawable", pack),
                    res.getIdentifier("emoji_u1f370", "drawable", pack),
                    res.getIdentifier("emoji_u1f36a", "drawable", pack),
                    res.getIdentifier("emoji_u1f36b", "drawable", pack),
                    res.getIdentifier("emoji_u1f36c", "drawable", pack),
                    res.getIdentifier("emoji_u1f36d", "drawable", pack),
                    res.getIdentifier("emoji_u1f36e", "drawable", pack),
                    res.getIdentifier("emoji_u1f36f", "drawable", pack),
                    res.getIdentifier("emoji_u1f373", "drawable", pack),
                    res.getIdentifier("emoji_u1f354", "drawable", pack),
                    res.getIdentifier("emoji_u1f35f", "drawable", pack),
                    res.getIdentifier("emoji_u1f35d", "drawable", pack),
                    res.getIdentifier("emoji_u1f355", "drawable", pack),
                    res.getIdentifier("emoji_u1f356", "drawable", pack),
                    res.getIdentifier("emoji_u1f357", "drawable", pack),
                    res.getIdentifier("emoji_u1f364", "drawable", pack),
                    res.getIdentifier("emoji_u1f363", "drawable", pack),
                    res.getIdentifier("emoji_u1f371", "drawable", pack),
                    res.getIdentifier("emoji_u1f35e", "drawable", pack),
                    res.getIdentifier("emoji_u1f35c", "drawable", pack),
                    res.getIdentifier("emoji_u1f359", "drawable", pack),
                    res.getIdentifier("emoji_u1f35a", "drawable", pack),
                    res.getIdentifier("emoji_u1f35b", "drawable", pack),
                    res.getIdentifier("emoji_u1f372", "drawable", pack),
                    res.getIdentifier("emoji_u1f365", "drawable", pack),
                    res.getIdentifier("emoji_u1f362", "drawable", pack),
                    res.getIdentifier("emoji_u1f361", "drawable", pack),
                    res.getIdentifier("emoji_u1f358", "drawable", pack),
                    res.getIdentifier("emoji_u1f360", "drawable", pack),
                    res.getIdentifier("emoji_u1f34c", "drawable", pack),
                    res.getIdentifier("emoji_u1f34e", "drawable", pack),
                    res.getIdentifier("emoji_u1f34f", "drawable", pack),
                    res.getIdentifier("emoji_u1f34a", "drawable", pack),
                    res.getIdentifier("emoji_u1f34b", "drawable", pack),
                    res.getIdentifier("emoji_u1f344", "drawable", pack),
                    res.getIdentifier("emoji_u1f345", "drawable", pack),
                    res.getIdentifier("emoji_u1f346", "drawable", pack),
                    res.getIdentifier("emoji_u1f347", "drawable", pack),
                    res.getIdentifier("emoji_u1f348", "drawable", pack),
                    res.getIdentifier("emoji_u1f349", "drawable", pack),
                    res.getIdentifier("emoji_u1f350", "drawable", pack),
                    res.getIdentifier("emoji_u1f351", "drawable", pack),
                    res.getIdentifier("emoji_u1f352", "drawable", pack),
                    res.getIdentifier("emoji_u1f353", "drawable", pack),
                    res.getIdentifier("emoji_u1f34d", "drawable", pack),
                    res.getIdentifier("emoji_u1f330", "drawable", pack),
                    res.getIdentifier("emoji_u1f331", "drawable", pack),
                    res.getIdentifier("emoji_u1f332", "drawable", pack),
                    res.getIdentifier("emoji_u1f333", "drawable", pack),
                    res.getIdentifier("emoji_u1f334", "drawable", pack),
                    res.getIdentifier("emoji_u1f335", "drawable", pack),
                    res.getIdentifier("emoji_u1f337", "drawable", pack),
                    res.getIdentifier("emoji_u1f338", "drawable", pack),
                    res.getIdentifier("emoji_u1f339", "drawable", pack),
                    res.getIdentifier("emoji_u1f340", "drawable", pack),
                    res.getIdentifier("emoji_u1f341", "drawable", pack),
                    res.getIdentifier("emoji_u1f342", "drawable", pack),
                    res.getIdentifier("emoji_u1f343", "drawable", pack),
                    res.getIdentifier("emoji_u1f33a", "drawable", pack),
                    res.getIdentifier("emoji_u1f33b", "drawable", pack),
                    res.getIdentifier("emoji_u1f33c", "drawable", pack),
                    res.getIdentifier("emoji_u1f33d", "drawable", pack),
                    res.getIdentifier("emoji_u1f33e", "drawable", pack),
                    res.getIdentifier("emoji_u1f33f", "drawable", pack),
                    res.getIdentifier("emoji_u2600", "drawable", pack),
                    res.getIdentifier("emoji_u1f308", "drawable", pack),
                    res.getIdentifier("emoji_u26c5", "drawable", pack),
                    res.getIdentifier("emoji_u2601", "drawable", pack),
                    res.getIdentifier("emoji_u1f301", "drawable", pack),
                    res.getIdentifier("emoji_u1f302", "drawable", pack),
                    res.getIdentifier("emoji_u2614", "drawable", pack),
                    res.getIdentifier("emoji_u1f4a7", "drawable", pack),
                    res.getIdentifier("emoji_u26a1", "drawable", pack),
                    res.getIdentifier("emoji_u1f300", "drawable", pack),
                    res.getIdentifier("emoji_u2744", "drawable", pack),
                    res.getIdentifier("emoji_u26c4", "drawable", pack),
                    res.getIdentifier("emoji_u1f319", "drawable", pack),
                    res.getIdentifier("emoji_u1f31e", "drawable", pack),
                    res.getIdentifier("emoji_u1f31d", "drawable", pack),
                    res.getIdentifier("emoji_u1f31a", "drawable", pack),
                    res.getIdentifier("emoji_u1f31b", "drawable", pack),
                    res.getIdentifier("emoji_u1f31c", "drawable", pack),
                    res.getIdentifier("emoji_u1f311", "drawable", pack),
                    res.getIdentifier("emoji_u1f312", "drawable", pack),
                    res.getIdentifier("emoji_u1f313", "drawable", pack),
                    res.getIdentifier("emoji_u1f314", "drawable", pack),
                    res.getIdentifier("emoji_u1f315", "drawable", pack),
                    res.getIdentifier("emoji_u1f316", "drawable", pack),
                    res.getIdentifier("emoji_u1f317", "drawable", pack),
                    res.getIdentifier("emoji_u1f318", "drawable", pack),
                    res.getIdentifier("emoji_u1f391", "drawable", pack),
                    res.getIdentifier("emoji_u1f304", "drawable", pack),
                    res.getIdentifier("emoji_u1f305", "drawable", pack),
                    res.getIdentifier("emoji_u1f307", "drawable", pack),
                    res.getIdentifier("emoji_u1f306", "drawable", pack),
                    res.getIdentifier("emoji_u1f303", "drawable", pack),
                    res.getIdentifier("emoji_u1f30c", "drawable", pack),
                    res.getIdentifier("emoji_u1f309", "drawable", pack),
                    res.getIdentifier("emoji_u1f30a", "drawable", pack),
                    res.getIdentifier("emoji_u1f30b", "drawable", pack),
                    res.getIdentifier("emoji_u1f30e", "drawable", pack),
                    res.getIdentifier("emoji_u1f30f", "drawable", pack),
                    res.getIdentifier("emoji_u1f30d", "drawable", pack),
                    res.getIdentifier("emoji_u1f310", "drawable", pack)
            };
        }
    }
}