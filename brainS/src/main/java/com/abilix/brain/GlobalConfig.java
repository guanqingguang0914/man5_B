package com.abilix.brain;

import android.os.Build;

import com.abilix.brain.utils.Utils;

/**
 * 全局静态变量配置。机器人类型获取。
 */
public class GlobalConfig {

    public static int BRAIN_TYPE = 1;// Brain 是针对哪个系列的 1：C系列 2：M系列 3:H系列 4：F系列

    public static int BRAIN_CHILD_TYPE = -1;


    //统一用ROBOT_TYPE
    /* 机器人类型 */
    public static final int ROBOT_TYPE_COMMON = 0x00;
    public static final int ROBOT_TYPE_C = 0x01;
    public static final int ROBOT_TYPE_M = 0x02;
    public static final int ROBOT_TYPE_H = 0x03;
    public static final int ROBOT_TYPE_F = 0x04;
    public static final int ROBOT_TYPE_S = 0x05;
    public static final int ROBOT_TYPE_AF = 0x06;
    public static final int ROBOT_TYPE_U = 0x07;

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


    public static final int ROBOT_TYPE_U5 = 0x51;


    /**
     * 友盟appkey
     */
    public static final String BRAIN_UMENG_APPKEY = "58c8e049c895760847000648";
    public static final String BRAIN_C_UMENG_APPKEY = "58d8ca487f2c7449410017b9";
    public static final String BRAIN_M_UMENG_APPKEY = "58d8ca8782b6354af1000cf0";
    public static final String BRAIN_H_UMENG_APPKEY = "58d8ca9f310c93546200166e";
    public static final String BRAIN_F_UMENG_APPKEY = "58d8cab382b63547fc00043f";
    public static final String BRAIN_S_UMENG_APPKEY = "58e4cf0f1061d209f2000c29";
    public static final String BRAIN_AF_UMENG_APPKEY = "58e4cf53c62dca3e95001c1b";

    /**
     * 友盟渠道号
     */
    public static final String BRAIN_UMENG_CHANNEL = "Abilix Brain";


    public static final String HARDWARE_TYPE_RK = "rk30board";
    public static final String HARDWARE_TYPE_MT = "mt6580";


    // 更改为动态密码
    // public static final String PASSWORD = "64952827";
    public static final String WIFI_SSID = "Abilix-";

    /**
     * 建立tcp长连接
     */
    public static final boolean DEBUG_ON_TCP = true;
    /**
     * 心跳包发送的间隔秒数
     */
    public static final int HEART_BEAT_TIME = 1;
    /**
     * 是否接受心跳回复
     */
    public static final boolean isReceiveHeartBeatReply = true;
    /**
     * 检测心跳回复的间隔秒数
     */
    public static final int RECEIVE_HEART_BEAT_TIME = 2;
    /**
     * 接受心跳回复的不不响应最大允许次数
     */
    public static final int RECEIVE_HEART_BEAT_MAX_TIMES = 4;
    /**
     * 系统最大音量值
     */
    public static final int MAX_VOL = 12;
    /**
     * pad端发送最大音量值
     */
    public static final int MAX_VOL_RECEIVED = 255;
    /**
     * pad端发送最大音量值 From RoboticsU
     */
    public static final int MAX_VOL_RECEIVED_FROM_ROBOTICSU = 100;
    /**
     * 是否开启限制连一台客户端的功能
     */
    public static final boolean isLimitClientToOne = true;
    /**
     * 是否开启无限向右滑动
     */
    public static final boolean isUnlimitLoopRightSide = true;
    /**
     * 是否开启双向无限滑动 未实现
     */
    public static final boolean isUnlimitLoopBothSide = true;
    /**
     * 双向无限滑动最大页数
     */
    public static final int MAX_PAGE_INDEX = 1000;
    /**
     * 是否启用录音停止命令
     */
    public static final boolean isUsingRecordStopCmd = true;

    /**
     * 是否使用explain app运行项目编程，vjc
     */
    public static final boolean IS_USING_EXPLAINER_APP = true;
    /**
     * 是否使用新规则生成热点名称
     */
    public static final boolean IS_USING_NEW_RULE_CREATE_HOTPOT_SSID = true;
    /**
     * 是否使用H34充电安全机制
     */
    public static final boolean IS_USING_H_CHARGING_PROTECT = false;

    /**
     * 是否录音完成后播放录音
     */
    public static boolean ENABLE_RECORDING_PLAY = false;
    /**是否显示F固件升级进度*/
    public static final boolean isShowFFirmwareUpdateProgress = true;

