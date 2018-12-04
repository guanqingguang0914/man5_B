package com.abilix.explainer.helper;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.ProtocolUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jingh on 2017/8/24.
 */

public class H56ExplainerHelper extends HExplainerHelper {

    /**
     * 设置LED灯光
     *
     * @param type 表示部位，0x00代表额头灯，0x01代表左手，0x02代表右手，0x03代表左脚，0x04代表右脚, 0x05双眼 0x06双手 0x07双脚
     * @param rgb  部位为头部、双眼时有效 RGB
     * @param mode 部位不为头部、双眼时有效 0x00代表关闭，0x01代表打开
     */
    @Override
    public void setLed(int type, byte[] rgb, int mode) {
        LogMgr.d("led type:" + type + "    mode:" + mode);
        switch (type) {
            case 0x00://0x00代表额头
                setHeadLed(new LedSet(LedSet.TYPE_FOREHEAD, rgb));
                break;
            case 0x01://0x01代表左手
                setHandsLed(0x01, mode);
                break;
            case 0x02://0x02代表右手
                setHandsLed(0x02, mode);
                break;
            case 0x03://0x03代表左脚
                setFeetLed(0x01, mode);
                break;
            case 0x04://0x04代表右脚
                setFeetLed(0x02, mode);
                break;
            case 0x05://双眼
                setHeadLed(new LedSet(LedSet.TYPE_EYE_LEFT, rgb), new LedSet(LedSet.TYPE_EYE_RIGHT, rgb));
                break;
            case 0x06://双手
                setHandsLed(0x00, mode);
                break;
            case 0x07://双脚
                setFeetLed(0x00, mode);
                break;
            default:
                break;
        }
    }

    /**
     * 设定头部灯光
     *
     * @param rgb RGB
     */
    @Override
    public void setHeadLed(byte[] rgb) {
        setHeadLed(new LedSet(LedSet.TYPE_FOREHEAD, rgb));
    }

    public void setHeadLed(int type, byte[] rgb) {
        setHeadLed(new LedSet(type, rgb));
    }

