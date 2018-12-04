package com.abilix.brain;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;

import com.abilix.brain.control.ClientFileDownloadProcesser;
import com.abilix.brain.control.ClientRunnable;
import com.abilix.brain.control.ServerHeartBeatProcesser;
import com.abilix.brain.data.DataBuffer;
import com.abilix.brain.utils.BrainData;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.SharedPreferenceTools;
import com.abilix.brain.utils.Utils;
import com.abilix.brain.utils.WifiApAdmin;
import com.abilix.brain.utils.WifiUtils;
import com.abilix.control.aidl.Control;
import com.abilix.control.aidl.IControl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


/**
 * BrainService功能实现类。
 * Created by yangz on 2017/6/29.
 */

public class BrainServiceHelper {

    private BrainService mBrainService;

    /**处理传递信息至Control的Thread*/
    private HandlerThread sendMessageToControlHandlerThread;
    /**群控对应的一些标志量 */
    public  int doingIndex = -10;
    public  int doingIndex31 = -10;
    public  int doingIndex33 = -10;
    public  int receiveIndex = 0;
    public DatagramSocket socket2;
    public DatagramPacket sendpacket;
    public static final int serverPort2 = 51000;
    public static boolean wifiControl = false;
    public static boolean stopSend = false;
    public boolean isReceiveKeepingMsg = false;

    private static String lastBinName = "";
    /**处理传递信息至Control的Handler 运行于sendMessageToControlHandlerThread*/
    private Handler mSendMessageToControlHandler = null;
    public Handler getmSendMessageToControlHandler() {
        return mSendMessageToControlHandler;
    }

    private IControl mIControl;
    public void setmIControl(IControl mIControl) {
        this.mIControl = mIControl;
    }

    public BrainServiceHelper(BrainService brainService){
        mBrainService = brainService;
        WifiManager wm = (WifiManager) mBrainService.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiAp = WifiApAdmin.getInstance(wm);
    }

    /**
     * 检测 mSendMessageToControlHandler 是否已初始化
     * @return
     */
    public boolean isSendMessageToControlHandlerInit(){
        if(mSendMessageToControlHandler != null){
            return true;
        }else{
            return false;
        }
    }

    /**7777端口是否启用标志量*/
    private boolean mIsReceiveBroadcast;
    /**7777端口监听广播线程*/
    private ReceiveBroadcastThread mReceiveBroadcastThread;
    /**接收到广播时，记录对方的ip地址*/
    private InetAddress mInetAddressForBroadcast;

    private Timer wifi_timer;
    private String string_wifiPwd;
    private String string_wifiSsid;
    private WifiApAdmin wifiAp;

    /**接收到扫码广播后是否处理*/
    private boolean isReceivingBroadcast = true;
    public void setReceivingBroadcast(boolean receivingBroadcast) {
        isReceivingBroadcast = receivingBroadcast;
    }

    /**扫码时是否返回应用端子类型*/
    private boolean isReturnChildTypeFromScan = false;
    /**扫码时是否返回应用端子类型*/
    public boolean isReturnChildTypeFromScan() {
        return isReturnChildTypeFromScan;
    }
    /**设置扫码时是否返回应用端子类型*/
    public void setReturnChildTypeFromScan(boolean isReturnChildTypeFromScan) {
        this.isReturnChildTypeFromScan = isReturnChildTypeFromScan;
    }

