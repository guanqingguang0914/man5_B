package com.abilix.explainer.camera.systemcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.SurfaceView;

import com.abilix.explainer.camera.CameraStateCallBack;
import com.abilix.explainer.camera.IRobotCamera;
import com.abilix.explainer.camera.ImageSaver;
import com.abilix.explainer.camera.PictureFileSaveListener;
import com.abilix.explainer.camera.RobotCameraStateCode;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.usbcamera.client.PreviewCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * 封装系统原生Camera API
 * Created by jingh on 2017/5/31.
 */

public class SystemCamera implements IRobotCamera {
    /**
     * 系统摄像头对象
     */
    private static Camera mCamera;
    private SurfaceTexture mSurfaceTexture;
    // private static SystemCamera instance;
    private static Object mLock = new Object();
    private static Context mContext;
    /**
     * 回调视频数据到上层的回调
     */
    private static PreviewCallback mPreviewCallback;
    private static CameraStateCallBack mTackePictureCallBack;
    /**
     * 照片保存线程
     */
    private static HandlerThread mCameraThread;
    /**
     * 照片保存Handler
     */
    private static Handler mCameraHandler;
    private static String mImgPath;
    private static SurfaceView mSurfaceView;
    private static IRobotCamera instance;
    /**
     * 发送视频宽度
     */
    private static int mVideoWidth = 320;
    /**
     * 发送视频高度
     */
    private static int mVideoHeight = 240;
    /**
     * 视频格式索引
     */
    private static int mVideoFormatIndex = 0;

    /**
     * 初始化系统摄像头，
     */
    private SystemCamera() {
        LogMgr.d("初始化SystemCamera");
        mCamera = Camera.open();
        if (mCameraThread == null) {
            mCameraThread = new HandlerThread("cameraThread");
            mCameraThread.start();
        }
        if (mCameraHandler == null) {
            mCameraHandler = new Handler(mCameraThread.getLooper());
        }
        setCamera();
    }

    public static IRobotCamera getSystemCameraInstance() {
        return instance;
    }

    public static IRobotCamera create() {
        if (instance == null) {
            synchronized (SystemCamera.class) {
                if (instance == null) {
                    instance = new SystemCamera();
                }
            }
        }
        return instance;
    }

    public synchronized void takePicture(Context context, String imagePath, CameraStateCallBack cameraStateCallBack) {
        mTackePictureCallBack = cameraStateCallBack;
        LogMgr.d("opening camera callback");
        if (mTackePictureCallBack != null) {
            mTackePictureCallBack.onState(RobotCameraStateCode.OPENING_CAMERA);
        }
        mImgPath = imagePath;
        LogMgr.d("takePicture::拍照前开启预览");
        mCamera.startPreview();
        LogMgr.d("takePicture::");
        if (mCamera != null) {
            mCamera.takePicture(null, null, mPicturePreviewCallback);
        } else {
            LogMgr.e("takePicture:: mCamera == null");
        }
    }

    /**
     * 设置数据回调，打开系统Camera
     *
     * @param context
     * @param previewCallback
     */
    public synchronized void preview(Context context, PreviewCallback previewCallback) {
        LogMgr.d("preview===>");
        if (mCamera != null) {
            mCamera.startPreview();
        }
        mPreviewCallback = previewCallback;
    }

    @Override
    public String getPicturePath() {
        String path = FileUtils.getFilePath(FileUtils.DIR_ABILIX_PHOTO, "1", FileUtils._TYPE_JPEG);
        FileUtils.buildDirectory(path);
        return path;
    }

    @Override
    public synchronized void destory() {
        LogMgr.e("destory() 释放资源");
        mTackePictureCallBack = null;
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (instance != null) {
            instance = null;
        }
        if (mCameraHandler != null) {
            mCameraHandler = null;
        }
        if (mCameraThread != null) {
            mCameraThread.quitSafely();
            mCameraThread = null;
        }
    }

    @Override
    public void cancelTakePicCallback() {
        mTackePictureCallBack = null;
    }


    public static int getWidth() {
        //  LogMgr.d("mVideoWidth:"+mVideoWidth);
        return mVideoWidth;
    }

    public static int getHeight() {
        //  LogMgr.d("mVideoHeight:"+mVideoHeight);
        return mVideoHeight;
    }

    public static int getFormat() {
        //  LogMgr.d("mVideoFormatIndex:"+mVideoFormatIndex);
        return mVideoFormatIndex;
    }

    /**
     * 配置系统摄像头，开启预览
     */
    private void setCamera() {
        LogMgr.d("setCamera()");
        try {
            mCamera.setPreviewCallback(mCameraPreviewCallback);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mVideoWidth, mVideoHeight);
            mVideoFormatIndex = parameters.getPreviewFormat();
            mCamera.setParameters(parameters);
            mCamera.setErrorCallback(mCameraErrorCallback);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogMgr.d("setCamera<====");
    }

    /**
     * 系统摄像头数据回调
     */
    private Camera.PreviewCallback mCameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            //LogMgr.d("onPreviewFrame===>-" + bytes.length);
            if (mPreviewCallback != null) {
                mPreviewCallback.onPreviewFrame(bytes);
            }
        }
    };

    private static boolean isRotate = false;

    public void setIsRotate(boolean b) {
        LogMgr.d("M系列机器人，拍照需要旋转90°");
        isRotate = b;
    }

    private Camera.ErrorCallback mCameraErrorCallback = new Camera.ErrorCallback() {
        @Override
        public void onError(int error, Camera camera) {
            LogMgr.e("Camera.ErrorCallback: " + error);
            if (mTackePictureCallBack != null) {
                mTackePictureCallBack.onState(RobotCameraStateCode.TAKE_PICTURE_CONFIGURED_FAILED);
            }
        }
    };

    private Camera.PictureCallback mPicturePreviewCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            if (isRotate) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length);
                Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postRotate(90);
                Bitmap bMapRotate = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
                bitmap = bMapRotate;

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                try {
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bytes = out.toByteArray();
                bMapRotate.recycle();
                bitmap.recycle();
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            LogMgr.d("onImageAvailable,保存照片文件");
            mCameraHandler.post(new ImageSaver(bytes, mImgPath, new PictureFileSaveListener() {

                @Override
                public void onSucess() {
                    LogMgr.d("save img file sucess callback");
                    if (mTackePictureCallBack != null) {
                        mTackePictureCallBack.onState(RobotCameraStateCode.SAVE_PICTURE_SUCESS);
                    }
                }

                @Override
                public void onFailed(int stateCode) {
                    if (mTackePictureCallBack != null) {
                        mTackePictureCallBack.onState(stateCode);
                    }
                }
            }));
        }
    };
}
