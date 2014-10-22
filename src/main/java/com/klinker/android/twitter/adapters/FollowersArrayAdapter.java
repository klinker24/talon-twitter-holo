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

package com.klinker.android.twitter.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import twitter4j.User;

public class FollowersArrayAdapter extends PeopleArrayAdapter {

    ArrayList<Long> followingIds;

    public FollowersArrayAdapter(Context context, ArrayList<User> users, ArrayList<Long> followingIds) {
        super(context, users);

        this.followingIds = followingIds;

        Log.v("talon_followers", followingIds.size() + " followers");
    }

    @Override
    public void setFollowingStatus(ViewHolder holder, User u) {
        if (holder.following != null) {
            Log.v("talon_followers", "checking follow status for: " + u.getName());
            Long l = u.getId();
            if (followingIds.contains(l)) {
                holder.following.setVisibility(View.VISIBLE);
                Log.v("talon_followers", "i am following this person");
            } else {
                holder.following.setVisibility(View.GONE);
                Log.v("talon_followers", "i am not following this person");
            }
        }
    }
}
