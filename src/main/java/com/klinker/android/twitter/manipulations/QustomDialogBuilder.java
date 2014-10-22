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

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.SearchedPeopleCursorAdapter;
import com.klinker.android.twitter.data.sq_lite.FollowersDataSource;
import com.klinker.android.twitter.manipulations.widgets.HoloEditText;

public class QustomDialogBuilder extends AlertDialog.Builder{

    public Context context;
    private View mDialogView;
    private TextView mTitle;
    public HoloEditText text;
    public ListView list;
    private View mDivider;
    private int currentAccount;
        
    public QustomDialogBuilder(Context context, int currentAccount) {
        super(context);

        this.context = context;
        this.currentAccount = currentAccount;

        mDialogView = View.inflate(context, R.layout.qustom_dialog_layout, null);
        setView(mDialogView);

        mTitle = (TextView) mDialogView.findViewById(R.id.alertTitle);
        mDivider = mDialogView.findViewById(R.id.titleDivider);
        text = (HoloEditText) mDialogView.findViewById(R.id.content);

        list = (ListView) mDialogView.findViewById(R.id.contact_list);
        list.setAdapter(new SearchedPeopleCursorAdapter(context,
                FollowersDataSource.getInstance(context).getCursor(currentAccount, text.getText().toString()), text));
    }

    /** 
     * Use this method to color the divider between the title and content.
     * Will not display if no title is set.
     */
    public QustomDialogBuilder setDividerColor(int color) {
            mDivider.setBackgroundColor(color);
            return this;
    }
 
    @Override
    public QustomDialogBuilder setTitle(CharSequence text) {
        mTitle.setText(text);
        return this;
    }

    public QustomDialogBuilder setTitleColor(int color) {
            mTitle.setTextColor(color);
            return this;
    }
    
    @Override
    public AlertDialog show() {
        if (mTitle.getText().equals("")) mDialogView.findViewById(R.id.topPanel).setVisibility(View.GONE);

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String searchText = text.getText().toString();

                try {
                    if(searchText.substring(searchText.length() - 1, searchText.length()).equals(" ")) {
                        list.setAdapter(new SearchedPeopleCursorAdapter(context,
                                FollowersDataSource.getInstance(context).getCursor(currentAccount, ""), text));
                    } else {
                        if (searchText.contains(" ")) {
                            String[] split = searchText.split(" ");
                            searchText = split[split.length - 1];
                        }

                        list.setAdapter(new SearchedPeopleCursorAdapter(context,
                                FollowersDataSource.getInstance(context).getCursor(currentAccount, searchText), text));
                    }
                } catch (Exception e) {
                    list.setAdapter(new SearchedPeopleCursorAdapter(context,
                            FollowersDataSource.getInstance(context).getCursor(currentAccount, searchText), text));
                }
            }
        });
        return super.show();
    }
}