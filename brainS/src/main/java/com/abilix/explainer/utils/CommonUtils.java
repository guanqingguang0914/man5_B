package com.abilix.explainer.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abilix.explainer.ControlInfo;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.ExplainerInitiator;
import com.abilix.brain.R;


public class CommonUtils {

    public static final String ABILIX_PROJECT_PROGRAM = "AbilixProjectProgram";
    public static final String ABILIX_CHART = "AbilixChart";
    public static final String ABILIX_APP_STORE = "app_store";
    public static final String ABILIX_SCRATCH = "AbilixScratch";
    public static final String ABILIX_SKILLPLAYER = "Abilix_Skillplayer";
    public static final String ABILIX_SKILL_CREATOR = "Abilix_Skillcreator";

    public static final int FILE_TYPE_UNKNOWN = -1;
    public static final int FILE_TYPE_PROJECT_PROGRAM = 0;
    public static final int FILE_TYPE_CHART = 1;
    public static final int FILE_TYPE_SCRATCH = 2;
    public static final int FILE_TYPE_SKILLPLAYER = 3;

    private static int explainFileType = FILE_TYPE_UNKNOWN;

    public static void setFileType(int fileType) {
        explainFileType = fileType;
    }

    public static int getFileType() {
        return explainFileType;
    }

    /*
     * 判断当前语言环境是否是英语
     */
    public static boolean isEn() {
        Locale locale = ExplainerApplication.instance.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language != null) {
            if (language.endsWith("en")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * 获取现在时间
     *
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
     */
    public static String getNowDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
//		ParsePosition pos = new ParsePosition(8);
//		Date currentTime_2 = formatter.parse(dateString, pos);
//		return currentTime_2;
    }


    /**
     * 获取现在时间
     *
     * @return 返回时间类型 MM_dd_HH_mm
     */
    public static String getNowDate1() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM_dd_HH_mm");
        String dateString = formatter.format(currentTime);
        return dateString;
//		ParsePosition pos = new ParsePosition(8);
//		Date currentTime_2 = formatter.parse(dateString, pos);
//		return currentTime_2;
    }

    /**
     * 将毫秒转化成固定格式的时间
     * 时间格式: yyyy-MM-dd HH:mm:ss
     *
     * @param millisecond
     * @return
     */
    public static String getDateTimeFromMillisecond(Long millisecond) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    /**
     * 将毫秒转化成固定格式的时间
     * 时间格式: yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param millisecond
     * @return
     */
    public static String getDateTimeFromMillisecond2(Long millisecond) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    /**
     * 获取 SDCard 总容量大小
     *
     * @return
     */
    public static long getTotalSize() {
        String sdcard = Environment.getExternalStorageState();
        String state = Environment.MEDIA_MOUNTED;
        if (sdcard.equals(state)) {
            File file = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(file.getPath());
            //获得sdcard上 block的总数
            long blockCount = statFs.getBlockCountLong();
            //获得sdcard上每个block 的大小
            long blockSize = statFs.getBlockSizeLong();
            //计算标准大小使用：1024，当然使用1000也可以
            long blockTotalSize = blockCount * blockSize / 1024 / 1024;
            LogMgr.i("getTotalSize() 总容量大小 blockTotalSize = " + blockTotalSize + " MB");
            return blockTotalSize;
        } else {
            LogMgr.e("getTotalSize() 存储状态异常 sdcard = " + sdcard + " state = " + state);
            return -1;
        }
    }

    /**
     * 获取 SDCard 剩余容量大小
     *
     * @return
     */
    public static long getAvailableSize() {
        String sdcard = Environment.getExternalStorageState();
        String state = Environment.MEDIA_MOUNTED;
        if (sdcard.equals(state)) {
            File file = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(file.getPath());
            //获得可供程序使用的Block数量
            long blockAvailable = statFs.getAvailableBlocksLong();
            //获得sdcard上每个block 的大小
            long blockSize = statFs.getBlockSizeLong();
            //计算标准大小使用：1024，当然使用1000也可以
            long blockAvailableSize = blockAvailable * blockSize / 1024 / 1024;
            LogMgr.i("getAvailableSize() 剩余容量大小 blockAvailableSize = " + blockAvailableSize + " MB");
            return blockAvailableSize;
        } else {
            LogMgr.e("getAvailableSize() 存储状态异常 sdcard = " + sdcard + " state = " + state);
            return -1;
        }
    }

