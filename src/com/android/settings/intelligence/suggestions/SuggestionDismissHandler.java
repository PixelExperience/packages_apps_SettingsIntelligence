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

package com.android.settings.intelligence.suggestions;

import android.content.Context;

public class SuggestionDismissHandler {

    private static final String IS_DISMISSED = "_is_dismissed";

    private static SuggestionDismissHandler sDismissHandler;

    private SuggestionDismissHandler() {}

    public static SuggestionDismissHandler getInstance() {
        if (sDismissHandler == null) {
            sDismissHandler = new SuggestionDismissHandler();
        }
        return sDismissHandler;
    }

    public void markSuggestionDismissed(Context context, String id) {
        SuggestionService.getSharedPrefs(context)
                .edit()
                .putBoolean(getDismissKey(id), true)
                .apply();
    }

    public void markSuggestionNotDismissed(Context context, String id) {
        SuggestionService.getSharedPrefs(context)
                .edit()
                .putBoolean(getDismissKey(id), false)
                .apply();
    }

    public boolean isSuggestionDismissed(Context context, String id) {
        return SuggestionService.getSharedPrefs(context)
                .getBoolean(getDismissKey(id), false);
    }

    private static String getDismissKey(String id) {
        return id + IS_DISMISSED;
    }
}
