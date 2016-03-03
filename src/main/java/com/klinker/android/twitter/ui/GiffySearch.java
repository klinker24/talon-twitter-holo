package com.klinker.android.twitter.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

import com.klinker.android.twitter.R;
import com.lapism.arrow.ArrowDrawable;

public class GiffySearch extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {

        }

        setContentView(R.layout.giffy_search_activity);

        ArrowDrawable backArrow = (ArrowDrawable) ((ImageView) findViewById(R.id.imageView_arrow_back)).getDrawable();
        backArrow.animate(ArrowDrawable.STATE_ARROW);
    }
}
