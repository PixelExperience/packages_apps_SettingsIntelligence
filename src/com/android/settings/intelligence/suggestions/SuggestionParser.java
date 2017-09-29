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

import static com.android.settings.intelligence.suggestions.model.SuggestionCategoryRegistry
        .CATEGORIES;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.service.settings.suggestions.Suggestion;
import android.support.annotation.VisibleForTesting;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.android.settings.intelligence.suggestions.model.CandidateSuggestion;
import com.android.settings.intelligence.suggestions.model.SuggestionCategory;
import com.android.settings.intelligence.suggestions.model.SuggestionListBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main parser to get suggestions from all providers.
 * <p/>
 * Copied from framework/packages/SettingsLib/src/.../SuggestionParser
 */
class SuggestionParser {

    private static final String TAG = "SuggestionParser";
    private static final String SETUP_TIME = "_setup_time";

    private final Context mContext;
    private final PackageManager mPackageManager;
    private final SharedPreferences mSharedPrefs;
    private final Map<String, Suggestion> mAddCache;


    public SuggestionParser(Context context) {
        mContext = context.getApplicationContext();
        mPackageManager = context.getPackageManager();
        mAddCache = new ArrayMap<>();
        mSharedPrefs = SuggestionService.getSharedPrefs(mContext);
    }

    public List<Suggestion> getSuggestions() {
        final SuggestionListBuilder suggestionBuilder = new SuggestionListBuilder();

        for (SuggestionCategory category : CATEGORIES) {
            if (category.isExclusive() && !isExclusiveCategoryExpired(category)) {
                // If suggestions from an exclusive category are present, parsing is stopped
                // and only suggestions from that category are displayed. Note that subsequent
                // exclusive categories are also ignored.

                // Read suggestion and force ignoreSuggestionDismissRule to be false so the rule
                // defined from each suggestion itself is used.
                final List<Suggestion> exclusiveSuggestions =
                        readSuggestions(category, false /* ignoreDismissRule */);
                if (!exclusiveSuggestions.isEmpty()) {
                    suggestionBuilder.addSuggestions(category, exclusiveSuggestions);
                    return suggestionBuilder.build();
                }
            } else {
                // Either the category is not exclusive, or the exclusiveness expired so we should
                // treat it as a normal category.
                final List<Suggestion> suggestions =
                        readSuggestions(category, true /* ignoreDismissRule */);
                suggestionBuilder.addSuggestions(category, suggestions);
            }
        }
        return suggestionBuilder.build();
    }

    @VisibleForTesting
    List<Suggestion> readSuggestions(SuggestionCategory category, boolean ignoreDismissRule) {
        final List<Suggestion> suggestions = new ArrayList<>();
        final Intent probe = new Intent(Intent.ACTION_MAIN);
        probe.addCategory(category.getCategory());
        List<ResolveInfo> results = mPackageManager
                .queryIntentActivities(probe, PackageManager.GET_META_DATA);
        for (ResolveInfo resolved : results) {
            final CandidateSuggestion candidate = new CandidateSuggestion(mContext, resolved,
                    ignoreDismissRule);
            if (!candidate.isEligible()) {
                continue;
            }

            final String id = candidate.getId();
            Suggestion suggestion = mAddCache.get(id);
            if (suggestion == null) {
                suggestion = candidate.toSuggestion();
                mAddCache.put(id, suggestion);
            }
            if (!suggestions.contains(suggestion)) {
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }

    /**
     * Whether or not the category's exclusiveness has expired.
     */
    private boolean isExclusiveCategoryExpired(SuggestionCategory category) {
        final String keySetupTime = category.getCategory() + SETUP_TIME;
        final long currentTime = System.currentTimeMillis();
        if (!mSharedPrefs.contains(keySetupTime)) {
            mSharedPrefs.edit()
                    .putLong(keySetupTime, currentTime)
                    .commit();
        }
        if (category.getExclusiveExpireDaysInMillis() < 0) {
            // negative means never expires
            return false;
        }
        final long setupTime = mSharedPrefs.getLong(keySetupTime, 0);
        final long elapsedTime = currentTime - setupTime;
        Log.d(TAG, "Day " + elapsedTime / DateUtils.DAY_IN_MILLIS + " for "
                + category.getCategory());
        return elapsedTime > category.getExclusiveExpireDaysInMillis();
    }

//
//    /**
//     * Gets text associated with the input key from the content provider.
//     * @param context context
//     * @param uriString URI for the content provider
//     * @param providerMap Maps URI authorities to providers
//     * @param key Key mapping to the text in bundle returned by the content provider
//     * @return Text associated with the key, if returned by the content provider
//     */
//    public static String getTextFromUri(Context context, String uriString,
//            Map<String, IContentProvider> providerMap, String key) {
//        Bundle bundle = getBundleFromUri(context, uriString, providerMap);
//        return (bundle != null) ? bundle.getString(key) : null;
//    }
//
//    private static Bundle getBundleFromUri(Context context, String uriString,
//            Map<String, IContentProvider> providerMap) {
//        if (TextUtils.isEmpty(uriString)) {
//            return null;
//        }
//        Uri uri = Uri.parse(uriString);
//        String method = getMethodFromUri(uri);
//        if (TextUtils.isEmpty(method)) {
//            return null;
//        }
//        IContentProvider provider = getProviderFromUri(context, uri, providerMap);
//        if (provider == null) {
//            return null;
//        }
//        try {
//            return provider.call(context.getPackageName(), method, uriString, null);
//        } catch (RemoteException e) {
//            return null;
//        }
//    }
//
//    private static IContentProvider getProviderFromUri(Context context, Uri uri,
//            Map<String, IContentProvider> providerMap) {
//        if (uri == null) {
//            return null;
//        }
//        String authority = uri.getAuthority();
//        if (TextUtils.isEmpty(authority)) {
//            return null;
//        }
//        if (!providerMap.containsKey(authority)) {
//            providerMap.put(authority, context.getContentResolver().acquireUnstableProvider(uri));
//        }
//        return providerMap.get(authority);
//    }
//
//    /** Returns the first path segment of the uri if it exists as the method, otherwise null. */
//    static String getMethodFromUri(Uri uri) {
//        if (uri == null) {
//            return null;
//        }
//        List<String> pathSegments = uri.getPathSegments();
//        if ((pathSegments == null) || pathSegments.isEmpty()) {
//            return null;
//        }
//        return pathSegments.get(0);
//    }
}
