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

package io.ticofab.cm_android_sdk.library.models.responses;

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.consts.DeliveryReasons;
import io.ticofab.cm_android_sdk.library.consts.JsonLabels;
import io.ticofab.cm_android_sdk.library.consts.Outcomes;

/**
 * (Optional) Response to a delivery message.
 */
public class DeliveryResponse {
    public Outcomes mOutcome = Outcomes.unknown;
    public DeliveryReasons mDeliveryReasons = DeliveryReasons.unknown;
    public String mGroupId;

    public static DeliveryResponse fromJson(final JSONObject jsonObj) throws JSONException {
        final DeliveryResponse response = new DeliveryResponse();
        if (jsonObj.has(JsonLabels.OUTCOME)) {
            response.mOutcome = Outcomes.valueOf(jsonObj.getString(JsonLabels.OUTCOME));
        }
        if (jsonObj.has(JsonLabels.REASON)) {
            response.mDeliveryReasons = DeliveryReasons.valueOf(jsonObj.getString(JsonLabels.REASON));
        }
        if (jsonObj.has(JsonLabels.GROUP_ID)) {
            response.mGroupId = jsonObj.getString(JsonLabels.GROUP_ID);
        }

        return response;
    }
}
