package com.abilix.explainer.helper;

import java.util.Arrays;
import java.util.Locale;

import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.ProtocolUtils;

public class MExplainerHelper extends AExplainHelper {

    private byte[] LUMINANCE = new byte[20];
    public static MExplainerHelper mInstance = null;

    public MExplainerHelper() {
        System.arraycopy(ProtocolUtils.LUMINANCE, 0, LUMINANCE, 0, ProtocolUtils.LUMINANCE.length);
    }

    public static MExplainerHelper getInstance() {
        if (mInstance == null) {
            mInstance = new MExplainerHelper();
        }
        return mInstance;
    }

    /**
     * 地面灰度
     */
    public int getGroundGray(int id) {
        try {
            byte[] readBuff = ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x38, null);
            if (readBuff[0] == (byte) 0xAA && readBuff[1] == (byte) 0x55 &&
                    readBuff[4] == (byte) 0x02 && readBuff[5] == (byte) 0xF0 && readBuff[6] == (byte) 0x31) {
                return readBuff[11 + id - 1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 下视传感器
     * @return int 下视传感器返回值
     */
    public int getDownLook() {
        return getAIValue(AI_TYPE_DOWN_LOOK);
    }

    /**
     *下视传感器是否悬空
     * @param type 下视传感器类型：0代表是否左悬空，1代表是否右悬空，2代表是否左右悬空
     * @return 0 否 1是
     */
    public int getDownLook(int type) {
        //下视传感器：0代表后左右没有悬空，1代表后左有悬空，2代表后右有悬空，3代表左右悬空
        int value = getAIValue(AI_TYPE_DOWN_LOOK);
        int returnValue = 0;
        switch (type) {
            case 0x03://没有悬空
                returnValue = (value == 0x00) ? 1 : 0;
                break;
            case 0x00://后左有悬空
                returnValue = value & 0x01;
                break;
            case 0x01://后右有悬空
                returnValue = (value >> 1) & 0x01;
                break;
            case 0x02://左右悬空
                returnValue = (value == 0x03) ? 1 : 0;
                break;
        }
        return returnValue;
    }

    /**
     *下视传感器是否悬空
     * @param type 下视传感器类型：0x00代表后左，0x01代表后中,0x02代表后右,0x03代表前左,0x04代表前右；0x05代表全部悬空；0x06代表全部不悬空
     * @return 0 否 1是
     */
    public int getDownLookM3S(int type) {
        //第4字节,使用低五位表示下视 bit0代表后左，bit1代表后中,bit2代表后右,bit3代表前左,bit4代表前右；0为没悬空，1为悬空；
        //即00000000没有悬空，00000001后左悬空，00000010后中悬空，00000100后右悬空，00001000前左悬空，00010000前右悬空。
        int value = getAIValue(AI_TYPE_DOWN_LOOK);
        int returnValue = 0;
        switch (type) {
            case 0x00://代表后左
                returnValue = value & 0x01;
                break;
            case 0x01://代表后中
                returnValue = (value >> 1) & 0x01;
                break;
            case 0x02://代表后右
                returnValue = (value >> 2) & 0x01;
                break;
            case 0x03://代表前左
                returnValue = (value >> 3) & 0x01;
                break;
            case 0x04://代表前右
                returnValue = (value >> 4) & 0x01;
                break;
            case 0x05://代表全部悬空
                returnValue = (value == (0x1F & 0xFF)) ? 1 : 0;
                break;
            case 0x06://代表全部不悬空
                returnValue = (value == 0x00) ? 1 : 0;
                break;
        }
        return returnValue;
    }

    /**
     * 超声距离
     * @param type 超声类型：0前超声，1后超声
     * @return int 超声距离
     */
    public int getUltrasonic(int type) {
        if (type == 0) {
            return getAIValue(AI_TYPE_ULTRASONIC_FRONT);
        } else {
            return getAIValue(AI_TYPE_ULTRASONIC_BACK);
        }
    }

    /**
     *前方是否有障碍物
     * @param type 超声类型：0前超声，1后超声
     * @return 0 否 1 是
     */
    public int findBarM(int type) {
        int value = getUltrasonic(type);
        if (value > 0 && value < 30) {
            return 1;
        }
        return 0;
    }

    /**
     * 后端红外距离
     * @return int类型
     */
    public int getInfrared() {
        return getAIValue(AI_TYPE_INFRARED);
    }

    /**
     * 碰撞
     * @return 0 无碰撞，1表示左碰撞，2右碰撞，3前碰撞
     */
    public int getCollision() {
        return getAIValue(AI_TYPE_COLLISION);
    }

    /**
     * 是否检测到碰撞
     * @param type 碰撞类型：0代表左前碰撞，1代表右前碰撞，2代表正前碰撞
     * @return 0 无碰撞 1 检测到碰撞
     */
    public int getCollision(int type) {
        int value = getAIValue(AI_TYPE_COLLISION);
        int returnValue = 0;
        switch (type) {
            case 0x00://左碰撞
                if (value == 1) {
                    returnValue = 1;
                }
                break;
            case 0x01://右碰撞
                if (value == 2) {
                    returnValue = 1;
                }
                break;
            case 0x02://前碰撞
                if (value == 3) {
                    returnValue = 1;
                }
                break;
        }
        return returnValue;
    }


    // 触摸
    public int getTouchHead() {
        return getAIValue(AI_TYPE_TOUCH_HEAD);
    }

    public int getTouchBack() {
        return getAIValue(AI_TYPE_TOUCH_BACK);
    }

    public int getTouchChest() {
        return getAIValue(AI_TYPE_TOUCH_CHEST);
    }

    public int getTouchArmLeft() {
        return getAIValue(AI_TYPE_TOUCH_ARM_LEFT);
    }

    public int getTouchArmRight() {
        return getAIValue(AI_TYPE_TOUCH_ARM_RIGHT);
    }

    public int getTouchEarLeft() {
        return getAIValue(AI_TYPE_TOUCH_EAR_LEFT);
    }

    public int getTouchEarRight() {
        return getAIValue(AI_TYPE_TOUCH_EAR_RIGHT);
    }

    /**
     *机器人触摸传感器的状态
     * @param type 0头部，1前方，2后方,3左耳，4右耳，5左臂，6右臂
     * @return 0 没有触摸 1 有触摸
     */
    public int getTouchM3S(int type) {
        int value = 0;
        switch (type) {
            case 0x00://0头部
                value = getAIValue(AI_TYPE_TOUCH_HEAD);
                break;
            case 0x01://1前方
                value = getAIValue(AI_TYPE_TOUCH_CHEST);
                break;
            case 0x02://2后方
                value = getAIValue(AI_TYPE_TOUCH_BACK);
                break;
            case 0x03://3左耳
                value = getAIValue(AI_TYPE_TOUCH_EAR_LEFT);
                break;
            case 0x04://4右耳
                value = getAIValue(AI_TYPE_TOUCH_EAR_RIGHT);
                break;
            case 0x05://5左臂
                value = getAIValue(AI_TYPE_TOUCH_ARM_LEFT);
                break;
            case 0x06://6右臂
                value = getAIValue(AI_TYPE_TOUCH_ARM_RIGHT);
                break;
        }
        return value;
    }

    private final static int AI_TYPE_ULTRASONIC_FRONT = 0x01; //前端超声距离
    private final static int AI_TYPE_ULTRASONIC_BACK = 0x02; //后端超声距离
    private final static int AI_TYPE_COLLISION = 0x03; //碰撞
    private final static int AI_TYPE_INFRARED = 0x04; //后端红外距离
    private final static int AI_TYPE_DOWN_LOOK = 0x05; //下视
    private final static int AI_TYPE_TOUCH_ARM_LEFT = 0x06; //左臂
    private final static int AI_TYPE_TOUCH_ARM_RIGHT = 0x07; //右臂
    private final static int AI_TYPE_TOUCH_HEAD = 0x08; //头部
    private final static int AI_TYPE_TOUCH_EAR_LEFT = 0x09; //左耳
    private final static int AI_TYPE_TOUCH_EAR_RIGHT = 0x0A; //右耳
    private final static int AI_TYPE_TOUCH_CHEST = 0x0B; //前胸
    private final static int AI_TYPE_TOUCH_BACK = 0x0C; //后背

    /**
     * @param type AI_TYPE
     * @return value
     */
    private int getAIValue(int type) {
        try {
            byte[] readBuff = ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x37, null);// 发送获取传感器协议。
            if (readBuff[0] == (byte) 0xAA && readBuff[1] == (byte) 0x55 && readBuff[5] == (byte) 0xF0 && readBuff[6] == (byte) 0x30) {
                /*8字节：第1字节代表前端超声距离；第2字节代表后端超声距离；第3字节代表碰撞：0代表没有碰撞，1代表左碰撞，2代表右碰撞，3代表前碰撞；
                第4字节代表触摸：0代表没有触摸，1代表有触摸；第5字节代表下视：0代表后左右没有悬空，1代表后左有悬空，2代表后右有悬空，3代表左右悬空；
                第6-7字节代表后端红外距离值。*/
                //aa 55 00 0f 02 f0 30 00 00 00 00 6f 06 00 01 00 0b cf b1
                int value;
                switch (type) {
                    case AI_TYPE_ULTRASONIC_FRONT:
                        value = readBuff[11] & 0xFF;
                        break;
                    case AI_TYPE_ULTRASONIC_BACK:
                        value = readBuff[12] & 0xFF;
                        break;
                    case AI_TYPE_COLLISION:
                        value = readBuff[13] & 0xFF;
                        break;
                    case AI_TYPE_INFRARED:
                        value = ByteUtils.byte2int_2byteHL(readBuff, 16);
                        break;
                    case AI_TYPE_DOWN_LOOK:
                        value = readBuff[15] & 0xFF;
                        break;
                    case AI_TYPE_TOUCH_HEAD:
                        value = readBuff[14] & 0xFF;
                        break;
                    default:
                        value = -1;
                        break;
                }
                return value;
            } else if (readBuff[0] == (byte) 0xAA && readBuff[1] == (byte) 0x55 && readBuff[5] == (byte) 0xF0 && readBuff[6] == (byte) 0x35) {
                /*"7字节：
                第1字节：前超声距离
                第2字节：后超声距离
                第3字节,碰撞 0 无碰撞，1表示左碰撞，2右碰撞，3前碰撞
                第4字节,使用低五位表示下视 bit0代表后左，bit1代表后中,bit2代表后右,bit3代表前左,bit4代表前右；0为没悬空，1为悬空；
                即00000000没有悬空，00000001后左悬空，00000010后中悬空，00000100后右悬空，00001000前左悬空，00010000前右悬空。等等
                第5-6字节,红外测距值（高位在前，低位在后）
                第7字节：低7位表示触摸传感器 依次为左臂，右臂，头部，左耳，右耳，前胸，后背。0为没触摸，1为有触摸。"*/
                //aa 55 00 0f 4f f0 35 00 00 00 00 04 02 00 00 0e ff 02 97
                int value = -1;
                switch (type) {
                    case AI_TYPE_ULTRASONIC_FRONT:
                        value = readBuff[11] & 0xFF;
                        break;
                    case AI_TYPE_ULTRASONIC_BACK:
                        value = readBuff[12] & 0xFF;
                        break;
                    case AI_TYPE_COLLISION:
                        value = readBuff[13] & 0xFF;
                        break;
                    case AI_TYPE_INFRARED:
                        value = ByteUtils.byte2int_2byteHL(readBuff, 15);
                        break;
                    case AI_TYPE_DOWN_LOOK:
                        value = readBuff[14] & 0xFF;
                        break;
                    case AI_TYPE_TOUCH_ARM_LEFT:
                        value = readBuff[17] >> 6 & 0x01;
                        break;
                    case AI_TYPE_TOUCH_ARM_RIGHT:
                        value = readBuff[17] >> 5 & 0x01;
                        break;
                    case AI_TYPE_TOUCH_HEAD:
                        value = readBuff[17] >> 4 & 0x01;
                        break;
                    case AI_TYPE_TOUCH_EAR_LEFT:
                        value = readBuff[17] >> 3 & 0x01;
                        break;
                    case AI_TYPE_TOUCH_EAR_RIGHT:
                        value = readBuff[17] >> 2 & 0x01;
                        break;
                    case AI_TYPE_TOUCH_CHEST:
                        value = readBuff[17] >> 1 & 0x01;
                        break;
                    case AI_TYPE_TOUCH_BACK:
                        value = readBuff[17] & 0x01;
                        break;
                }
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     *
     * @param type 类型(0-闭环 1-开环 2-位移)
     * @param mode 模式(0-左电机 1-右电机)
     * @param speed 速度
     * @param dis 位移
     */
    public void setNewOneWheelMoto(int type, int mode, int speed, int dis) {
        if (type == 1) {
            type = 2;//类型(0-闭环 1-位移 2-开环)
        } else if (type == 2) {
            type = 1;
        }
        int leftSet = 0, rightSet = 0;
        int leftSpeed = 0, rightSpeed = 0;
        int leftDis = 0, rightDis = 0;
        if (mode == 0) {
            leftSet = 1;
            leftSpeed = speed;
            leftDis = dis;
        } else {
            rightSet = 1;
            rightSpeed = speed;
            rightDis = dis;
        }
        setNewAllWheelMoto(type, leftSet, leftSpeed, rightSet, rightSpeed, leftDis, rightDis);
    }

    /**
     *
     * @param type 类型(0-闭环 1-位移 2-开环)
     * @param leftSet 左轮子是否设置(0-不设置，1-设置
     * @param leftSpeed 左轮子速度 范围为-100~100
     * @param rightSet 右轮子是否设置(0-不设置，1-设置
     * @param rightSpeed 右轮子速度 范围为-100~100
     * @param leftDis 左轮子位移(0-4095)单位cm
     * @param rightDis 右轮子位移(0-4095)单位cm
     */
    public void setNewAllWheelMoto(int type, int leftSet, int leftSpeed, int rightSet, int rightSpeed, int leftDis, int rightDis) {
        // 数据越界限制。
        if (leftSpeed > 100) {
            leftSpeed = 100;
        } else if (leftSpeed < -100) {
            leftSpeed = -100;
        }
        if (rightSpeed > 100) {
            rightSpeed = 100;
        } else if (rightSpeed < -100) {
            rightSpeed = -100;
        }

        byte[] data = new byte[8];
        if (type == 2) { // 类型(0-闭环 1-位移 2-开环) 解决开环速度过小电机不转问题
            leftSpeed = checkOpenLoopSpeed(leftSpeed);
            rightSpeed = checkOpenLoopSpeed(rightSpeed);
            LogMgr.d(String.format(Locale.US, "OpenLoopSpeed{%d, %d}", leftSpeed, rightSpeed));
        }
        //0x11两个电机，0x10左电机,0x01右电机。
        data[0] = (byte) ((leftSet & 0x01) << 1 | rightSet & 0x01);
        data[1] = (byte) ((leftSpeed + 100) & 0xff);
        data[2] = (byte) ((rightSpeed + 100) & 0xff);
        data[3] = (byte) (type & 0xff);
        data[4] = (byte) ((leftDis >> 8) & 0xff);
        data[5] = (byte) (leftDis & 0xff);
        data[6] = (byte) ((rightDis >> 8) & 0xff);
        data[7] = (byte) (rightDis & 0xff);
        ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x41, data);
    }

    private int checkOpenLoopSpeed(int speed) {
        if (speed == 0) {
            return 0;
        }

        int preSign = speed < 0 ? -1 : 1;
        return (8 * speed) / 10 + preSign * 20;
    }

    private void setMotorSpeed(int leftSpeed, int rightSpeed) {
        setNewAllWheelMoto(0, 1, leftSpeed, 1, rightSpeed, 0, 0);

    }

    public void startSleepStop() {
        try {
            ProtocolUtils.write((byte) 0x02, (byte) 0x11, (byte) 0x08, new byte[]{0x01});
            Thread.sleep(100);
            ProtocolUtils.write((byte) 0x02, (byte) 0x11, (byte) 0x08, new byte[]{0x00});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置轮子电机
     *
     * @param type 表示电机开闭环，0闭环（默认是闭环电机），1开环(0~1)
     * @param id 表示控制哪个轮子的电机，0左电机，1右电机(0~1)
     * @param speed 正数表示电机向前速度，负数表示电机向后速度(-100~100)
     */
    public void setWheelMoto(int type, int id, int speed) {
        setNewOneWheelMoto(type, id, speed, 0);
    }

    // 按新协议发眼睛协议
    public void setEyeColor(int count, int r, int g, int b) {
        // 48字节：分别代表眼睛1~16号LED的红绿蓝值。
        byte[] data = new byte[48];
        for (int i = 0; i < count; i++) {
            data[i * 3 + 0] = (byte) r;
            data[i * 3 + 1] = (byte) g;
            data[i * 3 + 2] = (byte) b;
        }
        ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x31, data);
    }

    /**
     * @param mode 0x1 脖子 0x2|0x3 轮子 0x4底部
     * @param r
     * @param g
     * @param b
     */
    public void setColor(int mode, int r, int g, int b) {
        byte[] data = new byte[3];
        data[0] = (byte) r;
        data[1] = (byte) g;
        data[2] = (byte) b;
        switch (mode) {
            case 0x01://脖子
                ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x32, data);
                break;
            case 0x02:
            case 0x03://轮子
                ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x34, data);
                break;
            case 0x04://底部
                ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x33, data);
                break;
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

    /**
     * @param mode     0x1 脖子 0x2|0x3 轮子 0x4底部
     * @param wavemode 0x1 正弦波 0x2 宽波 0x3 高电平 0x4 低电平
     */
    public void setWave(int mode, int wavemode) {
        byte[] data = new byte[2];//"2字节：第1字节代表灯光模式：1脖子灯光模式，2轮子灯光模式，3底部灯光模式，
        //第2字节代表波形模式：1代表正弦波，2代表宽波，3代表高平，4代表低电平。"
        switch (mode) {
            case 0x01://脖子
                data[0] = 0x01;
                break;
            case 0x02:
            case 0x03://轮子
                data[0] = 0x02;
                break;
            case 0x04://底部
                data[0] = 0x03;
                break;
        }
        data[1] = (byte) wavemode;
        ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x36, data);
    }

    /**
     * 吸尘电机
     * @param power 吸尘功率大小(0~100)
     */
    public void setVacuumPower(int power) {
        byte[] data = new byte[1];
        data[0] = (byte) power;
        ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x39, data);
    }

    /**
     *
     * @param mode 0代表左右电机，1代表俯仰电机
     * @param angle 角度值：俯仰电机角度范围-15～+35（发送值+15），左右电机角度范围 -130～+130（发送值+130）
     */
    public void setNeckMotor(int mode, int angle) {
        byte[] data = new byte[3];
        data[0] = (byte) mode;
        data[1] = (byte) (angle >> 8 & 0xFF);
        data[2] = (byte) (angle & 0xFF);
        ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x3A, data);
    }

    /**
     * 上下脖子电机 progress = -15~30
     */
    public void SetNeckUPMotor(int progress) {
        progress += 15;
        setNeckMotor(1, progress);
    }

    /**
     * 左右脖子电机 progress =-130~130
     */
    public void SetNeckLRMotor(int progress) {
        progress += 130;
        setNeckMotor(0, progress);
    }

    // scratch M系列添加函数---------------------------------------scratch
    /**
     *运动
     * @param direction 方向：0~1(“向前”,”向后”)
     * @param speed 速度
     */
    public void Mrunmove(int direction, int speed) {
        int sign = (direction == 0) ? 1 : -1;
        speed *= sign;
        setMotorSpeed(speed, speed);
    }

    /**
     * 旋转
     * @param direction 方向：0~1("左转","右转")
     * @param speed 速度
     */
    public void MrunRotate(int direction, int speed) {
        if (direction == 0) {
            setMotorSpeed(0, speed);
        } else {
            setMotorSpeed(speed, 0);
        }
    }

    /**
     * 旋转+延时
     * @param direction 顺逆时针：0~1("顺时针","逆时针")
     * @param speed 速度
     */
    public void Mrunwise(int direction, int speed) {
        if (direction == 0) {//顺时针
            setMotorSpeed(speed, -speed);
        } else {//逆时针
            setMotorSpeed(-speed, speed);
        }
    }

    public void Mrunstop() {
        setMotorSpeed(0, 0);
    }

    /**
     *设置眼睛LED
     * @param colorSet 颜色类型：0~4("灭","红","绿","蓝","全亮")
     */
    public void setEyeColorMode(int colorSet) {
        int r = 0;
        int g = 0;
        int b = 0;
        switch (colorSet) {
            case 0x00:// 灭
                r = 0x00;
                g = 0x00;
                b = 0x00;
                break;
            case 0x01:// 红
                r = 0xFF;
                g = 0x00;
                b = 0x00;
                break;
            case 0x02:// 绿
                r = 0x00;
                g = 0xFF;
                b = 0x00;
                break;
            case 0x03:// 蓝
                r = 0x00;
                g = 0x00;
                b = 0xFF;
                break;
            case 0x04:// 全亮
                r = 0xFF;
                g = 0xFF;
                b = 0xFF;
                break;
        }
        setEyeColor(16, r, g, b);
    }

    /**
     *
     * @param part 0x1 脖子 0x2|0x3 轮子 0x4底部
     * @param colorSet (0-3)代表{红绿蓝白}
     * @param mode （0-3）代表{正弦，方波，常亮，常灭}
     */
    public void setColorMode(int part, int colorSet, int mode) {
        int r = 0;
        int g = 0;
        int b = 0;
        switch (colorSet) {
            case 0x00:// 红
                r = 0xFF;
                g = 0x00;
                b = 0x00;
                break;
            case 0x01:// 绿
                r = 0x00;
                g = 0xFF;
                b = 0x00;
                break;
            case 0x02:// 蓝
                r = 0x00;
                g = 0x00;
                b = 0xFF;
                break;
            case 0x03:// 白
                r = 0xFF;
                g = 0xFF;
                b = 0xFF;
                break;
        }
        setColor(part, r, g, b);
        setWave(part, mode + 1);
    }

    public void Msetled2(int color, int mode) {
        setColorMode(0x01, color, mode);
    }

    public void Msetled3(int color, int mode) {
        setColorMode(0x04, color, mode);
    }

    public void Msetled4(int color, int mode) {
        setColorMode(0x02, color, mode);
    }

    public int getposM(int num) {

        int value = 0;
        float[] SN = mSensor.getmO();
        LogMgr.e("SN value is : " + (SN == null));
        // 上下是value[1].下正 上负。
        if (SN != null) {
            // 对于上扬 下府这个值是需要减去70的。
            if (num == 0) {
                // 对于下府取10~90度之间。  //bug 11637 把5改成4（暂时）
                if (SN[1] >= (4 + 70) && SN[1] <= (90 + 70)) {
                    value = 1;
                }
                // 上扬。
            } else if (num == 1) {
                // 这里应该取-10 ~ -90之间。 基础值是70度。
                if (SN[1] >= (70 - 90) && SN[1] <= (70 - 10)) {
                    value = 1;
                }
                // 左翻，右翻。 左正右负。 左右翻转的值是对的。
            } else if (num == 2) {
                if (SN[2] >= 5 && SN[2] <= 90) {
                    value = 1;
                }
                // 右翻。
            } else if (num == 3) {
                if (SN[2] >= -90 && SN[2] <= -5) {
                    value = 1;
                }
            }

            LogMgr.e("上下 vlaue[1] is: " + SN[1] + "左右value[2] is: " + SN[2]);

        }

        return value;

    }

    /***************************************M5 巡线模块库***************************************/

    /******************************************* 电机控制部分要重新写 *****************************************************/
        /*机器人电机驱动函数，当时间不为0时，运动指定时间后会停止
        Lspeed：左电机速度
        Rspeed：右电机速度
        hold_time：如果为0，无意义；如果不为0，则按照写定速度运动该时间后停止
        */
    public void WER_SetMotor(int Lspeed, int Rspeed, float hold_time) {
        Lspeed = (int) (Lspeed * WER_Motor_scale_L);
        Rspeed = (int) (Rspeed * WER_Motor_scale_R);
        WER_Move(Lspeed, Rspeed);//M系列跟电机编号没关系了。
        if (hold_time != 0) {
            wait(hold_time);
            // WER_Move(-Lspeed,-Rspeed);
            // wait((float) 0.03);
            //Log.e("lib","wait time finish");
            WER_Move(0, 0);

        }
    }

    //operator范围：0:<;1:<=;2:==;3:!=;4:>=;5:>;
    private boolean checkOperatorExpression(int io, int operator, int reference) {
        int value = AI(io, true);
        if (operator == 0 && value >= reference) {
            return true;
        } else if (operator == 1 && value > reference) {
            return true;
        } else if (operator == 2 && value != reference) {
            return true;
        } else if (operator == 3 && value == reference) {
            return true;
        } else if (operator == 4 && value < reference) {
            return true;
        } else if (operator == 5 && value <= reference) {
            return true;
        }
        return false;
    }

    // 7字节： 第1字节： 代表IO端口号
    // 第2字节： 代表比较符，范围：0:<;1:<=;2:==;3:!=;4:>=;5:>;
    // 第3-4字节：代表阈值参数，范围0~4095
    // 第5字节： 代表左电机速度，范围0~200 (底层转换为-100~100)
    // 第6字节： 代表右电机速度，范围0~200 (底层转换为-100~100)
    // 第7字节： 代表结束后是否停车，1是，0否
    public void WER_SetMotor_L(int IO, int operator, int reference, int SpeedL, int SpeedR, int stop) {
        //这里用0:<;1:<=;2:==;3:!=;4:>=;5:> 来代表6个符号。
        IO = IO + 2;
        WER_SetMotor(SpeedL, SpeedR, 0);
        while (checkOperatorExpression(IO, operator, reference) && ai_flag){
        }
        if (stop == 1) {
            WER_SetMotor(0, 0, 0);
        }//是否停止
    }

    //电机有3个参数，开闭环，左右轮速度。
    private void WER_Move(int Lspeed, int Rspeed) {
        byte[] data = new byte[8];
        data[0] = 0x03;
        data[1] = (byte) ((Lspeed + 100) & 0xff);
        data[2] = (byte) ((Rspeed + 100) & 0xff);
        if (WER_motor_sign == 1) {//电机类型(0:闭环;1:开环;默认值:0，注：左右电机同一种类型)
            data[3] = 0x02;//2代表开环
        } else if (WER_motor_sign == 0) {//电机类型(0:闭环;1:开环;默认值:0，注：左右电机同一种类型)
            data[3] = 0x00;//0代表闭环
        }
        ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x41, data);
    }

