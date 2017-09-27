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
import android.content.SharedPreferences;
import android.service.settings.suggestions.Suggestion;
import android.util.Log;

import com.android.settings.intelligence.suggestions.ranking.SuggestionRanker;

import java.util.List;

public class SuggestionService extends android.service.settings.suggestions.SuggestionService {

    private static final String TAG = "SuggestionService";

    private static final String SHARED_PREF_FILENAME = "suggestions";

    @Override
    public List<Suggestion> onGetSuggestions() {
        final SuggestionParser parser = new SuggestionParser(this);
        final List<Suggestion> list = parser.getSuggestions();
        SuggestionRanker.getInstance(this).rankSuggestions(list);
        return list;
    }

    @Override
    public void onSuggestionDismissed(Suggestion suggestion) {
        final String id = suggestion.getId();
        Log.d(TAG, "dismissing suggestion " + id);
        SuggestionDismissHandler.getInstance()
                .markSuggestionDismissed(this /* context */, id);
    }

    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF_FILENAME, Context.MODE_PRIVATE);
    }
}
