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


import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import com.android.settings.intelligence.TestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class ProviderEligibilityCheckerTest {

    private static final String ID = "test";
    private Context mContext;
    private ResolveInfo mInfo;

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application;
        mInfo = new ResolveInfo();
        mInfo.activityInfo = new ActivityInfo();
        mInfo.activityInfo.metaData = new Bundle();
        mInfo.activityInfo.applicationInfo = new ApplicationInfo();
        mInfo.activityInfo.applicationInfo.packageName =
                RuntimeEnvironment.application.getPackageName();
    }

    @Test
    public void isEligible_systemFlagSet_shouldReturnTrue() {
        mInfo.activityInfo.applicationInfo.flags |= ApplicationInfo.FLAG_SYSTEM;

        assertThat(ProviderEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isTrue();
    }

    @Test
    public void isEligible_systemFlagNotSet_shouldReturnFalse() {
        mInfo.activityInfo.applicationInfo.flags &= ~ApplicationInfo.FLAG_SYSTEM;

        assertThat(ProviderEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isFalse();
    }
}
