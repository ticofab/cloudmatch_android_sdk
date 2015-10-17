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
import android.graphics.PointF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import io.ticofab.cm_android_sdk.library.consts.Areas;
import io.ticofab.cm_android_sdk.library.consts.MovementType;
import io.ticofab.cm_android_sdk.library.consts.Movements;
import io.ticofab.cm_android_sdk.library.exceptions.CloudMatchViewInterfaceNotSetException;
import io.ticofab.cm_android_sdk.library.helpers.Matcher;
import io.ticofab.cm_android_sdk.library.helpers.MovementHelper;
import io.ticofab.cm_android_sdk.library.models.inputs.GesturePurposeInfo;
import io.ticofab.cm_android_sdk.library.models.inputs.GroupComMatchInput;
import io.ticofab.cm_android_sdk.library.models.inputs.GroupCreateMatchInput;

/**
 * Abstract implementation of CloudMatchView, providing the functionality for interaction when the gesture happens
 * across two screens adjacent to each other.
 */
public abstract class CloudMatchAcrossView extends CloudMatchView {

    public CloudMatchAcrossView(final Context context) {
        super(context);
    }

    public CloudMatchAcrossView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    protected static final int SIDE_AREA_WIDTH = 65; // dp

    private Areas mStartArea = Areas.invalid;
    private PointF mStartPoint;

    @Override
    public boolean onTouchEvent(final MotionEvent event) throws CloudMatchViewInterfaceNotSetException {
        if (mClientInterface == null) {
            throw new CloudMatchViewInterfaceNotSetException();
        }

        final float drawingX = event.getX();
        final float drawingY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartArea = getArea(drawingX, drawingY);
                mStartPoint = new PointF(drawingX, drawingY);
                break;
            case MotionEvent.ACTION_UP:
                final Areas areaEnd = getArea(drawingX, drawingY);
                final Movements movement = MovementHelper.fromAreas(mStartArea, areaEnd);

                // if the movement is valid, send notifications and match request
                if (movement != Movements.invalid && mClientInterface.isGestureValid()) {
                    final PointF pointEnd = new PointF(drawingX, drawingY);
                    final MovementType movementType = MovementHelper.movementToSwipeType(movement);
                    mClientInterface.onMovementDetection(movement, movementType, mStartPoint, pointEnd);
                    final GesturePurposeInfo gesturePurposeInfo = mClientInterface.getGesturePurposeInfo();
                    if (gesturePurposeInfo != null) {
                        sendMatchRequest(mStartArea, areaEnd, gesturePurposeInfo);
                    }
                }

                mStartArea = Areas.invalid;
                mStartPoint = null;
        }

        // let the event propagate through
        return false;
    }

    private void sendMatchRequest(final Areas areaStart, final Areas areaEnd,
                                  final GesturePurposeInfo gesturePurposeInfo) {

        final String equalityParam = mClientInterface.getEqualityParam();

        switch (gesturePurposeInfo.mGesturePurpose) {
            case group_creation:
                final GroupCreateMatchInput matchCreateInput = new GroupCreateMatchInput(mCriteria);
                matchCreateInput.mAreaStart = areaStart;
                matchCreateInput.mAreaEnd = areaEnd;
                if (!TextUtils.isEmpty(equalityParam)) {
                    matchCreateInput.mEqualityParam = equalityParam;
                }
                try {
                    Matcher.sendGroupCreateMatchRequest(matchCreateInput);
                } catch (final RuntimeException e) {
                    mClientInterface.onError(e);
                }
                break;

            case inter_group_communication:
                final GroupComMatchInput matchComInput = new GroupComMatchInput(mCriteria);
                matchComInput.mAreaStart = areaStart;
                matchComInput.mAreaEnd = areaEnd;
                matchComInput.mGroupId = gesturePurposeInfo.mGroupId;
                matchComInput.mIdInGroup = String.valueOf(gesturePurposeInfo.mMyIdInGroup);
                if (!TextUtils.isEmpty(equalityParam)) {
                    matchComInput.mEqualityParam = equalityParam;
                }
                try {
                    Matcher.sendGroupComMatchRequest(matchComInput);
                } catch (final RuntimeException e) {
                    mClientInterface.onError(e);
                }
                break;

            default:
                // TODO: error!
                return;
        }

    }

    protected abstract Areas getArea(final float x, final float y);
}
