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

package io.cloudmatch.demo.swipeandcolor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.cloudmatch.demo.R;
import io.ticofab.cm_android_sdk.library.interfaces.LocationProvider;

/*
 * This demo lets you match up to 8 devices using a single swipe, and each one will display a different color.
 * Tapping on any device will make all of them rotate color.
 */
public class SACActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = SACActivity.class.getSimpleName();

    @Bind(R.id.color_image_iv) ImageView mColorIV;
    @Bind(R.id.container_view) RelativeLayout mContainer;

    int[] mColorTable;
    int mCurrentColorIndex;
    int mGroupSize;

    // location stuff
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;

    // Implementation of the matched interface. When the device is matched in a group, this callback will enable
    // the ImageView and give it an inital color corresponding to the color table. It will then set a click
    // listener on it which will broadcast a "rotation message" to all the other devices in the group when the
    // image receives a click.
    private final SACMatchedInterface mMatchedInterface = new SACMatchedInterface() {

        @Override
        public void onMatched(final String groupId, final int groupSize, final int myIdInGroup) {
            Log.d(TAG, "onMatched, groupId: " + groupId + ", groupSize: " + groupSize + ", myId: " + myIdInGroup);

            // store the group size
            mGroupSize = groupSize;

            // clear and disable the drawing layout
            mDrawingLayout.cleanCanvas();
            mDrawingLayout.setVisibility(View.GONE);

            // trigger the "touch to rotate" mechanism
            mColorIV.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    try {
                        final JSONObject json = new JSONObject();
                        json.put(SACDeliveryInterface.ROTATION_MESSAGE, 1);
                        mDrawingLayout.deliverPayload(json.toString(), groupId);
                        setNewColor();
                    } catch (final JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });

            // 1. pick a color table according to the group size
            mColorTable = ColorTables.getColorTable(groupSize);

            // 2. use a color corresponding to my id
            mCurrentColorIndex = myIdInGroup;
            setNewColor();
        }

        @Override
        public void onMatcheeLeft() {
            // if nobody is in the group anymore, let's quit it.
            mGroupSize = mGroupSize - 1;
            if (mGroupSize <= 1) {
                mDrawingLayout.setVisibility(View.VISIBLE);
                mColorIV.setBackgroundColor(Color.TRANSPARENT);
                Toast.makeText(getApplicationContext(), "Everybody left", Toast.LENGTH_SHORT).show();
            }
        }

    };

    // Implementation of the SwipeAndColorDemoDeliveryInterface. When the server event handler receives a delivery
    // containing a rotation message, it will trigger the color switch.
    private final SACDeliveryInterface mRotationInterface = new SACDeliveryInterface() {

        @Override
        public void onRotateMessage() {
            Log.d(TAG, "onRotateMessage");
            setNewColor();
        }
    };

    SACDrawingLayout mDrawingLayout;
    SACServerEventListener mSwipeAndColorDemoSEL = new SACServerEventListener(this,
            mMatchedInterface, mRotationInterface);

    // triggers a new color on screen.
    private void setNewColor() {
        mCurrentColorIndex = (mCurrentColorIndex + 1) % mColorTable.length;
        final int newColor = mColorTable[mCurrentColorIndex];
        mColorIV.setBackgroundColor(newColor);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_and_color_demo);
        ButterKnife.bind(this);

        mColorIV.setBackgroundColor(Color.TRANSPARENT);

        // create layout programmatically so we can pass the activity as context
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mDrawingLayout = new SACDrawingLayout(this, mSwipeAndColorDemoSEL, new LocationProvider() {
            @Override
            public Location getLocation() {
                if (mGoogleApiClient.isConnected()) {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
                return mLastLocation;
            }
        });
        mDrawingLayout.setLayoutParams(params);
        mDrawingLayout.setBackgroundColor(Color.TRANSPARENT);
        mContainer.addView(mDrawingLayout);

        // init google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onDestroy() {
        mDrawingLayout.onDestroy();
        super.onDestroy();
    }

    // *******************************************************************
    // The following code comes from Google's guide to check for
    // the presence of GooglePlayServices.
    // https://developers.google.com/android/guides/api-client
    // *******************************************************************
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (!mResolvingError) {
            if (result.hasResolution()) {
                try {
                    mResolvingError = true;
                    result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    mGoogleApiClient.connect();
                }
            } else {
                // Show dialog using GoogleApiAvailability.getErrorDialog()
                showErrorDialog(result.getErrorCode());
                mResolvingError = true;
            }
        }
    }

    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");

    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends android.support.v4.app.DialogFragment {
        public ErrorDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((SACActivity) getActivity()).onDialogDismissed();
        }
    }
}
