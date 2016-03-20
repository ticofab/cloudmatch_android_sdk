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

package io.cloudmatch.demo.pinchanddrag;

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
import io.ticofab.cm_android_sdk.library.models.responses.MatchResponse;

/*
 * Implementation of the OnCloudMatchEvent interface from CloudMatch. Will receive callbacks upon server events.
 * This implementation also gets the context and two interfaces in the constructor.
 * A lot of these methods are not used in this application and are therefore left empty. 
 */
public class PADServerEventListener extends CloudMatchEventListener {
    private static final String TAG = PADServerEventListener.class.getSimpleName();

    private final Activity mActivity;
    private final PADMatchedInterface mMatchedListener;
    private final PADDeliveryInterface mDeliveryListener;

    public PADServerEventListener(final Activity activity,
                                  final PADMatchedInterface matchedInterface,
                                  final PADDeliveryInterface deliveryInterface) {
        mActivity = activity;
        mMatchedListener = matchedInterface;
        mDeliveryListener = deliveryInterface;
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
            msg = "The API Key and APP Id that you are using seem to be incorrect. Are you running the latest version of CloudMatch Demo?";
        } else if (error instanceof CloudMatchConnectionException) {
            msg = error.getMessage();
        }
        Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
    }

    // if a successful match is established, will notify the listener through the interface.
    @Override
    public void onMatchResponse(final MatchResponse response) {
        Log.d(TAG, "onMatchResponse: " + response);
        switch (response.mOutcome) {
            case ok:
                mMatchedListener.onMatched(response.mGroupId);
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

    // When a new delivery arrives, this method parses it and notifies
    // the listener through the proper interface call.
    @Override
    public void onMatcheeDelivery(final MatcheeDelivery delivery) {
        try {
            final JSONObject json = new JSONObject(delivery.mPayload);
            if (json.has(PADDeliveryInterface.COIN_TOSS)) {
                final Double coinToss = json.getDouble(PADDeliveryInterface.COIN_TOSS);
                Log.d(TAG, "matchee delivery: CoinToss, " + coinToss);
                mDeliveryListener.onCoinToss(coinToss);
            } else if (json.has(PADDeliveryInterface.SHAPE_DRAG)) {
                final String shape = json.getString(PADDeliveryInterface.SHAPE_DRAG);
                Log.d(TAG, "matchee delivery, shape drag other side: " + shape);
                mDeliveryListener.onShapeDragInitiatedOnOtherSide(shape);
            } else if (json.has(PADDeliveryInterface.SHAPE_ACQUISITION_ACK)) {
                Log.d(TAG, "matchee delivery, shape acquisition other side.");
                final String shape = json.getString(PADDeliveryInterface.SHAPE_ACQUISITION_ACK);
                mDeliveryListener.onShapeReceivedOnOtherSide(shape);
            } else if (json.has(PADDeliveryInterface.SHAPE_DRAG_STOPPED)) {
                Log.d(TAG, "matchee delivery, shape drag stopped other side.");
                mDeliveryListener.onShapeDragStoppedOnOtherSide();
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
        mMatchedListener.onMatcheeLeft();
    }

}
