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

package com.klinker.android.twitter.ui;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Window;

import com.klinker.android.twitter.utils.Utils;

public class MainActivityPopup extends MainActivity {

    public void setDim() {
        // don't dim here
    }

    @Override
    public void setUpWindow() {
        try {
            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        } catch (Exception e) {
            recreate();
        }

        setDim();

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .9), (int) (height * .8));
        } else {
            getWindow().setLayout((int) (width * .7), (int) (height * .8));
        }

        MainActivity.isPopup = true;
    }

    @Override
    public Intent getRestartIntent() {
        return new Intent(context, MainActivityPopup.class);
    }

    @Override
    public void setUpTheme() {

        translucent = false;

        Utils.setUpNotifTheme(context, settings);

        if (settings.addonTheme) {
            getWindow().setBackgroundDrawable(new ColorDrawable(settings.backgroundColor));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.isPopup = true;
    }

    @Override
    public void onPause() {
        sharedPrefs.edit().putBoolean("refresh_me", true).commit();
        super.onPause();
    }

    @Override
    public void onStop() {
        sharedPrefs.edit().putBoolean("remake_me", true).commit();

        super.onStop();
    }


}
