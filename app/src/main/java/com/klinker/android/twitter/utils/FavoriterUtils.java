package com.klinker.android.twitter.utils;


import android.content.Context;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FavoriterUtils {

    public List<User> getFavoriters(Context context, long tweetId) {
        List<User> users = new ArrayList<User>();

        Twitter twitter =  Utils.getTwitter(context);

        try {
            Status stat = twitter.showStatus(tweetId);
            if (stat.isRetweet()) {
                tweetId = stat.getRetweetedStatus().getId();
            }

            long[] ids = getFavoritersIds(tweetId);
            users = twitter.lookupUsers(ids);

        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return users;
    }

    public JSONObject getJson(long tweetId) {
        try {
            String url = "https://twitter.com/i/activity/favorited_popup?id=" + tweetId;
            URL obj = new URL(url);

            HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
            connection.setRequestProperty("Content-Type", "text/html");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestMethod("GET");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.94 Safari/537.36");
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            String docHtml = sb.toString();

            try {
                connection.disconnect();
            } catch (Exception e) {

            }

            return new JSONObject(docHtml);
        } catch (Exception e) {
            return null;
        }
    }

    public long[] getFavoritersIds(long tweetId) {
        List<Long> idsList = new ArrayList<Long>();
        try {
            JSONObject json = getJson(tweetId);
            Document doc = Jsoup.parse(json.getString("htmlUsers"));

            if(doc != null) {
                Elements elements = doc.getElementsByTag("img");

                for (Element e : elements) {
                    try {
                        Long l = Long.parseLong(e.attr("data-user-id"));
                        if (l != null) {
                            idsList.add(l);
                        }
                    } catch (Exception x) {
                        // doesn't have it, could be an emoji or something from the looks of it.
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        long[] ids = new long[idsList.size()];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = idsList.get(i);
        }

        return ids;
    }
}
