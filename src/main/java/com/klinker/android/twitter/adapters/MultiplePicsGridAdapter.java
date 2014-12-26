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
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.manipulations.photo_viewer.PhotoPagerActivity;
import com.klinker.android.twitter.manipulations.photo_viewer.PhotoViewerActivity;
import com.klinker.android.twitter.manipulations.widgets.NetworkedCacheableImageView;
import twitter4j.Status;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

import java.util.ArrayList;

public class MultiplePicsGridAdapter extends PicturesGridAdapter {

    String pics = "";

    public MultiplePicsGridAdapter(Context context, ArrayList<String> text, ArrayList<Status> statuses, int gridWidth) {
        super(context, text, statuses, gridWidth);

        for (String s : text) {
            pics += s + " ";
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.picture, null);

            AbsListView.LayoutParams params = new AbsListView.LayoutParams(gridWidth, gridWidth);
            convertView.setLayoutParams(params);

            ViewHolder holder = new ViewHolder();
            holder.iv = (NetworkedCacheableImageView) convertView.findViewById(R.id.picture);
            convertView.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.iv.loadImage(text.get(position), false, new NetworkedCacheableImageView.OnImageLoadedListener() {
            @Override
            public void onImageLoaded(CacheableBitmapDrawable result) {
                holder.iv.setBackgroundDrawable(null);
            }
        });

        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewImage = new Intent(context, PhotoPagerActivity.class);
                viewImage.putExtra("url", pics);
                viewImage.putExtra("start_page", position);
                context.startActivity(viewImage);
            }
        });

        return convertView;
    }
}
