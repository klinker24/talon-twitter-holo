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

package com.klinker.android.twitter.settings.configure_pages;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.PicturesGridAdapter;
import com.klinker.android.twitter.adapters.TimelinePagerAdapter;
import com.klinker.android.twitter.settings.configure_pages.fragments.ChooserFragment;
import com.klinker.android.twitter.settings.configure_pages.fragments.ExampleHomeFragment;
import com.klinker.android.twitter.settings.configure_pages.fragments.PageOneFragment;
import com.klinker.android.twitter.settings.configure_pages.fragments.PageTwoFragment;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private List<Fragment> frags = new ArrayList<Fragment>();

    public ConfigurationPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        this.context = context;

        for (int i = 0; i <= TimelinePagerAdapter.MAX_EXTRA_PAGES; i++) {
            if (i == TimelinePagerAdapter.MAX_EXTRA_PAGES) {
                frags.add(new ExampleHomeFragment());
            } else {
                frags.add(new ChooserFragment());
            }
        }
    }

    @Override
    public Fragment getItem(int i) {
        return frags.get(i);
    }

    @Override
    public int getCount() {
        return TimelinePagerAdapter.MAX_EXTRA_PAGES + 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == TimelinePagerAdapter.MAX_EXTRA_PAGES) {
            return context.getResources().getString(R.string.timeline);
        } else {
            return context.getResources().getString(R.string.page).replace("%s", (position + 1) + "");
        }
    }
}
