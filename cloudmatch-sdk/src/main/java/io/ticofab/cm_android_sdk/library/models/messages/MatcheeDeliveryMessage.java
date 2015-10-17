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

package io.ticofab.cm_android_sdk.library.models.messages;

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.consts.JsonLabels;

/**
 * Internal model used by the SDK to deal with chunked deliveries.
 */
public class MatcheeDeliveryMessage {
    public Integer mSenderMatchee;
    public String mDeliveryId;
    public String mPayload;
    public String mGroupId;
    public String mTag;
    public int mChunkNr = -1;
    public int mTotalChunks = -1;

    public static MatcheeDeliveryMessage fromJson(final JSONObject jsonObj) throws JSONException {
        final MatcheeDeliveryMessage message = new MatcheeDeliveryMessage();
        if (jsonObj.has(JsonLabels.GROUP_ID)) {
            message.mGroupId = jsonObj.getString(JsonLabels.GROUP_ID);
        }
        if (jsonObj.has(JsonLabels.PAYLOAD)) {
            message.mPayload = jsonObj.getString(JsonLabels.PAYLOAD);
        }
        if (jsonObj.has(JsonLabels.DELIVERY_TAG)) {
            message.mTag = jsonObj.getString(JsonLabels.DELIVERY_TAG);
        }
        if (jsonObj.has(JsonLabels.MATCHEE_ID)) {
            message.mSenderMatchee = jsonObj.getInt(JsonLabels.MATCHEE_ID);
        }
        if (jsonObj.has(JsonLabels.INPUT_CHUNK_NUMBER)) {
            message.mChunkNr = jsonObj.getInt(JsonLabels.INPUT_CHUNK_NUMBER);
        }
        if (jsonObj.has(JsonLabels.INPUT_TOTAL_CHUNKS)) {
            message.mTotalChunks = jsonObj.getInt(JsonLabels.INPUT_TOTAL_CHUNKS);
        }
        if (jsonObj.has(JsonLabels.INPUT_DELIVERY_ID)) {
            message.mDeliveryId = jsonObj.getString(JsonLabels.INPUT_DELIVERY_ID);
        }
        return message;
    }
}
