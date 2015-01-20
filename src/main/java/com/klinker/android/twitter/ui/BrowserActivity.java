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

package com.klinker.android.twitter.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.manipulations.widgets.HTML5WebView;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.Utils;

public class BrowserActivity extends Activity {

    public AppSettings settings;
    public String url;
    private HTML5WebView browser;

    public Context context;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_zoom_enter, R.anim.slide_out_right);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getWindow().requestFeature(Window.FEATURE_PROGRESS);
        } catch (Exception e) {
            // oops, something went wrong... don't quite know what though, or why
            startActivity(new Intent(this, BrowserActivity.class).putExtra("url", url));
            overridePendingTransition(0,0);
            finish();
            return;
        }

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        context = this;

        overridePendingTransition(R.anim.slide_in_left, R.anim.activity_zoom_exit);

        settings = AppSettings.getInstance(this);

        url = getIntent().getStringExtra("url");

        Utils.setUpTheme(this, settings);
        Utils.setActionBar(this);

        setUpLayout();

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void setUpLayout() {

        browser = new HTML5WebView(this);
        setContentView(browser.getLayout());

        browser.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        if (url.contains("youtu") || url.contains("play.google.com")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else {
            browser.loadUrl(url);
        }

        browser.setWebViewClient(new WebClient());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_open_web:
                try {
                    Uri weburi = Uri.parse(browser.getUrl());
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, weburi);
                    startActivity(launchBrowser);
                } catch (Exception e) {
                    e.printStackTrace();
                    // it is a picture link that they clicked from the timeline i think...
                }
                return true;

            default:
                return true;
        }
    }

    @Override
    public void onDestroy() {
        try {
            browser.destroy();
        } catch (Exception e) {
            // plain text browser
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (url.contains("vine")) {
                ((AudioManager)getSystemService(
                        Context.AUDIO_SERVICE)).requestAudioFocus(
                        new AudioManager.OnAudioFocusChangeListener() {
                            @Override
                            public void onAudioFocusChange(int focusChange) {}
                        }, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onBackPressed() {
        if (browser != null && browser.canGoBack() && !browser.getUrl().equals(url)) {
            browser.goBack();
        } else {
            super.onBackPressed();
        }
    }

    class WebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            webView.loadUrl(url);
            getIntent().putExtra("url", url);
            return true;
        }
    }
}
