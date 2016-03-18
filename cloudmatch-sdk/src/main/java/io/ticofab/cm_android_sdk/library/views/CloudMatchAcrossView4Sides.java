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

package io.ticofab.cm_android_sdk.library.views;

import android.content.Context;
import android.util.AttributeSet;

import io.ticofab.cm_android_sdk.library.consts.Areas;

/**
 * Abstract specification of CloudMatchAcrossView which enables communication on all sides of the screen.
 */
abstract public class CloudMatchAcrossView4Sides extends CloudMatchAcrossView {

    public CloudMatchAcrossView4Sides(final Context context) {
        super(context);
    }

    public CloudMatchAcrossView4Sides(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    private Integer mLeftEdge;
    private Integer mRightEdge;
    private Integer mTopEdge;
    private Integer mBottomEdge;

    @Override
    protected Areas getArea(final float x, final float y) {
        Areas result = Areas.inner;

        final boolean leftSide = x < mLeftEdge + SIDE_AREA_WIDTH;
        final boolean rigthSide = x > mRightEdge - SIDE_AREA_WIDTH;
        final boolean topSide = y < mTopEdge + SIDE_AREA_WIDTH;
        final boolean bottomSide = y > mBottomEdge - SIDE_AREA_WIDTH;
        final boolean topOrBottom = topSide || bottomSide;
        final boolean rightOrLeft = rigthSide || leftSide;

        if (leftSide) {
            result = topOrBottom ? Areas.invalid : Areas.left;
        } else if (rigthSide) {
            result = topOrBottom ? Areas.invalid : Areas.right;
        } else if (topSide) {
            result = rightOrLeft ? Areas.invalid : Areas.top;
        } else if (bottomSide) {
            result = rightOrLeft ? Areas.invalid : Areas.bottom;
        }

        return result;
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // here you have the size of the view and you can do stuff
        mRightEdge = getRight();
        mLeftEdge = getLeft();
        mTopEdge = getTop();
        mBottomEdge = getBottom();
    }
}
