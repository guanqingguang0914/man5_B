package com.abilix.brain;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abilix.brain.BrainInfo.Builder;
import com.abilix.brain.aidl.Brain;
import com.abilix.brain.aidl.IBrain;
import com.abilix.brain.control.FileReceiveRunnable;
import com.abilix.brain.control.ServerHandler;
import com.abilix.brain.control.ServerHeartBeatProcesser;
import com.abilix.brain.control.ServerRunnable;
import com.abilix.brain.data.DataProcess;
import com.abilix.brain.m.MUtils;
import com.abilix.brain.utils.BrainData;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileManager;
import com.abilix.brain.utils.FileManager.FileCallback;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.MyAlertDialogs;
import com.abilix.brain.utils.PictureCallbackCamera;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.SharedPreferenceTools;
import com.abilix.brain.utils.Utils;
import com.abilix.clrdet.clrdetect;
import com.abilix.control.aidl.Control;
import com.abilix.control.aidl.IControl;
import com.abilix.explainer.ExplainMessage;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.camera.CameraStateCallBack;
import com.abilix.explainer.camera.IRobotCamera;
import com.abilix.explainer.camera.RobotCameraStateCode;
import com.abilix.explainer.camera.systemcamera.SystemCamera;
import com.abilix.explainer.camera.usbcamera.UsbCamera;
import com.abilix.explainer.utils.ProtocolUtils;
import com.abilix.explainer.utils.SPUtils;
import com.abilix.explainer.view.MainActivity;
import com.abilix.learn.oculus.distributorservice.DistributorBack;
import com.abilix.learn.oculus.distributorservice.IDistributorInterface;
import com.abilix.usbcamera.client.PreviewCallback;

import java.io.File;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import static com.abilix.brain.GlobalConfig.BRAIN_TYPE;


//import com.abilix.brain.ui.USBVideo;

/**
 * Brain主服务，创建7777端口Socket，接收，处理扫码连接；判断是否需要stm32升级；分发从Control收到的信息；启动两个TCP服务端；TCP连接建立时创建{@link BrainInfo}；启动绑定Control服务；
 * 接收多种广播，做出响应；控制编程页面的显示状态；BrainInfo的编程状态；维护向Control的通信线程。
 */
public class BrainService extends Service {
    public static final int HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION = 305;
    public static final int HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_SHOW_DATA = 306;
    public static final int HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_BEGIN = 307;
    public static final int HANDLER_MESSAGE_BLANCE_CAR = 308;
    public static final int HANDLER_MESSAGE_BLANCE_CAR_STOP = 311;
    public static final int HANDLER_MESSAGE_COMPASS = 313;
    public static final int HANDLER_MESSAGE_USB_CAMERA_PHOTO_TAKE = 317;
    public static final int HANDLER_MESSAGE_SYSTEM_CAMERA_PHOTO_TAKE = 318;
    public static final int HANDLER_MESSAGE_STM32_UPDATE_NOTIFY = 319;
    public static final int HANDLER_MESSAGE_QUERY_STM32_VERSION = 321;
    public static final int HANDLER_MESSAGE_H34_BOOT_RECOVER = 322;
    public static final int HANDLER_MESSAGE_M_BLOCKED_CONFIM = 500;
    public static final int HANDLER_MESSAGE_M_BLOCKED_NOTIFY = 501;

    private static final int WHAT_HANDLE_STATUS = 666;

    private final String TAG = "BrainService";
    /**
     * 绑定机器人9999端口，发送信息到客户端9999端口的socket
     */
    public static DatagramSocket mSocket;
    /**
     * 连接正式建立后，即更新为最新客户端的ip地址
     */
    private InetAddress mInetAddress;
    /**
     * 连接正式建立后，记录对方APP类型
     */
    private int mAppTypeConnected = GlobalConfig.APP_DEFAULT;

    /**
     * String类型ip，类似 192.168.1.105
     */
    private String ip;
    private BrainServiceBroadcastReceiver mBrainServiceBroadcastReceiver;
    private AlertDialog ad1, ad2, ad3, ad4, ad5, ad6;
    private IControl mIControl;
    private Control mControlForLineTrack;

    public BrainInfo getmBrainInfo() {
        return mBrainInfo;
    }

    private BrainInfo mBrainInfo;

    private Context mContext;
    private FileManager mFileManager;
    private Builder mBuilder;
    // 是否绑定Control
    private boolean isBinding;
    // 是否绑定M
    private boolean isMBinding;
    private BindingControl mBindingControl;
    private BindingM mBindingM;
    private byte skillplayer_type = -1;
    // 对话框
    private boolean isBlanct = false;
    private PictureCallbackCamera mCallbackCamera = null;

    private BrainServiceHelper mBrainServiceHelper;
    /**
     * BrainService的唯一实例
     */
    private static BrainService mBrainService;
    //    private static BrainActivity mBrainActivity;
    private boolean isFirstonServiceConnected = true;

    /**
     * 获取BrainService的唯一实例
     *
     * @return BrainService的唯一实例
     */
    public static BrainService getmBrainService() {
        return mBrainService;
    }

    private Timer chargeCheckTiemr;
    private TimerTask chargeTimerTask;


    /**
     * 心跳时是否返回应用端子类型
     */
    private boolean isReturnChildType = false;

    /**
     * 心跳时是否返回应用端子类型
     */
    public boolean isReturnChildType() {
        return isReturnChildType;
    }

    /**
     * 设置心跳时是否返回应用端子类型
     */
    public void setReturnChildType() {
        this.isReturnChildType = mBrainServiceHelper.isReturnChildTypeFromScan();
    }

    public boolean isReturnChildTypeFromScan() {
        return mBrainServiceHelper.isReturnChildTypeFromScan();
    }


    /**
     * 拍照 状态 -1 不显示 1 显示 m h 2 显示 c
     */
    private int Photograph_State = 0;
    private int times = 0;

    private AlertDialog progressDialog;
    public static final byte UPGRADE_SUCESS = 0X01;
    public static final byte UPGRADE_FAILED = 0X00;

