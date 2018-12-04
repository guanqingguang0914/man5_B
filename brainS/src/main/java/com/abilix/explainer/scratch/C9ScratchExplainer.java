package com.abilix.explainer.scratch;

import android.os.Handler;

import com.abilix.explainer.AExplainer;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.FunNode;
import com.abilix.brain.R;
import com.abilix.explainer.balancecar.BalanceCarData;
import com.abilix.explainer.helper.AExplainHelper;
import com.abilix.explainer.helper.C9ExplainerHelper;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.PlayerUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

public class C9ScratchExplainer extends AExplainer {
    protected C9ExplainerHelper explainerHelper;

    public C9ScratchExplainer(Handler handler) {
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
            LogMgr.d(String.format(Locale.US, "====>index:%d funPos:%d runBuffer:%s", index, funPos, ByteUtils.bytesToString(runBuffer, runBuffer.length)));

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
                    LogMgr.d("setLedC9(" + param1 + ")");
                    explainerHelper.setLedC9((int) param1);
                    break;

                case 38: // findbar 这个是前方有障碍物。一个是type 一个是value。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int port = explainerHelper.getPortByAIType(C9ExplainerHelper.AI_TYPE_ULTRASONIC); // 获取超声对应的AI口
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
                    int dis_port = explainerHelper.getPortByAIType(C9ExplainerHelper.AI_TYPE_ULTRASONIC); // 获取超声对应的AI口
                    mValue[reValue] = explainerHelper.ReadAIValue(dis_port); // 获取AI口的值
                    LogMgr.d("超声 findBarDistance() = " + mValue[reValue]);
                    break;

                case 40: // findobject 也是一样的。
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int objectindex = explainerHelper.getPortByAIType(C9ExplainerHelper.AI_TYPE_COLLISION); // 获取按钮对应的AI口
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
                    int grayindex = explainerHelper.getPortByAIType(C9ExplainerHelper.AI_TYPE_GRAY); // 超声传递0，按钮传递1.地面灰度传递？暂时默认跟超声一样。
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

                case 320://runMoveC9(int, int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.d("runMoveC9(" + param1 + ", " + param2 + ", " + param3 + ")");
                    explainerHelper.runMoveC9((int) param1, (int) param2, (int) param3);
                    break;

                case 321://setSoundC9(int, int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    explainerHelper.playMusic((int) param1, (int) param2);
                    break;

                case 322://setLedC9(int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    explainerHelper.setLedC9Rgb((int) param1);
                    break;

                case 323://显示。
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

                case 324://closeC9(int)
                    int type = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    switch (type) {
                        case 0:// 关闭电机
                            explainerHelper.C_runstop();
                            break;
                        case 1:// 关闭扬声器
                            explainerHelper.stopPlaySound();
                            break;
                        case 2:// 关闭LED
                            explainerHelper.setLedC9(0);
                            break;
                        case 3://关闭显示
                            sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                            break;
                        default:
                            break;
                    }
                    break;

                case 325://findBarC9(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.haveObject((int) param1);
                    LogMgr.d("haveObject(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 326://findBarDistanceC9(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.distance((int) param1);
                    LogMgr.d("distance(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 327://findObjectC9(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.touch((int) param1);
                    LogMgr.d("touch(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 328://findColorC9(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.findColorC9((int) param1);
                    LogMgr.d("findColorC9(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 329://getGrayC9(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.getGraySensor((int) param1);
                    LogMgr.d("getGraySensor(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 330://cameraC9()
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_CAMERA, (int) param1);
                    pauseExplain();
                    break;

                case 331://resettimeC9()
                    init_time = System.currentTimeMillis();
                    break;

                case 332://secondsC9()
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    end_time = System.currentTimeMillis();
                    Double b2 = ((double) (end_time - init_time) / 1000);
                    BigDecimal bd2 = new BigDecimal(b2);
                    bd2 = bd2.setScale(3, BigDecimal.ROUND_HALF_UP);//四舍五入。
                    b2 = bd2.doubleValue();
                    mValue[reValue] = Float.parseFloat(b2.toString());//double 转float
                    break;

                case 333://InitCompassC9() 重用
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_COMPASS);
                    pauseExplain();
                    break;

                case 334://getGyroC9(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getCompass();
                    break;

                case 335://getGyroC9(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.position((int) param1);
                    LogMgr.d("position(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 336://MicrophoneC9(int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param2, (int) param1);
                    LogMgr.d("record(" + param1 + ", " + param2 + ")");
                    pauseExplain();
                    break;

                case 337://int getMagneticC9(int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getMagneticSwitchC9((int) param1);
                    LogMgr.d("getMagneticSwitchC9(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 338://void setColorLedC9(int,int,float,int)
                    /*设置彩灯
                    "第一个参数为ID：0-7（0代表全部，1-7代表底层110-116），
                    第二个参数为模式：0~3（""开"",""关"",""呼吸"",""频闪""），
                    第三个参数为呼吸频率：0.1~60，
                    第四个参数为颜色值。
                    第1字节：不需设置
                    第2字节：代表红色亮度，范围0~255
                    第3字节：代表绿色亮度，范围0~255
                    第4字节：代表蓝色亮度，范围0~255"*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    byte[] rgb = Arrays.copyOfRange(runBuffer, 27, 30);
                    explainerHelper.setColorLedC9((int) param1, (int) param2, param3, rgb);
                    break;

                case 339: //void setSoundNew(char *) 扬声器
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

                case 340: //bool findFrameC9(int)	获取火焰传感器
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.getFireSensingValue((int) param1);
                    LogMgr.d("getFireSensingValue(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 341: //bool findTemperatureC9(int)	获取温度传感器
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.getTemperatureSensingValue((int) param1);
                    LogMgr.d("getTemperatureSensingValue(" + param1 + ") = " + mValue[reValue]);
                    break;

                case 342: //void setSmartMotor(int,int)	设置智能电机
                    /*"第一个参数 舵机ID号(1-22)
                    第二个参数 舵机角度（0-1023）高位在前低位在后
                    第三个参数 舵机速度（200-1023）高位在前低位在后"*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    explainerHelper.setSmartMotor((int) param1, (int) param3, (int) param2);
                    LogMgr.d("setSmartMotor(" + param1 + ", " + param2 + ", " + param3 + ")");
                    break;

                case 343: //void setElectromagnet(int) 	电磁铁(C9)
                    /*第一个参数：端口 [1, 7]；
                    * 第二个参数：1-打开，0-关闭；*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    explainerHelper.setElectromagnet((int) param1, (int) param2);
                    LogMgr.d("setElectromagnet(" + param1 + ", " + param2 + ")");
                    break;

                case 344: //int getInfrared()	红外
                    /*暂无
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d("param1: " + param1);
                    mValue[reValue] = explainerHelper.getInfraredValue((int) param1);
                    */
                    break;

                case 345: //int soundExamine()	声音检测（C9）
                    /*无参数，返回值范围0-100；*/
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getMicVolumeDbValue();
                    LogMgr.d("129 getMicVolumeDbValue() =  " + mValue[reValue]);
                    break;

                case 346: //int getLightIntensity	获取光强
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getLightForceValue((int) param1);
                    LogMgr.d("getLightForceValue(" + param1 + ") = " + mValue[reValue]);
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
        explainerHelper.setColorLedC9(0, 1, 0, new byte[3]);
        explainerHelper.resetStm32();
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
