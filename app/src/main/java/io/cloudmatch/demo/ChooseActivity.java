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

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import io.cloudmatch.demo.pinchanddrag.PADActivity;
import io.cloudmatch.demo.pinchandview.PAVActivity;
import io.cloudmatch.demo.swipeandcolor.SACActivity;

/*
 * From this activity we can choose one of the demos.
 */
public class ChooseActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        updateAndroidSecurityProvider();
    }

    // from view
    public void onPinchDemoClick(final View v) {
        final Intent intent = new Intent(this, PAVActivity.class);
        startActivity(intent);
    }

    // from view
    public void onPinchAndDragDemoClick(final View v) {
        final Intent intent = new Intent(this, PADActivity.class);
        startActivity(intent);
    }

    // from view
    public void onSwipeAndColorDemoClick(final View v) {
        final Intent intent = new Intent(this, SACActivity.class);
        startActivity(intent);
    }

    // from view
    public void onAboutClick(final View v) {
        final FragmentManager fm = getFragmentManager();
        final AboutDialogFragment aboutDialog = new AboutDialogFragment();
        aboutDialog.show(fm, "aboutDialog");
    }

    // needed to make the SSL protocol works on older phones too
    private void updateAndroidSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }
}
