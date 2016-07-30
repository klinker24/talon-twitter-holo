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

package com.klinker.android.twitter.activities.tweet_viewer.fragments;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.klinker.android.twitter.views.HTML5WebView;

import java.util.ArrayList;

public class WebFragment extends Fragment {
    private View layout;
    private ArrayList<String> webpages;
    private String[] pages;

    private HTML5WebView webView;
    private ProgressBar progressBar;

    public Context context;

    public WebFragment() {
        this.webpages = new ArrayList<String>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        webpages = getArguments().getStringArrayList("webpages");

        context = getActivity();

        try {
            webView = new HTML5WebView(context);
        } catch (OutOfMemoryError e) {
            return null;
        }
        webView.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        try {
            webView.loadUrl(webpages.get(0));
        } catch (Exception e) {

        }

        return webView.getLayout();
    }

    @Override
    public void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (webpages.get(0).contains("vine")) {
                ((AudioManager)context.getSystemService(
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
}
