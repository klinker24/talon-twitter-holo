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

public class TransEmojiAdapter extends BaseEmojiAdapter {

    public TransEmojiAdapter(Context context, EmojiKeyboard keyboard) {
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

        imageView.setImageDrawable(res.getDrawable(sIconIds[position]));
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
            // transportation
            "\uD83C\uDFE0", "\uD83C\uDFe1", "\uD83C\uDFE2", "\uD83C\uDFE3", "\uD83C\uDFE4",
            "\uD83C\uDFE5", "\uD83C\uDFe6", "\uD83C\uDFE7", "\uD83C\uDFE8", "\uD83C\uDFE9",
            "\uD83C\uDFEa", "\uD83C\uDFeb", "\u26ea", "\u26f2", "\uD83C\uDFEc",
            "\uD83C\uDFEf", "\uD83C\uDFf0", "\uD83C\uDFEd", "\uD83D\uDDFB", "\uD83D\uDDFc",
            "\uD83D\uDC88", "\uD83D\uDD27", "\uD83D\uDD28", "\uD83D\uDD29", "\uD83D\uDEBF",
            "\uD83D\uDEc1", "\uD83D\uDEc0", "\uD83D\uDEbd", "\uD83D\uDEBe", "\uD83C\uDFbd",
            "\uD83C\uDFa3", "\uD83C\uDFb1", "\uD83C\uDFb3", "\u26be", "\u26f3",
            "\uD83C\uDFbe", "\u26bd", "\uD83C\uDFbf", "\uD83C\uDFc0", "\uD83C\uDFc1",
            "\uD83C\uDFc2", "\uD83C\uDFc3", "\uD83C\uDFc4", "\uD83C\uDFc6", "\uD83C\uDFc7",
            "\uD83D\uDC0E", "\uD83C\uDFc8", "\uD83C\uDFc9", "\uD83C\uDFca", "\uD83D\uDE82",
            "\uD83D\uDE83", "\uD83D\uDE84", "\uD83D\uDE85", "\uD83D\uDE86", "\uD83D\uDE87",
            "\u24c2", "\uD83D\uDE88", "\uD83D\uDE8a", "\uD83D\uDE8b", "\uD83D\uDE8c",
            "\uD83D\uDE8d", "\uD83D\uDE8e", "\uD83D\uDE8f", "\uD83D\uDE90", "\uD83D\uDE91",
            "\uD83D\uDE92", "\uD83D\uDE93", "\uD83D\uDE94", "\uD83D\uDE95", "\uD83D\uDE96",
            "\uD83D\uDE97", "\uD83D\uDE98", "\uD83D\uDE99", "\uD83D\uDE9a", "\uD83D\uDE9b",
            "\uD83D\uDE9c", "\uD83D\uDE9d", "\uD83D\uDE9e", "\uD83D\uDE9f", "\uD83D\uDEa0",
            "\uD83D\uDEa1", "\uD83D\uDEa2", "\uD83D\uDEa3", "\uD83D\uDE81", "\u2708",
            "\uD83D\uDEc2", "\uD83D\uDEc3", "\uD83D\uDEc4", "\uD83D\uDEc5", "\u26f5",
            "\uD83D\uDEb2", "\uD83D\uDEb3", "\uD83D\uDEb4", "\uD83D\uDEb5", "\uD83D\uDEb7",
            "\uD83D\uDEb8", "\uD83D\uDE89", "\uD83D\uDE80", "\uD83D\uDEa4", "\uD83D\uDEb6",
            "\u26fd", "\uD83C\uDD7F", "\uD83D\uDEa5", "\uD83D\uDEa6", "\uD83D\uDEa7",
            "\uD83D\uDEa8", "\u2668", "\uD83D\uDC8C", "\uD83D\uDC8d", "\uD83D\uDC8e",
            "\uD83D\uDC90", "\uD83D\uDC92", "\uD83C\uDDEF\uD83C\uDDF5", "\uD83C\uDDFA\uD83C\uDDF8", "\uD83C\uDDEB\uD83C\uDDF7",
            "\uD83C\uDDE9\uD83C\uDDEA", "\uD83C\uDDEE\uD83C\uDDF9", "\uD83C\uDDEC\uD83C\uDDE7", "\uD83C\uDDEA\uD83C\uDDF8", "\uD83C\uDDF7\uD83C\uDDFA",
            "\uD83C\uDDE8\uD83C\uDDF3", "\uD83C\uDDF0\uD83C\uDDF7"
    };

