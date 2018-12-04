package com.abilix.brain;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.ColorRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abilix.brain.control.FileDownloadProcesser;
import com.abilix.brain.control.ServerHeartBeatProcesser;
import com.abilix.brain.data.AppInfo;
import com.abilix.brain.data.BasePagerAdapter;
import com.abilix.brain.data.BrainDatabaseHelper;
import com.abilix.brain.fragment.ManageFragment;
import com.abilix.brain.m.FutureCoolPlayActivity;
import com.abilix.brain.m.FutureScienceActivity;
import com.abilix.brain.m.MUtils;
import com.abilix.brain.m.MyGiftView;
import com.abilix.brain.m.TrainingActivity;
import com.abilix.brain.ui.BrainRecord;
import com.abilix.brain.ui.BrainViewPagerForBrainActivity;
import com.abilix.brain.ui.IPlayStateListener;
import com.abilix.brain.ui.IRecordStateListener;
import com.abilix.brain.ui.MyViewPager;
import com.abilix.brain.ui.PathView;
import com.abilix.brain.ui.SendPathView;
import com.abilix.brain.ui.SendVideo;
import com.abilix.brain.utils.BrainData;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.EncodingHandler;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.MusicPlayer;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.SharedPreferenceTools;
import com.abilix.brain.utils.Utils;
import com.abilix.brain.utils.WifiApAdmin;
import com.abilix.brain.utils.WifiUtils;
import com.abilix.explainer.camera.transvedio.RTSPServiceMgr;
import com.abilix.explainer.camera.transvedio.VedioTransMgr;
import com.abilix.explainer.utils.ProtocolUtils;
import com.bumptech.glide.Glide;
import com.google.zxing.WriterException;
import com.grandar.deleteservice.AIDLDeleteService;
import com.grandar.installservice.AIDLInstallService;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.abilix.brain.utils.FileUtils.SHUTDOWN;


//import com.abilix.brain.ui.USBVideo;

/**
 * Brain的主Activity，进行主界面的显示、刷新，状态变化，启动主服务、安装服务、删除服务，打开热点/WIFI,
 */
public class BrainActivity extends Activity {

    //定义BrainActivity Handler处理的Private Message
    private static final int BRAINACTIVITY_HANDLER_MESSAGE_INSERT_PAGE = 0x03;
    private static final int BRAINACTIVITY_HANDLER_MESSAGE_JUMP_TO_PAGE = 0x06;
    private static final int BRAINACTIVITY_HANDLER_MESSAGE_SET_SCROLLBLE_TRUE = 0x07;
    private static final int BRAINACTIVITY_HANDLER_MESSAGE_STOP_ANI_AND_FUNC = 0x09;
    private static final int BRAINACTIVITY_HANDLER_MESSAGE_STOP_ANI = 0x11;
    private static final int BRAINACTIVITY_HANDLER_MESSAGE_SCREEN_TEST = 0x10;
    private static final int BRAINACTIVITY_HANDLER_MESSAGE_START_PROGRAM_ANI_AND_FUNC = 0x12;


    //M系列一级页面长时间没有操作
    private static final int M_LONG_TIME_NO_OPERATION = 10000;

    //定义BrainActivity Handler处理的Public Message,主要给其他类使用
    public static final int BRAINACTIVITY_HANDLER_MESSAGE_UPDATE_THE_QRCODE = 0x1000;


    private static final String IS_FIRST_START_BRAIN_AFTER_ROOT = "isFirst";

    //    public static final int REQUEST_CODE_FOR_VJC_AND_PROGRAM_JROJECT = 0x80;
    private static final String INTENT_EXTRA_BOOLEAN_HAS_DELETED_PAGE = "intent_extra_boolean_has_deleted_page";

    /**
     * 我叫奥克流思的包名
     */
    public final static String OCULUSNAME = "com.abilix.oculus";
    /**
     * BrainActivity唯一实例
     */
    private static BrainActivity mBrainActivity;
    private static BrainService mBrainService;

    private Typeface mFromAsset;

    /*我叫奧克劉思2.0情緒*/
    private MyGiftView mMyGiftView;
    private java.lang.Runnable longTimeRunnable = new Runnable() {
        @Override
        public void run() {
            MUtils.changeMState(mMyGiftView, MUtils.MState.M_STATE_HEART_JUMP);
            MUtils.startEmotion();
        }
    };

    /**
     * 获取BrainActivity唯一实例
     */
    public static BrainActivity getmBrainActivity() {
        return mBrainActivity;
    }

    //排序相关
    private BrainDatabaseHelper mBrainDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;

    /**
     * 是否在界面最前方
     */
    private boolean isForegroundFirst = false;
    /**
     * 是否是由onPuase转到onResume
     */
    private boolean isPauseToResume = false;
    /**
     * 自动模式是否开启  ,默认开启
     */
    private boolean isAutoRest = true;

    /**
     * 是否是从App退出来的
     */


    /**
     * 是否在界面最前方
     */
    public boolean isForegroundFirst() {
        return isForegroundFirst;
    }

    //BrainActivity的Handler，所有的需要BrainActivity处理的消息，均可以发消息到该处处理
    public Handler mBrainActivityHandler = new Handler() {
        //private Handler mBrainActivityHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                     int item = mAppInfos.get(0).getPageType() == AppInfo.PAGE_TYPE_PROGRAM ? 1 : 0;
                    //转到休息界面的操作
                    mViewPager.setCurrentItem(getMiddlePlaceForFirstItem() + item, false);
                    //进入休息界面
                    startAnimaAndFunc(mAppInfos.get(item).getViewHoder().imageView_qr, mAppInfos.get(item));
                    break;
                //case 0x01:
                //    break;
                //case 0x02:
                //    break;
                case BRAINACTIVITY_HANDLER_MESSAGE_INSERT_PAGE:
                    insertPage(mAppInfos.size(), (AppInfo) msg.obj);
                    break;
                case 4://执行复位动作

                    BrainService.getmBrainService().sendMessageToControl(15, null, null, 22, 0);
                    break;
                //case 0x04:
                // deletePageView((String) msg.obj);
                //    break;
                case 0x05:
                    setStopImageView();
                    break;
                case BRAINACTIVITY_HANDLER_MESSAGE_JUMP_TO_PAGE:
                    // 跳转至第 arg1 页
                    int pageIndex = msg.arg1;
                    initUI();
                    mViewPager.setCurrentItem(getMiddlePlaceForFirstItem() + pageIndex);
                    break;
                case BRAINACTIVITY_HANDLER_MESSAGE_SET_SCROLLBLE_TRUE:
                    if (mViewPager != null) {
                        mViewPager.setScrollble(true);
                    } else {
                        LogMgr.e("mViewPager == null");
                    }
                    break;
                // 录音
                //case 0x08:
                // String hancor = Mb
                // if (mBrainRecord != null) {
                // mBrainRecord.stopRecord();
                // }
                //   break;
                case BRAINACTIVITY_HANDLER_MESSAGE_STOP_ANI_AND_FUNC:
                    stopAnimaAndFunc(false);
                    break;
                case BRAINACTIVITY_HANDLER_MESSAGE_STOP_ANI:
                    stopAnima();
                    break;

                case BRAINACTIVITY_HANDLER_MESSAGE_SCREEN_TEST:
                    LogMgr.e("SCREEN_TEST:" + msg.arg1);
                    if (msg.arg1 == 0) {
                        setStopImageView();
                    } else if (msg.arg1 == 1) {
                        setImageViewColor(msg.arg2);
                    }

                    break;
                case BRAINACTIVITY_HANDLER_MESSAGE_START_PROGRAM_ANI_AND_FUNC:
                    startProgramAnimaAndFunc();
                    break;
                case BRAINACTIVITY_HANDLER_MESSAGE_UPDATE_THE_QRCODE:
                    updateQRcodeView();
                    break;
                case 121:
                    // try {
                    // int id = SharedPreferenceTools.getInt(mBrainActivity,
                    // GlobalConfig.SHARED_PREFERENCE_IS_TCP_CONNECTING,
                    // GlobalConfig.SHARED_PREFERENCE_CONNECTING_STATE_UNKNOWN);
                    // if (id ==
                    // GlobalConfig.SHARED_PREFERENCE_CONNECTING_STATE_NOT_CONNECTING)
                    // {
                    // isProgrammingUI = false;
                    // delPadAppConnectView(0);
                    // } else if (id ==
                    // GlobalConfig.SHARED_PREFERENCE_CONNECTING_STATE_IS_CONNECTING)
                    // {
                    // isProgrammingUI = true;
                    // addPadAppConnectView(0, 0);
                    // }
                    // } catch (Exception e) {
                    // e.printStackTrace();
                    // }
                    // SharedPreferenceTools.saveBoolean(BrainActivity.this,
                    // GlobalConfig.SHARED_PREFERENCE_IS_LOCALE_CHANGE, false);
                    break;
                case 40001:
                    // mUsbVideo.destoryUSBVideo();
                    VedioTransMgr.stopVedioTrans();
                    break;
                case 40002:
                    try {
                        // mUsbVideo.startUSBVideo();
                        VedioTransMgr.startUsbVedioTrans();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 374://群控对应的关LED灯
                    LogMgr.e("374");
                    for (int i = 0; i < 3; i++) {
                        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H) {
                            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                    ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0x6A, new byte[]{0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}), null, 0, 0);
                        } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                    ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0x74, new byte[]{0, 0, 0}), null, 0, 0);
                        }
//                        else if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M){
//                            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
//                                    ProtocolUtil.buildProtocol((byte)0x02,(byte) 0xA3, (byte) 0x32,new byte[]{0,0,0}), null, 0, 0);
//                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private BrainActivityBroadcastReceiver mBrainActivityBroadcastReceiver;
    //private ConnectBroadcastReceiver mConnectBroadcastReceiver = null;   //wangdongdong cancel
    private InstallationBroadcastReceiver installationBroadcastReceiver;

    private AIDLDeleteService mDeleteService;
    private ServiceConnection mDeleteServiceCon;
    private AIDLInstallService mInstallService;
    private ServiceConnection mInstallServiceCon;

    public AIDLInstallService getmInstallService() {
        return mInstallService;
    }

    /**
     * 上次点击页面的时间
     */
    private long mLastClickTime = 0L;

    /**
     * ui
     */
    private BrainViewPagerForBrainActivity mViewPager;
    /**
     * 当前所在的页面序号
     */
    private int mCurrentPageNum = 0;
    /**
     * 显示VJC String数据的TextView
     */
    private RelativeLayout mVjcRelativeLayout;
    private TextView mTextViewForVjc;
//	private TextView mTextForVjc;
    /**
     * 显示照片的ImageView
     */
    private ImageView mImageView;
    private ImageView mWelcomeImageView;
    //    private List<ViewHoder> mViewHoders = new LinkedList<ViewHoder>();
    private List<AppInfo> mAppInfos = new LinkedList<AppInfo>();
    private PackageManager mPackageManager;
    // private AddThread mAddThread;
    private LayoutInflater mLayoutInflater;
    private AnimatorSet mAnimatorSet;
    private AnimatorSet mAnimatorSetForCUProgram;
    private BasePagerAdapter basePagerAdapter;
    /**
     * 当前二维码的内容
     */
    private String mCurrentQrContent;
    // private int mCurrentRunIdx = 1; // 当前正在运行的页面索引
    /**
     * 当前点击选中的列表项
     */
    private AppInfo mCurrentAppInfo = null;
    /**
     * 进入解析执行程序前最后一个选中的列表项
     */
    private AppInfo mLastAppInfoBeforeEnterExplainer = null;
    /**
     * 跳至编程界面前最后一个选中的列表项
     */
    private AppInfo mLastAppInfoBeforeEnterProgramPage = null;
    /**
     * 当前页面的列表项
     */
    private AppInfo mCurrentPageAppInfo = null;

    /**
     * 录音
     */
    private RelativeLayout mRecordRelativeLayout;
    private ImageView mRecordImageViewOutside;
    private ImageView mRecordImageViewInside;
    private BrainRecord mBrainRecord;
    private MyRecord mYRecord;
    private ObjectAnimator mRotateAnimator;

    /**
     * 视频
     */
    private SurfaceView mSurfaceView;
    // private InetAddress mInetAddress;
    private PathView mPathView;
    private SendPathView mSendPathView;
    private SendVideo mSendVideo = null;
    // private USBVideo mUsbVideo;
    private WifiManager wm;
    private WifiApAdmin wifiAp;

    // 音频播放
    private MusicPlayer mMusicPlayer;

    // private int mQRPageClickNum = 0;// 二维码界面 连续点击10次后 进入自动测试程序

    private Intent mMainIntent = null;

    private File file_parent = null;
    private File file_autorest = null;
    // 是否返回给 ipad 录音
    private boolean isRecord;
    // 是否是Scratch录音
    private boolean isScratchRecord;
    // /** 当前是否有照片页面 */
    // private boolean isAddImage = false;
    // /** 当前是否有录音页面 */
    // private boolean isAddRecord = false;
    // /** 当前是否有skillpaly页面 */
    // private boolean isAddSkillPlay = false;
    /**
     * 用于在mImageView中显示
     */
    private Bitmap mBitmap = null;
    /**
     * 是否想要退出mTextForVjc
     */
    private boolean mIsWantToCloseTextViewForVjc;

    // private boolean isProgrammingUI;
    /**
     *
     */
    // private boolean isRecord_No;
    private boolean isVjc_MRecord;

    // private boolean stop_paizhao;
    private boolean isInWelcome = false;
    private int welcomeStep = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.getInstance().setOrientation(this);
        LogMgr.i("BrainActivity onCreate");
        setContentView(R.layout.activity_main);
        // 后续将从service统一获取ip
        // if (savedInstanceState != null) {
        // mInetAddress = (InetAddress)
        // savedInstanceState.getSerializable("ip");
        // Log.e("test", "onCreate:" + mInetAddress);
        // }
        mBrainActivity = BrainActivity.this;

        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C && GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig
                .ROBOT_TYPE_CU && Application.getInstance().isFirstStartApplication()) {
            LogMgr.w("C201项目，先进入欢迎界面，再进入设置wifi界面，最后显示二维码界面");
            isInWelcome = true;
            mEnablePageSlidePointsBar = true;
        } else {
            LogMgr.w("不是C201项目");
        }
        mPackageManager = getPackageManager();
        mLayoutInflater = LayoutInflater.from(this);
        wm = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiAp = WifiApAdmin.getInstance(wm);
        //排序相关
        mBrainDatabaseHelper = new BrainDatabaseHelper(mBrainActivity);
        mSqLiteDatabase = mBrainDatabaseHelper.getWritableDatabase();
        file_parent = new File(BrainUtils.ROBOTINFO);
        file_autorest = new File(BrainUtils.AUTORESTINFO);
        //initHandler(); //wangdongdong ,取消该函数，而是直接在全局变量中定义
        initViewM();
        mViewPager.setCurrentItem(getMiddlePlaceForFirstItem());

        if (Application.getInstance().isFirstStartApplication()) {

            /** 保存 STM_VERSION */
            FileUtils.saveSTMtm32VersionFile(FileUtils.STM_VERSION_BRAIN);
            // AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            // am.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);

            /** 初始化声音 */
            boolean isFirst = SharedPreferenceTools.getBoolean(BrainActivity.this, IS_FIRST_START_BRAIN_AFTER_ROOT,
                    true);
            if (isFirst) {
                isFirst = false;
                SharedPreferenceTools.saveBoolean(BrainActivity.this, IS_FIRST_START_BRAIN_AFTER_ROOT, isFirst);
                AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);
            }
//            if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_AF) {
//                LogMgr.e("1");
//                startAnimaAndFunc(mAppInfos.get(0).getViewHoder().imageView_qr, mAppInfos.get(0));
//            } else if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
//                LogMgr.i("KU201不播放欢迎语音！");
//            } else {
//                playWelcomeVoice();
//            }
            playWelcomeVoice();
            if (GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_M) {
                startServer();
            }
        } else {
            if (Application.getInstance().isTcpConnecting()) {
                addPadAppConnectView(0, 0);
            } else {
                delPadAppConnectView(0, true);
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                createWifiHot();
            }
        }).start();
//        mBrainActivityHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                initDeleteAndInstallService();
//            }
//        },120);

        // bindService();
        registerBroadcastReceiver();
        if (isInWelcome) {
            Locale locale = Application.instance.getResources().getConfiguration().locale;
            int imgId = R.drawable.welcome0_201_cn;
            if (locale != null) {
                switch (locale.getLanguage()) {
                    case "en":
                        imgId = R.drawable.welcome0_201;
                        break;
                    case "zh":
                        switch (locale.getCountry()) {
                            case "CN":
                                imgId = R.drawable.welcome0_201_cn;
                                break;
                            case "TW":
                                imgId = R.drawable.welcome0_201_tw;
                                break;
                        }
                        break;
                }
            }
            mWelcomeImageView.setImageResource(imgId);
            mWelcomeImageView.setVisibility(View.VISIBLE);
            //mBrainActivityHandler.obtainMessage(BRAINACTIVITY_HANDLER_MESSAGE_JUMP_TO_PAGE, 1, 0).sendToTarget();
        }
        Application.getInstance().setFirstStartApplication(false);
        /** 打开监听usb摄像头 */
        //  mUsbVideo = USBVideo.GetManger(BrainActivity.this, null, mHandler);
        //  mUsbVideo.initUSB();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.d("wddadd","-----registerReceiver mConnectBroadcastReceiver----BrainActivity onStart()");
//        if (mConnectBroadcastReceiver == null) {
//            mConnectBroadcastReceiver = new ConnectBroadcastReceiver();
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//            filter.addAction(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE);
//            registerReceiver(mConnectBroadcastReceiver, filter);
//        }

