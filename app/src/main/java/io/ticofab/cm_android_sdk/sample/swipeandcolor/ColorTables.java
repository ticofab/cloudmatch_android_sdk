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

package io.ticofab.cm_android_sdk.sample.swipeandcolor;

import android.graphics.Color;

/*
 * This object contains statically initialized tables which define the color rotation, up to eight devices.
 */
public class ColorTables {
    private static int[] ERROR_TABLE = new int[]{-1};

    private static final int CLOUDMATCH_BLUE = Color.parseColor("#13B4DB");
    private static final int CLOUDMATCH_RED = Color.parseColor("#EB5F60");

    private static int[] TWO_COLORS_TABLE = new int[]{
            CLOUDMATCH_RED,
            CLOUDMATCH_BLUE};

    private static int[] THREE_COLORS_TABLE = new int[]{
            CLOUDMATCH_RED,
            CLOUDMATCH_BLUE,
            Color.GREEN,};

    private static int[] FOUR_COLORS_TABLE = new int[]{
            CLOUDMATCH_RED,
            CLOUDMATCH_BLUE,
            Color.GREEN,
            Color.YELLOW};

    private static int[] FIVE_COLORS_TABLE = new int[]{
            CLOUDMATCH_RED,
            CLOUDMATCH_BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.BLACK};

    private static int[] SIX_COLORS_TABLE = new int[]{
            CLOUDMATCH_RED,
            CLOUDMATCH_BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.BLACK,
            Color.GRAY};

    private static int[] SEVEN_COLORS_TABLE = new int[]{
            CLOUDMATCH_RED,
            CLOUDMATCH_BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.BLACK,
            Color.GRAY,
            Color.CYAN};

    private static int[] EIGHT_COLORS_TABLE = new int[]{
            CLOUDMATCH_RED,
            CLOUDMATCH_BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.BLACK,
            Color.GRAY,
            Color.CYAN,
            Color.MAGENTA};

    public static int[] getColorTable(final int groupSize) {
        switch (groupSize) {
            case 2:
                return TWO_COLORS_TABLE;
            case 3:
                return THREE_COLORS_TABLE;
            case 4:
                return FOUR_COLORS_TABLE;
            case 5:
                return FIVE_COLORS_TABLE;
            case 6:
                return SIX_COLORS_TABLE;
            case 7:
                return SEVEN_COLORS_TABLE;
            case 8:
                return EIGHT_COLORS_TABLE;
        }
        // error
        return ERROR_TABLE;
    }
}
