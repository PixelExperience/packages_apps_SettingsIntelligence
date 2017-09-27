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

import static com.android.settings.intelligence.suggestions.eligibility
        .ConnectivityEligibilityChecker.META_DATA_IS_CONNECTION_REQUIRED;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.android.settings.intelligence.SettingsIntelligenceRobolectricTestRunner;
import com.android.settings.intelligence.TestConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowConnectivityManager;

@RunWith(SettingsIntelligenceRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class ConnectivityEligibilityCheckerTest {

    private static final String ID = "test";
    private Context mContext;
    private ResolveInfo mInfo;
    private ShadowConnectivityManager mConnectivityManager;

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application;
        mInfo = new ResolveInfo();
        mInfo.activityInfo = new ActivityInfo();
        mInfo.activityInfo.metaData = new Bundle();
        mInfo.activityInfo.applicationInfo = new ApplicationInfo();
        mInfo.activityInfo.applicationInfo.packageName =
                RuntimeEnvironment.application.getPackageName();

        mConnectivityManager = Shadows.shadowOf(
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    @After
    public void tearDown() {
        mConnectivityManager.setActiveNetworkInfo(null);
    }

    @Test
    public void isEligible_noRequirement_shouldReturnTrue() {
        assertThat(ConnectivityEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isTrue();

        mInfo.activityInfo.metaData.putBoolean(META_DATA_IS_CONNECTION_REQUIRED, false);
        assertThat(ConnectivityEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isTrue();
    }

    @Test
    public void isEligible_hasConnection_shouldReturnTrue() {
        mInfo.activityInfo.metaData.putBoolean(META_DATA_IS_CONNECTION_REQUIRED, true);

        final NetworkInfo networkInfo = mock(NetworkInfo.class);
        when(networkInfo.isConnectedOrConnecting())
                .thenReturn(true);

        mConnectivityManager.setActiveNetworkInfo(networkInfo);

        assertThat(ConnectivityEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isTrue();
    }

    @Test
    public void isEligible_noConnection_shouldReturnFalse() {
        mInfo.activityInfo.metaData.putBoolean(META_DATA_IS_CONNECTION_REQUIRED, true);

        mConnectivityManager.setActiveNetworkInfo(null);

        assertThat(ConnectivityEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isFalse();
    }
}