    public static final String CHART_AND_PROJECT_PROGRAM_APP_PACKAGE_NAME = "com.abilix.explainer";
    public static final String CHART_APP_FILE_PATH_EXTRA_NAME = "filePath";
    public static final String CHART_APP_PAGE_NAME_EXTRA = "pageName";
    public static final String BRAIN_SET_GOTO_WIFI_PAGE = "brain_set_goto_wifi_page";

//    public static final int UPGRADE_STOP_REST = 254;// 固件升级是关闭休息动画

    //	public static final int TCP_CONNECT_SUCCESS = -3;//TCP连接成功
    public static final int APP_DEFAULT = -2;// 默认值
    public static final int PAD_APP_DISCONNECT = -1; // 断开连接
    public static final int KNOW_ROBOT_APP_FLAG = 0; // 连接上的是认识机器人
    public static final int PROGRAM_ROBOT_APP_FLAG = 1; // 连接上的是机器人编程
    public static final int ABILIX_CHART_APP_FLAG = 2; // 连接上的是abilix chart
    public static final int ABILIX_SCRATCH_APP_FLAG = 3; // 连接上的abilix scratch
    public static final int SKILL_PLAYER_APP_FLAG = 4; // 连接上的是skill player
    public static final int XICHEN_CONTROL_APP_FLAG = 5; // 连接上的是吸尘app
    public static final int TIQIU_APP_FLAG = 6; // 连接上的是踢球app
    public static final int JIUYUAN_APP_FLAG = 7; // 连接上的是救援机器人
    public static final int INNER_FILE_TRANSPORT_APP_STORE = 8; // app store
    // 下载的文件
    public static final int INNER_FILE_TRANSPORT_APP_FLAG = 9; // 连接上的是内部调试传输文件应用
    public static final int SKILL_CREATOR_APP_FLAG = 0x0D;//skill creator


    public static final int GET_IP = 192; // ip
    public static final int OPEN_UDP_FUNCTION = 193;
    public static final int CLOSE_UDP_FUNCTION = 194;
    public static final int FILE_DOWNLOAD = 200;// 文件下载成功，更新页面
    public static final int FILE_DOWNLOAD_S = 201;// 文件下载成功 ， 但不更新页面 与

    public static final int FILE_DOWNLOAD_GROUP = 205;// 群控文件下载成功 ， 但不更新页面 与
    // apk下载成功更新页面
    public static final int FILE_DOWNLOAD_U = 202;// 文件下载失败
    public static final int DELETE_SKILL_OLD_FILE = 203;// 删除skill-play 旧的文件夹
    public static final int FILE_DOWNLOAD_UPDATE = 204;//文件下载成功，用新数据更新老页面页面

    public static final int POWER_BUTTON_ONCLICK = 301;//电源键点击事件

    public static final String PAD_APP_CONNECT_STATE_CHANGE = "com.abilix.brain.pad_app_connect_state_change";
    public static final String PAD_APP_CONNECT_STATE_CHANGE_PARAM = "state";
    public static final String INSTALL_COMPLETE = "com.grandar.installservice.installComplete";
    public static final String INSTALL_COMPLETE_PACKAGENAME = "PackageName";
    public static final String INSTALL_ERROR = "com.grandar.installservice.installError";
    public static final String BROADCAST_CHARGING_INTENT_ACTION = "com.abilix.brain.charging";
    public static final String BROADCAST_ACTION_STOP_ALL_BRAIN = "com.abilix.brainset.stopall";

    public static final String BROADCAST_POWEKEYDOWN_ACTION = "Broadcast-for-User-App";

    public static final String M_ROBOT_STUCK_ZH_FILENAME = "mrobot_stuck_zh.mp3";
    public static final String M_ROBOT_STUCK_EN_FILENAME = "mrobot_stuck_en.mp3";

    public static final String C_ROBOT_ZH_HEITEMP = "C5_HeiTempNotice.mp3";
    public static final String C_ROBOT_EN_HEITEMP = "C5_HeiTempNotice_EN.mp3";

