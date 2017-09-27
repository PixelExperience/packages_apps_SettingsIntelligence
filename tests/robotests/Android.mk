#############################################
# Turbo Robolectric test target. #
#############################################
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, runners/android_mk src)

# Include the testing libraries (JUnit4 + Robolectric libs).
LOCAL_STATIC_JAVA_LIBRARIES := \
    mockito-robolectric-prebuilt \
    truth-prebuilt

LOCAL_JAVA_LIBRARIES := \
    junit \
    platform-robolectric-3.4.2-prebuilt \
    sdk_vcurrent

LOCAL_INSTRUMENTATION_FOR := SettingsIntelligence
LOCAL_MODULE := SettingsIntelligenceRoboTests

LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_JAVA_LIBRARY)

#############################################################
# Turbo runner target to run the previous target. #
#############################################################
include $(CLEAR_VARS)

LOCAL_MODULE := RunSettingsIntelligenceRoboTests

LOCAL_SDK_VERSION := system_current

LOCAL_STATIC_JAVA_LIBRARIES := \
    SettingsIntelligenceRoboTests

LOCAL_TEST_PACKAGE := SettingsIntelligence

include prebuilts/misc/common/robolectric/3.4.2/run_robotests.mk
