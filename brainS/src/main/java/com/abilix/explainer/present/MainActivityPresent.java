/**
 *
 */
package com.abilix.explainer.present;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.explainer.ControlInfo;
import com.abilix.explainer.ExplainMessage;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.ExplainerInitiator;
import com.abilix.explainer.IExplainCallBack;
import com.abilix.explainer.camera.CameraStateCallBack;
import com.abilix.explainer.camera.IRobotCamera;
import com.abilix.explainer.camera.RobotCameraStateCode;
import com.abilix.explainer.camera.systemcamera.SystemCamera;
import com.abilix.explainer.camera.usbcamera.UsbCamera;
import com.abilix.explainer.helper.M1ExplainerHelper;
import com.abilix.explainer.helper.MExplainerHelper;
import com.abilix.explainer.pushmsg.PushMsgExcutor;
import com.abilix.explainer.pushmsg.PushMsgResponse;
import com.abilix.explainer.pushmsg.PushMsgTracker;
import com.abilix.explainer.utils.CommonUtils;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.PlayerUtils;
import com.abilix.explainer.utils.RecorderUtils;
import com.abilix.explainer.view.ExplainerAlertDialogs;
import com.abilix.explainer.view.IMainActivity;
import com.abilix.explainer.view.MainActivity;
import com.abilix.vision.doMain.IdentificationType;
import com.abilix.vision.utils.VisionControl;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author jingh
 * @Descripton:
 * @date2017-2-3下午5:01:07
 */
