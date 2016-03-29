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

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.WritableCallback;
import com.koushikdutta.async.http.WebSocket;

import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchEventListener;
import io.ticofab.cm_android_sdk.library.interfaces.OnServerMessage;

/**
 * SDK's implementation of the WebSocket Listener interface to receive messages from the backend.
 */
public class CloudMatchListener {

    public CloudMatchListener(final Activity activity,
                              final OnServerMessage messageHandler,
                              final CloudMatchEventListener clientListener) {
        mActivity = activity;
        mServerMessageHandler = messageHandler;
        mCloudMatchEventListener = clientListener;
    }

    final Activity mActivity;
    final OnServerMessage mServerMessageHandler;
    final CloudMatchEventListener mCloudMatchEventListener;

    public CompletedCallback getClosedCallback() {
        return mClosedCallback;
    }

    public CompletedCallback getEndCallback() {
        return mEndCallback;
    }

    public WebSocket.StringCallback getStringCallback() {
        return mStringCallback;
    }

    public DataCallback getDataCallback() {
        return mDataCallback;
    }

    public WritableCallback getWritabledCallback() {
        return mWritableCallback;
    }

    final CompletedCallback mClosedCallback = new CompletedCallback() {
        @Override
        public void onCompleted(final Exception ex) {
            mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mCloudMatchEventListener.onConnectionClosed();
                }
            });
        }
    };

    final CompletedCallback mEndCallback = new CompletedCallback() {
        @Override
        public void onCompleted(final Exception ex) {
            mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mCloudMatchEventListener.onConnectionError(ex);
                }
            });
        }
    };

    final WebSocket.StringCallback mStringCallback = new WebSocket.StringCallback() {
        @Override
        public void onStringAvailable(String s) {
            mServerMessageHandler.onServerMessage(s);
        }
    };

    final DataCallback mDataCallback = new DataCallback() {
        @Override
        public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {

        }
    };

    final WritableCallback mWritableCallback = new WritableCallback() {
        @Override
        public void onWriteable() {

        }
    };

};
