package com.abilix.explainer.helper;

import android.os.RemoteException;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.abilix.control.aidl.IPushListener;
import com.abilix.explainer.MotorBytesBuilder;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.ProtocolUtils;
import com.abilix.explainer.utils.SPUtils;

public class CExplainerHelper extends AExplainHelper {

    public int DI(int port) {
        return 0;
    }

    private static boolean ledBlinkFlag = false;
    private final int ledBlinkTimeGap = 500; //ms
    private int lastLedColorSet = 0;

    /**
     * @param colorSet 0:灭 1:红 2:蓝 3:绿
     *                 Chart1.0版本的协议      熄灭-红色-蓝色-绿色 为 0-1-2-3
     *                 Chart2.0版本由于设计改为 红色-蓝色-绿色 为 1-2-3
     * @param mode     0:点亮 1：闪烁
     *                 Chart1.0版本的协议      点亮-闪烁 为 0-1
     *                 Chart2.0版本由于设计改为 开启-关闭 为 0-1 为了兼容以前协议不改协议，做一下处理:当第二个参数状态为关闭时，代码中将第一个参数改为0
     */
    public void setButtonLight(final int colorSet, int mode) {
        switch (mode) {
            case 0: //开启
                ledBlinkFlag = false;
                lastLedColorSet = colorSet;
                setLedRbg(colorSet);
                break;
            case 1: //关闭
                if (colorSet == 0) { //关闭
                    ledBlinkFlag = false;
                    lastLedColorSet = colorSet;
                    setLedRbg(colorSet);
                } else { //闪烁
                    if (ledBlinkFlag && colorSet == lastLedColorSet) {
                        return;
                    }
                    ledBlinkFlag = true;
                    lastLedColorSet = colorSet;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long lastTime;
                            long currentTime;
                            while (ledBlinkFlag) {
                                //设置LED颜色
                                setLedRbg(colorSet);
                                //延时操作
                                currentTime = System.currentTimeMillis();
                                lastTime = currentTime;
                                while (ledBlinkFlag && currentTime - lastTime < ledBlinkTimeGap) {
                                    currentTime = System.currentTimeMillis();
                                }
                                //关闭LED
                                setLedRbg(0);
                                //延时操作
                                currentTime = System.currentTimeMillis();
                                lastTime = currentTime;
                                while (ledBlinkFlag && currentTime - lastTime < ledBlinkTimeGap) {
                                    currentTime = System.currentTimeMillis();
                                }
                            }
                        }
                    }).start();
                }
                break;
        }
    }

    // type:x,y 0,1 color:红绿蓝黄黑白 0,1,2,3,4,5
    public int getImgResponse(int type, int color) {
        int color_value = 0;
        switch (color) {
            case 0:
                color_value = 0xB40703;
                break;
            case 1:
                color_value = 0x00FF00;
                break;
            case 2:
                color_value = 0x0000FF;
                break;
            case 3:
                color_value = 0xFFFF00;
                break;
            case 4:
                color_value = 0x000000;
                break;
            case 5:
                color_value = 0xFFFFFF;
                break;
        }

        // return mColorBlockInterface.getImagePos(type, color_value);
        return 0;
    }

    public int getColorForce() {
        int[] hsl = getColorHSL();
        return hsl[2] / 2;
    }

    @Override
    public int getMotoParam(byte[] buffer, float[] val, int index) {

        int ff = 3;
        int i;
        int res = buffer[index];

        int pVel = ByteUtils.byte2int_2byte(buffer, index + 1);
        if (res == 1) {
            i = (int) pVel;
            ff = (int) val[i];
        } else {
            ff = pVel;
        }
        return ff;
    }

    public int getcolor(int[] color) {
        int[] res = new int[8];

        for (int i = 0; i < color.length; i++) {
            LogMgr.e("color value: " + color[i]);
            if (color[i] == 0) {
                res[0]++;
            } else if (color[i] == 1) {
                res[1]++;
            } else if (color[i] == 2) {
                res[2]++;
            } else if (color[i] == 3) {
                res[3]++;
            } else if (color[i] == 4) {
                res[4]++;
            } else if (color[i] == 5) {
                res[5]++;
            } else if (color[i] == 6) {
                res[6]++;
            } else {
                res[7]++;
            }
        }
        int max = 0, index = -1;
        for (int j = 0; j < res.length; j++) {
            if (res[j] > max) {
                max = res[j];
                index = j;
            }

        }
        // 这里利用buf的index跟颜色的数据重合。
        return index;
    }

    private Timer allMotor_timerA;
    private Timer allMotor_timerB;
    private Timer allMotor_timerC;
    private Timer allMotor_timerD;

    private TimerTask allMotor_timerTaskA;
    private TimerTask allMotor_timerTaskB;
    private TimerTask allMotor_timerTaskC;
    private TimerTask allMotor_timerTaskD;

    private Timer timerMotor;
    private TimerTask timerTaskMotor;

    private boolean motorTimerFlag = false;
    private boolean motorFeedBackFlag = false;

    private int[] mMotorFeedBackIdSets = new int[4];
    private int mMotorFeedBackCount = 0;

    public void setAllMotor(byte[] set_bytes, byte[] value_bytes, byte[] speed_bytes, final int motorType, float[] mValue) {
        byte[] colose_loop_motor_protocol_bytes = new byte[21];
        byte[] open_loop_motor_protocol_bytes = new byte[5];
        byte[] motorState = getMotorState(set_bytes);
        int[] idSets = new int[4];
        int[] motorTypes = new int[4];
        int[] modes = new int[4];
        int[] speeds = new int[4];
        int[] values = new int[4];
        int motorTimerMaxTimeout = 0;
        mMotorFeedBackCount = 0;
        for (int i = 0; i < idSets.length; i++) {
            idSets[i] = set_bytes[i] >> 7 & 0x01;
            motorTypes[i] = set_bytes[i] >> 6 & 0x01;
            modes[i] = set_bytes[i] >> 2 & 0x0F;
            speeds[i] = speed_bytes[i] & 0xFF;
            values[i] = 0;
            if (idSets[i] == 0x01 && motorTypes[i] == 0x00 && (modes[i] == 0x01 || modes[i] == 0x02)) {
                mMotorFeedBackIdSets[i] = 0x01;
                mMotorFeedBackCount++;
            } else {
                mMotorFeedBackIdSets[i] = 0x00;
            }
        }
        motorTimerFlag = false;
        motorFeedBackFlag = false;

        LogMgr.d("motor state::" + ByteUtils.bytesToString(motorState, motorState.length));
        // 被设置成开环时，取消定时器
        if (((motorState[0] & 0x08) == 0x08)) {
            if (allMotor_timerA != null) {
                allMotor_timerA.cancel();
                allMotor_timerA = null;
                LogMgr.e("A电机被设置成开环，取消之前闭环设置的时间");
            }
            if (allMotor_timerTaskA != null) {
                allMotor_timerTaskA.cancel();
                allMotor_timerTaskA = null;
            }
        }
        if (((motorState[0] & 0x04) == 0x04)) {
            if (allMotor_timerB != null) {
                allMotor_timerB.cancel();
                allMotor_timerB = null;
                LogMgr.e("B电机被设置成开环，取消之前闭环设置的时间");
            }

            if (allMotor_timerTaskB != null) {
                allMotor_timerTaskB.cancel();
                allMotor_timerTaskB = null;
            }
        }
        if ((motorState[0] & 0x02) == 0x02) {
            if (allMotor_timerC != null) {
                allMotor_timerC.cancel();
                allMotor_timerC = null;
                LogMgr.e("C电机被设置成开环，取消之前闭环设置的时间");
            }
            if (allMotor_timerTaskC != null) {
                allMotor_timerTaskC.cancel();
                allMotor_timerTaskC = null;
            }
        }
        if ((motorState[0] & 0x01) == 0x01) {
            if (allMotor_timerD != null) {
                allMotor_timerD.cancel();
                allMotor_timerD = null;
                LogMgr.e("D电机被设置成开环，取消之前闭环设置的时间");
            }
            if (allMotor_timerTaskD != null) {
                allMotor_timerTaskD.cancel();
                allMotor_timerTaskD = null;
            }
        }

        // 闭环电机
        if (motorState[1] != 0) {
            colose_loop_motor_protocol_bytes[0] = motorState[1];
            byte[] close_loop_motor_a_bytes = new MotorBytesBuilder().setMotorPort(0).setMotorBytes(new byte[5]).setMotorType(motorType).setValueType(set_bytes)
                    .setCloseLoopSpeed(set_bytes, speed_bytes, mValue).setTypeValue(set_bytes, value_bytes, mValue).build();
            byte[] close_loop_motor_b_bytes = new MotorBytesBuilder().setMotorPort(1).setMotorBytes(new byte[5]).setMotorType(motorType).setValueType(set_bytes)
                    .setCloseLoopSpeed(set_bytes, speed_bytes, mValue).setTypeValue(set_bytes, value_bytes, mValue).build();
            byte[] close_loop_motor_c_bytes = new MotorBytesBuilder().setMotorPort(2).setMotorBytes(new byte[5]).setMotorType(motorType).setValueType(set_bytes)
                    .setCloseLoopSpeed(set_bytes, speed_bytes, mValue).setTypeValue(set_bytes, value_bytes, mValue).build();
            byte[] close_loop_motor_d_bytes = new MotorBytesBuilder().setMotorPort(3).setMotorBytes(new byte[5]).setMotorType(motorType).setValueType(set_bytes)
                    .setCloseLoopSpeed(set_bytes, speed_bytes, mValue).setTypeValue(set_bytes, value_bytes, mValue).build();
            // 拼装闭环电机协议
            System.arraycopy(close_loop_motor_a_bytes, 0, colose_loop_motor_protocol_bytes, 1, 5);
            System.arraycopy(close_loop_motor_b_bytes, 0, colose_loop_motor_protocol_bytes, 6, 5);
            System.arraycopy(close_loop_motor_c_bytes, 0, colose_loop_motor_protocol_bytes, 11, 5);
            System.arraycopy(close_loop_motor_d_bytes, 0, colose_loop_motor_protocol_bytes, 16, 5);
            LogMgr.d("set close loop motor data bytes::" + ByteUtils.bytesToString(colose_loop_motor_protocol_bytes, colose_loop_motor_protocol_bytes.length));
            try {
                LogMgr.d("发送闭环电机命令");
                ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x03, colose_loop_motor_protocol_bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 开环电机
        if (motorState[0] != 0) {
            open_loop_motor_protocol_bytes[0] = motorState[0];
            byte[] open_loop_motor_a_bytes = new MotorBytesBuilder().setMotorPort(0).setMotorBytes(new byte[1]).setOpenLoopSpeed(set_bytes, speed_bytes, mValue).build();
            byte[] open_loop_motor_b_bytes = new MotorBytesBuilder().setMotorPort(1).setMotorBytes(new byte[1]).setOpenLoopSpeed(set_bytes, speed_bytes, mValue).build();
            byte[] open_loop_motor_c_bytes = new MotorBytesBuilder().setMotorPort(2).setMotorBytes(new byte[1]).setOpenLoopSpeed(set_bytes, speed_bytes, mValue).build();
            byte[] open_loop_motor_d_bytes = new MotorBytesBuilder().setMotorPort(3).setMotorBytes(new byte[1]).setOpenLoopSpeed(set_bytes, speed_bytes, mValue).build();
            // 拼装开环电机协议
            System.arraycopy(open_loop_motor_a_bytes, 0, open_loop_motor_protocol_bytes, 1, 1);
            System.arraycopy(open_loop_motor_b_bytes, 0, open_loop_motor_protocol_bytes, 2, 1);
            System.arraycopy(open_loop_motor_c_bytes, 0, open_loop_motor_protocol_bytes, 3, 1);
            System.arraycopy(open_loop_motor_d_bytes, 0, open_loop_motor_protocol_bytes, 4, 1);
            LogMgr.d("set open loop motor data bytes::" + ByteUtils.bytesToString(open_loop_motor_protocol_bytes, open_loop_motor_protocol_bytes.length));
            try {
                LogMgr.d("发送开环电机命令");
                ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x13, open_loop_motor_protocol_bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (((set_bytes[0] >> 7) & 0x01) == 0x01 && ((set_bytes[0] >> 2) & 0x0F) == 0x03 && ((set_bytes[0] >> 6) & 0x01) == 0) {//A 数值类型(3-时间) 闭环
            if (allMotor_timerA != null) {
                allMotor_timerA.cancel();
                allMotor_timerA = null;
            }
            if (allMotor_timerTaskA != null) {
                allMotor_timerTaskA.cancel();
                allMotor_timerTaskA = null;
            }
            allMotor_timerA = new Timer();
            allMotor_timerTaskA = new TimerTask() {

                @Override
                public void run() {
                    setMotorCloseloop(0, motorType, 0, 0, 0);
                }
            };

            boolean valueIsReferenceVariable = (set_bytes[0] & 0x02) == 0x02;
            LogMgr.d("port" + 0 + "时间值是否是引用变量::" + valueIsReferenceVariable);
            int typeValue = ByteUtils.byte2int_2byteHL(value_bytes, 0);
            if (valueIsReferenceVariable) {
                int valuePosition = typeValue;
                float value = getMvalue(mValue, valuePosition);
                LogMgr.d("引用变量位置::" + valuePosition + "    " + "引用变量的值:" + value);
                typeValue = (int) value;
            }
            LogMgr.d(" A motor time::" + typeValue);
            allMotor_timerA.schedule(allMotor_timerTaskA, typeValue * 1000);
            values[0] = typeValue;
        }
        if (((set_bytes[1] >> 7) & 0x01) == 0x01 && ((set_bytes[1] >> 2) & 0x0F) == 0x03 && ((set_bytes[1] >> 6) & 0x01) == 0) {//B 数值类型(3-时间) 闭环
            if (allMotor_timerB != null) {
                allMotor_timerB.cancel();
                allMotor_timerB = null;
            }
            if (allMotor_timerTaskB != null) {
                allMotor_timerTaskB.cancel();
                allMotor_timerTaskB = null;
            }
            allMotor_timerB = new Timer();
            allMotor_timerTaskB = new TimerTask() {

                @Override
                public void run() {
                    setMotorCloseloop(1, motorType, 0, 0, 0);
                }
            };
            boolean valueIsReferenceVariable = (set_bytes[1] & 0x02) == 0x02;
            LogMgr.d("port" + 1 + "时间值是否是引用变量::" + valueIsReferenceVariable);
            int typeValue = ByteUtils.byte2int_2byteHL(value_bytes, 2);
            if (valueIsReferenceVariable) {
                int valuePosition = typeValue;
                float value = getMvalue(mValue, valuePosition);
                LogMgr.d("引用变量位置::" + valuePosition + "    " + "引用变量的值:" + value);
                typeValue = (int) value;
            }
            LogMgr.d("B motor time::" + typeValue);
            allMotor_timerB.schedule(allMotor_timerTaskB, typeValue * 1000);
            values[1] = typeValue;
        }
        if (((set_bytes[2] >> 7) & 0x01) == 0x01 && ((set_bytes[2] >> 2) & 0x0F) == 0x03 && ((set_bytes[2] >> 6) & 0x01) == 0) {//C 数值类型(3-时间) 闭环
            if (allMotor_timerC != null) {
                allMotor_timerC.cancel();
                allMotor_timerC = null;
            }
            if (allMotor_timerTaskC != null) {
                allMotor_timerTaskC.cancel();
                allMotor_timerTaskC = null;
            }
            allMotor_timerC = new Timer();
            allMotor_timerTaskC = new TimerTask() {

                @Override
                public void run() {
                    setMotorCloseloop(2, motorType, 0, 0, 0);
                }
            };
            boolean valueIsReferenceVariable = (set_bytes[2] & 0x02) == 0x02;
            LogMgr.d("port" + 2 + "时间值是否是引用变量::" + valueIsReferenceVariable);
            int typeValue = ByteUtils.byte2int_2byteHL(value_bytes, 4);
            if (valueIsReferenceVariable) {
                int valuePosition = typeValue;
                float value = getMvalue(mValue, valuePosition);
                LogMgr.d("引用变量位置::" + valuePosition + "    " + "引用变量的值:" + value);
                typeValue = (int) value;
            }
            LogMgr.d("C motor time::" + typeValue);
            allMotor_timerC.schedule(allMotor_timerTaskC, typeValue * 1000);
            values[2] = typeValue;
        }
        if (((set_bytes[3] >> 7) & 0x01) == 0x01 && ((set_bytes[3] >> 2) & 0x0F) == 0x03 && ((set_bytes[3] >> 6) & 0x01) == 0) {//D 数值类型(3-时间) 闭环
            if (allMotor_timerD != null) {
                allMotor_timerD.cancel();
                allMotor_timerD = null;
            }
            if (allMotor_timerTaskD != null) {
                allMotor_timerTaskD.cancel();
                allMotor_timerTaskD = null;
            }
            allMotor_timerD = new Timer();
            allMotor_timerTaskD = new TimerTask() {

                @Override
                public void run() {
                    setMotorCloseloop(3, motorType, 0, 0, 0);
                }
            };
            boolean valueIsReferenceVariable = (set_bytes[3] & 0x02) == 0x02;
            LogMgr.d("port" + 3 + "时间值是否是引用变量::" + valueIsReferenceVariable);
            int typeValue = ByteUtils.byte2int_2byteHL(value_bytes, 6);
            if (valueIsReferenceVariable) {
                int valuePosition = typeValue;
                float value = getMvalue(mValue, valuePosition);
                LogMgr.d("引用变量位置::" + valuePosition + "    " + "引用变量的值:" + value);
                typeValue = (int) value;
            }
            LogMgr.d("D motor time::" + typeValue);
            allMotor_timerD.schedule(allMotor_timerTaskD, typeValue * 1000);
            values[3] = typeValue;
        }
        if (timerMotor != null) {
            timerMotor.cancel();
            timerMotor = null;
        }
        if (timerTaskMotor != null) {
            timerTaskMotor.cancel();
            timerTaskMotor = null;
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] > motorTimerMaxTimeout) {
                motorTimerMaxTimeout = values[i];
            }
        }
        if (motorTimerMaxTimeout > 0) {
            timerMotor = new Timer("timerMotor");
            timerTaskMotor = new TimerTask() {
                @Override
                public void run() {
                    motorTimerFlag = true;
                    LogMgr.e("setAllMotor() motorTimerFlag = " + motorTimerFlag);
                    if (motorTimerFlag && motorFeedBackFlag) {
                        iswait = false;
                        LogMgr.e("setAllMotor() setIswait(false)");
                    }
                }
            };
            timerMotor.schedule(timerTaskMotor, motorTimerMaxTimeout * 1000);
        }
        motorTimerFlag = motorTimerMaxTimeout <= 0;
        motorFeedBackFlag = mMotorFeedBackCount <= 0;
        LogMgr.e("setAllMotor() motorTimerMaxTimeout:" + motorTimerMaxTimeout + " mMotorFeedBackCount:" + mMotorFeedBackCount);
        if (mMotorFeedBackCount > 0 || motorTimerMaxTimeout > 0) {
            waitReveice();
        }
    }

    public void setAllMotorStop() {
        if (allMotor_timerA != null) {
            allMotor_timerA.cancel();
            allMotor_timerA = null;
        }
        if (allMotor_timerTaskA != null) {
            allMotor_timerTaskA.cancel();
            allMotor_timerTaskA = null;
        }
        if (allMotor_timerB != null) {
            allMotor_timerB.cancel();
            allMotor_timerB = null;
        }

        if (allMotor_timerTaskB != null) {
            allMotor_timerTaskB.cancel();
            allMotor_timerTaskB = null;
        }
        if (allMotor_timerC != null) {
            allMotor_timerC.cancel();
            allMotor_timerC = null;
        }
        if (allMotor_timerTaskC != null) {
            allMotor_timerTaskC.cancel();
            allMotor_timerTaskC = null;
        }
        if (allMotor_timerD != null) {
            allMotor_timerD.cancel();
            allMotor_timerD = null;
        }
        if (allMotor_timerTaskD != null) {
            allMotor_timerTaskD.cancel();
            allMotor_timerTaskD = null;
        }
        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x14, null);
    }

    public byte[] getMotorState(byte[] set_bytes) {
        byte[] motorState = new byte[2];
        int openLoopMotorState = 0;
        int closeLoopMotorState = 0;
        /*for (int i = 0; i < set_bytes.length; i++) {
            int set = (set_bytes[i] & 0x80) >> (i + 2);
            if (((set_bytes[i] >> 6) & 0x01) == 0x01) {
                openLoopMotorState += set;
                LogMgr.d(String.format(Locale.US, "开环电机[%d]端口设置状态:%#X", i, set & 0xFF));
            } else {
                closeLoopMotorState += set;
                LogMgr.d(String.format(Locale.US, "闭环电机[%d]端口设置状态:%#X", i, set & 0xFF));
            }
        }*/
        if ((int) ((set_bytes[0] >> 6) & 0x01) == 1) {
            openLoopMotorState += (int) ((set_bytes[0] >> 4) & 0x08);
            LogMgr.d("开环电机A端口设置状态:" + (int) ((set_bytes[0] >> 4) & 0x08));
            closeLoopMotorState += 0;
        } else {
            openLoopMotorState += 0;
            closeLoopMotorState += (int) ((set_bytes[0] >> 4) & 0x08);
            LogMgr.d("闭环电机A端口设置状态:" + (int) ((set_bytes[0] >> 4) & 0x08));
        }
        if ((int) ((set_bytes[1] >> 6) & 0x01) == 1) {
            openLoopMotorState += (int) ((set_bytes[1] >> 5) & 0x04);
            LogMgr.d("开环电机B端口设置状态:" + (int) ((set_bytes[1] >> 5) & 0x04));
            closeLoopMotorState += 0;
        } else {
            openLoopMotorState += 0;
            closeLoopMotorState += (int) ((set_bytes[1] >> 5) & 0x04);
            LogMgr.d("闭环电机B端口设置状态:" + (int) ((set_bytes[1] >> 5) & 0x04));
        }
        if ((int) ((set_bytes[2] >> 6) & 0x01) == 1) {
            openLoopMotorState += (int) ((set_bytes[2] >> 6) & 0x02);
            LogMgr.d("开环电机C端口设置状态:" + (int) ((set_bytes[2] >> 6) & 0x02));
            closeLoopMotorState += 0;
        } else {
            openLoopMotorState += 0;
            closeLoopMotorState += (int) ((set_bytes[2] >> 6) & 0x02);
            LogMgr.d("闭环电机C端口设置状态:" + (int) ((set_bytes[2] >> 6) & 0x02));
        }
        if ((int) ((set_bytes[3] >> 6) & 0x01) == 1) {
            openLoopMotorState += (int) ((set_bytes[3] >> 7) & 0x01);
            LogMgr.d("开环电机D端口设置状态:" + (int) ((set_bytes[3] >> 7) & 0x01));
            closeLoopMotorState += 0;
        } else {
            openLoopMotorState += 0;
            closeLoopMotorState += (int) ((set_bytes[3] >> 7) & 0x01);
            LogMgr.d("闭环电机D端口设置状态:" + (int) ((set_bytes[3] >> 7) & 0x01));
        }
        motorState[0] = (byte) openLoopMotorState;
        motorState[1] = (byte) closeLoopMotorState;
        return motorState;
    }


    /***********************************巡线部分*********************************************/
    public void init_7(int DC_L, int mDC_L, int DC_R, int mDC_R, int IO1, int IO2, int IO3, int IO4, int IO5, int IO6, int IO7, int line, int offset, int type, int bigOrSmall) {

        byte[] data = new byte[15];
        data[0] = (byte) (DC_L & 0x00FF);
        data[1] = (byte) (mDC_L & 0x00FF);
        data[2] = (byte) (DC_R & 0x00FF);
        data[3] = (byte) (mDC_R & 0x00FF);
        data[4] = (byte) (IO1 & 0x00FF);
        data[5] = (byte) (IO2 & 0x00FF);
        data[6] = (byte) (IO3 & 0x00FF);
        data[7] = (byte) (IO4 & 0x00FF);
        data[8] = (byte) (IO5 & 0x00FF);
        data[9] = (byte) (IO6 & 0x00FF);
        data[10] = (byte) (IO7 & 0x00FF);
        data[11] = (byte) (line & 0x00FF);
        data[12] = (byte) (offset & 0x00FF);
        data[13] = (byte) (type & 0x00FF);
        data[14] = (byte) (bigOrSmall & 0x00FF);

        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x0A, data);
    }

    // 12字节： 第1字节：代表左轮电机所接DC口编号；
    // 第2字节：代表左轮电机输出系数，范围0~200 (底层转换为-1.0~1.0)
    // 第3字节：代表右轮电机所接DC口编号；
    // 第4字节：代表右轮电机输出系数，范围0~200 (底层转换为-1.0~1.0)
    // 第5-9字节：分别代表第1-5个灰度所接I/O编号
    // 第10字节：0代表白底黑线，1代表黑底白线
    // 第11字节：灰度临界值偏移，范围0~100 （底层转换为0~1.0）
    // 第12字节：电机类型，0表示开环，1表示闭环
    // 第13字节：电机选择，0x00表示小电机，0x01表示大电机

    public void init_5(int DC_L, int mDC_L, int DC_R, int mDC_R, int IO1, int IO2, int IO3, int IO4, int IO5, int line, int offset, int type, int bigOrSmall) {

        byte[] data = new byte[13];
        data[0] = (byte) DC_L;
        data[1] = (byte) mDC_L;
        data[2] = (byte) DC_R;
        data[3] = (byte) mDC_R;
        data[4] = (byte) IO1;
        data[5] = (byte) IO2;
        data[6] = (byte) IO3;
        data[7] = (byte) IO4;
        data[8] = (byte) IO5;
        data[9] = (byte) line;
        data[10] = (byte) offset;
        data[11] = (byte) type;
        data[12] = (byte) bigOrSmall;

        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x09, data);
    }

    private boolean iswait = true;
    private String className = this.getClass().getName();
    private IPushListener iPushListener = new IPushListener.Stub() {
        @Override
        public void onPush(byte[] data) throws RemoteException {
            LogMgr.e("IPushListener->onPush():" + ByteUtils.bytesToString(data, data.length));
            if (data[5] == (byte) 0xF0 && data[6] == (byte) 0x25) {//巡线上报
                iswait = false;
            } else if (data[5] == (byte) 0xF0 && data[6] == (byte) 0x2D) {//电机上报
                //aa 66 00 11 01 f0 2d 00 00 00 10 04 00 03 01 01 02 01 03 01 5f
                int checkCount = 0;
                for (int i = 0; i < mMotorFeedBackIdSets.length; i++) {
                    if (mMotorFeedBackIdSets[i] == 0x01 && data[12 + i * 2 + 1] == 0x03) {
                        checkCount++;
                    }
                }
                if (checkCount == mMotorFeedBackCount) {
                    LogMgr.e("IPushListener->onPush() motorFeedBackFlag = " + motorFeedBackFlag);
                    motorFeedBackFlag = true;
                }
                if (motorTimerFlag && motorFeedBackFlag) {
                    iswait = false;
                    LogMgr.e("IPushListener->onPush() setIswait(false)");
                }
            }
        }
    };

    public void setIswait(boolean iswait) {
        this.iswait = iswait;
        if (iswait) {
            try {
                SPUtils.registerPush(iPushListener, className);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                SPUtils.unregisterPush(iPushListener, className);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void waitReveice() {
        iswait = true;// 这个方法默认是 等待的，停止按钮要退出的。
        while (iswait) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void readandsend(int num) {

        if (num == 1) {
            // 这里是7灰度的值 1是线 2是背景。
            File mfile = new File(FileUtils.AI_ENVIRONMENT1);
            LogMgr.e("---" + !mfile.exists());
            if (!mfile.exists()) { // 如果文件不存在。

                environment(1000, 1000, 1000, 1000, 1000, 1000, 1000);
                return;
            }
            String grayvalue1 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT1);
            String[] temp = null;
            temp = grayvalue1.split(",");

            String grayvalue2 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT2);
            String[] temp2 = null;
            temp2 = grayvalue2.split(",");
            if (grayvalue1 != null && grayvalue2 != null && temp2.length > 6 && temp.length > 6) {

                int AI0 = (Integer.parseInt(temp[0]) + Integer.parseInt(temp2[0])) / 2;
                int AI1 = (Integer.parseInt(temp[1]) + Integer.parseInt(temp2[1])) / 2;
                int AI2 = (Integer.parseInt(temp[2]) + Integer.parseInt(temp2[2])) / 2;
                int AI3 = (Integer.parseInt(temp[3]) + Integer.parseInt(temp2[3])) / 2;
                int AI4 = (Integer.parseInt(temp[4]) + Integer.parseInt(temp2[4])) / 2;
                int AI5 = (Integer.parseInt(temp[5]) + Integer.parseInt(temp2[5])) / 2;
                int AI6 = (Integer.parseInt(temp[6]) + Integer.parseInt(temp2[6])) / 2;

                LogMgr.e("AI0:" + AI0 + "," + "AI1:" + AI1 + "," + "AI2:" + AI2 + "," + "AI3:" + AI3 + "," + "AI4:" + AI4 + "," + "AI5:" + AI5 + "," + "AI6:" + AI6 + ",");
                environment(AI0, AI1, AI2, AI3, AI4, AI5, AI6);

            } else {

                environment(1000, 1000, 1000, 1000, 1000, 1000, 1000);
            }

        } else {
            // 这里是5灰度的值。
            File mfile = new File(FileUtils.AI_ENVIRONMENT1);
            LogMgr.e("---" + !mfile.exists());
            if (!mfile.exists()) {// 如果文件不存在。

                environment(1000, 0, 1000, 1000, 1000, 0, 1000);
                return;
            }

            String grayvalue1 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT1);
            String[] temp = null;
            temp = grayvalue1.split(",");

            String grayvalue2 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT2);
            String[] temp2 = null;
            temp2 = grayvalue2.split(",");
            if (grayvalue1 != null && grayvalue2 != null) {

                int AI0 = (Integer.parseInt(temp[0]) + Integer.parseInt(temp2[0])) / 2;
                int AI1 = (Integer.parseInt(temp[1]) + Integer.parseInt(temp2[1])) / 2;
                int AI2 = (Integer.parseInt(temp[2]) + Integer.parseInt(temp2[2])) / 2;
                int AI3 = (Integer.parseInt(temp[3]) + Integer.parseInt(temp2[3])) / 2;
                int AI4 = (Integer.parseInt(temp[4]) + Integer.parseInt(temp2[4])) / 2;

                LogMgr.e("AI0:" + AI0 + "," + "AI1:" + AI1 + "," + "AI2:" + AI2 + "," + "AI3:" + AI3 + "," + "AI4:" + AI4 + "," + "AI5:");
                environment(AI0, 0, AI1, AI2, AI3, 0, AI4);// init之后应该可以这么发。

            } else {

                environment(1000, 0, 1000, 1000, 1000, 0, 1000);// 默认值1000.
            }

        }

    }

    public void environment(int a0, int a1, int a2, int a3, int a4, int a5, int a6) {
        // int value = 800;
        byte[] data = new byte[14];

        data[0] = (byte) ((a0 >> 8) & 0xff);
        data[1] = (byte) (a0 & 0xff);

        data[2] = (byte) ((a1 >> 8) & 0xff);
        data[3] = (byte) (a1 & 0xff);

        data[4] = (byte) ((a2 >> 8) & 0xff);
        data[5] = (byte) (a2 & 0xff);

        data[6] = (byte) ((a3 >> 8) & 0xff);
        data[7] = (byte) (a3 & 0xff);

        data[8] = (byte) ((a4 >> 8) & 0xff);
        data[9] = (byte) (a4 & 0xff);

        data[10] = (byte) ((a5 >> 8) & 0xff);
        data[11] = (byte) (a5 & 0xff);

        data[12] = (byte) ((a6 >> 8) & 0xff);
        data[13] = (byte) (a6 & 0xff);

        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x0B, data);
    }

    public void environment() {
        int value = 800;
        byte[] data = new byte[14];

        data[0] = (byte) ((value >> 8) & 0xff);
        data[1] = (byte) (value & 0xff);

        data[2] = (byte) ((value >> 8) & 0xff);
        data[3] = (byte) (value & 0xff);

        data[4] = (byte) ((value >> 8) & 0xff);
        data[5] = (byte) (value & 0xff);

        data[6] = (byte) ((value >> 8) & 0xff);
        data[7] = (byte) (value & 0xff);

        data[8] = (byte) ((value >> 8) & 0xff);
        data[9] = (byte) (value & 0xff);

        data[10] = (byte) ((value >> 8) & 0xff);
        data[11] = (byte) (value & 0xff);

        data[12] = (byte) ((value >> 8) & 0xff);
        data[13] = (byte) (value & 0xff);

        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x0B, data);
    }

    public void line_road(int N, int Crossing, int speed, int Lcut, int Rcut, int stop, float time) {
        // 11字节： 第1-2字节：代表循环次数，范围0~4095
        // 第3字节： 代表路口类型，0代表左侧路口，1代表右侧路口
        // 第4字节： 代表巡线速度，范围10~100
        // 第5字节： 代表左转差速，范围0~100
        // 第6字节： 代表右转差速，范围0~100
        // 第7字节： 代表结束后是否停车，0是，1否
        // 第8~11字节：代表冲路口时间，范围0~60.000 (float)

        byte[] data = new byte[11];
        byte[] temp = ByteUtils.float2byte(time);

        data[0] = (byte) ((N >> 8) & 0xFF);
        data[1] = (byte) (N & 0xFF);

        data[2] = (byte) Crossing;

        data[3] = (byte) (speed & 0xff);

        data[4] = (byte) (Lcut & 0xff);

        data[5] = (byte) (Rcut & 0xff);

        data[6] = (byte) stop;

        data[7] = temp[3];
        data[8] = temp[2];
        data[9] = temp[1];
        data[10] = temp[0];

        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x0C, data);
    }

    //按时巡线。
    public void LineWay_T(int speed, int Lcut, int Rcut, int stop, float time) {

        // 8字节： 第1字节： 代表巡线速度，范围10~100
        // 第2字节： 代表左转差速，范围0~100
        // 第3字节： 代表右转差速，范围0~100
        // 第4字节： 代表结束后是否停车，1是，0否
        // 第5~8字节：代表按时巡线时间，范围0~60.000 (float)

        byte[] data = new byte[8];
        byte[] temp = ByteUtils.float2byte(time);

        data[0] = (byte) (speed & 0xff);
        data[1] = (byte) (Lcut & 0xff);
        data[2] = (byte) (Rcut & 0xff);
        data[3] = (byte) stop;

        data[4] = temp[3];
        data[5] = temp[2];
        data[6] = temp[1];
        data[7] = temp[0];

        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x0D, data);
    }

    // 8字节： 第1字节： 代表IO端口号
    // 第2字节： 代表比较符，范围：0:<;1:<=;2:==;3:!=;4:>=;5:>;
    // 第3-4字节：代表阈值参数，范围0~4095
    // 第5字节： 代表巡线速度，范围0~100
    // 第6字节： 代表左转差速，范围0~100
    // 第7字节： 代表右转差速，范围0~100
    // 第8字节： 代表结束后是否停车，0是，1否 高级寻线。
    public void LineWay_O(int IO, int operator, int reference, int speed, int Lcut, int Rcut, int stop) {

        byte[] data = new byte[8];

        data[0] = (byte) IO;
        data[1] = (byte) operator;
        data[2] = (byte) ((reference >> 8) & 0xFF);
        data[3] = (byte) (reference & 0xFF);

        data[4] = (byte) speed;
        data[5] = (byte) Lcut;
        data[6] = (byte) Rcut;
        data[7] = (byte) stop;

        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x0E, data);
    }

    // 5字节： 第1字节： 代表左电机速度，范围0~200 (底层转换为-100~100)
    // 第2字节： 代表右电机速度，范围0~200 (底层转换为-100~100)
    // 第3字节： 代表结束后是否停车，0是，1否
    // 第4字节： 代表过线条数，范围0~100
    // 第5字节： 代表最后停于哪个灰度上，0中间灰度，1中间偏左，2中间偏右
    public void Around(int speed_L, int speed_R, int stop, int N, int P) {

        byte[] data = new byte[5];

        data[0] = (byte) speed_L;
        data[1] = (byte) speed_R;
        data[2] = (byte) stop;
        data[3] = (byte) N;
        data[4] = (byte) P;

        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x0F, data);
    }

    // 6字节： 第1字节： 代表左电机速度，范围0~200 (底层转换为-100~100)
    // 第2字节： 代表右电机速度，范围0~200 (底层转换为-100~100)
    // 第3~6字节：代表运动时间，0无意义
    public void motor_statr(int speed_L, int speed_R, float time) {

        byte[] data = new byte[6];
        byte[] temp = ByteUtils.float2byte(time);

        data[0] = (byte) speed_L;
        data[1] = (byte) speed_R;
        data[2] = (byte) temp[3];
        data[3] = (byte) temp[2];
        data[4] = (byte) temp[1];
        data[5] = (byte) temp[0];

        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x10, data);
    }

    public void WER_SetMotor_L(int IO, int operator, int reference, int Lspeed, int Rspeed, int stop) {

        // 7字节： 第1字节： 代表IO端口号
        // 第2字节： 代表比较符，范围：0:<;1:<=;2:==;3:!=;4:>=;5:>;
        // 第3-4字节：代表阈值参数，范围0~4095
        // 第5字节： 代表左电机速度，范围0~200 (底层转换为-100~100)
        // 第6字节： 代表右电机速度，范围0~200 (底层转换为-100~100)
        // 第7字节： 代表结束后是否停车，1是，0否
        byte[] data = new byte[7];

        data[0] = (byte) IO;
        data[1] = (byte) operator;
        data[2] = (byte) ((reference >> 8) & 0xff);
        data[3] = (byte) (reference & 0xff);
        data[4] = (byte) Lspeed;
        data[5] = (byte) Rspeed;
        data[6] = (byte) stop;
        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x12, data);
    }

    public void Motor_stop() {
        byte[] data = new byte[8];
        ProtocolUtils.write((byte) 0x01, (byte) 0xA3, (byte) 0x11, data);
    }

    //新加两个模块。
    public void SetDO(byte[] data) {
        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x20, data);
    }

    // scratch C系列添加 函数。-------------------------------------scratch

    public void Crunmove(int id, int speed) {
        setMotorSpeedOpenloop(id, speed);
    }

    public void C_runstop() {
        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x14, null);
    }

    /**
     *读取指定传感器所在的端口
     * @param type 0 超声 1 碰撞 2 灰度
     * @return 传感器所在端口号
     */
    public int ReadAIType(int type) {
        try {
            int[] AI_type = new int[7];
            int min = 0, max = 0;
            if (type == 0) { // 超声
                min = 1640;
                max = 2260;
            } else if (type == 1) { // 碰撞
                min = 100;
                max = 410;
            } else if (type == 2) { // 灰度
                min = 820;
                max = 1230;
            }
            byte[] readBuff = ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x05, null);
            if (readBuff[0] == (byte) 0xAA && readBuff[1] == (byte) 0x55 &&
                    readBuff[4] == (byte) 0x01 && readBuff[5] == (byte) 0xF0 && readBuff[6] == (byte) 0x21) {
                for (int j = 0; j < AI_type.length; j++) {
                    AI_type[j] = ByteUtils.byte2int_2byteHL(readBuff, 11 + j * 2);
                    LogMgr.e("AI_type["+ j +"]:" + AI_type[j]);
                    if (AI_type[j] > min && AI_type[j] < max) { // 查找到Type所在的AI端口
                        return j;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     *
     * @param port 端口号[0, 6]
     * @return 传感器模拟值
     */
    public int ReadAIValue(int port) {
        int[] AI_value = ReadAIValue();
        if (AI_value != null && port >= 0 && port < AI_value.length) {
            return AI_value[port];
        }
        return 0;
    }

    // 这里写一个获取AI的方法
    public int[] ReadAIValue() {
        int[] AI_value = new int[7];
        try {
            byte[] readBuff = ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x06, null);
            if (readBuff[0] == (byte) 0xAA && readBuff[1] == (byte) 0x55 &&
                    readBuff[4] == (byte) 0x01 && readBuff[5] == (byte) 0xF0 && readBuff[6] == (byte) 0x22) {
                for (int j = 0; j < AI_value.length; j++) {
                    AI_value[j] = ByteUtils.byte2int_2byteHL(readBuff, 11 + j * 2);
                }
                return AI_value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AI_value;
    }

    /**
     * 判断控制器方位
     * @param pos 0~3 ("下俯","上仰","左翻","右翻")
     * @return value
     */
    public int position(int pos) {
        int value = 0;
        float[] SN = mSensor.getmO();
        switch (pos) {
            case 0://下俯
                if (SN[1] >= -180 && SN[1] <= -95) {
                    value = 1;
                }
                break;
            case 1://上仰
                if (SN[1] >= -90 && SN[1] <= -5) {
                    value = 1;
                }
                break;
            case 2://左翻
                if (SN[2] >= 5 && SN[2] <= 90) {
                    value = 1;
                }
                break;
            case 3://右翻
                if (SN[2] >= -90 && SN[2] <= -5) {
                    value = 1;
                }
                break;
            default:
                break;
        }

        return value;
    }

    /**
     * 获取颜色传感器返回颜色值
     * @return 0x01代表红 0x02代表绿 0x03代表蓝 0x04代表黄 0x05代表黑 0x06代表白
     */
    public int ReadColorValue() {
        int[] value = getColorSensorValue();
        return value[0];
    }

    /*"1字节：代表获取颜色值；  (0x01代表红； 0x02代表绿； 0x03代表蓝； 0x04代表黄； 0x05代表黑； 0x06代表白)
            3字节：代表获取RGB值；
            3字节：代表获取HSL值；"*/
    private int[] getColorSensorValue() {
        int[] value = new int[7];
        try {
            byte[] readBuff = ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x2D, null);
            if (readBuff[0] == (byte) 0xAA && readBuff[1] == (byte) 0x55 &&
                    readBuff[4] == (byte) 0x01 && readBuff[5] == (byte) 0xF0 && readBuff[6] == (byte) 0x2C) {
                for (int k = 0; k < value.length; k++) {
                    value[k] = readBuff[k + 11] & 0xFF;
                }
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public int[] getColorRGB() {
        int[] value = getColorSensorValue();
        return Arrays.copyOfRange(value, 1, 4);
    }

    public int[] getColorHSL() {
        int[] value = getColorSensorValue();
        return Arrays.copyOfRange(value, 4, 7);
    }

    /**
     *
     * @param port:端口
     * @return result:0~4（"红","黄","绿","蓝","白"）
     */
    public int getColor(int port) {
        int result = -1;
        int colorValue= ReadColorValue();
        switch (colorValue) {
            case 1: //0x01代表红
                result = 0;
                break;
            case 2: //0x02代表绿
                result = 2;
                break;
            case 3: //0x03代表蓝
                result = 3;
                break;
            case 4: //0x04代表黄
                result = 1;
                break;
            case 5: //0x05代表黑
                result = 5;
                break;
            case 6: //0x06代表白
                result = 4;
                break;
            default:
                result = 7;
                break;
        }
        return result;
    }

    /**
     *
     * @param colorIndex:0~4（"红","黄","绿","蓝","白"）
     * @return 0：正确 1：错误
     */
    public int findColor(int colorIndex) {
        if (colorIndex < 0 || colorIndex > 4) {
            return 0;
        }
        int[] result = new int[5];
        int colorValue= ReadColorValue();
        switch (colorValue) {
            case 1: //0x01代表红
                result[0] = 1;
                break;
            case 2: //0x02代表绿
                result[2] = 1;
                break;
            case 3: //0x03代表蓝
                result[3] = 1;
                break;
            case 4: //0x04代表黄
                result[1] = 1;
                break;
            case 5: //0x05代表黑
                //result[3] = 1;
                break;
            case 6: //0x06代表白
                result[4] = 1;
                break;
        }
        return result[colorIndex];
    }

    /**
     * Scratch index 41:废弃
     * @param colorIndex:0~4 ("红色","绿色","蓝色","黑色","白色")
     * @return 0：正确 1：错误
     */
    public int findColorC(int colorIndex) {
        if (colorIndex < 0 || colorIndex > 4) {
            return 0;
        }
        int[] result = new int[5];
        int colorValue= ReadColorValue();
        switch (colorValue) {
            case 1: //0x01代表红
                result[0] = 1;
                break;
            case 2: //0x02代表绿
                result[1] = 1;
                break;
            case 3: //0x03代表蓝
                result[2] = 1;
                break;
            case 4: //0x04代表黄
                break;
            case 5: //0x05代表黑
                result[3] = 1;
                break;
            case 6: //0x06代表白
                result[4] = 1;
                break;
        }
        return result[colorIndex];
    }

    /**
     *
     * @param id [0,3]代表ABCD四个电机
     * @param direction [0,1] 0：正转 1：反转
     * @param speed [0, 100] 电机速度
     */
    public void runMove(int id, int direction, int speed) {
        speed *= (direction == 0) ? 1 : -1;
        setMotorSpeedOpenloop(id, speed);
    }

    /**
     *
     * @param idSet ABCD 4个电机是否设置，设置为1，未设置为0；
     * @param speed ABCD 4个电机对应速度；
     *7字节：第1字节低4位表示ABCD四个电机是否选择(例：1100代表选择AB，0011代表选择CD)；
     *2~7字节代表ABCD四个电机参数值[-100, 100];
     */
    public void setMotorSpeedOpenloop(byte[] idSet, int[] speed) {
        byte[] data = new byte[5];
        for (int i = 0; i < idSet.length; i++) {
            data[0] |= idSet[i] << (3 - i);
            data[i + 1] = convertMotorSpeedValue(speed[i]);
        }
        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x13, data);
    }

    public void setMotorSpeedOpenloop(int id, int speed) {
        byte[] idSets = new byte[4];
        int[] speeds = new int[4];
        idSets[id] = 0x01;
        speeds[id] = speed;
        setMotorSpeedOpenloop(idSets, speeds);
    }

    /**
     *闭环电机控制
     * @param idSet ABCD 4个电机是否设置，设置为1，未设置为0；
     * @param motorType ABCD 4个电机类型：1为大电机，0为小电机；
     * @param mode ABCD 4个电机闭环参数：0为速度，1为圈数，2为角度；
     * @param speed ABCD 4个电机速度值；
     * @param value ABCD 4个电机圈数/角度值;
     */
    public void setMotorCloseloop(byte[] idSet, byte[] motorType, byte[] mode, int[] speed, int[] value) {
        byte[] data = new byte[21];
        for (int i = 0; i < idSet.length; i++) {
            data[0] |= idSet[i] << (3 - i);
            // A电机参数
            data[i * 5 + 1] = motorType[i];
            data[i * 5 + 2] = mode[i];
            data[i * 5 + 3] = convertMotorSpeedValue(speed[i]);
            data[i * 5 + 4] = (byte) ((value[i] >> 8) & 0xFF);
            data[i * 5 + 5] = (byte) (value[i]  & 0xFF);
        }
        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x03, data);
    }

    public void setMotorCloseloop(int idSet, int motorType, int mode, int speed, int value) {
        byte[] idSets = new byte[4];
        byte[] motorTypes = new byte[4];
        byte[] modes = new byte[4];
        int[] speeds = new int[4];
        int[] values = new int[4];
        idSets[idSet] = 0x01;
        motorTypes[idSet] = (byte) motorType;
        modes[idSet] = (byte) mode;
        speeds[idSet] = speed;
        values[idSet] = value;
        setMotorCloseloop(idSets, motorTypes, modes, speeds, values);
    }

    /**
     * 电机速度转换 speed + 100 转换为stm32接受范围[0,200]
     * @param speed [-100,100]
     * @return
     */
    private byte convertMotorSpeedValue(int speed) {
        if (speed > 100) {
            speed = 100;
        } else if (speed < -100) {
            speed = -100;
        }
        speed = speed + 100;
        return (byte) speed;
    }

    /**
     *
     * @param color 0:灭 1:红 2:蓝 3:绿
     */
    public void setLedRbg(int color) {
        byte[] data = new byte[1];
        data[0] = (byte) color;
        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x04, data);
    }

    /**
     *
     * @param color 0:灭 1:红 2:绿 3:蓝
     */
    public void setLed(int color) {
        int colorSet = 0;
        switch (color) {
            case 1: //红
                colorSet = 0x01;
                break;
            case 2: //绿
                colorSet = 0x03;
                break;
            case 3: //蓝
                colorSet = 0x02;
                break;
        }
        setLedRbg(colorSet);
    }

    /**
     *
     * @param color 0:红 1:绿 2:蓝
     */
    public void setLedRgb(int color) {
        setLed(color + 1);
    }


    // 是否遇到障碍物
    public int haveObject(int port) {
        int value = distance(port);

        if (value > 0 && value <= 200) {
            return 1;
        }
        return 0;
    }

    public int distance(int port) {

        if (port == -1 || port > 7) {
            LogMgr.e("端口错误");
            return 0;
        }
        int value = 0;
        if (port != 0) {
            port = port - 1;
            value = ReadAIValue(port);// 这里调试一下端口。
        } else {
            int searchPort = ReadAIType(0);// 超声是0，按钮是1，灰度是2.
            value = ReadAIValue(searchPort);
        }
        if (value > 1500) {
            value = 1500;
        }
        LogMgr.i("distance: " + value);
        return value;
    }

    public int touch(int port) {

        if (port == -1 || port > 7) {
            LogMgr.e("端口错误");
            return 0;
        }
        int value = 0;
        if (port != 0) {
            port = port - 1;
            value = ReadAIValue(port);// 这里调试一下端口。
        } else {
            int searchPort = ReadAIType(1);// 超声是0，按钮是1，灰度是2.
            value = ReadAIValue(searchPort);
        }
        if (value > 1000 && value < 4096) {
            value = 1;
        } else {
            value = 0;
        }
        return value;
    }

    public int getGraySensor(int port) {

        if (port == -1 || port > 7) {
            LogMgr.e("端口越界");
            return 0;
        }
        int grayValue = 0;
        if (port != 0) {
            port = port - 1;
            grayValue = ReadAIValue(port);
        } else {
            int searchPort = ReadAIType(2);// 超声是0，按钮是1，灰度是2.
            grayValue = ReadAIValue(searchPort);
        }
        return grayValue;

    }

    public int getAIMSensor(int port) {
        //byte[] data = ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x2A, null);
        return 0;
    }

    /**
     *
     * @param port：0~7 (0为自动)
     * @param mode：0~3（""开"",""关"",""呼吸"",""频闪""）
     * @param frequency：0.1~60
     * @param rgb：RGB
     */
    public void setColorLed(int port, int mode, float frequency, byte[] rgb) {
        byte[] data = new byte[9];
        byte[] bytes = ByteUtils.float2byte(frequency);
        data[0] = (byte) port;
        data[1] = (byte) mode;
        System.arraycopy(bytes, 0, data, 2, 4);
        data[6] = rgb[0];
        data[7] = rgb[1];
        data[8] = rgb[2];
        LogMgr.d(String.format(Locale.US, "port:%d, mode:%d, frequency:%f, RBG[%d, %d, %d]", port, mode, frequency, rgb[0], rgb[1], rgb[2]));
        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA4, (byte) 0x52, data);
    }

    /**
     * 设置智能电机
     * @param id 舵机ID号(1-22)
     * @param angle 舵机角度（0-1023）高位在前低位在后
     * @param speed 舵机速度（200-1023）高位在前低位在后
     */
    public void setSmartMotor(int id, int angle, int speed) {
        byte[] data = new byte[6];
        data[0] = 0x03;
        angle = 512 + angle * 1024 / 300;
        data[1] = (byte) (id & 0xFF);
        data[2] = (byte) ((angle >> 8) & 0xFF);
        data[3] = (byte) (angle &0xFF);
        data[4] = (byte) ((speed >> 8) & 0xFF);
        data[5] = (byte) (speed &0xFF);
        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA4, (byte) 0x53, data);
    }

    /**
     * 设置电磁铁
     * @param id 端口 [1, 7]；
     * @param mode 1-打开，0-关闭
     */
    public void setElectromagnet(int id, int mode) {

        int modeSet = (0x02 | mode & 0x01) << ((8 - id) * 2);
        byte[] data = new byte[2];
        data[0] = (byte) (modeSet >> 8 & 0xFF);
        data[1] = (byte) (modeSet & 0xFF);
        LogMgr.e("setElectromagnet() " + Integer.toBinaryString(modeSet));
        SetDO(data);
        //ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA4, (byte) 0x53, data);
    }

    /*"第一个 1字节 参数舵机ID号(1-22)
    第二个 2字节 参数舵机角度（0-1023）高位在前低位在后"*/

    public void setSingleEngineC5(int id, int angle) {
        byte[] data = new byte[3];
        angle = 512 + angle * 1024 / 300;
        data[0] = (byte) (id & 0xFF);
        data[1] = (byte) ((angle >> 8) & 0xFF);
        data[2] = (byte) (angle &0xFF);
        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x2B, data);
    }

    /**
     *
     * @param type 0x00自平衡； 0x01前进； 0x02后退； 0x03左转； 0x04右转；0x05停止；0x06前进右转;0x07后退左转;0x08后退右转;0x09前进左转; 0x0A左右轮速度设置
     * @param usSet 超声控制：0x00无控制； 0x01开启超声控制； 0x02关闭超声控制；
     * @param leftSpeed 速度设置：0x00不设置； 0x01速度值(默认)；  （第1字节为0x0A时，该字节表示左轮速度）
     * @param rightSpeed 速度设置：0x00不设置； 0x01速度值(默认)；  （第1字节为0x0A时，该字节表示右轮速度）
     * @param valueSet 其他设置：0x00不设置;0x02距离值 50-500cm，第1字节为0x01前进、0x02后退时，该参数值有效；0x03角度值，第1字节为0x03左转、0x04右转时，该参数值有效；0x04时间值 0-30s，第1字节为0x03左转、0x04右转时，该参数值有效；
     * @param value 参数值
     */
    public void setBalanceCar(int type, int usSet, int leftSpeed, int rightSpeed, int valueSet, int value) {
        byte[] data = new byte[7];
        data[0] = (byte) (type & 0xFF);
        data[1] = (byte) (usSet & 0xFF);
        data[2] = convertMotorSpeedValue(leftSpeed);
        data[3] = convertMotorSpeedValue(rightSpeed);
        data[4] = (byte) (valueSet & 0xFF);
        data[5] = (byte) (value >> 8 & 0xFF);
        data[6] = (byte) (value & 0xFF);

        ProtocolUtils.sendProtocol((byte) 0x01, (byte) 0xA3, (byte) 0x29, data);
    }

    public static final int MODEL_CMD_INIT = 23;
    public static final int MODEL_CMD_MOVE = 24;
    public static final int MODEL_CMD_FUNCTION = 25;
    public static final int MODEL_CMD_ACTION = 26;

    /**
     * 设置模型类型
     * @param type 模型类型 0x00退出 0x01坦克 0x02蝎子
     */
    public void setMotionModel(int type) {
        byte[] data = new byte[2];
        data[0] = 0x01;
        data[1] = (byte) (type & 0xFF);
        ProtocolUtils.sendPatchCmdToControl(MODEL_CMD_INIT, data);
    }
    /**
     *蝎子控制
     * @param cmdType 命令类型 0x01 移动命令 0x02 功能命令
     * @param cmdValue cmdType为“移动命令”时： 0x00 停止 0x01 前进 0x02 后退;cmdType为“功能命令”时： 0x01 攻击
     * @param cmdSpeed 移动速度值0~100
     */
    public void setScorpion(int cmdType, int cmdValue, int cmdSpeed) {
        byte[] data;
        switch (cmdType) {
            case 0x01://移动命令
                data = new byte[3];
                data[0] = 0x02;
                data[1] = (byte) (cmdValue & 0xFF);
                data[2] = (byte) (cmdSpeed & 0xFF);
                ProtocolUtils.sendPatchCmdToControl(MODEL_CMD_MOVE, data);
                break;
            case 0x02://功能命令
                data = new byte[2];
                data[0] = 0x01;
                data[1] = (byte) (cmdValue & 0xFF);
                ProtocolUtils.sendPatchCmdToControl(MODEL_CMD_ACTION, data);
                break;
        }
    }

    /**
     *
     * @param cmdType 命令类型 0x01 移动命令 0x02 功能命令
     * @param cmdValue cmdType为“移动命令”时： 0x00 停止 0x01 前进 0x02 后退 0x03左转 0x04右转;cmdType为“功能命令”时： 0x01 避障开 0x02避障关
     * @param cmdSpeed 移动速度值0~100
     */
    public void setTank(int cmdType, int cmdValue, int cmdSpeed) {
        byte[] data;
        switch (cmdType) {
            case 0x01://移动命令
                data = new byte[3];
                data[0] = 0x02;
                data[1] = (byte) (cmdValue & 0xFF);
                data[2] = (byte) (cmdSpeed & 0xFF);
                ProtocolUtils.sendPatchCmdToControl(MODEL_CMD_MOVE, data);
                break;
            case 0x02://功能命令
                data = new byte[3];
                data[0] = 0x02;
                data[1] = 0x01;
                data[2] = (byte) (cmdValue & 0xFF);
                ProtocolUtils.sendPatchCmdToControl(MODEL_CMD_FUNCTION, data);
                break;
        }
    }

}
