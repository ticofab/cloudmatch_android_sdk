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

package io.ticofab.cm_android_sdk.library.helpers;

import java.util.UUID;

/**
 * Helper to retrieve the unique id of a device.
 * 
 * @author @ticofab
 * 
 */
public class UniqueIDHelper {

    public static String createDeliveryId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 3);
    }
}
