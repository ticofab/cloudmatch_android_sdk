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

package io.ticofab.cm_android_sdk.library.views.base;

import android.content.Context;
import android.util.AttributeSet;

import io.ticofab.cm_android_sdk.library.consts.Areas;

/**
 * Abstract specification of CloudMatchAcrossView which enables communication on the horizontal sides of the
 * screen.
 */
abstract public class CloudMatchAcrossViewHorizontal extends CloudMatchAcrossView {
    public CloudMatchAcrossViewHorizontal(final Context context) {
        super(context);
    }

    public CloudMatchAcrossViewHorizontal(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    private Integer mLeftEdge;
    private Integer mRightEdge;

    @Override
    protected Areas getArea(final float x, final float y) {
        Areas result = Areas.inner;
        if (x < mLeftEdge + SIDE_AREA_WIDTH) {
            result = Areas.left;
        }
        if (x > mRightEdge - SIDE_AREA_WIDTH) {
            result = Areas.right;
        }
        return result;
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // here you have the size of the view and you can do stuff
        mRightEdge = getRight();
        mLeftEdge = getLeft();
    }
}