    public static final String M_ROBOT_ZH_HEITEMP = "M5_HeiTempNotice.mp3";
    public static final String M_ROBOT_EN_HEITEMP = "M5_HeiTempNotice_EN.mp3";
    /**
     * 包含固件版本信息的广播
     */
    public static final String FIREWARE_VERSION_BROADCAST = "com.abilix.control.STM32_SOFTWARE_VERSION_RSP";
    /**
     * 固件进行了升级的广播
     */
    public static final String FIREWARE_UPDATE_BROADCAST = "com.abilix.control.STM32_SOFTWARE_UPDATE_STATE";
    /**
     * 请求Control发送固件版本的广播
     */
    public static final String FIREWARE_REQUEST_VERSION_BROADCAST = "com.abilix.brain.STM32_REQUEST_VERSION";
    public static final String BROADCAST_ACTION_STM32_UPDATE_PROGRESS = "com.abilix.control.STM32_UPDATE_PROGRESS";
    public static final String BROADCAST_ACTION_EXTRA_NAME_STM32_UPDATE_PROGRESS = "stm32_update_progress";

    //Jinghao add  显示广告的广播
    //public static final String AD_SHOW_BROADCAST = "ad_show_broadcast";
    //Jinghao add  不显示广告的广播
    //public static final String AD_DISSHOW_BROADCAST = "ad_disshow_broadcast";
    //Jinghao add  启动广告服务广播
    //public static final String AD_SERVICE_BROADCAST = "ad_service_broadcast";


    public static final String ACTION_ACTIVITY = "com.abilix.brain.BrainActivity";
    public static final String ACTION_ACTIVITY_MODE = "mode";

    public static final String ACTION_ACTIVITY_FILE_FULL_PATH = "file_path";
    public static final String ACTION_ACTIVITY_APK_PKA_NAME = "apk_packageName"; // apk 包名
    /**
     * 安装应用前发送广播的action
     */
    public static final String ACTION_PACKAGE_NAME_TO_INSTALL = "com.abilix.install.packagename"; // apk 包名
    /**
     * tcp断开时发送广播的action，用于解析执行程序 退出
     */
    public static final String ACTION_TCP_DISCONNECT = "com.abilix.tcp.disconnect";


    /* stm32 版本号 */
//    public static final int STM_VISION_C = 16973844;
//    public static final int STM_VISION_C = 33685561;
    public static final int STM_VISION_C = 33685571;
    public static final int STM_VISION_M = 16842802;
    public static final int STM_VISION_H = 16973861;
    public static final int STM_VISION_C1 = 17367046;
    public static final int STM_VISION_M1 = 17498113;
    public static final int STM_VISION_S = 17104898;
    public static final int STM_VISION_F = 17039362;
    public static final int STM_VISION_AF = 17498113;
    public static final int STM_VISION_C9 = 34209800;
    public static final int STM_VISION_H3 = 16973859;
    public static final int STM_VISION_CU = 22020116;
    public static final int STM_VISION_MS = 21889025;//M3S,M4S

    /**
     * BrainActivity发送广播至BrainService
     */
    public static final String ACTION_SERVICE = "com.abilix.brain.BrainService";
    public static final String ACTION_SERVICE_MODE = "mode";
    public static final String ACTION_ACTIVITY_STATE = "state";
    public static final int ACTION_SERVICE_MODE_OLD_PROTOCOL = 0; // *0:代表brain传给control的是老协议数据mSendByte[] 保存命令数据
    public static final int ACTION_SERVICE_MODE_PROTOCOL = 1; // *1:代表brain传给control的是新协议数据已经解析完成只包含 命令字1 命令字2 以及 参数
    public static final int ACTION_SERVICE_MODE_SOUL = 2; // *2:代表要求执行soul
    public static final int ACTION_SERVICE_MODE_CHART = 3; // *3:代表要求执行 AbilixChart 文件 fileFullPath 是完整文件路径名
    public static final int ACTION_SERVICE_MODE_SCRATCH = 4; // *4:代表要求执行 AbilixScratch命令 mSendByte[]保存命令数据
    public static final int ACTION_SERVICE_MODE_SKILLPLAYER = 5; // *5:代表要求执行SkillPlayer 文件 fileFullPath是完整文件路径名
    public static final int ACTION_SERVICE_MODE_SERIES = 6; // *6:代表告知当前是哪个系列
    public static final int ACTION_SERVICE_MODE_REST = 7; // *7:休息状态切换
    public static final int ACTION_SERVICE_MODE_PROGRAM = 8; // 8:代表要求执行 项目编程文件
    public static final int ACTION_SERVICE_MODE_REQ_STM32_VER = 9; // 9:请求stm32版本号
    public static final int ACTION_SERVICE_POWER_OFF = 11; // 11:代表要求control发送关键指令给stm32
    public static final int ACTION_SERVICE_MODE_SCRATCH_FILE = 12; // *12:代表要求执行 AbilixScratch 文件fileFullPath 是完整文件路径名
    public static final int ACTION_SERVICE_MODE_STOP_ALL = 13;// *13:代表告诉control 停止所有的执行
    public static final int ACTION_SERVICE_MODE_LEARN_LETTER = 14;// *14:代表要求执行学习字母的命令fileFullPath是完整文件路径名
    public static final int ACTION_SERVICE_MODE_CONNECT = 255; // 255:代表 可以执行apd app命令或者不能执行pad app命令

