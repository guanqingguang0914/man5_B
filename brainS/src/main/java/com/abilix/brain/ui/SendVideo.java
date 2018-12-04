package com.abilix.brain.ui;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

import com.abilix.brain.GlobalConfig;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 视屏数据发送功能类，未被使用。
 */
public class SendVideo implements SurfaceHolder.Callback, Camera.PreviewCallback {
    public static SendVideo sendVideo = null;
    /** 视频刷新间隔 */
    private int VideoPreRate = 1;
    /** 视频质量 */
    private int VideoQuality = 80;
    /** 发送视频宽度比例 */
    private final static float VideoWidthRatio = 1;
    /** 发送视频高度比例 */
    private final static float VideoHeightRatio = 1;
    /** 发送视频宽度 */
    private static int VideoWidth = 320;
    /** 发送视频高度 */
    private static int VideoHeight = 240;
    /** 视频格式索引 */
    private int VideoFormatIndex = 0;

    private static SurfaceView mSurfaceview = null; // SurfaceView对象：(视图组件)视频显示
    private static SurfaceHolder mSurfaceHolder = null; // /SurfaceHolder对象：(抽象接口)SurfaceView支持类
    private Camera mCamera = null; // Camera对象，相机预览
    public static boolean cameraShow = true;
    public static boolean ifStartCamera = false;//

    public static SendVideo GetManger(SurfaceView surfaceView0, InetAddress inetAddress0) {
        mSurfaceview = surfaceView0;
        VideoBuffer.mInetAddress = inetAddress0;
        // 单例
        if (sendVideo == null) {
            sendVideo = new SendVideo();
        }
        return sendVideo;
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        if (ifStartCamera) {
            return;
        }
        // else ifStartCamera = true;
        mSurfaceHolder = mSurfaceview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
        mSurfaceHolder.addCallback(this); // SurfaceHolder加入回调接口
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 设置显示器类型，setType必须设置
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mCamera = Camera.open();
                    /** 初始化摄像头 */
                    // 后置
                    // mCamera = openFrontFacingCameraGingerbread(); //前置
                    createdcamera();
                    changedcamera();
                    ifStartCamera = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (ifStartCamera) {
                    VideoBuffer.cameraRefresh = true;
                    new Thread(new SendCameraRunnable()).start();
                } else {
                    stopPreview();
                }
            }
        }).start();
    }

    public void stopPreview() {
        VideoBuffer.cameraRefresh = false;
        if (!ifStartCamera) {
            return;
        }
        try {
            destroyedcamera();
            ifStartCamera = false;
            sendVideo = null;
            mSurfaceHolder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createdcamera() {
        try {
            if (cameraShow && mCamera != null) {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                switch (GlobalConfig.BRAIN_TYPE) {
                    case 0x02:// M
                        mCamera.setDisplayOrientation(90); // 设置横行显示
                        break;
                    case 0x03:// H
                        // mCamera.setDisplayOrientation(0); // 设置横行显示
                        break;
                    default:
                        break;
                }
                mCamera.setPreviewCallback(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changedcamera() {
        if (mCamera == null) {
            return;
        }
        mCamera.stopPreview();
        mCamera.setPreviewCallback(this);
        // 获取摄像头参数
        Camera.Parameters parameters = mCamera.getParameters();
        Size size = parameters.getPreviewSize();
        VideoWidth = size.width;
        VideoHeight = size.height;
        VideoFormatIndex = parameters.getPreviewFormat();
        mCamera.startPreview();
    }

    public void destroyedcamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        createdcamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        changedcamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        destroyedcamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        try {
            if (VideoBuffer.cameraRefresh && data != null) {
                YuvImage image = new YuvImage(data, VideoFormatIndex, VideoWidth, VideoHeight, null);
                if (image != null) {
                    if (VideoBuffer.lockData.tryLock()) {
                        try {
                            VideoBuffer.outstream1 = new ByteArrayOutputStream();
                            // 在此设置图片的尺寸和质量 //
                            // 调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
                            image.compressToJpeg(
                                    new Rect(0, 0, (int) (VideoWidthRatio * VideoWidth),
                                            (int) (VideoHeightRatio * VideoHeight)),
                                    VideoQuality, VideoBuffer.outstream1);
                            VideoBuffer.outstream1.flush();
                        } finally {
                            VideoBuffer.lockData.unlock();
                        }
                    } else if (VideoBuffer.lockData.tryLock()) {
                        try {
                            VideoBuffer.outstream2 = new ByteArrayOutputStream();
                            // 在此设置图片的尺寸和质量 //
                            // 调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
                            image.compressToJpeg(
                                    new Rect(0, 0, (int) (VideoWidthRatio * VideoWidth),
                                            (int) (VideoHeightRatio * VideoHeight)),
                                    VideoQuality, VideoBuffer.outstream2);
                            VideoBuffer.outstream2.flush();
                        } finally {
                            VideoBuffer.lockData.unlock();
                        }
                    } else {
                        VideoBuffer.lockData.lock();
                        VideoBuffer.outstream1 = new ByteArrayOutputStream();
                        // 在此设置图片的尺寸和质量 //
                        // 调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
                        image.compressToJpeg(new Rect(0, 0, (int) (VideoWidthRatio * VideoWidth),
                                (int) (VideoHeightRatio * VideoHeight)), VideoQuality, VideoBuffer.outstream1);
                        VideoBuffer.outstream1.flush();
                        VideoBuffer.lockData.unlock();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }

        return cam;
    }
}
