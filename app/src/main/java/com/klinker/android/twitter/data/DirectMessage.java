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

package com.klinker.android.twitter.data;

public class DirectMessage {

    public String name;
    public String screenname;
    public String message;
    public String proPic;

    public DirectMessage(String name, String screenname, String message, String proPic) {
        this.name = name;
        this.screenname = screenname;
        this.message = message;
        this.proPic = proPic;
    }

    public String getName() {
        return name;
    }

    public String getScreenname() {
        return screenname;
    }

    public String getMessage() {
        return message;
    }

    public String getPicture() {
        return proPic;
    }
}
