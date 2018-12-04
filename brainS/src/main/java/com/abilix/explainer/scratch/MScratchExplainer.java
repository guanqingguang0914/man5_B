package com.abilix.explainer.scratch;

import android.os.Handler;

import com.abilix.explainer.AExplainer;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.FunNode;
import com.abilix.brain.R;
import com.abilix.explainer.helper.AExplainHelper;
import com.abilix.explainer.helper.MExplainerHelper;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

public class MScratchExplainer extends AExplainer {
    protected MExplainerHelper explainerHelper;

    public MScratchExplainer(Handler handler) {
        super(handler);
        explainerHelper = MExplainerHelper.getInstance();
    }

    // 函数标识
    private boolean isDisplay = false;

    @Override
    public void doExplain(String filePath) {
        super.doExplain(filePath);
        LogMgr.d("start explain vjc file");
        // 文件总长度
        int len = filebuf.length;
        subFunNodeJava = new FunNode();
        int funPos = 0;
        byte runBuffer[] = new byte[40];
        int index = 0;
        int reValue = 0;
        float param1 = 0;
        float param2 = 0;
        float param3 = 0;
        float param4 = 0;
        float param5 = 0;
        gloableCount = -1;
        globalFunNodeCount = 0;
        funPos = start;
        //系统时钟两个变量。
        long init_time = 0;
        long end_time = 0;
        init_time = end_time = System.currentTimeMillis();
        do {
            if (!doexplain) { // 文件解析结束后退出
                LogMgr.d("explain finish and exit");
                return;
            }
            explainerHelper.readFromFlash(filebuf, funPos, runBuffer, 40); // 获取40字节数据
            index = explainerHelper.getU16(runBuffer, 8);
            funPos = explainerHelper.getU16(runBuffer, 34) * 40;
            LogMgr.d(String.format(Locale.US, "====>index:%d funPos:%d runBuffer:%s", index, funPos, ByteUtils.bytesToString(runBuffer, runBuffer.length)));
            if (funPos == 0 && index == 0) {
                break;
            }
            switch (index) {
                // 休眠时间
                case 3:
                    LogMgr.d(" control ------------------>休眠");
                    isSleep = true;
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param1 = param1 * 1000f;
                    String str = String.valueOf((int) param1);
                    int i = (int) (Long.parseLong(str) / 100);
                    int n = 0;
                    while (isSleep & n < i) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        n++;
                    }
                    if (isDisplay) {
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                        isDisplay = false;
                    }
                    break;
                // 运算
                case 21:
                    LogMgr.d(" control ------------------>运算");
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);

                    reValue = explainerHelper.getU16(runBuffer, 30);
                    LogMgr.d(" control param1:" + param1 + " param2:" + param2 + " param3:" + param3 + " funPos:" + funPos + " reValue:" + reValue);
                    mValue[reValue] = explainerHelper.my_Calc(param1, param2, (int) param3);
                    break;
                // 跳转-->循环
                case 22:
                    LogMgr.d("control ------------------>循环");
                    if (gloableCount == -1) {
                        gloableCount = explainerHelper.getU16(runBuffer, 4) - 1;
                    }
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    funPos = (param1 > 0.5) ? (int) param2 : (int) param3;
                    funPos = funPos * 40;
                    LogMgr.e("param1:" + param1 + " param2:" + param2 + " param3:" + param3 + " funPos:" + funPos + " gloableCount:" + gloableCount);
                    break;
                // 调用子程序
                case 24:
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    if (!addFunNodeJava(funPos)) {
                        return;
                    }
                    LogMgr.e(String.format(Locale.US, "====>case %d: mValue[0]-->tempValue[%d] length:%d", index, subFunNodeJava.valStart, mValue.length));
                    System.arraycopy(mValue, 0, tempValue, subFunNodeJava.valStart, mValue.length);
                    funPos = (int) param1 * 40;
                    break;
                // 跳出子程序
                case 25:
                    LogMgr.e(String.format(Locale.US, "====>case %d: tempValue[%d]-->mValue[%d] length:%d", index, subFunNodeJava.valStart + gloableCount, gloableCount, mValue.length - gloableCount));
                    System.arraycopy(tempValue, subFunNodeJava.valStart + gloableCount, mValue, gloableCount, mValue.length - gloableCount);
                    int curFunNodePos = subFunNodeJava.pos;
                    if (!delFunNodeJava()) {
                        return;
                    }
                    funPos = curFunNodePos;
                    break;

                case 100:
                    //runmove 向前向后。//两个参数 一个前后一个速度。 bin文件参数有问题。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.e("control", "param1:" + param1 + " param2:" + param2);
                    explainerHelper.Mrunmove((int) param1, (int) param2);
                    break;

                case 101://runRotate 左转右转  转速。 两个参数。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    explainerHelper.MrunRotate((int) param1, (int) param2);
                    break;

                case 102://runwise 旋转+延时
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    explainerHelper.Mrunwise((int) param1, (int) param2);
                    waitExplain(param3);
                    explainerHelper.Mrunstop();
                    break;

                case 103:
                    //runstop  这个可以复用stopWheelMoto（0）
                    explainerHelper.Mrunstop();
                    break;

                case 104://runmotor  这个代码复用。 这个参数不对。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    explainerHelper.setVacuumPower((int) param1);
                    break;

                case 105://runmotorstop  //这个复用代码。
                    explainerHelper.setVacuumPower(0);
                    break;

                case 106://runneck1  上下         这个复用代码。参数不对。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("control", "SetNeckUPMotor-->" + "param1:" + param1);
                    explainerHelper.SetNeckUPMotor((int) param1);
                    break;

                case 107://runneck2 左右           复用代码。参数不对。
                    LogMgr.e("control", "左右脖子：--->SetNeckLRMotor");
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("control", "SetNeckLRMotor-->" + "param1:" + param1);
                    explainerHelper.SetNeckLRMotor((int) param1);
                    break;

                case 108:
                    explainerHelper.playSoundRandom();
                    break;

                case 109://stopsound
                    explainerHelper.stopPlaySound();
                    break;

                case 110://setled1 眼睛   眼睛要重写的。setled1(int color)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    explainerHelper.setEyeColorMode((int) param1);
                    break;

                case 111://setled2 脖子
                    //param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    //explainerHelper.Msetled2((int)param1);
                    //新增功能。
                    //第一个参数(0-3)代表{红绿蓝白} 第二个参数（0-3）代表{正弦，方波，常亮，常灭}
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.e("setled2 脖子" + "param1: " + param1 + "param2: " + param2);
                    explainerHelper.Msetled2((int) param1, (int) param2);
                    break;

                case 112://setled3 底部
                    //param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    //explainerHelper.Msetled3((int)param1);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.e("setled2 底部" + "param1: " + param1 + "param2: " + param2);
                    explainerHelper.Msetled3((int) param1, (int) param2);
                    break;

                case 113://setled4 轮子 led写完了。
                    //param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    //explainerHelper.Msetled4((int)param1);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.e("setled4 轮子" + "param1: " + param1 + "param2: " + param2);
                    explainerHelper.Msetled4((int) param1, (int) param2);
                    break;

                case 114: //bool findBarM(int)	判断前方是否有障碍物
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.findBarM((int) param1);
                    LogMgr.d("control 超声：--->findBarM：" + mValue[reValue] + "param1:" + param1);
                    break;

                case 115://int  findBarDistanceM(int)	探测障碍物距离
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getUltrasonic((int) param1);
                    LogMgr.d("control 超声：--->getUltrasonic：" + mValue[reValue] + "param1:" + param1);
                    break;

                case 116://findobject
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);//一个字节 0代表左前碰撞，1代表右前碰撞，2代表正前碰撞
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getCollision((int) param1);
                    LogMgr.d("control 碰撞：--->getCollision: " + mValue[reValue] + " param1:" + param1);
                    break;

