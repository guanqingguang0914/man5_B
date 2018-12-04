package com.abilix.explainer;

import com.abilix.explainer.utils.LogMgr;

public class ControlInfo {
    private static int main_robot_type = 0x00;
    private static int child_robot_type = 0x00;
    private static int platform_type = 0x00;
    private static int main_serialport_type = 0x00;
    private static int motor_serialport_type = 0x00;

    public static int getMain_robot_type() {
        return main_robot_type;
    }

    public static void setMain_robot_type(int main_robot_type) {
        LogMgr.d("main_robot_type:" + main_robot_type);
        ControlInfo.main_robot_type = main_robot_type;
    }

    public static int getChild_robot_type() {
        if (child_robot_type < 1) {
            return main_robot_type;
        }
        return child_robot_type;
    }

    public static void setChild_robot_type(int child_robot_type) {
        LogMgr.d("child_robot_type:" + child_robot_type);
        ControlInfo.child_robot_type = child_robot_type;
    }

    public static int getPlatform_type() {
        return platform_type;
    }

    public static void setPlatform_type(int platform_type) {
        LogMgr.d("platform_type:" + platform_type);
        ControlInfo.platform_type = platform_type;
    }

    public static int getMain_serialport_type() {
        return main_serialport_type;
    }

    public static void setMain_serialport_type(int main_serialport_type) {
        LogMgr.d("main_serialport_type:" + main_serialport_type);
        ControlInfo.main_serialport_type = main_serialport_type;
    }

    public static int getMotor_serialport_type() {
        return motor_serialport_type;
    }

    public static void setMotor_serialport_type(int motor_serialport_type) {
        LogMgr.d("motor_serialport_type:" + motor_serialport_type);
        ControlInfo.motor_serialport_type = motor_serialport_type;
    }
}
