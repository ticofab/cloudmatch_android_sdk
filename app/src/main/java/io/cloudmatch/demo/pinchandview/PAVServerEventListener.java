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

package io.cloudmatch.demo.pinchandview;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchConnectionException;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchInvalidCredentialException;
import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchEventListener;
import io.ticofab.cm_android_sdk.library.models.DeviceInScheme;
import io.ticofab.cm_android_sdk.library.models.PositionScheme;
import io.ticofab.cm_android_sdk.library.models.messages.MatcheeDelivery;
import io.ticofab.cm_android_sdk.library.models.messages.MatcheeLeftMessage;
import io.ticofab.cm_android_sdk.library.models.responses.DeliveryResponse;
import io.ticofab.cm_android_sdk.library.models.responses.LeaveGroupResponse;
import io.ticofab.cm_android_sdk.library.models.responses.MatchResponse;
import io.cloudmatch.demo.R;

/*
 * Implementation of the OnCloudMatchEvent interface in the CloudMatchSDK. It adds some logic for "internal"
 * communication within this demo.
 */
public class PAVServerEventListener implements CloudMatchEventListener {
    private static final String TAG = PAVServerEventListener.class.getSimpleName();

    private final Activity mActivity;
    private final PAVOnMatchedInterface mMatchedListener;

    public PAVServerEventListener(final Activity activity,
                                  final PAVOnMatchedInterface matchedInterface) {
        mActivity = activity;
        mMatchedListener = matchedInterface;
    }

    @Override
    public void onConnectionOpen() {
        Log.d(TAG, "onConnectionOpen");
        Toast.makeText(mActivity, mActivity.getString(R.string.connected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionClosed() {
        Log.d(TAG, "onConnectionClosed");
        Toast.makeText(mActivity, mActivity.getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionError(final Exception error) {
        Log.d(TAG, "onConnectionError");
        String msg = "Connection error";
        if (error instanceof CloudMatchInvalidCredentialException) {
            msg = "The API Key and APP Id that you are using seem to be incorrect. Are you running the latest version of CloudMatch Demo?";
        } else if (error instanceof CloudMatchConnectionException) {
            msg = error.getMessage();
        }
        Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
    }

    /*
     * When a match between two devices is successfully established, this code will understand the respective
     * positions and notify it to the main activity through the PinchOnMatchedInterface interface.
     */
    @Override
    public void onMatchResponse(final MatchResponse response) {
        Log.d(TAG, "onMatchResponse: " + response);
        switch (response.mOutcome) {
            case ok:
                final int groupSize = response.mGroupSize;

                // it's pinch. I know only two devices are involved.
                final PositionScheme scheme = response.mPositionScheme;
                if (scheme.mDevices.size() != 2) {
                    // error, there should only be two devices!
                    final String txt = "Error: matched in a group with more than 2 devices.";
                    Toast.makeText(mActivity, txt, Toast.LENGTH_LONG).show();
                    return;
                }

                // for this demo, devices are only paired horizontally

                int otherDeviceId = -1;
                for (final Integer i : response.mOthersInGroup) {
                    if (i != response.mMyIdInGroup) {
                        otherDeviceId = i;
                        break;
                    }
                }
                final DeviceInScheme myself = scheme.getDevicePerId(response.mMyIdInGroup);
                final DeviceInScheme other = scheme.getDevicePerId(otherDeviceId);

                if (myself != null && other != null) {
                    final boolean xGreater = myself.mPosition.x > other.mPosition.x;
                    final PAVScreenPositions position = xGreater ? PAVScreenPositions.right
                            : PAVScreenPositions.left;
                    mMatchedListener.onMatched(response.mGroupId, groupSize, position);
                }
                break;
            case fail:
                // is there a reason?
                switch (response.mReason) {
                    case timeout:
                    case uncertain:
                        Toast.makeText(mActivity, "Match request timed out.", Toast.LENGTH_LONG).show();
                        break;
                    case error:
                    case unknown:
                    default:
                        Toast.makeText(mActivity, "Match request failed.", Toast.LENGTH_LONG).show();
                        break;
                }
            default:
                break;
        }
    }

    /*
     * We don't expect anything else in this app, so the following methods won't do anything.
     */
    @Override
    public void onLeaveGroupResponse(final LeaveGroupResponse response) {
        Log.d(TAG, "onLeaveGroupResponse: " + response);
    }

    @Override
    public void onDeliveryResponse(final DeliveryResponse response) {
        Log.d(TAG, "onDeliveryResponse: " + response);
    }

    @Override
    public void onDeliveryProgress(final String tag, final String deliveryId, final int progress) {
        Log.d(TAG, "onDeliveryProgress: " + progress);
    }

    @Override
    public void onMatcheeDeliveryProgress(final String tag, final int progress) {
        Log.d(TAG, "onMatcheeDeliveryProgress: " + progress);
    }

    @Override
    public void onMatcheeDelivery(final MatcheeDelivery delivery) {
        Log.d(TAG, "onMatcheeDelivery");

        try {
            final JSONObject json = new JSONObject(delivery.mPayload);
            if (json.has(PAVOnMatchedInterface.IMAGE_HEIGHT)) {
                final int imageHeight = json.getInt(PAVOnMatchedInterface.IMAGE_HEIGHT);
                Log.d(TAG, "matchee delivery: image height, " + imageHeight);
                mMatchedListener.onOtherMeasurements(imageHeight);
            } else {
                Log.d(TAG, "matchee delivery, not sure what it was: " + delivery);
            }
        } catch (final JSONException e) {
            Log.d(TAG, "JSONException caught: " + e);
            // TODO: show toast?
        }
    }

    @Override
    public void onMatcheeLeft(final MatcheeLeftMessage message) {
        Log.d(TAG, "onMatcheeLeft: " + message);
    }

}