    public static final String ACTION_SERVICE_MODE_STATE = "modeState";
    public static final int ACTION_SERVICE_MODE_STATE_OFF = 0;
    public static final int ACTION_SERVICE_MODE_STATE_ON = 1;
    public static final String ACTION_SERVICE_FILE_FULL_PATH = "file_path";

    /* 协议相关 */
    public static final int PROTOCOL_MIN_LENGTH = 12; // 协议最短长度
    public final static int CMD_BROADCAST_RETURN_CMD1 = 0xA0; // 广播消息 机器人端恢复
    public final static int CMD_BROADCAST_RETURN_CMD2_1 = 0x00; // 协议1.1.2
    public final static int CMD_BROADCAST_RETURN_CMD2_2 = 0xFF; // 协议1.1.4
    public static final int CMD_BROADCAST_ENTER_CMD2_2 = 0x61; // 进入编程界面

    public static final byte CMD_0 = (byte) 0xAA;
    public static final byte CMD_1 = (byte) 0x55;


    /* 文件传输命令字 */
    public static final byte FILE_RECEIVE_IN_CMD_1 = (byte) 0x01;
    public static final byte FILE_RECEIVE_IN_CMD_2_REQUEST = (byte) 0x01;
    public static final byte FILE_RECEIVE_IN_CMD_2_CANCEL = (byte) 0x02;
    public static final byte FILE_RECEIVE_IN_CMD_2_SEND = (byte) 0x03;
    public static final byte FILE_RECEIVE_IN_CMD_2_COMPLETE = (byte) 0x04;
    public static final byte FILE_RECEIVE_IN_CMD_2_REQUEST_FROM_BROKEN_POINT = (byte) 0x05;
    public static final byte FILE_RECEIVE_IN_CMD_2_REQUEST_FROM_START = (byte) 0x06;
    public static final byte FILE_RECEIVE_IN_CMD_2_PAUSE = (byte) 0x07;
    public static final byte FILE_RECEIVE_IN_CMD_2_QUERY = (byte) 0x08;

    public static final byte FILE_RECEIVE_OUT_CMD_1 = (byte) 0xA0;
    public static final byte FILE_RECEIVE_OUT_CMD_2_NOTIFY = (byte) 0x01;
    public static final byte FILE_RECEIVE_OUT_CMD_2_CHECK_WRONG = (byte) 0x02;
    public static final byte FILE_RECEIVE_OUT_CMD_2_RECEIVE_SUCCESS = (byte) 0x03;
    public static final byte FILE_RECEIVE_OUT_CMD_2_NOTIFY_FROM_BROKEN_POINT = (byte) 0x04;
    public static final byte FILE_RECEIVE_OUT_CMD_2_PAUSE_OK = (byte) 0x05;
    public static final byte FILE_RECEIVE_OUT_CMD_2_CANCEL_OK = (byte) 0x06;
    public static final byte FILE_RECEIVE_OUT_CMD_2_QUERY_OK = (byte) 0x07;

    /* App Store 控制命令 */
    public static final byte APP_STORE_IN_CMD_1 = (byte) 0x09;
    public static final byte APP_STORE_IN_CMD_2_REQUEST_APP_INFO = (byte) 0x01;
    public static final byte APP_STORE_IN_CMD_2_OPEN_APP = (byte) 0x02;
    public static final byte APP_STORE_IN_CMD_2_CLOSE_APP = (byte) 0x03;

