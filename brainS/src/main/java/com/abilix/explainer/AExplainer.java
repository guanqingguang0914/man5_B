package com.abilix.explainer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;

import android.os.Handler;
import android.os.Message;

public abstract class AExplainer implements IExplainer {
    protected final static int GLOBAL_VALUE_ARRAY_LEN = 200;
    protected final static int TEMP_VALUE_ARRAYS_NUM = 20;
    protected int globalFunNodeCount = 0;
    protected FunNode subFunNodeJava = null;
    protected int gloableCount = -1;
    protected boolean doexplain = true;
    protected float mValue[] = new float[GLOBAL_VALUE_ARRAY_LEN];
    protected float[] tempValue = new float[TEMP_VALUE_ARRAYS_NUM * GLOBAL_VALUE_ARRAY_LEN];
    protected String strInfo;
    protected Handler mHandler;
    protected boolean isStop = false;
    protected boolean isSleep = true;

    protected int start = 0;
    protected byte[] filebuf;

    protected int packetProtocolVersion = 0x00;
    protected int packetDataLength = 40;
    protected int packetDataValueIndex = 30;
    protected int packetDataLine1Index = 34;
    protected int packetDataLine2Index = 36;

    public AExplainer(Handler handler) {
        this.mHandler = handler;
        isStop = false;
        isSleep = true;
    }

    public boolean delFunNodeJava() {
        FunNode node = subFunNodeJava;
        if (node != null) {
            subFunNodeJava = node.next;
            node.next = null;
            LogMgr.e(String.format(Locale.US, "====>delFunNodeJava(%d) succeed !", node.pos));
            return true;
        }
        LogMgr.e("====>delFunNodeJava() fail!");

        return false;
    }

    public boolean addFunNodeJava(int pos) {
        FunNode node = new FunNode();
        FunNode curNode = subFunNodeJava;

        while (curNode != null && pos != curNode.pos) {
            curNode = curNode.next;
        }
        if (curNode != null) {
            node.valStart = curNode.valStart;
            LogMgr.e(String.format(Locale.US, "====>addFunNodeJava(%d)-->already exists!! tempValueArrays[%d]", pos, node.valStart / GLOBAL_VALUE_ARRAY_LEN));
        } else {
            node.valStart = globalFunNodeCount * GLOBAL_VALUE_ARRAY_LEN;
            LogMgr.e(String.format(Locale.US, "====>addFunNodeJava(%d)-->globalFunNodeCount=%d", pos, globalFunNodeCount));
            globalFunNodeCount = (globalFunNodeCount + 1) % TEMP_VALUE_ARRAYS_NUM;
        }

        node.pos = pos;
        node.next = subFunNodeJava;
        subFunNodeJava = node;

        return true;
    }

    @Override
    public void pauseExplain() {
        LogMgr.d("pauseExplain()");
        isSleep = true;
        while (isSleep) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void doExplain(String filePath) {
        LogMgr.d("doExplain()");
        //isStop = false;
        isSleep = true;

        byte[] buf = null;
        if (filePath != null) {
            try {
                buf = ByteUtils.readFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
                LogMgr.e("read vjc file error::" + e);
            }
            if (buf != null) {
                if (buf[0] == (byte) 0xAA && buf[1] == (byte) 0x55) {//新协议版本1. length：60
                    packetProtocolVersion = buf[58];
                    packetDataLength = 60;
                    packetDataValueIndex = 50;
                    packetDataLine1Index = 54;
                    packetDataLine2Index = 56;
                } else if (buf[0] == (byte) 0x55 && buf[1] == (byte) 0xAA) {//当前协议：length:40
                    packetProtocolVersion = 0x00;
                    packetDataLength = 40;
                    packetDataValueIndex = 30;
                    packetDataLine1Index = 34;
                    packetDataLine2Index = 36;
                }
                filebuf = Arrays.copyOfRange(buf, packetDataLength, buf.length);
            } else {
                LogMgr.e("vjc file buff is null");
            }
        } else {
            LogMgr.e("vjc file path is null");
        }

    }

    @Override
    public void stopExplain() {
        LogMgr.d("stop explain");
        isStop = true;
        isSleep = false;
    }

    @Override
    public void resumeExplain() {
        LogMgr.d("resume explain");
        isSleep = false;

    }

    public void waitExplain(float seconds) {
        LogMgr.d("waitExplain(" + seconds + " sec)");
        int millis = (int) (seconds * 1000);
        long currentTime;
        long lastTime = System.currentTimeMillis();
        do {
            currentTime = System.currentTimeMillis();
        } while (isSleep && currentTime - lastTime < millis);
    }

    public void sendExplainMessage(int what) {
        sendExplainMessage(what, -1, null);
    }

    public void sendExplainMessage(int what, int arg1) {
        sendExplainMessage(what, arg1, null);
    }

    public void sendExplainMessage(int what, int arg1, Object obj) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.arg1 = arg1;
        msg.obj = obj;
        mHandler.sendMessage(msg);
        switch (what) {
            case ExplainTracker.MSG_EXPLAIN_DISPLAY:
                switch (arg1) {
                    case ExplainTracker.ARG_DISPLAY_PHOTO:
                    case ExplainTracker.ARG_DISPLAY_ANIMATION:
                        waitExplain(0.1f);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        waitExplain(0.002f);
    }

}
