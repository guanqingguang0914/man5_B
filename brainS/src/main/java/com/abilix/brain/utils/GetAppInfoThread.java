package com.abilix.brain.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.abilix.brain.Application;
import com.abilix.brain.BrainActivity;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.data.DataProcess;
import com.abilix.brain.data.AppInfo;

/**
 * 获取机器上安装APP信息线程。
 */
public class GetAppInfoThread extends Thread {

    private static final String TAG = "GetAppInfoThread";

    public static final int FLAG_SINGLE_PACKAGE = 101;
    public static final int FLAG_ALL_ABILIX_LAUNCHER = 102;

    public static final int FLAG_ACTIVE_NOT_DELETE = 200;
    public static final int FLAG_ACTIVE_DELETE = 201;
    public static final int FLAG_PASSIVE = 202;


    private static final String APP_LIST = "applist";
    private static final String APP_TITLE = "apptitle";
    private static final String ROBOT_CLASS = "robotclass";
    private static final String PACKAGE_NAME = "packagename";
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";
    private static final String DOWN_TIME = "downtime";
    private static final String APP_LEVEL = "applevel";

    //	private BrainInfo mBrainInfo;
    private PackageInfoProvider mPakageInfoProvider;
    private int mFlag;
    private int mFlagOfState;
    private String mPackageName;
    private String mCategory;
    private PackageManager mPackageManager;

    private JSONObject appAllJson;
    private JSONArray appListJson;
    private List<AppInfo> appInfos;

    /**
     * @param flag        FLAG_SINGLE_PACKAGE/FLAG_ALL_ABILIX_LAUNCHER.
     * @param filter      FLAG_SINGLE_PACKAGE:使用此flag时，filter为单个包名；FLAG_ALL_ABILIX_LAUNCHER:使用此flag时，filter为activity的category。
     * @param flagOfState FLAG_ACTIVE_NOT_DELETE/FLAG_ACTIVE_DELETE/FLAG_PASSIVE .
     *                    flag为FLAG_SINGLE_PACKAGE时选择FLAG_ACTIVE_NOT_DELETE/FLAG_ACTIVE_DELETE表示机器人端主动返回状态改变（安装或卸载）的应用的信息。
     *                    FLAG_ACTIVE_NOT_DELETE表示发完后不删除应用，FLAG_ACTIVE_DELETE表示发送后删除应用
     *                    其余情况均应选择FLAG_PASSIVE表示是回复客户端的请求。
     * @throws Exception flag参数错误
     */
    public GetAppInfoThread(int flag, String filter, int flagOfState) throws Exception {
//		this.mBrainInfo = brainInfo;
        LogMgr.i("GetAppInfoThread() 给appstore发送应用信息 flag = " + flag + " filter = " + filter + " flagOfState = " + flagOfState);
        this.mPakageInfoProvider = new PackageInfoProvider(Application.getInstance());
        this.mFlag = flag;
        this.mFlagOfState = flagOfState;
        if (mFlag == FLAG_SINGLE_PACKAGE) {
            this.mPackageName = filter;
        } else if (mFlag == FLAG_ALL_ABILIX_LAUNCHER) {
            this.mCategory = filter;
        } else {
            throw new Exception("获取应用信息是的Flag类型错误");
        }
    }

    @Override
    public void run() {
        try {
            if (mFlag == FLAG_SINGLE_PACKAGE) {

                mPackageManager = Application.getInstance().getPackageManager();
//				ApplicationInfo applicationInfo = mPackageManager.getApplicationInfo(mPackageName, 0);
                try {
                    PackageInfo packageInfo = mPackageManager.getPackageInfo(mPackageName, PackageManager.GET_ACTIVITIES);
                    AppInfo appInfo = mPakageInfoProvider.getAppinfoFromApplicationInfo(packageInfo);
                    appInfos = new ArrayList<AppInfo>();
                    appInfos.add(appInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    appInfos = new ArrayList<AppInfo>();
                    LogMgr.e(TAG, "可能没有该应用 mPackageName = " + mPackageName);
                }

            } else if (mFlag == FLAG_ALL_ABILIX_LAUNCHER) {
                Log.d(TAG, "mFlag == FLAG_ALL_ABILIX_LAUNCHER");
                appInfos = mPakageInfoProvider.getAppInfos(GlobalConfig.APP_ABILIX_LAUNCHER);
            } else {
                LogMgr.e(TAG, "mFlag错误");
            }

            appAllJson = new JSONObject();
            appListJson = new JSONArray();
            for (int i = 0; i < appInfos.size(); i++) {
                JSONObject appInfoJson = new JSONObject();
                appInfoJson.put(APP_TITLE, appInfos.get(i).getAppLabel());
                appInfoJson.put(ROBOT_CLASS, String.valueOf(appInfos.get(i).getRobotClass()));
                appInfoJson.put(PACKAGE_NAME, appInfos.get(i).getPkgName());
                appInfoJson.put(VERSION_NAME, appInfos.get(i).getVersionName());
                appInfoJson.put(VERSION_CODE, String.valueOf(appInfos.get(i).getVersionCode()));
                appInfoJson.put(DOWN_TIME, Utils.getDateTimeFromMillisecond(appInfos.get(i).getInstallTime()));
                appInfoJson.put(APP_LEVEL, String.valueOf(appInfos.get(i).getAppLevel()));
                appListJson.put(appInfoJson);
            }
            appAllJson.put(APP_LIST, appListJson);

            String appInfosString = appAllJson.toString();
            byte[] appInfoByteArray = appInfosString.getBytes();
            LogMgr.d(TAG, "appInfoByteArray.length = " + appInfoByteArray.length + " mPackageName = " + mPackageName);

            //**********
//			String s = new String(appInfoByteArray, "UTF-8");
//			JSONObject json = new JSONObject(s);
//			LogMgr.w("json = "+json);
            //**********

            byte[] dataToSend = null;
            if (mFlagOfState == FLAG_ACTIVE_DELETE || mFlagOfState == FLAG_ACTIVE_NOT_DELETE) {
                LogMgr.i("当前为主动反馈");
                dataToSend = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE,
                        GlobalConfig.APP_STORE_OUT_CMD_1, GlobalConfig.APP_STORE_OUT_CMD_2_FEEDBACK, appInfoByteArray);
            } else if (mFlagOfState == FLAG_PASSIVE) {
                LogMgr.i("当前为被动回复");
                dataToSend = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE,
                        GlobalConfig.APP_STORE_OUT_CMD_1, GlobalConfig.APP_STORE_OUT_CMD_2_RESPONSE_APP_INFO, appInfoByteArray);
            }

            DataProcess.GetManger().sendMsg(dataToSend);


//			mBrainInfo.returnToClient2(appInfoByteArray);

        } catch (Exception e) {
            e.printStackTrace();
            LogMgr.e("GetAppInfoThread", "构造json异常 e = " + e);
        } finally {
            if (mFlag == FLAG_SINGLE_PACKAGE && mFlagOfState == FLAG_ACTIVE_DELETE && null != BrainActivity.getmBrainActivity()) {
                LogMgr.i("删除应用反馈完成，开始删除应用 mPackageName = " + mPackageName);
                BrainActivity.getmBrainActivity().deleteAPK(mPackageName);
            }
        }
    }

}
