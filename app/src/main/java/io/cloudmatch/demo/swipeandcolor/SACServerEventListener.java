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

package io.cloudmatch.demo.swipeandcolor;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.cloudmatch.demo.R;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchConnectionException;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchInvalidCredentialException;
import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchEventListener;
import io.ticofab.cm_android_sdk.library.models.messages.MatcheeDelivery;
import io.ticofab.cm_android_sdk.library.models.messages.MatcheeLeftMessage;
import io.ticofab.cm_android_sdk.library.models.responses.LeaveGroupResponse;
import io.ticofab.cm_android_sdk.library.models.responses.MatchResponse;

/*
 * Implementation of the OnCloudMatchEvent interface from the CloudMatch. This class also takes two listeners,
 * see constructor. As usual, many callbacks are not implemented as they're not required in this application.
 */
public class SACServerEventListener extends CloudMatchEventListener {
    private static final String TAG = SACServerEventListener.class.getSimpleName();

    private final Activity mActivity;
    private final SACMatchedInterface mMatchedListener;
    private final SACDeliveryInterface mRotationListener;

    public SACServerEventListener(final Activity activity,
                                  final SACMatchedInterface matchedInterface,
                                  final SACDeliveryInterface rotationListener) {
        mActivity = activity;
        mMatchedListener = matchedInterface;
        mRotationListener = rotationListener;
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
        String msg = "connection error";
        if (error instanceof CloudMatchInvalidCredentialException) {
            msg = "The API Key and APP Id that you are using seem to be incorrect."
                    + " Are you running the latest version of CloudMatch Demo?";
        } else if (error instanceof CloudMatchConnectionException) {
            msg = error.getMessage();
        }
        Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
    }

    // This method simply notifies the listener if the match was successful.
    @Override
    public void onMatchResponse(final MatchResponse response) {
        Log.d(TAG, "onMatchResponse: " + response);
        switch (response.mOutcome) {
            case ok:
                final int groupSize = response.mGroupSize;
                final int myIdInGroup = response.mMyIdInGroup;
                final String groupId = response.mGroupId;
                mMatchedListener.onMatched(groupId, groupSize, myIdInGroup);
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

    // This callback will notify the listener if a rotation message has been received.
    @Override
    public void onMatcheeDelivery(final MatcheeDelivery delivery) {
        try {
            final JSONObject json = new JSONObject(delivery.mPayload);
            if (json.has(SACDeliveryInterface.ROTATION_MESSAGE)) {
                Log.d(TAG, "matchee delivery: rotation");
                mRotationListener.onRotateMessage();
            }
        } catch (final JSONException e) {
            Log.d(TAG, "JSONException! " + e);
        }
    }

    @Override
    public void onMatcheeLeft(final MatcheeLeftMessage message) {
        Log.d(TAG, "onMatcheeLeft: " + message);
        mMatchedListener.onMatcheeLeft();
    }

}
