package com.abilix.brain.control;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.abilix.brain.Application;
import com.abilix.brain.BrainActivity;
import com.abilix.brain.BrainInfo;
import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.data.DataProcess;
import com.abilix.brain.utils.GetAppInfoThread;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 心跳TCP信息处理
 * @author yangz
 * @date 2017/12/4
 */

public class ServerHeartBeatProcesser {

    public static Context context = Application.getInstance();
    /* 平台类型 */
    private static final byte PLATFORM_TYPE_ANDROID = (byte) 0x00;
    private static final byte PLATFORM_TYPE_IOS = (byte) 0x01;
    private static final byte PLATFORM_TYPE_UNKNOWN = (byte) 0xFF;

    private static final int[] ANDROID_CONNECT_VERTION_START_RESEND_HEARTBEART = new int[]{1, 0, 1, 4};
    private static final int[] IOS_CONNECT_VERTION_START_RESEND_HEARTBEART = new int[]{1, 0, 5, 8};

    private static ServerHeartBeatProcesser instance;
    private ServerHeartBeatProcesser(){

    }

    public static ServerHeartBeatProcesser getInstance() {
        // 单例
        if (instance == null) {
            synchronized (ServerHeartBeatProcesser.class) {
                if (instance == null) {
                    instance = new ServerHeartBeatProcesser();
                }
            }
        }
        return instance;
    }

    private long mLastCmdFromHeartTCP = 0;
    /**
     * 上一次收到客户端心跳的时间
     */
    private long lastReceiveHeartTime;
    private Timer mReceiveHeartBeatReplyTimer;
    private TimerTask mReceiveHeartBeatReplyTimerTask;
    private int mHeartBeatCount = 0;

    /**当前是否K5治具状态*/
    private boolean isK5CheckState = false;
    public boolean isK5CheckState() {
        return isK5CheckState;
    }
    public void setK5CheckState(boolean k5CheckState) {
        isK5CheckState = k5CheckState;
    }

    /**根据心跳获取信息判断是否启用限制只能连一台客户端的功能*/
    private boolean isNeedToLimitClientToOne = true;
    /**PAD端APP版本号*/
    private int[] appVersion;
    /**通讯版本号*/
    private int[] appCommunicationVersion;
    /**平台类型*/
    private byte platformType;

    public boolean isNeedToLimitClientToOne() {
        return isNeedToLimitClientToOne;
    }

    public void setNeedToLimitClientToOne(boolean isNeedToLimitClientToOne) {
        this.isNeedToLimitClientToOne = isNeedToLimitClientToOne;
    }

    public long getLastReceiveHeartTime() {
        return lastReceiveHeartTime;
    }

    public void setLastReceiveHeartTime(long lastReceiveHeartTime) {
        this.lastReceiveHeartTime = lastReceiveHeartTime;
    }

