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

package com.klinker.android.twitter.activities;

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
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
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

import com.klinker.android.twitter.BuildConfig;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.activities.compose.Compose;
import com.klinker.android.twitter.adapters.CursorListLoader;
import com.klinker.android.twitter.adapters.TimeLineCursorAdapter;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.data.sq_lite.DMDataSource;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.views.text.HoloEditText;
import com.klinker.android.twitter.views.text.HoloTextView;
import com.klinker.android.twitter.activities.setup.LoginActivity;
import com.klinker.android.twitter.utils.IOUtils;
import com.klinker.android.twitter.utils.Utils;
import com.yalantis.ucrop.UCrop;

import org.lucasr.smoothie.AsyncListView;
import org.lucasr.smoothie.ItemManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.DirectMessageEvent;
import twitter4j.MessageData;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UploadedMedia;
import twitter4j.User;
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
                    charRemaining.setText(AppSettings.getInstance(context).tweetCharacterCount -
                            composeBar.getText().length() - (attachedUri.equals("") ? 0 : 23) + "");
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

                charRemaining.setText(AppSettings.getInstance(context).tweetCharacterCount - count + "");
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

        context = this;
        sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                0);
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

        charRemaining.setVisibility(View.GONE);

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

        charRemaining.setText(AppSettings.getInstance(this).tweetCharacterCount -
                composeBar.getText().length() + "");
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

                new SendDirectMessage().execute(status);
                composeBar.setText("");
                attachImage.setVisibility(View.GONE);
                Toast.makeText(context, getString(R.string.sending), Toast.LENGTH_SHORT).show();
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

                String sendTo = listName;
                User user = twitter.showUser(sendTo);
                MessageData data = new MessageData(user.getId(), status);

                if (!attachedUri.equals("")) {
                    try {
                        File f;

                        if (attachmentType == null) {
                            // image file
                            f = ImageUtils.scaleToSend(context, Uri.parse(attachedUri));
                        } else {
                            f = new File(URI.create(attachedUri));
                        }

                        UploadedMedia media = twitter.uploadMedia(f);
                        data.setMediaId(media.getMediaId());
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, getString(R.string.error_attaching_image), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }

                DirectMessageEvent message = twitter.createMessage(data);

                if (!settings.pushNotifications) {
                    DMDataSource.getInstance(context).createSentDirectMessage(message, user, settings, settings.currentAccount);
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
                attachImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ImageView attachImage;
    public String attachedUri = "";
    public String attachmentType = "";

    public static final int SELECT_PHOTO = 100;
    public static final int CAPTURE_IMAGE = 101;
    public static final int SELECT_GIF = 102;
    public static final int FIND_GIF = 104;

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        Log.v("talon_image_attach", "got the result, code: " + requestCode);
        switch (requestCode) {
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = UCrop.getOutput(imageReturnedIntent);

                        String filePath = IOUtils.getPath(selectedImage, context);
                        Log.v("talon_compose_pic", "path to gif on sd card: " + filePath);

                        try {
                            attachImage.setImageBitmap(getThumbnail(selectedImage));
                            attachImage.setVisibility(View.VISIBLE);
                            attachedUri = selectedImage.toString();
                        } catch (Throwable e) {
                            Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(imageReturnedIntent);
                    cropError.printStackTrace();
                }
                countHandler.post(getCount);
                break;
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    startUcrop(imageReturnedIntent.getData());
                }

                break;
            case CAPTURE_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Talon/", "photoToTweet.jpg"));
                    startUcrop(selectedImage);
                }

                break;
            case FIND_GIF:
            case SELECT_GIF:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = imageReturnedIntent.getData();

                        String filePath = IOUtils.getPath(selectedImage, context);

                        Log.v("talon_compose_pic", "path to gif on sd card: " + filePath);

                        attachImage.setImageBitmap(getThumbnail(selectedImage));
                        attachImage.setVisibility(View.VISIBLE);
                        attachedUri = selectedImage.toString();
                        attachmentType = "animated_gif";
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                }
                countHandler.post(getCount);
                break;
        }

        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
    }

    private void startUcrop(Uri sourceUri) {
        try {
            UCrop.Options options = new UCrop.Options();
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
            options.setCompressionQuality(90);

            File destination = File.createTempFile("ucrop", "jpg", getCacheDir());
            UCrop.of(sourceUri, Uri.fromFile(destination))
                    .withOptions(options)
                    .start(DirectMessageConversation.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void attachImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(R.array.attach_dm_options, new DialogInterface.OnClickListener() {
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

                    captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    try {
                        Uri photoURI = FileProvider.getUriForFile(context,
                                BuildConfig.APPLICATION_ID + ".provider", f);

                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(captureIntent, CAPTURE_IMAGE);
                    } catch (Exception e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Toast.makeText(DirectMessageConversation.this, "Have you given Talon the storage permission?", Toast.LENGTH_LONG).show();
                        }
                    }

                } else if (item == 1) { // attach picture
                    try {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO);
                    } catch (Exception e) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(Intent.createChooser(photoPickerIntent,
                                "Select Picture"), SELECT_PHOTO);
                    }
                } else if (item == 2) {
                    Toast.makeText(DirectMessageConversation.this, "GIFs must be less than 5 MB", Toast.LENGTH_SHORT).show();

                    try {
                        Intent gifIntent = new Intent();
                        gifIntent.setType("image/gif");
                        gifIntent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(gifIntent, SELECT_GIF);
                    } catch (Exception e) {
                        Intent gifIntent = new Intent();
                        gifIntent.setType("image/gif");
                        gifIntent.setAction(Intent.ACTION_PICK);
                        startActivityForResult(gifIntent, SELECT_GIF);
                    }
                } else if (item == 3) {
                    Intent gif = new Intent(context, GiphySearch.class);
                    startActivityForResult(gif, FIND_GIF);
                }
            }
        });

        builder.create().show();
    }


    private Bitmap getThumbnail(Uri uri) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);
        int reqWidth = 150;
        int reqHeight = 150;

        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = input.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = Compose.calculateInSampleSize(options, reqWidth,
                    reqHeight);
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap b = BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            if (!Compose.isAndroidN()) {
                ExifInterface exif = new ExifInterface(IOUtils.getPath(uri, context));
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                input.close();

                b = ImageUtils.cropSquare(b);
                return Compose.rotateBitmap(b, orientation);
            } else {
                input.close();
                b = ImageUtils.cropSquare(b);
                return b;
            }

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}