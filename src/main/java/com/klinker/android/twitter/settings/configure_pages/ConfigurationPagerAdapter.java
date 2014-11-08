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
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.TimelinePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private List<Fragment> frags = new ArrayList<Fragment>();

    public ConfigurationPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        this.context = context;

        for (int i = 0; i <= TimelinePagerAdapter.MAX_EXTRA_PAGES; i++) {
            ChooserFragment f = new ChooserFragment();
            Bundle b = new Bundle();
            b.putInt("position", i);
            f.setArguments(b);
            frags.add(f);
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
        return context.getResources().getString(R.string.page).replace("%s", (position + 1) + "");
    }
}
