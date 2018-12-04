package com.abilix.brain.utils;

import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;

/**
 * 协议工具类。
 */
public class ProtocolUtil {

    private static final byte MULTI_MEDIA_AUDIO = (byte) 0x01;
    private static final byte MULTI_MEDIA_VEDIO = (byte) 0x02;
    private static final byte MULTI_MEDIA_PIC = (byte) 0x03;

    /**
     * 是否认识机器人进入录音界面命令
     *
     * @param data
     */
    public static boolean isKnowRobotRecordEnterWindowCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 12 && data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == GlobalConfig.KNOW_ROBOT_RECORD_IN_CMD_2 && data[11] == (byte) 0x00) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否认识机器人开始录音命令
     *
     * @param data
     */
    public static boolean isKnowRobotRecordBeginCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 12 && data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == GlobalConfig.KNOW_ROBOT_RECORD_IN_CMD_2 && data[11] == (byte) 0x01) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否认识机器人播放录音命令
     *
     * @param data
     */
    public static boolean isKnowRobotRecordPlayCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 12 && data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == GlobalConfig.KNOW_ROBOT_RECORD_IN_CMD_2 && data[11] == (byte) 0x02) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否认识机器人停止录音命令
     *
     * @param data
     */
    public static boolean isKnowRobotRecordStopCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 12 && data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == GlobalConfig.KNOW_ROBOT_RECORD_IN_CMD_2 && data[11] == (byte) 0x04) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否退出认识机器人划线界面
     *
     * @param data
     */
    public static boolean isKnowRobotExitDrawLineCmd(byte[] data) {
        if (data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == (byte) 0x06 && data[11] == (byte) 0x02) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 是否认识机器人退出录音界面命令
     * @param data
     */
    public static boolean isKnowRobotRecordExitWindowCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 12 && data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == GlobalConfig.KNOW_ROBOT_RECORD_IN_CMD_2 && data[11] == (byte) 0x03) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 是否认识机器人关闭摄像头命令
     * @param data
     */
    public static boolean isKnowRobotCloseCameraCmd(byte[] data){
        if (data == null) {
            return false;
        }

        if (data.length > 12 && data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == GlobalConfig.KNOW_ROBOT_RTSP_VIDEO_IN_CMD_2 && data[11] == (byte) 0x00) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否认识机器人打开摄像头命令
     * @param data
     */
    public static boolean isKnowRobotOpenCameraCmd(byte[] data){
        if (data == null) {
            return false;
        }

        if (data.length > 12 && data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == GlobalConfig.KNOW_ROBOT_RTSP_VIDEO_IN_CMD_2 && data[11] == (byte) 0x01) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否认识机器人获取mic音量命令
     * @param data
     */
    public static boolean isKnowRobotGetMicVolCmd(byte[] data){
        if (data == null) {
            return false;
        }

        if (data.length > 11 && data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == GlobalConfig.KNOW_ROBOT_MIC_VOL_IN_CMD_2) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 是否扩展多媒体命令
     *
     * @param data
     */
    public static boolean isMultiMediaCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 6 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1 && data[5] == GlobalConfig.MULTI_MEDIA_IN_CMD_1 &&
                (data[6] == GlobalConfig.MULTI_MEDIA_IN_CMD_2_PLAY || data[6] == GlobalConfig.MULTI_MEDIA_IN_CMD_2_PAUSE ||
                        data[6] == GlobalConfig.MULTI_MEDIA_IN_CMD_2_STOP || data[6] == GlobalConfig.MULTI_MEDIA_IN_CMD_2_RESUME)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否多媒体音频播放命令
     *
     * @param data
     */
    public static boolean isMultiMediaAudioPlayCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 18 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1 && data[5] == GlobalConfig.MULTI_MEDIA_IN_CMD_1 &&
                data[6] == GlobalConfig.MULTI_MEDIA_IN_CMD_2_PLAY && data[12] == MULTI_MEDIA_AUDIO) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否多媒体暂停命令
     *
     * @param data
     */
    public static boolean isMultiMediaPauseCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 6 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1 && data[5] == GlobalConfig.MULTI_MEDIA_IN_CMD_1 &&
                data[6] == GlobalConfig.MULTI_MEDIA_IN_CMD_2_PAUSE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否多媒体停止命令
     *
     * @param data
     */
    public static boolean isMultiMediaStopCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 6 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1 && data[5] == GlobalConfig.MULTI_MEDIA_IN_CMD_1 &&
                data[6] == GlobalConfig.MULTI_MEDIA_IN_CMD_2_STOP) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否多媒体继续播放命令
     *
     * @param data
     */
    public static boolean isMultiMediaResumeCmd(byte[] data) {
        if (data == null) {
            return false;
        }

        if (data.length > 6 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1 && data[5] == GlobalConfig.MULTI_MEDIA_IN_CMD_1 &&
                data[6] == GlobalConfig.MULTI_MEDIA_IN_CMD_2_RESUME) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param receive_type 接收到的类型
     */
    public static boolean isTypeRelate(byte receive_type) {
        LogMgr.i("isTypeRelate() 收到的类型 = " + receive_type + " 本机的主类型 = " + GlobalConfig.BRAIN_TYPE + " 本机的子类型 = " + GlobalConfig.BRAIN_CHILD_TYPE);
        boolean result = false;
        if (receive_type == (byte) GlobalConfig.ROBOT_TYPE_COMMON) {
            result = true;
        } else if (receive_type == (byte) GlobalConfig.BRAIN_TYPE || receive_type == (byte) GlobalConfig.BRAIN_CHILD_TYPE) {
            result = true;
        } else if ((receive_type == GlobalConfig.ROBOT_TYPE_C || isChildTypeOfC(receive_type)) && isMainTypeOfC(GlobalConfig.BRAIN_TYPE)) {
            result = true;
        } else if ((receive_type == GlobalConfig.ROBOT_TYPE_M || isChildTypeOfM(receive_type)) && isMainTypeOfM(GlobalConfig.BRAIN_TYPE)) {
            result = true;
        } else if ((receive_type == GlobalConfig.ROBOT_TYPE_H || isChildTypeOfH(receive_type)) && isMainTypeOfH(GlobalConfig.BRAIN_TYPE)) {
            result = true;
        } else if ((receive_type == GlobalConfig.ROBOT_TYPE_F || isChildTypeOfF(receive_type)) && isMainTypeOfF(GlobalConfig.BRAIN_TYPE)) {
            result = true;
        } else if ((receive_type == GlobalConfig.ROBOT_TYPE_S || isChildTypeOfS(receive_type)) && isMainTypeOfS(GlobalConfig.BRAIN_TYPE)) {
            result = true;
        } else if ((receive_type == GlobalConfig.ROBOT_TYPE_AF || isChildTypeOfAF(receive_type)) && isMainTypeOfAF(GlobalConfig.BRAIN_TYPE)) {
            result = true;
        } else if ((receive_type == GlobalConfig.ROBOT_TYPE_U || isChildTypeOfU(receive_type)) && isMainTypeOfU(GlobalConfig.BRAIN_TYPE)) {
            result = true;
        }
        LogMgr.i("isTypeRelate() 本次类型匹配的结果 = " + result);
        return result;
    }

    /**
     * 是C的主类型之一
     *
     * @param brainType
     */
    public static boolean isMainTypeOfC(int brainType) {
        LogMgr.d("isMainTypeOfC() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_C1 || brainType == GlobalConfig.ROBOT_TYPE_C
                || brainType == GlobalConfig.ROBOT_TYPE_C9 || brainType == GlobalConfig.ROBOT_TYPE_CU);
    }

    /**
     * 是C的子类型之一
     *
     * @param brainType
     */
    public static boolean isChildTypeOfC(int brainType) {
        LogMgr.d("isChildTypeOfC() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_C1 || brainType == GlobalConfig.ROBOT_TYPE_C2 || brainType == GlobalConfig.ROBOT_TYPE_C3 ||
                brainType == GlobalConfig.ROBOT_TYPE_C4 || brainType == GlobalConfig.ROBOT_TYPE_C5 || brainType == GlobalConfig.ROBOT_TYPE_C6 ||
                brainType == GlobalConfig.ROBOT_TYPE_C7 || brainType == GlobalConfig.ROBOT_TYPE_C8 || brainType == GlobalConfig.ROBOT_TYPE_C9 ||
                brainType == GlobalConfig.ROBOT_TYPE_C1_1 || brainType == GlobalConfig.ROBOT_TYPE_C1_2 ||
                brainType == GlobalConfig.ROBOT_TYPE_C2_1 || brainType == GlobalConfig.ROBOT_TYPE_C2_2 || brainType == GlobalConfig.ROBOT_TYPE_CU);
    }

    /**
     * 是M的主类型之一
     *
     * @param brainType
     */
    public static boolean isMainTypeOfM(int brainType) {
        LogMgr.d("isMainTypeOfM() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_M1 || brainType == GlobalConfig.ROBOT_TYPE_M || brainType == GlobalConfig.ROBOT_TYPE_M7);
    }

    /**
     * 是M的子类型之一
     *
     * @param brainType
     */
    public static boolean isChildTypeOfM(int brainType) {
        LogMgr.d("isChildTypeOfM() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_M1 || brainType == GlobalConfig.ROBOT_TYPE_M2 || brainType == GlobalConfig.ROBOT_TYPE_M3 ||
                brainType == GlobalConfig.ROBOT_TYPE_M4 || brainType == GlobalConfig.ROBOT_TYPE_M5 || brainType == GlobalConfig.ROBOT_TYPE_M6 ||
                brainType == GlobalConfig.ROBOT_TYPE_M7 || brainType == GlobalConfig.ROBOT_TYPE_M8 || brainType == GlobalConfig.ROBOT_TYPE_M9 ||
                brainType == GlobalConfig.ROBOT_TYPE_M3S || brainType == GlobalConfig.ROBOT_TYPE_M4S);
    }

    /**
     * 是H的主类型之一
     *
     * @param brainType
     */
    public static boolean isMainTypeOfH(int brainType) {
        LogMgr.d("isMainTypeOfH() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_H || brainType == GlobalConfig.ROBOT_TYPE_H3);
    }

    /**
     * 是H的子类型之一
     *
     * @param brainType
     */
    public static boolean isChildTypeOfH(int brainType) {
        LogMgr.d("isChildTypeOfH() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_H1 || brainType == GlobalConfig.ROBOT_TYPE_H2 || brainType == GlobalConfig.ROBOT_TYPE_H3 ||
                brainType == GlobalConfig.ROBOT_TYPE_H4 || brainType == GlobalConfig.ROBOT_TYPE_H5 || brainType == GlobalConfig.ROBOT_TYPE_H6 ||
                brainType == GlobalConfig.ROBOT_TYPE_H7 || brainType == GlobalConfig.ROBOT_TYPE_H8 || brainType == GlobalConfig.ROBOT_TYPE_H9
                || brainType == GlobalConfig.ROBOT_TYPE_SE901);
    }

    /**
     * 是F的主类型之一
     *
     * @param brainType
     */
    public static boolean isMainTypeOfF(int brainType) {
        LogMgr.d("isMainTypeOfF() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_F);
    }

    /**
     * 是F的子类型之一
     *
     * @param brainType
     */
    public static boolean isChildTypeOfF(int brainType) {
        LogMgr.d("isChildTypeOfF() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_F1 || brainType == GlobalConfig.ROBOT_TYPE_F2 || brainType == GlobalConfig.ROBOT_TYPE_F3 ||
                brainType == GlobalConfig.ROBOT_TYPE_F4 || brainType == GlobalConfig.ROBOT_TYPE_F5 || brainType == GlobalConfig.ROBOT_TYPE_F6 ||
                brainType == GlobalConfig.ROBOT_TYPE_F7 || brainType == GlobalConfig.ROBOT_TYPE_F8 || brainType == GlobalConfig.ROBOT_TYPE_F9);
    }

    /**
     * 是S的主类型之一
     *
     * @param brainType
     */
    public static boolean isMainTypeOfS(int brainType) {
        LogMgr.d("isMainTypeOfS() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_S);
    }

    /**
     * 是S的子类型之一
     *
     * @param brainType
     */
    public static boolean isChildTypeOfS(int brainType) {
        LogMgr.d("isChildTypeOfS() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_S1 || brainType == GlobalConfig.ROBOT_TYPE_S2 || brainType == GlobalConfig.ROBOT_TYPE_S3 ||
                brainType == GlobalConfig.ROBOT_TYPE_S4 || brainType == GlobalConfig.ROBOT_TYPE_S5 || brainType == GlobalConfig.ROBOT_TYPE_S6 ||
                brainType == GlobalConfig.ROBOT_TYPE_S7 || brainType == GlobalConfig.ROBOT_TYPE_S8 || brainType == GlobalConfig.ROBOT_TYPE_S9);
    }

    /**
     * 是AF的主类型之一
     *
     * @param brainType
     */
    public static boolean isMainTypeOfAF(int brainType) {
        LogMgr.d("isMainTypeOfAF() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_AF);
    }

    /**
     * 是U的主类型之一
     *
     * @param brainType
     */
    public static boolean isMainTypeOfU(int brainType) {
        LogMgr.d("isMainTypeOfU() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_U);
    }

    /**
     * 是AF的子类型之一
     *
     * @param brainType
     */
    public static boolean isChildTypeOfAF(int brainType) {
        LogMgr.d("isChildTypeOfAF() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_AF1 || brainType == GlobalConfig.ROBOT_TYPE_AF2 || brainType == GlobalConfig.ROBOT_TYPE_AF3 ||
                brainType == GlobalConfig.ROBOT_TYPE_AF4 || brainType == GlobalConfig.ROBOT_TYPE_AF5 || brainType == GlobalConfig.ROBOT_TYPE_AF6 ||
                brainType == GlobalConfig.ROBOT_TYPE_AF7 || brainType == GlobalConfig.ROBOT_TYPE_AF8 || brainType == GlobalConfig.ROBOT_TYPE_AF9);
    }

    /**
     * 是U的子类型之一
     *
     * @param brainType
     */
    public static boolean isChildTypeOfU(int brainType) {
        LogMgr.d("isChildTypeOfAF() brainType = " + brainType);
        return (brainType == GlobalConfig.ROBOT_TYPE_U5);
    }

    /**
     * 构造传输命令
     *
     * @param type 1个字节，代表不同的上层(机器人)型号
     * @param cmd1 1个字节，代表主命令
     * @param cmd2 1个字节，代表子命令
     * @param data 可变长度 null表示不带数据
     * @return 封装好的协议
     */
    public static byte[] buildProtocol(byte type, byte cmd1, byte cmd2, byte[] data) {
        byte[] sendbuff;
        if (data == null) {
            byte[] buf = new byte[7];
            buf[0] = type;
            buf[1] = cmd1;
            buf[2] = cmd2;
            sendbuff = addProtocol(buf);
        } else {
            byte[] buf = new byte[7 + data.length];
            buf[0] = type;
            buf[1] = cmd1;
            buf[2] = cmd2;
            System.arraycopy(data, 0, buf, 7, data.length);
            sendbuff = addProtocol(buf);
        }

        return sendbuff;
    }

    // 协议封装： AA 55 len1 len2 type cmd1 cmd2 00 00 00 00 (data) check

    /**
     * 增加协议头前两个字节为AA 55，三，四字节为长度，最后一个字节为校验位
     *
     * @param buff 类型，命令字1，命令字2，保留字，数据位
     */
    public static byte[] addProtocol(byte[] buff) {
        short len = (short) (buff.length + 5);
        byte[] sendbuff = new byte[len];
        sendbuff[0] = (byte) 0xAA; // 头
        sendbuff[1] = (byte) 0x55;
        // 扫描二维码协议更改为高位在前，地位在后
        sendbuff[3] = (byte) ((len - 4) & 0x00FF); // 长度: 从type到check
        sendbuff[2] = (byte) (((len - 4) >> 8) & 0x00FF);
        System.arraycopy(buff, 0, sendbuff, 4, buff.length); // type - data

        byte check = 0x00; // 校验位
        for (int n = 0; n < len - 1; n++) {
            check += sendbuff[n];
        }
        sendbuff[len - 1] = (byte) (check & 0x00FF);
        return sendbuff;
    }

    /**
     * 获取当前子类型，没有子类型时返回主类型 用于心跳中使用
     *
     */
    public static int getBrainType() {
        int brainType;
        if (BrainService.getmBrainService().isReturnChildType()) {
            brainType = (GlobalConfig.BRAIN_CHILD_TYPE == -1 || GlobalConfig.BRAIN_CHILD_TYPE == 0) ? GlobalConfig.BRAIN_TYPE : GlobalConfig.BRAIN_CHILD_TYPE;
        } else {
            brainType = GlobalConfig.BRAIN_TYPE;
        }
        if(GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_SE901){
            brainType = GlobalConfig.BRAIN_CHILD_TYPE;
        }
        LogMgr.i("getBrainType() brainType = " + brainType + "GlobalConfig.BRAIN_CHILD_TYPE = " + GlobalConfig.BRAIN_CHILD_TYPE + " GlobalConfig.BRAIN_TYPE = " + GlobalConfig.BRAIN_TYPE);
        return brainType;
    }

    /**
     * 获取当前子类型，没有子类型时返回主类型 用于回复扫码广播使用
     *
     */
    public static int getBrainTypeForScan() {
        int brainType;
        if (BrainService.getmBrainService().isReturnChildTypeFromScan()) {
            brainType = (GlobalConfig.BRAIN_CHILD_TYPE == -1 || GlobalConfig.BRAIN_CHILD_TYPE == 0) ? GlobalConfig.BRAIN_TYPE : GlobalConfig.BRAIN_CHILD_TYPE;
        } else {
            brainType = GlobalConfig.BRAIN_TYPE;
        }
        if(GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_SE901){
            brainType = GlobalConfig.BRAIN_CHILD_TYPE;
        }
        LogMgr.i("getBrainTypeForScan() brainType = " + brainType + "GlobalConfig.BRAIN_CHILD_TYPE = " + GlobalConfig.BRAIN_CHILD_TYPE + " GlobalConfig.BRAIN_TYPE = " + GlobalConfig.BRAIN_TYPE);
        return brainType;
    }

    /**
     * 是否屏幕自检命令
     * @param data
     * @return
     */
    public static boolean isScreenTestCmd(byte[] data) {
        boolean result = false;
        if(data != null && data.length >= 14 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1
                && data[5] == GlobalConfig.SELF_CHECK_IN_CMD_1 && data[6] == GlobalConfig.SELF_CHECK_IN_CMD_2_SCREEN){
            result = true;
        }
        return result;
    }

    /**
     * 是否自检程序中获取电量命令
     * @param data
     * @return
     */
    public static boolean isGetElectricityCmd(byte[] data) {
        boolean result = false;
        if(data != null && data.length >= 12 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1
                && data[5] == GlobalConfig.SELF_CHECK_IN_CMD_1 && data[6] == GlobalConfig.SELF_CHECK_IN_CMD_2_BATTERY){
            result = true;
        }
        return result;
    }

    public static boolean isResetAppTypeCmd(byte[] data) {
        boolean result = false;
        if(data != null && data.length >= 12 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1
                && data[5] == (byte)0x00 && data[6] == (byte)0x55){
            result = true;
        }
        return result;
    }

    /**
     * 是否动画命令
     * @param data
     * @return
     */
    public static boolean isAnimationCmd(byte[] data) {
        boolean result = false;
        if (data != null && data.length >= 11 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1
                && data[5] == GlobalConfig.ROBOTICSU_IN_CMD_1 && data[6] == GlobalConfig.ROBOTICSU_IN_CMD_2_PLAY_ANIM) {
            result = true;
        }
        return result;
    }

    /**
     * 是否获取音量命令
     * @return
     */
    public static boolean isGetVolumnCmd(byte[] data){
        boolean result = false;
        if (data != null && data.length >= 12 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1
                && data[5] == GlobalConfig.PLAY_IN_CMD_1 && data[6] == GlobalConfig.PLAY_IN_CMD_2_GET_VOL) {
            result = true;
        }
        return result;
    }

    /**
     * 是播放命令中的音量设置命令
     * @param data
     * @return
     */
    public static boolean isSetVolumnFromSkillPlayerCmd(byte[] data){
        boolean result = false;
        if (data != null && data.length > 12 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1
                && data[5] == GlobalConfig.PLAY_IN_CMD_1 && data[6] == GlobalConfig.PLAY_IN_CMD_2_SET_VOL) {
            result = true;
        }
        return result;
    }

    /**
     * 是RoboticsU的音量设置命令
     * @param data
     * @return
     */
    public static boolean isSetVolumnFromRoboticsUCmd(byte[] data){
        boolean result = false;
        if (data != null && data.length > 12 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1
                && data[5] == GlobalConfig.ROBOTICSU_IN_CMD_1 && data[6] == GlobalConfig.ROBOTICSU_IN_CMD_2_SET_VOL) {
            result = true;
        }
        return result;
    }

    /**
     * 是RoboticsU的显示电机图片命令
     * @param data
     * @return
     */
    public static boolean isSetMotorViewFromRoboticsUCmd(byte[] data){
        boolean result = false;
        if (data != null && data.length > 12 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1
                && data[5] == GlobalConfig.ROBOTICSU_IN_CMD_1 && data[6] == GlobalConfig.ROBOTICSU_IN_CMD_2_SHOW_MOTOR_PAGE) {
            result = true;
        }
        return result;
    }

    /**
     * 是否K5治具命令
     * @param data
     * @return
     */
    public static boolean isK5UDPCheckCmd(byte[] data) {
        boolean result = false;
        if(data != null && data.length >= 8
                &&(   (data[1] == GlobalConfig.K5_STM32_OUT_CMD_1 && data[2] == GlobalConfig.K5_STM32_OUT_CMD_2_ENGINE_POWER)
                    ||(data[1] == GlobalConfig.K5_STM32_OUT_CMD_1 && data[2] == GlobalConfig.K5_STM32_OUT_CMD_2_SENSOR)
                    ||(data[1] == GlobalConfig.K5_STM32_OUT_CMD_1 && data[2] == GlobalConfig.K5_STM32_OUT_CMD_2_COLOR)
                    ||(data[1] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[2] == GlobalConfig.KNOW_ROBOT_GYRO_IN_CMD_2)
                    ||(data[1] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[2] == GlobalConfig.KNOW_ROBOT_RECORD_IN_CMD_2)
                )){
            result = true;
        }
        return result;
    }

    /**
     * 是RoboticsM的获取Mac命令
     * @param data
     * @return
     */
    public static boolean isGetMacForMCmd(byte[] data){
        boolean result = false;
        if (data != null && data.length > 11 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1
                && data[5] == GlobalConfig.KNOW_ROBOT_IN_CMD_1 && data[6] == GlobalConfig.KNOW_ROBOT_MAC_CMD_2) {
            result = true;
        }
        return result;
    }
}
