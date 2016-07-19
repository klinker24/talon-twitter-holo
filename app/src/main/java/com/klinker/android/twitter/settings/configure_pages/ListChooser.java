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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.setup.LoginActivity;
import com.klinker.android.twitter.utils.Utils;

import org.lucasr.smoothie.AsyncListView;

import java.util.Collections;
import java.util.Comparator;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.UserList;


public class ListChooser extends Activity {

    private Context context;
    private SharedPreferences sharedPrefs;
    private AppSettings settings;
    private ActionBar actionBar;

    private AsyncListView listView;
    private ListChooserArrayAdapter arrayAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        settings = AppSettings.getInstance(this);

        Utils.setUpTheme(context, settings);
        setContentView(R.layout.list_chooser);

        actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.lists));


        if (!settings.isTwitterLoggedIn) {
            Intent login = new Intent(context, LoginActivity.class);
            startActivity(login);
            finish();
        }

        listView = (AsyncListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserList list = arrayAdapter.getItem(i);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("listId", list.getId());
                returnIntent.putExtra("listName", list.getName());
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });

        new GetLists().execute();
    }

    class GetLists extends AsyncTask<String, Void, ResponseList<UserList>> {

        protected ResponseList<UserList> doInBackground(String... urls) {
            try {
                Twitter twitter =  Utils.getTwitter(context, settings);

                ResponseList<UserList> lists = twitter.getUserLists(settings.myScreenName);

                Collections.sort(lists, new Comparator<UserList>() {
                    public int compare(UserList result1, UserList result2) {
                        return result1.getName().compareTo(result2.getName());
                    }
                });

                return lists;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(ResponseList<UserList> lists) {

            if (lists != null) {
                arrayAdapter = new ListChooserArrayAdapter(context, lists);
                listView.setAdapter(arrayAdapter);
                listView.setVisibility(View.VISIBLE);
            }

            LinearLayout spinner = (LinearLayout) findViewById(R.id.list_progress);
            spinner.setVisibility(View.GONE);
        }
    }

}
