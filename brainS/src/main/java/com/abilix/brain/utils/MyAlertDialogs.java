package com.abilix.brain.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.abilix.brain.Application;
import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;

/**
 * 弹窗管理类。
 */
public class MyAlertDialogs {
    private static AlertDialog alertDialog1, alertDialog2, alertDialog3,
            alertDialog4, alertDialog10,alertDialog11;

    public static void setAlertDialog1(final Context context, final Handler mHandler) {
        Utils.showTwoButtonDialog(context, context.getString(R.string.huanjingcaiji), context.getString(R.string.shifouhuanjingcaiji),
                context.getString(R.string.fou), context.getString(R.string.shi), true, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_BEGIN, -1, 0).sendToTarget();
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setAlertDialog2(context, mHandler);
                    }
                });
    }

    private static void setAlertDialog2(final Context context,
                                        final Handler mHandler) {
        Utils.showTwoButtonDialog(context, context.getString(R.string.huanjingcaiji), context.getString(R.string.huanjingcaiji_duizhunheixian),
                context.getString(R.string.fou), context.getString(R.string.shi), true, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_BEGIN, -1, 0).sendToTarget();
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_BEGIN, 1, 0).sendToTarget();
                        setAlertDialog3(context, mHandler);
                    }
                });
    }

    private static void setAlertDialog3(Context context, final Handler mHandler) {
        Utils.showTwoButtonDialog(context, context.getString(R.string.huanjingcaiji), context.getString(R.string.huanjingcaiji_duizhunbaise),
                context.getString(R.string.fou), context.getString(R.string.shi), true, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_BEGIN, -1, 0).sendToTarget();
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_BEGIN, 2, 0).sendToTarget();
                    }
                });
    }

    public static void setAlertDialog4(String text, Context context) {
        Utils.showTwoButtonDialog(context, context.getString(R.string.huiduzhi), text,
                context.getString(R.string.fou), context.getString(R.string.shi), true, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
    }

    /**
     * 平衡车
     *
     * @param context
     * @param text
     * @param b
     * @param mode
     * @return 窗口
     */
    public static AlertDialog setAlertDialog5(Context context, String text,
                                              boolean b, int mode) {
        AlertDialog ad = null;
        if (mode == 0) {
            ad = Utils.showNoButtonDialog(context, context.getString(R.string.pinghengche), text, true);
        } else {
            ad = Utils.showNoButtonDialog(context, context.getString(R.string.pinghengche), "\t\t\t\t" + text, true);
        }
        return ad;
    }

    /**
     * 指南针校准
     *
     * @param context
     * @param b
     * @param mHandler
     */
    public static void setAlertDialog6(final Context context, boolean b, final Handler mHandler, final int mode) {
        int stringId;
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
            stringId = R.string.zhinanzhenxuanzhuan;
        } else {
            stringId =  R.string.zhinanzhenbazi;
        }
        Utils.showTwoButtonDialog(context, context.getString(R.string.zhinanzhenjiaozhun), context.getString(stringId),
                context.getString(R.string.fou), context.getString(R.string.shi), true, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message.obtain(mHandler, 314, mode, 0).sendToTarget();
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
                            Message.obtain(mHandler, 314, mode, 1).sendToTarget();
                        }
                        setAlertDialog7(context, mHandler, mode);
                    }
                });
    }

    /**
     * 指南针校准
     *
     * @param context
     * @param mHandler
     */
    private static void setAlertDialog7(final Context context, final Handler mHandler, final int mode) {
        Utils.showTwoButtonDialog(context, context.getString(R.string.zhinanzhenjiaozhun), context.getString(R.string.zhinanzhenwancheng),
                context.getString(R.string.fou), context.getString(R.string.shi), true, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message.obtain(mHandler, 314, mode, 0).sendToTarget();
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message.obtain(mHandler, 314, mode, 0).sendToTarget();
                    }
                });
    }

    /**
     * 摄像头
     *
     * @param context
     * @param text
     * @param b
     * @return 窗口
     */
    public static AlertDialog setAlertDialog8(Context context, String text,
                                              String mss, boolean b) {
        LogMgr.d("setAlertDialog8()");

        return Utils.showNoButtonDialog(context, text, mss, true);
    }

    /**
     * stm 升级
     *
     * @param context
     * @param b
     * @return 窗口
     */
    public static AlertDialog setAlertDialog9(Context context, boolean b, final Handler mHandler) {
        String leftBbuttonString = context.getString(R.string.fou);
        String rightBbuttonString = context.getString(R.string.shi);
        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
            leftBbuttonString = context.getString(R.string.later);
            rightBbuttonString = context.getString(R.string.update);
        }
        return Utils.showTwoButtonDialog(context, context.getString(R.string.shengjigujian), context.getString(R.string.shengjigujian_stm32),
                leftBbuttonString, rightBbuttonString, true, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message.obtain(mHandler, 314, 10, -1).sendToTarget();
                    }
                });
    }
    /**
     * M轮子进入保护状态
     *
     * @param context
     * @param mHandler
     */
    public static void setAlertDialog10(Context context, final Handler mHandler) {
        if (alertDialog10 != null && alertDialog10.isShowing()) {
            LogMgr.w("setAlertDialog10() M轮子进入保护状态 的提示框已显示");
            return;
        }
        LogMgr.w("setAlertDialog10() 显示 M轮子进入保护状态 的提示框");
        Utils.showSingleButtonDialog(context, context.getString(R.string.tishi), context.getString(R.string.m_robot_blocked),
                context.getString(R.string.queren), true, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Application.getInstance().setMWheelProtected(false);
                        mHandler.sendEmptyMessage(BrainService.HANDLER_MESSAGE_M_BLOCKED_CONFIM);
                    }
                });
    }
    //机器对应的温度过高 的提示框
    public static void setAlertDialog11(Context context, final Handler mHandler) {
        if (alertDialog11 != null && alertDialog11.isShowing()) {
            LogMgr.w("setAlertDialog11() 机器对应的温度过高");
            return;
        }
        LogMgr.w("setAlertDialog11() 显示 机器对应的温度过高 的提示框");
        alertDialog11 = Utils.showNoButtonDialogTempH(context, true, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(121);
                alertDialog11.dismiss();
                alertDialog11.cancel();
                alertDialog11 = null;
            }
        });
    }
    public static void setAlertDialog11close(Context context){
        if (alertDialog11 != null && alertDialog11.isShowing()) {
            LogMgr.w("setAlertDialog11() 关闭提示框");
            alertDialog11.dismiss();
            alertDialog11.cancel();
            alertDialog11 = null;
        }
    }
    public static void close() {
        alertDialog1 = null;
        alertDialog2 = null;
        alertDialog3 = null;
        alertDialog4 = null;
    }

}
