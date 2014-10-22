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

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.MenuItem;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.utils.Utils;


public class PrefActivity extends Activity {

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.setUpTheme(this, AppSettings.getInstance(this));

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

        final PrefFragment fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putInt("position", getIntent().getIntExtra("position", 0));
        fragment.setArguments(args);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();

        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle(getIntent().getStringExtra("title"));

        setIcon(ab, getIntent().getIntExtra("position", 0));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setIcon(ActionBar ab, int position) {
        if (position == 0) {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.themeSettings});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            ab.setIcon(resource);
        } else if (position == 1) {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.timelineItem});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            ab.setIcon(resource);
        } else if (position == 2) {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.syncSettings});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            ab.setIcon(resource);
        } else if (position == 3) {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.notificationSettings});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            ab.setIcon(resource);
        } else if (position == 5) {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.advancedSettings});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            ab.setIcon(resource);
        } else if (position == 7) {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.getHelp});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            ab.setIcon(resource);
        } else if (position == 8) {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.otherApps});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            ab.setIcon(resource);
        } else if (position == 6) {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.memManagement});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            ab.setIcon(resource);
        } else if (position == 4) {
            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.links});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            ab.setIcon(resource);
        }
    }
}
