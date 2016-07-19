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

package com.klinker.android.twitter.ui.launcher_page;

import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.ui.MainActivityPopup;

public class LauncherPopup extends MainActivityPopup {

    @Override
    public void setDim() {
        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = .75f;  // set it higher if you want to dim behind the window

        getWindow().setAttributes(params);
    }

    @Override
    public void setLauncherPage() {
        mViewPager.setCurrentItem(getIntent().getIntExtra("launcher_page", 0));

        LinearLayout drawer = (LinearLayout) findViewById(R.id.left_drawer);
        drawer.setVisibility(View.GONE);
    }

    @Override
    public Intent getRestartIntent() {
        Intent restart = new Intent(context, LauncherPopup.class);
        restart.putExtra("launcher_page", getIntent().getIntExtra("launcher_page", 0));
        restart.putExtra("from_launcher", true);
        return restart;
    }
}
