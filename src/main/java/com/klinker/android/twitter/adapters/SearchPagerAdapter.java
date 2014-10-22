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

package com.klinker.android.twitter.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.ui.search.TimelineSearchFragment;
import com.klinker.android.twitter.ui.search.TwitterSearchFragment;
import com.klinker.android.twitter.ui.search.UserSearchFragment;


public class SearchPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private boolean onlyId;
    private boolean onlyProfile;
    private boolean translucent;
    private String query;

    public SearchPagerAdapter(FragmentManager fm, Context context, boolean onlyId, boolean onlyProfile, String query, boolean translucent) {
        super(fm);
        this.context = context;
        this.onlyId = onlyId;
        this.translucent = translucent;
        this.query = query;
        this.onlyProfile = onlyProfile;

        Log.v("talon_searching", "query: " + query);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment f = null;
        switch (i) {
            case 0:
                f = new TimelineSearchFragment();
                f.setArguments(getTimelineBundle());
                break;
            case 1:
                f = new TwitterSearchFragment();
                f.setArguments(getTwitterBundle());
                break;
            case 2:
                f = new UserSearchFragment();
                f.setArguments(getUserBundle());
                break;
        }
        return f;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.timeline);
            case 1:
                return context.getResources().getString(R.string.twitter);
            case 2:
                return context.getResources().getString(R.string.user);
        }
        return null;
    }

    public Bundle getTwitterBundle() {
        Bundle b = new Bundle();
        b.putBoolean("translucent", translucent);
        b.putBoolean("only_status", onlyId);
        b.putString("search", query);
        return b;
    }

    public Bundle getUserBundle() {
        Bundle b = new Bundle();
        b.putBoolean("translucent", translucent);
        b.putBoolean("only_profile", onlyProfile);
        b.putString("search", query);
        return b;
    }

    public Bundle getTimelineBundle() {
        Bundle b = new Bundle();
        b.putBoolean("translucent", translucent);
        b.putString("search", query);
        return b;
    }
}