    public static final byte APP_STORE_OUT_CMD_1 = (byte) 0xA3;
    public static final byte APP_STORE_OUT_CMD_2_RESPONSE_APP_INFO = (byte) 0x01;
    public static final byte APP_STORE_OUT_CMD_2_FEEDBACK = (byte) 0x02;
    public static final byte APP_STORE_OUT_CMD_2_OPEN_APP = (byte) 0x03;
    public static final byte APP_STORE_OUT_CMD_2_CLOSE_APP = (byte) 0x04;
    /* 心跳包命令字 */
    public static final byte HEART_BEAT_FIRST_IN_CMD_1 = (byte) 0x00;
    public static final byte HEART_BEAT_FIRST_IN_CMD_2 = (byte) 0x02;
    public static final byte HEART_BEAT_IN_CMD_1 = (byte) 0x00;
    public static final byte HEART_BEAT_IN_CMD_2 = (byte) 0x04;
    public static final byte HEART_BEAT_OUT_CMD_1 = (byte) 0xA0;
    public static final byte HEART_BEAT_OUT_CMD_2 = (byte) 0x04;
    /* 确认机器人端是否已有连接命令字 */
    public static final byte CHECK_BEFORE_CONNECT_IN_CMD_1 = (byte) 0x00;
    public static final byte CHECK_BEFORE_CONNECT_IN_CMD_2 = (byte) 0x03;
    public static final byte CHECK_BEFORE_CONNECT_OUT_CMD_1 = (byte) 0xA0;
    public static final byte CHECK_BEFORE_CONNECT_OUT_CMD_2 = (byte) 0x04;
    /* 认识机器人命令字*/
    public static final byte KNOW_ROBOT_IN_CMD_1 = (byte) 0x20;
    public static final byte KNOW_ROBOT_RECORD_IN_CMD_2 = (byte) 0x02;
    public static final byte KNOW_ROBOT_GYRO_IN_CMD_2 = (byte) 0x03;
    public static final byte KNOW_ROBOT_PLAY_SOUND_IN_CMD_2 = (byte) 0x04;
    public static final byte KNOW_ROBOT_RTSP_VIDEO_IN_CMD_2 = (byte) 0x05;
    public static final byte KNOW_ROBOT_DRAW_LINE_IN_CMD_2 = (byte) 0x06;
    public static final byte KNOW_ROBOT_MIC_VOL_IN_CMD_2 = (byte) 0x0D;
    public static final byte KNOW_ROBOT_MAC_CMD_2 = (byte) 0x0F;

    public static final byte KNOW_ROBOT_OUT_CMD_1 = (byte) 0xA4;
    public static final byte KNOW_ROBOT_RECORD_OUT_CMD_2 = (byte) 0x00;
    public static final byte KNOW_ROBOT_GYRO_OUT_CMD_2 = (byte) 0x01;
    public static final byte KNOW_ROBOT_VIDEO_OUT_CMD_2 = (byte) 0x03;
    public static final byte KNOW_ROBOT_MIC_VOL_OUT_CMD_2 = (byte) 0x04;
    public static final byte KNOW_ROBOT_MAC_OUT_CMD_2 = (byte) 0x07;

    /* 多媒体播放命令字 2.3.15*/
    public static final byte MULTI_MEDIA_IN_CMD_1 = (byte) 0x22;
    public static final byte MULTI_MEDIA_IN_CMD_2_PLAY = (byte) 0x01;
    public static final byte MULTI_MEDIA_IN_CMD_2_PAUSE = (byte) 0x02;
    public static final byte MULTI_MEDIA_IN_CMD_2_STOP = (byte) 0x03;
    public static final byte MULTI_MEDIA_IN_CMD_2_RESUME = (byte) 0x04;

    public static final byte MULTI_MEDIA_OUT_CMD_1 = (byte) 0xA6;
    public static final byte MULTI_MEDIA_OUT_CMD_2_PLAY = (byte) 0x01;
    public static final byte MULTI_MEDIA_OUT_CMD_2_PAUSE = (byte) 0x02;
    public static final byte MULTI_MEDIA_OUT_CMD_2_STOP = (byte) 0x03;
    public static final byte MULTI_MEDIA_OUT_CMD_2_RESUME = (byte) 0x04;
    public static final byte MULTI_MEDIA_OUT_CMD_2_PLAY_COMPLETE = (byte) 0x05;
    /* H自检程序命令字*/
    public static final byte SELF_CHECK_IN_CMD_1 = (byte) 0x25;
    public static final byte SELF_CHECK_IN_CMD_2_SCREEN = (byte) 0x01;
    public static final byte SELF_CHECK_IN_CMD_2_BATTERY = (byte) 0x02;
    public static final byte SELF_CHECK_IN_CMD_2_JOINT = (byte) 0x03;

    public static final byte SELF_CHECK_OUT_CMD_1 = (byte) 0xA7;
    public static final byte SELF_CHECK_OUT_CMD_2_BATTERY = (byte) 0x01;
    public static final byte SELF_CHECK_OUT_CMD_2_JOINT = (byte) 0x02;

