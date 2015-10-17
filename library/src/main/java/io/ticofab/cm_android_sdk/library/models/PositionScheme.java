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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Representation of a matrix of devices, to show their respective positions.
 *
 * @author @ticofab
 */
public class PositionScheme {
    public static final String JSON_LABEL_WIDTH = "width";
    public static final String JSON_LABEL_HEIGHT = "height";
    public static final String JSON_LABEL_DEVICES = "devices";

    public int mWidth;
    public int mHeight;
    public ArrayList<DeviceInScheme> mDevices = new ArrayList<DeviceInScheme>();

    public static PositionScheme fromJson(final JSONObject jsonObj) throws JSONException {
        final PositionScheme ps = new PositionScheme();
        if (jsonObj.has(JSON_LABEL_WIDTH)) {
            ps.mWidth = jsonObj.getInt(JSON_LABEL_WIDTH);
        }
        if (jsonObj.has(JSON_LABEL_HEIGHT)) {
            ps.mHeight = jsonObj.getInt(JSON_LABEL_HEIGHT);
        }
        if (jsonObj.has(JSON_LABEL_DEVICES)) {
            final JSONArray devicesArray = jsonObj.getJSONArray(JSON_LABEL_DEVICES);
            for (int i = 0; i < devicesArray.length(); i++) {
                final DeviceInScheme dis = DeviceInScheme.fromJson(devicesArray.getJSONObject(i));
                ps.mDevices.add(dis);
            }
        }
        return ps;
    }

    public DeviceInScheme getDevicePerId(final Integer deviceId) {
        for (final DeviceInScheme d : mDevices) {
            if (d.mIdInGroup == deviceId) {
                return d;
            }
        }
        return null;
    }
}