//        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
//
//            LogMgr.d("FZXX", "== onStart ==");
//            if (MUtils.sIsYuyinControl) {
//                MUtils.sIsYuyinControl = false;
//            } else {
//                startAD();
//            }
//
//            if (BrainService.getmBrainService() != null
//                    && BrainService.getmBrainService().getMService() != null
//                    && !Application.getInstance().isFirstStartApplication()
//                    && MUtils.sIsAppBack
//                    ) {
//
//                if (MUtils.sIsVoiceAppBack) {
//                    try {
//                        BrainService.getmBrainService().getMService().handAction(MUtils.B_BACK_TO_BRAIN);
//                        LogMgr.d("FZXX", "handAction(MUtils.B_BACK_TO_BRAIN)");
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        BrainService.getmBrainService().getMService().handAction(MUtils.b_CLICK_BACK_TO_BRAIN);
//                        LogMgr.d("FZXX", "handAction(MUtils.b_CLICK_BACK_TO_BRAIN)");
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//
//            }
//
//            MUtils.sIsVoiceAppBack = false;
//            MUtils.sIsAppBack = false;
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogMgr.i("BrainActivity onResume()");
        LogMgr.i("BrainActivity onResume() isInWelcome = " + isInWelcome + " welcomeStep = " + welcomeStep);
        if ((isInWelcome && welcomeStep == 2) || (!isInWelcome)) {
            mWelcomeImageView.setVisibility(View.GONE);
            isInWelcome = false;
        }
        MobclickAgent.onResume(this);
        isForegroundFirst = true;
        mViewPager.setScrollble(true);

        if (Application.getInstance().isTcpConnecting()) {
            addPadAppConnectView(0, 0);
        } else {
            delPadAppConnectView(0, false);
        }
        mBrainService = BrainService.getmBrainService();
        if(!file_autorest.exists() || FileUtils.readFile(file_autorest) == "" || FileUtils.readFile(file_autorest).contains("true")){
            isAutoRest = true;
        }else{
            isAutoRest = false;
        }
        LogMgr.e("isPauseToResume = " + isPauseToResume + ";;isAutoRest = " + isAutoRest);
        if(isPauseToResume){
            AutoToRestWithOutOptionIn3Min();//开始⏲ 3 分钟，无操作，停止运行，断开编程
        }else {
            stopAutoToRestWithOutOptionIn3Min();//停止⏲：开始操作，运行，开始编程，
        }
    }
    private Timer OptionIn3MinTimer;
    private TimerTask OptionIn3MinTimerTask;
    private int mCurrentTimes = 0;
    private void AutoToRestWithOutOptionIn3Min() {
        if(!isAutoRest){
            LogMgr.e("自动待机已关闭");
            return;
        }
        LogMgr.e("AutoToRestWithOutOptionIn3Min");
        stopAutoToRestWithOutOptionIn3Min();
        OptionIn3MinTimer = new Timer();
        OptionIn3MinTimerTask = new TimerTask() {
            @Override
            public void run() {
                //每10S走一次，到3分钟后停止；3*60*1000，需要走18次
                if(mCurrentTimes > 18){
                    stopAutoToRestWithOutOptionIn3Min();
                    mBrainActivityHandler.sendEmptyMessage(1);
//                    int item = mAppInfos.get(0).getPageType() == AppInfo.PAGE_TYPE_PROGRAM ? 1 : 0;
//                    //转到休息界面的操作
//                    mViewPager.setCurrentItem(getMiddlePlaceForFirstItem() + item, false);
//                    //进入休息界面
//                    startAnimaAndFunc(mAppInfos.get(item).getViewHoder().imageView_qr, mAppInfos.get(item));
                }
                mCurrentTimes++;
                LogMgr.e("mCurrentTimes = " + mCurrentTimes);
            }
        };
        OptionIn3MinTimer.schedule(OptionIn3MinTimerTask,100,10*1000);
    }

    private void stopAutoToRestWithOutOptionIn3Min() {
        LogMgr.e("stopAutoToRestWithOutOptionIn3Min");
        mCurrentTimes = 0;
        if (OptionIn3MinTimer != null) {
            OptionIn3MinTimer.cancel();
            OptionIn3MinTimer = null;
        }
        if (OptionIn3MinTimerTask != null) {
            OptionIn3MinTimerTask.cancel();
            OptionIn3MinTimerTask = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 解析Scheme
        analysisScheme(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogMgr.i("BrainActivity onPause()");
        MobclickAgent.onPause(this);
        isForegroundFirst = false;
        isPauseToResume = true;
        // SharedPreferenceTools.saveString(mBrainActivity, "file_success", "");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogMgr.i("BrainActivity onStop()");
        stopShutdownTiemr();
        isPauseToResume = true;
        // if (mBrainActivityBroadcastReceiver != null && installationBroadcastReceiver !=
        // null) {
        // unregisterReceiver(mBrainActivityBroadcastReceiver);
        // unregisterReceiver(installationBroadcastReceiver);
        // }
        // mBrainActivityBroadcastReceiver = null;
        // installationBroadcastReceiver = null;

//        if (mConnectBroadcastReceiver != null) {
//            unregisterReceiver(mConnectBroadcastReceiver);
//        }
//        mConnectBroadcastReceiver = null;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogMgr.i("BrainActivity onRestart()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogMgr.i("BrainActivity onDestroy()");
        if (mBrainActivityBroadcastReceiver != null) {
            unregisterReceiver(mBrainActivityBroadcastReceiver);

        }
        if (installationBroadcastReceiver != null) {
            unregisterReceiver(installationBroadcastReceiver);
        }
        mBrainActivityBroadcastReceiver = null;
        installationBroadcastReceiver = null;
        unBindDeleteService();
        unBindInstallService();
        mSqLiteDatabase.close();
        mBrainDatabaseHelper.close();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        LogMgr.i("BrainActivity onRestoreInstanceState()");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogMgr.i("BrainActivity onSaveInstanceState()");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_M) {
            return super.dispatchTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mBrainActivityHandler.removeCallbacks(longTimeRunnable);
                break;

            case MotionEvent.ACTION_UP:
                startAD();
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * M开始倒计时进入自主情绪
     */
    private void startAD() {
        mBrainActivityHandler.removeCallbacks(longTimeRunnable);
        mBrainActivityHandler.postDelayed(longTimeRunnable, M_LONG_TIME_NO_OPERATION);
    }

    /**
     * M开始倒计时进入自主情绪
     */
    private void stopAD() {
        mBrainActivityHandler.removeCallbacks(longTimeRunnable);
        MUtils.changeMState(mMyGiftView, MUtils.MState.M_STATE_GONE);
    }

    /**
     *
     */
    public void stopEmotion() {
        if (mBrainActivityHandler != null) {
            mBrainActivityHandler.removeCallbacks(longTimeRunnable);
            LogMgr.d("FZXX", "清空情绪计时器");
        }
    }

    /**
     * 初始化页面
     */
    private void initViewM() {
        mImageView = (ImageView) findViewById(R.id.activity_iamge);
        mWelcomeImageView = (ImageView) findViewById(R.id.welcome_iamge);
        mViewPager = (BrainViewPagerForBrainActivity) findViewById(R.id.activity_viewpager);
        mViewPager.addOnPageChangeListener(new MyViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                mCurrentPageNum = arg0;
                mCurrentPageAppInfo = mAppInfos.get(mCurrentPageNum % mAppInfos.size());
                LogMgr.i("BasePagerAdapter", "当前选定页面序号 = " + arg0 + " 页面名 = " + mCurrentPageAppInfo.getName()
                        + " mAppInfos.size() = " + mAppInfos.size() + " mCurrentPageNum%mAppInfos.size() = "
                        + mCurrentPageNum % mAppInfos.size());
                if (mCurrentPageNum % mAppInfos.size() == 0
                        && mCurrentPageAppInfo.getPageType() == AppInfo.PAGE_TYPE_PROGRAM) {
                    LogMgr.i("BasePagerAdapter", "当前选中编程界面");
                } else {
                    mLastAppInfoBeforeEnterProgramPage = mAppInfos.get(mCurrentPageNum % mAppInfos.size());
                }
                if (mEnablePageSlidePointsBar) {
                    updatePageSlidePointsBar();
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // LogMgr.d("BasePagerAdapter",
                // "当前页面 = "+arg0+" 当前页面偏移百分比 = "+arg1+" 当前页面偏移像素位置 = "+arg2);
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // if(arg0 == 1){
                // LogMgr.d("BasePagerAdapter", "滑动中");
                // }else if(arg0 == 2){
                // LogMgr.d("BasePagerAdapter", "滑动结束");
                // }else if(arg0 == 0){
                // LogMgr.d("BasePagerAdapter", "滑动静止");
                // }
            }
        });
        llPointsGroup = (LinearLayout) findViewById(R.id.points);
        mSurfaceView = (SurfaceView) findViewById(R.id.activity_sufaceview);
        mPathView = (PathView) findViewById(R.id.activity_pathview);
        mRecordRelativeLayout = (RelativeLayout) findViewById(R.id.activity_record_relativelayout);
        mRecordImageViewOutside = (ImageView) findViewById(R.id.record_imageview_outside);
        mRecordImageViewInside = (ImageView) findViewById(R.id.record_imageview_inside);

        mVjcRelativeLayout = (RelativeLayout) findViewById(R.id.activity_vjc_relativelayout);
        mTextViewForVjc = (TextView) findViewById(R.id.activity_vjc_textview);
        mTextViewForVjc.setBackgroundResource(BrainUtils.getVJCTextViewBackgroundDrawableResource());
        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
            mTextViewForVjc.setTextColor(Color.BLACK);
        }
        //   mTextViewForVjc.setMovementMethod(ScrollingMovementMethod.getInstance());
//		mTextForVjc = (TextView) findViewById(R.id.activity_vjc);
        mVjcRelativeLayout.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                try {
                    mIsWantToCloseTextViewForVjc = true;
                    // if (mAppInfos.size() > mCurrentRunIdx && mCurrentRunIdx
                    // >= 0) {
                    if (null != mCurrentAppInfo) {
                        // AppInfo aif = mAppInfos.get(mCurrentRunIdx);
                        if (mCurrentAppInfo.getPageType() == AppInfo.PAGE_TYPE_FILE) {
                            int type = mCurrentAppInfo.getFileType();
                            exeFileFunc(type, null, 0, null);
                        }
                    }
                    mTextViewForVjc.setText("");
                    //jinghao add
                    mTextViewForVjc.setVisibility(View.GONE);
                    mVjcRelativeLayout.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        mRecordRelativeLayout.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // isRecord_No = true;
                LogMgr.d("onLongClick() isVjc_MRecord = " + isVjc_MRecord);
                if (isVjc_MRecord) {
                    // if (mAppInfos.size() > mCurrentRunIdx && mCurrentRunIdx
                    // >= 0) {
                    if (null != mCurrentAppInfo) {
                        // AppInfo aif = mAppInfos.get(mCurrentRunIdx);
                        if (mCurrentAppInfo.getPageType() == AppInfo.PAGE_TYPE_FILE) {
                            int type = mCurrentAppInfo.getFileType();
                            exeFileFunc(type, null, 0, null);
                        }/** Scratch发送命令录音。中途停止 */
                        else if (mCurrentAppInfo.getPageType() == AppInfo.PAGE_TYPE_PROGRAM) {
                            BrainService.getmBrainService().exeFilePageFunc(null,
                                    GlobalConfig.CONTROL_CALLBACKMODE_SCRATCH_CMD, 0);
                        }
                    }
                    if (mBrainRecord != null) {
                        mBrainRecord.destory();
                        mBrainRecord = null;
                    }
                    stopTimer();
//					mRecordRelativeLayout.setText("");
                    mRecordRelativeLayout.setVisibility(View.GONE);
                }
                isVjc_MRecord = false;
                return true;
            }
        });

        mWelcomeImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LogMgr.i("mWelcomeImageView.setOnClickListener isInWelcome = " + isInWelcome + " welcomeStep = " + welcomeStep);
                if (isInWelcome) {
                    mWelcomeImageView.setVisibility(View.GONE);
                    isInWelcome = false;
                    /*if (welcomeStep == 0) {
                        mWelcomeImageView.setImageResource(R.drawable.welcome1_201);
                        welcomeStep++;
                    } else if (welcomeStep == 1) {
                        Utils.openApp(mBrainActivity, "com.abilix.brainset", true);
                        welcomeStep++;
//                        mImageView.setVisibility(View.GONE);
                        isInWelcome = false;
                    }*/
                } else {
                    //do nothing
                }
            }
        });
        mImageView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (null != mCurrentAppInfo) {
                    // 在执行文件时打开的mImageView的情况下，长按将停止当前文件的执行
                    if (mCurrentAppInfo.getPageType() == AppInfo.PAGE_TYPE_FILE) {
                        int type = mCurrentAppInfo.getFileType();
                        exeFileFunc(type, null, 0, null);
                    } else {
                        // do nothing
                    }
                }
                setStopImageView();
                return true;
            }
        });


        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
            //加载M系列字体
            mFromAsset = Typeface.createFromAsset(getAssets(), "fonts/m_fzxx.ttf");
            initDataForM();
            LogMgr.d("== initDataForM ==");
            mMyGiftView = (MyGiftView) findViewById(R.id.mgv);
            MUtils.changeMState(mMyGiftView, MUtils.MState.M_STATE_HEART_JUMP);
            mMyGiftView.setEnabled(false);
            mMyGiftView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setVisibility(View.GONE);
                    MUtils.stopEmotion();
                    startAD();
                }
            });
        } else {
            initData();
            LogMgr.d("== initData ==");
        }
        initUI();

    }


    /**
     * 辅助学习奥科流思2.0
     * yhd
     * PAGE_TYPE_M_FUTURE_SCIENCE          PAGE_ORDER_M_FUTURE_SCIENCE
     * PAGE_TYPE_M_FUTURE_COOL             PAGE_ORDER_M_FUTURE_COOL
     * PAGE_TYPE_M_FUTURE_COMMUNICATION    PAGE_ORDER_M_FUTURE_COMMUNICATION
     * PAGE_TYPE_M_ABILITY_TRAINING        PAGE_ORDER_M_ABILITY_TRAINING
     * PAGE_TYPE_FILE                      PAGE_ORDER_M_SKILL
     * PAGE_TYPE_APK                       PAGE_ORDER_M_SET
     * PAGE_TYPE_M_AKLS                    PAGE_ORDER_M_AKLS
     * PAGE_TYPE_QR_CODE                   PAGE_ORDER_M_QR_CODE
     */
    private void initDataForM() {

//        AppInfo.PAGE_ORDER_SET = AppInfo.PAGE_ORDER_M_SET;
        mAppInfos.clear();
//        mViewHoders.clear();
        for (int i = 0; i < 7; i++) {
            AppInfo appInfo = new AppInfo();
            switch (i) {
                case 0:
                    appInfo.setPageType(AppInfo.PAGE_TYPE_M_FUTURE_SCIENCE);
                    appInfo.setName(getString(R.string.future_science));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_M_FUTURE_SCIENCE);
                    break;
                case 1:
                    appInfo.setPageType(AppInfo.PAGE_TYPE_M_FUTURE_COOL);
                    appInfo.setName(getString(R.string.future_cool));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_M_FUTURE_COOL);
                    break;
                case 2:
                    appInfo.setPageType(AppInfo.PAGE_TYPE_M_FUTURE_COMMUNICATION);
                    appInfo.setName(getString(R.string.future_communication));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_M_FUTURE_COMMUNICATION);
                    break;
                case 3:
                    appInfo.setPageType(AppInfo.PAGE_TYPE_M_ABILITY_TRAINING);
                    appInfo.setName(getString(R.string.ability_training));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_M_ABILITY_TRAINING);
                    break;
                case 4:
                    appInfo.setPageType(AppInfo.PAGE_TYPE_FILE);
                    appInfo.setFileType(AppInfo.FILE_TYPE_SKILLPLAYER);
                    appInfo.setName(getString(R.string.skill_play));
                    appInfo.setPathName(BrainUtils.ABILIX + BrainUtils.ABILIX_SKILLPLAYER);
                    appInfo.setOrder(AppInfo.PAGE_ORDER_M_SKILL);
                    break;
                case 5:
                    appInfo.setPageType(AppInfo.PAGE_TYPE_M_AKLS);
                    appInfo.setName(getString(R.string.akls));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_M_AKLS);
                    break;
                case 6:
                    appInfo.setPageType(AppInfo.PAGE_TYPE_QR_CODE);
                    appInfo.setName("二维码");
                    appInfo.setOrder(AppInfo.PAGE_ORDER_M_QR_CODE);
                    break;
                default:
                    break;
            }

            mAppInfos.add(appInfo);
        }

        //M系列不需要再查询App和File,只需要将设置显示即可
//        queryAppInfo();
        queryFile();

        displaySet();
        sort();
        initView();
    }

    private void displaySet() {
        mMainIntent = new Intent(Intent.ACTION_MAIN, null);
        mMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mMainIntent.addCategory(GlobalConfig.APP_ABILIX_LAUNCHER);

        List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(mMainIntent, 0);

        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(mPackageManager));
        if (mAppInfos != null) {
            for (ResolveInfo reInfo : resolveInfos) {
                String activityName = reInfo.activityInfo.name;
                String pkgName = reInfo.activityInfo.packageName;
                String appLabel = (String) reInfo.loadLabel(mPackageManager).toString();
                Drawable icon = reInfo.loadIcon(mPackageManager);
                ApplicationInfo appInfo = null;
                try {
                    appInfo = mPackageManager.getApplicationInfo(pkgName, 0);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                String appFile = appInfo.sourceDir;
                long installed = new File(appFile).lastModified();
                Intent launchIntent = mPackageManager.getLaunchIntentForPackage(pkgName);
                AppInfo appInfos = new AppInfo();
                appInfos.setAppName(activityName);
                appInfos.setAppLabel(appLabel);
                appInfos.setPkgName(pkgName);
                appInfos.setAppIcon(icon);
                appInfos.setIntent(launchIntent);
                appInfos.setInstallTime(installed);
                if (TextUtils.equals(GlobalConfig.APP_PKGNAME_VOLUMECONTROL, pkgName)) {
                    continue;
                }

                appInfos.setPageType(AppInfo.PAGE_TYPE_APK);
                if (TextUtils.equals(GlobalConfig.APP_PKGNAME_BRAINSET, pkgName)) {
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
                        appInfos.setOrder(AppInfo.PAGE_ORDER_M_SET);
                    } else {
                        appInfos.setOrder(AppInfo.PAGE_ORDER_SET);
                    }
                    if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                        mAppInfos.add(1, appInfos);
                    } else {
                        mAppInfos.add(3, appInfos);
                    }
                }
            }
        }

    }

    /**
     * 初始化数据mAppInfos，mViewHoders
     */
    private void initData() {
        mAppInfos.clear();
//        mViewHoders.clear();
        for (int i = 0; i < 7; i++) {
            AppInfo appInfo = new AppInfo();
            switch (i) {
                case 0:
                    if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                        continue;
                    }
                    appInfo.setPageType(AppInfo.PAGE_TYPE_REST);
                    appInfo.setName(getString(R.string.rest));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_REST);
                    break;
                case 1:
                    if (GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_H && GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_H3) {
                        continue;
                    }
                    appInfo.setPageType(AppInfo.PAGE_TYPE_RESET);
                    appInfo.setName(getString(R.string.reset));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_RESET);
                    break;
                case 2:
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_AF || GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU
                            || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                        continue;
                    }
                    appInfo.setPageType(AppInfo.PAGE_TYPE_SOUL);
                    appInfo.setName(getString(R.string.soul));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_SOUL);
                    break;
                case 3:
                    appInfo.setPageType(AppInfo.PAGE_TYPE_QR_CODE);
                    appInfo.setName("二维码");
                    appInfo.setOrder(AppInfo.PAGE_ORDER_QR_CODE);
                    break;
                case 4:
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_AF || GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                        continue;
                    }
                    appInfo.setPageType(AppInfo.PAGE_TYPE_MULTIMEDIA);
                    appInfo.setName(getString(R.string.duomeiti));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_MULTIMEDIA);
                    break;
                case 5:
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C || GlobalConfig.BRAIN_TYPE ==
                            GlobalConfig.ROBOT_TYPE_C9 || GlobalConfig.BRAIN_TYPE ==
                            GlobalConfig.ROBOT_TYPE_CU) {
                        continue;
                    }
                    appInfo.setPageType(AppInfo.PAGE_TYPE_FILE);
                    appInfo.setFileType(AppInfo.FILE_TYPE_SKILLPLAYER);
                    appInfo.setName(getString(R.string.skill_play));
                    appInfo.setPathName(BrainUtils.ABILIX + BrainUtils.ABILIX_SKILLPLAYER);
                    appInfo.setOrder(AppInfo.PAGE_ORDER_SKILLPLAYER);
                    break;
                case 6:
                    if (GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_H && GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_H3 &&
                            GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_M) {
                        continue;
                    }
                    appInfo.setPageType(AppInfo.PAGE_TYPE_GROUP);
                    appInfo.setName(getString(R.string.groupcontrol));
                    appInfo.setOrder(AppInfo.PAGE_ORDER_GROUP);
                    break;
            }

            mAppInfos.add(appInfo);
        }
        // H系列增加激活界面，保护界面
