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

package com.klinker.android.twitter.settings.configure_pages.fragments;

import com.klinker.android.twitter.settings.AppSettings;

public class PageOneFragment extends ChooserFragment {

    public static int type = AppSettings.PAGE_TYPE_NONE;
    public static long listId = 0;
    public static String listName = "";

    public PageOneFragment() {
        type = AppSettings.PAGE_TYPE_NONE;
        listId = 0;
        listName = "";
    }

    protected void setType(int type) {
        this.type = type;
    }
    protected void setId(long id) {
        this.listId = id;
    }
    protected void setListName(String listName) {
        this.listName = listName;
    }
}
