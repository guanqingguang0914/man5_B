package com.abilix.explainer.scratch;

import android.os.Handler;

import com.abilix.explainer.AExplainer;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.FunNode;
import com.abilix.brain.R;
import com.abilix.explainer.balancecar.BalanceCarData;
import com.abilix.explainer.helper.AExplainHelper;
import com.abilix.explainer.helper.CExplainerHelper;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.PlayerUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

public class CScratchExplainer extends AExplainer {
    protected CExplainerHelper explainerHelper;
    private boolean setScorpionModel = false;
    private boolean setTankModel = false;

    public CScratchExplainer(Handler handler) {
        super(handler);
        explainerHelper = new CExplainerHelper();
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
        globalFunNodeCount = 0;

        // 添加两个时钟变量。赋初始值。
        long init_time = 0;
        long end_time = 0;
        init_time = end_time = System.currentTimeMillis();

        funPos = start;

        do {
            if (!doexplain) { // 文件解析结束后退出
                LogMgr.d("explain finish and exit");
                return;
            }
            explainerHelper.readFromFlash(filebuf, funPos, runBuffer, 40); // 获取40字节数据
            index = explainerHelper.getU16(runBuffer, 8); // 解析函数名对应的index
            funPos = explainerHelper.getU16(runBuffer, 34) * 40; // 解析跳转到哪一行
            //LogMgr.d(String.format(Locale.US, "====>index:%d funPos:%d runBuffer:%s", index, funPos, ByteUtils.bytesToString(runBuffer, runBuffer.length)));
            //LogMgr.d("====>index:" + index);

            switch (index) {
                case 0: //getName
                    byte name[] = new byte[20];
                    explainerHelper.getName(name, runBuffer);
                    //String temp = new String(name);
                    break;

                case 3: // wait
                    isSleep = true;
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    waitExplain(param1);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                    break;

                case 21: // my_Calc (运算符计算)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);

                    reValue = explainerHelper.getU16(runBuffer, 30);
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
                    funPos = funPos * 40;
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

                case 30: // runmove--------------------------------------------ok
                    // 函数需要两个参数，一个是端口，一个是value。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d("Crunmove(" + param1 + ", " + param2 + ")");
                    explainerHelper.Crunmove((int) param1, (int) param2);
                    break;

                case 31: // runmoveTime 这个先不写，有个延时函数。----------------------ok
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.d("Crunmove(" + param1 + ", " + param2 + ")" + " time: " + param3);
                    // 这个先不测。
                    explainerHelper.Crunmove((int) param1, (int) param2);
                    waitExplain(param3);
                    explainerHelper.Crunmove((int) param1, 0);
                    break;

                case 32: // runstop--------------------------------------------ok
                    // 这个无参数，
                    explainerHelper.C_runstop();
                    break;

                case 33: // setsound1------------------------------------------ok
                    // 这里要获取动物种类。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d("playSound(SOUND_TYPE_ANIMAL, " + param1 + ")");
                    explainerHelper.playSound(AExplainHelper.SOUND_TYPE_ANIMAL, (int) param1);
                    break;

                case 34: // setsound2------------------------------------------ok
                    // 这里要获取音乐种类
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d("playSound(SOUND_TYPE_INSTRUMENT, " + param1 + ")");
                    explainerHelper.playSound(AExplainHelper.SOUND_TYPE_INSTRUMENT, (int) param1);
                    break;

                case 35:// setsound3------------------------------------------ok
                    // 这里要获取自我介绍几
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d("playSound(SOUND_TYPE_INTRODUCE, " + param1 + ")");
                    explainerHelper.playSound(AExplainHelper.SOUND_TYPE_INTRODUCE, (int) param1);
                    break;

                case 36: // voicestop-----------------------------------------ok
                    // 这个无参数
                    explainerHelper.stopPlaySound();
                    break;

                case 37: // setled
                    // 需要获取颜色种类。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d("setLed(" + param1 + ")");
                    explainerHelper.setLed((int) param1);
                    break;

                case 38: // findbar 这个是前方有障碍物。一个是type 一个是value。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int port = explainerHelper.ReadAIType(0); // 获取超声对应的AI口
                    int value = 0;
                    if (port != -1) {
                        value = explainerHelper.ReadAIValue(port); // 获取AI口的值
                    }
                    if (value >= 200 || value <= 0) {
                        value = 0;
                    }
                    // value 就是最终的结果。
                    mValue[reValue] = value;
                    LogMgr.d("超声 findBar() = " + mValue[reValue]);
                    break;

                case 39: // findbardistance
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int dis_port = explainerHelper.ReadAIType(0); // 获取超声对应的AI口
                    mValue[reValue] = explainerHelper.ReadAIValue(dis_port); // 获取AI口的值
                    LogMgr.d("超声 findBarDistance() = " + mValue[reValue]);
                    break;

                case 40: // findobject 也是一样的。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int objectindex = explainerHelper.ReadAIType(1); // 获取按钮对应的AI口
                    int objectvalue = 0;
                    if (objectindex != -1) {
                        objectvalue = explainerHelper.ReadAIValue(objectindex); // 获取AI口的值
                    }
                    if (objectvalue != -1) {
                        if (objectvalue > 1000 && objectvalue < 4096) { // 碰撞
                            objectvalue = 1;
                        } else { // 未碰撞
                            objectvalue = 0;
                        }
                    } else {
                        // 未检测到按钮传感器
                        objectvalue = 0;
                    }
                    mValue[reValue] = objectvalue;
                    LogMgr.d("碰撞 findObject() = " + mValue[reValue]);
                    break;

                case 41: // bool findColorC(int)	感受到颜色	参数为颜色类型：0~4 ("红色","绿色","蓝色","黑色","白色")
                    int colorIndex = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.findColorC(colorIndex);
                    LogMgr.d("findColorC(" + colorIndex + ") = " + mValue[reValue]);
                    break;

                case 42: // getgray 也是两个接口。
                    // 灰度模式是2.
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int grayindex = explainerHelper.ReadAIType(2); // 超声传递0，按钮传递1.地面灰度传递？暂时默认跟超声一样。
                    int grayvalue = 0;
                    if (grayindex != -1) {
                        grayvalue = explainerHelper.ReadAIValue(grayindex); // 获取AI口的值
                    }
                    if (grayvalue < 0) {
                        grayvalue = 0;
                    }
                    mValue[reValue] = grayvalue;
                    LogMgr.d("灰度 getGray() = " + mValue[reValue]);
                    break;

                case 43:
                    // 获取指南针的角度。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getCompass();
                    LogMgr.d("getCompass() = " + mValue[reValue]);
                    break;

                case 44: // getGyro 上府左右翻转。陀螺仪。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("action =： " + param1);
                    mValue[reValue] = explainerHelper.position((int) param1);
                    LogMgr.d("position(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 45: // 麦克风录音。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param1);
                    pauseExplain();
                    break;

                case 46:// 时钟复位。
                    // 复位时间
                    init_time = System.currentTimeMillis();
                    break;

                case 47:// 系统时间。
                    // 计算时间
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    end_time = System.currentTimeMillis();
                    Double b = ((double) (end_time - init_time) / 1000);
                    BigDecimal bd = new BigDecimal(b);
                    bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);//四舍五入。
                    b = bd.doubleValue();
                    mValue[reValue] = Float.parseFloat(b.toString());//double 转float
                    break;

                case 48:// 显示。 copy from VJC。
                    String content = "";
                    // 因为scratch只显示一行。所以我就不用管换行了。
                    int isvariable = runBuffer[10];
                    if (isvariable == 0) {
                        // 内容是11~29.
                        byte[] data_bytes = new byte[20];
                        System.arraycopy(runBuffer, 11, data_bytes, 0, 20);
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
                            // scratch 只需要显示1行。
                            content += new String(str_byte, "UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (isvariable == 1) {

                        float paramV = explainerHelper.getParam(runBuffer, mValue, 10);
                        content += paramV;
                    }
                    LogMgr.d("display content::" + content);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    break;

                case 49: // 拍照。
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_CAMERA, 1);
                    pauseExplain();
                    break;

                case 50:// 指南针校准。
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_COMPASS);
                    pauseExplain();
                    break;

                case 250://runMoveC1(int, int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.d("runMove(" + param1 + ", " + param2 + ", " + param3 + ")");
                    explainerHelper.runMove((int) param1, (int) param2, (int) param3);
                    break;

                case 251://setSoundC1(int, int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    explainerHelper.playMusic((int) param1, (int) param2);
                    break;

                case 252://setLedC1(int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    explainerHelper.setLedRgb((int) param1);
                    break;

                case 253://显示。
                    String content1 = "";
                    int isvar = runBuffer[10];//0字符  1 照片   2引用变量。
                    LogMgr.d("type: " + isvar);
                    if (isvar == 0) { //0字符
                        byte[] data_bytes = new byte[20];
                        System.arraycopy(runBuffer, 11, data_bytes, 0, 20);
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
                            // scratch 只需要显示1行。
                            content1 += new String(str_byte, "UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content1);
                    } else if (isvar == 1) { //1 照片
                        param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                        LogMgr.d("picnum:" + param2);
                        //主要是加一个文件路劲判断。
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_PHOTO, String.valueOf((int) param2));
                    } else if (isvar == 2) { //2引用变量
                        float paramV = explainerHelper.getParam(runBuffer, mValue, 15);
                        LogMgr.d("2引用变量: " + paramV);
                        if (Float.isNaN(paramV) || Float.isInfinite(paramV)) {
                            content1 = ExplainerApplication.instance.getString(R.string.guoda);
                        } else {
                            DecimalFormat df = new DecimalFormat("0.000");
                            content1 += df.format(paramV);
                        }
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content1);
                    }
                    break;

                case 254://closeC1(int)
                    int type = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    switch (type) {
                        case 0:// 关闭电机
                            explainerHelper.C_runstop();
                            break;
                        case 1:// 关闭扬声器
                            explainerHelper.stopPlaySound();
                            break;
                        case 2:// 关闭LED
                            explainerHelper.setLed(0);
                            break;
                        case 3://关闭显示
                            sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                            break;
                        default:
                            break;
                    }
                    break;

                case 255://findBarC1(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.haveObject((int) param1);
                    LogMgr.d("haveObject(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 256://findBarDistanceC1(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.distance((int) param1);
                    LogMgr.d("distance(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 257://findObjectC1(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.touch((int) param1);
                    LogMgr.d("touch(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 258://findColorC1(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.findColor((int) param1);
                    LogMgr.d("findColor(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 259://getGrayC1(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.getGraySensor((int) param1);
                    LogMgr.d("getGraySensor(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 260://cameraC1()
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_CAMERA, (int) param1);
                    pauseExplain();
                    break;

                case 261://resettimeC1()
                    init_time = System.currentTimeMillis();
                    break;

                case 262://secondsC1()
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    end_time = System.currentTimeMillis();
                    Double b2 = ((double) (end_time - init_time) / 1000);
                    BigDecimal bd2 = new BigDecimal(b2);
                    bd2 = bd2.setScale(3, BigDecimal.ROUND_HALF_UP);//四舍五入。
                    b2 = bd2.doubleValue();
                    mValue[reValue] = Float.parseFloat(b2.toString());//double 转float
                    break;

                case 263://InitCompassC1() 重用
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_COMPASS);
                    pauseExplain();
                    break;

                case 264://getGyroC1(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getCompass();
                    break;

                case 265://getGyroC1(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.position((int) param1);
                    LogMgr.d("position(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 266://MicrophoneC1(int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param2, (int) param1);
                    LogMgr.d("record(" + param1 + ", " + param2 + ")");
                    pauseExplain();
                    break;

                case 267://void BalanceCarC1(int)	平衡车控制
                    /*"第一个参数 第1字节：0x00自平衡； 0x01前进； 0x02后退； 0x03左转； 0x04右转；0x05停止；0x06前进右转;0x07后退左转;0x08后退右转;0x09前进左转; 0x0A左右轮速度设置
                    第2字节：超声控制：0x00无控制； 0x01开启超声控制； 0x02关闭超声控制；
                    第3字节：速度设置：0x00不设置； 0x01速度值(默认)；  （第1字节为0x0A时，该字节表示左轮速度）
                    第4字节：速度值                                   （第1字节为0x0A时，该字节表示右轮速度）
                    第二个参数 第1字节：其他设置：0x00不设置
                    0x02距离值 50-500cm，第1字节为0x01前进、0x02后退时，该参数值有效；
                    0x03角度值，第1字节为0x03左转、0x04右转时，该参数值有效；
                    0x04时间值 0-30s，第1字节为0x03左转、0x04右转时，该参数值有效；
                    第2~3字节：参数值（高位在前 低位在后）"*/
                    int moveType = runBuffer[11] & 0xFF;
                    int usSet = runBuffer[12] & 0xFF;
                    int leftSpeed = runBuffer[13] & 0xFF;
                    int rightSpeed = runBuffer[14] & 0xFF;
                    int valueSet = runBuffer[16] & 0xFF;
                    int otherValue = ByteUtils.byte2int_2byteHL(runBuffer, 17);
                    explainerHelper.setBalanceCar(moveType, usSet, leftSpeed, rightSpeed, valueSet, otherValue);
                    break;

                case 268://void AnimationC1(int)	显示动画
                    //1参数：动画编号0~5（高兴，加油，可爱，哭泣，委屈，鸡）
                    int animId = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_ANIMATION, String.valueOf(animId));
                    break;

                case 269://void setSound1C1(char *)	扬声器
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

                case 270://void ScorpionC1(int)	蝎子控制
                    /*"第一个参数：
                    第一字节：命令类型 0x01 移动命令 0x02 功能命令
                    第二字节：第一字节为“移动命令”时： 0x00 停止 0x01 前进 0x02 后退
                    第一字节为“功能命令”时： 0x01 攻击
                    第三字节：第一字节为“移动命令”时： 速度值0~100
                    第四字节：第一字节为“移动命令”时： 时间1~10S"*/
                    int cmdType = runBuffer[11] & 0xFF;
                    int cmdValue = runBuffer[12] & 0xFF;
                    int cmdSpeed = runBuffer[13] & 0xFF;
                    int cmdTime = runBuffer[14] & 0xFF;
                    if (!setScorpionModel) {
                        setScorpionModel = true;
                        explainerHelper.setMotionModel(0x02);
                    }
                    explainerHelper.setScorpion(cmdType, cmdValue, cmdSpeed);
                    if (cmdType == 0x01) {//移动命令
                        waitExplain(cmdTime);
                    } else if (cmdType == 0x02) {//功能命令
                        pauseExplain();
                    }
                    break;

                case 271://void TankC1(int)	坦克（救援车）控制
                    /*"第一个参数：
                    第一字节：命令类型 0x01 移动命令 0x02 功能命令
                    第二字节：第一字节为“移动命令”时： 0x00 停止 0x01 前进 0x02 后退 0x03左转 0x04右转
                    第一字节为“功能命令”时： 0x01 避障开 0x02避障关
                    第三字节：第一字节为“移动命令”时： 速度值0~100
                    第四字节：第一字节为“移动命令”时： 时间1~10S"*/
                    int cmdTypeT = runBuffer[11] & 0xFF;
                    int cmdValueT = runBuffer[12] & 0xFF;
                    int cmdSpeedT = runBuffer[13] & 0xFF;
                    int cmdTimeT = runBuffer[14] & 0xFF;
                    if (!setTankModel) {
                        setTankModel = true;
                        explainerHelper.setMotionModel(0x01);
                    }
                    explainerHelper.setTank(cmdTypeT, cmdValueT, cmdSpeedT);
                    if (cmdTypeT == 0x01) {//移动命令
                        waitExplain(cmdTimeT);
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
        BalanceCarData.getOut();
        explainerHelper.C_runstop();
        explainerHelper.setLed(0);
        if (setScorpionModel || setTankModel) {//关闭模型
            setScorpionModel = false;
            setTankModel = false;
            explainerHelper.setMotionModel(0x00);
        }
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
