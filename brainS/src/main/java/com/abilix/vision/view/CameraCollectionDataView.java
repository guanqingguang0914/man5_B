package com.abilix.vision.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.abilix.brain.Application;
import com.abilix.brain.utils.FileUtils;
import com.abilix.explainer.ExplainMessage;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.camera.systemcamera.SystemCamera;
import com.abilix.vision.doMain.IdentificationType;
import com.abilix.vision.doMain.VisionListener;
import com.abilix.vision.utils.VisionUtils;
import com.abilix.vision.wervisionlib.WERVisionLib;

import java.io.IOException;

/**
 * Created by LCT
 * Time:2018/11/6 9:35.
 * Annotation：
 */
public class CameraCollectionDataView {
    private String[] colored_arrow_class_name = {"blue down", "blue left", "blue right", "blue up",
            "red down", "red left", "red right", "red up",
            "yellow down", "yellow left", "yellow right", "yellow up"};
    private static final String TAG = "CameraCollectionDataVie";
    private static CameraCollectionDataView cameraCollectionDataView = null;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams param;
    private Context context;
    private WERVisionLib mWERVisionLib;
    private boolean wer_vision_lib_init_flag = false;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private MySurfaceHolder mYSurfaceHolder;

    private VisionUtils visionUtils;
    VisionListener visionListener;

    public static CameraCollectionDataView getInstance() {
        Log.d(TAG, "getInstance: 开始");
        if (cameraCollectionDataView == null) {
            cameraCollectionDataView = new CameraCollectionDataView();
        }
        Log.d(TAG, "getInstance: 开始 中");
        return cameraCollectionDataView;
    }

    private CameraCollectionDataView() {
        visionUtils = VisionUtils.getInstance();
        visionListener = visionUtils.getVisionListener();
        initLib();
        initView();

    }

