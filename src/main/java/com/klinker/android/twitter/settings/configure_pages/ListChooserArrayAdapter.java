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

import android.content.Context;
import android.view.View;

import com.klinker.android.twitter.adapters.ListsArrayAdapter;

import twitter4j.ResponseList;
import twitter4j.UserList;


public class ListChooserArrayAdapter extends ListsArrayAdapter {

    private Context context;

    public ListChooserArrayAdapter(Context context, ResponseList<UserList> lists) {
        super(context, lists);
        this.context = context;
    }

    @Override
    public void bindView(final View view, Context mContext, final UserList list) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        final String name = list.getName();
        final String id = list.getId() + "";

        holder.text.setText(name);
    }
}
