package com.klinker.android.twitter.ui.compose;

import android.content.Intent;
import android.util.Log;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.manipulations.widgets.HoloTextView;
import com.klinker.android.twitter.manipulations.widgets.NetworkedCacheableImageView;

public class ComposeSecAccActivity extends ComposeActivity {

    public void setUpReplyText() {

        useAccOne = false;
        useAccTwo = true;

        NetworkedCacheableImageView pic = (NetworkedCacheableImageView) findViewById(R.id.profile_pic);
        HoloTextView currentName = (HoloTextView) findViewById(R.id.current_name);
        if (settings.roundContactImages) {
            pic.loadImage(settings.secondProfilePicUrl, false, null, NetworkedCacheableImageView.CIRCLE);
        } else {
            pic.loadImage(settings.secondProfilePicUrl, false, null);
        }
        currentName.setText("@" + settings.secondScreenName);

        // for failed notification
        if (!sharedPrefs.getString("draft", "").equals("")) {
            reply.setText(sharedPrefs.getString("draft", ""));
            reply.setSelection(reply.getText().length());
        }

        String to = getIntent().getStringExtra("user") + (isDM ? "" : " ");

        if ((!to.equals("null ") && !isDM) || (isDM && !to.equals("null"))) {
            if(!isDM) {
                Log.v("username_for_noti", "to place: " + to);
                reply.setText(to);
                reply.setSelection(reply.getText().toString().length());
            } else {
                contactEntry.setText(to);
                reply.requestFocus();
            }

            sharedPrefs.edit().putString("draft", "").commit();
        }

        notiId = getIntent().getLongExtra("id", 0);
        replyText = getIntent().getStringExtra("reply_to_text");

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        }
    }
}
