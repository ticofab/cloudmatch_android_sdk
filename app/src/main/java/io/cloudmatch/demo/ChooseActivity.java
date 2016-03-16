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

package io.cloudmatch.demo;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.cloudmatch.demo.pinchanddrag.PinchAndDragActivity;
import io.cloudmatch.demo.pinchandview.PinchAndViewActivity;
import io.cloudmatch.demo.swipeandcolor.SwipeAndColorActivity;

/*
 * From this activity we can choose one of the demos.
 */
public class ChooseActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
    }

    // from view
    public void onPinchDemoClick(final View v) {
        final Intent intent = new Intent(this, PinchAndViewActivity.class);
        startActivity(intent);
    }

    // from view
    public void onPinchAndDragDemoClick(final View v) {
        final Intent intent = new Intent(this, PinchAndDragActivity.class);
        startActivity(intent);
    }

    // from view
    public void onSwipeAndColorDemoClick(final View v) {
        final Intent intent = new Intent(this, SwipeAndColorActivity.class);
        startActivity(intent);
    }

    // from view
    public void onAboutClick(final View v) {
        final FragmentManager fm = getFragmentManager();
        final AboutDialogFragment aboutDialog = new AboutDialogFragment();
        aboutDialog.show(fm, "aboutDialog");
    }
}
