package com.abilix.explainer.camera;

import android.content.Context;

/**
 * Created by jingh on 2017/6/8.
 */

public interface IRobotCamera {

    void takePicture(Context context, String imagePath, final CameraStateCallBack cameraStateCallBack);

    String getPicturePath();

    void setIsRotate(boolean b);

    void destory();

    void cancelTakePicCallback();

}