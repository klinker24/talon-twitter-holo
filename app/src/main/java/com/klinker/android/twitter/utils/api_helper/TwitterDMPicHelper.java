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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.TweetLinkUtils;
import org.apache.http.*;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import twitter4j.BASE64Encoder;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;


public class TwitterDMPicHelper {

    public Bitmap getDMPicture (String picUrl, Twitter twitter) {

        try {
            AccessToken token = twitter.getOAuthAccessToken();
            String oauth_token = token.getToken();
            String oauth_token_secret = token.getTokenSecret();

            // generate authorization header
            String get_or_post = "GET";
            String oauth_signature_method = "HMAC-SHA1";

            String uuid_string = UUID.randomUUID().toString();
            uuid_string = uuid_string.replaceAll("-", "");
            String oauth_nonce = uuid_string; // any relatively random alphanumeric string will work here

            // get the timestamp
            Calendar tempcal = Calendar.getInstance();
            long ts = tempcal.getTimeInMillis();// get current time in milliseconds
            String oauth_timestamp = (new Long(ts/1000)).toString(); // then divide by 1000 to get seconds

            // the parameter string must be in alphabetical order, "text" parameter added at end
            String parameter_string = "oauth_consumer_key=" + AppSettings.TWITTER_CONSUMER_KEY + "&oauth_nonce=" + oauth_nonce + "&oauth_signature_method=" + oauth_signature_method +
                    "&oauth_timestamp=" + oauth_timestamp + "&oauth_token=" + encode(oauth_token) + "&oauth_version=1.0";

            String twitter_endpoint = picUrl;
            String twitter_endpoint_host = picUrl.substring(0, picUrl.indexOf("1.1")).replace("https://", "").replace("/", "");
            String twitter_endpoint_path = picUrl.replace("ton.twitter.com", "").replace("https://", "");
            String signature_base_string = get_or_post + "&"+ encode(twitter_endpoint) + "&" + encode(parameter_string);
            String oauth_signature = computeSignature(signature_base_string, AppSettings.TWITTER_CONSUMER_SECRET + "&" + encode(oauth_token_secret));

            Log.v("talon_dm_image", "endpoint_host: " + twitter_endpoint_host);
            Log.v("talon_dm_image", "endpoint_path: " + twitter_endpoint_path);

            String authorization_header_string = "OAuth oauth_consumer_key=\"" + AppSettings.TWITTER_CONSUMER_KEY + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + oauth_timestamp +
                    "\",oauth_nonce=\"" + oauth_nonce + "\",oauth_version=\"1.0\",oauth_signature=\"" + encode(oauth_signature) + "\",oauth_token=\"" + encode(oauth_token) + "\"";


            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            HttpProtocolParams.setUserAgent(params, "HttpCore/1.1");
            HttpProtocolParams.setUseExpectContinue(params, false);
            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpRequestInterceptor[] {
                    // Required protocol interceptors
                    new RequestContent(),
                    new RequestTargetHost(),
                    // Recommended protocol interceptors
                    new RequestConnControl(),
                    new RequestUserAgent(),
                    new RequestExpectContinue()});

            HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
            HttpContext context = new BasicHttpContext(null);
            HttpHost host = new HttpHost(twitter_endpoint_host,443);
            DefaultHttpClientConnection conn = new DefaultHttpClientConnection();

            context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
            context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);

            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, null, null);
            SSLSocketFactory ssf = sslcontext.getSocketFactory();
            Socket socket = ssf.createSocket();
            socket.connect(
                    new InetSocketAddress(host.getHostName(), host.getPort()), 0);
            conn.bind(socket, params);
            BasicHttpEntityEnclosingRequest request2 = new BasicHttpEntityEnclosingRequest("GET", twitter_endpoint_path);
            request2.setParams(params);
            request2.addHeader("Authorization", authorization_header_string);
            httpexecutor.preProcess(request2, httpproc, context);
            HttpResponse response2 = httpexecutor.execute(request2, conn, context);
            response2.setParams(params);
            httpexecutor.postProcess(response2, httpproc, context);

            StatusLine statusLine = response2.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200 || statusCode == 302) {
                HttpEntity entity = response2.getEntity();
                byte[] bytes = EntityUtils.toByteArray(entity);

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length);
                return bitmap;
            } else {
                Log.v("talon_dm_image", statusCode + "");
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String encode(String value)
    {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
        }
        StringBuilder buf = new StringBuilder(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && (i + 1) < encoded.length()
                    && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }

    private static String computeSignature(String baseString, String keyString) throws GeneralSecurityException, UnsupportedEncodingException
    {
        SecretKey secretKey = null;

        byte[] keyBytes = keyString.getBytes();
        secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKey);

        byte[] text = baseString.getBytes();

        return new String(BASE64Encoder.encode(mac.doFinal(text))).trim();
    }
}