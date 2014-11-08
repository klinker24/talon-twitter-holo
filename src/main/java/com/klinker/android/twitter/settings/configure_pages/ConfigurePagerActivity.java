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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.TimelinePagerAdapter;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.setup.LoginActivity;
import com.klinker.android.twitter.utils.Utils;


public class ConfigurePagerActivity extends Activity {

    private ConfigurationPagerAdapter chooserAdapter;
    private Context context;
    private SharedPreferences sharedPrefs;
    private AppSettings settings;
    private ActionBar actionBar;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        settings = AppSettings.getInstance(this);

        try {
            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        } catch (Throwable e) {
            // don't have a clue why
        }

        Utils.setUpTheme(context, settings);
        setContentView(R.layout.configuration_activity);

        setUpDoneDiscard();

        actionBar = getActionBar();

        if (!settings.isTwitterLoggedIn) {
            Intent login = new Intent(context, LoginActivity.class);
            startActivity(login);
            finish();
        }

        chooserAdapter = new ConfigurationPagerAdapter(getFragmentManager(), context);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(chooserAdapter);
        mViewPager.setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);

        mViewPager.setOffscreenPageLimit(6);

        if (sharedPrefs.getBoolean("show_performance_tip", true)) {
            new AlertDialog.Builder(context)
                    .setTitle("Timeline Tip")
                    .setMessage("With this version of Talon, you can completely customize your swipable timelines." +
                            "\n\n" +
                            "You can place up to 6 swipeable pages on the main screen of Talon, including lists, mentions, direct messages, your 'home' timeline, and some filtered timelines.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Don't Show Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPrefs.edit().putBoolean("show_performance_tip", false).commit();
                        }
                    })
                    .create().show();
        }
    }

    public void setUpDoneDiscard() {
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_done_discard, null);
        TextView doneButton = (TextView) customActionBarView.findViewById(R.id.done);
        doneButton.setText(getResources().getString(R.string.done_label));
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentAccount = sharedPrefs.getInt("current_account", 1);

                        SharedPreferences.Editor editor = sharedPrefs.edit();

                        for (int i = 0; i < chooserAdapter.getCount(); i++) {
                            if (chooserAdapter.getItem(i) instanceof ChooserFragment) {
                                ChooserFragment f = (ChooserFragment) chooserAdapter.getItem(i);

                                int num = i + 1;
                                editor.putInt("account_" + currentAccount + "_page_" + num, f.type);
                                editor.putLong("account_" + currentAccount + "_list_" + num + "_long", f.listId);
                                editor.putString("account_" + currentAccount + "_name_" + num, f.listName);

                                if (f.check.isChecked()) {
                                    editor.putInt("default_timeline_page_" + currentAccount, i);
                                }
                            }
                        }

                        editor.commit();

                        onBackPressed();
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.configuration_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_select_number_of_pages:
                final NumberPicker picker = new NumberPicker(context);
                FrameLayout.LayoutParams params =
                        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                picker.setLayoutParams(params);
                picker.setMaxValue(TimelinePagerAdapter.MAX_EXTRA_PAGES);
                picker.setMinValue(0);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.menu_number_of_pages);
                builder.setView(picker);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPrefs.edit().putInt("number_of_extra_pages", picker.getValue()).commit();
                        dialog.dismiss();
                        recreate();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();

                return true;

            default:
                return true;
        }
    }

}
