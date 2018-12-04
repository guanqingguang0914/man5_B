package com.abilix.explainer.view;

import com.abilix.explainer.ControlInfo;
import com.abilix.explainer.ExplainerInitiator;
import com.abilix.brain.R;
import com.abilix.explainer.present.IMainActivityPresent;
import com.abilix.explainer.utils.CommonUtils;
import com.abilix.explainer.utils.LogMgr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ExplainerAlertDialogs implements IExplainerAlertDialogs {
    public static final int ALERTDIALOG_BALANCING = 0;
    public static final int ALERTDIALOG_BALANCE_SUCESSED = 1;
    public static final int ALERTDIALOG_BALANCE_FAILED = 2;
    public static final int ALERTDIALOG_COMPASSADJUST_NOTIFICATION = 3;
    public static final int ALERTDIALOG_COMPASSADJUST_FINISHED = 4;
    public static final int ALERTDIALOG_CAMERA_OPENING = 5;
    public static final int ALERTDIALOG_CAMERA_CONNECT_ERROR = 6;
    public static final int ALERTDIALOG_GRAY_VALUE = 7;
    public static final int ALERTDIALOG_GRAY_ISCOLLECT = 8;
    public static final int ALERTDIALOG_GRAY_BLACKLINE = 9;
    public static final int ALERTDIALOG_GRAY_WHITEBACKGROUND = 10;
    public static final int ALERTDIALOG_DELETE_VIEWPAGE = 11;

    private AlertDialog mBalancingAlertDialog;
    private AlertDialog mBalanceAlertDialogFailedDialog;
    private AlertDialog mBalanceAlertDialogSucessedDialog;
    private AlertDialog mCompassAdjustNotificationAlertDialog;
    private AlertDialog mCompassAdjustFinishedAlertDialog;
    private AlertDialog mCameraOpeningAlertDialog;
    private AlertDialog mCameraConnectiongErrorAlertDialog;
    private AlertDialog mGrayValueAlertDialog;

    private AlertDialog mGrayIsCollectAlertDialog;
    private AlertDialog mGrayBlackLineAlertDialog;
    private AlertDialog mGrayWhiteBackGroundAlertDialog;
    private AlertDialog mDeleteViewPageAlertDialog;

    private IMainActivityPresent mainActivityPresent;

    public ExplainerAlertDialogs(IMainActivityPresent mainActivityPresent) {
        this.mainActivityPresent = mainActivityPresent;
    }

    public void showAlertDialog(Context context, int which, String message) {
        LogMgr.d("showAlertDialog: which: " + which + " message: " + message);
        switch (which) {
            case ALERTDIALOG_BALANCING: //0
                showBalancingAlertDialog(context);
                break;
            case ALERTDIALOG_BALANCE_SUCESSED: //1
                showBalanceSucessedAlertDialog(context);
                break;
            case ALERTDIALOG_BALANCE_FAILED: //2
                showBalanceFailedAlertDialog(context);
                break;
            case ALERTDIALOG_COMPASSADJUST_NOTIFICATION: //3
                showCompassAdjustNotificationAlertDialog(context);
                break;
            case ALERTDIALOG_COMPASSADJUST_FINISHED: //4
                showCompassAdjustFinishedAlertDialog(context);
                break;
            case ALERTDIALOG_CAMERA_OPENING: //5
                showCameraOpeningAlertDialog(context);
                break;
            case ALERTDIALOG_CAMERA_CONNECT_ERROR: //6
                showCameraConnectiongErrorAlertDialog(context);
                break;

            case ALERTDIALOG_GRAY_VALUE: //7
                showGrayValueAlertDialog(context, message);
                break;
            case ALERTDIALOG_GRAY_ISCOLLECT:
                showGrayIsCollectAlertDialog(context);
                break;
            case ALERTDIALOG_GRAY_BLACKLINE:
                showGrayBlackLineAlertDialog(context);
                break;
            case ALERTDIALOG_GRAY_WHITEBACKGROUND:
                showGrayWhiteBackGroundAlertDialog(context);
                break;
            case ALERTDIALOG_DELETE_VIEWPAGE:
                showDeleteViewPageAlertDialog(context);
                break;

            default:
                break;
        }
    }

    public void dismissAlertDialog(int which) {
        switch (which) {
            case ALERTDIALOG_BALANCING: //0
                dismissBalancingAlertDialog();
                break;
            case ALERTDIALOG_BALANCE_SUCESSED: //1
                dismissBalanceSucessedAlertDialog();
                break;
            case ALERTDIALOG_BALANCE_FAILED: //2
                dismissBalanceFailedAlertDialog();
                break;
            case ALERTDIALOG_COMPASSADJUST_NOTIFICATION: //3
                dismissCompassAdjustNotificationAlertDialog();
                break;
            case ALERTDIALOG_COMPASSADJUST_FINISHED: //4
                dismissCompassAdjustFinishedAlertDialog();
                break;
            case ALERTDIALOG_CAMERA_OPENING: //5
                dismissCameraOpeningAlertDialog();
                break;
            case ALERTDIALOG_CAMERA_CONNECT_ERROR: //6
                dismissCameraConnectiongErrorAlertDialog();
                break;
            case ALERTDIALOG_GRAY_VALUE: //7
                dismissDeleteViewPageAlertDialog();
                break;

            default:
                break;
        }
    }

    public void showBalancingAlertDialog(Context context) {
        mBalancingAlertDialog = CommonUtils.showNoButtonDialog(
                context,
                context.getResources().getString(R.string.pinghengche),
                context.getResources().getString(R.string.pinghengchezhengzai),
                false);
        mBalancingAlertDialog.setCancelable(true);
        mBalancingAlertDialog.show();
    }

    public void showBalanceFailedAlertDialog(Context context) {
        mBalanceAlertDialogFailedDialog = CommonUtils.showNoButtonDialog(
                context,
                context.getResources().getString(R.string.pinghengche),
                context.getResources().getString(
                        R.string.pinghengshibai),
                false);
        mBalanceAlertDialogFailedDialog.setCancelable(true);
        mBalanceAlertDialogFailedDialog.show();
    }

    public void showBalanceSucessedAlertDialog(Context context) {
        mBalanceAlertDialogSucessedDialog = CommonUtils.showNoButtonDialog(
                context,
                context.getResources().getString(R.string.pinghengche),
                context.getResources().getString(
                        R.string.pinghengchenggong),
                false);
        mBalanceAlertDialogSucessedDialog.setCancelable(true);
        mBalanceAlertDialogSucessedDialog.show();
    }

    // 显示采集到的灰度数据
    public void showGrayValueAlertDialog(Context context, String data) {
        mGrayValueAlertDialog = CommonUtils.showNoButtonDialog(
                context,
                context.getResources().getString(R.string.huiduzhi),
                data,
                false);
        mGrayValueAlertDialog.setCancelable(true);
        mGrayValueAlertDialog.show();
    }

    public void dismissGrayValueAlertDialog() {
        if (mGrayValueAlertDialog != null) {
            mGrayValueAlertDialog.dismiss();
            mGrayValueAlertDialog = null;
        }

    }

    public void showDeleteViewPageAlertDialog(Context context) {
        mDeleteViewPageAlertDialog = CommonUtils.showTwoButtonDialog(
                context,
                "",
                context.getString(R.string.delete),
                context.getString(R.string.determine),
                context.getString(R.string.cancel),
                false,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogMgr.d("点击确认删除");
                        mainActivityPresent.responseAndfinish();
                    }
                },
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
        mDeleteViewPageAlertDialog.setCancelable(true);
        mDeleteViewPageAlertDialog.show();
    }

    public void dismissDeleteViewPageAlertDialog() {
        if (mDeleteViewPageAlertDialog != null) {
            mDeleteViewPageAlertDialog.dismiss();
            mDeleteViewPageAlertDialog = null;
        }

    }

    public void showGrayIsCollectAlertDialog(Context context) {
        mGrayIsCollectAlertDialog = CommonUtils.showTwoButtonDialog(
                context,
                context.getResources().getString(R.string.huanjingcaiji),
                context.getString(R.string.shifouhuanjingcaiji),
                context.getString(R.string.shi),
                context.getString(R.string.fou),
                false,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.grayIsCollectPosEvent();
                    }
                },
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.grayIsCollectNegEvent();
                    }
                });
        mGrayIsCollectAlertDialog.setCancelable(true);
        mGrayIsCollectAlertDialog.show();
    }

    public void dismissGrayIsCollectAlertDialog() {
        if (mGrayIsCollectAlertDialog != null) {
            mGrayIsCollectAlertDialog.dismiss();
            mGrayIsCollectAlertDialog = null;
        }

    }

    public void showGrayWhiteBackGroundAlertDialog(Context context) {
        mGrayWhiteBackGroundAlertDialog = CommonUtils.showTwoButtonDialog(
                context,
                context.getResources().getString(R.string.huanjingcaiji),
                context.getString(R.string.huanjingcaiji_duizhunbaise),
                context.getString(R.string.shi),
                context.getString(R.string.fou),
                false,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.grayWhiteBackgroundPosEvent();
                    }
                },
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.grayWhiteBackgroundNegEvent();
                    }
                });
        mGrayWhiteBackGroundAlertDialog.setCancelable(true);
        mGrayWhiteBackGroundAlertDialog.show();
    }

    public void dismissGrayWhiteBackGroundAlertDialog() {
        if (mGrayWhiteBackGroundAlertDialog != null) {
            mGrayWhiteBackGroundAlertDialog.dismiss();
            mGrayWhiteBackGroundAlertDialog = null;
        }

    }

    public void showGrayBlackLineAlertDialog(Context context) {
        mGrayBlackLineAlertDialog = CommonUtils.showTwoButtonDialog(
                context,
                context.getResources().getString(R.string.huanjingcaiji),
                context.getString(R.string.huanjingcaiji_duizhunheixian),
                context.getString(R.string.shi),
                context.getString(R.string.fou),
                false,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.grayBlackLinePosEvent();
                    }
                },
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.grayBlackLineNegEvent();
                    }
                });
        mGrayBlackLineAlertDialog.setCancelable(true);
        mGrayBlackLineAlertDialog.show();
    }

    public void dismissGrayBlackLineAlertDialog() {
        if (mGrayBlackLineAlertDialog != null) {
            mGrayBlackLineAlertDialog.dismiss();
            mGrayBlackLineAlertDialog = null;
        }

    }

    public void dismissBalanceFailedAlertDialog() {
        if (mBalanceAlertDialogFailedDialog != null) {
            mBalanceAlertDialogFailedDialog.dismiss();
            mBalanceAlertDialogFailedDialog = null;
        }

    }

    public void dismissBalanceSucessedAlertDialog() {
        if (mBalanceAlertDialogSucessedDialog != null) {
            mBalanceAlertDialogSucessedDialog.dismiss();
            mBalanceAlertDialogSucessedDialog = null;
        }

    }

    @Override
    public void showCompassAdjustNotificationAlertDialog(Context context) {
        LogMgr.d("showCompassAdjustNotificationAlertDialog");
        int stringId;
        if (ControlInfo.getMain_robot_type() == ExplainerInitiator.ROBOT_TYPE_M) {
            stringId = R.string.zhinanzhenxuanzhuan;
        } else {
            stringId =  R.string.zhinanzhenbazi;
        }
        mCompassAdjustNotificationAlertDialog = CommonUtils.showTwoButtonDialog(
                context,
                context.getResources().getString(R.string.zhinanzhenjiaozhun),
                context.getResources().getString(stringId),
                context.getString(R.string.shi),
                context.getString(R.string.fou),
                false,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.compassNotifiPosEvent();
                    }
                },
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.compassNotifiNegEvent();
                    }
                });
        mCompassAdjustNotificationAlertDialog.setCancelable(true);
        mCompassAdjustNotificationAlertDialog.show();
    }

    @Override
    public void dismissCompassAdjustNotificationAlertDialog() {
        if (mCompassAdjustNotificationAlertDialog != null) {
            mCompassAdjustNotificationAlertDialog.dismiss();
            mCompassAdjustNotificationAlertDialog = null;
        }
    }

    @Override
    public void showCompassAdjustFinishedAlertDialog(Context context) {
        mCompassAdjustFinishedAlertDialog = CommonUtils.showTwoButtonDialog(
                context,
                context.getResources().getString(R.string.zhinanzhenjiaozhun),
                context.getResources().getString(R.string.zhinanzhenwancheng),
                context.getString(R.string.shi),
                context.getString(R.string.fou),
                false,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.compassAdjustFinishedPosEvent();
                    }
                },
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivityPresent.compassAdjustFinishedNegEvent();
                    }
                });
        mCompassAdjustFinishedAlertDialog.setCancelable(true);
        mCompassAdjustFinishedAlertDialog.show();

    }

    @Override
    public void dismissCompassAdjustFinishedAlertDialog() {
        if (mCompassAdjustFinishedAlertDialog != null) {
            mCompassAdjustFinishedAlertDialog.dismiss();
            mCompassAdjustFinishedAlertDialog = null;
        }
    }

    @Override
    public void showCameraOpeningAlertDialog(Context context) {
        // dismissCameraOpeningAlertDialog();
        if (mCameraOpeningAlertDialog != null
                && mCameraOpeningAlertDialog.isShowing()) {
            return;
        }
        mCameraOpeningAlertDialog = CommonUtils.showNoButtonDialog(
                context,
                context.getResources().getString(R.string.shexiangtou),
                context.getResources().getString(R.string.shexiangtouzhengzai),
                false);
        mCameraOpeningAlertDialog.setCancelable(true);
        mCameraOpeningAlertDialog.show();
    }

    @Override
    public void dismissCameraOpeningAlertDialog() {
        if (mCameraOpeningAlertDialog != null) {
            mCameraOpeningAlertDialog.dismiss();
            mCameraOpeningAlertDialog = null;
        }
    }

    @Override
    public void showCameraConnectiongErrorAlertDialog(Context context) {

        if (mCameraConnectiongErrorAlertDialog!=null&&mCameraConnectiongErrorAlertDialog.isShowing()){
            return;
        }
        mCameraConnectiongErrorAlertDialog = CommonUtils.showNoButtonDialog(
                context,
                context.getResources().getString(R.string.shexiangtou),
                context.getResources().getString(R.string.shexiangtoushifouchahao),
                false);
        mCameraConnectiongErrorAlertDialog.setCancelable(true);
        mCameraConnectiongErrorAlertDialog.show();

    }

    @Override
    public void dismissCameraConnectiongErrorAlertDialog() {
        if (mCameraConnectiongErrorAlertDialog != null) {
            mCameraConnectiongErrorAlertDialog.dismiss();
            mCameraConnectiongErrorAlertDialog = null;
        }
    }

    @Override
    public void dismissBalancingAlertDialog() {
        if (mBalancingAlertDialog != null) {
            mBalancingAlertDialog.dismiss();
            mBalancingAlertDialog = null;
        }
    }

}
