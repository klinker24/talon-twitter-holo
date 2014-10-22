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

package com.klinker.android.twitter.adapter;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.util.RecyclerOnClickListener;

import java.util.List;

public class SettingsAdapter extends WearableListView.Adapter {

    private List<String> items;
    private RecyclerOnClickListener onClickListener;

    public SettingsAdapter(List<String> items, RecyclerOnClickListener onClickListener) {
        this.items = items;
        this.onClickListener = onClickListener;
    }

    @Override
    public SettingsViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.settings_list_item, viewGroup, false);
        final SettingsViewHolder holder = new SettingsViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(holder);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
        ((SettingsViewHolder) viewHolder).text.setText(items.get(position));
        ((SettingsViewHolder) viewHolder).position = position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class SettingsViewHolder extends WearableListView.ViewHolder {

        public TextView text;
        public int position;

        public SettingsViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
        }

    }

}
