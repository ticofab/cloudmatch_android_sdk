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

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotConnectedException;
import io.ticofab.cm_android_sdk.library.views.CloudMatchPinchViewHorizontal;

/*
 * This class encapsulates the logic to create payload to deliver to the other devices.
 */
public class PADDeliveryHelper {
    private static final String TAG = PADDeliveryHelper.class.getSimpleName();

    final CloudMatchPinchViewHorizontal mPinchView;

    public PADDeliveryHelper(CloudMatchPinchViewHorizontal pinchView) {
        mPinchView = pinchView;
    }

    public void sendShapeDragStart(final String groupId, final String shape) {
        // deliver message to other with the right tag
        final JSONObject json = new JSONObject();
        try {
            json.put(PADDeliveryInterface.SHAPE_DRAG, shape);
            mPinchView.deliverPayloadToGroup(json.toString(), groupId, null);
        } catch (final JSONException e) {
            Log.d(TAG, "JSONException caught: " + e);
            // TODO: toast?
        } catch (final CloudMatchNotConnectedException e1) {
            Log.d(TAG, "CloudMatchNotConnectedException: " + e1);
        }
    }

    public void sendShapeDragStopped(final String groupId) {
        // deliver message to other with the right tag
        final JSONObject json = new JSONObject();
        try {
            json.put(PADDeliveryInterface.SHAPE_DRAG_STOPPED, 0);
            mPinchView.deliverPayloadToGroup(json.toString(), groupId, null);
        } catch (final JSONException e) {
            Log.d(TAG, "JSONException caught: " + e);
            // TODO: toast?
        } catch (final CloudMatchNotConnectedException e1) {
            Log.d(TAG, "CloudMatchNotConnectedException: " + e1);
        }
    }

    public void sendShapeReceivedAck(final String groupId, final String shape) {
        // send ack of received shape
        final JSONObject json = new JSONObject();
        try {
            json.put(PADDeliveryInterface.SHAPE_ACQUISITION_ACK, shape);
            mPinchView.deliverPayloadToGroup(json.toString(), groupId, null);
        } catch (final JSONException e) {
            Log.d(TAG, "JSONException caught: " + e);
            // TODO: toast?
        } catch (final CloudMatchNotConnectedException e1) {
            Log.d(TAG, "CloudMatchNotConnectedException: " + e1);
        }
    }

    public void sendCointoss(final String groupId, final Double cointoss) {

        final JSONObject json = new JSONObject();
        try {
            json.put(PADDeliveryInterface.COIN_TOSS, cointoss);
            mPinchView.deliverPayloadToGroup(json.toString(), groupId, null);
        } catch (final JSONException e) {
            Log.d(TAG, "JSONException caught: " + e);
            // TODO: toast?
        } catch (final CloudMatchNotConnectedException e1) {
            Log.d(TAG, "CloudMatchNotConnectedException: " + e1);
        }
    }
}
