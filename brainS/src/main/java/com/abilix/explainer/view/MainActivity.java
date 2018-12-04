package com.abilix.explainer.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abilix.brain.Application;
import com.abilix.brain.R;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerInitiator;
import com.abilix.explainer.MySensor;
import com.abilix.explainer.model.ControlReceiveBroadcastReceiver;
import com.abilix.explainer.present.IMainActivityPresent;
import com.abilix.explainer.present.MainActivityPresent;
import com.abilix.explainer.pushmsg.PushMsgTracker;
import com.abilix.explainer.utils.CommonUtils;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.bumptech.glide.Glide;

import java.io.FileInputStream;

public class MainActivity extends Activity implements IMainActivity,
        OnClickListener, OnLongClickListener {
    public static final String INTENT_ACTION_PICTURE = "com.abilix.explainer.mainactivity.picture";
    public static final String INTENT_ACTION_RECORD = "com.abilix.explainer.mainactivity.record";
    public static final String INTENT_ACTION_TCP_DISCONNECT = "com.abilix.tcp.disconnect";
    private static final String INTENT_EXTRA_BOOLEAN_HAS_DELETED_PAGE = "intent_extra_boolean_has_deleted_page";
    public static final String INTENT_ACTION_CUSTOM_LOG = "com.abilix.change_log_status";
    public static final String CUSTOM_LOG_STATUS_KEY = "log_status";
    private static final int REQUEST_CODE_FOR_VJC_AND_PROGRAM_JROJECT = 0x80;
    private IMainActivityPresent mMainActivityPresent;

    private ExplainerAlertDialogs mExplainerAlertDialogs;
    private ControlReceiveBroadcastReceiver mBroadcastReceiver;

    protected int activityCloseEnterAnimation;

    protected int activityCloseExitAnimation;

    private ImageView iv;
    private RelativeLayout rl_record;
    private ImageView iv_record_animation;
    private ImageView iv_record;
    private ImageView iv_picture;
    private TextView tv;
    private TextView tv_display;
    private AnimatorSet mAnimatorSet;
    private static MainActivity instance;
    private static final String ELF_EXCUTE="elf_excute";
    private String programName;
    private ObjectAnimator mRecordAnimator;
    private Handler mHandler = new Handler();
    public static MainActivity getActivity() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowAttributes();
        setContentView(R.layout.activity_explainer);
        setTyle();
        instance = this;
        //ExplainerApplication.instance.addActivity(this);
        ExplainerInitiator.init();
        MySensor.obtainMySensor(Application.getInstance()).openSensorEventListener();
        iv = (ImageView) findViewById(R.id.iv);
        rl_record = (RelativeLayout) findViewById(R.id.rl_record);
        rl_record.setOnLongClickListener(this);
        iv_record_animation = (ImageView) findViewById(R.id.iv_record_animation);
        iv_record_animation.setImageResource(CommonUtils.getRecordInitDrawableResource());
        iv_record = (ImageView) findViewById(R.id.iv_record);
        iv_record.setImageResource(R.drawable.record_recording_inside);
        iv_picture = (ImageView) findViewById(R.id.iv_picture);
        iv_picture.setOnLongClickListener(this);
        tv = (TextView) findViewById(R.id.tv_program_name);
        tv_display = (TextView) findViewById(R.id.tv_display);
        tv_display.setBackgroundResource(CommonUtils.getVJCTextViewBackgroundDrawableResource());
        tv_display.setOnLongClickListener(this);
        // iv.setBackgroundResource(CommonUtils.getAppBg());
        iv.setImageResource(CommonUtils.getAppBg());
        iv.setOnClickListener(this);
        iv.setOnLongClickListener(this);
        //启动时禁用点击事件，延时1秒后恢复
        iv.setClickable(false);
        iv.setLongClickable(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                iv.setClickable(true);
                iv.setLongClickable(true);
            }
        }, 1500);
        registerBroadcastReceiver();
        startAnimation();
        //	setResult(REQUEST_CODE_FOR_VJC_AND_PROGRAM_JROJECT);
        //	finish();
        mMainActivityPresent = new MainActivityPresent(this);
        mExplainerAlertDialogs = new ExplainerAlertDialogs(mMainActivityPresent);
        Intent i = getIntent();
        if (i != null) {
            mMainActivityPresent.disposeFilePath(i);

        }
    }

    private void setWindowAttributes() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Application.getInstance().setOrientation(this);
    }

    private void setTyle() {
        TypedArray activityStyle = getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.windowAnimationStyle});

        int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);

        activityStyle.recycle();

        activityStyle = getTheme().obtainStyledAttributes(
                windowAnimationStyleResId,
                new int[]{android.R.attr.activityCloseEnterAnimation,
                        android.R.attr.activityCloseExitAnimation});

        activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);

        activityCloseExitAnimation = activityStyle.getResourceId(1, 0);

        activityStyle.recycle();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(activityCloseEnterAnimation,
                activityCloseExitAnimation);
        mMainActivityPresent.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //MySensor.obtainMySensor(Application.getInstance()).unRegistSensor();
        mMainActivityPresent.destroy();
        // LogMgr.e("关闭串口");
        // SerialPortCommunicator.getInstance().destory();
        LogMgr.e("MainActivity onDestroy()");
    }

    @Override
    public void finish() {
        super.finish();
        LogMgr.e("MainActivity finish()");
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
//        //SPUtils.destory();
//        mBroadcastReceiver = null;
//        PushMsgTracker.getInstance().destory();
//        //Process.killProcess(Process.myPid());
//        ExplainTracker.getInstance().destroy();
        try {
            ExplainTracker.getInstance().destroy();
        } catch (Exception e) {
//            Log.d(TAG, "finish: ExplainTracker.getInstance().destroy 错误");
            e.printStackTrace();
        }
        try {
            PushMsgTracker.getInstance().destory();
        } catch (Exception e) {
//            Log.d(TAG, "finish:PushMsgTracker.getInstance().destory 错误");
            e.printStackTrace();
        }
    }

    @Override
    public void showAlertDialog(int which, String message) {
        LogMgr.d("showAlertDialog: which: " + which + " message: " + message);
        mExplainerAlertDialogs.showAlertDialog(this, which, message);
    }

    @Override
    public void dismissAlertDialog(int which) {
        mExplainerAlertDialogs.dismissAlertDialog(which);
    }

    @Override
    public void startAnimation() {
        stopAnimation();
        LogMgr.d("开始动画");
        ObjectAnimator rotate = ObjectAnimator
                .ofFloat(iv, "rotation", 0f, 360f);
        rotate.setRepeatCount(-1);
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.setInterpolator(new LinearInterpolator());
        mAnimatorSet.play(rotate);
        mAnimatorSet.setDuration(8000);
        mAnimatorSet.start();
    }

    @Override
    public void stopAnimation() {
        LogMgr.d("停止动画");
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet.end();
            mAnimatorSet = null;
        }
    }

    /**
     * 停止录音动画
     */
    private void stopRecordAnimator() {
        if (mRecordAnimator != null) {
            mRecordAnimator.cancel();
            mRecordAnimator.end();
            mRecordAnimator = null;
        }
    }

    /**
     * 开始录音动画
     *
     * @param view
     */
    private void startRecordAnimator(View view) {
        stopRecordAnimator();

        mRecordAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        mRecordAnimator.setRepeatCount(-1);
        mRecordAnimator.setInterpolator(new LinearInterpolator());
        mRecordAnimator.setDuration(900);
        mRecordAnimator.start();
    }

    private void restAllView() {
        //iv.setVisibility(View.INVISIBLE);
        rl_record.setVisibility(View.INVISIBLE);
        //tv.setVisibility(View.INVISIBLE);
        //iv_picture.setVisibility(View.INVISIBLE);
        setImageViewStop();
        tv_display.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        mMainActivityPresent.disposeOnClickEvent(v);
    }

    @Override
    public void showProgram(String programName) {
        LogMgr.d("====showProgram: " + programName);
        restAllView();

        tv.setVisibility(View.VISIBLE);
        tv.setTextColor(Color.WHITE);
        this.programName = programName;
        tv.setText(programName);
        /*tv.setTextSize(getResources().getDimension(
                R.dimen.txt_size_programe_name));*/
        iv.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgram() {
        LogMgr.d("====dismissProgram");
        iv.setVisibility(View.INVISIBLE);
        tv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void display(String content) {
        LogMgr.d("====display: " + content);
        restAllView();

        String displaycontent = content;
        tv_display.setText(displaycontent);
        tv_display.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishDisplay() {
        LogMgr.d("====finishDisplay");
        tv_display.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showRecordView() {
        LogMgr.d("====showRecordView");
        restAllView();
        rl_record.setVisibility(View.VISIBLE);
        iv_record.setImageResource(R.drawable.record_recording_inside);
        iv_record_animation.setImageResource(CommonUtils.getRecordLightRingDrawableResource());
        startRecordAnimator(iv_record_animation);
        /*tv.setVisibility(View.VISIBLE);
        tv.setTextColor(Color.RED);
        tv.setTextSize(getResources().getDimension(R.dimen.txt_size_record));
        tv.setText(getResources().getText(R.string.ls_record));*/
    }

    @Override
    public void showPlayRecordView() {
        LogMgr.d("====showPlayRecordView");
        restAllView();

        stopRecordAnimator();
        rl_record.setVisibility(View.VISIBLE);
        iv_record.setImageResource(R.drawable.record_playing_inside);
        iv_record_animation.setImageResource(CommonUtils.getRecordLightRingDrawableResource());
        startRecordAnimator(iv_record_animation);
        /*tv.setVisibility(View.VISIBLE);
        tv.setTextColor(Color.RED);
        tv.setText(getResources().getText(R.string.record_play));*/
    }

    @Override
    public void dimissRecordView() {
        LogMgr.d("====dimissRecordView");
        rl_record.setVisibility(View.INVISIBLE);
        tv.setVisibility(View.INVISIBLE);

        showProgram(programName);
    }

    /**
     * 用于在mImageView中显示
     */
    private Bitmap mBitmap = null;

    /**
     * 显示制定路径的图片 并添加照片页面
     *
     * @param filename
     */
    private void setImageView(ImageView mImageView, final String filename) {
        try {
            mImageView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(filename) && filename.endsWith(FileUtils._TYPE_GIF)) {
                Glide.with(this).load(filename).into(mImageView);
            } else {
                FileInputStream fis = new FileInputStream(filename);
                if (fis.available() > 0) {
                    mBitmap = BitmapFactory.decodeStream(fis);
                    if (mBitmap != null) {
                        mImageView.setImageBitmap(mBitmap);
                    }
                }
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭照片页面的显示
     */
    private void setImageViewStop() {
        try {
            iv_picture.setVisibility(View.INVISIBLE);
            if (mBitmap != null && !mBitmap.isRecycled()) {
                iv_picture.setImageBitmap(null);
                mBitmap.recycle();
            }
            mBitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showPicture(String picturePath) {
        LogMgr.d("====showPicture: " + picturePath);
        restAllView();

        setImageView(iv_picture, picturePath);
        iv_picture.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissPicture() {
        LogMgr.d("====dismissPicture");
        //iv_picture.setVisibility(View.INVISIBLE);
        setImageViewStop();
    }

    @Override
    public boolean onLongClick(View v) {
        mMainActivityPresent.disposeOnLongClickEvent(v);
        return true;
    }

    @Override
    public void sendBroadCastToBrain(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    @Override
    public void responseResult() {
        LogMgr.d("返回 result");
        Intent i = new Intent();
        i.putExtra(INTENT_EXTRA_BOOLEAN_HAS_DELETED_PAGE, true);
        setResult(Activity.RESULT_OK, i);
    }

    private void registerBroadcastReceiver() {
        mBroadcastReceiver = new ControlReceiveBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(INTENT_ACTION_TCP_DISCONNECT);
        LogMgr.d("register charge state change broadcast");
        Intent intent = registerReceiver(mBroadcastReceiver, filter);
        /*if (intent != null && intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            LogMgr.d(String.format(Locale.US, "battery state:: battery status[%d] plugged[%d]", status, plugged));
            if (status == BatteryManager.BATTERY_STATUS_CHARGING
                    || plugged == BatteryManager.BATTERY_PLUGGED_AC
                    || plugged == BatteryManager.BATTERY_PLUGGED_USB
                    || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
                SP.chargeProtect(true);
            }
        }*/
    }
}
