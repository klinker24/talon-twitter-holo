package com.klinker.android.twitter.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class GiffySearch extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {

        }

    }
}
