package com.abilix.explainer.scratch;

import android.os.Handler;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.explainer.AExplainer;
import com.abilix.explainer.ControlInfo;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.FunNode;
import com.abilix.explainer.helper.H56ExplainerHelper;
import com.abilix.explainer.helper.HExplainerHelper;
import com.abilix.explainer.helper.Interface.IHRobot;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.PlayerUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

public class HScratchExplainer extends AExplainer {
    private boolean isDisplay = false;
    protected IHRobot explainerHelper;

    public HScratchExplainer(Handler handler) {
        super(handler);
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H) {
            explainerHelper = new H56ExplainerHelper();
        } else /*if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3)*/ {
            explainerHelper = new HExplainerHelper();
        }
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
            if (doexplain == false) { // 文件解析结束后退出
                LogMgr.d("explain finish and exit");
                return;
            }
            explainerHelper.readFromFlash(filebuf, funPos, runBuffer, 40); // 获取40字节数据
            index = explainerHelper.getU16(runBuffer, 8); // 解析函数名对应的index
            funPos = explainerHelper.getU16(runBuffer, 34) * 40; // 解析跳转到哪一行
            LogMgr.d(String.format(Locale.US, "====>index:%d funPos:%d runBuffer:%s", index, funPos, ByteUtils.bytesToString(runBuffer, runBuffer.length)));
            switch (index) {

                case 3: // wait 这个是延时函数。 运动+时长 应该解释成3个函数 运动 + 延时+ stop
                    isSleep = true;
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param1 = param1 * 1000f;
                    String str = String.valueOf((int) param1);
                    int i = (int) (Long.parseLong(str) / 100);
                    int n = 0;
                    LogMgr.e("case 3: " + isSleep);
                    while (isSleep & n < i) {
                        try {
                            //LogMgr.e("inner");
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

                case 21: // my_Calc (运算符计算)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.e("   param1:" + param1 + "   param2:" + param2 + "   param3:" + param3);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    LogMgr.e("reValue =" + reValue);
                    mValue[reValue] = explainerHelper.my_Calc(param1, param2,
                            (int) param3);
                    break;

                case 22: // my_jump(跳转)
                    LogMgr.e("come my_jump --------------");
                    if (gloableCount == -1) {
                        gloableCount = explainerHelper.getU16(runBuffer, 4) - 1;
                    }
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.e("param1:= " + param1 + " param2: " + param2
                            + "  param3:" + param3);
                    funPos = (param1 > 0.5) ? (int) param2 : (int) param3;
                    funPos = funPos * 40;
                    break;

                case 23: // return(程序结束)
                    if (isDisplay) {
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                        isDisplay = false;
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



                //Scratch H系列
                case 870: // void runServoH3(int, int, int)	0x01(舵机控制)
                    /*"第一个参数舵机ID号(1-N)； 第二个参数舵机角度（0-1023）；第三个参数舵机角度（-180~+180）
                    N = 22"*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.d(String.format(Locale.US, "case %d: runServoH3(%f, %f, %f)", index, param1, param2, param3));
                    explainerHelper.runServo((int) param1, (int) param2, (int) param3);
                    break;

                case 871: // void startServoH3(int)	0x02(舵机上电)
                    /*第一个参数舵机ID号(0-N)，0为全部*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: startServoH3(%f)", index, param1));
                    explainerHelper.startServo((int) param1);
                    break;

                case 872: // void stopServoH3(int)	0x03(舵机释放)
                    /*第一个参数舵机ID号(0-N)，0为全部*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: stopServoH3(%f)", index, param1));
                    explainerHelper.stopServo((int) param1);
                    break;

                case 873: // void zeroServoH3(int)	0x04(舵机归零)
                    /*第一个参数舵机ID号(0-N)，0为全部*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: zeroServoH3(%f)", index, param1));
                    explainerHelper.zeroServo((int) param1);
                    break;

                case 874: // void setDisplayH3(char *)	0x05(启动显示)
                    /*"第一个参数为类型：0~2(""字符"",""照片""，""舵机角度"")，
                    （1）当第一个参数为""舵机角度""时，第二个参数为0~N,0为全部
                    （2）当第一个参数为""字符""时，第二个参数为字符串数组（最大19个字节）
                    （3）当第一个参数为""照片""时，第二个参数为0~9
                    第三个参数：第一个参数为2，第二个参数为0时，表示舵机总个数"*/
                    isDisplay = true;
                    String content = "";
                    LogMgr.e("runBuffer::" + ByteUtils.bytesToString(runBuffer, runBuffer.length));
                    int isvar = runBuffer[10];//0字符  1 照片  2引用变量  3舵机角度
                    LogMgr.e("type: " + isvar);
                    if (isvar == 0) {
                        byte[] data_bytes = new byte[20];
                        System.arraycopy(runBuffer, 11, data_bytes, 0, 20);
                        LogMgr.e("data_bytes::" + ByteUtils.bytesToString(data_bytes, data_bytes.length));
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
                            LogMgr.e("data_bytes::" + content);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    } else if (isvar == 1) { //1 照片
                        param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                        LogMgr.e("picnum:" + param2);
                        //主要是加一个文件路劲判断。
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_PHOTO, String.valueOf((int) param2));
                    } else if (isvar == 2) { //2引用变量
                        float paramV = explainerHelper.getParam(runBuffer, mValue, 15);
                        LogMgr.e("2引用变量: " + paramV);
                        if (Float.isNaN(paramV) || Float.isInfinite(paramV)) {
                            content = ExplainerApplication.instance.getString(R.string.guoda);
                        } else {
                            DecimalFormat df = new DecimalFormat("0.000");
                            content += df.format(paramV);
                        }
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    } else if (isvar == 3) { //"0"舵机角度""
                        int id = (int) explainerHelper.getParam(runBuffer, mValue, 15);
                        //获取舵机角度
                        content = explainerHelper.getServoAngleString(id);
                        LogMgr.e("content = " + content);
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    }
                    break;

                case 875: //void setSoundH3(int, int)	0x06(启动扬声器)
                    /*"第一个参数为类型：0~5(""打招呼"",""表情"",""动作"",""动物"",""钢琴"",""录音"")，
                    （1）当第一个参数为""打招呼""时，第二个参数为0~4（""你好"",""再见"",""反对"",""欢迎"",""请多关照""）
                    （2）当第一个参数为""表情""时，第二个参数为0~11（""生气"",""傲慢"",""哭泣"",""激动"",""惊吓""，""委屈"",""高兴"",""可爱"",""大笑"",""悲伤"",""愤怒"",""调皮""）
                    （3）当第一个参数为""动作""时，第二个参数为0~8（""打寒颤"",""卖萌"",""赞成"",""求抱抱"",""打哈欠""，""加油"",""睡觉"",""休闲"",""鬼鬼祟祟""）
                    （4）当第一个参数为""动物""时，第二个参数为0~5(""牛"",""虎"",""海豚"",""蟋蟀"",""鸭"",""飞虫"")
                    （5）当第一个参数为""钢琴""时，第二个参数为0~7(""1"",""2"",""3"",""4"",""5"",""6"",""7"",""8"")
                    （6）当第一个参数为""录音""时，第二个参数为0~9"	启动扬声器【介绍/动物/音乐】*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d(String.format(Locale.US, "case %d: setSoundH3(%f, %f)", index, param1, param2));
                    explainerHelper.playMusic((int) param1, (int) param2);
                    break;

                case 876: //void setLed1H3(int)	0x07(设置LED)
                    /*第一个参数：后三个字节分别表示 红绿蓝（0~255）*/
                    byte[] rgb = Arrays.copyOfRange(runBuffer, 12, 12 + 3);
                    LogMgr.d(String.format(Locale.US, "case %d: setLed1H3(rgb[%d, %d, %d])", index, rgb[0] &0xFF, rgb[1] &0xFF, rgb[2] &0xFF));
                    explainerHelper.setHeadLed(rgb);
                    break;

                case 877: //void closeH3(int)	0x08(关闭)
                    /*第一个参数为类型：0~2("显示","扬声器","LED")*/
                    int type = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: closeH3(%d)", index, type));
                    switch (type) {
                        case 0:// 关闭显示。
                            sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                            break;
                        case 1:// 关闭扬声器
                            explainerHelper.stopPlaySound();
                            break;
                        case 2:// 关闭LED
                            explainerHelper.setHeadLed(new byte[3]);
                            break;
                        default:
                            break;
                    }
                    break;

                case 878: //bool findBarH3(int)	0x09(前方是否有障碍物)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int val = explainerHelper.findBar();
                    LogMgr.d(String.format(Locale.US, "case %d: findBarH3() = %d", index, val));
                    mValue[reValue] = val;
                    break;

                case 879: //int  findBarDistanceH3(int)	0x0A(探测障碍物距离)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int disValue = explainerHelper.findBarDistance();
                    LogMgr.d(String.format(Locale.US, "case %d: findBarDistanceH3() = %d", index, disValue));
                    mValue[reValue] = disValue;
                    break;

                case 880: //void InitCompassH3()	0x0B(指南针校准)
                    LogMgr.d(String.format(Locale.US, "case %d: InitCompassH3()", index));
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_COMPASS);
                    pauseExplain();
                    break;

                case 881: //int getCompassH3()	0x0C(指南针探测角度)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    LogMgr.d(String.format(Locale.US, "case %d: getCompassH3()", index));
                    int compassValue = (int) explainerHelper.getCompass();
                    mValue[reValue] = compassValue;
                    break;

                case 882: //void MicrophoneH3(int)	0x0D(麦克风录音)
                    /*第一个参数为：0~9，第二个参数为时间：1~60*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d(String.format(Locale.US, "case %d: MicrophoneH3(%f, %f)", index, param1, param2));
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param2, (int) param1);
                    pauseExplain();
                    break;

                case 883: //void cameraH3()	0x0E(拍照)	第一个参数为时间：1~10	摄像头端口CAM拍照【1/2/3/4/5/6/7/8/9/10】
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: cameraH3(%f)", index, param1));
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_CAMERA, (int) param1);
                    pauseExplain();
                    break;

                case 884://void setSound1H3(char *)	0x0F(扬声器)
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

                case 885://void runMoveH3(int)	0x10(运动)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    /*第一个参数 0x00 前进 0x01 后退 0x02 向左 0x03 向右 0x04 转体*/
                    explainerHelper.move((int) param1);
                    pauseExplain();
                    break;

                case 886://void stopRunMoveH3()	0x11(停止运动)
                    explainerHelper.stopMove();
                    break;

                case 887://bool getGyroH3(int) 0x12(陀螺仪)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("action =： " + param1);
                    mValue[reValue] = explainerHelper.position((int) param1);
                    break;

                case 888://bool getHeadTouchH5()	0x13(头部触摸)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    mValue[reValue] = explainerHelper.getHeadTouch();
                    LogMgr.d(String.format(Locale.US, "case %d: getHeadTouch() = %f", index, mValue[reValue]));
                    break;

                case 889://bool checkColorH5(int)	0x14(颜色检测)
                    /*第一个参数 0x00红色 0x01绿色 0x02蓝色 0x03黑色*/
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    //mValue[reValue] = explainerHelper.getColor();
                    LogMgr.d(String.format(Locale.US, "case %d: getColor() = %f", index, mValue[reValue]));
                    break;

                case 890://void setLedH5(int，int)	0x15(设置LED H5~6)
                    /*"第一个参数：0x00额头 0x01双眼
                    第二个参数：后三个字节分别表示 红绿蓝（0~255）"*/
                    int typeHead = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    byte[] rgbHead = Arrays.copyOfRange(runBuffer, 17, 17 + 3);
                    LogMgr.d(String.format(Locale.US, "type:%d rgb[%d,%d,%d]", typeHead, rgbHead[0] & 0xFF, rgbHead[1] & 0xFF, rgbHead[2] & 0xFF));
                    if (typeHead == 0x01) {
                        typeHead = 0x05;//双眼
                    }
                    explainerHelper.setLed(typeHead, rgbHead, 0);
                    break;

                case 891://void setLightH5(int,int)	0x16(设置手脚灯光 H5~6)
                    /*"第一个参数：0x00左手 0x01右手 0x02双手 0x03左脚 0x04右脚 0x05双脚
                    第二个参数：0x00常亮 0x01闪烁"*/
                    int typeHand = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    int mode = (int) explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d(String.format(Locale.US, "type:%d mode:%d", typeHand, mode));
                    explainerHelper.setHandsFeetLed(typeHand, mode);
                    break;

                case 892://void closeH5(int)	0x17(关闭 H5~6)
                    /*第一个参数为类型：0~9( 0x00额头,0x01双眼,0x02左手灯，0x03右手灯，0x04双手灯，0x05左脚灯，0x06右脚灯，0x07双脚灯，0x08扬声器，0x09显示)*/
                    int closeType = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    int[] trans = new int[]{0x00, 0x05, 0x01, 0x02, 0x06, 0x03, 0x04, 0x07};
                    switch (closeType) {
                        case 0x00:
                        case 0x01:
                        case 0x02:
                        case 0x03:
                        case 0x04:
                        case 0x05:
                        case 0x06:
                        case 0x07:
                            explainerHelper.setLed(trans[closeType], new byte[3], 0);
                            break;
                        case 0x08:
                            explainerHelper.stopPlaySound();
                            break;
                        case 0x09:
                            sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                            break;
                    }
                    break;

                case 893://void gaitMotion(int，int)	0x18(步态运动 H3、4)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.e("gaitMotion() type:" + param1 + " speed:" + param2);
                    explainerHelper.gaitMotion((int) param1, (int) param2);
                    break;

                default:
                    LogMgr.e("HScratchExplainer: NO DEFINED INDEX: " + index);
                    break;
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
        super.stopExplain();
        explainerHelper.stopMove(); // 停止时必须先发stopMove()停止动作播放，否则会出现卡顿；
        explainerHelper.stopPlaySound();
        explainerHelper.turnoffLed();
    }

}