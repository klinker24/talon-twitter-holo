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

public class ThingsEmojiAdapter extends BaseEmojiAdapter {

    public ThingsEmojiAdapter(Context context, EmojiKeyboard keyboard) {
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
            //things
            "\uD83D\uDD30", "\uD83D\uDC84", "\uD83D\uDC5e", "\uD83D\uDC5f", "\uD83D\uDC51",
            "\uD83D\uDC52", "\uD83C\uDFa9", "\uD83C\uDF93", "\uD83D\uDC53", "\u231a",
            "\uD83D\uDC54", "\uD83D\uDC55", "\uD83D\uDC56", "\uD83D\uDC57", "\uD83D\uDC58",
            "\uD83D\uDC59", "\uD83D\uDC60", "\uD83D\uDC61", "\uD83D\uDC62", "\uD83D\uDC5a",
            "\uD83D\uDC5c", "\uD83D\uDCbc", "\uD83C\uDF92", "\uD83D\uDC5d", "\uD83D\uDC5b",
            "\uD83D\uDCb0", "\uD83D\uDCb3", "\uD83D\uDCb2", "\uD83D\uDCb5", "\uD83D\uDCb4",
            "\uD83D\uDCb6", "\uD83D\uDCb7", "\uD83D\uDCb1", "\uD83D\uDCb8", "\uD83D\uDCb9",
            "\uD83D\uDD2b", "\uD83D\uDD2a", "\uD83D\uDCa3", "\uD83D\uDC89", "\uD83D\uDC8a",
            "\uD83D\uDEac", "\uD83D\uDD14", "\uD83D\uDD15", "\uD83D\uDEaa", "\uD83D\uDD2c",
            "\uD83D\uDD2d", "\uD83D\uDD2e", "\uD83D\uDD26", "\uD83D\uDD0b", "\uD83D\uDD0c",
            "\uD83D\uDCdc", "\uD83D\uDCd7", "\uD83D\uDCd8", "\uD83D\uDCd9", "\uD83D\uDCda",
            "\uD83D\uDCd4", "\uD83D\uDCd2", "\uD83D\uDCd1", "\uD83D\uDCd3", "\uD83D\uDCd5",
            "\uD83D\uDCd6", "\uD83D\uDCf0", "\uD83D\uDCdb", "\uD83C\uDF83", "\uD83C\uDF84",
            "\uD83C\uDF80", "\uD83C\uDF81", "\uD83C\uDF82", "\uD83C\uDF88", "\uD83C\uDF86",
            "\uD83C\uDF87", "\uD83C\uDF89", "\uD83C\uDF8a", "\uD83C\uDF8d", "\uD83C\uDF8f",
            "\uD83C\uDF8c", "\uD83C\uDF90", "\uD83C\uDF8b", "\uD83C\uDF8e", "\uD83D\uDCf1",
            "\uD83D\uDCf2", "\uD83D\uDCdf", "\u260e", "\uD83D\uDCde", "\uD83D\uDCe0",
            "\uD83D\uDCe6", "\u2709", "\uD83D\uDCe8", "\uD83D\uDCe9", "\uD83D\uDCea",
            "\uD83D\uDCeb", "\uD83D\uDCed", "\uD83D\uDCec", "\uD83D\uDCee", "\uD83D\uDCe4",
            "\uD83D\uDCe5", "\uD83D\uDCef", "\uD83D\uDCe3", "\uD83D\uDCe2", "\uD83D\uDCe1",
            "\uD83D\uDCac", "\uD83D\uDCad", "\u2712", "\u270f", "\uD83D\uDCdd",
            "\uD83D\uDCcf", "\uD83D\uDCd0", "\uD83D\uDCcd", "\uD83D\uDCcc", "\uD83D\uDCce",
            "\u2702", "\uD83D\uDCba", "\uD83D\uDCbb", "\uD83D\uDCbd", "\uD83D\uDCbe",
            "\uD83D\uDCbf", "\uD83D\uDCc6", "\uD83D\uDCc5", "\uD83D\uDCc7", "\uD83D\uDCcb",
            "\uD83D\uDCc1", "\uD83D\uDCc2", "\uD83D\uDCc3", "\uD83D\uDCc4", "\uD83D\uDCca",
            "\uD83D\uDCc8", "\uD83D\uDCc9", "\u26fa", "\uD83C\uDFa1", "\uD83C\uDFa1",
            "\uD83C\uDFa0", "\uD83C\uDFaa", "\uD83C\uDFa8", "\uD83C\uDFac", "\uD83C\uDFa5",
            "\uD83D\uDCf7", "\uD83D\uDCf9", "\uD83C\uDFa6", "\uD83C\uDFad", "\uD83C\uDFab",
            "\uD83C\uDFae", "\uD83C\uDFb2", "\uD83C\uDFb0", "\uD83C\uDCCF", "\uD83C\uDFb4",
            "\uD83C\uDC04", "\uD83C\uDFaf", "\uD83D\uDCfa", "\uD83D\uDCfb", "\uD83D\uDCc0",
            "\uD83D\uDCfc", "\uD83C\uDFa7", "\uD83C\uDFa4", "\uD83C\uDFb5", "\uD83C\uDFb6",
            "\uD83C\uDFbc", "\uD83C\uDFbb", "\uD83C\uDFb9", "\uD83C\uDFb7", "\uD83C\uDFba",
            "\uD83C\uDFb8", "\u303d"
    };