    static {
        try {
            mSocket = new DatagramSocket(BrainData.DATA_PORT_KNOW);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public IControl getControlService() {
        return mIControl;
    }

    /**
     * wangdongdong
     * Control会调用这个BrainInterface，将Parcelable：brain传到BrainService中，然后该处负责发给BrainActivity
     * 但此处用了DisplayActivity.displayActivity，所用的方法是发送广播的方式：SendBroadcastActivity
     */
    IBrain.Stub mStub = new IBrain.Stub() {
        // 运行在Binder 线程池中
        @Override
        public void BrainInterface(Brain brain) throws RemoteException {
            try {
                if (brain != null) {
                    LogMgr.i("收到Control的 信息 = CallBackMode():"
                            + brain.getCallBackMode() + " ModeState:"
                            + brain.getModeState() + " brain.getSendByte():"
                            + Utils.showDataHex(brain.getSendByte()));
                    byte[] data = brain.getSendByte();
                    if (brain.getCallBackMode() == 2) {
                        DisplayActivity.displayActivity(brain, mHandler, mContext);
                    } else if (brain.getCallBackMode() == 0 && data != null && data.length == 2 && data[0] == 0x0E &&
                            data[1] == 0x10) {
                        ExplainMessage resumeMsg = new ExplainMessage();
                        resumeMsg.setFuciton(ExplainMessage.EXPLAIN_RESUME);
                        ExplainTracker.getInstance().doExplainCmd(resumeMsg, null);
                    } else {
                        if(ServerHeartBeatProcesser.getInstance().isK5CheckState()){
                            LogMgr.i("返回治具协议反馈值 data = "+(data==null?"null":Utils.bytesToString(data)));
                            DataProcess.GetManger().sendMsg(data);
                            ServerHeartBeatProcesser.getInstance().setK5CheckState(false);
                            return;
                        }
                        if (mBrainInfo != null) {
                            mBrainInfo.returnDataToClient(brain.getSendByte());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * DeathRecipient 捕获进程杀死
     *
     * @author luox
     */
    private IBinder.DeathRecipient mDeathRecipient = new DeathRecipient() {

        @Override
        public void binderDied() {
            if (mIControl == null) {
                LogMgr.e("mIControl == null is" + (mIControl == null));
                return;
            }

            // mHandler.sendEmptyMessage(0x404);
            mIControl.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mIControl = null;
            if (isBinding) {
                unbindControlService();
            }
            startBindingControl();
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogMgr.d("onServiceConnected");
            try {
                stopBindingControl();
                isBinding = true;
                mIControl = IControl.Stub.asInterface(service);
                SPUtils.setIControl(mIControl); //SPUtils初始化IControl接口；
                mBrainServiceHelper.setmIControl(mIControl);
                service.linkToDeath(mDeathRecipient, 0);
                /* 告知Control apk机器人的类型 */
                byte[] buff = new byte[1];
                buff[0] = (byte) GlobalConfig.BRAIN_TYPE;
                LogMgr.d("发送给Control的机器人类型：" + buff[0]);
                sendMessageToControl(
                        GlobalConfig.CONTROL_CALLBACKMODE_NOTIFY_ROBOT_TYPE,
                        buff, null, 0, 0);
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3 && isFirstonServiceConnected) {
                    isFirstonServiceConnected = false;
                    startH34RecoverTimer();
//                    mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_H34_BOOT_RECOVER, 3200);
                }
                //请求control告知stm32版本号
                mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_QUERY_STM32_VERSION, 5000);
                //H5 开机启动手指保护功能
                startFingerProtect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * DeathRecipient 捕获进程杀死
     *
     * @author luox
     */
    private IBinder.DeathRecipient mMServiceDeathRecipient = new DeathRecipient() {

        @Override
        public void binderDied() {
            if (mIDistributorInterface == null) {
                LogMgr.e("mIDistributorInterface == null is" + (mIDistributorInterface == null));
                return;
            }

            // mHandler.sendEmptyMessage(0x404);
            mIDistributorInterface.asBinder().unlinkToDeath(mMServiceDeathRecipient, 0);
            mIDistributorInterface = null;
            if (isMBinding) {
                unbindMService();
            }
            startBindingM();
        }
    };

    private IDistributorInterface mIDistributorInterface;

    public IDistributorInterface getMService() {
        return mIDistributorInterface;
    }

    private ServiceConnection mMServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            stopBindingMService();
            isMBinding = true;
            mIDistributorInterface = IDistributorInterface.Stub.asInterface(service);
            try {
                mIDistributorInterface.setDisBack(mDistributorBack);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                service.linkToDeath(mMServiceDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            LogMgr.d("FZXX", " === MService绑定成功 ===");
//            MUtils.startEmotion();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIDistributorInterface = null;
        }
    };
    ;
    private DistributorBack mDistributorBack = new DistributorBack.Stub() {

        @Override
        public void disCallBack(int status) throws RemoteException {
            LogMgr.d("FZXX", "=== status ===  :  " + status);
            Message msg = Message.obtain(mHandler);
            msg.what = WHAT_HANDLE_STATUS;
            msg.obj = status;
            msg.sendToTarget();
        }
    };


    private Timer mTimerForH34Recover;
    private TimerTask mTimerTaskForH34Recover;
    private int countForH34Recover;

    /**
     * 开机执行H34回复动作的Timer，检测到动作文件保存完成之后执行动作，如果十秒后仍未完成文件写入，就放弃执行恢复动作
     */
    private void startH34RecoverTimer() {
        stopH34RecoverTimer();
        countForH34Recover = 0;
        mTimerForH34Recover = new Timer();
        mTimerTaskForH34Recover = new TimerTask() {
            @Override
            public void run() {
                if (countForH34Recover >= 10) {
                    LogMgr.e("10次尝试后文件仍未保存结束");
                    stopH34RecoverTimer();
                    return;
                }
                if (Application.getInstance().isFileSaveCompleted()) {
                    LogMgr.i("文件保存结束，发送复原命令");
                    mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_H34_BOOT_RECOVER, 100);
                    stopH34RecoverTimer();
                } else {
                    LogMgr.i("文件保存未结束，尝试次数countForH34Recover = " + countForH34Recover);
                    countForH34Recover++;
                }
            }
        };
        mTimerForH34Recover.schedule(mTimerTaskForH34Recover, 20, 1000);

    }

    private void stopH34RecoverTimer() {
        if (mTimerForH34Recover != null) {
            mTimerForH34Recover.cancel();
        }
        if (mTimerTaskForH34Recover != null) {
            mTimerTaskForH34Recover.cancel();
        }
    }

    /**
     * 初始化 Brainfo
     */
    protected void initBrainIfo() {
        LogMgr.i("initBrainIfo() 初始化BrainInfo");
        if (mIControl != null) {
            mBuilder = new Builder()
                    // .setInfoDatagramSocket(soket)
                    .setInfoDatagramSocket(mSocket).setInetAddress(mInetAddress)
//                    .setContext(mContext)
//                    .setInfoIControl(mIControl)
//                    .setInfoBinder(mIControl.asBinder())
                    .setBrainInfoActive(false);
            mBrainInfo = mBuilder.create();
        } else {
            LogMgr.e("mIControl == null is " + (mIControl == null));
        }
    }

    /**
     * 初始化 FileManager
     */
    public void initFileManager(int mode) {
        LogMgr.e("initFileManager");
        mFileManager = FileManager.getFileManager();
        mFileManager.setFileCallback(new FileUpdateSuccess());
        mFileManager.startDownload(mode);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogMgr.i("BrainService onCreate()");
        mContext = BrainService.this;
        mBrainService = BrainService.this;
        mBrainServiceHelper = new BrainServiceHelper(mBrainService);
        mBrainServiceHelper.startReceiveBroadcastThread();
        registerBroadcastReceiver();
        // 开启TCP监听端口线程 用于 pad App长连接 检测连接断开 发送tcp 命令
        new Thread(new ServerRunnable()).start();
        // 开启TCP监听端口线程 开启文件传输服务器端
        new Thread(new FileReceiveRunnable()).start();

//        WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        wifiAp = WifiApAdmin.getInstance(wm);
        if (ProtocolUtil.isMainTypeOfH(GlobalConfig.BRAIN_TYPE) && GlobalConfig.IS_USING_H_CHARGING_PROTECT) {
            startChargeTimer();
        }
    }

    private void startChargeTimer() {
        LogMgr.i("startChargeTimer");
        stopChargeTimer();
        chargeCheckTiemr = new Timer();
        chargeTimerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent("com.abilix.brainset.awaken");
                intent.setPackage("com.abilix.brainset");
                intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mBrainService.sendBroadcast(intent);
            }
        };
        chargeCheckTiemr.schedule(chargeTimerTask,0,3*1000);
    }

    private void stopChargeTimer() {
        if (chargeCheckTiemr != null) {
            chargeCheckTiemr.cancel();
        }
        if (chargeTimerTask != null) {
            chargeTimerTask.cancel();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogMgr.i("BrainService onStartCommand()");
        if (isBinding) {
            unbindControlService();
        }
        startBindingControl();

        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
            if (isMBinding) {
                unbindMService();
            }
            startBindingM();
        }

        if (!mBrainServiceHelper.isSendMessageToControlHandlerInit()) {
            mBrainServiceHelper.startSendMessageToControlThread();
        }

//        mBrainActivity = BrainActivity.getmBrainActivity();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogMgr.e("BrainService onDestroy()");
        try {
            unbindControlService();
            mBrainServiceHelper.stoptReceiveBroadcastThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mBrainServiceBroadcastReceiver != null) {
            unregisterReceiver(mBrainServiceBroadcastReceiver);
        }
        mBrainServiceBroadcastReceiver = null;
        mBrainService = null;
    }

    /**
     * 注册BrainService广播
     */
    private void registerBroadcastReceiver() {
        if (mBrainServiceBroadcastReceiver == null) {
            mBrainServiceBroadcastReceiver = new BrainServiceBroadcastReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalConfig.ACTION_SERVICE);
        filter.addAction(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);// 用来检测电量
        filter.addAction(GlobalConfig.FIREWARE_VERSION_BROADCAST);
        filter.addAction(GlobalConfig.FIREWARE_UPDATE_BROADCAST);
        filter.addAction(GlobalConfig.BROADCAST_ACTION_STM32_UPDATE_PROGRESS);
        filter.addAction(GlobalConfig.FIREWARE_REQUEST_VERSION_BROADCAST);
        filter.addAction(GlobalConfig.BROADCAST_POWEKEYDOWN_ACTION);

        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(GlobalConfig.BROADCAST_CHARGING_INTENT_ACTION);
        filter.addAction(GlobalConfig.BROADCAST_ACTION_STOP_ALL_BRAIN);

        registerReceiver(mBrainServiceBroadcastReceiver, filter);
    }

    private void startControlService() {
        Intent mIntent = new Intent();
        mIntent.setAction("com.abilix.control.aidl.IControl");
        mIntent.setPackage("com.abilix.control");
        startService(mIntent);
        bindControlService(mIntent);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startMService() {
        Intent intent = new Intent();
        intent.setAction("com.abilix.learn.oculus.distributorservice.DistributorService");
        intent.setPackage("com.abilix.learn.oculus.distributorservice");
        startService(intent);
        bindMService(intent);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void bindControlService(Intent intent) {
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindControlService() {
        if (mConnection != null) {
            isBinding = false;
            unbindService(mConnection);
        }
    }

    private void bindMService(Intent intent) {
        bindService(intent, mMServiceCon, Context.BIND_AUTO_CREATE);
    }

    private void unbindMService() {
        if (mMServiceCon != null) {
            isMBinding = false;
            unbindService(mMServiceCon);
        }
    }

    /**
     * 通信广播
     *
     * @author luox
     */
    private class BrainServiceBroadcastReceiver extends BroadcastReceiver {
        @SuppressWarnings("unused")
        @Override
        public void onReceive(Context context, Intent intent) {
            LogMgr.d("intent.getAction() = " + intent.getAction());
            switch (intent.getAction()) {
                case GlobalConfig.BROADCAST_POWEKEYDOWN_ACTION://电源键点击事件，让屏幕变亮
                    LogMgr.d("收到系统发来的广播：电源按键点击事件触发");
                    if(Application.getInstance().getIsRestState()){
                        SendBroadCastToActivity(GlobalConfig.POWER_BUTTON_ONCLICK);
                    }
                    break;
                case GlobalConfig.BROADCAST_CHARGING_INTENT_ACTION:
                    LogMgr.i("收到设置发出的广播，更新充电状态");
                    boolean isCharging = intent.getBooleanExtra("isCharging", false);
                    if (isCharging) {//当前代表的是低电量,没充电
                        LogMgr.i("当前正在充电，关闭广播接收，断开连接1");
                        mBrainServiceHelper.setReceivingBroadcast(false);
                        if (Application.getInstance().isTcpConnecting()) {
                            mBrainService.sendBroadcast(new Intent(MainActivity.INTENT_ACTION_TCP_DISCONNECT));
                            DataProcess.GetManger().closeAllTcpConnecting();
                        }
                        mHandler.sendEmptyMessageDelayed(23,200);
//                        else{
//                            mHandler.sendEmptyMessageDelayed(23,200);
////                            sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_STOP_ALL_CMD, null, null, 0, 0);
//                        }
                        LogMgr.i("当前正在充电，关闭广播接收，断开连接2");
                        mHandler.sendEmptyMessageDelayed(20,600);
//                        sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, null, 28, 0);
//                        sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, null, 22, 0);//这里对应的是H3 复位
                    } else {//当前代表的是低电量，充电状态,可接收信息等
//                        if(SharedPreferenceTools.getInt(BrainService.getmBrainService(), SharedPreferenceTools.SHAREDPREFERENCE_KEY_ELECTRICITY, 0) < 10){
//                            return;
//                        }
                        LogMgr.i("当前不在充电，回复广播接收");
                        mBrainServiceHelper.setReceivingBroadcast(true);
                        BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                ProtocolUtil.buildProtocol((byte)0x03,(byte) 0xA3, (byte) 0x65,new byte[]{0x01}), null, 0, 0);//固定
                        mHandler.sendEmptyMessageDelayed(24,600);
//                        sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, null, 29, 0);//对应的H56，站立
                    }
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_NEW_STATE, -1);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLING:
                            LogMgr.i("Wifi正在关闭");
                            break;
                        case WifiManager.WIFI_STATE_DISABLED:
                            LogMgr.i("Wifi已经关闭");
                            DataProcess.GetManger().closeAllTcpConnecting();
                            break;
                        case WifiManager.WIFI_STATE_ENABLING:
                            LogMgr.i("Wifi正在打开");
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            LogMgr.i("Wifi已经打开");
                            DataProcess.GetManger().closeAllTcpConnecting();
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                            LogMgr.w("Wifi未知状态");
                            break;
                        default:
                            break;
                    }
                    break;

                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (null != parcelableExtra) {
                        NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                        if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                            String ssid = wifiInfo.getSSID();
                            LogMgr.d("连接到Wifi = " + ssid);
                            DataProcess.GetManger().closeAllTcpConnecting();
                        }
                    }
//                    if (mBrainActivity != null && mBrainActivity.mBrainActivityHandler != null) {
                    if (BrainActivity.getmBrainActivity() != null) {
                        BrainActivity.getmBrainActivity().mBrainActivityHandler.
                                sendEmptyMessage(BrainActivity.BRAINACTIVITY_HANDLER_MESSAGE_UPDATE_THE_QRCODE);
                    }
                    break;

                case Intent.ACTION_SHUTDOWN:
                    LogMgr.d("intent.getAction().equals(Intent.ACTION_SHUTDOWN)");
//			            DataProcess.GetManger().closeAllTcpConnecting();
                    setBrainInfoState(false);
                    removeMessageToSendToControlFromHandler();
                    sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_STOP_ALL_CMD, null, null, 0, 0);
                    sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, null, 20, 0);
                    break;
                case GlobalConfig.BROADCAST_ACTION_STOP_ALL_BRAIN:
                    //通过brainset打开第三方app发送停止广播后需要关闭串口
                    boolean destroySPFlag = intent.getBooleanExtra("destroy_sp_flag", false);
                    LogMgr.i("收到BrainSet或软件更新中的广播，destroy_sp_flag = " + destroySPFlag);
                    if (destroySPFlag) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(300);
                                    LogMgr.e("关闭串口");
                                    BrainService.getmBrainService().getControlService().destorySP();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } else {
                        //收到BrainSet或软件更新中的广播，停止所有动作
                        LogMgr.i("收到BrainSet或软件更新中的广播，停止所有动作");
                        sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_STOP_ALL_CMD, null, null, 1, 0);
                    }
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_CHARGING);
                    int voltage = intent.getIntExtra("voltage", 0);
                    int level = intent.getIntExtra("level", 0);
                    int plugged = intent.getIntExtra("plugged", BatteryManager.BATTERY_PLUGGED_AC);
                    double temperature = intent.getIntExtra("temperature", 0);
                    SharedPreferenceTools.saveInt(mBrainService, SharedPreferenceTools.SHAREDPREFERENCE_KEY_VOLTAGE,
                            voltage);
                    SharedPreferenceTools.saveInt(mBrainService, SharedPreferenceTools
                            .SHAREDPREFERENCE_KEY_ELECTRICITY, level);
                    LogMgr.d("BrainServiceBroadcastReceiver voltage =" + voltage + ";status = " + status + ";level="
                            + level + ";temperature = " + temperature * 0.1);
                    Matching.getMatching().playTohes(context, voltage, status, level, plugged, temperature, mHandler);
                    return;
                case GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE:
                    int flag = intent.getIntExtra(
                            GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE_PARAM,
                            GlobalConfig.APP_DEFAULT);
                    if (flag == GlobalConfig.APP_DEFAULT) {
                        return;
                    }
                    switch (flag) {
                        case GlobalConfig.PAD_APP_DISCONNECT: // 断开连接
                            LogMgr.e("TCP连接 断开连接");
                            BrainInfoDestroy();
                            afterTCPDisconnect();
                            break;
                        case GlobalConfig.GET_IP:
                            mInetAddress = (InetAddress) intent.getSerializableExtra("ip");
                            ip = mInetAddress.getHostAddress().replaceAll("/", "");
                            LogMgr.i("更新 客户端ip = " + ip + " mInetAddress = " + mInetAddress);
//                            sendBrainActivityIp(GlobalConfig.GET_IP);
                            break;
                        case GlobalConfig.KNOW_ROBOT_APP_FLAG: // 认识机器人
                        case GlobalConfig.PROGRAM_ROBOT_APP_FLAG: // 项目编程
                        case GlobalConfig.ABILIX_CHART_APP_FLAG:// VJC
                        case GlobalConfig.ABILIX_SCRATCH_APP_FLAG:
                        case GlobalConfig.SKILL_PLAYER_APP_FLAG:// skillplay
                        case GlobalConfig.INNER_FILE_TRANSPORT_APP_STORE:
                        case GlobalConfig.SKILL_CREATOR_APP_FLAG:
                            LogMgr.i("心跳TCP 连接成功 flag = " + flag);
                            mAppTypeConnected = flag;
                            initBrainIfo();
                            addConnectView();
                            break;
                        // 文件下载
                        case GlobalConfig.FILE_DOWNLOAD:
                            int stuts = intent.getIntExtra("file", GlobalConfig.APP_DEFAULT);
                            initFileManager(stuts);
                            break;
                        default:
                            break;
                    }
                    break;
                // 接收Activity广播
                case GlobalConfig.ACTION_SERVICE:
                    int mode = intent.getIntExtra(GlobalConfig.ACTION_SERVICE_MODE,
                            GlobalConfig.APP_DEFAULT);
                    int modeState = intent.getIntExtra(
                            GlobalConfig.ACTION_SERVICE_MODE_STATE,
                            GlobalConfig.APP_DEFAULT);

                    LogMgr.d("BrainServiceBroadcastReceiver mode=" + mode + " modeState=" + modeState);
                    LogMgr.d("BrainServiceBroadcastReceiver mIControl == null is " + (mIControl == null));
                    // if (mIControl == null || mControlForLineTrack == null || mode
                    // == GlobalConfig.APP_DEFAULT) {
                    if (mIControl == null || mode == GlobalConfig.APP_DEFAULT) {
                        LogMgr.e("BrainServiceBroadcastReceiver mIControl == null:"
                                + (mIControl == null));
                        return;
                    }

                    switch (mode) {
                        case GlobalConfig.ACTION_SERVICE_MODE_CONNECT:// 不接受任何ipad端消息
                            // LogMgr.d("onReceive() GlobalConfig.ACTION_SERVICE GlobalConfig
                            // .ACTION_SERVICE_MODE_CONNECT modeState = "+modeState);
                            // setBrainInfoState(modeState);
                            // executeControl(GlobalConfig.ACTION_SERVICE_MODE_STOP_ALL,
                            // intent, modeState);
                            break;
                        // 录音
                        case BrainData.RECORD_SUCCESS:
                            if (mBrainInfo == null) {
                                LogMgr.e("mBrainInfo == null mBrainInfo 已被销毁");
                                break;
                            }
                            mBrainInfo.completeRecord();
                            break;
                        case BrainData.RECORD_SUCCESS_PLAY:
                            if (mBrainInfo == null) {
                                LogMgr.e("mBrainInfo == null mBrainInfo 已被销毁");
                                break;
                            }
                            mBrainInfo.playscompleteRecord();
                            break;
                        default:
                            // if(mBrainInfo!=null){
                            // mBrainInfo.setBrainInfoActive(false);
                            // }
                            executeControl(mode, intent, modeState);
                            break;
                    }
                    break;
                case GlobalConfig.FIREWARE_VERSION_BROADCAST:
                    int version = (Integer) intent.getSerializableExtra("version");
                    LogMgr.d("广播收到固件的版本为 = " + version + "; 当前的版本为 = "
                            + Application.getInstance().getmFirewareVersion()
                            + "; times =" + times);
                    LogMgr.d("GlobalConfig.UPGRADE_FILE+ File.separator + pathName="
                            + FileUtils.UPGRADE_FILE
                            + File.separator
                            + Matching.pathName);
                    LogMgr.d("Matching.STM_VISION=" + Matching.STM_VISION
                            + Application.getInstance().getmFirewareVersion());
                    //版本判断不要了。
//                    mHandler.sendEmptyMessage(319);
                    if (version != -1 && version < Matching.STM_VISION) {
                        // mControl.setFileFullPath(FileUtils.UPGRADE_FILE+
                        // File.separator + Matching.pathName);
                        // 进行弹框
                        if (times < 1) {
                            if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_F && version == 0){
                                Message.obtain(mHandler, 314, 10, -1).sendToTarget();
                            }else{
                                mHandler.sendEmptyMessage(HANDLER_MESSAGE_STM32_UPDATE_NOTIFY);
                        }
                    }
                    }
                    times++;
                    if (version != -1) {
                        Application.getInstance().setmFirewareVersion(version);
                    }
                    break;
                case GlobalConfig.FIREWARE_UPDATE_BROADCAST:
                    LogMgr.d("收到固件升级的广播");
                    // /*请求control告知stm32版本号*/
                    byte state = (Byte) intent.getSerializableExtra("state");
                    String upgradeFilePath = (String) intent.getSerializableExtra("apkname");
                    LogMgr.d("state=" + state + ";upgradeFilePath=" + upgradeFilePath);
                    if(GlobalConfig.isShowFFirmwareUpdateProgress && GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_F && progressDialog!=null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                        String updateResult = state == UPGRADE_SUCESS?"升级成功":"升级失败";
                        Utils.showSingleButtonDialog(mBrainService, "升级结果", updateResult, mBrainService.getString(R.string.queren), true,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                });
                    }
                    //H5升级，加延时1S后能获取版本号
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C9 || GlobalConfig.BRAIN_TYPE ==
                            GlobalConfig.ROBOT_TYPE_H || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sendMessageToControl(
                            GlobalConfig.CONTROL_CALLBACKMODE_ASK_FOR_STM_VERSION_CMD,
                            null, null, 0, 0);

                    break;
                case GlobalConfig.FIREWARE_REQUEST_VERSION_BROADCAST:
                    LogMgr.d("收到请求control告知stm32版本号的广播");
                    sendMessageToControl(
                            GlobalConfig.CONTROL_CALLBACKMODE_ASK_FOR_STM_VERSION_CMD,
                            null, null, 0, 0);
                    break;
                case GlobalConfig.BROADCAST_ACTION_STM32_UPDATE_PROGRESS:
                    LogMgr.e("收到进度广播");
                    int progress = intent.getIntExtra(GlobalConfig.BROADCAST_ACTION_EXTRA_NAME_STM32_UPDATE_PROGRESS,0);
                    LogMgr.e("progress = "+progress);
                    if(GlobalConfig.isShowFFirmwareUpdateProgress && GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_F && progressDialog!=null){
                        SeekBar seekBar = (SeekBar) progressDialog.getWindow().findViewById(R.id.progressbar_no_button_progress_dialog);
                        TextView textView = (TextView) progressDialog.getWindow().findViewById(R.id.tv_no_button_progress_dialog_message);
                        seekBar.setMax(100);
                        seekBar.setProgress(progress);
                        textView.setText(String.format("正在升级stm32" + "\n" + "%d/100",(int) progress));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 更改当前连接的APP类型
     */
    public void changeConnectedAppType(int appType) {
        sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_STOP_ALL_CMD, null, null, 1, 0);
        mAppTypeConnected = appType;
    }

    /**
     * 设置BrainInfo 是否接收消息
     *
     * @param isBrainInfoActive
     */
    public void setBrainInfoState(boolean isBrainInfoActive) {
        if (mBrainInfo != null) {
            mBrainInfo.setBrainInfoActive(isBrainInfoActive);
        } else {
            LogMgr.e("setBrainInfoState() mBrainInfo == null");
        }
        ServerHeartBeatProcesser.getInstance().sendHeartBeat(ServerHandler.HEART_BEAT_TYPE_TEMP);

    }

    /**
     * 增加编程/遥控页面
     */
    protected void addConnectView() {
        if (null != BrainActivity.getmBrainActivity()) {
            BrainActivity.getmBrainActivity().addPadAppConnectView(0, 0);
        } else {
            LogMgr.e("addConnectView() null == BrainActivity.getmBrainActivity()");
        }
    }

    /**
     * TCP连接断开后的后续处理
     */
    private void afterTCPDisconnect() {
        if (null != BrainActivity.getmBrainActivity()) {
            BrainActivity.getmBrainActivity().afterTCPDisconnected();
        } else {
            LogMgr.e("afterTCPDisconnect() null == BrainActivity.getmBrainActivity()");
        }
    }

    /**
     * BrainInfo 销毁
     */
    protected void BrainInfoDestroy() {
        if (mBrainInfo != null) {
            mBrainInfo.setBrainInfoActive(false);
            mBrainInfo.destroy();
            mBrainInfo = null;
            mBuilder = null;
        }
    }

    /**
     * 执行Control 底层
     *
     * @param mode
     * @param intent
     */
    private void executeControl(int mode, Intent intent, int modeState) {
        switch (mode) {
            case GlobalConfig.ACTION_SERVICE_MODE_STOP_ALL:
                // break;
            case GlobalConfig.ACTION_SERVICE_MODE_REST:
            case GlobalConfig.ACTION_SERVICE_MODE_SOUL:
                exeFilePageFunc(null, mode, modeState);
                break;
            default:
                String filePath = intent
                        .getStringExtra(GlobalConfig.ACTION_SERVICE_FILE_FULL_PATH);
                exeFilePageFunc(filePath, mode, modeState);
                break;
        }
    }

    /**
     * 执行页面功能
     *
     * @param filePath
     * @param mode      GlobalConfig.ACTION_SERVICE_MODE_CHART: // 执行chart文件
     *                  GlobalConfig.ACTION_SERVICE_MODE_PROGRAM: // 执行program文件
     *                  GlobalConfig.ACTION_SERVICE_MODE_SCRATCH: // 执行scratch文件
     *                  GlobalConfig.ACTION_SERVICE_MODE_SKILLPLAYER: // 执行skill
     *                  GlobalConfig.ACTION_SERVICE_MODE_STOP_ALL: //停止所有
     * @param modeState 0 关闭 1 开启
     */
    public synchronized void exeFilePageFunc(String filePath, int mode,
                                             int modeState) {
        LogMgr.i("exeFilePageFunc() filePath = " + filePath + " mode = " + mode
                + " modeState = " + modeState);
        // if (mIControl.asBinder().isBinderAlive()) {
        try {
            byte[] sendByte = null;
            if (mode == GlobalConfig.ACTION_SERVICE_MODE_SKILLPLAYER) {
                if (modeState == 0) {
                    sendByte = new byte[]{(byte) 0x00};
                } else {
                    StringBuffer sb = new StringBuffer();
                    String str = loopFile(filePath);
                    byte[] tou = {GlobalConfig.APP_DEFAULT, skillplayer_type, 0, 0, 0};
                    sb.append(
                            filePath.substring(filePath.lastIndexOf("/") + 1,
                                    filePath.length())).append("\\")
                            .append(str);
                    byte[] data = BrainUtils.byteMerger(tou, sb.toString()
                            .getBytes());
                    sendByte = data;
                }
            }
            sendMessageToControl(mode, sendByte, filePath, modeState, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // }
    }

    private String loopFile(String name) {
        int conut = 0;
        StringBuffer sb = new StringBuffer();
        File filePath = new File(name);
        String[] fileArray = filePath.list();
        String wav = null;
        for (String n : fileArray) {
            if (n.endsWith(".bin")) {
                sb.append(n);
                skillplayer_type = 1;
                conut++;
            } else if (n.toLowerCase().endsWith(".wav")
                    || n.toLowerCase().endsWith(".mp3")) {
                wav = n;
                skillplayer_type = 2;
                conut++;
            }
        }
        if (wav != null) {
            sb.append("&").append(wav);
        }
        if (conut == 2) {
            skillplayer_type = 3;
        }
        return sb.toString();
    }

    /**
     * 发送广播消息给MainActivity
     */
    private void SendBroadCastToActivity(int mode, File file) {
        String filePath = file.getAbsolutePath();
        Intent sendIntent = new Intent(GlobalConfig.ACTION_ACTIVITY);
        sendIntent.putExtra(GlobalConfig.ACTION_ACTIVITY_MODE, mode);
        sendIntent.putExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH, filePath);
        sendBroadcast(sendIntent);
        LogMgr.d("SendBroadCastToActivity mode=" + mode + " filePath=" + filePath);
    }
    /**
     * 发送广播消息给BrainActivity
     */
    private void SendBroadCastToActivity(int mode) {
        Intent sendIntent = new Intent(GlobalConfig.ACTION_ACTIVITY);
        sendIntent.putExtra(GlobalConfig.ACTION_ACTIVITY_MODE, mode);
        sendBroadcast(sendIntent);
        LogMgr.d("SendBroadCastToActivity mode=" + mode);
    }

    /**
     * 文件下载成功
     *
     * @author luox
     */
    private class FileUpdateSuccess implements FileCallback {

        @Override
        public void downloadSuccess(File file, int stauts) {
            // 告知 BrainActivity 刷新界面
            if (file != null) {
                switch (stauts) {
                    case GlobalConfig.FILE_DOWNLOAD:
                        BrainUtils.utilisToast(getString(R.string.xiazaichenggong),
                                mContext);
                        SendBroadCastToActivity(GlobalConfig.FILE_DOWNLOAD, file);
                        // String filePath = file.getAbsolutePath();
                        // if (filePath != null) {
                        // SharedPreferenceTools.saveString(
                        // BrainActivity.getmBrainActivity(), "file_success",
                        // filePath);
                        // }
                        break;
                    case GlobalConfig.FILE_DOWNLOAD_S:
                        BrainUtils.utilisToast(getString(R.string.xiazaichenggong),
                                mContext);
                        break;
                    case GlobalConfig.FILE_DOWNLOAD_U:
                        BrainUtils.utilisToast(getString(R.string.xiazaishibai),
                                mContext);
                        break;
                }
                mFileManager = null;
            }
        }
    }


    /**
     * 获取客户端IP地址
     */
    public String getIp() {
        return ip;
    }

    /**
     * 停止 在扫码连接第一步中创建的 重建热点的Timer
     */
    public void stopRecreateHotSpotTimer() {
        LogMgr.i("stopRecreateHotSpotTimer() 心跳TCP连接成功，停止在扫码连接第一步中创建的重建热点的Timer 保存当前路由器名字、密码");
        mBrainServiceHelper.stopRecreateHotSpotTimer();
    }

    /**
     * 开始绑定
     */
    private void startBindingControl() {
        if (mBindingControl == null) {
            mBindingControl = new BindingControl();
            mBindingControl.isB = false;
            if (!mBindingControl.isAlive()) {
                mBindingControl.start();
            }
        }
    }

    /**
     * 开始绑定M
     */
    private void startBindingM() {
        if (mBindingM == null) {
            mBindingM = new BindingM();
            mBindingM.isMB = false;
            if (!mBindingM.isAlive()) {
                mBindingM.start();
            }
        }
    }

    /**
     * 停止绑定
     */
    private void stopBindingControl() {
        if (mBindingControl != null) {
            mBindingControl.isB = true;
            mBindingControl = null;
        }
    }

    /**
     * 停止绑定M
     */
    private void stopBindingMService() {
        if (mBindingM != null) {
            mBindingM.isMB = true;
            mBindingM = null;
        }
    }

    /**
     * 绑定 Control的线程
     *
     * @author luox
     */
    class BindingControl extends Thread {
        private boolean isB;

        @Override
        public void run() {
            while (!isB) {
                startControlService();
            }


        }
    }

    /**
     * 绑定 M的线程
     */
    class BindingM extends Thread {
        private boolean isMB;

        @Override
        public void run() {
            while (!isMB) {
                startMService();
            }


        }
    }

    private String mImageFile = null;
    private Timer batteryTimer;
    private TimerTask batteryTimerTask;
    private long m_millisecond0 = 2000; // 200ms定时器
    public static int batteryColor = 0;
    public final static String MOVEBIN_DIR_GROUP = "Abilix" + File.separator + "Download" + File.separator;
    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case 0://控制对应的播放bin文件
                    String file = msg.obj.toString() + ".bin";
                    int loopC = msg.arg1;
//                    DataBuffer.stopSction = true;
                    LogMgr.e("file = " + file);
                    sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, MOVEBIN_DIR_GROUP + file, 30, 0);//对应的H56，下蹲
//                    ProtocolUtils.controlSkillPlayer((byte) 0x11, MOVEBIN_DIR_GROUP + file);//11代表的是开始，10代表的是结束
//                    ProtocolUtils.controlSkillPlayer((byte) 0x11, MOVEBIN_DIR + "H_walk.bin");
                case 1:

                    break;
                case 2:

                    break;
//                case 3:
//                    mDownLoadServerTcp.mDownLoadClient.controlHandler = mServerTcp.mAcceptClient.mHandler;
//                    break;
//                case 4:
//                    mServerTcp.mAcceptClient.downLoadHandler = mDownLoadServerTcp.mDownLoadClient.mHandler;
//                    break
                case 5://归零
                    sendMessageToControl(15, null, null, 22, 0);//此条信息用于开机时播放归零bin文件；
//                    freeAndZero(3);
//                    if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H){
//
////                                            sendMessageToControl(
////                            GlobalConfig.ACTION_SERVICE_MODE_OLD_PROTOCOL,
////                            ProtocolUtil.buildProtocol((byte) GlobalConfig.ROBOT_TYPE_H,(byte)0xA3,(byte)0x78,new byte[]{0x02,0x03,0x00}), null, 1, 0);
//                    }else{
//
//                    }
                    break;
                case 6:// 释放
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H) {
                        try{
//                            byte[] data = new byte[]{(byte)10,(byte)13,0x02,(byte)14,0x02,(byte)15,0x02,(byte)16,0x02,
//                                    (byte)17,0x02,(byte)18,0x02,(byte)19,0x02,(byte)20,0x02,(byte)21,0x02,(byte)22,0x02};
                            byte[] data = new byte[1];
                            data[0] = 0;
                            for (int i = 0; i < 1; i++) {
                                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                        ProtocolUtil.buildProtocol((byte)0x03,(byte) 0xA3, (byte) 0x65,data), null, 0, 0);//释放
//                                ProtocolUtils.write(ProtocolUtils.buildProtocol((byte)0x03,(byte)0xA3,(byte)0x65,data));
                                Thread.sleep(50);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        freeAndZero(2);
                    }
                    break;
                case 7:// 固定
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H) {
                        byte[] data = new byte[1];
                        data[0] = 1;
                        BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                ProtocolUtil.buildProtocol((byte)0x03,(byte) 0xA3, (byte) 0x65,data), null, 0, 0);//固定
                    }else {
                        freeAndZero(1);
                    }
                    break;
                case 8:// 检测当前电量
                    int batteryPct = SharedPreferenceTools.getInt(mBrainService, SharedPreferenceTools
                            .SHAREDPREFERENCE_KEY_ELECTRICITY, 0);
                    //根据电量的 多少显示不同的颜色，可以开启计时器
                    if (batteryPct < 30) {
                        batteryColor = 1;//R
                    } else if (batteryPct < 70) {
                        batteryColor = 2;//G
                    } else {
                        batteryColor = 3;//B
                    }
                    if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H){
                        RGBEyesBlink(batteryColor);
                    }else {
                    startBatteryTimer();
                    }
                    break;
                case 9:// 关闭电池检测
                    //关闭计时器
                    if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H){
                        RGBEyesBlink(0);
                    }else {
                    stopBatteryTimer();
                    }
                    break;
                case 10:// 停止动作
