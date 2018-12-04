package com.abilix.explainer.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abilix.explainer.ExplainMessage;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.view.MainActivity;

public class ControlReceiveBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogMgr.d("receive broadcast action::" + intent.getAction());
        switch (intent.getAction()) {
            /*case Intent.ACTION_POWER_CONNECTED:
                LogMgr.d("charge state::connected");
                SP.chargeProtect(true);
                break;

            case Intent.ACTION_POWER_DISCONNECTED:
                LogMgr.d("charge state::disconnected");
                SP.chargeProtect(false);
                break;
            case Intent.ACTION_BATTERY_CHANGED:
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                LogMgr.d(String.format(Locale.US, "battery state:: battery status[%d] plugged[%d]", status, plugged));
                if (status == BatteryManager.BATTERY_STATUS_CHARGING
                        || plugged == BatteryManager.BATTERY_PLUGGED_AC
                        || plugged == BatteryManager.BATTERY_PLUGGED_USB
                        || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
                    SP.chargeProtect(true);
                }
                break;*/
            case MainActivity.INTENT_ACTION_TCP_DISCONNECT:
                LogMgr.d("tcp state::disconnected");
                ExplainMessage stopMessage = new ExplainMessage();
                stopMessage.setFuciton(ExplainMessage.EXPLAIN_STOP);
                ExplainTracker.getInstance().doExplainCmd(stopMessage, null);
                MainActivity.getActivity().stopAnimation();
                //MainActivity.getActivity().finish();
                break;
            case MainActivity.INTENT_ACTION_CUSTOM_LOG:
                LogMgr.d("log state::");
                Boolean log_status = intent.getBooleanExtra(MainActivity.CUSTOM_LOG_STATUS_KEY, false);
                LogMgr.d("InstallReceiver onReceive() action = " + intent.getAction() + " log_status = " + log_status);
                if(log_status){
                    LogMgr.setLogLevel(LogMgr.VERBOSE);
                }else{
                    LogMgr.setLogLevel(LogMgr.NOLOG);
                }
                break;
        }
    }
}
