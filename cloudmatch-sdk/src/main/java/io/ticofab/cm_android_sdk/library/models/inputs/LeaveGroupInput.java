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

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.consts.JsonLabels;
import io.ticofab.cm_android_sdk.library.consts.ResponseTypes;


/**
 * Model of a client input to leave the current group.
 */
public class LeaveGroupInput {

    public String mReason;
    public String mGroupId;

    public String toJsonStr() throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put(JsonLabels.TYPE, ResponseTypes.leaveGroup);
        if (!TextUtils.isEmpty(mReason)) {
            jsonObj.put(JsonLabels.REASON, mReason);
        }
        if (!TextUtils.isEmpty(mGroupId)) {
            jsonObj.put(JsonLabels.GROUP_ID, mGroupId);
        }
        return jsonObj.toString();
    }

}
