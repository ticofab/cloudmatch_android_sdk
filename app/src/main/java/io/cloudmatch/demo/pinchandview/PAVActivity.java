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

package io.cloudmatch.demo.pinchandview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.cloudmatch.demo.R;
import io.ticofab.cm_android_sdk.library.consts.MovementType;
import io.ticofab.cm_android_sdk.library.consts.Movements;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotConnectedException;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchViewInterfaceNotSetException;
import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchViewInterface;
import io.ticofab.cm_android_sdk.library.interfaces.LocationProvider;
import io.ticofab.cm_android_sdk.library.views.CloudMatchPinchViewHorizontal;

/*
 * This demo lets the user pair two devices, pinching them across the long side. The devices
 * will understand their relative position (left or right) and the corresponding image will be shown.
 */
public class PAVActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = PAVActivity.class.getSimpleName();

    @Bind(R.id.image_position) ImageView mImage;
    @Bind(R.id.container_view) RelativeLayout mContainerRL;
    @Bind(R.id.pinch_view) CloudMatchPinchViewHorizontal mPinchView;
    @Bind(R.id.pinchandview_pinch_instruction_iv) ImageView mPinchInstructionsIcon;

    int mIVTopY;
    int mIVTopX;
    int mIVWidth;
    int mIVHeight;
    int mPointEndX;
    int mPointEndY;
    int mHalfScreenX;
    int mHalfScreenY;
    Point mScreenDimensions;

    MyRectView mMyRectView;

    private final Handler mHandler = new Handler();
    private final Runnable mRemoveViewRunnable = new Runnable() {

        @Override
        public void run() {
            mContainerRL.removeView(mMyRectView);
        }
    };

    PAVScreenPositions mPosition;
    PAVDeliveryHelper mPAVDeliveryHelper;

    // location stuff
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;

    /*
     * Implementation of the PinchOnMatchedInterface. Upon a successful matching, the corresponding image is shown.
     */
    private final PAVOnMatchedInterface mMatchedInterface = new PAVOnMatchedInterface() {

        @Override
        public void onMatched(final String groupId, final int groupSize,
                              final PAVScreenPositions position) {

            final String txt = "Matched in a group of " + groupSize + ", my position is " + position + ".";
            Toast.makeText(PAVActivity.this, txt, Toast.LENGTH_LONG).show();

            if (position == PAVScreenPositions.unknown) {
                return;
            }

            mPosition = position;

            // send message to the other guy with my data
            mPAVDeliveryHelper.sendImageHeight(groupId, mIVHeight);
        }

        @Override
        public void onOtherMeasurements(final int othersImageHeight) {
            Log.d(TAG, "received measurement: " + othersImageHeight + ", mine is " + mIVHeight);
            if (othersImageHeight > mIVHeight) {
                // display it as it is
                displayImage();
            } else {
                // recalculate positions and dimensions
                {
                    mIVHeight = othersImageHeight;
                    mIVWidth = mIVHeight * mScreenDimensions.x / mScreenDimensions.y;
                    mIVTopY = mPointEndY - (mIVHeight / 2);
                    mIVTopX = mPointEndX > mHalfScreenX ? mScreenDimensions.x - mIVWidth : 0;

                    displayImage();
                }
            }

        }

        private void displayImage() {
            final LayoutParams params = new LayoutParams(mIVWidth, mIVHeight);
            params.setMargins(mIVTopX, mIVTopY, 0, 0);
            mImage.setLayoutParams(params);
            mImage.setVisibility(View.VISIBLE);
            mPinchInstructionsIcon.setVisibility(View.GONE);
            mPinchInstructionsIcon.requestLayout();
            switch (mPosition) {
                case left:
                    mImage.setImageResource(R.drawable.split_image_2_2);
                    break;
                case right:
                    mImage.setImageResource(R.drawable.split_image_2_1);
                    break;
                default: // we shouldn't get here
                    Log.d(TAG, "error, can't recognize position");
                    mImage.setVisibility(View.GONE);
                    break;
            }
            mImage.requestLayout();
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinch_and_view_demo);
        ButterKnife.bind(this);

        mImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                mImage.setVisibility(View.GONE);
                mPinchInstructionsIcon.setVisibility(View.VISIBLE);
            }
        });

        // initialize here or otherwise 'this' & the view will be null
        mMyRectView = new MyRectView(this);
        mPAVDeliveryHelper = new PAVDeliveryHelper(mPinchView);

        mScreenDimensions = PAVDisplayHelper.getScreenSize(this);
        mHalfScreenY = mScreenDimensions.y / 2;
        mHalfScreenX = mScreenDimensions.x / 2;

        // init google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();
    }

    public void initCloudMatch() {
        // Initializes the CloudMatch. Here the connection is established immediately after, but it could be done
        // at a different stage.
        try {
            // get the CloudMatchView object (defined in the xml layout) and set its interface.
            mPinchView.connectCloudMatch(this,
                    new PAVServerEventListener(this, mMatchedInterface),
                    new LocationProvider() {

                        @Override
                        public Location getLocation() {
                            if (mGoogleApiClient.isConnected()) {
                                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            }
                            return mLastLocation;
                        }
                    },
                    mPinchDemoSMVI);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        // Closing the cloudmatch connection in the onDestroy() method.
        mPinchView.closeConnection();
        super.onDestroy();
    }

    /*
     * Pass touch events to the CloudMatchView.
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        // this is an example of how an exception can be thrown
        try {
            mPinchView.onTouchEvent(event);
        } catch (CloudMatchViewInterfaceNotSetException e) {
            Log.d(TAG, "It seems that the cloud match interface was not set.");
        } catch (CloudMatchNotConnectedException e) {
            Log.d(TAG, "CloudMatch wasn't connected.");
        }
        return true;
    }

    /*
     * Implementation of the CloudMatchViewInterface. This needs to be set on the CloudMatchView that the
     * application is using. See activity_pinch_demo.xml.
     */
    private final CloudMatchViewInterface mPinchDemoSMVI = new CloudMatchViewInterface() {

        @Override
        public void onMovementDetection(final Movements movement, final MovementType movementType,
                                        final PointF pointStart, final PointF pointEnd) {
            Log.d(TAG, "onMovementDetection. Movement: " + movement + ", swipeType: " + movementType
                    + ", point start: (" + pointStart.x + ", " + pointStart.y + ")" + ", point end: ("
                    + pointEnd.x + ", " + pointEnd.y + ")");

            mPointEndY = (int) pointEnd.y;
            mPointEndX = (int) pointEnd.x;

            // this block takes care of the little rect to show where the pinch happened
            {
                // remove existing stuff if there
                mContainerRL.removeView(mMyRectView);
                mHandler.removeCallbacksAndMessages(null);

                // position the little rect view at the appropriate coordinates
                final int side = MyRectView.SIDE_LENGHT;
                final LayoutParams params = new RelativeLayout.LayoutParams(side, side);

                int rule = RelativeLayout.ALIGN_PARENT_RIGHT;
                switch (movement) {
                    case innerleft:
                    case topleft:
                    case bottomleft:
                    case rightleft:
                        rule = RelativeLayout.ALIGN_PARENT_LEFT;
                        break;
                    default:
                        break;
                }

                params.addRule(rule, R.id.container_view);
                params.setMargins(10, mPointEndY, 10, 0);
                mContainerRL.addView(mMyRectView, params);

                mHandler.postDelayed(mRemoveViewRunnable, 2000);
            }

            // here we calculate the size and positioning of the image view
            {
                if (mPointEndY > mHalfScreenY) {
                    mIVHeight = (mScreenDimensions.y - mPointEndY) * 2;
                    mIVTopY = mScreenDimensions.y - mIVHeight;
                } else {
                    mIVHeight = mPointEndY * 2;
                    mIVTopY = 0;
                }

                mIVWidth = mIVHeight * mScreenDimensions.x / mScreenDimensions.y;
                if (mPointEndX > mHalfScreenX) {
                    mIVTopX = mScreenDimensions.x - mIVWidth;
                } else {
                    mIVTopX = 0;
                }

                Log.d(TAG, "iv height: " + mIVHeight + ", width: " + mIVWidth + ", y: " + mIVTopY + ", x: "
                        + mIVTopX);
            }
        }

        @Override
        public void onError(final RuntimeException e) {
            Log.d(TAG, "onError: " + e);
        }

        @Override
        public boolean isGestureValid() {
            return true;
        }
    };

    /*
     * Tiny custom view to show a rectangle at the screen end where the matching movement has happened.
     */
    private class MyRectView extends View {
        public static final int SIDE_LENGHT = 20;

        final Paint mMyPaint = new Paint();

        public MyRectView(final Context context) {
            super(context);
        }

        @Override
        protected void onDraw(final Canvas canvas) {
            mMyPaint.setColor(Color.RED);
            mMyPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, SIDE_LENGHT, SIDE_LENGHT, mMyPaint);
        }
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
        initCloudMatch();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
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
    public static class ErrorDialogFragment extends DialogFragment {
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
            ((PAVActivity) getActivity()).onDialogDismissed();
        }
    }
}
