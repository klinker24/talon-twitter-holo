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
}
