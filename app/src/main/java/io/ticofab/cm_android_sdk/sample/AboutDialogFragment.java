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

package io.ticofab.cm_android_sdk.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class AboutDialogFragment extends DialogFragment {

    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setView(getActivity().getLayoutInflater().inflate(R.layout.about_dialog_layout, null))
                .setIcon(R.mipmap.ic_launcher_cmdemo)
                .setTitle(R.string.app_name)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        // just dismiss
                    }
                })
                .setNegativeButton(R.string.about_dialog_visit_site_button, new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final Uri uri = Uri.parse(getString(R.string.website_url));
                        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                })
                .create();
    }
}
