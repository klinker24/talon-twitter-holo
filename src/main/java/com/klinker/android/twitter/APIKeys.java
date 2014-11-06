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

package com.klinker.android.twitter;

public class APIKeys {

    /**
     * Twitter's API Keys
     * THESE TWO ARE THE ONLY ONES REQUIRED TO RUN THE APP!
     * They are easy to obtain from Twitter
     *
     * Sign in with your Twitter credentials here:
     * https://apps.twitter.com/
     *
     * Then create a new application.
     *
     * For steps to creating an application, view the Readme.md
     */
    public static String TWITTER_CONSUMER_KEY = "";
    public static String TWITTER_CONSUMER_SECRET = "";

    /**
     * For the In-App Youtube Player
     * It WILL NOT work if you do not obtain a key for yourself.
     * It is easy to get one of these though.
     *
     * Here is how you can get a key for yourself:
     * https://developers.google.com/youtube/android/player/register
     */
    public static String YOUTUBE_API_KEY = "";

    /**
     * These are third party service API keys for Talon.
     *
     * If you wish to use these services, You will need to get a key as I will not be sharing mine
     * for obvious security reasons.
     *
     * Tweetmarker is a paid service, so if you want a key, you will have to pay $75 a month for it
     * For Twitlonger, you must request access to their API for your app. I do not know if he would grant an Open Source Api key or not.
     * TwitPic is dead, but I kept its classes in here so that you can still learn from them. The service no longer is supported.
     */
    public static final String TWEETMARKER_API_KEY = "";
    public static final String TWITLONGER_API_KEY = "";
    public static final String TWITPIC_API_KEY = "";
}