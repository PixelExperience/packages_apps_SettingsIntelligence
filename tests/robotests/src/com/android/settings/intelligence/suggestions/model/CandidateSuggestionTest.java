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

package com.android.settings.intelligence.suggestions.model;

import static com.android.settings.intelligence.suggestions.model.CandidateSuggestion
        .META_DATA_PREFERENCE_ICON;
import static com.android.settings.intelligence.suggestions.model.CandidateSuggestion
        .META_DATA_PREFERENCE_SUMMARY;
import static com.android.settings.intelligence.suggestions.model.CandidateSuggestion
        .META_DATA_PREFERENCE_SUMMARY_URI;
import static com.android.settings.intelligence.suggestions.model.CandidateSuggestion
        .META_DATA_PREFERENCE_TITLE;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.service.settings.suggestions.Suggestion;

import com.android.settings.intelligence.TestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class CandidateSuggestionTest {

    private static final String PACKAGE_NAME = "pkg";
    private static final String CLASS_NAME = "class";

    private Context mContext;
    private ResolveInfo mInfo;

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application;
        mInfo = new ResolveInfo();
        mInfo.activityInfo = new ActivityInfo();
        mInfo.activityInfo.metaData = new Bundle();
        mInfo.activityInfo.packageName = PACKAGE_NAME;
        mInfo.activityInfo.name = CLASS_NAME;

        mInfo.activityInfo.applicationInfo = new ApplicationInfo();
        mInfo.activityInfo.applicationInfo.packageName =
                RuntimeEnvironment.application.getPackageName();
    }

    @Test
    public void getId_shouldUseComponentName() {
        final CandidateSuggestion candidate =
                new CandidateSuggestion(mContext, mInfo, true /* ignoreAppearRule */);

        assertThat(candidate.getId())
                .contains(PACKAGE_NAME + "/" + CLASS_NAME);
    }

    @Test
    public void parseMetadata_eligibleSuggestion() {
        final ResolveInfo info = newInfo(mContext, "class", true /* systemApp */,
                null /*summaryUri */, "title", 0 /* titleResId */);
        Suggestion suggestion = new CandidateSuggestion(
                mContext, info, false /* ignoreAppearRule*/)
                .toSuggestion();
        assertThat(suggestion.getId()).isEqualTo(mContext.getPackageName() + "/class");
        assertThat(suggestion.getTitle()).isEqualTo("title");
        assertThat(suggestion.getSummary()).isEqualTo("static-summary");
    }

    @Test
    public void parseMetadata_ineligibleSuggestion() {
        final ResolveInfo info = newInfo(mContext, "class", false /* systemApp */,
                null /*summaryUri */, "title", 0 /* titleResId */);
        final CandidateSuggestion candidate = new CandidateSuggestion(
                mContext, info, false /* ignoreAppearRule*/);

        assertThat(candidate.isEligible()).isFalse();
        assertThat(candidate.toSuggestion()).isNull();
    }

    public static ResolveInfo newInfo(Context context, String className, boolean systemApp,
            String summaryUri, String title, int titleResId) {
        final ResolveInfo info = new ResolveInfo();
        info.activityInfo = new ActivityInfo();
        info.activityInfo.applicationInfo = new ApplicationInfo();
        if (systemApp) {
            info.activityInfo.applicationInfo.flags |= ApplicationInfo.FLAG_SYSTEM;
        }
        info.activityInfo.packageName = context.getPackageName();
        info.activityInfo.applicationInfo.packageName = info.activityInfo.packageName;
        info.activityInfo.name = className;
        info.activityInfo.metaData = new Bundle();
        info.activityInfo.metaData.putInt(META_DATA_PREFERENCE_ICON, 314159);
        info.activityInfo.metaData.putString(META_DATA_PREFERENCE_SUMMARY, "static-summary");
        if (summaryUri != null) {
            info.activityInfo.metaData.putString(META_DATA_PREFERENCE_SUMMARY_URI, summaryUri);
        }
        if (titleResId != 0) {
            info.activityInfo.metaData.putInt(META_DATA_PREFERENCE_TITLE, titleResId);
        } else if (title != null) {
            info.activityInfo.metaData.putString(META_DATA_PREFERENCE_TITLE, title);
        }
        return info;
    }
}
