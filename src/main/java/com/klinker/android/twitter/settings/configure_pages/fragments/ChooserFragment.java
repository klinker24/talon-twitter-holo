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

package com.klinker.android.twitter.settings.configure_pages.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.settings.configure_pages.ListChooser;
import com.klinker.android.twitter.manipulations.widgets.HoloTextView;


public abstract class ChooserFragment extends Fragment {

    protected Context context;
    protected ActionBar actionBar;
    protected HoloTextView current;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        actionBar = activity.getActionBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View layout = inflater.inflate(R.layout.configuration_page, null);

        current = (HoloTextView) layout.findViewById(R.id.current);
        current.setText(getResources().getString(R.string.current) + ": \n" + getResources().getString(R.string.dont_use));

        Button dontUse = (Button) layout.findViewById(R.id.dont_use);
        dontUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current.setText(getResources().getString(R.string.current) + ": \n" + getResources().getString(R.string.dont_use));
                setType(AppSettings.PAGE_TYPE_NONE);
            }
        });

        Button pics = (Button) layout.findViewById(R.id.use_pics);
        pics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current.setText(getResources().getString(R.string.current) + ": \n" + getResources().getString(R.string.picture_page));
                setType(AppSettings.PAGE_TYPE_PICS);
            }
        });

        Button links = (Button) layout.findViewById(R.id.use_links);
        links.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current.setText(getResources().getString(R.string.current) + ": \n" + getResources().getString(R.string.link_page));
                setType(AppSettings.PAGE_TYPE_LINKS);
            }
        });

        Button favUsers = (Button) layout.findViewById(R.id.use_fav_users);
        favUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current.setText(getResources().getString(R.string.current) + ": \n" + getResources().getString(R.string.favorite_users));
                setType(AppSettings.PAGE_TYPE_FAV_USERS);
            }
        });

        Button list = (Button) layout.findViewById(R.id.use_list);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooser = new Intent(context, ListChooser.class);
                startActivityForResult(chooser, REQUEST_LIST);
            }
        });


        return layout;
    }

    public static int REQUEST_LIST = 1;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_LIST) {

            if(resultCode == Activity.RESULT_OK) {
                setId(data.getLongExtra("listId", 0));
                String listName = data.getStringExtra("listName");
                setListName(listName);
                current.setText(getResources().getString(R.string.current) + ": \n" + listName);
                setType(AppSettings.PAGE_TYPE_LIST);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                current.setText(getResources().getString(R.string.current) + ": \n" + getResources().getString(R.string.dont_use));
                setType(AppSettings.PAGE_TYPE_NONE);
                setListName("");
                setId(0);
            }
        }
    }

    protected abstract void setType(int type);
    protected abstract void setId(long id);
    protected abstract void setListName(String name);
}