    public static final String[] mEmojiTextsIos = {
            //things
            "\uD83D\uDD30", "\uD83D\uDC84", "\uD83D\uDC5e", "\uD83D\uDC5f", "\uD83D\uDC51",
            "\uD83D\uDC52", "\uD83C\uDFa9", "\uD83C\uDF93", "\uD83D\uDC53", "\u231a",
            "\uD83D\uDC54", "\uD83D\uDC55", "\uD83D\uDC56", "\uD83D\uDC57", "\uD83D\uDC58",
            "\uD83D\uDC59", "\uD83D\uDC60", "\uD83D\uDC61", "\uD83D\uDC62", "\uD83D\uDC5a",
            "\uD83D\uDC5c", "\uD83D\uDCbc", "\uD83C\uDF92", "\uD83D\uDC5d", "\uD83D\uDC5b",
            "\uD83D\uDCb0", "\uD83D\uDCb3", "\uD83D\uDCb2", "\uD83D\uDCb5", "\uD83D\uDCb4",
            "\uD83D\uDCb6", "\uD83D\uDCb7", "\uD83D\uDCb1", "\uD83D\uDCb8", "\uD83D\uDCb9",
            "\uD83D\uDD2b", "\uD83D\uDD2a", "\uD83D\uDCa3", "\uD83D\uDC89", "\uD83D\uDC8a",
            "\uD83D\uDEac", "\uD83D\uDD14", "\uD83D\uDD15", "\uD83D\uDEaa", "\uD83D\uDD2c",
            "\uD83D\uDD2d", "\uD83D\uDD2e", "\uD83D\uDD26", "\uD83D\uDD0b", "\uD83D\uDD0c",
            "\uD83D\uDCdc", "\uD83D\uDCd7", "\uD83D\uDCd8", "\uD83D\uDCd9", "\uD83D\uDCda",
            "\uD83D\uDCd4", "\uD83D\uDCd2", "\uD83D\uDCd1", "\uD83D\uDCd3", "\uD83D\uDCd5",
            "\uD83D\uDCd6", "\uD83D\uDCf0", "\uD83D\uDCdb", "\uD83C\uDF83", "\uD83C\uDF84",
            "\uD83C\uDF80", "\uD83C\uDF81", "\uD83C\uDF82", "\uD83C\uDF88", "\uD83C\uDF86",
            "\uD83C\uDF87", "\uD83C\uDF89", "\uD83C\uDF8a", "\uD83C\uDF8d", "\uD83C\uDF8f",
            "\uD83C\uDF8c", "\uD83C\uDF90", "\uD83C\uDF8b", "\uD83C\uDF8e", "\uD83D\uDCf1",
            "\uD83D\uDCf2", "\uD83D\uDCdf", "\u260e", "\uD83D\uDCde", "\uD83D\uDCe0",
            "\uD83D\uDCe6", "\u2709", "\uD83D\uDCe8", "\uD83D\uDCe9", "\uD83D\uDCea",
            "\uD83D\uDCeb", "\uD83D\uDCed", "\uD83D\uDCec", "\uD83D\uDCee", "\uD83D\uDCe4",
            "\uD83D\uDCe5", "\uD83D\uDCef", "\uD83D\uDCe3", "\uD83D\uDCe2", "\uD83D\uDCe1",
            "\uD83D\uDCac", "\uD83D\uDCad", "\u2712", "\u270f", "\uD83D\uDCdd",
            "\uD83D\uDCcf", "\uD83D\uDCd0", "\uD83D\uDCcd", "\uD83D\uDCcc", "\uD83D\uDCce",
            "\u2702", "\uD83D\uDCba", "\uD83D\uDCbb", "\uD83D\uDCbd", "\uD83D\uDCbe",
            "\uD83D\uDCbf", "\uD83D\uDCc6", "\uD83D\uDCc5", "\uD83D\uDCc7", "\uD83D\uDCcb",
            "\uD83D\uDCc1", "\uD83D\uDCc2", "\uD83D\uDCc3", "\uD83D\uDCc4", "\uD83D\uDCca",
            "\uD83D\uDCc8", "\uD83D\uDCc9", "\u26fa", "\uD83C\uDFa1", "\uD83C\uDFa1",
            "\uD83C\uDFa0", "\uD83C\uDFaa", "\uD83C\uDFa8", "\uD83C\uDFac", "\uD83C\uDFa5",
            "\uD83D\uDCf7", "\uD83D\uDCf9", "\uD83C\uDFad", "\uD83C\uDFab",
            "\uD83C\uDFae", "\uD83C\uDFb2", "\uD83C\uDFb0", "\uD83C\uDCCF", "\uD83C\uDFb4",
            "\uD83C\uDC04", "\uD83C\uDFaf", "\uD83D\uDCfa", "\uD83D\uDCfb", "\uD83D\uDCc0",
            "\uD83D\uDCfc", "\uD83C\uDFa7", "\uD83C\uDFa4", "\uD83C\uDFb5", "\uD83C\uDFb6",
            "\uD83C\uDFbc", "\uD83C\uDFbb", "\uD83C\uDFb9", "\uD83C\uDFb7", "\uD83C\uDFba",
            "\uD83C\uDFb8", "\u303d"
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
                    res.getIdentifier("emoji_u1f530", "drawable", pack),
                    res.getIdentifier("emoji_u1f484", "drawable", pack),
                    res.getIdentifier("emoji_u1f45e", "drawable", pack),
                    res.getIdentifier("emoji_u1f45f", "drawable", pack),
                    res.getIdentifier("emoji_u1f451", "drawable", pack),
                    res.getIdentifier("emoji_u1f452", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a9", "drawable", pack),
                    res.getIdentifier("emoji_u1f393", "drawable", pack),
                    res.getIdentifier("emoji_u1f453", "drawable", pack),
                    res.getIdentifier("emoji_u231a", "drawable", pack),
                    res.getIdentifier("emoji_u1f454", "drawable", pack),
                    res.getIdentifier("emoji_u1f455", "drawable", pack),
                    res.getIdentifier("emoji_u1f456", "drawable", pack),
                    res.getIdentifier("emoji_u1f457", "drawable", pack),
                    res.getIdentifier("emoji_u1f458", "drawable", pack),
                    res.getIdentifier("emoji_u1f459", "drawable", pack),
                    res.getIdentifier("emoji_u1f460", "drawable", pack),
                    res.getIdentifier("emoji_u1f461", "drawable", pack),
                    res.getIdentifier("emoji_u1f462", "drawable", pack),
                    res.getIdentifier("emoji_u1f45a", "drawable", pack),
                    res.getIdentifier("emoji_u1f45c", "drawable", pack),
                    res.getIdentifier("emoji_u1f4bc", "drawable", pack),
                    res.getIdentifier("emoji_u1f392", "drawable", pack),
                    res.getIdentifier("emoji_u1f45d", "drawable", pack),
                    res.getIdentifier("emoji_u1f45b", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b3", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b4", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b6", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b7", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b8", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b9", "drawable", pack),
                    res.getIdentifier("emoji_u1f52b", "drawable", pack),
                    res.getIdentifier("emoji_u1f52a", "drawable", pack),
                    res.getIdentifier("emoji_u1f4a3", "drawable", pack),
                    res.getIdentifier("emoji_u1f489", "drawable", pack),
                    res.getIdentifier("emoji_u1f48a", "drawable", pack),
                    res.getIdentifier("emoji_u1f6ac", "drawable", pack),
                    res.getIdentifier("emoji_u1f514", "drawable", pack),
                    res.getIdentifier("emoji_u1f515", "drawable", pack),
                    res.getIdentifier("emoji_u1f6aa", "drawable", pack),
                    res.getIdentifier("emoji_u1f52c", "drawable", pack),
                    res.getIdentifier("emoji_u1f52d", "drawable", pack),
                    res.getIdentifier("emoji_u1f52e", "drawable", pack),
                    res.getIdentifier("emoji_u1f526", "drawable", pack),
                    res.getIdentifier("emoji_u1f50b", "drawable", pack),
                    res.getIdentifier("emoji_u1f50c", "drawable", pack),
                    res.getIdentifier("emoji_u1f4dc", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d7", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d8", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d9", "drawable", pack),
                    res.getIdentifier("emoji_u1f4da", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d4", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d3", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d6", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4db", "drawable", pack),
                    res.getIdentifier("emoji_u1f383", "drawable", pack),
                    res.getIdentifier("emoji_u1f384", "drawable", pack),
                    res.getIdentifier("emoji_u1f380", "drawable", pack),
                    res.getIdentifier("emoji_u1f381", "drawable", pack),
                    res.getIdentifier("emoji_u1f382", "drawable", pack),
                    res.getIdentifier("emoji_u1f388", "drawable", pack),
                    res.getIdentifier("emoji_u1f386", "drawable", pack),
                    res.getIdentifier("emoji_u1f387", "drawable", pack),
                    res.getIdentifier("emoji_u1f389", "drawable", pack),
                    res.getIdentifier("emoji_u1f38a", "drawable", pack),
                    res.getIdentifier("emoji_u1f38d", "drawable", pack),
                    res.getIdentifier("emoji_u1f38f", "drawable", pack),
                    res.getIdentifier("emoji_u1f38c", "drawable", pack),
                    res.getIdentifier("emoji_u1f390", "drawable", pack),
                    res.getIdentifier("emoji_u1f38b", "drawable", pack),
                    res.getIdentifier("emoji_u1f38e", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4df", "drawable", pack),
                    res.getIdentifier("emoji_u260e", "drawable", pack),
                    res.getIdentifier("emoji_u1f4de", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e6", "drawable", pack),
                    res.getIdentifier("emoji_u2709", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e8", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e9", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ea", "drawable", pack),
                    res.getIdentifier("emoji_u1f4eb", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ed", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ec", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ee", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e4", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ef", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e3", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ac", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ad", "drawable", pack),
                    res.getIdentifier("emoji_u2712", "drawable", pack),
                    res.getIdentifier("emoji_u270f", "drawable", pack),
                    res.getIdentifier("emoji_u1f4dd", "drawable", pack),
                    res.getIdentifier("emoji_u1f4cf", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4cd", "drawable", pack),
                    res.getIdentifier("emoji_u1f4cc", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ce", "drawable", pack),
                    res.getIdentifier("emoji_u2702", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ba", "drawable", pack),
                    res.getIdentifier("emoji_u1f4bb", "drawable", pack),
                    res.getIdentifier("emoji_u1f4bd", "drawable", pack),
                    res.getIdentifier("emoji_u1f4be", "drawable", pack),
                    res.getIdentifier("emoji_u1f4bf", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c6", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c7", "drawable", pack),
                    res.getIdentifier("emoji_u1f4cb", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c3", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c4", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ca", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c8", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c9", "drawable", pack),
                    res.getIdentifier("emoji_u26fa", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a1", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a0", "drawable", pack),
                    res.getIdentifier("emoji_u1f3aa", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a8", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ac", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f7", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f9", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a6", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ad", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ab", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ae", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b0", "drawable", pack),
                    res.getIdentifier("emoji_u1f0cf", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b4", "drawable", pack),
                    res.getIdentifier("emoji_u1f004", "drawable", pack),
                    res.getIdentifier("emoji_u1f3af", "drawable", pack),
                    res.getIdentifier("emoji_u1f4fa", "drawable", pack),
                    res.getIdentifier("emoji_u1f4fb", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4fc", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a7", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a4", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b5", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b6", "drawable", pack),
                    res.getIdentifier("emoji_u1f3bc", "drawable", pack),
                    res.getIdentifier("emoji_u1f3bb", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b9", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b7", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ba", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b8", "drawable", pack),
                    res.getIdentifier("emoji_u303d", "drawable", pack)
            };
        } else {
            sIconIds = new int[]{
                    res.getIdentifier("emoji_u1f530", "drawable", pack),
                    res.getIdentifier("emoji_u1f484", "drawable", pack),
                    res.getIdentifier("emoji_u1f45e", "drawable", pack),
                    res.getIdentifier("emoji_u1f45f", "drawable", pack),
                    res.getIdentifier("emoji_u1f451", "drawable", pack),
                    res.getIdentifier("emoji_u1f452", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a9", "drawable", pack),
                    res.getIdentifier("emoji_u1f393", "drawable", pack),
                    res.getIdentifier("emoji_u1f453", "drawable", pack),
                    res.getIdentifier("emoji_u231a", "drawable", pack),
                    res.getIdentifier("emoji_u1f454", "drawable", pack),
                    res.getIdentifier("emoji_u1f455", "drawable", pack),
                    res.getIdentifier("emoji_u1f456", "drawable", pack),
                    res.getIdentifier("emoji_u1f457", "drawable", pack),
                    res.getIdentifier("emoji_u1f458", "drawable", pack),
                    res.getIdentifier("emoji_u1f459", "drawable", pack),
                    res.getIdentifier("emoji_u1f460", "drawable", pack),
                    res.getIdentifier("emoji_u1f461", "drawable", pack),
                    res.getIdentifier("emoji_u1f462", "drawable", pack),
                    res.getIdentifier("emoji_u1f45a", "drawable", pack),
                    res.getIdentifier("emoji_u1f45c", "drawable", pack),
                    res.getIdentifier("emoji_u1f4bc", "drawable", pack),
                    res.getIdentifier("emoji_u1f392", "drawable", pack),
                    res.getIdentifier("emoji_u1f45d", "drawable", pack),
                    res.getIdentifier("emoji_u1f45b", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b3", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b4", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b6", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b7", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b8", "drawable", pack),
                    res.getIdentifier("emoji_u1f4b9", "drawable", pack),
                    res.getIdentifier("emoji_u1f52b", "drawable", pack),
                    res.getIdentifier("emoji_u1f52a", "drawable", pack),
                    res.getIdentifier("emoji_u1f4a3", "drawable", pack),
                    res.getIdentifier("emoji_u1f489", "drawable", pack),
                    res.getIdentifier("emoji_u1f48a", "drawable", pack),
                    res.getIdentifier("emoji_u1f6ac", "drawable", pack),
                    res.getIdentifier("emoji_u1f514", "drawable", pack),
                    res.getIdentifier("emoji_u1f515", "drawable", pack),
                    res.getIdentifier("emoji_u1f6aa", "drawable", pack),
                    res.getIdentifier("emoji_u1f52c", "drawable", pack),
                    res.getIdentifier("emoji_u1f52d", "drawable", pack),
                    res.getIdentifier("emoji_u1f52e", "drawable", pack),
                    res.getIdentifier("emoji_u1f526", "drawable", pack),
                    res.getIdentifier("emoji_u1f50b", "drawable", pack),
                    res.getIdentifier("emoji_u1f50c", "drawable", pack),
                    res.getIdentifier("emoji_u1f4dc", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d7", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d8", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d9", "drawable", pack),
                    res.getIdentifier("emoji_u1f4da", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d4", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d3", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d6", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4db", "drawable", pack),
                    res.getIdentifier("emoji_u1f383", "drawable", pack),
                    res.getIdentifier("emoji_u1f384", "drawable", pack),
                    res.getIdentifier("emoji_u1f380", "drawable", pack),
                    res.getIdentifier("emoji_u1f381", "drawable", pack),
                    res.getIdentifier("emoji_u1f382", "drawable", pack),
                    res.getIdentifier("emoji_u1f388", "drawable", pack),
                    res.getIdentifier("emoji_u1f386", "drawable", pack),
                    res.getIdentifier("emoji_u1f387", "drawable", pack),
                    res.getIdentifier("emoji_u1f389", "drawable", pack),
                    res.getIdentifier("emoji_u1f38a", "drawable", pack),
                    res.getIdentifier("emoji_u1f38d", "drawable", pack),
                    res.getIdentifier("emoji_u1f38f", "drawable", pack),
                    res.getIdentifier("emoji_u1f38c", "drawable", pack),
                    res.getIdentifier("emoji_u1f390", "drawable", pack),
                    res.getIdentifier("emoji_u1f38b", "drawable", pack),
                    res.getIdentifier("emoji_u1f38e", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4df", "drawable", pack),
                    res.getIdentifier("emoji_u260e", "drawable", pack),
                    res.getIdentifier("emoji_u1f4de", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e6", "drawable", pack),
                    res.getIdentifier("emoji_u2709", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e8", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e9", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ea", "drawable", pack),
                    res.getIdentifier("emoji_u1f4eb", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ed", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ec", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ee", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e4", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ef", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e3", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4e1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ac", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ad", "drawable", pack),
                    res.getIdentifier("emoji_u2712", "drawable", pack),
                    res.getIdentifier("emoji_u270f", "drawable", pack),
                    res.getIdentifier("emoji_u1f4dd", "drawable", pack),
                    res.getIdentifier("emoji_u1f4cf", "drawable", pack),
                    res.getIdentifier("emoji_u1f4d0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4cd", "drawable", pack),
                    res.getIdentifier("emoji_u1f4cc", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ce", "drawable", pack),
                    res.getIdentifier("emoji_u2702", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ba", "drawable", pack),
                    res.getIdentifier("emoji_u1f4bb", "drawable", pack),
                    res.getIdentifier("emoji_u1f4bd", "drawable", pack),
                    res.getIdentifier("emoji_u1f4be", "drawable", pack),
                    res.getIdentifier("emoji_u1f4bf", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c6", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c7", "drawable", pack),
                    res.getIdentifier("emoji_u1f4cb", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c1", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c2", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c3", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c4", "drawable", pack),
                    res.getIdentifier("emoji_u1f4ca", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c8", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c9", "drawable", pack),
                    res.getIdentifier("emoji_u26fa", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a1", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a0", "drawable", pack),
                    res.getIdentifier("emoji_u1f3aa", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a8", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ac", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a5", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f7", "drawable", pack),
                    res.getIdentifier("emoji_u1f4f9", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ad", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ab", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ae", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b2", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b0", "drawable", pack),
                    res.getIdentifier("emoji_u1f0cf", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b4", "drawable", pack),
                    res.getIdentifier("emoji_u1f004", "drawable", pack),
                    res.getIdentifier("emoji_u1f3af", "drawable", pack),
                    res.getIdentifier("emoji_u1f4fa", "drawable", pack),
                    res.getIdentifier("emoji_u1f4fb", "drawable", pack),
                    res.getIdentifier("emoji_u1f4c0", "drawable", pack),
                    res.getIdentifier("emoji_u1f4fc", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a7", "drawable", pack),
                    res.getIdentifier("emoji_u1f3a4", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b5", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b6", "drawable", pack),
                    res.getIdentifier("emoji_u1f3bc", "drawable", pack),
                    res.getIdentifier("emoji_u1f3bb", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b9", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b7", "drawable", pack),
                    res.getIdentifier("emoji_u1f3ba", "drawable", pack),
                    res.getIdentifier("emoji_u1f3b8", "drawable", pack),
                    res.getIdentifier("emoji_u303d", "drawable", pack)
            };
        }
    }
}