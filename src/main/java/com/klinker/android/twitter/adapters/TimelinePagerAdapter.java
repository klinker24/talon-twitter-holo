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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.main_fragments.other_fragments.*;
import com.klinker.android.twitter.ui.main_fragments.home_fragments.extentions.FavUsersFragment;
import com.klinker.android.twitter.ui.main_fragments.home_fragments.HomeFragment;
import com.klinker.android.twitter.ui.main_fragments.home_fragments.extentions.LinksFragment;
import com.klinker.android.twitter.ui.main_fragments.home_fragments.extentions.PicFragment;
import com.klinker.android.twitter.ui.main_fragments.other_fragments.trends.LocalTrendsFragment;
import com.klinker.android.twitter.ui.main_fragments.other_fragments.trends.WorldTrendsFragment;

import java.util.ArrayList;
import java.util.List;

public class TimelinePagerAdapter extends FragmentPagerAdapter {

    public static final int MAX_EXTRA_PAGES = 8;

    private Context context;
    private SharedPreferences sharedPrefs;

    public List<Long> listIds = new ArrayList<Long>(); // 0 is the furthest to the left
    public List<Integer> pageTypes = new ArrayList<Integer>();
    public List<String> pageNames = new ArrayList<String>();
    public List<String> searches = new ArrayList<String>();

    public List<Fragment> frags = new ArrayList<Fragment>();
    public List<String> names = new ArrayList<String>();

    public int mentionIndex = -1;

    // remove the home fragment to swipe to, since it is on the launcher
    public TimelinePagerAdapter(FragmentManager fm, Context context, SharedPreferences sharedPreferences, boolean removeHome) {
        super(fm);
        this.context = context;
        this.sharedPrefs = sharedPreferences;

        int currentAccount = sharedPreferences.getInt("current_account", 1);

        if (sharedPrefs.getBoolean("convert_long_lists", true)) {
            sharedPreferences.edit().putBoolean("convert_long_lists", false).commit();
            sharedPrefs.edit().putLong("account_1_list_1_long", sharedPrefs.getInt("account_1_list_1", 0)).commit();
            sharedPrefs.edit().putLong("account_1_list_2_long", sharedPrefs.getInt("account_1_list_2", 0)).commit();
            sharedPrefs.edit().putLong("account_1_list_1_long", sharedPrefs.getInt("account_2_list_1", 0)).commit();
            sharedPrefs.edit().putLong("account_1_list_2_long", sharedPrefs.getInt("account_2_list_2", 0)).commit();
        }

        for (int i = 0; i < MAX_EXTRA_PAGES; i++) {
            String listIdentifier = "account_" + currentAccount + "_list_" + (i + 1) + "_long";
            String pageIdentifier = "account_" + currentAccount + "_page_" + (i + 1);
            String nameIdentifier = "account_" + currentAccount + "_name_" + (i + 1);
            String searchIdentifier = "account_" + currentAccount + "_search_" + (i + 1);

            int type = sharedPrefs.getInt(pageIdentifier, AppSettings.PAGE_TYPE_NONE);

            if (type != AppSettings.PAGE_TYPE_NONE &&
                    !(removeHome && type == AppSettings.PAGE_TYPE_HOME)) {
                pageTypes.add(type);
                listIds.add(sharedPrefs.getLong(listIdentifier, 0l));
                pageNames.add(sharedPrefs.getString(nameIdentifier, ""));
                searches.add(sharedPrefs.getString(searchIdentifier, ""));
            }
        }

        for (int i = 0; i < pageTypes.size(); i++) {
            switch (pageTypes.get(i)) {
                case AppSettings.PAGE_TYPE_HOME:
                    frags.add(new HomeFragment());
                    names.add(context.getResources().getString(R.string.timeline));
                    break;
                case AppSettings.PAGE_TYPE_MENTIONS:
                    frags.add(new MentionsFragment());
                    names.add(context.getResources().getString(R.string.mentions));
                    mentionIndex = i;
                    break;
                case AppSettings.PAGE_TYPE_SECOND_MENTIONS:
                    frags.add(new SecondAccMentionsFragment());
                    names.add("@" + AppSettings.getInstance(context).secondScreenName);
                    mentionIndex = i;
                    break;
                case AppSettings.PAGE_TYPE_DMS:
                    frags.add(new DMFragment());
                    names.add(context.getResources().getString(R.string.direct_messages));
                    break;
                case AppSettings.PAGE_TYPE_WORLD_TRENDS:
                    frags.add(new WorldTrendsFragment());
                    names.add(context.getString(R.string.world_trends));
                    break;
                case AppSettings.PAGE_TYPE_LOCAL_TRENDS:
                    frags.add(new LocalTrendsFragment());
                    names.add(context.getString(R.string.local_trends));
                    break;
                case AppSettings.PAGE_TYPE_SAVED_SEARCH:
                    Fragment f = new SavedSearchFragment();
                    Bundle b = new Bundle();
                    b.putString("saved_search", searches.get(i));
                    f.setArguments(b);
                    frags.add(f);
                    names.add(searches.get(i));
                    break;
                case AppSettings.PAGE_TYPE_FAVORITE_STATUS:
                    frags.add(new FavoriteTweetsFragment());
                    names.add(context.getString(R.string.favorite_tweets));
                    break;
                case AppSettings.PAGE_TYPE_ACTIVITY:
                    frags.add(new ActivityFragment());
                    names.add(context.getString(R.string.activity));
                    mentionIndex = i;
                    break;
                default:
                    frags.add(getFrag(pageTypes.get(i), listIds.get(i)));
                    names.add(getName(pageNames.get(i), pageTypes.get(i)));
                    break;
            }
        }
    }

    @Override
    public Fragment getItem(int i) {
        return frags.get(i);
    }

    @Override
    public CharSequence getPageTitle(int i) {
        if (names.size() > i) {
            return names.get(i);
        } else {
            return "";
        }
    }

    @Override
    public int getCount() {
        return frags.size();
    }

    public Fragment getFrag(int type, long listId) {
        switch (type) {
            case AppSettings.PAGE_TYPE_LIST:
                Fragment f = new ListFragment();

                Bundle b = new Bundle();
                b.putLong("list_id", listId);

                f.setArguments(b);

                return f;
            case AppSettings.PAGE_TYPE_LINKS:
                return new LinksFragment();
            case AppSettings.PAGE_TYPE_PICS:
                return new PicFragment();
            case AppSettings.PAGE_TYPE_FAV_USERS:
                return new FavUsersFragment();
        }

        return null;
    }

    public String getName(String listName, int type) {
        switch (type) {
            case AppSettings.PAGE_TYPE_LIST:
                return listName;
            case AppSettings.PAGE_TYPE_LINKS:
                return context.getResources().getString(R.string.links);
            case AppSettings.PAGE_TYPE_PICS:
                return context.getResources().getString(R.string.pictures);
            case AppSettings.PAGE_TYPE_FAV_USERS:
                return context.getString(R.string.favorite_users);
        }

        return null;
    }
}
