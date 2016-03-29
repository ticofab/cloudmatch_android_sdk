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

package io.ticofab.cm_android_sdk.library.helpers;

import android.content.Context;
import android.provider.Settings.Secure;

import java.net.URI;
import java.net.URISyntaxException;

import io.ticofab.cm_android_sdk.library.consts.ServerConsts;

/**
 * Helper to connectCloudMatch the CloudMatch instance to the backend.
 */
public class Connector {
    final String CONNECTION_PARAM_APIKEY = "apiKey";
    final String CONNECTION_PARAM_APPID = "appId";
    final String CONNECTION_PARAM_OS = "os";
    final String CONNECTION_PARAM_DEVICEID = "deviceId";

    String mApiKey;
    String mAppId;
    String mDeviceId;
    final String mOS = "Android";

    public Connector(final Context context, final String apiKey, final String appId) {
        // note: this value may change upon factory reset, but it works fine from 2.3 on
        mDeviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        mApiKey = apiKey;
        mAppId = appId;
    }

    public URI getConnectionUri() throws URISyntaxException {
        final String url = ServerConsts.WS_ENDPOINT_URL + "?"
                + CONNECTION_PARAM_APIKEY + "=" + mApiKey + "&"
                + CONNECTION_PARAM_APPID + "=" + mAppId + "&"
                + CONNECTION_PARAM_OS + "=" + mOS + "&"
                + CONNECTION_PARAM_DEVICEID + "=" + mDeviceId;
        return new URI(url);
    }
}
