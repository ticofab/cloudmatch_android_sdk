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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Helper to split a generic string into chunks.
 */
public class StringHelper {
    public static ArrayList<String> splitEqually(final String str, final int chunkSize) {
        // Give the list the right capacity to start with.
        final ArrayList<String> ret = new ArrayList<String>((str.length() + chunkSize - 1) / chunkSize);

        for (int start = 0; start < str.length(); start += chunkSize) {
            ret.add(str.substring(start, Math.min(str.length(), start + chunkSize)));
        }
        return ret;
    }

    public static String getApiKey(Context context) throws PackageManager.NameNotFoundException {
        return getValue(context, "cloudmatch_api_key");
    }

    public static String getAppId(Context context) throws PackageManager.NameNotFoundException {
        return getValue(context, "cloudmatch_app_id");
    }

    private static String getValue(Context context, String key) throws PackageManager.NameNotFoundException {
        String value = null;
        ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        if (ai != null) {
            Bundle bundle = ai.metaData;
            if (bundle != null) {
                value = bundle.getString(key);
            }
        }
        if (value == null) {
            String msg = "It seems like you forgot api key & app id in your Manifest!";
            throw new PackageManager.NameNotFoundException(msg);
        }
        return value;
    }
}
