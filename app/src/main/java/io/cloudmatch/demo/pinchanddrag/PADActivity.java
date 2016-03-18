/*
 * Copyright 2014 CloudMatch.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudmatch.demo.pinchanddrag;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.cloudmatch.demo.R;
import io.ticofab.cm_android_sdk.library.consts.GesturePurpose;
import io.ticofab.cm_android_sdk.library.consts.MovementType;
import io.ticofab.cm_android_sdk.library.consts.Movements;
import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchViewInterface;
import io.ticofab.cm_android_sdk.library.interfaces.LocationProvider;
import io.ticofab.cm_android_sdk.library.models.inputs.GesturePurposeInfo;
import io.ticofab.cm_android_sdk.library.views.CloudMatchPinchViewHorizontal;

/*
 * This is the most complex demo. There are two phases: the first phase is when two devices are matched, by pinching
 * across their screens on the long side. Once a match has been established, one of the two devices involved displays two shapes.
 * At that point, either shape can be dragged across on to the other device. This effect is achieved using this mechanism:
 *
 *   1. The device where a shape starts to be dragged sends a message to the other one. A shape being dropped on the side
 *      of the first device will temporarily disappear.
 *
 *   2. Upon receiving such message, and for the duration of an interval, the other device "awaits" the shape:
 *      it means that it will show it if a drag action starts on one of the two sides.
 *
 *   3. If the shape is then dropped in the center area of the second device, it will "acquire it" and send an ACK message
 *      to the first device, which won't make the shape appear again.
 */
