package com.abilix.usbcamera;
/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: CaptureActivity.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb, jin/libuvc, jni/rapidjson folder may have a different license, see the respective files.
*/

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;


import com.abilix.usbcamera.utils.LogMgr;

import java.lang.Thread.UncaughtExceptionHandler;

public class CaptureActivity extends Activity {
    private static final boolean DEBUG = false;
    private static final String TAG = "CaptureActivity";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            if (DEBUG) Log.i(TAG, "onCreate:new");
            final Fragment fragment = new CameraFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment).commit();
        }
        //	uncatchExecptionForLog();
    }

    // 拦截不可捕获的异常，保存到Log日志里
    private void uncatchExecptionForLog() {
        // 拦截异常
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                throwable.printStackTrace();
                LogMgr.e("UsbCamera已经崩溃！！！！！崩溃原因：" + throwable.toString());
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume:");
//		updateScreenRotation();
    }

    @Override
    protected void onPause() {
        if (DEBUG) Log.v(TAG, "onPause:isFinishing=" + isFinishing());
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        super.onDestroy();
    }

    protected final void updateScreenRotation() {
        final int screenRotation = 2;
        switch (screenRotation) {
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
            case 2:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            default:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
        }
    }

}
