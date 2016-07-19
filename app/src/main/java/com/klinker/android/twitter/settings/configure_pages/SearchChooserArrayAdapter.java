package com.klinker.android.twitter.settings.configure_pages;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.SavedSearchArrayAdapter;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.drawer_activities.SavedSearchesActivity;
import com.klinker.android.twitter.ui.drawer_activities.discover.trends.SearchedTrendsActivity;
import com.klinker.android.twitter.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Twitter;

public class SearchChooserArrayAdapter extends SavedSearchArrayAdapter{

    public SearchChooserArrayAdapter(Context context, ArrayList<String> text) {
        super(context, text);
    }

    @Override
    public void bindView(final View view, Context mContext, final String trend) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.text.setText(trend);
    }

    public String getSearch(int position) {
        return text.get(position);
    }
}
