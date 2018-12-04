package com.abilix.brain.m;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;

import com.abilix.brain.BrainActivity;
import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.Utils;

import java.util.Iterator;
import java.util.List;

/**
 * creater: yhd
 * created: 2017/12/1 13:43
 */

public class MUtils {

    /*
    超声琴：com.abilix.learn.echopiano
    赶鸭子：com.abilix.ganyazi
    */

    public static final int B_UI_A = 3;//心跳页面
    public static final int B_UI_B = 4;//生气页面
    public static final int B_UI_C = 5;//爱心页面
    public static final int B_UI_F = 8;//唱歌页面
    public static final int B_UI_G = 9;//休眠页面
    public static final int B_UI_H = 10;//紧张页面

    public static final int B_GAME_A = 11;//进入赶鸭子小应用
    public static final int B_GAME_B = 12;//进入超声琴小应用
    public static final int B_GAME_C = 13;//进入学英语小应用
    public static final int B_GAME_D = 14;//进入乐感达人小应用 false
    public static final int B_GAME_E = 15;//跳舞启动的与感达人  true
    public static final int B_SHOW_BATTERY = 16;//展示电池UI
    public static final int B_HIDE_BATTERY = 17;//隐藏电池UI
    public static final int B_SHOW_VOICE_PICKER_CANTTOUCH = 18;//展示一个不能点击触摸的声音UI
    public static final int B_HIDE_VOICE_PICKER_CANTTOUCH = 19;//隐藏这个不能点击触摸的声音UI
    public static final int B_BACK_TO_BRAIN_MAINACTIVITY = 20;//回到brain的MainActivity
    public static final int B_TOAPP = 21;//进入拍照漫游activity
    public static final int B_BACKAPP = 22;//退出拍照漫游activity
    public static final int B_INIT_COMPLETE = 23;//初始化完成

    public static final int B_WIFI = 200;//进入WIfi
    private static final int B_OTHER_APP = 201;  //进入其他APP

    public static final int START_EMOTION = 0x05000300;  //開始情緒
    public static final int EXIT_EMOTION = 0x05000400;  //關閉情緒

    public static final int B_GAMEA_ERROR = 0x05000600;//进入赶鸭子小应用失败
    public static final int B_GAMEB_ERROR = 0x05000700;//进入超声琴小应用失败
    public static final int B_GAMEC_ERROR = 0x05000800;//进入学英语小应用失败
    public static final int B_GAMED_ERROR = 0x05000900;//进入乐感达人小应用失败

    public static final int B_GO_TO_APP = 0x05000A00;//brain进入其他应用
    public static final int B_BACK_TO_BRAIN = 0x05000B00;//返回brain

    public static final int B_CLICK_GO_TO_APP = 0x0500C00;//brain进入其他应用(手点)
    public static final int b_CLICK_BACK_TO_BRAIN = 0x05000D00;//返回brain(手点)

    public static final String APP_GANYAZI = "com.abilix.ganyazi";
    public static final String APP_ECHOPIANO = "com.abilix.learn.echopiano";
    public static final String APP_LEARNEN = "com.abilix.learnenglish";
    public static final String APP_YUEGAN = "com.abilix.learn.m_musicking";
    public static final String OCULUS_APP = "oculus_app";  //乐感达人附加的信息

    public static boolean sIsAppBack = false;
    public static boolean sIsVoiceAppBack = false;  //是否是语音开启APP  ,true代表语音开启,false代表点击开启


    public static final String BRAIN_PKG_NAME = "com.abilix.brain";
    public static final String BRAIN_ACTIVITY_NAME = "com.abilix.brain.BrainActivity";

    public static final String BRAINSET_MANAGEFRAGMENT = "com.abilix.brainset.ManageFragment";

    public static final int FRAGMENT_ID_WIFI = 0x00;
    public static final int FRAGMENT_ID_BATTERY = 0x03;
    public static final int FRAGMENT_ID_VOLUME = 0x04;

