package com.abilix.explainer.chart;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

import com.abilix.explainer.AExplainer;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.FunNode;
import com.abilix.brain.R;
import com.abilix.explainer.helper.C1ExplainerHelper;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.PlayerUtils;

import android.os.Handler;
import android.util.Log;

public class C1ChartExplainer extends AExplainer {
    protected C1ExplainerHelper explainerHelper;

    public C1ChartExplainer(Handler handler) {
        super(handler);
        explainerHelper = new C1ExplainerHelper();
    }

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

        // 添加两个时钟变量。赋初始值。
        long init_time = 0;
        long end_time = 0;
        init_time = end_time = System.currentTimeMillis();
        long displayTimeInit = System.currentTimeMillis();
        long displayTimeCurrent = System.currentTimeMillis();

        funPos = start;// 这里start是0
        do {
            if (doexplain == false) { // 文件解析结束后退出
                LogMgr.d("explain finish and exit");
                return;
            }
            explainerHelper.readFromFlash(filebuf, funPos, runBuffer, 40); // 获取40字节数据
            if (runBuffer[0] != 0x55 & runBuffer[1] != 0xaa) {
                LogMgr.e("explain file is not correct");
                return;
            }
            index = explainerHelper.getU16(runBuffer, 8); // 解析函数名对应的index
            funPos = explainerHelper.getU16(runBuffer, 34) * 40; // 解析跳转到哪一行
            LogMgr.d(String.format(Locale.US, "====>index:%d funPos:%d runBuffer:%s", index, funPos, ByteUtils.bytesToString(runBuffer, runBuffer.length)));
            switch (index) {

                case 0:// getName
                    byte name[] = new byte[20];
                    explainerHelper.getName(name, runBuffer);
                    String temp = new String(name);
                    break;
                case 3://公共函数 延时。
                    isSleep = true;
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    //explainerHelper.wait(param1);
                    LogMgr.e("time: " + param1);
                    if (param1 > 0) {
                        param1 = param1 * 1000f;
                        String strparam1 = String.valueOf((int) param1);
                        int sleepnum = (int) (Long.parseLong(strparam1) / 100);
                        int n = 0;
                        while (isSleep & n < sleepnum) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            n++;
                        }
                        displayTimeCurrent = System.currentTimeMillis();
                        if (displayTimeCurrent - displayTimeInit > 1000) {
                            sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                        } else {
                            //等待一会儿。
                            int part = (int) ((1000 - (displayTimeCurrent - displayTimeInit)) / 100);
                            int m = 0;
                            while (isSleep & m < part) {
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                m++;
                            }
                            sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                        }
                    }
                    break;
                case 8://所有AI
                    LogMgr.e("runBuffer::"
                            + ByteUtils.bytesToString(runBuffer, runBuffer.length));
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    // vjc pad端口号改成1到7
                    int nTemp = explainerHelper.ReadAIValue((int) param1);
                    LogMgr.e("position::" + reValue + "  " + "nTemp::" + nTemp);
                    if (nTemp < 20000) {
                        mValue[reValue] = nTemp;
                    }
                    break;
                case 13:
                    // 计算时间
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    end_time = System.currentTimeMillis();
                    Double b = ((double) (end_time - init_time) / 1000);
                    BigDecimal bd = new BigDecimal(b);
                    bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);//四舍五入。
                    b = bd.doubleValue();
                    mValue[reValue] = Float.parseFloat(b.toString());//double 转float
                    break;
                case 14:
                    // 复位时间
                    init_time = System.currentTimeMillis();
                    break;
                case 21:// my_Calc (运算符计算)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.e("param1:" + param1 + "   param2: " + param2 + " param3: " + param3);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.my_Calc(param1, param2,
                            (int) param3);
                    break;
                case 22:// my_jump(跳转)
                    if (gloableCount == -1) {
                        gloableCount = explainerHelper.getU16(runBuffer, 4) - 1;
                    }
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.e("param1:" + param1 + "   param2: " + param2 + " param3: " + param3);
                    funPos = (param1 > 0.5) ? (int) param2 : (int) param3;
                    funPos = funPos * 40;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    break;
                case 23:// return(程序结束)
                    displayTimeCurrent = System.currentTimeMillis();
                    if (displayTimeCurrent - displayTimeInit > 1000) {
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                    } else {
                        //等待一会儿。
                        int part = (int) ((1000 - (displayTimeCurrent - displayTimeInit)) / 100);
                        int m = 0;
                        while (isSleep & m < part) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            m++;
                        }
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                    }
                    return;

                case 24: // stepin(调用子程序)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    if (!addFunNodeJava(funPos)) {
                        return;
                    }
                    LogMgr.e(String.format(Locale.US, "====>case %d: mValue[0]-->tempValue[%d] length:%d", index, subFunNodeJava.valStart, mValue.length));
                    System.arraycopy(mValue, 0, tempValue, subFunNodeJava.valStart, mValue.length);
                    funPos = (int) param1 * 40;
                    break;

                case 25: // stepout(跳出子程序)
                    LogMgr.e(String.format(Locale.US, "====>case %d: tempValue[%d]-->mValue[%d] length:%d", index, subFunNodeJava.valStart + gloableCount, gloableCount, mValue.length - gloableCount));
                    System.arraycopy(tempValue, subFunNodeJava.valStart + gloableCount, mValue, gloableCount, mValue.length - gloableCount);
                    int curFunNodePos = subFunNodeJava.pos;
                    if (!delFunNodeJava()) {
                        return;
                    }
                    funPos = curFunNodePos;
                    break;
                case 28:
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    // LogMgr.e("param1::" + param1 + "  " + "param2::" + param2);
                    mValue[reValue] = (int) explainerHelper.getRandom((int) param1, (int) param2);
                    LogMgr.e("random response::" + mValue[reValue]);
                    break;
                case 31:// bigMoto
