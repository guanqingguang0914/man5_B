package com.abilix.explainer.helper;

import java.util.Timer;
import java.util.TimerTask;

import com.abilix.explainer.ControlInfo;
import com.abilix.explainer.MotorBytesBuilder;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.ProtocolUtils;


public class C1ExplainerHelper extends AExplainHelper {

    private int openloopdc0 = 100;
    private int openloopdc1 = 100;

    private int closeloopdc0 = 100;
    private int closeloopdc1 = 100;

    public int ReadAIValue(int port) {
        //C1的接受端口0~3
        if (port < 0 || port > 3) {
            LogMgr.e("端口 错误");
            return 0;
        }
        for (int i = 0; i < 5; i++) {
            byte[] readbuff = ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x06, null);
            LogMgr.e("readbuff: " + ByteUtils.bytesToString(readbuff, readbuff.length));
            for (int j = 0; j < 20; j++) {
                if ((readbuff[j] & 0xff) == 0xf2
                        && (readbuff[j + 1] & 0xff) == 0x22) {
                    return ByteUtils.byte2int_2byteHL(readbuff, j + 4 + port * 2);
                }
            }
        }
        return 0;
    }

    private boolean ledflag = false;

    public void setButtonLight(int num1, int num2) {

        final byte[] array = new byte[1];
//		array[0] = ledbuff[parms1];
//		sendProtocol((byte)0x09, (byte)0xA5, (byte)0x04, array, toStm32);
        // 点亮
        if (num2 == 0) {
            ledflag = false;
            try {
                Thread.sleep(80);
            } catch (InterruptedException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
            if (num1 == 1) {
                // 红色
                array[0] = (byte) 0x01;
            } else if (num1 == 2) {
                // 蓝色
                array[0] = (byte) 0x02;
            } else if (num1 == 3) {
                // 绿色
                array[0] = (byte) 0x03;
            } else if (num1 == 0) {
                array[0] = (byte) 0x00;
            }
            ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x04, array);
            // 闪烁。
        } else if (num2 == 1) {
            // 用于关闭上次状态
            ledflag = false;
            try {
                Thread.sleep(60);
            } catch (InterruptedException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            if (num1 == 1) {
                // 红色
                array[0] = (byte) 0x01;
            } else if (num1 == 2) {
                // 蓝色
                array[0] = (byte) 0x02;
            } else if (num1 == 3) {
                // 绿色
                array[0] = (byte) 0x03;
            } else if (num1 == 0) {
                array[0] = (byte) 0x00;
            }

            ledflag = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (ledflag) {
                        ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x04, array);
                        try {
                            // 这里将500 分成10份来延时。
                            for (int i = 0; i < 10; i++) {
                                if (ledflag) {
                                    Thread.sleep(50);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (ledflag) {
                                array[0] = 0x00;
                                ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x04, array);
                            }
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        try {
                            // 这里将500 分成10份来延时。
                            for (int i = 0; i < 10; i++) {
                                if (ledflag) {
                                    Thread.sleep(50);
                                }
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }).start();
        }
    }

    public void reSetStm32() {
        ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0x11, (byte) 0x0B, null);
    }

    public static final int COLOR_RED = 0;
    public static final int COLOR_YELLOW = 1;
    public static final int COLOR_GREEN = 2;
    public static final int COLOR_BLUE = 3;
    public static final int COLOR_WHITE = 4;
    public static final int COLOR_GRAY = 5;
    public static final int COLOR_BLACK = 6;

    public int getColorResponse(int port) {
        for (int i = 0; i < 5; i++) {
            byte[] readbuff = ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x07, null);
            LogMgr.e("head " + ByteUtils.bytesToString(readbuff, readbuff.length));
            for (int j = 0; j < 20; j++) {
                if ((readbuff[j] & 0xff) == 0xf2
                        && (readbuff[j + 1] & 0xff) == 0x23) {
                    // return Utils.byte2int_2byteHL(readbuff, j+4+type*2);
                    int h = (readbuff[j + 6] & 0xff);
                    int s = (readbuff[j + 7] & 0xff);
                    int l = (readbuff[j + 8] & 0xff);
                    int c = (readbuff[j + 9] & 0xff);
                    if (h == 8 || l > 255) {
                        // NSLog(@"错误");
                        return -1;
                    } else if (h != 8) {
                        if (h > 12 && h <= 45) {
                            if (c < 90) {
                                if (s == 240) {
                                    // [UIColor yellowColor];
                                    return COLOR_YELLOW;
                                } else {
                                    // [UIColor whiteColor];
                                    return COLOR_WHITE;
                                }

                            } else {
                                // =[UIColor yellowColor];
                                return COLOR_YELLOW;
                            }
                        } else {
                            if ((h == 0 && s == 0 && l == 240) || (l > 150)) {
                                // [UIColor whiteColor];
                                return COLOR_WHITE;
                            } else if (((h >= 0 && h <= 10) || (h >= 200 && h < 255))
                                    && l < 150) {
                                // [UIColor redColor];
                                return COLOR_RED;
                            } else if (h >= 120 && h < 160 && l < 150) {
                                if (l < 90) {
                                    if (h < 160 && h >= 145) {
                                        // = [UIColor blueColor];
                                        return COLOR_BLUE;
                                    } else if (h > 60 && h < 144) {
                                        // = [UIColor greenColor];
                                        return COLOR_GREEN;
                                    }
                                } else if (l >= 90) {
                                    if (h < 131 && h >= 120) {
                                        // = [UIColor greenColor];
                                        return COLOR_GREEN;
                                    } else if (h > 130) {
                                        // = [UIColor blueColor];
                                        return COLOR_BLUE;
                                    }
                                }
                            } else if ((h >= 60 && h <= 119) && l < 110) {
                                // [UIColor greenColor];
                                return COLOR_GREEN;
                            } else if (h >= 160 && h <= 180 && l < 80) {
                                // [UIColor blueColor];
                                return COLOR_BLUE;
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    public int getcolor(int[] color) {
        int[] res = {0, 0, 0, 0, 0, 0, 0};// 7个0.

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

    //开环电机不分大小电机，只有端口和速度。
    public void motorChoice(int port, int sudu) {
        //端口只有0和1，速度限制在-100到100之间。
        if (port < 0 || port > 1 || sudu > 100 || sudu < -100) {
            LogMgr.e("参数错误");
            return;
        }
        byte[] closeMtor = new byte[4];
        closeMtor[0] = (byte) 100;
        closeMtor[1] = (byte) 100;
        if (port == 0) {
            closeMtor[0] = (byte) (sudu + 100);
        } else if (port == 1) {
            closeMtor[1] = (byte) (sudu + 100);
        }
        ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x02, closeMtor);
    }

    //闭环电机控制。						速度时间。。          端口                    圈数时间                   速度                        大小电机
    public void closeloop_control(int type, int port, int num1, int num2, int bigorsmalltype) {

        if (type == 3) {
            // 这里面只需要处理一个时间一个数值还有一个端口。
            closeloop_time(num1, num2, port, bigorsmalltype);
        } else {
            byte[] data = new byte[11];
            num2 = num2 + 100;
            // 圈数和类型的参数反一下。
            if (type == 1) {
                type = 2;
            } else if (type == 2) {
                type = 1;
            }
            // 这里确定哪个电机口。
            if (port == 0) {
                data[0] = (0x01 << 1) & 0xff;// A端口
                closeloopdc0 = num2;

            } else if (port == 1) {
                data[0] = (0x01 << 0) & 0xff;// B端口
                closeloopdc1 = num2;
            }
            /************* 这里默认大电机 ******************/
            data[1] = (byte) bigorsmalltype;// 大电机
            data[2] = (byte) type;
            data[3] = (byte) closeloopdc0;
            data[4] = (byte) ((num1 >> 8) & 0xFF);// 高位在前低位在后。
            data[5] = (byte) (num1 & 0xFF);
            /************** B口 7~11字节 **************/
            data[6] = (byte) bigorsmalltype;// 大电机
            data[7] = (byte) type;
            data[8] = (byte) closeloopdc1;
            data[9] = (byte) ((num1 >> 8) & 0xFF);// 高位在前低位在后。
            data[10] = (byte) (num1 & 0xFF);
            ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x03, data);
        }

    }

    private Timer timerA = null;
    private Timer timerB = null;

    private TimerTask timerTaskA = null;
    private TimerTask timerTaskB = null;

    public void cleantimer() {
        // 清理A口。
        if (timerTaskA != null) {
            timerTaskA.cancel();
            timerTaskA = null;
        }
        if (timerA != null) {
            timerA.cancel();
            timerA = null;
        }
        // 清理B口。
        if (timerTaskB != null) {
            timerTaskB.cancel();
            timerTaskB = null;
        }
        if (timerB != null) {
            timerB.cancel();
            timerB = null;
        }
    }

    private void closeloop_time(int time, int sudu, final int port,
                                final int bigorsmalltype) {

        sudu = sudu + 100;
        byte[] data = new byte[11];
        if (port == 0) {
            data[0] = (0x01 << 1) & 0xff;// A端口
            closeloopdc0 = sudu;

        } else if (port == 1) {
            data[0] = (0x01 << 0) & 0xff;// B端口
            closeloopdc1 = sudu;
        }
        /************* 这里默认大电机 ******************/
        data[1] = (byte) bigorsmalltype;// 大电机
        data[2] = 0;
        data[3] = (byte) closeloopdc0;
        data[4] = 0;// 高位在前低位在后。
        data[5] = 0;
        /************** B口 7~11字节 **************/
        data[6] = (byte) bigorsmalltype;// 大电机
        data[7] = 0;
        data[8] = (byte) closeloopdc1;
        data[9] = 0;// 高位在前低位在后。
        data[10] = 0;
        ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x03, data);
        /****************** 定时器的处理 ******************************/
        if (port == 0) {
            if (timerA != null) {
                timerA.cancel();
                timerA = null;
            }
            if (timerTaskA != null) {
                timerTaskA.cancel();
                timerTaskA = null;
            }
            timerA = new Timer();
            timerTaskA = new TimerTask() {

                @Override
                public void run() {
                    closeloop_control(0, port, 0, 0, bigorsmalltype);
                }
            };
            timerA.schedule(timerTaskA, time * 1000);
        } else if (port == 1) {

            if (timerB != null) {
                timerB.cancel();
                timerB = null;
            }
            if (timerTaskB != null) {
                timerTaskB.cancel();
                timerTaskB = null;
            }
            timerB = new Timer();
            timerTaskB = new TimerTask() {

                @Override
                public void run() {
                    closeloop_control(0, port, 0, 0, bigorsmalltype);
                }
            };
            timerB.schedule(timerTaskB, time * 1000);
        }
    }

    Timer allMotor_timerA;
    Timer allMotor_timerB;

    TimerTask allMotor_timerTaskA;
    TimerTask allMotor_timerTaskB;

    // 这段代码保留
    /*public void setAllMotor(byte[] praram1_bytes, byte[] praram2_bytes, byte[] praram3_bytes, byte[] praram4_bytes, final int motorType, float[] mValue) {
        byte[] colose_loop_motor_protocol_bytes = new byte[11];//按照协议这里11个字节 不是21个字节。
        byte[] open_loop_motor_protocol_bytes = new byte[5];
        byte[] motorState = getMotorState(praram1_bytes);
        LogMgr.d("motor state::" + ByteUtils.bytesToString(motorState, motorState.length));
        // 被设置成开环时，取消定时器
        if (((int) (motorState[0] & 0x08) == 8)) {
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
        if (((int) (motorState[0] & 0x04) == 4)) {
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


        System.arraycopy(motorState, 1, colose_loop_motor_protocol_bytes, 0, 1);
        System.arraycopy(motorState, 0, open_loop_motor_protocol_bytes, 0, 1);
        // 闭环电机
        byte[] close_loop_motor_a_bytes = new MotorBytesBuilder().setMotorPort(0).setMotorBytes(new byte[5]).setMotorType(motorType).setValueType(praram1_bytes)
                .setCloseLoopSpeed(praram1_bytes, praram4_bytes, praram3_bytes, mValue).setTypeValue(praram1_bytes, praram2_bytes, praram3_bytes, mValue).build();
        byte[] close_loop_motor_b_bytes = new MotorBytesBuilder().setMotorPort(1).setMotorBytes(new byte[5]).setMotorType(motorType).setValueType(praram1_bytes)
                .setCloseLoopSpeed(praram1_bytes, praram4_bytes, praram3_bytes, mValue).setTypeValue(praram1_bytes, praram2_bytes, praram3_bytes, mValue).build();
        byte[] close_loop_motor_c_bytes = new MotorBytesBuilder().setMotorPort(2).setMotorBytes(new byte[5]).setMotorType(motorType).setValueType(praram1_bytes)
                .setCloseLoopSpeed(praram1_bytes, praram4_bytes, praram3_bytes, mValue).setTypeValue(praram1_bytes, praram2_bytes, praram3_bytes, mValue).build();
        byte[] close_loop_motor_d_bytes = new MotorBytesBuilder().setMotorPort(3).setMotorBytes(new byte[5]).setMotorType(motorType).setValueType(praram1_bytes)
                .setCloseLoopSpeed(praram1_bytes, praram4_bytes, praram3_bytes, mValue).setTypeValue(praram1_bytes, praram2_bytes, praram3_bytes, mValue).build();
        // 开环电机
        byte[] open_loop_motor_a_bytes = new MotorBytesBuilder().setMotorPort(0).setMotorBytes(new byte[1]).setOpenLoopSpeed(praram1_bytes, praram4_bytes, praram3_bytes, mValue).build();
        byte[] open_loop_motor_b_bytes = new MotorBytesBuilder().setMotorPort(1).setMotorBytes(new byte[1]).setOpenLoopSpeed(praram1_bytes, praram4_bytes, praram3_bytes, mValue).build();
        byte[] open_loop_motor_c_bytes = new MotorBytesBuilder().setMotorPort(2).setMotorBytes(new byte[1]).setOpenLoopSpeed(praram1_bytes, praram4_bytes, praram3_bytes, mValue).build();
        byte[] open_loop_motor_d_bytes = new MotorBytesBuilder().setMotorPort(3).setMotorBytes(new byte[1]).setOpenLoopSpeed(praram1_bytes, praram4_bytes, praram3_bytes, mValue).build();
        LogMgr.d("-------------" + (int) praram1_bytes[0] + "++++++++++++++" + (int) ((praram1_bytes[0] >> 6) & 0x01));

        // 拼装闭环电机协议
        System.arraycopy(close_loop_motor_a_bytes, 0, colose_loop_motor_protocol_bytes, 1, close_loop_motor_a_bytes.length);
        System.arraycopy(close_loop_motor_b_bytes, 0, colose_loop_motor_protocol_bytes, 6, close_loop_motor_a_bytes.length);
        System.arraycopy(close_loop_motor_c_bytes, 0, colose_loop_motor_protocol_bytes, 11, close_loop_motor_a_bytes.length);
        System.arraycopy(close_loop_motor_d_bytes, 0, colose_loop_motor_protocol_bytes, 16, close_loop_motor_a_bytes.length);
        LogMgr.d("set close loop motor data bytes::" + ByteUtils.bytesToString(colose_loop_motor_protocol_bytes, colose_loop_motor_protocol_bytes.length));

        // 拼装开环电机协议
        System.arraycopy(open_loop_motor_a_bytes, 0, open_loop_motor_protocol_bytes, 1, open_loop_motor_a_bytes.length);
        System.arraycopy(open_loop_motor_b_bytes, 0, open_loop_motor_protocol_bytes, 2, open_loop_motor_a_bytes.length);
        System.arraycopy(open_loop_motor_c_bytes, 0, open_loop_motor_protocol_bytes, 3, open_loop_motor_a_bytes.length);
        System.arraycopy(open_loop_motor_d_bytes, 0, open_loop_motor_protocol_bytes, 4, open_loop_motor_a_bytes.length);
        LogMgr.d("set open loop motor data bytes::" + ByteUtils.bytesToString(open_loop_motor_protocol_bytes, open_loop_motor_protocol_bytes.length));
        try {
            LogMgr.d("发送开环电机命令");
            ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x13, open_loop_motor_protocol_bytes);
            LogMgr.d("发送闭环电机命令");
            ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x03, colose_loop_motor_protocol_bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((((praram1_bytes[0] >> 2) & 0x0f) == 3) && (int) ((praram1_bytes[0] >> 6) & 0x01) == 0) {
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
                    closeloop_control(0, 0, 0, 0, motorType);
                }
            };
            int typeValue = ByteUtils.byte2int_2byteHL(praram2_bytes, 0);
            LogMgr.d("port" + 0 + "时间值是否是引用变量::" + ((((praram1_bytes[0] << 6) >> 6) & 0x02) > 0 ? true : false));
            if ((((praram1_bytes[0] << 6) >> 6) & 0x02) > 0) {
                int valuePosition = ByteUtils.byte2int_2byteHL(praram2_bytes, 0);
                float value = getMvalue(mValue, valuePosition);
                LogMgr.d("引用变量位置::" + valuePosition + "    " + "引用变量的值:" + value);
                typeValue = (int) value;
            }
            LogMgr.d(" A motor time::" + typeValue);
            allMotor_timerA.schedule(allMotor_timerTaskA, typeValue * 1000);
        }
        if ((((praram1_bytes[1] >> 2) & 0x0f) == 3) && (int) ((praram1_bytes[1] >> 6) & 0x01) == 0) {
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
                    closeloop_control(0, 1, 0, 0, motorType * 1000);
                }
            };
            int typeValue = ByteUtils.byte2int_2byteHL(praram2_bytes, 2);
            LogMgr.d("port" + 1 + "时间值是否是引用变量::" + ((((praram1_bytes[1] << 6) >> 6) & 0x02) > 0 ? true : false));
            if ((((praram1_bytes[1] << 6) >> 6) & 0x02) > 0) {
                int valuePosition = ByteUtils.byte2int_2byteHL(praram2_bytes, 2);
                float value = getMvalue(mValue, valuePosition);
                LogMgr.d("引用变量位置::" + valuePosition + "    " + "引用变量的值:" + value);
                typeValue = (int) value;
            }
            LogMgr.d("B motor time::" + typeValue);
            allMotor_timerB.schedule(allMotor_timerTaskB, typeValue * 1000);
        }


    }*/

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

        //这里停止所有电机。
        byte[] closeMtor = new byte[4];
        closeMtor[0] = (byte) 100;
        closeMtor[1] = (byte) 100;
        ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x02, closeMtor);
    }

    //	第一个参数: (顺序：由高位到低位)
//  第1字节：A端口是否设置(0-不设置，1-设置，占1位)-A端口电机类型(0-闭环，1-开环，占1位)-数值类型(0-速度，1-角度，2-圈数，3-时间，占4位)-A数值是否引用变量(占1位)-A速度是否引用变量(占1位)  
//  第2字节：B端口是否设置(0-不设置，1-设置，占1位)-B端口电机类型(0-闭环，1-开环，占1位)-数值类型(0-速度，1-角度，2-圈数，3-时间，占4位)-B数值是否引用变量(占1位)-B速度是否引用变量(占1位)
//  第3字节：C端口是否设置(0-不设置，1-设置，占1位)-C端口电机类型(0-闭环，1-开环，占1位)-数值类型(0-速度，1-角度，2-圈数，3-时间，占4位)-C数值是否引用变量(占1位)-C速度是否引用变量(占1位)
//  第4字节：D端口是否设置(0-不设置，1-设置，占1位)-D端口电机类型(0-闭环，1-开环，占1位)-数值类型(0-速度，1-角度，2-圈数，3-时间，占4位)-D数值是
    public byte[] getMotorState(byte[] param1Chars) {
        byte[] motorState = new byte[2];
        int openLoopMotorState = 0;
        int closeLoopMotorState = 0;
        if ((int) ((param1Chars[0] >> 6) & 0x01) == 1) {
            openLoopMotorState += (int) ((param1Chars[0] >> 4) & 0x08);
            LogMgr.d("开环电机A端口设置状态:" + (int) ((param1Chars[0] >> 4) & 0x08));
            closeLoopMotorState += 0;
        } else {
            openLoopMotorState += 0;
            closeLoopMotorState += (int) ((param1Chars[0] >> 4) & 0x08);
            LogMgr.d("闭环电机A端口设置状态:" + (int) ((param1Chars[0] >> 4) & 0x08));
        }
        if ((int) ((param1Chars[1] >> 6) & 0x01) == 1) {
            openLoopMotorState += (int) ((param1Chars[1] >> 5) & 0x04);
            LogMgr.d("开环电机B端口设置状态:" + (int) ((param1Chars[1] >> 5) & 0x04));
            closeLoopMotorState += 0;
        } else {
            openLoopMotorState += 0;
            closeLoopMotorState += (int) ((param1Chars[1] >> 5) & 0x04);
            LogMgr.d("闭环电机B端口设置状态:" + (int) ((param1Chars[1] >> 5) & 0x04));
        }
        motorState[0] = (byte) openLoopMotorState;
        motorState[1] = (byte) closeLoopMotorState;
        return motorState;
    }

    //闭环是0，开环是1，                                                                                                                                 shuj
    public void setNewAllMotor(byte[] isSet, byte[] portType, byte[] numtype,
                               int[] TandAngle, int[] sudu, int bigOrSmall) {//1代表大电机，0代表小电机。

        for (int i = 0; i < 2; i++) {

            if (isSet[i] == 1) {// 只有设置才往下发。
                //经确认，chart只有闭环。
                closeloop_control(numtype[i], i, TandAngle[i], sudu[i], bigOrSmall);
//				if (portType[i] == 1) {//开环
//
//					motorChoice(i, sudu[i]);//开环电机协议有问题，不支持选择性设置。
//				} else {
//
//					closeloop_control(numtype[i], i, TandAngle[i], sudu[i], bigOrSmall);
//				}
            }
        }

    }


    /*********************************c1解释执行部分*****************************************/
    // 0x01 调用电机。
    public void motorSetCX(int port, int drection, int sudu) { // -------------------电机OK。

        if (Math.abs(drection) > 1 || Math.abs(port) > 2) {
            LogMgr.e("参数解析错误");
            return;
        }
        // 速度还是要做一个越界处理。
        if (sudu > 100) {
            sudu = 100;
        } else if (sudu < -100) {
            sudu = -100;
        }

        if (drection == 0) {
            sudu = sudu + 100;
        } else if (drection == 1) {
            sudu = 100 - sudu;
        }
        byte[] senddata = new byte[11];
        if (port == 0) {// 这是A口转
            senddata[0] = 2;
        } else if (port == 1) {
            senddata[0] = 1;
        }
        senddata[1] = 0;
        senddata[2] = 0;
        senddata[3] = (byte) sudu;
        senddata[4] = 0;
        senddata[5] = 0;
        // B 电机。
        senddata[6] = 0;
        senddata[7] = 0;
        senddata[8] = (byte) sudu;
        senddata[9] = 0;
        senddata[10] = 0;
        ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x03, senddata);
    }

    public void setLedCx(int color) {
        if (color >= 0 && color < 3) {
            // ledSet(parms1);
            byte[] ledbuff = {1, 3, 2};
            byte[] array = new byte[1];
            array[0] = ledbuff[(int) color];
            ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x04, array);
        }
    }

    public void C1_CloseLedCx() {
        byte[] array = new byte[1];//0就是关闭。  ----------------------------------------------OK
        ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x04, array);
    }

    // 是否遇到障碍物
    public int haveObjectCX(int port) {

        if (port < 0 || port > 4) {
            LogMgr.e("端口错误");
            return 0;
        }
        int value = 0;
        if (port != 0) {
            //port = port - 1;
            value = ReadAIValueCX(port);// 这里调试一下端口。
        } else {
            int searchPort = ReadAITypeCX(0);// 超声是0，按钮是1，灰度是2.
            value = ReadAIValueCX(searchPort);
        }

        if (value >= 200 || value <= 0) {
            value = 0;
        } else {
            value = 1;
        }
        return value;
    }

    private int ReadAITypeCX(int type) {
        byte[] readbuff = new byte[200];
        for (int i = 0; i < 5; i++) {

            try {
                readbuff = ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x05, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            LogMgr.e("AIType " + ByteUtils.bytesToString(readbuff, readbuff.length));
            int min = 0, max = 0, value = 0;
            if (type == 0) { // 超声 -- 测试OK
                min = 857;
                max = 1057;
            } else if (type == 1) { // 按钮 -- 测试OK
                min = 10;
                max = 264;
            } else if (type == 2) { // 灰度

                min = 536;
                max = 736;
            }
            for (int j = 0; j < 20; j++) {

                if ((readbuff[j] & 0xff) == 0xf2
                        && (readbuff[j + 1] & 0xff) == 0x21) {
                    // return ByteUtils.byte2int_2byteHL(readbuff, j+4+type*2);
                    for (int n = 1; n < 4; n++) {// 这样就返回1.2.3.
                        value = ByteUtils.byte2int_2byteHL(readbuff, j + 4 + n * 2);
                        LogMgr.e("AIType value is: " + value);
                        if (value > min && value < max) { // 查找到Type所在的AI
                            LogMgr.e("AIType duankou: " + n);
                            return n;
                        }
                    }
                }
            }

        }
        return 0;
    }

    private int ReadAIValueCX(int type) {

        byte[] readbuff = new byte[200];
        for (int i = 0; i < 5; i++) {

            try {
                readbuff = ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x06, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            LogMgr.e("AIvalue " + ByteUtils.bytesToString(readbuff, readbuff.length));
            for (int j = 0; j < 20; j++) {

                if ((readbuff[j] & 0xff) == 0xf2
                        && (readbuff[j + 1] & 0xff) == 0x22) {
                    return ByteUtils.byte2int_2byteHL(readbuff, j + 4 + type * 2);
                }
            }

        }
        return 0;
    }

    public int distanceCX(int port) {

        if (port == -1 || port > 4) {
            LogMgr.e("端口错误");
            return 0;
        }
        int value = 0;
        if (port != 0) {
            //port = port - 1;
            value = ReadAIValueCX(port);// 这里调试一下端口。
        } else {
            int searchPort = ReadAITypeCX(0);// 超声是0，按钮是1，灰度是2.
            value = ReadAIValueCX(searchPort);
        }
        if (value > 1500) {
            value = 1500;
        }
        LogMgr.i("distance: " + value);
        return value;
    }

    public int touchCX(int port) {

        if (port == -1 || port > 4) {
            LogMgr.e("端口错误");
            return 0;
        }
        int value = 0;
        if (port != 0) {
//			port = port - 1;
            value = ReadAIValueCX(port);// 这里调试一下端口。
        } else {
            int searchPort = ReadAITypeCX(1);// 超声是0，按钮是1，灰度是2.
            value = ReadAIValueCX(searchPort);
        }
        LogMgr.e("value: " + value);
        //C1 碰撞传感器返回值可能大于4095
        value = value > 4095 ? 4095 : value;
        if (value > 1000 && value < 4096) {
            value = 1;
        } else {
            value = 0;
        }
        return value;
    }

    public int readColorCX(int color) {// 颜色与端口没有关系。

        if (color == -1 || color > 4) {
            LogMgr.e("数组越界");
            return 0;
        }
        // 第二个参数为颜色值：0~4（"红","黄","绿","蓝","白"）
        int[] colorArray = new int[10];
        for (int m = 0; m < 10; m++) {
            colorArray[m] = ReadColorValueCX();// 这里存储6个值。
        }
        int sensorColor = getcolor(colorArray);
        int[] colorValue = {1, 6, 2, 3, 5};// 这里与上面的颜色顺序一致。
        int sendvalue = 0;
        if (colorValue[color] == sensorColor) {
            sendvalue = 1;
        } else {
            sendvalue = 0;
        }
        return sendvalue;
    }

    private int ReadColorValueCX() {
        byte[] readbuff = new byte[200];
        for (int i = 0; i < 5; i++) {

            try {
                readbuff = ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x07, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            LogMgr.e("head " + ByteUtils.bytesToString(readbuff, readbuff.length));
            for (int j = 0; j < 20; j++) {

                if ((readbuff[j] & 0xff) == 0xf2
                        && (readbuff[j + 1] & 0xff) == 0x23) {
                    // return ByteUtils.byte2int_2byteHL(readbuff, j+4+type*2);
                    int h = (readbuff[j + 6] & 0xff);
                    int s = (readbuff[j + 7] & 0xff);
                    int l = (readbuff[j + 8] & 0xff);
                    int c = (readbuff[j + 9] & 0xff);
                    if (h == 8 || l > 255) {
                        // NSLog(@"错误");
                        return -1;
                    } else if (h != 8) {
                        if (h > 12 && h <= 45) {
                            if (c < 90) {
                                if (s == 240) {
                                    // [UIColor yellowColor];
                                    return 6;
                                } else {
                                    // [UIColor whiteColor];
                                    return 5;
                                }

                            } else {
                                // =[UIColor yellowColor];
                                return 6;
                            }
                        } else {
                            if ((h == 0 && s == 0 && l == 240) || (l > 150)) {
                                // [UIColor whiteColor];
                                return 5;
                            } else if (((h >= 0 && h <= 10) || (h >= 200 && h < 255))
                                    && l < 150) {
                                // [UIColor redColor];
                                return 1;
                            } else if (h >= 120 && h < 160 && l < 150) {
                                if (l < 90) {
                                    if (h < 160 && h >= 145) {
                                        // = [UIColor blueColor];
                                        return 3;
                                    } else if (h > 60 && h < 144) {
                                        // = [UIColor greenColor];
                                        return 2;
                                    }
                                } else if (l >= 90) {
                                    if (h < 131 && h >= 120) {
                                        // = [UIColor greenColor];
                                        return 2;
                                    } else if (h > 130) {
                                        // = [UIColor blueColor];
                                        return 3;
                                    }
                                }
                            } else if ((h >= 60 && h <= 119) && l < 110) {
                                // [UIColor greenColor];
                                return 2;
                            } else if (h >= 160 && h <= 180 && l < 80) {
                                // [UIColor blueColor];
                                return 3;
                            }
                        }
                    }

                }

            }
        }
        return -1;
    }

    public int getGraySensorCX(int port) {

        if (port == -1 || port > 4) {
            LogMgr.e("端口越界");
            return 0;
        }
        int grayValue = 0;
        if (port != 0) {
//			port = port - 1;
            grayValue = ReadAIValueCX(port);
        } else {
            int searchPort = ReadAITypeCX(2);// 超声是0，按钮是1，灰度是2.
            grayValue = ReadAIValueCX(searchPort);
        }
        return grayValue;

    }

    // 这里需要记录各个电机口的速度。
    private int dc0speed = 100;
    private int dc1speed = 100;
    private int dc2speed = 100;
    private int dc3speed = 100;


    public void C1runmove(int port, int value) {// c的一个端口一个值。
        if (value > 100) {
            value = 100;
        } else if (value < -100) {
            value = -100;
        }
        byte speed = (byte) (value + 100);
        byte[] cmd = new byte[2];
        // 这里是电机0~3.
        if (port == 0) {
            // motor[8] = speed;
            dc0speed = speed;
        } else if (port == 1) {
            // motor[9] = speed;
            dc1speed = speed;
        }
        cmd[0] = (byte) dc0speed;
        cmd[1] = (byte) dc1speed;
        ProtocolUtils.sendProtocol((byte) ControlInfo.getChild_robot_type(), (byte) 0xA5, (byte) 0x02, cmd);
    }

    public void C1_runstop() {
        /*byte[] motor = new byte[2];
        motor[0] = (byte) 100;
        motor[1] = (byte) 100;
        ProtocolBuilder.sendProtocol((byte) ControlInfo.getChild_robot_type(), ProtocolBuilder.C1_CMD_MOTOR_, motor);*/
        byte[] closeMtor = new byte[4];
        closeMtor[0] = (byte) 100;
        closeMtor[1] = (byte) 100;
        ProtocolUtils.sendProtocol((byte) 0x09, (byte) 0xA5, (byte) 0x02, closeMtor);
    }

    public void C1_setled(int color) {

        byte[] cmd = new byte[1];
        // 灭 红 绿 蓝
        if (color == 0) {

            cmd[0] = (byte) 0x00;
        } else if (color == 1) {
            // rgb
            cmd[0] = (byte) 0x01;
        } else if (color == 2) {

            cmd[0] = (byte) 0x03;
        } else if (color == 3) {

            cmd[0] = (byte) 0x02;
        }
        ProtocolUtils.sendProtocol((byte) ControlInfo.getChild_robot_type(), (byte) 0xA5, (byte) 0x02, cmd);
    }

}
