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


import static com.android.settings.intelligence.suggestions.eligibility.FeatureEligibilityChecker
        .META_DATA_REQUIRE_FEATURE;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import com.android.settings.intelligence.TestConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPackageManager;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class FeatureEligibilityCheckerTest {

    private static final String ID = "test";
    private Context mContext;
    private ResolveInfo mInfo;
    private ShadowPackageManager mPackageManager;

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application;
        mInfo = new ResolveInfo();
        mInfo.activityInfo = new ActivityInfo();
        mInfo.activityInfo.metaData = new Bundle();
        mInfo.activityInfo.applicationInfo = new ApplicationInfo();
        mInfo.activityInfo.applicationInfo.packageName =
                RuntimeEnvironment.application.getPackageName();
        mPackageManager = Shadows.shadowOf(mContext.getPackageManager());
    }

    @After
    public void tearDown() {
        mPackageManager.clearSystemAvailableFeatures();
    }

    @Test
    public void isEligible_noRequirement_shouldReturnTrue() {
        assertThat(FeatureEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isTrue();
    }

    @Test
    public void isEligible_failRequirement_shouldReturnFalse() {
        mInfo.activityInfo.metaData.putString(META_DATA_REQUIRE_FEATURE, "test_feature");

        assertThat(FeatureEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isFalse();
    }

    @Test
    public void isEligible_passRequirement_shouldReturnTrue() {
        final FeatureInfo featureInfo = new FeatureInfo();
        featureInfo.name = "fingerprint";
        mInfo.activityInfo.metaData.putString(META_DATA_REQUIRE_FEATURE, featureInfo.name);
        mPackageManager.addSystemAvailableFeature(featureInfo);

        assertThat(FeatureEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isFalse();
    }
}