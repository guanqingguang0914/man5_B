package com.abilix.brain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Process;
import android.text.TextUtils;

import com.abilix.brain.Matching.MathingC;
import com.abilix.brain.Matching.MathingM;
import com.abilix.brain.utils.BrainData;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.SharedPreferenceTools;
import com.abilix.brain.utils.Utils;
import com.abilix.explainer.camera.CameraStateCallBack;
import com.abilix.explainer.camera.transvedio.RTSPServiceMgr;
import com.abilix.explainer.camera.usbcamera.UsbCamera;
import com.abilix.explainer.utils.RecorderUtils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * 在TCP连接建立后由{@link BrainService}创建，维护UPD命令接收、回复线程；进行UPD命令的初步解析，分发。
 */
public class BrainInfo {
    private static final String TAG = "BrainInfo";
    // 锁 对象
    private static final Object InfoObject = new Object();

    /**
     * 发送命令缓存队列
     */
    protected static final BlockingQueue<byte[]> mInfoBlockingQueue = new LinkedBlockingQueue<byte[]>(1024);
    /**
     * 接收命令Scoket
     */
    public DatagramSocket infoDatagramSocket;
    /**
     * 发送命令Packet
     */
    protected DatagramPacket infoDatagraPacket1;
    /**
     * 接收命令Packet
     */
    private DatagramPacket infoDatagraPacket2;

//    // 控制接口
//    protected IControl infoIControl;
//    // 控制
//    protected Control infoControl;
    /**
     * 接收信息的线程
     */
    private BrainInfoReceiveThread infoBrainInfoReceiveThread;
    /**
     * 发送信息返回客户端的线程
     */
    private BrainInfoSendThread mBrainInfoSendThread;

    // ip
    public InetAddress infoInetAddress;

//    public static Context infoContext;
    /**
     * 返回客户端线程运行标志
     */
    volatile private boolean isBrainInfoSend;
    /**
     * 接收命令线程运行标志
     */
    volatile protected static boolean isBrainInfoReceive;

    public static BrainInfo mBrainInfo;

//    protected IBinder infoBinder;
    /**
     * 当前BrainInfo是否处理数据
     */
    volatile protected static boolean isActive; // 是否处理数据

    private AudioManager mAudioManager;
    /**
     * 命令重复发送三次的情况下，用于区分不同命令的标识
     */
    private byte mLastCmdOrder = (byte) 0x00;

