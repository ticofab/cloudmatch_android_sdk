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

package io.ticofab.cm_android_sdk.sample.pinchanddrag;

/*
 * Interface for internal communication about the payload exchanges.
 */
public interface PinchAndDragDeliveryInterface {

    String COIN_TOSS = "cointoss";
    String SHAPE_DRAG = "shapedrag";
    String SHAPE_ACQUISITION_ACK = "shapeack";
    String SHAPE_DRAG_STOPPED = "shapedragstop";

    void onCoinToss(Double value);

    void onShapeDragInitiatedOnOtherSide(String shape);

    void onShapeDragStoppedOnOtherSide();

    void onShapeReceivedOnOtherSide(String shape);

}
