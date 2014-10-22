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

import android.app.Application;
import android.content.Context;

import com.klinker.android.twitter.utils.EmojiUtils;

import java.io.File;

import uk.co.senab.bitmapcache.BitmapLruCache;

public class App extends Application {
    private BitmapLruCache mCache;
    private BitmapLruCache profileCache;

    private static final int MEGA_BYTE = 1024 * 1024;

    @Override
    public void onCreate() {
        super.onCreate();

        File cacheDir = new File(getCacheDir(), "talon");
        cacheDir.mkdirs();

        File proCacheDir = new File(getCacheDir(), "talon-profile");
        proCacheDir.mkdirs();

        BitmapLruCache.Builder builder = new BitmapLruCache.Builder();
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize(.25f);
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheDir).setDiskCacheMaxSize(100 * MEGA_BYTE);

        mCache = builder.build();

        builder.setDiskCacheLocation(proCacheDir);

        profileCache = builder.build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                EmojiUtils.init(App.this);
            }
        }).start();

    }

    public BitmapLruCache getBitmapCache() {
        return mCache;
    }

    public BitmapLruCache getProfileCache() {
        return profileCache;
    }

    public static App getInstance(Context context) {
        return (App) context.getApplicationContext();
    }
}