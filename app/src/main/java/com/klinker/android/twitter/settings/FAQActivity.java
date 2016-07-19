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

package com.klinker.android.twitter.settings;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.FAQArrayAdapter;
import com.klinker.android.twitter.utils.Utils;

import java.util.ArrayList;

public class FAQActivity extends Activity {

    public ArrayList<String[]> links = new ArrayList<String[]>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpLinks();

        AppSettings settings = AppSettings.getInstance(this);

        Utils.setUpPopupTheme(this, settings);
        setUpWindow();

        setContentView(R.layout.faq_activity);

        ListView list = (ListView) findViewById(R.id.listView);
        list.setAdapter(new FAQArrayAdapter(this, this.links));
    }

    public void setUpWindow() {

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        params.dimAmount = .75f;  // set it higher if you want to dim behind the window

        getWindow().setAttributes(params);

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

    }

    public void setUpLinks() {
        // TODO add a list of all here
        links.add(new String[] {
                "All FAQ (XDA Link)",
                "http://forum.xda-developers.com/showpost.php?p=49392415&postcount=2"
        });
        links.add(new String[]{
                "Push Notifications",
                "https://plus.google.com/117432358268488452276/posts/31oSKEmMFnq"
        });
        links.add(new String[]{
                "Translucency",
                "https://plus.google.com/117432358268488452276/posts/Kc2sB8uBYwa"
        });
        links.add(new String[]{
                "Theming Limits",
                "https://plus.google.com/117432358268488452276/posts/dHDRSc4J3yV"
        });
        links.add(new String[]{
                "More Info on Status's",
                "https://plus.google.com/117432358268488452276/posts/hY7Aa3eSVvC"
        });
        links.add(new String[]{
                "Clearing Cache",
                "https://plus.google.com/117432358268488452276/posts/ZgAHJxKycfv"
        });
        links.add(new String[]{
                "Immersive Mode Support",
                "https://plus.google.com/117432358268488452276/posts/ec8UwdGUEEH"
        });
        links.add(new String[]{
                "Battery Consumption",
                "https://plus.google.com/117432358268488452276/posts/e2h3DTY5h7Q"
        });

    }
}
