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

package com.klinker.android.twitter.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.CursorListLoader;
import com.klinker.android.twitter.adapters.TimeLineCursorAdapter;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.sq_lite.DMDataSource;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.manipulations.widgets.HoloEditText;
import com.klinker.android.twitter.manipulations.widgets.HoloTextView;
import com.klinker.android.twitter.ui.setup.LoginActivity;
import com.klinker.android.twitter.utils.IOUtils;
import com.klinker.android.twitter.utils.Utils;
import com.klinker.android.twitter.utils.api_helper.TwitPicHelper;

import org.lucasr.smoothie.AsyncListView;
import org.lucasr.smoothie.ItemManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import uk.co.senab.bitmapcache.BitmapLruCache;


public class DirectMessageConversation extends Activity {

    public AppSettings settings;
    private Context context;
    private SharedPreferences sharedPrefs;

    private ActionBar actionBar;

    private AsyncListView listView;
    private HoloEditText composeBar;
    private ImageButton sendButton;
    private HoloTextView charRemaining;

    private String listName;

    final Pattern p = Patterns.WEB_URL;
    public Handler countHandler;
    public Runnable getCount = new Runnable() {
        @Override
        public void run() {
            String text = composeBar.getText().toString();

            if (!Patterns.WEB_URL.matcher(text).find()) { // no links, normal tweet
                try {
                    charRemaining.setText(140 - composeBar.getText().length() - (attachedUri.equals("") ? 0 : 23) + "");
                } catch (Exception e) {
                    charRemaining.setText("0");
                }
            } else {
                int count = text.length();
                Matcher m = p.matcher(text);
                while(m.find()) {
                    String url = m.group();
                    count -= url.length(); // take out the length of the url
                    count += 23; // add 23 for the shortened url
                }

                if (!attachedUri.equals("")) {
                    count += 23;
                }

                charRemaining.setText(140 - count + "");
            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);
    }

    @Override
    public void onDestroy() {
        try {
            cursorAdapter.getCursor().close();
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);

        countHandler = new Handler();

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        context = this;
        sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
        settings = AppSettings.getInstance(this);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        if (settings.advanceWindowed) {
            setUpWindow();
        }

        Utils.setUpPopupTheme(this, settings);

        actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.lists));

        setContentView(R.layout.dm_conversation);

        attachImage = (ImageView) findViewById(R.id.attached_image);

        if (!settings.isTwitterLoggedIn) {
            Intent login = new Intent(context, LoginActivity.class);
            startActivity(login);
            finish();
        }

        listView = (AsyncListView) findViewById(R.id.listView);
        sendButton = (ImageButton) findViewById(R.id.send_button);
        composeBar = (HoloEditText) findViewById(R.id.tweet_content);
        charRemaining = (HoloTextView) findViewById(R.id.char_remaining);

        BitmapLruCache cache = App.getInstance(context).getBitmapCache();
        CursorListLoader loader = new CursorListLoader(cache, context);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(50);
        builder.setThreadPoolSize(4);

        listView.setItemManager(builder.build());

        listName = getIntent().getStringExtra("screenname");

        actionBar.setTitle(getIntent().getStringExtra("name"));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        new GetList().execute();

