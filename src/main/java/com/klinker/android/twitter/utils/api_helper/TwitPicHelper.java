/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.twitter.utils.api_helper;

import android.content.Context;
import android.util.Log;

import com.klinker.android.twitter.APIKeys;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;

/**
 * NOTE: TwitPic has been SHUT DOWN. This class remains here as it can still be helpful
 * to show HTTP POST requests with data along with authentication
 *
 * This is something I really struggled with when I started, so hopefully this will help someone.
 */
public class TwitPicHelper extends APIHelper {

    public static final String TWITPIC_API_KEY = APIKeys.TWITPIC_API_KEY;
    public static final String POST_URL = "http://api.twitpic.com/2/upload.json";

    private Twitter twitter;
    private String message;
    private InputStream stream = null;
    private File file = null;
    private long replyToStatusId = 0;
    private GeoLocation location = null;
    private Context context;

    public TwitPicHelper(Twitter twitter, String message, InputStream stream, Context context) {
        this.twitter = twitter;
        this.message = message;
        this.stream = stream;
        this.context = context;
    }

    public TwitPicHelper(Twitter twitter, String message, File file, Context context) {
        this.twitter = twitter;
        this.message = message;
        this.file = file;
        this.context = context;
    }

    /**
     * Sets the tweet id if it is replying to another users tweet
     * @param replyToStatusId
     */
    public void setInReplyToStatusId(long replyToStatusId) {
        this.replyToStatusId = replyToStatusId;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    /**
     * posts the status onto Twitlonger, it then posts the shortened status (with link) to the user's twitter and updates the status on twitlonger
     * to include the posted status's id.
     *
     * @return id of the status that was posted to twitter
     */
    public long createPost() {
        TwitPicStatus status = uploadToTwitPic();
        Log.v("talon_twitpic", "past upload");
        long statusId;
        try {
            Status postedStatus;
            StatusUpdate update = new StatusUpdate(status.getText());

            if (replyToStatusId != 0) {
                update.setInReplyToStatusId(replyToStatusId);
            }
            if (location != null) {
                update.setLocation(location);
            }

            postedStatus = twitter.updateStatus(update);

            statusId = postedStatus.getId();
        } catch (Exception e) {
            e.printStackTrace();
            statusId = 0;
        }

        // if zero, then it failed
        return statusId;
    }

    public String uploadForUrl() {
        TwitPicStatus status = uploadToTwitPic();
        return status.getPicUrl();
    }

    private TwitPicStatus uploadToTwitPic() {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(POST_URL);
            post.addHeader("X-Auth-Service-Provider", SERVICE_PROVIDER);
            post.addHeader("X-Verify-Credentials-Authorization", getAuthrityHeader(twitter));

            if (file == null) {
                // only the input stream was sent, so we need to convert it to a file
                Log.v("talon_twitpic", "converting to file from input stream");
                String filePath = saveStreamTemp(stream);
                file = new File(filePath);
            } else {
                Log.v("talon_twitpic", "already have the file, going right to send it");
            }

            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("key", new StringBody(TWITPIC_API_KEY));
            entity.addPart("media", new FileBody(file));
            entity.addPart("message", new StringBody(message));

            Log.v("talon_twitpic", "uploading now");

            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line;
            String url = "";
            StringBuilder builder = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                Log.v("talon_twitpic", line);
                builder.append(line);
            }

            try {
                // there is only going to be one thing returned ever
                JSONObject jsonObject = new JSONObject(builder.toString());
                url = jsonObject.getString("url");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v("talon_twitpic", "url: " + url);
            Log.v("talon_twitpic", "message: " + message);

            return new TwitPicStatus(message, url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    class TwitPicStatus {
        private String tweetText;
        private String picUrl;
        private String totalTweet;

        public TwitPicStatus(String text, String url) {
            this.tweetText = text;
            this.picUrl = url;

            this.totalTweet = text + " " + url;
        }

        public String getText() {
            return totalTweet;
        }
        public String getPicUrl() {
            return picUrl;
        }
    }

    String saveStreamTemp(InputStream fStream){
        final File file;
        try {
            String timeStamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            file = new File(context.getCacheDir(), "temp_" + timeStamp + ".jpg");
            final OutputStream output;
            try {
                output = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return "";
            }
            try {
                try {
                    final byte[] buffer = new byte[1024];
                    int read;

                    while ((read = fStream.read(buffer)) != -1)
                        output.write(buffer, 0, read);

                    output.flush();
                } finally {
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            try {
                fStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

        return file.getPath();
    }

}
