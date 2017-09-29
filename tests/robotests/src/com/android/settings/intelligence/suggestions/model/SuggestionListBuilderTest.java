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

import static com.google.common.truth.Truth.assertThat;

import android.service.settings.suggestions.Suggestion;

import com.android.settings.intelligence.SettingsIntelligenceRobolectricTestRunner;
import com.android.settings.intelligence.TestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Arrays;

@RunWith(SettingsIntelligenceRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class SuggestionListBuilderTest {

    private SuggestionListBuilder mBuilder;
    private SuggestionCategory mCategory1;
    private SuggestionCategory mCategory2;
    private Suggestion mSuggestion1;
    private Suggestion mSuggestion2;

    @Before
    public void setUp() {
        mSuggestion1 = new Suggestion.Builder("id1")
                .setTitle("title1")
                .setSummary("summary1")
                .build();
        mCategory1 = SuggestionCategoryRegistry.CATEGORIES.get(3);
        mCategory2 = SuggestionCategoryRegistry.CATEGORIES.get(4);
        mSuggestion2 = new Suggestion.Builder("id2")
                .setTitle("title2")
                .setSummary("summary2")
                .build();
        mBuilder = new SuggestionListBuilder();
    }

    @Test
    public void dedupe_shouldSkipSameSuggestion() {
        mBuilder.addSuggestions(mCategory1, Arrays.asList(mSuggestion1));
        mBuilder.addSuggestions(mCategory2, Arrays.asList(mSuggestion1));

        assertThat(mBuilder.build()).hasSize(1);
    }

    @Test
    public void dedupe_shouldContainDifferentSuggestion() {
        mBuilder.addSuggestions(mCategory1, Arrays.asList(mSuggestion1, mSuggestion2));

        assertThat(mBuilder.build()).hasSize(2);
    }
}
