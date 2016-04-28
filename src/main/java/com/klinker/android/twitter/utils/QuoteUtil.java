package com.klinker.android.twitter.utils;

import java.util.List;

import twitter4j.Status;

public class QuoteUtil {
    public static List<Status> stripNoQuotes(List<Status> statuses) {
        for (int i = 0; i < statuses.size(); i++) {
            if (statuses.get(i).getQuotedStatus() == null) {
                statuses.remove(i);
                i--;
            }
        }

        return statuses;
    }

    public static List<Status> stripNoQuotesForActivity(List<Status> statuses, String screenname) {
        screenname = screenname.replace("@", "");

        for (int i = 0; i < statuses.size(); i++) {
            if (statuses.get(i).getQuotedStatus() == null ||
                    !statuses.get(i).getQuotedStatus().getUser().getScreenName().equals(screenname)) {
                statuses.remove(i);
                i--;
            }
        }

        return statuses;
    }
}
