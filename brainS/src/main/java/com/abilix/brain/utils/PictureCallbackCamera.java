package com.abilix.brain.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.util.Log;

/**
 * 拍照 未被使用。
 *
 * @author luox
 */
public class PictureCallbackCamera {
    private Camera mCamera = null;
    private int mode;
    volatile private boolean nool = true;
    volatile private Bitmap bitmap = null;
    volatile private Bitmap bMapRotate = null;
    private String mImageFile;

    @SuppressWarnings("deprecation")
    private PictureCallback mCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            new Thread() {
                public void run() {
                    try {
                        bitmap = BitmapFactory.decodeByteArray(data, 0,
                                data.length);
                        if (mode == 2) {
                            Matrix matrix = new Matrix();
                            matrix.reset();
                            matrix.postRotate(90);
                            bMapRotate = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(),
                                    matrix, true);
                            bitmap = bMapRotate;
                        }
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        byte[] result = out.toByteArray();
                        if (mImageFile != null) {
                            File file = new File(mImageFile);
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            FileOutputStream fos = null;
                            if (nool) {
                                fos = new FileOutputStream(file);
                                fos.write(result);
                            }
                            out.close();
                            if (fos != null) {
                                fos.close();
                            }
                            fos = null;
                            out = null;
                            file = null;
                            if (mCameraSucceed != null && nool) {
                                mCameraSucceed.CamSucceed(mImageFile);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (bitmap != null) {
                                bitmap.recycle();
                                bitmap = null;
                            }
                            if (bMapRotate != null) {
                                bMapRotate.recycle();
                                bMapRotate = null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }.start();

        }
    };

    class MyThread extends Thread {
        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            try {
                mCamera = Camera.open();
                if (mCamera != null) {
                    Parameters params = mCamera.getParameters();
                    params.setFlashMode(Parameters.FLASH_MODE_AUTO);//
                    // 自动闪光灯
                    params.setJpegQuality(100);
                    mCamera.setParameters(params);
                    mCamera.startPreview();
                    mCamera.takePicture(null, null, mCallback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public PictureCallbackCamera(int mode, String mImageFile) {
        if (mCamera == null) {
            nool = true;
            this.mode = mode;
            this.mImageFile = mImageFile;
            new MyThread().start();
        }
    }

    @SuppressWarnings("deprecation")
    public void destroy() {
        nool = false;
        if (mCamera != null) {
            LogMgr.w("拍照结束");
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
    }

    private CameraSucceed mCameraSucceed;

    public void setmCameraSucceed(CameraSucceed mCameraSucceed) {
        this.mCameraSucceed = mCameraSucceed;
    }

    public interface CameraSucceed {
        public void CamSucceed(String name);
    }
}
