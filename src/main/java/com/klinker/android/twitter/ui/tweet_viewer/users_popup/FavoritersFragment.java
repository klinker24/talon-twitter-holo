package com.klinker.android.twitter.ui.tweet_viewer.users_popup;


import android.os.Bundle;
import com.klinker.android.twitter.utils.FavoriterUtils;
import twitter4j.User;

import java.util.List;

public class FavoritersFragment extends UserListFragment {

    public static FavoritersFragment getFragment(long tweetId) {
        Bundle args = new Bundle();
        args.putLong("id", tweetId);

        FavoritersFragment frag = new FavoritersFragment();
        frag.setArguments(args);

        return frag;
    }
    @Override
    protected List<User> findUsers(long tweetId) {
        return (new FavoriterUtils()).getFavoriters(context, tweetId);
    }
}
