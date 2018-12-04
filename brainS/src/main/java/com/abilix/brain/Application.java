package com.abilix.brain;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.Utils;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.camera.usbcamera.UsbCamera;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;
import com.umeng.analytics.MobclickAgent.UMAnalyticsConfig;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Brain的Application类，
 * 存储全局的状态变量，
 * 进行项目的初始化工作。
 */
public class Application extends android.app.Application {

    public static Application instance;
    /**
     * H5只执行一次保护动作，true执行，false不执行
     */
    public boolean onceProtectionAction = true;

    private Context context;
    /* 当前是否有外接设备连接 */
    private boolean isTcpConnecting = false;
    private int mVersionCode = 0;
    private int mFirewareVersion = -1;
    private boolean isMWheelProtected = false;
    private boolean isRestState = false;
    private boolean isFirstStartApplication = true;

    /**开机时H34动作文件存储是否完成*/
    private boolean isFileSaveCompleted = false;

    /**群控模式是否*/
    private boolean isKeepGroup = false;
    /**群控模式是否下载*/
    private boolean isKeepGroupLoadingComPelte = true;
    private String pathName = "";

    private final String PHOTO_DIR_CONFIG = "photo_dir_config";
    private final int photoDirVer = 0x01;

    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
//        LogMgr.i("Brain Application onCreate()1");
        super.onCreate();
        LogMgr.startExportLog();
        instance = this;
        context = getApplicationContext();
        if (!Utils.appIsDebugable(instance)) {
            LogMgr.setLogLevel(LogMgr.NOLOG);
        }
        Matching.stm32_Update();
        ExplainerApplication.init(instance);
        //清空用户文件[.bin, .elf, 录音，照片]
        if (FileUtils.getCleanUpFlag()) {
            FileUtils.cleanUpUserFiles();
        }
        new Thread(new Runnable() {
            @SuppressWarnings("unused")
            @Override
            public void run() {
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C
                        || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C9
                        || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                    pathName = "music_c";
                } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M
                        || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M1) {
                    pathName = "music_m";
                } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H
                        || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                    pathName = "music_h";
                } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_F) {
                    pathName = "music_f";
                } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_S) {
                    pathName = "music_s";
                } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_AF) {
                    pathName = "music_af";
                }
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H
                        || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                    FileUtils.saveSoundFileToSdCard(context, FileUtils.LIBRARY_RES , "vision");
                    FileUtils.saveSoundFileToSdCard(context, FileUtils.SDCARD, "www");
