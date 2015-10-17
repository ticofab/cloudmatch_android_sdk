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

import io.ticofab.cm_android_sdk.library.consts.JsonLabels;
import io.ticofab.cm_android_sdk.library.consts.LeaveGroupReasons;
import io.ticofab.cm_android_sdk.library.consts.Outcomes;

/**
 * Server response to a client's action to leave its current group.
 *
 * @author @ticofab
 */
public class LeaveGroupResponse {
    public Outcomes mOutcome = Outcomes.unknown;
    public LeaveGroupReasons mLeaveGroupReason = LeaveGroupReasons.unknown;
    public String mGroupId;

    public static LeaveGroupResponse fromJson(final JSONObject jsonObj) throws JSONException {
        final LeaveGroupResponse response = new LeaveGroupResponse();
        if (jsonObj.has(JsonLabels.OUTCOME)) {
            final String outcome = jsonObj.getString(JsonLabels.OUTCOME);
            response.mOutcome = Outcomes.valueOf(outcome);
        }
        if (jsonObj.has(JsonLabels.REASON)) {
            response.mLeaveGroupReason = LeaveGroupReasons.valueOf(jsonObj.getString(JsonLabels.OUTCOME));
        }
        if (jsonObj.has(JsonLabels.GROUP_ID)) {
            response.mGroupId = jsonObj.getString(JsonLabels.GROUP_ID);
        }
        return response;
    }
}