//                    DataBuffer.stopSction=false;
                    ProtocolUtils.controlSkillPlayer((byte) 0x10, null);
                    break;
                case 11:// 打开最大声音
                    AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);
                    break;
                case 12:// 最小声音
                    AudioManager am1 = (AudioManager) getSystemService(AUDIO_SERVICE);
                    am1.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    break;
                case 15://设置群控,弹框设置分组；
                    if (dialog != null) {
                        return;
                    }
                    showPwdInitDialog();
                    break;
                case 17://代表正在下载,红色/蓝灯
                    if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H){
                        RGBEyesBlink(3);
                    }else {
                        ProtocolUtils.sendProtocol((byte)0x03,(byte) 0xA3, (byte) 0x74,new byte[]{(byte)0xFF,0,0});
                    }
                    break;
                case 18://代表下载完成或者已经有的，绿色
                    if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H){
                        RGBEyesBlink(2);
                    }else {
                        ProtocolUtils.sendProtocol((byte)0x03,(byte) 0xA3, (byte) 0x74,new byte[]{0,(byte)0xFF,0});
                    }
                    break;
                case 20://对应的是H6充电保护下蹲
//                    LogMgr.e("20");
                    sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, null, 28, 0);//对应的H56，下蹲
                    break;
                case 21://对应的是H6充电保护站立
                    sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, null, 29, 0);//对应的H56，站立
                    break;
                case 22://对应的是H6低电量时给Brainset发送广播；
                    Intent intent = new Intent("com.abilix.abilixbattey");
                    intent.setPackage("com.abilix.brainset");
                    intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mBrainService.sendBroadcast(intent);
