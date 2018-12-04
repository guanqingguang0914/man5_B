package com.abilix.vision.utils;

import android.content.Context;
import android.util.Log;

import com.abilix.brain.utils.FileUtils;
import com.abilix.explainer.ExplainMessage;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.helper.MExplainerHelper;
import com.abilix.explainer.utils.PlayerUtils;
import com.abilix.vision.doMain.IdentificationType;
import com.abilix.vision.doMain.VisionListener;
import com.abilix.vision.view.CameraCollectionDataView;

import java.io.File;

/**
 * Created by LCT
 * Time:2018/9/28 14:34.
 * Annotation： 视觉跟踪工具类
 */
public class VisionUtils {
    private String[] colored_arrow_class_name = {"blue.mp3", "left.mp3", "right.mp3", "up.mp3",
            "red.mp3", "left.mp3", "right.mp3", "up.mp3",
            "yellow.mp3", "left.mp3", "right.mp3", "up.mp3"};
    private String[] color_block_arrow = {"red.mp3", "red.mp3", "red.mp3", "yellow.mp3", "blue.mp3", "blue.mp3"};
    private static final String TAG = "VisionUtils";
    private static VisionUtils visionUtils;
    VisionListener visionListener;
    private IdentificationType visionType;
    /**
     * 是否打开摄像头
     */
    private boolean IsOpenCamera;
    MExplainerHelper explainerHelper;
    /**
     * 是否正在识别
     */
    boolean isVision=false;
    public static VisionUtils getInstance() {
        if (visionUtils == null) {
            visionUtils = new VisionUtils();
        }
        return visionUtils;
    }

    private VisionUtils() {
        isVision=false;
        explainerHelper = MExplainerHelper.getInstance();
    }

    public void stopVision() {
        Log.i(TAG, "stopVision: 开始关闭");
        try {
            setOpenCamera(false);
            BlockTrackUtils.stopTrack();
            visionListener = null;
            CameraCollectionDataView.getInstance().removeView();
            isVision=false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "stopVision: 关闭失败");
        }
    }

    public IdentificationType getVisionType() {
        return visionType;
    }

    public void setVisionType(IdentificationType visionType) {
        this.visionType = visionType;
    }

    public boolean isOpenCamera() {
        return IsOpenCamera;
    }

    public void setOpenCamera(boolean openCamera) {
        IsOpenCamera = openCamera;
    }

    /**
     * 更新获取识别类型
     *
     * @param visionType
     */
    public void updateVisionType(IdentificationType visionType) {
        if (isOpenCamera()) {
            this.visionType = visionType;
        } else {
            Log.i(TAG, "updateVisionType: 更新获取识别类型 失败，请先开启摄像头");
        }

    }

    public void startVision(Context context, IdentificationType visionType) {
        if (visionType == null) {
            VisionUtils.getInstance().setVisionType(IdentificationType.ARROW_COLOR_DIRECTION);
        } else {
            VisionUtils.getInstance().setVisionType(visionType);
        }
        if (!isOpenCamera()) {
            setOpenCamera(true);
            CameraCollectionDataView.getInstance().showView();
        }

//        Intent intent = new Intent(context, VisionActivity.class);
//        context.startActivity(intent);
    }

    public VisionListener getVisionListener() {
        if (visionListener == null) {
            visionListener = new MyVisionListener();
        }
        return visionListener;
    }

    class MyVisionListener implements VisionListener {

        @Override
        public void Identification(int data) {
            Log.i(TAG, "除跟踪外数据" + data + "---->" + getVisionType().name());
            if (data==-1) {
                Log.i(TAG, "未识别到有效数据" + data);
                return;
            }
           // BlockTrackUtils.startTrack(dataS);
            String name = null;
            if (getVisionType().equals(IdentificationType.COLOR_BLOCK)) {
                Log.i("TAG", "色块：" + data + "");
                name = color_block_arrow[data];
            } else if (getVisionType().equals(IdentificationType.ARROW_COLOR_DIRECTION)) {
                if (data >= 0) {
                    Log.i("TAG", "颜色箭头：" + colored_arrow_class_name[data]);
                    name = colored_arrow_class_name[data];
                } else {
                    Log.i("TAG", "颜色箭头：" + "Has no colored arrow detected!");
                }
            } else if (getVisionType().equals(IdentificationType.DIGITAL)) {
                name=null;
            }
            if (name != null && !isVision) {
                isVision=true;
                explainerHelper.playSound(FileUtils.VISION_RES_FILE + File.separator, name, new PlayerUtils.OnCompletionListener() {
                    @Override
                    public void onCompletion(int state) {
                        if (state == PlayerUtils.PLAY_STATE_COMPLETED) {

                        } else {
                            Log.i("TAG", "播放异常");
                        }
                        ExplainMessage resumeMessage = new ExplainMessage();
                        resumeMessage.setFuciton(ExplainMessage.EXPLAIN_RESUME);
                        ExplainTracker.getInstance().doExplainCmd(resumeMessage, null);
                        VisionControl.stopVision();
                    }
                });
            } else {
                //isVision=false;
                Log.i("TAG", "播放异常未找到资源");
            }
        }

        @Override
        public void IdentificationRectDetect(int[] data) {
            Log.i(TAG, "开始视觉跟踪" + data.length + "" + isOpenCamera());
            if (isOpenCamera()) {
                BlockTrackUtils.startTrack(data);
            }

        }
    }
}