    /* RoboticsU命令*/
    public static final byte ROBOTICSU_IN_CMD_1 = (byte) 0x24;
    public static final byte ROBOTICSU_IN_CMD_2_NOTIFY_MODEL_TYPE = (byte) 0x01;
    public static final byte ROBOTICSU_IN_CMD_2_MOVE = (byte) 0x02;
    public static final byte ROBOTICSU_IN_CMD_2_FUNCTION = (byte) 0x03;
    public static final byte ROBOTICSU_IN_CMD_2_SET_VOL = (byte) 0x04;
    public static final byte ROBOTICSU_IN_CMD_2_PLAY_ANIM = (byte) 0x05;
    public static final byte ROBOTICSU_IN_CMD_2_SHOW_MOTOR_PAGE = (byte) 0x07;

    /*K5stm32层协议*/
    public static final byte K5_STM32_OUT_CMD_1 = (byte) 0xA3;
    public static final byte K5_STM32_OUT_CMD_2_ENGINE_POWER = (byte) 0x02;
    public static final byte K5_STM32_OUT_CMD_2_SENSOR = (byte) 0x06;
    public static final byte K5_STM32_OUT_CMD_2_COLOR_HLSC = (byte) 0x07;
    public static final byte K5_STM32_OUT_CMD_2_GYROSCOPE = (byte) 0x2A;
    public static final byte K5_STM32_OUT_CMD_2_COLOR= (byte) 0x2D;

    public static final byte K5_STM32_IN_CMD_1 = (byte) 0xA3;
    public static final byte K5_STM32_IN_CMD_2_SENSOR = (byte) 0x22;
    public static final byte K5_STM32_IN_CMD_2_COLOR_HLSC = (byte) 0x23;
    public static final byte K5_STM32_IN_CMD_2_GYROSCOPE = (byte) 0x2A;

    /* 上层APP类别 */
    public static final byte APP_TYPE_KNOW_ROBOT = (byte) 0x00;
    public static final byte APP_TYPE_PROGRAM_ROBOT = (byte) 0x01;
    public static final byte APP_TYPE_ABILIX_CHART = (byte) 0x02;
    public static final byte APP_TYPE_ABILIX_SCRATCH = (byte) 0x03;
    public static final byte APP_TYPE_SKILL_PLAYER = (byte) 0x04;
    public static final byte APP_TYPE_XICHEN_CONTROL = (byte) 0x05;
    public static final byte APP_TYPE_TIQIU = (byte) 0x06;
    public static final byte APP_TYPE_JIUYUAN = (byte) 0x07;
    public static final byte APP_TYPE_STORE = (byte) 0x08;
    public static final byte APP_TYPE_INNER_FILE = (byte) 0x09;
    public static final byte APP_TYPE_MULTI_MEDIA = (byte) 0x0B;
    public static final byte APP_TYPE_PROGRAM_ROBOT_FOR_S = (byte) 0x0C;
    public static final byte APP_TYPE_SKILL_CREATOR = (byte) 0x0D;
    public static final byte APP_TYPE_UNKNOWN = (byte) 0xFF;

