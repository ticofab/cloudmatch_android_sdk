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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.ticofab.cm_android_sdk.library.consts.JsonLabels;
import io.ticofab.cm_android_sdk.library.consts.MovementType;
import io.ticofab.cm_android_sdk.library.consts.Outcomes;
import io.ticofab.cm_android_sdk.library.consts.ResponseReasons;
import io.ticofab.cm_android_sdk.library.models.PositionScheme;

/**
 * Server response to a match attempt from a client.
 */
public class MatchResponse {

    private static final String OTHERS_IN_GROUP = "group";

    public Outcomes mOutcome = Outcomes.unknown;
    public ResponseReasons mReason = ResponseReasons.unknown;
    public String mGroupId;
    public Integer mGroupSize;
    public MovementType mMovementType;
    public Integer mMyIdInGroup;
    public ArrayList<Integer> mOthersInGroup = new ArrayList<Integer>();
    public PositionScheme mPositionScheme;

    public static MatchResponse fromJson(final JSONObject jsonObj) throws JSONException {
        final MatchResponse matchResponse = new MatchResponse();

        if (jsonObj.has(JsonLabels.OUTCOME)) {
            final String outcome = jsonObj.getString(JsonLabels.OUTCOME);
            matchResponse.mOutcome = Outcomes.valueOf(outcome);
        }
        if (jsonObj.has(JsonLabels.REASON)) {
            matchResponse.mReason = ResponseReasons.valueOf(jsonObj.getString(JsonLabels.REASON));
        }
        if (jsonObj.has(JsonLabels.GROUP_ID)) {
            matchResponse.mGroupId = jsonObj.getString(JsonLabels.GROUP_ID);
        }
        if (jsonObj.has(JsonLabels.MY_ID_IN_GROUP)) {
            matchResponse.mMyIdInGroup = jsonObj.getInt(JsonLabels.MY_ID_IN_GROUP);
        }
        if (jsonObj.has(OTHERS_IN_GROUP)) {
            final JSONArray othersJsonObj = jsonObj.getJSONArray(OTHERS_IN_GROUP);
            for (int i = 0; i < othersJsonObj.length(); i++) {
                matchResponse.mOthersInGroup.add(othersJsonObj.getInt(i));
            }
            matchResponse.mGroupSize = othersJsonObj.length();
        }
        if (jsonObj.has(JsonLabels.MOVEMENT_TYPE)) {
            matchResponse.mMovementType = MovementType.valueOf(jsonObj.getString(JsonLabels.MOVEMENT_TYPE));
        }
        if (jsonObj.has(JsonLabels.POSITION_SCHEME)) {
            final JSONObject positionJson = jsonObj.getJSONObject(JsonLabels.POSITION_SCHEME);
            matchResponse.mPositionScheme = PositionScheme.fromJson(positionJson);
        }
        return matchResponse;
    }
}
