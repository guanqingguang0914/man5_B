package com.abilix.explainer;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.utils.Utils;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.ProtocolUtils;

import java.io.File;

/**
 * @author jingh
 * @Descripton:该类负责Control的初始化，包括读取机器人主类型、平台类型、Log日志、传感器，初始化串口，读取 机器人子类型
 * @date2017-3-28下午5:02:31
 */

public class ExplainerInitiator {
    public static int MAIN_ROBOT_TYPE_BRAIN = 0x00;
    public static int CHILD_ROBOT_TYPE = 0x00;
    /* 系统版本号 */
    private final static String ROBOT_BUILD_TYPE = Build.DISPLAY;
    private final static String ROBOT_HARDWARE_TYPE = Build.HARDWARE;

    /* 硬件平台名称 */
    public static final String HARDWARE_TYPE_RK = "rk30board";
    public static final String HARDWARE_TYPE_MT = "mt6580";
    public static final int HW_TYPE_RK = 0x02;
    public static final int HW_TYPE_MT = 0x01;

    //统一用ROBOT_TYPE
    /* 机器人类型 */
    public static final int ROBOT_TYPE_COMMON = 0x00;
    public static final int ROBOT_TYPE_C = 0x01;
    public static final int ROBOT_TYPE_M = 0x02;
    public static final int ROBOT_TYPE_H = 0x03;
    public static final int ROBOT_TYPE_F = 0x04;
    public static final int ROBOT_TYPE_S = 0x05;
    public static final int ROBOT_TYPE_AF = 0x06;

    public static final int ROBOT_TYPE_M0 = 0x0C;
    public static final int ROBOT_TYPE_M1 = 0x0B;
    public static final int ROBOT_TYPE_M2 = 0x0D;
    public static final int ROBOT_TYPE_M3 = 0x0E;
    public static final int ROBOT_TYPE_M3S = 0x4E;
    public static final int ROBOT_TYPE_M4 = 0x0F;
    public static final int ROBOT_TYPE_M4S = 0x4F;
    public static final int ROBOT_TYPE_M5 = 0x10;
    public static final int ROBOT_TYPE_M6 = 0x12;
    public static final int ROBOT_TYPE_M7 = 0x13;
    public static final int ROBOT_TYPE_M8 = 0x14;
    public static final int ROBOT_TYPE_M9 = 0x15;

    public static final int ROBOT_TYPE_C0 = 0x08;
    public static final int ROBOT_TYPE_C1 = 0x09;
    public static final int ROBOT_TYPE_C1_1 = 0x48;
    public static final int ROBOT_TYPE_C1_2 = 0x49;
    public static final int ROBOT_TYPE_C2 = 0x18;
    public static final int ROBOT_TYPE_C2_1 = 0x4A;
    public static final int ROBOT_TYPE_C2_2 = 0x4B;
    public static final int ROBOT_TYPE_C3 = 0x19;
    public static final int ROBOT_TYPE_C4 = 0x1A;
    public static final int ROBOT_TYPE_C5 = 0x1B;
    public static final int ROBOT_TYPE_C6 = 0x1C;
    public static final int ROBOT_TYPE_C7 = 0x1D;
    public static final int ROBOT_TYPE_C8 = 0x1E;
    public static final int ROBOT_TYPE_C9 = 0x0A;
    public static final int ROBOT_TYPE_CU = 0x50;

    public static final int ROBOT_TYPE_H0 = 0x20;
    public static final int ROBOT_TYPE_H1 = 0x21;
    public static final int ROBOT_TYPE_H2 = 0x22;
    public static final int ROBOT_TYPE_H3 = 0x23;
    public static final int ROBOT_TYPE_H4 = 0x24;
    public static final int ROBOT_TYPE_H5 = 0x25;
    public static final int ROBOT_TYPE_H6 = 0x26;
    public static final int ROBOT_TYPE_H7 = 0x27;
    public static final int ROBOT_TYPE_H8 = 0x28;
    public static final int ROBOT_TYPE_H9 = 0x29;
    public static final int ROBOT_TYPE_SE901 = 0x6C;

    public static final int ROBOT_TYPE_F0 = 0x2A;
    public static final int ROBOT_TYPE_F1 = 0x2B;
    public static final int ROBOT_TYPE_F2 = 0x2C;
    public static final int ROBOT_TYPE_F3 = 0x2D;
    public static final int ROBOT_TYPE_F4 = 0x2E;
    public static final int ROBOT_TYPE_F5 = 0x2F;
    public static final int ROBOT_TYPE_F6 = 0x30;
    public static final int ROBOT_TYPE_F7 = 0x31;
    public static final int ROBOT_TYPE_F8 = 0x32;
    public static final int ROBOT_TYPE_F9 = 0x33;

    public static final int ROBOT_TYPE_S0 = 0x34;
    public static final int ROBOT_TYPE_S1 = 0x35;
    public static final int ROBOT_TYPE_S2 = 0x36;
    public static final int ROBOT_TYPE_S3 = 0x37;
    public static final int ROBOT_TYPE_S4 = 0x38;
    public static final int ROBOT_TYPE_S5 = 0x39;
    public static final int ROBOT_TYPE_S6 = 0x3A;
    public static final int ROBOT_TYPE_S7 = 0x3B;
    public static final int ROBOT_TYPE_S8 = 0x3C;
    public static final int ROBOT_TYPE_S9 = 0x3D;

