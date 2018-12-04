package com.abilix.explainer.camera.usbcamera;

import android.content.Context;

import com.abilix.explainer.camera.CameraStateCallBack;
import com.abilix.explainer.camera.IRobotCamera;
import com.abilix.usbcamera.client.PreviewCallback;

/**
 * Created by jingh on 2017/6/9.
 */

public interface IUsbCamera extends IRobotCamera{
   void  registeSystemUSBCamera(Context context);

    void setBrightnessS(final Context context,final int cmd, final CameraStateCallBack cameraStateCallBack);

    /**
     *
     * @param context
     * @param previewCallback 预览回调
     * @param cameraStateCallBack 相机状态回调
     */
    void preview(final Context context, final PreviewCallback previewCallback, final CameraStateCallBack cameraStateCallBack);

    void stopPreview();
}