    /**
     * 设置头部LED灯光
     *
     * @param ledSets 可变长参数1--5
     */
    private void setHeadLed(LedSet... ledSets) {
        byte[] data = new byte[2];
        data[0] = 0x02;
        data[1] = (byte) ledSets.length;
        for (LedSet ledSet : ledSets) {
            data = ByteUtils.byteMerger(data, ledSet.getData());
        }
        ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0xC0, data);
    }

    class LedSet {
        public static final int TYPE_FOREHEAD = 0x01;
        public static final int TYPE_EYE_LEFT = 0x02;
        public static final int TYPE_EYE_RIGHT = 0x03;
        public static final int TYPE_EAR_LEFT = 0x04;
        public static final int TYPE_EAR_RIGHT = 0x05;

        private byte type;
        private byte[] rgb;

        public LedSet(int type, byte[] rgb) {
            this.type = (byte) type;
            this.rgb = rgb;
        }

        public byte[] getData() {
            byte[] data = new byte[4];
            data[0] = type;
            System.arraycopy(rgb, 0, data, 1, 3);
            return data;
        }
    }

    /**
     * 设定眼部灯光
     *
     * @param onOff      0x00关 0x01开
     * @param rgb        3字节 RGB
     * @param brightness 亮度 0~255
     * @param mode       模式 0x00常亮 0x01颜色正常变换 0x02 颜色随机变换 0x03 呼吸灯
     */
    public void setEyesLed(int onOff, byte[] rgb, int brightness, int mode) {
        byte[] data = new byte[7];
        data[0] = 0x04;
        data[1] = (byte) (onOff & 0xFF);
        System.arraycopy(rgb, 0, data, 2, 3);
        data[5] = (byte) (brightness & 0xFF);
        data[6] = (byte) (mode & 0xFF);
        ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0x6A, data);
    }

    /**
     * 设置手部LED灯光
     *
     * @param type 0x00 双手；0x01 左手；0x02 右手
     * @param mode 0x00 关； 0x01 开； 0x02 闪烁
     */
    public void setHandsLed(int type, int mode) {
        switch (mode) {
            case 0x00://0x00 关
            case 0x01://0x01 开
                handsLedType[TYPE_HANDS] = 0x00;
                switch (type) {
                    case TYPE_HANDS:
                        handsLedType[TYPE_HAND_LEFT] = 0x00;
                        handsLedType[TYPE_HAND_RIGHT] = 0x00;
                        break;
                    case TYPE_HAND_LEFT:
                        handsLedType[TYPE_HAND_LEFT] = 0x00;
                        break;
                    case TYPE_HAND_RIGHT:
                        handsLedType[TYPE_HAND_RIGHT] = 0x00;
                        break;
                }
                setHandsLedCmd(type, mode);
                break;
            case 0x02://0x02 闪烁
                if (timerHandsLed == null) {
                    timerHandsLed = new Timer("HandsLedTimer");
                    timerHandsLed.scheduleAtFixedRate(timerTaskHandsLed, 0, 500);
                }
                switch (type) {
                    case TYPE_HANDS:
                        handsLedType[TYPE_HANDS] = 0x01;
                        handsLedType[TYPE_HAND_LEFT] = 0x01;
                        handsLedType[TYPE_HAND_RIGHT] = 0x01;
                        break;
                    case TYPE_HAND_LEFT:
                        handsLedType[TYPE_HAND_LEFT] = 0x01;
                        break;
                    case TYPE_HAND_RIGHT:
                        handsLedType[TYPE_HAND_RIGHT] = 0x01;
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置手部LED灯光
     *
     * @param type 0x00 双手；0x01 左手；0x02 右手
     * @param mode 0x00 关； 0x01 开； 0x02 闪烁
     */
    private void setHandsLedCmd(int type, int mode) {
        switch (type){
            case 0:
                try{
                    ProtocolUtils.handLight(mode,19);
                    Thread.sleep(20);
                    ProtocolUtils.handLight(mode,20);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case 1:
                ProtocolUtils.handLight(mode,19);
                break;
            case 2:
                ProtocolUtils.handLight(mode,20);
                break;
        }
//        byte[] data = new byte[3];
//        data[0] = 0x02;
//        data[1] = (byte) type;
//        data[2] = (byte) mode;
//        ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0xC1, data);
    }

    public static final int TYPE_HANDS = 0x00;
    public static final int TYPE_HAND_LEFT = 0x01;
    public static final int TYPE_HAND_RIGHT = 0x02;

    public static final int TYPE_FEET = 0x00;
    public static final int TYPE_FOOT_LEFT = 0x01;
    public static final int TYPE_FOOT_RIGHT = 0x02;

    private int[] handsLedType = new int[3];
    private int[] feetLedType = new int[3];
    private Timer timerHandsLed = null;
    private TimerTask timerTaskHandsLed = new TimerTask() {
        private int ledBlinkMode = 0;
        @Override
        public void run() {

            ledBlinkMode = (ledBlinkMode == 0) ? 1 : 0;
            //手部
            if (handsLedType[TYPE_HANDS] == 0x01
                    || (handsLedType[TYPE_HAND_LEFT] == 0x01 && handsLedType[TYPE_HAND_RIGHT] == 0x01)) {
                //0x00 双手
                setHandsLedCmd(TYPE_HANDS, ledBlinkMode);
            } else if (handsLedType[TYPE_HAND_LEFT] == 0x01) {
                //0x01 左手
                setHandsLedCmd(TYPE_HAND_LEFT, ledBlinkMode);
            } else if (handsLedType[TYPE_HAND_RIGHT] == 0x01) {
                //0x02 右手
                setHandsLedCmd(TYPE_HAND_RIGHT, ledBlinkMode);
            }

            //脚部
            int brightness = ledBlinkMode > 0 ? 1023 : 0;
            if (feetLedType[TYPE_FEET] == 0x01
                    || (feetLedType[TYPE_FOOT_LEFT] == 0x01 && feetLedType[TYPE_FOOT_RIGHT] == 0x01)) {
                //0x00 双脚
                setFeedLedBrightness(TYPE_FEET, brightness);
            } else if (feetLedType[TYPE_FOOT_LEFT] == 0x01) {
                //0x01 左脚
                setFeedLedBrightness(TYPE_FOOT_LEFT, brightness);
            } else if (feetLedType[TYPE_FOOT_RIGHT] == 0x01) {
                //0x02 右脚
                setFeedLedBrightness(TYPE_FOOT_RIGHT, brightness);
            }
        }
    };

    /**
     * 设置脚部LED灯光
     *
     * @param type 0x00 双脚；0x01 左脚；0x02 右脚
     * @param mode 0x00 关； 0x01 开； 0x02 闪烁
     */
    public void setFeetLed(int type, int mode) {

        switch (mode) {
            case 0x00://关
                feetLedType[TYPE_FEET] = 0x00;
                switch (type) {
                    case TYPE_FEET:
                        feetLedType[TYPE_FOOT_LEFT] = 0x00;
                        feetLedType[TYPE_FOOT_RIGHT] = 0x00;
                        break;
                    case TYPE_FOOT_LEFT:
                        feetLedType[TYPE_FOOT_LEFT] = 0x00;
                        break;
                    case TYPE_FOOT_RIGHT:
                        feetLedType[TYPE_FOOT_RIGHT] = 0x00;
                        break;
                }
                setFeedLedBrightness(type, 0);
                break;
            case 0x01://开
                feetLedType[TYPE_FEET] = 0x00;
                switch (type) {
                    case TYPE_FEET:
                        feetLedType[TYPE_FOOT_LEFT] = 0x00;
                        feetLedType[TYPE_FOOT_RIGHT] = 0x00;
                        break;
                    case TYPE_FOOT_LEFT:
                        feetLedType[TYPE_FOOT_LEFT] = 0x00;
                        break;
                    case TYPE_FOOT_RIGHT:
                        feetLedType[TYPE_FOOT_RIGHT] = 0x00;
                        break;
                }
                setFeedLedBrightness(type, 1023);
                break;
            case 0x02://闪烁
                if (timerHandsLed == null) {
                    timerHandsLed = new Timer("HandsLedTimer");
                    timerHandsLed.scheduleAtFixedRate(timerTaskHandsLed, 0, 500);
                }
                switch (type) {
                    case TYPE_FEET:
                        feetLedType[TYPE_FEET] = 0x01;
                        feetLedType[TYPE_FOOT_LEFT] = 0x01;
                        feetLedType[TYPE_FOOT_RIGHT] = 0x01;
                        break;
                    case TYPE_FOOT_LEFT:
                        feetLedType[TYPE_FOOT_LEFT] = 0x01;
                        break;
                    case TYPE_FOOT_RIGHT:
                        feetLedType[TYPE_FOOT_RIGHT] = 0x01;
                        break;
                }
                break;
        }
    }

    /**
     * 设定脚部灯光亮度
     *
     * @param type       0:双脚；1：左脚；2：右脚
     * @param brightness 亮度 0~1023
     */
    private void setFeedLedBrightness(int type, int brightness) {
        switch (type){
            case 0:
                try{
                    ProtocolUtils.footLight(23,brightness);
                    Thread.sleep(20);
                    ProtocolUtils.footLight(24,brightness);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case 1:
                ProtocolUtils.footLight(23,brightness);
                break;
            case 2:
                ProtocolUtils.footLight(24,brightness);
                break;
        }
//        byte[] data = new byte[4];
//        data[0] = 0x02;
//        data[1] = (byte) type;
//        data[2] = (byte) (brightness >> 8 & 0xFF);
//        data[3] = (byte) (brightness & 0xFF);
//        ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0xC2, data);
    }

    /**
     * 设置手脚灯光
     *
     * @param type 0x00左手 0x01右手 0x02双手 0x03左脚 0x04右脚 0x05双脚
     * @param mode 0x00常亮 0x01闪烁
     */
    @Override
    public void setHandsFeetLed(int type, int mode) {
        mode = mode + 1;
        switch (type) {
            case 0x00://0x00左手
                setHandsLed(0x01, mode);
                break;
            case 0x01://0x01右手
                setHandsLed(0x02, mode);
                break;
            case 0x02://0x02双手
                setHandsLed(0x00, mode);
                break;
            case 0x03://0x03左脚
                setFeetLed(0x01, mode);
                break;
            case 0x04://0x04右脚
                setFeetLed(0x02, mode);
                break;
            case 0x05://0x05双脚
                setFeetLed(0x00, mode);
                break;
        }
    }

    @Override
    public void turnoffLed() {
        //关闭头部所有，包括额头、耳朵、眼睛
        byte[] rgb = new byte[3];
        setHeadLed(new LedSet(LedSet.TYPE_FOREHEAD, rgb),
                new LedSet(LedSet.TYPE_EYE_LEFT, rgb),
                new LedSet(LedSet.TYPE_EYE_RIGHT, rgb),
                new LedSet(LedSet.TYPE_EAR_LEFT, rgb),
                new LedSet(LedSet.TYPE_EAR_RIGHT, rgb));
        //关闭手部脚部灯光
        if (timerHandsLed != null) {
            timerHandsLed.cancel();
        }
        setHandsLed(0, 0);
        setFeetLed(0, 0);
    }

    @Override
    public int getUtrasonic() {
        return super.getUtrasonic();
    }

    @Override
    public void runServo(int id, int speed, int angle) {
        super.runServo(id, speed, angle);
    }

    @Override
    public void startServo(int id) {
        super.startServo(id);
    }

    @Override
    public void stopServo(int id) {
        super.stopServo(id);
    }

    @Override
    public void zeroServo(int id) {
        super.zeroServo(id);
    }

    @Override
    public int[] getServoAngles() {
        return super.getServoAngles();
    }

    @Override
    public String getServoAngleString(int id) {
        return super.getServoAngleString(id);
    }

    @Override
    public int findBar() {
        return super.findBar();
    }

    @Override
    public int findBarDistance() {
        return super.findBarDistance();
    }

    /**
     * @param action 0x00 前进 0x01 后退 0x02 向左 0x03 向右 0x04 转体
     *               0x06://跳舞 0x07://踢球 0x08://并步抱拳 0x09://左冲拳 0x0A://复原
     */
    @Override
    public void move(int action) {
        LogMgr.d("move() action = " + action);
        switch (action) {
            case 0x00://前进
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_walk.bin");
                gaitMotion(1, 10);
                break;
            case 0x01://后退
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_backwalk.bin");
                gaitMotion(2, 10);
                break;
            case 0x02://左侧移
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_zuoyi.bin");
                gaitMotion(3, 10);
                break;
            case 0x03://右侧移
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_youyi.bin");
                gaitMotion(4, 10);
                break;
            case 0x04://转体
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_ztleft.bin");
                gaitMotion(0x0A, 10);
                break;
            case 0x05://跳舞
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H5_tiaowu.bin");
                break;
            case 0x06://踢球
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_football.bin");
                break;
            case 0x07://并步抱拳
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_bbbq.bin");
                break;
            case 0x08://左冲拳
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_zcq.bin");
                break;
            case 0x09://复原   暂时修改为初始化
                ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H5_csh.bin");
                break;
            case 0x0A://抓取
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_zhuaqu.bin");
            case 0x0B://左转
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_ztleft.bin");
            case 0x0C://右转
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_ztright.bin");
                break;
            case 0x0D://右冲拳
                //ProtocolUtils.controlSkillPlayer(SKILL_PLAYER_ACTION_PLAY, MOVEBIN_DIR + "H_ycq.bin");
                break;
            default:
                break;
        }
    }

    @Override
    public void stopMove() {
        super.stopMove();
    }

    @Override
    public int getHeadTouch() {
        try {
            byte[] data = ProtocolUtils.sendProtocol((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0xA3, (byte) 0x6C, null);
            if (data[0] == (byte) 0xAA && data[1] == (byte) 0x55 &&
                    data[5] == (byte) 0xA3 && data[6] == (byte) 0x69) {
                return data[12] & 0xFF;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void gaitMotion(int action, int speed) {
        byte[] data = new byte[6];
        data[0] = 0x02;
        data[1] = (byte) (action & 0xFF);
        data[5] = (byte) (speed & 0xFF);
        ProtocolUtils.sendCmdToControl((byte) GlobalConfig.ROBOT_TYPE_H, (byte) 0x20, (byte) 0x08, data);
    }
}
