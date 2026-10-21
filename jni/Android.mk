LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := swe-2.10.03

LOCAL_LDFLAGS   += -Wl,-z,max-page-size=16384,--gc-sections
# -O2: armeabi-v7a (thumb) otherwise gets the NDK preset -Oz; the other ABIs already have -O2
LOCAL_CFLAGS    += -O2
LOCAL_CFLAGS    += -ffunction-sections -fdata-sections -fvisibility=hidden -Wall
LOCAL_SRC_FILES := swedate.c swehouse.c swejpl.c swemmoon.c swemplan.c sweph.c swephlib.c swecl.c swehel.c swetest.c swetest_jni.c obama.c obama_jni.c
include $(BUILD_SHARED_LIBRARY)