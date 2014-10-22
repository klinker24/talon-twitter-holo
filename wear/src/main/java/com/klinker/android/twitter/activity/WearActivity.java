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

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.widget.TextView;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapter.ArticleGridPagerAdapter;
import com.klinker.android.twitter.transaction.KeyProperties;
import com.klinker.android.twitter.view.CircularProgressBar;

public class WearActivity extends WearTransactionActivity {

    private static final String TAG = "WearActivity";

    private GridViewPager viewPager;
    private ArticleGridPagerAdapter adapter;
    private CircularProgressBar progressBar;
    private TextView emptyView;

    private int primaryColor;
    private int accentColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wear);
        viewPager = (GridViewPager) findViewById(R.id.article_pager);
        adapter = new ArticleGridPagerAdapter(this);
        viewPager.setAdapter(adapter);

        progressBar = (CircularProgressBar) findViewById(R.id.progress_bar);
        emptyView = (TextView) findViewById(R.id.empty_view);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        accentColor = sharedPreferences.getInt(KeyProperties.KEY_ACCENT_COLOR, getResources().getColor(R.color.orange_accent_color));
        primaryColor = sharedPreferences.getInt(KeyProperties.KEY_PRIMARY_COLOR, getResources().getColor(R.color.orange_primary_color));
        progressBar.setColor(accentColor);
        viewPager.setBackgroundColor(primaryColor);

        viewPager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, int i1, float v, float v1, int i2, int i3) {
            }

            @Override
            public void onPageSelected(int row, int col) {
                sendReadStatus(getIds().get(row));
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    @Override
    public void updateDisplay() {
        if (getTitles().size() > 0) {
            progressBar.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter = new ArticleGridPagerAdapter(this);
            viewPager.setAdapter(adapter);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewPager.setCurrentItem(adapter.getRowCount() - 1,0, adapter.getRowCount() > 20 ? false : true);
                }
            }, 300);
        } else {
            progressBar.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);

            Drawable biker = getResources().getDrawable(R.drawable.ic_biker);
            biker.setColorFilter(accentColor, PorterDuff.Mode.MULTIPLY);
            emptyView.setCompoundDrawablesWithIntrinsicBounds(null, biker, null, null);
        }
    }

}
