package com.klinker.android.twitter.utils.api_helper;

import android.os.AsyncTask;

import com.klinker.android.twitter.APIKeys;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GiphyHelper {

    private static final String[] SIZE_OPTIONS = new String[] {
            "original", "downsized_medium", "fixed_height", "fixed_width", "fixed_height_small",
            "fixed_width_small", "downsized"
    };
    private static final long TWITTER_SIZE_LIMIT = 300000;

    public interface Callback {
        void onResponse(List<Gif> gifs);
    }

    public static void search(String query, Callback callback) {
        new SearchGiffy(query, callback).execute();
    }

    public static void trends(Callback callback) {
        new GiffyTrends(callback).execute();
    }

    private static class GiffyTrends extends SearchGiffy {

        public GiffyTrends(Callback callback) {
            super(null, callback);
        }

        @Override
        protected String buildSearchUrl(String query) throws UnsupportedEncodingException {
            return "http://api.giphy.com/v1/gifs/trending?api_key=" + APIKeys.GIPHY_API_KEY;
        }
    }
    private static class SearchGiffy extends AsyncTask<Void, Void, List<Gif>> {

        private String query;
        private Callback callback;

        public SearchGiffy(String query, Callback callback) {
            this.query = query;
            this.callback = callback;
        }

        @Override
        protected List<Gif> doInBackground(Void... arg0) {
            List<Gif> gifList = new ArrayList<>();

            try {
                // create the connection
                URL urlToRequest = new URL(buildSearchUrl(query));
                HttpURLConnection urlConnection = (HttpURLConnection)
                        urlToRequest.openConnection();

                // create JSON object from content
                InputStream in = new BufferedInputStream(
                        urlConnection.getInputStream());
                JSONObject root = new JSONObject(getResponseText(in));
                JSONArray data = root.getJSONArray("data");

                for (int i = 0; i < data.length(); i++) {
                    JSONObject gif = data.getJSONObject(i);
                    JSONObject images = gif.getJSONObject("images");
                    JSONObject originalStill = images.getJSONObject("original_still");
                    JSONObject originalSize = images.getJSONObject("original");
                    JSONObject downsized = null;

                    // get the highest quality that twitter can post (3 mb)
                    for (String size : SIZE_OPTIONS) {
                        downsized = images.getJSONObject(size);
                        if (Long.parseLong(downsized.getString("size")) < TWITTER_SIZE_LIMIT) {
                            break;
                        } else {
                            downsized = null;
                        }
                    }

                    if (downsized != null) {
                        gifList.add(
                                new Gif(originalStill.getString("url"),
                                        downsized.getString("url"),
                                        originalSize.getString("mp4"))
                        );
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return gifList;
        }

        @Override
        protected void onPostExecute(List<Gif> result) {
            if (callback != null) {
                callback.onResponse(result);
            }
        }

        protected String buildSearchUrl(String query) throws UnsupportedEncodingException {
            return "http://api.giphy.com/v1/gifs/search?" +
                    "q=" + URLEncoder.encode(query, "UTF-8") + "&" +
                    "limit=60&" +
                    "api_key=" + APIKeys.GIPHY_API_KEY;
        }

        private String getResponseText(InputStream inStream) {
            return new Scanner(inStream).useDelimiter("\\A").next();
        }
    }

    public static class Gif {
        public String previewImage;
        public String gifUrl;
        public String mp4Url;

        public Gif(String previewImage, String gifUrl, String mp4Url) {
            this.previewImage = URLDecoder.decode(previewImage);
            this.gifUrl = URLDecoder.decode(gifUrl);
            this.mp4Url = URLDecoder.decode(mp4Url);
        }
    }

}