    /**
     * 初始化跟Control通信的线程，Handler
     */
    public void startSendMessageToControlThread() {
        if (mSendMessageToControlHandler == null) {
            sendMessageToControlHandlerThread = new HandlerThread("BrainService-HandlerThread");
            sendMessageToControlHandlerThread.start();
            mSendMessageToControlHandler = new Handler(sendMessageToControlHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    //wangdongdong 此处打印和下面的重复，所以取消
                    //LogMgr.d("发送到mSendMessageToControlHandler");
                    Control control = (Control) msg.obj;
                    if (control != null && mIControl != null) {
                        try {
                            LogMgr.i("发往Control的消息 control.getControlFuncType():"
                                    + control.getControlFuncType()
                                    + " control.getModeState():"
                                    + control.getModeState()
                                    + " control.getmCmd():" + control.getmCmd()
                                    + " control.getFileFullPath:"
                                    + control.getFileFullPath()
                                    + " control.getSendByte():"
                                    + Utils.showDataHex(control.getSendByte()));
                            mIControl.ControlInterface(control); //wangdongdong mark: this the only interface for send cmd to control
                        } catch (RemoteException e) {
                            LogMgr.e("发送消息至Control异常 control.getControlFuncType():"
                                    + control.getControlFuncType()
                                    + " control.getModeState():"
                                    + control.getModeState()
                                    + " control.getmCmd():" + control.getmCmd()
                                    + " control.getFileFullPath:"
                                    + control.getFileFullPath()
                                    + " control.getSendByte():"
                                    + Arrays.toString(control.getSendByte()));
                            e.printStackTrace();
                        }
                    } else {
                        LogMgr.e("control == null is" + (control == null)
                                + " mIControl == null is" + (mIControl == null));
                    }
                }
            };
        }
    }

    /**
     * 移除mSendMessageToControlHandler中的消息
     */
    public void removeMessageToSendToControlFromHandler() {
        if (mSendMessageToControlHandler != null) {
            mSendMessageToControlHandler.removeMessages(0);
        }
    }

    /**
     * 开启7777广播
     */
    public void startReceiveBroadcastThread() {
        LogMgr.d("BrainService startReceiveBroadcastThread()");
        mIsReceiveBroadcast = true;
        if (mReceiveBroadcastThread == null) {
            mReceiveBroadcastThread = new ReceiveBroadcastThread();
            if (!mReceiveBroadcastThread.isAlive()) {
                mReceiveBroadcastThread.start();
            }
        }
    }

    /**
     * 关闭7777广播
     */
    public void stoptReceiveBroadcastThread() {
        mIsReceiveBroadcast = false;
        if (mReceiveBroadcastThread != null) {
            mReceiveBroadcastThread.close();
            mReceiveBroadcastThread = null;
        }
    }

    /**
     * 接收客户端 广播
     *
     * @author luox
     */
    private class ReceiveBroadcastThread extends Thread {
        /**接受7777广播的socket*/
        private DatagramSocket mDatagramSocketForReceiveBroadcast;
        private DatagramPacket mPacketForReceiveBroadcast;
        int isDestroy = -1;

        @Override
        public void run() {
            LogMgr.d("BrainService ReceiveBroadcastThread run()");
            try {
                if (mDatagramSocketForReceiveBroadcast == null) {
                    mDatagramSocketForReceiveBroadcast = new DatagramSocket(BrainData.BROADCAST_PORT);
                }
                byte[] receive_data = new byte[128];
                mPacketForReceiveBroadcast = new DatagramPacket(receive_data, receive_data.length);
                while (mIsReceiveBroadcast) {
//                    LogMgr.w("等待udp广播：start receive data from 7777");
                    mDatagramSocketForReceiveBroadcast.receive(mPacketForReceiveBroadcast);
//                    LogMgr.w("接收到udp广播：receive data from 7777");
                    if (mPacketForReceiveBroadcast.getLength() > 0) {
                        String str = new String(mPacketForReceiveBroadcast.getData(), 0,
                                mPacketForReceiveBroadcast.getLength());
                        mInetAddressForBroadcast = mPacketForReceiveBroadcast.getAddress();
                        if (mInetAddressForBroadcast != null) {
//                            LogMgr.i("7777收到数据:" + new String(str.getBytes(), "UTF-8"));
//                            LogMgr.i("7777收到数据:" + Arrays.toString(receive_data));
                            byte[] data = new byte[mPacketForReceiveBroadcast.getLength()];
                            System.arraycopy(receive_data, 0, data, 0, data.length);
                            LogMgr.d("data = " + Utils.bytesToString(data));
                            if(data[0] == (byte) 0xAA && data[1] == (byte) 0x55 && data[5] == (byte)0xC0){//群空新协议
//                                LogMgr.i("群控 广播收到 doingIndex = " +doingIndex);
                                if(Application.getInstance().getisKeepGroup()){//群控模式开启，可以正常执行相应指令
                                    try{
                                        if ((data[6] & 0xff) == 32 || ((data[6] & 0xff) != 32) && doingIndex != (data[11] & 0xff)){
                                            if ((data[6] & 0xff) != 32){
                                                doingIndex = (data[11] & 0xff);
                                            }
                                            if ((data[6] & 0xff) != 31 && (data[6] & 0xff) != 32 && (data[6] & 0xff) != 33) {// 31/32/33立马执行
                                                receiveIndex = (data[12] & 0xff);
                                                int sleeptime0 = (data[13] & 0xff);
                                                LogMgr.i("群控 receiveIndex = " + receiveIndex + ";sleeptime0 = "+sleeptime0);
                                                if (sleeptime0 < 20){
                                                    sleeptime0 = 100;
                                                }
//                                                LogMgr.i("(sleeptime0 - receiveIndex) * 50 = " +(sleeptime0 - receiveIndex) * 50 );
                                                Thread.sleep((sleeptime0 - receiveIndex) * 50);// 100*50
//                                                LogMgr.i("(sleeptime0 - receiveIndex) * 50 ");
                                            }
                                            switch ((data[6] & 0xff)) {

                                                case 0:// 释放
                                                    mBrainService.mHandler.sendEmptyMessage(6);
                                                    break;
                                                case 1:// 固定
//                                                    LogMgr.e("固定");
                                                    mBrainService.mHandler.sendEmptyMessage(7);
                                                    break;
                                                case 2:// 归零
//
                                                    mBrainService.mHandler.sendEmptyMessage(5);
                                                    break;
                                                case 3:// 动作执行广播
                                                    if(Application.getInstance().getisKeepGroupLoadingComPelte()){
                                                        mBrainService.mHandler.sendEmptyMessage(10);
                                                        Thread.sleep(500);
                                                        String binName = Utils.bytesToString(data, 18, data.length - 2);
                                                        LogMgr.e("binName = " + binName);
                                                        Message msg1 = Message.obtain();
                                                        msg1.what = 0;
                                                        msg1.arg1 = 0;
                                                        msg1.obj = binName;
                                                        mBrainService.mHandler.sendMessage(msg1);
                                                        lastBinName = binName;
                                                    }
                                                    break;
                                                case 4:// 打开电量检测
                                                    mBrainService.mHandler.sendEmptyMessage(8);
                                                    break;
                                                case 5:// 关闭电量检测
                                                    mBrainService.mHandler.sendEmptyMessage(9);
                                                    break;
                                                case 6:// 停止动作
                                                    mBrainService.mHandler.sendEmptyMessage(10);
                                                    break;
                                                case 7:// 打开最大声音
//                                                    DataBuffer.StopMusic = false;
                                                    mBrainService.mHandler.sendEmptyMessage(11);
                                                    break;
                                                case 8:// 最小声音
//                                                    DataBuffer.StopMusic = true;
                                                    mBrainService.mHandler.sendEmptyMessage(12);
                                                    break;
                                                case 9:// 打开帧显示
                                                    mBrainService.mHandler.sendEmptyMessage(13);
                                                    break;
                                                case 10:// 关闭帧显示
                                                    mBrainService.mHandler.sendEmptyMessage(14);
                                                    break;
                                                case 12://设置group
                                                    mBrainService.mHandler.sendEmptyMessage(15);
                                                    break;
                                                case 13://显示组号(暂时不加)
                                                    mBrainService.mHandler.sendEmptyMessage(16);
                                                    break;
                                                case 20:// 下载bin文件前先发送bin文件信息

                                                    break;
                                                case 21:// 下载的bin文件内容

                                                    break;
                                                case 31:// 回复
//                                                    LogMgr.e("31");
                                                    if (doingIndex31 == (data[3] & 0xff)) {
                                                        continue;
                                                    }
                                                    else {
                                                        doingIndex31 = (data[3] & 0xff);
                                                    }
                                                    if (!stopSend) {
                                                        stopSend = true;
                                                        if (socket2 == null) {
                                                            socket2 = new DatagramSocket(); // 本地端口号和地址
                                                        }
                                                        final InetAddress Address = mPacketForReceiveBroadcast.getAddress();
                                                        new Thread() {// 回复消息
                                                            @Override
                                                            public void run() {
                                                                int shitNum = 1;
                                                                byte[] sendData = sendDateByte(101);
                                                                Long stopTime = System.currentTimeMillis();
                                                                while (stopSend && (System.currentTimeMillis() - stopTime) < 5000) {
                                                                    try {
                                                                        sendpacket = new DatagramPacket(sendData, sendData.length, Address,
                                                                                serverPort2); // 目标端口号和地址
                                                                        if (shitNum == 1) {
                                                                            Thread.sleep(10 + (int) (Math.random() * 200 / shitNum));
                                                                        }
                                                                        if (stopSend) {
                                                                            socket2.send(sendpacket); // 调用socket对象的send方法，发送数据
                                                                        }
                                                                        Thread.sleep(50 + (int) (Math.random() * 200 / shitNum));
                                                                        if (shitNum < 20) {
                                                                            shitNum = shitNum * 2;
                                                                        }
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                                stopSend = false;
                                                            }
                                                        }.start();
                                                    }
                                                    break;
                                                case 32:// 停止回复
//                                                    LogMgr.e("32");
                                                    stopSend = false;
                                                    break;
                                                case 33:// 发送下载文件信息 让机器人建立tcp连接接收文件
//                                                LogMgr.e("33 ;doingIndex33 == (data[3] & 0xff)) == " + (doingIndex33 == (data[3] & 0xff)) +";DataBuffer.hasclient = " + DataBuffer.hasclient
//                                                +";DataProcess.GetManger().initGetFile(data, data.length) = " + (DataProcess.GetManger().initGetFile(data, data.length)));
                                                    if (doingIndex33 == (data[3] & 0xff)){
                                                        return;
                                                    } else {
                                                        doingIndex33 = (data[3] & 0xff);
                                                    }
                                                    if (!ClientFileDownloadProcesser.hasclient && ClientFileDownloadProcesser.getInstance().initGetFile(data, data.length)) {// 初始化完成
//                                                    LogMgr.e("34");
                                                        Application.getInstance().setisKeepGroupLoadingComPelte(false);
                                                        mBrainService.mHandler.sendEmptyMessage(17);
                                                        new Thread(new ClientRunnable()).start();
                                                    }else{
                                                        mBrainService.mHandler.sendEmptyMessage(18);
                                                        Application.getInstance().setisKeepGroupLoadingComPelte(true);
                                                    }
                                                    break;
                                                case 40:// 分组动作执行
                                                    // json["group","1";"name","TBboys_End";"time",1]
                                                    String group = SharedPreferenceTools.getString(mBrainService,"group", "0");
                                                    if ("0".equals(group)) {
//                                        LogMgr.e("没有分组，不能执行分组动作");
                                                        return;
                                                    }
                                                    int len = data.length - 19;
                                                    byte[] str_name = new byte[len];
                                                    System.arraycopy(data, 18, str_name, 0, len);
                                                    if (str_name.length <= 0) {
//                                        LogMgr.e("没有分组数据");
                                                        return;
                                                    }
                                                    byte[] bs = new byte[20];
                                                    int delayTime = 0;
                                                    String fileName = "";
                                                    for (int i = 0; i < str_name.length / 20; i++) {
                                                        System.arraycopy(str_name, i * 20, bs, 0, bs.length);
                                                        int j = bs[0] & 0xff;
                                                        if (String.valueOf(j).equals(group)) {
                                                            delayTime = Utils.bytesToInt3(bs, 1);
                                                            int k = bs[4] & 0xff;
                                                            fileName = Utils.bytesToString(bs, 5, 5 + k - 1);
                                                            // serialActivity.handler.sendEmptyMessage(1);
                                                            break;
                                                        }
                                                    }
//                                                    if (lastBinName.equals("")
//                                                            || (lastBinName.equals("TBboys_End") && fileName.equals("TBboys_Start"))
//                                                            || (lastBinName.equals("TBboys_Start") && !fileName.equals("TBboys_Start"))
//                                                            || (lastBinName.equals("zero") && fileName.equals("TBboys_Start"))
//                                                            || ((!lastBinName.equals("") && !lastBinName.equals("TBboys_Start")
//                                                            && !lastBinName.equals("TBboys_End") && !lastBinName.equals("zero")) && !fileName
//                                                            .equals("TBboys_Start"))) {// 相冲突状态不执行
                                                        Message msg2 = Message.obtain();
                                                        msg2.what = 0;
                                                        msg2.arg1 = 0;
                                                        msg2.obj = fileName;
                                                        mBrainService.mHandler.sendMessageDelayed(msg2, delayTime);
                                                        lastBinName = fileName;
                                                    break;
                                                case 80:
                                                case 81:
                                                case 82:
                                                case 83:
                                                case 84:
                                                    Message msg3 = Message.obtain();
                                                    msg3.what = (data[6] & 0xff);
                                                    mBrainService.mHandler.sendMessage(msg3);
                                                    break;
                                                default:
                                                    break;
                                                    }

                                            }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }else {
                                    LogMgr.e("群控模式未开启");
                                }
                            } else if ((mPacketForReceiveBroadcast.getData().length >= GlobalConfig.PROTOCOL_MIN_LENGTH)
                                    && data[0] == (byte) 0xAA
                                    && data[1] == (byte) 0x55) {
                                // 判断长度是否正确 接收完成
                                byte[] lenBuf = new byte[2];
                                System.arraycopy(data, 2, lenBuf, 0, 2);
                                int len = BrainUtils.ProtocolByteToInt(lenBuf);
                                if (mPacketForReceiveBroadcast.getLength() >= (len + 4)) {
                                    // 进入扫码连接流程
                                    byte[] dataBuf = new byte[len];
//                                    System.arraycopy(data, 4, dataBuf, 0, len);
                                    enterScanCodeConnect(data);
                                }
                                // byte[] dataBuf = { 0x01, 0x00, 0x00, 0x00,
                                // 0x00, 0x00, 0x00, 0x00, 0x00 };
                                // enterScanCodeConnect(dataBuf);

                            } else if((data[0] & 0xff) == 0xff && (data[mPacketForReceiveBroadcast.getLength() - 1] & 0xff) == 0xAA){//群控发布的广播；
                                LogMgr.i("群控 广播收到 doingIndex = " +doingIndex);
                                if(Application.getInstance().getisKeepGroup()){//群控模式开启，可以正常执行相应指令
                                    //data[3] 新指令自加1
                                    try{
                                        if ((data[5] & 0xff) == 32 || ((data[5] & 0xff) != 32) && doingIndex != (data[3] & 0xff)){
                                            if ((data[5] & 0xff) != 32){
                                                doingIndex = (data[3] & 0xff);
                                            }
                                            if ((data[5] & 0xff) != 31 && (data[5] & 0xff) != 32 && (data[5] & 0xff) != 33) {// 31/32/33立马执行
                                                receiveIndex = (data[4] & 0xff);
                                                int sleeptime0 = (data[6] & 0xff);
                                                LogMgr.i("群控 receiveIndex = " + receiveIndex + ";sleeptime0 = "+sleeptime0);
                                                if (sleeptime0 < 20)
                                                    sleeptime0 = 100;
                                                Thread.sleep((sleeptime0 - receiveIndex) * 50);// 100*50
                                            }
                                            switch ((data[5] & 0xff)) {

                                                case 0:// 释放
                                                    mBrainService.mHandler.sendEmptyMessage(6);
                                                    break;
                                                case 1:// 固定
//                                                    LogMgr.e("固定");
                                                    mBrainService.mHandler.sendEmptyMessage(7);
                                                    break;
                                                case 2:// 归零
                                                    mBrainService.mHandler.sendEmptyMessage(5);
                                                    break;
                                                case 3:// 动作执行广播
                                                    if(Application.getInstance().getisKeepGroupLoadingComPelte()){
                                                        mBrainService.mHandler.sendEmptyMessage(10);
                                                        Thread.sleep(500);
                                                        String binName = Utils.bytesToString(data, 10, data.length - 3);
                                                        Message msg1 = Message.obtain();
                                                        msg1.what = 0;
                                                        msg1.arg1 = 0;
                                                        msg1.obj = binName;
                                                        mBrainService.mHandler.sendMessage(msg1);
                                                        lastBinName = binName;
                                                    }
                                                    break;
                                                case 4:// 打开电量检测
                                                    mBrainService.mHandler.sendEmptyMessage(8);
                                                    break;
                                                case 5:// 关闭电量检测
                                                    mBrainService.mHandler.sendEmptyMessage(9);
                                                    break;
                                                case 6:// 停止动作
                                                    mBrainService.mHandler.sendEmptyMessage(10);
                                                    break;
                                                case 7:// 打开最大声音
//                                                    DataBuffer.StopMusic = false;
                                                    mBrainService.mHandler.sendEmptyMessage(11);
                                                    break;
                                                case 8:// 最小声音
//                                                    DataBuffer.StopMusic = true;
                                                    mBrainService.mHandler.sendEmptyMessage(12);
                                                    break;
                                                case 9:// 打开帧显示
                                                    mBrainService.mHandler.sendEmptyMessage(13);
                                                    break;
                                                case 10:// 关闭帧显示
                                                    mBrainService.mHandler.sendEmptyMessage(14);
                                                    break;
                                                case 12://设置group
                                                    mBrainService.mHandler.sendEmptyMessage(15);
                                                case 20:// 下载bin文件前先发送bin文件信息

                                                    break;
                                                case 21:// 下载的bin文件内容

                                                    break;
                                                case 31:// 回复
//                                                    LogMgr.e("31");
                                                    if (doingIndex31 == (data[3] & 0xff))
                                                        continue;
                                                    else {
                                                        doingIndex31 = (data[3] & 0xff);
                                                    }
                                                    if (!stopSend) {
                                                        stopSend = true;
                                                        if (socket2 == null) {
                                                            socket2 = new DatagramSocket(); // 本地端口号和地址
                                                        }
                                                        final InetAddress Address = mPacketForReceiveBroadcast.getAddress();
                                                        new Thread() {// 回复消息
                                                            @Override
                                                            public void run() {
                                                                int shitNum = 1;
                                                                byte[] sendData = sendDateByte(101);
                                                                Long stopTime = System.currentTimeMillis();
                                                                while (stopSend && (System.currentTimeMillis() - stopTime) < 5000) {
                                                                    try {
                                                                        sendpacket = new DatagramPacket(sendData, sendData.length, Address,
                                                                                serverPort2); // 目标端口号和地址
                                                                        if (shitNum == 1) {
                                                                            Thread.sleep(10 + (int) (Math.random() * 200 / shitNum));
                                                                        }
                                                                        if (stopSend) {
                                                                            socket2.send(sendpacket); // 调用socket对象的send方法，发送数据
                                                                        }
                                                                        Thread.sleep(50 + (int) (Math.random() * 200 / shitNum));
                                                                        if (shitNum < 20) {
                                                                            shitNum = shitNum * 2;
                                                                        }
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                                stopSend = false;
                                                            }
                                                        }.start();
                                                    }
                                                    break;
                                                case 32:// 停止回复
                                                    LogMgr.i("32");
                                                    stopSend = false;
                                                    break;
                                                case 33:// 发送下载文件信息 让机器人建立tcp连接接收文件
                                                LogMgr.i("33 ;doingIndex33 == (data[3] & 0xff)) == " + (doingIndex33 == (data[3] & 0xff)) +";ClientFileDownloadProcesser.hasclient = " + ClientFileDownloadProcesser.hasclient
                                                +";DataProcess.GetManger().initGetFile(data, data.length) = " + (ClientFileDownloadProcesser.getInstance().initGetFile(data, data.length)));
                                                if (doingIndex33 == (data[3] & 0xff)){
                                                    return;
                                                } else {
                                                    doingIndex33 = (data[3] & 0xff);
                                                }
                                                if (!ClientFileDownloadProcesser.hasclient && ClientFileDownloadProcesser.getInstance().initGetFile(data, data.length)) {// 初始化完成
                                                    LogMgr.i("34");
                                                    Application.getInstance().setisKeepGroupLoadingComPelte(false);
                                                    mBrainService.mHandler.sendEmptyMessage(17);
                                                    new Thread(new ClientRunnable()).start();
                                                }else{
                                                    Application.getInstance().setisKeepGroupLoadingComPelte(true);
                                                    mBrainService.mHandler.sendEmptyMessage(18);
                                                }
                                                    break;
                                                case 40:// 分组动作执行
                                                    String group = SharedPreferenceTools.getString(mBrainService,"group", "0");
                                                    if ("0".equals(group)) {
                                                        return;
                                                    }
                                                    int len = data.length - 3 - 10 + 1;
                                                    byte[] str_name = new byte[len];
                                                    System.arraycopy(data, 10, str_name, 0, len);
                                                    if (str_name.length <= 0) {
                                                        return;
                                                    }
                                                    byte[] bs = new byte[20];
                                                    int delayTime = 0;
                                                    String fileName = "";
                                                    for (int i = 0; i < str_name.length / 20; i++) {
                                                        System.arraycopy(str_name, i * 20, bs, 0, bs.length);
                                                        int j = bs[0] & 0xff;
                                                        if (String.valueOf(j).equals(group)) {
                                                            delayTime = Utils.bytesToInt3(bs, 1);
                                                            int k = bs[4] & 0xff;
                                                            fileName = Utils.bytesToString(bs, 5, 5 + k - 1);
                                                            // serialActivity.handler.sendEmptyMessage(1);
                                                            break;
                                                        }
                                                    }
//                                                    if (lastBinName.equals("")
//                                                            || (lastBinName.equals("TBboys_End") && fileName.equals("TBboys_Start"))
//                                                            || (lastBinName.equals("TBboys_Start") && !fileName.equals("TBboys_Start"))
//                                                            || (lastBinName.equals("zero") && fileName.equals("TBboys_Start"))
//                                                            || ((!lastBinName.equals("") && !lastBinName.equals("TBboys_Start")
//                                                            && !lastBinName.equals("TBboys_End") && !lastBinName.equals("zero")) && !fileName
//                                                            .equals("TBboys_Start"))) {// 相冲突状态不执行
                                                        Message msg2 = Message.obtain();
                                                        msg2.what = 0;
                                                        msg2.arg1 = 0;
                                                        msg2.obj = fileName;
                                                        mBrainService.mHandler.sendMessageDelayed(msg2, delayTime);
                                                        lastBinName = fileName;
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }else {
                                    LogMgr.e("群控模式未开启");
                                }

                            }else {
                                // 兼容老协议
                                // 认识机器人 与 项目编程
                                if (str.trim().contains(BrainData.KNOW_ROBOT)) {
                                    isDestroy = BrainData.DATA_ONE;
                                }// VJC
                                else if (str.trim().contains(
                                        "Broadcast message")) {
                                    isDestroy = BrainData.DATA_TWO;
                                }// SCRATCH
                                else if (str.trim().contains(BrainData.SCRATCH)) {
                                    isDestroy = BrainData.DATA_THREE;
                                }// PROJECT 项目编程 下载
                                else if (str.trim().contains(BrainData.PROJECT)) {
                                    isDestroy = BrainData.DATA_FOUR;
                                } else {
                                    isDestroy = 0x06;
                                }
                                if (isDestroy != -1) {
                                    sendMessageToClient(isDestroy);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogMgr.e("7777接收客户端 广播异常e = " + e.getMessage());
            }
        }

        /**
         * 没有数据位打包
         *
         * @param cmd
         * @return
         */
        public byte[] sendDateByte(int cmd) {// 指令打包
            byte[] data0 = new byte[12]; // 发送数据
            data0[0] = (byte) 0xff;// 报头
            data0[1] = (byte) (0xff & ((12 - 3) >> 8));// 长度高位
            data0[2] = (byte) (0xff & (12 - 3));// 长度低位
            data0[3] = (byte) (0);//
            data0[4] = (byte) (0);//
            data0[5] = (byte) cmd;// 指令种类
            data0[8] = (byte) 3;// 机器人类型
            data0[11] = (byte) 0xAA;// 报尾
            data0[10] = XORcheckSend(data0, 12);
            return data0;
        }

        public void close() {
            if (mDatagramSocketForReceiveBroadcast != null) {
                mDatagramSocketForReceiveBroadcast.disconnect();
                mDatagramSocketForReceiveBroadcast.close();
                mDatagramSocketForReceiveBroadcast = null;
                mPacketForReceiveBroadcast = null;
            }
        }
    }

    public static byte XORcheckSend(byte[] buf, int len) {// 传参是完整报文,生成CRC
        if (len < 12) {
            return -1;
        }

        byte crc = buf[0];
        for (int i = 1; i <= len - 3; i++) {
            crc = (byte) (crc ^ (buf[i]));
        }
        return crc;
    }

    /**
     * 广播数据接收成功 给 客户端发送数据 进行 通信
     */
    private void sendMessageToClient(int status) {
        try {
            // if (status == 0x02) {
            // byte[] data = BrainData.I_AM.getBytes();
            // DatagramPacket packet = new DatagramPacket(data, data.length,
            // mInetAddress, BrainData.DATA_PORT_KNOW);
            // soket.send(packet);
            // LogMgr.e("test", "I_AM------->:" + BrainData.I_AM);
            // } else {
            String ip = Utils.getIpAddress();
            byte[] data = null;
            if (ip != null) {
                data = ip.getBytes();
            } else {
                ip = "193.168.43.1";
                data = ip.getBytes();
            }
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    mInetAddressForBroadcast, BrainData.DATA_PORT_KNOW);
            // if (soket != null) {
            // soket.send(packet);
            // }
            if (BrainService.mSocket != null) {
                BrainService.mSocket.send(packet);
            }
            LogMgr.e("test", "ip------->:" + ip + " mInetAddress:"
                    + mInetAddressForBroadcast.getHostAddress());
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                TimeUnit.MILLISECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switch (status) {
                case BrainData.DATA_TWO:
                    mBrainService.mHandler.sendEmptyMessage(0x100);
                    break;
                case BrainData.DATA_FOUR:
                    mBrainService.mHandler.sendEmptyMessage(0x101);
                    break;
                case BrainData.DATA_ONE:
                    mBrainService.mHandler.sendEmptyMessage(0x200);
                    break;
            }
        }
    }

    /**
     * 处理长连接建立前的广播协议
     *
     * @param data
     */
    private void enterScanCodeConnect(byte[] data) {
        LogMgr.i("enterScanCodeConnect() 7777收到的数据 = " + Utils.bytesToString(data));
        if(!isReceivingBroadcast){
            LogMgr.e("当前因为正在充电，不处理广播");
            return;
        }
        if (!ProtocolUtil.isTypeRelate(data[4])) {
            LogMgr.e("enterScanCodeConnect() 协议系列类型不匹配");
            return;
        }
        try {
            //扫码广播中第一条广播的处理
            if (data[5] == (byte) 0x00 && data[6] == (byte) 0x00) {
                LogMgr.i("enterScanCodeConnect()1 收到udp广播   命令字1:0x00 命令字2:0x00 通知路由器的名字，密码");
                String sIp = Utils.getIpAddress();
                LogMgr.i("enterScanCodeConnect()1 本地Ip::" + sIp);
                boolean isTcpConnecting;

                //获取通讯版本号
                if (data[8] >= (byte) 0x01) {
                    LogMgr.i("enterScanCodeConnect()1 包含版本号 设置返回子类型");
                    setReturnChildTypeFromScan(true);
                } else {
                    LogMgr.i("enterScanCodeConnect()1 不包含版本号 设置返回主类型");
                    setReturnChildTypeFromScan(false);
                }
                //回复广播
                if (sIp != null) {
                    isTcpConnecting = sendIpSoket(sIp);
                } else {
                    isTcpConnecting = sendIpSoket("192.168.43.1");
                }
                if (isTcpConnecting == true
                        && GlobalConfig.isLimitClientToOne == true
                        && ServerHeartBeatProcesser.getInstance().isNeedToLimitClientToOne() == true) {
                    // 当前已有TCP连接，不进行后续操作
                    LogMgr.w("enterScanCodeConnect()1 收到udp广播   命令字1:0x00 命令字2:0x00    当前已有TCP连接，不进行后续操作");
                    return;
                }
                // data.length == 12 代表直连机器人热点 data.length > 12代表告诉机器人路由器名字和密码
                if (data.length > 12) {
                    int ssid_length = Integer.valueOf(data[11]);
                    byte[] byte_wifiSsid = Arrays.copyOfRange(data, 12, 12 + ssid_length);
                    string_wifiSsid = new String(byte_wifiSsid);
                    byte[] byte_wifiPwd = Arrays.copyOfRange(data, 13 + ssid_length, data.length - 1);
                    string_wifiPwd = new String(byte_wifiPwd);
                    LogMgr.d("enterScanCodeConnect()1 收到的路由器名字 = "  + string_wifiSsid + " 路由器密码 = " + string_wifiPwd);
                    wifiAp.closeWifiAp(mBrainService);

                    if (wifi_timer != null) {
                        wifi_timer.cancel();
                        wifi_timer = null;
                    }

                    wifi_timer = new Timer();
                    LogMgr.i("enterScanCodeConnect()1 创建60秒Timer 用于连接失败时 重新创建热点");
                    TimerTask wifi_task = new TimerTask() {

                        @Override
                        public void run() {
                            LogMgr.i("enterScanCodeConnect()1 60 秒 Timer 结束 重新创建热点");
                            wifiAp.createWifiHot();
                        }
                    };
                    wifi_timer.schedule(wifi_task, 60*1000);
                    LogMgr.i("enterScanCodeConnect()1 60 秒 Timer 开始");
                    boolean wifi_state = WifiUtils.connectWifi(mBrainService, string_wifiSsid, string_wifiPwd);
                    LogMgr.i("enterScanCodeConnect()1 连接制定路由器结果 = " + wifi_state);
                }else{
                    LogMgr.i("当前为热点直连");
                }
            } else if (data[5] == (byte) 0x00 && data[6] == (byte) 0x01) {
                LogMgr.i("enterScanCodeConnect()2 收到udp广播  命令字1:0x00 命令字2:0x01 确认是否连上指定路由器");
                //获取ssid
                byte[] byte_apInfo = Arrays.copyOfRange(data, 11, data.length - 1);
                String string_apInfo = new String(byte_apInfo);
                String[] apInfo = string_apInfo.split(",");
                String ssid = apInfo[0];

                LogMgr.i("enterScanCodeConnect()2 收到的数据" + string_apInfo + " ssid = " + ssid);
                if (ssid.equals(WifiApAdmin.readSsid()) && apInfo[1].contains(WifiApAdmin.readPass())) {
                    //获取通讯版本号
                    if (Utils.isArrayAContainsArrayB(byte_apInfo, new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x01}) > 0) {
                        LogMgr.i("enterScanCodeConnect()2 包含版本号 设置返回子类型");
                        setReturnChildTypeFromScan(true);
                    } else {
                        LogMgr.i("enterScanCodeConnect()2 不包含版本号 设置返回主类型");
                        setReturnChildTypeFromScan(false);
                    }

                    boolean isTcpConnecting = false;
                    String sIp = Utils.getIpAddress();
                    LogMgr.d("enterScanCodeConnect()2 本机Ip = " + sIp);
                    if (sIp == null) {
                        LogMgr.e("enterScanCodeConnect()2 robot ip is null");
                    }
                    String[] localHostIp = sIp.split("[.]");
                    byte[] mIp = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        mIp[i] = (byte) Integer.parseInt(localHostIp[i]);
                    }
                    byte[] sendData = getSendData(mIp);
                    if (sendData[sendData.length - 1] == (byte) 0x00) {
                        isTcpConnecting = true;
                    } else if (sendData[sendData.length - 1] == (byte) 0x01) {
                        isTcpConnecting = false;
                    } else {
                        LogMgr.e("enterScanCodeConnect()2 tcp连接状态错误");
                    }
                    byte[] buff_send = ProtocolUtil.buildProtocol(
                            (byte) ProtocolUtil.getBrainTypeForScan(),
                            (byte) GlobalConfig.CMD_BROADCAST_RETURN_CMD1,
                            (byte) GlobalConfig.CMD_BROADCAST_RETURN_CMD2_2,
                            sendData);
                    DatagramPacket packet = new DatagramPacket(buff_send,
                            buff_send.length, mInetAddressForBroadcast,
                            BrainData.DATA_PORT_KNOW);
                    BrainService.mSocket.send(packet);
                    LogMgr.i("enterScanCodeConnect()2 回复udp确认命令");
                    if (isTcpConnecting == true
                            && GlobalConfig.isLimitClientToOne == true
                            && ServerHeartBeatProcesser.getInstance().isNeedToLimitClientToOne() == true) {
                        // 当前已有tcp连接
                        LogMgr.w("enterScanCodeConnect()2 收到udp广播   命令字1:0x00 命令字2:0x01    当前已有TCP连接，不进行后续操作");
                        return;
                    }

                    // if (wifi_timer != null) {
                    // wifi_timer.cancel();
                    // wifi_timer = null;
                    // }
                    // if (string_wifiSsid != null && string_wifiPwd != null) {
                    // WifiUtils.saveWifiInfo(string_wifiSsid, string_wifiPwd);
                    // }
                    // LogMgr.i("enterScanCodeConnect()2  保存当前路由器名字，密码");
                    Thread.sleep(500);
                }
            } else if (data.length > 6
                    && data[5] == GlobalConfig.CHECK_BEFORE_CONNECT_IN_CMD_1
                    && data[6] == GlobalConfig.CHECK_BEFORE_CONNECT_IN_CMD_2) {
                // 协议命令1.1.7（pad端 退出应用 进入后重连时 先发udp命令给机器人端进行确认）
                LogMgr.i("enterScanCodeConnect()3 协议命令1.1.7（pad端 退出应用 进入后重连时 先发udp命令给机器人端进行确认）");
                //获取通讯版本号
                if (data.length >= 17) {
                    setReturnChildTypeFromScan(true);
                    LogMgr.i("enterScanCodeConnect()3 包含版本号 设置返回子类型");
                } else {
                    setReturnChildTypeFromScan(false);
                    LogMgr.i("enterScanCodeConnect()3 不包含版本号 设置返回主类型");
                }
                byte[] connectState = new byte[1];
                if (Application.getInstance().isTcpConnecting() == true) {
                    connectState[0] = (byte) 0x00;
                    LogMgr.i("enterScanCodeConnect()3 当前已有TCP连接");
                } else {
                    connectState[0] = (byte) 0x01;
                    LogMgr.i("enterScanCodeConnect()3 当前没有TCP连接");
                }
                byte[] sendData = ProtocolUtil.buildProtocol(
                        (byte) ProtocolUtil.getBrainTypeForScan(),
                        GlobalConfig.CHECK_BEFORE_CONNECT_OUT_CMD_1,
                        GlobalConfig.CHECK_BEFORE_CONNECT_OUT_CMD_2,
                        connectState);
                DatagramPacket packet = new DatagramPacket(sendData,
                        sendData.length, mInetAddressForBroadcast,
                        BrainData.DATA_PORT_KNOW);
                BrainService.mSocket.send(packet);
                Thread.sleep(500);
            } else if(data.length >= 14
                    && data[5] == GlobalConfig.CHECK_BEFORE_CONNECT_IN_CMD_1
                    && data[6] == (byte)0x60){
                LogMgr.i("直接控制UDP命令接收的打开或关闭 data[12] = "+data[12]);
                if(data[12] == (byte)0x00){
                    mBrainService.BrainInfoDestroy();
                }else if(data[12] == (byte)0x01){
                    mBrainService.initBrainIfo();
                    mBrainService.setBrainInfoState(true);
//                    mBrainService.addConnectView();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 答复udp命令 扫码广播的第一条
     *
     * @param ip 本地ip
     * @return 当前是否已有链接
     */
    private boolean sendIpSoket(String ip) {
        boolean isTcpConnecting = false;
        String[] localHostIp = ip.split("[.]");
        byte[] mIp = new byte[4];
        for (int i = 0; i < 4; i++) {
            mIp[i] = (byte) Integer.parseInt(localHostIp[i]);
        }
        byte[] sendData = getSendData(mIp);

        if (sendData[sendData.length - 1] == (byte) 0x00) {
            isTcpConnecting = true;
        } else if (sendData[sendData.length - 1] == (byte) 0x01) {
            isTcpConnecting = false;
        } else {
            LogMgr.e("sendIpSoket() tcp连接状态错误");
        }
        byte[] buff_send = ProtocolUtil.buildProtocol(
                (byte) ProtocolUtil.getBrainTypeForScan(),
                (byte) GlobalConfig.CMD_BROADCAST_RETURN_CMD1,
                (byte) GlobalConfig.CMD_BROADCAST_RETURN_CMD2_1, sendData);
        DatagramPacket packet = new DatagramPacket(buff_send, buff_send.length,
                mInetAddressForBroadcast, BrainData.DATA_PORT_KNOW);
        LogMgr.d("mSocket == null = " + (BrainService.mSocket == null)
                + " packet.getAddress() = " + packet.getAddress());
        if (BrainService.mSocket != null) {
            try {
                BrainService.mSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return isTcpConnecting;
    }

    /**
     * 在ip数组后面加一位tcp连接状态位
     *
     * @param mIp
     */
    private byte[] getSendData(byte[] mIp) {
        byte[] sendData = new byte[mIp.length + 1];
        System.arraycopy(mIp, 0, sendData, 0, mIp.length);
        if (Application.getInstance().isTcpConnecting() == true) {
            sendData[sendData.length - 1] = (byte) 0x00;
        } else {
            sendData[sendData.length - 1] = (byte) 0x01;
        }
        return sendData;
    }

    /**
     * 停止 在扫码连接第一步中创建的 重建热点的Timer
     */
    public void stopRecreateHotSpotTimer() {
        LogMgr.i("stopRecreateHotSpotTimer() 心跳TCP连接成功，停止在扫码连接第一步中创建的重建热点的Timer 保存当前路由器名字、密码");
        if (wifi_timer != null) {
            wifi_timer.cancel();
            wifi_timer = null;
        }
        if (string_wifiSsid != null && string_wifiPwd != null) {
            WifiUtils.saveWifiInfo(string_wifiSsid, string_wifiPwd);
            string_wifiSsid = null;
            string_wifiPwd = null;
        }
    }
}
