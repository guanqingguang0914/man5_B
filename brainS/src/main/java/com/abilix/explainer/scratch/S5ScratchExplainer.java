package com.abilix.explainer.scratch;

import android.os.Handler;

import com.abilix.explainer.AExplainer;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.FunNode;
import com.abilix.brain.R;
import com.abilix.explainer.camera.CameraStateCallBack;
import com.abilix.explainer.camera.usbcamera.UsbCamera;
import com.abilix.explainer.helper.SExplainerHelper;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.PlayerUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

public class S5ScratchExplainer extends AExplainer {
    private boolean isDisplay = false;
    protected SExplainerHelper explainerHelper;
    public S5ScratchExplainer(Handler handler) {
        super(handler);
        explainerHelper = new SExplainerHelper();
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

                //Scratch S5系列
                case 700: // void runServoS5(int, int, int) 0x01(舵机控制)
                    /*第一个参数舵机ID号(1-8)； 第二个参数舵机速度（0-1023）；第三个参数舵机角度（-180~+180）
                    * 启动舵机ID【1~8】舵机速度【0~1023】舵机角度【-180~180】*/
                case 750: //S3,S4 第一个参数舵机ID号(1-4)； 第二个参数舵机角度（0-1023）；第三个参数舵机角度（-180~+180）
                case 790: //S7,S8 第一个参数舵机ID号(1-16)； 第二个参数舵机角度（0-1023）；第三个参数舵机角度（-180~+180）
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.d(String.format(Locale.US, "case %d: runServoS5(%f, %f, %f)", index, param1, param2, param3));
                    explainerHelper.runServoS5((int) param1, (int) param2, (int) param3);
                    break;

                case 701: // void startServoS5(int) 0x02(舵机上电)
                    /*第一个参数舵机ID号(0-8)，0为全部 舵机ID【全部/1-8】上电*/
                case 751: //S3,S4 第一个参数舵机ID号(0-4)，0为全部
                case 791: //S7,S8 第一个参数舵机ID号(0-16)，0为全部
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: startServoS5(%f)", index, param1));
                    explainerHelper.startServoS5((int) param1);
                    break;

                case 702: // void stopServoS5(int) 0x03(舵机释放)
                    /*第一个参数舵机ID号(0-8)，0为全部	舵机ID【全部/1-8】释放*/
                case 752: //S3,S4 第一个参数舵机ID号(0-4)，0为全部
                case 792: //S7,S8 第一个参数舵机ID号(0-16)，0为全部
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: stopServoS5(%f)", index, param1));
                    explainerHelper.stopServoS5((int) param1);
                    break;

                case 703: // void zeroServoS5(int)	0x04(舵机归零)
                    /*第一个参数舵机ID号(0-8)，0为全部	舵机ID【全部/1-8】归零*/
                case 753: //S3,S4 第一个参数舵机ID号(0-4)，0为全部
                case 793: //S7,S8 第一个参数舵机ID号(0-16)，0为全部
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: zeroServoS5(%f)", index, param1));
                    explainerHelper.zeroServoS5((int) param1);
                    break;

                case 704: // void setDisplayS5(char *)	0x05(启动显示)
                    /*"第一个参数为类型：0~2(""舵机角度"",""字符"",""照片"")，
                    （1）当第一个参数为""舵机角度""时，第二个参数为0~22,0为全部
                    （2）当第一个参数为""字符""时，第二个参数为字符串数组（最大19个字节）
                    （3）当第一个参数为""照片""时，第二个参数为0~9"*/
                case 754: //S3,S4
                case 794: //S7,S8
                    isDisplay = true;
                    String content = "";
                    LogMgr.e("runBuffer::" + ByteUtils.bytesToString(runBuffer, runBuffer.length));
                    int isvar = runBuffer[10];//0字符  1 照片  2引用变量  3舵机角度 4陀螺仪的值
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
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    }else if(isvar == 4){//获取陀螺仪的值，0代表所有，1位X
                        int id = (int) explainerHelper.getParam(runBuffer, mValue, 15);
                        content = explainerHelper.getGrayString(id);
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    }
                    break;

                case 705: //void setSoundS5(int, int)	0x06(启动扬声器)
                    /*"第一个参数为类型：0~5(""打招呼"",""表情"",""动作"",""动物"",""钢琴"",""录音"")，
                    （1）当第一个参数为""打招呼""时，第二个参数为0~4（""你好"",""再见"",""反对"",""欢迎"",""请多关照""）
                    （2）当第一个参数为""表情""时，第二个参数为0~11（""生气"",""傲慢"",""哭泣"",""激动"",""惊吓""，""委屈"",""高兴"",""可爱"",""大笑"",""悲伤"",""愤怒"",""调皮""）
                    （3）当第一个参数为""动作""时，第二个参数为0~8（""打寒颤"",""卖萌"",""赞成"",""求抱抱"",""打哈欠""，""加油"",""睡觉"",""休闲"",""鬼鬼祟祟""）
                    （4）当第一个参数为""动物""时，第二个参数为0~5(""牛"",""虎"",""海豚"",""蟋蟀"",""鸭"",""飞虫"")
                    （5）当第一个参数为""钢琴""时，第二个参数为0~7(""1"",""2"",""3"",""4"",""5"",""6"",""7"",""8"")
                    （6）当第一个参数为""录音""时，第二个参数为0~9"	启动扬声器【介绍/动物/音乐】*/
                case 755: //S3,S4
                case 795: //S7,S8
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d(String.format(Locale.US, "case %d: setSoundS5(%f, %f)", index, param1, param2));
                    explainerHelper.playMusic((int) param1, (int) param2);
                    break;