//                    RGBEyesBlink(4);
                    headLED(1);
                    break;
                case 23://发送指令13，关闭所有
                    sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_STOP_ALL_CMD, null, null, 0, 0);
                    break;
                case 24://手脚灯光亮
//                    RGBEyesBlink(4);
                    BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_REST_CMD, null,
                            null, 3, 0);//这里是开启手部脚部灯光；
                    break;
                case 80:
                case 81:
                case 82:
                case 83:
                case 84:
                    playRomate(msg.what);
                    break;

                // 认识机器人
                case 0x200:
//                    sendBrainActivityIp(GlobalConfig.GET_IP);
                    break;
                // SCRATCH
                case 0x400:
                    initBrainIfo();
                    break;
                // 项目编程
                case 0x101:
                    initFileManager(0x01);
                    // initBrainIfo(BrainData.DATA_ONE);
                    break;
                // Control Death
                case 0x404:
                    Toast.makeText(mContext, "Control Death", Toast.LENGTH_SHORT).show();
                    break;
                // vjc 寻线采集数据 询问是否环境采集
                case HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION:
                    MyAlertDialogs.setAlertDialog1(mContext, mHandler);
                    break;
                case HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_SHOW_DATA:
                    MyAlertDialogs.setAlertDialog4((String) msg.obj, mContext);
                    MyAlertDialogs.close();
                    break;
                // 写入 Control
                case HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_BEGIN:
                    setControlData(msg.arg1, 3);
                    break;
                // 平衡车
                case HANDLER_MESSAGE_BLANCE_CAR:
                    balanceCar(msg);
                    break;
                case HANDLER_MESSAGE_BLANCE_CAR_STOP:
                    stopBalanceCar(msg);
                    break;
                // 指南针校准
                case HANDLER_MESSAGE_COMPASS:
                    MyAlertDialogs.setAlertDialog6(mContext, true, mHandler, msg.arg1);
                    break;
                case 314:
                    // vjc
                    if (msg.arg1 == 1) {
                        setControlData(4, 3);
                    }// scratch
                    else if (msg.arg1 == 2) {
                        if (msg.arg2 == 1) {
                            setControlData(5, 12);
                        } else {
                            setControlData(4, 12);
                        }
                    } else if (msg.arg1 == 10) {
                        // setControlData(0, 10);
                        LogMgr.e(FileUtils.UPGRADE_FILE + File.separator + Matching.pathName);
                        /** 固件升级 **/
                        sendMessageToControl(
                                GlobalConfig.CONTROL_CALLBACKMODE_ASK_FOR_STM_UPDATE_CMD,
                                null, FileUtils.UPGRADE_FILE + File.separator
                                        + Matching.pathName, 0, 0);
//                        sendBrainActivityIp(GlobalConfig.UPGRADE_STOP_REST);
                        if(GlobalConfig.isShowFFirmwareUpdateProgress && GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_F){
                            progressDialog = Utils.showNoButtonProgressDialog(BrainService.getmBrainService(),"升级","正在升级stm32",true);
                        }
                    } else if (msg.arg1 == GlobalConfig.ACTION_SERVICE_POWER_OFF) {
                        /** 关机指令 **/
                        sendMessageToControl(GlobalConfig.ACTION_SERVICE_POWER_OFF,
                                null, null, 0, 0);
                        // setControlData(5, GlobalConfig.ACTION_SERVICE_POWER_OFF);
                    }
                    break;
                case HANDLER_MESSAGE_USB_CAMERA_PHOTO_TAKE:
                    if (msg.arg1 == 1) {
                        Photograph_State = 2;
                        // File file = new File(FileUtils.SCRATCH_VJC_IMAGE_);
                        // if (!file.exists()) {
                        // file.mkdir();
                        // }
                        // file = null;
                        mImageFile = FileUtils.SCRATCH_VJC_IMAGE_ + msg.arg2
                                + FileUtils.SCRATCH_VJC_IMAGE_JPG;
                        LogMgr.e("--> mImageFile:" + mImageFile);
//                        USBVideo.GetManger(BrainActivity.getmBrainActivity(), null,
//                                mHandler).getCamera(mImageFile);
                        UsbCameraTakePicture(mImageFile);
                    } else if (msg.arg1 == 0) {
                        Photograph_State = -1;
                        //       USBVideo.stopCamera();
                    }
                    break;
                // m h 拍照
                case HANDLER_MESSAGE_SYSTEM_CAMERA_PHOTO_TAKE:
                    mh_Photograph(msg);
                    break;
                // stm Update
                case HANDLER_MESSAGE_STM32_UPDATE_NOTIFY:
                    if (ad6 == null) {
                        ad6 = MyAlertDialogs.setAlertDialog9(mContext, true,
                                mHandler);
                        ad6.show();
                    }
                    break;
                case HANDLER_MESSAGE_QUERY_STM32_VERSION:
                    LogMgr.i("查询STM32版本号");
                    sendMessageToControl(
                            GlobalConfig.CONTROL_CALLBACKMODE_ASK_FOR_STM_VERSION_CMD,
                            null, null, 0, 0);
                    break;
                case HANDLER_MESSAGE_H34_BOOT_RECOVER:
                    sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, null, 22, 0);//此条信息用于开机时播放归零bin文件；
                    break;
                case HANDLER_MESSAGE_M_BLOCKED_CONFIM:
                    LogMgr.d("M轮子保护提示框确认按钮被按。");
                    sendMessageToControl(
                            GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null,
                            null, 1, 0);
                    // try {
                    // mControl.setCallBackMode(15);
                    // mControl.setModeState(1);// ModeState 1:通知control M 去除掉轮子保护
                    // // mIControl.ControlInterface(mControl);
                    // if (mSendMessageToControlHandler != null) {
                    // Message.obtain(mSendMessageToControlHandler, 0,
                    // mControl).sendToTarget();
                    // }
                    // } catch (Exception e) {
                    // e.printStackTrace();
                    // }
                    break;
                case HANDLER_MESSAGE_M_BLOCKED_NOTIFY:
                    LogMgr.d("M轮子进入保护状态 step2");
                    // 改变M轮子状态变量
                    Application.getInstance().setMWheelProtected(true);
                    // 弹出提示窗口
                    MyAlertDialogs.setAlertDialog10(mContext, mHandler);
                    // 播放提示声音
                    Utils.playSoundFile(mContext,
                            GlobalConfig.M_ROBOT_STUCK_ZH_FILENAME,
                            GlobalConfig.M_ROBOT_STUCK_EN_FILENAME);
                    break;
                case 505:
