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

public class GiffyHelper {

    public interface Callback {
        void onResponse(List<Gif> gifs);
    }

    public static void search(String query, Callback callback) {
        new SearchGiffy(query, callback).execute();
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
                    JSONObject originalSize = images.getJSONObject("original");
                    gifList.add(
                            new Gif(originalSize.getString("url"),
                                originalSize.getString("mp4"))
                    );
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

        private String buildSearchUrl(String query) throws UnsupportedEncodingException {
            return "http://api.giphy.com/v1/gifs/search?" +
                    "q=" + URLEncoder.encode(query, "UTF-8") + "&" +
                    "api_key=" + APIKeys.GIFFY_API_KEY;
        }

        private String getResponseText(InputStream inStream) {
            return new Scanner(inStream).useDelimiter("\\A").next();
        }
    }

    public static class Gif {
        public String gifUrl;
        public String mp4Url;

        public Gif(String gifUrl, String mp4Url) {
            this.gifUrl = URLDecoder.decode(gifUrl);
            this.mp4Url = URLDecoder.decode(mp4Url);
        }
    }

}
