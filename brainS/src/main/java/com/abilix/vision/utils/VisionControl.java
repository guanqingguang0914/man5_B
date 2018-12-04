package com.abilix.vision.utils;

import android.content.Context;

import com.abilix.vision.doMain.IdentificationType;

/**
 * Created by LCT
 * Time:2018/9/29 11:36.
 * Annotation：视觉识别控制类
 */
public class VisionControl {
    /**
     * 开始识别
     *
     * @param visionType 识别类型 传null 默认是 /**
     * 箭头 颜色方向识别
     * ARROW_COLOR_DIRECTION,
     */
    public static void startVision(Context context, IdentificationType visionType) {
       VisionUtils.getInstance().startVision(context,visionType);
    }

    /**
     * 更新识别类型
     * @param visionType 识别类型
     */
    public static void updateVisionType(IdentificationType visionType){
        VisionUtils.getInstance().updateVisionType(visionType);
    }

    /**
     * 关闭识别
     */
    public static void stopVision(){
        VisionUtils.getInstance().stopVision();
    }
}