public class PADActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static final String TAG = PADActivity.class.getSimpleName();
    static final String DRAG_LABEL = "shape";
    static final String CIRCLE_STRING = "circle";
    static final String RECT_STRING = "rect";

    static final int SHAPE_VISIBILITY_RESET_INTERVAL = 3000;
    static final int DRAGGING_CANCEL_INTERVAL = 3000; // milliseconds

    // UI stuff
    @Bind(R.id.rect_shape) ImageView mRectIV;
    @Bind(R.id.left_view) RelativeLayout mLeftRL;
    @Bind(R.id.circle_shape) ImageView mCircleIV;
    @Bind(R.id.right_view) RelativeLayout mRightRL;
    @Bind(R.id.container_view) RelativeLayout mContainerRL;
    @Bind(R.id.pinch_view) CloudMatchPinchViewHorizontal mPinchView;
    @Bind(R.id.pinchanddrag_pinch_instruction_iv) ImageView mPinchInstructionsIcon;

    String mGroupId;
    boolean mIHaveCircle;
    boolean mIHaveSquare;
    Double mCoinTossMyValue;
    MyCircleView mMyCircleView;
    PADDeliveryHelper mPNDDeliveryHelper;
    String mShapeBeingDraggedOnOtherSide = "";
    final Handler mWaitingForDragHandler = new Handler();
    final Handler mShapeVisibilityHandler = new Handler();

    // location stuff
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;

    private final Handler mHandler = new Handler();
    private final Runnable mRemoveViewRunnable = new Runnable() {

        @Override
        public void run() {
            mContainerRL.removeView(mMyCircleView);
        }
    };

    // implementation of the PinchAndDragMatchedInterface
    private final PADMatchedInterface mMatchedInterface = new PADMatchedInterface() {

        @Override
        public void onMatched(final String groupId) {
            mGroupId = groupId;

            final String txt = "Matched in group " + groupId;
            Toast.makeText(PADActivity.this, txt, Toast.LENGTH_LONG).show();

            // do the "coin toss" to decide who gets the shapes first
            mCoinTossMyValue = new Random().nextDouble();
            mPNDDeliveryHelper.sendCointoss(groupId, mCoinTossMyValue);

            mPinchInstructionsIcon.setVisibility(View.GONE);
        }

        @Override
        public void onMatcheeLeft() {
            mGroupId = null;
            final String txt = "Everybody left";
            Toast.makeText(PADActivity.this, txt, Toast.LENGTH_LONG).show();

            mIHaveCircle = false;
            mIHaveSquare = false;
            setShapesVisibility();
            mPinchInstructionsIcon.setVisibility(View.VISIBLE);
        }
    };

    private void setShapesVisibility() {
        mCircleIV.setVisibility(mIHaveCircle ? View.VISIBLE : View.INVISIBLE);
        mRectIV.setVisibility(mIHaveSquare ? View.VISIBLE : View.INVISIBLE);
    }

    /*
     * Implementation of PinchAndDragDeliveryInterface, triggered in response to deliveries from other devices in
     * the same group.
     */
    private final PADDeliveryInterface mDeliveryInterface = new PADDeliveryInterface() {

        // upon receiving a coin toss, check who has the biggest value
        @Override
        public void onCoinToss(final Double value) {
            final boolean haveStuff = mCoinTossMyValue > value;
            mIHaveCircle = haveStuff;
            mIHaveSquare = haveStuff;
            setShapesVisibility();
        }

        // the other side started to drag a shape over.
        @Override
        public void onShapeDragInitiatedOnOtherSide(final String shape) {
            mShapeBeingDraggedOnOtherSide = shape;

            // if I don't receive the shape within some interval, stop waiting for it.
            mWaitingForDragHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mShapeBeingDraggedOnOtherSide = "";
                }
            }, DRAGGING_CANCEL_INTERVAL);
        }

        // the other side stopped dragging a shape.
        @Override
        public void onShapeDragStoppedOnOtherSide() {
            // the other side stopped dragging a shape.
            mShapeBeingDraggedOnOtherSide = "";
        }

        // the other side has received a shape, so he has it now.
        @Override
        public void onShapeReceivedOnOtherSide(final String shape) {
            // the other side got it. Don't make it back to visibility.
            final boolean haveLostCircle = shape.equals(CIRCLE_STRING);
            if (haveLostCircle) {
                mIHaveCircle = false;
            } else {
                mIHaveSquare = false;
            }
            setShapesVisibility();
            mShapeVisibilityHandler.removeCallbacksAndMessages(null);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinch_and_drag_demo);
        ButterKnife.bind(this);

        // initialize here or otherwise 'this' & the view will be null
        mMyCircleView = new MyCircleView(this);
        mPNDDeliveryHelper = new PADDeliveryHelper(mPinchView);

        // init google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();
    }

    public void initCloudMatch() {
        // initializes the CloudMatch. In this case we also immediately connect,
        // but it could be done also at a different stage.
        try {
            // TODO: implement LocationProvider
            mPinchView.initCloudMatch(this,
                    new PADServerEventListener(this, mMatchedInterface, mDeliveryInterface),
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
            mPinchView.connect();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // get the CloudMatchPinchViewHorizontal object from the xml layout and sets the interface on it.
        mRectIV.setOnTouchListener(new ShapeOnTouchListener());
        mCircleIV.setOnTouchListener(new ShapeOnTouchListener());

        // setup gesture detector and areas
        mRightRL.setOnDragListener(new SideAreaDragListener(SideAreas.Right));
        mRightRL.setOnTouchListener(new SideAreaOnTouchListener(SideAreas.Right));

        mLeftRL.setOnDragListener(new SideAreaDragListener(SideAreas.Left));
        mLeftRL.setOnTouchListener(new SideAreaOnTouchListener(SideAreas.Left));

        final RelativeLayout centerRL = (RelativeLayout) findViewById(R.id.center_view);
        centerRL.setOnDragListener(new CenterAreaDragListener());
    }

    /*
     * Closing the connection in the onDestroy() method.
     */
    @Override
    public void onDestroy() {
        mPinchView.closeConnection();
        super.onDestroy();
    }

    /*
     * Pass touches to the CloudMatchView object.
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        mPinchView.onTouchEvent(event);
        return true;
    }

    /*
     * Implementation of CloudMatchViewInterface. When a matching movement is detected, we'll show a little red
     * circle at the side of the screen where the gesture ended, and remove it after a little time.
     */
    private final CloudMatchViewInterface mPinchDemoSMVI = new CloudMatchViewInterface() {

        @Override
        public void onMovementDetection(final Movements movement, final MovementType movementType,
                                        final PointF pointStart, final PointF pointEnd) {
            Log.d(TAG, "onMovementDetection. Movement: " + movement + ", swipeType: " + movementType
                    + ", point start: " + pointStart + ", point end: " + pointEnd);

            // remove existing stuff if there
            mContainerRL.removeView(mMyCircleView);
            mWaitingForDragHandler.removeCallbacksAndMessages(null);

            // only display circle if we're not already connected
            if (TextUtils.isEmpty(mGroupId)) {
                // position the little rect view at the appropriate coordinates
                final int side = MyCircleView.RADIUS * 2;
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
                params.setMargins(10, (int) pointEnd.y, 10, 0);
                mContainerRL.addView(mMyCircleView, params);

                mHandler.postDelayed(mRemoveViewRunnable, 2000);
            }
        }

        @Override
        public void onError(final RuntimeException e) {
            Log.d(TAG, "onError: " + e);
        }

        @Override
        public boolean isGestureValid() {
            // only attempt to connect if we're not already connected
            return TextUtils.isEmpty(mGroupId);
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

    // a little custom view to show where a matching movement ended.
    private class MyCircleView extends View {
        public static final int RADIUS = 15;

        public MyCircleView(final Context context) {
            super(context);
        }

        @Override
        protected void onDraw(final Canvas canvas) {
            final Paint myPaint = new Paint();
            myPaint.setColor(Color.RED);
            myPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(RADIUS, RADIUS, RADIUS, myPaint);
        }
    }

    // enum to facilitate dragging logic
    private enum SideAreas {
        Left, Right
    }

    // invoked when a shape has been dropped in this device from another device.
    // It will be "acquired" and a confirmation will be sent to the other one.
    private void shapeHasBeenDropped() {
        if (!TextUtils.isEmpty(mShapeBeingDraggedOnOtherSide)) {
            final boolean isDraggingCircle = mShapeBeingDraggedOnOtherSide.equals(CIRCLE_STRING);

            if (isDraggingCircle && !mIHaveCircle) {
                // a circle has been dragged from the other side
                mIHaveCircle = true;
            } else if (!mIHaveSquare) {
                // a square has been dragged from the other side
                mIHaveSquare = true;
            }

            setShapesVisibility();

            mPNDDeliveryHelper.sendShapeReceivedAck(mGroupId, isDraggingCircle ? CIRCLE_STRING : RECT_STRING);
        }
    }

    // listener that detects if a drag has ended. Used on the central area.
    private class CenterAreaDragListener implements OnDragListener {

        @Override
        public boolean onDrag(final View v, final DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                if (TextUtils.isEmpty(mShapeBeingDraggedOnOtherSide)) {
                    // we were dragging our own shape and dropped in in the center
                    setShapesVisibility();

                    mPNDDeliveryHelper.sendShapeDragStopped(mGroupId);
                } else {
                    // a shape has been dragged to us
                    shapeHasBeenDropped();
                }
            }

            return true;
        }

    }

    // listener that detects a dragging action on a side that will be specified in the constructor.
    private class SideAreaDragListener implements OnDragListener {

        private final SideAreas mMyself;

        public SideAreaDragListener(final SideAreas area) {
            mMyself = area;
        }

        @Override
        public boolean onDrag(final View v, final DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                final String tag = event.getClipData().getItemAt(0).getText().toString();
                Log.d(TAG, "shape-to-side drop, tag: " + tag + ", end area: " + mMyself.name());

                // I assume that if we get here we are connected
                if (TextUtils.isEmpty(mShapeBeingDraggedOnOtherSide)) {
                    // we dropped a shape on the side to transfer it to the other side

                    final View viewToRestore = tag.equals(CIRCLE_STRING) ? mCircleIV : mRectIV;
                    mShapeVisibilityHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            Log.d(TAG, "mShapeVisibilityHandler, didn't receive any ack.");
                            viewToRestore.setVisibility(View.VISIBLE);
                            mPNDDeliveryHelper.sendShapeDragStopped(mGroupId);
                        }
                    }, SHAPE_VISIBILITY_RESET_INTERVAL);

                } else {
                    // a shape has been dragged to us
                    shapeHasBeenDropped();
                }
            }
            return true;
        }
    }

    // Upon touching a shape, we start to drag it.
    private class ShapeOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            // we should get here only if the shape is visible
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                final DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                final String shapeTag = (String) v.getTag();
                final ClipData tag = ClipData.newPlainText(DRAG_LABEL, shapeTag);
                v.startDrag(tag, shadowBuilder, v, 0);
                v.setVisibility(View.INVISIBLE);

                mPNDDeliveryHelper.sendShapeDragStart(mGroupId, shapeTag);

                return true;
            }

            return false;
        }
    }

    // if a movement is detected on the side area, and we know that a shape is being dragged from the other side,
    // let's "drag the shape in", simulating the passage between the two screens.
    private class SideAreaOnTouchListener implements OnTouchListener {

        private final SideAreas mArea;

        public SideAreaOnTouchListener(final SideAreas area) {
            mArea = area;
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            // move grey ball to where the touch started
            if (event.getAction() == MotionEvent.ACTION_DOWN && !TextUtils.isEmpty(mShapeBeingDraggedOnOtherSide)) {
                final boolean isDraggingCircle = mShapeBeingDraggedOnOtherSide.equals(CIRCLE_STRING);
                final View draggedView = isDraggingCircle ? mCircleIV : mRectIV;
                final DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(draggedView);
                final ClipData tag = ClipData.newPlainText(DRAG_LABEL, mArea.name());
                draggedView.startDrag(tag, shadowBuilder, draggedView, 0);
                return true;
            }

            return false;
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
    public void onConnectionFailed(ConnectionResult result) {
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
    public static class ErrorDialogFragment extends android.support.v4.app.DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((PADActivity) getActivity()).onDialogDismissed();
        }
    }
}
