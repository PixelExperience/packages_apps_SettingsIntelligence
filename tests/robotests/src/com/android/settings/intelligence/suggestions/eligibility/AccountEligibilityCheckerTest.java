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

import static com.android.settings.intelligence.suggestions.eligibility.AccountEligibilityChecker
        .META_DATA_REQUIRE_ACCOUNT;
import static com.google.common.truth.Truth.assertThat;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
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
import org.robolectric.shadows.ShadowAccountManager;

@RunWith(SettingsIntelligenceRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class AccountEligibilityCheckerTest {

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

    @After
    public void tearDown() {
        final ShadowAccountManager shadowAccountManager = Shadows.shadowOf(
                AccountManager.get(mContext));
        shadowAccountManager.removeAllAccounts();
    }

    @Test
    public void isEligible_noAccountRequirement_shouldReturnTrue() {
        assertThat(AccountEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isTrue();
    }

    @Test
    public void isEligible_failRequirement_shouldReturnFalse() {
        // Require android.com account but AccountManager doesn't have it.
        mInfo.activityInfo.metaData.putString(META_DATA_REQUIRE_ACCOUNT, "android.com");

        assertThat(AccountEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isFalse();
    }

    @Test
    public void isEligible_passRequirement_shouldReturnTrue() {
        mInfo.activityInfo.metaData.putString(META_DATA_REQUIRE_ACCOUNT, "android.com");
        final Account account = new Account("TEST", "android.com");

        final ShadowAccountManager shadowAccountManager = Shadows.shadowOf(
                AccountManager.get(mContext));
        shadowAccountManager.addAccount(account);

        assertThat(AccountEligibilityChecker.isEligible(mContext, ID, mInfo))
                .isTrue();
    }
}
