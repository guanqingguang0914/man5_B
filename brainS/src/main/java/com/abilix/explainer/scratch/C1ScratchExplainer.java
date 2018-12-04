package com.abilix.explainer.scratch;

import android.os.Handler;

import com.abilix.explainer.AExplainer;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.FunNode;
import com.abilix.brain.R;
import com.abilix.explainer.helper.C1ExplainerHelper;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

public class C1ScratchExplainer extends AExplainer {
    private boolean isDisplay = false;
    protected C1ExplainerHelper explainerHelper;

    public C1ScratchExplainer(Handler handler) {
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
        globalFunNodeCount = 0;

        // 添加两个时钟变量。赋初始值。
        long init_time = 0;
        long end_time = 0;
        init_time = end_time = System.currentTimeMillis();

        funPos = start;
        // 打开wait函数。
        //explainerHelper.setIssleep(true);
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

                case 0: // getName
                    byte name[] = new byte[20];
                    explainerHelper.getName(name, runBuffer);
                    String temp;
                    try {
                        temp = new String(name, "utf-8");
                        LogMgr.e("case 0 getName is  " + temp);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;

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

                case 390://
                //第一个参数为电机端口：0~1("A","B")，第二个参数为转向0~1 ("正转","反转")，
                //第三个参数为速度：0~100("快","中","慢");
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    LogMgr.e("parms1: " + param1 + " 正反：" + param2 + "  sudu:" + param3);
                    explainerHelper.motorSetCX((int) param1, (int) param2, (int) param3);
                    break;

                case 391://setSoundCx(int, int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    explainerHelper.playMusic((int) param1, (int) param2);
                    break;

                case 392://setLedCx(int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("parms1: " + param1);
                    explainerHelper.setLedCx((int) param1);
                    break;
                
                case 393://setDisplayCx(int,*)  copy from C
                    isDisplay = true;
                    String content1 = "";
                    LogMgr.e("runBuffer1::" + ByteUtils.bytesToString(runBuffer, runBuffer.length));
                    int isvar = runBuffer[10];//0字符  1 照片   2引用变量。
                    LogMgr.e("type: " + isvar);
                    if (isvar == 0) { //0字符
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
                            content1 += new String(str_byte, "UTF-8");
                            LogMgr.e("data_bytes::" + content1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content1);
                    } else if (isvar == 1) { //1 照片
                        param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                        LogMgr.e("picnum:" + param2);
                        //主要是加一个文件路劲判断。
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_PHOTO, String.valueOf((int) param2));
                    } else if (isvar == 2) { //2引用变量
                        float paramV = explainerHelper.getParam(runBuffer, mValue, 15);
                        LogMgr.e("2引用变量: " + paramV);
                        if (Float.isNaN(paramV) || Float.isInfinite(paramV)) {
                            content1 = ExplainerApplication.instance.getString(R.string.guoda);
                        } else {
                            DecimalFormat df = new DecimalFormat("0.000");
                            content1 += df.format(paramV);
                        }
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content1);
                    }
                    break;

                case 394://closeCx(int)//和C的close
                    int type = (int) explainerHelper.getParam(runBuffer, mValue, 10);
                    //explainerHelper.NewCloseCX((int) param1, mHandler);//要传一个handler。
                    switch (type) {
                        case 0:// 关闭电机
                            explainerHelper.C1_runstop();
                            break;
                        case 1:// 关闭扬声器
                            explainerHelper.stopPlaySound();
                            break;
                        case 2:// 关闭LED
                            explainerHelper.C1_CloseLedCx();
                            break;
                        case 3://关闭显示
                            sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                            break;
                        default:
                            break;
                    }
                    break;

                case 395://findBarCx(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.i("duankou: " + param1);
                    mValue[reValue] = explainerHelper.haveObjectCX((int) param1);
                    break;

                case 396://findBarDistanceCx(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.i("duankou: " + param1);
                    mValue[reValue] = explainerHelper.distanceCX((int) param1);
                    break;

                case 397://findObjectCx(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.touchCX((int) param1);
                    break;

                case 398://findColorCx(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.readColorCX((int) param1);
                    break;

                case 399:// getGrayCx(int)
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    mValue[reValue] = explainerHelper.getGraySensorCX((int) param1);
                    LogMgr.i("gray value:" + mValue[reValue]);
                    break;

                case 400:// cameraCx()   拍照暂时没有。
                    //param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    //sendExplainMessage(ExplainTracker.MSG_EXPLAIN_CAMERA, (int) param1);
                    //pauseExplain();
                    break;

                case 401://resettimeCx()
                    init_time = System.currentTimeMillis();
                    break;

                case 402://secondsCx()
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    //Calendar end = Calendar.getInstance();
                    end_time = System.currentTimeMillis();
                    Double b = ((double) (end_time - init_time) / 1000);
                    BigDecimal bd = new BigDecimal(b);
                    bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);//四舍五入。
                    b = bd.doubleValue();
                    mValue[reValue] = Float.parseFloat(b.toString());//double 转float
                    break;

                case 403://InitCompassCx()
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_COMPASS);
                    pauseExplain();
                    break;

                case 404://getCompassCx() 指南针陀螺仪都不能用。
                    //reValue = explainerHelper.getU16(runBuffer, 30);
                    //mValue[reValue] = explainerHelper.getCompass();
                    break;

                case 405://getGyroCx(int) 暂时不能用。
                    //reValue = explainerHelper.getU16(runBuffer, 30);
                    //param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    //LogMgr.e("action =： " + param1);
                    //mValue[reValue] = explainerHelper.position((int) param1);
                    break;

                case 406://MicrophoneCx(int)
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    LogMgr.e("param1: " + param1 + " param2: " + param2);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param2, (int) param1);
                    pauseExplain();
                    break;

                default: // 可捕获未解释执行的函数
                    strInfo += (" -- default(未处理): " + index + "   " + funPos);
                    LogMgr.e("default: ", strInfo);
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
        explainerHelper.stopPlaySound();
        //等待解释执行执行完毕。
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //explainerHelper.setIssleep(false);
        explainerHelper.reSetStm32();
//		takePicture(0);
//		explainerHelper.C_runstop();
//		explainerHelper.C_setled(0);
    }

    @Override
    public void pauseExplain() {
        super.pauseExplain();
    }

}
