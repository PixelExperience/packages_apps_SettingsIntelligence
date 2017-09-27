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

import static com.google.common.truth.Truth.assertThat;

import android.service.settings.suggestions.Suggestion;

import com.android.settings.intelligence.SettingsIntelligenceRobolectricTestRunner;
import com.android.settings.intelligence.TestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;

@RunWith(SettingsIntelligenceRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class SuggestionServiceTest {

    private SuggestionService mService;
    private ServiceController<SuggestionService> mServiceController;

    @Before
    public void setUp() {
        mServiceController = Robolectric.buildService(SuggestionService.class);
        mService = mServiceController.create().get();
    }

    @Test
    public void getSuggestion_shouldReturnNonNull() {
        assertThat(mService.onGetSuggestions()).isNotNull();
    }

    @Test
    public void dismissSuggestion_shouldDismiss() {
        final String id = "id1";
        final Suggestion suggestion = new Suggestion.Builder(id).build();

        // Not dismissed
        assertThat(SuggestionDismissHandler.getInstance().isSuggestionDismissed(mService, id))
                .isFalse();

        // Dismiss
        mService.onSuggestionDismissed(suggestion);

        // Dismissed
        assertThat(SuggestionDismissHandler.getInstance().isSuggestionDismissed(mService, id))
                .isTrue();
    }
}
