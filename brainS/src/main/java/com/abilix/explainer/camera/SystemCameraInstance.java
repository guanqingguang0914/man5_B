package com.abilix.explainer.camera;

import java.nio.ByteBuffer;
import java.util.Arrays;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.opengl.GLES11Ext;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import com.abilix.explainer.utils.LogMgr;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SystemCameraInstance {

    private static SystemCameraInstance instance;
    private static Context mContext;
    private static SystemCameraInstanceCreateListener mSystemCameraInstanceCreateListener;
    private static CameraManager mCMgr;
    private static ImageReader mImageReader;
    private static CameraDevice mCameraDevice;
    private static CameraCaptureSession mCaptureSession;
    private static SurfaceTexture mSurfaceTexture;
    private static Surface mSurface;
    private static CaptureRequest.Builder mPreviewRequestBuilder;
    private static CameraStateCallBack mTackePictureCallBack;

    private int mState = STATE_PREVIEW;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static String mImgPath;
    private static HandlerThread mCameraThread;
    private static Handler mCameraHandler;
    private static CameraDevice.StateCallback mStateCallback;

    private SystemCameraInstance() {

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void createCameraInstance(Context context, SystemCameraInstanceCreateListener systemCameraInstanceCreateListener) {
        LogMgr.d("createCameraInstance>>>");
        if (mCameraThread == null) {
            mCameraThread = new HandlerThread("cameraThread");
            mCameraThread.start();
        }
        if (mCameraHandler == null) {
            mCameraHandler = new Handler(mCameraThread.getLooper());
        }
        mSystemCameraInstanceCreateListener = systemCameraInstanceCreateListener;
        mContext = context;
        mCMgr = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mImageReader = ImageReader.newInstance(480, 640, ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(mImageAvailableListener, mCameraHandler);
        mStateCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(CameraDevice camera) {
                LogMgr.d("摄像头打开成功");
                mCameraDevice = camera;
                createCameraPreviewSession();

            }

            @Override
            public void onError(CameraDevice camera, int error) {
                LogMgr.e("摄像头打开onError");

            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                LogMgr.e("摄像头打开onDisconnected");

            }
        };
        try {
            LogMgr.d("openCamera>>>>");
            mCMgr.openCamera("0", mStateCallback, mCameraHandler);
        } catch (Exception e) {
            LogMgr.e("摄像头打开失败");
            e.printStackTrace();
        }
    }

    private static OnImageAvailableListener mImageAvailableListener = new OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            LogMgr.e("onImageAvailable,保存照片文件");
            ByteBuffer buffer = reader.acquireNextImage().getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            mCameraHandler.post(new ImageSaver(bytes, mImgPath, new PictureFileSaveListener() {

                @Override
                public void onSucess() {
                    mTackePictureCallBack.onState(RobotCameraStateCode.SAVE_PICTURE_SUCESS);
                }

                @Override
                public void onFailed(int stateCode) {
                    mTackePictureCallBack.onState(stateCode);
                }
            }));
            // reader.close();

        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void createCameraPreviewSession() {
        LogMgr.d("createCameraPreviewSession>>>");
        mSurfaceTexture = new SurfaceTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        // mSurfaceTexture = new
        // SurfaceTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);

        if (mSurfaceTexture == null) {
            LogMgr.e("mSurfaceTexture为空");
        }
        try {
            mSurface = new Surface(mSurfaceTexture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    LogMgr.e("onConfigured");
                    if (null == mCameraDevice) {
                        LogMgr.e("mCameraDevice==null");
                        return;
                    }
                    mCaptureSession = cameraCaptureSession;
                    LogMgr.i("create cameraInstance sucess");
                    if (instance == null) {
                        instance = new SystemCameraInstance();
                    }
                    mSystemCameraInstanceCreateListener.onSucess(instance);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    mSystemCameraInstanceCreateListener.onFailed(RobotCameraStateCode.TAKE_PICTURE_CONFIGURED_FAILED);
                    LogMgr.e("摄像头配置失败");
                }
            }, null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void destoryCameraInstance() {
        mContext = null;
        mSystemCameraInstanceCreateListener = null;
        mCMgr = null;
        try {
            mCaptureSession.abortCaptures();
            mCaptureSession.stopRepeating();
            mCaptureSession.close();
            mCaptureSession = null;
        } catch (CameraAccessException e) {
            LogMgr.e("system usb cameraInstance destroy error");
            e.printStackTrace();
        }
        mImageReader.close();
        mImageReader = null;
        mCameraDevice.close();
        mCameraDevice = null;
        mSurfaceTexture.release();
        mSurfaceTexture = null;
        mPreviewRequestBuilder = null;
        mStateCallback = null;
        mTackePictureCallBack = null;
        instance = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void takePicture(String imagePath, CameraStateCallBack tackePictureCallBack) {
        LogMgr.d("拍照");
        mImgPath = imagePath;
        mTackePictureCallBack = tackePictureCallBack;

        try {
            // CaptureRequest.CONTROL_AF_TRIGGER 代表自动聚焦 key值
            // CameraMetadata.CONTROL_AF_TRIGGER_START 代表自动聚焦开始 value值
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            LogMgr.d("onCaptureProgressed");
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            LogMgr.e("照片拍摄完成");
            process(result);
        }

    };

    private void process(CaptureResult result) {
        switch (mState) {
            case STATE_PREVIEW: {
                LogMgr.d("相机状态:STATE_PREVIEW");
                break;
            }
            case STATE_WAITING_LOCK: {
                LogMgr.d("相机状态:STATE_WAITING_LOCK");
                captureStillPicture();
                break;
            }
            case STATE_WAITING_PRECAPTURE: {
                LogMgr.d("STATE_WAITING_PRECAPTURE");
                break;
            }
            case STATE_WAITING_NON_PRECAPTURE: {
                LogMgr.d("STATE_WAITING_PRECAPTURE");
                break;
            }
        }
    }

    private void captureStillPicture() {
        try {
            LogMgr.d("captureStillPicture>>>");
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            // captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
            // CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    LogMgr.d("onCaptureCompleted");
                    // unlockFocus();
                }
            };
            mCaptureSession.stopRepeating();
            LogMgr.d("capture>>>");
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            // mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
            // CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mCameraHandler);
            // After this, the camera will go back to the normal state of
            // preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}
