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

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.settings.configure_pages.ListChooser;
import com.klinker.android.twitter.manipulations.widgets.HoloTextView;

import java.util.ArrayList;
import java.util.List;


public class ChooserFragment extends Fragment {

    private static final String DEFAULT_CLICKED = "com.klinker.android.twitter.CLICKED_CHECK";

    protected Context context;
    protected ActionBar actionBar;
    public CheckBox check;

    private boolean thisFragmentClicked = false;
    BroadcastReceiver defaultClicked = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!thisFragmentClicked) {
                check.setChecked(false);
            } else {
                thisFragmentClicked = false;
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        actionBar = activity.getActionBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        List<String> list = new ArrayList<String>();
        list.add(context.getString(R.string.do_not_use));
        list.add(context.getString(R.string.timeline));
        list.add(context.getString(R.string.mentions));
        list.add(context.getString(R.string.direct_messages));
        list.add(context.getString(R.string.list_page));
        list.add(context.getString(R.string.favorite_users));
        list.add(context.getString(R.string.link_page));
        list.add(context.getString(R.string.picture_page));
        list.add(context.getString(R.string.second_acc_mentions));

        View layout = inflater.inflate(R.layout.configuration_page, null);

        Spinner spinner = (Spinner) layout.findViewById(R.id.selection_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        setType(AppSettings.PAGE_TYPE_NONE);
                        break;
                    case 1:
                        setType(AppSettings.PAGE_TYPE_HOME);
                        break;
                    case 2:
                        setType(AppSettings.PAGE_TYPE_MENTIONS);
                        break;
                    case 3:
                        setType(AppSettings.PAGE_TYPE_DMS);
                        break;
                    case 4:
                        Intent chooser = new Intent(context, ListChooser.class);
                        startActivityForResult(chooser, REQUEST_LIST);
                        break;
                    case 5:
                        setType(AppSettings.PAGE_TYPE_FAV_USERS);
                        break;
                    case 6:
                        setType(AppSettings.PAGE_TYPE_LINKS);
                        break;
                    case 7:
                        setType(AppSettings.PAGE_TYPE_PICS);
                        break;
                    case 8:
                        setType(AppSettings.PAGE_TYPE_SECOND_MENTIONS);
                        break;
                    default:
                        setType(AppSettings.PAGE_TYPE_NONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);


        check = (CheckBox) layout.findViewById(R.id.default_page);
        final LinearLayout checkLayout = (LinearLayout) layout.findViewById(R.id.default_page_layout);
        checkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check.isChecked()) {
                    check.setChecked(true);
                    thisFragmentClicked = true;
                    context.sendBroadcast(new Intent(DEFAULT_CLICKED));
                }
            }
        });

        if (getArguments().getInt("position", 0) == 0) {
            check.setChecked(true);
        }

        return layout;
    }

    public static int REQUEST_LIST = 1;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_LIST) {

            if(resultCode == Activity.RESULT_OK) {
                setId(data.getLongExtra("listId", 0));
                String listName = data.getStringExtra("listName");
                setListName(listName);
                setType(AppSettings.PAGE_TYPE_LIST);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                setType(AppSettings.PAGE_TYPE_NONE);
                setListName("");
                setId(0);
            }
        }
    }

    public int type = AppSettings.PAGE_TYPE_NONE;
    public long listId = 0;
    public String listName = "";

    protected void setType(int type) {
        this.type = type;
    }
    protected void setId(long id) {
        this.listId = id;
    }
    protected void setListName(String listName) {
        this.listName = listName;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(DEFAULT_CLICKED);
        context.registerReceiver(defaultClicked, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        context.unregisterReceiver(defaultClicked);
    }
}