//        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H || GlobalConfig.BRAIN_TYPE == GlobalConfig
// .ROBOT_TYPE_H3) {
//            AppInfo appInfoActive = new AppInfo();
//            appInfoActive.setPageType(AppInfo.PAGE_TYPE_ACTIVATE_H);
//            appInfoActive.setName(getString(R.string.activate));
//			appInfoActive.setOrder(AppInfo.PAGE_ORDER_ACTIVE);
//            mAppInfos.add(appInfoActive);
//
//            AppInfo appInfoProtect = new AppInfo();
//            appInfoProtect.setPageType(AppInfo.PAGE_TYPE_PROTECT_H);
//            appInfoProtect.setName(getString(R.string.protect));
//			appInfoProtect.setOrder(AppInfo.PAGE_ORDER_PROTECT);
//            mAppInfos.add(appInfoProtect);
//        }
        queryAppInfo();
        queryFile();
        sort();
        initView();
    }

    private void sort() {
        Collections.sort(mAppInfos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                if (lhs.getOrder() < rhs.getOrder()) {
                    return -1;
                } else if (lhs.getOrder() > rhs.getOrder()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        LogMgr.i("排序表中的行数 = " + mBrainDatabaseHelper.getRowCountInTablePageSortInfo(mSqLiteDatabase) + " 页面总数 = " +
                mAppInfos.size());
        if (mBrainDatabaseHelper.getRowCountInTablePageSortInfo(mSqLiteDatabase) - mAppInfos.size() >
                BrainDatabaseHelper.MOST_INVALID_ROW_NUMBER) {
            List<String> pageNames = new ArrayList<String>();
            for (int i = 0; i < mAppInfos.size(); i++) {
                if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_APK) {
                    pageNames.add(mAppInfos.get(i).getPkgName());
                } else if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_FILE && mAppInfos.get(i).getFileType()
                        != AppInfo.FILE_TYPE_SKILLPLAYER) {
                    pageNames.add(mAppInfos.get(i).getPathName());
                }
            }
            int deleteRows = mBrainDatabaseHelper.deleteAllInvalidPageSortInfo(mSqLiteDatabase, pageNames);
            LogMgr.i("删除排序表中无用行数 deleteRows = " + deleteRows);

        }
    }

    /**
     * 刷新显示字，添加点击回调
     */
    public void initUI() {
        for (int i = 0; i < mAppInfos.size(); i++) {
            // 设置显示文字
            if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_FILE) {
                mAppInfos.get(i).getViewHoder().textview.setText(mAppInfos.get(i).getName());
            } else if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_APK) {
                mAppInfos.get(i).getViewHoder().textview.setText(mAppInfos.get(i).getAppLabel());
                // } else if (mAppInfos.get(i).getName() != null &&
                // !"".equals(mAppInfos.get(i).getName())) {
            } else if (!TextUtils.isEmpty(mAppInfos.get(i).getName())) {
                mAppInfos.get(i).getViewHoder().textview.setText(mAppInfos.get(i).getName());
            }
            // if (mAppInfos.get(i).getName() != null &&
            // TextUtils.equals(mAppInfos.get(i).getName(), "二维码")) {
            // 设置点击回调
            if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_QR_CODE
                    || mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_MOTOR) {
                continue;
            } else {
                initViewFunc(i, mAppInfos.get(i).getViewHoder(), mAppInfos.get(i));
            }
        }
        if (mEnablePageSlidePointsBar) {
            updatePageSlidePointsBar();
        }
        basePagerAdapter = new BasePagerAdapter(mAppInfos);
        mViewPager.setAdapter(basePagerAdapter);
        basePagerAdapter.notifyDataSetChanged();
    }

    /**
     * 底部滑动栏
     */
    private static boolean mEnablePageSlidePointsBar = false; //控制开关

    private LinearLayout llPointsGroup;
    private List<ImageView> ivPoints = new ArrayList<>();

    private void addPageSlidePoint() {
        ImageView point = new ImageView(BrainActivity.this);
        point.setPadding(4, 4, 4, 4);
        point.setImageResource(R.drawable.page_unfocused);
        ivPoints.add(point);
        llPointsGroup.addView(point);
    }

    private void removePageSlidePoint() {
        int index = ivPoints.size() - 1;
        if (index < 0) {
            return;
        }
        ivPoints.remove(index);
        llPointsGroup.removeViewAt(index);
    }

    private void setCurrentPageSlidePoint(int pageIndex) {
        for (int i = 0; i < ivPoints.size(); i++) {
            if (i == pageIndex) {
                ivPoints.get(i).setImageResource(R.drawable.page_focused);
            } else {
                ivPoints.get(i).setImageResource(R.drawable.page_unfocused);
            }
        }
    }

    private void updatePageSlidePointsBar() {
        int defaultPageIndexOffset = 0; //默认0:二维码 1：setting
        for (AppInfo appInfo : mAppInfos) {
            switch (appInfo.getPageType()) {
                case AppInfo.PAGE_TYPE_PROGRAM:
                case AppInfo.PAGE_TYPE_QR_CODE:
                    defaultPageIndexOffset++;
                    break;
            }
        }
        int defaultPageIndex = mCurrentPageNum % mAppInfos.size() - defaultPageIndexOffset;
        setCurrentPageSlidePoint(defaultPageIndex);
        if (mCurrentPageAppInfo != null) {
            switch (mCurrentPageAppInfo.getPageType()) {
                case AppInfo.PAGE_TYPE_PROGRAM:
                case AppInfo.PAGE_TYPE_QR_CODE:
                    llPointsGroup.setVisibility(View.GONE);
                    break;
                default:
                    llPointsGroup.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    // /**
    // * 绑定apk安装卸载服务
    // */
    // private void bindService() {
    // bindDeleteService(); // 绑定删除service
    // bindInstallService(); // 绑定安装service
    // }

    // @Override
    // protected void onSaveInstanceState(Bundle outState) {
    // super.onSaveInstanceState(outState);
    // if (mInetAddress != null) {
    // outState.putSerializable("ip", mInetAddress);
    // }
    // }

    /**
     * 启动Brain主服务
     */
    private void startServer() {
        LogMgr.i("BrainActivity startServer() 启动Brain主服务");
        Intent it = new Intent();
        it.setAction("com.abilix.brain.aidl.IBrain");
        it.setPackage(getPackageName());
        startService(it);
    }

    /**
     * 初始化安装卸载app服务
     */
    public void initDeleteAndInstallService() {

        mDeleteServiceCon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mDeleteService = AIDLDeleteService.Stub.asInterface(service);
                if (mDeleteService != null) {
                    LogMgr.i("mDeleteServiceCon onServiceConnected");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mDeleteService = null;
                LogMgr.i("mDeleteServiceCon onServiceDisconnected");
            }
        };

        mInstallServiceCon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mInstallService = AIDLInstallService.Stub.asInterface(service);
                if (mInstallService != null) {
                    LogMgr.i("mInstallServiceCon onServiceConnected");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mInstallService = null;
                LogMgr.i("mInstallServiceCon onServiceDisconnected");
            }
        };

        bindDeleteService(); // 绑定删除service
        bindInstallService(); // 绑定安装service
    }

    /**
     * 删除apk功能使用 AIDLDeleteService
     */
    private void bindDeleteService() {
        startDeleteService();
        Intent intent = new Intent();
        intent.setAction("com.grandar.deleteservice.AIDLDeleteService");
        intent.setPackage("com.grandar.deleteservice");
        bindService(intent, mDeleteServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void unBindDeleteService() {
        if (mDeleteServiceCon != null) {
            unbindService(mDeleteServiceCon);
        }
    }

    private void startDeleteService() {
        Intent intent = new Intent();
        intent.setAction("com.grandar.deleteservice.AIDLDeleteService");
        intent.setPackage("com.grandar.deleteservice");
        this.startService(intent);
    }

    private void stopDeleteService() {
        Intent intent = new Intent();
        intent.setAction("com.grandar.deleteservice.AIDLDeleteService");
        this.stopService(intent);
    }

    /**
     * 安装apk功能使用 AIDLInstallService
     */
    private void bindInstallService() {
        startInstallService();
        Intent intent = new Intent();
        intent.setAction("com.grandar.installservice.AIDLInstallService");
        intent.setPackage("com.grandar.installservice");
        bindService(intent, mInstallServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void unBindInstallService() {
        if (mInstallServiceCon != null) {
            unbindService(mInstallServiceCon);
        }

    }

    private void startInstallService() {
        Intent intent = new Intent();
        intent.setAction("com.grandar.installservice.AIDLInstallService");
        intent.setPackage("com.grandar.installservice");
        this.startService(intent);
    }

    private void stopInstallService() {
        Intent intent = new Intent();
        intent.setAction("com.grandar.installservice.AIDLInstallService");
        this.stopService(intent);
    }

    /**
     * 更新安装状态
     * UPDATE_ONLINE_OPERATION_SUCCESS = 0x01; // 安装成功
     * UPDATE_ONLINE_OPERATION_IGNORED = 0x02; // 忽略更新
     * UPDATE_ONLINE_OPERATION_FAILED = 0x03; // 操作失败
     * UPDATE_ONLINE_OPERATION_NO_UPDATES = 0x04; // 没有可用更新
     */
    private static int updateOperationState = -1;
    private static boolean updateOperationStateEntrySettingsFlag = false;

    public void updateOperationState(int state) {
        updateOperationState = state;
        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
            mBrainActivityHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (AppInfo appInfo : mAppInfos) {
                        if (TextUtils.equals(GlobalConfig.APP_PKGNAME_BRAINSET, appInfo.getPkgName())) {
                            View childview = appInfo.getViewHoder().view;
                            ImageView updateTips = (ImageView) childview.findViewById(R.id.imageview_h3);
                            if (updateOperationState == GlobalConfig.UPDATE_ONLINE_OPERATION_IGNORED) {
                                updateTips.setVisibility(View.VISIBLE);
                                if (updateOperationStateEntrySettingsFlag) {
                                    updateOperationStateEntrySettingsFlag = false;
                                    Utils.openApp(BrainActivity.this, GlobalConfig.APP_PKGNAME_BRAINSET, null, false);
                                }
                            } else {
                                updateTips.setVisibility(View.INVISIBLE);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    /**
     * 初始化数据mViewHoders
     */
    private void initView() {
        for (int i = 0; i < mAppInfos.size(); i++) {
            if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_QR_CODE) { // 生成二维码界面
                ViewHoder vh = new ViewHoder();
                View childview = mLayoutInflater.inflate(R.layout.brain_content_layout, null);
                TextView textview = (TextView) childview.findViewById(R.id.textview);
                textview.setVisibility(View.GONE);
                final ImageView imageView = (ImageView) childview.findViewById(R.id.imageview_h);
                if (GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_M) {
                    imageView.setImageResource(BrainUtils.getAppBg());
                } else {
                    imageView.setImageResource(R.drawable.m_qr_code_bg);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    imageView.setAdjustViewBounds(false);
                }
                ImageView iv_qr = (ImageView) childview.findViewById(R.id.imageview_qr);
                ImageView imageview_h3 = (ImageView) childview.findViewById(R.id.imageview_h3);
                ImageView m_qr_bg = (ImageView) childview.findViewById(R.id.m_qr_bg);
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
                    m_qr_bg.setVisibility(View.VISIBLE);
                }
                iv_qr.setBackgroundColor(getResources().getColor(R.color.WHITE));
                String qr = WifiApAdmin.readSsid() + "," + WifiApAdmin.readPass()
                        + WifiUtils.getWiFiSSID(BrainActivity.this);
                LogMgr.i("initView() 生成二维码 qr content::" + qr);
                Bitmap qrCodeBitmap = null;
                try {
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H | GlobalConfig.BRAIN_TYPE ==
                            GlobalConfig.ROBOT_TYPE_H3) {
                        qrCodeBitmap = EncodingHandler.createQRCode(qr, 225, 1);
                        imageview_h3.setVisibility(View.VISIBLE);
                    } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
                        qrCodeBitmap = EncodingHandler.createQRCode(qr, 260, 0);
                    } else {
                        qrCodeBitmap = EncodingHandler.createQRCode(qr, 320, 3);
                    }
                    iv_qr.setImageBitmap(qrCodeBitmap);
                    iv_qr.setVisibility(View.VISIBLE);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                vh.view = childview;
                vh.textview = textview;
                vh.imageView = imageView;
                vh.imageView_qr = iv_qr;
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
                    vh.m_qr_bg = m_qr_bg;
                }
                mAppInfos.get(i).setViewHoder(vh);
            } else if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU &&
                    mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_APK) {
                if (TextUtils.equals(GlobalConfig.APP_PKGNAME_BRAINSET, mAppInfos.get(i).getPkgName())) { // 设置界面
                    View childview = mLayoutInflater.inflate(R.layout.brain_content_layout, null);
                    childview.setBackgroundResource(R.drawable.main_bg_201);
                    ViewHoder vh = new ViewHoder();
                    TextView textview = (TextView) childview.findViewById(R.id.textview);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textview.getLayoutParams();
                    lp.gravity = Gravity.TOP | Gravity.CENTER;
                    textview.setLayoutParams(lp);
                    textview.setTextSize(36.0f);
                    ImageView imageView = (ImageView) childview.findViewById(R.id.imageview_h);
                    ImageView qr = (ImageView) childview.findViewById(R.id.imageview_qr);
                    ImageView updateTips = (ImageView) childview.findViewById(R.id.imageview_h3);
                    imageView.setImageResource(R.drawable.gearbackground);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    qr.setImageResource(R.drawable.gear);
                    //更新安装状态
                    if (updateOperationState == GlobalConfig.UPDATE_ONLINE_OPERATION_IGNORED) {
                        updateTips.setVisibility(View.VISIBLE);
                    } else {
                        updateTips.setVisibility(View.INVISIBLE);
                    }
                    updateTips.setScaleType(ImageView.ScaleType.CENTER);
                    updateTips.setX(80.0f);
                    updateTips.setY(-80.0f);
                    updateTips.setImageResource(R.drawable.update_tips_count_ku);

                    vh.view = childview;
                    vh.textview = textview;
                    vh.imageView = imageView;
                    vh.imageView_qr = qr;
                    mAppInfos.get(i).setViewHoder(vh);
                } else { //其他APP界面
                    View childview = mLayoutInflater.inflate(R.layout.brain_content_layout, null);
                    childview.setBackgroundResource(R.drawable.main_bg_201);
                    ViewHoder vh = new ViewHoder();
                    TextView textview = (TextView) childview.findViewById(R.id.textview);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textview.getLayoutParams();
                    lp.gravity = Gravity.TOP | Gravity.CENTER;
                    textview.setLayoutParams(lp);
                    textview.setTextSize(36.0f);
                    ImageView imageView = (ImageView) childview.findViewById(R.id.imageview_h);
                    ImageView qr = (ImageView) childview.findViewById(R.id.imageview_qr);
                    imageView.setImageResource(R.drawable.gearbackground);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    String pkgName = mAppInfos.get(i).getPkgName();
                    if (GlobalConfig.APP_PKGNAME_MEMORYGAME.equals(pkgName)) {
                        qr.setImageResource(R.drawable.memory_game_icon);
                    } else if (GlobalConfig.APP_PKGNAME_TENNIS.equals(pkgName)) {
                        qr.setImageResource(R.drawable.tennis_game_icon);
                    } else if (GlobalConfig.APP_PKGNAME_IOS7LEVEL.equals(pkgName)) {
                        qr.setImageResource(R.drawable.gyroscope_icon);
                    } else {
                        qr.setVisibility(View.GONE);
                    }
                    vh.view = childview;
                    vh.textview = textview;
                    vh.imageView = imageView;
                    vh.imageView_qr = qr;
                    mAppInfos.get(i).setViewHoder(vh);
                }
            } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
                View childview = mLayoutInflater.inflate(R.layout.brain_content_layout, null);
                childview.setBackgroundResource(R.drawable.m_bg);
                ViewHoder vh = new ViewHoder();
                TextView textview = (TextView) childview.findViewById(R.id.textview);
                if (mFromAsset != null) {
                    textview.setTypeface(mFromAsset);
                }
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textview.getLayoutParams();
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER;
                lp.bottomMargin = 20;
                textview.setLayoutParams(lp);
                textview.setTextSize(36.0f);

                ImageView imageView = (ImageView) childview.findViewById(R.id.imageview_h);
                FrameLayout.LayoutParams lp2 = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                lp2.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                lp2.topMargin = -50;
                imageView.setLayoutParams(lp2);
                ImageView qr = (ImageView) childview.findViewById(R.id.imageview_qr);
                imageView.setScaleType(ImageView.ScaleType.CENTER);

                vh.view = childview;
                vh.textview = textview;
                vh.imageView = imageView;
                vh.imageView_qr = qr;
                mAppInfos.get(i).setViewHoder(vh);

                if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_APK) {
                    if (TextUtils.equals(GlobalConfig.APP_PKGNAME_BRAINSET, mAppInfos.get(i).getPkgName())) { // 设置界面
                        imageView.setImageResource(R.drawable.m_setting);
                    }
                } else if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_M_FUTURE_SCIENCE) {
                    imageView.setImageResource(R.drawable.m_science);
                } else if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_M_FUTURE_COOL) {
                    imageView.setImageResource(R.drawable.m_cool_play);
                } else if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_M_FUTURE_COMMUNICATION) {
                    imageView.setImageResource(R.drawable.m_communication);
                } else if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_M_ABILITY_TRAINING) {
                    imageView.setImageResource(R.drawable.m_training);
                } else if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_M_AKLS) {
                    imageView.setImageResource(R.drawable.m_akls);
                } else if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_FILE) {
                    imageView.setImageResource(R.drawable.m_skill);
                }
            } else {
                View childview = mLayoutInflater.inflate(R.layout.brain_content_layout, null);
                ViewHoder vh = new ViewHoder();
                TextView textview = (TextView) childview.findViewById(R.id.textview);
                ImageView imageView = (ImageView) childview.findViewById(R.id.imageview_h);
                ImageView qr = (ImageView) childview.findViewById(R.id.imageview_qr);
                qr.setBackgroundResource(BrainUtils.getAppBg());
                vh.view = childview;
                vh.textview = textview;
                vh.imageView = imageView;
                vh.imageView_qr = qr;
                mAppInfos.get(i).setViewHoder(vh);
            }
            if (mEnablePageSlidePointsBar && mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_APK) {
                //添加底部滑动圆点
                addPageSlidePoint();
            }
        }
    }
    private void initViewFunc(final int i, final ViewHoder viewHoder, final AppInfo appInfo) {
        viewHoder.view.findViewById(R.id.clickFeedbackView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // LogMgr.d("onClick() mCurrentRunIdx = "+mCurrentRunIdx+" i = "+i);
                // if (mCurrentRunIdx == i) {
                if (System.currentTimeMillis() - mLastClickTime < 100) {
                    LogMgr.w("点击Brain屏幕过快，此次点击事件不做响应");
                    return;
                }
                mLastClickTime = System.currentTimeMillis();
                mViewPager.setScrollble(false);
                mBrainActivityHandler.sendMessageDelayed(mBrainActivityHandler.obtainMessage
                        (BRAINACTIVITY_HANDLER_MESSAGE_SET_SCROLLBLE_TRUE), 500);
                if (appInfo == mCurrentAppInfo) {
                    stopAnimaAndFunc(false);
                } else {
                    LogMgr.e("2");
                    startAnimaAndFunc(viewHoder.imageView_qr, appInfo);
                }
            }
        });

        //M系列一級頁面取消长按事件
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
            return;
        }


        if ((appInfo.getPageType() == AppInfo.PAGE_TYPE_FILE && appInfo.getFileType() != AppInfo.FILE_TYPE_SKILLPLAYER)
                || appInfo.getPageType() == AppInfo.PAGE_TYPE_IMAGE || appInfo.getPageType() == AppInfo.PAGE_TYPE_RECORD
                || (isDel(appInfo.getPkgName()) && appInfo.getPageType() == AppInfo.PAGE_TYPE_APK)) {
            viewHoder.view.findViewById(R.id.clickFeedbackView).setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (appInfo.getPageType() == AppInfo.PAGE_TYPE_FILE && appInfo.getFileType() == AppInfo
                            .FILE_TYPE_SCRATCH) {
                        LogMgr.d("当前长按Scratch页面");
                        if (appInfo == mCurrentAppInfo) {
                            LogMgr.d("当前长按的Scratch页面，正在运行中");
                            return true;
                        }
                    }
                    mViewPager.setScrollble(false);
                    if (file_parent.exists()) {
                        String is = FileUtils.readFile(file_parent);
                        if (!is.contains("true")) {
                            delViewPager(i, appInfo);
                        } else {
                            // 家长模式下无法删除
                            Utils.showSingleButtonDialog(BrainActivity.this, getString(R.string.tishi), getString(R
                                            .string.shezhibrain),
                                    getString(R.string.queren), false, new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mViewPager.setScrollble(true);
                                        }
                                    });
//							final AlertDialog dialog = new AlertDialog.Builder(BrainActivity.this).create();
//							dialog.setCancelable(false);
//							dialog.setCanceledOnTouchOutside(false);
//							dialog.show();
//							Window window = dialog.getWindow();  
//							window.setContentView(R.layout.single_button_dialog);  
//							
//							RelativeLayout relativeLayout = (RelativeLayout)window.findViewById(R.id
// .rl_single_button_dialog);
//							TextView tv_title = (TextView)window.findViewById(R.id.tv_single_dialog_title);
//							TextView tv_message = (TextView)window.findViewById(R.id.tv_single_dialog_message);
//							Button btn = (Button)window.findViewById(R.id.btn_single_dialog);
//							
//							relativeLayout.setBackgroundResource(BrainUtils.getButtonDialogBackgroundDrawableResource
// ());
//							tv_title.setText(getString(R.string.tishi));
//							tv_message.setText(getString(R.string.shezhibrain));
//							btn.setText(getString(R.string.queren));
//							btn.setBackgroundResource(BrainUtils.getSingleButtonDialogButtonBackgroundDrawableResource
// ());
//							btn.setOnClickListener(new OnClickListener() {
//								@Override
//								public void onClick(View v) {
//									mViewPager.setScrollble(true);
//									dialog.dismiss();
//								}
//							});

//							AlertDialog.Builder dialog = new AlertDialog.Builder(BrainActivity.this);
//							dialog.setTitle(getString(R.string.tishi));
//							dialog.setMessage(getString(R.string.shezhibrain));
//							dialog.setCancelable(false);
//							dialog.setPositiveButton(getString(R.string.queren), new DialogInterface.OnClickListener
// () {
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									mViewPager.setScrollble(true);
//								}
//							});
//							dialog.show();
                        }
                    } else {
                        delViewPager(i, appInfo);
                    }
                    return true;
                }
            });
        }
    }

    /**
     * 应用是否可以删除
     *
     * @param pkgName
     */
    public boolean isDel(String pkgName) {
        if (pkgName != null) {
            switch (pkgName) {
                case GlobalConfig.APP_PKGNAME_MEMORYGAME:
                case GlobalConfig.APP_PKGNAME_UPDATEONLINETEST:
                case GlobalConfig.APP_PKGNAME_BRAINSET:
                case GlobalConfig.APP_PKGNAME_TENNIS:
                case GlobalConfig.APP_PKGNAME_IOS7LEVEL:
                case GlobalConfig.APP_PKGNAME_IM_QOMOLANGMA:
                case GlobalConfig.APP_PKGNAME_QOMOLANGMA_SHOW:
                    return false;
                default:
                    return true;
            }
        }
        return true;
    }

    private static String mCUMyRobotImagePath = "";

    /**
     * 插入页面：将appInfo viewHolder添加到列表中 并刷新页面 跳至该页面
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void insertPage(int i, AppInfo appInfo) {

        //如果是M系列,不用添加页面
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M && appInfo.getPageType() == AppInfo.PAGE_TYPE_APK) {
            return;
        }

        if (appInfo != null && appInfo.getPkgName() != null && appInfo.getPkgName().equals(OCULUSNAME)) {
            LogMgr.d("我叫奥克流思不显示");
            return;
        }
        if (appInfo == null) {
            LogMgr.e("insertPage() appInfo == null");
            return;
        }
        ViewHoder viewHoder = new ViewHoder();
        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU
                && appInfo.getPageType() == AppInfo.PAGE_TYPE_APK) { //其他APP界面
            View childview = mLayoutInflater.inflate(R.layout.brain_content_layout, null);
            TextView textview = (TextView) childview.findViewById(R.id.textview);
            ImageView imageView = (ImageView) childview.findViewById(R.id.imageview_h);
            ImageView qr = (ImageView) childview.findViewById(R.id.imageview_qr);
            textview.setText(appInfo.getPageType() == AppInfo.PAGE_TYPE_APK ? appInfo.getAppLabel() : appInfo.getName());
            imageView.setImageResource(R.drawable.basegame);
            qr.setVisibility(View.GONE);
            viewHoder.view = childview;
            viewHoder.textview = textview;
            viewHoder.imageView = imageView;
            viewHoder.imageView_qr = qr;
            appInfo.setViewHoder(viewHoder);
            mAppInfos.add(i, appInfo);
            LogMgr.i("mCurrentPageNum = " + mCurrentPageNum + " mAppInfos.size() = " + mAppInfos.size());
        }
        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU
                && appInfo.getPageType() == AppInfo.PAGE_TYPE_MOTOR) { //项目编程lesson4、5显示电机界面
            View childview = mLayoutInflater.inflate(R.layout.brain_content_layout, null);
            childview.setBackgroundResource(R.drawable.main_bg_201);
            ViewHoder vh = new ViewHoder();
            TextView textview = (TextView) childview.findViewById(R.id.textview);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textview.getLayoutParams();
            lp.gravity = Gravity.TOP | Gravity.CENTER;
            textview.setLayoutParams(lp);
            textview.setTextSize(36.0f);
            ImageView imageView = (ImageView) childview.findViewById(R.id.imageview_h);
            ImageView qr = (ImageView) childview.findViewById(R.id.imageview_qr);
            imageView.setImageResource(R.drawable.motorbackground);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            qr.setImageResource(R.drawable.motor);

            vh.view = childview;
            vh.textview = textview;
            vh.imageView = imageView;
            vh.imageView_qr = qr;
            appInfo.setViewHoder(vh);
            mAppInfos.add(i, appInfo);
        } else {
            int oldListSize = mAppInfos.size();
            View childview = mLayoutInflater.inflate(R.layout.brain_content_layout, null);
            TextView textview = (TextView) childview.findViewById(R.id.textview);
            final ImageView imageView = (ImageView) childview.findViewById(R.id.imageview_h);
            ImageView qr = (ImageView) childview.findViewById(R.id.imageview_qr);
            qr.setBackgroundResource(BrainUtils.getAppBg());
            textview.setText(appInfo.getPageType() == AppInfo.PAGE_TYPE_APK ? appInfo.getAppLabel() : appInfo.getName());
            viewHoder.imageView = imageView;
            viewHoder.textview = textview;
            viewHoder.imageView_qr = qr;
            viewHoder.view = childview;
            appInfo.setViewHoder(viewHoder);
//        mViewHoders.add(i, viewHoder);
            mAppInfos.add(i, appInfo);
            LogMgr.i("mCurrentPageNum = " + mCurrentPageNum + " oldListSize = " + oldListSize);
        }
        if (i == 0) {
            // 添加的是编程界面
            if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                LogMgr.e("替换编程界面");
                ViewHoder vh = mAppInfos.get(i).getViewHoder();
                ImageView qr_iv = vh.imageView_qr;
                ImageView v = vh.imageView;
                //v.setVisibility(View.GONE);
                TextView textView = vh.textview;
                textView.setVisibility(View.GONE);
                // stopAnimaFromOutSide();
                v.setImageResource(R.drawable.ku_background);
                qr_iv.setBackgroundColor(Color.TRANSPARENT);
                File file = new File(mCUMyRobotImagePath);
                if (!TextUtils.isEmpty(mCUMyRobotImagePath) && file.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(mCUMyRobotImagePath);
                    qr_iv.setImageBitmap(bm);
                } else {
                    qr_iv.setImageResource(R.drawable.ku_smile);
                }
                changeProgramStatus(true);
            } else {
                ViewHoder vh = mAppInfos.get(i).getViewHoder();
                ImageView v = vh.imageView;
                v.setVisibility(View.VISIBLE);
                initViewFunc(i, viewHoder, appInfo);
            }

            if (basePagerAdapter != null) {
                basePagerAdapter.notifyDataSetChanged();
            }
        } else {
            if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU
                    && appInfo.getPageType() == AppInfo.PAGE_TYPE_MOTOR) {
                //项目编程lesson4、5显示电机界面 不需要加入数据库
            } else {
                //用于插入数据库中
                String pageName = appInfo.getPageType() == AppInfo.PAGE_TYPE_APK ? appInfo.getPkgName() : appInfo
                        .getPathName();
                int pageType = appInfo.getPageType() == AppInfo.PAGE_TYPE_APK ? appInfo.getPageType() : appInfo
                        .getFileType();
                long rowID = mBrainDatabaseHelper.insertTablePageSortInfo(mSqLiteDatabase, pageName, pageType);
                if (rowID == -1) {
                    LogMgr.e("插入数据库中pageName = " + pageName + "时，数据库中已存在该数据，插入失败。");
                    if (mBrainDatabaseHelper.deleteTablePageSortInfo(mSqLiteDatabase, pageName)) {
                        LogMgr.i("删除老旧数据成功pageName = " + pageName);
                        rowID = mBrainDatabaseHelper.insertTablePageSortInfo(mSqLiteDatabase, pageName, pageType);
                        if (rowID == -1) {
                            LogMgr.e("删除数据库中pageName = " + pageName + "后，插入数据库仍失败，放弃插入，重启后会丢失页面");
                        } else {
                            LogMgr.i("删除数据库中pageName = " + pageName + "后，插入数据库成功 rowID = " + rowID);
                            appInfo.setOrder(rowID);
                        }
                    } else {
                        LogMgr.e("删除老旧数据失败pageName = " + pageName + "  页面顺序重启后可能不按时间顺序排列");
                    }
                } else {
                    LogMgr.i("插入数据库中pageName = " + pageName + "时，插入成功 rowID = " + rowID);
                    appInfo.setOrder(rowID);
                }
            }
            if (mEnablePageSlidePointsBar) {
                switch (appInfo.getPageType()) {
                    case AppInfo.PAGE_TYPE_PROGRAM:
                    case AppInfo.PAGE_TYPE_QR_CODE:
                        break;
                    default:
                        addPageSlidePoint();
                        break;
                }
            }

            initUI();
            // mViewPager.setCurrentItem(getMiddlePlaceForFirstItem() +
            // mCurrentPageNum%oldListSize,false);
            mViewPager.setCurrentItem(getMiddlePlaceForFirstItem() + i, false);
        }
    }


    public void setImg(String imgPath) {
        mCUMyRobotImagePath = imgPath;
        AppInfo appInfo = mAppInfos.get(0);
        if (appInfo != null) {
            ViewHoder vh = appInfo.getViewHoder();
            ImageView qr_iv = vh.imageView_qr;
            LogMgr.e("设置201图片 path:" + imgPath);
            Bitmap bm = BitmapFactory.decodeFile(imgPath);
            qr_iv.setImageBitmap(bm);
        }
    }

    /**
     * 添加编程/遥控界面 跳转至编程界面
     *
     * @param i    页面插入的位置
     * @param type 0:编程。1：遥控
     */
    public void addPadAppConnectView(int i, int type) {
        LogMgr.e("addPadAppConnectView - stopAutoToRestWithOutOptionIn3Min()");
        stopAutoToRestWithOutOptionIn3Min();
        if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_PROGRAM) {
            LogMgr.w("addPadAppConnectView() 当前已有编程界面");
            return;
        }

        if (!isForegroundFirst) {
            LogMgr.w("addPadAppConnectView() 当前在后台，不添加编程界面");
            return;
        } else {
            LogMgr.i("addPadAppConnectView() 当前在前台，添加编程界面");
        }

        AppInfo ai = new AppInfo();
        if (type == 0) {
            ai.setName(getString(R.string.programming));
        } else if (type == 1) {
            ai.setName(getString(R.string.telecontrol));
        }
        ai.setPageType(AppInfo.PAGE_TYPE_PROGRAM);
        insertPage(i, ai);
        // mHandler.obtainMessage(BRAIN_ACTIVITY_HANDLER_JUMP_TO_PAGE, 0, 0);
        initUI();
        mViewPager.setCurrentItem(getMiddlePlaceForFirstItem());
        LogMgr.e("3");
        startAnimaAndFunc(mAppInfos.get(i).getViewHoder().imageView_qr, mAppInfos.get(i));
