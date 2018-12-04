package com.abilix.explainer.helper;

import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.ProtocolUtils;

import java.util.Locale;

public class SExplainerHelper extends AExplainHelper {
    //
    /*0xA3	0xA2(所有舵机控制)	4字节	"数据位分为三部分：
    第1部分：1字节，表示舵机个数N
    第2部分：N字节，表示舵机ID号(每个ID号使用1字节表示)
    第3部分：N*2字节，表示舵机角度(每个角度使用2字节表示，512为舵机0°，范围0~1023)
    例：控制1、3、7号舵机分别转到29°、58°、0°（舵机范围1对应0.29°，29°相当于612），数据位可表示如下：
            03 01 03 07 02 64 02 C8 02 00"			*/
    public void setAllMotorS(int[] ids, int[] angles, int[] speeds) {
        int curIndex = 0;
        byte[] data = new byte[ids.length * 3 + 1];
        data[curIndex] = (byte) (ids.length & 0xFF);
        curIndex++;
        for (int i = 0; i < ids.length; i++) {
            data[i + curIndex] = (byte) (ids[i] & 0xFF);
        }
        curIndex += ids.length;
        for (int i = 0; i < ids.length; i++) {
            int angle = 512 + angles[i] * 1024 / 300;
            data[curIndex + i * 2] = (byte) ((angle << 8) & 0xFF);
            data[curIndex + i * 2 + 1] = (byte) (angle & 0xFF);
        }
        ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0xA3, (byte) 0xA2, data);
    }

    public void setSingleMotorS(int id, int speed, int angle) {
        runServoS5(id, speed, angle);
    }

    /**
     *设置车轮模式开关
     * @param id 表示电机ID，范围：1~22
     * @param mode 表示模式，0：关节模式；1：车轮模式
     */
    public void setSingleMotorMode(int id, int mode) {
        setServoS5Mode(id, mode);
    }

    /**
     *设置车轮模式电机
     * @param id 表示电机ID，范围：1~22。
     * @param speed 表示电机速度，范围：-1023~-200，0，200~1023。
     */
    public void setSingleMotorInWheelMode(int id, int speed) {
        int mode;
        if (speed != 0) {
            mode = speed > 0 ? 1 : 2;
        } else {
            mode = 0;
        }
        runServoS5InWheelMode(id, mode, Math.abs(speed));
    }

    //void setHeadLed(int,int,int,int)	头部LED
    /*"第一个参数：4字节，int类型，表示状态，0x00代表关闭，0x01代表开启。
    第二个参数：4字节:(单色蓝光 就只用第1个字节 )
    第1个字节:色：0x00代表单色蓝光;0x01代表多色光;
    第2个字节:红色：0-代表不设置，255-代表设置;
    第3个字节:绿色：0-代表不设置，255-代表设置;
    第4个字节:蓝色：0-代表不设置，255-代表设置;
    第三个参数：4字节，int类型，表示频率，范围1~10(次/s)。
    第四个参数：4字节，int类型，表示亮度，范围10~100。"*/
    public void setHeadLed(int onOff, int type, int rSet, int gSet, int bSet, int frequency, int luminance) {
        if (onOff == 0) {//0x00代表关闭
            ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0x22, (byte) 0x08, new byte[]{0x00});
        } else {//0x01代表开启
            byte ledControl = (byte) ((frequency & 0x0F) << 4 | (luminance &0x0F));
            ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0x22, (byte) 0x08, new byte[]{ledControl});
            byte[] ledSetData = new byte[3];
            if (type == 0) {//0x00代表单色蓝光
                ledSetData[0] = 0x00;
                ledSetData[1] = 0x00;
                ledSetData[2] = (byte) 0xFF;
            } else if (type == 1) {//0x01代表多色光
                ledSetData[0] = (byte) rSet;
                ledSetData[1] = (byte) gSet;
                ledSetData[2] = (byte) bSet;
            }
            ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0xA3, (byte) 0xA0, ledSetData);
        }
    }

    /**
     * 超声数据
     */
    public int GetUtrasonic() {
        try {
            byte[] data = ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0xA3, (byte) 0xA3, null);
                /*readbuff::aa 55 00 0a 05 f0 a0 00 00 00 00 00 c7 65 00 00 00 00 00 00*/
            if (data[0] == (byte) 0xAA && data[1] == (byte) 0x55 &&
                    data[5] == (byte) 0xF0 && data[6] == (byte) 0xA0) {
                int value = ByteUtils.byte2int_2byteHL(data, 11);
                return value > 200 ? 199 : value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /*********************************S5解释执行部分*****************************************/
    /*第一个参数舵机ID号(1-8)； 第二个参数舵机速度（0-1023）；第三个参数舵机角度（-180~+180）
     * 启动舵机ID【1~8】舵机速度【0~1023】舵机角度【-180~180】*/
    public void runServoS5(int id, int speed, int angle) {
        byte[] data = {(byte) 0xFF, (byte) 0xFF, 0x00, 0x07, 0x03, (byte) 0x1E, 0x00, 0x00, 0x00, 0x00, 0x00};

        data[2] = (byte) id;
        angle = 512 + angle * 1024 / 300;
        data[6] = (byte) (angle & 0xFF);
        data[7] = (byte) ((angle >> 8) & 0xFF);
        data[8] = (byte) (speed & 0xFF);
        data[9] = (byte) ((speed >> 8) & 0xFF);
        byte check = 0;
        for (int i = 2; i < 10; i++) {
            check += data[i];
        }
        data[10] = (byte) ~(check & 0xFF);
        LogMgr.e("电机数据 " + ByteUtils.bytesToString(data, data.length));
        ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0xA3, (byte) 0xA5, data);

        //runServoS5New(id, speed, angle);
    }

    private void runServoS5New(int id, int speed, int angle) {
        byte[] data = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x09, (byte) 0x83, (byte) 0x1E, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        data[7] = (byte) id;
        angle = 512 + angle * 1024 / 300;
        data[8] = (byte) (angle & 0xFF);
        data[9] = (byte) ((angle >> 8) & 0xFF);
        data[10] = (byte) (speed & 0xFF);
        data[11] = (byte) ((speed >> 8) & 0xFF);
        byte check = 0;
        for (int i = 2; i < 12; i++) {
            check += data[i];
        }
        data[12] = (byte) ~(check & 0xFF);
        LogMgr.e("电机数据 " + ByteUtils.bytesToString(data, data.length));
        ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0xA3, (byte) 0xA5, data);
    }

    /**
     *设置车轮模式开关
     * @param id 表示电机ID，范围：1~22
     * @param mode 表示模式，0：关节模式；1：车轮模式
     */
    private void setServoS5Mode(int id, int mode) {
        byte[] data = new byte[3];

        data[0] = (byte) 0x02;
        data[1] = (byte) id;
        data[2] = (byte) mode;

        ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0xA3, (byte) 0xA9, data);
    }

    /**
     * 车轮模式转动
     * @param id 舵机ID
     * @param mode 0x00 停止转动 0x01 正转 0x02 反转
     * @param speed 速度值 高位在前低位在后
     */
    private void runServoS5InWheelMode(int id, int mode, int speed) {
        byte[] data = new byte[5];

        data[0] = (byte) 0x03;
        data[1] = (byte) id;
        data[2] = (byte) mode;
        data[3] = (byte) ((speed >> 8) & 0xFF);
        data[4] = (byte) (speed & 0xFF);

        ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0xA3, (byte) 0xAA, data);
    }

    //上电释放归零写一个函数。
    /*1字节：ID个数N
    2字节：舵机ID号
    3字节：舵机配置：0x01代表固定； 0x02代表释放； 0x03代表归零
    后续表示剩余N-1个舵机配置：同2/3字节*/
    private void controlServoS5(int id, int type) {
        byte[] data;
        if (id == 0) {
            data = new byte[45];
            data[0] = 22;
            for (int i = 1; i <= 22; i++) {
                data[2 * i - 1] = (byte) i;
                data[2 * i] = (byte) type;
            }
        } else {
            data = new byte[3];
            data[0] = (byte) 1;//就是一个。
            data[1] = (byte) id;
            data[2] = (byte) type;
        }
        ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0xA3, (byte) 0x65, data);//按照H的协议来。
    }

    /*第一个参数舵机ID号(0-8)，0为全部 舵机ID【全部/1-8】上电*/
    public void startServoS5(int id) {
        controlServoS5(id, 1);
    }

    public void stopServoS5(int id) {
        controlServoS5(id, 2);
    }

    public void zeroServoS5(int id) {
        controlServoS5(id, 3);
    }

    public String getServoAngleString(int id) {
        int[] angles = getServoAngles();
        String content = "";
        if (id > 0 && id <= angles.length) {
            int angle = angles[id - 1];
            if (angle  == 150 || angle == -150) {
                angle = 0;
            }
            content += String.format(Locale.US, "%d:%d", id, angle);
        } else if (id == 0){
            int count = 0;
            for (int i = 0; i < angles.length; i++) {
                if (angles[i] == 150 || angles[i] == -150) {
                    continue;
                }
                content += String.format(Locale.US, "%d:%d  ", i + 1, angles[i]);
                if (count % 3 == 2) {
                    content += "\n";
                }
                count++;
            }
        }
        return content;
    }
    public String getGrayString(int id) {
        String content = "";
        if(id <0 || id >4){
            return "下标越界";
        }
        float[] SN = mSensor.getmO();
        switch (id){
            case 0://全部 X,Y,Z"\n"
                content = "X:"+SN[1] +"\n"+"Y:"+SN[2] +"\n"+"Z:"+SN[0] ;
                break;
            case 1:
                content = "X:"+SN[1];
                break;
            case 2:
                content ="Y:"+SN[2];
                break;
            case 3:
                content ="Z:"+SN[0];
                break;
        }

        return content;
    }
    private final int SERVO_COUNT_MAX = 22;

    public int[] getServoAngles() {
        int[] angles = new int[SERVO_COUNT_MAX];
        try {
            byte[] data = ProtocolUtils.sendProtocol((byte) 0x05, (byte) 0xA3, (byte) 0x61, null);
                /*readbuff::aa 55 00 0a 05 f0 a0 00 00 00 00 00 c7 65 00 00 00 00 00 00*/
            if (data[0] == (byte) 0xAA && data[1] == (byte) 0x55 &&
                    data[5] == (byte) 0xF0 && data[6] == (byte) 0x61) {
                String content = "angles::";
                for (int k = 0; k < angles.length; k++) {
                    angles[k] = convertToAngle(ByteUtils.byte2int_2byteHL(data, 11 + 2 * k));
                    content += angles[k] + " ";
                }
                LogMgr.e(content);
                return angles;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < angles.length; i++) {
            angles[i] = convertToAngle(0);
        }
        return angles;
    }

    private int convertToAngle(int value) {
        /*if (value == 0) {
            return 0;
        }*/
        //value = 512 + angle * 1024 / 300;
        return (value - 512) * 300 / 1024;
    }

    public void setLedS5(int luminance, int frequency) {

    }

    public int findBarS5() {
        int dis = findBarDistanceS5();
        if (dis > 0 && dis < 20) {
            return 1;
        }
        return 0;
    }

    public int findBarDistanceS5() {
        //获取超声
        return GetUtrasonic();
    }

    public int getmYSensor(int num){
        int value = 0;
        if(num < 0 || num > 3){
            return value;
        }
        float[] SN = mSensor.getmO();
        switch (num){
            case 0:
                value = (int) SN[1];
                break;
            case 1:
                value = (int) SN[2];
                break;
            case 2:
                value = (int) SN[0];
                break;
        }
        return value;
    }
}
