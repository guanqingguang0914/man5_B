package com.abilix.explainer.chart;

import android.os.Handler;

import com.abilix.explainer.AExplainer;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.FunNode;
import com.abilix.brain.R;
import com.abilix.explainer.balancecar.BalanceCarData;
import com.abilix.explainer.helper.C9ExplainerHelper;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.PlayerUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

public class C9ChartExplainer extends AExplainer {
    private BalanceCarData balancecarData = null;
    protected C9ExplainerHelper explainerHelper;

    public C9ChartExplainer(Handler handler) {
        super(handler);
        explainerHelper = new C9ExplainerHelper();
    }

    @Override
    public void doExplain(String filePath) {
        super.doExplain(filePath);
        LogMgr.d("start explain vjc file");
        // 文件总长度
        int len = filebuf.length;
        subFunNodeJava = new FunNode();
        int funPos = 0;
        byte runBuffer[] = new byte[packetDataLength];
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

        funPos = start;// 这里start是0
        explainerHelper.setIswait(true);

        do {
            if (!doexplain) { // 文件解析结束后退出
                LogMgr.d("explain finish and exit");
                return;
            }
            explainerHelper.readFromFlash(filebuf, funPos, runBuffer, packetDataLength); // 获取40字节数据
            /*if (runBuffer[0] != 0x55 & runBuffer[1] != 0xaa) {
                LogMgr.e("explain file is not correct");
                return;
            }*/
            index = explainerHelper.getU16(runBuffer, 8); // 解析函数名对应的index
            funPos = explainerHelper.getU16(runBuffer, packetDataLine1Index) * packetDataLength; // 解析跳转到哪一行
            LogMgr.d(String.format(Locale.US, "====>index:%d funPos:%d runBuffer:%s", index, funPos, ByteUtils.bytesToString(runBuffer, runBuffer.length)));

            switch (index) {
                case 0: //getName
                    byte name[] = new byte[20];
                    explainerHelper.getName(name, runBuffer);
                    //String temp = new String(name);
                    break;

                case 1: // SetMoto
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    explainerHelper.setMotorSpeedOpenloopC9((int) param1, (int) param2);  //这里改为新协议。 一个端口，一个速度。
                    break;

                case 2: // Printf
                    int i = 0;
                    int j = 32;
                    byte[] str = new byte[200];
                    float[] valData = new float[100];// = {0};
                    for (int a = 0; a < 100; a++) {
                        valData[a] = 0.0f;
                    }
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    for (int a = 0; a < 200; a++) {
                        str[a] = 0x00;
                    }

                    for (i = 0; i < (int) param1; ++i) {
                        if (j >= 32) {
                            j = 10;
                            explainerHelper.readFromFlash(filebuf, funPos,
                                    runBuffer, packetDataLength);
                            funPos = explainerHelper.getU16(runBuffer, packetDataLine1Index) * packetDataLength;
                        }
                        str[i] = runBuffer[j++];
                    }
                    for (int i11 = 0; i11 < 100000000; i11++) {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for (i = 0, j = 30; i < (int) param2; ++i) {
                        if (j > 25) {
                            j = 10;
                            explainerHelper.readFromFlash(filebuf, funPos,
                                    runBuffer, packetDataLength);
                            funPos = explainerHelper.getU16(runBuffer, packetDataLine1Index) * packetDataLength;
                        }
                        valData[i] = explainerHelper
                                .getParam(runBuffer, mValue, j);
                        j = j + 5;
                    }
                    strInfo += (" -- printf: " + param1 + "   " + param2);
                    break;

                case 3: // wait
                    isSleep = true;
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    waitExplain(param1);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                    break;

                case 8: // AI
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    // vjc pad端口号改成1到7
                    mValue[reValue] = explainerHelper.ReadAIValue((int) param1 - 1);
                    LogMgr.d("reValue::" + reValue + "  " + "ReadAIValue() = " + mValue[reValue]);
                    break;

                case 9: // DI
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    mValue[reValue] = explainerHelper.DI((int) param1);
                    break;
                case 13:
                    // 计算时间
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    end_time = System.currentTimeMillis();
                    //这里保留一位小数。-----------------------------------------------------------------------------------------
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

                case 21: // my_Calc (运算符计算)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);

                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    mValue[reValue] = explainerHelper.my_Calc(param1, param2, (int) param3);
                    break;

                case 22: // my_jump(跳转)
                    if (gloableCount == -1) {
                        gloableCount = explainerHelper.getU16(runBuffer, 4) - 1;
                    }
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    funPos = (param1 > 0.5) ? (int) param2 : (int) param3;
                    funPos = funPos * packetDataLength;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    break;

                case 23: // return(程序结束)
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                    return;

                case 24: // stepin(调用子程序)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    if (!addFunNodeJava(funPos)) {
                        return;
                    }
                    LogMgr.e(String.format(Locale.US, "====>case %d: mValue[0]-->tempValue[%d] length:%d", index, subFunNodeJava.valStart, mValue.length));
                    System.arraycopy(mValue, 0, tempValue, subFunNodeJava.valStart, mValue.length);
                    funPos = (int) param1 * packetDataLength;
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

                case 28:// getRandom
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    mValue[reValue] = (int) explainerHelper.getRandom((int) param1, (int) param2);
                    LogMgr.d("random response::" + mValue[reValue]);
                    break;

                case 29:// getImage
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    mValue[reValue] = explainerHelper.getImgResponse((int) param1, (int) param2);
                    LogMgr.d("img response::" + mValue[reValue]);
                    break;

                case 30:// getVoice
                    LogMgr.d("getVoice");
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    break;

                case 31:// bigMoto
                case 32:// smallMoto
                    int motor_type = index == 31 ? 1 : 0;//电机类型：1为大电机，0为小电机；
                    int loopTypeBigMotor = explainerHelper.getMotoParam(runBuffer, mValue, 10);//表示电机开闭环，0闭环，1开环
                    int typeBigMotor = explainerHelper.getMotoParam(runBuffer, mValue, 12);//表示设置电机运动类型。0：速度、1：角度、2：圈数、3：时间
                    int valueBigMotor = (int) explainerHelper.getParam(runBuffer, mValue, 15);//表示数值
                    int idSetBigMotor = (int) explainerHelper.getParam(runBuffer, mValue, 20);//表示电机端口。0~3（A、B、C、D、E）
                    int speedBigMotor = (int) explainerHelper.getParam(runBuffer, mValue, 25);//表示电机速度（-100~100）
                    LogMgr.d(String.format(Locale.US, "motor_type:%d loopTypeBigMotor:%d typeBigMotor:%d valueBigMotor:%d idSetBigMotor:%d speedBigMotor%d",
                            motor_type, loopTypeBigMotor, typeBigMotor, valueBigMotor, idSetBigMotor, speedBigMotor));
                    byte[] motor_set_bytes = new byte[5];
                    byte[] motor_value_bytes = new byte[10];
                    byte[] motor_speed_bytes = new byte[5];
                    byte motor_setByte = 0x00;
                    motor_setByte |= 0x01 << 7;//端口是否设置(0-不设置，1-设置，占1位)
                    motor_setByte |= (loopTypeBigMotor & 0x01) << 6;//电机类型(0-闭环，1-开环，占1位)
                    motor_setByte |= (typeBigMotor & 0x0F) << 5;//数值类型(0-速度，1-角度，2-圈数，3-时间，占4位)
                    motor_setByte |= (runBuffer[15] & 0x01) << 1;//数值是否引用变量(占1位)
                    motor_setByte |= runBuffer[20] & 0x01;//速度是否引用变量(占1位)
                    motor_set_bytes[idSetBigMotor] = motor_setByte;
                    motor_value_bytes[idSetBigMotor * 2] = (byte) (valueBigMotor >> 8 & 0xFF);
                    motor_value_bytes[idSetBigMotor * 2 + 1] = (byte) (valueBigMotor & 0xFF);
                    motor_speed_bytes[idSetBigMotor] = (byte) (speedBigMotor & 0xFF);
                    explainerHelper.setAllMotor(motor_set_bytes, motor_value_bytes, motor_speed_bytes, motor_type, mValue);
                    break;

                case 33:// 灯
                    // 两个参数。为什么是两个参数。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d("setButtonLight(" + param1 + ", " + param2 + ")");
                    explainerHelper.setButtonLight((int) param1, (int) param2);
                    break;

                case 34:// setSound
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d("playSound(" + param1 + ", " + param2 + ")");
                    explainerHelper.playSound((int) param1, (int) param2);
                    break;

                case 38:// getColor
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    int[] color = new int[6];
                    for (int m = 0; m < 6; m++) {
                        color[m] = explainerHelper.getColor((int) param1);
                    }
                    //这里写一个函数  传递一个颜色数组，返回一个颜色值。
                    mValue[reValue] = explainerHelper.getcolor(color);
                    LogMgr.d("getColor(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 52://麦克风。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param1);
                    LogMgr.d("setRecord(" + param1 + ")");
                    pauseExplain();
                    break;

                case 53: //拍照。
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_CAMERA, 1);
                    LogMgr.d("takePicture(" + param1 + ")");
                    pauseExplain();
                    break;

                case 54:
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    mValue[reValue] = (int) explainerHelper.getGyro(param1, param2);
                    LogMgr.d("getGyro(" + param1 + ", " + param2 + ") = " + mValue[reValue]);
                    break;

                case 39: //指南针。
                case 55:
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    mValue[reValue] = explainerHelper.getCompass();
                    LogMgr.d("getCompass() = " + mValue[reValue]);
                    break;

                case 58: //
                    int ivoice = 0;
                    int jvoice = 32;
                    byte[] strvoice = new byte[200];
                    float[] valDatavoice = new float[100];// = {0};
                    for (int a = 0; a < 100; a++) {
                        valDatavoice[a] = 0.0f;
                    }
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    for (int a = 0; a < 200; a++) {
                        strvoice[a] = 0x00;
                    }

                    for (ivoice = 0; ivoice < (int) param1; ++ivoice) {
                        if (jvoice >= 32) {
                            jvoice = 10;
                            explainerHelper.readFromFlash(filebuf, funPos,
                                    runBuffer, packetDataLength);
                            funPos = explainerHelper.getU16(runBuffer, packetDataLine1Index) * packetDataLength;
                        }
                        strvoice[ivoice] = runBuffer[jvoice++];
                    }
                    for (i = 0; i < 100000000; i++) {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for (ivoice = 0, jvoice = 30; ivoice < (int) param2; ++ivoice) {
                        if (jvoice > 25) {
                            jvoice = 10;
                            explainerHelper.readFromFlash(filebuf, funPos,
                                    runBuffer, packetDataLength);
                            funPos = explainerHelper.getU16(runBuffer, packetDataLine1Index) * packetDataLength;
                        }
                        valDatavoice[ivoice] = explainerHelper.getParam(runBuffer,
                                mValue, jvoice);
                        jvoice = jvoice + 5;
                    }
                    break;

                case 70:// setDisplay
                    String content = "";
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    int data_lines = (int) param1;//显示在哪一行
                    LogMgr.e("data_lines::" + data_lines);
                    for (int k = 0; k < data_lines; k++) {
                        funPos = explainerHelper.getU16(runBuffer, packetDataLine1Index) * packetDataLength;
                        explainerHelper.readFromFlash(filebuf, funPos, runBuffer,
                                packetDataLength);
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
                            funPos = explainerHelper.getU16(runBuffer, packetDataLine1Index) * packetDataLength;
                            explainerHelper.readFromFlash(filebuf, funPos,
                                    runBuffer, packetDataLength);
                        }
                    }
                    LogMgr.d("display content::" + content);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    break;

                //寻线模块库。
                case 100://初始化。这里需要解析参数。---------------------------------------------------------------------ok
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);//左马达功率
                    param1 += 1;
                    param1 *= 100;
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);//右马达功率。
                    param2 += 1;
                    param2 *= 100;
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);//阈值偏移。
                    param3 = param3 * 100;
                    //接下来就是按位解析。26~29  四个字节的解析。
                    byte[] temp = new byte[4];
                    System.arraycopy(runBuffer, 26, temp, 0, temp.length);
                    /*public void init_7 ( int DC_L, int mDC_L, int DC_R, int mDC_R, int IO1,
                    int IO2, int IO3, int IO4, int IO5, int IO6, int IO7, int line,
                    int offset, int type){
                    通道1(占3位) - 通道2(占3位) - 闭环电机(占1位) - 电机选择(0:大电机；1:小电机；占1位);
                    通道3(占3位) - 通道4(占3位) - 左马达通道(占2位);
                    通道5(占3位) - 通道6(占3位) - 右马达通道(占2位);
                    通道7(占3位) - 地面灰度数量(占1位) - 场地类型(占1位) - (空3位) 地面灰度数量是没有的。*/

