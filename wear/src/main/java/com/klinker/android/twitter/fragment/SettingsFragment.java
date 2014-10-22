/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.twitter.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.activity.TextSizeActivity;
import com.klinker.android.twitter.adapter.SettingsAdapter;
import com.klinker.android.twitter.util.RecyclerOnClickListener;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private static final int TEXT_SIZE = 0;

    private static final int[] SETTINGS_ITEMS = new int[] {
            R.string.pref_text_size
    };

    private List<String> settingsItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsItems = new ArrayList<String>();
        for (int item : SETTINGS_ITEMS) {
            settingsItems.add(getString(item));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        WearableListView listView = new WearableListView(getActivity());
        SettingsAdapter adapter = new SettingsAdapter(settingsItems, new RecyclerOnClickListener() {
            @Override
            public void onClick(SettingsAdapter.SettingsViewHolder holder) {
                switch (holder.position) {
                    case TEXT_SIZE: // text size:
                        startActivity(new Intent(getActivity(), TextSizeActivity.class));
                        break;
                }
            }
        });

        listView.setAdapter(adapter);
        return listView;
    }

}
