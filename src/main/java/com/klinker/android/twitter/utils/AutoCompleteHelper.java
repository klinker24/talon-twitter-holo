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

package com.klinker.android.twitter.utils;

import android.widget.EditText;

public class AutoCompleteHelper {

    public String completeTweet(EditText text, String completeTo, char type) {
        String tweetText = text.getText().toString();
        int position = text.getSelectionStart() - 1;
        int endPosition = text.getSelectionStart();
        int startPosition = position;

        if (tweetText.length() == 1) {
            startPosition = 0;
            endPosition = 1;
        } else {
             try {
                 while (tweetText.charAt(position) != type) {
                     startPosition = position--;
                 }
             } catch (Exception e) {
                 // don't know why
             }
        }

        String textPart1 = tweetText.substring(0, startPosition);
        String textPart3 = tweetText.substring(endPosition, tweetText.length());
        boolean space = !textPart3.equals("") && !textPart3.startsWith(" ");
        String textPart2 = completeTo + (space ? " " : "");
        if (!textPart2.startsWith(type + "")) {
            textPart2 = type + textPart2;
        }
        String result = (textPart1 + textPart2 + textPart3).replace(type + "" + type, type + "");
        text.setText(result);

        try {
            text.setSelection(textPart1.length() + textPart2.length() - (space ? 1 : 0));
        } catch (Exception e) {
            text.setSelection(text.length());
        }

        return result;
    }

}
