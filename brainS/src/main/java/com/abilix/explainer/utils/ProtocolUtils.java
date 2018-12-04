package com.abilix.explainer.utils;

import android.os.RemoteException;

import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.utils.Utils;

import java.util.Locale;

public class ProtocolUtils {

    /**
     * 数据头 不需要返回
     */
    public final static int DATA_HEAD = 0x55;

    /**
     * 数据头 返回
     */
    public final static int DATA_HEAD_ = 0x56;

    /**
     * 1 ONE
     */
    public final static int DATA_ONE = 0x01;

    /**
     * 2 TWO
     */
    public final static int DATA_TWO = 0x02;

    /**
     * 3 THREE
     */
    public final static int DATA_THREE = 0x03;

    /**
     * 4 FOUR
     */
    public final static int DATA_FOUR = 0x04;

    /**
     * 5 FIVE
     */
    public final static int DATA_FIVE = 0x05;

    /**
     * 6 SIX
     */
    public final static int DATA_SIX = 0X06;

    /**
     * 0 ZERO
     */
    public final static int DATA_ZERO = 0x00;
    /**
     * 轮子协议
     */
    public final static byte A = 'A';

    public final static byte B = 'B';

    public final static byte C = 'C';

    public final static byte D = 'D';

    public final static byte E = 'E';

    public final static byte F = 'F';

    public final static byte G = 'G';

    public final static byte H = 'H';

    public final static byte I = 'I';

    public final static byte J = 'J';

    public final static byte K = 'K';

    public final static byte L = 'L';

    public final static byte M = 'M';

    public final static byte N = 'N';

    public final static byte O = 'O';

    public final static byte P = 'P';

    public final static byte Q = 'Q';

    public final static byte DR = 'R';

    public final static byte S = 'S';

    public final static byte T = 'T';

    public final static byte U = 'U';

    public final static byte V = 'V';

    public final static byte W = 'W';

    public final static byte X = 'X';

    public final static byte Y = 'Y';

    public final static byte Z = 'Z';

    /**
     * 轮子协议
     */
    public static byte[] WHEELBYTE = {DATA_HEAD, B, L, E, S, E, T, 0X03, 100,
            100, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00};

