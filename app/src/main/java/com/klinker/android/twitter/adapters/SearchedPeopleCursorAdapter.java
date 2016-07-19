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

package com.klinker.android.twitter.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.sq_lite.FavoriteUsersSQLiteHelper;
import com.klinker.android.twitter.manipulations.widgets.HoloEditText;
import com.klinker.android.twitter.utils.ImageUtils;

public class SearchedPeopleCursorAdapter extends PeopleCursorAdapter {

    public EditText text;

    public SearchedPeopleCursorAdapter(Context context, Cursor cursor, EditText text) {
        super(context, cursor);
        this.text = text;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View v = null;
        final ViewHolder holder = new ViewHolder();
        if (settings.addonTheme) {
            try {
                Context viewContext = null;

                if (res == null) {
                    res = context.getPackageManager().getResourcesForApplication(settings.addonThemePackage);
                }

                try {
                    viewContext = context.createPackageContext(settings.addonThemePackage, Context.CONTEXT_IGNORE_SECURITY);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (res != null && viewContext != null) {
                    int id = res.getIdentifier("person", "layout", settings.addonThemePackage);
                    v = LayoutInflater.from(viewContext).inflate(res.getLayout(id), null);


                    holder.name = (TextView) v.findViewById(res.getIdentifier("name", "id", settings.addonThemePackage));
                    holder.screenName = (TextView) v.findViewById(res.getIdentifier("screen_name", "id", settings.addonThemePackage));
                    holder.background = (LinearLayout) v.findViewById(res.getIdentifier("background", "id", settings.addonThemePackage));
                    holder.picture = (ImageView) v.findViewById(res.getIdentifier("profile_pic", "id", settings.addonThemePackage));
                }
            } catch (Exception e) {
                e.printStackTrace();
                v = inflater.inflate(layout, viewGroup, false);

                holder.name = (TextView) v.findViewById(R.id.name);
                holder.screenName = (TextView) v.findViewById(R.id.screen_name);
                holder.background = (LinearLayout) v.findViewById(R.id.background);
                holder.picture = (ImageView) v.findViewById(R.id.profile_pic);
            }
        } else {
            v = inflater.inflate(layout, viewGroup, false);

            holder.name = (TextView) v.findViewById(R.id.name);
            holder.screenName = (TextView) v.findViewById(R.id.screen_name);
            holder.background = (LinearLayout) v.findViewById(R.id.background);
            holder.picture = (ImageView) v.findViewById(R.id.profile_pic);
        }

        // sets up the font sizes
        holder.name.setTextSize(settings.textSize + 4);
        holder.screenName.setTextSize(settings.textSize);

        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        final String name = cursor.getString(cursor.getColumnIndex(FavoriteUsersSQLiteHelper.COLUMN_NAME));
        final String screenName = cursor.getString(cursor.getColumnIndex(FavoriteUsersSQLiteHelper.COLUMN_SCREEN_NAME));
        final String url = cursor.getString(cursor.getColumnIndex(FavoriteUsersSQLiteHelper.COLUMN_PRO_PIC));
        final long id = cursor.getLong(cursor.getColumnIndex(FavoriteUsersSQLiteHelper.COLUMN_ID));
        holder.userId = id;

        if (holder.divider != null && holder.divider.getVisibility() == View.VISIBLE) {
            holder.divider.setVisibility(View.GONE);
        }

        holder.name.setText(name);
        holder.screenName.setText("@" + screenName);

        //holder.picture.loadImage(url, true, null, NetworkedCacheableImageView.CIRCLE);
        if(settings.roundContactImages) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (holder.userId == id) {
                        loadCircleImage(context, holder, url, mCache, id);
                    }
                }
            }, 500);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (holder.userId == id) {
                        loadImage(context, holder, url, mCache, id);
                    }
                }
            }, 500);
        }

        holder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String currentText = text.getText().toString();

                try {
                    String[] curr = currentText.split(" ");
                    currentText = "";

                    for (String s : curr) {
                        if (s.contains("@")) {
                            currentText += s + " ";
                        }
                    }
                } catch (Exception e) {
                    currentText = text.getText().toString();
                }

                text.setText(currentText + "@" + screenName);
                text.setSelection(text.getText().length());

            }
        });
    }
}
