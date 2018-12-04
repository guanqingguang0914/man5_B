package com.abilix.explainer.pushmsg;

import android.os.RemoteException;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.utils.Utils;
import com.abilix.explainer.protocol.ProtocolBuilder;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.SPUtils;


/**
 * Created by jingh on 2017/7/17.
 */

public class PushMsgResponse {

    public void TakePictureResopnse(int state) {
        byte[] requestData;
        if (state == 0) {
            byte[] data = {(byte) 0x01, (byte) 0x01};
            requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_REPONSE_CAMERA, data);
        } else {
            byte[] data = {(byte) 0x01, (byte) 0x00};
            requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_REPONSE_CAMERA, data);
        }
        try {
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void recordResponse(int state) {
        byte[] requestData;
        if (state == 0) {
            byte[] data = {(byte) 0x01, (byte) 0x00};
            requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_RECORD, data);
        } else if (state == 1) {
            byte[] data = {(byte) 0x01, (byte) 0x01};
            requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_RECORD, data);
        } else {
            byte[] data = {(byte) 0x01, (byte) 0x02};
            requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_RECORD, data);
        }
        try {
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void playSoundResponse(int state) {
        byte[] requestData;
        if (state == 0) {
            byte[] data = {(byte) 0x01, (byte) 0x00};
            requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_SOUND, data);
        } else if (state == 1) {
            byte[] data = {(byte) 0x01, (byte) 0x01};
            requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_SOUND, data);
        } else {
            byte[] data = {(byte) 0x01, (byte) 0x02};
            requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_SOUND, data);
        }
        try {
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void gyroResponse(byte[] gyroData) {
        byte[] requestData;
        byte[] data = new byte[37];
        data[0] = (byte) 0x01;
        System.arraycopy(gyroData, 0, data, 1, gyroData.length);
        requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_GYRO, data);
        try {
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void compassResponse(int state) {
        LogMgr.d("compassResponse===>");
        byte[] data = new byte[2];
        data[0]=(byte)0x01;
        if (state == 0) {
            data[1] = (byte) 0x00;
        } else if (state == 1) {
            data[1] = (byte) 0x01;
        }
        byte[] requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_COMPASS, data);
        try {
            LogMgr.d("返回指南针状态值："+ Utils.bytesToString(requestData));
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void trackLine_EnvCollect_Response(int state){
        LogMgr.d("trackLine_EnvCollect_Response===>");
        byte[] data = new byte[2];
        data[0]=(byte)0x01;
        if (state == 0) {
            data[1] = (byte) 0x00;
        } else if (state == 1) {
            data[1] = (byte) 0x01;
        }
        byte[] requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_TRACKLINE_ENV_COLLECT, data);
        try {
            LogMgr.d("返回巡线状态值："+ Utils.bytesToString(requestData));
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void trackLine_LineCollect_Response(int state){
        LogMgr.d("trackLine_LineCollect_Response===>");
        byte[] data = new byte[2];
        data[0]=(byte)0x01;
        if (state == 0) {
            data[1] = (byte) 0x00;
        } else if (state == 1) {
            data[1] = (byte) 0x01;
        }
        byte[] requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_TRACKLINE_LINE_COLLECT, data);
        try {
            LogMgr.d("返回巡线状态值："+ Utils.bytesToString(requestData));
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void trackLine_BGCollect_Response(int state){
        LogMgr.d("trackLine_BGCollect_Response===>");
        byte[] data = new byte[2];
        data[0]=(byte)0x01;
        if (state == 0) {
            data[1] = (byte) 0x00;
        } else if (state == 1) {
            data[1] = (byte) 0x01;
        }
        byte[] requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_TRACKLINE_BG_COLLECT, data);
        try {
            LogMgr.d("返回巡线状态值："+ Utils.bytesToString(requestData));
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void micVolResponse(int db) {
        LogMgr.d("micVolResponse===>");
        byte[] data = new byte[2];
        data[0] = 0x01;
        data[1] = (byte) db;
        byte[] requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_RESPONSE_MIC_VOL, data);
        try {
            LogMgr.d("返回麦克风声音分贝值："+ Utils.bytesToString(requestData));
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopExcute(){
        byte[] stop_cmd={(byte)0x00};
        byte[] requestData = ProtocolBuilder.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, ProtocolBuilder.CMD_STOP_EXCUTE, stop_cmd);
        try {
            LogMgr.d("停止elf 程序执行："+ Utils.bytesToString(requestData));
            SPUtils.request(requestData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