//                    USBVideo.GetManger(BrainActivity.getmBrainActivity(), null,
//                            null).setBrightnessS(msg.arg1 & 0xff);
//                    USBVideo.GetManger(BrainActivity.getmBrainActivity(), null,
//                            null).setBrightnessS(msg.arg1 & 0xff);
                    UsbCamera.create().setBrightnessS(Application.getInstance(), msg.arg1 & 0xff, mCameraStateCallBack);
                    break;
                case 506://506 暂时表示H56摄像头的颜色检测
//                    getColor(msg.arg1);
                    break;
                case 119:
                    //高温提醒图片弹出提示窗口
                    LogMgr.d("高温提醒图片弹出提示窗口");
                    MyAlertDialogs.setAlertDialog11(mContext, mHandler);
//                    Utils.playSoundFile(mContext,
//                                    GlobalConfig.C_ROBOT_ZH_HEITEMP,
//                                    GlobalConfig.C_ROBOT_EN_HEITEMP);
                    switch (GlobalConfig.BRAIN_TYPE) {
                        case GlobalConfig.ROBOT_TYPE_M:
                            Utils.playSoundFile(mContext,
                                    GlobalConfig.M_ROBOT_ZH_HEITEMP,
                                    GlobalConfig.M_ROBOT_EN_HEITEMP);
                            break;
                        default:
                            Utils.playSoundFile(mContext,
                                    GlobalConfig.C_ROBOT_ZH_HEITEMP,
                                    GlobalConfig.C_ROBOT_EN_HEITEMP);
                            break;
                    }
                    break;
                case 120:
                    LogMgr.d("高温提醒图片关闭提示窗口");
                    MyAlertDialogs.setAlertDialog11close(mContext);
                    break;
                case 121:
                    Utils.releaseMedia();
                    break;

                case WHAT_HANDLE_STATUS:
                    Integer status = (Integer) msg.obj;
                    try {
                        MUtils.handleStatus(status);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    private void headLED(int color){
        byte[] headColor = new byte[6];//这里显示的双眼的灯
        headColor[0] = 2;
        headColor[1] = 1;
        headColor[2] = 1;
        switch (color){
            case 1:
                headColor[3] = (byte)0xFF;
                break;
            case 2:
                headColor[4] = (byte)0xFF;
                break;
            case 3:
                headColor[5] = (byte)0xFF;
                break;
            default:
                headColor[3] = (byte)0;
                headColor[4] = (byte)0;
                headColor[5] = (byte)0;
                break;
        }
        ProtocolUtils.write(ProtocolUtils.buildProtocol((byte)0x03,(byte)0xA3,(byte)0xC0,headColor));
    }
    private void RGBEyesBlink(int batteryColor) {
        byte[] color = new byte[10];//这里显示的双眼的灯
        color[0] = 2;
        color[1] = 2;
        color[2] = 2;
        color[6] = 3;
        switch (batteryColor){
            case 1://打开-低电量
            case 4://低电量检测10%
                color[3] = (byte)0xFF;
                color[7] = (byte)0xFF;
                break;
            case 2://打开-中电量
                color[4] = (byte)0xFF;
                color[8] = (byte)0xFF;
                break;
            case 3://打开-高电量
                color[5] = (byte)0xFF;
                color[9] = (byte)0xFF;
                break;
            case 0://关闭电量检测
                color[3] = (byte)0;
                color[4] = (byte)0;
                color[5] = (byte)0;
                color[7] = (byte)0;
                color[8] = (byte)0;
                color[9] = (byte)0;
                break;
        }
        ProtocolUtils.write(ProtocolUtils.buildProtocol((byte)0x03,(byte)0xA3,(byte)0xC0,color));
    }

    private void playRomate(int mode) {
        switch (mode) {
            case 80://前进
                ProtocolUtils.controlSkillPlayer(FileUtils.SKILL_PLAYER_ACTION_PLAY, FileUtils.MOVEBIN_DIR + "H_walk.bin");
                break;
            case 81://左转
                ProtocolUtils.controlSkillPlayer(FileUtils.SKILL_PLAYER_ACTION_PLAY, FileUtils.MOVEBIN_DIR + "H_zuoyi.bin");
                break;
            case 82://后退
                ProtocolUtils.controlSkillPlayer(FileUtils.SKILL_PLAYER_ACTION_PLAY, FileUtils.MOVEBIN_DIR + "H_backwalk.bin");
                break;
            case 83://右移
                ProtocolUtils.controlSkillPlayer(FileUtils.SKILL_PLAYER_ACTION_PLAY, FileUtils.MOVEBIN_DIR + "H_youyi.bin");
                break;
            case 84://停止
                ProtocolUtils.controlSkillPlayer(FileUtils.SKILL_PLAYER_ACTION_STOP, null);
//                ProtocolUtils.controlSkillPlayer(FileUtils.SKILL_PLAYER_ACTION_PLAY, FileUtils.MOVEBIN_DIR + "H_walk.bin");
                break;
        }
    }

    private AlertDialog dialog;

    private void showPwdInitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BrainActivity.getmBrainActivity());
        View view = View.inflate(BrainActivity.getmBrainActivity(), R.layout.dialog_groupcontrol_pwd, null);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCancelable(false);
        Button mBtOK = (Button) view.findViewById(R.id.pwd_init_ok);
        Button mBtCancel = (Button) view.findViewById(R.id.pwd_init_cancel);
        final EditText mEtPwd = (EditText) view.findViewById(R.id.pwd_init_first);
        final EditText mEtPwdAgain = (EditText) view.findViewById(R.id.pwd_init_second);
        mBtOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //首先判断组号是否为空
                String pwd = mEtPwd.getText().toString().trim();
                if(TextUtils.isEmpty(pwd)){
                    mEtPwd.requestFocus();
                    return;
                }
                //判断两次输入组号是否一致