    public static final byte PLAY_IN_CMD_1 = (byte) 0x02;
    public static final byte PLAY_IN_CMD_2_GET_VOL = (byte) 0x04;
    public static final byte PLAY_IN_CMD_2_SET_VOL = (byte) 0x05;
    public static final byte PLAY_OUT_CMD_1 = (byte) 0xA2;
    public static final byte PLAY_OUT_CMD_2_SEND_VOL = (byte) 0x05;

//    public static final byte KNOW_ROBOT_CAMERA_IN_CMD_1 = (byte) 0x20;
//    public static final byte KNOW_ROBOT_CAMERA_IN_CMD_2 = (byte) 0x05;
//    public static final byte KNOW_ROBOT_CAMERA_OUT_CMD_1 = (byte) 0xA4;
//    public static final byte KNOW_ROBOT_CAMERA_OUT_CMD_2 = (byte) 0x03;
    /**
     * 代表brain传给control的是老协议数据 mSendByte[] 保存命令数据
     */
    public static final int CONTROL_CALLBACKMODE_OLD_PROTOCOL = 0;
    /**
     * 代表brain传给control的是新协议数据已经解析完成只包含 命令字1 命令字2 以及 参数
     */
    public static final int CONTROL_CALLBACKMODE_NEW_PROTOCOL = 1;
    /**
     * 代表soul开启或关闭mModeState
     */
    public static final int CONTROL_CALLBACKMODE_SOUL_CMD = 2;
    /**
     * 代表要求执行 AbilixChart 文件 fileFullPath是完整文件路径名 mModeState; //模式状态 0:关闭 1:开启
     */
    public static final int CONTROL_CALLBACKMODE_CHART_CMD = 3;
    /**
     * 代表要求执行 AbilixScratch 命令mSendByte[] 保存命令数据 mModeState; //模式状态 0:关闭 1:开启
     */
    public static final int CONTROL_CALLBACKMODE_SCRATCH_CMD = 4;
    /**
     * 代表要求执行SkillPlayer 文件fileFullPath 是完整文件路径名 mModeState; mSendByte里的参数看3.1.1 //模式状态 0:关闭 1:开启
     */
    public static final int CONTROL_CALLBACKMODE_SKILLPLAYER_CMD = 5;
    /**
     * 代表告知当前是哪个系列 mSendByte[] 1个字节代表是哪个系列的 0x01:C系列 0x02:M系列 0x03:H系列0x04:F系列
     */
    public static final int CONTROL_CALLBACKMODE_NOTIFY_ROBOT_TYPE = 6;
    /**
     * 休息状态切换 mModeState; //模式状态 0:关闭 1:开启
     */
    public static final int CONTROL_CALLBACKMODE_REST_CMD = 7;
    /**
     * 代表要求执行 项目编程文件 文件fileFullPath 是完整文件路径名 mModeState; / /模式状态 0:关闭 1:开启
     */
    public static final int CONTROL_CALLBACKMODE_PROGRAM_CMD = 8;
    /**
     * 代表要求获取固件stm32版本号control 和当前机器人类型 获取到后 发送广播消息告知版本号
     */
    public static final int CONTROL_CALLBACKMODE_ASK_FOR_STM_VERSION_CMD = 9;
    /**
     * 代表要求升级stm32固件mFileFullPath代表升级固件的文件名 文件传输成功后发送广播消息告知
     */
    public static final int CONTROL_CALLBACKMODE_ASK_FOR_STM_UPDATE_CMD = 10;
    /**
     * 代表要求发送关机命令给 stm32
     */
    public static final int CONTROL_CALLBACKMODE_TURN_OFF_CMD = 11;
    /**
     * 代表要求执行AbilixScratch文件
     */
    public static final int CONTROL_CALLBACKMODE_SCRATCH_FILE_CMD = 12;
    /**
     * 代表停止所有的
     */
    public static final int CONTROL_CALLBACKMODE_STOP_ALL_CMD = 13;
    /**
     * 代表学字母接口，modeState 0：停止。 1：播放。 2：暂停。3：继续。
     */
    public static final int CONTROL_CALLBACKMODE_LEARN_LETTER_CMD = 14;
    /**
     * 代表不成规模的小功能。modeState 1：通知去掉M轮子点击保护。
     */
    public static final int CONTROL_CALLBACKMODE_PATCH_CMD = 15;

    public static final int CONTROL_CALLBACKMODE_SEND_ELF_FILE_CMD = 17;

    public static final String APP_ABILIX_LAUNCHER = "android.intent.category.abilix_launcher";
    public static final String APP_PKGNAME_TEST_C = "com.abilix.robot_c_test"; // 自检程序 c 包名
    public static final String APP_PKGNAME_TEST_M = "com.abilix.test_stm32"; // 自检程序 m 包名
    public static final String APP_PACKAGE_NAME_UPDATE_STM32_APK = "com.example.abilixreceiver";//

    /**
     * H5开机启动手指保护
     */
    public static final int STARTING_UP_FINGER_PROTECT = 27;

    /**
     * H5执行充电保护动作
     */
    public static final int CHARGE_PROTECTION_MOVE = 28;

    /**
     * 不能卸载的app包名
     */
    public static final String APP_PKGNAME_VOLUMECONTROL = "com.abilix.volumecontrol";
    public static final String APP_PKGNAME_UPDATEONLINETEST = "com.abilix.updateonlinetest";
    public static final String APP_PKGNAME_BRAINSET = "com.abilix.brainset";


    /**
     * 我叫奥科流思2.0内置应用或不能卸载的应用
     */
//    public static final String APP_PKGNAME_FACEDETECT = "com.abilix.learn.facedetect";   //人脸
    public static final String APP_PKGNAME_MY_FAMILY = "com.abilix.learn.myfamily";   //我的家人
    public static final String APP_PKGNAME_GUESSFRUIT = "com.abilix.learn.guessfruit";   //水果
    public static final String APP_PKGNAME_GUESSSTUDY = "com.abilix.learn.guessstudy";   //文具
//    com.abilix.learn.oculus.distributorservice.activity.LearnOrderActivity
    public static final String APP_PKGNAME_OCULUS = "com.abilix.learn.oculus.distributorservice";   //指令学习

