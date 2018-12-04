package com.abilix.explainer.camera.transvedio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.abilix.brain.BrainActivity;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.ProtocolUtil;

/**
 * RTSP服务功能类
 */
public class RTSPServiceMgr {
	private static final String ACTION_START_RTSP_SERVICE = "com.abilix.learn.rtspserver.RTSP_START_SERVICE";
	private static final String ACTION_STOP_RTSP_SERVICE = "com.abilix.learn.rtspserver.RTSP_STOP_SERVICE";
    private static boolean isResgitered = false;


    public static void resgiterPhotoReceiver(BroadcastReceiver takePhotoReceiver){
        if (!isResgitered){
            BrainActivity.getmBrainActivity().registerReceiver(takePhotoReceiver,new IntentFilter("com.abilix.learn.rtspserver.takephoto.receiver"));
            isResgitered = true;
        }

    }

    public static void startRtspTakePhoto(){
        BrainActivity.getmBrainActivity().sendBroadcast(new Intent("com.abilix.learn.rtspserver.takephoto"));
    }

	public synchronized static void startRTSPService() {
		Intent intent = new Intent();
		intent.setAction(ACTION_START_RTSP_SERVICE);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// 端口
		intent.putExtra("PORT", 12554); // 端口12554

		// 分辨率
		intent.putExtra("FRAME_RATE", 25); // 每秒25帧
		intent.putExtra("BIT_RATE", 200000); // 200K/秒
		intent.putExtra("RESOLUTION_X", 320); // 水平分辨率
		intent.putExtra("RESOLUTION_Y", 240); // 纵向分辨率

		// 声音
		intent.putExtra("AUDIO_ON", false); // 声音关
		LogMgr.d("start RTSP server");
		try {
			BrainActivity.getmBrainActivity().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			LogMgr.e("未安装RTSPServer应用");
		}
		// rtsp
		// 客户端可以通过请求的url更改分辨率，如：rtsp://xxx.xxx.xxx.xxx:8086?h264=200-20-320-240
	}

	public synchronized static void stopRTSPService() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(ACTION_STOP_RTSP_SERVICE);
		LogMgr.d("stop RTSP server");
		try {
			BrainActivity.getmBrainActivity().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			LogMgr.e("未安装RTSPServer应用");
		}

	}
}