//        LogMgr.e("addPadAppConnectView - stopAutoToRestWithOutOptionIn3Min()");
//        stopAutoToRestWithOutOptionIn3Min();

    }

    /**
     * 删除 编程/遥控界面
     */
    public void delPadAppConnectView(int j, boolean needToJumpToFirstPage) {
        LogMgr.i("delPadAppConnectView() + 删除 编程:" + mAppInfos.get(j).getName());
        if (!mAppInfos.isEmpty()) {
            AppInfo aif = mAppInfos.get(j);
            if (aif != null && aif.getName() != null) {
                if (aif.getName().equals(getString(R.string.programming)) || aif.getName().equals(getString(R.string.telecontrol))
                        || aif.getPageType() == AppInfo.PAGE_TYPE_MOTOR) {
                    LogMgr.d("delPadAppConnectView() 第一页面是编程页面 删除");
                    mAppInfos.remove(j);
                    if (mEnablePageSlidePointsBar && aif.getPageType() == AppInfo.PAGE_TYPE_MOTOR) {
                        removePageSlidePoint();
                    }
                    initUI();
                    if (mCurrentPageAppInfo.getPageType() == AppInfo.PAGE_TYPE_PROGRAM) {
                        LogMgr.d("delPadAppConnectView() 当前页面为编程界面");
                        int nextPageIndex = 0;
                        int lastPageIndex = mAppInfos.indexOf(mLastAppInfoBeforeEnterProgramPage);
                        if (lastPageIndex != -1) {
                            nextPageIndex = lastPageIndex;
                        }
                        LogMgr.d("delPadAppConnectView() nextPageIndex = " + nextPageIndex);
//                        mCurrentAppInfo = mAppInfos.get(nextPageIndex);
                        mViewPager.setCurrentItem(getMiddlePlaceForFirstItem() + nextPageIndex, false);
                    } else {
                        LogMgr.d("delPadAppConnectView() 当前页面不是编程界面");
                        int nextPageIndex = 0;
                        int lastPageIndex = mAppInfos.indexOf(mCurrentPageAppInfo);
                        if (lastPageIndex != -1) {
                            nextPageIndex = lastPageIndex;
                        }
                        LogMgr.d("delPadAppConnectView() nextPageIndex = " + nextPageIndex);
//                        mCurrentAppInfo = mAppInfos.get(nextPageIndex);
                        mViewPager.setCurrentItem(getMiddlePlaceForFirstItem() + nextPageIndex, false);
                    }
                } else if (aif.getName().equals(getString(R.string.rest))) {
                    LogMgr.d("delPadAppConnectView() 第一页面是休息界面 不作操作");
                    if (needToJumpToFirstPage) {
                        initUI();
//                        mCurrentAppInfo = mAppInfos.get(0);
                        mViewPager.setCurrentItem(getMiddlePlaceForFirstItem(), false);
                    }
                }
            }
        }
    }

    /**
     * 开启编程动画和功能
     */
    private void startProgramAnimaAndFunc() {
        if (!isForegroundFirst) {
            LogMgr.w("startProgramAnimaAndFunc() 当前在后台，不开启");
            return;
        } else {
            LogMgr.i("startProgramAnimaAndFunc() 当前在前台，开启");
        }
        if (mAppInfos.get(0).getPageType() == AppInfo.PAGE_TYPE_PROGRAM) {
            LogMgr.e("4");
            startAnimaAndFunc(mAppInfos.get(0).getViewHoder().imageView_qr, mAppInfos.get(0));
        } else {
            LogMgr.e("当前没有编程界面");
        }
    }


    /**
     * 删除页面
     *
     * @param appInfo
     */
    public void delViewPager(final int i, final AppInfo appInfo) {
        Utils.showTwoButtonDialog(BrainActivity.this, getString(R.string.tishi), getString(R.string.delete),
                getString(R.string.cancel),
                getString(R.string.determine), false, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setScrollble(true);
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (appInfo != null) {
                            if (appInfo == mCurrentAppInfo) {
                                stopAnimaAndFunc(false);
                            }
                            String pageName = null;
                            if (appInfo.getPageType() == AppInfo.PAGE_TYPE_FILE) {
                                String fileName = appInfo.getPathName();
                                pageName = fileName;
                                if (!TextUtils.isEmpty(fileName)) {
                                    File file = new File(fileName);
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    file = null;
                                }
                            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_APK) {
                                pageName = appInfo.getPkgName();
                                ServerHeartBeatProcesser.getInstance().feedbackToAppStore(appInfo.getPkgName(), true);
                            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_RECORD) {
                            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_IMAGE) {
                            }

                            if (mBrainDatabaseHelper.deleteTablePageSortInfo(mSqLiteDatabase, pageName)) {
                                LogMgr.i("删除文件排序表成功 pageName = " + pageName);
                            } else {
                                LogMgr.e("删除文件排序表失败 pageName = " + pageName);
                            }

                            int realIndex = mAppInfos.indexOf(appInfo);
                            LogMgr.i("删除对象的原index = " + i + " 真实index = " + realIndex);
//                            mViewHoders.remove(realIndex);
                            mAppInfos.remove(realIndex);
                            if (mEnablePageSlidePointsBar) {
                                removePageSlidePoint();
                            }
                            initUI();
                            if (realIndex < mAppInfos.size()) {
                                mViewPager.setCurrentItem(getMiddlePlaceForFirstItem() + realIndex, false);
                            } else {
                                mViewPager.setCurrentItem(getMiddlePlaceForFirstItem(), false);
                            }
                            mViewPager.setScrollble(true);
                        }
                    }
                });
    }

    /**
     * 查询文件
     */
    private void queryFile() {
        File rootFile = BrainUtils.getDiskCacheDir(this);
        if (!rootFile.isDirectory()) {
            return;
        }
        File file = null;
        int fileType = AppInfo.FILE_TYPE_UNKNOWN;
        for (int i = 0; i < 3; i++) {
            switch (i) {
                case AppInfo.FILE_TYPE_PROJECT_PROGRAM:
                    file = BrainUtils.getDiskCacheDir(BrainUtils.ABILIX_PROJECT_PROGRAM, this);
                    fileType = AppInfo.FILE_TYPE_PROJECT_PROGRAM;
                    break;
                case AppInfo.FILE_TYPE_CHART:
                    file = BrainUtils.getDiskCacheDir(BrainUtils.ABILIX_CHART, this);
                    fileType = AppInfo.FILE_TYPE_CHART;
                    break;
                case AppInfo.FILE_TYPE_SCRATCH:
                    file = BrainUtils.getDiskCacheDir(BrainUtils.ABILIX_SCRATCH, this);
                    fileType = AppInfo.FILE_TYPE_SCRATCH;
                    break;
                // case Mode.FILE_TYPE_SKILLPLAYER:
                // file = BrainUtils.getDiskCacheDir(
                // BrainUtils.ABILIX_SKILLPLAYER, this);
                // fileType = Mode.FILE_TYPE_SKILLPLAYER;
                // if (file.listFiles().length > 0) {
                // isAddSkillPlay = true;
                // } else {
                // isAddSkillPlay = false;
                // }
                // break;
            }
            if (file != null) {
                addFile(file, fileType);
            }
        }
        BrainUtils.getDiskCacheDirS(FileUtils.DATA_PATH, this);
        file = null;
        file = BrainUtils.getDiskCacheDirS(BrainRecord.RECORD_SCRATCH_VJC_, this);
        // if (file.listFiles().length > 0) {
        // isAddRecord = true;
        // add_Record_Image(1);
        // } else {
        // isAddRecord = false;
        // }
        // file = null;
        file = BrainUtils.getDiskCacheDirS(FileUtils.SCRATCH_VJC_IMAGE_, this);
        // if (file.listFiles().length > 0) {
        // isAddImage = true;
        // add_Record_Image(2);
        // } else {
        // isAddImage = false;
        // }
        file = null;
    }

    // /**
    // * 添加其他页面 ，录音 ，图片
    // *
    // * @param file
    // */
    // private void add_Record_Image(int mode) {
    // AppInfo appInfo = new AppInfo();
    // if (mode == 1) {
    // appInfo.setName(getString(R.string.luyin));
    // appInfo.setPageType(Mode.PAGE_TYPE_RECORD);
    // // appInfo.filterCmdProcess(true);
    // // appInfo.setPathName(file);
    // mAppInfos.add(appInfo);
    // } else if (mode == 2) {
    // appInfo.setName(getString(R.string.zhaopian));
    // appInfo.setPageType(Mode.PAGE_TYPE_IMAGE);
    // // appInfo.setImage(true);
    // // appInfo.setPathName(file);
    // mAppInfos.add(appInfo);
    // }
    // }

    /**
     * 添加文件
     *
     * @param file
     */
    private void addFile(File file, int FileType) {
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length > 0) {
                if (FileType == AppInfo.FILE_TYPE_SKILLPLAYER) {
                    LogMgr.e("此Log不允许出现");
                    AppInfo appInfo = new AppInfo();
                    appInfo.setName(getString(R.string.skill_play));
                    appInfo.setPathName(BrainUtils.ABILIX + BrainUtils.ABILIX_SKILLPLAYER);
                    appInfo.setPageType(AppInfo.PAGE_TYPE_FILE);
                    appInfo.setFileType(FileType);
                    mAppInfos.add(appInfo);
                } else {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            continue;
                        }
                        AppInfo appInfo = new AppInfo();
                        if (f.getName().trim().endsWith(FileUtils.FILE_FORMAT_ELF)) {
                            appInfo.setName(FileUtils.getFileNameNoEx(f.getName().trim()) + FileUtils.FILE_ENDFIX_C);
                        } else {
                            appInfo.setName(FileUtils.getFileNameNoEx(f.getName().trim()));
                        }
//                        appInfo.setName(FileUtils.getFileNameNoEx(f.getName().trim()));
//                        appInfo.setName(f.getName().trim());//修复Bug #17797 【1.1.1.18】下载ProgramA后，发现机器人端出现2个同名项目
                        // 文件绝对路径
                        appInfo.setPathName(f.getAbsolutePath().trim());
                        appInfo.setPageType(AppInfo.PAGE_TYPE_FILE);
                        appInfo.setFileType(FileType);
                        //排序相关
                        long rowId = mBrainDatabaseHelper.queryTablePageSortInfo(mSqLiteDatabase, f.getAbsolutePath());
                        if (rowId == -1) {
                            LogMgr.i("页面排序表中不存在页面" + f.getName() + "的信息 f.getAbsolutePath() = " + f.getAbsolutePath());
                            long newRowId = mBrainDatabaseHelper.insertTablePageSortInfo(mSqLiteDatabase, f
                                    .getAbsolutePath(), FileType);
                            if (newRowId == -1) {
                                LogMgr.e("页面排序表插入失败f.getAbsolutePath() = " + f.getAbsolutePath() + " 此页面不显示处理");
                                continue;
                            } else {
                                LogMgr.i("页面排序表插入成功f.getAbsolutePath() = " + f.getAbsolutePath() + " newRowId = " +
                                        newRowId);
                                appInfo.setOrder(newRowId);
                            }
                        } else {
                            LogMgr.i("页面排序表中存在页面" + f.getName() + "的信息 f.getAbsolutePath() = " + f.getAbsolutePath()
                                    + " rowId = " + rowId);
                            appInfo.setOrder(rowId);
                        }
                        mAppInfos.add(appInfo);
                        if (mEnablePageSlidePointsBar) {
                            addPageSlidePoint();
                        }
                    }
                }
            }
        }
    }

    /**
     * 查询apk
     */
    private void queryAppInfo() {
        mMainIntent = new Intent(Intent.ACTION_MAIN, null);
        mMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mMainIntent.addCategory(GlobalConfig.APP_ABILIX_LAUNCHER);

        List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(mMainIntent, 0);

        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(mPackageManager));
        if (mAppInfos != null) {
            for (ResolveInfo reInfo : resolveInfos) {
                String activityName = reInfo.activityInfo.name;
                String pkgName = reInfo.activityInfo.packageName;
                String appLabel = (String) reInfo.loadLabel(mPackageManager).toString();
                Drawable icon = reInfo.loadIcon(mPackageManager);
                ApplicationInfo appInfo = null;
                try {
                    appInfo = mPackageManager.getApplicationInfo(pkgName, 0);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                String appFile = appInfo.sourceDir;
                long installed = new File(appFile).lastModified();
                Intent launchIntent = mPackageManager.getLaunchIntentForPackage(pkgName);
                AppInfo appInfos = new AppInfo();
                appInfos.setAppName(activityName);
                appInfos.setAppLabel(appLabel);
                appInfos.setPkgName(pkgName);
                appInfos.setAppIcon(icon);
                appInfos.setIntent(launchIntent);
                appInfos.setInstallTime(installed);
                if (TextUtils.equals(GlobalConfig.APP_PKGNAME_VOLUMECONTROL, pkgName) || pkgName.equals(OCULUSNAME)) {
                    continue;
                }
                /*if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU &&
                        TextUtils.equals(GlobalConfig.APP_PKGNAME_MEMORYGAME, pkgName)) {
                    appInfos.setAppLabel("Memory Game");
                }*/
                // appInfos.setApk(true);
                appInfos.setPageType(AppInfo.PAGE_TYPE_APK);
                if (TextUtils.equals(GlobalConfig.APP_PKGNAME_BRAINSET, pkgName)) {
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
                        appInfos.setOrder(AppInfo.PAGE_ORDER_M_SET);
                    } else {
                        appInfos.setOrder(AppInfo.PAGE_ORDER_SET);
                    }
                    if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                        mAppInfos.add(1, appInfos);
                    } else {
                        mAppInfos.add(3, appInfos);
                    }
                } else if (TextUtils.equals(GlobalConfig.APP_PKGNAME_IM_QOMOLANGMA, pkgName)) {
                    appInfos.setOrder(AppInfo.PAGE_ORDER_IM_QOMOLANGMA);
                    mAppInfos.add(1, appInfos);
                } else if (TextUtils.equals(GlobalConfig.APP_PKGNAME_QOMOLANGMA_SHOW, pkgName)) {
                    appInfos.setOrder(AppInfo.PAGE_ORDER_QOMOLANGMA_SHOW);
                    mAppInfos.add(2, appInfos);
                } else {
                    long rowId = mBrainDatabaseHelper.queryTablePageSortInfo(mSqLiteDatabase, pkgName);
                    if (rowId == -1) {
                        LogMgr.i("页面排序数据库中还没有应用" + pkgName + "的信息");
                        long newRowId = mBrainDatabaseHelper.insertTablePageSortInfo(mSqLiteDatabase, pkgName,
                                BrainDatabaseHelper.PAGE_TYPE_APK);
                        if (newRowId == -1) {
                            LogMgr.e("插入页面排序表失败pkgName = " + pkgName + " 此页面不显示处理");
                            continue;
                        } else {
                            LogMgr.i("插入页面排序表成功pkgName = " + pkgName + " newRowId = " + newRowId);
                            appInfos.setOrder(newRowId);
                        }
                    } else {
                        LogMgr.i("页面排序数据库中有应用" + pkgName + "的信息 rowId = " + rowId);
                        appInfos.setOrder(rowId);
                    }
                    mAppInfos.add(appInfos);
                }
            }
        }
    }

    // /**
    // * 初始化UI线程
    // *
    // * @author luox
    // *
    // */
    // private class AddThread extends Thread {
    // @Override
    // public void run() {
    // try {
    // initData();
    // mHandler.sendEmptyMessage(0x01);
    // } catch (Exception e) {
    // e.printStackTrace();
    // } finally {
    // mHandler.sendEmptyMessage(0x02);
    // }
    // }
    // }

    /**
     * 创建Wifi
     */
    private void createWifiHot() {
        LogMgr.d("init hot pot or connect to wifi");
        if (!WifiUtils.isMyWifiConnected(BrainActivity.this)) {
            if (!wifiAp.isMyWifiHotOpen()) {
                LogMgr.e("robot does not connect to my wifi,try to connect to my wifi");
                if (!WifiUtils.connectSavedWifi(BrainActivity.this)) {
                    LogMgr.e("robot connects to my wifi failed or my wifi does not exist,try to build hot pot");
                    wifiAp.createWifiHot();
                }
            }
        }
    }

    /**
     * 开启页面动画及功能
     *
     * @param view
     */
    private void startAnimaAndFunc(ImageView view, AppInfo appInfo) {
        LogMgr.i("startAnimaAndFunc() " + appInfo.getName());
        // if (idx != mCurrentRunIdx) {


        if (appInfo != mCurrentAppInfo) {
            stopAnimaAndFunc(false);
        } else {
            stopAnima();
        }
        ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.8f, 1f);
        alpha.setRepeatCount(-1);
        rotate.setRepeatCount(-1);

        if (appInfo.getPageType() == AppInfo.PAGE_TYPE_PROGRAM && GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
            if (mAnimatorSetForCUProgram == null || (mAnimatorSetForCUProgram != null && !mAnimatorSetForCUProgram.isPaused())) {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.0f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.0f);
                scaleX.setRepeatCount(-1);
                scaleY.setRepeatCount(-1);
                scaleX.setRepeatMode(ValueAnimator.REVERSE);
                scaleY.setRepeatMode(ValueAnimator.REVERSE);
                mAnimatorSetForCUProgram = new AnimatorSet();
                mAnimatorSetForCUProgram.setInterpolator(new LinearInterpolator());
                mAnimatorSetForCUProgram.playTogether(scaleX, scaleY);
                mAnimatorSetForCUProgram.setDuration(1000);
                mAnimatorSetForCUProgram.start();
            } else {
                mAnimatorSetForCUProgram.resume();
            }
        } else {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.setInterpolator(new LinearInterpolator());
            mAnimatorSet.play(rotate).with(alpha);
            mAnimatorSet.setDuration(8000);
            mAnimatorSet.start();
        }

        // mCurrentRunIdx = idx;
        mCurrentAppInfo = appInfo;
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
//            executeCurrPageFuncForM(mCurrentAppInfo);
//        } else {
//            executeCurrPageFunc(mCurrentAppInfo);
//        }
        executeCurrPageFunc(mCurrentAppInfo);
        LogMgr.e("startAnimaAndFunc - stopAutoToRestWithOutOptionIn3Min");
        stopAutoToRestWithOutOptionIn3Min();
    }

    /**
     * 关闭页面动画及功能
     *
     * @param isDeleteProgramPage
     */
    private void stopAnimaAndFunc(boolean isDeleteProgramPage) {
        LogMgr.e("stopAnimaAndFunc");
        AutoToRestWithOutOptionIn3Min();

//        stopShutdownTiemr();
        endCurrPageFunc(mCurrentAppInfo);
//        if (mCurrentAppInfo != null && mCurrentAppInfo.getPageType() == AppInfo.PAGE_TYPE_PROGRAM && GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C
//                && GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
//            //CU201的编程 暂停动画
//            LogMgr.i("CU201的编程 暂停动画");
//            if (mAnimatorSetForCUProgram != null) {
//                mAnimatorSetForCUProgram.pause();
//            } else {
//                LogMgr.e("mAnimatorSetForCUProgram == null");
//            }
//
//        } else {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet.end();
            mAnimatorSet = null;
        }
