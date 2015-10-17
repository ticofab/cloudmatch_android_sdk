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

package io.ticofab.cm_android_sdk.library.views;

import android.content.Context;
import android.util.AttributeSet;

import io.ticofab.cm_android_sdk.library.consts.Criteria;
import io.ticofab.cm_android_sdk.library.views.base.CloudMatchAcrossViewHorizontal;

/**
 * Custom view to be embedded by the client in its layouts. Will provide Swipe interaction on the horizontal sides
 * of the screen.
 * 
 * @author @ticofab
 * 
 */
public class CloudMatchSwipeViewHorizontal extends CloudMatchAcrossViewHorizontal {

    public CloudMatchSwipeViewHorizontal(final Context context) {
        super(context);
        mCriteria = Criteria.swipe;
    }

    public CloudMatchSwipeViewHorizontal(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mCriteria = Criteria.swipe;
    }
}
