/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.intelligence.suggestions.eligibility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.support.annotation.VisibleForTesting;
import android.text.format.DateUtils;
import android.util.Log;

import com.android.settings.intelligence.suggestions.SuggestionDismissHandler;
import com.android.settings.intelligence.suggestions.SuggestionService;

public class DismissedChecker {

    /**
     * Allows suggestions to appear after a certain number of days, and to re-appear if dismissed.
     * For instance:
     * 0,10
     * Will appear immediately, the 10 is ignored.
     *
     * 10
     * Will appear after 10 days
     */
    @VisibleForTesting
    static final String META_DATA_DISMISS_CONTROL = "com.android.settings.dismiss";

    // Shared prefs keys for storing dismissed state.
    // Index into current dismissed state.
    private static final String SETUP_TIME = "_setup_time";
    // Default dismiss rule for suggestions.

    private static final int DEFAULT_FIRST_APPEAR_DAY = 0;

    private static final String TAG = "DismissedChecker";

    public static boolean isEligible(Context context, String id, ResolveInfo info,
            boolean ignoreAppearRule) {
        final SharedPreferences prefs = SuggestionService.getSharedPrefs(context);
        final SuggestionDismissHandler dismissHandler = SuggestionDismissHandler.getInstance();
        final String keySetupTime = id + SETUP_TIME;
        if (!prefs.contains(keySetupTime)) {
            prefs.edit()
                    .putLong(keySetupTime, System.currentTimeMillis())
                    .apply();
        }

        // Check if it's already manually dismissed
        final boolean isDismissed = dismissHandler.isSuggestionDismissed(context, id);
        if (isDismissed) {
            return false;
        }

        // Parse when suggestion should first appear. return true to artificially hide suggestion
        // before then.
        int firstAppearDay = ignoreAppearRule
                ? DEFAULT_FIRST_APPEAR_DAY
                : parseAppearDay(info);
        long firstAppearDayInMs = getEndTime(prefs.getLong(keySetupTime, 0), firstAppearDay);
        if (System.currentTimeMillis() >= firstAppearDayInMs) {
            // Dismiss timeout has passed, undismiss it.
            dismissHandler.markSuggestionNotDismissed(context, id);
            return true;
        }
        return false;
    }

    /**
     * Parse the first int from a string formatted as "0,1,2..."
     * The value means suggestion should first appear on Day X.
     */
    private static int parseAppearDay(ResolveInfo info) {
        if (!info.activityInfo.metaData.containsKey(META_DATA_DISMISS_CONTROL)) {
            return 0;
        }

        final Object firstAppearRule = info.activityInfo.metaData
                .get(META_DATA_DISMISS_CONTROL);
        if (firstAppearRule instanceof Integer) {
            return (int) firstAppearRule;
        } else {
            try {
                final String[] days = ((String) firstAppearRule).split(",");
                return Integer.parseInt(days[0]);
            } catch (Exception e) {
                Log.w(TAG, "Failed to parse appear/dismiss rule, fall back to 0");
                return 0;
            }
        }
    }

    private static long getEndTime(long startTime, int daysDelay) {
        long days = daysDelay * DateUtils.DAY_IN_MILLIS;
        return startTime + days;
    }
}