    private void initView() {
        Log.d(TAG, "initView: 开始view");
        try {
            context = Application.getInstance();
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            param = new WindowManager.LayoutParams();
            param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;     // 系统提示类型,重要
            param.format = 1;
            param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 不能抢占聚焦点
            param.flags = param.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            param.flags = param.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制

            param.alpha = 1.0f;

            param.gravity = Gravity.RIGHT | Gravity.TOP;   //调整悬浮窗口至左上角
            //以屏幕左上角为原点，设置x、y初始值
            param.x = 0;
            param.y = 0;

            //设置悬浮窗口长宽数据
            param.width = ViewGroup.LayoutParams.MATCH_PARENT;
            param.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mSurfaceView = new SurfaceView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mSurfaceView.setLayoutParams(params);
            mSurfaceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ExplainMessage finishExplain = new ExplainMessage();
                    finishExplain.setFuciton(ExplainMessage.EXPLAIN_STOP);
                    ExplainTracker.getInstance().doExplainCmd(finishExplain, null);
                }
            });
            // mSurfaceView.setBackgroundColor(Color.BLUE);
            Log.d(TAG, "initView: 结束view");
        } catch (Exception e) {
            Log.e(TAG, "初始化view 异常" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initLib() {
        Log.d(TAG, "initLib: 开始库");
        mWERVisionLib = new WERVisionLib();
        wer_vision_lib_init_flag = mWERVisionLib.Init(FileUtils.VISION_LIB_FILE);
        mWERVisionLib.YellowRectSetParameter(0.8f, 0.5f);
        Log.d(TAG, "initLib: 结束库");
    }

    /**
     * 显示view
     */
    public void showView() {
        if (mWindowManager != null) {
            mWindowManager.addView(mSurfaceView, param);
            Log.d(TAG, "showView: 开始显示");
            openSurfaceHolder();
            Log.d(TAG, "showView: 开始结束");
        }
    }

    /**
     * 移除view
     */
    public void removeView() {
        Log.d(TAG, "removeView: 开始removeView");
        try {
            if (mWindowManager != null && mSurfaceView != null) {
//                mCamera.stopPreview();
//                mCamera.release();
//                mCamera = null;
                stopSurfaceHolder();
                mSurfaceView.destroyDrawingCache();
                mWindowManager.removeView(mSurfaceView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开 摄像头
     */
    private void openSurfaceHolder() {
        try {
            SystemCamera.create().destory();
        } catch (Exception e) {
            Log.d(TAG, "openSurfaceHolder: 关闭brain 执行摄像头出错");
            e.printStackTrace();
        }
        try {
            mSurfaceHolder = mSurfaceView.getHolder();
            mYSurfaceHolder = new MySurfaceHolder();
            mSurfaceHolder.addCallback(mYSurfaceHolder);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//            mCamera= SystemCamera.create().getCamera();
//            if (mCamera==null) {
                Log.d(TAG, "openSurfaceHolder: 相机为空自己创建了");
                mCamera = Camera.open();
          //  }
            setCamera();
        } catch (Exception e) {
            visionUtils.setOpenCamera(false);
            e.printStackTrace();
        }
    }
    private void setCamera() {
        try {
            mCamera.setPreviewCallback(mCameraPreviewCallback);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(320, 240);
            parameters.setPreviewFrameRate(60);
            parameters.setPictureSize(320, 240);
            parameters.set("jpeg-quality", 90);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 系统摄像头数据回调
     */
    private Camera.PreviewCallback mCameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            if (wer_vision_lib_init_flag) {
                IdentificationType type = visionUtils.getVisionType();
                if (type.equals(IdentificationType.IMAGE_TRACKING)) {
                    final int[] yellow_rect_info = mWERVisionLib.YellowRectDetect(bytes, 320, 240);
                    Log.i(TAG, "图像跟踪：" + (yellow_rect_info.length == 5 ? (yellow_rect_info[1] + "" + "_" + yellow_rect_info[2] + "") : 1) + "");
                    visionListener.IdentificationRectDetect(yellow_rect_info);
                } else {
                    if (type.equals(IdentificationType.COLOR_BLOCK)) {
                        final int colored_block_index = mWERVisionLib.ColoredBlocksDetect(bytes, 320, 240);
                        Log.i(TAG, "色块：" + colored_block_index + "");
                        visionListener.Identification(colored_block_index);
                    } else if (type.equals(IdentificationType.ARROW_COLOR_DIRECTION)) {
                        final int colored_arrow_index = mWERVisionLib.ColoredArrowsDetect(bytes, 320, 240);
                        if (colored_arrow_index >= 0) {
                            Log.i(TAG, "颜色箭头：" + colored_arrow_class_name[colored_arrow_index]);
                        } else {
                            Log.i(TAG, "颜色箭头：" + "Has no colored arrow detected!");
                        }
                        visionListener.Identification(colored_arrow_index);
                    } else if (type.equals(IdentificationType.DIGITAL)) {
                        final int math_symbol_index = mWERVisionLib.MathSymbolDetect(bytes, 320, 240);
                        Log.i(TAG, "数字：" + math_symbol_index + "");
                        visionListener.Identification(math_symbol_index);
                    }
                }
            } else {
                Log.e(TAG, "Load models failed, please check model path!");
            }
        }
    };

    /**
     * 关闭 摄像头
     */
    private void stopSurfaceHolder() {
        try {
            if (visionUtils != null) {
                visionUtils.setOpenCamera(false);
            }
            mYSurfaceHolder = null;
            mSurfaceHolder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 摄像头
     *
     * @author luox
     */
    private class MySurfaceHolder implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.startPreview();
//                    mCamera.setDisplayOrientation(90);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            if (mCamera == null) {
                return;
            }
            try {
                mCamera.stopPreview();
                // 获取摄像头参数
//                Camera.Parameters parameters = mCamera.getParameters();
//                Camera.Size size = parameters.getPreviewSize();
//                int VideoWidth = size.width;
//                int VideoHeight = size.height;
//                int VideoFormatIndex = parameters.getPreviewFormat();
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                if (mCamera != null) {
                    mCamera.setPreviewCallback(null);
                    if (mSurfaceHolder!=null) {
                        mSurfaceHolder.removeCallback(this);
                    }
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