//                    FileUtils.saveSoundFileToSdCard(context, FileUtils.MOVEBIN, "walktunner");
                    FileUtils.saveSoundFileToSdCard(context, FileUtils.MOVEBIN, "MoveBin");
                    FileUtils.saveSoundFileToSdCard(context, FileUtils.MOVEBIN, "Download");
                }
                isFileSaveCompleted = true;
                if (pathName != null) {
                    FileUtils.saveSoundFileToSdCard(context, FileUtils.AUDIOPATH, pathName);
                }

            }
        }).start();
        initUM(); //初始化友盟统计，此为第三方合作的植入
        UsbCamera.create().registeSystemUSBCamera(this);


        uncatchExecptionForLog();
        LogMgr.i("Brain Application onCreate()2");
        // Utils.getExternalAvailableSize();
        // Utils.getExternalTotalSize();
        // Utils.getDataAvailableSize();
        // Utils.getDataTotalSize();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogMgr.stopExportLog();
    }

    public String getPathName() {
        return pathName;
    }

    public boolean isTcpConnecting() {
        return isTcpConnecting;
    }

    public void setTcpConnecting(boolean isTcpConnecting) {
        this.isTcpConnecting = isTcpConnecting;
    }

    public int getmVersionCode() {
        return mVersionCode;
    }

    public void setmVersionCode(int mVersionCode) {
        LogMgr.d("ServerHandler", "mVersionCode = " + mVersionCode);
        this.mVersionCode = mVersionCode;
    }

    public boolean getisKeepGroup() {
        return isKeepGroup;
    }
    public void setisKeepGroup(boolean isKeepGroup) {
        this.isKeepGroup = isKeepGroup;
    }
    public boolean getisKeepGroupLoadingComPelte() {
        return isKeepGroupLoadingComPelte;
    }
    public void setisKeepGroupLoadingComPelte(boolean isKeepGroupLoadingComPelte) {
        this.isKeepGroupLoadingComPelte = isKeepGroupLoadingComPelte;
    }
    public int getmFirewareVersion() {
        return mFirewareVersion;
    }

    public void setmFirewareVersion(int mFirewareVersion) {
        this.mFirewareVersion = mFirewareVersion;
    }

    public boolean getIsRestState() {//是否是休息状态
        return isRestState;
    }

    public void setIsRestState(boolean isRestState) {
        this.isRestState = isRestState;
    }

    public boolean isMWheelProtected() {
        return isMWheelProtected;
    }

    public void setMWheelProtected(boolean isMWheelProtected) {
        this.isMWheelProtected = isMWheelProtected;
    }

    public boolean isFirstStartApplication() {
        return isFirstStartApplication;
    }

    public void setFirstStartApplication(boolean isFirstStartApplication) {
        this.isFirstStartApplication = isFirstStartApplication;
    }

    public boolean isFileSaveCompleted() {
        return isFileSaveCompleted;
    }

    /**
     * 初始化友盟统计
     */
    private void initUM() {
        UMAnalyticsConfig config = null;
        if (ProtocolUtil.isMainTypeOfC(GlobalConfig.BRAIN_TYPE)) { // C类型
            config = new UMAnalyticsConfig(context, GlobalConfig.BRAIN_C_UMENG_APPKEY,
                    GlobalConfig.BRAIN_UMENG_CHANNEL, EScenarioType.E_UM_NORMAL);
        } else if (ProtocolUtil.isMainTypeOfM(GlobalConfig.BRAIN_TYPE)) { // M类型
            config = new UMAnalyticsConfig(context, GlobalConfig.BRAIN_M_UMENG_APPKEY,
                    GlobalConfig.BRAIN_UMENG_CHANNEL, EScenarioType.E_UM_NORMAL);
        } else if (ProtocolUtil.isMainTypeOfH(GlobalConfig.BRAIN_TYPE)) { // H类型
            config = new UMAnalyticsConfig(context, GlobalConfig.BRAIN_H_UMENG_APPKEY,
                    GlobalConfig.BRAIN_UMENG_CHANNEL, EScenarioType.E_UM_NORMAL);
        } else if (ProtocolUtil.isMainTypeOfF(GlobalConfig.BRAIN_TYPE)) { // F类型
            config = new UMAnalyticsConfig(context, GlobalConfig.BRAIN_F_UMENG_APPKEY,
                    GlobalConfig.BRAIN_UMENG_CHANNEL, EScenarioType.E_UM_NORMAL);
        } else if (ProtocolUtil.isMainTypeOfS(GlobalConfig.BRAIN_TYPE)) { // S类型
            config = new UMAnalyticsConfig(context, GlobalConfig.BRAIN_S_UMENG_APPKEY,
                    GlobalConfig.BRAIN_UMENG_CHANNEL, EScenarioType.E_UM_NORMAL);
        } else if (ProtocolUtil.isMainTypeOfAF(GlobalConfig.BRAIN_TYPE)) { // AF类型
            config = new UMAnalyticsConfig(context, GlobalConfig.BRAIN_AF_UMENG_APPKEY,
                    GlobalConfig.BRAIN_UMENG_CHANNEL, EScenarioType.E_UM_NORMAL);
        }
        MobclickAgent.startWithConfigure(config);
    }

    // 拦截不可捕获的异常，保存到Log日志里
    private void uncatchExecptionForLog() {
        // 拦截异常
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                throwable.printStackTrace();
                LogMgr.e("Brain已经崩溃！！！！！崩溃原因：" + throwable.toString());
            }
        });
    }

    /**
     * 根据型号设置屏幕方向
     */
    public void setOrientation(Activity activity) {
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C9) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制横屏
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
        }
    }
}