//        }
        if (isDeleteProgramPage) {
            if (mAnimatorSetForCUProgram != null) {
                mAnimatorSetForCUProgram.cancel();
                mAnimatorSetForCUProgram.end();
                mAnimatorSetForCUProgram = null;
            }
        }
        // mCurrentRunIdx = -1;
        mCurrentAppInfo = null;
    }

    /**
     * 关闭页面动画
     */
    private void stopAnima() {
        LogMgr.e("关闭动画");
        if (mAnimatorSet != null) {
            LogMgr.e("关闭动画");
            mAnimatorSet.cancel();
            mAnimatorSet.end();
            mAnimatorSet = null;
        }
    }

    /**
     * 关闭页面动画及功能
     */
    public void stopAnimaAndFuncFromOutside() {
        mBrainActivityHandler.obtainMessage(BRAINACTIVITY_HANDLER_MESSAGE_STOP_ANI_AND_FUNC).sendToTarget();
    }

    /**
     * 开启编程动画和功能
     */
    public void startProgramPageAnimaAndFuncFromOutside() {
        mBrainActivityHandler.obtainMessage(BRAINACTIVITY_HANDLER_MESSAGE_START_PROGRAM_ANI_AND_FUNC).sendToTarget();
    }

    public void stopAnimaFromOutSide() {
        mBrainActivityHandler.obtainMessage(BRAINACTIVITY_HANDLER_MESSAGE_STOP_ANI).sendToTarget();
    }


    /**
     * @param openOrClose 0:close 1:open
     * @param color
     */
    public void screenTest(int openOrClose, int color) {
        mBrainActivityHandler.obtainMessage(BRAINACTIVITY_HANDLER_MESSAGE_SCREEN_TEST, openOrClose, color)
                .sendToTarget();
    }

    // /**
    // * 仅关闭页面动画
    // */
    // private void stopAnimaOnly(){
    // if (mAnimatorSet != null) {
    // mAnimatorSet.cancel();
    // mAnimatorSet.end();
    // mAnimatorSet = null;
    // }
    // }

    // private BrainDatabaseHelper brainDatabaseHelper;
    // private SQLiteDatabase db;
    // private int index = 10000;


    /**
     * 页面功能开始执行
     *
     * @author wsl
     */
