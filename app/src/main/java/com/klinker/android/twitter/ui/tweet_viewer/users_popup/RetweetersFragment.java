package com.klinker.android.twitter.ui.tweet_viewer.users_popup;

import android.os.Bundle;
import com.klinker.android.twitter.utils.Utils;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

import java.util.ArrayList;
import java.util.List;

public class RetweetersFragment extends UserListFragment{

    public static RetweetersFragment getFragment(long tweetId) {
        Bundle args = new Bundle();
        args.putLong("id", tweetId);

        RetweetersFragment frag = new RetweetersFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    protected List<User> findUsers(long tweetId) {
        Twitter twitter =  Utils.getTwitter(context, settings);
        List<User> users = new ArrayList<User>();

        try {
            Status stat = twitter.showStatus(tweetId);
            if (stat.isRetweet()) {
                tweetId = stat.getRetweetedStatus().getId();
            }

            // can get 100 retweeters is all
            ResponseList<Status> lists = twitter.getRetweets(tweetId);

            for (Status status : lists) {
                users.add(status.getUser());
            }
        } catch (Exception e) {

        }

        return users;
    }
}
