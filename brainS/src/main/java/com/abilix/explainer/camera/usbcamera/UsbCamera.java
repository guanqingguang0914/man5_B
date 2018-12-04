package com.abilix.explainer.camera.usbcamera;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;


import com.abilix.brain.BrainInfo;
import com.abilix.explainer.camera.CameraStateCallBack;
import com.abilix.explainer.camera.transvedio.VedioTransMgr;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.usbcamera.RobotCameraStateCode;
import com.abilix.usbcamera.client.CameraClient;
import com.abilix.usbcamera.client.ICameraClient;
import com.abilix.usbcamera.client.ICameraClientCallback;
import com.abilix.usbcamera.client.PreviewCallback;
import com.abilix.usbcamera.client.TakePictureCallback;

import java.util.HashMap;
import java.util.Iterator;


/**
 * @author jingh
 * @Descripton:对USB摄像头的封装，目前提供拍照、和预览数据两个接口
 * @date2017-5-26下午1:41:46
 */
public class UsbCamera implements IUsbCamera {
    private static final int DEFAULT_WIDTH = 320;
    private static final int DEFAULT_HEIGHT = 240;
    private static int mFrameWidth = DEFAULT_WIDTH;
    private static int mFrameHeight = DEFAULT_HEIGHT;

    private static final int USB_CONNECT = 0X01;
    private static final int USB_DISCONNECT = 0X00;
    private UsbManager mUsbManager;
    private Context mContext;
    private ICameraClient mCameraClient;
    private Object mLock = new Object();
    /**初始化和销毁mCameraClient时的同步锁*/
    private Object mCameraClientLock = new Object();
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    /**USB Camera是否已经打开成功的标志量*/
    private boolean isCameraClientConnected = false;
    private HashMap<String, UsbDevice> mUsbDevicesMap;
    private UsbDevice mDevice = null;
    private final String CAMERA_DEVICE_KEY = "";
    /**相机状态回调*/
    private CameraStateCallBack mCameraStateCallBack;
    //xiongxin@20171228 add start
    private CaptureCallback mCaptureCallback;
    private FaceTrackCallback mFaceTrackCallback;
    //add end
    private UsbCameraThread mUsbCameraThread;

    private static IUsbCamera iRobotCameraInstance;
    private CameraStateCallBack mTakePictureCallBack;

    private static final String ROBOT_BUILD_TYPE = Build.DISPLAY;

    //xiongxin@20171114 add start
    public interface CaptureCallback{
        public void onCapture(byte[] data);
    }
    //add end
    //xiongxin@20171228 add start
    public interface FaceTrackCallback{
        public void onFaceTrack(int[] position);
    }
    //add end

    //之前的系统不支持320X240的分辨率，需要根据系统版本号来做动态判断
    static {
        LogMgr.d("length:" + ROBOT_BUILD_TYPE.length());
        if (ROBOT_BUILD_TYPE.length() == 11) {
            char version1 = ROBOT_BUILD_TYPE.charAt(4);
            char version2 = ROBOT_BUILD_TYPE.charAt(10);
            int v1 = (int) version1;
            int v2 = (int) version2;
            if (v1 > 48 || v2 > 69) {
                mFrameWidth = 320;
                mFrameHeight = 240;
                LogMgr.d("Width:" + mFrameWidth + "     Height:" + mFrameHeight);
            }
        }
    }


    private UsbCamera() {

    }

