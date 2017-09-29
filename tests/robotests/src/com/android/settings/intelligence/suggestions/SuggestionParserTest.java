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

import static com.android.settings.intelligence.suggestions.model.CandidateSuggestionTest.newInfo;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.service.settings.suggestions.Suggestion;

import com.android.settings.intelligence.TestConfig;
import com.android.settings.intelligence.suggestions.model.SuggestionCategoryRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPackageManager;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class SuggestionParserTest {

    private Context mContext;
    private ShadowPackageManager mPackageManager;
    private SuggestionParser mSuggestionParser;
    private ResolveInfo mInfo1;
    private ResolveInfo mInfo1Dupe;
    private ResolveInfo mInfo2;
    private ResolveInfo mInfo3;
    private ResolveInfo mInfo4;

    private Intent exclusiveIntent1;
    private Intent exclusiveIntent2;
    private Intent regularIntent;

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application;
        mPackageManager = Shadows.shadowOf(mContext.getPackageManager());
        mSuggestionParser = new SuggestionParser(mContext);

        mInfo1 = newInfo(mContext, "Class1", true /* systemApp */,
                null /* summaryUri */, "title4", 0 /* titleResId */);
        mInfo1Dupe = newInfo(mContext, "Class1", true /* systemApp */,
                null /* summaryUri */, "title4", 0 /* titleResId */);
        mInfo2 = newInfo(mContext, "Class2", true /* systemApp */,
                null /* summaryUri */, "title4", 0 /* titleResId */);
        mInfo3 = newInfo(mContext, "Class3", true /* systemApp */,
                null /* summaryUri */, "title4", 0 /* titleResId */);
        mInfo4 = newInfo(mContext, "Class4", true /* systemApp */,
                null /* summaryUri */, "title4", 0 /* titleResId */);
        mInfo4.activityInfo.applicationInfo.packageName = "ineligible";

        exclusiveIntent1 = new Intent(Intent.ACTION_MAIN).addCategory(
                SuggestionCategoryRegistry.CATEGORIES.get(0).getCategory());
        exclusiveIntent2 = new Intent(Intent.ACTION_MAIN).addCategory(
                SuggestionCategoryRegistry.CATEGORIES.get(1).getCategory());
        regularIntent = new Intent(Intent.ACTION_MAIN).addCategory(
                SuggestionCategoryRegistry.CATEGORIES.get(2).getCategory());
    }

    @Test
    public void testGetSuggestions_exclusive() {
        mPackageManager.addResolveInfoForIntent(exclusiveIntent1, mInfo1);
        mPackageManager.addResolveInfoForIntent(exclusiveIntent1, mInfo1Dupe);
        mPackageManager.addResolveInfoForIntent(exclusiveIntent2, mInfo2);
        mPackageManager.addResolveInfoForIntent(regularIntent, mInfo3);
        final List<Suggestion> suggestions = mSuggestionParser.getSuggestions();

        // info1
        assertThat(suggestions).hasSize(1);
    }

    @Test
    public void testGetSuggestion_onlyRegularCategoryAndNoDupe() {
        mPackageManager.addResolveInfoForIntent(regularIntent, mInfo1);
        mPackageManager.addResolveInfoForIntent(regularIntent, mInfo1Dupe);
        mPackageManager.addResolveInfoForIntent(regularIntent, mInfo2);
        mPackageManager.addResolveInfoForIntent(regularIntent, mInfo3);
        mPackageManager.addResolveInfoForIntent(regularIntent, mInfo4);

        final List<Suggestion> suggestions = mSuggestionParser.getSuggestions();

        // info1, info2, info3 (info4 is skip because its package name is ineligible)
        assertThat(suggestions).hasSize(3);
    }
}
