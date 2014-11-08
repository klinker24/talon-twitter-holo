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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.manipulations.widgets.NotificationDrawerLayout;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.manipulations.widgets.HoloTextView;
import com.klinker.android.twitter.ui.main_fragments.home_fragments.HomeFragment;
import com.klinker.android.twitter.ui.main_fragments.other_fragments.DMFragment;
import com.klinker.android.twitter.ui.main_fragments.other_fragments.MentionsFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainDrawerArrayAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> text = new ArrayList<String>();
    public SharedPreferences sharedPrefs;
    public static int current = 0;
    public int textSize;

    public List<Long> listIds = new ArrayList<Long>(); // 0 is the furthest to the left
    public List<Integer> pageTypes = new ArrayList<Integer>();
    public List<String> pageNames = new ArrayList<String>();

    public Set<String> shownItems;

    static class ViewHolder {
        public HoloTextView name;
        public ImageView icon;
    }

    public static String[] getItems(Context context1) {
        String[] items = new String[] {
                context1.getResources().getString(R.string.discover),
                context1.getResources().getString(R.string.lists),
                context1.getResources().getString(R.string.favorite_users),
                context1.getResources().getString(R.string.retweets),
                context1.getResources().getString(R.string.favorite_tweets),
                context1.getResources().getString(R.string.saved_searches) };

        return items;
    }

    public MainDrawerArrayAdapter(Context context) {
        super(context, 0);
        this.context = (Activity) context;
        this.sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        textSize = 15;


        int currentAccount = sharedPrefs.getInt("current_account", 1);

        for (int i = 0; i < TimelinePagerAdapter.MAX_EXTRA_PAGES; i++) {
            String listIdentifier = "account_" + currentAccount + "_list_" + (i + 1) + "_long";
            String pageIdentifier = "account_" + currentAccount + "_page_" + (i + 1);
            String nameIdentifier = "account_" + currentAccount + "_name_" + (i + 1);

            int type = sharedPrefs.getInt(pageIdentifier, AppSettings.PAGE_TYPE_NONE);

            if (type != AppSettings.PAGE_TYPE_NONE) {
                pageTypes.add(type);
                listIds.add(sharedPrefs.getLong(listIdentifier, 0l));
                pageNames.add(sharedPrefs.getString(nameIdentifier, ""));
            }
        }

        for (int i = 0; i < pageTypes.size(); i++) {
            switch (pageTypes.get(i)) {
                case AppSettings.PAGE_TYPE_HOME:
                    text.add(context.getResources().getString(R.string.timeline));
                    break;
                case AppSettings.PAGE_TYPE_MENTIONS:
                    text.add(context.getResources().getString(R.string.mentions));
                    break;
                case AppSettings.PAGE_TYPE_DMS:
                    text.add(context.getResources().getString(R.string.direct_messages));
                    break;
                default:
                    text.add(getName(pageNames.get(i), pageTypes.get(i)));
                    break;
            }
        }

        for (String s : getItems(context)) {
            text.add(s);
        }

        shownItems = sharedPrefs.getStringSet("drawer_elements_shown_" + currentAccount, new HashSet<String>());

        for (int i = text.size() - 1; i >= 0; i--) {
            if (!shownItems.contains(i + "")) {
                text.remove(i);
            }
        }

    }

    @Override
    public int getCount() {
        return text.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        String settingName = text.get(position);

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.drawer_list_item, null);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.name = (HoloTextView) rowView.findViewById(R.id.title);
            viewHolder.icon = (ImageView) rowView.findViewById(R.id.icon);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.name.setText(settingName);
        holder.name.setTextSize(18);

        try {
            if (text.get(position).equals(context.getResources().getString(R.string.timeline))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.timelineItem});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.mentions))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.mentionItem});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.direct_messages))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.directMessageItem});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.retweets))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.retweetButton});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.favorite_tweets))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.favoritedButton});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.favorite_users))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.favUser});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.discover))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.links});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.search))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.searchIcon});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.lists))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.listIcon});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.saved_searches))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.searchIcon});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.links))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.links});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else if (text.get(position).equals(context.getResources().getString(R.string.pictures))) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.picturePlaceholder});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            } else {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.listIcon});
                int resource = a.getResourceId(0, 0);
                a.recycle();
                holder.icon.setImageResource(resource);
            }
        } catch (OutOfMemoryError e) {

        }

        if (highlightedCurrent == position) {
            if (!DrawerActivity.settings.addonTheme) {
                holder.icon.setColorFilter(context.getResources().getColor(R.color.app_color));
                holder.name.setTextColor(context.getResources().getColor(R.color.app_color));
            } else {
                holder.icon.setColorFilter(DrawerActivity.settings.accentInt);
                holder.name.setTextColor(DrawerActivity.settings.accentInt);
            }
        } else {
            TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.textColor});
            int resource = a.getResourceId(0, 0);

            holder.icon.setColorFilter(context.getResources().getColor(resource));
            holder.name.setTextColor(context.getResources().getColor(resource));
        }

        return rowView;
    }

    public static int highlightedCurrent;
    public static void setCurrent(Context context, int i) {
        current = i;
        highlightedCurrent = i;

        SharedPreferences sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        int currentAccount = sharedPrefs.getInt("current_account", 1);

        Set<String> shownItems = sharedPrefs.getStringSet("drawer_elements_shown_" + currentAccount, new HashSet<String>());
        for (int index = 0; index <= highlightedCurrent; index++) {
            if (!shownItems.contains(index + "")) {
                highlightedCurrent--;
            }
        }
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