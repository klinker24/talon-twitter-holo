/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.twitter.adapter;

import android.app.Fragment;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.Gravity;
import com.klinker.android.twitter.activity.WearTransactionActivity;
import com.klinker.android.twitter.fragment.ExpandableCardFragment;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.fragment.SettingsButtonFragment;
import com.klinker.android.twitter.transaction.KeyProperties;

public class ArticleGridPagerAdapter extends FragmentGridPagerAdapter {

    private static final String TAG = "ArticleGridPagerAdapter";

    private WearTransactionActivity context;

    public ArticleGridPagerAdapter(WearTransactionActivity context) {
        super(context.getFragmentManager());
        this.context = context;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (col == 1) {
            return SettingsButtonFragment.create();
        } else {
            if (context.getTitles() == null || context.getTitles().size() == 0) {
                return CardFragment.create(context.getString(R.string.no_articles), "");
            } else {
                String bodyText = context.getBodies().get(row).replace(KeyProperties.LINE_BREAK, "\n\n");
                if (bodyText.endsWith("\n\n")) {
                    bodyText = bodyText.substring(0, bodyText.length() - 2);
                }

                ExpandableCardFragment card = ExpandableCardFragment.create(
                        context.getTitles().get(row),
                        bodyText,
                        Long.parseLong(context.getIds().get(row))
                );

                card.setExpansionEnabled(true);
                card.setExpansionDirection(CardFragment.EXPAND_DOWN);
                card.setCardGravity(Gravity.BOTTOM);
                return card;
            }
        }
    }

    @Override
    public int getRowCount() {
        if (context.getTitles() != null && context.getTitles().size() != 0) {
            return context.getTitles().size();
        } else {
            return 1;
        }
    }

    @Override
    public int getColumnCount(int row) {
        return 2;
    }

}