    /**
     * 头部
     */
    public static byte[] HEADBYTE = {DATA_HEAD, N, E, C, K, M, O, T, 0X00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    /**
     * 设置眼睛颜色
     */
    public static byte[] EYE_COLOR = {DATA_HEAD, E, Y, 1, 0X00, 0X00, 0X00,
            0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00,
            0X00, 0X00};

    /**
     * 设置眼睛数量
     */
    public static byte[] EYE_COUNT = {DATA_HEAD, E, Y, 2, 0X00, 0X00, 0X00,
            0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00,
            0X00, 0X00};

    /**
     * 设置颜色
     */
    public static byte[] COLOR = {DATA_HEAD, C, O, L, E, D, 0X00, 0X00, 0X00,
            0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00};

    /**
     * 设置亮度
     */
    public static byte[] LUMINANCE = {DATA_HEAD, L, U, M, I, N, A, N, C, E,
            0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00};

    /**
     * 设置波形
     */
    public static byte[] WAVEMODE = {DATA_HEAD, W, A, V, E, F, O, DR, M, 0X00,
            0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00};

    /**
     * 设置吸尘
     */
    public static byte[] VACUUM = {DATA_HEAD, B, L, E, S, E, T, 0X04, 0X00,
            0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00, 0X00};
    /**
     * 超声波 协议
     */
    public static final byte[] mUltrasonicByte = {DATA_HEAD_, A, I, DR, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00};

    /**
     * 下视 协议
     */
    public static final byte[] mDown_WatchByte = {DATA_HEAD_, L, O, O, K, D,
            O, W, N, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00};

    /**
     * 后端红外测距
     */
    public static final byte[] mRear_End_InfraredByte = {DATA_HEAD_, I, N, F,
            DR, A, 0x00, 0x00, 0X00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00};

    /**
     * 碰撞测试
     */
    public static final byte[] mCrashInfraredByte = {DATA_HEAD_, C, O, L, L,
            I, S, I, O, N, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00};
    /**
     * 地面灰度 打开
     */
    public static final byte[] mGround_Grayscale_OpenByte = {DATA_HEAD, G, DR,
            A, Y, O, P, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00};
    /**
     * 地面灰度 发送
     */
    public static final byte[] mGround_Grayscale_SendByte = {DATA_HEAD_, G,
            DR, A, Y, DR, D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00};

    /**
     * read缓冲区长度
     */
    private static final int BUFF_LENGTH = 20;

    /**
     * request超时时间 ms
     */
    private static final int REQUEST_TIMEOUT = 100;

    /**
     * request超时重发次数
     */
    private static final int RETRY_TIMES = 3;

    public static void footLight(int mCurrentID,int angleV){
        byte bChecksum = 0;
        byte[] gSendBuff = new byte[9];
        byte[] angles = new byte[2];
        angles = Utils.intToBytesLH(angleV);
        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) mCurrentID;
        gSendBuff[3] = (byte) 5;
        gSendBuff[4] = (byte) 3;
        gSendBuff[5] = (byte) 0x1E;
        System.arraycopy(angles,0,gSendBuff,6,2);
        for (int i = 2; i < gSendBuff.length - 1; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[8] = bChecksum;
        LogMgr.e("gSendBuff = " + Utils.bytesToString(gSendBuff));
        DoWriteFrame(gSendBuff,gSendBuff.length);
    }
    //手部灯光
    public static void handLight(int onOrOff, int mCurrentID){
        byte[] gSendBuff = new byte[8];
        byte bChecksum = 0;
        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) mCurrentID;
        gSendBuff[3] = (byte) 4 ;
        gSendBuff[4] = (byte) 3;
        gSendBuff[5] = (byte) 0x19;
        gSendBuff[6] = (byte)onOrOff;
        for (int i = 2; i <  7; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[7] = bChecksum;
        DoWriteFrame(gSendBuff,gSendBuff.length);
    }
    //固定释放
    public static void freeFix(int mCurrentID,int type){//type,固定释放
        byte[] gSendBuff = new byte[8];
        byte bChecksum = 0;
        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) mCurrentID;
        gSendBuff[3] = (byte) 4 ;
        gSendBuff[4] = (byte) 3;
        gSendBuff[5] = (byte) 0x18;
        gSendBuff[6] = (byte)type;
        for (int i = 2; i <  7; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[7] = bChecksum;
        DoWriteFrame(gSendBuff,gSendBuff.length);
    }
    //舵机归零;H5初始零位
    public static int[] motor_zero = new int[]{  512, 512, 512, 512, 512, 512, 542, 481, 512, 512, 512, 512,
            719, 305, 818, 205, 512, 512, 512, 512, 512, 512};//
    public static void goServePosZeros(){
        int angle1=0;
        byte[] motor_id = new byte[22];
        byte[] pPos = new byte[22*2];
        for (int i = 0; i < 22; i++) {
            angle1 = motor_zero[i];
            motor_id[i] = (byte) (i+1);
            pPos[i*2] = (byte) (angle1 & 0xff);
            pPos[i*2 + 1] = (byte) ((angle1 & 0xff00) >> 8);
        }
        sendEngineAngles((byte) 22, motor_id, pPos);
    }
    public static void goZero(int mCurrentID){
        byte[] gSendBuff = new byte[9];
        int angle = motor_zero[mCurrentID - 1];
        byte bChecksum = 0;
        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) mCurrentID;
        gSendBuff[3] = (byte) 5 ;
        gSendBuff[4] = (byte) 3;
        gSendBuff[5] = (byte) 0x1E;
        gSendBuff[6] = (byte)(angle & 0xff);
        gSendBuff[7] = (byte)((angle & 0xff00) >> 8);
        for (int i = 2; i <  8; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[8] = bChecksum;
        DoWriteFrame(gSendBuff,gSendBuff.length);
    }

    //获取舵机角度
//type 0:获取舵机角度  1：获取零位偏移量
    public static int getSingleServoPos(int mCurrentID,int type) throws RemoteException {
        int getSingleTimes = 3;
        int getAngle = 0;
        byte bChecksum = 0;

        byte[] gSendBuff = new byte[8];
        byte[] sendBuff = new byte[11];
        sendBuff[0] = (byte) 0x02;
        sendBuff[1] = (byte) 0;
        sendBuff[2] = (byte) 8;
        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) mCurrentID;
        gSendBuff[3] = (byte) 4 ;
        gSendBuff[4] = (byte) 2;
        gSendBuff[5] = (byte) 0x24;
        gSendBuff[6] = (byte) 0x02;
        if(type ==1){
            gSendBuff[5] = (byte) 0x0A;
            gSendBuff[6] = (byte) 0x01;
        }
        for (int i = 2; i <  7; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[7] = bChecksum;
        System.arraycopy(gSendBuff,0,sendBuff,3,8);
        while (getSingleTimes> 0 ){
            getSingleTimes --;
            byte[] resquest = SPUtils.request(buildProtocol((byte)0x03,(byte) 0x11,(byte) 0x15,sendBuff),15);
            if(resquest == null){
                LogMgr.d("request = null");
                getAngle = 0;
            }else if( resquest.length >= 21 && (resquest[0] & 0xFF) == 0xAA && (resquest[1] & 0xFF) == 0x55
                    && (resquest[5] & 0xFF) == 0xF0 && (resquest[6] & 0xFF) == 0x0B
                    ){
                if(resquest[16] == (byte) mCurrentID){
                    LogMgr.d("request = " + Utils.bytesToString(resquest));
                    if(type == 0){
//                    getAngle = (resquest[19]  +  resquest[20]<<8);
                        getAngle = (int) (((resquest[20] & 0xFF) << 8) | resquest[19] & 0xFF);
                    }else if(type == 1){
                        getAngle = resquest[19];
                    }
                    break;
                }
            }
        }
        LogMgr.d("getAngle = " + getAngle);
        return getAngle;
    }
    /**
     * 往串口发送一次动作数据
     *
     * @param iCount 舵机个数
     * @param pID 舵机ID号
     * @param pPos 舵机角度
     */
    public static void sendEngineAngles(byte iCount, byte[] pID, byte[] pPos) {
        // FF FF FE 07 83 1E 02 id FF 03 D4
        // byte[] Buffer = new byte[16];
        int iLength = 0;
        byte bChecksum = 0;
        int i = 0;
        // int iTest = 0;

        if (iCount < 1 || iCount > 30)
            return;
        iLength = 8 + iCount * 3;
        byte[] gSendBuff = new byte[iLength];

        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) 0xFE;
        gSendBuff[3] = (byte) (4 + iCount * 3);
        gSendBuff[4] = (byte) 0x83;
        gSendBuff[5] = (byte) 0x1E;
        gSendBuff[6] = 0x02;

        for (i = 0; i < iCount; i++) // 22次
        {
            if (pID[i] < 254) {
                gSendBuff[7 + i * 3] = pID[i];
                // gSendBuff[7+i*3+2] = (byte)((pPos[i]>>8) & 0x00FF);
                // gSendBuff[7+i*3+1] = (byte)((pPos[i] & 0x00FF));

                gSendBuff[7 + i * 3 + 1] = (byte) (pPos[i * 2]);
                gSendBuff[7 + i * 3 + 2] = (byte) (pPos[i * 2 + 1]);
            } else {
                gSendBuff[7 + i * 3] = 0x00;
                gSendBuff[7 + i * 3 + 2] = 0x00;
                gSendBuff[7 + i * 3 + 1] = 0x00;
            }
        }

        for (i = 2; i < iLength - 1; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[iLength - 1] = bChecksum;

        DoWriteFrame(gSendBuff, iLength);
    }

    /**
     * type:0:释放,1:固定   reg:寄存器：0x18 释放上电
     *
     */
    public static void relAndFix(int type,byte reg){
        byte bChecksum = 0;
        byte[] gSendBuff = new byte[8 + 2*22];
        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) 0xFE;
        gSendBuff[3] = (byte) 48;
        gSendBuff[4] = (byte) 0x83;
        gSendBuff[5] = (byte) reg;
        gSendBuff[6] = 0x01;
        for (int i = 0; i < 22; i++) {
            gSendBuff[7 + i*2 ] = (byte) (i + 1);
            gSendBuff[7 + i*2 +1] = (byte) type;
        }
        for (int i = 2; i < gSendBuff.length - 1; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[gSendBuff.length - 1] = bChecksum;
        DoWriteFrame(gSendBuff,gSendBuff.length);
    }

    private static void DoWriteFrame(byte[] pBuf, int dwLen){
        int iLength = dwLen + 3;//构建数据位
        //高位在前，低位在后
        byte[] sendBuff = new byte[iLength];
        sendBuff[0] = (byte) 0x02;
        sendBuff[1] = (byte) ((dwLen >> 8) & 0xFF);
        sendBuff[2] = (byte) (dwLen & 0xFF);
        System.arraycopy(pBuf, 0, sendBuff, 3, dwLen);
        byte[] gSendBuffer = buildProtocol((byte)0x03,(byte) 0x11,(byte) 0x15,sendBuff);
        write(gSendBuffer);
    }
    /**
     * 构造传输命令
     *
     * @param type 1个字节，代表不同的上层(机器人)型号
     * @param cmd1 1个字节，代表主命令
     * @param cmd2 1个字节，代表子命令
     * @param data 可变长度 null表示不带数据
     * @return
     */
    public static byte[] buildProtocol(byte type, byte cmd1, byte cmd2, byte[] data) {
        byte[] sendBuff;
        if (data == null) {
            byte[] buff = new byte[8];
            buff[0] = type;
            buff[1] = cmd1;
            buff[2] = cmd2;
            sendBuff = addProtocol(buff);
        } else {
            byte[] buff = new byte[8 + data.length];
            buff[0] = type;
            buff[1] = cmd1;
            buff[2] = cmd2;
            System.arraycopy(data, 0, buff, 7, data.length);
            sendBuff = addProtocol(buff);
        }

        return sendBuff;
    }

    /**
     * 增加协议头前两个字节为AA 55，三，四字节为长度，最后一个字节为校验位
     * 协议封装： AA 55 len1 len2 type cmd1 cmd2 00 00 00 00 (data) check
     * @param buff 类型，命令字1，命令字2，保留字，数据位
     * @return
     */
    private static byte[] addProtocol(byte[] buff) {
        short len = (short) (buff.length);
        byte[] sendbuff = new byte[len + 4];
        sendbuff[0] = (byte) 0xAA; // 头
        sendbuff[1] = (byte) 0x55;
        sendbuff[3] = (byte) (len & 0x00FF); // 长度: 从type到check
        sendbuff[2] = (byte) ((len >> 8) & 0x00FF);
        System.arraycopy(buff, 0, sendbuff, 4, buff.length); // type - data

        byte check = 0x00; // 校验位
        for (int n = 0; n <= len + 2; n++) {
            check += sendbuff[n];
        }
        sendbuff[len + 3] = (byte) (check & 0x00FF);
        return sendbuff;
    }

    //向STM32发送指令，并读取返回值
    public synchronized static byte[] sendProtocol(byte[] sendBuff) {
        byte[] readBuff = null;
        try {
            LogMgr.d(String.format(Locale.US, "sendProtocol() write:%s", ByteUtils.bytesToString(sendBuff, sendBuff.length)));
            for (int i = 0; i < RETRY_TIMES && readBuff == null; i++) {
                LogMgr.i("sendProtocol() RETRY_TIMES: " + i);
                readBuff = SPUtils.request(sendBuff, REQUEST_TIMEOUT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (readBuff == null) {
            readBuff = new byte[BUFF_LENGTH];
        }
        LogMgr.d(String.format(Locale.US, "sendProtocol()  read:%s", ByteUtils.bytesToString(readBuff, readBuff.length)));
        return readBuff;
    }

    public synchronized static byte[] sendProtocol(byte[] sendBuff, int timeout) {
        byte[] readBuff = null;
        try {
            LogMgr.d(String.format(Locale.US, "sendProtocol() write:%s", ByteUtils.bytesToString(sendBuff, sendBuff.length)));
            for (int i = 0; i < RETRY_TIMES && readBuff == null; i++) {
                LogMgr.i("sendProtocol() RETRY_TIMES: " + i);
                readBuff = SPUtils.request(sendBuff, timeout);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (readBuff == null) {
            readBuff = new byte[BUFF_LENGTH];
        }
        LogMgr.d(String.format(Locale.US, "sendProtocol()  read:%s", ByteUtils.bytesToString(readBuff, readBuff.length)));
        return readBuff;
    }

    public synchronized static byte[] sendProtocol(byte type, byte cmd1, byte cmd2, byte[] data) {
        byte[] sendBuff = buildProtocol(type, cmd1, cmd2, data);
        return sendProtocol(sendBuff);
    }

    public synchronized static byte[] sendProtocol(byte type, byte cmd1, byte cmd2, byte[] data, int timeout) {
        byte[] sendBuff = buildProtocol(type, cmd1, cmd2, data);
        return sendProtocol(sendBuff, timeout);
    }

    public synchronized static void cancelRequestTimeout() {
        try {
            SPUtils.cancelRequestTimeout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //向STM32发送指令，不读取返回值
    public synchronized static void write(byte[] sendBuff) {
        try {
            LogMgr.d(String.format(Locale.US, "write:%s", ByteUtils.bytesToString(sendBuff, sendBuff.length)));
            SPUtils.write(sendBuff);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void write(byte type, byte cmd1, byte cmd2, byte[] data) {
        byte[] sendBuff = buildProtocol(type, cmd1, cmd2, data);
        write(sendBuff);
    }

    public synchronized static void controlSkillPlayer(int state,String filePath) {
        try {
            LogMgr.d(String.format(Locale.US, "controlSkillPlayer: state:%d, filePath:%s", state, filePath));
            //SPUtils.controlSkillPlayer(state, filePath);
            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.ACTION_SERVICE_MODE_LEARN_LETTER, null, filePath, state, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void sendCmdToControl(byte type, byte cmd1, byte cmd2, byte[] data) {
        try {
            byte[] sendBuff = buildProtocol(type, cmd1, cmd2, data);
            LogMgr.d(String.format(Locale.US, "sendCmdToControl() sendBuff: %s", ByteUtils.bytesToString(sendBuff, sendBuff.length)));
            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.ACTION_SERVICE_MODE_OLD_PROTOCOL, sendBuff, null, 1, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void sendPatchCmdToControl(int modeState, byte[] cmdData) {
        try {
            LogMgr.d(String.format(Locale.US, "sendCmdToControl() modeState: %d cmdData: %s", modeState, ByteUtils.bytesToString(cmdData, cmdData.length)));
            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, cmdData, null, modeState, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
