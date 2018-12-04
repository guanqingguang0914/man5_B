package com.abilix.explainer.camera.systemusbcamera;

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.abilix.explainer.camera.RobotCameraStateCode;
import com.abilix.explainer.camera.SystemCameraInstance;
import com.abilix.explainer.camera.SystemCameraInstanceCreateListener;
import com.abilix.explainer.camera.CameraStateCallBack;
import com.abilix.explainer.utils.LogMgr;


public class SystemUSBCamera {
    private static SystemCameraInstance mCameraInstance;
    private static UsbManager mUsbManager;
    private static Context mContext;
    private static Object mLock = new Object();
    private static BroadcastReceiver receiver;
    private static IntentFilter intentFilter;

    public static void registeSystemUSBCamera(Context context) {
        LogMgr.d("注册Usb摄像头");
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        if (mCameraInstance != null) {
                            mCameraInstance.destoryCameraInstance();
                            mCameraInstance = null;
                            LogMgr.e("Usb摄像头拔出，mCameraInstance销毁置空");
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(receiver, intentFilter);
    }

    public static synchronized void takePicture(Context context, String imagePath, final CameraStateCallBack cameraStateCallBack) {
        mContext = context;
        // 判断Usb摄像头是否插入
        if (!isConnectedUsbDevice()) {
            LogMgr.e("Usb摄像头未插入");
            cameraStateCallBack.onState(RobotCameraStateCode.TAKE_PICTURE_USB_CAMERA_IS_NOT_CONNECTED);
            return;
        }

        if (mCameraInstance == null) {
            LogMgr.e("mCameraInstance is null,create camera instance");
            cameraStateCallBack.onState(RobotCameraStateCode.OPENING_CAMERA);
            SystemCameraInstance.createCameraInstance(context, new SystemCameraInstanceCreateListener() {

                @Override
                public void onSucess(SystemCameraInstance instance) {
                    mCameraInstance = instance;
                    LogMgr.d("create system usb camera instance sucess");
                    synchronized (mLock) {
                        LogMgr.d("notify all create system usb camera instance sucess");
                        mLock.notifyAll();
                    }

                }

                @Override
                public void onFailed(int state) {
                    LogMgr.e("create system usb camera instance failed");
                    cameraStateCallBack.onState(state);
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                }
            });
            synchronized (mLock) {
                try {
                    // 初始化摄像头时候最多等待10秒钟，10秒钟后，摄像头还未初始化成功，拍照失败
                    mLock.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (mCameraInstance == null) {
            cameraStateCallBack.onState(RobotCameraStateCode.TAKE_PICTURE_CAMERA_INIT_ERROR);
            return;
        }
        cameraStateCallBack.onState(RobotCameraStateCode.OPEN_CAMERA_SUCESS);
        LogMgr.d("初始化摄像头成功，准备拍照");
        mCameraInstance.takePicture(imagePath, cameraStateCallBack);
    }

    /**
     * 判断当前设备是否连接USB设备
     */
    private static boolean isConnectedUsbDevice() {
        if (mUsbManager == null) {
            if (mContext != null) {
                mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            }
        }
        HashMap<String, UsbDevice> mUsbDevicesMap = mUsbManager.getDeviceList();
        LogMgr.d("mUsbDevicesMap.size():" + mUsbDevicesMap.size());
        if (mUsbDevicesMap.size() > 1) {
            return true;
        }

        return false;

    }
}
