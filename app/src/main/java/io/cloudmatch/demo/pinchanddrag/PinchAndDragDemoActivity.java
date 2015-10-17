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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Random;

import io.cloudmatch.demo.R;
import io.ticofab.cm_android_sdk.library.CloudMatch;
import io.ticofab.cm_android_sdk.library.consts.GesturePurpose;
import io.ticofab.cm_android_sdk.library.consts.MovementType;
import io.ticofab.cm_android_sdk.library.consts.Movements;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchNotInitializedException;
import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchViewInterface;
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
public class PinchAndDragDemoActivity extends Activity {
    private static final String TAG = PinchAndDragDemoActivity.class.getSimpleName();
    private static final String DRAG_LABEL = "shape";

    // the groupId will be stored here.
    private String mGroupId;

    // implementation of the PinchAndDragMatchedInterface
    private final PinchAndDragMatchedInterface mMatchedInterface = new PinchAndDragMatchedInterface() {

        @Override
        public void onMatched(final String groupId) {
            mGroupId = groupId;

            final String txt = "Matched in group " + groupId;
            Toast.makeText(PinchAndDragDemoActivity.this, txt, Toast.LENGTH_LONG).show();

            // do the "coin toss" to decide who gets the shapes first
            mCoinTossMyValue = new Random().nextDouble();
            mPinchAndDragDeliveryHelper.sendCointoss(groupId, mCoinTossMyValue);

            mPinchInstructionsIcon.setVisibility(View.GONE);
        }

        @Override
        public void onMatcheeLeft() {
            mGroupId = null;
            final String txt = "Everybody left";
            Toast.makeText(PinchAndDragDemoActivity.this, txt, Toast.LENGTH_LONG).show();

            mIHaveCircle = false;
            mIHaveSquare = false;
            setShapesVisibility();
            mPinchInstructionsIcon.setVisibility(View.VISIBLE);
        }
    };

    // delivery stuff
    private String mShapeBeingDraggedOnOtherSide = "";
    private Double mCoinTossMyValue;
    private String mCircleString;
    private String mRectString;
    private boolean mIHaveCircle;
    private boolean mIHaveSquare;
    private final PinchAndDragDeliveryHelper mPinchAndDragDeliveryHelper = new PinchAndDragDeliveryHelper();
    private final Handler mWaitingForDragHandler = new Handler();
    private static final int DRAGGING_CANCEL_INTERVAL = 3000; // milliseconds
    private final Handler mShapeVisibilityHandler = new Handler();
    private static final int SHAPE_VISIBILITY_RESET_INTERVAL = 3000;

    private void setShapesVisibility() {
        mCircleIV.setVisibility(mIHaveCircle ? View.VISIBLE : View.INVISIBLE);
        mRectIV.setVisibility(mIHaveSquare ? View.VISIBLE : View.INVISIBLE);
    }

    /*
     * Implementation of PinchAndDragDeliveryInterface, triggered in response to deliveries from other devices in
     * the same group.
     */
    private final PinchAndDragDeliveryInterface mDeliveryInterface = new PinchAndDragDeliveryInterface() {

        // upon receiving a cointoss, check who has the biggest value
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
            final boolean haveLostCircle = shape.equals(mCircleString);
            if (haveLostCircle) {
                mIHaveCircle = false;
            } else {
                mIHaveSquare = false;
            }
            setShapesVisibility();
            mShapeVisibilityHandler.removeCallbacksAndMessages(null);
        }
    };

    // UI stuff
    private RelativeLayout mContainerRL;
    private ImageView mPinchInstructionsIcon;
    private CloudMatchPinchViewHorizontal mPinchView;
    private MyCircleView mMyCircleView;
    private ImageView mRectIV;
    private ImageView mCircleIV;

    // create the server event handler object, passing it the two interfaces
    private final PinchAndDragDemoServerEvent mPinchAndDragDemoSEL = new PinchAndDragDemoServerEvent(this,
            mMatchedInterface, mDeliveryInterface);

    private final Handler mHandler = new Handler();
    private final Runnable mRemoveViewRunnable = new Runnable() {

        @Override
        public void run() {
            mContainerRL.removeView(mMyCircleView);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinch_and_drag_demo);

        mContainerRL = (RelativeLayout) findViewById(R.id.container_view);
        mPinchInstructionsIcon = (ImageView) findViewById(R.id.pinchanddrag_pinch_instruction_iv);
        mMyCircleView = new MyCircleView(this);

        // get the CloudMatchPinchViewHorizontal object from the xml layout and sets the interface on it.
        mPinchView = (CloudMatchPinchViewHorizontal) findViewById(R.id.pinch_view);
        mPinchView.setCloudMatchInterface(mPinchDemoSMVI);

        mRectIV = (ImageView) findViewById(R.id.rect_shape);
        mRectIV.setOnTouchListener(new ShapeOnTouchListener());

        mCircleIV = (ImageView) findViewById(R.id.circle_shape);
        mCircleIV.setOnTouchListener(new ShapeOnTouchListener());

        mCircleString = getString(R.string.circle);
        mRectString = getString(R.string.rect);

        // setup gesture detector and areas
        final RelativeLayout rightRL = (RelativeLayout) findViewById(R.id.right_view);
        rightRL.setOnDragListener(new SideAreaDragListener(SideAreas.Right));
        rightRL.setOnTouchListener(new SideAreaOnTouchListener(SideAreas.Right));

        final RelativeLayout leftRL = (RelativeLayout) findViewById(R.id.left_view);
        leftRL.setOnDragListener(new SideAreaDragListener(SideAreas.Left));
        leftRL.setOnTouchListener(new SideAreaOnTouchListener(SideAreas.Left));

        final RelativeLayout centerRL = (RelativeLayout) findViewById(R.id.center_view);
        centerRL.setOnDragListener(new CenterAreaDragListener());

        if (servicesConnected()) {
            initCloudMatch();
        }
    }

    public void initCloudMatch() {
        // initializes the CloudMatch. In this case we also immediately connect, but it could be done also at a
        // different stage.
        try {
            CloudMatch.init(this, mPinchAndDragDemoSEL);
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
            final boolean isDraggingCircle = mShapeBeingDraggedOnOtherSide.equals(mCircleString);

            if (isDraggingCircle && !mIHaveCircle) {
                // a circle has been dragged from the other side
                mIHaveCircle = true;
            } else if (!mIHaveSquare) {
                // a square has been dragged from the other side
                mIHaveSquare = true;
            }

            setShapesVisibility();

            mPinchAndDragDeliveryHelper.sendShapeReceivedAck(mGroupId, isDraggingCircle ? mCircleString
                    : mRectString);
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

                    mPinchAndDragDeliveryHelper.sendShapeDragStopped(mGroupId);
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

                    final View viewToRestore = tag.equals(mCircleString) ? mCircleIV : mRectIV;
                    mShapeVisibilityHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            Log.d(TAG, "mShapeVisibilityHandler, didn't receive any ack.");
                            viewToRestore.setVisibility(View.VISIBLE);
                            mPinchAndDragDeliveryHelper.sendShapeDragStopped(mGroupId);
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

                mPinchAndDragDeliveryHelper.sendShapeDragStart(mGroupId, shapeTag);

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
                final boolean isDraggingCircle = mShapeBeingDraggedOnOtherSide.equals(mCircleString);
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
