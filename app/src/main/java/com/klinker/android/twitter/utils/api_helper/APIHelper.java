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

import com.klinker.android.twitter.settings.AppSettings;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import twitter4j.BASE64Encoder;
import twitter4j.HttpParameter;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

public abstract class APIHelper {

    public static final String SERVICE_PROVIDER = "https://api.twitter.com/1.1/account/verify_credentials.json";

    /**
     * Gets the header to verify the user on Twitter
     * @param twitter Coming from Twitter.getInstance()
     * @return String of the header to be used with X-Verify-Credentials-Authorization
     */
    public String getAuthrityHeader(Twitter twitter) {
        try {
            // gets the system time for the header
            long time = System.currentTimeMillis() / 1000;
            long millis = time + 12;

            // set the necessary parameters
            List<HttpParameter> oauthHeaderParams = new ArrayList<HttpParameter>(5);
            oauthHeaderParams.add(new HttpParameter("oauth_consumer_key", AppSettings.TWITTER_CONSUMER_KEY));
            oauthHeaderParams.add(new HttpParameter("oauth_signature_method", "HMAC-SHA1"));
            oauthHeaderParams.add(new HttpParameter("oauth_timestamp", time + ""));
            oauthHeaderParams.add(new HttpParameter("oauth_nonce", millis + ""));
            oauthHeaderParams.add(new HttpParameter("oauth_version", "1.0"));
            oauthHeaderParams.add(new HttpParameter("oauth_token", twitter.getOAuthAccessToken().getToken()));
            List<HttpParameter> signatureBaseParams = new ArrayList<HttpParameter>(oauthHeaderParams.size());
            signatureBaseParams.addAll(oauthHeaderParams);

            // create the signature
            StringBuilder base = new StringBuilder("GET").append("&")
                    .append(HttpParameter.encode(constructRequestURL(SERVICE_PROVIDER))).append("&");
            base.append(HttpParameter.encode(normalizeRequestParameters(signatureBaseParams)));

            String oauthBaseString = base.toString();
            String signature = generateSignature(oauthBaseString, twitter.getOAuthAccessToken());

            oauthHeaderParams.add(new HttpParameter("oauth_signature", signature));

            // create the header to post
            return "OAuth " + encodeParameters(oauthHeaderParams, ",", true);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Generates the signature to use with the header
     * @param data base signature data
     * @param token the user's access token
     * @return String of the signature to use in your header
     */
    public String generateSignature(String data, AccessToken token) {
        byte[] byteHMAC = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec spec;
            String oauthSignature = HttpParameter.encode(AppSettings.TWITTER_CONSUMER_SECRET) + "&" + HttpParameter.encode(token.getTokenSecret());
            spec = new SecretKeySpec(oauthSignature.getBytes(), "HmacSHA1");
            mac.init(spec);
            byteHMAC = mac.doFinal(data.getBytes());
        } catch (InvalidKeyException ike) {
            throw new AssertionError(ike);
        } catch (NoSuchAlgorithmException nsae) {
            throw new AssertionError(nsae);
        }
        return BASE64Encoder.encode(byteHMAC);
    }

    /**
     * Sorts and prepares the parameters
     * @param params Your parameters to post
     * @return String of the encoded parameters
     */
    static String normalizeRequestParameters(List<HttpParameter> params) {
        Collections.sort(params);
        return encodeParameters(params, "&", false);
    }

    /**
     * Encodes the parameters
     * @param httpParams parameters you want to send
     * @param splitter character used to split the parameters
     * @param quot whether you should use quotations or not
     * @return string of the desired encoding
     */
    public static String encodeParameters(List<HttpParameter> httpParams, String splitter, boolean quot) {
        StringBuilder buf = new StringBuilder();
        for (HttpParameter param : httpParams) {
            if (!param.isFile()) {
                if (buf.length() != 0) {
                    if (quot) {
                        buf.append("\"");
                    }
                    buf.append(splitter);
                }
                buf.append(HttpParameter.encode(param.getName())).append("=");
                if (quot) {
                    buf.append("\"");
                }
                buf.append(HttpParameter.encode(param.getValue()));
            }
        }
        if (buf.length() != 0) {
            if (quot) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }

    /**
     * Used to create the base signature text
     * @param url url of the post
     * @return string of the base signature
     */
    static String constructRequestURL(String url) {
        int index = url.indexOf("?");
        if (-1 != index) {
            url = url.substring(0, index);
        }
        int slashIndex = url.indexOf("/", 8);
        String baseURL = url.substring(0, slashIndex).toLowerCase();
        int colonIndex = baseURL.indexOf(":", 8);
        if (-1 != colonIndex) {
            // url contains port number
            if (baseURL.startsWith("http://") && baseURL.endsWith(":80")) {
                // http default port 80 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            } else if (baseURL.startsWith("https://") && baseURL.endsWith(":443")) {
                // http default port 443 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            }
        }
        url = baseURL + url.substring(slashIndex);

        return url;
    }
}
