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

package com.klinker.android.twitter.activity;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WearableListView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapter.SettingsAdapter;
import com.klinker.android.twitter.util.RecyclerOnClickListener;

import java.util.Arrays;
import java.util.List;

public class TextSizeActivity extends Activity {

    public static final int DEFAULT_TEXT_SIZE = 15;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> options = Arrays.asList(getResources().getStringArray(R.array.text_size_values));
        WearableListView listView = new WearableListView(this);
        SettingsAdapter adapter = new SettingsAdapter(options, new RecyclerOnClickListener() {
            @Override
            public void onClick(SettingsAdapter.SettingsViewHolder holder) {
                PreferenceManager.getDefaultSharedPreferences(TextSizeActivity.this)
                        .edit()
                        .putString(getString(R.string.pref_text_size_key), holder.text.getText().toString())
                        .commit();
                finish();
            }
        });

        int textSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_text_size_key), DEFAULT_TEXT_SIZE + ""));
        int position = textSize - Integer.parseInt(options.get(0));

        listView.setAdapter(adapter);
        listView.scrollToPosition(position);

        setContentView(listView);
    }

}