    /**
     * 发送数据到客户端
     *
     * @param data 返回数据
     */
    public void returnDataToClient(byte[] data) {
        try {
            // 3秒,还是存不了,清空InfoBlockingQueue
            if (mInfoBlockingQueue.offer(data, 3, TimeUnit.SECONDS)) {
                startSendThread();
            } else {
                mInfoBlockingQueue.clear();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 直接返回 到客户端 不经过control处理
     *
     * @param data 已经是要发的完整数据
     */
    private synchronized void directReturnToClient(byte[] data) {
        LogMgr.i("directReturnToClient data = " + Utils.bytesToString(data));
        returnDataToClient(data);

    }

    /**
     * 接收 客户端数据
     */
    public void receiveClientData() {
        while (isBrainInfoReceive) {
            try {
                byte[] data = receiveData();
                LogMgr.d("是否处理UDP命令 = " + isActive + " data = " + Utils.bytesToString(data));
                if (isActive && data != null) {
                    handleReceivedCmd(data);
                } else {
                    continue;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理接受到的命令
     *
     * @param data
     */
    public void handleReceivedCmd(byte[] data) {
        LogMgr.i("当前应用类型 = " + BrainService.getmBrainService().getmAppTypeConnected() + " mLastCmdOrder = " + mLastCmdOrder);
        if (ProtocolUtil.isResetAppTypeCmd(data)) {
            LogMgr.e("收到设置APP类型命令 data[12] = " + data[12]);
            if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU && BrainService.getmBrainService().getmAppTypeConnected() == data[12]) {
                LogMgr.e("收到设置APP类型命令 重复!!!!!!");
                return;
            }
            byte[] returnData = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA0, (byte) 0x55, new byte[]{(byte) 0x01, (byte) 0x01});
            directReturnToClient(returnData);
            BrainService.getmBrainService().changeConnectedAppType(data[12]);
            return;
        }
        if (BrainService.getmBrainService().getmAppTypeConnected() == GlobalConfig.ABILIX_SCRATCH_APP_FLAG) {
            LogMgr.d("收到scratch命令");
            setControl(GlobalConfig.ACTION_SERVICE_MODE_SCRATCH, data);
            return;
        }
        if (isRepeatUDPCmd(data)) {
            LogMgr.i("此命令序号与之前的命令序号相同，已被处理，不需要重复执行 data[10] = " + data[10]);
            return;
        }
        LogMgr.d("开始处理UDP命令");
        switch (GlobalConfig.BRAIN_TYPE) {
            // M
            case GlobalConfig.ROBOT_TYPE_M:
                receiveM(data);
                break;
            // C
            case GlobalConfig.ROBOT_TYPE_C:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                receiveC(data);
                //receiveS(data);
                break;
            // H
            case GlobalConfig.ROBOT_TYPE_H:
            case GlobalConfig.ROBOT_TYPE_H3:
                receiveH(data);
                break;
            // F
            case GlobalConfig.ROBOT_TYPE_F:
                receiveF(data);
                break;
            // AF
            case GlobalConfig.ROBOT_TYPE_AF:
                receiveAF(data);
                break;
            case GlobalConfig.ROBOT_TYPE_C1:
                receiveC1(data);
                break;
            case GlobalConfig.ROBOT_TYPE_S:
                LogMgr.d("收到PAD端S的数据");
                receiveS(data);
                break;
            case GlobalConfig.ROBOT_TYPE_M1:
                receiveM1(data);
                break;

            case GlobalConfig.ROBOT_TYPE_U:
            case GlobalConfig.ROBOT_TYPE_U5:
                receiveU5(data);
                break;
            default:
                LogMgr.e("机器人型号异常");
                break;
        }
    }

    /**
     * 判断此命令是否是不需处理的命令序号重复的命令
     */
    private boolean isRepeatUDPCmd(byte[] data) {
        boolean result = false;

        if (data != null && data.length >= 12 && data[0] == GlobalConfig.CMD_0 && data[1] == GlobalConfig.CMD_1 && data[10] != (byte) 0x00) {
            if (data[10] == (byte) 0xFF) {
                LogMgr.e("命令序号不应该 = 0xFF");
            }
            if (mLastCmdOrder == data[10]) {
                result = true;
            } else {
                result = false;
                mLastCmdOrder = data[10];
            }
        } else {
            result = false;
        }
        return result;
    }

    private long remoteTime;
    //private static byte[] data_stop;

    //xiongxin@20171114 add start
    private BroadcastReceiver takePhotoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogMgr.i("收到Rtsp拍照返回信息");
            byte[] receiver = intent.getByteArrayExtra("pictureData");
            if (receiver != null && receiver.length >= 0) {
                readyCaptureData(receiver);
            } else LogMgr.w("收到Rtsp拍照数据为空");

        }
    };
    private byte[] packageDetail = new byte[17];
    private int picpackageLength = 1024;
    private int picpackageCount = 0;
    private long packageCrc = 0;
    private byte[] mReceiver = null;
    private List<byte[]> allPackage = new ArrayList<byte[]>();
    private Object takePhotoFeelbackLock = new Object();

    //准备分包发送的图片
    private void readyCaptureData(byte[] receiver) {
        LogMgr.d("readyCaptureData");
        packageDetail[0] = 0x04;
        packageDetail[1] = (byte) ((receiver.length & 0xFF000000) >> 24);
        packageDetail[2] = (byte) ((receiver.length & 0xFF0000) >> 16);
        packageDetail[3] = (byte) ((receiver.length & 0xFF00) >> 8);
        packageDetail[4] = (byte) ((receiver.length & 0xFF));


        packageDetail[5] = (byte) ((picpackageLength & 0xFF000000) >> 24);
        packageDetail[6] = (byte) ((picpackageLength & 0xFF0000) >> 16);
        packageDetail[7] = (byte) ((picpackageLength & 0xFF00) >> 8);
        packageDetail[8] = (byte) ((picpackageLength & 0xFF));


        LogMgr.d("picpackageCount " + (receiver.length % picpackageLength));
        if (receiver.length % picpackageLength > 0)
            picpackageCount = receiver.length / picpackageLength + 1;
        else picpackageCount = receiver.length / picpackageLength;
        packageDetail[9] = (byte) ((picpackageCount & 0xFF000000) >> 24);
        packageDetail[10] = (byte) ((picpackageCount & 0xFF0000) >> 16);
        packageDetail[11] = (byte) ((picpackageCount & 0xFF00) >> 8);
        packageDetail[12] = (byte) ((picpackageCount & 0xFF));

        packageCrc = Utils.getCRC32(receiver);
        packageDetail[13] = (byte) ((packageCrc & 0xFF000000) >> 24);
        packageDetail[14] = (byte) ((packageCrc & 0xFF0000) >> 16);
        packageDetail[15] = (byte) ((packageCrc & 0xFF00) >> 8);
        packageDetail[16] = (byte) ((packageCrc & 0xFF));

        allPackage.clear();
        for (int i = 0; i < picpackageCount; i++) {
            int onepackage = picpackageLength;
            if (i == (picpackageCount - 1)) {
                onepackage = receiver.length - picpackageLength * (picpackageCount - 1);
            }
            byte[] picpackage = new byte[7 + onepackage];
            picpackage[0] = 0x03;
            picpackage[1] = (byte) (((i + 1) & 0xFF00) >> 8);
            picpackage[2] = (byte) (((i + 1) & 0xFF));
            picpackage[3] = (byte) ((onepackage & 0xFF000000) >> 24);
            picpackage[4] = (byte) ((onepackage & 0xFF0000) >> 16);
            picpackage[5] = (byte) ((onepackage & 0xFF00) >> 8);
            picpackage[6] = (byte) ((onepackage & 0xFF));

            System.arraycopy(receiver, i * picpackageLength, picpackage, 7, onepackage);

            allPackage.add(picpackage);
        }

        byte[] returnCmd = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA4, (byte) 0x08, packageDetail);
        directReturnToClient(returnCmd);
        LogMgr.d("takephotofeelback-readyCaptureData end " + picpackageCount);
    }

    //分包发送图片到移动app端
    private void packCaptureDataToApp() {
        LogMgr.d("takephotofeelback-packCaptureDataToApp ");

        for (int i = 0; i < allPackage.size(); i++) {
            byte[] picpackage = allPackage.get(i);
            byte[] returnCmd = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA4, (byte) 0x09, picpackage);
            directReturnToClient(returnCmd);
        }

        allPackage.clear();

        LogMgr.d("takephotofeelback-packCaptureDataToApp end");
    }
    //add end

    private void receiveF(byte[] data) {
//		LogMgr.i("BrainInfo receiveF收到数据" + showDataHex(data));
        if (BrainUtils.matchingVideoArrayM(data) == BrainUtils.VIDEO) {
            MathingM.sendVideo(data);
        } else if (BrainUtils.matchingWindowArrayM(data) == BrainUtils.WINDOW) {
            MathingM.sendWindowM(data);
        } else {
            if ((data[0] & 0xff) == 0xAA && (data[1] & 0xff) == 0x55) {
                if (((data[4] & 0xff) == 0x04 && (data[5] & 0xff) == 0x23 && (data[6] & 0xff) == 0x05)) {
                    if (System.currentTimeMillis() - remoteTime > 10) {
//						BrainService.getmBrainService().sendMessageToControl(0, data, null,1, 0);
                        setControl(0, data);
                        remoteTime = System.currentTimeMillis();
                    }
                } else if ((data[5] & 0xff) == 0x20 && (data[6] & 0xff) == 0x05) {//新协议视频传输
                    MathingM.sendVideoNew(data);
                } else if ((data[5] & 0xff) == 0x20 && (data[6] & 0xff) == 0x06) {//新协议划线
                    MathingM.sendWindowNew(data);
                } else {
//					BrainService.getmBrainService().sendMessageToControl(0, data, null,1, 0);
                    setControl(0, data);
                }
            }
        }
    }

    private void receiveAF(byte[] data) {
//		LogMgr.i("BrainInfo receiveF收到数据" + showDataHex(data));
        if (BrainUtils.matchingVideoArrayM(data) == BrainUtils.VIDEO) {
            MathingM.sendVideo(data);
        } else if (BrainUtils.matchingWindowArrayM(data) == BrainUtils.WINDOW) {
            MathingM.sendWindowM(data);
        } else {
            if ((data[0] & 0xff) == 0xAA && (data[1] & 0xff) == 0x55) {//所有的连续发送数据都加时间限制
                if (((data[4] & 0xff) == 0x06 && (data[5] & 0xff) == 0xA3 && (data[6] & 0xff) == 0x87)) {
                    if (System.currentTimeMillis() - remoteTime > 10) {
//						BrainService.getmBrainService().sendMessageToControl(0, data, null,1, 0);
                        setControl(0, data);
                        remoteTime = System.currentTimeMillis();
                    }
                } else if ((data[5] & 0xff) == 0x20 && (data[6] & 0xff) == 0x05) {//新协议视频传输
                    MathingM.sendVideoNew(data);
                } else if ((data[5] & 0xff) == 0x20 && (data[6] & 0xff) == 0x06) {//新协议划线
                    MathingM.sendWindowNew(data);
                } else {
//					BrainService.getmBrainService().sendMessageToControl(0, data, null,1, 0);
                    setControl(0, data);
                }
            }
        }
    }

    /**
     * 接收数据 阻塞接收
     */
    private byte[] receiveData() {
        try {
            LogMgr.d("infoDatagraPacket2 is null:" + (infoDatagraPacket2 == null)
                    + " infoDatagramSocket is null:" + (infoDatagramSocket == null));
            if (infoDatagramSocket != null) {
                infoDatagramSocket.receive(infoDatagraPacket2);
                byte[] data = Arrays.copyOfRange(infoDatagraPacket2.getData(),
                        0, infoDatagraPacket2.getLength());
                return data;
            } else {
                if (BrainService.mSocket != null) {
                    mBrainInfo.infoDatagramSocket = BrainService.mSocket;
                }
                if (infoDatagramSocket != null) {
                    infoDatagramSocket.receive(infoDatagraPacket2);
                    byte[] data = Arrays.copyOfRange(
                            infoDatagraPacket2.getData(), 0,
                            infoDatagraPacket2.getLength());
                    return data;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 接收数据 Thread
     *
     * @author luox
     */
    public class BrainInfoReceiveThread extends Thread {
        @Override
        public void run() {
            try {
                receiveClientData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送数据 Thread
     *
     * @author luox
     */
    public class BrainInfoSendThread extends Thread {
        @Override
        public void run() {
            try {
                LogMgr.d("BrainInfoSendThread 开始执行");
                while (isBrainInfoSend) {
                    byte[] data = mInfoBlockingQueue.take();
                    if (data != null) {
                        LogMgr.i("返回客户端数据 = " + Utils.bytesToString(data));
                        infoDatagraPacket1.setData(data);
                        infoDatagramSocket.send(infoDatagraPacket1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogMgr.i("返回客户端数据 Exception " + e.getMessage());
            }
        }
    }

    /**
     * 开始 接收数据 Thread
     */
    public void startReceiveThread() {
        isBrainInfoReceive = true;
        if (infoBrainInfoReceiveThread == null) {
            infoBrainInfoReceiveThread = new BrainInfoReceiveThread();
            if (!infoBrainInfoReceiveThread.isAlive()) {
                infoBrainInfoReceiveThread.start();
            }
        }
    }

    /**
     * 开始 发送数据 Thread
     */
    public void startSendThread() {
        LogMgr.d(TAG, "startSendThread");
        isBrainInfoSend = true;
        if (mBrainInfoSendThread == null) {
            mBrainInfoSendThread = new BrainInfoSendThread();
            if (!mBrainInfoSendThread.isAlive()) {
                mBrainInfoSendThread.start();
            }
        }
    }

    /**
     * 关闭 发送Thread
     */
    public void stopSendThread() {
        isBrainInfoSend = false;
        if (mBrainInfoSendThread != null) {
            // mBrainInfoSendThread.interrupt();
            mBrainInfoSendThread = null;
        }
    }

    /**
     * 关闭 接收Thread
     */
    public void stopReceiveThread() {
        isBrainInfoReceive = false;
        if (infoBrainInfoReceiveThread != null) {
            // socketClose();
            infoBrainInfoReceiveThread = null;
        }
    }

//    /**
//     * infoDatagramSocket close
//     */
//    protected void socketClose() {
//        if (infoDatagramSocket != null) {
//            infoDatagramSocket.close();
//            infoDatagramSocket = null;
//        }
//    }

    /**
     * M 接收
     *
     * @param data
     */
    private void receiveM(byte[] data) {
        try {
            // for (int i = 0; i < data.length; i++) {
            // Log.e("test m", "接收ipad:data[" + i + "]:" + data[i]);
            // }
            LogMgr.e("m 接收ipad:" + Utils.bytesToString(data));
            // 视频
            if (BrainUtils.matchingVideoArrayM(data) == BrainUtils.VIDEO) {
                MathingM.sendVideo(data);
            }// 显示窗
            //jingh add RTSP视频传输
            else if ((data[5] == (byte) 0x02 && data[6] == (byte) 0x07) || (data[5] == (byte) 0x20 && data[6] == (byte) 0x05)) {
                if (data[11] == 1) {
                    LogMgr.d("开启RTSP视频传输");
                    RTSPServiceMgr.startRTSPService();
                } else if (data[11] == 0) {
                    LogMgr.d("关闭RTSP视频传输");
                    RTSPServiceMgr.stopRTSPService();
                } else if (data[11] == 2) {
                    LogMgr.d("RTSP拍照");
                    RTSPServiceMgr.resgiterPhotoReceiver(takePhotoReceiver);
                    RTSPServiceMgr.startRtspTakePhoto();
                }
            } else if (BrainUtils.matchingWindowArrayM(data) == BrainUtils.WINDOW) {
                MathingM.sendWindowM(data);
            } else {
                filterCmdProcess(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * C 接收
     *
     * @param data
     */
    private void receiveC(byte[] data) {
        try {
            // for (int i = 0; i < data.length; i++) {
            // Log.e("test c", "data[" + i + "]:" + data[i]);
            // }
            LogMgr.e("c 接收ipad:" + Utils.bytesToString(data));
            // 视频
            if (BrainUtils.matchingVideoArrayC(data) == BrainUtils.VIDEO) {
                MathingC.sendVideo(data);
            }
            // RTSP视频传输
            else if (data[5] == (byte) 0x20 && data[6] == (byte) 0x05) {
                if (data[11] == 1) {
                    LogMgr.d("开启RTSP视频传输");
                    RTSPServiceMgr.startRTSPService();
                } else if (data[11] == 0) {
                    LogMgr.d("关闭RTSP视频传输");
                    RTSPServiceMgr.stopRTSPService();
                }
                //xiongxin20171114 add start
                else if (data[11] == 2) {
                    LogMgr.d("takephotofeelback-RTSP拍照传输");
                    ((UsbCamera) UsbCamera.create()).addCaptureCallback(new UsbCamera.CaptureCallback() {
                        @Override
                        public void onCapture(byte[] data) {

                            LogMgr.i("takephotofeelback-收到Rtsp拍照数据 : 长度 = " + data.length + " data = " + Utils.bytesToString(data));
                            if (data != null && data.length > 0) {
                                synchronized (takePhotoFeelbackLock) {
                                    readyCaptureData(data);
                                    try {
                                        LogMgr.w("takephotofeelback-thread id = " + Thread.currentThread().getId() + " wait feel back start");
                                        takePhotoFeelbackLock.wait(1000);
                                        LogMgr.w("takephotofeelback-wait feel back end");
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    packCaptureDataToApp();
                                }

                            } else {
                                LogMgr.w("收到Rtsp拍照数据为空");
                            }
                            ((UsbCamera) UsbCamera.create()).addCaptureCallback(null);
                        }
                    });
                    UsbCamera.create().takePicture(Application.getInstance(), "/sdcard/rtsp-capture.jpg", new CameraStateCallBack() {
                        @Override
                        public void onState(int state) {

                        }
                    });
                } else if (data[11] == 3) {
                    LogMgr.d("takephotofeelback-拍照ack");
                    synchronized (takePhotoFeelbackLock) {
                        LogMgr.w("takephotofeelback-thread id = " + Thread.currentThread().getId() + " notify");
                        takePhotoFeelbackLock.notify();
                    }

                }
                //add end
            }
            // 显示窗
            else if (BrainUtils.matchingWindowArrayC(data) == BrainUtils.WINDOW) {
                MathingC.sendWindowC(data);
            }// 显示窗 清空
            else if (BrainUtils.matchingWindowArrayClearC(data) == BrainUtils.WINDOW_CLEAR) {
                MathingC.sendWindowC(data);
            } else if (ProtocolUtil.isGetElectricityCmd(data)) {
                handleGetElectricityCmd(data);
            }// 播放新音频
           /* else if((data[5] & 0xff) == 0x20 && (data[6] & 0xff) == 0x04){
                MathingC.sendMediaPlay(data);
            }*/
            else if (data.length > 12 && data[5] == (byte) 0x22 && data[6] == (byte) 0x01 && data[12] == (byte) 0x03) {
                LogMgr.e("显示编程图片");
                MathingC.displayPic(data);
            } else {
                filterCmdProcess(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveS(byte[] data) {
        try {
            LogMgr.i("s 接收ipad:" + Utils.bytesToString(data));
            // 视频
            if ((data[5] & 0xff) == 0x21 && (data[6] & 0xff) == 0x01) {
                if ((data[11] & 0xff) == 0x01) {
                    //打开摄像头
                    MathingC.sendVideo(data);
                } else if ((data[11] & 0xff) == 0x02) {
                    //关闭摄像头
                    MathingC.closeVideo(data);
                }
            } else if (data[5] == (byte) 0x20 && data[6] == (byte) 0x05) {
                if (data[11] == 1) {
                    LogMgr.d("开启RTSP视频传输");
                    RTSPServiceMgr.startRTSPService();
                } else if (data[11] == 0) {
                    LogMgr.d("关闭RTSP视频传输");
                    RTSPServiceMgr.stopRTSPService();
                }
            } else if ((data[5] & 0xff) == 0x22 && (data[6] & 0xff) == 0x08) {
//                USBVideo.GetManger(BrainActivity.getmBrainActivity(), null,
//                        null).setBrightnessS(data[11] & 0xff);
//                USBVideo.GetManger(BrainActivity.getmBrainActivity(), null,
//                        null).setBrightnessS(data[11] & 0xff);
                UsbCamera.create().setBrightnessS(Application.getInstance(), data[11] & 0xff, mCameraStateCallBack);
                //打开显示窗
            } else if (BrainUtils.matchingWindowArrayC(data) == BrainUtils.WINDOW) {
                MathingC.sendWindowC(data);
            }// 显示窗 清空
            else if (BrainUtils.matchingWindowArrayClearC(data) == BrainUtils.WINDOW_CLEAR) {
                MathingC.sendWindowC(data);
            } else {
                filterCmdProcess(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


		/*if ((data[0] & 0xff) == 0xaa && (data[1] & 0xff) == 0x55
                && (data[4] & 0xff) == 0x04 && (data[5] & 0xff) == 0x05) {
			if (System.currentTimeMillis() - remoteTime > 4) {
				BrainService.getmBrainService().sendMessageToControl(0, data,
						null, 1, 0);
				remoteTime = System.currentTimeMillis();
			}
		} else {
			BrainService.getmBrainService().sendMessageToControl(0, data, null,
					1, 0);
		}*/
        //LogMgr.e("s 接收ipad:" + Arrays.toString(data));
    /*	if((data[5] & 0xff) == 0x21 && (data[6] & 0xff) == 0x01){
			MathingC.sendVideo(data);
		}*/
		/*LogMgr.e("s 接收ipad:" + Utils.bytesToString(data, data.length));
		if((data[5] & 0xff) == 0x21 && (data[6] & 0xff) == 0x01){
			if((data[11] & 0xff) == 0x01){
				//打开摄像头
				MathingC.sendVideo(data);
			}else if((data[11] & 0xff) == 0x02){
				//关闭摄像头
				MathingC.closeVideo(data);
			}

		}*/
		/*else if((data[5] & 0xff) == 0x02 && (data[6] & 0xff) == 0x01){
			//前进
			byte[] move_data = ProtocolUtil.buildProtocol((byte)0x05,
					(byte)0x02,
					(byte)0x01,
					null);
			BrainService.getmBrainService().sendMessageToControl(0, move_data, null,
					1, 0);
		}else if((data[5] & 0xff) == 0x02 && (data[6] & 0xff) == 0x07){
			//停止
			byte[] stop_data = ProtocolUtil.buildProtocol((byte)0x05,
					(byte)0x02,
					(byte)0x07,
					null);
			BrainService.getmBrainService().sendMessageToControl(0, stop_data, null,
					1, 0);
		}else if((data[5] & 0xff) == 0x21 && (data[6] & 0xff) == 0x02){
			//超声波
			byte[] data_ul = { (byte)0xFE, 0x68, 'Z', 0, 0, 0x08, (byte)0xFF,
					(byte)0xFF, (byte)200, 0x04, 0x02, 0x24, 0x02, 0,
					(byte)0xAA, 0x16 };
			data_ul[13] = Utils.check(data_ul);
			BrainService.getmBrainService().sendMessageToControl(0, data_ul, null,
					1, 0);
			// 显示窗
		}else if(){


		}else{
			BrainService.getmBrainService().sendMessageToControl(0, data, null,
					1, 0);
		}*/


    }

    private CameraStateCallBack mCameraStateCallBack = new CameraStateCallBack() {
        @Override
        public void onState(int state) {
            com.abilix.explainer.utils.LogMgr.d("相机状态回调：" + state);
        }
    };

    /**
     * C1 接收
     *
     * @param data
     */
    private void receiveC1(byte[] data) {
        try {
            // for (int i = 0; i < data.length; i++) {
            // Log.e("test c", "data[" + i + "]:" + data[i]);
            // }
            LogMgr.i("c1 接收ipad:" + Arrays.toString(data));
            // 视频
            if (BrainUtils.matchingVideoArrayC(data) == BrainUtils.VIDEO) {
                MathingC.sendVideo(data);
            }// 显示窗
            else if (BrainUtils.matchingWindowArrayC(data) == BrainUtils.WINDOW) {
                MathingC.sendWindowC(data);
            }// 显示窗 清空
            else if (BrainUtils.matchingWindowArrayClearC(data) == BrainUtils.WINDOW_CLEAR) {
                MathingC.sendWindowC(data);
            } else {
                filterCmdProcess(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * M1 接收
     *
     * @param data
     */
    private void receiveM1(byte[] data) {
        try {
            // for (int i = 0; i < data.length; i++) {
            // Log.e("test c", "data[" + i + "]:" + data[i]);
            // }
            LogMgr.i("m1 接收ipad:" + Arrays.toString(data));
            // 视频
            if (BrainUtils.matchingVideoArrayM(data) == BrainUtils.VIDEO) {
                MathingM.sendVideo(data);
            }// 显示窗
            else if (BrainUtils.matchingWindowArrayM(data) == BrainUtils.WINDOW) {
                MathingM.sendWindowM(data);
            } else {
                filterCmdProcess(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * U5 接收
     *
     * @param data
     */
    private void receiveU5(byte[] data) {
        try {
            // for (int i = 0; i < data.length; i++) {
            // Log.e("test c", "data[" + i + "]:" + data[i]);
            // }
            LogMgr.i("u5 接收ipad:" + Arrays.toString(data));
//            // 视频
//            if (BrainUtils.matchingVideoArrayM(data) == BrainUtils.VIDEO) {
//                MathingM.sendVideo(data);
//            }// 显示窗
//            else if (BrainUtils.matchingWindowArrayM(data) == BrainUtils.WINDOW) {
//                MathingM.sendWindowM(data);
//            }  else {
            if (data[5] == (byte) 0x20 && data[6] == (byte) 0x05) {
                if (data[11] == 1) {
                    LogMgr.d("开启RTSP视频传输");
                    RTSPServiceMgr.startRTSPService();
                } else if (data[11] == 0) {
                    LogMgr.d("关闭RTSP视频传输");
                    RTSPServiceMgr.stopRTSPService();
                }
            } else {
                filterCmdProcess(data);
            }


//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * H 接收
     *
     * @param data
     */
    private void receiveH(byte[] data) {
        try {
            // for (int i = 0; i < data.length; i++) {
            // Log.e("test h", "data[" + i + "]:" + data[i]);
            // }
            // 视频
            if (BrainUtils.matchingVideoArrayM(data) == BrainUtils.VIDEO) {
                MathingM.sendVideo(data);
            }// 显示窗
            else if (BrainUtils.matchingWindowArrayM(data) == BrainUtils.WINDOW) {

                MathingM.sendWindowM(data);
            } else if (ProtocolUtil.isScreenTestCmd(data)) {
                handleScreenTestCmd(data);
            } else if (ProtocolUtil.isGetElectricityCmd(data)) {
                handleGetElectricityCmd(data);
            } else if (data[5] == (byte) 0x20 && data[6] == (byte) 0x05) {
                if (data[11] == 1) {
                    LogMgr.d("开启RTSP视频传输");
                    RTSPServiceMgr.startRTSPService();
                } else if (data[11] == 0) {
                    LogMgr.d("关闭RTSP视频传输");
                    RTSPServiceMgr.stopRTSPService();
                }
            } else if ((data[5] & 0xff) == 0x21 && (data[6] & 0xff) == 0x01) {
                if ((data[11] & 0xff) == 0x01) {
                    //打开摄像头
                    MathingC.sendVideo(data);
                } else if ((data[11] & 0xff) == 0x02) {
                    //关闭摄像头
                    MathingC.closeVideo(data);
                }
            }
            //xiongxin@20171121 add start
            else if (data[5] == (byte) 0x20 && data[6] == (byte) 0x0E) {
                LogMgr.d("H5为CREATOR模块固定舵机");
                setControl(0, data);
            }
            //add end
            else {
                filterCmdProcess(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理获取电量命令
     *
     * @param data
     */
    private void handleGetElectricityCmd(byte[] data) {
        int voltage = SharedPreferenceTools.getInt(BrainService.getmBrainService(), SharedPreferenceTools.SHAREDPREFERENCE_KEY_VOLTAGE, 0);
        int level = SharedPreferenceTools.getInt(BrainService.getmBrainService(), SharedPreferenceTools.SHAREDPREFERENCE_KEY_ELECTRICITY, 0);
        byte[] returnData = new byte[4];
        returnData[0] = (byte) ((voltage >> 8) & 0xFF);
        returnData[1] = (byte) (voltage & 0xFF);
        returnData[2] = (byte) ((level >> 8) & 0xFF);
        returnData[3] = (byte) (level & 0xFF);
        byte[] returnCmd = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE,
                GlobalConfig.SELF_CHECK_OUT_CMD_1, GlobalConfig.SELF_CHECK_OUT_CMD_2_BATTERY, returnData);
        directReturnToClient(returnCmd);
    }


    /**
     * 处理屏幕自检命令
     *
     * @param data
     */
    private void handleScreenTestCmd(byte[] data) {
        final byte SCREEN_TEST_TYPE_COLOR_CAST = (byte) 0x01;
        final byte SCREEN_TEST_TYPE_COLOR_LIGHT = (byte) 0x02;
        final byte SCREEN_TEST_TYPE_COLOR_DARK = (byte) 0x03;
        final byte SCREEN_TEST_TYPE_COLOR_QUIT = (byte) 0x04;
        final byte SCREEN_TEST_COLOR_RED = (byte) 0x01;
        final byte SCREEN_TEST_COLOR_GREEN = (byte) 0x02;
        final byte SCREEN_TEST_COLOR_BLUE = (byte) 0x03;
        final byte SCREEN_TEST_COLOR_WHITE = (byte) 0x04;
        final byte SCREEN_TEST_COLOR_BLACK = (byte) 0x05;
        int color = R.color.TRANSPARENT, state = 1;
        if (data[11] == SCREEN_TEST_TYPE_COLOR_CAST) {
            if (data[12] == SCREEN_TEST_COLOR_RED) {
                color = R.color.RED;
            } else if (data[12] == SCREEN_TEST_COLOR_GREEN) {
                color = R.color.GREEN;
            } else if (data[12] == SCREEN_TEST_COLOR_BLUE) {
                color = R.color.BLUE;
            } else if (data[12] == SCREEN_TEST_COLOR_WHITE) {
                color = R.color.WHITE;
            } else if (data[12] == SCREEN_TEST_COLOR_BLACK) {
                color = R.color.BLACK;
            } else {
                return;
            }
        } else if (data[11] == SCREEN_TEST_TYPE_COLOR_LIGHT) {
            color = R.color.BLACK;
        } else if (data[11] == SCREEN_TEST_TYPE_COLOR_DARK) {
            color = R.color.WHITE;
        } else if (data[11] == SCREEN_TEST_TYPE_COLOR_QUIT) {
            state = 0;
        } else {
            return;
        }
        BrainActivity.getmBrainActivity().screenTest(state, color);
    }

    private void handleSkillplayerCmd(byte[] data) {
        // 此处的fileName 的格式为 文件夹名\动作文件名&音频文件名
        String fileName = BrainUtils.getFileNameFromCmd(data);
        LogMgr.d(TAG, "fileName = " + fileName);
        String filePath = fileName.split("\\\\")[0]; // filePath为文件夹名
        LogMgr.d(TAG, "filePath = " + filePath);
        File file = new File(BrainUtils.ABILIX_SKILLPLAYER_PATH, filePath);
        if (file.exists()) {
            LogMgr.d(TAG, "receiveAll() isSkillPlayerCmd 文件存在 fileName = "
                    + filePath);
            // 文件存在时，给客户端发送回应在Control执行 然后开始播放
            // byte[] returnData = new byte[5];
            // returnData[0] = (byte) 0x00;
            // byte[] returnCmd = DataProcess.GetManger().buildProtocol(
            // (byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA2, (byte) 0x01,
            // returnData);
            byte[] downData = new byte[data.length - 12];
            System.arraycopy(data, 11, downData, 0, downData.length);
            // receiveAll(data);
            // 下发信息到Control
            // infoControl.setCallBackMode(5);
            // infoControl.setSendByte(downData);
            // infoIControl.ControlInterface(infoControl);
            setControl(GlobalConfig.ACTION_SERVICE_MODE_SKILLPLAYER, downData);
            // 返回信息到pad 播放成功
            // directReturnToClient(returnCmd);
        } else {
            LogMgr.d(TAG, "receiveAll() isSkillPlayerCmd 文件不存在 fileName = "
                    + filePath);
            // 文件不存在时，给客户端发送回应 然后开始传输文件
            byte[] tempdata = new byte[5];
            tempdata[0] = (byte) 0x01;
            byte[] tempCmd = ProtocolUtil.buildProtocol(
                    (byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA2, (byte) 0x01,
                    tempdata);
            directReturnToClient(tempCmd);
            return;
        }
    }

    /**
     * 公用命令过滤流程
     *
     * @param data
     */
    public void filterCmdProcess(byte[] data) {
        try {
            // 录音界面
            if (BrainUtils.matchingRecordWindowArrayC(data) == BrainUtils.RECORD_WINDOW || ProtocolUtil.isKnowRobotRecordEnterWindowCmd(data)) {
                MathingC.windowRecord(data);
            }// 录音开始
            else if (BrainUtils.matchingRecordArrayC(data) == BrainUtils.RECORD || ProtocolUtil.isKnowRobotRecordBeginCmd(data)) {
                MathingC.sendRecord(data);
            } // 播放录音
            else if (BrainUtils.matchingPlayRecordArrayC(data) == BrainUtils.PLAY_RECORD || ProtocolUtil.isKnowRobotRecordPlayCmd(data)) {
                MathingC.playRecord(data);
            }
            // 停止录音
            else if (ProtocolUtil.isKnowRobotRecordStopCmd(data) && GlobalConfig.isUsingRecordStopCmd) {
                MathingC.stopRecord(data);
            }
            // END
            else if (BrainUtils.matchingENDC(data) == BrainUtils.END_C || ProtocolUtil.isKnowRobotRecordExitWindowCmd(data) || ProtocolUtil.isKnowRobotCloseCameraCmd(data)
                    || ProtocolUtil.isKnowRobotExitDrawLineCmd(data)) {
                MathingC.sendEndC();
            } else if (ProtocolUtil.isKnowRobotGetMicVolCmd(data)) {
                LogMgr.d("获取MIC声音强度的命令");
                getAndReturnMicVol();
            } else if (BrainUtils.isSkillPlayerCmd(data) && BrainUtils.isSkillPlayerPlayCmd(data)) {
                LogMgr.d("SkillPlayer播放命令");
                handleSkillplayerCmd(data);
            } else if (ProtocolUtil.isGetVolumnCmd(data)) {
                // 获取音量
                LogMgr.d("获取音量的命令");
                getAndReturnVol();
            } else if (ProtocolUtil.isSetVolumnFromSkillPlayerCmd(data)) {
                // 设置音量
                LogMgr.d("设置音量的命令");
                setVolumn(data, GlobalConfig.MAX_VOL_RECEIVED);
            } else if (ProtocolUtil.isSetVolumnFromRoboticsUCmd(data)) {
                // 设置音量
                LogMgr.d("设置音量的命令");
                setVolumn(data, GlobalConfig.MAX_VOL_RECEIVED_FROM_ROBOTICSU);
            } else if (ProtocolUtil.isMultiMediaCmd(data)) {
                LogMgr.i("收到多媒体控制命令");
                handleMultiMediaCmd(data);
            } else if (ProtocolUtil.isAnimationCmd(data)) {
                LogMgr.i("收到动画显示命令");
                handleAnimationCmd(data);
            } else if (ProtocolUtil.isSetMotorViewFromRoboticsUCmd(data)) {
                LogMgr.i("收到显示电机命令");
                handleMotorViewCmd(data);
            } else if (ProtocolUtil.isGetMacForMCmd(data)) {
                LogMgr.i("收到获取Mac命令");
                getAndReturnMac();
            } else {
                setControl(GlobalConfig.ACTION_SERVICE_MODE_OLD_PROTOCOL, data);
            }
        } catch (Exception e) {
            LogMgr.e("处理udp命令异常 data = " + data + " e = " + e);
            e.printStackTrace();
        }
    }

    /**
     * 设置音量
     *
     * @param data
     * @param receivedMaxVol 收到的可能最大的音量
     */
    private void setVolumn(byte[] data, int receivedMaxVol) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) Application.getInstance().getSystemService(Context.AUDIO_SERVICE);
        }
        //GlobalConfig.MAX_VOL = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int vol = (int) (data[11] & 0xFF);
        int vol_local = (int) (vol * 1.0 / receivedMaxVol * GlobalConfig.MAX_VOL);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol_local, 0);
        LogMgr.d(TAG, "设置音量为 vol = " + vol + " vol_local = " + vol_local);
    }

    /**
     * 获取并返回音量
     */
    private void getAndReturnVol() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) Application.getInstance()
                    .getSystemService(Context.AUDIO_SERVICE);
        }
        int currentVol = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVol > GlobalConfig.MAX_VOL) {
            currentVol = GlobalConfig.MAX_VOL;
        }
        int sendVol = (int) (currentVol * 1.0 * GlobalConfig.MAX_VOL_RECEIVED / GlobalConfig.MAX_VOL);
        byte[] currentVolByteArray = new byte[1];
        currentVolByteArray[0] = (byte) (sendVol & 0xFF);
        byte[] sendData = ProtocolUtil.buildProtocol(
                (byte) GlobalConfig.BRAIN_TYPE,
                GlobalConfig.PLAY_OUT_CMD_1,
                GlobalConfig.PLAY_OUT_CMD_2_SEND_VOL,
                currentVolByteArray);
        LogMgr.d(TAG, "获取音量为 currentVol = " + currentVol + " sendVol = " + sendVol);
        directReturnToClient(sendData);
    }

	 /**
     * 获取并返Mac地址
     */
    private void getAndReturnMac() {
        if (BrainActivity.getmBrainActivity() != null) {
            String mac = Utils.getLocalMacAddressFromWifiInfo(BrainActivity.getmBrainActivity());
            if(TextUtils.isEmpty(mac)){
                LogMgr.e("获取mac地址为空");
                return;
            }
            byte[] sendData = ProtocolUtil.buildProtocol(
                    (byte) GlobalConfig.BRAIN_TYPE,
                    GlobalConfig.KNOW_ROBOT_OUT_CMD_1,
                    GlobalConfig.KNOW_ROBOT_MAC_OUT_CMD_2,
                    Utils.getMacBytes(mac));
            LogMgr.d(TAG, "获取 mac 为 Mac = " + mac);
            LogMgr.d(TAG, "返回 mac 协议 = " + Utils.bytesToString(sendData));
            directReturnToClient(sendData);
        }

    }
	
    /**
     * 获取并返回声音强度
     */
    private synchronized void getAndReturnMicVol() {

        int micVol = (int) new RecorderUtils().getMicDbValue();
        byte[] data = new byte[1];
        data[0] = (byte) (micVol & 0xFF);
        byte[] sendData = ProtocolUtil.buildProtocol(
                (byte) GlobalConfig.BRAIN_TYPE,
                GlobalConfig.KNOW_ROBOT_OUT_CMD_1,
                GlobalConfig.KNOW_ROBOT_MIC_VOL_OUT_CMD_2,
                data);
        LogMgr.d(TAG, "获取MIC声音强度为 micVol = " + micVol);
        directReturnToClient(sendData);
    }

    /**
     * 处理动画命令
     *
     * @param data
     */
    private void handleAnimationCmd(byte[] data) {
        if (data[13] == 0x00) {
            //关闭动画
            MathingC.stopAnimation();
        } else if (data[13] == 0x01) {
            //打开动画
            MathingC.playAnimation(data);
        }
    }

    /**
     * 处理多媒体命令
     *
     * @param data
     * @throws Exception
     */
    private void handleMultiMediaCmd(byte[] data) throws Exception {
        if (ProtocolUtil.isMultiMediaAudioPlayCmd(data)) {
            LogMgr.i("收到多媒体音频播放命令");
            byte[] temp = new byte[data.length - 12 - 6];
            System.arraycopy(data, 17, temp, 0, temp.length);
            String filePath = new String(temp, "UTF-8");
            String totalPath = BrainUtils.ABILIX + "media" + filePath;
            File file = new File(totalPath);
            if (file.exists() && file.isFile()) {
                LogMgr.i("文件存在 file = " + totalPath);
                setControl(GlobalConfig.ACTION_SERVICE_MODE_OLD_PROTOCOL, data);
            } else {
                LogMgr.w("文件不存在 file = " + totalPath);
                byte[] returnData = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, GlobalConfig.MULTI_MEDIA_OUT_CMD_1,
                        GlobalConfig.MULTI_MEDIA_OUT_CMD_2_PLAY, new byte[]{(byte) 0x01});
                directReturnToClient(returnData);
            }
        } else if (ProtocolUtil.isMultiMediaPauseCmd(data)) {
            LogMgr.i("收到多媒体暂停命令");
            setControl(GlobalConfig.ACTION_SERVICE_MODE_OLD_PROTOCOL, data);
        } else if (ProtocolUtil.isMultiMediaResumeCmd(data)) {
            LogMgr.i("收到多媒体续播命令");
            setControl(GlobalConfig.ACTION_SERVICE_MODE_OLD_PROTOCOL, data);
        } else if (ProtocolUtil.isMultiMediaStopCmd(data)) {
            LogMgr.i("收到多媒体停止命令");
            setControl(GlobalConfig.ACTION_SERVICE_MODE_OLD_PROTOCOL, data);
        } else {
            LogMgr.e("收到无效的多媒体命令");
        }
    }

    /**
     * 处理显示电机页面命令
     *
     * @param data
     */
    private void handleMotorViewCmd(byte[] data) {
        if (data[12] == 0x00) {
            //删除电机教学界面并进入编程界面
            MathingC.closeMotorView(data);
        } else if (data[12] == 0x01) {
            //添加电机教学界面并进入
            MathingC.openMotorView(data);
        }
    }

    /**
     * 设置BrainInfo是否执行udp命令
     *
     * @param isActive
     */
    public void setBrainInfoActive(boolean isActive) {
        LogMgr.e("setBrainInfoActive:" + isActive);
        BrainInfo.isActive = isActive;
//        BrainInfo.isActive = true;
    }


    public boolean isActive() {
        return isActive;
    }

    /**
     * 销毁
     */
    public void destroy() {
        // infoDatagraPacket1 = null;
        // infoDatagraPacket2 = null;
        // infoDatagramSocket = null;
        stopSendThread();
        stopReceiveThread();
//        isBrainInfoSend = false;
//        isBrainInfoReceive = false;
        mInfoBlockingQueue.clear();
        mBrainInfo = null;
        System.gc();// 回收
    }

//    /**
//     * 发送广播 Service
//     *
//     * @param state = 0x02 program , 0x03 skillplayer , 0x04 scratch
//     */
//    public void SendBroadcastServer(int state) {
//        Intent intent = new Intent(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE);
//        intent.putExtra(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE_PARAM,
//                GlobalConfig.FILE_DOWNLOAD);
//        intent.putExtra("file", state);
//        infoContext.sendBroadcast(intent);
//    }

    /**
     * 设置 Control 属性 调用BrainService统一接口 发送信息至Control
     *
     * @param mode
     * @param data
     */
    protected void setControl(int mode, byte[] data) {
        LogMgr.d("setControl()");

        if (isActive) {
            BrainService.getmBrainService().sendMessageToControl(mode, data, null, 1, 0);
        } else {
            LogMgr.w("发送控制命令到Control时，已经是编程停止状态，停止发送。");
        }
    }


    /**
     * BrainInfo的构建类。
     *
     * @author luox
     */
    static public class Builder {
        private static final byte[] receivebyte = new byte[128];
        private static final byte[] sendbyte = new byte[64];

        /**
         */
        public Builder() {
//            switch (i) {
//                case 1:
            mBrainInfo = obtainBrainBrainInfo();
//                    break;
//            }
        }

        public Builder setInetAddress(InetAddress inetAddress) {
            if (mBrainInfo.infoDatagraPacket1 != null && mBrainInfo.infoDatagraPacket2 != null) {
                if (inetAddress != null) {
                    if (inetAddress.equals(mBrainInfo.infoInetAddress)) {
                        return this;
                    } else {
                        mBrainInfo.infoDatagraPacket1 = null;
                        mBrainInfo.infoDatagraPacket1 = new DatagramPacket(sendbyte, sendbyte.length, inetAddress, BrainData.DATA_PORT_KNOW);
                    }
                }
            } else {
                mBrainInfo.infoDatagraPacket1 = new DatagramPacket(sendbyte, sendbyte.length, inetAddress, BrainData.DATA_PORT_KNOW);
                mBrainInfo.infoDatagraPacket2 = new DatagramPacket(receivebyte, receivebyte.length);
            }
            mBrainInfo.infoInetAddress = inetAddress;
            return this;
        }

//        public Builder setInfoIControl(IControl infoIControl) {
//            mBrainInfo.infoIControl = infoIControl;
//            return this;
//        }

//        public Builder setInfoBinder(IBinder infoBinder) {
//            mBrainInfo.infoBinder = infoBinder;
//            return this;
//        }

        public Builder setBrainInfoActive(boolean isBrainInfoActive) {
            mBrainInfo.setBrainInfoActive(isBrainInfoActive);
            return this;
        }

//        public Builder setContext(Context context) {
//            mBrainInfo.infoControl = infoControl;
//            infoContext = context;
//            return this;
//        }

        public Builder setInfoDatagramSocket(DatagramSocket datagramSocket) {
            mBrainInfo.infoDatagramSocket = datagramSocket;
            return this;
        }

        private BrainInfo obtainBrainBrainInfo() {
            if (mBrainInfo == null) {
                synchronized (InfoObject) {
                    if (mBrainInfo == null) {
                        mBrainInfo = new BrainInfo();
                    }
                }
            }
            return mBrainInfo;
        }


        public BrainInfo create() {
            mBrainInfo.startReceiveThread();
            LogMgr.e("BrainInfo 创建成功");
            return mBrainInfo;
        }

        public static BrainInfo getBrainInfo() {
            if (mBrainInfo != null) {
                return mBrainInfo;
            }
            return null;
        }
    }

    /**
     * c组 usb 摄像头 拔出 通知 客户端
     */
    public static void sendCVideoStop() {
        LogMgr.i("sendCVideoStop");
        final byte[] data_stop;
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C) {
            data_stop = new byte[]{'V', 'I', 'D', 'E', 'O', 'S', 'T', 'O', 'P'};
        } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_S) {
            data_stop = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA5, (byte) 0x03, new byte[]{1});
        } else {
            data_stop = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA4, (byte) 0x03, new byte[]{1});
        }
        LogMgr.i("sendCVideoStop::data_stop = " + Utils.bytesToString(data_stop));
        if (mBrainInfo != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        LogMgr.i("sendCVideoStop 摄像头地址 = "
                                + mBrainInfo.infoInetAddress);
                        DatagramPacket packet = new DatagramPacket(data_stop,
                                data_stop.length, mBrainInfo.infoInetAddress,
                                BrainData.DATA_PORT_KNOW);
                        for (int i = 0; i < 5; i++) {
                            packet.setData(data_stop);
                            mBrainInfo.infoDatagramSocket.send(packet);
                            TimeUnit.MILLISECONDS.sleep(6);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }.start();
        }
    }

    /**
     * 完成录音
     */
    public void completeRecord() {
        final byte[] data;
//		final byte[] data = ProtocolUtil.buildProtocol((byte)GlobalConfig.BRAIN_TYPE,
//				GlobalConfig.KNOW_ROBOT_RECORD_OUT_CMD_1,
//				GlobalConfig.KNOW_ROBOT_RECORD_OUT_CMD_2,
//				new byte[] { (byte) 0x01 });
        LogMgr.i("GlobalConfig.BRAIN_TYPE:: = " + GlobalConfig.BRAIN_TYPE);
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C9 || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_S || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H
                || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
            data = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA4, (byte) 0x00, new byte[]{1});
        } else {
            //wangdogndong 老协议，暂时保留。不知道pad端是如何配合处理的这些字符串
            data = new byte[]{'R', 'E', 'C', 'O', 'R', 'D', 'E', 'N', 'D'};
        }
        LogMgr.i("sendcompleteRecord::data = " + Utils.bytesToString(data));
        new Thread() {
            @Override
            public void run() {
                try {
                    LogMgr.i("完成录音:" + infoInetAddress);
                    DatagramPacket packet = new DatagramPacket(data,
                            data.length, infoInetAddress,
                            BrainData.DATA_PORT_KNOW);
                    for (int i = 0; i < 5; i++) {
                        packet.setData(data);
                        infoDatagramSocket.send(packet);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ;
        }.start();
    }

    /**
     * 播放完成录音
     */
    public void playscompleteRecord() {
        final byte[] data;
//		final byte[] data = ProtocolUtil.buildProtocol((byte)
//				(GlobalConfig.BRAIN_CHILD_TYPE == -1 ? GlobalConfig.BRAIN_TYPE
//						: GlobalConfig.BRAIN_CHILD_TYPE),
//				GlobalConfig.KNOW_ROBOT_RECORD_OUT_CMD_1,
//				GlobalConfig.KNOW_ROBOT_RECORD_OUT_CMD_2,
//				new byte[] { (byte) 0x02 });
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C9 || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_S || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H
                || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
            data = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA4, (byte) 0x00, new byte[]{2});
        } else {
            data = new byte[]{'P', 'L', 'A', 'Y', 'M', 'E', 'D', 'I', 'A', 'E', 'N', 'D'};
        }
        LogMgr.i("sendplayscompleteRecord::data = " + Utils.bytesToString(data));
        new Thread() {
            @Override
            public void run() {
                try {
                    // Log.e("test", "播放完成录音:" + infoInetAddress);
                    DatagramPacket packet = new DatagramPacket(data,
                            data.length, infoInetAddress,
                            BrainData.DATA_PORT_KNOW);
                    for (int i = 0; i < 5; i++) {
                        packet.setData(data);
                        infoDatagramSocket.send(packet);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ;
        }.start();
    }


}
