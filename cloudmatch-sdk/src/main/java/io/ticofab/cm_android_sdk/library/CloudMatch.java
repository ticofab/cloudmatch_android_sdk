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
package io.ticofab.cm_android_sdk.library;

import android.app.Activity;
import android.content.pm.PackageManager;

import com.codebutler.android_websockets.WebSocketClient;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.cm_android_sdk.library.consts.ServerConsts;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotConnectedException;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotInitializedException;
import io.ticofab.cm_android_sdk.library.exceptions.LocationServicesUnavailableException;
import io.ticofab.cm_android_sdk.library.handlers.ServerMessagesHandler;
import io.ticofab.cm_android_sdk.library.helpers.Connector;
import io.ticofab.cm_android_sdk.library.helpers.Matcher;
import io.ticofab.cm_android_sdk.library.helpers.StringHelper;
import io.ticofab.cm_android_sdk.library.helpers.UniqueIDHelper;
import io.ticofab.cm_android_sdk.library.interfaces.OnCloudMatchEvent;
import io.ticofab.cm_android_sdk.library.listeners.CloudMatchListener;
import io.ticofab.cm_android_sdk.library.location.LocationController;
import io.ticofab.cm_android_sdk.library.models.inputs.DeliveryInput;

public class CloudMatch {
    // the WebSocket client object, used in all network operations
    private static WebSocketClient mWSClient;

    // we need a reference to an activity for various things
    private static Activity mActivity;

    // the client-provided implementation of OnCloudMatchEvent
    private static OnCloudMatchEvent mListener;

    /**
     * Initializes the CloudMatch. Always call it at the beginning of your application.
     *
     * @param activity       Provides the context where the application runs.
     * @param clientListener Implementation of the OnCloudMatchEvent interface. Will be used to notify any network
     *                       communication.
     */
    public static void init(final Activity activity, final OnCloudMatchEvent clientListener) throws PackageManager.NameNotFoundException {
        mActivity = activity;
        mListener = clientListener;

        LocationController.init(mActivity);
        Connector.init(mActivity, StringHelper.getApiKey(activity), StringHelper.getAppId(activity));
        Matcher.init(mActivity);
    }

    /**
     * Call this in the onResume() method of your application.
     *
     * @throws CloudMatchNotInitializedException
     */
    public static void onResume() throws CloudMatchNotInitializedException {
        checkInitializationOrThrow();
        LocationController.connect();
    }

    /**
     * Call this in the onPause() method of your application.
     *
     * @throws CloudMatchNotInitializedException
     */
    public static void onPause() throws CloudMatchNotInitializedException {
        checkInitializationOrThrow();
        LocationController.disconnect();
    }

    /**
     * Connects the CloudMatch. You need to call init() first.
     *
     * @throws LocationServicesUnavailableException
     * @throws CloudMatchNotInitializedException
     */
    public static void connect() throws LocationServicesUnavailableException, CloudMatchNotInitializedException {

        checkInitializationOrThrow();
        LocationController.checkAvailabilityOrThrow(mActivity);

        if (mWSClient != null && mWSClient.isConnected()) {
            mWSClient.disconnect();
        }

        final ServerMessagesHandler myMessagesHandler = new ServerMessagesHandler(mActivity, mListener);
        final CloudMatchListener myListener = new CloudMatchListener(mActivity, mListener, myMessagesHandler);

        try {
            // initialize socket
            final List<BasicNameValuePair> extraHeaders = null;
            final URI uri = Connector.getConnectionUri();
            mWSClient = new WebSocketClient(uri, myListener, extraHeaders);
            mWSClient.connect();
            Matcher.setWebsocketClient(mWSClient);
        } catch (final URISyntaxException e) {
            // TODO: handle exception
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
    public static void deliverPayloadToGroup(final String payload,
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
    public static void deliverPayload(final ArrayList<Integer> recipients,
                                      final String payload,
                                      final String groupId,
                                      final String tag) throws CloudMatchNotConnectedException {
        checkWebsocketClientOrThrow();

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
                mActivity.runOnUiThread(new DeliveryProgressRunnable(tag, deliveryId, totalChunks, i));
            } catch (final JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static class DeliveryProgressRunnable implements Runnable {
        int mCurrentChunk;
        String mTag;
        String mDeliveryId;
        int mTotalChunks;

        public DeliveryProgressRunnable(final String tag, final String deliveryId, final int totalChunks, final int i) {
            mCurrentChunk = i;
            mTag = tag;
            mDeliveryId = deliveryId;
            mTotalChunks = totalChunks;
        }

        @Override
        public void run() {
            mListener.onDeliveryProgress(mTag, mDeliveryId, 100 * (mCurrentChunk + 1) / mTotalChunks);
        }
    }

    /**
     * Closes the connection with the server, disconnecting the WebSocketClient object.
     */
    public static void closeConnection() {
        LocationController.disconnect();
        if (mWSClient != null && mWSClient.isConnected()) {
            mWSClient.disconnect();
        }
    }

    /**
     * Throws an exception if the WebSocketClient object is null or not connected to a WebSocket endpoint.
     *
     * @throws CloudMatchNotConnectedException
     */
    private static void checkWebsocketClientOrThrow() throws CloudMatchNotConnectedException {
        if (mWSClient == null || !mWSClient.isConnected()) {
            throw new CloudMatchNotConnectedException();
        }
    }

    /**
     * Check if a context (Activity) has been set on the CloudMatch, and throws an exception otherwise.
     *
     * @throws CloudMatchNotInitializedException
     */
    private static void checkInitializationOrThrow() throws CloudMatchNotInitializedException {
        if (mActivity == null) {
            throw new CloudMatchNotInitializedException();
        }
    }
}