//                String pwdAgain = mEtPwdAgain.getText().toString().trim();
//                if(!pwd.equals(pwdAgain)){
//                    Toast.makeText(getApplicationContext(), "密码不一致！", Toast.LENGTH_SHORT).show();
//                    mEtPwdAgain.requestFocus();
//                    return;
//                }
                //保存组号
                SharedPreferenceTools.saveString(mBrainService, "group", pwd);
                dialog.dismiss();
                dialog.cancel();
                dialog = null;
            }
        });
        mBtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //对话框消失
                dialog.dismiss();
                dialog.cancel();
                dialog = null;
            }
        });
        dialog.show();
    }

    private void startBatteryTimer() {
            try {
                byte[] eyergb = new byte[3];
                eyergb[batteryColor - 1] = (byte)0xFF;
                for (int i = 0; i < 3; i++) {
                    ProtocolUtils.write((byte) 0x03, (byte) 0xA3, (byte) 0x74, eyergb);
                    Thread.sleep(50);
            }
            } catch (InterruptedException e) {
                e.printStackTrace();
    }
//        stopBatteryTimer();
//        batteryTimer = new Timer();
//        batteryTimerTask = new TimerTask() {
//            @Override
//            public void run() {
//                byte[] eyergb = new byte[3];
//                eyergb[batteryColor] = (byte)0xFF;
//                ProtocolUtils.sendProtocol((byte)0x03,(byte) 0xA3, (byte) 0x74,eyergb);
//            }
//        };
//        batteryTimer.schedule(batteryTimerTask,0,m_millisecond0);
    }

    private void stopBatteryTimer() {
//        if(batteryTimer != null){
//            batteryTimer.cancel();
//            batteryTimer = null;
//        }
//        if(batteryTimerTask != null){
//            batteryTimerTask.cancel();
//            batteryTimerTask = null;
//        }
//        ProtocolUtils.sendProtocol((byte)0x03,(byte) 0xA3, (byte) 0x74,new byte[]{0,0,0});
        try{
            for (int i = 0; i < 3; i++) {
                ProtocolUtils.write((byte)0x03,(byte) 0xA3, (byte) 0x74,new byte[]{0,(byte)0xFF,0});
                Thread.sleep(50);
        }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void freeAndZero(int type) {//固定  释放  归零
        byte[] dataALL = new byte[45];
        dataALL[0] = 22;
        for (int i = 1; i <= 22; i++) {
            dataALL[2 * i - 1] = (byte) i;
            dataALL[2 * i] = (byte) type;
        }
        ProtocolUtils.write((byte) 0x05, (byte) 0xA3, (byte) 0x65, dataALL);
    }

    private CameraStateCallBack mCameraStateCallBack = new CameraStateCallBack() {
        @Override
        public void onState(int state) {
            com.abilix.explainer.utils.LogMgr.d("相机状态回调：" + state);
        }
    };

    //弹框提示相关
    private Handler alerDialogHandler = new Handler(Looper.getMainLooper());
    Runnable disAd4Runnable = new Runnable() {
        @Override
        public void run() {
            if (ad4 != null) {
                ad4.dismiss();
                ad4 = null;
            }
        }
    };
    Runnable disAd5Runnable = new Runnable() {
        @Override
        public void run() {
            if (ad5 != null) {
                ad5.dismiss();
                ad5 = null;
            }
        }
    };

    private void UsbCameraTakePicture(String imagePath) {
        alerDialogHandler.removeCallbacks(disAd4Runnable);
        alerDialogHandler.removeCallbacks(disAd5Runnable);
        UsbCamera.create().takePicture(Application.getInstance(), mImageFile, new CameraStateCallBack() {
            @Override
            public void onState(int state) {
                LogMgr.d("usb camera take picture state:" + state);
                switch (state) {
                    //未插入Usb摄像头，弹框提示
                    case RobotCameraStateCode.TAKE_PICTURE_USB_CAMERA_IS_NOT_CONNECTED:
                        alerDialogHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (ad5 != null) {
                                    ad5.dismiss();
                                    ad5 = null;
                                }
                                if (ad4 == null) {
                                    ad4 = MyAlertDialogs.setAlertDialog8(mContext,
                                            getString(R.string.shexiangtou),
                                            getString(R.string.shexiangtoushifouchahao), true);
                                }
                            }
                        });

                        //4秒后，弹框自动消失
                        alerDialogHandler.postDelayed(disAd4Runnable, 4000);
                        break;

                    //Usb摄像头正在初始化，弹框提示
                    case RobotCameraStateCode.OPENING_CAMERA:
                        alerDialogHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (ad4 != null) {
                                    ad4.dismiss();
                                    ad4 = null;
                                }
                                if (ad5 == null) {
                                    ad5 = MyAlertDialogs.setAlertDialog8(mContext,
                                            getString(R.string.shexiangtou), "\t"
                                                    + getString(R.string.shexiangtouzhengzai),
                                            true);
                                }
                            }
                        });

                        //12妙后，弹框自动消失
                        alerDialogHandler.postDelayed(disAd5Runnable, 12000);
                        break;

                    //拍照完成，显示照片
                    case RobotCameraStateCode.SAVE_PICTURE_SUCESS:
                        if (ad4 != null) {
                            ad4.dismiss();
                            ad4 = null;
                        }
                        if (ad5 != null) {
                            ad5.dismiss();
                            ad5 = null;
                        }
                        if (mImageFile != null) {
                            File file = new File(mImageFile);
                            if (!file.exists()) {
                                file = null;
                                LogMgr.e("照片文件不存在");
                                return;
                            } else {
                                LogMgr.d(" 显示照片：" + file.getAbsolutePath());
                                if (Photograph_State == 2) {
                                    SendBroadCastToActivity(
                                            BrainUtils.SCRATCH_VJC_IMAGEVIEW, file);
                                }
                            }
                        }
                        break;
                }

            }
        });
    }


    private IRobotCamera mSystemCamera;

    /**
     * MH 拍照
     *
     * @param msg
     */
    private final void mh_Photograph(Message msg) {
        LogMgr.d("系统相机拍照");
        if (msg.arg1 == 1) {
            Photograph_State = 1;
            final String imgPath = FileUtils.SCRATCH_VJC_IMAGE_
                    + msg.arg2 + FileUtils.SCRATCH_VJC_IMAGE_JPG;
            //如果是m系列，需要旋转90°
            if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M
                    && GlobalConfig.BRAIN_CHILD_TYPE != GlobalConfig.ROBOT_TYPE_M3S
                    && GlobalConfig.BRAIN_CHILD_TYPE != GlobalConfig.ROBOT_TYPE_M4S) {
                SystemCamera.create().setIsRotate(true);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemCamera.create().takePicture(Application.getInstance(), imgPath, new CameraStateCallBack() {
                        @Override
                        public void onState(int state) {
                            LogMgr.d("拍照状态回调：" + state);
                            switch (state) {
                                case RobotCameraStateCode.SAVE_PICTURE_SUCESS:
                                    File file = new File(imgPath);
                                    if (!file.exists()) {
                                        LogMgr.e("照片文件不存在");

                                        return;
                                    } else {
                                        if (Photograph_State == 1) {
                                            LogMgr.d("显示照片：" + file.getAbsolutePath());
                                            SendBroadCastToActivity(
                                                    BrainUtils.SCRATCH_VJC_IMAGEVIEW, file);
                                        }
                                        SystemCamera.create().destory();
                                    }
                                    if (ad5 != null) {
                                        ad5.dismiss();
                                        ad5 = null;
                                    }
                                    break;
                                case RobotCameraStateCode.OPENING_CAMERA:
                                    alerDialogHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (ad5 == null) {
                                                ad5 = MyAlertDialogs.setAlertDialog8(mContext,
                                                        getString(R.string.shexiangtou), "\t"
                                                                + getString(R.string.shexiangtouzhengzai),
                                                        true);
                                            }
                                        }
                                    });

                                    //12妙后，弹框自动消失
                                    alerDialogHandler.postDelayed(disAd5Runnable, 12000);
                                    break;
                                default:

                                    break;
                            }

                        }
                    });
                }
            }).start();
        } else if (msg.arg1 == 0) {
            Photograph_State = -1;
            SystemCamera.create().destory();
        }
    }