                    //init_7(0, 0, 1, 0, 0,1, 2, 3, 4, 5, 6, 0, 50, 0);  测试参数。
                    int IO1, IO2, IO3, IO4, IO5, IO6, IO7, type, DC_L, DC_R, line, num, bigorSmall;

                    IO1 = ((temp[0] >> 5) & 0x07) - 1;
                    IO2 = ((temp[0] >> 2) & 0x07) - 1;
                    type = (temp[0] >> 1) & 0x01;//一定要保证这里是开环。 0
                    bigorSmall = temp[0] & 0x01;
                    type = switchint(type);
                    IO3 = ((temp[1] >> 5) & 0x07) - 1;
                    IO4 = ((temp[1] >> 2) & 0x07) - 1;
                    DC_L = temp[1] & 0x03;
                    IO5 = ((temp[2] >> 5) & 0x07) - 1;
                    IO6 = ((temp[2] >> 2) & 0x07) - 1;
                    DC_R = temp[2] & 0x03;
                    IO7 = ((temp[3] >> 5) & 0x07) - 1;
                    num = (temp[3] >> 4) & 0x01;
                    line = (temp[3] >> 3) & 0x01;
                    //第五个参数：（仅用于C9) 第一个字节：左电机端口 取值范围0-4（前四位）右电机端口 取值范围0-4（后四位）
                    DC_L = runBuffer[31] >> 4 & 0x0F;
                    DC_R = runBuffer[31] & 0x0F;
                    LogMgr.e("bigorSmall:" + bigorSmall);
                    LogMgr.e("DC_L:" + DC_L + "," + "mDC_L: " + param1 + "," + "DC_R" + DC_R + "," + "mDC_R" + param2 + "line: " + line + "type" + type);//这里bin文件就是1.
                    //这里先把这些写入配置文件里。
                    String io_config = num + "," + IO1 + "," + IO2 + "," + IO3 + "," + IO4 + "," + IO5 + "," + IO6 + "," + IO7 + ",";//这就是配置文件。

