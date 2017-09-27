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

import static com.android.settings.intelligence.suggestions.model.SuggestionCategoryRegistry
        .CATEGORIES;
import static com.google.common.truth.Truth.assertThat;

import com.android.settings.intelligence.SettingsIntelligenceRobolectricTestRunner;
import com.android.settings.intelligence.TestConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(SettingsIntelligenceRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class SuggestionCategoryRegistryTest {

    @Test
    public void getCategories_shouldHave10Categories() {
        assertThat(CATEGORIES)
                .hasSize(10);
    }

    @Test
    public void verifyExclusiveCategories() {
        final List<String> exclusiveCategories = new ArrayList<>();
        exclusiveCategories.add(SuggestionCategoryRegistry.CATEGORY_KEY_DEFERRED_SETUP);
        exclusiveCategories.add(SuggestionCategoryRegistry.CATEGORY_KEY_FIRST_IMPRESSION);

        int exclusiveCount = 0;
        for (SuggestionCategory category : CATEGORIES) {
            if (category.isExclusive()) {
                exclusiveCount++;
                assertThat(exclusiveCategories).contains(category.getCategory());
            }
        }
        assertThat(exclusiveCount).isEqualTo(exclusiveCategories.size());
    }

}