//    private void getColor(int color_id) {
//        LogMgr.i("getColor");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ((SystemCamera) SystemCamera.create()).preview(Application.getInstance(), new PreviewCallback() {
//                    @Override
//                    public void onPreviewFrame(byte[] data) {
//                        int result = clrdetect.getInstance().clrdetect(data, 240, 320, new int[]{-1, -1, -1, -1});
//                        LogMgr.i("result = " + result);
////                        int colorid =
////                        mBrainInfo.returnDataToClient();
//                    }
//                });
//            }
//        }).start();
//    }

    /**
     * 平衡车
     *
     * @param msg
     */
    private final void balanceCar(Message msg) {
        if (msg.arg1 == 1) {
            isBlanct = (boolean) msg.obj;
            if (ad1 == null) {
                ad1 = MyAlertDialogs.setAlertDialog5(
                        BrainActivity.getmBrainActivity(),
                        getString(R.string.pinghengchezhengzai), true, 0);
//				ad1.show();
            }
        } else if (msg.arg1 == 2) {
            if (ad1 != null) {
                ad1.dismiss();
                ad1 = null;
            }
            if (ad2 == null) {
                ad2 = MyAlertDialogs.setAlertDialog5(
                        BrainActivity.getmBrainActivity(),
                        getString(R.string.pinghengchenggong), true, 0);
//				ad2.show();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLER_MESSAGE_BLANCE_CAR, 3, 0),
                        3500);
            }
        } else if (msg.arg1 == 3) {
            if (ad2 != null) {
                ad2.dismiss();
                ad2 = null;
            }
        }
    }

    /**
     * 平衡车 关闭
     *
     * @param msg
     */
    private final void stopBalanceCar(Message msg) {
        if (msg.arg1 == 3) {
            if (ad3 != null) {
                ad3.dismiss();
                ad3 = null;
            }
            if (ad1 != null) {
                ad1.dismiss();
                ad1 = null;
            }
            if (ad2 != null) {
                ad2.dismiss();
                ad2 = null;
            }
        } else {
            if (isBlanct) {
                if (ad1 != null) {
                    ad1.dismiss();
                    ad1 = null;
                }
                if (ad3 == null) {
                    ad3 = MyAlertDialogs.setAlertDialog5(
                            BrainActivity.getmBrainActivity(),
                            getString(R.string.pinghengshibai), true, 0);
//					ad3.show();
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(HANDLER_MESSAGE_BLANCE_CAR_STOP, 3, 0), 3500);
                }
            }
        }
    }

    /**
     * 寻线
     *
     * @param z = 1 第一次采集, -1 不采集 , 2 第二次采集, 4 指南针, 0 stm 更新, 关机指令 ,
     */
    public final void setControlData(int z, int mode) {
        try {
            if (null == mControlForLineTrack) {
                mControlForLineTrack = new Control(mode,
                        new byte[]{(byte) 0x00});
            }
            if (z == 1) {
                mControlForLineTrack.setControlFuncType(mode);
                mControlForLineTrack.setmCmd(1);
            } else if (z == 2) {
                mControlForLineTrack.setControlFuncType(mode);
                mControlForLineTrack.setmCmd(2);
            } else if (z == -1) {
                mControlForLineTrack.setControlFuncType(mode);
                mControlForLineTrack.setmCmd(3);
            } else if (z == 4) {
                mControlForLineTrack.setControlFuncType(mode);
                mControlForLineTrack.setmCmd(4);
                mControlForLineTrack.setModeState(1);
            } else if (z == 5) {
                mControlForLineTrack.setControlFuncType(mode);
                mControlForLineTrack.setmCmd(5);
                mControlForLineTrack.setModeState(1);
            }

            sendMessageToControl(mControlForLineTrack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送信息至Control
     *
     * @param controlFuncType 0:代表brain传给control的是老协议数据 mSendByte[] 保存命令数据</br>
     *                        1:代表brain传给control的是新协议数据已经解析完成只包含 命令字1 命令字2 以及 参数 </br>
     *                        2:代表soul开启或关闭mModeState; //模式状态 0:关闭 1:开启 </br>
     *                        3:代表要求执行AbilixChart 文件 fileFullPath是完整文件路径名 mModeState; //模式状态 0:关闭1:开启 </br>
     *                        4:代表要求执行 AbilixScratch 命令mSendByte[] 保存命令数据 mModeState; //模式状态 0:关闭 1:开启 </br>
     *                        5:代表要求执行SkillPlayer文件fileFullPath 是完整文件路径名 mModeState; mSendByte里的参数看3.1.1 //模式状态0:关闭
     *                        1:开启</br>
     *                        6:代表告知当前是哪个系列 mSendByte[] 1个字节代表是哪个系列的 0x01:C系列0x02:M系列 0x03:H系列0x04:F系列 </br>
     *                        7:休息状态切换 mModeState; //模式状态0:关闭 1:开启 </br>
     *                        8:代表要求执行 项目编程文件 文件fileFullPath 是完整文件路径名mModeState; / /模式状态 0:关闭 1:开启 </br>
     *                        9:代表要求获取固件stm32版本号control和当前机器人类型 获取到后 发送广播消息告知版本号 </br>
     *                        10:代表要求升级stm32固件mFileFullPath代表升级固件的文件名 文件传输成功后发送广播消息告知 </br>
     *                        11:代表要求发送关机命令给 stm32 </br> 12:代表要求执行AbilixScratch文件 </br>
     *                        13:代表停止所有的</br>
     *                        14:代表学字母接口，modeState 0：停止。 1：播放。2：暂停。3：继续。</br>
     *                        15:代表不成规模的小功能。modeState 1：通知去掉M轮子点击保护。</br>
     * @param sendByte        需要发送的数据
     * @param fileFullPath    文件完整路径名
     * @param modeState       模式状态 0:机器人关闭当前的工作，或者关闭所有的工作 1:默认状态，与其他参数一起发送
     * @param cmd             默认是0，获取AI是1，否是2
     */
    public synchronized void sendMessageToControl(int controlFuncType, byte[] sendByte, String fileFullPath, int
            modeState, int cmd) {
        LogMgr.v("sendMessageToControl()1 controlFuncType = " + controlFuncType
                + " fileFullPath = " + fileFullPath + " modeState = "
                + modeState);
        if (null == sendByte) {
            sendByte = new byte[]{0x00};
        }
        Control control = new Control(controlFuncType, sendByte);
        control.setFileFullPath(fileFullPath);
        control.setModeState(modeState);
        control.setmCmd(cmd);
        sendMessageToControl(control);
    }

    /**
     * 发送信息至Control 建议不要直接使用。而是使用上面的多参函数
     *
     * @param control
     */
    public void sendMessageToControl(Control control) {
        if (mBrainServiceHelper.isSendMessageToControlHandlerInit()) {
            LogMgr.v("sendMessageToControl()");
            Message.obtain(mBrainServiceHelper.getmSendMessageToControlHandler(), 0, control).sendToTarget();
            //勿更改第二个参数。
        } else {
            LogMgr.e("mSendMessageToControlHandler 为null，所以先创建Handler，然后发送message");
            mBrainServiceHelper.startSendMessageToControlThread();
            Message.obtain(mBrainServiceHelper.getmSendMessageToControlHandler(), 0, control).sendToTarget();
        }
    }

    /**
     * 移除mSendMessageToControlHandler中的消息
     */
    public void removeMessageToSendToControlFromHandler() {
        mBrainServiceHelper.removeMessageToSendToControlFromHandler();
    }

    /**
     * 获取客户端IP地址
     */
    public InetAddress getClientIp() {
        return mInetAddress;
    }

    /**
     * 获取当前连接的app类型
     */
    public int getmAppTypeConnected() {
        return mAppTypeConnected;
    }

    /**
     * H5 开机启动手指保护功能
     */
    private void startFingerProtect() {
        if (BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_H) {
            LogMgr.e("不是H56不执行手机保护功能");
            return;
        }
        sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, null, GlobalConfig
                .STARTING_UP_FINGER_PROTECT, 0);
    }
}
