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

package io.ticofab.cm_android_sdk.library.models.inputs.base;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.consts.Areas;
import io.ticofab.cm_android_sdk.library.consts.Criteria;
import io.ticofab.cm_android_sdk.library.consts.JsonLabels;
import io.ticofab.cm_android_sdk.library.consts.ResponseTypes;

/**
 * Model of a match input to send to the server.
 * 
 * @author @ticofab
 * 
 */
abstract public class MatchInput {

    private static final String MATCH_INPUT_CRITERIA = "criteria";
    private static final String MATCH_INPUT_AREASTART = "areaStart";
    private static final String MATCH_INPUT_AREAEND = "areaEnd";
    private static final String MATCH_INPUT_EQUALITYPARAM = "equalityParam";

    // mandatory stuff
    public Criteria mCriteria = Criteria.undefined;
    public Areas mAreaStart = Areas.invalid;
    public Areas mAreaEnd = Areas.invalid;

    // optional stuff
    public String mEqualityParam;

    public MatchInput(final Criteria criteria) {
        mCriteria = criteria;
    }

    protected JSONObject toJson() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(JsonLabels.TYPE, ResponseTypes.match);

        if (mCriteria != Criteria.undefined) {
            json.put(MATCH_INPUT_CRITERIA, mCriteria.name());
        }
        if (!TextUtils.isEmpty(mEqualityParam)) {
            json.put(MATCH_INPUT_EQUALITYPARAM, mEqualityParam);
        }
        if (mAreaStart != Areas.invalid) {
            json.put(MATCH_INPUT_AREASTART, mAreaStart.name());
        }
        if (mAreaEnd != Areas.invalid) {
            json.put(MATCH_INPUT_AREAEND, mAreaEnd.name());
        }
        return json;
    }

    public abstract String toJsonStr() throws JSONException;
}