    public void DataType(byte[] recv) {// 接收mobile数据存缓冲区
        LogMgr.d("心跳TCP收到信息 = " + Utils.bytesToString(recv));
        setLastReceiveHeartTime(System.currentTimeMillis());
        try {
            if (recv.length >= 8 && recv[1] == GlobalConfig.APP_STORE_IN_CMD_1 && recv[2] == GlobalConfig.APP_STORE_IN_CMD_2_REQUEST_APP_INFO) {
                LogMgr.i("请求获取机器人端安装的apk的信息");
                if (recv.length == 8) {
                    // 数据位为空，则机器人端反馈所有 加了
                    // “android.intent.category.abilix_Launcher”的标志的apk的相关信息
                    new GetAppInfoThread(
                            GetAppInfoThread.FLAG_ALL_ABILIX_LAUNCHER,
                            GlobalConfig.APP_ABILIX_LAUNCHER, GetAppInfoThread.FLAG_PASSIVE).start();
                } else if (recv.length > 8) {
                    // 数据位不为空，返回需要获取的apk信息
                    byte[] tempbuff = new byte[recv.length - 8];
                    System.arraycopy(recv, 7, tempbuff, 0, tempbuff.length);
                    String pkgName = new String(tempbuff, "UTF-8");
                    LogMgr.i("要获取的包名为 = " + pkgName);
                    new GetAppInfoThread(GetAppInfoThread.FLAG_SINGLE_PACKAGE, pkgName, GetAppInfoThread.FLAG_PASSIVE).start();
                } else {
                    LogMgr.e("命令长度异常");
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogMgr.e("获取机器人端安装的apk的信息异常");
            return;
        }
        if (recv.length > 8 && recv[1] == GlobalConfig.APP_STORE_IN_CMD_1 && recv[2] == GlobalConfig.APP_STORE_IN_CMD_2_OPEN_APP) {
            if(System.currentTimeMillis() - mLastCmdFromHeartTCP < 1000){
                LogMgr.i("两次打开应用的时间太短，不处理");
                return;
            }
            mLastCmdFromHeartTCP = System.currentTimeMillis();
            LogMgr.i("请求打开机器人端的一个APP");
            byte[] tempbuff = new byte[recv.length - 8];
            System.arraycopy(recv, 7, tempbuff, 0, tempbuff.length);
            String pkgName = null;
            try {
                pkgName = new String(tempbuff, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            LogMgr.i("要打开的包名为 = " + pkgName);
            Utils.openAppForAppStore(context, pkgName);
            return;
        }else if (recv.length > 8 && recv[1] == GlobalConfig.CMD_BROADCAST_RETURN_CMD2_1 && recv[2] == GlobalConfig.CMD_BROADCAST_ENTER_CMD2_2) {
            LogMgr.i("进入编程界面");
            int type = recv[7];
            if (type == 1){// 激活编程状态
                LogMgr.e("进入编程界面");
                Intent intent = new Intent("com.abilix.brainset.close.otherapp");
                intent.setPackage("com.abilix.brainset");
                intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                context.sendBroadcast(intent);
            }
//            byte[] tempbuff = new byte[recv.length - 8];
//            System.arraycopy(recv, 7, tempbuff, 0, tempbuff.length);
//            String pkgName = null;
//            try {
//                pkgName = new String(tempbuff, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            LogMgr.i("要打开的包名为 = " + pkgName);
//            Utils.openAppForAppStore(context, pkgName);
            return;
        }else if(recv.length > 8 && recv[1] == GlobalConfig.APP_STORE_IN_CMD_1 && recv[2] == GlobalConfig.APP_STORE_IN_CMD_2_CLOSE_APP){
            LogMgr.i("请求关闭机器人端的一个APP");
            try {
                int paraNum = (int)(recv[7]&0xFF);
                if(paraNum >= 2){
                    int lengthOfPackageName = (int)(recv[8]&0xFF);
                    byte[] tempbuff = new byte[lengthOfPackageName];
                    System.arraycopy(recv, 9, tempbuff, 0, tempbuff.length);
                    String pkgName = new String(tempbuff, "UTF-8");
                    LogMgr.i("要关闭的包名为 = " + pkgName);
                    Utils.closeAppForAppStore(context, pkgName);
                    if(TextUtils.equals(pkgName,GlobalConfig.APP_PKGNAME_IM_QOMOLANGMA)){
                        LogMgr.i("要关闭的应用为珠穆朗玛，启动编程");
                        Thread.sleep(500);
                        BrainActivity.getmBrainActivity().startProgramPageAnimaAndFuncFromOutside();
                    }

                }else{
                    LogMgr.e("参数个数异常");
                }
            } catch (Exception e) {
                LogMgr.e("请求关闭机器人端的一个APP e = "+e);
                e.printStackTrace();
            }
            return;
        }else if(ProtocolUtil.isK5UDPCheckCmd(recv)){
            LogMgr.i("收到K5治具命令 recv = "+Utils.bytesToString(recv));
            try {
                BrainService.getmBrainService().getmBrainInfo().handleReceivedCmd(Utils.byteMerger(new byte[]{(byte)0xAA,(byte)0x55,(byte)0x00,(byte)recv.length},recv));
                isK5CheckState = true;
            } catch (Exception e) {
                LogMgr.e("处理K5治具命令 异常e = "+e);
                e.printStackTrace();
            }
            return;
        }
        analyzeFrame(recv);
//		analyzeClientcmdData(recv);
    }

    // 解析接收到的客户端命令
    private void analyzeFrame(byte[] buff) {
        StringBuilder sbForLog = new StringBuilder();
        sbForLog.append("analyzeFrame()");

        if (buff.length == 9) {
            // 心跳只有上层应用
//			LogMgr.d("老版本心跳，不做一个客户端限制处理");
            sbForLog.append(" 老版本心跳，不做一个客户端限制处理");
            setNeedToLimitClientToOne(false);
            appVersion = null;
            appCommunicationVersion = null;
            platformType = PLATFORM_TYPE_UNKNOWN;
        } else if (buff.length >= 18) {
            // 心跳包含更多信息的版本
//			LogMgr.v("新版本心跳，做一个客户端限制处理");
            sbForLog.append(" 新版本心跳，做一个客户端限制处理");
            setNeedToLimitClientToOne(true);
            appVersion = getVersionFromByteArray(buff, 8, 4);
//			LogMgr.v("上层应用版本号为 = " + Arrays.toString(appVersion));
            sbForLog.append(" 上层应用版本号为 = " + Arrays.toString(appVersion));
            appCommunicationVersion = getVersionFromByteArray(buff, 12, 4);
//			LogMgr.v("上层应用通信模块版本号为 = " + Arrays.toString(appCommunicationVersion));
            sbForLog.append(" 上层应用通信模块版本号为 = " + Arrays.toString(appCommunicationVersion));
            if (buff[16] == PLATFORM_TYPE_ANDROID) {
//				LogMgr.v("当前为android平台");
                sbForLog.append(" 当前为android平台");
                platformType = PLATFORM_TYPE_ANDROID;
            } else if (buff[16] == PLATFORM_TYPE_IOS) {
//				LogMgr.v("当前为ios平台");
                sbForLog.append(" 当前为ios平台");
                platformType = PLATFORM_TYPE_IOS;
            } else {
//				LogMgr.e("平台参数错误");
                sbForLog.append(" 平台参数错误");
                platformType = PLATFORM_TYPE_UNKNOWN;
            }

            if (GlobalConfig.isReceiveHeartBeatReply
                    && buff[1] == GlobalConfig.HEART_BEAT_FIRST_IN_CMD_1
                    && buff[2] == GlobalConfig.HEART_BEAT_FIRST_IN_CMD_2) {
                // 在此处根据上层应用通信模块版本号和平台类型判断是否需要打开监听心跳回复功能
                if (isNeedToReceiveHeartbeatReply()) {
//					LogMgr.i("根据上层应用通信模块版本号和平台类型  打开接收心跳回复功能");
                    sbForLog.append(" 根据上层应用通信模块版本号和平台类型  打开接收心跳回复功能");
                    startReceiveHeartBeatReplyTimer();
                } else {
//					LogMgr.i("根据上层应用通信模块版本号和平台类型  不打开接收心跳回复功能");
                    sbForLog.append(" 根据上层应用通信模块版本号和平台类型  不打开接收心跳回复功能");
                }
            }
        } else {
            // 错误的长度
            LogMgr.e("analyzeFrame() 心跳包长度错误");
        }
        LogMgr.i(sbForLog.toString());
//		LogMgr.d(TAG, "analyzeFrame buff[0]:" + buff[0]);

        if (!ProtocolUtil.isTypeRelate(buff[0])) // 首先判断系列是否对应正确
        {
            LogMgr.e("analyzeFrame() cmd 协议系列名称不对应");
            return;
        }
        switch (buff[1]) // cmd
        {
            // 首先
            case (byte) 0x00: // 建立连接
                if (buff[2] == GlobalConfig.HEART_BEAT_FIRST_IN_CMD_2) // 建立了tcp连接同时发送
                // 广播告知哪个应用建立了连接
                {
                    LogMgr.w("analyzeFrame() 收到第一包心跳信息");
                    sendPadAppConnectStateBroadcast((int) buff[7]);
                } else if (buff[2] == GlobalConfig.HEART_BEAT_IN_CMD_2) {
//                    LogMgr.i("analyzeFrame() 收到一包心跳回复");
                }
                break;
            default:
                LogMgr.e("analyzeFrame() cmd error");
                break;
        }
    }

    /**
     * 长连接建立时发送IP信息广播至BrainService
     *
     * @param inetAddress
     */
    public void setInetAddress(InetAddress inetAddress) {
        if (inetAddress != null) {
            Intent sendIntent = new Intent(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE);
            sendIntent.putExtra(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE_PARAM, GlobalConfig.GET_IP);
            sendIntent.putExtra("ip", inetAddress);
            context.sendBroadcast(sendIntent);
        }
    }

    /**
     * 打开接受客户端心跳的timer
     */
    public synchronized void startReceiveHeartBeatReplyTimer() {
        stopReceiveHeartBeatReplyTimer();
        LogMgr.i("startReceiveHeartBeatReplyTimer()");
        setLastReceiveHeartTime(System.currentTimeMillis());
        mReceiveHeartBeatReplyTimer = new Timer();
        mReceiveHeartBeatReplyTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis()
                        - getLastReceiveHeartTime() > GlobalConfig.RECEIVE_HEART_BEAT_MAX_TIMES
                        * GlobalConfig.RECEIVE_HEART_BEAT_TIME * 1000) {
                    LogMgr.e("长时间没有接受到客户端的心跳回复，主动断开当前连接");
                    if (DataProcess.ctx01 != null) {
                        DataProcess.ctx01.channel().close();
                    }
                    stopReceiveHeartBeatReplyTimer();
                    // closeConnect();
                }
            }
        };
        mReceiveHeartBeatReplyTimer.schedule(mReceiveHeartBeatReplyTimerTask,
                80, GlobalConfig.RECEIVE_HEART_BEAT_TIME * 1000);
    }

    /**
     * 关闭接受客户端心跳的timer
     */
    public synchronized void stopReceiveHeartBeatReplyTimer() {
        LogMgr.i("stopReceiveHeartBeatReplyTimer()");
        if (mReceiveHeartBeatReplyTimer != null) {
            mReceiveHeartBeatReplyTimer.cancel();
        }
        if (mReceiveHeartBeatReplyTimerTask != null) {
            mReceiveHeartBeatReplyTimerTask.cancel();
        }
    }

    /**
     * 发送一次心跳的操作
     */
    public void sendHeartBeat(int heartBeatType) {
        LogMgr.v("sendHeartBeat() heartBeatType = " + heartBeatType);
        StringBuilder sbForLog = new StringBuilder();
        sbForLog.append("sendHeartBeat()");
        byte[] heartBeatData = new byte[8];
        // 第0位 编程是否可用
        if (BrainInfo.Builder.getBrainInfo() != null && BrainInfo.Builder.getBrainInfo().isActive()) {
//			LogMgr.v("编程可用");
            sbForLog.append(" 编程可用");
            heartBeatData[0] = (byte) 0x01;
        } else {
//			LogMgr.v("编程停止");
            sbForLog.append(" 编程停止");
            heartBeatData[0] = (byte) 0x00;
        }
        // 第1,2位 Brain的版本
        int versionCode = Application.getInstance().getmVersionCode();
        if (versionCode == 0) {
            versionCode = Utils.getVersion(Application.getInstance().getApplicationContext());
            Application.getInstance().setmVersionCode(versionCode);
        }
        heartBeatData[1] = (byte) ((versionCode >> 8) & 0xFF);
        heartBeatData[2] = (byte) (versionCode & 0xFF);
        // 第3~6位 固件版本号
        int firewareVersion = Application.getInstance().getmFirewareVersion();
        if (firewareVersion == -1 && mHeartBeatCount % 60 == 0) {
            // 固件版本号为-1的情况下，去请求一次
            Intent intentRequestFireware = new Intent(GlobalConfig.FIREWARE_REQUEST_VERSION_BROADCAST);
            Application.getInstance().getApplicationContext().sendBroadcast(intentRequestFireware);
        }
//		LogMgr.v("versionCode = " + versionCode + " firewareVersion = " + firewareVersion);
        sbForLog.append(" versionCode = " + versionCode + " firewareVersion = " + firewareVersion);
        byte[] firewareVersionByteArray = Utils.getByteArrayFromInt(firewareVersion);
        System.arraycopy(firewareVersionByteArray, 0, heartBeatData, 3, firewareVersionByteArray.length);
        // 第7位 M轮子电机保护状态
        if (Application.getInstance().isMWheelProtected()) {
//			LogMgr.v("M轮子电机已保护");
            sbForLog.append(" M轮子电机已保护");
            heartBeatData[7] = (byte) 0x01;
        } else {
//			LogMgr.v("M轮子电机未保护");
            sbForLog.append(" M轮子电机未保护");
            heartBeatData[7] = (byte) 0x00;
        }
        LogMgr.i(sbForLog.toString());
        // 构成发送命令
        byte[] heartBeatCmd = ProtocolUtil.buildProtocol(
                (byte) ProtocolUtil.getBrainType(),
                GlobalConfig.HEART_BEAT_OUT_CMD_1,
                GlobalConfig.HEART_BEAT_OUT_CMD_2, heartBeatData);
        DataProcess.GetManger().sendMsg(heartBeatCmd);
        mHeartBeatCount++;
        if (mHeartBeatCount > 599) {
            mHeartBeatCount = 0;
        }
    }

    public boolean isNeedToReceiveHeartbeatReply() {
        boolean result = false;
        if ((platformType == PLATFORM_TYPE_IOS &&
                Utils.isLaterThanCertanCommunicationVersion(appCommunicationVersion,
                        IOS_CONNECT_VERTION_START_RESEND_HEARTBEART))
                || (platformType == PLATFORM_TYPE_ANDROID &&
                Utils.isLaterThanCertanCommunicationVersion(appCommunicationVersion,
                        ANDROID_CONNECT_VERTION_START_RESEND_HEARTBEART))) {
            LogMgr.i("版本符合接受心跳回复");
            result = true;
        } else {
            LogMgr.i("版本不符合不接受心跳回复");
            result = false;
        }
        return result;
    }

    /**
     * 从buff中获取版本号
     *
     * @param buff   数据源
     * @param start  起始位置
     * @param length 长度
     * @return 包含版本信息的int数组
     */
    private int[] getVersionFromByteArray(byte[] buff, int start, int length) {
        if (buff == null || buff.length <= 0 || start < 0
                || start >= buff.length || length < 0
                || start + length > buff.length) {
            LogMgr.e("获取版本号时参数异常");
            return null;
        }
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[i] = (int) (buff[i + start] & 0xFF);
        }

        return result;
    }