        charRemaining.setText(140 - composeBar.getText().length() + "");
        composeBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                countHandler.removeCallbacks(getCount);
                countHandler.postDelayed(getCount, 300);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = composeBar.getText().toString();
                if (Integer.parseInt(charRemaining.getText().toString()) >= 0) {
                    new SendDirectMessage().execute(status);
                    composeBar.setText("");
                    attachImage.setVisibility(View.GONE);
                    Toast.makeText(context, getString(R.string.sending), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, getString(R.string.tweet_to_long), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Utils.setActionBar(context);
    }

    public void setUpWindow() {

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        params.dimAmount = .75f;  // set it higher if you want to dim behind the window
        getWindow().setAttributes(params);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .9), (int) (height * .8));
        } else {
            getWindow().setLayout((int) (width * .7), (int) (height * .8));
        }

    }

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }

    class GetList extends AsyncTask<String, Void, Cursor> {

        protected Cursor doInBackground(String... urls) {
            try {
                Cursor cursor = DMDataSource.getInstance(context)
                        .getConvCursor(listName, settings.currentAccount);
                return cursor;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Cursor cursor) {

            if (cursor != null) {
                Cursor c = null;
                try {
                    c = cursorAdapter.getCursor();
                } catch (Exception e) {

                }
                cursorAdapter = new TimeLineCursorAdapter(context, cursor, true);
                try {
                    listView.setAdapter(cursorAdapter);
                } catch (Exception e) {
                    // database is closed
                    try {
                        DMDataSource.getInstance(context).close();
                    } catch (Exception x) {

                    }
                    new GetList().execute();
                    return;
                }
                listView.setVisibility(View.VISIBLE);
                listView.setStackFromBottom(true);

                try {
                    c.close();
                } catch (Exception e) {

                }
            }

            LinearLayout spinner = (LinearLayout) findViewById(R.id.list_progress);
            spinner.setVisibility(View.GONE);
        }
    }

    public TimeLineCursorAdapter cursorAdapter;

    class SendDirectMessage extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected Boolean doInBackground(String... args) {
            String status = args[0];
            try {
                Twitter twitter = Utils.getTwitter(getApplicationContext(), settings);

                if (!attachedUri.equals("")) {
                    try {
                        File outputDir = context.getCacheDir();
                        File f = File.createTempFile("compose", "picture", outputDir);

                        Bitmap bitmap = getBitmapToSend(Uri.parse(attachedUri));
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        byte[] bitmapdata = bos.toByteArray();

                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();

                        // we wont attach any text to this image at least, since it is a direct message
                        TwitPicHelper helper = new TwitPicHelper(twitter, " ", f, context);
                        String url = helper.uploadForUrl();

                        status += " " + url;
                    } catch (Exception e) {
                        Toast.makeText(context, getString(R.string.error_attaching_image), Toast.LENGTH_SHORT).show();
                    }

                }

                String sendTo = listName;

                twitter4j.DirectMessage message = twitter.sendDirectMessage(sendTo, status);

                if (!settings.pushNotifications) {
                    DMDataSource.getInstance(context).createDirectMessage(message, settings.currentAccount);
                }

                sharedPrefs.edit().putLong("last_direct_message_id_" + sharedPrefs.getInt("current_account", 1), message.getId()).commit();
                sharedPrefs.edit().putBoolean("refresh_me_dm", true).commit();

                return true;

            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return false;
        }

        protected void onPostExecute(Boolean sent) {
            // dismiss the dialog after getting all products

            if (sent) {
                Toast.makeText(getBaseContext(),
                        getApplicationContext().getResources().getString(R.string.direct_message_sent),
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.error),
                        Toast.LENGTH_SHORT)
                        .show();
            }

            context.sendBroadcast(new Intent("com.klinker.android.twitter.UPDATE_DM"));
        }

    }

    public BroadcastReceiver updateConv = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new GetList().execute();
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.twitter.UPDATE_DM");
        filter.addAction("com.klinker.android.twitter.NEW_DIRECT_MESSAGE");
        context.registerReceiver(updateConv, filter);
    }

    @Override
    public void onPause() {
        try {
            context.unregisterReceiver(updateConv);
        } catch (Exception e) {

        }

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dm_conversation, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_attach_picture:
                // if they haven't seen the disclaimer, show it to them
                if (!sharedPrefs.getBoolean("knows_twitpic_dm_warning", false)) {
                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.twitpic_disclaimer))
                            .setMessage(getResources().getString(R.string.twitpic_disclaimer_summary))
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    attachImage();
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNeutralButton(R.string.dont_show_again, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sharedPrefs.edit().putBoolean("knows_twitpic_dm_warning", true).commit();
                                    attachImage();
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();
                } else {
                    // they know and don't want to see the disclaimer again
                    attachImage();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ImageView attachImage;
    public String attachedUri = "";

    public static final int SELECT_PHOTO = 100;
    public static final int CAPTURE_IMAGE = 101;
    public static final int PWICCER = 420;

    public boolean pwiccer = false;

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        Uri selectedImage = imageReturnedIntent.getData();

                        String filePath = IOUtils.getPath(selectedImage, context);

                        Log.v("talon_compose_pic", "path to image on sd card: " + filePath);

                        try {
                            attachImage.setImageBitmap(getBitmapToSend(selectedImage));
                            attachImage.setVisibility(View.VISIBLE);
                            attachedUri = selectedImage.toString();
                            new Handler().post(getCount);
                        } catch (FileNotFoundException e) {
                            Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                        } catch (IOException e) {
                            Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Uri selectedImage = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Talon/", "photoToTweet.jpg"));

                        try {
                            attachImage.setImageBitmap(getBitmapToSend(selectedImage));
                            attachImage.setVisibility(View.VISIBLE);
                            attachedUri = selectedImage.toString();
                            new Handler().post(getCount);
                        } catch (FileNotFoundException e) {
                            Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                        } catch (IOException e) {
                            Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                        }

                    } catch (Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public void attachImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(R.array.attach_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if(item == 0) { // take picture
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory() + "/Talon/", "photoToTweet.jpg");

                    if (!f.exists()) {
                        try {
                            f.getParentFile().mkdirs();
                            f.createNewFile();
                        } catch (IOException e) {

                        }
                    }

                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(captureIntent, CAPTURE_IMAGE);
                } else { // attach picture
                    if (attachedUri == null || attachedUri.equals("")) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                    } else {
                        attachedUri = "";
                        attachImage.setImageDrawable(null);
                        attachImage.setVisibility(View.GONE);
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                    }
                }
            }
        });

        builder.create().show();
    }

    private Bitmap getBitmapToSend(Uri uri) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > 1000) ? (originalSize / 1000) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

}