    public static final int ROBOT_TYPE_AF0 = 0x3E;
    public static final int ROBOT_TYPE_AF1 = 0x3F;
    public static final int ROBOT_TYPE_AF2 = 0x40;
    public static final int ROBOT_TYPE_AF3 = 0x41;
    public static final int ROBOT_TYPE_AF4 = 0x42;
    public static final int ROBOT_TYPE_AF5 = 0x43;
    public static final int ROBOT_TYPE_AF6 = 0x44;
    public static final int ROBOT_TYPE_AF7 = 0x45;
    public static final int ROBOT_TYPE_AF8 = 0x46;
    public static final int ROBOT_TYPE_AF9 = 0x47;

    public static final int ROBOT_TYPE_BRIANC = ROBOT_TYPE_C1;

    private static final int CORE_MTK6580 = 1;
    private static final int CORE_RK3128 = 2;

    private static final int FUNCTION_DEFAULT = 0;
    private static final int FUNCTION_C_201 = 1;
    private static final int FUNCTION_C9 = 2;
    private static final int FUNCTION_H3_H4 = 1;
    private static final int FUNCTION_M3S_M4S = 1;
    private static final int FUNCTION_SE901 = 3;

    static {
        switch (Utils.getProductSerial(ROBOT_BUILD_TYPE)) {
            case "C":
                ControlInfo.setMain_robot_type(ROBOT_TYPE_C);
                if (Utils.getCoreVersionNumber(ROBOT_BUILD_TYPE) == CORE_MTK6580) {
                    //mtk6580平台
                    if (Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_C_201) {
                        //表示目前用于C项目平衡车（201）项目
                        ControlInfo.setChild_robot_type(ROBOT_TYPE_CU);
                    } else if (Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_C9) {
                        //表示目前用于C项目C9项目
                        ControlInfo.setChild_robot_type(ROBOT_TYPE_C9);
                    } else {
                        //表示目前用于C项目C3-C8项目
                    }
                } else if (Utils.getCoreVersionNumber(ROBOT_BUILD_TYPE) == CORE_RK3128) {
                    //rk3128平台
                    ControlInfo.setChild_robot_type(ROBOT_TYPE_C1);
                }
                break;
            case "M":
                ControlInfo.setMain_robot_type(ROBOT_TYPE_M);
                if (Utils.getCoreVersionNumber(ROBOT_BUILD_TYPE) == CORE_MTK6580) {
                    //mtk6580平台
                    if (Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_M3S_M4S) {
                        ControlInfo.setChild_robot_type(ROBOT_TYPE_M3S);
                    }
                    //M3-M6
                } else if (Utils.getCoreVersionNumber(ROBOT_BUILD_TYPE) == CORE_RK3128) {
                    //rk3128平台
                    ControlInfo.setChild_robot_type(ROBOT_TYPE_M1);
                }
                break;
            case "H":
                ControlInfo.setMain_robot_type(ROBOT_TYPE_H);
                if (Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_H3_H4) {
                    ControlInfo.setChild_robot_type(ROBOT_TYPE_H3);
                }else if(Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_SE901){
                    ControlInfo.setChild_robot_type(ROBOT_TYPE_SE901);
                } else {
                    ControlInfo.setChild_robot_type(ROBOT_TYPE_H);
                }
                break;
            case "F":
                ControlInfo.setMain_robot_type(ROBOT_TYPE_F);
                break;
            case "S":
                ControlInfo.setMain_robot_type(ROBOT_TYPE_S);
                break;
            case "AF":
                ControlInfo.setMain_robot_type(ROBOT_TYPE_AF);
                break;
            default:
                break;
        }
    }

    /**
     * Control相关初始化操作都在这里进行,且初始化顺序不能随意改变
     */
    public static void init() {
        initLog();
        initChildRobotType();
    }

    /**
     * 初始化机器人子类型
     */
    private static void initChildRobotType() {
        int robotType = GlobalConfig.BRAIN_CHILD_TYPE; //getChildRobotType();
        LogMgr.e("robotType" + robotType);
        if (robotType > 0) {
            ControlInfo.setChild_robot_type(robotType);
        }
    }

    /**
     * 初始化Log日志
     */
    private static void initLog() {
        //release版本关闭log
        if (appIsDebugable(ExplainerApplication.instance) || DebugModeIsEnabled()) {
            LogMgr.setLogLevel(LogMgr.VERBOSE);
        } else {
            LogMgr.setLogLevel(com.abilix.brain.utils.LogMgr.LOG_LEVEL);
        }
        //release版本查看Log
        //LogMgr.startExportLog();
    }

    /**
     * 获取机器人子类型
     */
    private static int getChildRobotType() {
        byte[] byte_readRobotType = ProtocolUtils.sendProtocol((byte) ROBOT_TYPE_COMMON, (byte) 0x11, (byte) 0x09, null);
        LogMgr.d("robot type bytes:" + ByteUtils.bytesToString(byte_readRobotType, byte_readRobotType.length));
        if (byte_readRobotType[5] == (byte) 0xF0 && byte_readRobotType[6] == (byte) 0x07) {
            int type = byte_readRobotType[11];
            if (byte_readRobotType[11] == (byte) 0xff) {
                LogMgr.e("未设置机器人类型");
                return -1;
            }
            LogMgr.d("机器人类型：" + type);
            return type;
        }
        LogMgr.e("获取机器人类型失败");
        return -1;
    }

    /**
     * 获取app是否是DEBUG版本
     */
    public static boolean appIsDebugable(Application application) {
        try {
            ApplicationInfo info = application.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检测DEBUG模式是否打开
     * release版本打开调试模式方法：adb shell touch /mnt/shell/emulated/0/Abilix/RobotInfo/.debug
     */
    public static boolean DebugModeIsEnabled() {
        File debug = FileUtils.getFile(FileUtils.DIR_ABILIX_ROBOT_INFO, ".debug");
        return debug.exists();
    }

}
