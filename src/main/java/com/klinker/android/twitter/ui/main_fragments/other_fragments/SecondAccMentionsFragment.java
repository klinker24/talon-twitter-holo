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

package com.klinker.android.twitter.ui.main_fragments.other_fragments;

import com.klinker.android.twitter.utils.Utils;
import twitter4j.Twitter;

public class SecondAccMentionsFragment extends MentionsFragment {

    @Override
    public int getCurrentAccount() {
        if (sharedPrefs.getInt("current_account", 1) == 1) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public Twitter getTwitter() {
        return Utils.getSecondTwitter(context);
    }
}
