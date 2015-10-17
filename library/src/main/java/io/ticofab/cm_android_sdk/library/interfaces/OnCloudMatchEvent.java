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

import io.ticofab.cm_android_sdk.library.models.messages.MatcheeDelivery;
import io.ticofab.cm_android_sdk.library.models.messages.MatcheeLeftMessage;
import io.ticofab.cm_android_sdk.library.models.responses.DeliveryResponse;
import io.ticofab.cm_android_sdk.library.models.responses.LeaveGroupResponse;
import io.ticofab.cm_android_sdk.library.models.responses.MatchResponse;

/**
 * Interface used to notify the client of a server event.
 *
 * @author @ticofab
 */
public interface OnCloudMatchEvent {
    void onConnectionOpen();

    void onConnectionClosed();

    void onConnectionError(Exception error);

    void onMatchResponse(MatchResponse response);

    void onLeaveGroupResponse(LeaveGroupResponse response);

    void onDeliveryResponse(DeliveryResponse response);

    void onDeliveryProgress(String tag, String deliveryId, int progress);

    void onMatcheeDeliveryProgress(String tag, int progress);

    void onMatcheeDelivery(MatcheeDelivery delivery);

    void onMatcheeLeft(MatcheeLeftMessage message);
}
