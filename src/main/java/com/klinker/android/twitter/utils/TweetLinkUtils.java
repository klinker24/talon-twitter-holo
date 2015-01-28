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

package com.klinker.android.twitter.utils;

import android.util.Log;

import com.klinker.android.twitter.settings.AppSettings;

import java.util.ArrayList;

import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class TweetLinkUtils {

    public static String[] getLinksInStatus(Status status) {
        UserMentionEntity[] users = status.getUserMentionEntities();
        String mUsers = "";

        for(UserMentionEntity name : users) {
            String n = name.getScreenName();
            if (n.length() > 1) {
                mUsers += n + "  ";
            }
        }

        HashtagEntity[] hashtags = status.getHashtagEntities();
        String mHashtags = "";

        for (HashtagEntity hashtagEntity : hashtags) {
            String text = hashtagEntity.getText();
            if (text.length() > 1) {
                mHashtags += text + "  ";
            }
        }

        URLEntity[] urls = status.getURLEntities();
        String expandedUrls = "";
        String compressedUrls = "";

        for (URLEntity entity : urls) {
            String url = entity.getExpandedURL();
            if (url.length() > 1) {
                expandedUrls += url + "  ";
                compressedUrls += entity.getURL() + "  ";
            }
        }

        MediaEntity[] medias = status.getMediaEntities();
        String mediaExp = "";
        String mediaComp = "";
        String mediaDisplay = "";

        for (MediaEntity e : medias) {
            String url = e.getURL();
            if (url.length() > 1) {
                mediaComp += url + "  ";
                mediaExp += e.getExpandedURL() + "  ";
                mediaDisplay += e.getDisplayURL() + "  ";
            }
        }

        String[] sExpandedUrls;
        String[] sCompressedUrls;
        String[] sMediaExp;
        String[] sMediaComp;
        String[] sMediaDisplay;

        try {
            sCompressedUrls = compressedUrls.split("  ");
        } catch (Exception e) {
            sCompressedUrls = new String[0];
        }

        try {
            sExpandedUrls = expandedUrls.split("  ");
        } catch (Exception e) {
            sExpandedUrls = new String[0];
        }

        try {
            sMediaComp = mediaComp.split("  ");
        } catch (Exception e) {
            sMediaComp = new String[0];
        }

        try {
            sMediaExp = mediaExp.split("  ");
        } catch (Exception e) {
            sMediaExp = new String[0];
        }

        try {
            sMediaDisplay = mediaDisplay.split("  ");
        } catch (Exception e) {
            sMediaDisplay = new String[0];
        }

        String tweetTexts = status.getText();

        String imageUrl = "";
        String otherUrl = "";

        for (int i = 0; i < sCompressedUrls.length; i++) {
            String comp = sCompressedUrls[i];
            String exp = sExpandedUrls[i];

            if (comp.length() > 1 && exp.length() > 1) {
                String str = exp.toLowerCase();

                try {
                    tweetTexts = tweetTexts.replace(comp, exp.replace("http://", "").replace("https://", "").replace("www.", "").substring(0, 30) + "...");
                } catch (Exception e) {
                    tweetTexts = tweetTexts.replace(comp, exp.replace("http://", "").replace("https://", "").replace("www.", ""));
                }
                if (str.contains("instag") && !str.contains("blog.insta")) {
                    imageUrl = exp + "media/?size=l";
                    otherUrl += exp + "  ";
                } else if (exp.toLowerCase().contains("youtub") && !(str.contains("channel") || str.contains("user"))) {
                    // first get the youtube video code
                    int start = exp.indexOf("v=") + 2;
                    int end = exp.length();
                    if (exp.substring(start).contains("&")) {
                        end = exp.indexOf("&");
                    } else if (exp.substring(start).contains("?")) {
                        end = exp.indexOf("?");
                    }
                    try {
                        imageUrl = "http://img.youtube.com/vi/" + exp.substring(start, end) + "/hqdefault.jpg";
                    } catch (Exception e) {
                        imageUrl = "http://img.youtube.com/vi/" + exp.substring(start, exp.length() - 1) + "/hqdefault.jpg";
                    }
                    otherUrl += exp + "  ";
                } else if (str.contains("youtu.be")) {
                    // first get the youtube video code
                    int start = exp.indexOf(".be/") + 4;
                    int end = exp.length();
                    if (exp.substring(start).contains("&")) {
                        end = exp.indexOf("&");
                    } else if (exp.substring(start).contains("?")) {
                        end = exp.indexOf("?");
                    }
                    try {
                        imageUrl = "http://img.youtube.com/vi/" + exp.substring(start, end) + "/hqdefault.jpg";
                    } catch (Exception e) {
                        imageUrl = "http://img.youtube.com/vi/" + exp.substring(start, exp.length() - 1) + "/hqdefault.jpg";
                    }
                    otherUrl += exp + "  ";
                } else if (str.contains("twitpic")) {
                    int start = exp.indexOf(".com/") + 5;
                    imageUrl = "http://twitpic.com/show/full/" + exp.substring(start).replace("/", "");
                    otherUrl += exp + "  ";
                } else if (str.contains("i.imgur") && !str.contains("/a/")) {
                    int start = exp.indexOf(".com/") + 5;
                    imageUrl = "http://i.imgur.com/" + exp.replace("http://i.imgur.com/", "").replace(".jpg", "") + "l.jpg";
                    imageUrl = imageUrl.replace("gallery/", "");
                    otherUrl += exp + "  ";
                } else if (str.contains("imgur") && !str.contains("/a/")) {
                    int start = exp.indexOf(".com/") + 6;
                    imageUrl = "http://i.imgur.com/" + exp.replace("http://imgur.com/", "").replace(".jpg", "") + "l.jpg";
                    imageUrl = imageUrl.replace("gallery/", "").replace("a/", "");
                    otherUrl += exp + "  ";
                } else if (str.contains("pbs.twimg.com")) {
                    imageUrl = exp;
                    otherUrl += exp + "  ";
                } else if (str.contains("ow.ly/i/")) {
                    imageUrl = "http://static.ow.ly/photos/original/" + exp.substring(exp.lastIndexOf("/")).replaceAll("/", "") + ".jpg";
                    otherUrl += exp + "  ";
                } else if (str.contains("p.twipple.jp")) {
                    imageUrl = "http://p.twipple.jp/show/large/" + exp.replace("p.twipple.jp/", "").replace("http://", "").replace("https://", "").replace("www.", "");
                    otherUrl += exp + "  ";
                } else if (str.contains(".jpg") || str.contains(".png")) {
                    imageUrl = exp;
                    otherUrl += exp + "  ";
                } else if (str.contains("img.ly")) {
                    imageUrl = exp.replace("https", "http").replace("http://img.ly/", "http://img.ly/show/large/");
                    otherUrl += exp + "  ";
                } else {
                    otherUrl += exp + "  ";
                }
            }
        }

        for (int i = 0; i < sMediaComp.length; i++) {
            String comp = sMediaComp[i];
            String exp = sMediaExp[i];

            if (comp.length() > 1 && exp.length() > 1) {
                try {
                    tweetTexts = tweetTexts.replace(comp, sMediaDisplay[i].replace("http://", "").replace("https://", "").replace("www.", "").substring(0, 22) + "...");
                } catch (Exception e) {
                    tweetTexts = tweetTexts.replace(comp, sMediaDisplay[i].replace("http://", "").replace("https://", "").replace("www.", ""));
                }
                imageUrl = status.getMediaEntities()[0].getMediaURL();

                for (MediaEntity m : status.getExtendedMediaEntities()) {
                    if (m.getType().equals("photo")) {
                        if (!imageUrl.contains(m.getMediaURL())) {
                            imageUrl += " " + m.getMediaURL();
                        }
                    }
                }

                otherUrl += sMediaDisplay[i];
            }
        }

        return new String[] { tweetTexts, imageUrl, otherUrl, mHashtags, mUsers };
    }

    public static String[] getLinksInStatus(DirectMessage status) {
        UserMentionEntity[] users = status.getUserMentionEntities();
        String mUsers = "";

        for(UserMentionEntity name : users) {
            String n = name.getScreenName();
            if (n.length() > 1) {
                mUsers += n + "  ";
            }
        }

        HashtagEntity[] hashtags = status.getHashtagEntities();
        String mHashtags = "";

        for (HashtagEntity hashtagEntity : hashtags) {
            String text = hashtagEntity.getText();
            if (text.length() > 1) {
                mHashtags += text + "  ";
            }
        }

        URLEntity[] urls = status.getURLEntities();
        String expandedUrls = "";
        String compressedUrls = "";

        for (URLEntity entity : urls) {
            String url = entity.getExpandedURL();
            if (url.length() > 1) {
                expandedUrls += url + "  ";
                compressedUrls += entity.getURL() + "  ";
            }
        }

        MediaEntity[] medias = status.getMediaEntities();
        String mediaExp = "";
        String mediaComp = "";
        String mediaDisplay = "";

        for (MediaEntity e : medias) {
            String url = e.getURL();
            if (url.length() > 1) {
                mediaComp += url + "  ";
                mediaExp += e.getExpandedURL() + "  ";
                mediaDisplay += e.getDisplayURL() + "  ";
            }
        }

        String[] sExpandedUrls;
        String[] sCompressedUrls;
        String[] sMediaExp;
        String[] sMediaComp;
        String[] sMediaDisply;

        try {
            sCompressedUrls = compressedUrls.split("  ");
        } catch (Exception e) {
            sCompressedUrls = new String[0];
        }

        try {
            sExpandedUrls = expandedUrls.split("  ");
        } catch (Exception e) {
            sExpandedUrls = new String[0];
        }

        try {
            sMediaComp = mediaComp.split("  ");
        } catch (Exception e) {
            sMediaComp = new String[0];
        }

        try {
            sMediaExp = mediaExp.split("  ");
        } catch (Exception e) {
            sMediaExp = new String[0];
        }

        try {
            sMediaDisply = mediaDisplay.split("  ");
        } catch (Exception e) {
            sMediaDisply = new String[0];
        }

        String tweetTexts = status.getText();

        String imageUrl = "";
        String otherUrl = "";

        for (int i = 0; i < sCompressedUrls.length; i++) {
            String comp = sCompressedUrls[i];
            String exp = sExpandedUrls[i];

            if (comp.length() > 1 && exp.length() > 1) {
                String str = exp.toLowerCase();

                tweetTexts = tweetTexts.replace(comp, exp.replace("http://", "").replace("https://", "").replace("www.", ""));

                if(str.contains("instag") && !str.contains("blog.instag")) {
                    imageUrl = exp + "media/?size=m";
                    otherUrl += exp + "  ";
                } else if (str.contains("youtub") && !(str.contains("channel") || str.contains("user"))) { // normal youtube link
                    // first get the youtube video code
                    int start = exp.indexOf("v=") + 2;
                    int end = exp.length();
                    if (exp.substring(start).contains("&")) {
                        end = exp.indexOf("&");
                    } else if (exp.substring(start).contains("?")) {
                        end = exp.indexOf("?");
                    }
                    imageUrl = "http://img.youtube.com/vi/" + exp.substring(start, end) + "/hqdefault.jpg";
                    otherUrl += exp + "  ";
                } else if (str.contains("youtu.be")) { // shortened youtube link
                    // first get the youtube video code
                    int start = exp.indexOf(".be/") + 4;
                    int end = exp.length();
                    if (exp.substring(start).contains("&")) {
                        end = exp.indexOf("&");
                    } else if (exp.substring(start).contains("?")) {
                        end = exp.indexOf("?");
                    }
                    imageUrl = "http://img.youtube.com/vi/" + exp.substring(start, end) + "/hqdefault.jpg";
                    otherUrl += exp + "  ";
                } else if (str.contains("twitpic")) {
                    int start = exp.indexOf(".com/") + 5;
                    imageUrl = "http://twitpic.com/show/full/" + exp.substring(start).replace("/", "");
                    otherUrl += exp + "  ";
                } else if (str.contains("imgur") && !str.contains("/a/")) {
                    int start = exp.indexOf(".com/") + 6;
                    imageUrl = "http://i.imgur.com/" + exp.substring(start) + "l.jpg" ;
                    imageUrl = imageUrl.replace("gallery/", "").replace("a/", ""); 
                    otherUrl += exp + "  ";
                } else if (str.contains("pbs.twimg.com")) {
                    imageUrl = exp;
                    otherUrl += exp + "  ";
                } else if (str.contains("ow.ly/i/")) {
                    Log.v("talon_owly", exp);
                    imageUrl = "http://static.ow.ly/photos/original/" + exp.substring(exp.lastIndexOf("/")).replaceAll("/", "") + ".jpg";
                    otherUrl += exp + "  ";
                } else if (str.contains(".jpg") || str.contains(".png")) {
                    imageUrl = exp;
                    otherUrl += exp + "  ";
                } else if (str.contains("img.ly")) {
                    imageUrl = exp.replace("https", "http").replace("http://img.ly/", "http://img.ly/show/large/");
                    otherUrl += exp + "  ";
                } else {
                    otherUrl += exp + "  ";
                }
            }
        }

        for (int i = 0; i < sMediaComp.length; i++) {
            String comp = sMediaComp[i];
            String exp = sMediaExp[i];

            if (comp.length() > 1 && exp.length() > 1) {
                tweetTexts = tweetTexts.replace(comp, sMediaDisply[i]);
                imageUrl = status.getMediaEntities()[0].getMediaURL();
                otherUrl += sMediaDisply[i];
            }
        }

        return new String[] { tweetTexts, imageUrl, otherUrl, mHashtags, mUsers };
    }

    public static ArrayList<String> getAllExternalPictures(Status status) {
        URLEntity[] urls = status.getURLEntities();
        String expandedUrls = "";
        String compressedUrls = "";

        for (URLEntity entity : urls) {
            String url = entity.getExpandedURL();
            if (url.length() > 1) {
                expandedUrls += url + "  ";
                compressedUrls += entity.getURL() + "  ";
            }
        }

        MediaEntity[] medias = status.getMediaEntities();
        String mediaExp = "";
        String mediaComp = "";

        for (MediaEntity e : medias) {
            String url = e.getURL();
            if (url.length() > 1) {
                mediaComp += url + "  ";
                mediaExp += e.getExpandedURL() + "  ";
            }
        }

        String[] sExpandedUrls;
        String[] sCompressedUrls;
        String[] sMediaExp;
        String[] sMediaComp;

        try {
            sCompressedUrls = compressedUrls.split("  ");
        } catch (Exception e) {
            sCompressedUrls = new String[0];
        }

        try {
            sExpandedUrls = expandedUrls.split("  ");
        } catch (Exception e) {
            sExpandedUrls = new String[0];
        }

        try {
            sMediaComp = mediaComp.split("  ");
        } catch (Exception e) {
            sMediaComp = new String[0];
        }

        try {
            sMediaExp = mediaExp.split("  ");
        } catch (Exception e) {
            sMediaExp = new String[0];
        }

        ArrayList<String> images = new ArrayList<String>();

        for (int i = 0; i < sCompressedUrls.length; i++) {
            String comp = sCompressedUrls[i];
            String exp = sExpandedUrls[i];

            if (comp.length() > 1 && exp.length() > 1) {
                String str = exp.toLowerCase();

                if(str.contains("instag") && !str.contains("blog.insta")) {
                    images.add(exp + "media/?size=m");
                } else if (exp.toLowerCase().contains("youtub") && !(str.contains("channel") || str.contains("user"))) {
                    // first get the youtube video code
                    int start = exp.indexOf("v=") + 2;
                    int end = exp.length();
                    if (exp.substring(start).contains("&")) {
                        end = exp.indexOf("&");
                    } else if (exp.substring(start).contains("?")) {
                        end = exp.indexOf("?");
                    }
                    try {
                        images.add("http://img.youtube.com/vi/" + exp.substring(start, end) + "/hqdefault.jpg");
                    } catch (Exception e) {
                        images.add("http://img.youtube.com/vi/" + exp.substring(start, exp.length() - 1) + "/hqdefault.jpg");
                    }
                } else if (str.contains("youtu.be")) {
                    // first get the youtube video code
                    int start = exp.indexOf(".be/") + 4;
                    int end = exp.length();
                    if (exp.substring(start).contains("&")) {
                        end = exp.indexOf("&");
                    } else if (exp.substring(start).contains("?")) {
                        end = exp.indexOf("?");
                    }
                    try {
                        images.add("http://img.youtube.com/vi/" + exp.substring(start, end) + "/hqdefault.jpg");
                    } catch (Exception e) {
                        images.add("http://img.youtube.com/vi/" + exp.substring(start, exp.length() - 1) + "/mqefault.jpg");
                    }
                } else if (str.contains("twitpic")) {
                    int start = exp.indexOf(".com/") + 5;
                    images.add("http://twitpic.com/show/full/" + exp.substring(start).replace("/",""));
                } else if (str.contains("i.imgur") && !str.contains("/a/")) {
                    images.add(("http://i.imgur.com/" + exp.replace("http://i.imgur.com/", "").replace(".jpg", "") + "m.jpg").replace("gallery/", ""));
                } else if (str.contains("imgur") && !str.contains("/a/")) {
                    images.add(("http://i.imgur.com/" + exp.replace("http://imgur.com/", "").replace(".jpg", "") + "m.jpg").replace("gallery/", "").replace("a/", ""));
                } else if (str.contains("pbs.twimg.com")) {
                    images.add(exp);
                } else if (str.contains("ow.ly/i/")) {
                    images.add("http://static.ow.ly/photos/original/" + exp.substring(exp.lastIndexOf("/")).replaceAll("/", "") + ".jpg");
                } else if (str.contains("p.twipple.jp")) {
                    images.add("http://p.twipple.jp/show/large/" + exp.replace("p.twipple.jp/", "").replace("http://", "").replace("https://", "").replace("www.", ""));
                } else if (str.contains(".jpg") || str.contains(".png")) {
                    images.add(exp);
                } else if (str.contains("img.ly")) {
                    images.add(exp.replace("https", "http").replace("http://img.ly/", "http://img.ly/show/large/"));
                }
            }
        }

        for (int i = 0; i < sMediaComp.length; i++) {
            String comp = sMediaComp[i];
            String exp = sMediaExp[i];

            if (comp.length() > 1 && exp.length() > 1) {
                images.add(status.getMediaEntities()[0].getMediaURL());
            }
        }

        return images;
    }

    public static String removeColorHtml(String text, AppSettings settings) {
        text = text.replaceAll("<font color='#FF8800'>", "");
        text = text.replaceAll("</font>", "");
        if (settings.addonTheme) {
            text = text.replaceAll("<font color='" + settings.accentColor + "'>", "");
            text = text.replaceAll("</font>", "");
        }
        return text;
    }

    public static String getGIFUrl(Status s, String otherUrls) {

        // this will be used after twitter begins to support them
        for (MediaEntity e : s.getExtendedMediaEntities()) {
            if (e.getType().equals("animated_gif")) {
                return e.getMediaURL();
            } else if (e.getType().equals("video")) {
                // TODO this probably won't work, so we might have to find another way
                return e.getMediaURL();
            }
        }

        // this is how the urls are currently stored
        String gifUrl = "twitter.com/" + s.getUser().getScreenName() + "/status/" + s.getId() + "/photo/1";
        if (otherUrls.contains(gifUrl)) {
            return gifUrl;
        }

        // otherwise, lets just go with a blank string
        return "";
    }
}
