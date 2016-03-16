package io.cloudmatch.demo.pinchandview;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotConnectedException;
import io.cloudmatch.demo.pinchanddrag.PinchAndDragDeliveryHelper;

public class PinchAndViewDeliveryHelper {
    private static final String TAG = PinchAndDragDeliveryHelper.class.getSimpleName();

    public static void sendImageHeight(final String groupId, final int imageHeight) {
        // deliver message to other
        final JSONObject json = new JSONObject();
        try {
            json.put(PinchAndViewOnMatchedInterface.IMAGE_HEIGHT, imageHeight);
            CloudMatch.deliverPayloadToGroup(json.toString(), groupId, null);
        } catch (final JSONException e) {
            Log.d(TAG, "JSONException caught: " + e);
            // TODO: toast?
        } catch (final CloudMatchNotConnectedException e1) {
            Log.d(TAG, "CloudMatchNotConnectedException: " + e1);
        }
    }

}
