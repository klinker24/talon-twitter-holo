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

package com.klinker.android.twitter.manipulations.widgets;

import android.content.Intent;
import android.net.Uri;
import android.webkit.*;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.widget.FrameLayout;

import com.klinker.android.twitter.R;

public class HTML5WebView extends WebView {

    private Context mContext;
    private MyWebChromeClient mWebChromeClient;
    private View mCustomView;
    private FrameLayout	mCustomViewContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private FrameLayout mContentView;
    private FrameLayout	mBrowserFrameLayout;
    private FrameLayout	mLayout;

    private void init(Context context) {
        mContext = context;
        Activity a = (Activity) mContext;

        mLayout = new FrameLayout(context);

        try {
            mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(a).inflate(R.layout.html_web_view, null);
        } catch (Exception e) {
            return;
        }
        mContentView = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.main_content);
        mCustomViewContainer = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.fullscreen_custom_content);

        mLayout = mBrowserFrameLayout;

        mWebChromeClient = new MyWebChromeClient();
        setWebChromeClient(mWebChromeClient);

        setWebViewClient(new MyWebViewClient());

        // Configure the webview
        WebSettings s = getSettings();
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSavePassword(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setAppCacheEnabled(false);
        s.setPluginState(WebSettings.PluginState.OFF);

        clearCache(true);

        // enable navigator.geolocation
        s.setGeolocationEnabled(true);
        s.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");

        // enable Web Storage: localStorage, sessionStorage
        s.setDomStorageEnabled(true);

        mContentView.addView(this);
    }

    public HTML5WebView(Context context) {
        super(context);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public FrameLayout getLayout() {
        return mLayout;
    }

    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((mCustomView == null) && canGoBack()){
                goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyWebChromeClient extends WebChromeClient {
        private Bitmap 		mDefaultVideoPoster;
        private View 		mVideoProgressView;

        @Override
        public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
        {
            Log.v("talon_webview", "showing custom view of youtube");

            /*HTML5WebView.this.setVisibility(View.GONE);

            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mCustomViewContainer.addView(view);
            mCustomView = view;
            mCustomViewCallback = callback;
            mCustomViewContainer.setVisibility(View.VISIBLE);*/
        }

        @Override
        public void onHideCustomView() {

            /*if (mCustomView == null)
                return;

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            mCustomViewContainer.removeView(mCustomView);
            mCustomView = null;
            mCustomViewContainer.setVisibility(View.GONE);
            mCustomViewCallback.onCustomViewHidden();

            HTML5WebView.this.setVisibility(View.VISIBLE);

            //Log.i(LOGTAG, "set it to webVew");*/
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            ((Activity) mContext).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress*100);
            if (newProgress > 90) {
                setBackgroundColor(getResources().getColor(android.R.color.white));
            } else {
                setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            return false;
        }
    }

    static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    public class MyWebViewClient extends WebViewClient {
        @Override

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.v("talon_url", "url: " + url);

            if (url.contains("play.google.com") || url.contains("youtube.com") || url.contains("youtu.be")) {
                Uri weburi;
                weburi = Uri.parse(url);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, weburi);
                getContext().startActivity(launchBrowser);
            } else {
                view.loadUrl(url);
            }

            return true;

        }

    }
}