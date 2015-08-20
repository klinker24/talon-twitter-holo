package com.klinker.android.twitter.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.activity.SettingsActivity;
import com.klinker.android.twitter.activity.WearActivity;
import com.klinker.android.twitter.transaction.KeyProperties;

public class FavoriteButtonFragment extends Fragment {

    private static final String ARG_TWEET_ID = "tweet_id";

    public static FavoriteButtonFragment create(long id) {
        Bundle args = new Bundle();
        args.putLong(ARG_TWEET_ID, id);

        FavoriteButtonFragment frag = new FavoriteButtonFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_button, parent, false);
        CircledImageView button = (CircledImageView) view.findViewById(R.id.favorite_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WearActivity)getActivity()).sendFavoriteRequest(getArguments().getLong(ARG_TWEET_ID));
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int accentColor = sharedPreferences.getInt(KeyProperties.KEY_ACCENT_COLOR, getResources().getColor(R.color.orange_accent_color));
        button.setCircleColor(accentColor);
        return view;
    }

}
