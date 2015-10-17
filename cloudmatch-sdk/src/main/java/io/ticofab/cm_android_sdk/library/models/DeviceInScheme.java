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

package io.ticofab.cm_android_sdk.library.models;

import android.graphics.Point;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a device in the PositionScheme matrix.
 */
public class DeviceInScheme {
    private static final String JSON_LABEL_ID = "id";
    private static final String JSON_LABEL_X = "x";
    private static final String JSON_LABEL_Y = "y";

    public int mIdInGroup;
    public Point mPosition = new Point();

    public static DeviceInScheme fromJson(final JSONObject jsonObj) throws JSONException {
        final DeviceInScheme dis = new DeviceInScheme();
        if (jsonObj.has(JSON_LABEL_ID)) {
            dis.mIdInGroup = jsonObj.getInt(JSON_LABEL_ID);
        }
        if (jsonObj.has(JSON_LABEL_X)) {
            dis.mPosition.x = jsonObj.getInt(JSON_LABEL_X);
        }
        if (jsonObj.has(JSON_LABEL_Y)) {
            dis.mPosition.y = jsonObj.getInt(JSON_LABEL_Y);
        }
        return dis;
    }
}
