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

package com.android.settings.intelligence.suggestions.ranking;

import static com.google.common.truth.Truth.assertThat;

import android.service.settings.suggestions.Suggestion;

import com.android.settings.intelligence.SettingsIntelligenceRobolectricTestRunner;
import com.android.settings.intelligence.TestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(SettingsIntelligenceRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class SuggestionFeaturizerTest {

    private EventStore mEventStore;
    private SuggestionFeaturizer mSuggestionFeaturizer;

    @Before
    public void setUp() {
        mEventStore = new EventStore(RuntimeEnvironment.application);
        mSuggestionFeaturizer = new SuggestionFeaturizer(mEventStore);
    }

    @Test
    public void testFeaturize_singlePackage() {
        mEventStore.writeEvent("pkg", EventStore.EVENT_DISMISSED);
        mEventStore.writeEvent("pkg", EventStore.EVENT_SHOWN);
        mEventStore.writeEvent("pkg", EventStore.EVENT_SHOWN);
        final Map<String, Double> features = mSuggestionFeaturizer
                .featurize(Arrays.asList(new Suggestion.Builder("pkg").build()))
                .get("pkg");
        assertThat(features.get(SuggestionFeaturizer.FEATURE_IS_SHOWN)).isEqualTo(1.0);
        assertThat(features.get(SuggestionFeaturizer.FEATURE_IS_DISMISSED)).isEqualTo(1.0);
        assertThat(features.get(SuggestionFeaturizer.FEATURE_IS_CLICKED)).isEqualTo(0.0);

        assertThat(features.get(SuggestionFeaturizer.FEATURE_TIME_FROM_LAST_SHOWN)).isLessThan
                (1.0);
        assertThat(features.get(SuggestionFeaturizer.FEATURE_TIME_FROM_LAST_DISMISSED))
                .isLessThan(1.0);
        assertThat(features.get(SuggestionFeaturizer.FEATURE_TIME_FROM_LAST_CLICKED))
                .isEqualTo(1.0);
        assertThat(features.get(SuggestionFeaturizer.FEATURE_SHOWN_COUNT))
                .isEqualTo(2.0 / SuggestionFeaturizer.COUNT_NORMALIZATION_FACTOR);
        assertThat(features.get(SuggestionFeaturizer.FEATURE_DISMISSED_COUNT))
                .isEqualTo(1.0 / SuggestionFeaturizer.COUNT_NORMALIZATION_FACTOR);
        assertThat(features.get(SuggestionFeaturizer.FEATURE_CLICKED_COUNT)).isEqualTo(0.0);
    }

    @Test
    public void testFeaturize_multiplePackages() {
        mEventStore.writeEvent("pkg1", EventStore.EVENT_DISMISSED);
        mEventStore.writeEvent("pkg2", EventStore.EVENT_SHOWN);
        mEventStore.writeEvent("pkg1", EventStore.EVENT_SHOWN);
        final List<Suggestion> suggestions = new ArrayList<Suggestion>() {
            {
                add(new Suggestion.Builder("pkg1").build());
                add(new Suggestion.Builder("pkg2").build());
            }
        };
        final Map<String, Map<String, Double>> features = mSuggestionFeaturizer
                .featurize(suggestions);
        final Map<String, Double> features1 = features.get("pkg1");
        final Map<String, Double> features2 = features.get("pkg2");

        assertThat(features1.get(SuggestionFeaturizer.FEATURE_IS_SHOWN)).isEqualTo(1.0);
        assertThat(features1.get(SuggestionFeaturizer.FEATURE_IS_DISMISSED)).isEqualTo(1.0);
        assertThat(features1.get(SuggestionFeaturizer.FEATURE_IS_CLICKED)).isEqualTo(0.0);
        assertThat(features1.get(SuggestionFeaturizer.FEATURE_TIME_FROM_LAST_SHOWN))
                .isLessThan(1.0);
        assertThat(features1.get(SuggestionFeaturizer.FEATURE_TIME_FROM_LAST_DISMISSED))
                .isLessThan(1.0);
        assertThat(features1.get(SuggestionFeaturizer.FEATURE_TIME_FROM_LAST_CLICKED))
                .isEqualTo(1.0);
        assertThat(features1.get(SuggestionFeaturizer.FEATURE_SHOWN_COUNT))
                .isEqualTo(1.0 / SuggestionFeaturizer.COUNT_NORMALIZATION_FACTOR);
        assertThat(features1.get(SuggestionFeaturizer.FEATURE_DISMISSED_COUNT))
                .isEqualTo(1.0 / SuggestionFeaturizer.COUNT_NORMALIZATION_FACTOR);
        assertThat(features1.get(SuggestionFeaturizer.FEATURE_CLICKED_COUNT)).isEqualTo(0.0);

        assertThat(features2.get(SuggestionFeaturizer.FEATURE_IS_SHOWN)).isEqualTo(1.0);
        assertThat(features2.get(SuggestionFeaturizer.FEATURE_IS_DISMISSED)).isEqualTo(0.0);
        assertThat(features2.get(SuggestionFeaturizer.FEATURE_IS_CLICKED)).isEqualTo(0.0);
        assertThat(features2.get(SuggestionFeaturizer.FEATURE_TIME_FROM_LAST_SHOWN))
                .isLessThan(1.0);
        assertThat(features2.get(SuggestionFeaturizer.FEATURE_TIME_FROM_LAST_DISMISSED))
                .isEqualTo(1.0);
        assertThat(features2.get(SuggestionFeaturizer.FEATURE_TIME_FROM_LAST_CLICKED))
                .isEqualTo(1.0);
        assertThat(features2.get(SuggestionFeaturizer.FEATURE_SHOWN_COUNT))
                .isEqualTo(1.0 / SuggestionFeaturizer.COUNT_NORMALIZATION_FACTOR);
        assertThat(features2.get(SuggestionFeaturizer.FEATURE_DISMISSED_COUNT)).isEqualTo(0.0);
        assertThat(features2.get(SuggestionFeaturizer.FEATURE_CLICKED_COUNT)).isEqualTo(0.0);
    }
}