    //自我介绍
    private final static String[] introduce_m = {"changedansw1.mp3", "changedansw2.mp3", "changedansw3.mp3", "changedansw4.mp3", "changedansw5.mp3", "changedansw6.mp3", "changedansw7.mp3", "changedansw8.mp3", "changedansw9.mp3"};
    private final static String[] introduce_c = {"cchangedansw1.mp3", "cchangedansw2.mp3", "cchangedansw3.mp3", "cchangedansw4.mp3", "cchangedansw5.mp3", "cchangedansw6.mp3", "cchangedansw7.mp3", "cchangedansw8.mp3"};
    private final static String[] introduce_s = {"cchangedansw1.mp3", "cchangedansw2.mp3", "cchangedansw3.mp3", "cchangedansw4.mp3", "cchangedansw5.mp3", "cchangedansw6.mp3", "cchangedansw7.mp3", "cchangedansw8.mp3"};
    private final static String[] introduce_h = {"changedH1.mp3", "changedH2.mp3", "changedH3.mp3", "changedH4.mp3", "changedH5.mp3", "changedH6.mp3", "changedH7.mp3", "changedH8.mp3"};

    public static String getMusicIntroduceName(int index) {
        String[] introduce = null;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                introduce = introduce_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                introduce = introduce_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                introduce = introduce_h;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                introduce = introduce_s;
                break;
        }