    /**
     * 通知BrainService 心跳建立成功或连接断开
     *
     * @param flag
     */
    public void sendPadAppConnectStateBroadcast(int flag) {
        Intent sendIntent = new Intent(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE);
        sendIntent.putExtra(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE_PARAM, flag);
        context.sendBroadcast(sendIntent);
        LogMgr.i("sendPadAppConnectStateBroadcast flag=" + flag);
    }

    /**
     * 应用有安装完成或卸载时，通知appStore
     *
     * @param packageName  反馈给客户端的应用的包名
     * @param needToDelete 反馈后是否删除该应用
     */
    public void feedbackToAppStore(String packageName, boolean needToDelete) {
        LogMgr.i("feedbackToAppStore packageName = " + packageName + " needToDelete = " + needToDelete);
        //根据版本号去判断是否发送
        int[] IOS_CONNECT_VERTION_START_SEND_FEEDBACK = new int[]{1, 0, 5, 16};

        if ((platformType == PLATFORM_TYPE_IOS
                && Utils.isLaterThanCertanCommunicationVersion(appCommunicationVersion, IOS_CONNECT_VERTION_START_SEND_FEEDBACK))
                || (platformType == PLATFORM_TYPE_ANDROID)) {
            try {
                if (Application.getInstance().isTcpConnecting()
                        && (BrainService.getmBrainService().getmAppTypeConnected() == GlobalConfig.INNER_FILE_TRANSPORT_APP_STORE
                        || BrainService.getmBrainService().getmAppTypeConnected() == GlobalConfig.INNER_FILE_TRANSPORT_APP_FLAG)) {
                    if (needToDelete) {
                        LogMgr.i("开始主动反馈 反馈后删除");
                        new GetAppInfoThread(GetAppInfoThread.FLAG_SINGLE_PACKAGE, packageName, GetAppInfoThread.FLAG_ACTIVE_DELETE).start();
                    } else {
                        LogMgr.i("开始主动反馈 反馈后不删除");
                        new GetAppInfoThread(GetAppInfoThread.FLAG_SINGLE_PACKAGE, packageName, GetAppInfoThread.FLAG_ACTIVE_NOT_DELETE).start();
                    }
                } else {
                    LogMgr.i("Application.getInstance().isTcpConnecting() = " + Application.getInstance().isTcpConnecting() +
                            " BrainService.getmBrainService().getmAppTypeConnected() = " + BrainService.getmBrainService().getmAppTypeConnected());
                    if (needToDelete && null != BrainActivity.getmBrainActivity()) {
                        LogMgr.w("应用卸载时 未连接appstore 不需给appstore反馈 开始卸载");
                        BrainActivity.getmBrainActivity().deleteAPK(packageName);
                    }
                }
            } catch (Exception e) {
                LogMgr.e("应用安装/卸载 给appstore反馈时异常 " + e);
                if (needToDelete && null != BrainActivity.getmBrainActivity()) {
                    LogMgr.e("应用安装/卸载 给appstore反馈时异常 开始卸载");
                    BrainActivity.getmBrainActivity().deleteAPK(packageName);
                }
                e.printStackTrace();
            }
        } else {
            if (needToDelete && null != BrainActivity.getmBrainActivity()) {
                LogMgr.i("应用安装/卸载 版本不满足 不需要反馈 开始卸载");
                BrainActivity.getmBrainActivity().deleteAPK(packageName);
            } else {
                LogMgr.i("应用安装/卸载 版本不满足 不需要反馈 安装完成");
            }
        }

    }
}
