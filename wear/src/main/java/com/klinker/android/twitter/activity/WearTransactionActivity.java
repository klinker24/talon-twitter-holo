/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.twitter.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;
import com.klinker.android.twitter.transaction.KeyProperties;
import com.klinker.android.twitter.util.IoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class WearTransactionActivity extends Activity implements
        MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WearActivity";

    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences sharedPreferences;

    private ArrayList<String> titles;
    private ArrayList<String> bodies;
    private ArrayList<String> ids;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(KeyProperties.PATH)) {
            final DataMap map = DataMap.fromByteArray(messageEvent.getData());

            if (map.containsKey(KeyProperties.KEY_TITLE)) {
                titles = map.getStringArrayList(KeyProperties.KEY_TITLE);
                bodies = map.getStringArrayList(KeyProperties.KEY_TWEET);
                ids = map.getStringArrayList(KeyProperties.KEY_ID);
                sharedPreferences.edit()
                        .putInt(KeyProperties.KEY_PRIMARY_COLOR, map.getInt(KeyProperties.KEY_PRIMARY_COLOR))
                        .putInt(KeyProperties.KEY_ACCENT_COLOR, map.getInt(KeyProperties.KEY_ACCENT_COLOR))
                        .commit();

                Log.v(TAG, "found " + titles.size() + " tweets");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateDisplay();
                    }
                });
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] imageData = map.getByteArray(KeyProperties.KEY_IMAGE_DATA);
                        String imageName = map.getString(KeyProperties.KEY_IMAGE_NAME);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        File file = new File(getCacheDir(), imageName);
                        IoUtils utils = new IoUtils();
                        try {
                            utils.cacheBitmap(bitmap, file);
                        } catch (Exception e) {
                            Log.v(TAG, "error caching bitmap", e);
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String node : getNodes()) {
                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node, KeyProperties.PATH, KeyProperties.GET_DATA_MESSAGE.getBytes());
                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.v(TAG, "sent message " + sendMessageResult.toString());
                        }
                    });
                }
            }
        }).start();
    }

    private HashSet<String> getNodes() {
        final HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    public void sendReadStatus(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "marking " + id + " as read");
                String message = KeyProperties.MARK_READ_MESSAGE + KeyProperties.DIVIDER + id;
                for (String node : getNodes()) {
                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node, KeyProperties.PATH, message.getBytes());
                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.v(TAG, "sent message " + sendMessageResult.toString());
                        }
                    });
                }
            }
        }).start();
    }

    public void sendImageRequest(final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "sending image request for " + name);
                String message = KeyProperties.REQUEST_IMAGE + KeyProperties.DIVIDER + name;
                for (String node : getNodes()) {
                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node, KeyProperties.PATH, message.getBytes());
                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.v(TAG, "sent message " + sendMessageResult.toString());
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            mGoogleApiClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "error connecting to google api client", e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "connection suspended from API client");
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "connection to API client failed: " + connectionResult);
    }

    public ArrayList<String> getTitles() {
        return this.titles;
    }

    public ArrayList<String> getBodies() {
        return this.bodies;
    }

    public ArrayList<String> getIds() {
        return this.ids;
    }

    public abstract void updateDisplay();

}