//				第一个参数：4字节，int类型，表示电机开闭环，0闭环，1开环。
//				第二个参数：4字节，int类型，表示设置电机运动类型。0：速度、1：角度、2：圈数、3：时间。*(程序中第一个参数取前2位，第二个参数取后两位，组成一个参数作为第一个参数)
//				第三个参数：4字节，int类型，表示数值。当电机运动类型为0时，表示设置速度，number模块隐藏；当为1时，表示设置角度，number模块显示，可输入角度数值 0-10000；当为2时，表示设置圈数，number模块显示，可输入圈数数值0-100；当type为3时，表示设置时间， number模块显示，可输入时间数值0-100s。
//				第四个参数：4字节，int类型，表示电机端口。0~3（A、B、C、D）。
//				第五个参数：4字节，int类型，表示电机速度（-100~100）。
                    param1 = explainerHelper.getMotoParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getMotoParam(runBuffer, mValue, 12);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param4 = explainerHelper.getParam(runBuffer, mValue, 20);
                    param5 = explainerHelper.getParam(runBuffer, mValue, 25);
                    LogMgr.e("param1::" + param1 + "  " + "param2::" + param2 +
                            "  " + "param3::" + param3 + "  "
                            + "param4::" + param4 + "  " + "param5::" + param5);
                    //开环是1，闭环是0.
                    if ((int) param1 == 1) {
                        //走开环
                        explainerHelper.motorChoice((int) param4, (int) param5);
                    } else {
                        //走闭环。
                        //explainerHelper.closeloop_control(type, port, num1, num2);   大电机最后一位是1.
                        explainerHelper.closeloop_control((int) param2, (int) param4, (int) param3, (int) param5, 1);
                    }

                    break;
                case 32:// smallMoto
                    param1 = explainerHelper.getMotoParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getMotoParam(runBuffer, mValue, 12);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param4 = explainerHelper.getParam(runBuffer, mValue, 20);
                    param5 = explainerHelper.getParam(runBuffer, mValue, 25);
                    if ((int) param1 == 1) {
                        //走开环
                        explainerHelper.motorChoice((int) param4, (int) param5);
                    } else {
                        //走闭环。
                        explainerHelper.closeloop_control((int) param2, (int) param4, (int) param3, (int) param5, 0);
                    }
                    break;
                case 33:
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.e("param1::" + param1 + "  " + "param2::" + param2);
                    explainerHelper.setButtonLight((int) param1, (int) param2);
                    break;
                case 34:
                    LogMgr.e("setSound runBuffer::"
                            + ByteUtils.bytesToString(runBuffer, runBuffer.length));
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.e("param1::" + param1 + "  " + "param2::" + param2);
                    explainerHelper.playSound((int) param1, (int) param2);
                    break;
                case 38:// getColor
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int[] color = new int[6];
                    for (int m = 0; m < 6; m++) {

                        color[m] = explainerHelper.getColorResponse((int) param1);
                    }
                    //这里写一个函数  传递一个颜色数组，返回一个颜色值。
                    mValue[reValue] = explainerHelper.getcolor(color);
                    LogMgr.e("value[reValue]::" + mValue[reValue]);
                    break;
                case 52://麦克风。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param1);
                    //waitExplain(param1);
                    pauseExplain();
                    break;
                case 53://拍照没有。
                    break;
                case 54://陀螺仪。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);

                    mValue[reValue] = (int) explainerHelper
                            .getGyro(param1, param2);
                    Log.e("value is ", "" + mValue[reValue]);
                    break;
                case 55:
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getCompass();
                    LogMgr.e("explainerHelper.getCompass() value is: "
                            + explainerHelper.getCompass());

                    break;
                case 70:// setDisplay
                    String content = "";
                    LogMgr.e("runBuffer1::"
                            + ByteUtils.bytesToString(runBuffer, runBuffer.length));
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    int data_lines = (int) param1;//显示在哪一行
                    LogMgr.e("data_lines::" + data_lines);
                    for (int k = 0; k < data_lines; k++) {
                        funPos = explainerHelper.getU16(runBuffer, 34) * 40;
                        explainerHelper.readFromFlash(filebuf, funPos, runBuffer,
                                40);
                        byte[] data_bytes = new byte[20];
                        System.arraycopy(runBuffer, 11, data_bytes, 0, 20);
                        // isvariable = 0 变量 ， isvariable = 1引用变量
                        int isvariable = runBuffer[10];

                        byte[] str_byte = null;
                        if (isvariable == 0) {
                            for (int l = 0; l < data_bytes.length; l++) {
                                if (data_bytes[l] == 0) {
                                    str_byte = new byte[l];
                                    System.arraycopy(data_bytes, 0, str_byte, 0, l);
                                    break;
                                }
                                if (l == 19) {
                                    str_byte = data_bytes;
                                }
                            }
                            try {
                                content += new String(str_byte, "UTF-8") + "\n";
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (isvariable == 1) {
                            float paramV = explainerHelper.getParam(runBuffer,
                                    mValue, 10);
                            LogMgr.e("paramV: " + paramV);

                            if (Float.isNaN(paramV) || Float.isInfinite(paramV)) {
                                LogMgr.e("paramV: " + paramV);
                                content += ExplainerApplication.instance.getString(R.string.guoda) + "\n";

                            } else {
                                DecimalFormat df = new DecimalFormat("0.000");
                                content += df.format(paramV) + "\n";
                            }
                        }

                        if (k == 7) {
                            funPos = explainerHelper.getU16(runBuffer, 34) * 40;
                            explainerHelper.readFromFlash(filebuf, funPos,
                                    runBuffer, 40);
                        }
                    }
                    LogMgr.e("content::" + content);
                    displayTimeInit = System.currentTimeMillis();
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    break;

                case 110://设置所有大电机

                    byte[] isSet = new byte[2];//是否设置
                    byte[] portType = new byte[2];//开闭环
                    byte[] numtype = new byte[2];//速度，角度，圈数，时间。
                    byte[] numIsquote = new byte[2];
                    byte[] suduIssquote = new byte[2];
                    int[] TandAngle = new int[2];//时间或者角度的数值。
                    int[] sudu = new int[2];

                    isSet[0] = (byte) ((runBuffer[11] >> 7) & 0x01);
                    isSet[1] = (byte) ((runBuffer[12] >> 7) & 0x01);

                    portType[0] = (byte) ((runBuffer[11] >> 6) & 0x01);//开闭环
                    portType[1] = (byte) ((runBuffer[12] >> 6) & 0x01);


                    numtype[0] = (byte) ((runBuffer[11] >> 2) & 0x0f);//速度，角度，圈数，时间。
                    numtype[1] = (byte) ((runBuffer[12] >> 2) & 0x0f);

                    numIsquote[0] = (byte) ((runBuffer[11] >> 1) & 0x01);//AB电机的速度，角度，圈数，时间，是否是引用变量
                    numIsquote[1] = (byte) ((runBuffer[12] >> 1) & 0x01);

                    suduIssquote[0] = (byte) ((runBuffer[11]) & 0x01);//速度是否是引用变量。
                    suduIssquote[1] = (byte) ((runBuffer[12]) & 0x01);

                    TandAngle[0] = ((runBuffer[16] & 0xff) << 8) + (runBuffer[17] & 0xff);//高位在前低位在后。
                    TandAngle[1] = ((runBuffer[18] & 0xff) << 8) + (runBuffer[19] & 0xff);

                    sudu[0] = (runBuffer[26] & 0xff);
                    sudu[1] = (runBuffer[27] & 0xff);
                    //这里开始赋值。
                    for (int k = 0; k < 2; k++) {
                        if (isSet[k] == 1) {//端口设置的话。
                            if (numIsquote[k] == 1) {//数据是引用变量的话

                                TandAngle[k] = (int) explainerHelper.getMvalue(mValue, TandAngle[k]);
                            }
                            if (suduIssquote[k] == 1) {

                                sudu[k] = (int) explainerHelper.getMvalue(mValue, sudu[k]);
                            }
                        }
                    }
                    explainerHelper.setNewAllMotor(isSet, portType, numtype, TandAngle, sudu, 1);
                    break;


                case 111://设置所有小电机
                    byte[] isSetSmall = new byte[2];//是否设置
                    byte[] portTypeSmall = new byte[2];//开闭环
                    byte[] numtypeSmall = new byte[2];//速度，角度，圈数，时间。
                    byte[] numIsquoteSmall = new byte[2];
                    byte[] suduIssquoteSmall = new byte[2];
                    int[] TandAngleSmall = new int[2];//时间或者角度的数值。
                    int[] suduSmall = new int[2];

                    isSetSmall[0] = (byte) ((runBuffer[11] >> 7) & 0x01);
                    isSetSmall[1] = (byte) ((runBuffer[12] >> 7) & 0x01);

                    portTypeSmall[0] = (byte) ((runBuffer[11] >> 6) & 0x01);//开闭环
                    portTypeSmall[1] = (byte) ((runBuffer[12] >> 6) & 0x01);


                    numtypeSmall[0] = (byte) ((runBuffer[11] >> 2) & 0x0f);//速度，角度，圈数，时间。
                    numtypeSmall[1] = (byte) ((runBuffer[12] >> 2) & 0x0f);

                    numIsquoteSmall[0] = (byte) ((runBuffer[11] >> 1) & 0x01);//AB电机的速度，角度，圈数，时间，是否是引用变量
                    numIsquoteSmall[1] = (byte) ((runBuffer[12] >> 1) & 0x01);

                    suduIssquoteSmall[0] = (byte) ((runBuffer[11]) & 0x01);//速度是否是引用变量。
                    suduIssquoteSmall[1] = (byte) ((runBuffer[12]) & 0x01);

                    TandAngleSmall[0] = ((runBuffer[16] & 0xff) << 8) + (runBuffer[17] & 0xff);//高位在前低位在后。
                    TandAngleSmall[1] = ((runBuffer[18] & 0xff) << 8) + (runBuffer[19] & 0xff);

                    suduSmall[0] = (runBuffer[26] & 0xff);
                    suduSmall[1] = (runBuffer[27] & 0xff);
                    //这里开始赋值。
                    for (int k = 0; k < 2; k++) {
                        if (isSetSmall[k] == 1) {//端口设置的话。
                            if (numIsquoteSmall[k] == 1) {//数据是引用变量的话

                                TandAngleSmall[k] = (int) explainerHelper.getMvalue(mValue, TandAngleSmall[k]);
                            }
                            if (suduIssquoteSmall[k] == 1) {

                                suduSmall[k] = (int) explainerHelper.getMvalue(mValue, suduSmall[k]);
                            }
                        }
                    }
                    explainerHelper.setNewAllMotor(isSetSmall, portTypeSmall, numtypeSmall, TandAngleSmall, suduSmall, 0);
                    break;
                case 113:
                    explainerHelper.stopPlaySound();
                    break;

                case 119: //void setSoundNew(char *) 扬声器
                    /*char*类型，表示音频文件名（N<20字节）
                    附：STM32端内置音频命名规则——中文拼音_语言+下标
                    例：中文“打招呼”下的“你好”——dazhaohu_c0
                    英文“打招呼”下的“欢迎”——dazhaohu_e3
                    公共“交通”下的“赛车” —— jiaotong_p1
                    公共“动物”下的“狗” —— dongwu_p3*/
                    String soundName = explainerHelper.getString(runBuffer, 11);
                    final int isNeedWaiting = runBuffer[29] & 0xFF;
                    LogMgr.d("soundName:" + soundName + " isNeedWaiting: " + isNeedWaiting);
                    explainerHelper.playSound(soundName, new PlayerUtils.OnCompletionListener() {
                        @Override
                        public void onCompletion(int state) {
                            LogMgr.d("onCompletion() state: " + state);
                            if (isNeedWaiting == 1) {
                                resumeExplain();
                            }
                        }
                    });
                    if (isNeedWaiting == 1) {
                        pauseExplain();
                    }
                    break;

                default: // 可捕获未解释执行的函数
                    strInfo += (" -- default(未处理): " + index + "   " + funPos);
                    LogMgr.e("error::" + "no index");
                    return;
            }

            if (len == funPos) { // 判断程序结束
                sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                LogMgr.e("explain file end");
                explainerHelper.reSetStm32();
                doexplain = false;
            }

        } while (!isStop);

    }

    @Override
    public void stopExplain() {
        super.stopExplain();
        explainerHelper.stopPlaySound();
        explainerHelper.setCompassCheck(false);//退出指南针校准等待。
        explainerHelper.setButtonLight(0, 0);
        explainerHelper.cleantimer();
        //这个向STM32发一个重置命令。
        explainerHelper.reSetStm32();
    }

    @Override
    public void pauseExplain() {
        super.pauseExplain();
    }


    @Override
    public void resumeExplain() {
        super.resumeExplain();
    }

    @Override
    public void waitExplain(float time) {
        super.waitExplain(time);
    }

}
