package com.abilix.brain.utils;

import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.data.DataProcess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

/**
 * 网络状态变化广播，关机广播接收器。
 */
public class WifiStateChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiStateChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogMgr.d(TAG, "WifiStateChangeReceiver onReceive() action = " + intent.getAction());
        if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_NEW_STATE, -1);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLING:
                    LogMgr.i(TAG, "Wifi正在关闭");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    LogMgr.i(TAG, "Wifi已经关闭");
                    DataProcess.GetManger().closeAllTcpConnecting();
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    LogMgr.i(TAG, "Wifi正在打开");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    LogMgr.i(TAG, "Wifi已经打开");
                    DataProcess.GetManger().closeAllTcpConnecting();
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    LogMgr.w(TAG, "Wifi未知状态");
                    break;
                default:
                    break;
            }
        } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();
                    LogMgr.d(TAG, "连接到Wifi = " + ssid);
                    DataProcess.GetManger().closeAllTcpConnecting();
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            LogMgr.d("intent.getAction().equals(Intent.ACTION_SHUTDOWN)");
//			DataProcess.GetManger().closeAllTcpConnecting();
            BrainService.getmBrainService().setBrainInfoState(false);
            BrainService.getmBrainService().removeMessageToSendToControlFromHandler();
            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_STOP_ALL_CMD, null, null, 0, 0);
            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, null, 20, 0);
        }
    }
}