    /*************************************** 这里写一个AI线程 *********************************************************************/
    // 启动停止线程，以及关闭电机。这里包含一个总的关闭方式。
    private volatile int[] ai_value = new int[8];//这里为了与之前的序号保持一致，AI[0]不用了。

    public int AI(int id) {
        return 100 - ai_value[id];
    }

    public int AI(int id, boolean expression) {
        return ai_value[id];
    }

    private static boolean ai_flag = false;

    private Thread AI_Thread = null;

    public void AI_start() {

        ai_flag = true;
        ai_num();
        AI_Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LogMgr.e("AI_Thread start!!!!!!!!");
                while (ai_flag) {
                    try {
                        Thread.sleep(10);
                        ai_num();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                AI_Thread = null;
                LogMgr.e("AI_Thread stop!!!!!!!!");
            }

        });
        AI_Thread.start();

    }

    public void AI_stop() {
        ai_flag = false;
        if (AI_Thread != null) {
            AI_Thread.interrupt();
            AI_Thread = null;
            //wait(0.1f);
            WER_Move(0, 0);
            //ai_flag = true;
            lineflag = 0;
            for (int j = 0; j < ai_value.length; j++) {
                ai_value[j] = 0;
            }
        }
    }

    private void ai_num() {
        try {
            byte[] readBuff = ProtocolUtils.sendProtocol((byte) 0x02, (byte) 0xA3, (byte) 0x38, null);
            if (readBuff[0] == (byte) 0xAA && readBuff[1] == (byte) 0x55 &&
                    readBuff[4] == (byte) 0x02 && readBuff[5] == (byte) 0xF0 && readBuff[6] == (byte) 0x31) {
                ai_value[0] = readBuff[11] & 0xFF;
                ai_value[1] = readBuff[11] & 0xFF;
                ai_value[2] = readBuff[11] & 0xFF;
                ai_value[3] = readBuff[12] & 0xFF;
                ai_value[4] = readBuff[13] & 0xFF;
                ai_value[5] = readBuff[14] & 0xFF;
                ai_value[6] = readBuff[15] & 0xFF;
                ai_value[7] = readBuff[15] & 0xFF;
                //LogMgr.d("ai_value[]: " + Arrays.toString(ai_value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ///////////////////////////////////////////////巡线模块库/////////////////////////////////////////////////
    /* 初始化模块定义的全局变量 */
//	private int WER_Motor_DC_L; // 左轮电机所接DC口编号。
//	private int WER_Motor_DC_R; // 右轮电机所接DC口编号。M系列不需要电机编号。
    private float WER_Motor_scale_L; // 左轮电机输出系数，取值范围-1.0-1.0。
    private float WER_Motor_scale_R; // 右轮电机输出系数，取值范围-1.0-1.0。
    private int WER_Floor_Quantity; // 机器人灰度传感器的数量，5或者7。
    //private int WER_Floor_IO_1; // 左边起第1个灰度所接I/O编号。
    private int WER_Floor_IO_2; // 左边起第2个灰度所接I/O编号。
    private int WER_Floor_IO_3; // 左边起第3个灰度所接I/O编号。
    private int WER_Floor_IO_4; // 左边起第4个灰度所接I/O编号。
    private int WER_Floor_IO_5; // 左边起第5个灰度所接I/O编号。
    private int WER_Floor_IO_6; // 左边起第6个灰度所接I/O编号。
    //private int WER_Floor_IO_7; // 左边起第7个灰度所接I/O编号。
    private int WER_line_colour; // 白底黑线为0，黑底白线为1。
    private float WER_Floor_Coe; // 灰度临界值偏移情况，取值范围0~1，越小越靠近白色，越大越靠近黑色，0.5时是中间值。
    int WER_motor_sign; // 电机类型，取值为0时表示开环电机，取值为1时表示闭环电机。
    private int lineflag;

    private int BLACK_COLOR = 60; // 这里改为30.
    private int /*F1 = BLACK_COLOR,*/ F2 = BLACK_COLOR, F3 = BLACK_COLOR, F4 = BLACK_COLOR,
            F5 = BLACK_COLOR, F6 = BLACK_COLOR/*, F7 = BLACK_COLOR*/; // 灰度临界值

    void wait(float f) {
        long t1 = seconds();

        while (true) {
            if (seconds() - t1 > 1000 * f) {
                break;
            }
        }
    }

    private boolean mIsWaiting = false;

    public void startWaiting() {
        mIsWaiting = true;
        while (mIsWaiting) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopWaiting() {
        mIsWaiting = false;
    }

    long seconds() {
        return System.currentTimeMillis();
    }

    // 初始化五口AI。这个是一样的，12共用一个口，67共用一个口。
    public void WER_InitRobot_5(float PowerL, float PowerR, int lineColor, float Coe, int motorType) {
        WER_Motor_scale_L = PowerL; // 左轮电机输出系数，取值范围-1.0-1.0
        WER_Motor_scale_R = PowerR; // 右轮电机输出系数，取值范围-1.0-1.0
        WER_Floor_Quantity = 5; // 机器人灰度传感器的数量，5或者7

        //WER_Floor_IO_1 = 2; // 左边起第1个灰度所接I/O编号                       ------------M系列这里需要写死。不支持自由编程。
        WER_Floor_IO_2 = 2; // 左边起第2个灰度所接I/O编号
        WER_Floor_IO_3 = 3; // 左边起第3个灰度所接I/O编号
        WER_Floor_IO_4 = 4; // 左边起第4个灰度所接I/O编号
        WER_Floor_IO_5 = 5; // 左边起第5个灰度所接I/O编号
        WER_Floor_IO_6 = 6; // 左边起第6个灰度所接I/O编号
        //WER_Floor_IO_7 = 6; // 左边起第7个灰度所接I/O编号
        WER_line_colour = lineColor; // 白底黑线为0，黑底白线为1
        WER_Floor_Coe = Coe; // 灰度临界值偏移情况，取值范围0~1，越小越靠近白色，越大越靠近黑色，0.5时是中间值。
        WER_motor_sign = motorType; // 电机类型(0:闭环;1:开环;默认值:0，注：左右电机同一种类型)

        AI_start();
    }

    public void WER_LineWay_T(int speed, int Lcut, int Rcut, int stop, float time) {
        int SpeedL, SpeedR;
        long t;
        SpeedL = speed - Lcut;
        SpeedR = speed - Rcut;
        WER_SetMotor(SpeedL, SpeedR, 0);
        t = seconds();
        while (ai_flag) {
            if (seconds() - t > 1000 * time) {
                break;
            } else {
                WER_lineB(SpeedL, SpeedR);
            }

        }
        if (stop == 1) {
            WER_SetMotor(0, 0, 0);
        }// 是否停止
    }

    public void WER_Around(int speed_L, int speed_R, int stop, int N, int P) {

        int i;
        WER_SetMotor(speed_L, speed_R, 0);
        if (WER_line_colour == 0) {

            if (speed_L > speed_R) {
                for (i = 0; i < N && ai_flag; i++) {
                    //这里的转弯只有停中线。
                    while (AI(WER_Floor_IO_6) < F6 && ai_flag) {
                    }
                    while ((AI(WER_Floor_IO_5) < F5) && ai_flag) {
                    }
                    while ((AI(WER_Floor_IO_4) < F3) && ai_flag) {
                    }
                }
            } else if (speed_L < speed_R) {
                for (i = 0; i < N && ai_flag; i++) {
                    //对于M的只有中线。
                    while (AI(WER_Floor_IO_2) < F2 && ai_flag) {
                    }
                    while ((AI(WER_Floor_IO_3) < F2) && ai_flag) {
                    }
                    while ((AI(WER_Floor_IO_4) < F3) && ai_flag) {
                    }

                }
            } else {
            }
        }
        if (stop == 1) {
            WER_SetMotor(0, 0, 0);
        }
    }

    public void WER_lineB(int SpeedL, int SpeedR) {

        int FN1 = AI(WER_Floor_IO_2);
        int FN2 = AI(WER_Floor_IO_3);
        int FN3 = AI(WER_Floor_IO_4);
        int FN4 = AI(WER_Floor_IO_5);
        int FN5 = AI(WER_Floor_IO_6);
        if ((FN3 > F3)) {
            if (FN2 > F2) {
                if (lineflag == -1) {
                    return;
                } else {
                    lineflag = -1;
                    WER_SetMotor((int) (SpeedL * 0.85), SpeedR, 0);
                }
            } else if (FN4 > F4) {
                if (lineflag == 1) {
                    return;
                } else {
                    lineflag = 1;
                    WER_SetMotor(SpeedL, (int) (SpeedR * 0.85), 0);
                }
            } else if (lineflag == 0) {
                return;
            } else {
                lineflag = 0;
                WER_SetMotor(SpeedL, SpeedR, 0);
            }
        } else if (FN2 > F2) {
            if (lineflag == -2) {
                return;
            } else {
                lineflag = -2;
                WER_SetMotor((int) (SpeedL * 0.5), SpeedR, 0);
            }
        } else if (FN4 > F4) {
            if (lineflag == 2) {
                return;
            } else {
                lineflag = 2;
                WER_SetMotor(SpeedL, (int) (SpeedR * 0.5), 0);
            }
        } else if (FN1 > F2) {
            if (lineflag == -4) {
                return;
            } else {
                lineflag = -4;
                WER_SetMotor(-(int) (SpeedL * 0.5), SpeedR, 0);
            }
        } else if (FN5 > F5) {
            if (lineflag == 4) {
                return;
            } else {
                lineflag = 4;
                WER_SetMotor(SpeedL, -(int) (SpeedR * 0.5), 0);
            }
        }
    }

    /*******************************路口寻线******************************************/
        /*
     * 按路口巡线，分为左侧路口和右侧路口。 N：该模块循环次数； Crossing：路口标志，左侧路口取值为0，右侧路口取值为1；
     * speed：中间灰度压线时两个电机速度中的较大值； Lcut：逆时针走圆弧时左轮降低的速度值； Rcut：顺时针走圆弧是右轮降低的速度值；
     * stop：标志该巡线模块结束后是否停止运动，1停止，0不停止 time：冲路口时间
     */
    public void WER_LineWay_C(int N, int Crossing, int speed, int Lcut, int Rcut, int stop, float time) {
        int SpeedL, SpeedR;
        SpeedL = speed - Lcut;
        SpeedR = speed - Rcut;
        WER_SetMotor(SpeedL, SpeedR, 0);
        LogMgr.d(String.format(Locale.US,"WER_LineWay_C[%d,%d,%d,%d,%d]", F2,F3,F4,F5,F6));

        for (int i = 0; i < N; i++) {
            if (WER_line_colour == 0) {
                if (Crossing == 0) {
                    while (AI(WER_Floor_IO_2) < F2 && ai_flag) {// 巡黑线
                        //LogMgr.v("WER_LineWay_C[" + i + "] L:AI(WER_Floor_IO_2) = " + AI(WER_Floor_IO_2));
                        WER_lineBL(SpeedL, SpeedR);//左侧路口寻线。
                    }
                    LogMgr.d("WER_LineWay_C[" + i + "] L END:AI(WER_Floor_IO_2) = " + AI(WER_Floor_IO_2));
                    WER_SetMotor(SpeedL, SpeedR, 0);
                } else {
                    while (AI(WER_Floor_IO_6) < F6 && ai_flag) {// 巡黑线
                        //LogMgr.v("WER_LineWay_C[" + i + "] R:AI(WER_Floor_IO_6) = " + AI(WER_Floor_IO_6));
                        WER_lineBR(SpeedL, SpeedR);
                    }
                    LogMgr.d("WER_LineWay_C[" + i + "] R END:AI(WER_Floor_IO_6) = " + AI(WER_Floor_IO_6));
                    WER_SetMotor(SpeedL, SpeedR, 0);
                }
                wait(time); // 前面路口冲路口
            }
        }
        //wait(time); // 最后一个路口冲路口
        LogMgr.d("WER_LineWay_C[" + N + "] END stop = " + stop);

        if (stop == 1) {
            WER_SetMotor(0, 0, 0);
        }
    }

    /************************************左侧路口寻线**************************************************/
    //@version
    public void WER_lineBL(int SpeedL, int SpeedR) {

        // int FN1=AI(WER_Floor_IO_2);
        int FN2 = AI(WER_Floor_IO_3);
        int FN3 = AI(WER_Floor_IO_4);
        int FN4 = AI(WER_Floor_IO_5);
        int FN5 = AI(WER_Floor_IO_6);
        //LogMgr.e("gray","-----------WER_lineBL "+0+" ,"+FN2+","+FN3+" ,"+FN4+","+FN5);
        if ((FN3 > F3)) { // 全部属于微调。
            if (FN2 > F2) {
                if (lineflag == -1) {
                    return;
                } else {
                    lineflag = -1;
                    WER_SetMotor((int) (SpeedL * 0.9), SpeedR, 0);
                }
            } else if (FN4 > F4) {
                if (lineflag == 1) {
                    return;
                } else {
                    lineflag = 1;
                    WER_SetMotor(SpeedL, (int) (SpeedR * 0.9), 0);
                }
            } else if (lineflag == 0) {
                return;
            } else {
                lineflag = 0;
                WER_SetMotor(SpeedL, SpeedR, 0);
            }
        } else if (FN2 > F2) {
            if (lineflag == -2) {
                return;
            } else {
                lineflag = -2;
                WER_SetMotor((int) (SpeedL * 0.3), SpeedR, 0);
            }
        } else if (FN4 > F4) {
            if (lineflag == 2) {
                return;
            } else {
                lineflag = 2;
                WER_SetMotor(SpeedL, (int) (SpeedR * 0.3), 0);
            }
        } else if (FN5 > F5) {
            if (lineflag == 4) {
                return;
            } else {
                lineflag = 4;
                WER_SetMotor(SpeedL, -(int) (SpeedR * 0.2), 0);
            }
        }
    }

    /***************************************右侧路口寻线*************************************************/
    public void WER_lineBR(int SpeedL, int SpeedR) {

        int FN1 = AI(WER_Floor_IO_2);
        int FN2 = AI(WER_Floor_IO_3);
        int FN3 = AI(WER_Floor_IO_4);
        int FN4 = AI(WER_Floor_IO_5);
        //int FN5 = AI(WER_Floor_IO_6);

        if ((FN3 > F3)) {

            if (FN4 > F4) {
                if (lineflag == 1) {
                    return;
                } else {
                    lineflag = 1;
                    WER_SetMotor((int) (SpeedL * 0.9), SpeedR, 0);
                }
            } else if (FN2 > F2) {
                if (lineflag == -1) {
                    return;
                } else {
                    lineflag = -1;
                    WER_SetMotor((int) (SpeedL * 0.9), SpeedR, 0);
                }
            } else if (lineflag == 0) {
                return;
            } else {
                lineflag = 0;
                WER_SetMotor(SpeedL, SpeedR, 0);
            }
            //----------------------------------FN3-------------------
        } else if (FN2 > F2) {
            if (lineflag == -2) {
                return;
            } else {
                lineflag = -2;
                WER_SetMotor((int) (SpeedL * 0.3), SpeedR, 0);
            }
        } else if (FN4 > F4) {
            if (lineflag == 2) {
                return;
            } else {
                lineflag = 2;
                WER_SetMotor(SpeedL, (int) (SpeedR * 0.3), 0);
            }
        } else if (FN1 > F2) {
            if (lineflag == -4) {
                return;
            } else {
                lineflag = -4;
                WER_SetMotor(-(int) (SpeedL * 0.2), SpeedR, 0);
            }
        }

    }

    //高级寻线。
    public void WER_LineWay_O(int IO, int operator, int reference, int speed, int Lcut, int Rcut, int stop) {
        //这里用0:<;1:<=;2:==;3:!=;4:>=;5:> 来代表6个符号。
        int SpeedL = speed - Lcut;
        int SpeedR = speed - Rcut;
        IO = IO + 2;
        WER_SetMotor(SpeedL, SpeedR, 0);
        while (checkOperatorExpression(IO, operator, reference) && ai_flag){
        }
        if (stop == 1) {
            WER_SetMotor(0, 0, 0);
        }//是否停止
    }

    //环境采集
    public void readandsend(int num) {

        if (num == 1) {// 这里是7灰度的值 1是线 2是背景。
            String grayvalue1 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT1);
            String[] temp = grayvalue1.split(",");
            String grayvalue2 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT2);
            String[] temp2 = grayvalue2.split(",");
            if (temp.length > 6 && temp2.length > 6) {
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
                environment(BLACK_COLOR, BLACK_COLOR, BLACK_COLOR, BLACK_COLOR, BLACK_COLOR, BLACK_COLOR, BLACK_COLOR);
            }
        } else {// 这里是5灰度的值。
            String grayvalue1 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT1);
            String[] temp = grayvalue1.split(",");
            String grayvalue2 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT2);
            String[] temp2 = grayvalue2.split(",");
            if (temp.length > 4 && temp2.length > 4) {
                int AI0 = (Integer.parseInt(temp[0]) + Integer.parseInt(temp2[0])) / 2;
                int AI1 = (Integer.parseInt(temp[1]) + Integer.parseInt(temp2[1])) / 2;
                int AI2 = (Integer.parseInt(temp[2]) + Integer.parseInt(temp2[2])) / 2;
                int AI3 = (Integer.parseInt(temp[3]) + Integer.parseInt(temp2[3])) / 2;
                int AI4 = (Integer.parseInt(temp[4]) + Integer.parseInt(temp2[4])) / 2;
                LogMgr.e("AI0:" + AI0 + "," + "AI1:" + AI1 + "," + "AI2:" + AI2 + "," + "AI3:" + AI3 + "," + "AI4:" + AI4 + "," + "AI5:");
                environment(AI0, 0, AI1, AI2, AI3, 0, AI4);// init之后应该可以这么发。
            } else {
                environment(BLACK_COLOR, 0, BLACK_COLOR, BLACK_COLOR, BLACK_COLOR, 0, BLACK_COLOR);
            }
        }

        stopWaiting();
    }

    public void environment(int a0, int a1, int a2, int a3, int a4, int a5, int a6) {
        //F1 = a0;
        F2 = a0;//a1;
        F3 = a2;
        F4 = a3;
        F5 = a4;
        F6 = a6;//a5;
        //F7 = a6;
        LogMgr.e("F2:" + F2 + "," + "F3:" + F3 + "," + "F4:" + F4 + "," + "F5:" + F5 + "," + "F6:" + F6);

    }

    // 这里写一个获取AI的方法
    public int[] ReadAIValue() {
        int[] AI_value = new int[7];

        AI_value[0] = ai_value[2];
        AI_value[1] = ai_value[2];
        ;
        AI_value[2] = ai_value[3];
        AI_value[3] = ai_value[4];
        AI_value[4] = ai_value[5];
        AI_value[5] = ai_value[6];
        AI_value[6] = ai_value[6];

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return AI_value;
    }

}
