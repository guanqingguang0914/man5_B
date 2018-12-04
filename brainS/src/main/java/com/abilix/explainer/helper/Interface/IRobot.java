package com.abilix.explainer.helper.Interface;

import com.abilix.explainer.utils.PlayerUtils;

/**
 * 定义所有系列机器人通用接口
 * Created by jingh on 2017/8/24.
 */

public interface IRobot {

    int getU16(byte[] b, int index);

    String getString(byte[] buffer, int index);

    boolean readFromFlash(byte[] src, int srcPos, byte[] dst, int length);

    float getParam(byte[] buffer, float[] val, int index);

    float getMvalue(float[] val, int index);

    int getMotoParam(byte[] buffer, float[] val, int index);

    void getName(byte[] name, byte[] Data);

    void setCompassCheck(boolean compassCheck);

    float my_Calc(float a, float b, int ch);

    int getRandom(int a, int b);

    float getCompass();

    float getGyro(float pam1, float pam2);

    void playMusic(int type, int index);

    void playSound(int type, int index);

    void playSound(String name);

    void playSound(String name, PlayerUtils.OnCompletionListener listener);

    void playSound(int soundType, String name, PlayerUtils.OnCompletionListener listener);

    void stopPlaySound();

    void playSoundRandom();

    void playeretMusic(int mode, int progress);

}
