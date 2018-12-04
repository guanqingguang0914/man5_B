package com.abilix.explainer.helper;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.ProtocolUtils;

public class M1ExplainerHelper extends AExplainHelper {
    //M1系列
    public static byte[] mWheelByte = new byte[20];
    private byte[] mHeadByte = new byte[20];
    private byte[] EYE_COLOR = new byte[20];
    private byte[] EYE_COUNT = new byte[20];
    private byte[] COLOR = new byte[20];
    private byte[] LUMINANCE = new byte[20];
    private byte[] WAVEMODE = new byte[20];
    private byte[] VACUUM = new byte[20];


    public M1ExplainerHelper() {
        System.arraycopy(ProtocolUtils.VACUUM, 0, VACUUM, 0, ProtocolUtils.VACUUM.length);
        System.arraycopy(ProtocolUtils.WAVEMODE, 0, WAVEMODE, 0, ProtocolUtils.WAVEMODE.length);
        System.arraycopy(ProtocolUtils.LUMINANCE, 0, LUMINANCE, 0, ProtocolUtils.LUMINANCE.length);
        System.arraycopy(ProtocolUtils.COLOR, 0, COLOR, 0, ProtocolUtils.COLOR.length);
        System.arraycopy(ProtocolUtils.EYE_COUNT, 0, EYE_COUNT, 0, ProtocolUtils.EYE_COUNT.length);
        System.arraycopy(ProtocolUtils.EYE_COLOR, 0, EYE_COLOR, 0, ProtocolUtils.EYE_COLOR.length);
        System.arraycopy(ProtocolUtils.WHEELBYTE, 0, mWheelByte, 0, ProtocolUtils.WHEELBYTE.length);
        System.arraycopy(ProtocolUtils.HEADBYTE, 0, mHeadByte, 0, ProtocolUtils.HEADBYTE.length);
    }

    /**
     * 校验
     *
     * @param data
     * @return
     */
    public boolean Check(byte[] data, int count) {
        int j = 0;
        int z = 0;
        int length = data.length - 4;
        for (int i = 0; i < length; i++) {
            j += data[4 + i] < 0 ? data[4 + i] + 256 : data[4 + i];
        }
        String data3 = Integer.toHexString((data[3] < 0 ? data[3] + 256 : data[3]));
        String data2 = Integer.toHexString((data[2] < 0 ? data[2] + 256 : data[2]));
        if (Integer.valueOf(data2, 16) <= 15) {
            data2 = "0" + data2;
        }
        z = Integer.valueOf(data3 + data2, 16);
        // Log.e("test", "z:" + z + " j:" + j);
        if (z == j) {
            return true;
        } else {
            LogMgr.e("出错的行号:" + count + " 数据:" + Arrays.toString(data));
            return false;
        }
    }

