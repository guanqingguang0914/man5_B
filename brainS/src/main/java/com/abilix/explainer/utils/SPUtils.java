package com.abilix.explainer.utils;

import android.os.RemoteException;

import com.abilix.control.aidl.IControl;
import com.abilix.control.aidl.IPushListener;


/**
 * @author jingh
 * @Descripton:该类为串口的唯一对外接口，提供串口的各种读写操作，如需对该类作修改，请先与我沟通
 * @date2017-3-24下午5:54:46
 */
public class SPUtils {

    private static IControl mIControl = null;

    public static void setIControl(IControl iControl) {
        mIControl = iControl;
    }

    public static void controlSkillPlayer(int state,String filePath) throws RemoteException {
        if (mIControl != null) {
            mIControl.controlSkillPlayer(state, filePath);
        } else {
            LogMgr.e("write(): mIControl == null");
        }
    }

    public static void write(byte[] data) throws RemoteException {
        if (mIControl != null) {
            mIControl.write(data);
        } else {
            LogMgr.e("write(): mIControl == null");
        }
    }

    public static void writeVice(byte[] data) throws RemoteException {
        if (mIControl != null) {
            mIControl.writeVice(data);
        } else {
            LogMgr.e("writeVice(): mIControl == null");
        }
    }

    public static byte[] request(byte[] data) throws RemoteException {
        byte[] readBuffer = null;
        if (mIControl != null) {
            readBuffer = mIControl.request(data);
        } else {
            LogMgr.e("request(): mIControl == null");
        }
        return readBuffer;
    }

    public static byte[] request(byte[] data, int timeout) throws RemoteException {
        byte[] readBuffer = null;
        if (mIControl != null) {
            readBuffer = mIControl.requestTimeout(data, timeout);
        } else {
            LogMgr.e("request(): mIControl == null");
        }
        return readBuffer;
    }

    public static void cancelRequestTimeout() throws RemoteException {
        if (mIControl != null) {
            mIControl.cancelRequestTimeout();
        } else {
            LogMgr.e("cancelRequestTimeout(): mIControl == null");
        }
    }

    public static byte[] requestVice(byte[] data) throws RemoteException {
        byte[] readBuffer = null;
        if (mIControl != null) {
            readBuffer = mIControl.requestVice(data);
        } else {
            LogMgr.e("requestVice(): mIControl == null");
        }
        return readBuffer;
    }


    public static int getRobotType() throws RemoteException {
        int robotType = -1;
        if (mIControl != null) {
            robotType = mIControl.getRobotType();
        } else {
            LogMgr.e("getRobotType(): mIControl == null");
        }
        return robotType;
    }

    public static void registerPush(IPushListener mListener,String fullClassName) throws android.os.RemoteException {
        if (mIControl != null) {
            mIControl.registerPush(mListener,fullClassName);
        } else {
            LogMgr.e("registerPush(): mIControl == null");
        }
    }

    public static void unregisterPush(IPushListener mListener,String fullClassName) throws android.os.RemoteException {
        if (mIControl != null) {
            mIControl.unregisterPush(mListener,fullClassName);
        } else {
            LogMgr.e("unregisterPush(): mIControl == null");
        }
    }

}
