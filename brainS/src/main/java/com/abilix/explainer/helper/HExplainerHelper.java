package com.abilix.explainer.helper;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.utils.Utils;
import com.abilix.explainer.helper.Interface.IHRobot;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.ProtocolUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

public class HExplainerHelper extends AExplainHelper implements IHRobot {
    //
    /*0xA3	0xA2(所有舵机控制)	4字节	"数据位分为三部分：
    第1部分：1字节，表示舵机个数N
    第2部分：N字节，表示舵机ID号(每个ID号使用1字节表示)
    第3部分：N*2字节，表示舵机角度(每个角度使用2字节表示，512为舵机0°，范围0~1023)
    例：控制1、3、7号舵机分别转到29°、58°、0°（舵机范围1对应0.29°，29°相当于612），数据位可表示如下：
            03 01 03 07 02 64 02 C8 02 00"			*/
    public void setAllMotorH(int[] ids, int[] angles) {
        int curIndex = 0;
        byte[] data = new byte[ids.length * 3 + 1];
        data[curIndex] = (byte) (ids.length & 0xFF);
        curIndex++;
        for (int i = 0; i < ids.length; i++) {
            data[i + curIndex] = (byte) (ids[i] & 0xFF);
        }
        curIndex += ids.length;
        for (int i = 0; i < ids.length; i++) {
            int angle = 512 + angles[i] * 100 / 29;
            data[curIndex + i * 2] = (byte) ((angle << 8) & 0xFF);
            data[curIndex + i * 2 + 1] = (byte) (angle & 0xFF);
        }
        ProtocolUtils.sendProtocol((byte) 0x03, (byte) 0xA3, (byte) 0xA2, data);
    }

    /**
     *
     * @param type 表示部位，0x00代表头部灯
     * @param rgb 部位为头部灯有效 RGB
     * @param mode H34无效
     */
    public void setLed(int type, byte[]rgb, int mode) {
        switch (type) {
            case 0x00://0x00代表头部
                setHeadLed(rgb);
                break;
            default:
                break;
        }
    }

