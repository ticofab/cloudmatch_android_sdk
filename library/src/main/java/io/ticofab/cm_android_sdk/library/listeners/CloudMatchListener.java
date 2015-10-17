/*
 * Copyright 2014 CloudMatch.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ticofab.cm_android_sdk.library.listeners;

import android.app.Activity;

import com.codebutler.android_websockets.WebSocketClient;

import io.ticofab.cm_android_sdk.library.interfaces.OnCloudMatchEvent;
import io.ticofab.cm_android_sdk.library.interfaces.OnServerMessage;

/**
 * SDK's implementation of the WebSocket Listener interface to receive messages from the backend.
 */
public class CloudMatchListener implements WebSocketClient.Listener {

    public CloudMatchListener(final Activity activity, final OnCloudMatchEvent clientListener,
                              final OnServerMessage messageHandler) {
        mServerMessageHandler = messageHandler;
        mOnCloudMatchEvent = clientListener;
        mActivity = activity;
    }

    private final OnServerMessage mServerMessageHandler;
    private final OnCloudMatchEvent mOnCloudMatchEvent;
    private final Activity mActivity;

    @Override
    public void onMessage(final byte[] data) {
        onMessage(new String(data));
    }

    @Override
    public void onMessage(final String message) {
        mServerMessageHandler.onServerMessage(message);
    }

    @Override
    public void onError(final Exception error) {
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mOnCloudMatchEvent.onConnectionError(error);
            }
        });
    }

    @Override
    public void onDisconnect(final int code, final String reason) {
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mOnCloudMatchEvent.onConnectionClosed();
            }
        });
    }

    @Override
    public void onConnect() {
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mOnCloudMatchEvent.onConnectionOpen();
            }
        });
    }
};