public class MainActivityPresent implements IMainActivityPresent,
        IExplainCallBack {
    public static final int CAMERA_SUCESS = 0;
    public static final int RUNNING_TYPE_BIN = 1;
    public static final int RUNNING_TYPE_ELF = 2;
    private IMainActivity mIMainActivity;
    private RecorderUtils mRecorder;
    private IRobotCamera mIRobotCamera;
    private boolean mIsSystemCamera = false;
    private PushMsgExcutor mPushMsgExcutor;

    private PushMsgResponse mMsgResponse;


    private Timer mDialogTimer;
    private TimerTask mTimerTask;
    private int runningType = 1;

    public void setRunningType(int type) {
        runningType = type;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {

        }
    };

    public MainActivityPresent(IMainActivity mIMainActivity) {
        this.mIMainActivity = mIMainActivity;
        mPushMsgExcutor = new PushMsgExcutor(mIMainActivity);
        mRecorder = new RecorderUtils();
        if (ControlInfo.getMain_robot_type() == ExplainerInitiator.ROBOT_TYPE_C
                || ControlInfo.getMain_robot_type() == ExplainerInitiator.ROBOT_TYPE_S
                || ControlInfo.getMain_robot_type() == ExplainerInitiator.ROBOT_TYPE_H3) {
            //USB摄像头
            mIRobotCamera = UsbCamera.create();
            mIsSystemCamera = false;
        } else /* if (ControlInfo.getMain_robot_type() == ExplainerInitiator.ROBOT_TYPE_M)*/ {
            mIRobotCamera = SystemCamera.create();
            mIsSystemCamera = true;
            if (ControlInfo.getMain_robot_type() == ExplainerInitiator.ROBOT_TYPE_M && (ControlInfo.getChild_robot_type() != GlobalConfig.ROBOT_TYPE_M3S)
                    && ControlInfo.getChild_robot_type() != GlobalConfig.ROBOT_TYPE_M4S) {
                mIRobotCamera.setIsRotate(true);
            }
        }
        mMsgResponse = new PushMsgResponse();
    }

    @Override
    public void compassNotifiNegEvent() {
        ExplainMessage filePathMessage = new ExplainMessage();
        filePathMessage.setFuciton(ExplainMessage.EXPLAIN_RESUME);
        ExplainTracker.getInstance().doExplainCmd(filePathMessage, this);
        PushMsgTracker.getInstance().getPushMsgHandler().post(new Runnable() {
            @Override
            public void run() {

                mMsgResponse.compassResponse(1);
            }
        });
    }

    @Override
    public void compassNotifiPosEvent() {
        LogMgr.d("compassNotifiPosEvent");
        ExplainMessage filePathMessage = new ExplainMessage();
        if (ControlInfo.getMain_robot_type() == ExplainerInitiator.ROBOT_TYPE_M) {
            //原地转5圈
            LogMgr.d("doExplain() compass check --> set turn around.");
            if (ControlInfo.getChild_robot_type() == ExplainerInitiator.ROBOT_TYPE_M1
                    || ControlInfo.getChild_robot_type() == ExplainerInitiator.ROBOT_TYPE_M2) {
                new M1ExplainerHelper().setNewAllWheelMoto(1, 1, -20, 1, 20, 276, 276);
            } else {
                new MExplainerHelper().setNewAllWheelMoto(1, 1, -20, 1, 20, 276, 276);
            }
        }

        mIMainActivity.showAlertDialog(
                ExplainerAlertDialogs.ALERTDIALOG_COMPASSADJUST_FINISHED, null);
    }

    public void compassAdjustFinishedNegEvent() {
        ExplainMessage filePathMessage = new ExplainMessage();
        filePathMessage.setFuciton(ExplainMessage.EXPLAIN_RESUME);
        ExplainTracker.getInstance().doExplainCmd(filePathMessage, this);
        PushMsgTracker.getInstance().getPushMsgHandler().post(new Runnable() {
            @Override
            public void run() {

                mMsgResponse.compassResponse(1);
            }
        });
    }

    public void compassAdjustFinishedPosEvent() {
        LogMgr.d("指南针校准结束点击");
        ExplainMessage filePathMessage = new ExplainMessage();
        filePathMessage.setFuciton(ExplainMessage.EXPLAIN_RESUME);
        ExplainTracker.getInstance().doExplainCmd(filePathMessage, this);
        PushMsgTracker.getInstance().getPushMsgHandler().post(new Runnable() {
            @Override
            public void run() {
                LogMgr.e("返回指南针校准状态");
                mMsgResponse.compassResponse(0);
            }
        });
    }

    @Override
    public void disposeFilePath(Intent i) {
        LogMgr.d("disposeFilePath");
        if (i != null) {
            String filePath = i.getStringExtra("filePath");
            String programName = i.getStringExtra("pageName");
            LogMgr.d("程序名称::" + programName);
            if (filePath != null) {
                LogMgr.d("解析执行文件路径::" + filePath);
                if (filePath.endsWith("elf")) {
                    LogMgr.d("执行elf文件解析");
                    runningType = 2;
                    PushMsgTracker.getInstance().startExcuteELF(filePath);
                } else {
                    runningType = 1;
                    ExplainMessage filePathMessage = new ExplainMessage();
/*				Toast.makeText(MainActivity.getActivity(),
                        "点击" ,
						Toast.LENGTH_SHORT).show();*/
                    LogMgr.d("点击");
                    filePathMessage.setFuciton(ExplainMessage.EXPLAIN_START).setStrData(filePath);

                    String fileDir = filePath.substring(0, filePath.lastIndexOf(File.separator));
                    int fileType = CommonUtils.FILE_TYPE_UNKNOWN;
                    if (fileDir.contains(CommonUtils.ABILIX_PROJECT_PROGRAM)) {
                        fileType = CommonUtils.FILE_TYPE_PROJECT_PROGRAM;
                    } else if (fileDir.contains(CommonUtils.ABILIX_CHART)) {
                        fileType = CommonUtils.FILE_TYPE_CHART;
                    } else if (fileDir.contains(CommonUtils.ABILIX_SCRATCH)) {
                        fileType = CommonUtils.FILE_TYPE_SCRATCH;
                    } else if (fileDir.contains(CommonUtils.ABILIX_SKILLPLAYER)) {
                        fileType = CommonUtils.FILE_TYPE_SKILLPLAYER;
                    }
                    CommonUtils.setFileType(fileType);

                    ExplainTracker.getInstance().doExplainCmd(filePathMessage, this);
                }
                if (programName != null) {
                    LogMgr.d("程序名称::" + programName);
                    mIMainActivity.showProgram(programName);
                }
            }
        }

    }

    @Override
    public void disposeOnClickEvent(View v) {
        switch (v.getId()) {
            case R.id.iv:
                LogMgr.d("点击退出程序");
                ExplainMessage stopMessage = new ExplainMessage();
                stopMessage.setFuciton(ExplainMessage.EXPLAIN_STOP);
                ExplainTracker.getInstance().doExplainCmd(stopMessage, this);
                mIMainActivity.stopAnimation();
                //MainActivity.getActivity().finish();
                break;
            case R.id.iv_picture:

                break;

            default:
                break;
        }

    }

    @Override
    public void doExplainCallBack(ExplainMessage mExplainMessage) {
        LogMgr.d("doExplainCallBack mExplainMessage::" + mExplainMessage.toString());
        switch (mExplainMessage.getFuciton()) {
            case ExplainMessage.EXPLAIN_BALANCE_BALANCED:
                mIMainActivity
                        .dismissAlertDialog(ExplainerAlertDialogs.ALERTDIALOG_BALANCING);
                mIMainActivity.showAlertDialog(
                        ExplainerAlertDialogs.ALERTDIALOG_BALANCE_SUCESSED, null);
                Timer balancedTimer = new Timer();
                TimerTask balancedTimerTask = new TimerTask() {

                    @Override
                    public void run() {
                        mIMainActivity
                                .dismissAlertDialog(ExplainerAlertDialogs.ALERTDIALOG_BALANCE_SUCESSED);

                    }
                };
                balancedTimer.schedule(balancedTimerTask, 5000);

                break;
            case ExplainMessage.EXPLAIN_BALANCE_INITIALIZING:
                mIMainActivity.showAlertDialog(
                        ExplainerAlertDialogs.ALERTDIALOG_BALANCING, null);
                break;
            case ExplainMessage.EXPLAIN_CAMERA:
                LogMgr.d("message 拍照");
                String imageName = mExplainMessage.getStrData();
                final String imagePath = FileUtils.getPicturePath(imageName);
                mIRobotCamera.takePicture(ExplainerApplication.instance, imagePath, new CameraStateCallBack() {
                    @Override
                    public void onState(int state) {
                        LogMgr.d("拍照状态回调：" + state);
                        switch (state) {
                            case RobotCameraStateCode.SAVE_PICTURE_SUCESS:
                                LogMgr.e("onSucess 回调");
                                MainActivity.getActivity().runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mIMainActivity
                                                        .dismissAlertDialog(ExplainerAlertDialogs.ALERTDIALOG_CAMERA_OPENING);
                                                LogMgr.d("显示照片");
                                                mIMainActivity
                                                        .showPicture(imagePath);
                                                //mIMainActivity.sendBroadCastToBrain(MainActivity.INTENT_ACTION_PICTURE);
                                                ExplainMessage resumeMessage = new ExplainMessage();
                                                resumeMessage
                                                        .setFuciton(ExplainMessage.EXPLAIN_RESUME);
                                                ExplainTracker
                                                        .getInstance()
                                                        .doExplainCmd(
                                                                resumeMessage,
                                                                MainActivityPresent.this);
                                            }
                                        });
                                mIRobotCamera.cancelTakePicCallback();

                                break;
                            case RobotCameraStateCode.OPENING_CAMERA:
                                MainActivity.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mIMainActivity
                                                .showAlertDialog(
                                                        ExplainerAlertDialogs.ALERTDIALOG_CAMERA_OPENING,
                                                        null);
                                        mHandler.postDelayed(new Runnable() {

                                            @Override
                                            public void run() {
                                                mIMainActivity
                                                        .dismissAlertDialog(ExplainerAlertDialogs.ALERTDIALOG_CAMERA_OPENING);

                                            }
                                        }, 8000);
                                    }
                                });

                                break;
                            case RobotCameraStateCode.TAKE_PICTURE_USB_CAMERA_IS_NOT_CONNECTED:
                                MainActivity.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mIMainActivity
                                                .showAlertDialog(
                                                        ExplainerAlertDialogs.ALERTDIALOG_CAMERA_CONNECT_ERROR,
                                                        null);
                                        mHandler.postDelayed(new Runnable() {

                                            @Override
                                            public void run() {
                                                mIMainActivity
                                                        .dismissAlertDialog(ExplainerAlertDialogs.ALERTDIALOG_CAMERA_CONNECT_ERROR);

                                            }
                                        }, 8000);
                                    }
                                });

                            case RobotCameraStateCode.TAKE_PICTURE_CONFIGURED_FAILED:
                                ExplainMessage resumeMessage = new ExplainMessage();
                                resumeMessage.setFuciton(ExplainMessage.EXPLAIN_RESUME);
                                ExplainTracker.getInstance().doExplainCmd(resumeMessage, MainActivityPresent.this);
                                break;
                        }
                    }
                });
                break;

            case ExplainMessage.EXPLAIN_COMPASS:
                mIMainActivity
                        .showAlertDialog(
                                ExplainerAlertDialogs.ALERTDIALOG_COMPASSADJUST_NOTIFICATION,
                                null);
                break;
            case ExplainMessage.EXPLAIN_DISPLAY:
                LogMgr.d("显示：" + mExplainMessage.getStrData());
                String content = mExplainMessage.getStrData();
                String path = "";
                switch (mExplainMessage.getState()) {
                    case ExplainTracker.ARG_DISPLAY_TEXT:
                        mIMainActivity.display(content);
                        break;
                    case ExplainTracker.ARG_DISPLAY_PHOTO:
                        path = FileUtils.getFilePath(FileUtils.DIR_ABILIX_PHOTO, content, FileUtils._TYPE_JPEG);
                        if (FileUtils.isFileExist(path)) {
                            mIMainActivity.showPicture(path);
                        }
                        break;
                    case ExplainTracker.ARG_DISPLAY_ANIMATION:
                        path = FileUtils.getFilePath(FileUtils.DIR_DEFAULT_IMAGE_ANIM_KU, content, FileUtils._TYPE_GIF);
                        if (FileUtils.isFileExist(path)) {
                            mIMainActivity.showPicture(path);
                        }
                    case ExplainTracker.ARG_DISPLAY_CUSTOM_IMAGE:
                        path = FileUtils.getFilePath(FileUtils.DIR_MEDIA_UPLOAD_IMAGE, content);
                        if (FileUtils.isFileExist(path)) {
                            mIMainActivity.showPicture(path);
                        }
                        break;
                }
                break;
            case ExplainMessage.EXPLAIN_NO_DISPLAY:
                LogMgr.d("不显示");
                mIMainActivity.finishDisplay();
                mIMainActivity.dismissPicture();
                break;
            case ExplainMessage.EXPLAIN_DISPLAY_GRAY:

                String s1 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT1);
                String s2 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT2);
                LogMgr.e("寻线车数据", "s1:" + s1 + " s2:" + s2);
                mIMainActivity.showAlertDialog(
                        ExplainerAlertDialogs.ALERTDIALOG_GRAY_VALUE, s1 + "\n"
                                + s2);
                break;
            case ExplainMessage.EXPLAIN_IS_GETAI:
                mIMainActivity.showAlertDialog(
                        ExplainerAlertDialogs.ALERTDIALOG_GRAY_ISCOLLECT, null);
                break;
            case ExplainMessage.EXPLAIN_RECORD:
                LogMgr.d("message 录音");
                String name = mExplainMessage.getStrData();
                final String audioPath = FileUtils.getFilePath(FileUtils.DIR_ABILIX_RECORD, name, FileUtils._TYPE_3GPP);
                final int duration = mExplainMessage.getIntData();
                mIMainActivity.showRecordView();
                mRecorder.startRecord(audioPath, duration, new RecorderUtils.OnCompletionListener() {
                    @Override
                    public void onCompletion(int what) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (GlobalConfig.ENABLE_RECORDING_PLAY) {
                                    mIMainActivity.showPlayRecordView();
                                    PlayerUtils.getInstance().play(audioPath, new PlayerUtils.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(int state) {
                                            mIMainActivity.dimissRecordView();
                                            ExplainMessage resumeMessage = new ExplainMessage();
                                            resumeMessage.setFuciton(ExplainMessage.EXPLAIN_RESUME);
                                            ExplainTracker.getInstance().doExplainCmd(resumeMessage, MainActivityPresent.this);
                                        }
                                    });
                                } else {
                                    mIMainActivity.dimissRecordView();
                                    ExplainMessage resumeMessage = new ExplainMessage();
                                    resumeMessage.setFuciton(ExplainMessage.EXPLAIN_RESUME);
                                    ExplainTracker.getInstance().doExplainCmd(resumeMessage, MainActivityPresent.this);
                                }
                            }
                        });
                    }

                    @Override
                    public void onMicStatusUpdate(float db) {
                        LogMgr.d("onMicStatusUpdate() " + db);
                    }
                }, false);
                break;
            case ExplainMessage.EXPLAIN_LED:
                byte[] data = mExplainMessage.getData();
                LogMgr.d("ExplainMessage.EXPLAIN_LED: USBVideo setBrightnessS()");
                int value = 0;
                if (data.length == 1) {
                    value = data[0] & 0xFF;
                } else {
                    value = (data[0] & 0xFF) << 8 | data[1] & 0xFF;
                }
                if (mLastLedValue != value) {
                    mLastLedValue = value;
                    if (data.length == 2) {
                        UsbCamera.create().setBrightnessS(ExplainerApplication.instance, (value >> 8) & 0xff, mCameraStateCallBack);
                        UsbCamera.create().setBrightnessS(ExplainerApplication.instance, value & 0xff, mCameraStateCallBack);
                    } else {
                        UsbCamera.create().setBrightnessS(ExplainerApplication.instance, value, mCameraStateCallBack);
                    }
                }
                break;
            case ExplainMessage.EXPLAIN_VISION:
                int index = mExplainMessage.getIntData();
                switch (index){
                    case 146:
                        VisionControl.startVision(null, IdentificationType.COLOR_BLOCK);
                        break;
                    case 147:
                        VisionControl.startVision(null, IdentificationType.DIGITAL);
                        break;
                    case 148:
                        VisionControl.startVision(null, IdentificationType.ARROW_COLOR_DIRECTION);
                        break;
                    case 149:
                        VisionControl.startVision(null, IdentificationType.IMAGE_TRACKING);
                        break;
                }
                break;

            default:
                break;
        }
    }

    private int mLastLedValue = -1; //S5 led灯无限循环概率性关闭不了；重复指令不处理

    private CameraStateCallBack mCameraStateCallBack = new CameraStateCallBack() {
        @Override
        public void onState(int state) {
            LogMgr.d("相机状态回调：" + state);
        }
    };

    @Override
    public void grayIsCollectNegEvent() {
        if (runningType == RUNNING_TYPE_ELF) {
            //elf文件
            PushMsgTracker.getInstance().getPushMsgHandler().post(new Runnable() {
                @Override
                public void run() {

                    mMsgResponse.trackLine_EnvCollect_Response(0);
                }
            });
        } else {
            ExplainMessage resumeMessage = new ExplainMessage();
            resumeMessage.setFuciton(ExplainMessage.EXPLAIN_NO_GETAI);
            ExplainTracker.getInstance().doExplainCmd(resumeMessage, this);
        }
    }

    @Override
    public void grayIsCollectPosEvent() {
        if (runningType == RUNNING_TYPE_ELF) {
            //elf文件
            PushMsgTracker.getInstance().getPushMsgHandler().post(new Runnable() {
                @Override
                public void run() {

                    mMsgResponse.trackLine_EnvCollect_Response(1);
                }
            });
        } else {
            mIMainActivity.showAlertDialog(
                    ExplainerAlertDialogs.ALERTDIALOG_GRAY_BLACKLINE, null);
        }
    }

    @Override
    public void grayBlackLineNegEvent() {
        if (runningType == RUNNING_TYPE_ELF) {
            mIMainActivity.dismissAlertDialog(ExplainerAlertDialogs.ALERTDIALOG_GRAY_ISCOLLECT);
            //elf文件
            PushMsgTracker.getInstance().getPushMsgHandler().post(new Runnable() {
                @Override
                public void run() {

                    mMsgResponse.trackLine_LineCollect_Response(0);
                }
            });
        } else {
            ExplainMessage resumeMessage = new ExplainMessage();
            resumeMessage.setFuciton(ExplainMessage.EXPLAIN_NO_GETAI);
            ExplainTracker.getInstance().doExplainCmd(resumeMessage, this);
        }
    }

    @Override
    public void grayBlackLinePosEvent() {
        LogMgr.d("runningType:" + runningType);
        if (runningType == RUNNING_TYPE_ELF) {
            //elf文件
            mIMainActivity.dismissAlertDialog(ExplainerAlertDialogs.ALERTDIALOG_GRAY_ISCOLLECT);
            PushMsgTracker.getInstance().getPushMsgHandler().post(new Runnable() {
                @Override
                public void run() {
                    mMsgResponse.trackLine_LineCollect_Response(1);
                }
            });
        } else {
            mIMainActivity.showAlertDialog(
                    ExplainerAlertDialogs.ALERTDIALOG_GRAY_WHITEBACKGROUND, null);
            ExplainMessage blackLineMessage = new ExplainMessage();
            blackLineMessage.setFuciton(ExplainMessage.EXPLAIN_FIRST_GETAI);
            ExplainTracker.getInstance().doExplainCmd(blackLineMessage, this);
        }
    }

    @Override
    public void grayWhiteBackgroundNegEvent() {
        LogMgr.d("runningType:" + runningType);
        if (runningType == RUNNING_TYPE_ELF) {
            //elf文件
            mIMainActivity.dismissAlertDialog(ExplainerAlertDialogs.ALERTDIALOG_GRAY_WHITEBACKGROUND);
            PushMsgTracker.getInstance().getPushMsgHandler().post(new Runnable() {
                @Override
                public void run() {

                    mMsgResponse.trackLine_BGCollect_Response(0);
                }
            });
        } else {
            ExplainMessage resumeMessage = new ExplainMessage();
            resumeMessage.setFuciton(ExplainMessage.EXPLAIN_NO_GETAI);
            ExplainTracker.getInstance().doExplainCmd(resumeMessage, this);
        }
    }

    @Override
    public void grayWhiteBackgroundPosEvent() {
        LogMgr.d("runningType:" + runningType);
        if (runningType == RUNNING_TYPE_ELF) {
            //elf文件
            mIMainActivity.dismissAlertDialog(ExplainerAlertDialogs.ALERTDIALOG_GRAY_WHITEBACKGROUND);
            PushMsgTracker.getInstance().getPushMsgHandler().post(new Runnable() {
                @Override
                public void run() {

                    mMsgResponse.trackLine_BGCollect_Response(1);
                }
            });
        } else {
            ExplainMessage whiteBackgroundMessage = new ExplainMessage();
            whiteBackgroundMessage.setFuciton(ExplainMessage.EXPLAIN_SECOND_GETAI);
            ExplainTracker.getInstance().doExplainCmd(whiteBackgroundMessage, this);
        }
    }

    @Override
    public void disposeOnLongClickEvent(View v) {
        switch (v.getId()) {
            case R.id.iv_picture:
                LogMgr.d("长按picture view");
                // mIMainActivity.dismissPicture();
                ExplainMessage stop_picture_Message = new ExplainMessage();
                stop_picture_Message.setFuciton(ExplainMessage.EXPLAIN_STOP);
                ExplainTracker.getInstance().doExplainCmd(stop_picture_Message, this);
                //MainActivity.getActivity().finish();
                break;
            case R.id.rl_record:
                LogMgr.d("长按record view");
                // mIMainActivity.dimissRecordView();
                ExplainMessage stop_record_Message = new ExplainMessage();
                stop_record_Message.setFuciton(ExplainMessage.EXPLAIN_STOP);
                ExplainTracker.getInstance().doExplainCmd(stop_record_Message, this);
                //MainActivity.getActivity().finish();
                break;

            case R.id.tv_display:
                LogMgr.d("长按display view");
                // mIMainActivity.finishDisplay();
                ExplainMessage stop_display_Message = new ExplainMessage();
                stop_display_Message.setFuciton(ExplainMessage.EXPLAIN_STOP);
                ExplainTracker.getInstance().doExplainCmd(stop_display_Message, this);
                //MainActivity.getActivity().finish();
                break;
        }

    }

    @Override
    public void pause() {
        LogMgr.d("pause()");
        //if (mRecorder != null) {
        //    mRecorder.stop();
        //}
        //PlayerUtils.getInstance().stop();
    }

    @Override
    public void destroy() {
        LogMgr.d("destroy()");
        if (mRecorder != null) {
            try {
                mRecorder.stopRecord();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PlayerUtils.getInstance().destroy();
        if (mIRobotCamera != null) {
            if (mIsSystemCamera) {
                mIRobotCamera.destory();
            } else {
                mIRobotCamera.cancelTakePicCallback();
            }
            mIRobotCamera = null;
        }
        //退出时清空Handler回调函数和消息队列
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void responseAndfinish() {
        LogMgr.d("response and finish");
        //mIMainActivity.responseResult();
        ExplainMessage stop_display_Message = new ExplainMessage();
        stop_display_Message.setFuciton(ExplainMessage.EXPLAIN_STOP);
        ExplainTracker.getInstance().doExplainCmd(stop_display_Message, this);
        //MainActivity.getActivity().finish();
    }
}
