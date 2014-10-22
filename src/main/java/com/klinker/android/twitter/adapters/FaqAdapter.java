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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.utils.XmlFaqUtils;

public class FaqAdapter extends ArrayAdapter<XmlFaqUtils.FAQ> {

    private final Context context;
    private final XmlFaqUtils.FAQ[] items;

    static class ViewHolder {
        public TextView title;
        public TextView text;
        public LinearLayout background;
    }

    public FaqAdapter(Context context, XmlFaqUtils.FAQ[] spans) {
        super(context, R.layout.faq_item);
        this.context = context;
        this.items = spans;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(R.layout.faq_item, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.background = (LinearLayout) rowView.findViewById(R.id.faq_item);
            viewHolder.title = (TextView) rowView.findViewById(R.id.faq_title);
            viewHolder.text = (TextView) rowView.findViewById(R.id.faq_text);

            rowView.setTag(viewHolder);
        }

        final ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.title.setText(items[position].question);
        holder.text.setText(items[position].text);

        holder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.text.getVisibility() != View.VISIBLE) {
                    holder.text.setVisibility(View.VISIBLE);
                } else {
                    holder.text.setVisibility(View.GONE);
                }
            }
        });

        if (holder.text.getVisibility() != View.GONE) {
            holder.text.setVisibility(View.GONE);
        }

        return rowView;
    }
}