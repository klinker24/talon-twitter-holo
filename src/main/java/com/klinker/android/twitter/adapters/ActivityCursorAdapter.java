package com.klinker.android.twitter.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.view.View;
import android.view.ViewGroup;
import com.klinker.android.twitter.data.sq_lite.ActivityDataSource;
import com.klinker.android.twitter.data.sq_lite.ActivitySQLiteHelper;
import com.klinker.android.twitter.utils.EmojiUtils;
import com.klinker.android.twitter.utils.text.TextUtils;
import com.klinker.android.twitter.utils.text.TouchableMovementMethod;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class ActivityCursorAdapter extends TimeLineCursorAdapter {

    public ActivityCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, false);
    }

    private int TYPE_COL;
    private int TITLE_COL;

    @Override
    protected void init() {
        super.init();

        TYPE_COL = cursor.getColumnIndex(ActivitySQLiteHelper.COLUMN_TYPE);
        TITLE_COL = cursor.getColumnIndex(ActivitySQLiteHelper.COLUMN_TITLE);
    }

    @Override
    public void bindView(final View view, Context mContext, final Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        final String title = cursor.getString(TITLE_COL);
        final long id = cursor.getLong(TWEET_COL);
        holder.tweetId = id;
        final String profilePic = cursor.getString(PRO_PIC_COL);
        holder.proPicUrl = profilePic;
        final String tweetText = cursor.getString(TEXT_COL);
        final String name = cursor.getString(NAME_COL);
        final String screenname = cursor.getString(SCREEN_NAME_COL);
        final String picUrl = cursor.getString(PIC_COL);
        holder.picUrl = picUrl;
        final long longTime = cursor.getLong(TIME_COL);
        final String otherUrl = cursor.getString(URL_COL);
        final String users = cursor.getString(USER_COL);
        final String hashtags = cursor.getString(HASHTAG_COL);
        holder.gifUrl = cursor.getString(GIF_COL);

        holder.tweet.setMaxLines(2);
        holder.name.setSingleLine(true);

        int type = cursor.getInt(TYPE_COL);
        switch (type) {
            case ActivityDataSource.TYPE_FAVORITES:
                break;
            case ActivityDataSource.TYPE_NEW_FOLLOWER:
                break;
            case ActivityDataSource.TYPE_MENTION:
                break;
            case ActivityDataSource.TYPE_RETWEETS:
                break;
        }

        holder.name.setText(title);
        holder.tweet.setText(tweetText);

        CacheableBitmapDrawable wrapper2 = mCache.getFromMemoryCache(holder.proPicUrl);

        final boolean gotProPic;
        if (wrapper2 == null) {
            gotProPic = false;
            if (holder.profilePic.getDrawable() != null) {
                holder.profilePic.setImageDrawable(null);
            }
        } else {
            gotProPic = true;
            holder.profilePic.setImageDrawable(wrapper2);
        }

        mHandlers[currHandler].postDelayed(new Runnable() {
            @Override
            public void run() {
                if (holder.tweetId == id) {
                    if (!gotProPic) {
                        loadProPic(context, holder, holder.proPicUrl, mCache, id);
                    }

                    if (settings.useEmoji && (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || EmojiUtils.ios)) {
                        String text = holder.tweet.getText().toString();
                        if (EmojiUtils.emojiPattern.matcher(text).find()) {
                            final Spannable span = EmojiUtils.getSmiledText(context, Html.fromHtml(tweetText));
                            holder.tweet.setText(span);
                        }
                    }

                    holder.tweet.setSoundEffectsEnabled(false);
                    holder.tweet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!TouchableMovementMethod.touched) {
                                // we need to manually set the background for click feedback because the spannable
                                // absorbs the click on the background
                                if (!holder.preventNextClick) {
                                    holder.background.getBackground().setState(new int[]{android.R.attr.state_pressed});
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            holder.background.getBackground().setState(new int[]{android.R.attr.state_empty});
                                        }
                                    }, 25);
                                }

                                holder.background.performClick();
                            }
                        }
                    });

                    holder.tweet.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if (!TouchableMovementMethod.touched) {
                                holder.background.performLongClick();
                                holder.preventNextClick = true;
                            }
                            return false;
                        }
                    });

                    TextUtils.linkifyText(context, holder.tweet, holder.background, true, otherUrl, false);
                    TextUtils.linkifyText(context, holder.retweeter, holder.background, true, "", false);

                }
            }
        }, 400);
        currHandler++;

        if (currHandler == 10) {
            currHandler = 0;
        }
    }
}
