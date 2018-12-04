package com.abilix.vision.doMain;

/**
 * Created by LCT
 * Time:2018/9/28 16:00.
 * Annotation：识别监听
 */
public interface VisionListener {
    /**
     * 识别结果
     * @param data
     * @return
     */
    public void Identification(int data);

    /**
     * 视觉跟踪返回结果
     * @param data
     * @return
     */
    public void IdentificationRectDetect(int[] data);
}