//    private void executeCurrPageFuncForM(AppInfo appInfo) {
//        if (null == appInfo) {
//            LogMgr.e("executeCurrPageFunc() null == appInfo is true");
//            return;
//        }
//        LogMgr.i("开始执行 executeCurrPageFunc " + appInfo.getName());
//        try {
//            LogMgr.d("appInfo.getPageType() = " + appInfo.getPageType());
//            //编程
//            if (appInfo.getPageType() == AppInfo.PAGE_TYPE_PROGRAM) {
//                changeProgramStatus(true);
//                //File
//            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_FILE) {
//                // isRecord_No = false;
//                mIsWantToCloseTextViewForVjc = false;
//                // stop_paizhao = false;
//                int type = appInfo.getFileType();
//                String filePath = appInfo.getPathName();
//                LogMgr.i("开始执行filePath:" + filePath);
//                // 进入skillplay二级界面
//                if (appInfo.getFileType() == AppInfo.FILE_TYPE_SKILLPLAYER) {
//                    startActivity(ManageFragment.class, AppInfo.FILE_TYPE_SKILLPLAYER);
//                    stopAnimaAndFunc(false);
//                    //進入二級二級頁面不需要10秒進入情緒gif
//                    stopAD();
//                } else {
//                    exeFileFunc(type, filePath, 1, appInfo.getName());
//                }
//                //APK
//            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_APK) {
//                stopAnimaAndFunc(false); // 首先停止动画 复原标记 解决回来后 需要再点2次才能运行的问题
//                Utils.openApp(BrainActivity.this, appInfo.getPkgName(), null, false);
//                //進入設置頁面不需要10秒進入情緒gif
//                stopAD();
//                //未来科学站
//            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_M_FUTURE_SCIENCE) {
//                // 进入未来科学站二级界面
//                startActivity(FutureScienceActivity.class, AppInfo.PAGE_TYPE_M_FUTURE_SCIENCE);
//                stopAnimaAndFunc(false);
//                //進入二級二級頁面不需要10秒進入情緒gif
//                stopAD();
//                //未来酷乐园
//            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_M_FUTURE_COOL) {
//                startActivity(FutureCoolPlayActivity.class, AppInfo.PAGE_TYPE_M_FUTURE_COOL);
//                stopAnimaAndFunc(false);
//                //進入二級二級頁面不需要10秒進入情緒gif
//                stopAD();
//                //能力训练营
//            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_M_ABILITY_TRAINING) {
//                startActivity(TrainingActivity.class, AppInfo.PAGE_TYPE_M_ABILITY_TRAINING);
//                stopAnimaAndFunc(false);
//                //進入二級二級頁面不需要10秒進入情緒gif
//                stopAD();
//                //TODO 我叫奥科流思
//            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_M_AKLS) {
//
//                //進入其他应用不需要10秒進入情緒gif
//                stopAD();
//            } else {//wifi模式亮绿灯,然后关闭
//                for (int i = 0; i < 3; i++) {
//                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H) {
//                        BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
//                                ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0x6A, new byte[]{0x04, 0x01, 0, (byte) 0xFF, 0, (byte) 0xFE, 0x00}), null, 0, 0);
//                    } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
//                        BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
//                                ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0x74, new byte[]{0, (byte) 0xFF, 0}), null, 0, 0);
//                    } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
//                        BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
//                                ProtocolUtil.buildProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x32, new byte[]{0, (byte) 0xFF, 0}), null, 0, 0);
//                    }
//                }
//            }
//
//            mBrainActivityHandler.sendMessageDelayed(mBrainActivityHandler.obtainMessage
//                    (374), 2000);
//        } catch (Exception e) {
//            LogMgr.e("executeCurrPageFunc() 执行页面功能异常");
//            e.printStackTrace();
//        }
//    }


    /**
     * 页面功能开始执行
     *
     * @author wsl
     */
    private void executeCurrPageFunc(AppInfo appInfo) {
        if (null == appInfo) {
            LogMgr.e("executeCurrPageFunc() null == appInfo is true");
            return;
        }
        LogMgr.i("开始执行 executeCurrPageFunc " + appInfo.getName());
        try {
//            if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_C9) {
//                //K9执行文件或打开应用时重新设置8个DO口
//                LogMgr.d("K9执行文件或打开应用时重新设置8个DO口");
//                byte[] cmdData = ProtocolUtil.buildProtocol((byte) 0x01, (byte) 0xA4, (byte) 0x40, new byte[]{(byte) 0xFF, (byte) 0xFF});
//                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL, cmdData, null, 0, 0);
//            } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C && GlobalConfig.BRAIN_CHILD_TYPE != GlobalConfig.ROBOT_TYPE_CU) {
//                //K5编程连接状态重新设置8个DO口
//                LogMgr.d("K5执行文件或打开应用时重新设置8个DO口");
//                byte[] cmdData = ProtocolUtil.buildProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x20, new byte[]{(byte) 0xFF, (byte) 0xFF});
//                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL, cmdData, null, 0, 0);
//            }
            LogMgr.d("appInfo.getPageType() = " + appInfo.getPageType());
            if (appInfo.getPageType() == AppInfo.PAGE_TYPE_REST) {
                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_REST_CMD, null,
                        null, 1, 0);
                setScreenBrightness(0);
                Application.getInstance().setIsRestState(true);
                if(mViewPager != null){
                    mViewPager.setNoScroll(true);
                }
//                startShutdown(100);
            } else{
                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_REST_CMD, null,
                        null, 2, 0);//这里是启动其他功能的时候先停止头部触摸的检测；
                if (appInfo.getPageType() == AppInfo.PAGE_TYPE_SOUL) {
                    // 执行soul功能
//                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_SOUL_CMD, null,
//                        null, 1, 0);
                    // TODO 只有M系列替换成我叫奥克流思apk  lz  2017-7-29 11:19:25
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M && (GlobalConfig.BRAIN_CHILD_TYPE !=
                            GlobalConfig.ROBOT_TYPE_M3S && GlobalConfig.BRAIN_CHILD_TYPE != GlobalConfig.ROBOT_TYPE_M4S)) {
                        stopAnimaAndFunc(false); // 首先停止动画 复原标记 解决回来后 需要再点2次才能运行的问题
                        Utils.openApp(BrainActivity.this, OCULUSNAME, null, false);
                    } else {
//                    startShutdown(30 * 1000);
                        BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_SOUL_CMD,
                                null, null, 1, 0);
                    }
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_PROGRAM) {
                    changeProgramStatus(true);
                    if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C
                            && BrainService.getmBrainService().getmAppTypeConnected() == GlobalConfig.APP_TYPE_PROGRAM_ROBOT) {
                        openVideo();
                    }
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_FILE) {
                    // isRecord_No = false;
                    mIsWantToCloseTextViewForVjc = false;
                    // stop_paizhao = false;
                    int type = appInfo.getFileType();
                    String filePath = appInfo.getPathName();
                    LogMgr.i("开始执行filePath:" + filePath);
                    // 进入skillplay二级界面
                    if (appInfo.getFileType() == AppInfo.FILE_TYPE_SKILLPLAYER) {
                        startActivity(ManageFragment.class, AppInfo.FILE_TYPE_SKILLPLAYER);
                        stopAnimaAndFunc(false);
                    } else {
                        exeFileFunc(type, filePath, 1, appInfo.getName());
                    }
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_APK) {
                    if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU
                            && TextUtils.equals(GlobalConfig.APP_PKGNAME_BRAINSET, appInfo.getPkgName())
                            && updateOperationState == GlobalConfig.UPDATE_ONLINE_OPERATION_IGNORED) {// 设置界面
                        stopAnimaAndFunc(false); // 首先停止动画 复原标记 解决回来后 需要再点2次才能运行的问题
                        Utils.openApp(BrainActivity.this, GlobalConfig.APP_PKGNAME_UPDATEONLINETEST, null, false);
                        updateOperationStateEntrySettingsFlag = true;
                    } else {
                        stopAnimaAndFunc(false); // 首先停止动画 复原标记 解决回来后 需要再点2次才能运行的问题
                        Utils.openApp(BrainActivity.this, appInfo.getPkgName(), appInfo.getAppName(), false);
                    }
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_RECORD) {
                    startActivity(ManageFragment.class, AppInfo.PAGE_TYPE_RECORD);
                    stopAnimaAndFunc(false);
                    // MyplayRecord();
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_IMAGE) {
                    stopAnimaAndFunc(false);
                    startActivity(ManageFragment.class, AppInfo.PAGE_TYPE_IMAGE);
                    // mImageView.setVisibility(View.VISIBLE);
                    // mBitmap =
                    // BitmapFactory.decodeFile(FileUtils.SCRATCH_VJC_IMAGE);
                    // if (mBitmap != null) {
                    // mImageView.setImageBitmap(mBitmap);
                    // }
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_MULTIMEDIA) {
                    stopAnimaAndFunc(false);
                    startActivity(MultimediaActivity.class);
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_ACTIVATE_H) {
                    LogMgr.d("激活功能 启动");
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_PROTECT_H) {
                    LogMgr.d("保护功能 启动");
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_RESET) {
                    LogMgr.d("开启复位");
                    BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_REST_CMD, null,
                            null, 2, 0);//这里是停止了头部检测功能，并复位
                    //停止所有
                    BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_STOP_ALL_CMD, null, null, 0, 0);
                    mBrainActivityHandler.sendEmptyMessageDelayed(4, 500);
                } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_GROUP) {
                    LogMgr.d("群控 开启");
                    Application.getInstance().setisKeepGroup(true);
                    Application.getInstance().setisKeepGroupLoadingComPelte(true);
                    //开启眼睛灯，判断是热点还是wifi
                    BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                            ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0x0C, (byte) 0x01, null), null, 0, 0);//开启433 暂时关闭
                    if (wifiAp.isMyWifiHotOpen()) {//热点是否打开了，热点打开亮红灯，H3 H5 不一样
                        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H) {
                            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                    ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0xC0, getRGBEyesBlink(0)), null, 0, 0);
                        } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                    ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0x74, new byte[]{(byte) 0xFF, 0, 0}), null, 0, 0);
                        }
                    } else {//wifi模式亮绿灯,然后关闭
                        for (int i = 0; i < 3; i++) {
                            if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H) {
                                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                        ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0xC0, getRGBEyesBlink(1)), null, 0, 0);
                            } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                        ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0x74, new byte[]{0, (byte) 0xFF, 0}), null, 0, 0);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            LogMgr.e("executeCurrPageFunc() 执行页面功能异常");
            e.printStackTrace();
        }
    }
    private byte[] getRGBEyesBlink(int redOrBlue){//0:红灯，1：绿灯  只有眼部的灯
        byte[] color = new byte[10];//这里显示的双眼的灯
        color[0] = 2;
        color[1] = 2;
        color[2] = 2;
        color[6] = 3;
        switch (redOrBlue){
            case 0://红灯
                color[3] = (byte)0xFF;
                color[7] = (byte)0xFF;
                break;
            case 1://绿灯
                color[4] = (byte)0xFF;
                color[8] = (byte)0xFF;
                break;
            case 2:
                color[3] = (byte)0;
                color[4] = (byte)0;
                color[5] = (byte)0;
                color[7] = (byte)0;
                color[8] = (byte)0;
                color[9] = (byte)0;
                break;
        }
        return color;
    }
    private void startActivity(Class<?> cls, int id) {
        Intent intent = new Intent(BrainActivity.this, cls);
        intent.putExtra("id", id);
        // startActivityForResult(intent, id);
        startActivity(intent);
    }

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(BrainActivity.this, cls);
        // startActivityForResult(intent, id);
        startActivity(intent);
    }

    /**
     * 页面功能停止执行
     *
     * @author wsl
     */
    private void endCurrPageFunc(AppInfo appInfo) {
        if (appInfo == null) {
            LogMgr.e("endCurrPageFunc() appInfo == null");
            return;
        }
        LogMgr.i("停止执行 endCurrPageFunc() " + appInfo.getName());
        try {
            LogMgr.d("appInfo.getPageType() = " + appInfo.getPageType());
            if (appInfo.getPageType() == AppInfo.PAGE_TYPE_REST) {
                // SendBroadCastToService(GlobalConfig.ACTION_SERVICE_MODE_REST,
                // 0, null);
                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_REST_CMD, null,
                        null, 0, 0);
                setScreenBrightness(255);
                Application.getInstance().setIsRestState(false);
                if(mViewPager != null){
                    mViewPager.setNoScroll(false);
                }
            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_SOUL) {
                // SendBroadCastToService(GlobalConfig.ACTION_SERVICE_MODE_SOUL,
                // 0, null);
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M && (GlobalConfig.BRAIN_CHILD_TYPE !=
                        GlobalConfig.ROBOT_TYPE_M3S && GlobalConfig.BRAIN_CHILD_TYPE != GlobalConfig.ROBOT_TYPE_M4S)) {
                    //do nothing
                } else {
                    BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_SOUL_CMD, null, null, 0, 0);
                }
            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_PROGRAM) {
                changeProgramStatus(false);
                stopVideo(true);
                stopWindow();
                stopRecordView();
            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_FILE) {
                int type = appInfo.getFileType();
                exeFileFunc(type, null, 0, null);
                //添加判断，S类型
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_S) {
                    //   USBVideo.GetManger(BrainActivity.getmBrainActivity(), null,
                    //        null).setBrightnessS(0x00,1);
                }
            } // 进入apk
            else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_APK) {

            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_RECORD) {
                // MystopPlayRecord();
            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_IMAGE) {

            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_ACTIVATE_H) {
                LogMgr.d("激活功能 停止");
            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_PROTECT_H) {
                LogMgr.d("保护功能 停止");
            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_GROUP) {
                LogMgr.d("群控 停止");
                Application.getInstance().setisKeepGroup(false);
                //关灯
                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                        ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0x0C, (byte) 0x00, null), null, 0, 0);//暂时关闭433
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H) {
//                    BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
//                            ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0xC0, getRGBEyesBlink(2)), null, 0, 0);
                } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                    BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                            ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0x74, new byte[]{0, 0, 0}), null, 0, 0);
                    ProtocolUtils.controlSkillPlayer((byte) 0x10, null);
                }
                BrainService.getmBrainService().sendMessageToControl(15, null, null, 23, 0);
            }
            // }
        } catch (Exception e) {
            LogMgr.d("endCurrPageFunc() 页面功能停止执行异常");
            e.printStackTrace();
        }
    }

    // /**
    // * 设置休息与soul的状态
    // */
    // private boolean setRestSoul(int idx, int mode) {
    // LogMgr.e("setRestSoul:" + " idx:" + idx + " mode:" + mode
    // + " isProgrammingUI:" + isProgrammingUI);
    // boolean b = false;
    // if (isProgrammingUI) {
    // if (idx == 1) {
    // // 进入休息状态
    // SendBroadCastToService(GlobalConfig.ACTION_SERVICE_MODE_REST,
    // mode, null);
    // b = true;
    // } else if (idx == 2) {
    // // 执行soul功能
    // SendBroadCastToService(GlobalConfig.ACTION_SERVICE_MODE_SOUL,
    // mode, null);
    // b = true;
    // }
    // } else {
    // if (idx == 0) {
    // // 进入休息状态
    // SendBroadCastToService(GlobalConfig.ACTION_SERVICE_MODE_REST,
    // mode, null);
    // b = true;
    // } else if (idx == 1) {
    // // 执行soul功能
    // SendBroadCastToService(GlobalConfig.ACTION_SERVICE_MODE_SOUL,
    // mode, null);
    // b = true;
    // }
    // }
    // return b;
    // }

    /**
     * 改变编程状态下 udp命令是否执行。 转圈时执行，不转圈时不执行。
     *
     * @param isBrainInfoActive
     */
    private void changeProgramStatus(boolean isBrainInfoActive) {
        LogMgr.i("changeProgramStatus() isBrainInfoActive = " + isBrainInfoActive);
        // SendBroadCastToService(GlobalConfig.ACTION_SERVICE_MODE_CONNECT,
        // mode, null);
        LogMgr.e("setBrainInfoState:" + isBrainInfoActive);
        if (isBrainInfoActive) {
            // 编程激活时，先停止所有动作，再激活BrainInfo
            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_STOP_ALL_CMD, null,
                    null, isBrainInfoActive ? 1 : 0, 0);
            BrainService.getmBrainService().setBrainInfoState(isBrainInfoActive);
        } else {
            // 编程停止时，先停止Braininfo，再停止所有动作
            BrainService.getmBrainService().setBrainInfoState(isBrainInfoActive);
            BrainService.getmBrainService().removeMessageToSendToControlFromHandler();
            if (mCurrentAppInfo != null) {
                LogMgr.v("changeProgramStatus() mCurrentAppInfo != null 发送停止所有命令到Control");
                BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_STOP_ALL_CMD,
                        null, null, isBrainInfoActive ? 1 : 0, 0);
            } else {
                LogMgr.v("changeProgramStatus() mCurrentAppInfo == null");
            }

        }
    }

    /**
     * 连接断开后的处理
     */
    public void afterTCPDisconnected() {
//        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
//            for (int i = 0; i < mAppInfos.size(); i++) {
//                if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_MOTOR) {
//                    delPadAppConnectView(i, true);
//                }
//            }
//        }
        LogMgr.e("afterTCPDisconnected");
        AutoToRestWithOutOptionIn3Min();
        //断开连接之后恢复正常，手灯脚灯
        BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_REST_CMD, null,
                null, 3, 0);//这里是开启手部脚部灯光；
        changeProgramStatus(false);
        delPadAppConnectView(0, true);
        stopAnimaAndFunc(true);
        stopVideo(false);
        stopWindow();
        stopRecordView();
        setVJCViewStop();
        setStopImageView();
        Utils.sendBroadcast(BrainActivity.this, GlobalConfig.ACTION_TCP_DISCONNECT);
        // jingh add 停止RTSP视频传输服务
        RTSPServiceMgr.stopRTSPService();
    }

    /**
     * 执行/停止文件页面的功能
     *
     * @param fileType 文件类型
     * @param filePath 文件路径
     * @param mode     0:停止 1：执行
     * @param name     页面名
     */
    private void exeFileFunc(int fileType, String filePath, int mode, String name) {
        LogMgr.i("exeFileFunc() fileType = " + fileType + " filePath = " + filePath + " mode = " + mode
                + " pageName = " + name);
        switch (fileType) {
            case AppInfo.FILE_TYPE_PROJECT_PROGRAM:
                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_S) {
                    LogMgr.i("执行项目编程页面功能，当前机器人类型为S,按照S的项目编程解析");
                    if (mode == 0) {
                        byte[] cmd = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0x02, (byte)
                                        0x03,
                                null);
                        BrainService.getmBrainService().sendMessageToControl(
                                GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL, cmd, null, 0, 0);
                    } else {
                        // 获取到的应该是完整的命令
                        byte[] content = FileUtils.readFileRetrunByteArray(filePath);
                        BrainService.getmBrainService().sendMessageToControl(
                                GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL, content, null, 1, 0);
                    }
                    break;
                }
                if (GlobalConfig.IS_USING_EXPLAINER_APP) {
                    if (mode == 0) {
                        // donothing
                    } else {
                        mLastAppInfoBeforeEnterExplainer = mCurrentAppInfo;
                        stopAnimaAndFunc(false);
                        Utils.openAppChart(BrainActivity.this, GlobalConfig.CHART_AND_PROJECT_PROGRAM_APP_PACKAGE_NAME,
                                filePath, name);
                    }
                } else {
                    // BrainService.getmBrainService().exeFilePageFunc(filePath,
                    //       GlobalConfig.CONTROL_CALLBACKMODE_PROGRAM_CMD, mode);
                }
                break;
            case AppInfo.FILE_TYPE_CHART:

                if (GlobalConfig.IS_USING_EXPLAINER_APP) {
                    if (mode == 0) {
                        // donothing
                    } else {
                        mLastAppInfoBeforeEnterExplainer = mCurrentAppInfo;
                        stopAnimaAndFunc(false);
                        Utils.openAppChart(BrainActivity.this, GlobalConfig.CHART_AND_PROJECT_PROGRAM_APP_PACKAGE_NAME,
                                filePath, name);
                    }
                    if (filePath != null && filePath.endsWith(FileUtils.FILE_FORMAT_ELF)) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        LogMgr.d("elf文件，发给Control");
                        BrainService.getmBrainService().exeFilePageFunc(filePath, GlobalConfig
                                        .CONTROL_CALLBACKMODE_SEND_ELF_FILE_CMD,
                                mode);
                    }

                } else {
                    BrainService.getmBrainService().exeFilePageFunc(filePath, GlobalConfig
                                    .CONTROL_CALLBACKMODE_CHART_CMD,
                            mode);
                }
                break;
            case AppInfo.FILE_TYPE_SCRATCH:
                if (GlobalConfig.IS_USING_EXPLAINER_APP) {
                    if (mode == 0) {
                        // donothing
                    } else {
                        mLastAppInfoBeforeEnterExplainer = mCurrentAppInfo;
                        stopAnimaAndFunc(false);
                        Utils.openAppChart(BrainActivity.this, GlobalConfig.CHART_AND_PROJECT_PROGRAM_APP_PACKAGE_NAME,
                                filePath, name);
                    }
                } else {
                    BrainService.getmBrainService().exeFilePageFunc(filePath,
                            GlobalConfig.CONTROL_CALLBACKMODE_SCRATCH_FILE_CMD, mode);
                }
                break;
            case AppInfo.FILE_TYPE_SKILLPLAYER:
                BrainService.getmBrainService().exeFilePageFunc(filePath,
                        GlobalConfig.CONTROL_CALLBACKMODE_SKILLPLAYER_CMD, mode);
                break;
            default:
                break;
        }
    }

    /**
     * 插入文件页面 并跳至该界面
     */
    private void insertFileView(String fullPath) {
        File file = new File(fullPath);
        LogMgr.d("insertFileView fullPath = " + fullPath);
        LogMgr.d("insertFileView !file.exists():" + !file.exists() + " !file.isDirectory():" + !file.isDirectory());
        if (!file.exists()) {
            file = null;
            return;
        }
        // file = null;

        String filePath = fullPath.substring(0, fullPath.lastIndexOf(File.separator));
        String fileName = fullPath.substring(fullPath.lastIndexOf(File.separator) + 1, fullPath.length());
        int fileType = AppInfo.FILE_TYPE_UNKNOWN;
        if (filePath.indexOf(BrainUtils.ABILIX_PROJECT_PROGRAM) != -1) {
            fileType = AppInfo.FILE_TYPE_PROJECT_PROGRAM;
        } else if (filePath.indexOf(BrainUtils.ABILIX_CHART) != -1) {
            fileType = AppInfo.FILE_TYPE_CHART;
        } else if (filePath.indexOf(BrainUtils.ABILIX_SCRATCH) != -1) {
            fileType = AppInfo.FILE_TYPE_SCRATCH;
        } else if (filePath.indexOf(BrainUtils.ABILIX_SKILLPLAYER) != -1) {
            fileType = AppInfo.FILE_TYPE_SKILLPLAYER;
        }
        AppInfo appInfo = new AppInfo();
        // 文件名称
        if (fileType == AppInfo.FILE_TYPE_SKILLPLAYER) {
            LogMgr.i("尝试插入的页面为skillplayer文件，不添加页面");
//			if (!isAddSkillPlay) {
//				isAddSkillPlay = true;
//				appInfo.setName(getString(R.string.skill_play));
//				appInfo.setPathName(BrainUtils.ABILIX
//						+ BrainUtils.ABILIX_SKILLPLAYER);
//				appInfo.setPageType(Mode.PAGE_TYPE_FILE);
//				appInfo.setFileType(fileType);
//				insertPage(mAppInfos.size(), appInfo);
//			}
        } else {
            // 文件绝对路径
            if (fileName.endsWith(FileUtils.FILE_FORMAT_ELF)) {
                appInfo.setName(FileUtils.getFileNameNoEx(fileName) + FileUtils.FILE_ENDFIX_C);
            } else {
                appInfo.setName(FileUtils.getFileNameNoEx(fileName));
            }

//            appInfo.setName(FileUtils.getFileNameNoEx(fileName));
//            appInfo.setName(fileName);
            appInfo.setPathName(fullPath);
            // appInfo.setFile(true);
            appInfo.setPageType(AppInfo.PAGE_TYPE_FILE);
            appInfo.setFileType(fileType);
            insertPage(mAppInfos.size(), appInfo);
        }
    }

    /**
     * 刷新页面 不增加页面
     *
     * @param fullPath
     */
    public void refreshView(String fullPath) {
        File file = new File(fullPath);
        LogMgr.d("refreshView fullPath = " + fullPath);
        LogMgr.d("refreshView !file.exists():" + !file.exists() + " !file.isDirectory():" + !file.isDirectory());
        if (!file.exists()) {
            file = null;
            return;
        }
        int pageIndex = 0;
        /** 是否找到原页面 */
        boolean hasFreshView = false;

        // String filePath = fullPath.substring(0,
        // fullPath.lastIndexOf(File.separator));
        String fileName = fullPath.substring(fullPath.lastIndexOf(File.separator) + 1, fullPath.length());
        for (int i = 0; i < mAppInfos.size(); i++) {
            if (mAppInfos.get(i).getPathName() != null && mAppInfos.get(i).getPathName().equalsIgnoreCase(fullPath)) {
                hasFreshView = true;
                pageIndex = i;
                mAppInfos.get(i).setPathName(fullPath);
                if (fileName.endsWith(FileUtils.FILE_FORMAT_ELF)) {
                    mAppInfos.get(i).setName(FileUtils.getFileNameNoEx(fileName) + FileUtils.FILE_ENDFIX_C);
                } else {
                    mAppInfos.get(i).setName(FileUtils.getFileNameNoEx(fileName));
                }
//                mAppInfos.get(i).setName(FileUtils.getFileNameNoEx(fileName));
//                mAppInfos.get(i).setName(fileName);
                ViewHoder vh = mAppInfos.get(i).getViewHoder();
                vh.textview.setText(mAppInfos.get(i).getName());
                mAppInfos.get(i).setViewHoder(vh);
//                mViewHoders.set(i, vh);
                break;
            }
        }
        if (!hasFreshView) {
            insertFileView(fullPath);
        } else {
            LogMgr.d("refreshView 数据更新完成");
            initUI();
            mViewPager.setCurrentItem(getMiddlePlaceForFirstItem() + pageIndex);
        }
    }

    /**
     * 插入apk页面
     */
    public void insertApkView(String apkPakName) {
        if (apkPakName != null) {
            if (mMainIntent == null) {
                mMainIntent = new Intent(Intent.ACTION_MAIN, null);
                mMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                mMainIntent.addCategory(GlobalConfig.APP_ABILIX_LAUNCHER);
            }
            List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(mMainIntent, 0);

            Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(mPackageManager));
            if (mAppInfos != null) {
                for (ResolveInfo reInfo : resolveInfos) {
                    String activityName = reInfo.activityInfo.name;
                    String pkgName = reInfo.activityInfo.packageName;
                    if (apkPakName.equals(pkgName)) {
                        String appLabel = (String) reInfo.loadLabel(mPackageManager).toString();
                        Drawable icon = reInfo.loadIcon(mPackageManager);
                        ApplicationInfo appInfo = null;
                        try {
                            appInfo = mPackageManager.getApplicationInfo(pkgName, 0);
                        } catch (NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        String appFile = appInfo.sourceDir;
                        long installed = new File(appFile).lastModified();
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkgName);
                        AppInfo appInfos = new AppInfo();
                        appInfos.setAppName(activityName);
                        appInfos.setAppLabel(appLabel);
                        appInfos.setPkgName(pkgName);
                        appInfos.setAppIcon(icon);
                        appInfos.setIntent(launchIntent);
                        appInfos.setInstallTime(installed);
                        // appInfos.setApk(true);
                        appInfos.setPageType(AppInfo.PAGE_TYPE_APK);
                        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU &&
                                TextUtils.equals(GlobalConfig.APP_PKGNAME_MEMORYGAME, pkgName)) {
                            appInfos.setAppLabel("Memory Game");
                        }
                        Message.obtain(mBrainActivityHandler, BRAINACTIVITY_HANDLER_MESSAGE_INSERT_PAGE, appInfos)
                                .sendToTarget();
                        // insertView(mAppInfos.size(), appInfos);
                        return;
                    } else {
                        continue;
                    }
                }
            }
        }
    }

    // /**
    // * 打开app
    // *
    // * @param packageName
    // * @param filePath
    // * 打开chart时需要文件路径 别的不需要
    // * @param pageName
    // * 页面名
    // */
    // private void openApp(String packageName, String filePath, String
    // pageName) {
    // LogMgr.e("open app::"+packageName);
    // try {
    // PackageInfo pi = getPackageManager().getPackageInfo(packageName, 0);
    // PackageManager pm = this.getApplicationContext()
    // .getPackageManager();
    //
    // Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
    // resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    // resolveIntent.setPackage(pi.packageName);
    //
    // List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);
    //
    // ResolveInfo ri = apps.iterator().next();
    // if (ri != null) {
    // String pkName = ri.activityInfo.packageName;
    // String className = ri.activityInfo.name;
    //
    // Intent intent = new Intent(Intent.ACTION_MAIN);
    // intent.addCategory(Intent.CATEGORY_LAUNCHER);
    // // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //
    // ComponentName cn = new ComponentName(pkName, className);
    //
    // if (!TextUtils.isEmpty(filePath)) {
    // intent.putExtra(
    // GlobalConfig.CHART_APP_FILE_PATH_EXTRA_NAME,
    // filePath);
    // }
    //
    // if (!TextUtils.isEmpty(pageName)) {
    // intent.putExtra(GlobalConfig.CHART_APP_PAGE_NAME_EXTRA,
    // pageName);
    // }
    //
    // intent.setComponent(cn);
    // if(GlobalConfig.CHART_AND_PROJECT_PROGRAM_APP_PACKAGE_NAME.equals(packageName)){
    // LogMgr.d("start explainer");
    // startActivityForResult(intent, REQUEST_CODE_FOR_VJC_AND_PROGRAM_JROJECT);
    // }else{
    // startActivity(intent);
    // }
    // }
    // } catch (NameNotFoundException e) {
    // LogMgr.e("getPackInfo failed for package " + packageName);
    // }
    // }

    /**
     * 注册广播,
     */
    private void registerBroadcastReceiver() {
        if (mBrainActivityBroadcastReceiver == null) {
            mBrainActivityBroadcastReceiver = new BrainActivityBroadcastReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalConfig.ACTION_ACTIVITY);
//        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        filter.addAction(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE);

//            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
//            filter.addDataScheme("package");
//            filter.setPriority(1000);

        // filter.addAction(GlobalConfig.INTENT_ACTION_PICTURE);
        // filter.addAction(GlobalConfig.INTENT_ACTION_RECORD);
        registerReceiver(mBrainActivityBroadcastReceiver, filter);

        if (installationBroadcastReceiver == null) {
            installationBroadcastReceiver = new InstallationBroadcastReceiver();
        }
        IntentFilter filter1 = new IntentFilter();
        // 安装
        filter1.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter1.addDataScheme("package");
        filter1.setPriority(1000);
        registerReceiver(installationBroadcastReceiver, filter1);
    }

    /**
     * 监听apk 安装卸载
     *
     * @author luox
     */
    class InstallationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogMgr.d("InstallationBroadcastReceiver onReceive intent.getAction() = " + intent.getAction());
            // 安装
            if (TextUtils.equals(Intent.ACTION_PACKAGE_ADDED, intent.getAction())) {
                // if (Utils.isEn()) {
                // BrainUtils.utilisToast("Successful installation",
                // mBrainActivity);
                // } else {
                // BrainUtils.utilisToast("安装成功", mBrainActivity);
                // }
                BrainUtils.utilisToast(getString(R.string.anzhuangchenggong), mBrainActivity);
                isUpdataPage(intent.getData().getSchemeSpecificPart());
            }
        }
    }

    /**
     * Installation apk 是否更新 页面
     *
     * @param apkPakName
     */
    private void isUpdataPage(final String apkPakName) {
        if (apkPakName == null) {
            return;
        }
        new Thread() {
            private boolean isUpdataPage = true;

            @Override
            public void run() {
                for (int i = 0; i < mAppInfos.size(); i++) {
                    AppInfo aif = mAppInfos.get(i);
                    if (aif.getPageType() == AppInfo.PAGE_TYPE_APK && TextUtils.equals(apkPakName, aif.getPkgName())) {
                        // 跳转至第i页
                        mBrainActivityHandler.obtainMessage(BRAINACTIVITY_HANDLER_MESSAGE_JUMP_TO_PAGE, i, 0)
                                .sendToTarget();
                        isUpdataPage = false;
                        break;
                    } else {
                        continue;
                    }
                }
                if (isUpdataPage) {
                    insertApkView(apkPakName);
                }
            }
        }.start();
    }