    public static boolean sIsFirstResume = true;  //是否是第一次走BrainActivity的Resume方法
    public static boolean sIsYuyinControl; //是语音控制返回


    /**
     * 切换M系列情绪状态
     */
    public static void changeMState(MyGiftView giftView, MState mState) {

        if (giftView == null) {
            return;
        }

        switch (mState) {
            case M_STATE_HEART_JUMP:
                giftView.setMovieResource(R.drawable.m_gif_1);
                break;

            case M_STATE_ANGRY:
                giftView.setMovieResource(R.drawable.m_gif_2);
                break;

            case M_STATE_HEART:
                giftView.setMovieResource(R.drawable.m_gif_3);
                break;

            case M_STATE_SINGING:
                giftView.setMovieResource(R.drawable.m_gif_6);
                break;

            case M_STATE_SLEEP:
                giftView.setMovieResource(R.drawable.m_gif_7);
                break;

            case M_STATE_NERVOUS:
                giftView.setMovieResource(R.drawable.m_gif_8);
                break;

            case M_STATE_GONE:
                giftView.setVisibility(View.GONE);
                break;

            default:
                break;
        }

        if (!MState.M_STATE_GONE.equals(mState)) {
            giftView.setVisibility(View.VISIBLE);
        }
    }

    public static void openApp(String pkgName) {
        //String pkg代表包名，String download代表下载url
        final PackageManager pm = BrainActivity.getmBrainActivity().getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(pkgName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            BrainActivity.getmBrainActivity().startActivity(intent);
            LogMgr.d("FZXX", "启动 :" + pkgName);
        }
    }

    public enum MState {
        M_STATE_GONE,
        M_STATE_HEART_JUMP,
        M_STATE_ANGRY,
        M_STATE_HEART,
        M_STATE_SINGING,
        M_STATE_SLEEP,
        M_STATE_NERVOUS;
    }

