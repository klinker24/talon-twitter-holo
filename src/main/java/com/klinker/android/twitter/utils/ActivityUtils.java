package com.klinker.android.twitter.utils;

import android.content.Context;

public class ActivityUtils {

    private Context context;

    private String notificationText = "";
    private String notificationTitle = "";

    public ActivityUtils(Context context) {
        this.context = context;
    }

    /**
     * Refresh the new followers, mentions, number of favorites, and retweeters
     * @return boolean if there was something new
     */
    public boolean refreshActivity() {
        return false;
    }

    public void postNotification() {

    }
}
