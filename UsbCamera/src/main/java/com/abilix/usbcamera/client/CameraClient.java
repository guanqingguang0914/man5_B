package com.abilix.usbcamera.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;

import com.abilix.usbcamera.IPreviewCallback;
import com.abilix.usbcamera.ITakePictureCallback;
import com.abilix.usbcamera.IUVCService;
import com.abilix.usbcamera.IUVCServiceCallback;
import com.abilix.usbcamera.utils.LogMgr;


/**
 * @author jingh
 * @Descripton:之前代码逻辑混乱，有问题一直查不到原因，重写一下
 * @date2017-5-25下午2:46:48
 */
public class CameraClient implements ICameraClient {

    private static final int MSG_SELECT = 0;
    private static final int MSG_CONNECT = 1;
    private static final int MSG_DISCONNECT = 2;
    private static final int MSG_ADD_SURFACE = 3;
    private static final int MSG_REMOVE_SURFACE = 4;
    private static final int MSG_START_RECORDING = 6;
    private static final int MSG_STOP_RECORDING = 7;
    private static final int MSG_CAPTURE_STILL = 8;
    private static final int MSG_RESIZE = 9;
    private static final int MSG_TAKEPICTURE = 10;
    private static final int MSG_PREVIEW = 11;
    private static final int MSG_RELEASE = 99;
    private static final int MSG_SET_BRIGHTNESS = 100;
    private static final int MSG_GET_BRIGHTNESS = 101;

    private Context mContext;
    /**本地用于更新Camera打开状态的回调*/
    private static ICameraClientCallback mListener;
    /**rtsp端UVCService服务对象*/
    private IUVCService mService;
    private int mServiceId;
    private static boolean isServiceConnected = false;
    /**使用mService时的锁，用于等待mService对象的获取*/
    protected final Object mServiceSync = new Object();
    private TaskHandler mTaskHandler;
    private HandlerThread mTaskThread;
    /**当前选定的USB设备*/
    protected UsbDevice mUsbDevice;


