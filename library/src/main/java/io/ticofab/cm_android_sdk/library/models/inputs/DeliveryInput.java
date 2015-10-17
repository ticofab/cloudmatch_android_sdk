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

package io.ticofab.cm_android_sdk.library.models.inputs;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.ticofab.cm_android_sdk.library.consts.JsonLabels;
import io.ticofab.cm_android_sdk.library.consts.ResponseTypes;

/**
 * Model of a delivery message to send off.
 * 
 * @author @ticofab
 * 
 */
public class DeliveryInput {
    public ArrayList<Integer> mRecipients;
    public String mDeliveryId;
    public String mPayload;
    public String mGroupId;
    public String mTag;
    public int mTotalChunks = -1;
    public int mChunkNr = -1;

    public String toJsonStr() throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put(JsonLabels.TYPE, ResponseTypes.delivery.toString());

        if (!TextUtils.isEmpty(mPayload)) {
            jsonObj.put(JsonLabels.PAYLOAD, mPayload);
        }
        if (!TextUtils.isEmpty(mGroupId)) {
            jsonObj.put(JsonLabels.GROUP_ID, mGroupId);
        }
        if (!TextUtils.isEmpty(mTag)) {
            jsonObj.put(JsonLabels.DELIVERY_TAG, mTag);
        }
        if (mRecipients != null && !mRecipients.isEmpty()) {
            final JSONArray recipientsJsonArray = new JSONArray();
            for (final Integer recipient : mRecipients) {
                recipientsJsonArray.put(recipient);
            }
            jsonObj.put(JsonLabels.INPUT_RECIPIENTS, recipientsJsonArray);
        }
        if (mTotalChunks != -1) {
            jsonObj.put(JsonLabels.INPUT_TOTAL_CHUNKS, mTotalChunks);
        }
        if (mChunkNr != -1) {
            jsonObj.put(JsonLabels.INPUT_CHUNK_NUMBER, mChunkNr);
        }
        if (!TextUtils.isEmpty(mDeliveryId)) {
            jsonObj.put(JsonLabels.INPUT_DELIVERY_ID, mDeliveryId);
        }

        return jsonObj.toString();
    }
}
