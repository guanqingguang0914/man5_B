package com.abilix.explainer.helper.Interface;

/**
 * 接口定义了H机器人所具备的功能
 * Created by jingh on 2017/8/24.
 */

public interface IHRobot extends IRobot {
    //控制灯
    void setLed(int type, byte[] rgb, int mode);

    //设置头部灯
    void setHeadLed(byte[] rgb);

    //设置手脚灯光
    void setHandsFeetLed(int type, int mode);

    //关Led灯
    void turnoffLed();

    //获取超声传感器的值
    int getUtrasonic();

    //舵机运行
    void runServo(int id, int speed, int angle);

    //舵机上电
    void startServo(int id);

    //舵机释放
    void stopServo(int id);

    //归零
    void zeroServo(int id);

    //读取舵机角度
    String getServoAngleString(int id);

    //读取舵机角度
    int[] getServoAngles();

    //判断前方是否有障碍物
    int findBar();

    //读取前方障碍物距离
    int findBarDistance();

    //机器人动作
    void move(int action);

    //自定义机器人动作
    void customMove(String name);

    //机器人停止动作
    void stopMove();

    //读取当前机器人姿势
    int position(int drc);

    //步态运动
    void gaitMotion(int action, int speed);

    //头部触摸
    int getHeadTouch();
}
