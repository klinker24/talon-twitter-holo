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

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.ui.profile_viewer.fragments.ProfileFragment;
import com.klinker.android.twitter.ui.profile_viewer.fragments.sub_fragments.ProfileFavoritesFragment;
import com.klinker.android.twitter.ui.profile_viewer.fragments.sub_fragments.ProfileMentionsFragment;
import com.klinker.android.twitter.ui.profile_viewer.fragments.sub_fragments.ProfilePicturesFragment;

public class ProfilePagerAdapter extends FragmentPagerAdapter {
    private Context context;
    private String name;
    private String screenName;
    private String proPic;
    private long tweetId;
    private boolean isRetweet;
    private boolean isMyProfile;

    public ProfilePagerAdapter(FragmentManager fm, Context context, String name, String screenName, String proPic, long tweetId, boolean isRetweet, boolean isMyProfile) {
        super(fm);
        this.context = context;
        this.name = name;
        this.screenName = screenName;
        this.proPic = proPic;
        this.tweetId = tweetId;
        this.isRetweet = isRetweet;
        this.isMyProfile = isMyProfile;
    }
    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                ProfilePicturesFragment pics = new ProfilePicturesFragment();
                pics.setArguments(getSceeennameBundle());
                return pics;
            case 1:
                ProfileFragment profile = new ProfileFragment();
                profile.setArguments(getSceeennameBundle());
                return profile;
            case 2:
                ProfileMentionsFragment mentions = new ProfileMentionsFragment();
                mentions.setArguments(getSceeennameBundle());
                return mentions;
            case 3:
                ProfileFavoritesFragment favs = new ProfileFavoritesFragment();
                favs.setArguments(getSceeennameBundle());
                return favs;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.pictures);
            case 1:
                return context.getResources().getString(R.string.profile);
            case 2:
                return context.getResources().getString(R.string.mentions);
            case 3:
                return context.getResources().getString(R.string.favorites);
        }
        return null;
    }

    public Bundle getSceeennameBundle() {
        Bundle b = new Bundle();
        b.putString("screen_name", screenName);
        return b;
    }
}
