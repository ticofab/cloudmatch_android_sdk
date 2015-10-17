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

package io.ticofab.cm_android_sdk.sample.pinchandview;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Helper to deal with the device's screen technicalities.
 *
 * @author @ticofab
 */
public class PinchAndViewDisplayHelper {

    private static float mScreenDensityFactor = -1f;
    private static int mScreenDensityFactorDP = -1;

    public static Point getScreenSize(final Context context) {
        return getScreenSizeInternal(context);
    }

    public static int getScreenWidth(final Context context) {
        return getScreenSizeInternal(context).x;
    }

    private static Point getScreenSizeInternal(final Context context) {
        Point size = null;
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            final Display display = windowManager.getDefaultDisplay();
            if (display != null) {
                // only works if Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2
                size = new Point();
                display.getSize(size);
            }
        }
        return size;
    }

    public static float getDensityFactor(final Context context) {
        if (mScreenDensityFactor == -1f) {
            mScreenDensityFactor = context.getResources().getDisplayMetrics().density;
        }
        return mScreenDensityFactor;
    }

    public static int pixelsToDP(final Context context, final int pixels) {
        if (mScreenDensityFactorDP == -1) {
            mScreenDensityFactorDP = context.getResources().getDisplayMetrics().densityDpi;
        }
        return Math.round(pixels / mScreenDensityFactorDP / 160f);
    }

    public static int dpToPixels(final Context context, final int dp) {
        if (mScreenDensityFactorDP == -1f) {
            mScreenDensityFactorDP = context.getResources().getDisplayMetrics().densityDpi;
        }
        return Math.round(dp * mScreenDensityFactorDP / 160f);
    }
}