    public void setWheelMoto(int loop, int port, int speed) {
        try {
            LogMgr.e("loop =  " + loop + ";port = " + port + ";speed = " + speed);
            byte[] wheel = new byte[8];
            if (port == 0) {//左电机
                wheel[0] = (byte) 0x02;
                wheel[1] = (byte) (speed + 100);
            } else {//右电机
                wheel[0] = (byte) 0x01;
                wheel[2] = (byte) (speed + 100);
            }
            wheel[3] = 0x00;
            //这里暂定为闭速
            LogMgr.e("wheel = " + ByteUtils.bytesToString(wheel, wheel.length));
            ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, (byte) 0x3B, wheel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 按新协议发眼睛协议
    public void setEyeColor_new(int count, int r, int g, int b) {
        // 48字节：分别代表眼睛1~16号LED的红绿蓝值。
        if (count > 10 || count < 0) {
            count = 10;
        }
        byte[] data = new byte[30];
        for (int i = 0; i < count; i++) {
            data[i * 3 + 0] = (byte) r;
            data[i * 3 + 1] = (byte) g;
            data[i * 3 + 2] = (byte) b;
        }
        ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, (byte) 0x31, data);
    }

    public void setColor(int mode, int r, int g, int b) {
        try {
            byte[] modebyte = new byte[1];
            if (mode == 1) {
                modebyte[0] = (byte) 0x32;
            } else if (mode == 2 || mode == 3) {
                modebyte[0] = (byte) 0x33;
            } else if (mode == 4) {
                modebyte[0] = (byte) 0x34;
            } else {
                LogMgr.e("发送类型错误");
            }
            ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, modebyte[0], new byte[]{(byte) r, (byte) g, (byte) b});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param mode     0x1 脖子 0x2|0x3 轮子 0x4底部
     * @param progress
     */
    public void setLuminance(int mode, int progress) {
        try {
            LUMINANCE[10] = (byte) mode;
            LUMINANCE[11] = (byte) progress;
            ProtocolUtils.sendProtocol(LUMINANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setWave(int mode, int wavemode) {
        try {
            ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, (byte) 0x36, new byte[]{(byte) mode, (byte) wavemode});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下视数据
     */
    public byte[] getlook_down() {
        try {
            byte[] rest = new byte[20];
            byte[] data = ProtocolUtils.sendProtocol(ProtocolUtils.mDown_WatchByte);
            for (int i = 0; i < 20; i++) {
                // Log.e("test", "data.length:" + data.length);
                if (data[i] == ProtocolUtils.DATA_HEAD_ && data[i + 1] == ProtocolUtils.L && data[i + 2] == ProtocolUtils.O && data[i + 3] == ProtocolUtils.O) {
                    // return data;
                    rest = Arrays.copyOfRange(data, i, i + 20);
                    return rest;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 超声数据
     */
    public byte[] getUtrasonic() {
        try {
            byte[] rest = new byte[20];
            byte[] data = ProtocolUtils.sendProtocol(ProtocolUtils.mUltrasonicByte);
            for (int i = 0; i < 20; i++) {
                // Log.e("test", "data.length:" + data.length);
                if (data[i] == ProtocolUtils.DATA_HEAD_ && data[i + 1] == ProtocolUtils.A && data[i + 2] == ProtocolUtils.I && data[i + 3] == ProtocolUtils.DR) {
                    // return data;
                    rest = Arrays.copyOfRange(data, i, i + 20);
                    return rest;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 红外数据
     */
    public byte[] getIfrared() {
        try {
            byte[] rest = new byte[20];
            byte[] data = ProtocolUtils.sendProtocol(ProtocolUtils.mRear_End_InfraredByte);
            for (int i = 0; i < 20; i++) {
                // Log.e("test", "data.length:" + data.length);
                if (data[i] == ProtocolUtils.DATA_HEAD_ && data[i + 1] == ProtocolUtils.I && data[i + 2] == ProtocolUtils.N && data[i + 3] == ProtocolUtils.F) {
                    // return data;
                    rest = Arrays.copyOfRange(data, i, i + 20);
                    return rest;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 地面灰度
     */
    public byte[] getground_gray(float mode) {
        try {
            byte[] rest = new byte[20];
            byte[] data = ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, (byte) 0x38, null);
            for (int i = 0; i < 20; i++) {
                if ((data[i + 5] & 0xff) == 0xf3 && (data[i + 6] & 0xff) == 0x31) {
                    rest = Arrays.copyOfRange(data, i, i + 20);
                    LogMgr.e("rest =" + ByteUtils.bytesToString(rest, rest.length));
                    return rest;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 碰撞数据
     */
    public byte[] getcollision() {
        try {
            byte[] rest = new byte[20];
            byte[] data = ProtocolUtils.sendProtocol(ProtocolUtils.mCrashInfraredByte);
            for (int i = 0; i < 20; i++) {
                // Log.e("test", "data.length:" + data.length);
                if (data[i] == ProtocolUtils.DATA_HEAD_ && data[i + 1] == ProtocolUtils.C && data[i + 2] == ProtocolUtils.O && data[i + 3] == ProtocolUtils.L) {
                    rest = Arrays.copyOfRange(data, i, i + 20);
                    // return data;
                    return rest;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 吸尘器
     */
    public void SetcollectionMotor(int progress) {
        VACUUM[8] = (byte) progress;
        ProtocolUtils.sendProtocol(VACUUM);
    }

    public void stopCollectionMotor() {
        VACUUM[8] = 0;
        ProtocolUtils.sendProtocol(VACUUM);
    }

    public void turnOutLights() {
        byte[] turn_out_light_byte = new byte[20];
        System.arraycopy(ProtocolUtils.COLOR, 0, turn_out_light_byte, 0, ProtocolUtils.COLOR.length);
        try {
            turn_out_light_byte[6] = 1;
            ProtocolUtils.sendProtocol(turn_out_light_byte);
            TimeUnit.MILLISECONDS.sleep(8);
            turn_out_light_byte[6] = 2;
            ProtocolUtils.sendProtocol(turn_out_light_byte);
            TimeUnit.MILLISECONDS.sleep(8);
            turn_out_light_byte[6] = 4;
            ProtocolUtils.sendProtocol(turn_out_light_byte);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void turnOutWave() {
        byte[] turn_out_wavemode_byte = new byte[20];
        System.arraycopy(ProtocolUtils.WAVEMODE, 0, turn_out_wavemode_byte, 0, ProtocolUtils.WAVEMODE.length);
        try {
            WAVEMODE[9] = (byte) 1;
            WAVEMODE[10] = (byte) 3;
            ProtocolUtils.sendProtocol(WAVEMODE);
            TimeUnit.MILLISECONDS.sleep(8);
            WAVEMODE[9] = (byte) 2;
            WAVEMODE[10] = (byte) 3;
            ProtocolUtils.sendProtocol(WAVEMODE);
            TimeUnit.MILLISECONDS.sleep(8);
            WAVEMODE[9] = (byte) 4;
            WAVEMODE[10] = (byte) 3;
            ProtocolUtils.sendProtocol(WAVEMODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上下脖子电机 progress = -15~30
     */
    public void SetNeckUPMotor(int progress) {
        progress += 15;
        ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, (byte) 0x39, new byte[]{0x01, 0x00, (byte) (progress)});
    }

    /**
     * 左右脖子电机 progress =-130~130
     */
    public void SetNeckLRMotor(int progress) {
        progress += 130;
        byte[] neck = {0x00, 0x00, 0x00};
        if (progress >= 256) {
            if (progress == 256) {
                neck[2] = 1;
                neck[1] = 0;
            } else {
                neck[2] = 1;
                neck[1] = (byte) (progress - 256);
            }
        } else {
            neck[1] = (byte) progress;
            neck[2] = 0;
        }
        ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, (byte) 0x39, neck);
    }

    /**
     * 停止电机
     *
     * @param count 重复次数 默认三次
     */
    protected void stopWheelMoto(int count) {
        try {
            mWheelByte[8] = 100;
            mWheelByte[9] = 100;
            if (count == 0) {
                count = 3;
            }
            ProtocolUtils.sendProtocol(mWheelByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 头部触摸。
    public int gettouchhead() {
        byte[] readbuff = ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, (byte) 0x37, null);// 发送获取传感器协议。
        for (int j = 0; j < 20; j++) {
            if ((readbuff[j] & 0xff) == 0xf0
                    && (readbuff[j + 1] & 0xff) == 0x30) {
                return readbuff[j + 9] & 0xff;
            }
        }
        return 0;
    }

    public void setNewOneWheelMoto(int type, int mode, int sudu, int distance) {
        // 7字节：第1字节代表右轮速度；
        // 第2字节代表左轮速度；
        // 第3字节：0x00代表闭环电机速度（速度-60～+60）（发送值+100）,0x01代表位移距离，0x02代表占空比（速度-100～+100）（发送值+100）；
        // 第4和5字节代表右轮位移距离(高位在前)；
        // 第6和7字节代表左轮位移距离(高位在前)；单位cm。
        byte[] data = new byte[7];

        if (type == 1) {
            type = 2;
        } else if (type == 2) {
            type = 1;
        }

        if (mode == 0) {// 左电机。

            data[0] = (byte) ((100) & 0xff);// 右轮速度
            data[1] = (byte) ((sudu + 100) & 0xff);// 左轮速度。
            data[2] = (byte) (type & 0xff); // 开闭环
            // 右电机
            data[3] = 0;
            data[4] = 0;
            // 左电机。
            data[5] = (byte) ((distance >> 8) & 0xff);
            data[6] = (byte) ((distance) & 0xff);

        } else if (mode == 1) {// 右电机。

            data[0] = (byte) ((sudu + 100) & 0xff);// 右轮速度
            data[1] = (byte) ((100) & 0xff);// 左轮速度。
            data[2] = (byte) (type & 0xff); // 开闭环
            // 右电机
            data[3] = (byte) ((distance >> 8) & 0xff);
            data[4] = (byte) ((distance) & 0xff);
            // 左电机。
            data[5] = 0;
            data[6] = 0;
        }
        ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, (byte) 0x30, data);
    }

    public void setNewAllWheelMoto(int type, int leftIsSet, int leftSudu, int RightIsSet, int ringhtSudu, int leftDis, int rightDis) {
        // 数据越界限制。
        if (leftSudu > 100) {
            leftSudu = 100;
        } else if (leftSudu < -100) {
            leftSudu = -100;
        }
        if (ringhtSudu > 100) {
            ringhtSudu = 100;
        } else if (ringhtSudu < -100) {
            ringhtSudu = -100;
        }

        byte[] data = new byte[8];
        if (type == 2) { // 类型(0-闭环 1-位移 2-开环) 解决开环速度过小电机不转问题
            leftSudu = checkOpenLoopSpeed(leftSudu);
            ringhtSudu = checkOpenLoopSpeed(ringhtSudu);
            LogMgr.d(String.format("OpenLoopSpeed{%d, %d}", leftSudu, ringhtSudu));
        }
        //0x11两个电机，0x10左电机,0x01右电机。
        data[0] = (byte) ((leftIsSet & 0x01) << 1 | RightIsSet & 0x01);
        data[1] = (byte) ((leftSudu + 100) & 0xff);
        data[2] = (byte) ((ringhtSudu + 100) & 0xff);
        data[3] = (byte) (type & 0xff);
        data[4] = (byte) ((leftDis >> 8) & 0xff);
        data[5] = (byte) (leftDis & 0xff);
        data[6] = (byte) ((rightDis >> 8) & 0xff);
        data[7] = (byte) (rightDis & 0xff);
        ProtocolUtils.sendProtocol((byte) 0x0B, (byte) 0xA6, (byte) 0x3B, data);
    }

    private int checkOpenLoopSpeed(int speed) {
        if (speed == 0) {
            return 0;
        }

        int preSign = speed < 0 ? -1 : 1;
        return (8 * speed) / 10 + preSign * 20;
    }

    public void startSleepStop() {
        try {
            byte[] start = {(byte) 0xAA, 0x55, 00, 0x10, 0, 0x11, 0x08, 00, 00, 00, 00, 0x01, 00, 00, 00, 00, 00, 00, 00, 0x29};
            byte[] stop = {(byte) 0xAA, 0x55, 00, 0x10, 0, 0x11, 0x08, 00, 00, 00, 00, 0x00, 00, 00, 00, 00, 00, 00, 00, 0x28};
            LogMgr.e("休息" + " start:" + Arrays.toString(start) + " stop:" + Arrays.toString(stop));
            TimeUnit.MILLISECONDS.sleep(50);
            ProtocolUtils.sendProtocol(start);
            TimeUnit.MILLISECONDS.sleep(18);
            ProtocolUtils.sendProtocol(stop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