//
//    class ConnectBroadcastReceiver extends BroadcastReceiver {
//        public void onReceive(Context context, Intent intent) {
//            Log.d("wddadd","-----registerReceiver mConnectBroadcastReceiver----onReceive");
//
//            switch (intent.getAction()) {
//                case GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE:
//                    int flag = intent
//                            .getIntExtra(GlobalConfig.PAD_APP_CONNECT_STATE_CHANGE_PARAM, GlobalConfig.APP_DEFAULT);
//                    switch (flag) {
//                        case GlobalConfig.PAD_APP_DISCONNECT: // 断开连接
//                            // Log.e("test", "Activity 断开连接");
//                            // BrainUtils.utilisToast("Activity 断开连接 ", context);
//                            // isProgrammingUI = false;
//                            // // AppInfo aif = mAppInfos.get(0);
//                            // changeProgramStatus(false);
//                            // // delPadAppConnectView(0, true);
//                            // stopAnimaAndFunc();
//                            // stopVideo(false);
//                            // stopWindow();
//                            // stopRecordView();
//                            // // aif = null;
//                            break;
//                        // case GlobalConfig.TCP_CONNECT_SUCCESS:
//                        case GlobalConfig.KNOW_ROBOT_APP_FLAG:
//                        case GlobalConfig.PROGRAM_ROBOT_APP_FLAG:
//                        case GlobalConfig.ABILIX_CHART_APP_FLAG:
//                        case GlobalConfig.ABILIX_SCRATCH_APP_FLAG:
//                        case GlobalConfig.SKILL_PLAYER_APP_FLAG:
//                        case GlobalConfig.INNER_FILE_TRANSPORT_APP_STORE:
//                            // addPadAppConnectView(0, 0);// 插入编程页面
//                            break;
//                        case GlobalConfig.XICHEN_CONTROL_APP_FLAG:
//                        case GlobalConfig.TIQIU_APP_FLAG:
//                        case GlobalConfig.JIUYUAN_APP_FLAG:
//                            // addPadAppConnectView(0, 1);// 插入遥控页面
//                            // mViewPager.setCurrentItem(0, true);
//                            break;
//                        // 网络
//                    }
//                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
//                    Log.d("wddadd","-----NETWORK_STATE_CHANGED_ACTION--------999-------");
//                    refreshQrView();
//                    break;
//            }
//        }
//    }

    /**
     * 发送消息给Service 广播
     *
     * @author luox
     */
    class BrainActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogMgr.v("receive broadcast action::" + intent.getAction());
            switch (intent.getAction()) {

//                  case WifiManager.NETWORK_STATE_CHANGED_ACTION:
//                    Log.d("wddadd","-----NETWORK_STATE_CHANGED_ACTION--------999-------");
//                    updateQRcodeView();
//                    break;

//                // 安装新应用成功，消息来自系统
//                case Intent.ACTION_PACKAGE_ADDED:
//                    BrainUtils.utilisToast(getString(R.string.anzhuangchenggong), mBrainActivity);
//                    isUpdataPage(intent.getData().getSchemeSpecificPart());
//                    break;
                // 接收服务发送广播
                case GlobalConfig.ACTION_ACTIVITY:
                    int mode = intent.getIntExtra(GlobalConfig.ACTION_ACTIVITY_MODE, -1);
                    switch (mode) {
                        case GlobalConfig.FILE_DOWNLOAD:
                            // 文件下载成功 插入页面
                            String filePath = intent.getStringExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH);
                            LogMgr.i("下载成功  GlobalConfig.FILE_DOWNLOAD and path:" + filePath);
                            //Log.d("wddadd","下载成功  GlobalConfig.FILE_DOWNLOAD and path:" + filePath);
                            insertFileView(filePath);
                            BrainUtils.utilisToast(getString(R.string.xiazaichenggong), context);
                            break;
                        case GlobalConfig.FILE_DOWNLOAD_S:
                            String fullPath = intent.getStringExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH);
                            LogMgr.i("下载成功  GlobalConfig.FILE_DOWNLOAD_S and path:" + fullPath);
                            //Log.d("wddadd","下载成功  GlobalConfig.FILE_DOWNLOAD_S and path:" + fullPath);
                            BrainUtils.utilisToast(getString(R.string.xiazaichenggong), context);
                            break;
                        case GlobalConfig.FILE_DOWNLOAD_U:
                            LogMgr.i("GlobalConfig.FILE_DOWNLOAD_U");
                            BrainUtils.utilisToast(getString(R.string.xiazaishibai), context);
                            break;
                        case GlobalConfig.FILE_DOWNLOAD_UPDATE:
                            LogMgr.i("GlobalConfig.FILE_DOWNLOAD_UPDATE");
                            String filePath1 = intent.getStringExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH);
                            refreshView(filePath1);
                            BrainUtils.utilisToast(getString(R.string.xiazaichenggong), context);
                            break;
                        case GlobalConfig.DELETE_SKILL_OLD_FILE:
                            String fileName = intent.getStringExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH);
                            LogMgr.e("此log不应该出现 删除skillplayer文件 fileName:" + fileName);
                            // if (fileName != null) {
                            // String name = fileName.substring(0,
                            // fileName.lastIndexOf(File.separator));
                            // Message.obtain(mHandler, 0x04, name).sendToTarget();
                            // }
                            break;
                        case BrainData.DATA_VIEDO_OPEN:
                            openVideo();
                            break;
                        case BrainData.DATA_VIEDO_STOP:
                            stopVideo(true);
                            break;
                        case BrainData.DATA_WINDOW_OPEN:
                            openWindow();
                            break;
                        case BrainData.DATA_WINDOW_CLEAR:
                            clearWindow();
                            break;
                        case BrainData.DATA_WINDOW_STOP:
                            stopWindow();
                            break;
                        // 录音
                        case BrainData.START_RECORD_WINDOW:
                            isVjc_MRecord = false;
                            openRecordView();
                            break;
                        case BrainData.STOP_RECORD_WINDOW:
                            stopRecordView();
                            break;
                        case BrainData.START_RECORD:
                            isRecord = true;
                            startRecord(1, -1);
                            break;
                        case BrainData.STOP_RECORD:
                            stopRecord();
                            break;
                        case BrainData.START_RECORD_PLAY:
                            playRecord();
                            break;
                        case BrainData.OPEN_MEDIA:
                            String args = intent.getStringExtra("args");
                            playMusic(args);
                            break;
                        case BrainData.STOP_MEDIA:
                            stopMusic();
                            break;
                        // 录音 解析播放
                        case BrainData.START_RECORD_EXPLAIN:
                            stopTimer();
                            stopRecordView();
                            isRecord = false;
                            isVjc_MRecord = true;
                            byte[] da = intent.getByteArrayExtra("data");
                            int id = da.length > 5 ? (int) Utils.byte2float2(da, 3) : 1;
                            /** 如果是Scratch就录音完成,停止播放 */
                            if (da.length > 5) {
                                isScratchRecord = true;
                            }
                            int f = (int) Utils.byte2float2(da, da.length - 1);
                            // if (!isRecord_No) {
                            openRecordView();
                            /** 录音最大60s */
                            if (f > 60) {
                                f = 60;
                            }
                            int j = f * 1000;
                            startRecord(2, id);
                            // mHandler.sendEmptyMessageDelayed(0x08, j);
                            startTimer(j);
                            // }
                            // isRecord_No = false;
                            break;
                        // vjc
                        case BrainUtils.C_OPEN_CJV:
                            byte[] data = intent.getByteArrayExtra("data");
                            if (!mIsWantToCloseTextViewForVjc) {
                                setVJCViewOpen(data);
                            }
                            break;
                        case BrainUtils.C_OPEN_SCRATCH:
                            byte[] datas = intent.getByteArrayExtra("data");
                            LogMgr.e("datas:" + Arrays.toString(datas));
                            setVJCViewOpen(datas);
                            break;
                        case BrainUtils.C_STOP_CJV:
                            setVJCViewStop();
                            break;
                        // image
                        case BrainUtils.SCRATCH_VJC_IMAGEVIEW:
                            // if (!stop_paizhao) {
                            String file = intent.getStringExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH);
                            setImageView(file);
                            // stop_paizhao = false;
                            // }
                            break;
                        case BrainUtils.SCRATCH_VJC_IMAGEVIEW_STOP:
                            setStopImageView();
                            break;

                        case BrainUtils.PICTURE_DISPLAY:
                            String picPath = intent.getStringExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH);
                            setImg(picPath);
                            break;
                        //KU201 项目编程 显示电机图片
                        case BrainUtils.KU_OPEN_MOTOR_VIEW:
                            AppInfo motorInfo = new AppInfo();
                            motorInfo.setName(getString(R.string.robot_motor));
                            motorInfo.setPageType(AppInfo.PAGE_TYPE_MOTOR);
                            boolean pageIsInserted = false;
                            for (int i = 0; i < mAppInfos.size(); i++) {
                                if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_MOTOR) {
                                    pageIsInserted = true;
                                }
                            }
                            if (!pageIsInserted) {
                                insertPage(mAppInfos.size(), motorInfo);
                            }
                            break;

                        case BrainUtils.KU_CLOSE_MOTOR_VIEW:
                            for (int i = 0; i < mAppInfos.size(); i++) {
                                if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_MOTOR) {
                                    delPadAppConnectView(i, true);
                                }
                            }
                            break;

                        // 获取ip
//                        case GlobalConfig.GET_IP:
                        // mInetAddress = (InetAddress)
                        // intent.getSerializableExtra("ip");
//                            break;
//                        case GlobalConfig.UPGRADE_STOP_REST:
//                            if (mAnimatorSet != null) {
//                                mAnimatorSet.cancel();
//                                mAnimatorSet.end();
//                                mAnimatorSet = null;
//                            }
//                            // mCurrentRunIdx = -1;
//                            mCurrentAppInfo = null;
//                            break;
                        case GlobalConfig.FILE_DOWNLOAD_GROUP:
                            for (int i = 0; i < 3; i++) {
                                if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H) {
                                    byte[] color = new byte[14];//这里显示的额头和双眼的灯
                                    color[0] = 2;
                                    color[1] = 3;
                                    color[2] = 1;
                                    color[4] = (byte) 0xFF;
                                    color[6] = 2;
                                    color[8] = (byte) 0xFF;
                                    color[10] = 3;
                                    color[12] = (byte) 0xFF;
                                    BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                            ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0xC0, color), null, 0, 0);
                                } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                                    BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                                            ProtocolUtil.buildProtocol((byte) 0x03, (byte) 0xA3, (byte) 0x74, new byte[]{0, (byte) 0xFF, 0}), null, 0, 0);
                                }
                            }
                            LogMgr.i("FILE_DOWNLOAD_GROUP");
