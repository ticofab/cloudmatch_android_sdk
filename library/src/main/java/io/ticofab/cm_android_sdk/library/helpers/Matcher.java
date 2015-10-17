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

import android.app.Activity;
import android.location.Location;

import com.codebutler.android_websockets.WebSocketClient;

import org.json.JSONException;

import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotConnectedException;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotInitializedException;
import io.ticofab.cm_android_sdk.library.exceptions.LocationServicesUnavailableException;
import io.ticofab.cm_android_sdk.library.location.LocationController;
import io.ticofab.cm_android_sdk.library.models.inputs.GroupComMatchInput;
import io.ticofab.cm_android_sdk.library.models.inputs.GroupCreateMatchInput;
import io.ticofab.cm_android_sdk.library.models.inputs.base.MatchInput;

/**
 * Helper to package and send a match request to the backend.
 *
 * @author @ticofab
 */
public class Matcher {

    private static WebSocketClient mWSClient;
    private static Activity mActivity;

    public static void init(final Activity activity) {
        mActivity = activity;
    }

    public static void setWebsocketClient(final WebSocketClient wsClient) {
        mWSClient = wsClient;
    }

    public static void sendGroupComMatchRequest(final GroupComMatchInput matchInput) {
        sendMatchRequest(matchInput);
    }

    public static void sendGroupCreateMatchRequest(final GroupCreateMatchInput matchInput)
            throws LocationServicesUnavailableException {

        LocationController.checkAvailabilityOrThrow(mActivity);

        final Location currentLocation = LocationController.getLastLocation();
        if (currentLocation == null) {
            throw new LocationServicesUnavailableException("Location Services are unavailable.");
        }

        // initialize match request with mandatory stuff
        matchInput.mLatitude = currentLocation.getLatitude();
        matchInput.mLongitude = currentLocation.getLongitude();

        sendMatchRequest(matchInput);
    }

    private static void sendMatchRequest(final MatchInput matchInput) throws CloudMatchNotConnectedException,
            CloudMatchNotInitializedException {
        checkInitializationOrThrow();
        checkWebsocketClientOrThrow();

        try {
            // get json string and send it over
            final String matchInputJsonStr = matchInput.toJsonStr();
            mWSClient.send(matchInputJsonStr);
        } catch (final JSONException e) {
            // TODO: manage exception
        }
    }

    private static void checkInitializationOrThrow() throws CloudMatchNotInitializedException {
        if (mActivity == null) {
            throw new CloudMatchNotInitializedException();
        }
    }

    private static void checkWebsocketClientOrThrow() throws CloudMatchNotConnectedException {
        if (mWSClient == null || !mWSClient.isConnected()) {
            throw new CloudMatchNotConnectedException();
        }
    }
}
