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
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.manipulations.EmojiKeyboard;
import com.klinker.android.twitter.utils.EmojiUtils;

public class PeopleEmojiAdapter extends BaseEmojiAdapter {

    public PeopleEmojiAdapter(Context context, EmojiKeyboard keyboard) {
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
        } catch (Resources.NotFoundException e) {
            // don't know why this would happen I guess
            Toast.makeText(context, "Missing an emoji! Is everything installed correctly?", Toast.LENGTH_SHORT).show();
        } catch (OutOfMemoryError e) {
            return null;
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
            // CategoryFragment
            "\u263A", "\uD83D\uDE0A", "\uD83D\uDE00", "\uD83D\uDE01", "\uD83D\uDE02",
            "\uD83D\uDE03", "\uD83D\uDE04", "\uD83D\uDE05", "\uD83D\uDE06", "\uD83D\uDE07",
            "\uD83D\uDE08", "\uD83D\uDE09", "\uD83D\uDE2F", "\uD83D\uDE10", "\uD83D\uDE11",
            "\uD83D\uDE15", "\uD83D\uDE20", "\uD83D\uDE2C", "\uD83D\uDE21", "\uD83D\uDE22",
            "\uD83D\uDE34", "\uD83D\uDE2E", "\uD83D\uDE23", "\uD83D\uDE24", "\uD83D\uDE25",
            "\uD83D\uDE26", "\uD83D\uDE27", "\uD83D\uDE28", "\uD83D\uDE29", "\uD83D\uDE30",
            "\uD83D\uDE1F", "\uD83D\uDE31", "\uD83D\uDE32", "\uD83D\uDE33", "\uD83D\uDE35",
            "\uD83D\uDE36", "\uD83D\uDE37", "\uD83D\uDE1E", "\uD83D\uDE12", "\uD83D\uDE0D",
            "\uD83D\uDE1b", "\uD83D\uDE1c", "\uD83D\uDE1d", "\uD83D\uDE0b", "\uD83D\uDE17",
            "\uD83D\uDE19", "\uD83D\uDE18", "\uD83D\uDE1a", "\uD83D\uDE0e", "\uD83D\uDE2d",
            "\uD83D\uDE0c", "\uD83D\uDE16", "\uD83D\uDE14", "\uD83D\uDE2a", "\uD83D\uDE0f",
            "\uD83D\uDE13", "\uD83D\uDE2b", "\uD83D\uDE4b", "\uD83D\uDE4c", "\uD83D\uDE4d",
            "\uD83D\uDE45", "\uD83D\uDE46", "\uD83D\uDE47", "\uD83D\uDE4e", "\uD83D\uDE4f",
            "\uD83D\uDE3a", "\uD83D\uDE3c", "\uD83D\uDE38", "\uD83D\uDE39", "\uD83D\uDE3b",
            "\uD83D\uDE3d", "\uD83D\uDE3f", "\uD83D\uDE3e", "\uD83D\uDE40", "\uD83D\uDE48",
            "\uD83D\uDE49", "\uD83D\uDE4a", "\uD83D\uDCA9", "\uD83D\uDC76", "\uD83D\uDC66",
            "\uD83D\uDC66", "\uD83D\uDC68", "\uD83D\uDC69", "\uD83D\uDC74", "\uD83D\uDC75",
            "\uD83D\uDC8f", "\uD83D\uDC91", "\uD83D\uDC6a", "\uD83D\uDC6b", "\uD83D\uDC6c",
            "\uD83D\uDC6d", "\uD83D\uDC64", "\uD83D\uDC65", "\uD83D\uDC6e", "\uD83D\uDC77",
            "\uD83D\uDC81", "\uD83D\uDC82", "\uD83D\uDC6f", "\uD83D\uDC70", "\uD83D\uDC78",
            "\uD83C\uDF85", "\uD83D\uDC7c", "\uD83D\uDC71", "\uD83D\uDC72", "\uD83D\uDC73",
            "\uD83D\uDC83", "\uD83D\uDC86", "\uD83D\uDC87", "\uD83D\uDC85", "\uD83D\uDC7b",
            "\uD83D\uDC79", "\uD83D\uDC7a", "\uD83D\uDC7d", "\uD83D\uDC7e", "\uD83D\uDC7f",
            "\uD83D\uDC80", "\uD83D\uDCaa", "\uD83D\uDC40", "\uD83D\uDC42", "\uD83D\uDC43",
            "\uD83D\uDC63", "\uD83D\uDC44", "\uD83D\uDC45", "\uD83D\uDC8b", "\u2764",
            "\uD83D\uDC99", "\uD83D\uDC9a", "\uD83D\uDC9b", "\uD83D\uDC9c", "\uD83D\uDC93",
            "\uD83D\uDC94", "\uD83D\uDC95", "\uD83D\uDC96", "\uD83D\uDC97", "\uD83D\uDC98",
            "\uD83D\uDC9d", "\uD83D\uDC9e", "\uD83D\uDC9f", "\uD83D\uDC4d", "\uD83D\uDC4e",
            "\uD83D\uDC4c", "\u270a", "\u270c", "\u270b", "\uD83D\uDC4a",
            "\u261d", "\uD83D\uDC46", "\uD83D\uDC47", "\uD83D\uDC48", "\uD83D\uDC49",
            "\uD83D\uDC4b", "\uD83D\uDC4f", "\uD83D\uDC50"
    };

