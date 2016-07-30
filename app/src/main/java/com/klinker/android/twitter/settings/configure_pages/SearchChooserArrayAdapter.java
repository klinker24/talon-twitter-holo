package com.klinker.android.twitter.settings.configure_pages;

import android.content.Context;
import android.view.View;

import com.klinker.android.twitter.adapters.SavedSearchArrayAdapter;

import java.util.ArrayList;

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