    public static final String[] mEmojiTextsIos = {
            // transportation
            "\uD83C\uDFE0", "\uD83C\uDFe1", "\uD83C\uDFE2", "\uD83C\uDFE3", "\uD83C\uDFE4",
            "\uD83C\uDFE5", "\uD83C\uDFe6", "\uD83C\uDFE7", "\uD83C\uDFE8", "\uD83C\uDFE9",
            "\uD83C\uDFEa", "\uD83C\uDFeb", "\u26ea", "\u26f2", "\uD83C\uDFEc",
            "\uD83C\uDFEf", "\uD83C\uDFf0", "\uD83C\uDFEd", "\uD83D\uDDFB", "\uD83D\uDDFc",
            "\uD83D\uDC88", "\uD83D\uDD27", "\uD83D\uDD28", "\uD83D\uDD29", "\uD83D\uDEBF",
            "\uD83D\uDEc1", "\uD83D\uDEc0", "\uD83D\uDEbd", "\uD83D\uDEBe", "\uD83C\uDFbd",
            "\uD83C\uDFa3", "\uD83C\uDFb1", "\uD83C\uDFb3", "\u26be", "\u26f3",
            "\uD83C\uDFbe", "\u26bd", "\uD83C\uDFbf", "\uD83C\uDFc0", "\uD83C\uDFc1",
            "\uD83C\uDFc2", "\uD83C\uDFc3", "\uD83C\uDFc4", "\uD83C\uDFc6", "\uD83C\uDFc7",
            "\uD83D\uDC0E", "\uD83C\uDFc8", "\uD83C\uDFc9", "\uD83C\uDFca", "\uD83D\uDE82",
            "\uD83D\uDE84", "\uD83D\uDE85", "\uD83D\uDE86", "\uD83D\uDE87",
            "\u24c2", "\uD83D\uDE88", "\uD83D\uDE8a", "\uD83D\uDE8b", "\uD83D\uDE8c",
            "\uD83D\uDE8d", "\uD83D\uDE8e", "\uD83D\uDE8f", "\uD83D\uDE90", "\uD83D\uDE91",
            "\uD83D\uDE92", "\uD83D\uDE93", "\uD83D\uDE94", "\uD83D\uDE95", "\uD83D\uDE96",
            "\uD83D\uDE97", "\uD83D\uDE98", "\uD83D\uDE99", "\uD83D\uDE9a", "\uD83D\uDE9b",
            "\uD83D\uDE9c", "\uD83D\uDE9d", "\uD83D\uDE9f", "\uD83D\uDEa0",
            "\uD83D\uDEa1", "\uD83D\uDEa2", "\uD83D\uDEa3", "\uD83D\uDE81", "\u2708",
            "\uD83D\uDEc2", "\uD83D\uDEc3", "\uD83D\uDEc4", "\uD83D\uDEc5", "\u26f5",
            "\uD83D\uDEb2", "\uD83D\uDEb3", "\uD83D\uDEb4", "\uD83D\uDEb5", "\uD83D\uDEb7",
            "\uD83D\uDEb8", "\uD83D\uDE89", "\uD83D\uDE80", "\uD83D\uDEa4", "\uD83D\uDEb6",
            "\u26fd", "\uD83C\uDD7F", "\uD83D\uDEa5", "\uD83D\uDEa6", "\uD83D\uDEa7",
            "\uD83D\uDEa8", "\u2668", "\uD83D\uDC8C", "\uD83D\uDC8d", "\uD83D\uDC8e",
            "\uD83D\uDC90", "\uD83D\uDC92", "\uD83C\uDDEF\uD83C\uDDF5", "\uD83C\uDDFA\uD83C\uDDF8", "\uD83C\uDDEB\uD83C\uDDF7",
            "\uD83C\uDDE9\uD83C\uDDEA", "\uD83C\uDDEE\uD83C\uDDF9", "\uD83C\uDDEC\uD83C\uDDE7", "\uD83C\uDDEA\uD83C\uDDF8", "\uD83C\uDDF7\uD83C\uDDFA",
            "\uD83C\uDDE8\uD83C\uDDF3", "\uD83C\uDDF0\uD83C\uDDF7"
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
                    res.getIdentifier("emoji_u1f3e0", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e1", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e3", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e4", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e5", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e6", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e7", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e8", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e9", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ea", "drawable", pack),
                    res.getIdentifier("emoji_u1f3eb", "drawable", pack),
                    res.getIdentifier("emoji_u26ea", "drawable", pack),
                    res.getIdentifier("emoji_u26f2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ec", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ef", "drawable", pack),
                    res.getIdentifier("emoji_u1f3f0", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ed", "drawable", pack),
                    res.getIdentifier("emoji_u1f5fb", "drawable", pack),
                    res.getIdentifier("emoji_u1f5fc", "drawable", pack),
                    res.getIdentifier("emoji_u1f488", "drawable", pack),
                    res.getIdentifier("emoji_u1f527", "drawable", pack),
                    res.getIdentifier("emoji_u1f528", "drawable", pack),
                    res.getIdentifier("emoji_u1f529", "drawable", pack),
                    res.getIdentifier("emoji_u1f6bf", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c1", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c0", "drawable", pack),
                    res.getIdentifier("emoji_u1f6bd", "drawable", pack),
                    res.getIdentifier("emoji_u1f6be", "drawable", pack),
                    res.getIdentifier("emoji_u1f3bd", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a3", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b1", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b3", "drawable", pack),
                    res.getIdentifier("emoji_u26be", "drawable", pack),
                    res.getIdentifier("emoji_u26f3", "drawable", pack),
                    res.getIdentifier("emoji_u1f3be", "drawable", pack),
                    res.getIdentifier("emoji_u26bd", "drawable", pack),
                    res.getIdentifier("emoji_u1f3bf", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c0", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c1", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c3", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c4", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c6", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c7", "drawable", pack),
                    res.getIdentifier("emoji_u1f40e", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c8", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c9", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ca", "drawable", pack),
                    res.getIdentifier("emoji_u1f682", "drawable", pack),
                    res.getIdentifier("emoji_u1f683", "drawable", pack),
                    res.getIdentifier("emoji_u1f684", "drawable", pack),
                    res.getIdentifier("emoji_u1f685", "drawable", pack),
                    res.getIdentifier("emoji_u1f686", "drawable", pack),
                    res.getIdentifier("emoji_u1f687", "drawable", pack),
                    res.getIdentifier("emoji_u24c2", "drawable", pack),
                    res.getIdentifier("emoji_u1f688", "drawable", pack),
                    res.getIdentifier("emoji_u1f68a", "drawable", pack),
                    res.getIdentifier("emoji_u1f68b", "drawable", pack),
                    res.getIdentifier("emoji_u1f68c", "drawable", pack),
                    res.getIdentifier("emoji_u1f68d", "drawable", pack),
                    res.getIdentifier("emoji_u1f68e", "drawable", pack),
                    res.getIdentifier("emoji_u1f68f", "drawable", pack),
                    res.getIdentifier("emoji_u1f690", "drawable", pack),
                    res.getIdentifier("emoji_u1f691", "drawable", pack),
                    res.getIdentifier("emoji_u1f692", "drawable", pack),
                    res.getIdentifier("emoji_u1f693", "drawable", pack),
                    res.getIdentifier("emoji_u1f694", "drawable", pack),
                    res.getIdentifier("emoji_u1f695", "drawable", pack),
                    res.getIdentifier("emoji_u1f696", "drawable", pack),
                    res.getIdentifier("emoji_u1f697", "drawable", pack),
                    res.getIdentifier("emoji_u1f698", "drawable", pack),
                    res.getIdentifier("emoji_u1f699", "drawable", pack),
                    res.getIdentifier("emoji_u1f69a", "drawable", pack),
                    res.getIdentifier("emoji_u1f69b", "drawable", pack),
                    res.getIdentifier("emoji_u1f69c", "drawable", pack),
                    res.getIdentifier("emoji_u1f69d", "drawable", pack),
                    res.getIdentifier("emoji_u1f69e", "drawable", pack),
                    res.getIdentifier("emoji_u1f69f", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a0", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a1", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a2", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a3", "drawable", pack),
                    res.getIdentifier("emoji_u1f681", "drawable", pack),
                    res.getIdentifier("emoji_u2708", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c2", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c3", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c4", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c5", "drawable", pack),
                    res.getIdentifier("emoji_u26f5", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b2", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b3", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b4", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b5", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b7", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b8", "drawable", pack),
                    res.getIdentifier("emoji_u1f689", "drawable", pack),
                    res.getIdentifier("emoji_u1f680", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a4", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b6", "drawable", pack),
                    res.getIdentifier("emoji_u26fd", "drawable", pack),
                    res.getIdentifier("emoji_u1f17f", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a5", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a6", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a7", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a8", "drawable", pack),
                    res.getIdentifier("emoji_u2668", "drawable", pack),
                    res.getIdentifier("emoji_u1f48c", "drawable", pack),
                    res.getIdentifier("emoji_u1f48d", "drawable", pack),
                    res.getIdentifier("emoji_u1f48e", "drawable", pack),
                    res.getIdentifier("emoji_u1f490", "drawable", pack),
                    res.getIdentifier("emoji_u1f492", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e5", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e6", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e7", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e8", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e9", "drawable", pack),
                    res.getIdentifier("emoji_ufe4ea", "drawable", pack),
                    res.getIdentifier("emoji_ufe4eb", "drawable", pack),
                    res.getIdentifier("emoji_ufe4ec", "drawable", pack),
                    res.getIdentifier("emoji_ufe4ed", "drawable", pack),
                    res.getIdentifier("emoji_ufe4ee", "drawable", pack)
            };
        } else {
            sIconIds = new int[]{
                    res.getIdentifier("emoji_u1f3e0", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e1", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e3", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e4", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e5", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e6", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e7", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e8", "drawable", pack),
                    res.getIdentifier("emoji_u1f3e9", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ea", "drawable", pack),
                    res.getIdentifier("emoji_u1f3eb", "drawable", pack),
                    res.getIdentifier("emoji_u26ea", "drawable", pack),
                    res.getIdentifier("emoji_u26f2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ec", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ef", "drawable", pack),
                    res.getIdentifier("emoji_u1f3f0", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ed", "drawable", pack),
                    res.getIdentifier("emoji_u1f5fb", "drawable", pack),
                    res.getIdentifier("emoji_u1f5fc", "drawable", pack),
                    res.getIdentifier("emoji_u1f488", "drawable", pack),
                    res.getIdentifier("emoji_u1f527", "drawable", pack),
                    res.getIdentifier("emoji_u1f528", "drawable", pack),
                    res.getIdentifier("emoji_u1f529", "drawable", pack),
                    res.getIdentifier("emoji_u1f6bf", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c1", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c0", "drawable", pack),
                    res.getIdentifier("emoji_u1f6bd", "drawable", pack),
                    res.getIdentifier("emoji_u1f6be", "drawable", pack),
                    res.getIdentifier("emoji_u1f3bd", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a3", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b1", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b3", "drawable", pack),
                    res.getIdentifier("emoji_u26be", "drawable", pack),
                    res.getIdentifier("emoji_u26f3", "drawable", pack),
                    res.getIdentifier("emoji_u1f3be", "drawable", pack),
                    res.getIdentifier("emoji_u26bd", "drawable", pack),
                    res.getIdentifier("emoji_u1f3bf", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c0", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c1", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c3", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c4", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c6", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c7", "drawable", pack),
                    res.getIdentifier("emoji_u1f40e", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c8", "drawable", pack),
                    res.getIdentifier("emoji_u1f3c9", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ca", "drawable", pack),
                    res.getIdentifier("emoji_u1f682", "drawable", pack),
                    res.getIdentifier("emoji_u1f684", "drawable", pack),
                    res.getIdentifier("emoji_u1f685", "drawable", pack),
                    res.getIdentifier("emoji_u1f686", "drawable", pack),
                    res.getIdentifier("emoji_u1f687", "drawable", pack),
                    res.getIdentifier("emoji_u24c2", "drawable", pack),
                    res.getIdentifier("emoji_u1f688", "drawable", pack),
                    res.getIdentifier("emoji_u1f68a", "drawable", pack),
                    res.getIdentifier("emoji_u1f68b", "drawable", pack),
                    res.getIdentifier("emoji_u1f68c", "drawable", pack),
                    res.getIdentifier("emoji_u1f68d", "drawable", pack),
                    res.getIdentifier("emoji_u1f68e", "drawable", pack),
                    res.getIdentifier("emoji_u1f68f", "drawable", pack),
                    res.getIdentifier("emoji_u1f690", "drawable", pack),
                    res.getIdentifier("emoji_u1f691", "drawable", pack),
                    res.getIdentifier("emoji_u1f692", "drawable", pack),
                    res.getIdentifier("emoji_u1f693", "drawable", pack),
                    res.getIdentifier("emoji_u1f694", "drawable", pack),
                    res.getIdentifier("emoji_u1f695", "drawable", pack),
                    res.getIdentifier("emoji_u1f696", "drawable", pack),
                    res.getIdentifier("emoji_u1f697", "drawable", pack),
                    res.getIdentifier("emoji_u1f698", "drawable", pack),
                    res.getIdentifier("emoji_u1f699", "drawable", pack),
                    res.getIdentifier("emoji_u1f69a", "drawable", pack),
                    res.getIdentifier("emoji_u1f69b", "drawable", pack),
                    res.getIdentifier("emoji_u1f69c", "drawable", pack),
                    res.getIdentifier("emoji_u1f69d", "drawable", pack),
                    res.getIdentifier("emoji_u1f69f", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a0", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a1", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a2", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a3", "drawable", pack),
                    res.getIdentifier("emoji_u1f681", "drawable", pack),
                    res.getIdentifier("emoji_u2708", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c2", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c3", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c4", "drawable", pack),
                    res.getIdentifier("emoji_u1f6c5", "drawable", pack),
                    res.getIdentifier("emoji_u26f5", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b2", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b3", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b4", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b5", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b7", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b8", "drawable", pack),
                    res.getIdentifier("emoji_u1f689", "drawable", pack),
                    res.getIdentifier("emoji_u1f680", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a4", "drawable", pack),
                    res.getIdentifier("emoji_u1f6b6", "drawable", pack),
                    res.getIdentifier("emoji_u26fd", "drawable", pack),
                    res.getIdentifier("emoji_u1f17f", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a5", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a6", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a7", "drawable", pack),
                    res.getIdentifier("emoji_u1f6a8", "drawable", pack),
                    res.getIdentifier("emoji_u2668", "drawable", pack),
                    res.getIdentifier("emoji_u1f48c", "drawable", pack),
                    res.getIdentifier("emoji_u1f48d", "drawable", pack),
                    res.getIdentifier("emoji_u1f48e", "drawable", pack),
                    res.getIdentifier("emoji_u1f490", "drawable", pack),
                    res.getIdentifier("emoji_u1f492", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e5", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e6", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e7", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e8", "drawable", pack),
                    res.getIdentifier("emoji_ufe4e9", "drawable", pack),
                    res.getIdentifier("emoji_ufe4ea", "drawable", pack),
                    res.getIdentifier("emoji_ufe4eb", "drawable", pack),
                    res.getIdentifier("emoji_ufe4ec", "drawable", pack),
                    res.getIdentifier("emoji_ufe4ed", "drawable", pack),
                    res.getIdentifier("emoji_ufe4ee", "drawable", pack)
            };
        }
    }
}