    @Override
    public synchronized void registeSystemUSBCamera(Context context) {
        LogMgr.d("注册Usb摄像头");
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        LogMgr.d("摄像头插入");

                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        LogMgr.d("摄像头拔出");
                        sendUsbEventToApp(USB_DISCONNECT);
                        synchronized (mLock) {
                            mLock.notifyAll();
                        }
                        if (mCameraStateCallBack != null) {
                            mCameraStateCallBack.onState(RobotCameraStateCode.TAKE_PICTURE_USB_CAMERA_IS_NOT_CONNECTED);
                        }
                        VedioTransMgr.stopVedioTrans();
                        destoryCameraClient();
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

    private static void sendUsbEventToApp(int envnt) {
        switch (envnt) {
            case USB_CONNECT:

                break;
            case USB_DISCONNECT:
                //为兼容Brain的UDP通信，临时这么干
              /*  byte[] data_stop;
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_S) {
                    data_stop = ProtocolUtil.buildProtocol((byte) 0x05, (byte) 0xA5, (byte) 0x03, new byte[]{1});
                } else {
                    data_stop = new byte[]{'V', 'I', 'D', 'E', 'O', 'S', 'T', 'O', 'P'};
                }
                if (BrainService.getmBrainService() != null && BrainService.getmBrainService().getIp() != null) {
                    LogMgr.i("usb_disconnect = " + Utils.bytesToString(data_stop, data_stop.length));

                    UdpSender.getInstance().sendByUicast(data_stop, BrainService.getmBrainService().getIp(), BrainData.DATA_PORT_KNOW);
                }*/
                if (BrainInfo.mBrainInfo != null) {
                    BrainInfo.sendCVideoStop();
                }
                break;
            default:
                break;
        }

    }


    public static IUsbCamera create() {
        if (iRobotCameraInstance == null) {
            synchronized (UsbCamera.class) {
                if (iRobotCameraInstance == null) {
                    iRobotCameraInstance = new UsbCamera();
                }
            }
        }
        return iRobotCameraInstance;
    }

    //xiongxin@20171228 add start

    public void startFaceTrackAct(Context context,FaceTrackCallback cb){
        //注册人脸识别回调数据广播
        registerFaceTrackCallbackEvent(context);
        mFaceTrackCallback = cb;
        try {
            ComponentName cn = new ComponentName("com.abilix.learn.rtspserver",
                    "com.abilix.learn.rtspserver.usbcamera_facetrack.UsbCameraFaceTrackActivity");
            Intent intent = new Intent();
            intent.setComponent(cn);
            context.startActivity(intent);
        }catch (Exception e){
            Toast.makeText(context,"moudle not found",Toast.LENGTH_SHORT).show();
        }
        LogMgr.i("启动人脸识别");
    }

    private void registerFaceTrackCallbackEvent(Context context){
        context.registerReceiver(faceTrackreceiver,new IntentFilter("com.abilix.learn.rtspserver.facetrack.callback.action"));
    }

    private BroadcastReceiver faceTrackreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogMgr.i("收到人脸识别数据");
            int[] args = intent.getIntArrayExtra("position");
            if (mFaceTrackCallback !=null) mFaceTrackCallback.onFaceTrack(args);
        }
    };

    public void stopFaceTrackAct(Context context){
        context.sendBroadcast(new Intent("com.abilix.learn.rtspserver.facetrack.close.action"));
        try {
            context.unregisterReceiver(faceTrackreceiver);
        }catch (Exception e){}

        LogMgr.i("关闭人脸识别");
    }
    //add end

    /**
     * 初始化mCameraClient对象
     * @param context
     */
    private synchronized void initCameraClient(Context context) {
        synchronized (mCameraClientLock) {
            if (mCameraClient == null) {
                if (mCameraStateCallBack !=null){
                    mCameraStateCallBack.onState(RobotCameraStateCode.OPENING_CAMERA);
                }
                LogMgr.d("初始化 CameraClient");
                mCameraClient = new CameraClient(context, mCameraListener);
                mCameraClient.select(getCameraDevice());
                mCameraClient.resize(mFrameWidth, mFrameHeight);
                mCameraClient.connect();
            }
        }
    }

    /**
     * 销毁mCameraClient对象
     */
    private synchronized void destoryCameraClient() {
        synchronized (mCameraClientLock) {
            if (mCameraClient != null) {
                mCameraClient.stopPreview();
                mCameraClient.release();
                mCameraClient = null;
                isCameraClientConnected = false;
                mDevice = null;
                LogMgr.e("mCameraClient销毁置空");
            }
        }
    }

    //xiongxin@2017114 add start
    public void addCaptureCallback(CaptureCallback cb){
        mCaptureCallback = cb;
    }
    // add end

    @Override
    public synchronized void takePicture(final Context context, final String imagePath, final CameraStateCallBack cameraStateCallBack) {
        mCameraStateCallBack = cameraStateCallBack;
        if (mUsbCameraThread == null) {
            mUsbCameraThread = new UsbCameraThread();
        }
        mUsbCameraThread.cancelOtherTasks();
        mUsbCameraThread.runOnUsbCameraThread(new Runnable() {
            @Override
            public void run() {
                LogMgr.d("takePicture=====>");
                mContext = context;
                if (getCameraDevice() != null) {
                    initCameraClient(context);
                    synchronized (mLock) {
                        if (!isCameraClientConnected) {
                            try {
                                LogMgr.d("等待Camera 连接成功");
                                mLock.wait(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    LogMgr.d("开始拍照");
                    if (mCameraClient != null) {
                        mCameraClient.takePicture(imagePath, mCallback);
                    } else {
                        LogMgr.e("mCameraClient is null");
                    }

                } else {
                    if (mCameraStateCallBack != null) {
                        mCameraStateCallBack.onState(RobotCameraStateCode.TAKE_PICTURE_USB_CAMERA_IS_NOT_CONNECTED);
                    }
                }
            }
        });

    }


    private TakePictureCallback mCallback = new TakePictureCallback() {
        @Override
        public void onState(int state,byte[] data) {
            LogMgr.d("takePictureCallback");
            if(mCameraStateCallBack!=null) {
                mCameraStateCallBack.onState(state);
            }
            //xiongxin@20171114 add start
            if (mCaptureCallback !=null){
                mCaptureCallback.onCapture(data);
            }
            //add end
        }
    };

    public static int getWidth() {
        //  LogMgr.d("mVideoWidth:"+mVideoWidth);
        return mFrameWidth;
    }

    public static int getHeight() {
        //  LogMgr.d("mVideoHeight:"+mVideoHeight);
        return mFrameHeight;
    }

    @Override
    public synchronized void preview(final Context context, final PreviewCallback callback, final CameraStateCallBack cameraStateCallBack) {
        LogMgr.d("preview===>");
        mCameraStateCallBack = cameraStateCallBack;

        LogMgr.d("preview run");
        mContext = context;
        if (getCameraDevice() != null) {
            initCameraClient(context);
            if (!isCameraClientConnected) {
                synchronized (mLock) {
                    if (!isCameraClientConnected) {
                        try {
                            LogMgr.d("等待Camera 连接成功");
                            mLock.wait(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            LogMgr.d("开始预览");
            mCameraClient.preview(callback);
        } else {
            LogMgr.e("Usb摄像头未插入");
        }
    }


    @Override
    public synchronized void stopPreview() {
        if (mCameraClient != null) {
            destoryCameraClient();
            LogMgr.e("停止预览");
        }
    }


    @Override
    public synchronized void setBrightnessS(final Context context, final int cmd, final CameraStateCallBack cameraStateCallBack) {
        mCameraStateCallBack = cameraStateCallBack;
        if (mUsbCameraThread == null) {
            mUsbCameraThread = new UsbCameraThread();
        }
        mUsbCameraThread.cancelOtherTasks();
        mUsbCameraThread.runOnUsbCameraThread(new Runnable() {
            @Override
            public void run() {
                mContext = context;
                if (getCameraDevice() != null) {
                    initCameraClient(context);
                    synchronized (mLock) {
                        if (!isCameraClientConnected) {
                            try {
                                LogMgr.d("等待Camera 连接成功");
                                mLock.wait(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    LogMgr.d("设置摄像头灯");
                    mCameraClient.setBrightness(cmd);

                }
            }
        });

    }

    @Override
    public String getPicturePath() {
        String path = FileUtils.getFilePath(FileUtils.DIR_ABILIX_PHOTO, "1", FileUtils._TYPE_JPEG);
        FileUtils.buildDirectory(path);
        return path;
    }

    @Override
    public void setIsRotate(boolean b) {

    }


    /**
     * 获取当前USBDevice，没有USB设备插入则返回空
     */
    private UsbDevice getCameraDevice() {
        if (mDevice == null) {
            if (mUsbManager == null) {
                if (mContext != null) {
                    mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
                }
            }
            mUsbDevicesMap = mUsbManager.getDeviceList();
            Iterator<String> mIterator = mUsbDevicesMap.keySet().iterator();
            while (mIterator.hasNext()) {
                String key = (String) mIterator.next();
                LogMgr.d("UsbDevice key:" + key);
                mDevice = mUsbDevicesMap.get(key);

            }
        }

        return mDevice;
    }


    /**
     * 销毁USB摄像头服务，如果再次使用则需要重新初始化USB摄像头服务
     */
    @Override
    public synchronized void destory() {
        synchronized (mLock) {
            mLock.notifyAll();
        }
        mCameraStateCallBack=null;
        if (mCameraClient != null) {
            mCameraClient.stopPreview();
            mCameraClient.release();
            mCameraClient = null;
            isCameraClientConnected = false;
            mDevice = null;
            LogMgr.e("mCameraClient销毁置空");
        }
    }

    @Override
    public void cancelTakePicCallback() {
        mCameraStateCallBack=null;
    }

    /**更新变量 isCameraClientConnected 的回调*/
    private final ICameraClientCallback mCameraListener = new ICameraClientCallback() {
        @Override
        public void onConnect() {
            LogMgr.d("camera onConnect");
            isCameraClientConnected = true;
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }

        @Override
        public void onDisconnect() {
            synchronized (mLock) {
                mLock.notifyAll();
            }
            destory();
            LogMgr.e("camera onDisconnect");
        }

    };
}
