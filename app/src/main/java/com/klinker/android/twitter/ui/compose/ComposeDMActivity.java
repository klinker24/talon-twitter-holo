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

package com.klinker.android.twitter.ui.compose;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.adapters.AutoCompletePeopleAdapter;
import com.klinker.android.twitter.data.sq_lite.FollowersDataSource;
import com.klinker.android.twitter.manipulations.widgets.HoloEditText;
import com.klinker.android.twitter.manipulations.QustomDialogBuilder;

import java.io.File;
import java.io.IOException;

public class ComposeDMActivity extends Compose {

    @Override
    public void onDestroy() {
        try {
            ((AutoCompletePeopleAdapter) userAutoComplete.getListView().getAdapter()).getCursor().close();
        } catch (Exception e) {

        }
        super.onDestroy();
    }

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

        userAutoComplete = new ListPopupWindow(context);
        userAutoComplete.setAnchorView(contactEntry);
        userAutoComplete.setHeight(toDP(200));
        userAutoComplete.setWidth(toDP(275));
        userAutoComplete.setAdapter(new AutoCompletePeopleAdapter(context,
                FollowersDataSource.getInstance(context).getCursor(currentAccount, contactEntry.getText().toString()), contactEntry));
        userAutoComplete.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);

        userAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                userAutoComplete.dismiss();
            }
        });

        contactEntry.addTextChangedListener(new TextWatcher() {

            int length = 0;
            int lastLength = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String searchText = contactEntry.getText().toString();
                lastLength = length;
                length = searchText.length();

                try {
                    if (!userAutoComplete.isShowing()) {
                        userAutoComplete.show();
                    } else if (length > lastLength + 1 || length == 0) {
                        userAutoComplete.dismiss();
                    } else if (userAutoComplete.isShowing()) {
                        String[] split = searchText.split(" ");
                        String adapterText;
                        if (split.length > 1) {
                            adapterText = split[split.length - 1];
                        } else {
                            adapterText = split[0];
                        }
                        adapterText = adapterText.replace("@", "");
                        userAutoComplete.setAdapter(new AutoCompletePeopleAdapter(context,
                                FollowersDataSource.getInstance(context).getCursor(currentAccount, adapterText), contactEntry));
                    }
                } catch (Exception e) {
                    // there is no text
                    try {
                        userAutoComplete.dismiss();
                    } catch (Exception x) {
                        // that's weird...
                    }
                }

            }
        });

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
                if (imagesAttached > 0 && !sharedPrefs.getBoolean("know_twitpic_for_mult_attach", false)) {
                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.twitpic_disclaimer))
                            .setMessage(getResources().getString(R.string.twitpic_disclaimer_multi_summary))
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
                                    sharedPrefs.edit().putBoolean("know_twitpic_for_mult_attach", true).commit();
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
                    attachImage();
                }

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
                        Intent photoPickerIntent = new Intent();
                        photoPickerIntent.setType("image/*");
                        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                        try {
                            startActivityForResult(Intent.createChooser(photoPickerIntent,
                                    "Select Picture"), SELECT_PHOTO);
                        } catch (Throwable t) {
                            // no app to preform this..? hmm, tell them that I guess
                            Toast.makeText(context, "No app available to select pictures!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        attachedUri = "";
                        attachImage.setImageDrawable(null);
                        attachImage.setVisibility(View.GONE);
                        Intent photoPickerIntent = new Intent();
                        photoPickerIntent.setType("image/*");
                        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                        try {
                            startActivityForResult(Intent.createChooser(photoPickerIntent,
                                    "Select Picture"), SELECT_PHOTO);
                        } catch (Throwable t) {
                            // no app to preform this..? hmm, tell them that I guess
                            Toast.makeText(context, "No app available to select pictures!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        builder.create().show();
    }

}
