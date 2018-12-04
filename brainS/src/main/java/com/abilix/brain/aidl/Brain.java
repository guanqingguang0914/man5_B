package com.abilix.brain.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Control向Brain通信的通信类。
 * @author luox
 */
public class Brain implements Parcelable {
    /***********************************
     * mCallBackMode
     * 0:代表要求brain传老协议数据给pad app不需要打包直接传
     * 1:代表要求brain传新协议数据给pad 需要进行封装
     * 2:代表要求brain显示界面 界面上显示 mSendByte中的字符串  mModeState:0表示关闭显示  1表示开启显示
     * *********************************/
    private int mCallBackMode;
    private byte[] mSendByte; //需要发送的数据
    private int mModeState; //模式状态 0 1:开启vjc 2:关闭vjc 3:开启Scratch图片预览 4：开启vjc图片预览  //寻线采集数据 6 第二对话框的是,7 第三对话框的是,8 否 ,5通知打开对话框
    //11：M轮子进入保护状态

    public int getCallBackMode() {
        return mCallBackMode;
    }

    public Brain(int callBackMode, byte[] sendByte) {
        this.mCallBackMode = callBackMode;
        this.mSendByte = sendByte;
    }

    public void setCallBackMode(int callBackMode) {
        this.mCallBackMode = callBackMode;
    }

    public byte[] getSendByte() {
        return mSendByte;
    }

    public void setSendByte(byte[] mbyte) {
        this.mSendByte = mbyte;
    }


    public int getModeState() {
        return this.mModeState;
    }

    public void setModeState(int state) {
        this.mModeState = state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallBackMode);
        if (mSendByte != null) {
            dest.writeInt(mSendByte.length);
            dest.writeByteArray(mSendByte);
        }
        dest.writeInt(mModeState);
    }

    public static final Parcelable.Creator<Brain> CREATOR = new Creator<Brain>() {

        @Override
        public Brain[] newArray(int size) {
            return new Brain[size];
        }

        @Override
        public Brain createFromParcel(Parcel source) {
            return new Brain(source);
        }
    };

    private Brain(Parcel source) {
        mCallBackMode = source.readInt();
        mSendByte = new byte[source.readInt()];
        source.readByteArray(mSendByte);
        mModeState = source.readInt();
    }

}