                case 118://getgray   地面灰度有五个灯，所以有五个值。 这个要改。 那个比较参数没传进来。
                    LogMgr.d("control 地面灰度：--->getground_gray");
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int id = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.getGroundGray(id);
                    LogMgr.e(String.format(Locale.US, "control 地面灰度：--->getground_gray[%d] = %f", id, mValue[reValue]));
                    break;

                case 119://获取指南针角度。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getMcompass();
                    break;

                case 120://getGyro M的先这么做。
                    //上扬 下府，左翻右翻。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int jieguo = explainerHelper.getposM((int) param1);
                    mValue[reValue] = jieguo;
                    //param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    break;

                case 121://findfly  这个就是获取下视。一个字节 0代表左悬空，1代表右悬空，2代表左右悬空
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getDownLook((int) param1);
                    LogMgr.d("control 下视：--->getDownLook: " + mValue[reValue] + " param1:" + param1);
                    break;

                case 122: //microphone 麦克风录音功能。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("param1: " + param1);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param1);
                    pauseExplain();
                    break;

                case 123://时钟复位。
                    init_time = System.currentTimeMillis();
                    break;

                case 124://系统时间。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    end_time = System.currentTimeMillis();
                    Double b = ((double) (end_time - init_time) / 1000);
                    BigDecimal bd = new BigDecimal(b);
                    bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);//四舍五入。
                    b = bd.doubleValue();
                    mValue[reValue] = Float.parseFloat(b.toString());//double 转float
                    break;

                case 125://显示。
                    isDisplay = true;
                    String content = "";
                    LogMgr.e("runBuffer1::" + ByteUtils.bytesToString(runBuffer, runBuffer.length));
                    //因为scratch只显示一行。所以我就不用管换行了。
                    int isvariable = runBuffer[10];
                    if (isvariable == 0) {
                        //内容是11~29.
                        byte[] data_bytes = new byte[20];
                        System.arraycopy(runBuffer, 11, data_bytes, 0, 20);
                        LogMgr.e("data_bytes::"
                                + ByteUtils.bytesToString(data_bytes, data_bytes.length));
                        byte[] str_byte = null;
                        for (int m = 0; m < data_bytes.length; m++) {
                            if (data_bytes[m] != 0) {
                                str_byte = new byte[m + 1];
                                System.arraycopy(data_bytes, 0, str_byte, 0, m + 1);
                            } else {
                                break;
                            }
                        }
                        try {
                            //scratch 只需要显示1行。
                            content += new String(str_byte, "UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (isvariable == 1) {
                        float paramV = explainerHelper.getParam(runBuffer, mValue, 10);
                        if (Float.isNaN(paramV) || Float.isInfinite(paramV)) {
                            content = ExplainerApplication.instance.getString(R.string.guoda);
                        } else {
                            DecimalFormat df = new DecimalFormat("0.000");
                            content += df.format(paramV);
                        }
                    }
                    LogMgr.e("content::" + content);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    break;

                case 126://感受到触摸。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getTouchHead();
                    break;

                case 127://拍照。
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_CAMERA, 1);
                    pauseExplain();
                    break;

                case 128://校准指南针。
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_COMPASS);
                    pauseExplain();
                    break;

                case 129:
                    //扬声器模拟动物，此处与C代码一致，可以复用。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("M_setsound1 param1=： " + param1);
                    explainerHelper.playSound(AExplainHelper.SOUND_TYPE_ANIMAL, (int) param1);
                    break;

                case 130:
                    //扬声器模拟乐器。 跟C一样，借用代码。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("M_setsound2 param1=： " + param1);
                    explainerHelper.playSound(AExplainHelper.SOUND_TYPE_INSTRUMENT, (int) param1);
                    break;

                case 131:
                    //自我介绍不一样，要改掉。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("M_setsound3 param1=： " + param1);
                    explainerHelper.playSound(AExplainHelper.SOUND_TYPE_INTRODUCE, (int) param1);
                    break;

                case 132://bool getTouchM3S()	感受到触摸
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getTouchM3S((int) param1);
                    LogMgr.d("control 触摸：--->getTouchM3S: " + mValue[reValue] + " param1:" + param1);
                    break;

                case 133://bool findFlyM3S(int)	判断机器人是否悬挂
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getDownLookM3S((int) param1);
                    LogMgr.d("control 触摸：--->getDownLookM3S: " + mValue[reValue] + " param1:" + param1);
                    break;

                // 结束
                case 23:
                    LogMgr.d(" control vjc执行结束");
                    if (isDisplay) {
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                        isDisplay = false;
                    }
                    return;
            }
        } while (!isStop);
    }

    @Override
    public void stopExplain() {
        super.stopExplain();
        LogMgr.d("stop explain");
        explainerHelper.stopPlaySound();
        explainerHelper.startSleepStop();
    }

    @Override
    public void pauseExplain() {
        super.pauseExplain();
    }

    @Override
    public void resumeExplain() {
        super.resumeExplain();
    }
}
