package com.wangwei.camerahook;

import android.app.Application;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCamera implements IXposedHookLoadPackage {
    private static final String TAG = "HookCamera";
    private final HookConfig hookConfig = new HookConfig();
    private SurfaceTexture st = null;
    public static Camera camera;
    public static Surface mSurface;
    public static MediaPlayer mMediaPlayer;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("Loaded app: " + lpparam.packageName);
        Log.d(TAG, "Loaded app: " + lpparam.packageName );
        Log.d(TAG, "Hook config => " + hookConfig.describe());

        // Xposed模块自检测
        if (lpparam.packageName.equals(HookConfig.MODULE_PACKAGE)){
            XposedHelpers.findAndHookMethod("com.wangwei.camerahook.MainActivity",
                    lpparam.classLoader,
                    "isModuleActive",
                    XC_MethodReplacement.returnConstant(true));
        }

        if (lpparam.packageName.equals(HookConfig.MODULE_PACKAGE)) {
            XposedHelpers.findAndHookMethod("com.wangwei.camerahook.MainActivity",
                    lpparam.classLoader,
                    "getMessageInfo",
                    String.class,
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            //param.setResult("i am new result! before. ");
                            String para1 = (String)param.args[0];
                            String para2 = (String)param.args[1];

                            Log.d(TAG, "para1 before hook is : " + para1);
                            Log.d(TAG, "para2 before hook is : " + para2);

                            param.args[0] = para1 + '1';
                            param.args[1] = para2 + '2';
                        }
                    }
            );
        }
        if (!hookConfig.shouldHookPackage(lpparam.packageName)) {
            return;
        }

        if (!TextUtils.isEmpty(hookConfig.getTargetPackage())) {
            XposedHelpers.findAndHookMethod("android.hardware.Camera",
                    lpparam.classLoader,
                    "setPreviewTexture",
                    SurfaceTexture.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (param.args[0] != null) {
                                st = (SurfaceTexture) param.args[0];
                                Log.d(TAG, "SurfaceTexture class is : " + st.toString());
                            }
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(Application.class,
                    "attach",
                    Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            ClassLoader cl     = ((Context)param.args[0]).getClassLoader();

                            try {
                                final Class<?> hookclass = cl.loadClass(hookConfig.getCameraControllerClass());
                                Log.d(TAG, "查找自定义相机控制类成功: " + hookConfig.getCameraControllerClass());
                                Log.d(TAG, "hookclass is : " + hookclass.toString());

                                XposedHelpers.findAndHookMethod(hookclass,
                                        hookConfig.getTriggerMethodName(),
                                        new XC_MethodHook() {
                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                                if (st != null) {
                                                    // 将摄像头的previewTexture设置为空，然后将该Texture对象修改。
                                                    Field fieldA = hookclass.getDeclaredField(hookConfig.getCameraFieldName());
                                                    fieldA.setAccessible(true);

                                                    HookCamera.camera = (Camera)fieldA.get(param.thisObject);
                                                    if (HookCamera.camera != null) {
                                                        HookCamera.camera.setPreviewTexture(null);
                                                        Log.d(TAG, "deattach the camera and surfacetexture first.");
                                                    }

                                                    if (HookCamera.mSurface != null) {
                                                        HookCamera.mSurface.release();
                                                        HookCamera.mSurface = null;
                                                        Log.d(TAG, "释放Surface对象");
                                                    }

                                                    if (HookCamera.mMediaPlayer != null) {
                                                        HookCamera.mMediaPlayer.release();
                                                        HookCamera.mMediaPlayer = null;
                                                        Log.d(TAG, "释放MediaPlayer对象");
                                                    }

                                                    // 将surfacetexture的生产者设置为video
                                                    if (HookCamera.mSurface == null) {
                                                        HookCamera.mSurface = new Surface(st);
                                                        Log.d(TAG, "生成Surface对象");
                                                    }

                                                    if (HookCamera.mMediaPlayer == null) {
                                                        HookCamera.mMediaPlayer = new MediaPlayer();
                                                        HookCamera.mMediaPlayer.setSurface(mSurface);
                                                        HookCamera.mSurface.release();
                                                        Log.d(TAG, "生成MediaPlayer对象");
                                                    }

                                                    HookCamera.mMediaPlayer.setVolume(0, 0);
                                                    HookCamera.mMediaPlayer.setLooping(true);

                                                    HookCamera.mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                        @Override
                                                        public void onPrepared(MediaPlayer mp) {
                                                            HookCamera.mMediaPlayer.start();
                                                        }
                                                    });

                                                    try {
                                                        HookCamera.mMediaPlayer.setDataSource(hookConfig.getVideoPath());
                                                        HookCamera.mMediaPlayer.prepareAsync();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                            } catch (Exception e) {
                                Log.e(TAG, "查找自定义相机控制类出错", e);
                                return;
                            }


                        }
                    }
            );

        }
    }
}