    /**
     * H34我叫珠穆朗玛 包名
     */
    public static final String APP_PKGNAME_IM_QOMOLANGMA = "com.abilix.learn.h_qomolangma";
    /**
     * H34珠穆朗玛SHOW 包名
     */
    public static final String APP_PKGNAME_QOMOLANGMA_SHOW = "com.abilix.learn.randomwalk";
    //U201 内置apk不被删除；
    public static final String APP_PKGNAME_MEMORYGAME = "com.abilix.learn.simongame";
    public static final String APP_PKGNAME_TENNIS = "com.abilix.learn.playball";
    public static final String APP_PKGNAME_IOS7LEVEL = "mobi.thinkchange.android.ios7level";
//	public static final String APP_PKGNAME_KNOWTHEROBOT = "com.abilix.knowtherobot";

    //更新安装状态广播
    public static final String ACTION_UPDATE_ONLINE_OPERATION_STATE = "com.grandar.updateonlinetest.updateOperationState";
    public static final int UPDATE_ONLINE_OPERATION_SUCCESS = 0x01; // 安装成功
    public static final int UPDATE_ONLINE_OPERATION_IGNORED = 0x02; // 忽略更新
    public static final int UPDATE_ONLINE_OPERATION_FAILED = 0x03; // 操作失败
    public static final int UPDATE_ONLINE_OPERATION_NO_UPDATES = 0x04; // 没有可用更新

    public static final String INTENT_ACTION_PICTURE = "com.abilix.explainer.mainactivity.picture";
    public static final String INTENT_ACTION_RECORD = "com.abilix.explainer.mainactivity.record";
    private final static String ROBOT_BUILD_TYPE = Build.DISPLAY;


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
                if (Utils.getCoreVersionNumber(ROBOT_BUILD_TYPE) == CORE_MTK6580) {
                    //mtk6580平台
                    if (Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_C_201) {
                        //表示目前用于C项目平衡车（201）项目
                        BRAIN_TYPE = ROBOT_TYPE_C;
                        BRAIN_CHILD_TYPE = ROBOT_TYPE_CU;
                    } else if (Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_C9) {
                        //表示目前用于C项目C9项目
                        BRAIN_TYPE = ROBOT_TYPE_C9;
                        BRAIN_CHILD_TYPE = ROBOT_TYPE_C9;
                    } else {
                        BRAIN_TYPE = ROBOT_TYPE_C;
                    }
                } else if (Utils.getCoreVersionNumber(ROBOT_BUILD_TYPE) == CORE_RK3128) {
                    //rk3128平台
                    BRAIN_TYPE = ROBOT_TYPE_C1;
                } else {
                    BRAIN_TYPE = ROBOT_TYPE_C;
                }
                break;
            case "M":
                if (Utils.getCoreVersionNumber(ROBOT_BUILD_TYPE) == CORE_MTK6580) {
                    //mtk6580平台
                    BRAIN_TYPE = ROBOT_TYPE_M;
                    if (Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_M3S_M4S) {
                        BRAIN_CHILD_TYPE = ROBOT_TYPE_M3S;
                    }
                } else if (Utils.getCoreVersionNumber(ROBOT_BUILD_TYPE) == CORE_RK3128) {
                    //rk3128平台
                    BRAIN_TYPE = ROBOT_TYPE_M1;
                } else {
                    BRAIN_TYPE = ROBOT_TYPE_M;
                }
                break;
            case "H":
                if (Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_H3_H4) {
                    BRAIN_TYPE = ROBOT_TYPE_H3;
                } else if(Utils.getFunctionVersionNumber(ROBOT_BUILD_TYPE) == FUNCTION_SE901){
                    BRAIN_TYPE = ROBOT_TYPE_H;
//                    BRAIN_CHILD_TYPE = ROBOT_TYPE_SE901;//暂时注掉
                }else {
                    BRAIN_TYPE = ROBOT_TYPE_H;
                }
                break;
            case "F":
                BRAIN_TYPE = ROBOT_TYPE_F;
                break;
            case "S":
                BRAIN_TYPE = ROBOT_TYPE_S;
                break;
            case "AF":
                BRAIN_TYPE = ROBOT_TYPE_AF;
                break;
            case "U":
                BRAIN_TYPE = ROBOT_TYPE_U;
                break;
            default:
                BRAIN_TYPE = ROBOT_TYPE_C;
                break;
        }
    }
}