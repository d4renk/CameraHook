package com.wangwei.camerahook;

import de.robv.android.xposed.XSharedPreferences;

/**
 * 用于集中管理 Hook 参数，便于将原有的抖音专用逻辑切换到任意目标应用。
 */
class HookConfig {
    static final String MODULE_PACKAGE = "com.wangwei.camerahook";
    private static final String DEFAULT_TARGET_PACKAGE = "com.ss.android.ugc.aweme";
    private static final String DEFAULT_CAMERA_CONTROLLER_CLASS = "com.ss.android.ttvecamera.a";
    private static final String DEFAULT_TRIGGER_METHOD_NAME = "a";
    private static final String DEFAULT_CAMERA_FIELD_NAME = "a";
    private static final String DEFAULT_VIDEO_PATH = "/sdcard/DCIM/Camera/video_20190909_172631.mp4";

    private final String targetPackage;
    private final String cameraControllerClass;
    private final String triggerMethodName;
    private final String cameraFieldName;
    private final String videoPath;
    private final boolean prefsReadable;

    HookConfig() {
        XSharedPreferences prefs = new XSharedPreferences(MODULE_PACKAGE, "hook_config");
        prefsReadable = prefs.getFile().canRead();

        targetPackage = chooseValue(prefs.getString("target_package", DEFAULT_TARGET_PACKAGE), DEFAULT_TARGET_PACKAGE);
        cameraControllerClass = chooseValue(prefs.getString("camera_controller_class", DEFAULT_CAMERA_CONTROLLER_CLASS), DEFAULT_CAMERA_CONTROLLER_CLASS);
        triggerMethodName = chooseValue(prefs.getString("trigger_method_name", DEFAULT_TRIGGER_METHOD_NAME), DEFAULT_TRIGGER_METHOD_NAME);
        cameraFieldName = chooseValue(prefs.getString("camera_field_name", DEFAULT_CAMERA_FIELD_NAME), DEFAULT_CAMERA_FIELD_NAME);
        videoPath = chooseValue(prefs.getString("video_path", DEFAULT_VIDEO_PATH), DEFAULT_VIDEO_PATH);
    }

    private String chooseValue(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    boolean shouldHookPackage(String packageName) {
        if ("*".equals(targetPackage)) {
            return true;
        }
        return targetPackage.equals(packageName);
    }

    String getTargetPackage() {
        return targetPackage;
    }

    String getCameraControllerClass() {
        return cameraControllerClass;
    }

    String getTriggerMethodName() {
        return triggerMethodName;
    }

    String getCameraFieldName() {
        return cameraFieldName;
    }

    String getVideoPath() {
        return videoPath;
    }

    boolean isPrefsReadable() {
        return prefsReadable;
    }

    String describe() {
        return "targetPackage=" + targetPackage
                + ", cameraControllerClass=" + cameraControllerClass
                + ", triggerMethodName=" + triggerMethodName
                + ", cameraFieldName=" + cameraFieldName
                + ", videoPath=" + videoPath
                + ", prefsReadable=" + prefsReadable;
    }
}
