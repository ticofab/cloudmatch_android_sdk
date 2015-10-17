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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import io.ticofab.cm_android_sdk.library.CloudMatch;
import io.ticofab.cm_android_sdk.library.consts.GesturePurpose;
import io.ticofab.cm_android_sdk.library.consts.MovementType;
import io.ticofab.cm_android_sdk.library.consts.Movements;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotInitializedException;
import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchViewInterface;
import io.ticofab.cm_android_sdk.library.models.inputs.GesturePurposeInfo;
import io.ticofab.cm_android_sdk.library.views.CloudMatchPinchViewHorizontal;
import io.cloudmatch.demo.R;

/*
 * This demo lets the user pair two devices, pinching them across the long side. The devices
 * will understand their relative position (left or right) and the corresponding image will be shown.
 */
public class PinchAndViewDemoActivity extends Activity {
    private static final String TAG = PinchAndViewDemoActivity.class.getSimpleName();

    private Point mScreenDimensions;
    private int mHalfScreenY;
    private int mHalfScreenX;
    private int mPointEndY;
    private int mPointEndX;
    private int mIVWidth;
    private int mIVHeigth;
    private int mIVTopX;
    private int mIVTopY;

    private PinchAndViewDemoScreenPositions mPosition;

    /*
     * Implementation of the PinchOnMatchedInterface. Upon a successful matching, the corresponding image is shown.
     */
    private final PinchAndViewOnMatchedInterface mMatchedInterface = new PinchAndViewOnMatchedInterface() {

        @Override
        public void onMatched(final String groupId, final int groupSize,
                              final PinchAndViewDemoScreenPositions position) {

            final String txt = "Matched in a group of " + groupSize + ", my position is " + position + ".";
            Toast.makeText(PinchAndViewDemoActivity.this, txt, Toast.LENGTH_LONG).show();

            if (position == PinchAndViewDemoScreenPositions.unknown) {
                return;
            }

            mPosition = position;

            // send message to the other guy with my data
            PinchAndViewDeliveryHelper.sendImageHeight(groupId, mIVHeigth);
        }

        @Override
        public void onOtherMeasurements(final int othersImageHeight) {
            Log.d(TAG, "received measurement: " + othersImageHeight + ", mine is " + mIVHeigth);
            if (othersImageHeight > mIVHeigth) {
                // display it as it is
                displayImage();
            } else {
                // recalculate positions and dimensions
                {
                    mIVHeigth = othersImageHeight;
                    mIVWidth = mIVHeigth * mScreenDimensions.x / mScreenDimensions.y;
                    mIVTopY = mPointEndY - (mIVHeigth / 2);
                    mIVTopX = mPointEndX > mHalfScreenX ? mScreenDimensions.x - mIVWidth : 0;

                    displayImage();
                }
            }

        }

        private void displayImage() {
            final LayoutParams params = new LayoutParams(mIVWidth, mIVHeigth);
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

    private RelativeLayout mContainerRL;
    private ImageView mPinchInstructionsIcon;
    private CloudMatchPinchViewHorizontal mPinchView;
    private final PinchAndViewDemoServerEvent mPinchDemoSEL = new PinchAndViewDemoServerEvent(this,
            mMatchedInterface);
    private ImageView mImage;
    private MyRectView mMyRectView;

    private final Handler mHandler = new Handler();
    private final Runnable mRemoveViewRunnable = new Runnable() {

        @Override
        public void run() {
            mContainerRL.removeView(mMyRectView);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinch_and_view_demo);

        mContainerRL = (RelativeLayout) findViewById(R.id.container_view);
        mMyRectView = new MyRectView(this);
        mPinchInstructionsIcon = (ImageView) findViewById(R.id.pinchandview_pinch_instruction_iv);

        // get the CloudMatchView object (defined in the xml layout) and set its interface.
        mPinchView = (CloudMatchPinchViewHorizontal) findViewById(R.id.pinch_view);
        mPinchView.setCloudMatchInterface(mPinchDemoSMVI);

        if (servicesConnected()) {
            initCloudMatch();
        }

        mImage = (ImageView) findViewById(R.id.image_position);
        mImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                mImage.setVisibility(View.GONE);
                mPinchInstructionsIcon.setVisibility(View.VISIBLE);
            }
        });

        mScreenDimensions = PinchAndViewDisplayHelper.getScreenSize(this);
        mHalfScreenY = mScreenDimensions.y / 2;
        mHalfScreenX = mScreenDimensions.x / 2;
    }

    public void initCloudMatch() {
        // Initializes the CloudMatch. Here the connection is established immediately after, but it could be done
        // at a different stage.
        try {
            CloudMatch.init(this, mPinchDemoSEL);
            CloudMatch.connect();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
     * Always resume the CloudMatch in the onResume() method.
     */
    @Override
    public void onResume() {
        super.onResume();
        try {
            CloudMatch.onResume();
        } catch (final CloudMatchNotInitializedException e) {
            // handle exception
        }
    }

    /*
     * Always pause the CloudMatch in the onPause() method.
     */
    @Override
    public void onPause() {
        super.onPause();
        try {
            CloudMatch.onPause();
        } catch (final CloudMatchNotInitializedException e) {
            // handle exception
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

    /*
     * Pass touch events to the CloudMatchView.
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        mPinchView.onTouchEvent(event);
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
                    mIVHeigth = (mScreenDimensions.y - mPointEndY) * 2;
                    mIVTopY = mScreenDimensions.y - mIVHeigth;
                } else {
                    mIVHeigth = mPointEndY * 2;
                    mIVTopY = 0;
                }

                mIVWidth = mIVHeigth * mScreenDimensions.x / mScreenDimensions.y;
                if (mPointEndX > mHalfScreenX) {
                    mIVTopX = mScreenDimensions.x - mIVWidth;
                } else {
                    mIVTopX = 0;
                }

                Log.d(TAG, "iv height: " + mIVHeigth + ", width: " + mIVWidth + ", y: " + mIVTopY + ", x: "
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

        @Override
        public String getEqualityParam() {
            return null;
        }

        @Override
        public GesturePurposeInfo getGesturePurposeInfo() {
            return new GesturePurposeInfo(GesturePurpose.group_creation);
        }
    };

    /*
     * Tiny custom view to show a rectangle at the screen end where the matching movement has happened.
     */
    private class MyRectView extends View {
        public static final int SIDE_LENGHT = 20;

        public MyRectView(final Context context) {
            super(context);
        }

        @Override
        protected void onDraw(final Canvas canvas) {
            final Paint myPaint = new Paint();
            myPaint.setColor(Color.RED);
            myPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, SIDE_LENGHT, SIDE_LENGHT, myPaint);
        }
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