        if (introduce != null && index < introduce.length) {
            return introduce[index];
        }
        return null;
    }

    public static String getMusicDirPath() {
        String dirName = "";
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                dirName = FileUtils.DIR_ABILIX_MUSIC_C;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                dirName = FileUtils.DIR_ABILIX_MUSIC_M;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                dirName = FileUtils.DIR_ABILIX_MUSIC_H;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                dirName = FileUtils.DIR_ABILIX_MUSIC_F;
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                dirName = FileUtils.DIR_ABILIX_MUSIC_S;
                break;
            case ExplainerInitiator.ROBOT_TYPE_AF:
                dirName = FileUtils.DIR_ABILIX_MUSIC_AF;
                break;
            default:
                dirName = FileUtils.DIR_ABILIX_MUSIC_C;
                break;
        }
        return dirName;
    }

    //新UI接口
    public static int getAppBg() {
        int bg = 0;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                bg = R.drawable.main_bg_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                bg = R.drawable.main_bg_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                bg = R.drawable.main_bg_h;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                bg = R.drawable.main_bg_f;
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                bg = R.drawable.main_bg_s;
                break;
        }
        return bg;
    }

    /**
     * 获取录音初始界面图片资源ID
     *
     * @return
     */
    public static int getRecordInitDrawableResource() {
        int drawableResourceID = 0;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                drawableResourceID = R.drawable.record_init_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                drawableResourceID = R.drawable.record_init_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                drawableResourceID = R.drawable.record_init_h;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                drawableResourceID = R.drawable.record_init_f;
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                drawableResourceID = R.drawable.record_init_s;
                break;
            //AF系列
            case ExplainerInitiator.ROBOT_TYPE_AF:
                drawableResourceID = R.drawable.record_init_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取录音初始界面选装光圈ID
     *
     * @return
     */
    public static int getRecordLightRingDrawableResource() {
        int drawableResourceID = 0;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                drawableResourceID = R.drawable.lightring_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                drawableResourceID = R.drawable.lightring_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                drawableResourceID = R.drawable.lightring_h;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                drawableResourceID = R.drawable.lightring_f;
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                drawableResourceID = R.drawable.lightring_s;
                break;
            //AF系列
            case ExplainerInitiator.ROBOT_TYPE_AF:
                drawableResourceID = R.drawable.lightring_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取录音初始界面选装光圈ID
     *
     * @return
     */
    public static int getRecordCompleteDrawableResource() {
        int drawableResourceID = 0;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                drawableResourceID = R.drawable.record_complete_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                drawableResourceID = R.drawable.record_complete_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                drawableResourceID = R.drawable.record_complete_h;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                drawableResourceID = R.drawable.record_complete_f;
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                drawableResourceID = R.drawable.record_complete_s;
                break;
            //AF系列
            case ExplainerInitiator.ROBOT_TYPE_AF:
                drawableResourceID = R.drawable.record_complete_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取按钮弹窗的背景图id
     * @return
     */
    public static int getButtonDialogBackgroundDrawableResource(){
        int drawableResourceID = 0;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                drawableResourceID = R.drawable.button_dialog_background_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                drawableResourceID = R.drawable.button_dialog_background_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                drawableResourceID = R.drawable.button_dialog_background_h;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                drawableResourceID = R.drawable.button_dialog_background_f;
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                drawableResourceID = R.drawable.button_dialog_background_s;
                break;
            //AF系列
            case ExplainerInitiator.ROBOT_TYPE_AF:
                drawableResourceID = R.drawable.button_dialog_background_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取无按钮弹窗的背景图id
     * @return
     */
    public static int getNoButtonDialogBackgroundDrawableResource(){
        int drawableResourceID = 0;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                drawableResourceID = R.drawable.no_button_dialog_background_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                drawableResourceID = R.drawable.no_button_dialog_background_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                drawableResourceID = R.drawable.no_button_dialog_background_h;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                drawableResourceID = R.drawable.no_button_dialog_background_f;
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                drawableResourceID = R.drawable.no_button_dialog_background_s;
                break;
            //AF系列
            case ExplainerInitiator.ROBOT_TYPE_AF:
                drawableResourceID = R.drawable.no_button_dialog_background_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取单按钮弹窗的按钮背景图id
     * @return
     */
    public static int getSingleButtonDialogButtonBackgroundDrawableResource(){
        int drawableResourceID = 0;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                drawableResourceID = R.drawable.single_button_dialog_button_background_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                drawableResourceID = R.drawable.single_button_dialog_button_background_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                drawableResourceID = R.drawable.single_button_dialog_button_background_h;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                drawableResourceID = R.drawable.single_button_dialog_button_background_f;
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                drawableResourceID = R.drawable.single_button_dialog_button_background_s;
                break;
            //AF系列
            case ExplainerInitiator.ROBOT_TYPE_AF:
                drawableResourceID = R.drawable.single_button_dialog_button_background_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取双按钮弹窗的按钮背景图id
     * @param leftOrRignt f,h需要区分左右。0表示左，1表示右。其他系列设置后并无影响。
     * @return
     */
    public static int getTwoButtonDialogButtonBackgroundDrawableResource(int leftOrRignt){
        final int left = 0;
        final int right = 1;
        int side = leftOrRignt;

        int drawableResourceID = 0;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                drawableResourceID = R.drawable.two_button_dialog_button_background_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                drawableResourceID = R.drawable.two_button_dialog_button_background_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                if(side == right){
                    drawableResourceID = R.drawable.two_button_dialog_right_button_background_h;
                }else{
                    drawableResourceID = R.drawable.two_button_dialog_left_button_background_h;
                }
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                if(side == right){
                    drawableResourceID = R.drawable.two_button_dialog_right_button_background_f;
                }else{
                    drawableResourceID = R.drawable.two_button_dialog_left_button_background_f;
                }
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                drawableResourceID = R.drawable.two_button_dialog_button_background_s;
                break;
            //AF系列
            case ExplainerInitiator.ROBOT_TYPE_AF:
                drawableResourceID = R.drawable.two_button_dialog_button_background_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取vjc显示界面图id
     * @return
     */
    public static int getVJCTextViewBackgroundDrawableResource(){
        int drawableResourceID = 0;
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                drawableResourceID = R.drawable.vjc_textview_background_c;
                break;
            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                drawableResourceID = R.drawable.vjc_textview_background_m;
                break;
            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                drawableResourceID = R.drawable.vjc_textview_background_h;
                break;
            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                drawableResourceID = R.drawable.vjc_textview_background_f;
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                drawableResourceID = R.drawable.vjc_textview_background_s;
                break;
            //AF系列
            case ExplainerInitiator.ROBOT_TYPE_AF:
                drawableResourceID = R.drawable.vjc_textview_background_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 创建并展示单按钮窗口
     * @param context
     * @param title 窗口标题
     * @param message 窗口信息
     * @param buttonString 按钮文字
     * @param isSystemDialog 是否系统窗口
     * @param onClickListener 按钮点击事件
     */
    public static AlertDialog showSingleButtonDialog(Context context, String title, String message, String buttonString, boolean isSystemDialog,
                                                     final OnClickListener onClickListener){
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.dialog).create();
        if(isSystemDialog){
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.single_button_dialog);

        RelativeLayout relativeLayout = (RelativeLayout)window.findViewById(R.id.rl_single_button_dialog);
        TextView tv_title = (TextView)window.findViewById(R.id.tv_single_dialog_title);
        TextView tv_message = (TextView)window.findViewById(R.id.tv_single_dialog_message);
        Button btn = (Button)window.findViewById(R.id.btn_single_dialog);

        relativeLayout.setBackgroundResource(getButtonDialogBackgroundDrawableResource());
        tv_title.setText(title);
        tv_message.setText(message);
        tv_message.setMovementMethod(ScrollingMovementMethod.getInstance());
        btn.setText(buttonString);
        btn.setBackgroundResource(getSingleButtonDialogButtonBackgroundDrawableResource());
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onClickListener.onClick(v);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    private static boolean dialogButtonClicked = false;
    /**
     * 创建并展示双按钮窗口
     * @param context
     * @param title 窗口标题
     * @param message 窗口信息
     * @param leftButtonString 左按钮文字
     * @param rightButtonString 右按钮文字
     * @param isSystemDialog 是否系统窗口
     * @param leftButtonOnClickListener 左按钮点击事件
     * @param rightButtonOnClickListener 右按钮点击事件
     */
    public static AlertDialog showTwoButtonDialog(Context context, String title, String message, String leftButtonString, String rightButtonString, boolean isSystemDialog,
                                                  final OnClickListener leftButtonOnClickListener, final OnClickListener rightButtonOnClickListener){
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.dialog).create();
        if(isSystemDialog){
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialogButtonClicked = false;
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.two_button_dialog);

        RelativeLayout relativeLayout = (RelativeLayout)window.findViewById(R.id.rl_two_button_dialog);
        TextView tv_title = (TextView)window.findViewById(R.id.tv_two_dialog_title);
        TextView tv_message = (TextView)window.findViewById(R.id.tv_two_dialog_message);
        Button btn_left = (Button)window.findViewById(R.id.btn_left_two_dialog);
        Button btn_right = (Button)window.findViewById(R.id.btn_right_two_dialog);

        relativeLayout.setBackgroundResource(getButtonDialogBackgroundDrawableResource());
        tv_title.setText(title);
        tv_message.setText(message);
        tv_message.setMovementMethod(ScrollingMovementMethod.getInstance());
        btn_left.setText(leftButtonString);
        btn_left.setBackgroundResource(getTwoButtonDialogButtonBackgroundDrawableResource(0));
        btn_right.setText(rightButtonString);
        btn_right.setBackgroundResource(getTwoButtonDialogButtonBackgroundDrawableResource(1));
        btn_left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogButtonClicked) {
                    dialogButtonClicked = true;
                    leftButtonOnClickListener.onClick(v);
                    dialog.dismiss();
                }
            }
        });
        btn_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogButtonClicked) {
                    dialogButtonClicked = true;
                    rightButtonOnClickListener.onClick(v);
                    dialog.dismiss();
                }
            }
        });

        return dialog;
    }

    /**
     * 创建并展示无按钮窗口
     * @param context
     * @param title 窗口标题
     * @param message 窗口信息
     * @param isSystemDialog 是否系统窗口
     */
    public static AlertDialog showNoButtonDialog(Context context, String title, String message, boolean isSystemDialog){
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.dialog).create();
        if(isSystemDialog){
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.no_button_dialog);

        RelativeLayout relativeLayout = (RelativeLayout)window.findViewById(R.id.rl_no_button_dialog);
        TextView tv_title = (TextView)window.findViewById(R.id.tv_no_button_dialog_title);
        TextView tv_message = (TextView)window.findViewById(R.id.tv_no_button_dialog_message);

        relativeLayout.setBackgroundResource(getNoButtonDialogBackgroundDrawableResource());
        tv_title.setText(title);
        tv_message.setText(message);
        tv_message.setMovementMethod(ScrollingMovementMethod.getInstance());

        return dialog;

//    	final AlertDialog dialog = new AlertDialog.Builder(context).create();
//    	if(isSystemDialog){
//    		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//    	}
//		dialog.setCancelable(false);
//		dialog.setCanceledOnTouchOutside(false);
//		dialog.show();
//		Window window = dialog.getWindow();
//		window.setContentView(R.layout.single_button_dialog);
//
//		RelativeLayout relativeLayout = (RelativeLayout)window.findViewById(R.id.rl_single_button_dialog);
//		TextView tv_title = (TextView)window.findViewById(R.id.tv_single_dialog_title);
//		TextView tv_message = (TextView)window.findViewById(R.id.tv_single_dialog_message);
//		Button btn = (Button)window.findViewById(R.id.btn_single_dialog);
//
//		relativeLayout.setBackgroundResource(BrainUtils.getButtonDialogBackgroundDrawableResource());
//		tv_title.setText(title);
//		tv_message.setText(message);
//		btn.setText(title);
//		btn.setBackgroundResource(BrainUtils.getSingleButtonDialogButtonBackgroundDrawableResource());
//
//		return dialog;
    }
}
