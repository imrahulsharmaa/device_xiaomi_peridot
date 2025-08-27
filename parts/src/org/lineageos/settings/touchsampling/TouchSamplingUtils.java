/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.touchsampling;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.touchsampling.TouchSamplingSettingsFragment;
import org.lineageos.settings.utils.FileUtils;

import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public final class TouchSamplingUtils {
    private static final String TAG = "TouchSamplingUtils";
    private static final String HTSR_GOODIX_FILE = "/sys/devices/platform/goodix_ts.0/switch_report_rate";
    private static final String HTSR_FOCALTECH_FILE = "/sys/bus/spi/drivers/focaltech_ts/spi1.0/switch_report_rate";
    public static final String SCONFIG_FILE = "/sys/class/thermal/thermal_message/sconfig";
    
    private static String sHtsrFile = null;

    /**
     * Gets the appropriate HTSR file path, checking availability and caching the result.
     * First checks for goodix_ts.0, then falls back to focaltech_ts if not available.
     */
    public static String getHtsrFile() {
        if (sHtsrFile != null) {
            return sHtsrFile;
        }
        
        // First try goodix_ts.0
        if (new File(HTSR_GOODIX_FILE).exists()) {
            sHtsrFile = HTSR_GOODIX_FILE;
            Log.d(TAG, "Using goodix HTSR file: " + sHtsrFile);
        } else if (new File(HTSR_FOCALTECH_FILE).exists()) {
            sHtsrFile = HTSR_FOCALTECH_FILE;
            Log.d(TAG, "Using focaltech HTSR file: " + sHtsrFile);
        } else {
            // Fallback to goodix as default
            sHtsrFile = HTSR_GOODIX_FILE;
            Log.w(TAG, "Neither HTSR file exists, using goodix as default: " + sHtsrFile);
        }
        
        return sHtsrFile;
    }

    public static void restoreSamplingValue(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                TouchSamplingSettingsFragment.SHAREDHTSR, Context.MODE_PRIVATE);
        int htsrState = sharedPref.getInt(TouchSamplingSettingsFragment.SHAREDHTSR, 0);
        FileUtils.writeLine(getHtsrFile(), Integer.toString(htsrState));
    }

    /**
     * Returns the package name of the current foreground app.
     */
    private static String getForegroundApp(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && !tasks.isEmpty() && tasks.get(0).topActivity != null) {
                return tasks.get(0).topActivity.getPackageName();
            }
        }
        return null;
    }
}
