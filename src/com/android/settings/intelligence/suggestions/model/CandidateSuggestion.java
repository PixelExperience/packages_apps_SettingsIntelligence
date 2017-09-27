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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.service.settings.suggestions.Suggestion;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.settings.intelligence.suggestions.eligibility.AccountEligibilityChecker;
import com.android.settings.intelligence.suggestions.eligibility.ConnectivityEligibilityChecker;
import com.android.settings.intelligence.suggestions.eligibility.DismissedChecker;
import com.android.settings.intelligence.suggestions.eligibility.FeatureEligibilityChecker;
import com.android.settings.intelligence.suggestions.eligibility.ProviderEligibilityChecker;

/**
 * A wrapper to {@link android.content.pm.ResolveInfo} that matches Suggestion signature.
 * <p/>
 * This class contains necessary metadata to eventually be
 * processed into a {@link android.service.settings.suggestions.Suggestion}.
 */
public class CandidateSuggestion {

    public static final String META_DATA_PREFERENCE_ICON_TINTABLE =
            "com.android.settings.icon_tintable";

    public static final String META_DATA_PREFERENCE_TITLE = "com.android.settings.title";

    /**
     * Name of the meta-data item that should be set in the AndroidManifest.xml
     * to specify the summary text that should be displayed for the preference.
     */
    public static final String META_DATA_PREFERENCE_SUMMARY = "com.android.settings.summary";

    /**
     * Name of the meta-data item that should be set in the AndroidManifest.xml
     * to specify the content provider providing the summary text that should be displayed for the
     * preference.
     *
     * Summary provided by the content provider overrides any static summary.
     */
    public static final String META_DATA_PREFERENCE_SUMMARY_URI =
            "com.android.settings.summary_uri";

    public static final String META_DATA_PREFERENCE_CUSTOM_VIEW =
            "com.android.settings.custom_view";

    /**
     * Name of the meta-data item that should be set in the AndroidManifest.xml
     * to specify the icon that should be displayed for the preference.
     */
    public static final String META_DATA_PREFERENCE_ICON = "com.android.settings.icon";

    private static final String TAG = "CandidateSuggestion";

    private final String mId;
    private final Context mContext;
    private final ResolveInfo mResolveInfo;
    private final Intent mIntent;
    private final boolean mIsEligible;
    private final boolean mIgnoreAppearRule;

    public CandidateSuggestion(Context context, ResolveInfo resolveInfo,
            boolean ignoreAppearRule) {
        mContext = context;
        mIgnoreAppearRule = ignoreAppearRule;
        mResolveInfo = resolveInfo;
        mIntent = new Intent()
                .setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
        mId = generateId();
        mIsEligible = initIsEligible();
    }

    public String getId() {
        return mId;
    }

    /**
     * Whether or not this candidate is eligible for display.
     * <p/>
     * Note: eligible doesn't mean it will be displayed.
     */
    public boolean isEligible() {
        return mIsEligible;
    }

    public Suggestion toSuggestion() {
        if (!mIsEligible) {
            return null;
        }
        final Suggestion.Builder builder = new Suggestion.Builder(mId);
        updateBuilder(builder);
        return builder.build();
    }

    /**
     * Checks device condition against suggestion requirement. Returns true if the suggestion is
     * eligible.
     * <p/>
     * Note: eligible doesn't mean it will be displayed.
     */
    private boolean initIsEligible() {
        if (!ProviderEligibilityChecker.isEligible(mContext, mId, mResolveInfo)) {
            return false;
        }
        if (!ConnectivityEligibilityChecker.isEligible(mContext, mId, mResolveInfo)) {
            return false;
        }
        if (!FeatureEligibilityChecker.isEligible(mContext, mId, mResolveInfo)) {
            return false;
        }
        if (!AccountEligibilityChecker.isEligible(mContext, mId, mResolveInfo)) {
            return false;
        }
        if (!DismissedChecker.isEligible(mContext, mId, mResolveInfo, mIgnoreAppearRule)) {
            return false;
        }
        return true;
    }

    private void updateBuilder(Suggestion.Builder builder) {
        final PackageManager pm = mContext.getPackageManager();
        final ApplicationInfo applicationInfo = mResolveInfo.activityInfo.applicationInfo;

        int icon = 0;
        boolean iconTintable = false;
        String title = null;
        String summary = null;
        RemoteViews remoteViews = null;

        // Get the activity's meta-data
        try {
            final Resources res = pm.getResourcesForApplication(applicationInfo.packageName);
            final Bundle metaData = mResolveInfo.activityInfo.metaData;

            if (res != null && metaData != null) {
                if (metaData.containsKey(META_DATA_PREFERENCE_ICON)) {
                    icon = metaData.getInt(META_DATA_PREFERENCE_ICON);
                }
                if (metaData.containsKey(META_DATA_PREFERENCE_ICON_TINTABLE)) {
                    iconTintable = metaData.getBoolean(META_DATA_PREFERENCE_ICON_TINTABLE);
                }
                if (metaData.containsKey(META_DATA_PREFERENCE_TITLE)) {
                    if (metaData.get(META_DATA_PREFERENCE_TITLE) instanceof Integer) {
                        title = res.getString(metaData.getInt(META_DATA_PREFERENCE_TITLE));
                    } else {
                        title = metaData.getString(META_DATA_PREFERENCE_TITLE);
                    }
                }
                if (metaData.containsKey(META_DATA_PREFERENCE_SUMMARY)) {
                    if (metaData.get(META_DATA_PREFERENCE_SUMMARY) instanceof Integer) {
                        summary = res.getString(metaData.getInt(META_DATA_PREFERENCE_SUMMARY));
                    } else {
                        summary = metaData.getString(META_DATA_PREFERENCE_SUMMARY);
                    }
                }
                if (metaData.containsKey(META_DATA_PREFERENCE_CUSTOM_VIEW)) {
                    int layoutId = metaData.getInt(META_DATA_PREFERENCE_CUSTOM_VIEW);
                    remoteViews = new RemoteViews(applicationInfo.packageName, layoutId);
                }
            }
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            Log.d(TAG, "Couldn't find info", e);
        }

        // Set the preference title to the activity's label if no
        // meta-data is found
        if (TextUtils.isEmpty(title)) {
            title = mResolveInfo.activityInfo.loadLabel(pm).toString();
        }

        if (icon == 0) {
            icon = mResolveInfo.activityInfo.icon;
        }
        // TODO: Need to use ContentProvider to read dynamic title/summary etc.
        final PendingIntent pendingIntent = PendingIntent
                .getActivity(mContext, 0 /* requestCode */, mIntent, 0 /* flags */);
        builder.setTitle(title)
                .setSummary(summary)
                .setPendingIntent(pendingIntent);
        // TODO: Need to extend Suggestion and set the following.
        // set icon
        // set icon tintable
        // set remote view
    }

    private String generateId() {
        return mIntent.getComponent().flattenToString();
    }
}
