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

package com.klinker.android.twitter.manipulations.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.EditText;

public class HoloEditText extends EditText {

    private static SharedPreferences sharedPreferences;

    public HoloEditText(Context context) {
        super(context);
        setUp(context);
    }

    public HoloEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public HoloEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp(context);
    }

    private void setUp(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        }

        setTypeface(context);
    }

    public static Typeface typeface;
    private static boolean useDeviceFont;

    private void setTypeface(Context context) {
        if (typeface == null) {
            String type = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                    Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE)
                    .getString("font_type", "1");

            useDeviceFont = type.equals("0");

            switch (Integer.parseInt(type)) {
                case 1:
                    typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
                    break;
                case 2:
                    typeface = Typeface.createFromAsset(context.getAssets(), "RobotoCondensed-Regular.ttf");
                    break;
                case 3:
                    typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
                    break;
                default:
                    typeface = null;
                    break;
            }
        }

        if (!useDeviceFont) {
            setTypeface(typeface);
        }
    }
}
