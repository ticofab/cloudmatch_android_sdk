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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import io.ticofab.cm_android_sdk.library.consts.GesturePurpose;
import io.ticofab.cm_android_sdk.library.consts.MovementType;
import io.ticofab.cm_android_sdk.library.consts.Movements;
import io.ticofab.cm_android_sdk.library.interfaces.CloudMatchViewInterface;
import io.ticofab.cm_android_sdk.library.models.inputs.GesturePurposeInfo;
import io.ticofab.cm_android_sdk.library.views.CloudMatchSwipeViewAllSides;
import io.cloudmatch.demo.R;

/*
 * This layout will take care of managing the swipe drawing on screen and will listen to the
 * callbacks from the CloudMatchView (in this case a CloudMatchSwipeViewAllSides).
 */
public class SwipeAndColorDemoDrawingLayout extends RelativeLayout {

    private CloudMatchSwipeViewAllSides mSwipeAllSidesView;

    // drawing stuff
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    public SwipeAndColorDemoDrawingLayout(final Context context) {
        super(context);
        initStuff(context);
    }

    public SwipeAndColorDemoDrawingLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initStuff(context);
    }

    private void initStuff(final Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.swipe_and_color_demo_drawing_layout, this);

        mSwipeAllSidesView = (CloudMatchSwipeViewAllSides) findViewById(R.id.swipe_and_color_swipe_view);

        // the interface that is set here is just a stub - it doesn't do anything special.
        mSwipeAllSidesView.setCloudMatchInterface(new CloudMatchViewInterface() {

            @Override
            public void onMovementDetection(final Movements arg0,
                                            final MovementType arg1,
                                            final PointF arg2,
                                            final PointF arg3) {
                // do nothing

            }

            @Override
            public void onError(final RuntimeException arg0) {
                // do nothing
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
        });

        // setup drawing stuff
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        // first of all, pass the touch to the CloudMatchView
        mSwipeAllSidesView.onTouchEvent(event);

        // then, take care of drawing the swipe on screen
        final float drawingX = event.getX();
        final float drawingY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPaint.setColor(Color.BLACK);
                touchStart(drawingX, drawingY);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(drawingX, drawingY);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }

        invalidate();
        return true;
    }

    /*
     * follow methods to draw the swipes on screen
     */
    private void touchStart(final float x, final float y) {
        cleanCanvas();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(final float x, final float y) {
        final float dx = Math.abs(x - mX);
        final float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
        // commit the path to our off screen
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();
    }

    public void cleanCanvas() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