    /**
     * 设定头部灯光
     * @param rgb  RGB
     */
    public void setHeadLed(byte[] rgb) {
        ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0x74, rgb);
    }

    /**
     * setLedClosed() 关闭所有灯光
     */
    public void turnoffLed() {
        setHeadLed(new byte[3]);
    }

    /**
     * 超声数据
     */
    public int getUtrasonic() {
        try {
            byte[] data;
            data = ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0xA3, null);
                /*readbuff::aa 55 00 0a 03 f0 a0 00 00 00 00 00 c3 9c*/
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

    /**
     * 头部触摸 H56
     * @return 0代表无触摸，1代表有触摸
     */
    public int getHeadTouch() {
        return 0;
    }

    /**
     * 设置手脚灯光 H56
     * @param type 0x00左手 0x01右手 0x02双手 0x03左脚 0x04右脚 0x05双脚
     * @param mode 0x00常亮 0x01闪烁
     */
    public void setHandsFeetLed(int type, int mode) {

    }
        /*********************************H3解释执行部分*****************************************/
    /*"第一个参数舵机ID号(1-N)； 第二个参数舵机角度（0-1023）；第三个参数舵机角度（-180~+180）
      N = 22"*/
    public void runServo(int id, int speed, int angle) {
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
        DoWriteFrame(data,data.length);
//        ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0xA5, data);
    }
    private void DoWriteFrame(byte[] pBuf, int dwLen){
        int iLength = dwLen + 3;//构建数据位
        //高位在前，低位在后
        byte[] sendBuff = new byte[iLength];
        sendBuff[0] = (byte) 0x02;
        sendBuff[1] = (byte) ((dwLen >> 8) & 0xFF);
        sendBuff[2] = (byte) (dwLen & 0xFF);
        System.arraycopy(pBuf, 0, sendBuff, 3, dwLen);
        ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0x11, (byte) 0x15, sendBuff);

    }
    //上电释放归零写一个函数。
    /*1字节：ID个数N
    2字节：舵机ID号
    3字节：舵机配置：0x01代表固定； 0x02代表释放； 0x03代表归零
    后续表示剩余N-1个舵机配置：同2/3字节*/
    private void controlServo(int id, int type) {
//        byte[] data;
//        if (id == 0) {
//            if(type == 3){
//                int type0 =  GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H ? 2 :3;
//                ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0x78, new byte[]{2, (byte) type0,0});//按照H的协议来。
//            }else {
//                byte[] dataALL = new byte[45];
//                dataALL[0] = 22;
//                for (int i = 1; i <= 22; i++) {
//                    dataALL[2 * i - 1] = (byte) i;
//                    dataALL[2 * i] = (byte) type;
//                }
//                ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H,  (byte) 0xA3, (byte) 0x65, dataALL);//按照H的协议来。
//            }
//        } else {
//            if(type == 3){
//                runServo(id,200,0);
//            }else{
//                byte[] data = new byte[3];
//                data[0] = (byte) 1;//就是一个。
//                data[1] = (byte) id;
//                data[2] = (byte) type;
//                ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H,  (byte) 0xA3,(byte) 0x65, data);//按照H的协议来。
//            }
//        }
        if(id == 0){//全部舵机
            switch (type){
                case 1:
                    ProtocolUtils.relAndFix(0, (byte) 0x18);
                    break;
                case 2:
                    ProtocolUtils.relAndFix(1, (byte) 0x18);
                    break;
                case 3:
                    ProtocolUtils.goServePosZeros();
                    break;
            }
        }else{//单个舵机
            switch (type){
                case 1:
                    ProtocolUtils.freeFix(id,1);
                    break;
                case 2:
                    ProtocolUtils.freeFix(id,0);
                    break;
                case 3:
                    ProtocolUtils.goZero(id);
                    break;
            }
        }
    }

    /*第一个参数舵机ID号(0-8)，0为全部 舵机ID【全部/1-8】上电*/
    public void startServo(int id) {
        controlServo(id, 1);
    }

    public void stopServo(int id) {
        controlServo(id, 2);
    }

    public void zeroServo(int id) {
        controlServo(id, 3);
    }

    public String getServoAngleString(int id) {
        int[] angles = getServoAngles();
        LogMgr.d("angles = " + Arrays.toString(angles));
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
            LogMgr.d("content = " + content);
        }
        return content;
    }

    private final int SERVO_COUNT_MAX = 22;

    public int[] getServoAngles() {
        int[] angles = new int[SERVO_COUNT_MAX];
        try {
            for (int i = 0; i < angles.length; i++) {
                angles[i] = (int) ((float) (ProtocolUtils.getSingleServoPos(i+1,0) - 512) / (float) 307 * 90);
                Thread.sleep(30);
            }
//            byte[] data = ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0x61, null);
//                /*readbuff::aa 55 00 0a 05 f0 a0 00 00 00 00 00 c7 65 00 00 00 00 00 00*/
//            if (data[0] == (byte) 0xAA && data[1] == (byte) 0x55 &&
//                    data[5] == (byte) 0xF0 && data[6] == (byte) 0x61) {
//                String content = "angles::";
//                for (int k = 0; k < angles.length; k++) {
//                    angles[k] = convertToAngle(ByteUtils.byte2int_2byteHL(data, 11 + 2 * k));
//                    content += angles[k] + " ";
//                }
//                LogMgr.e(content);
//                return angles;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        for (int i = 0; i < angles.length; i++) {
//            angles[i] = convertToAngle(0);
//        }
        return angles;
    }

    private int convertToAngle(int value) {
        /*if (value == 0) {
            return 0;
        }*/
        //value = 512 + angle * 1024 / 300;
        return (value - 512) * 300 / 1024;
    }

    public int findBar() {
        int dis = findBarDistance();
        if (dis > 0 && dis < 20) {
            return 1;
        }
        return 0;
    }

    public int findBarDistance() {
        //获取超声
        return getUtrasonic();
    }

    //H动作文件播放
    public final static int SKILL_PLAYER_ACTION_STOP = 0x10; //停止
    public final static int SKILL_PLAYER_ACTION_PLAY = 0x11; //开始
    public final static int SKILL_PLAYER_ACTION_PUASE = 0x12; //暂停
    public final static int SKILL_PLAYER_ACTION_RESUME = 0x13; //继续
    public final static String MOVEBIN_DIR = "Abilix" + File.separator + "MoveBin" + File.separator;
    public final static String MOVEBIN_CUSTOM_DIR = "Abilix/media/upload/move/";
    /**
     *
     * @param action 0x00 前进 0x01 后退 0x02 左侧移 0x03 右侧移 0x04 转体
     *               0x05://跳舞 0x06://踢球 0x07://并步抱拳 0x08://左冲拳 0x09://复原 0x0A://抓取 0x0B://左转 0x0C://右转 0x0D://右冲拳
     */
    public void move(int action) {
        switch (action) {
            case 0x00://前进
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_walk.bin");
                break;
            case 0x01://后退
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_backwalk.bin");
                break;
            case 0x02://左侧移
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_zuoyi.bin");
                break;
            case 0x03://右侧移
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_youyi.bin");
                break;
            case 0x04://转体
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_ztleft.bin");
                break;
            case 0x05://跳舞
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_qcxlsc.bin");
                break;
            case 0x06://踢球
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_football.bin");
                break;
            case 0x07://并步抱拳
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_bbbq.bin");
                break;
            case 0x08://左冲拳
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_zcq.bin");
                break;
            case 0x09://复原   暂时修改为初始化
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_csh.bin");
                break;
            case 0x0A://抓取
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_zhuaqu.bin");
            case 0x0B://左转
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_ztleft.bin");
            case 0x0C://右转
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_ztright.bin");
                break;
            case 0x0D://右冲拳
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_ycq.bin");
                break;
            default:
                break;
        }
    }

    @Override
    public void customMove(String name) {
        ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_CUSTOM_DIR + name);
    }

    public void stopMove() {
        ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_STOP, null);
        gaitMotion(0, 10);
    }

    public int position(int drc) {
        int value = 0;
        float[] SN = mSensor.getmO();
        // 上下是value[1].下正 上负。
        if (drc == 0) {
//            if (SN[1] >= -175 && SN[1] <= -100) {
//                value = 1;
//            }
            if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3){//H3和H5对应的陀螺仪数值不同
                if ((SN[1] >= -180 && SN[1] <= -95) && (SN[2] > -10 && SN[2] < 10)) {
                    value = 1;
                }
            }else {
                if ((SN[1] >= 30 && SN[1] <= 80) && (SN[2] > -10 && SN[2] < 10)) {
                    value = 1;
                }
            }
            // 上扬。
        } else if (drc == 1) {
//            if (SN[1] >= -80 && SN[1] <= -5) {
//                value = 1;
//            }
            if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3){
                if ((SN[1] >= -75 && SN[1] <= 0)&& (SN[2] > -10 && SN[2] < 10)) {
                    value = 1;
                }
            }else {
                if ((SN[1] >= 95 && SN[1] <= 150)&& (SN[2] > -10 && SN[2] < 10)) {
                    value = 1;
                }
            }
            // 左翻，右翻。 左正右负。
        } else if (drc == 2) {
            if (SN[2] >= -85 && SN[2] <= -5) {
                value = 1;
            }
            // 右翻。
        } else if (drc == 3) {
            if (SN[2] >= 5 && SN[2] <= 85) {
                value = 1;
            }
        }
        return value;
    }

    private static int flagId = 0x00;
    /**
     *步态运动
     * @param action 类型 0x00 停止步态算法；0x01 前走；0x02 后走；0x03 左走；0x04 右走；0x05 左前走；0x06 右前走；0x07 左后走；0x08 右后走；0x09 停止走动；0x0A 左转； 0x0B 右转；
     * @param speed 速度，取值为0，10（慢），20（快）
     */
    @Override
    public void gaitMotion(int action, int speed) {
        byte[] data = new byte[12];
        byte[] flagByte = ByteUtils.intTo4Bytes(flagId++);
        byte[] actionByte = ByteUtils.intTo4Bytes(action);
        byte[] speedByte = ByteUtils.intTo4Bytes(speed);
        System.arraycopy(flagByte, 0, data, 0, 4);
        System.arraycopy(actionByte, 0, data, 4, 4);
        System.arraycopy(speedByte, 0, data, 8, 4);
        ProtocolUtils.sendCmdToControl((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0x17, (byte) 0x18, data);
    }
}