                case 706: //void setLed1S5(int)	0x07(设置LED)
                    /*第一个参数为亮度：1~10；第二个参数为频率1~10	设置LED颜色【红色/绿色/蓝色】状态【常灭/常灭/闪烁】*/
                case 756: //S3,S4
                case 796: //S7,S8
                    int luminance = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    int frequency = (int) explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d(String.format(Locale.US, "case %d: setLedS5(%f, %f)", index, param1, param2));
                    //explainerHelper.setLedS5((int) param1, (int) param2);
                    byte[] data;
                    data = new byte[]{0x00};
                    data[0] = (byte) ((frequency & 0x0F) << 4 | (luminance &0x0F));
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_LED, -1, data);
                    break;

                case 707: //void closeS5(int)	0x08(关闭)
                    /*第一个参数为类型：0~2("显示","扬声器","LED")	关闭【显示/扬声器/LED】*/
                case 757: //S3,S4
                case 797: //S7,S8
                    int type = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: closeS5(%d)", index, type));
                    //explainerHelper.closeS5((int) param1);
                    switch (type) {
                        case 0:// 关闭显示。
                            sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                            break;
                        case 1:// 关闭扬声器
                            explainerHelper.stopPlaySound();
                            break;
                        case 2:// 关闭LED
                            sendExplainMessage(ExplainTracker.MSG_EXPLAIN_LED, -1, new byte[]{0x00});
                            break;
                        default:
                            break;
                    }
                    break;

                case 708: //bool findBarS5()	0x09(前方是否有障碍物)
                case 758: //S3,S4
                case 798: //S7,S8
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int val = explainerHelper.findBarS5();
                    LogMgr.d(String.format(Locale.US, "case %d: findBarS5() = %d", index, val));
                    mValue[reValue] = val;
                    break;

                case 709: //int  findBarDistanceS5(int)	0x0A(探测障碍物距离)
                case 759: //S3,S4
                case 799: //S7,S8
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int disValue = explainerHelper.findBarDistanceS5();
                    LogMgr.d(String.format(Locale.US, "case %d: findBarDistanceS5() = %d", index, disValue));
                    mValue[reValue] = disValue;
                    break;

                case 710: //void InitCompassS5()	0x0B(指南针校准)
                case 760: //S3,S4
                case 800: //S7,S8
                    LogMgr.d(String.format(Locale.US, "case %d: InitCompassS5()", index));
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_COMPASS);
                    pauseExplain();
                    break;

                case 711: //int getCompassS5()	0x0C(指南针探测角度)
                case 761: //S3,S4
                case 801: //S7,S8
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    LogMgr.d(String.format(Locale.US, "case %d: getCompassS5()", index));
                    int compassValue = (int) explainerHelper.getCompass();
                    mValue[reValue] = compassValue;
                    break;

                case 712: //void MicrophoneS5(int)	0x0D(麦克风录音)
                case 762: //S3,S4
                case 803: //S7,S8
                    /*第一个参数为：0~9，第二个参数为时间：1~60*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.d(String.format(Locale.US, "case %d: MicrophoneS5(%f, %f)", index, param1, param2));
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param2, (int) param1);
                    pauseExplain();
                    break;

                case 713: //void cameraS5()	0x0E(拍照)	第一个参数为时间：1~10	摄像头端口CAM拍照【1/2/3/4/5/6/7/8/9/10】
                case 763: //S3,S4
                case 804: //S7,S8
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.d(String.format(Locale.US, "case %d: MicrophoneS5(%f)", index, param1));
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_CAMERA, (int) param1);
                    pauseExplain();
                    break;
                case 714://最新扬声器
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
                case 715://获取陀螺角度
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("action =： " + param1);
                    mValue[reValue] = explainerHelper.getmYSensor((int) param1);
                    break;
                default:
                    LogMgr.e("S5ScratchExplainer: NO DEFINED INDEX: " + index);
                    break;
            }

            if (len == funPos) { // 判断程序结束
                sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                LogMgr.e("explain file end");
                doexplain = false;
            }

        } while (!isStop);
    }

    private CameraStateCallBack mCameraStateCallBack=new CameraStateCallBack() {
        @Override
        public void onState(int state) {
            LogMgr.d("相机状态回调："+state);
        }
    };

    @Override
    public void stopExplain() {
        super.stopExplain();
        explainerHelper.stopPlaySound();
        UsbCamera.create().setBrightnessS(ExplainerApplication.instance,0,mCameraStateCallBack);
    }

}
