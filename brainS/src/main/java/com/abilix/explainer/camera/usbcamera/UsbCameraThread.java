package com.abilix.explainer.camera.usbcamera;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Usb摄像头 相关功能函数在该子线程运行
 * Created by jingh on 2017/6/8.
 */

public class UsbCameraThread {
    private HandlerThread mUsbCameraThread;
    private Handler mUsbCameraHandler;
    public UsbCameraThread(){
        mUsbCameraThread=new HandlerThread("UsbCameraThread");
        mUsbCameraThread.start();
        mUsbCameraHandler=new Handler(mUsbCameraThread.getLooper());
    }
    public void runOnUsbCameraThread(Runnable r){
        if (mUsbCameraHandler!=null){
            mUsbCameraHandler.post(r);
        }
    }
    public void cancelOtherTasks(){
        if (mUsbCameraHandler!=null){
            mUsbCameraHandler.removeCallbacksAndMessages(null);
        }
    }
}