//                            mBrainActivityHandler.sendMessageDelayed(mBrainActivityHandler.obtainMessage
//                                    (374), 2000);
                            break;
                        case GlobalConfig.POWER_BUTTON_ONCLICK:
                            if (mCurrentAppInfo != null) {//这里对应的是休息界面停止，
//                                startAnimaAndFunc(mCurrentAppInfo.getViewHoder().imageView_qr, mCurrentAppInfo);
                                stopAnimaAndFunc(false);
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 停止播放
     */
    private void stopMusic() {
        if (mMusicPlayer == null) {
            mMusicPlayer.stop();
        }
    }

    /**
     * 播放mp3
     *
     * @param filePath
     */
    private void playMusic(String filePath) {
        if (mMusicPlayer == null) {
            mMusicPlayer = new MusicPlayer();
        }
        if (filePath != null && !filePath.endsWith(".mp3")) {
            filePath = filePath + ".mp3";
        }
        mMusicPlayer.play(filePath);
    }

    // /**
    // * 删除 skill-play 旧文件的页面
    // *
    // * @param str
    // */
    // private void deletePageView(String str) {
    // for (int i = 0; i < mAppInfos.size(); i++) {
    // AppInfo aif = mAppInfos.get(i);
    // if (aif != null && aif.getFileType() == 3 && aif.getName() != null) {
    // if (TextUtils.equals(aif.getName(), str)) {
    // mViewHoders.remove(i);
    // mAppInfos.remove(i);
    // initUI();
    // if (i < mViewHoders.size()) {
    // mViewPager.setCurrentItem(i, false);
    // } else {
    // mViewPager.setCurrentItem(0, false);
    // }
    // return;
    // }
    // } else {
    // continue;
    // }
    // }
    // }

    /**
     * 发送广播消息给BrainService
     */
    public void SendBroadCastToService(int mode, int modeState, String filePath) {
        Intent sendIntent = new Intent(GlobalConfig.ACTION_SERVICE);
        sendIntent.putExtra(GlobalConfig.ACTION_SERVICE_MODE, mode);
        sendIntent.putExtra(GlobalConfig.ACTION_SERVICE_MODE_STATE, modeState);
        if (filePath != null) {
            sendIntent.putExtra(GlobalConfig.ACTION_SERVICE_FILE_FULL_PATH, filePath);
        }
        sendBroadcast(sendIntent);
        LogMgr.d("SendBroadCastToService mode=" + mode + " modeState=" + modeState + " filePath=" + filePath);
    }

    /**
     * ViewHoder
     *
     * @author luox
     */
    public class ViewHoder {
        public View view;
        public TextView textview;
        public ImageView imageView;
        public ImageView imageView_qr;
        public ImageView m_qr_bg;
    }

    /**
     * 更新二维码页面
     */
    private void updateQRcodeView() {
        LogMgr.d("updateQRcodeView");
        for (AppInfo appInfo : mAppInfos) {
            if (appInfo.getPageType() == AppInfo.PAGE_TYPE_QR_CODE) {
                ViewHoder vh = appInfo.getViewHoder();
                // ImageView iv_qr = vh.iv_qr;
                String qr = WifiApAdmin.readSsid() + "," + WifiApAdmin.readPass()
                        + WifiUtils.getWiFiSSID(BrainActivity.this);
                if (qr.equals(mCurrentQrContent)) {
                    LogMgr.i("refreshQrView() 网络没有改变 不更新二维码");
                } else {
                    LogMgr.i("refreshQrView() 网络改变 更新二维码 qr content::" + qr);
                    mCurrentQrContent = qr;
                    Bitmap qrCodeBitmap;
                    try {
                        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H | GlobalConfig.BRAIN_TYPE ==
                                GlobalConfig.ROBOT_TYPE_H3) {
                            qrCodeBitmap = EncodingHandler.createQRCode(qr, 225, 1);
                        } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
                            qrCodeBitmap = EncodingHandler.createQRCode(qr, 260, 0);
                            vh.imageView_qr.setVisibility(View.VISIBLE);
                        } else {
                            qrCodeBitmap = EncodingHandler.createQRCode(qr, 320, 3);
                        }
                        vh.imageView_qr.setImageBitmap(qrCodeBitmap);
                        vh.imageView.setVisibility(View.VISIBLE);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    // vh.imageView = iv_qr;
                    appInfo.setViewHoder(vh);
//                    mViewHoders.set(state, vh);
                    if (basePagerAdapter != null) {
                        basePagerAdapter.notifyDataSetChanged();
                    }
                }
                break;
            }
        }
    }

    /**
     * 开启视频
     */
    private void openVideo() {
        stopVideo(false);
        LogMgr.i("openVideo()");
        ServerHeartBeatProcesser.getInstance().stopReceiveHeartBeatReplyTimer();
        mSurfaceView.setVisibility(View.GONE);
        LogMgr.e("jing---------type" + GlobalConfig.BRAIN_TYPE);
        switch (GlobalConfig.BRAIN_TYPE) {
            // c
            case GlobalConfig.ROBOT_TYPE_C:
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_S:
            case GlobalConfig.ROBOT_TYPE_H3:
            case GlobalConfig.ROBOT_TYPE_C9:
                LogMgr.i("openVideo 摄像头地址 = " + BrainService.getmBrainService().getClientIp());
                //    mUsbVideo = USBVideo.GetManger(BrainActivity.this, BrainService.getmBrainService().getClientIp
                // (), mHandler);
                //    mUsbVideo.startUSBVideo();
                VedioTransMgr.startUsbVedioTrans();
                break;
            // m 与 h
            case GlobalConfig.ROBOT_TYPE_M:
            case GlobalConfig.ROBOT_TYPE_H:
            case GlobalConfig.ROBOT_TYPE_F:
            case GlobalConfig.ROBOT_TYPE_AF:
            case GlobalConfig.ROBOT_TYPE_M1:
                VedioTransMgr.startVedioTrans();
                //mSendVideo = SendVideo.GetManger(mSurfaceView, BrainService.getmBrainService().getClientIp());
                // mSendVideo.startPreview();
                break;
            default:
                break;
        }
    }

    // /**
    // * 重新开启视频
    // */
    // private void resumeVideo() {
    // LogMgr.i("resumeVideo()");
    // switch (GlobalConfig.BRAIN_TYPE) {
    // // c
    // case 0x01:
    // case GlobalConfig.ROBOT_TYPE_S:
    // mUsbVideo = USBVideo.GetManger(BrainActivity.this, BrainService
    // .getmBrainService().getClientIp(), mHandler);
    // if (VideoBuffer.cameraRefresh)
    // mUsbVideo.startUSBVideo();
    // break;
    // // m 与 h
    // case 0x02:
    // case 0x03:
    // mSendVideo = SendVideo.GetManger(mSurfaceView, BrainService
    // .getmBrainService().getClientIp());
    // if (VideoBuffer.cameraRefresh)
    // mSendVideo.startPreview();
    // break;
    // }
    // }

    /**
     * 关闭视频
     *
     * @param isNeedToStartReceiveHeartBeatReplyTimer 这次关闭视频是否需要打开检测心跳回复Timer
     */
    private void stopVideo(boolean isNeedToStartReceiveHeartBeatReplyTimer) {
        LogMgr.i("stopVideo()");
        mSurfaceView.setVisibility(View.GONE);
        if (mSendVideo != null) {
            mSendVideo.stopPreview();
            mSendVideo = null;
        }
        VedioTransMgr.stopVedioTrans();

        RTSPServiceMgr.stopRTSPService();
/*        if (mUsbVideo != null) {
            mUsbVideo.stopUSBVideo();
            mUsbVideo = null;
        }*/
        if (GlobalConfig.isReceiveHeartBeatReply && isNeedToStartReceiveHeartBeatReplyTimer
                && ServerHeartBeatProcesser.getInstance().isNeedToReceiveHeartbeatReply()) {
            ServerHeartBeatProcesser.getInstance().startReceiveHeartBeatReplyTimer();
        }
    }

    /**
     * 开始显示窗
     */
    private void openWindow() {
        stopWindow();
        mPathView.setVisibility(View.VISIBLE);
        if (mSendPathView == null) {
            mSendPathView = new SendPathView(mPathView, BrainService.getmBrainService().getClientIp());
            mSendPathView.openPathView();
        }
    }

    /**
     * 清空显示窗
     */
    private void clearWindow() {
        if (mSendPathView != null) {
            mSendPathView.clearPathView();
        }
    }

    /**
     * 关闭显示窗
     */
    private void stopWindow() {
        LogMgr.d("stopWindow()");

        mPathView.setVisibility(View.GONE);
        if (mSendPathView != null) {
            mSendPathView.stopPathView();
            mSendPathView = null;
        }
    }

    /**
     * 打开录音界面
     */
    private void openRecordView() {
        mRecordRelativeLayout.bringToFront();
//		mRecordRelativeLayout.setText(getString(R.string.record_page));
        stopRecordAnimator();
        mRecordImageViewOutside.setImageResource(BrainUtils.getRecordInitDrawableResource());
        mRecordImageViewInside.setVisibility(View.GONE);
        mRecordRelativeLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 停止录音动画
     */
    private void stopRecordAnimator() {
        if (mRotateAnimator != null) {
            mRotateAnimator.cancel();
            mRotateAnimator.end();
            mRotateAnimator = null;
        }
    }

    /**
     * 开始录音动画
     *
     * @param view
     */
    private void startRecordAnimator(View view) {
        stopRecordAnimator();

        mRotateAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        mRotateAnimator.setRepeatCount(-1);
        mRotateAnimator.setInterpolator(new LinearInterpolator());
        mRotateAnimator.setDuration(900);
        mRotateAnimator.start();
    }

    /**
     * 关闭录音界面
     */
    private void stopRecordView() {
        stopRecordAnimator();
        mRecordRelativeLayout.setVisibility(View.GONE);
        if (mBrainRecord != null) {
            mBrainRecord.destory();
            mBrainRecord = null;
        }
        mYRecord = null;
    }

    /**
     * 开始录音
     */
    private void startRecord(int mode, int id) {
        LogMgr.i("startRecord() mode = " + mode + " id = " + id);
//		mRecordRelativeLayout.setText(getString(R.string.ls_record));
        mRecordImageViewOutside.setImageResource(BrainUtils.getRecordLightRingDrawableResource());
        mRecordImageViewInside.setImageResource(R.drawable.record_recording_inside);
        startRecordAnimator(mRecordImageViewOutside);
        mRecordImageViewInside.setVisibility(View.VISIBLE);
        if (mBrainRecord != null) {
            mBrainRecord.destory();
            mBrainRecord = null;
            mYRecord = null;
        }
        mBrainRecord = new BrainRecord(mode, id);
        mYRecord = new MyRecord();
        LogMgr.d("startRecord", "BrianActivity-startRecord");
        mBrainRecord.startRecord(mYRecord, 2000);
    }

    /**
     * 停止录音
     */
    private void stopRecord() {
        LogMgr.i("stopRecord()");
        if (mBrainRecord != null) {
            mBrainRecord.stopRecord();
        }
    }

    /**
     * 录音完成
     */
    private void completeRecord() {
//		mRecordRelativeLayout.setText(getString(R.string.record_success));
        stopRecordAnimator();
        mRecordImageViewOutside.setImageResource(BrainUtils.getRecordCompleteDrawableResource());
        mRecordImageViewInside.setVisibility(View.GONE);
        if (isRecord) {
            SendBroadCastToService(BrainData.RECORD_SUCCESS, GlobalConfig.APP_DEFAULT, null);
        } else {
            /** Scratch录音完成不播放,直接停止 **/
            if (isScratchRecord) {
                isScratchRecord = false;
                stopRecordView();
            } else {
                playRecord();
            }
            // if (!isAddRecord) {
            // isAddRecord = true;
            // AppInfo record = new AppInfo();
            // record.setName(getString(R.string.luyin));
            // record.setPageType(Mode.PAGE_TYPE_RECORD);
            // // record.setPathName(BrainRecord.RECORD_PATH_);
            // Message.obtain(mHandler, BRAIN_ACTIVITY_HANDLER_INSERT_PAGE,
            // record).sendToTarget();
            // }
        }
    }

    /**
     * 播放录音
     */
    private void playRecord() {
        if (mBrainRecord == null || mYRecord == null) {
            LogMgr.e("录音模块未初始化！");
            return;
        }
//		mRecordRelativeLayout.setText(getString(R.string.record_play));
        mRecordImageViewOutside.setImageResource(BrainUtils.getRecordLightRingDrawableResource());
        mRecordImageViewInside.setImageResource(R.drawable.record_playing_inside);
        startRecordAnimator(mRecordImageViewOutside);
        mRecordImageViewInside.setVisibility(View.VISIBLE);
        mBrainRecord.playMedia(mYRecord);
    }

    /**
     * 播放录音完成
     */
    private void playcompleteRecord() {
//		mRecordRelativeLayout.setText(getString(R.string.record_play_success));
        stopRecordAnimator();
        mRecordImageViewOutside.setImageResource(BrainUtils.getRecordCompleteDrawableResource());
        mRecordImageViewInside.setVisibility(View.GONE);
        if (isRecord) {
            SendBroadCastToService(BrainData.RECORD_SUCCESS_PLAY, GlobalConfig.APP_DEFAULT, null);
        } else {
            stopRecordView();
        }
    }

    /**
     * 录音功能控制类
     *
     * @author luox
     */
    private class MyRecord implements IPlayStateListener, IRecordStateListener {

        @Override
        public void onStartRecord() {
        }

        @Override
        public void onStopRecord() {
            completeRecord();
        }

        @Override
        public void onFinished() {
            playcompleteRecord();
        }

    }

    /*
     * MyPageChangeListener
     *
     * @author wensl 页面切换时 的一些操作
     */
    // class BrainPageChangeListener implements OnPageChangeListener {
    //
    // @Override
    // public void onPageScrolled(int position, float positionOffset,
    // int positionOffsetPixels) {
    // }
    //
    // @Override
    // public void onPageSelected(int position) {
    // // mQRPageClickNum = 0;
    // LogMgr.e(TAG, " onPageSelected position:" + position + " yuan:"
    // + yuan);
    // if (position == 2) {
    // BrainUtils.setBrightness(mBrainActivity, 255);
    // } else {
    // BrainUtils.setBrightness(mBrainActivity, 100);
    // }
    // }
    //
    // @Override
    // public void onPageScrollStateChanged(int state) {
    // }
    // }

    /**
     * 显示VJC string数据
     *
     * @param data
     */
    private void setVJCViewOpen(byte[] data) {
        LogMgr.d("setVJCViewOpen()");

        mVjcRelativeLayout.bringToFront();
        mTextViewForVjc.setText("");
        mTextViewForVjc.setText(new String(data));
        mTextViewForVjc.setVisibility(View.VISIBLE);
        mVjcRelativeLayout.setVisibility(View.VISIBLE);
        // String str = null;
        // try {
        // DecimalFormat df = new DecimalFormat("#.000");
        // double f = Double.parseDouble(new String(data));
        // str = df.format(f);
        // } catch (Exception e) {
        // str = null;
        // e.printStackTrace();
        // }
        // if (str == null) {
        // mTextForVjc.bringToFront();
        // mTextForVjc.setText("");
        // mTextForVjc.setText(new String(data));
        // mTextForVjc.setVisibility(View.VISIBLE);
        // } else {
        // mTextForVjc.bringToFront();
        // mTextForVjc.setText("");
        // mTextForVjc.setText(str);
        // mTextForVjc.setVisibility(View.VISIBLE);
        // }
    }

    /**
     * 关闭VJC string数据显示
     */
    private void setVJCViewStop() {
        LogMgr.d("setVJCViewStop()");
        mTextViewForVjc.setText("");
        mVjcRelativeLayout.setVisibility(View.GONE);
    }

    /**
     * 显示纯色背景
     */
    private void setImageViewColor(@ColorRes int Color) {
        LogMgr.d("setImageViewColor Color = " + Color);
        mImageView.bringToFront();
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setBackgroundColor(getResources().getColor(Color));
    }

    /**
     * 显示制定路径的图片 并添加照片页面
     *
     * @param filename
     */
    private void setImageView(String filename) {
        LogMgr.e("filename:" + filename);
        mImageView.bringToFront();
        mImageView.setVisibility(View.VISIBLE);
        if (filename.endsWith(".jpg")) {
            try {
                FileInputStream fis = new FileInputStream(filename);
                if (fis.available() > 0) {
                    mBitmap = BitmapFactory.decodeStream(fis);
                    if (mBitmap != null) {
                        mImageView.setImageBitmap(mBitmap);
                        /** 拍照成功告诉 Scratch停止阻塞 */
                        BrainService.getmBrainService().setControlData(4, GlobalConfig
                                .ACTION_SERVICE_MODE_SCRATCH_FILE);
                        // mHandler.sendEmptyMessageDelayed(0x05, 5000);
                        // 添加照片页面
                        // if (!isAddImage) {
                        // isAddImage = true;
                        // AppInfo image = new AppInfo();
                        // image.setName(getString(R.string.zhaopian));
                        // image.setPageType(Mode.PAGE_TYPE_IMAGE);
                        // // image.setPathName(filename);
                        // Message.obtain(mHandler,
                        // BRAIN_ACTIVITY_HANDLER_INSERT_PAGE, image)
                        // .sendToTarget();
                        // }
                    }
                }
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (filename.endsWith(".gif")) {
            //Glide.with(this).load(R.drawable.11).into(mImageView);
            Glide.with(this).load(filename).into(mImageView);
        }

    }

    /**
     * 关闭照片页面的显示
     */
    private void setStopImageView() {
        try {
            mImageView.setVisibility(View.GONE);
            mImageView.setImageBitmap(null);
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
            mBitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // private MediaPlayer mPlayer = null;
    private TimerTask mTimerTask, shutdownTask;
    private Timer mTimer, shutdownTimer;

    /**
     * 播放录音
     */
    // private void MyplayRecord() {
    // if (mPlayer != null) {
    // MystopPlayRecord();
    // }
    // mPlayer = new MediaPlayer();
    // try {
    // mPlayer.setDataSource(BrainRecord.RECORD_SCRATCH_VJC_);
    // mPlayer.prepare();
    // mPlayer.start();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    //
    // private void MystopPlayRecord() {
    // if (mPlayer != null) {
    // mPlayer.stop();
    // mPlayer.reset();
    // mPlayer.release();
    // mPlayer = null;
    // }
    // }

    /**
     * 初次启动 播放音频
     */
    private void playWelcomeVoice() {
        mMusicPlayer = new MusicPlayer();

        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
            // 中文音频文件写到sdcard
            FileUtils.saveSoundFileToSdCard(Application.getInstance(), FileUtils.AUDIOPATH, Application.getInstance()
                    .getPathName() + "/"
                    + "oculus2_176.mp3");
            // 英文音频文件写到sdcard
            FileUtils.saveSoundFileToSdCard(Application.getInstance(), FileUtils.AUDIOPATH, Application.getInstance()
                    .getPathName() + "/"
                    + "oculus2_176.mp3");
            mMusicPlayer.play("oculus2_176.mp3", new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mBrainActivityHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 绑定FZXX的服务
                            mMyGiftView.setEnabled(true);
                            startServer();
                            return;
                        }
                    });
                }
            });

        } else {
            // 中文音频文件写到sdcard
            FileUtils.saveSoundFileToSdCard(Application.getInstance(), FileUtils.AUDIOPATH, Application.getInstance()
                    .getPathName() + "/"
                    + "changeddefb.mp3");
            // 英文音频文件写到sdcard
            FileUtils.saveSoundFileToSdCard(Application.getInstance(), FileUtils.AUDIOPATH, Application.getInstance()
                    .getPathName() + "/"
                    + "en_changeddefb.mp3");
            mMusicPlayer.play("changeddefb.mp3", new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mBrainActivityHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
                                return;
                            }
                            LogMgr.e("5");
                            startAnimaAndFunc(mAppInfos.get(0).getViewHoder().imageView_qr, mAppInfos.get(0));
                        }
                    });
                }
            });
        }


    }

    /**
     * 录音
     */
    public void startTimer(int time) {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    BrainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mBrainRecord != null) {
                                    mBrainRecord.stopRecord();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };
            mTimer.schedule(mTimerTask, time);
        }
    }

    /**
     * 关闭更新wifi定时器
     */
    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }


    /**
     * 删除apk
     *
     * @param packageName
     */
    public void deleteAPK(final String packageName) {
        LogMgr.i("开始卸载应用 packageName = " + packageName);
        FileDownloadProcesser.initDeleteAndInstallServiceCount++;
        try {
            if (mDeleteService != null) {
                mDeleteService.deleteApk(packageName);
            } else {
                if (FileDownloadProcesser.initDeleteAndInstallServiceCount > 5) {
                    LogMgr.e("卸载服务初始化5次失败，卸载失败");
                    FileDownloadProcesser.initDeleteAndInstallServiceCount = 0;
                    return;
                }
                LogMgr.e("卸载服务尚未初始化，初始化后卸载");
                BrainActivity.getmBrainActivity().initDeleteAndInstallService();
                mBrainActivityHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deleteAPK(packageName);
                    }
                }, 1000);

            }
        } catch (Exception e) {
            LogMgr.e("卸载应用异常");
            e.printStackTrace();
        }
    }

    /**
     * 获取第一个页面在无限循环中的位置
     */
    private int getMiddlePlaceForFirstItem() {
        // return Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 %
        // mAppInfos.size());
        return GlobalConfig.MAX_PAGE_INDEX / 2 - (GlobalConfig.MAX_PAGE_INDEX / 2 % mAppInfos.size());
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    /**
     * 开始执行自动定时关机
     */
    private void startShutdown(int delay) {
        if (GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_H) {
            LogMgr.e("不是H56不执行自动关机的功能");
            return;
        }
        File file = new File(SHUTDOWN);
        if (!file.exists()) {
            FileUtils.saveStringToFile("" + 3, SHUTDOWN);
        }
        long time = getTime() * 60 * 1000 + System.currentTimeMillis();
        startShutdownTimer(delay, time);
    }

    /**
     * 发送定时关机广播
     */
    private void sendShutdownOpenBroadcast() {
        Intent intent = new Intent();
        intent.setAction("action.timing.shutdown");
        sendBroadcast(intent);
    }

    /**
     * 启动自动关机定时器
     */
    private void startShutdownTimer(int delay, final long shutdownTime) {
        stopShutdownTiemr();
        shutdownTimer = new Timer();
        shutdownTask = new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() > shutdownTime) {
                    sendShutdownOpenBroadcast();
                }
            }
        };
        shutdownTimer.schedule(shutdownTask, delay, 5 * 1000);
    }

    /**
     * 关闭定时器
     */
    private void stopShutdownTiemr() {
        if (shutdownTimer != null) {
            shutdownTimer.cancel();
        }
        if (shutdownTask != null) {
            shutdownTask.cancel();
        }
    }


    /**
     * 获取时间
     *
     * @return
     */
    private int getTime() {
        int time = 3;
        String s = FileUtils.readFile(SHUTDOWN).trim();
        if (s != null && !s.equals("")) {
            time = Integer.parseInt(s);
        }
        return time;
    }

    /**
     * 解析Scheme数据
     */
    private void analysisScheme(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            LogMgr.d("uri:" + uri);
            if (uri == null) {
                return;
            }
            String type = uri.getQueryParameter("type");
            if (type.equals("qr")) {
                int pageType_Qr = getPagePosition(AppInfo.PAGE_TYPE_QR_CODE);
                mBrainActivityHandler.obtainMessage(BRAINACTIVITY_HANDLER_MESSAGE_JUMP_TO_PAGE, pageType_Qr, 0).sendToTarget();
            } else if (type.equals("program")) {
                LogMgr.d("跳转到编程界面:");
                int pageType_Qr = getPagePosition(AppInfo.PAGE_TYPE_PROGRAM);
                mBrainActivityHandler.obtainMessage(BRAINACTIVITY_HANDLER_MESSAGE_JUMP_TO_PAGE, 0, 0).sendToTarget();
                startAnimaAndFunc(mAppInfos.get(0).getViewHoder().imageView_qr, mAppInfos.get(0));
            }
        }
    }

    /**
     * @param pageType AppInfo.PAGE_TYPE_QR_CODE
     * @return index
     */
    private int getPagePosition(int pageType) {
        try {
            for (int i = 0; i < mAppInfos.size(); i++) {
                if (mAppInfos.get(i).getPageType() == pageType) {
                    return i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public MyGiftView getMyGiftView() {
        return mMyGiftView;
    }

    //添加调节屏幕亮度的问题；休息状态屏幕最暗 0 ，正常最亮 2
    private void setScreenBrightness(int process) {

        //设置当前窗口的亮度值.这种方法需要权限android.permission.WRITE_EXTERNAL_STORAGE
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        float f = process / 255.0F;
        localLayoutParams.screenBrightness = f;
        getWindow().setAttributes(localLayoutParams);
        //修改系统的亮度值,以至于退出应用程序亮度保持
        saveBrightness(getContentResolver(), process);
    }

    public static void saveBrightness(ContentResolver resolver, int brightness) {
        //改变系统的亮度值(申请权限失败)
        //这里需要权限android.permission.WRITE_SETTINGS
        //设置为手动调节模式
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        //保存到系统中
        Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(uri, null);
    }
}
