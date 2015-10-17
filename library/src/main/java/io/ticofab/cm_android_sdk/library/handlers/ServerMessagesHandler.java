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

package io.ticofab.cm_android_sdk.library.handlers;

import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.ticofab.cm_android_sdk.library.consts.ErrorTypes;
import io.ticofab.cm_android_sdk.library.consts.JsonLabels;
import io.ticofab.cm_android_sdk.library.consts.Kinds;
import io.ticofab.cm_android_sdk.library.consts.MessageTypes;
import io.ticofab.cm_android_sdk.library.consts.ResponseTypes;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchConnectionException;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchInvalidCredentialException;
import io.ticofab.cm_android_sdk.library.interfaces.OnCloudMatchEvent;
import io.ticofab.cm_android_sdk.library.interfaces.OnServerMessage;
import io.ticofab.cm_android_sdk.library.models.messages.MatcheeDelivery;
import io.ticofab.cm_android_sdk.library.models.messages.MatcheeDeliveryMessage;
import io.ticofab.cm_android_sdk.library.models.messages.MatcheeLeftMessage;
import io.ticofab.cm_android_sdk.library.models.responses.DeliveryResponse;
import io.ticofab.cm_android_sdk.library.models.responses.LeaveGroupResponse;
import io.ticofab.cm_android_sdk.library.models.responses.MatchResponse;


/**
 * Deals with incoming messages from the backend. It interprets the string messages sent through the WebSocket
 * connection and then triggers the appropriate callback on the client side.
 */
public class ServerMessagesHandler implements OnServerMessage {
    private final OnCloudMatchEvent mServerEventsHandler;
    private final Activity mActivity;

    public ServerMessagesHandler(final Activity activity, final OnCloudMatchEvent serverEventHandler) {
        mServerEventsHandler = serverEventHandler;
        mActivity = activity;
    }

    // TODO: TEMP this needs to go
    final HashMap<String, ArrayList<String>> mPartialDeliveries = new HashMap<String, ArrayList<String>>();

    @Override
    public void onServerMessage(final String message) {

        try {
            final JSONObject msgJson = new JSONObject(message);
            if (!msgJson.has(JsonLabels.KIND)) {
                return;
            }

            if (!msgJson.has(JsonLabels.TYPE)) {
                return;
            }

            final Kinds kind = Kinds.valueOf(msgJson.getString(JsonLabels.KIND));

            switch (kind) {
                case response:
                    final ResponseTypes inputType = ResponseTypes.valueOf(msgJson.getString(JsonLabels.TYPE));
                    switch (inputType) {
                        case match:
                        case matchInGroup:
                            final MatchResponse matchR = MatchResponse.fromJson(msgJson);
                            mActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mServerEventsHandler.onMatchResponse(matchR);
                                }
                            });

                            break;
                        case leaveGroup:
                            final LeaveGroupResponse leaveGroupR = LeaveGroupResponse.fromJson(msgJson);
                            mActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mServerEventsHandler.onLeaveGroupResponse(leaveGroupR);
                                }
                            });
                            break;
                        case delivery:
                            final DeliveryResponse deliveryR = DeliveryResponse.fromJson(msgJson);
                            mActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mServerEventsHandler.onDeliveryResponse(deliveryR);
                                }
                            });
                            break;
                        default:
                            break;
                    }

                    break;

                case message:
                    final MessageTypes messageType = MessageTypes.valueOf(msgJson.getString(JsonLabels.TYPE));
                    switch (messageType) {
                        case delivery:
                            final MatcheeDeliveryMessage deliveryMsg = MatcheeDeliveryMessage.fromJson(msgJson);

                            // TODO: put all this in a separate something!
                            // deal with situation where one chunk is lost: something like, when one partial item is
                            // received, start a watch dog which will delete the whole thing and send a "failed" message to
                            // the client if it hasn't been completed in the meantime.

                            // here check: is this message part of a partial delivery?
                            if (deliveryMsg.mChunkNr == -1 && deliveryMsg.mTotalChunks != -1) {
                                final MatcheeDelivery delivery = new MatcheeDelivery();
                                delivery.mSenderMatchee = deliveryMsg.mSenderMatchee;
                                delivery.mPayload = deliveryMsg.mPayload;
                                delivery.mGroupId = deliveryMsg.mGroupId;
                                delivery.mTag = deliveryMsg.mTag;
                                mActivity.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mServerEventsHandler.onMatcheeDelivery(delivery);
                                    }
                                });
                            } else {
                                // here, put all chunks in one place and send a "progress indication" notification
                                final String id = deliveryMsg.mDeliveryId;
                                if (!mPartialDeliveries.containsKey(id)) {
                                    mPartialDeliveries.put(id, new ArrayList<String>(deliveryMsg.mTotalChunks));
                                }

                                final ArrayList<String> receivedChunks = mPartialDeliveries.get(id);
                                receivedChunks.add(deliveryMsg.mChunkNr, deliveryMsg.mPayload);
                                final String tag = deliveryMsg.mTag;

                                if (receivedChunks.size() == deliveryMsg.mTotalChunks) {
                                    mActivity.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            mServerEventsHandler.onMatcheeDeliveryProgress(tag, 100);
                                        }
                                    });

                                    // 1. remove object from hash map
                                    mPartialDeliveries.remove(id);

                                    // 2. send whole message to client
                                    String completePayload = "";
                                    for (final String chunk : receivedChunks) {
                                        completePayload += chunk;
                                    }
                                    final MatcheeDelivery delivery = new MatcheeDelivery();
                                    delivery.mSenderMatchee = deliveryMsg.mSenderMatchee;
                                    delivery.mPayload = completePayload;
                                    delivery.mGroupId = deliveryMsg.mGroupId;
                                    delivery.mTag = deliveryMsg.mTag;
                                    mActivity.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            mServerEventsHandler.onMatcheeDelivery(delivery);
                                        }
                                    });
                                } else {
                                    // 1. put object back into hash map
                                    mPartialDeliveries.put(id, receivedChunks);

                                    // 2. deliver progress message to client
                                    final int progress = 100 * receivedChunks.size() / deliveryMsg.mTotalChunks;
                                    mActivity.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            mServerEventsHandler.onMatcheeDeliveryProgress(tag, progress);
                                        }
                                    });
                                }
                            }

                            break;
                        case matcheeLeft:
                            final MatcheeLeftMessage leftMsg = MatcheeLeftMessage.fromJson(msgJson);
                            mActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mServerEventsHandler.onMatcheeLeft(leftMsg);
                                }
                            });
                            break;
                        default:
                            break;
                    }

                    break;

                case error:
                    final ErrorTypes errorType = ErrorTypes.valueOf(msgJson.getString(JsonLabels.TYPE));

                    switch (errorType) {
                        case server_error:
                            mActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mServerEventsHandler.onConnectionError(new CloudMatchConnectionException());
                                }
                            });
                            break;
                        case invalidCredentials:
                            mActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mServerEventsHandler.onConnectionError(new CloudMatchInvalidCredentialException());
                                }
                            });
                            break;
                        default:
                            break;
                    }

                    break;

                default:
                    // TODO: error, we shouldn't get here
                    break;
            }

        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
