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

import com.klinker.android.twitter.settings.AppSettings;

import java.util.ArrayList;

import twitter4j.DirectMessage;
import twitter4j.DirectMessageEvent;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class TweetLinkUtils {

    public static String[] getLinksInStatus(Status status) {
        return getLinksInStatus(status.getText(), status.getUserMentionEntities(),
                status.getHashtagEntities(), status.getURLEntities(), status.getMediaEntities());
    }

    public static String[] getLinksInStatus(DirectMessage status) {
        return getLinksInStatus(status.getText(), status.getUserMentionEntities(),
                status.getHashtagEntities(), status.getURLEntities(), status.getMediaEntities());
    }

    public static String[] getLinksInStatus(DirectMessageEvent event) {
        return getLinksInStatus(event.getText(), event.getUserMentionEntities(), event.getHashtagEntities(),
                event.getUrlEntities(), event.getMediaEntities());
    }

    private static String[] getLinksInStatus(String tweetTexts, UserMentionEntity[] users, HashtagEntity[] hashtags,
                                             URLEntity[] urls, MediaEntity[] medias) {
        String mUsers = "";

        for(UserMentionEntity name : users) {
            String n = name.getScreenName();
            if (n.length() > 1) {
                mUsers += n + "  ";
            }
        }

        String mHashtags = "";

        for (HashtagEntity hashtagEntity : hashtags) {
            String text = hashtagEntity.getText();
            if (text.length() > 1) {
                mHashtags += text + "  ";
            }
        }

        String expandedUrls = "";
        String compressedUrls = "";

        for (URLEntity entity : urls) {
            String url = entity.getExpandedURL();
            if (url.length() > 1) {
                expandedUrls += url + "  ";
                compressedUrls += entity.getURL() + "  ";
            }
        }

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

        String imageUrl = "";
        String otherUrl = "";

        for (int i = 0; i < sCompressedUrls.length; i++) {
            String comp = sCompressedUrls[i];
            String exp = sExpandedUrls[i];

            if (comp.length() > 1 && exp.length() > 1) {
                String str = exp.toLowerCase();

                try {
                    String replacement = exp.replace("http://", "").replace("https://", "").replace("www.", "");

                    boolean hasCom = replacement.contains(".com");
                    replacement = replacement.substring(0, 30) + "...";

                    if (hasCom && !replacement.contains(".com")) { // the link was too long...
                        replacement = exp.replace("http://", "").replace("https://", "").replace("www.", "");
                        replacement = replacement.substring(0, replacement.indexOf(".com") + 6) + "...";
                    }

                    tweetTexts = tweetTexts.replace(comp, replacement);
                } catch (Exception e) {
                    tweetTexts = tweetTexts.replace(comp, exp.replace("http://", "").replace("https://", "").replace("www.", ""));
                }
                if (str.contains("instag") && !str.contains("blog.insta")) {
                    imageUrl = exp + "media/?size=l";
                    otherUrl += exp + "  ";
                } else if (exp.toLowerCase().contains("youtub") && !(str.contains("channel") || str.contains("user") || str.contains("playlist"))) {
                    // first get the youtube surfaceView code
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
                    // first get the youtube surfaceView code
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
                } else if (str.contains("i.imgur") && !str.contains("/a/") && !str.contains(".gifv")) {
                    int start = exp.indexOf(".com/") + 5;
                    imageUrl = "http://i.imgur.com/" + exp.replace("http://i.imgur.com/", "").replace(".jpg", "") + "l.jpg";
                    imageUrl = imageUrl.replace("gallery/", "");
                    otherUrl += exp + "  ";
                } else if (str.contains("imgur") && !str.contains("/a/") && !str.contains(".gifv")) {
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
                    String replacement = sMediaDisplay[i].replace("http://", "").replace("https://", "").replace("www.", "");

                    boolean hasCom = replacement.contains(".com");
                    replacement = replacement.substring(0, 22) + "...";

                    if (hasCom && !replacement.contains(".com")) { // the link was too long...
                        replacement = sMediaDisplay[i].replace("http://", "").replace("https://", "").replace("www.", "");
                        replacement = replacement.substring(0, replacement.indexOf(".com") + 6) + "...";
                    }

                    tweetTexts = tweetTexts.replace(comp, replacement);
                } catch (Exception e) {
                    e.printStackTrace();
                    tweetTexts = tweetTexts.replace(comp, sMediaDisplay[i].replace("http://", "").replace("https://", "").replace("www.", ""));
                }

                imageUrl = medias[0].getMediaURL();

                for (MediaEntity m : medias) {
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
                } else if (exp.toLowerCase().contains("youtub") && !(str.contains("channel") || str.contains("user") || str.contains("playlist"))) {
                    // first get the youtube surfaceView code
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
                    // first get the youtube surfaceView code
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

    public static String getGIFUrl(Status status, String otherUrls) {
        return getGIFUrl(status.getMediaEntities(), otherUrls);
    }

    public static String getGIFUrl(MediaEntity[] entities, String otherUrls) {

        for (MediaEntity e : entities) {
            if (e.getType().contains("gif")) {
                if (e.getVideoVariants().length > 0) {
                    String url = "";
                    MediaEntity.Variant variants[] = e.getVideoVariants();

                    if (variants.length == 0) {
                        return url;
                    }

                    for (int i = variants.length - 1; i >= 0; i--) {
                        MediaEntity.Variant v = variants[i];
                        if (v.getUrl().contains(".mp4") || v.getUrl().contains(".m3u8")) {
                            url = v.getUrl();
                        }
                    }

                    return url;
                }
                return e.getMediaURL().replace("tweet_video_thumb", "tweet_video").replace(".png", ".mp4").replace(".jpg", ".mp4").replace(".jpeg", ".mp4");
            } else if (e.getType().equals("surfaceView") || e.getType().equals("video")) {
                if (e.getVideoVariants().length > 0) {
                    String url = "";
                    MediaEntity.Variant variants[] = e.getVideoVariants();

                    if (variants.length == 0) {
                        return url;
                    }

                    for (int i = variants.length - 1; i >= 0; i--) {
                        MediaEntity.Variant v = variants[i];
                        if (v.getUrl().contains(".mp4")) {
                            url = v.getUrl();
                        }
                    }

                    if (url.isEmpty()) {
                        for (int i = variants.length - 1; i >= 0; i--) {
                            MediaEntity.Variant v = variants[i];
                            if (v.getUrl().contains(".m3u8")) {
                                url = v.getUrl();
                            }
                        }
                    }

                    return url;
                }
            }
        }

        // otherwise, lets just go with a blank string
        return "";
    }

}
