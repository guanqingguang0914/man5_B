package com.abilix.brain.utils;

import com.abilix.brain.BrainActivity;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.control.FileDownloadProcesser;
import com.abilix.brain.control.ServerHeartBeatProcesser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * 应用安装成功/失败广播接收。
 */
public class InstallReceiver extends BroadcastReceiver {

    public static String INSTALL_COMPLETE_PACKAGENAME = "PackageName";
    public static final String BROADCAST_LOG_STATUS = "log_status";
    public static final String UPDATE_ONLINE_OPERATION_STATE = "State";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals("com.grandar.installservice.installComplete") || action.equals("com.grandar.installservice.installError")) {
            String packageName = intent.getStringExtra(INSTALL_COMPLETE_PACKAGENAME);
            LogMgr.d("InstallReceiver onReceive() action = " + intent.getAction() + " packageName = " + packageName);
            String filePath = FileDownloadProcesser.getInstance().getmNeedToDeleteFilePath();

            if (!TextUtils.isEmpty(filePath)) {
                LogMgr.d("待删除的filePath = " + filePath);
                if (FileUtils.deleteFile(filePath)) {
                    LogMgr.d("删除成功filePath = " + filePath);
                } else {
                    LogMgr.d("删除失败filePath = " + filePath);
                }
            } else {
                LogMgr.d("待删除的文件不存在");
            }
            ServerHeartBeatProcesser.getInstance().feedbackToAppStore(packageName, false);
        } else if (action.equals("com.abilix.change_log_status")) {
            Boolean log_status = intent.getBooleanExtra(BROADCAST_LOG_STATUS, false);
            LogMgr.d("InstallReceiver onReceive() action = " + intent.getAction() + " log_status = " + log_status);
            if (log_status) {
                LogMgr.setLogLevel(LogMgr.VERBOSE);
            } else {
                LogMgr.setLogLevel(LogMgr.NOLOG);
            }
        } else if (action.equals(GlobalConfig.ACTION_UPDATE_ONLINE_OPERATION_STATE)) {
            int update_state = intent.getIntExtra(UPDATE_ONLINE_OPERATION_STATE, -1);
            LogMgr.d("InstallReceiver onReceive() action = " + intent.getAction() + " update_state = " + update_state);
            try {
                BrainActivity.getmBrainActivity().updateOperationState(update_state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//		try {
//			if(Application.getInstance().isTcpConnecting() 
//					&& (BrainService.getmBrainService().getmAppTypeConnected() == GlobalConfig.INNER_FILE_TRANSPORT_APP_STORE
//					|| BrainService.getmBrainService().getmAppTypeConnected() == GlobalConfig.INNER_FILE_TRANSPORT_APP_FLAG) ){
//				new GetAppInfoThread(GetAppInfoThread.FLAG_SINGLE_PACKAGE, packageName, GetAppInfoThread.FLAG_ACTIVE).start();
//			}
//		} catch (Exception e) {
//			LogMgr.e("应用安装结束 给appstore反馈时异常 "+e);
//			e.printStackTrace();
//		}
    }

}
