LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := settings-intelligence-contract

LOCAL_STATIC_JAVA_LIBRARIES := android-support-annotations

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        $(call all-aidl-files-under, src) \
        $(call all-Iaidl-files-under, src)

LOCAL_SDK_VERSION := system_current

include $(BUILD_STATIC_JAVA_LIBRARY)