    public static final String[] mEmojiTextsIos = {
            // CategoryFragment
            "\u263A", "\uD83D\uDE0A", "\uD83D\uDE00", "\uD83D\uDE02",
            "\uD83D\uDE03", "\uD83D\uDE04", "\uD83D\uDE05", "\uD83D\uDE06", "\uD83D\uDE07",
            "\uD83D\uDE08", "\uD83D\uDE09", "\uD83D\uDE2F", "\uD83D\uDE10", "\uD83D\uDE11",
            "\uD83D\uDE15", "\uD83D\uDE20", "\uD83D\uDE2C", "\uD83D\uDE21", "\uD83D\uDE22",
            "\uD83D\uDE34", "\uD83D\uDE2E", "\uD83D\uDE23", "\uD83D\uDE24", "\uD83D\uDE25",
            "\uD83D\uDE26", "\uD83D\uDE27", "\uD83D\uDE28", "\uD83D\uDE29", "\uD83D\uDE30",
            "\uD83D\uDE1F", "\uD83D\uDE31", "\uD83D\uDE32", "\uD83D\uDE35",
            "\uD83D\uDE36", "\uD83D\uDE37", "\uD83D\uDE1E", "\uD83D\uDE12", "\uD83D\uDE0D",
            "\uD83D\uDE1b", "\uD83D\uDE1c", "\uD83D\uDE1d", "\uD83D\uDE0b", "\uD83D\uDE17",
            "\uD83D\uDE19", "\uD83D\uDE18", "\uD83D\uDE1a", "\uD83D\uDE0e", "\uD83D\uDE2d",
            "\uD83D\uDE0c", "\uD83D\uDE16", "\uD83D\uDE14", "\uD83D\uDE2a", "\uD83D\uDE0f",
            "\uD83D\uDE13", "\uD83D\uDE2b", "\uD83D\uDE4b", "\uD83D\uDE4c", "\uD83D\uDE4d",
            "\uD83D\uDE45", "\uD83D\uDE46", "\uD83D\uDE47", "\uD83D\uDE4e", "\uD83D\uDE4f",
            "\uD83D\uDE3a", "\uD83D\uDE3c", "\uD83D\uDE38", "\uD83D\uDE39", "\uD83D\uDE3b",
            "\uD83D\uDE3d", "\uD83D\uDE3f", "\uD83D\uDE3e", "\uD83D\uDE40", "\uD83D\uDE48",
            "\uD83D\uDE49", "\uD83D\uDE4a", "\uD83D\uDCA9", "\uD83D\uDC76", "\uD83D\uDC66",
            "\uD83D\uDC66", "\uD83D\uDC68", "\uD83D\uDC69", "\uD83D\uDC74", "\uD83D\uDC75",
            "\uD83D\uDC8f", "\uD83D\uDC91", "\uD83D\uDC6a", "\uD83D\uDC6b", "\uD83D\uDC6c",
            "\uD83D\uDC6d", "\uD83D\uDC64", "\uD83D\uDC65", "\uD83D\uDC6e", "\uD83D\uDC77",
            "\uD83D\uDC81", "\uD83D\uDC82", "\uD83D\uDC6f", "\uD83D\uDC70", "\uD83D\uDC78",
            "\uD83C\uDF85", "\uD83D\uDC7c", "\uD83D\uDC71", "\uD83D\uDC72", "\uD83D\uDC73",
            "\uD83D\uDC83", "\uD83D\uDC86", "\uD83D\uDC87", "\uD83D\uDC85", "\uD83D\uDC7b",
            "\uD83D\uDC79", "\uD83D\uDC7a", "\uD83D\uDC7d", "\uD83D\uDC7e", "\uD83D\uDC7f",
            "\uD83D\uDC80", "\uD83D\uDCaa", "\uD83D\uDC40", "\uD83D\uDC42", "\uD83D\uDC43",
            "\uD83D\uDC63", "\uD83D\uDC44", "\uD83D\uDC45", "\uD83D\uDC8b", "\u2764",
            "\uD83D\uDC99", "\uD83D\uDC9a", "\uD83D\uDC9b", "\uD83D\uDC9c", "\uD83D\uDC93",
            "\uD83D\uDC94", "\uD83D\uDC95", "\uD83D\uDC96", "\uD83D\uDC97", "\uD83D\uDC98",
            "\uD83D\uDC9d", "\uD83D\uDC9e", "\uD83D\uDC9f", "\uD83D\uDC4d", "\uD83D\uDC4e",
            "\uD83D\uDC4c", "\u270a", "\u270c", "\u270b", "\uD83D\uDC4a",
            "\u261d", "\uD83D\uDC46", "\uD83D\uDC47", "\uD83D\uDC48", "\uD83D\uDC49",
            "\uD83D\uDC4b", "\uD83D\uDC4f", "\uD83D\uDC50"
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
                    res.getIdentifier("emoji_u263a", "drawable", pack),
                    res.getIdentifier("emoji_u1f60a", "drawable", pack),
                    res.getIdentifier("emoji_u1f600", "drawable", pack),
                    res.getIdentifier("emoji_u1f601", "drawable", pack),
                    res.getIdentifier("emoji_u1f602", "drawable", pack),
                    res.getIdentifier("emoji_u1f603", "drawable", pack),
                    res.getIdentifier("emoji_u1f604", "drawable", pack),
                    res.getIdentifier("emoji_u1f605", "drawable", pack),
                    res.getIdentifier("emoji_u1f606", "drawable", pack),
                    res.getIdentifier("emoji_u1f607", "drawable", pack),
                    res.getIdentifier("emoji_u1f608", "drawable", pack),
                    res.getIdentifier("emoji_u1f609", "drawable", pack),
                    res.getIdentifier("emoji_u1f62f", "drawable", pack),
                    res.getIdentifier("emoji_u1f610", "drawable", pack),
                    res.getIdentifier("emoji_u1f611", "drawable", pack),
                    res.getIdentifier("emoji_u1f615", "drawable", pack),
                    res.getIdentifier("emoji_u1f620", "drawable", pack),
                    res.getIdentifier("emoji_u1f62c", "drawable", pack),
                    res.getIdentifier("emoji_u1f621", "drawable", pack),
                    res.getIdentifier("emoji_u1f622", "drawable", pack),
                    res.getIdentifier("emoji_u1f634", "drawable", pack),
                    res.getIdentifier("emoji_u1f62e", "drawable", pack),
                    res.getIdentifier("emoji_u1f623", "drawable", pack),
                    res.getIdentifier("emoji_u1f624", "drawable", pack),
                    res.getIdentifier("emoji_u1f625", "drawable", pack),
                    res.getIdentifier("emoji_u1f626", "drawable", pack),
                    res.getIdentifier("emoji_u1f627", "drawable", pack),
                    res.getIdentifier("emoji_u1f628", "drawable", pack),
                    res.getIdentifier("emoji_u1f629", "drawable", pack),
                    res.getIdentifier("emoji_u1f630", "drawable", pack),
                    res.getIdentifier("emoji_u1f61f", "drawable", pack),
                    res.getIdentifier("emoji_u1f631", "drawable", pack),
                    res.getIdentifier("emoji_u1f632", "drawable", pack),
                    res.getIdentifier("emoji_u1f633", "drawable", pack),
                    res.getIdentifier("emoji_u1f635", "drawable", pack),
                    res.getIdentifier("emoji_u1f636", "drawable", pack),
                    res.getIdentifier("emoji_u1f637", "drawable", pack),
                    res.getIdentifier("emoji_u1f61e", "drawable", pack),
                    res.getIdentifier("emoji_u1f612", "drawable", pack),
                    res.getIdentifier("emoji_u1f60d", "drawable", pack),
                    res.getIdentifier("emoji_u1f61b", "drawable", pack),
                    res.getIdentifier("emoji_u1f61c", "drawable", pack),
                    res.getIdentifier("emoji_u1f61d", "drawable", pack),
                    res.getIdentifier("emoji_u1f60b", "drawable", pack),
                    res.getIdentifier("emoji_u1f617", "drawable", pack),
                    res.getIdentifier("emoji_u1f619", "drawable", pack),
                    res.getIdentifier("emoji_u1f618", "drawable", pack),
                    res.getIdentifier("emoji_u1f61a", "drawable", pack),
                    res.getIdentifier("emoji_u1f60e", "drawable", pack),
                    res.getIdentifier("emoji_u1f62d", "drawable", pack),
                    res.getIdentifier("emoji_u1f60c", "drawable", pack),
                    res.getIdentifier("emoji_u1f616", "drawable", pack),
                    res.getIdentifier("emoji_u1f614", "drawable", pack),
                    res.getIdentifier("emoji_u1f62a", "drawable", pack),
                    res.getIdentifier("emoji_u1f60f", "drawable", pack),
                    res.getIdentifier("emoji_u1f613", "drawable", pack),
                    res.getIdentifier("emoji_u1f62b", "drawable", pack),
                    res.getIdentifier("emoji_u1f64b", "drawable", pack),
                    res.getIdentifier("emoji_u1f64c", "drawable", pack),
                    res.getIdentifier("emoji_u1f64d", "drawable", pack),
                    res.getIdentifier("emoji_u1f645", "drawable", pack),
                    res.getIdentifier("emoji_u1f646", "drawable", pack),
                    res.getIdentifier("emoji_u1f647", "drawable", pack),
                    res.getIdentifier("emoji_u1f64e", "drawable", pack),
                    res.getIdentifier("emoji_u1f64f", "drawable", pack),
                    res.getIdentifier("emoji_u1f63a", "drawable", pack),
                    res.getIdentifier("emoji_u1f63c", "drawable", pack),
                    res.getIdentifier("emoji_u1f638", "drawable", pack),
                    res.getIdentifier("emoji_u1f639", "drawable", pack),
                    res.getIdentifier("emoji_u1f63b", "drawable", pack),
                    res.getIdentifier("emoji_u1f63d", "drawable", pack),
                    res.getIdentifier("emoji_u1f63f", "drawable", pack),
                    res.getIdentifier("emoji_u1f63e", "drawable", pack),
                    res.getIdentifier("emoji_u1f640", "drawable", pack),
                    res.getIdentifier("emoji_u1f648", "drawable", pack),
                    res.getIdentifier("emoji_u1f649", "drawable", pack),
                    res.getIdentifier("emoji_u1f64a", "drawable", pack),
                    res.getIdentifier("emoji_u1f4a9", "drawable", pack),
                    res.getIdentifier("emoji_u1f476", "drawable", pack),
                    res.getIdentifier("emoji_u1f466", "drawable", pack),
                    res.getIdentifier("emoji_u1f467", "drawable", pack),
                    res.getIdentifier("emoji_u1f468", "drawable", pack),
                    res.getIdentifier("emoji_u1f469", "drawable", pack),
                    res.getIdentifier("emoji_u1f474", "drawable", pack),
                    res.getIdentifier("emoji_u1f475", "drawable", pack),
                    res.getIdentifier("emoji_u1f48f", "drawable", pack),
                    res.getIdentifier("emoji_u1f491", "drawable", pack),
                    res.getIdentifier("emoji_u1f46a", "drawable", pack),
                    res.getIdentifier("emoji_u1f46b", "drawable", pack),
                    res.getIdentifier("emoji_u1f46c", "drawable", pack),
                    res.getIdentifier("emoji_u1f46d", "drawable", pack),
                    res.getIdentifier("emoji_u1f464", "drawable", pack),
                    res.getIdentifier("emoji_u1f465", "drawable", pack),
                    res.getIdentifier("emoji_u1f46e", "drawable", pack),
                    res.getIdentifier("emoji_u1f477", "drawable", pack),
                    res.getIdentifier("emoji_u1f481", "drawable", pack),
                    res.getIdentifier("emoji_u1f482", "drawable", pack),
                    res.getIdentifier("emoji_u1f46f", "drawable", pack),
                    res.getIdentifier("emoji_u1f470", "drawable", pack),
                    res.getIdentifier("emoji_u1f478", "drawable", pack),
                    res.getIdentifier("emoji_u1f385", "drawable", pack),
                    res.getIdentifier("emoji_u1f47c", "drawable", pack),
                    res.getIdentifier("emoji_u1f471", "drawable", pack),
                    res.getIdentifier("emoji_u1f472", "drawable", pack),
                    res.getIdentifier("emoji_u1f473", "drawable", pack),
                    res.getIdentifier("emoji_u1f483", "drawable", pack),
                    res.getIdentifier("emoji_u1f486", "drawable", pack),
                    res.getIdentifier("emoji_u1f487", "drawable", pack),
                    res.getIdentifier("emoji_u1f485", "drawable", pack),
                    res.getIdentifier("emoji_u1f47b", "drawable", pack),
                    res.getIdentifier("emoji_u1f479", "drawable", pack),
                    res.getIdentifier("emoji_u1f47a", "drawable", pack),
                    res.getIdentifier("emoji_u1f47d", "drawable", pack),
                    res.getIdentifier("emoji_u1f47e", "drawable", pack),
                    res.getIdentifier("emoji_u1f47f", "drawable", pack),
                    res.getIdentifier("emoji_u1f480", "drawable", pack),
                    res.getIdentifier("emoji_u1f4aa", "drawable", pack),
                    res.getIdentifier("emoji_u1f440", "drawable", pack),
                    res.getIdentifier("emoji_u1f442", "drawable", pack),
                    res.getIdentifier("emoji_u1f443", "drawable", pack),
                    res.getIdentifier("emoji_u1f463", "drawable", pack),
                    res.getIdentifier("emoji_u1f444", "drawable", pack),
                    res.getIdentifier("emoji_u1f445", "drawable", pack),
                    res.getIdentifier("emoji_u1f48b", "drawable", pack),
                    res.getIdentifier("emoji_u2764", "drawable", pack),
                    res.getIdentifier("emoji_u1f499", "drawable", pack),
                    res.getIdentifier("emoji_u1f49a", "drawable", pack),
                    res.getIdentifier("emoji_u1f49b", "drawable", pack),
                    res.getIdentifier("emoji_u1f49c", "drawable", pack),
                    res.getIdentifier("emoji_u1f493", "drawable", pack),
                    res.getIdentifier("emoji_u1f494", "drawable", pack),
                    res.getIdentifier("emoji_u1f495", "drawable", pack),
                    res.getIdentifier("emoji_u1f496", "drawable", pack),
                    res.getIdentifier("emoji_u1f497", "drawable", pack),
                    res.getIdentifier("emoji_u1f498", "drawable", pack),
                    res.getIdentifier("emoji_u1f49d", "drawable", pack),
                    res.getIdentifier("emoji_u1f49e", "drawable", pack),
                    res.getIdentifier("emoji_u1f49f", "drawable", pack),
                    res.getIdentifier("emoji_u1f44d", "drawable", pack),
                    res.getIdentifier("emoji_u1f44e", "drawable", pack),
                    res.getIdentifier("emoji_u1f44c", "drawable", pack),
                    res.getIdentifier("emoji_u270a", "drawable", pack),
                    res.getIdentifier("emoji_u270c", "drawable", pack),
                    res.getIdentifier("emoji_u270b", "drawable", pack),
                    res.getIdentifier("emoji_u1f44a", "drawable", pack),
                    res.getIdentifier("emoji_u261d", "drawable", pack),
                    res.getIdentifier("emoji_u1f446", "drawable", pack),
                    res.getIdentifier("emoji_u1f447", "drawable", pack),
                    res.getIdentifier("emoji_u1f448", "drawable", pack),
                    res.getIdentifier("emoji_u1f449", "drawable", pack),
                    res.getIdentifier("emoji_u1f44b", "drawable", pack),
                    res.getIdentifier("emoji_u1f44f", "drawable", pack),
                    res.getIdentifier("emoji_u1f450", "drawable", pack)
            };
        } else {
            sIconIds = new int[]{
                    res.getIdentifier("emoji_u263a", "drawable", pack),
                    res.getIdentifier("emoji_u1f60a", "drawable", pack),
                    res.getIdentifier("emoji_u1f600", "drawable", pack),
                    res.getIdentifier("emoji_u1f602", "drawable", pack),
                    res.getIdentifier("emoji_u1f603", "drawable", pack),
                    res.getIdentifier("emoji_u1f604", "drawable", pack),
                    res.getIdentifier("emoji_u1f605", "drawable", pack),
                    res.getIdentifier("emoji_u1f606", "drawable", pack),
                    res.getIdentifier("emoji_u1f607", "drawable", pack),
                    res.getIdentifier("emoji_u1f608", "drawable", pack),
                    res.getIdentifier("emoji_u1f609", "drawable", pack),
                    res.getIdentifier("emoji_u1f62f", "drawable", pack),
                    res.getIdentifier("emoji_u1f610", "drawable", pack),
                    res.getIdentifier("emoji_u1f611", "drawable", pack),
                    res.getIdentifier("emoji_u1f615", "drawable", pack),
                    res.getIdentifier("emoji_u1f620", "drawable", pack),
                    res.getIdentifier("emoji_u1f62c", "drawable", pack),
                    res.getIdentifier("emoji_u1f621", "drawable", pack),
                    res.getIdentifier("emoji_u1f622", "drawable", pack),
                    res.getIdentifier("emoji_u1f634", "drawable", pack),
                    res.getIdentifier("emoji_u1f62e", "drawable", pack),
                    res.getIdentifier("emoji_u1f623", "drawable", pack),
                    res.getIdentifier("emoji_u1f624", "drawable", pack),
                    res.getIdentifier("emoji_u1f625", "drawable", pack),
                    res.getIdentifier("emoji_u1f626", "drawable", pack),
                    res.getIdentifier("emoji_u1f627", "drawable", pack),
                    res.getIdentifier("emoji_u1f628", "drawable", pack),
                    res.getIdentifier("emoji_u1f629", "drawable", pack),
                    res.getIdentifier("emoji_u1f630", "drawable", pack),
                    res.getIdentifier("emoji_u1f61f", "drawable", pack),
                    res.getIdentifier("emoji_u1f631", "drawable", pack),
                    res.getIdentifier("emoji_u1f632", "drawable", pack),
                    res.getIdentifier("emoji_u1f635", "drawable", pack),
                    res.getIdentifier("emoji_u1f636", "drawable", pack),
                    res.getIdentifier("emoji_u1f637", "drawable", pack),
                    res.getIdentifier("emoji_u1f61e", "drawable", pack),
                    res.getIdentifier("emoji_u1f612", "drawable", pack),
                    res.getIdentifier("emoji_u1f60d", "drawable", pack),
                    res.getIdentifier("emoji_u1f61b", "drawable", pack),
                    res.getIdentifier("emoji_u1f61c", "drawable", pack),
                    res.getIdentifier("emoji_u1f61d", "drawable", pack),
                    res.getIdentifier("emoji_u1f60b", "drawable", pack),
                    res.getIdentifier("emoji_u1f617", "drawable", pack),
                    res.getIdentifier("emoji_u1f619", "drawable", pack),
                    res.getIdentifier("emoji_u1f618", "drawable", pack),
                    res.getIdentifier("emoji_u1f61a", "drawable", pack),
                    res.getIdentifier("emoji_u1f60e", "drawable", pack),
                    res.getIdentifier("emoji_u1f62d", "drawable", pack),
                    res.getIdentifier("emoji_u1f60c", "drawable", pack),
                    res.getIdentifier("emoji_u1f616", "drawable", pack),
                    res.getIdentifier("emoji_u1f614", "drawable", pack),
                    res.getIdentifier("emoji_u1f62a", "drawable", pack),
                    res.getIdentifier("emoji_u1f60f", "drawable", pack),
                    res.getIdentifier("emoji_u1f613", "drawable", pack),
                    res.getIdentifier("emoji_u1f62b", "drawable", pack),
                    res.getIdentifier("emoji_u1f64b", "drawable", pack),
                    res.getIdentifier("emoji_u1f64c", "drawable", pack),
                    res.getIdentifier("emoji_u1f64d", "drawable", pack),
                    res.getIdentifier("emoji_u1f645", "drawable", pack),
                    res.getIdentifier("emoji_u1f646", "drawable", pack),
                    res.getIdentifier("emoji_u1f647", "drawable", pack),
                    res.getIdentifier("emoji_u1f64e", "drawable", pack),
                    res.getIdentifier("emoji_u1f64f", "drawable", pack),
                    res.getIdentifier("emoji_u1f63a", "drawable", pack),
                    res.getIdentifier("emoji_u1f63c", "drawable", pack),
                    res.getIdentifier("emoji_u1f638", "drawable", pack),
                    res.getIdentifier("emoji_u1f639", "drawable", pack),
                    res.getIdentifier("emoji_u1f63b", "drawable", pack),
                    res.getIdentifier("emoji_u1f63d", "drawable", pack),
                    res.getIdentifier("emoji_u1f63f", "drawable", pack),
                    res.getIdentifier("emoji_u1f63e", "drawable", pack),
                    res.getIdentifier("emoji_u1f640", "drawable", pack),
                    res.getIdentifier("emoji_u1f648", "drawable", pack),
                    res.getIdentifier("emoji_u1f649", "drawable", pack),
                    res.getIdentifier("emoji_u1f64a", "drawable", pack),
                    res.getIdentifier("emoji_u1f4a9", "drawable", pack),
                    res.getIdentifier("emoji_u1f476", "drawable", pack),
                    res.getIdentifier("emoji_u1f466", "drawable", pack),
                    res.getIdentifier("emoji_u1f467", "drawable", pack),
                    res.getIdentifier("emoji_u1f468", "drawable", pack),
                    res.getIdentifier("emoji_u1f469", "drawable", pack),
                    res.getIdentifier("emoji_u1f474", "drawable", pack),
                    res.getIdentifier("emoji_u1f475", "drawable", pack),
                    res.getIdentifier("emoji_u1f48f", "drawable", pack),
                    res.getIdentifier("emoji_u1f491", "drawable", pack),
                    res.getIdentifier("emoji_u1f46a", "drawable", pack),
                    res.getIdentifier("emoji_u1f46b", "drawable", pack),
                    res.getIdentifier("emoji_u1f46c", "drawable", pack),
                    res.getIdentifier("emoji_u1f46d", "drawable", pack),
                    res.getIdentifier("emoji_u1f464", "drawable", pack),
                    res.getIdentifier("emoji_u1f465", "drawable", pack),
                    res.getIdentifier("emoji_u1f46e", "drawable", pack),
                    res.getIdentifier("emoji_u1f477", "drawable", pack),
                    res.getIdentifier("emoji_u1f481", "drawable", pack),
                    res.getIdentifier("emoji_u1f482", "drawable", pack),
                    res.getIdentifier("emoji_u1f46f", "drawable", pack),
                    res.getIdentifier("emoji_u1f470", "drawable", pack),
                    res.getIdentifier("emoji_u1f478", "drawable", pack),
                    res.getIdentifier("emoji_u1f385", "drawable", pack),
                    res.getIdentifier("emoji_u1f47c", "drawable", pack),
                    res.getIdentifier("emoji_u1f471", "drawable", pack),
                    res.getIdentifier("emoji_u1f472", "drawable", pack),
                    res.getIdentifier("emoji_u1f473", "drawable", pack),
                    res.getIdentifier("emoji_u1f483", "drawable", pack),
                    res.getIdentifier("emoji_u1f486", "drawable", pack),
                    res.getIdentifier("emoji_u1f487", "drawable", pack),
                    res.getIdentifier("emoji_u1f485", "drawable", pack),
                    res.getIdentifier("emoji_u1f47b", "drawable", pack),
                    res.getIdentifier("emoji_u1f479", "drawable", pack),
                    res.getIdentifier("emoji_u1f47a", "drawable", pack),
                    res.getIdentifier("emoji_u1f47d", "drawable", pack),
                    res.getIdentifier("emoji_u1f47e", "drawable", pack),
                    res.getIdentifier("emoji_u1f47f", "drawable", pack),
                    res.getIdentifier("emoji_u1f480", "drawable", pack),
                    res.getIdentifier("emoji_u1f4aa", "drawable", pack),
                    res.getIdentifier("emoji_u1f440", "drawable", pack),
                    res.getIdentifier("emoji_u1f442", "drawable", pack),
                    res.getIdentifier("emoji_u1f443", "drawable", pack),
                    res.getIdentifier("emoji_u1f463", "drawable", pack),
                    res.getIdentifier("emoji_u1f444", "drawable", pack),
                    res.getIdentifier("emoji_u1f445", "drawable", pack),
                    res.getIdentifier("emoji_u1f48b", "drawable", pack),
                    res.getIdentifier("emoji_u2764", "drawable", pack),
                    res.getIdentifier("emoji_u1f499", "drawable", pack),
                    res.getIdentifier("emoji_u1f49a", "drawable", pack),
                    res.getIdentifier("emoji_u1f49b", "drawable", pack),
                    res.getIdentifier("emoji_u1f49c", "drawable", pack),
                    res.getIdentifier("emoji_u1f493", "drawable", pack),
                    res.getIdentifier("emoji_u1f494", "drawable", pack),
                    res.getIdentifier("emoji_u1f495", "drawable", pack),
                    res.getIdentifier("emoji_u1f496", "drawable", pack),
                    res.getIdentifier("emoji_u1f497", "drawable", pack),
                    res.getIdentifier("emoji_u1f498", "drawable", pack),
                    res.getIdentifier("emoji_u1f49d", "drawable", pack),
                    res.getIdentifier("emoji_u1f49e", "drawable", pack),
                    res.getIdentifier("emoji_u1f49f", "drawable", pack),
                    res.getIdentifier("emoji_u1f44d", "drawable", pack),
                    res.getIdentifier("emoji_u1f44e", "drawable", pack),
                    res.getIdentifier("emoji_u1f44c", "drawable", pack),
                    res.getIdentifier("emoji_u270a", "drawable", pack),
                    res.getIdentifier("emoji_u270c", "drawable", pack),
                    res.getIdentifier("emoji_u270b", "drawable", pack),
                    res.getIdentifier("emoji_u1f44a", "drawable", pack),
                    res.getIdentifier("emoji_u261d", "drawable", pack),
                    res.getIdentifier("emoji_u1f446", "drawable", pack),
                    res.getIdentifier("emoji_u1f447", "drawable", pack),
                    res.getIdentifier("emoji_u1f448", "drawable", pack),
                    res.getIdentifier("emoji_u1f449", "drawable", pack),
                    res.getIdentifier("emoji_u1f44b", "drawable", pack),
                    res.getIdentifier("emoji_u1f44f", "drawable", pack),
                    res.getIdentifier("emoji_u1f450", "drawable", pack)
            };
        }
    }
}