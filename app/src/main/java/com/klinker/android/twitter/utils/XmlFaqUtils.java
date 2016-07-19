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

package com.klinker.android.twitter.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Point;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.ListView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.FaqAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlFaqUtils {

    private static final String TAG = "XmlFaqUtils";
    private static final String ns = null;

    private static List items;

    public static final class FAQ {
        public Spanned question;
        public Spanned text;
    }

    public static FAQ[] parse(Context context) {
        try {
            XmlResourceParser parser = context.getResources().getXml(R.xml.classic_faq);
            parser.next();
            parser.nextTag();
            return readFaq(parser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static FAQ[] readFaq(XmlPullParser parser) throws XmlPullParserException, IOException {
        items = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "faq");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if ("question".equals(name)) {
                items.add(readItem(parser));
            } else {
                skip(parser);
            }
        }

        return (FAQ[]) items.toArray(new FAQ[items.size()]);
    }

    private static FAQ readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        FAQ faq = new FAQ();
        parser.require(XmlPullParser.START_TAG, ns, "question");
        faq.question = Html.fromHtml(readFaqQuestion(parser));

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if ("text".equals(name)) {
                faq.text = Html.fromHtml(readFaqText(parser));
            } else {
                skip(parser);
            }
        }

        return faq;
    }

    private static String readFaqQuestion(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "question");
        String faqName = parser.getAttributeValue(null, "name");
        String description = parser.getAttributeValue(null, "description");
        String question = (items.size() + 1) + ".) <u><b>" + faqName + "</u></b>";
        if (description != null) {
            question += "<br/>(" + description + ")";
        }
        return question;
    }

    private static String readFaqText(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "text");
        String text = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "text");
        return text.replaceAll("\n", "<br/>");
    }

    private static String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static void showFaqDialog(final Context context) {
        final ListView list = new ListView(context);
        list.setDividerHeight(0);

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height - 200);
        list.setLayoutParams(params);


        new AsyncTask<Spanned[], Void, FAQ[]>() {
            @Override
            public XmlFaqUtils.FAQ[] doInBackground(Spanned[]... params) {
                return XmlFaqUtils.parse(context);
            }

            @Override
            public void onPostExecute(XmlFaqUtils.FAQ[] result) {
                list.setAdapter(new FaqAdapter(context, result));
            }
        }.execute();

        new AlertDialog.Builder(context)
                .setTitle("FAQ")
                .setView(list)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
