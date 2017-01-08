package com.klinker.android.twitter.utils;

import android.app.Activity;
import android.graphics.Point;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListPopupWindow;

import com.klinker.android.twitter.adapters.AutoCompleteUserArrayAdapter;
import com.klinker.android.twitter.settings.AppSettings;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.User;

public class UserAutoCompleteHelper {

    public interface Callback {
        void onUserSelected(User selectedUser);
    }

    private Activity context;
    private Handler handler;
    private ListPopupWindow userAutoComplete;
    private AutoCompleteHelper autoCompleter;
    private Callback callback;

    private List<User> users = new ArrayList<>();

    public static UserAutoCompleteHelper applyTo(Activity activity, EditText tv) {
        UserAutoCompleteHelper helper = new UserAutoCompleteHelper(activity);
        helper.on(tv);

        return helper;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private UserAutoCompleteHelper(Activity activity) {
        this.handler = new Handler();
        this.context = activity;
        this.autoCompleter = new AutoCompleteHelper();

        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        userAutoComplete = new ListPopupWindow(context);
        userAutoComplete.setHeight(Utils.toDP(200, context));
        userAutoComplete.setWidth((int)(width * .75));
        userAutoComplete.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
    }

    private void on(final EditText textView) {
        userAutoComplete.setAnchorView(textView);

        textView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String tvText = textView.getText().toString();

                try {
                    int position = textView.getSelectionStart() - 1;
                    if (tvText.charAt(position) == '@') {
                        userAutoComplete.show();
                    } else if (!tvText.contains("@")) {
                        userAutoComplete.dismiss();
                    } else if (userAutoComplete.isShowing()) {
                        String searchText = "";

                        do {
                            searchText = tvText.charAt(position--) + searchText;
                        } while (tvText.charAt(position) != '@');

                        searchText = searchText.replace("@", "");
                        search(searchText);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    userAutoComplete.dismiss();
                }
            }
        });

        userAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                userAutoComplete.dismiss();
                autoCompleter.completeTweet(textView, users.get(i).getScreenName(), '@');

                if (callback != null) {
                    callback.onUserSelected(users.get(i));
                }
            }
        });
    }

    private void search(final String screenName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Twitter twitter = Utils.getTwitter(context, AppSettings.getInstance(context));

                try {
                    users = twitter.searchUsers("@" + screenName, 0);
                } catch (Exception e) { }

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userAutoComplete.setAdapter(new AutoCompleteUserArrayAdapter(context, users));
                    }
                });
            }
        }).start();
    }
}