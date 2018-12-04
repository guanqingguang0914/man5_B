package com.abilix.explainer;

import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;


public class MotorBytesBuilder {
    public byte[] motorBytes;
    public int port;

    public MotorBytesBuilder() {
    }

    public MotorBytesBuilder setMotorBytes(byte[] motorBytes) {
        this.motorBytes = motorBytes;
        return this;
    }

    // 设置端口值
    public MotorBytesBuilder setMotorPort(int port) {
        this.port = port;
        return this;
    }

    // 设置大小电机
    public MotorBytesBuilder setMotorType(int motorType) {
        motorBytes[0] = (byte) motorType;
        return this;
    }

    // 设置速度、圈数、角度
    public MotorBytesBuilder setValueType(byte[] praramBytes) {
        if ((int) ((praramBytes[port] >> 2) & 0x0f) == 0) {
            motorBytes[1] = (byte) 0x00;
        } else if ((int) ((praramBytes[port] >> 2) & 0x0f) == 1) {
            motorBytes[1] = (byte) 0x02;
        } else if ((int) ((praramBytes[port] >> 2) & 0x0f) == 2) {
            motorBytes[1] = (byte) 0x01;
        }
        return this;
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

    // 闭环设置速度
    public MotorBytesBuilder setCloseLoopSpeed(byte[] set_bytes, byte[] speed_bytes, float[] mValue) {
        motorBytes[2] = convertMotorSpeedValue((int) speed_bytes[port]);
        boolean isReferenceVariable = (set_bytes[port] & 0x01) == 0x01;
        LogMgr.d("闭环电机port" + port + "速度是否是引用变量::" + isReferenceVariable);
        if (isReferenceVariable) {
            int valuePosition = (int) speed_bytes[port] & 0xFF;
            float value = getMvalue(mValue, valuePosition);
            LogMgr.d("引用变量位置::" + valuePosition + "    " + "引用变量的值:" + value);
            motorBytes[2] = convertMotorSpeedValue((int) value);
        }
        LogMgr.d("闭环电机 " + port + " 端口电机速度:" + (motorBytes[2] & 0xFF));
        return this;
    }

    //开环电机设置速度
    public MotorBytesBuilder setOpenLoopSpeed(byte[] set_bytes, byte[] speed_bytes, float[] mValue) {
        motorBytes[0] = convertMotorSpeedValue((int) speed_bytes[port]);
        boolean isReferenceVariable = (set_bytes[port] & 0x01) == 0x01;
        LogMgr.d("开环电机port" + port + "速度是否是引用变量::" + isReferenceVariable);
        if (isReferenceVariable) {
            int valuePosition = (int) speed_bytes[port] & 0xFF;
            float value = getMvalue(mValue, valuePosition);
            LogMgr.d("引用变量位置::" + valuePosition + "    " + "引用变量的值:" + value);
            motorBytes[0] = convertMotorSpeedValue((int) value);
        }
        LogMgr.d("开环电机 " + port + " 端口电机速度:" + (motorBytes[0] & 0xFF));
        return this;
    }

    // 设置类型的值
    public MotorBytesBuilder setTypeValue(byte[] set_bytes, byte[] value_bytes, float[] mValue) {
        boolean isReferenceVariable = (set_bytes[port] & 0x02) == 0x02;
        LogMgr.d("port" + port + "类型值是否是引用变量::" + isReferenceVariable);
        if (isReferenceVariable) {
            int valuePosition = ByteUtils.byte2int_2byteHL(value_bytes, port * 2);
            int value = (int) getMvalue(mValue, valuePosition);
            LogMgr.d("引用变量位置::" + valuePosition + "    " + "引用变量的值:" + value);
            motorBytes[3] = (byte) (value >> 8 & 0xFF);
            motorBytes[4] = (byte) (value & 0xFF);
        } else {
            LogMgr.d("port" + port + "数值::" + ByteUtils.byte2int_2byteHL(value_bytes, port * 2));
            System.arraycopy(value_bytes, port * 2, motorBytes, 3, 2);
        }
        return this;
    }

    public byte[] build() {
        return motorBytes;
    }

    private float getMvalue(float[] val, int index) {
        return val[index];
    }

}
