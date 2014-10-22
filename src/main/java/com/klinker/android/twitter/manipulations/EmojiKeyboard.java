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

package com.klinker.android.twitter.manipulations;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.emoji.NatureEmojiAdapter;
import com.klinker.android.twitter.adapters.emoji.OtherEmojiAdapter;
import com.klinker.android.twitter.adapters.emoji.PeopleEmojiAdapter;
import com.klinker.android.twitter.adapters.emoji.RecentEmojiAdapter;
import com.klinker.android.twitter.adapters.emoji.ThingsEmojiAdapter;
import com.klinker.android.twitter.adapters.emoji.TransEmojiAdapter;
import com.klinker.android.twitter.data.sq_lite.EmojiDataSource;
import com.klinker.android.twitter.data.sq_lite.Recent;
import com.klinker.android.twitter.manipulations.widgets.HoloEditText;
import com.klinker.android.twitter.utils.EmojiUtils;

import java.util.ArrayList;

public class EmojiKeyboard extends LinearLayout {

    private HoloEditText input;
    private ViewPager emojiPager;
    private EmojiPagerAdapter emojiPagerAdapter;
    private PagerSlidingTabStrip tabs;
    private ImageButton backspace;
    private int keyboardHeight;

    private static EmojiDataSource dataSource;
    private static ArrayList<Recent> recents;

    public EmojiKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        try {
            try {
                getContext().getPackageManager().getPackageInfo("com.klinker.android.emoji_keyboard_trial", PackageManager.GET_META_DATA);
            } catch (Exception e) {
                getContext().getPackageManager().getPackageInfo("com.klinker.android.emoji_keyboard_trial_ios", PackageManager.GET_META_DATA);
            }

            emojiPager = (ViewPager) findViewById(R.id.emojiKeyboardPager);
            backspace = (ImageButton) findViewById(R.id.delete);
            Display d = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            keyboardHeight = (int) (d.getHeight() / 3.0);

            dataSource = new EmojiDataSource(getContext());
            dataSource.open();
            recents = (ArrayList<Recent>) dataSource.getAllRecents();

            emojiPager.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, keyboardHeight));

            tabs = (PagerSlidingTabStrip) findViewById(R.id.emojiTabs);
            tabs.setIndicatorColor(getResources().getColor(R.color.app_color));

            emojiPagerAdapter = new EmojiPagerAdapter(getContext(), emojiPager);
            emojiPager.setAdapter(emojiPagerAdapter);

            tabs.setViewPager(emojiPager);
            emojiPager.setCurrentItem(1);

            backspace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeText();
                }
            });
        } catch (Exception e) {
        }
    }

    public void setAttached(HoloEditText et) {
        this.input = et;
    }

    public boolean isShowing() {
        return getVisibility() == View.VISIBLE;
    }

    public void setVisibility(final boolean visible) {
        setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), visible ? R.anim.emoji_slide_out : R.anim.emoji_slide_in);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(visible ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(animation);
    }

    public void toggleVisibility() {
        setVisibility(getVisibility() != View.VISIBLE);
    }

    public void insertEmoji(String emoji, int icon) {
        input.setEnabled(false);
        int beforeSelectionStart = input.getSelectionStart();
        int beforeLength = input.getText().toString().length();
        CharSequence before = input.getText().subSequence(0, beforeSelectionStart);
        CharSequence after = input.getText().subSequence(input.getSelectionEnd(), beforeLength);
        input.setText(android.text.TextUtils.concat(before, Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !EmojiUtils.ios ? emoji : EmojiUtils.getSmiledText(getContext(), emoji), after));
        input.setEnabled(true);
        input.setSelection(beforeSelectionStart + (input.getText().toString().length() - beforeLength));
        for (Recent recent1 : recents) {
            if (recent1.text.equals(emoji)) {
                dataSource.updateRecent(icon + "");
                recent1.count++;
                return;
            }
        }
        Recent recent = dataSource.createRecent(emoji, icon + "");
        if (recent != null) recents.add(recent);
    }

    private void removeText() {
        String currentText = input.getText().toString();
        if (currentText.length() > 0 && input.getSelectionStart() > 0) {
            input.setEnabled(false);
            int selection = input.getSelectionStart();
            input.setText(EmojiUtils.getSmiledText(getContext(),
                    new StringBuilder(input.getText().toString()).deleteCharAt(selection - 1).toString()));
            input.setEnabled(true);
            input.setSelection(selection - 1);
        }
    }

    public void removeRecent(int position) {
        try {
            dataSource.deleteRecent(recents.get(position).id);
            recents.remove(position);
            emojiPagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {

        }
    }

    private class EmojiPagerAdapter extends PagerAdapter {

        private final String[] TITLES = {getContext().getString(R.string.recent), getContext().getString(R.string.people), getContext().getString(R.string.things), getContext().getString(R.string.nature), getContext().getString(R.string.places), getContext().getString(R.string.symbols)};
        private ViewPager pager;
        private ArrayList<View> pages;

        public EmojiPagerAdapter(Context context, ViewPager pager) {
            super();

            this.pager = pager;
            this.pages = new ArrayList<View>();

            pages.add(new KeyboardView(context, 0, recents).getView());
            pages.add(new KeyboardView(context, 1).getView());
            pages.add(new KeyboardView(context, 2).getView());
            pages.add(new KeyboardView(context, 3).getView());
            pages.add(new KeyboardView(context, 4).getView());
            pages.add(new KeyboardView(context, 5).getView());
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            pager.addView(pages.get(position), position, keyboardHeight);
            return pages.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            pager.removeView(pages.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private class KeyboardView {

        private int position;
        private Context context;
        private ArrayList<Recent> recents;

        public KeyboardView(Context context, int position) {
            this.context = context;
            this.position = position;
        }

        public KeyboardView(Context context, int position, ArrayList<Recent> recents) {
            this(context, position);
            this.recents = recents;
        }

        public View getView() {
            final GridView emojiGrid = new GridView(context);

            emojiGrid.setColumnWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));
            emojiGrid.setNumColumns(GridView.AUTO_FIT);

            if (position == 0)
                emojiGrid.setAdapter(new RecentEmojiAdapter(context, EmojiKeyboard.this, recents));
            else if (position == 1)
                emojiGrid.setAdapter(new PeopleEmojiAdapter(context, EmojiKeyboard.this));
            else if (position == 2)
                emojiGrid.setAdapter(new ThingsEmojiAdapter(context, EmojiKeyboard.this));
            else if (position == 3)
                emojiGrid.setAdapter(new NatureEmojiAdapter(context, EmojiKeyboard.this));
            else if (position == 4)
                emojiGrid.setAdapter(new TransEmojiAdapter(context, EmojiKeyboard.this));
            else
                emojiGrid.setAdapter(new OtherEmojiAdapter(context, EmojiKeyboard.this));

            return emojiGrid;
        }
    }
}