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

package io.ticofab.cm_android_sdk.library.consts;

/**
 * JSON labels for general purpose.
 * 
 * @author @ticofab
 * 
 */
public class JsonLabels {
    // top level
    public static final String KIND = "kind";
    public static final String TYPE = "type";
    public static final String OUTCOME = "outcome";
    public static final String PAYLOAD = "payload";
    public static final String REASON = "reason";

    // response labels
    public static final String GROUP_ID = "groupId";
    public static final String MATCHEE_ID = "matcheeId";
    public static final String POSITION_SCHEME = "scheme";
    public static final String MOVEMENT_TYPE = "movementType";
    public static final String MY_ID_IN_GROUP = "myId";

    // input labels
    public static final String DELIVERY_TAG = "tag";
    public static final String INPUT_RECIPIENTS = "recipients";
    public static final String INPUT_TOTAL_CHUNKS = "totalChunks";
    public static final String INPUT_CHUNK_NUMBER = "chunkNr";
    public static final String INPUT_DELIVERY_ID = "deliveryId";
}
