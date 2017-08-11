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

package com.klinker.android.twitter.activities.compose;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.klinker.android.twitter.BuildConfig;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.activities.GiphySearch;
import com.klinker.android.twitter.utils.UserAutoCompleteHelper;
import com.klinker.android.twitter.views.text.HoloEditText;

import java.io.File;
import java.io.IOException;

public class ComposeDMActivity extends Compose {

    public void setUpLayout() {
        isDM = true;

        setContentView(R.layout.compose_dm_activity);

        setUpSimilar();

        charRemaining.setVisibility(View.GONE);

        contactEntry = (EditText) findViewById(R.id.contact_entry);
        contactEntry.setVisibility(View.VISIBLE);

        String screenname = getIntent().getStringExtra("screenname");

        if (screenname != null) {
            contactEntry.setText("@" + screenname);
            contactEntry.setSelection(contactEntry.getText().toString().length());
        }
        UserAutoCompleteHelper.applyTo(this, contactEntry);

        if (settings.addonTheme) {
            try {
                Resources resourceAddon = context.getPackageManager().getResourcesForApplication(settings.addonThemePackage);
                int back = resourceAddon.getIdentifier("reply_entry_background", "drawable", settings.addonThemePackage);
                contactEntry.setBackgroundDrawable(resourceAddon.getDrawable(back));
            } catch (Exception e) {
                // theme does not include a reply entry box
            }
        }

        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachImage();
            }
        });


        if (!settings.useEmoji) {
            emojiButton.setVisibility(View.GONE);
        } else {
            emojiKeyboard.setAttached((HoloEditText) reply);

            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (emojiKeyboard.isShowing()) {
                        emojiKeyboard.setVisibility(false);

                        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.emoji_button_changing});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        emojiButton.setImageResource(resource);
                    }
                }
            });

            emojiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (emojiKeyboard.isShowing()) {
                        emojiKeyboard.setVisibility(false);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager imm = (InputMethodManager)getSystemService(
                                        INPUT_METHOD_SERVICE);
                                imm.showSoftInput(reply, 0);
                            }
                        }, 250);

                        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.emoji_button_changing});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        emojiButton.setImageResource(resource);
                    } else {
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(reply.getWindowToken(), 0);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                emojiKeyboard.setVisibility(true);
                            }
                        }, 250);

                        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.keyboard_button_changing});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        emojiButton.setImageResource(resource);
                    }
                }
            });
        }

        if (contactEntry.getText().toString().length() != 0) {
            reply.requestFocus();
        }

    }

    public void setUpReplyText() {

    }

    public boolean doneClick() {
        if (emojiKeyboard.isShowing()) {
            emojiKeyboard.setVisibility(false);

            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.emoji_button});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            emojiButton.setImageResource(resource);
        }

        EditText editText = (EditText) findViewById(R.id.tweet_content);
        String status = editText.getText().toString();

        if(oneUser(contactEntry.getText().toString())) {
            sendStatus(status);
            return true;
        }  else {
            Toast.makeText(this, R.string.one_recepient, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean oneUser(String s) {
        // check whether or not there is only one occurance of "@"
        int counter = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '@') {
                counter++;
            }
        }

        return counter == 1;
    }

    private void sendStatus(String status) {
        new SendDirectMessage().execute(status);
    }

    public ImageView attachImage;
    public String attachedUri = "";

    public static final int SELECT_PHOTO = 100;
    public static final int CAPTURE_IMAGE = 101;

    public boolean pwiccer = false;

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
                            Toast.makeText(ComposeDMActivity.this, "Have you given Talon the storage permission?", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ComposeDMActivity.this, "GIFs must be less than 5 MB", Toast.LENGTH_SHORT).show();

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

}