                    FileUtils.saveFile(io_config, FileUtils.IO_CONFIG);
                    bigorSmall = (bigorSmall == 0) ? 1 : 0; //CHART 0:大电机；1:小电机<=ERROR=>STM32 0x00表示小电机，0x01表示大电机
                    if (num == 1) {
                        explainerHelper.init_7(DC_L, (int) param1, DC_R, (int) param2, IO1, IO2, IO3, IO4, IO5, IO6, IO7, line, (int) param3, type, bigorSmall);
                    } else {
                        //这里把2,4 去掉。
                        explainerHelper.init_5(DC_L, (int) param1, DC_R, (int) param2, IO1, IO3, IO4, IO5, IO7, line, (int) param3, type, bigorSmall);
                    }
                    explainerHelper.waitReveice();// init 之后都需要等待回复。
                    explainerHelper.readandsend(num);
                    //这里应该卡着它一直在读。
                    explainerHelper.waitReveice();
                    break;

                case 102://路口寻线  ---------------------------------------------------------------------------------------------ok
                    /*第一个参数：路口类型(0-左侧路口   1-右侧路口)
                    第二个参数：第一个字节:巡线速度(10~100)；第二个字节:左转差速(0-100)；第三个字节:右转差速(0-100)；第四个字节:结束后是否停车(1-是 0-否)；
                    第三个参数：循环次数1-4095
                    第四个参数：冲过路口时间(0-60.000)*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);//路口类型。
                    param2 = explainerHelper.getParam(runBuffer, mValue, 20);//循环次数
                    param3 = explainerHelper.getParam(runBuffer, mValue, 25);//冲过路口时间。
                    //这里按字节解析第二个参数。
                    int speed, Lcut, Rcut, stop;  //16  17  18   19.
                    speed = runBuffer[16] & 0xff;
                    Lcut = runBuffer[17] & 0xff;
                    Rcut = runBuffer[18] & 0xff;
                    stop = runBuffer[19] & 0xff;
                    //stop = switchint(stop);
                    LogMgr.e("speed:" + speed + "," + "Lcut:" + Lcut + "," + "Rcut:" + Rcut + "," + "路口类型：" + param1 + "循环次数：" + param2 + "时间：" + param3);

                    explainerHelper.line_road((int) param2, (int) param1, speed, Lcut, Rcut, stop, param3);
                    explainerHelper.waitReveice();
                    break;

                case 103://按时寻线-----------------------------------------------------------------------------------------------OK
                    /*第一个参数：第一个字节:巡线速度(10~100)(默认值：10；)；第二个字节:左转差速(0-100)；第三个字节:右转差速(0-100)；第四个字节:结束后是否停车(1-是 0-否)；
                    第二个参数：巡线时间(0-60.000)*/
                    int T_speed, T_Lcut, T_Rcut, T_stop; //11,12,13,14.
                    T_speed = runBuffer[11] & 0xff;
                    T_Lcut = runBuffer[12] & 0xff;
                    T_Rcut = runBuffer[13] & 0xff;
                    T_stop = runBuffer[14] & 0xff;
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);//时间。
                    LogMgr.e("time is: " + param2);
                    explainerHelper.LineWay_T(T_speed, T_Lcut, T_Rcut, T_stop, param2);
                    explainerHelper.waitReveice();
                    break;

                case 104://高级寻线
                    /*第一个参数：第一个字节:巡线速度(10~100)(默认值：10；)；第二个字节:左转差速(0-100)；第三个字节:右转差速(0-100)；第四个字节:结束后是否停车(1-是 0-否)；
                    第二个参数：第一个字节:传感器端口(0-6)；第二个字节:比较符（比较符取值范围：0:<;1:<=;2:==;3:!=;4:>=;5:>;）；
                    第三个参数：参考值*/
                    int O_speed, O_Lcut, O_Rcut, O_stop, O_IO, O_operator, reference;

                    O_speed = runBuffer[11] & 0xff;
                    O_Lcut = runBuffer[12] & 0xff;
                    O_Rcut = runBuffer[13] & 0xff;
                    O_stop = runBuffer[14] & 0xff;

                    O_IO = runBuffer[16] & 0xff;
                    O_operator = runBuffer[17] & 0xff;
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);//参考值。
                    LogMgr.e("高级寻线：" + "O_IO:" + O_IO + " O_operator " + O_operator + "param3: " + param3 + " O_speed: " + O_speed + " O_Lcut: " + O_Lcut + " O_Rcut:" + O_Rcut + " O_stop:" + O_stop);
                    explainerHelper.LineWay_O(O_IO, O_operator, (int) param3, O_speed, O_Lcut, O_Rcut, O_stop);
                    explainerHelper.waitReveice();
                    break;

                case 105://转弯------------------------------------------------------------------------ok
                    /*第一个参数：过线条数；(1-100))
                    第二个参数：左马达速度；(-100-100)
                    第三个参数：右马达速度；(-100-100)
                    第四个参数：第一个字节:结束位置(0-偏左 1-中间 2-偏右)；第二个字节:结束后是否停车(1-是 0-否)；*/
                    //我传到底层的速度是0~200，也就是加100就好了。
                    int speed_L, speed_R, A_stop, N, P;
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);//过线条数N。
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);//左马达速度。
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);//右马达速度。

                    P = runBuffer[26] & 0xff;
                    A_stop = runBuffer[27] & 0xff;
                    //A_stop = switchint(A_stop);  转弯这里VJC已经改好了，不用转化。
                    param2 += 100;
                    param3 += 100;//我传到底层是0~200.
                    LogMgr.e("Lspeed:" + param2 + "Rspeed" + param3 + "P=" + P);
                    explainerHelper.Around((int) param2, (int) param3, A_stop, (int) param1, P);
                    explainerHelper.waitReveice();
                    break;

                case 106://启动马达。
                    /*第一个参数: 第一个字节：结束标志(0-延时 1-传感器)；第二个字节：端口(0-6)（仅对传感器有效）；第三个字节：比较符（比较符取值范围：0:<;1:<=;2:==;3:!=;4:>=;5:>;（仅对传感器有效））；第四个字节：结束后是否停车(1-是 0-否)。
                    第二个参数: 结束标志数值（延时-时间  传感器触发-次数）--占4字节。
                    第三个参数: 左马达功率(-100~100 默认值0)----------占4字节。
                    第四个参数: 右马达功率(-100~100 默认值0)----------占4字节*/
                    int IO, S_operator, S_reference, S_speed_L, Rspeed, S_stop, YN;
                    YN = runBuffer[11] & 0xff; //启动方式。
                    IO = runBuffer[12] & 0xff;
                    S_operator = runBuffer[13] & 0xff;
                    S_stop = runBuffer[14] & 0xff;
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);//时间time  阈值。
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);//左马达速度。
                    param4 = explainerHelper.getParam(runBuffer, mValue, 25);//右马达速度。
                    //这里要0~200的速度范围。
                    param3 += 100;
                    param4 += 100;

                    if (YN == 0) {
                        LogMgr.e("启动马达： " + "启动方式yanshi " + "param3" + param3 + "param4: " + param4 + "time:" + param2);
                        explainerHelper.motor_statr((int) param3, (int) param4, param2);
                    } else {

                        LogMgr.e("启动马达： " + "huidu " + "IO" + IO + "S_operator: " + S_operator + "param2:" + param2 + "param3: " + param3 + "param4" + param4 + "S_stop:" + S_stop);
                        explainerHelper.WER_SetMotor_L(IO, S_operator, (int) param2, (int) param3, (int) param4, S_stop);

                    }
                    explainerHelper.waitReveice();
                    break;

                case 107://环境采集
                    explainerHelper.Motor_stop();
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_IS_GETAI);
                    explainerHelper.waitReveice();
                    break;

                case 108://进退平衡车模式
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);//进入还是退出
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);//平衡车类型
                    balancecarData = BalanceCarData.GetManger(mHandler, 2);
                    balancecarData.InitBalanceCar((int) param1, (int) param2);
                    LogMgr.d("11111balanceCar 00000 = ");
                    waitExplain(10);
                    break;

                case 109://设置平衡车运动
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);//0停止，1前进，2后退，3左转，4右转
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);//左轮速度值，范围： 0~200
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);//右轮速度值，范围： 0~200
                    balancecarData = BalanceCarData.GetManger(mHandler, 2);
                    balancecarData.SetBalanceCar((int) param1, (int) param2, (int) param3);
                    LogMgr.d("11111balanceCar 111111111 = ");
                    break;

                case 110://设置所有大电机
                case 123://设置所有大电机
                    byte[] set_bytes = new byte[5];
                    System.arraycopy(runBuffer, 11, set_bytes, 0, 4);
                    System.arraycopy(runBuffer, 16, set_bytes, 4, 1);
                    byte[] value_bytes = new byte[10];
                    System.arraycopy(runBuffer, 21, value_bytes, 0, 4);
                    System.arraycopy(runBuffer, 26, value_bytes, 4, 4);
                    System.arraycopy(runBuffer, 31, value_bytes, 8, 2);
                    byte[] speed_bytes = new byte[5];
                    System.arraycopy(runBuffer, 36, speed_bytes, 0, 4);
                    System.arraycopy(runBuffer, 41, speed_bytes, 4, 1);
                    explainerHelper.setAllMotor(set_bytes, value_bytes, speed_bytes, 1, mValue);
                    break;

                case 111://设置所有小电机
                case 124://设置所有小电机
                    byte[] sm_set_bytes = new byte[5];
                    System.arraycopy(runBuffer, 11, sm_set_bytes, 0, 4);
                    System.arraycopy(runBuffer, 16, sm_set_bytes, 4, 1);
                    byte[] sm_value_bytes = new byte[12];
                    System.arraycopy(runBuffer, 21, sm_value_bytes, 0, 4);
                    System.arraycopy(runBuffer, 26, sm_value_bytes, 4, 4);
                    System.arraycopy(runBuffer, 31, sm_value_bytes, 8, 2);
                    byte[] sm_speed_bytes = new byte[5];
                    System.arraycopy(runBuffer, 36, sm_speed_bytes, 0, 4);
                    System.arraycopy(runBuffer, 41, sm_speed_bytes, 4, 1);
                    explainerHelper.setAllMotor(sm_set_bytes, sm_value_bytes, sm_speed_bytes, 0, mValue);
                    break;

                case 112: //指南针校准。
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_COMPASS);
                    pauseExplain();
                    break;

                case 113://停止播放。-----新增停止播放模块。
                    explainerHelper.stopPlaySound();
                    break;

                case 114:
                    //光敏传感器
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    mValue[reValue] = explainerHelper.getLightForceValue((int) param1);
                    LogMgr.d("getLightForceValue() = " + mValue[reValue]);
                    break;

                case 115:
                    //void SetDO(int)数字输出   经确认不支持引用变量
                    /*第一个参数：(顺序：由高位到低位)
                     第1字节：通道1是否设置(0-不设置，1-设置，占1位)-通道1是否接通(0-关闭，1-接通，占1位)-通道2是否设置(0-不设置，1-设置，占1位)-通道2是否接通(0-关闭，1-接通，占1位)-
                             通道3是否设置(0-不设置，1-设置，占1位)-通道3是否接通(0-关闭，1-接通，占1位)-通道4是否设置(0-不设置，1-设置，占1位)-通道4是否接通(0-关闭，1-接通，占1位)
                      第2字节：通道5是否设置(0-不设置，1-设置，占1位)-通道5是否接通(0-关闭，1-接通，占1位)-通道6是否设置(0-不设置，1-设置，占1位)-通道6是否接通(0-关闭，1-接通，占1位)-
                             通道7是否设置(0-不设置，1-设置，占1位)-通道7是否接通(0-关闭，1-接通，占1位)
                    2字节：第1字节：代表8个DO口的开或关，0为关，1为开
                             第2字节：前3位代表3个DO口的开或关，0为关，1为开*/
                    byte[] send = new byte[2];
                    System.arraycopy(runBuffer, 11, send, 0, 2);//直接转发。
                    explainerHelper.SetDO(send);
                    break;

                case 116:
                    /*int GetAI(int)模拟输入  获取端口的模拟值。 经确认不支持引用变量。
                    第一个参数：4字节，int类型，表示端口（1~7）。*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, packetDataValueIndex);
                    mValue[reValue] = explainerHelper.ReadAIValue((int) param1 - 1);
                    LogMgr.d("reValue::" + reValue + "  " + "ReadAIValue() = " + mValue[reValue]);
                    break;

                case 117: //void setSoundNew(int,int)	扬声器
                    /*"第一个参数：4字节，int类型，表示声音类型。0：打招呼；1：表情；2：动作；3：动物；4：钢琴；5：录音。
                    第二个参数：4字节，int类型，表示某种声音的文件下标。
                    打招呼分为：你好、再见、反对、欢迎、请多关照；
                    表情分为：生气、傲慢、哭泣、激动、惊吓、委屈、高兴、可爱、大笑、悲伤、愤怒、调皮；
                    动作分为：打寒颤、卖萌、赞成、求抱抱、打哈欠、加油、睡觉、休闲、鬼鬼祟祟；
                    动物分为：牛、虎、海豚、蟋蟀、鸭、飞虫；
                    钢琴分为：1-8；录音分为：1-10。"		*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d("117 setSoundNew(int,int) param1::" + param1 + "  " + "param2::" + param2);
                    explainerHelper.playMusic((int) param1, (int) param2);
                    break;

                case 118: //void MicrophoneNew(int,int)	麦克风
                    /*"第一个参数：4字节，int类型，表示录音文件（1-10）。
                    第二个参数：4字节，int类型，表示录音时间（单位s）。"*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d("118 MicrophoneNew(int,int) param1::" + param1 + "  " + "param2::" + param2);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param2, (int) param1);
                    pauseExplain();
                    break;

                case 119: //void setSoundNew(char *) 扬声器
                    /*char*类型，表示音频文件名（N<20字节）
                    附：STM32端内置音频命名规则——中文拼音_语言+下标
                    例：中文“打招呼”下的“你好”——dazhaohu_c0
                    英文“打招呼”下的“欢迎”——dazhaohu_e3
                    公共“交通”下的“赛车” —— jiaotong_p1
                    公共“动物”下的“狗” —— dongwu_p3
                    录音 —— luyin_p3（1~10）*/
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

                case 120: //int AI(int) 获取磁敏
                    /*4字节，int类型，表示端口（1~7）*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getMagneticSwitchC9((int) param1);
                    break;

                case 121: //void setColorLed(int)	设置彩灯
                    /*
                    第一个参数为模式：0~2（""开"",""关"",""呼吸""）
                    第二个参数为呼吸频率：0.1~60，
                    第三个参数为颜色值。
                    第1字节：不需设置
                    第2字节：代表红色亮度，范围0~255
                    第3字节：代表绿色亮度，范围0~255
                    第4字节：代表蓝色亮度，范围0~255"
                    第四个参数为ID：0-7（0代表全部，1-7代表底层110-116）*/
                    int mode = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    float frequency = explainerHelper.getParam(runBuffer, mValue, 15);
                    byte[] rgb = Arrays.copyOfRange(runBuffer, 22, 25);
                    int ledId = (int) explainerHelper.getParam(runBuffer, mValue, 25);
                    explainerHelper.setColorLedC9(ledId, mode, frequency, rgb);
                    break;

                case 122: //void setSingleEngine(int,int)	单个舵机控制
                case 127: //void setSmartMotor(int,int)	设置智能电机（C9）
                    /*"第一个参数 舵机ID号(1-22)
                    第二个参数 舵机角度（0-1023）高位在前低位在后
                    第三个参数 舵机速度（200-1023）高位在前低位在后"*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    explainerHelper.setSmartMotor((int) param1, (int) param2, (int) param3);
                    break;

                case 128: //void setElectromagnet(int) 	电磁铁(C9)
                    /*第一个参数：端口 [1, 7]；
                    * 第二个参数：1-打开，0-关闭；*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    explainerHelper.setElectromagnet((int) param1, (int)param2);
                    break;

                case 129: //int soundExamine()	声音检测（C9）
                    /*无参数，返回值范围0-100；*/
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getMicVolumeDbValue();
                    LogMgr.d("129 getMicVolumeDbValue() =  " + mValue[reValue]);
                    break;

                case 130://void playanim(int)	播放动画
                    //第一个参数：动画ID 1-7
                    int animId = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_ANIMATION, String.valueOf(animId));
                    break;

                case 136: //void playCustomMusic(char*)	播放自定义音频
                    //"第一个字节：类型 0：自定义音频(在多媒体目录下/Abilix/media/upload/audio/)。char*类型，表示音频名称（N<19 字节）"
                    int soundType = runBuffer[10] & 0xFF;
                    soundName = explainerHelper.getString(runBuffer, 11);
                    final int waitFlag = runBuffer[29] & 0xFF;
                    LogMgr.d("soundType:" + soundType + " soundName:" + soundName + " waitFlag: " + waitFlag);
                    explainerHelper.playSound(soundType, soundName, new PlayerUtils.OnCompletionListener() {
                        @Override
                        public void onCompletion(int state) {
                            LogMgr.d("onCompletion() state: " + state);
                            if (waitFlag == 1) {
                                resumeExplain();
                            }
                        }
                    });
                    if (waitFlag == 1) {
                        pauseExplain();
                    }
                    break;

                default: // 可捕获未解释执行的函数
                    strInfo += (" -- default(未处理): " + index + "   " + funPos);
                    LogMgr.e("error::" + strInfo);
                    return;
            }

            if (len == funPos) { // 判断程序结束
                sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                LogMgr.e("explain file end");
                doexplain = false;
            }

        } while (!isStop);
    }

    @Override
    public void stopExplain() {
        super.stopExplain();//关闭在前。退出循环用。
        explainerHelper.stopPlaySound();
        explainerHelper.Motor_stop();//寻线模块专用。
        explainerHelper.setIswait(false);//退出寻线模块等待。
        BalanceCarData.getOut();
        explainerHelper.setButtonLight(0, 0);
        explainerHelper.setAllMotorStop();
        explainerHelper.setColorLedC9(0, 1, 0, new byte[3]);
        //explainerHelper.resetStm32();
    }

    private int switchint(int a) {
        if (a == 1) {
            return 0;
        } else if (a == 0) {
            return 1;
        }
        return a;
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
