package com.abilix.explainer.pushmsg;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.utils.Utils;
import com.abilix.control.aidl.IPushListener;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.SPUtils;
import com.abilix.explainer.view.MainActivity;

import static android.R.attr.mode;

/**
 * Created by jingh on 2017/7/17.
 */

public class PushMsgTracker {

    public static final int MSG_EXPLAIN_NO_DISPLAY = 0x00;
    public static final int MSG_EXPLAIN_DISPLAY = 0x01;
    public static final int MSG_EXPLAIN_IS_GETAI = 0x02;
    public static final int MSG_EXPLAIN_DISPLAY_GRAY = 0x03;
    public static final int MSG_EXPLAIN_BALANCE_INITIALIZING = 0x04;
    public static final int MSG_EXPLAIN_BALANCE_BALANCED = 0x05;
    public static final int MSG_EXPLAIN_RECORD = 0x06;
    public static final int MSG_EXPLAIN_CAMERA = 0x07;
    public static final int MSG_EXPLAIN_COMPASS = 0x08;
    public static final int MSG_EXPLAIN_LED = 0x09;
    public static final int MSG_EXPLAIN_SOUND = 0x10;
    public static final int MSG_REQUEST_GYRO = 0X11;
    private static PushMsgTracker instance;
    private HandlerThread mPushMsgThread;
    private PushMsgHandler mPushMsgHandler;
    private PushMsgExcutor mPushMsgExcutor;

    private PushMsgTracker() {
        LogMgr.e("PushMsgTracker Constructor");
        mPushMsgThread = new HandlerThread("PushMsgThread");
        mPushMsgThread.start();
        mPushMsgHandler = new PushMsgHandler(mPushMsgThread.getLooper());
        mPushMsgExcutor = new PushMsgExcutor(MainActivity.getActivity());
        mPushMsgHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mPushListener == null) {
                        LogMgr.e("mPushListener is null");
                    }
                    String fullClassName = PushMsgTracker.class.getCanonicalName();
                    LogMgr.d("注册上报事件监听器：" + fullClassName);
                    SPUtils.registerPush(mPushListener, fullClassName);
                } catch (RemoteException e) {
                    LogMgr.e("registerPush mPushListener error");
                    e.printStackTrace();
                }
            }
        });

    }

    public static PushMsgTracker getInstance() {
        LogMgr.e("get instance");
        if (instance == null) {
            synchronized (PushMsgTracker.class) {
                if (instance == null) {
                    instance = new PushMsgTracker();
                }
            }
        }
        return instance;
    }

   public Handler getPushMsgHandler(){
        return mPushMsgHandler;
    }

    public void startExcuteELF(String filePath) {
        BrainService.getmBrainService().exeFilePageFunc(filePath,
                GlobalConfig.CONTROL_CALLBACKMODE_PROGRAM_CMD, mode);
    }

    public void destory() {
        if (mPushMsgHandler!=null) {
            mPushMsgHandler.removeCallbacksAndMessages(null);
            if (mPushListener != null) {

                mPushMsgHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mPushMsgExcutor.stopExcute();
                            SPUtils.unregisterPush(mPushListener, this.getClass().getCanonicalName());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            mPushMsgHandler = null;
        }
        if(mPushMsgThread!=null){
            mPushMsgThread.quitSafely();
            mPushMsgThread=null;
        }

        instance = null;
    }

    private IPushListener mPushListener = new IPushListener.Stub() {
        @Override
        public void onPush(byte[] data) throws RemoteException {
            LogMgr.e("onPush Msg:" + Utils.bytesToString(data));
            try {
                Message msg = mPushMsgHandler.obtainMessage();
                msg.obj = data;
                mPushMsgHandler.sendMessage(msg);
            } catch (Exception e) {
                LogMgr.e("ERROR: mPushMsgHandler == null");
                e.printStackTrace();
            }
        }

    };


    private class PushMsgHandler extends Handler {
        public PushMsgHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            byte[] data = (byte[]) msg.obj;
            LogMgr.e("receive onPush Msg:" + Utils.bytesToString(data));
            if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x01) {
                //mHandler.sendEmptyMessage(MSG_EXPLAIN_SOUND);
                if (data[12] == 0) {
                    mPushMsgExcutor.stopPlaySound();
                } else {
                    if (data[13] == 0) {//内置音频
                        int soudNameLength = data[14];
                        byte[] soudNameByte = new byte[soudNameLength];
                        System.arraycopy(data, 15, soudNameByte, 0, soudNameLength);
                        String soudName = new String(soudNameByte);
                        LogMgr.d("音频名称：" + soudName);
                        mPushMsgExcutor.playSound(soudName+".mp3");
                    } else if (data[13] == 1) {//录音文件
                        int soudIndex = data[14];
                        LogMgr.e("录音文件序号：" + soudIndex);
                        mPushMsgExcutor.playRecord(soudIndex);
                    }
                }
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x02) {
                //  mHandler.sendEmptyMessage(MSG_EXPLAIN_RECORD);
                int index = data[12];
                int recordTime = data[13];
                LogMgr.d("录音index：" + index + "    录音时间：" + recordTime);
                mPushMsgExcutor.record(index + "", recordTime);
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x03) {
                // mHandler.sendEmptyMessage(MSG_REQUEST_GYRO);
                mPushMsgExcutor.requireGryo();
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x04) {
                //mHandler.sendEmptyMessage(MSG_EXPLAIN_CAMERA);
                String imagePath = FileUtils.getPicturePath(((int) data[12]) + "");
                LogMgr.d("收到拍照指令:" + imagePath);
                mPushMsgExcutor.takePicture(imagePath);
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x05) {
                //mHandler.sendEmptyMessage(MSG_EXPLAIN_COMPASS);
                mPushMsgExcutor.requireGryo();
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x06) {
                // mHandler.sendEmptyMessage(MSG_EXPLAIN_DISPLAY);
                if (data[12] == 0) {//显示字符
                    int dataLenth = data[13] & 0xFF;
                    byte[] contentData = new byte[dataLenth];
                    System.arraycopy(data, 14, contentData, 0, dataLenth);
                    String content = new String(contentData);
                    LogMgr.d("显示的文字内容：" + content);
                    mPushMsgExcutor.displayText(content);
                } else if (data[12] == 1) {//显示照片
                    int ImgIndex = data[13];
                    String path = FileUtils.getFilePath(FileUtils.DIR_ABILIX_PHOTO, String.valueOf(ImgIndex), FileUtils._TYPE_JPEG);
                    mPushMsgExcutor.displayImage(path);
                } else if (data[12] == 2) {//显示关闭
                    mPushMsgExcutor.stopDisplay();
                } else if (data[12] == 3) {//显示环境采集数据
                    int dataLenth = data[13];
                    byte[] contentData = new byte[dataLenth];
                    System.arraycopy(data, 14, contentData, 0, dataLenth);
                    String content = new String(contentData);
                    LogMgr.d("显示的文字内容：" + content);
                    mPushMsgExcutor.grayValues(content);
                }
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x07) {
                LogMgr.d("指南针校准提示");
                mPushMsgExcutor.adjustCompass();
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x08) {
                LogMgr.d("环境采集提示");
                mPushMsgExcutor.envCollect();
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x09) {
                LogMgr.d("线采集提示");
                mPushMsgExcutor.lineCollect();
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x0A) {
                LogMgr.d("背景采集提示");
                mPushMsgExcutor.bgCollect();
            } else if (data[5] == (byte) 0xF7 && data[6] == (byte) 0x0C) {
                LogMgr.d("获取声音检测结果");
                mPushMsgExcutor.detectMicVol();
            }

        }
    }

}
