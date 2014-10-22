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

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v13.app.FragmentPagerAdapter;
import android.widget.ListView;

import com.klinker.android.twitter.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public static final int NUM_PAGES = 9;
    private Context context;
    private ListView mDrawerList;

    public SectionsPagerAdapter(FragmentManager fm, Context context, ListView drawerList) {
        super(fm);

        this.context = context;
        this.mDrawerList = drawerList;
    }

    @Override
    public Fragment getItem(int position) {
        PreferenceFragment fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.ui_settings);
            case 1:
                return context.getResources().getString(R.string.timelines_settings);
            case 2:
                return context.getResources().getString(R.string.sync_settings);
            case 3:
                return context.getResources().getString(R.string.notification_settings);
            case 4:
                return context.getResources().getString(R.string.browser_settings);
            case 5:
                return context.getResources().getString(R.string.advanced_settings);
            case 6:
                return context.getResources().getString(R.string.memory_manage);
            case 7:
                return context.getResources().getString(R.string.get_help_settings);
            case 8:
                return context.getResources().getString(R.string.other_apps);
        }
        return null;
    }
}