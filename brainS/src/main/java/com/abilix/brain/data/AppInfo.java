package com.abilix.brain.data;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.abilix.brain.BrainActivity;

//该类提供安装的apk的信息
//在初始化时将apk的信息，通过packageManager查询保存到相关的list中，后续获取使用

/**
 * {@link com.abilix.brain.BrainActivity}页面中的页面对象类。
 */
public class AppInfo {
//public class AppInfo extends Mode {

    /**
     * 文件名
     */
    private String name;
    private int pageType = PAGE_TYPE_UNKNOWN;
    private int fileType = FILE_TYPE_UNKNOWN; // 0:项目编程 1:AbilixChart 2：AbilixScratch 3:SkillPlayer
    private long order;

    private String appName;
    private String appLabel;
    private Drawable appIcon;
    private Intent intent;
    private String pkgName;
    private String pathName;
    private String versionName;
    private int versionCode;
    private long installTime;
    private int robotClass;
    private int appLevel;


    private BrainActivity.ViewHoder viewHoder;

    public static final int PAGE_TYPE_UNKNOWN = 0;
    public static final int PAGE_TYPE_PROGRAM = 1;
    public static final int PAGE_TYPE_REST = 2;
    public static final int PAGE_TYPE_SOUL = 3;
    public static final int PAGE_TYPE_QR_CODE = 4;
    public static final int PAGE_TYPE_FILE = 5;
    public static final int PAGE_TYPE_APK = 6;
    public static final int PAGE_TYPE_RECORD = 7;
    public static final int PAGE_TYPE_IMAGE = 8;
    public static final int PAGE_TYPE_MULTIMEDIA = 9;
    public static final int PAGE_TYPE_ACTIVATE_H = 10;
    public static final int PAGE_TYPE_PROTECT_H = 11;
    public static final int PAGE_TYPE_GROUP = 12;
    public static final int PAGE_TYPE_MOTOR = 13;
    public static final int PAGE_TYPE_RESET = 14;
    //我叫奥科流思2.0
    public static final int PAGE_TYPE_M_FUTURE_SCIENCE = 20;   //未来科学站
    public static final int PAGE_TYPE_M_FUTURE_COOL = 21;   //未来酷乐园
    public static final int PAGE_TYPE_M_FUTURE_COMMUNICATION = 22;   //未来通信站
    public static final int PAGE_TYPE_M_ABILITY_TRAINING = 23;   //能力训练营
    public static final int PAGE_TYPE_M_AKLS = 25;    //我叫奥科流思

    //未来科学站2级页面
    public static final int PAGE_TYPE_M_FUTURE_SCIENCE_MY_FAMILY = 30;   //人工智能之我的家人
    public static final int PAGE_TYPE_M_FUTURE_SCIENCE_FIND_FRUIT = 31;   //人工智能之水果识别
    public static final int PAGE_TYPE_M_FUTURE_SCIENCE_FIND_FACE = 32;   //人工智能之人脸识别

    public static final int FILE_TYPE_UNKNOWN = -1;
    public static final int FILE_TYPE_PROJECT_PROGRAM = 0;
    public static final int FILE_TYPE_CHART = 1;
    public static final int FILE_TYPE_SCRATCH = 2;
    public static final int FILE_TYPE_SKILLPLAYER = 3;

    public static final long PAGE_ORDER_REST = -100;
    public static final long PAGE_ORDER_RESET = -97;
    public static final long PAGE_ORDER_IM_QOMOLANGMA = -99;
    public static final long PAGE_ORDER_QOMOLANGMA_SHOW = -98;
    public static final long PAGE_ORDER_SOUL = -89;
    public static final long PAGE_ORDER_QR_CODE = -88;
    public static final long PAGE_ORDER_SET = -87;
    public static final long PAGE_ORDER_MULTIMEDIA = -86;
    public static final long PAGE_ORDER_SKILLPLAYER = -85;
    public static final long PAGE_ORDER_GROUP = -84;
    public static final long PAGE_ORDER_ACTIVE = -84;
    public static final long PAGE_ORDER_PROTECT = -83;

    //我叫奥科流思2.0
    public static final int PAGE_ORDER_M_FUTURE_SCIENCE = -200;   //未来科学站
    public static final int PAGE_ORDER_M_FUTURE_COOL = -199;   //未来酷乐园
    public static final int PAGE_ORDER_M_FUTURE_COMMUNICATION = -198;  //未来通信站
    public static final int PAGE_ORDER_M_ABILITY_TRAINING = -197;  //能力训练营
    public static final int PAGE_ORDER_M_SKILL = -196;  //技能
    public static final int PAGE_ORDER_M_SET = -195;   //设置
    public static final int PAGE_ORDER_M_AKLS = -194;   //我叫奥科流思
    public static final int PAGE_ORDER_M_QR_CODE = -193;   //二维码


    public AppInfo() { }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appName) {
        this.appLabel = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public long getInstallTime() {
        return installTime;
    }

    public void setInstallTime(long installTime) {
        this.installTime = installTime;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public int getRobotClass() {
        return robotClass;
    }

    public void setRobotClass(int robotClass) {
        this.robotClass = robotClass;
    }

    public int getAppLevel() {
        return appLevel;
    }

    public void setAppLevel(int appLevel) {
        this.appLevel = appLevel;
    }


    //moveform Mode.java

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int type) {
        this.fileType = type;
    }

    public int getPageType() { return pageType; }

    public void setPageType(int pageType) {
        this.pageType = pageType;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) { this.order = order; }

    public BrainActivity.ViewHoder getViewHoder() {
        return viewHoder;
    }

    public void setViewHoder(BrainActivity.ViewHoder viewHoder) {
        this.viewHoder = viewHoder;
    }
}
