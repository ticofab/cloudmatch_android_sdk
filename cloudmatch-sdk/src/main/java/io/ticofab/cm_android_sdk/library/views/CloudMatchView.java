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

package io.ticofab.cm_android_sdk.library.views;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.View;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import io.ticofab.cm_android_sdk.library.consts.Criteria;
import io.ticofab.cm_android_sdk.library.consts.ServerConsts;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotConnectedException;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotInitializedException;
import io.ticofab.cm_android_sdk.library.handlers.ServerMessagesHandler;
import io.ticofab.cm_android_sdk.library.helpers.Connector;
import io.ticofab.cm_android_sdk.library.helpers.Matcher;
import io.ticofab.cm_android_sdk.library.helpers.StringHelper;
import io.ticofab.cm_android_sdk.library.helpers.UniqueIDHelper;
import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchEventListener;
import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchViewInterface;
import io.ticofab.cm_android_sdk.library.interfaces.LocationProvider;
import io.ticofab.cm_android_sdk.library.listeners.CloudMatchListener;
import io.ticofab.cm_android_sdk.library.models.inputs.DeliveryInput;

/**
 * Abstract custom view providing the basic functionality of GestureMatch views.
 */
public abstract class CloudMatchView extends View {

    // the WebSocket client object, used in all network operations
    WebSocket mWSClient;

    // we need a reference to an activity for various things
    Activity mActivity;

    // the client-provided implementation of OnCloudMatchEvent
    CloudMatchEventListener mListener;

    // helper to match devices
    Matcher mMatcher;

    // the interface for the client to control his view
    CloudMatchViewInterface mClientInterface;

    // pinch or swipe?
    Criteria mCriteria;

    public CloudMatchView(final Context context) {
        super(context);
    }

    public CloudMatchView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Initializes the CloudMatch. Always call it at the beginning of your application.
     *
     * @param activity         Provides the context where the application runs.
     * @param clientListener   Implementation of the OnCloudMatchEvent interface. Will be used to notify any network communication.
     * @param locationProvider Implementation of LocationProvider, needs to serve coordinates when needed
     * @param clientInterface  Implementation of the CloudMatchViewInterface
     * @throws PackageManager.NameNotFoundException
     */
    public void connectCloudMatch(final Activity activity,
                                  final CloudMatchEventListener clientListener,
                                  final LocationProvider locationProvider,
                                  final CloudMatchViewInterface clientInterface)
            throws PackageManager.NameNotFoundException {
        mActivity = activity;
        mListener = clientListener;
        mClientInterface = clientInterface;

        if (mActivity == null) {
            throw new CloudMatchNotInitializedException("Activity cannot be null.");
        }

        // initialize socket
        final ServerMessagesHandler myMessagesHandler = new ServerMessagesHandler(mActivity, mListener);
        final CloudMatchListener myListener = new CloudMatchListener(mActivity, myMessagesHandler, mListener);

        Connector connector = new Connector(mActivity, StringHelper.getApiKey(mActivity), StringHelper.getAppId(mActivity));
        final URI uri;
        try {
            uri = connector.getConnectionUri();

            AsyncHttpClient.getDefaultInstance().websocket(
                    uri.toString(),
                    "my-protocol",
                    new AsyncHttpClient.WebSocketConnectCallback() {
                        @Override
                        public void onCompleted(Exception ex, WebSocket webSocket) {
                            if (ex == null) {
                                mWSClient = webSocket;
                                mWSClient.setClosedCallback(myListener.getClosedCallback());
                                mWSClient.setEndCallback(myListener.getEndCallback());
                                mWSClient.setDataCallback(myListener.getDataCallback());
                                mWSClient.setStringCallback(myListener.getStringCallback());
                                mWSClient.setWriteableCallback(myListener.getWritabledCallback());
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mListener.onConnectionOpen();
                                    }
                                });
                                mMatcher = new Matcher(mWSClient, locationProvider);
                            } else {
                                mListener.onConnectionError(ex);
                            }

                        }
                    });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delivers payload to all the members of the provided group.
     *
     * @param payload The delivery payload.
     * @param groupId The group to which send the payload.
     * @param tag     Optional tag for this delivery. Can be null.
     * @throws CloudMatchNotConnectedException
     */
    public void deliverPayloadToGroup(final String payload,
                                      final String groupId,
                                      final String tag)
            throws CloudMatchNotConnectedException {
        deliverPayload(null, payload, groupId, tag);
    }

    /**
     * Delivers payload to one or more devices (recipients) that are part of the provided group.
     *
     * @param recipients The devices that will receive this payload
     * @param payload    The payload to deliver
     * @param groupId    The group id
     * @throws CloudMatchNotConnectedException
     */
    // TODO: this should forward to a "deliverer" or something
    public void deliverPayload(final ArrayList<Integer> recipients,
                               final String payload,
                               final String groupId,
                               final String tag) throws CloudMatchNotConnectedException {
        if (mWSClient == null || !mWSClient.isOpen()) {
            throw new CloudMatchNotConnectedException();
        }

        final ArrayList<String> chunks = StringHelper.splitEqually(payload, ServerConsts.MAX_DELIVERY_CHUNK_SIZE);

        final DeliveryInput deliveryInput = new DeliveryInput();
        final String deliveryId = UniqueIDHelper.createDeliveryId();
        deliveryInput.mRecipients = recipients;
        deliveryInput.mGroupId = groupId;
        deliveryInput.mTag = tag;
        deliveryInput.mDeliveryId = deliveryId;

        final int totalChunks = chunks.size();

        for (int i = 0; i < totalChunks; i++) {
            deliveryInput.mPayload = chunks.get(i);
            deliveryInput.mChunkNr = i;
            deliveryInput.mTotalChunks = totalChunks;

            try {
                final String jsonToSend = deliveryInput.toJsonStr();
                mWSClient.send(jsonToSend);
                mActivity.runOnUiThread(new DeliveryProgressRunnable(tag, deliveryId, totalChunks, i, mListener));
            } catch (final JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static class DeliveryProgressRunnable implements Runnable {
        String mTag;
        int mTotalChunks;
        int mCurrentChunk;
        String mDeliveryId;
        CloudMatchEventListener mListener;

        public DeliveryProgressRunnable(final String tag,
                                        final String deliveryId,
                                        final int totalChunks,
                                        final int currentChunk,
                                        final CloudMatchEventListener listener) {
            mTag = tag;
            mListener = listener;
            mDeliveryId = deliveryId;
            mTotalChunks = totalChunks;
            mCurrentChunk = currentChunk;
        }

        @Override
        public void run() {
            mListener.onDeliveryProgress(mTag, mDeliveryId, 100 * (mCurrentChunk + 1) / mTotalChunks);
        }
    }

    /**
     * Closes the connection with the server, disconnecting the WebSocketClient object.
     */
    public void closeConnection() {
        if (mWSClient != null && mWSClient.isOpen()) {
            mWSClient.close();
        }
    }

}
