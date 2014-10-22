/*
 * Copyright 2013 Jacob Klinker
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

package com.klinker.android.twitter.utils.text;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.klinker.android.twitter.data.Link;
import com.klinker.android.twitter.settings.AppSettings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Linkify {
    /**
     * Bit field indicating that web URLs should be matched in methods that
     * take an options mask
     */
    private static final int WEB_URLS = 0x01;

    /**
     * Bit field indicating that email addresses should be matched in methods
     * that take an options mask
     */
    private static final int EMAIL_ADDRESSES = 0x02;

    /**
     * Bit field indicating that phone numbers should be matched in methods that
     * take an options mask
     */
    private static final int PHONE_NUMBERS = 0x04;

    /**
     * Bit field indicating that street addresses should be matched in methods that
     * take an options mask
     */
    private static final int MAP_ADDRESSES = 0x08;

    /**
     * Bit mask indicating that all available patterns should be matched in
     * methods that take an options mask
     */
    public static final int ALL = WEB_URLS | EMAIL_ADDRESSES | PHONE_NUMBERS | MAP_ADDRESSES;

    /**
     * Filters out web URL matches that occur after an at-sign (@).  This is
     * to prevent turning the domain name in an email address into a web link.
     */
    private static final MatchFilter sUrlMatchFilter = new MatchFilter() {
        public final boolean acceptMatch(CharSequence s, int start, int end) {
            if (start == 0) {
                return true;
            }

            return s.charAt(start - 1) != '@';

        }
    };

    /**
     * Filters out URL matches that don't have enough digits to be a
     * phone number.
     */
    private static final MatchFilter sPhoneNumberMatchFilter = new MatchFilter() {
        public final boolean acceptMatch(CharSequence s, int start, int end) {
            int digitCount = 0;

            for (int i = start; i < end; i++) {
                if (Character.isDigit(s.charAt(i))) {
                    digitCount++;
                    /*
      Don't treat anything with fewer than this many digits as a
      phone number.
     */
                    int PHONE_NUMBER_MINIMUM_DIGITS = 5;
                    if (digitCount >= PHONE_NUMBER_MINIMUM_DIGITS) {
                        return true;
                    }
                }
            }
            return false;
        }
    };

    /**
     * Transforms matched phone number text into something suitable
     * to be used in a tel: URL.  It does this by removing everything
     * but the digits and plus signs.  For instance:
     * &apos;+1 (919) 555-1212&apos;
     * becomes &apos;+19195551212&apos;
     */
    private static final TransformFilter sPhoneNumberTransformFilter = new TransformFilter() {
        public final String transformUrl(final Matcher match, String url) {
            return Regex.digitsAndPlusOnly(match);
        }
    };

    /**
     * MatchFilter enables client code to have more control over
     * what is allowed to match and become a link, and what is not.
     * <p/>
     * For example:  when matching web urls you would like things like
     * http://www.example.com to match, as well as just example.com itelf.
     * However, you would not want to match against the domain in
     * support@example.com.  So, when matching against a web url pattern you
     * might also include a MatchFilter that disallows the match if it is
     * immediately preceded by an at-sign (@).
     */
    public interface MatchFilter {
        /**
         * Examines the character span matched by the pattern and determines
         * if the match should be turned into an actionable link.
         *
         * @param s     The body of text against which the pattern
         *              was matched
         * @param start The index of the first character in s that was
         *              matched by the pattern - inclusive
         * @param end   The index of the last character in s that was
         *              matched - exclusive
         * @return Whether this match should be turned into a link
         */
        boolean acceptMatch(CharSequence s, int start, int end);
    }

    /**
     * TransformFilter enables client code to have more control over
     * how matched patterns are represented as URLs.
     * <p/>
     * For example:  when converting a phone number such as (919)  555-1212
     * into a tel: URL the parentheses, white space, and hyphen need to be
     * removed to produce tel:9195551212.
     */
    public interface TransformFilter {
        /**
         * Examines the matched text and either passes it through or uses the
         * data in the Matcher state to produce a replacement.
         *
         * @param match The regex matcher state that found this URL text
         * @param url   The text that was matched
         * @return The transformed form of the URL
         */
        String transformUrl(final Matcher match, String url);
    }

    /**
     * Scans the text of the provided Spannable and turns all occurrences
     * of the link types indicated in the mask into clickable links.
     * If the mask is nonzero, it also removes any existing URLSpans
     * attached to the Spannable, to avoid problems if you call it
     * repeatedly on the same text.
     */
    /*private static boolean addLinks(Context context, Spannable text, int mask, TextView tv, View holder) {
        if (mask == 0) {
            return false;
        }

        URLSpan[] old = text.getSpans(0, text.length(), URLSpan.class);

        for (int i = old.length - 1; i >= 0; i--) {
            text.removeSpan(old[i]);
        }

        ArrayList<LinkSpec> links = new ArrayList<LinkSpec>();

        if ((mask & WEB_URLS) != 0) {
            gatherLinks(links, text, Regex.WEB_URL_PATTERN, sUrlMatchFilter, null);
        }

        if ((mask & EMAIL_ADDRESSES) != 0) {
            gatherLinks(links, text, Regex.EMAIL_ADDRESS_PATTERN, null, null);
        }

        if ((mask & PHONE_NUMBERS) != 0) {
            gatherLinks(links, text, Regex.PHONE_PATTERN, sPhoneNumberMatchFilter, sPhoneNumberTransformFilter);
        }

        if ((mask & MAP_ADDRESSES) != 0) {
            gatherMapLinks(links, text);
        }

        pruneOverlaps(links);

        if (links.size() == 0) {
            return false;
        }

        for (LinkSpec link : links) {
            applyLink(context, tv, holder, link.url, link.start, link.end, text);
        }

        return true;
    }*/

    private static void addLinkMovementMethod(TextView t) {
        MovementMethod m = t.getMovementMethod();

        if ((m == null) || !(m instanceof TouchableMovementMethod)) {
            if (t.getLinksClickable()) {
                t.setMovementMethod(TouchableMovementMethod.getInstance());
            }
        }
    }

    /**
     * Applies a regex to the text of a TextView turning the matches into
     * links.  If links are found then UrlSpans are applied to the link
     * text match areas, and the movement method for the text is changed
     * to LinkMovementMethod.
     *
     * @param text    TextView whose text is to be marked-up with links
     * @param pattern Regex pattern to be used for finding links
     */
    public static void addLinks(Context context, TextView text, Pattern pattern, boolean extBrowser) {
        addLinks(context, text, pattern, null, null, null, null, "", extBrowser);
    }

    /**
     * Applies a regex to the text of a TextView turning the matches into
     * links.  If links are found then UrlSpans are applied to the link
     * text match areas, and the movement method for the text is changed
     * to LinkMovementMethod.
     *
     * @param text        TextView whose text is to be marked-up with links
     * @param p           Regex pattern to be used for finding links
     * @param matchFilter The filter that is used to allow the client code
     *                    additional control over which pattern matches are
     *                    to be converted into links.
     */
    public static void addLinks(Context context, TextView text, Pattern p,
                                MatchFilter matchFilter, TransformFilter transformFilter, TextView tv, View holder, String allUrls, boolean extBrowser) {
        SpannableString s = SpannableString.valueOf(text.getText());

        if (addLinks(context, s, p, matchFilter, transformFilter, tv, holder, allUrls, extBrowser)) {
            text.setText(s);
            addLinkMovementMethod(text);
        }
    }

    public static void addLinks(Context context, AppSettings settings, TextView text, Pattern p,
                                MatchFilter matchFilter, TransformFilter transformFilter, TextView tv, View holder, String allUrls, boolean extBrowser) {
        SpannableString s = SpannableString.valueOf(text.getText());

        if (addLinks(context, settings, s, p, matchFilter, transformFilter, tv, holder, allUrls, extBrowser)) {
            text.setText(s);
            addLinkMovementMethod(text);
        }
    }

    /**
     * Applies a regex to a Spannable turning the matches into
     * links.
     *
     * @param s           Spannable whose text is to be marked-up with
     *                    links
     * @param p           Regex pattern to be used for finding links
     * @param matchFilter The filter that is used to allow the client code
     *                    additional control over which pattern matches are
     *                    to be converted into links.
     */
    private static boolean addLinks(Context context, Spannable s, Pattern p,
                                    MatchFilter matchFilter,
                                    TransformFilter transformFilter, TextView tv, View holder, String allUrls, boolean extBrowser) {
        boolean hasMatches = false;
        Matcher m = p.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean allowed = true;

            if (matchFilter != null) {
                allowed = matchFilter.acceptMatch(s, start, end);
            }

            if (allowed) {
                String shortUrl = makeUrl(m.group(0), m, transformFilter);
                String longUrl = getLongUrl(shortUrl, allUrls);

                //Log.v("talon_replace", "longurl: " + longUrl);
                //Log.v("talon_replace", "shorturl: " + shortUrl);

                applyLink(context, tv, holder, new Link(shortUrl, longUrl), start, end, s, extBrowser);
                hasMatches = true;
            }
        }

        return hasMatches;
    }

    private static boolean addLinks(Context context, AppSettings settings, Spannable s, Pattern p,
                                    MatchFilter matchFilter,
                                    TransformFilter transformFilter, TextView tv, View holder, String allUrls, boolean extBrowser) {
        boolean hasMatches = false;
        Matcher m = p.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean allowed = true;

            if (matchFilter != null) {
                allowed = matchFilter.acceptMatch(s, start, end);
            }

            if (allowed) {
                String shortUrl = makeUrl(m.group(0), m, transformFilter);
                String longUrl = getLongUrl(shortUrl, allUrls);

                applyLink(context, settings, tv, holder, new Link(shortUrl, longUrl), start, end, s, extBrowser);
                hasMatches = true;
            }
        }

        return hasMatches;
    }

    private static String getLongUrl(String shortUrl, String allUrls) {
        if (allUrls == null) {
            return shortUrl;
        }

        String url = "";
        String[] otherUrls = allUrls.split("  ");
        shortUrl = shortUrl.replace("...", "");

        shortUrl = stripTrailingPeriods(shortUrl);

        /*if (shortUrl.contains("twitter.com/")) {
            // safe to assume picture
            // the link will be the last one in the array
            if (otherUrls.length == 1) {
                url = otherUrls[0];
            } else {
                url = otherUrls[otherUrls.length - 1];
            }
        } else {*/
            for (String s : otherUrls) {
                if (s.contains(shortUrl)) {
                    url = s;
                    break;
                }
            }
        //}

        if (url.equals("")) {
            url = shortUrl;
        }

        return url;
    }

    private static String stripTrailingPeriods(String url) {
        try {
            if (url.substring(url.length() - 1, url.length()).equals(".")) {
                return stripTrailingPeriods(url.substring(0, url.length() - 1));
            } else {
                return url;
            }
        } catch (Exception e) {
            return url;
        }
    }

    private static void applyLink(Context context, TextView tv, final View holder, Link url, final int start, final int end, final Spannable text, boolean extBrowser) {
        TouchableSpan span = new TouchableSpan(context, url, extBrowser);
        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void applyLink(Context context, AppSettings settings, TextView tv, final View holder, Link url, final int start, final int end, final Spannable text, boolean extBrowser) {
        TouchableSpan span = new TouchableSpan(context, url, extBrowser, settings);
        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static String makeUrl(String url, Matcher m, TransformFilter filter) {
        if (filter != null) {
            url = filter.transformUrl(m, url);
        }
        return url;
    }

    private static void gatherLinks(ArrayList<LinkSpec> links,
                                    Spannable s, Pattern pattern,
                                    MatchFilter matchFilter, TransformFilter transformFilter) {
        Matcher m = pattern.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            if (matchFilter == null || matchFilter.acceptMatch(s, start, end)) {
                LinkSpec spec = new LinkSpec();

                spec.url = makeUrl(m.group(0), m, transformFilter);
                spec.start = start;
                spec.end = end;

                links.add(spec);
            }
        }
    }

    private static void gatherMapLinks(ArrayList<LinkSpec> links, Spannable s) {
        String string = s.toString();
        String address;
        int base = 0;

        while ((address = WebView.findAddress(string)) != null) {
            int start = string.indexOf(address);

            if (start < 0) {
                break;
            }

            LinkSpec spec = new LinkSpec();
            int length = address.length();
            int end = start + length;

            spec.start = base + start;
            spec.end = base + end;
            string = string.substring(end);
            base += end;

            String encodedAddress = null;

            try {
                encodedAddress = URLEncoder.encode(address, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                continue;
            }

            spec.url = "geo:0,0?q=" + encodedAddress;
            links.add(spec);
        }
    }

    private static void pruneOverlaps(ArrayList<LinkSpec> links) {
        Comparator<LinkSpec> c = new Comparator<LinkSpec>() {
            public final int compare(LinkSpec a, LinkSpec b) {
                if (a.start < b.start) {
                    return -1;
                }

                if (a.start > b.start) {
                    return 1;
                }

                if (a.end < b.end) {
                    return 1;
                }

                if (a.end > b.end) {
                    return -1;
                }

                return 0;
            }

            public final boolean equals(Object o) {
                return false;
            }
        };

        Collections.sort(links, c);

        int len = links.size();
        int i = 0;

        while (i < len - 1) {
            LinkSpec a = links.get(i);
            LinkSpec b = links.get(i + 1);
            int remove = -1;

            if ((a.start <= b.start) && (a.end > b.start)) {
                if (b.end <= a.end) {
                    remove = i + 1;
                } else if ((a.end - a.start) > (b.end - b.start)) {
                    remove = i + 1;
                } else if ((a.end - a.start) < (b.end - b.start)) {
                    remove = i;
                }

                if (remove != -1) {
                    links.remove(remove);
                    len--;
                    continue;
                }

            }

            i++;
        }
    }

    static class LinkSpec {
        String url;
        int start;
        int end;
    }
}