    /**
     * 传递给FZXX一个开始情绪的状态
     */
    public static void startEmotion() {
        LogMgr.d("FZXX", "=== 开始情绪 ===");
        try {
            if (BrainService.getmBrainService() != null && BrainService.getmBrainService().getMService() != null) {
                BrainService.getmBrainService().getMService().handAction(START_EMOTION);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 传递给FZXX一个关闭情绪的状态
     */
    public static void stopEmotion() {
        LogMgr.d("FZXX", "=== 關閉情绪 ===");
        try {
            if (BrainService.getmBrainService() != null && BrainService.getmBrainService().getMService() != null) {
                BrainService.getmBrainService().getMService().handAction(EXIT_EMOTION);

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void handleStatus(int status) throws RemoteException {
        BrainActivity.getmBrainActivity().stopEmotion();

        switch (status) {
            case B_UI_A:
                changeMState(BrainActivity.getmBrainActivity().getMyGiftView(), MState.M_STATE_HEART_JUMP);
                BrainActivity.getmBrainActivity().stopEmotion();
                break;
            case B_UI_B:
                changeMState(BrainActivity.getmBrainActivity().getMyGiftView(), MState.M_STATE_ANGRY);
                break;
            case B_UI_C:
                changeMState(BrainActivity.getmBrainActivity().getMyGiftView(), MState.M_STATE_HEART);
                break;
            case B_UI_F:
                changeMState(BrainActivity.getmBrainActivity().getMyGiftView(), MState.M_STATE_SINGING);
                break;
            case B_UI_G:
                changeMState(BrainActivity.getmBrainActivity().getMyGiftView(), MState.M_STATE_SLEEP);
                break;
            case B_UI_H:
                changeMState(BrainActivity.getmBrainActivity().getMyGiftView(), MState.M_STATE_NERVOUS);
                break;

            case B_GAME_A:
                if (Utils.isAppInstalled(BrainActivity.getmBrainActivity(), APP_GANYAZI)) {
                    Utils.openApp(BrainActivity.getmBrainActivity(), APP_GANYAZI, null, false);
                    MUtils.sIsAppBack = true;
                    MUtils.sIsVoiceAppBack = true;
                } else {
                    //发送错误
                    BrainService.getmBrainService().getMService().handAction(B_GAMEA_ERROR);
                }
                break;
            case B_GAME_B:
                if (Utils.isAppInstalled(BrainActivity.getmBrainActivity(), APP_ECHOPIANO)) {
                    Utils.openApp(BrainActivity.getmBrainActivity(), APP_ECHOPIANO, null, false);
                    MUtils.sIsAppBack = true;
                    MUtils.sIsVoiceAppBack = true;
                } else {
                    BrainService.getmBrainService().getMService().handAction(B_GAMEB_ERROR);
                }
                break;
            case B_GAME_C:
                if (Utils.isAppInstalled(BrainActivity.getmBrainActivity(), APP_LEARNEN)) {
                    Utils.openApp(BrainActivity.getmBrainActivity(), APP_LEARNEN, null, false);
                    MUtils.sIsAppBack = true;
                    MUtils.sIsVoiceAppBack = true;
                } else {
                    BrainService.getmBrainService().getMService().handAction(B_GAMEC_ERROR);
                }
                break;
            case B_GAME_D:
                if (Utils.isAppInstalled(BrainActivity.getmBrainActivity(), APP_YUEGAN)) {
                    openApp(BrainActivity.getmBrainActivity(), APP_YUEGAN, null, false, OCULUS_APP, false);
                    MUtils.sIsAppBack = true;
                    MUtils.sIsVoiceAppBack = true;
                } else {
                    BrainService.getmBrainService().getMService().handAction(B_GAMED_ERROR);
                }
            case B_GAME_E:
                if (Utils.isAppInstalled(BrainActivity.getmBrainActivity(), APP_YUEGAN)) {
                    openApp(BrainActivity.getmBrainActivity(), APP_YUEGAN, null, false, OCULUS_APP, true);
                    MUtils.sIsAppBack = true;
                    MUtils.sIsVoiceAppBack = true;
                } else {
                    BrainService.getmBrainService().getMService().handAction(B_GAMED_ERROR);
                }
                break;
            case B_SHOW_BATTERY:
                startBrainSet(FRAGMENT_ID_BATTERY);
                break;
            case B_HIDE_BATTERY:
                //发送广播停止BrainSet
                sIsYuyinControl = true;
                closeBrainSet();
                startBrainActivity(BRAIN_PKG_NAME, BRAIN_ACTIVITY_NAME);
                break;
            case B_SHOW_VOICE_PICKER_CANTTOUCH:
                startBrainSet(FRAGMENT_ID_VOLUME);
                break;
            case B_HIDE_VOICE_PICKER_CANTTOUCH:
                //发送广播停止BrainSet
                sIsYuyinControl = true;
                closeBrainSet();
                startBrainActivity(BRAIN_PKG_NAME, BRAIN_ACTIVITY_NAME);
                break;

            case B_BACK_TO_BRAIN_MAINACTIVITY:
                //发送广播停止BrainSet
                sIsYuyinControl = true;
                closeBrainSet();
                startBrainActivity(BRAIN_PKG_NAME, BRAIN_ACTIVITY_NAME);
                break;


            case B_TOAPP:
                sIsYuyinControl = true;
                break;

            case B_BACKAPP:
                sIsYuyinControl = true;
                break;

            case B_INIT_COMPLETE:
                startEmotion();
                break;

            case B_WIFI:
                startBrainSet(FRAGMENT_ID_WIFI);
                break;

            case B_OTHER_APP:

                break;


            default:

                break;
        }
    }

    private static void closeBrainSet() {
        Intent intent = new Intent();
        intent.setAction("com.abilix.brainset.closeapp");
        intent.putExtra(Utils.INTENT_PACKAGE_NAME, GlobalConfig.APP_PKGNAME_BRAINSET);
        BrainActivity.getmBrainActivity().sendBroadcast(intent);
    }


    public static void startBrainActivity(String pkgName, String activityName) {
        ComponentName componentName = new ComponentName(pkgName, activityName);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        BrainActivity.getmBrainActivity().startActivity(intent);
    }

    public static void startBrainSet(int id) {
        ComponentName componentName = new ComponentName(GlobalConfig.APP_PKGNAME_BRAINSET, BRAINSET_MANAGEFRAGMENT);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.putExtra("id", id);
        intent.putExtra("type", GlobalConfig.ROBOT_TYPE_M);
        intent.putExtra("canClick", false);
        BrainActivity.getmBrainActivity().startActivity(intent);
    }


    /**
     * 打开app 使用Intent.FLAG_ACTIVITY_NEW_TASK
     * 用于从Brain页面点击打开应用
     *
     * @param packageName
     * @param gotoWifiPage 打开设置时，是否打开wifi界面
     */
    public static void openApp(Context context, String packageName, String activityName, boolean gotoWifiPage, String extraName, boolean add) {
        LogMgr.i("Brain Utils openApp() packageName = " + packageName);
        if (!TextUtils.equals(packageName, GlobalConfig.APP_PKGNAME_BRAINSET)
                && !TextUtils.equals(packageName, GlobalConfig.APP_PKGNAME_UPDATEONLINETEST)
                && !TextUtils.equals(packageName, GlobalConfig.APP_PACKAGE_NAME_UPDATE_STM32_APK)) {
            //打开赵辉那边应用前 先关闭掉串口
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                            long time = System.currentTimeMillis();
                            while (System.currentTimeMillis() - time < 1500) {
                            }
                        }
                        LogMgr.e("关闭串口");
                        if (BrainService.getmBrainService() != null & BrainService.getmBrainService().getControlService() != null) {
                            BrainService.getmBrainService().getControlService().destorySP();
                            //启动M系列小应用的时候需要发送状态值给他们
                            if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M
                                    && BrainService.getmBrainService().getMService() != null) {
                                BrainService.getmBrainService().getMService().handAction(MUtils.B_GO_TO_APP);
                                LogMgr.d("FZXX", "handAction(MUtils.B_GO_TO_APP)");
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            //打开设置应用 如果Control循环读取串口的功能被停止，发送一条命令打开
            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                    ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0x11, (byte) 0x09, null), null, 0, 0);
        }
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            PackageManager pm = context.getApplicationContext().getPackageManager();

            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = null;
            Iterator<ResolveInfo> it = apps.iterator();
            while (it.hasNext()) {
                ri = it.next();
                LogMgr.d("==Utils==", ri.activityInfo.name);
                if (ri.activityInfo.packageName.equals(packageName) &&
                        activityName != null && ri.activityInfo.name.equals(activityName)) {
                    break;
                }
            }
            if (ri != null) {
                String pkName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (packageName.equals(GlobalConfig.APP_PKGNAME_BRAINSET) && gotoWifiPage) {
//                    forceStopApp(context, packageName);
                    intent.putExtra(GlobalConfig.BRAIN_SET_GOTO_WIFI_PAGE, gotoWifiPage);
                }

                ComponentName cn = new ComponentName(pkName, className);

//                if (!TextUtils.isEmpty(filePath)) {
//                    intent.putExtra(GlobalConfig.CHART_APP_FILE_PATH_EXTRA_NAME, filePath);
//                }
//
//                if (!TextUtils.isEmpty(pageName)) {
//                    intent.putExtra(GlobalConfig.CHART_APP_PAGE_NAME_EXTRA, pageName);
//                }

                intent.setComponent(cn);
                intent.putExtra(extraName, add);


//                if (GlobalConfig.CHART_AND_PROJECT_PROGRAM_APP_PACKAGE_NAME.equals(packageName)) {
//                    LogMgr.d("start explainer");
//                    ((BrainActivity) context).startActivityForResult(intent, BrainActivity
// .REQUEST_CODE_FOR_VJC_AND_PROGRAM_JROJECT);
//                } else {
                context.startActivity(intent);
//                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogMgr.e("getPackInfo failed for package " + packageName);
        }
    }


}

