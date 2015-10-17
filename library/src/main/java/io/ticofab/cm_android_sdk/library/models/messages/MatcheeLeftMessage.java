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
 * Model to notify that one client has left the previously established group.
 *
 * @author @ticofab
 */
public class MatcheeLeftMessage {
    public Integer mLeaverMatchee;
    public String mReason;
    public String mGroupId;

    public static MatcheeLeftMessage fromJson(final JSONObject jsonObj) throws JSONException {
        final MatcheeLeftMessage message = new MatcheeLeftMessage();
        if (jsonObj.has(JsonLabels.GROUP_ID)) {
            message.mGroupId = jsonObj.getString(JsonLabels.GROUP_ID);
        }
        if (jsonObj.has(JsonLabels.REASON)) {
            message.mReason = jsonObj.getString(JsonLabels.REASON);
        }
        if (jsonObj.has(JsonLabels.MATCHEE_ID)) {
            message.mLeaverMatchee = jsonObj.getInt(JsonLabels.MATCHEE_ID);
        }
        return message;
    }
}
