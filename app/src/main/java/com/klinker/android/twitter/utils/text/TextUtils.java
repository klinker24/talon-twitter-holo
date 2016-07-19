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

package com.klinker.android.twitter.utils.text;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.EmojiUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    public static void linkifyText(Context context, TextView textView, View holder, boolean clickable, String allLinks, boolean extBrowser) {
        Linkify.TransformFilter filter = new Linkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group();
            }
        };

        textView.setLinksClickable(clickable);

        //Linkify.addLinks(context, textView, Patterns.PHONE, null, filter, textView, holder);
        Linkify.addLinks(context, textView, Patterns.EMAIL_ADDRESS, null, filter, textView, holder, allLinks, extBrowser);
        Linkify.addLinks(context, textView, Regex.VALID_URL, null, filter, textView, holder, allLinks, extBrowser);
        Linkify.addLinks(context, textView, Regex.HASHTAG_PATTERN, null, filter, textView, holder, allLinks, extBrowser);
        Linkify.addLinks(context, textView, Regex.CASHTAG_PATTERN, null, filter, textView, holder, allLinks, extBrowser);
        Linkify.addLinks(context, textView, Regex.MENTION_PATTERN, null, filter, textView, holder, allLinks, extBrowser);
    }

    public static void linkifyText(Context context, AppSettings settings, TextView textView, View holder, boolean clickable, String allLinks, boolean extBrowser) {
        Linkify.TransformFilter filter = new Linkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group();
            }
        };

        textView.setLinksClickable(clickable);

        //Linkify.addLinks(context, textView, Patterns.PHONE, null, filter, textView, holder);
        Linkify.addLinks(context, settings, textView, Patterns.EMAIL_ADDRESS, null, filter, textView, holder, allLinks, extBrowser);
        Linkify.addLinks(context, settings, textView, Regex.VALID_URL, null, filter, textView, holder, allLinks, extBrowser);
        Linkify.addLinks(context, settings, textView, Regex.HASHTAG_PATTERN, null, filter, textView, holder, allLinks, extBrowser);
        Linkify.addLinks(context, settings, textView, Regex.CASHTAG_PATTERN, null, filter, textView, holder, allLinks, extBrowser);
        Linkify.addLinks(context, settings, textView, Regex.MENTION_PATTERN, null, filter, textView, holder, allLinks, extBrowser);
    }

    public static Spannable colorText(Context context, String tweet, int color) {
        return colorText(context, tweet, color, false);
    }

    public static Spannable colorText(Context context, String tweet, int color, boolean emojis) {
        Spannable finish = new SpannableString(tweet);

        Matcher m = Regex.MENTION_PATTERN.matcher(tweet);
        while (m.find()) {
            finish = changeText(finish, m.group(0), color);
        }
        m = Regex.HASHTAG_PATTERN.matcher(tweet);
        while (m.find()) {
            finish = changeText(finish, m.group(0), color);
        }
        m = Regex.CASHTAG_PATTERN.matcher(tweet);
        while (m.find()) {
            finish = changeText(finish, m.group(0), color);
        }
        m = Regex.VALID_URL.matcher(tweet);
        while (m.find()) {
            finish = changeText(finish, m.group(0), color);
        }

        if (emojis) {
            EmojiUtils.addSmiles(context, finish);
        }

        return finish;
    }

    public static Spannable changeText(Spannable original, String target, int colour) {
        target = target.replaceAll("\"", "");
        String vString = original.toString();
        int startSpan = 0, endSpan = 0;
        Spannable spanRange = original;

        while (true) {
            startSpan = vString.indexOf(target, endSpan);
            ForegroundColorSpan foreColour = new ForegroundColorSpan(colour);
            //BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(colour);

            // Need a NEW span object every loop, else it just moves the span
            if (startSpan < 0)
                break;
            endSpan = startSpan + target.length();
            spanRange.setSpan(foreColour, startSpan, endSpan,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spanRange;
    }
}