    // jingh add 增加拍照成功回调
    private static TakePictureCallback mCallback;
    private static PreviewCallback mPreviewCallback;
    // private static IUVCServiceCallback mIUVIuvcServiceCallback;
    private static Handler mHandler=new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            LogMgr.d("call back");
            mCallback.onState(msg.what, (byte[]) msg.obj);
        }
    };
    private static ITakePictureCallback mITakePictureCallback = new ITakePictureCallback.Stub() {

        @Override
        public void onState(int state,byte[] data) throws RemoteException {
            if (mCallback!=null){
                mCallback.onState(state,data);
                LogMgr.d("callback from remote process " + data.length);
            }

         /*   Message msg=mHandler.obtainMessage();
            msg.what=state;
            msg.obj = data;
            mHandler.sendMessage(msg);*/
        }
    };

    private static IPreviewCallback mIPreviewCallback = new IPreviewCallback.Stub() {

        @Override
        public void onPreviewFrame(byte[] data) throws RemoteException {
            if (mPreviewCallback != null) {
                mPreviewCallback.onPreviewFrame(data);
            }
        }
    };
    /**传递到rtsp UVCService服务端的回调，回调中调用mListener的回调用于更新Camera的打开状态*/
    private static IUVCServiceCallback mIUVIuvcServiceCallback = new IUVCServiceCallback.Stub() {
        @Override
        public void onConnected() throws RemoteException {
            LogMgr.v("onConnected:");
            isServiceConnected = true;
            if (mListener != null) {
                mListener.onConnect();
            }
        }

        @Override
        public void onDisConnected() throws RemoteException {
            LogMgr.v("onDisConnected:");
            isServiceConnected = false;
            if (mListener != null) {
                mListener.onDisconnect();
            }
        }
    };

    /**
     * 设置Camera打开状态回调，绑定rtsp UVC服务
     * @param context
     * @param listener
     */
    public CameraClient(final Context context, final ICameraClientCallback listener) {
        LogMgr.v("Constructor:");
        mContext = context;
        mListener = listener;

        mTaskThread = new HandlerThread("TaskThread");
        mTaskThread.start();
        mTaskHandler = new TaskHandler(mTaskThread.getLooper());
        mTaskHandler.post(new Runnable() {
            @Override
            public void run() {
                doBindService();
            }
        });
    }

    /**
     * 绑定rtsp UVC服务
     * @return
     */
    private boolean doBindService() {
        LogMgr.d("doBindService===>");
        synchronized (mServiceSync) {
            if (mService == null) {
                if (mContext != null) {
                    final Intent intent = new Intent(IUVCService.class.getName());
                    //intent.setPackage("com.abilix.usbcamera");
                    intent.setPackage("com.abilix.learn.rtspserver");
                    LogMgr.d("bindService");
                    mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                } else
                    return true;
            }
        }
        return false;
    }

    private void doUnBindService() {
        LogMgr.v("doUnBindService===>");
        if (mService != null) {
            if (mContext != null) {
                LogMgr.v("doUnBindService");
                mContext.unbindService(mServiceConnection);
                final Intent intent = new Intent(IUVCService.class.getName());
                //intent.setPackage("com.abilix.usbcamera");
                intent.setPackage("com.abilix.learn.rtspserver");
                mContext.stopService(intent);
            }
             /*ActivityManager
             am=(ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
             am.killBackgroundProcesses("com.serenegiant.service.UVCService");*/
            mService = null;
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            LogMgr.e("绑定UVCService成功");
            synchronized (mServiceSync) {
                mService = IUVCService.Stub.asInterface(service);
                mServiceSync.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            LogMgr.e("Service:" + name + "断开连接");
            synchronized (mServiceSync) {
                mService = null;
                mServiceSync.notifyAll();
            }
        }
    };

    @Override
    public void select(final UsbDevice device) {
        LogMgr.v("select:device=" + (device != null ? device.getDeviceName() : null));
        mUsbDevice = device;
        mTaskHandler.sendMessage(mTaskHandler.obtainMessage(MSG_SELECT, device));
    }

    @Override
    public void release() {
        LogMgr.v("release:" + this);
       /* mUsbDevice = null;
        mTaskHandler.post(new Runnable() {
            @Override
            public void run() {
                doUnBindService();
            }
        });*/

        	boolean isSendSucess = mTaskHandler.sendEmptyMessage(MSG_RELEASE);
        //LogMgr.v("release message send sucess:" + isSendSucess);
    }

    @Override
    public UsbDevice getDevice() {
        return mUsbDevice;
    }

    @Override
    public void resize(final int width, final int height) {
        LogMgr.v(String.format("resize(%d,%d)", width, height));
        mTaskHandler.sendMessage(mTaskHandler.obtainMessage(MSG_RESIZE, width, height));
    }

    @Override
    public void connect() {
        boolean isConnectSendSucess = mTaskHandler.sendEmptyMessage(MSG_CONNECT);
        LogMgr.v("isConnectSendSucess:" + isConnectSendSucess);
    }

    @Override
    public void disconnect() {
        LogMgr.v("disconnect:" + this);
        mTaskHandler.sendEmptyMessage(MSG_DISCONNECT);
    }

    @Override
    public void addSurface(final Surface surface, final boolean isRecordable) {
        LogMgr.v("addSurface:surface=" + surface + ",hash=" + surface.hashCode());
        mTaskHandler.sendMessage(mTaskHandler.obtainMessage(MSG_ADD_SURFACE, isRecordable ? 1 : 0, 0, surface));
    }

    @Override
    public void removeSurface(final Surface surface) {
        LogMgr.v("removeSurface:surface=" + surface + ",hash=" + surface.hashCode());
        mTaskHandler.sendMessage(mTaskHandler.obtainMessage(MSG_REMOVE_SURFACE, surface));
    }

    @Override
    public boolean isRecording() {
        return isServiceRecording();
    }

    @Override
    public void startRecording() {
        if ((mTaskHandler != null) && !isServiceRecording()) {
            mTaskHandler.sendEmptyMessage(MSG_START_RECORDING);
        }
    }

    @Override
    public void stopRecording() {
        if ((mTaskHandler != null) && isServiceRecording()) {
            mTaskHandler.sendEmptyMessage(MSG_STOP_RECORDING);
        }
    }

    @Override
    public void captureStill(final String path) {
        if (mTaskHandler != null) {
            mTaskHandler.sendMessage(mTaskHandler.obtainMessage(MSG_CAPTURE_STILL, path));
        }
    }

    @Override
    public void takePicture(final String path, TakePictureCallback callback) {
        LogMgr.d("takePicture===>");
        mCallback = callback;
        if (mTaskHandler != null) {
            mTaskHandler.sendMessage(mTaskHandler.obtainMessage(MSG_TAKEPICTURE, path));
        }
    }

    @Override
    public void preview(PreviewCallback callback) {
        LogMgr.e("preview===>");
        mPreviewCallback = callback;
        if (mTaskHandler != null) {
            LogMgr.d("preview===>sendMessage");
            mTaskHandler.sendMessage(mTaskHandler.obtainMessage(MSG_PREVIEW));
        }
    }

    @Override
    public void stopPreview() {

        mPreviewCallback = null;

    }

    @Override
    public void setBrightness(int brightnessValue) {
        if (mTaskHandler != null) {
            mTaskHandler.sendMessage(mTaskHandler.obtainMessage(MSG_SET_BRIGHTNESS, brightnessValue));
        }
    }

    @Override
    public void getBrightness() {
        if (mTaskHandler != null) {
            mTaskHandler.sendMessage(mTaskHandler.obtainMessage(MSG_GET_BRIGHTNESS));
        }
    }

    private boolean isServiceRecording() {
        if (mService != null)
            try {
                return mService.isRecording(mServiceId);
            } catch (final RemoteException e) {
                LogMgr.e("isRecording:" + e);
            }
        return false;
    }

    private class TaskHandler extends Handler {

        public TaskHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // jingh add 如果还没获取到服务就阻塞在这里
            if (mService == null) {
                synchronized (mServiceSync) {
                    try {
                        LogMgr.e("还没获取到UVCService，等待中");
                            mServiceSync.wait(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            LogMgr.d("handleMessage what:" + msg.what);
            switch (msg.what) {
                case MSG_SELECT:
                    handleSelect((UsbDevice) msg.obj);
                    break;
                case MSG_CONNECT:
                    handleConnect();
                    break;
                case MSG_DISCONNECT:
                    handleDisconnect();
                    break;
                case MSG_ADD_SURFACE:
                    handleAddSurface((Surface) msg.obj, msg.arg1 != 0);
                    break;
                case MSG_REMOVE_SURFACE:
                    handleRemoveSurface((Surface) msg.obj);
                    break;
                case MSG_START_RECORDING:
                    handleStartRecording();
                    break;
                case MSG_STOP_RECORDING:
                    handleStopRecording();
                    break;
                case MSG_CAPTURE_STILL:
                    handleCaptureStill((String) msg.obj);
                    break;
                case MSG_TAKEPICTURE:
                    takePicture((String) msg.obj);
                    break;
                case MSG_PREVIEW:
                    preview();
                    break;
                case MSG_RESIZE:
                    handleResize(msg.arg1, msg.arg2);
                    break;
                case MSG_RELEASE:
                    handleRelease();
                    break;
                case MSG_SET_BRIGHTNESS:
                    setBrightnessS(mServiceId, (Integer) msg.obj);
                    break;
                default:
                    throw new RuntimeException("unknown message:what=" + msg.what);
            }

            LogMgr.i("cameraClient handle finish :" + msg.what);
        }
    }

    private void handleSelect(final UsbDevice device) {
        LogMgr.v("handleSelect:");
        if (mService != null) {
            try {
                mServiceId = mService.select(device, mIUVIuvcServiceCallback);
            } catch (final RemoteException e) {
                LogMgr.e("select:" + e);
            }
        }
    }

    private void handleRelease() {
        LogMgr.v("handleRelease:");

        isServiceConnected = false;
        doUnBindService();
    }

    private void handleConnect() {
        LogMgr.v("handleConnect:" + isServiceConnected);
        if (mService != null)
            try {
                if (!isServiceConnected) {
                    LogMgr.d("service connect");
                    mService.connect(mServiceId);
                } else {
                    mIUVIuvcServiceCallback.onConnected();
                }
            } catch (final RemoteException e) {
                LogMgr.e("handleConnect:" + e);
            }
    }

    private void handleDisconnect() {
        LogMgr.v("handleDisconnect:");
        if (mService != null)
            try {
                if (mService.isConnected(mServiceId)) {
                    mService.disconnect(mServiceId);
                } else {
                    mIUVIuvcServiceCallback.onDisConnected();
                }
            } catch (final RemoteException e) {
                LogMgr.e("handleDisconnect:" + e);
            }
        isServiceConnected = false;
    }

    private void handleAddSurface(final Surface surface, final boolean isRecordable) {
        LogMgr.v("handleAddSurface:surface=" + surface + ",hash=" + surface.hashCode());
        if (mService != null)
            try {
                mService.addSurface(mServiceId, surface.hashCode(), surface, isRecordable);
            } catch (final RemoteException e) {
                LogMgr.e("handleAddSurface:" + e);
            }
    }

    private void handleRemoveSurface(final Surface surface) {
        LogMgr.v("handleRemoveSurface:surface=" + surface + ",hash=" + surface.hashCode());
        if (mService != null)
            try {
                mService.removeSurface(mServiceId, surface.hashCode());
            } catch (final RemoteException e) {
                LogMgr.e("handleRemoveSurface:" + e);
            }
    }

    private void handleStartRecording() {
        LogMgr.v("handleStartRecording:");
        if (mService != null)
            try {
                if (!mService.isRecording(mServiceId)) {
                    mService.startRecording(mServiceId);
                }
            } catch (final RemoteException e) {
                LogMgr.e("handleStartRecording:" + e);
            }
    }

    private void handleStopRecording() {
        LogMgr.v("handleStopRecording:");
        if (mService != null)
            try {
                if (mService.isRecording(mServiceId)) {
                    mService.stopRecording(mServiceId);
                }
            } catch (final RemoteException e) {
                LogMgr.e("handleStopRecording:" + e);
            }
    }

    private void handleCaptureStill(final String path) {
        LogMgr.v("handleCaptureStill:" + path);
        if (mService != null)
            try {
                mService.captureStillImage(mServiceId, path);
            } catch (final RemoteException e) {
                LogMgr.e("handleCaptureStill:" + e);
            }
    }

    private void takePicture(final String path) {
        LogMgr.e("CameraTask===>拍照路径：" + path);
        LogMgr.v("handleCaptureStill:" + path);
        if (mService != null)
            try {
                if (mITakePictureCallback != null) {
                    LogMgr.e("UVCService===>mITakePictureCallback is null：" + (mITakePictureCallback == null ? true : false));
                    mService.takePicture(mServiceId, path, mITakePictureCallback);
                }
            } catch (final RemoteException e) {
                LogMgr.e("handleCaptureStill:" + e);
            }
    }

    private void setBrightnessS(final int serviceId, final int brightness) {
        if (mService != null) {
            try {
                mService.setBrightnessS(serviceId, brightness);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private int getBrightnessS(final int serviceId) {
        int mBrightness = -1;
        if (mService != null) {
            try {
                mBrightness = mService.getBrightnessS(serviceId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return mBrightness;
    }

    private void preview() {
        LogMgr.e("CameraTask===>preview");
        if (mService != null)
            try {
                if (mIPreviewCallback != null) {
                    LogMgr.e("UVCService===>mIPreviewCallback is null：" + (mIPreviewCallback == null ? true : false));
                    mService.preview(mServiceId, mIPreviewCallback);
                }
            } catch (final RemoteException e) {
                LogMgr.e("handleCaptureStill:" + e);
            }

    }

    private void handleResize(final int width, final int height) {
        LogMgr.v(String.format("handleResize(%d,%d)", width, height));
        if (mService != null)
            try {
                mService.resize(mServiceId, width, height);
            } catch (final RemoteException e) {
                LogMgr.e("handleResize:" + e);
            }
    }

}
