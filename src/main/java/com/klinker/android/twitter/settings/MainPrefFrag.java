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

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.klinker.android.twitter.R;

public class MainPrefFrag extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.main_settings);

        setClicks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        ListView list = (ListView) v.findViewById(android.R.id.list);
        list.setDivider(new ColorDrawable(getResources().getColor(android.R.color.transparent))); // or some other color int
        list.setDividerHeight(0);

        return v;
    }

    public void setClicks() {

        findPreference("ui_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSettings(0, preference.getTitle().toString());
                return false;
            }
        });

        findPreference("timeline_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSettings(1, preference.getTitle().toString());
                return false;
            }
        });

        findPreference("sync_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSettings(2, preference.getTitle().toString());
                return false;
            }
        });

        findPreference("notification_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSettings(3, preference.getTitle().toString());
                return false;
            }
        });

        findPreference("browser_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSettings(4, preference.getTitle().toString());
                return false;
            }
        });

        findPreference("advanced_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSettings(5, preference.getTitle().toString());
                return false;
            }
        });

        findPreference("main_pages_and_drawer").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSettings(6, preference.getTitle().toString());
                return false;
            }
        });


        findPreference("memory_management").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSettings(7, preference.getTitle().toString());
                return false;
            }
        });
    }

    private void showSettings(int position, String title) {
        startActivity(new Intent(getActivity(), PrefActivity.class)
                .putExtra("position", position)
                .putExtra("title", title));
    }
}
