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

public class ScheduledTweet {
    public String text;
    public int alarmId;
    public long time;
    public int account;

    public ScheduledTweet(String text, int alarmId, long time, int account) {
        this.text = text;
        this.alarmId = alarmId;
        this.time = time;
        this.account = account;
    }
}
