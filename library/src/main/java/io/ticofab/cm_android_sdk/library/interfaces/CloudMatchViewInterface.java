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

package io.ticofab.cm_android_sdk.library.interfaces;

import android.graphics.PointF;

import io.ticofab.cm_android_sdk.library.consts.MovementType;
import io.ticofab.cm_android_sdk.library.consts.Movements;
import io.ticofab.cm_android_sdk.library.models.inputs.GesturePurposeInfo;

/**
 * Interface used in the CloudMatch custom views to communicate with the client using them.
 */
public interface CloudMatchViewInterface {
    void onMovementDetection(Movements movement, MovementType movementType, PointF swipeStart,
                             PointF swipeEnd);

    boolean isGestureValid();

    GesturePurposeInfo getGesturePurposeInfo();

    String getEqualityParam();

    void onError(RuntimeException e);
}
