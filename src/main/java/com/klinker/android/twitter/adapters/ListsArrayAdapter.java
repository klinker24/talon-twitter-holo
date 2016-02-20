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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.drawer_activities.lists.ChoosenListActivity;
import com.klinker.android.twitter.ui.drawer_activities.lists.ViewUsers;
import com.klinker.android.twitter.utils.Utils;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.UserList;

public class ListsArrayAdapter extends ArrayAdapter<UserList> {

    private Context context;

    private ResponseList<UserList> lists;

    private LayoutInflater inflater;
    private AppSettings settings;

    public static class ViewHolder {
        public TextView text;
    }

    public ListsArrayAdapter(Context context, ResponseList<UserList> lists) {
        super(context, R.layout.tweet);

        this.context = context;
        this.lists = lists;

        settings = AppSettings.getInstance(context);
        inflater = LayoutInflater.from(context);

    }

    @Override
    public UserList getItem(int i) {
        return lists.get(i);
    }

    @Override
    public int getCount() {
        return lists.size();
    }


    public View newView(ViewGroup viewGroup) {
        View v;
        final ViewHolder holder;

        v = inflater.inflate(R.layout.text, viewGroup, false);

        holder = new ViewHolder();

        holder.text = (TextView) v.findViewById(R.id.text);

        // sets up the font sizes
        holder.text.setTextSize(24);

        v.setTag(holder);
        return v;
    }

    public void bindView(final View view, Context mContext, final UserList list) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        final String name = list.getName();
        final String id = list.getId() + "";

        holder.text.setText(name);

        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent list = new Intent(context, ChoosenListActivity.class);
                list.putExtra("list_id", id);
                list.putExtra("list_name", name);
                list.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(list);
            }
        });

        holder.text.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(R.array.lists_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final int DELETE_LIST = 0;
                        final int VIEW_USERS = 1;
                        switch (i) {
                            case DELETE_LIST:
                                new DeleteList().execute(id + "");
                                break;

                            case VIEW_USERS:
                                Intent viewUsers = new Intent(context, ViewUsers.class);
                                viewUsers.putExtra("list_id", Long.parseLong(id));
                                viewUsers.putExtra("list_name", name);
                                context.startActivity(viewUsers);
                                break;
                        }

                    }
                });

                builder.create();
                builder.show();

                return false;
            }
        });

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v;
        if (convertView == null) {
            v = newView(parent);
        } else {
            v = convertView;
        }

        bindView(v, context, lists.get(position));

        return v;
    }

    class DeleteList extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... urls) {
            Twitter twitter =  Utils.getTwitter(context, settings);

            boolean destroyedList;
            try {
                twitter.destroyUserList(Long.parseLong(urls[0]));
                destroyedList = true;
            } catch (Exception e) {
                destroyedList = false;
            }

            boolean unsubscribed;
            try {
                twitter.destroyUserListSubscription(Integer.parseInt(urls[0]));
                unsubscribed = true;
            } catch (Exception e) {
                unsubscribed = false;
            }

            return destroyedList || unsubscribed;
        }

        protected void onPostExecute(Boolean deleted) {

            if (deleted) {
                Toast.makeText(context, context.getResources().getString(R.string.deleted_list), Toast.LENGTH_SHORT).show();
                Toast.makeText(context, context.getResources().getString(R.string.back_to_refresh), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            }

        }
    }
}