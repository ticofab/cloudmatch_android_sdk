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

import io.ticofab.cm_android_sdk.library.consts.Areas;
import io.ticofab.cm_android_sdk.library.consts.MovementType;
import io.ticofab.cm_android_sdk.library.consts.Movements;

/**
 * Helper with the logic to interpret gestures into movements.
 *
 * @author @ticofab
 */
public class MovementHelper {

    public static Movements fromAreas(final Areas start, final Areas end) {
        Movements movement = Movements.invalid;

        try {
            movement = Movements.valueOf(start.name() + end.name());
        } catch (final IllegalArgumentException e) {
            // didn't work, return invalid
        }

        return movement;
    }

    public static MovementType movementToSwipeType(final Movements movement) {
        MovementType movementType = MovementType.undefined;

        switch (movement) {
            case innerbottom:
            case innerleft:
            case innerright:
            case innertop:
                movementType = MovementType.outgoing;
                break;

            case topinner:
            case bottominner:
            case leftinner:
            case rightinner:
                movementType = MovementType.incoming;
                break;

            case leftright:
            case lefttop:
            case leftbottom:
            case bottomright:
            case bottomleft:
            case bottomtop:
            case righttop:
            case rightleft:
            case rightbottom:
            case topleft:
            case topright:
            case topbottom:
                movementType = MovementType.transition;
                break;

            default:
                movementType = MovementType.undefined;
                break;
        }

        return movementType;
    }
}
