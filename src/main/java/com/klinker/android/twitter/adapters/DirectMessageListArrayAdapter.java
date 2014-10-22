/*
 * Copyright 2013 Luke Klinker
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.DirectMessage;
import com.klinker.android.twitter.data.sq_lite.DMDataSource;
import com.klinker.android.twitter.data.sq_lite.DMSQLiteHelper;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.DirectMessageConversation;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.utils.Utils;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.User;
import uk.co.senab.bitmapcache.BitmapLruCache;

public class DirectMessageListArrayAdapter extends ArrayAdapter<User> {

    public Context context;

    public ArrayList<DirectMessage> messages;

    public LayoutInflater inflater;
    public AppSettings settings;

    public int layout;
    public XmlResourceParser addonLayout = null;
    public Resources res;
    public int talonLayout;
    public BitmapLruCache mCache;
    public int border;

    public static class ViewHolder {
        public TextView name;
        public TextView text;
        public ImageView picture;
        public LinearLayout background;
    }

    public DirectMessageListArrayAdapter(Context context, ArrayList<DirectMessage> messages) {
        super(context, R.layout.person);

        this.context = context;
        this.messages = messages;

        settings = AppSettings.getInstance(context);
        inflater = LayoutInflater.from(context);

        setUpLayout();

    }

    public void setUpLayout() {
        talonLayout = settings.layout;

        if (settings.addonTheme) {
            try {
                res = context.getPackageManager().getResourcesForApplication(settings.addonThemePackage);
                addonLayout = res.getLayout(res.getIdentifier("person", "layout", settings.addonThemePackage));
            } catch (Exception e) {
                e.printStackTrace();
                switch (talonLayout) {
                    case AppSettings.LAYOUT_TALON:
                        layout = R.layout.person;
                        break;
                    case AppSettings.LAYOUT_HANGOUT:
                        layout = R.layout.person_hangouts;
                        break;
                    case AppSettings.LAYOUT_FULL_SCREEN:
                        layout = R.layout.person_full_screen;
                        break;
                }
            }
        } else {
            switch (talonLayout) {
                case AppSettings.LAYOUT_TALON:
                    layout = R.layout.person;
                    break;
                case AppSettings.LAYOUT_HANGOUT:
                    layout = R.layout.person_hangouts;
                    break;
                case AppSettings.LAYOUT_FULL_SCREEN:
                    layout = R.layout.person_full_screen;
                    break;
            }
        }

        TypedArray b;
        if (settings.roundContactImages) {
            b = context.getTheme().obtainStyledAttributes(new int[]{R.attr.circleBorder});
        } else {
            b = context.getTheme().obtainStyledAttributes(new int[]{R.attr.squareBorder});
        }
        border = b.getResourceId(0, 0);
        b.recycle();

        mCache = App.getInstance(context).getBitmapCache();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    public View newView(ViewGroup viewGroup) {
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
                    holder.text = (TextView) v.findViewById(res.getIdentifier("screen_name", "id", settings.addonThemePackage));
                    holder.background = (LinearLayout) v.findViewById(res.getIdentifier("background", "id", settings.addonThemePackage));
                    holder.picture = (ImageView) v.findViewById(res.getIdentifier("profile_pic", "id", settings.addonThemePackage));
                }
            } catch (Exception e) {
                e.printStackTrace();
                v = inflater.inflate(layout, viewGroup, false);

                holder.name = (TextView) v.findViewById(R.id.name);
                holder.text = (TextView) v.findViewById(R.id.screen_name);
                holder.background = (LinearLayout) v.findViewById(R.id.background);
                holder.picture = (ImageView) v.findViewById(R.id.profile_pic);
            }
        } else {
            v = inflater.inflate(layout, viewGroup, false);

            holder.name = (TextView) v.findViewById(R.id.name);
            holder.text = (TextView) v.findViewById(R.id.screen_name);
            holder.background = (LinearLayout) v.findViewById(R.id.background);
            holder.picture = (ImageView) v.findViewById(R.id.profile_pic);
        }

        // sets up the font sizes
        holder.name.setTextSize(settings.textSize + 4);
        holder.text.setTextSize(settings.textSize);
        holder.text.setSingleLine(true);

        v.setTag(holder);
        return v;
    }

    public void bindView(final View view, Context mContext, final DirectMessage dm) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.name.setText(settings.displayScreenName ? "@" + dm.getScreenname() : dm.getName());
        String tweetText = dm.getMessage();
        if (tweetText.contains("<font")) {
            if (settings.addonTheme) {
                holder.text.setText(Html.fromHtml(tweetText.replaceAll("FF8800", settings.accentColor).replaceAll("\n", "<br/>")));
            } else {
                holder.text.setText(Html.fromHtml(tweetText.replaceAll("\n", "<br/>")));
            }
        } else {
            holder.text.setText(tweetText);
        }

        //holder.picture.loadImage(user.getBiggerProfileImageURL(), true, null, NetworkedCacheableImageView.CIRCLE);
        if(settings.roundContactImages) {
            ImageUtils.loadCircleImage(context, holder.picture, dm.getPicture(), mCache);
        } else {
            ImageUtils.loadImage(context, holder.picture, dm.getPicture(), mCache);
        }

        holder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewConv = new Intent(context, DirectMessageConversation.class);
                viewConv.putExtra("screenname", dm.getScreenname());
                viewConv.putExtra("name", dm.getName());

                context.startActivity(viewConv);
            }
        });

        holder.background.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new DeleteConv(context, dm.getScreenname()).execute();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                builder.setTitle(R.string.delete_conversation);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
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

            final ViewHolder holder = (ViewHolder) v.getTag();

            holder.picture.setImageDrawable(context.getResources().getDrawable(border));
        }

        bindView(v, context, messages.get(position));

        return v;
    }

    class DeleteConv extends AsyncTask<String, Void, Boolean> {

        ProgressDialog pDialog;
        Context context;
        SharedPreferences sharedPrefs;
        String name;

        public DeleteConv(Context context, String name) {
            this.context = context;
            sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
            this.name = name;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(context.getResources().getString(R.string.deleting_messages) + "...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        protected Boolean doInBackground(String... urls) {

            DMDataSource data = DMDataSource.getInstance(context);

            try {
                Twitter twitter = Utils.getTwitter(context, AppSettings.getInstance(context));

                Cursor cursor = data.getConvCursor(name, settings.currentAccount);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndex(DMSQLiteHelper.COLUMN_TWEET_ID));
                        data.deleteTweet(id);
                        try {
                            twitter.destroyDirectMessage(id);
                        } catch (Exception x) {
                            // it doesn't actually exist on the twitter side
                        }
                    } while (cursor.moveToNext());
                }

                data.deleteDups(settings.currentAccount);

                return true;

            } catch (Exception e) {
                // they have no direct messages
                return true;
            }


        }

        protected void onPostExecute(Boolean deleted) {
            try {
                pDialog.dismiss();
                Toast.makeText(context, context.getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
            } catch (IllegalStateException e) {
                // view not attached
            } catch (IllegalArgumentException e) {

            }

            context.sendBroadcast(new Intent("com.klinker.android.twitter.UPDATE_DM"));
            sharedPrefs.edit().putLong("last_direct_message_id_" + settings.currentAccount, 0).commit();
        }
    }
}