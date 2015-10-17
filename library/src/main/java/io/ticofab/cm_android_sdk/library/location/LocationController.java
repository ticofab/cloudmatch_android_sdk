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

package io.ticofab.cm_android_sdk.library.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import io.ticofab.cm_android_sdk.library.exceptions.LocationServicesUnavailableException;

/**
 * Controller to retrieve location information using Google Play Services.
 *
 * @author @ticofab
 */
public class LocationController {

    static LocationClient mLocationClient;

    // Set the update interval to 5 seconds
    static final long UPDATE_INTERVAL = 5 * 1000; // milliseconds

    // Set the fastest update interval to 1 second
    static final long FASTEST_INTERVAL = 1000; // milliseconds

    // Set high location accuracy;
    static final int LOC_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;

    // Define an object that holds accuracy and frequency parameters
    static LocationRequest mLocationRequest;

    static boolean mIsConnected = false;

    public static void init(final Context context) {
        // if we're here, I assume that location services are available
        mLocationClient = new LocationClient(context, mConnCallbacks, mConnFailedListener);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LOC_PRIORITY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    public static void connect() {
        if (mLocationClient != null) {
            mLocationClient.connect();
        }
    }

    public static void disconnect() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            mLocationClient.disconnect();
        }
    }

    public static Location getLastLocation() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            return mLocationClient.getLastLocation();
        } else {
            return null;
        }
    }

    public static void checkAvailabilityOrThrow(final Context context) throws LocationServicesUnavailableException {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS != resultCode) {
            // Google Play services was not available for some reason
            throw new LocationServicesUnavailableException("Google Play Location Servies are unavailable.");
        }
    }

    private static final GooglePlayServicesClient.ConnectionCallbacks mConnCallbacks =
            new GooglePlayServicesClient.ConnectionCallbacks() {

                @Override
                public void onConnected(final Bundle connectionHint) {
                    mLocationClient.requestLocationUpdates(mLocationRequest, mLocListener);
                    mIsConnected = true;
                }

                @Override
                public void onDisconnected() {
                    mIsConnected = false;
                }
            };

    private static final GooglePlayServicesClient.OnConnectionFailedListener mConnFailedListener =
            new GooglePlayServicesClient.OnConnectionFailedListener() {

                @Override
                public void onConnectionFailed(final ConnectionResult connectionResult) {
                }
            };

    private static final LocationListener mLocListener = new LocationListener() {

        @Override
        public void onLocationChanged(final Location location) {
            // got the first location update, stop.
            if (mIsConnected) {
                mLocationClient.removeLocationUpdates(mLocListener);
            }
        }
    };
}
