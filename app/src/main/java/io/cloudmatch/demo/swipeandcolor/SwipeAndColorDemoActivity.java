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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotInitializedException;
import io.cloudmatch.demo.R;

/*
 * This demo lets you match up to 8 devices using a single swipe, and each one will display a different color.
 * Tapping on any device will make all of them rotate color.
 */
public class SwipeAndColorDemoActivity extends Activity {

    private static final String TAG = SwipeAndColorDemoActivity.class.getSimpleName();

    int[] mColorTable;
    int mCurrentColorIndex;
    int mGroupSize;

    // Implementation of the matched interface. When the device is matched in a group, this callback will enable
    // the ImageView and give it an inital color corresponding to the color table. It will then set a click
    // listener on it which will broadcast a "rotation message" to all the other devices in the group when the
    // image receives a click.
    private final SwipeAndColorDemoMatchedInterface mMatchedInterface = new SwipeAndColorDemoMatchedInterface() {

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
                        json.put(SwipeAndColorDemoDeliveryInterface.ROTATION_MESSAGE, 1);
                        CloudMatch.deliverPayloadToGroup(json.toString(), groupId, null);
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
    private final SwipeAndColorDemoDeliveryInterface mRotationInterface = new SwipeAndColorDemoDeliveryInterface() {

        @Override
        public void onRotateMessage() {
            Log.d(TAG, "onRotateMessage");
            setNewColor();
        }
    };

    private ImageView mColorIV;
    private SwipeAndColorDemoDrawingLayout mDrawingLayout;
    private final SwipeAndColorDemoServerEvent mSwipeAndColorDemoSEL = new SwipeAndColorDemoServerEvent(this,
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

        mColorIV = (ImageView) findViewById(R.id.color_image_iv);
        mColorIV.setBackgroundColor(Color.TRANSPARENT);
        mDrawingLayout = (SwipeAndColorDemoDrawingLayout) findViewById(R.id.swipe_and_color_drawing_layout);

        if (servicesConnected()) {
            initCloudMatch();
        }
    }

    public void initCloudMatch() {
        // initializes the CloudMatch. In this case we also immediately connect, but it could be done also at a
        // different stage.
        try {
            CloudMatch.init(this, mSwipeAndColorDemoSEL);
            CloudMatch.connect();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
     * Closing the connection in the onDestroy() method.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        CloudMatch.closeConnection();
    }

    // *******************************************************************
    // The following code comes from Google's guide to check for
    // the presence of GooglePlayServices.
    // *******************************************************************
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(final Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    protected void onActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (servicesConnected()) {
                            initCloudMatch();
                        }
                        break;
                    default:
                        // nothing we can do.
                        break;
                }
        }
    }

    private boolean servicesConnected() {
        final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == errorCode) {
            return true;
        } else {
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            if (errorDialog != null) {
                final ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getFragmentManager(), "Location Updates");
            }

            return false;
        }
    }
}
