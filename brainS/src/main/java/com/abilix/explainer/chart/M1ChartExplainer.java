package com.abilix.explainer.chart;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

import com.abilix.explainer.AExplainer;
import com.abilix.explainer.ExplainTracker;
import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.FunNode;
import com.abilix.brain.R;
import com.abilix.explainer.helper.M1ExplainerHelper;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.PlayerUtils;

import android.os.Handler;
import android.util.Log;

public class M1ChartExplainer extends AExplainer {
    protected M1ExplainerHelper explainerHelper;

    public M1ChartExplainer(Handler handler) {
        super(handler);
        explainerHelper = new M1ExplainerHelper();
    }

    private boolean isDisplay;

    @Override
    public void doExplain(String filePath) {
        super.doExplain(filePath);
        LogMgr.d("start explain vjcM! file");
        // 文件总长度
        int len = filebuf.length;
        subFunNodeJava = new FunNode();
        int funPos = 0;
        byte runBuffer[] = new byte[40];
        int index = 0;
        int reValue = 0;
        float param1 = 0;
        float param2 = 0;
        float param3 = 0;
        float param4 = 0;
        float param5 = 0;
        gloableCount = -1;

        // 添加两个时钟变量。赋初始值。
        long init_time = 0;
        long end_time = 0;
        init_time = end_time = System.currentTimeMillis();
        long displayTimeInit = System.currentTimeMillis();
        long displayTimeCurrent = System.currentTimeMillis();

        funPos = start;// 这里start是0
        do {
            if (doexplain == false) { // 文件解析结束后退出
                LogMgr.d("explain finish and exit");
                return;
            }
//			explainerHelper.readFromFlash(filebuf, funPos, runBuffer, 40); // 获取40字节数据
//			if (runBuffer[0] != 0x55 & runBuffer[1] != 0xaa) {
//				LogMgr.e("explain file is not correct");
//				return;
//			}
            explainerHelper.readFromFlash(filebuf, funPos, runBuffer, 40); // 获取40字节数据
            // 校验40个字节
            if (!explainerHelper.Check(runBuffer,
                    explainerHelper.getU16(runBuffer, 34))) {
                return;
            }
            index = explainerHelper.getU16(runBuffer, 8); // 解析函数名对应的index
            funPos = explainerHelper.getU16(runBuffer, 34) * 40; // 解析跳转到哪一行
            LogMgr.d(String.format(Locale.US, "====>index:%d funPos:%d runBuffer:%s", index, funPos, ByteUtils.bytesToString(runBuffer, runBuffer.length)));
            if (funPos == 0 && index == 0) {
                break;
            }
            switch (index) {
                /**
                 * 公用index  3,21,22,23,24,25,26
                 *
                 */
                //延时
                case 3:
                    LogMgr.e("3");
                    isSleep = true;
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param1 = param1 * 1000f;
                    String str = String.valueOf((int) param1);
                    int i = (int) (Long.parseLong(str) / 100);
                    int n = 0;
                    while (isSleep & n < i) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        n++;
                    }
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                    break;
                // 计算时间
                case 13:
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    end_time = System.currentTimeMillis();

                    Double b = ((double) (end_time - init_time) / 1000);
                    BigDecimal bd = new BigDecimal(b);
                    bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);//四舍五入。
                    b = bd.doubleValue();
                    mValue[reValue] = Float.parseFloat(b.toString());//double 转float
                    break;
                // 复位时间
                case 14:
                    init_time = System.currentTimeMillis();
                    break;
                // 运算
                case 21:
                    Log.e("test", " control ------------------>运算");
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);

                    reValue = explainerHelper.getU16(runBuffer, 30);
                    Log.e("test", " control param1:" + param1 + " param2:" + param2
                            + " param3:" + param3 + " funPos:" + funPos
                            + " reValue:" + reValue);
                    mValue[reValue] = explainerHelper.my_Calc(param1, param2,
                            (int) param3);
                    break;
                //循环
                case 22:
                    Log.e("test", "control ------------------>循环");
                    if (gloableCount == -1) {
                        gloableCount = explainerHelper.getU16(runBuffer, 4) - 1;
                    }
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    funPos = (param1 > 0.5) ? (int) param2 : (int) param3;
                    funPos = funPos * 40;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    break;
                // 调用子程序
                case 24:
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    if (!addFunNodeJava(funPos)) {
                        return;
                    }
                    LogMgr.e(String.format(Locale.US, "====>case %d: mValue[0]-->tempValue[%d] length:%d", index, subFunNodeJava.valStart, mValue.length));
                    System.arraycopy(mValue, 0, tempValue, subFunNodeJava.valStart, mValue.length);
                    funPos = (int) param1 * 40;
                    break;
                // 跳出子程序
                case 25:
                    LogMgr.e(String.format(Locale.US, "====>case %d: tempValue[%d]-->mValue[%d] length:%d", index, subFunNodeJava.valStart + gloableCount, gloableCount, mValue.length - gloableCount));
                    System.arraycopy(tempValue, subFunNodeJava.valStart + gloableCount, mValue, gloableCount, mValue.length - gloableCount);
                    int curFunNodePos = subFunNodeJava.pos;
                    if (!delFunNodeJava()) {
                        return;
                    }
                    funPos = curFunNodePos;
                    break;
                // getRandom
                case 28:
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    // LogMgr.e("param1::" + param1 + "  " + "param2::" + param2);
                    mValue[reValue] = (int) explainerHelper.getRandom(
                            (int) param1, (int) param2);
                    Log.e("test", "control reValue:" + reValue
                            + " random response::" + mValue[reValue]);
                    // }
                    break;
                // 设置电机
                case 29:
                    LogMgr.e("29");
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    // Log.e("test", "setWheelMoto" + "param1:"
                    // + param1 + " param2:" + param2
                    // + " param3:" + param3);
                    LogMgr.e("param1 =" + param1 + ";param2 = " + param2 + ";param3 = " + param3);
                    explainerHelper.setWheelMoto((int) param1, (int) param2,
                            (int) param3);
                    break;
                case 30:// 设置眼睛颜色
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    param4 = explainerHelper.getParam(runBuffer, mValue, 25);
                    LogMgr.e("control setEyeColor-->" + "param1:" + param1
                            + " param2:" + param2 + " param3:" + param3
                            + " param4:" + param4);
                    explainerHelper.setEyeColor_new((int) param1, (int) param2,
                            (int) param3, (int) param4);
                    break;
                //设置颜色,这里的设置颜色代表的是设置底部或轮子的颜色
                case 31:
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    param4 = explainerHelper.getParam(runBuffer, mValue, 25);
                    Log.e("test", "control setColor-->" + "param1:" + param1
                            + " param2:" + param2 + " param3:" + param3
                            + " param4:" + param4);
                    explainerHelper.setColor((int) param1, (int) param2,
                            (int) param3, (int) param4);
                    break;
//			// 设置亮度	
                case 32:
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    Log.e("test", "control setLuminance-->" + "param1:" + param1
                            + " param2:" + param2 + " param3:" + param3
                            + " param4:" + param4);
                    explainerHelper.setLuminance((int) param1, (int) param2);
                    break;
                // 设置波形
                case 33:
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    Log.e("test", "control setWave-->" + "param1:" + param1
                            + " param2:" + param2 + " param3:" + param3
                            + " param4:" + param4);
                    explainerHelper.setWave((int) param1, (int) param2);
                    break;
                // 获取下视
//			case 34:
//				Log.e("test", "control 下视：--->getlook_down");
//				// param1 = explainerHelper.getParam(runBuffer, mValue, 10);
//				// Log.e("test", "getlook_down-->" + "param1:" + param1);
//				reValue = explainerHelper.getU16(runBuffer, 30);
//				byte[] datal = explainerHelper.getlook_down();
//				if (datal != null) {
//					int rd = datal[9];
//					if (rd == 2) {
//						rd = 1;
//					} else if (rd == 1) {
//						rd = 2;
//					}
//					mValue[reValue] = rd;
//					Log.e("test", "control reValue:" + reValue + " rd:" + rd);
//				}
//				break;
                //TODO 获取超声:需要替換獲取超声对应的数据
//			case 35:
//				Log.e("test", "control 超声：--->getUtrasonic");
//				param1 = explainerHelper.getParam(runBuffer, mValue, 10);
//				reValue = explainerHelper.getU16(runBuffer, 30);
//				byte[] datau = explainerHelper.getUtrasonic();
//				if (datau != null) {
//					int ru1 = 0;
//					// 前
//					if (param1 == 0) {
//						ru1 = datau[4];
//						if (ru1 > 0) {
//							ru1 <<= 8;
//						}
//						ru1 += (datau[5] & 255);
//						mValue[reValue] = ru1;
//					}// 后
//					else if (param1 == 1) {
//						ru1 = datau[6];
//						if (ru1 > 0) {
//							ru1 <<= 8;
//						}
//						ru1 += (datau[7] & 255);
//						mValue[reValue] = ru1;
//					}
//
//					Log.e("test", "control reValue:" + reValue + " param1:"
//							+ param1 + " ru1: " + ru1);
//				}
//				break;
                // 获取红外  M1的红外不能用，4/22
//			case 36:
//				Log.e("test", "control 红外：--->getIfrared");
//				reValue = explainerHelper.getU16(runBuffer, 30);
//				byte[] datai = explainerHelper.getIfrared();
//				if (datai != null) {
//					int rr = datai[6];
//					if (rr > 0) {
//						rr <<= 8;
//					}
//					rr += (datai[7] & 255);
//					mValue[reValue] = rr;
//					Log.e("test", "control reValue:" + reValue + " rr:" + rr);
//				}
//				break;
                // 获取地面灰度:这里对应了7个灰度传感器，需改动；还有对应的协议
                case 37:
                    LogMgr.e("control 地面灰度：--->getground_gray");
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    LogMgr.e("control 地面灰度：--->getground_gray2  reValue = " + reValue + ";param1 = " + param1);
//				if (param1 > 0) {
//					ProtocolUtils.mGround_Grayscale_OpenByte[7] = 1;
//				} else {
//					ProtocolUtils.mGround_Grayscale_OpenByte[7] = 0;
//				}

                    byte[] datag = explainerHelper.getground_gray(param1);
                    LogMgr.e("param1 = " + param1 + "; datag = " + ByteUtils.bytesToString(datag, datag.length));
                    if (datag != null /*&& datag[0] == ProtocolUtils.DATA_HEAD_*/) {
                        int ug = 0;
                        if (param1 == 1) {
                            ug = (byte) datag[11];
//						if (ug > 0) {
//							ug <<= 8;
//						}
//						ug += datag[8] & 255;
                            mValue[reValue] = ug;
                        } else if (param1 == 2) {
                            ug = (byte) datag[12];
//						if (ug > 0) {
//							ug <<= 8;
//						}
//						ug += datag[10] & 255;
                            mValue[reValue] = ug;
                        } else if (param1 == 3) {
                            ug = (byte) datag[13];
//						if (ug > 0) {
//							ug <<= 8;
//						}
//						ug += datag[12] & 255;
                            mValue[reValue] = ug;
                        } else if (param1 == 4) {
                            ug = (byte) datag[14];
//						if (ug > 0) {
//							ug <<= 8;
//						}
//						ug += datag[14] & 255;
                            mValue[reValue] = ug;
                        } else if (param1 == 5) {
                            ug = (byte) datag[15];
//						if (ug > 0) {
//							ug <<= 8;
//						}
//						ug += datag[16] & 255;
                            mValue[reValue] = ug;
                        }
                        LogMgr.e("test", "control reValue:" + reValue + " param1:"
                                + param1 + " ug:" + ug);
                    }
                    break;
                // 获取碰撞
//			case 38:
//				Log.e("test", "control 碰撞：--->getcollision");
//				byte[] datac = explainerHelper.getcollision();
//				reValue = explainerHelper.getU16(runBuffer, 30);
//				if (datac != null) {
//					int rc = datac[10];
//					mValue[reValue] = rc;
//					Log.e("test", "control reValue:" + reValue + " rc:" + rc);
//				}
//				break;
                // 设置吸尘
//			case 39:
//				Log.e("test", "control 吸尘：--->SetcollectionMotor");
//				param1 = explainerHelper.getParam(runBuffer, mValue, 10);
//				explainerHelper.SetcollectionMotor((int) param1);
//				break;
                // 设置上下脖子
                case 40:
                    Log.e("test", "control 上下脖子：--->SetNeckUPMotor");
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    Log.e("test", "control SetNeckUPMotor-->" + "param1:" + param1);
                    explainerHelper.SetNeckUPMotor((int) param1);
                    break;
                // 设置左右脖子
                case 41:
                    Log.e("test", "control 左右脖子：--->SetNeckLRMotor");
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    Log.e("test", "control SetNeckLRMotor-->" + "param1:" + param1);
                    explainerHelper.SetNeckLRMotor((int) param1);
                    break;
                // 播放音乐
                case 51:
                    Log.e("test", "control 播放音乐：--->playeretMusic");
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    Log.e("test", "control playeretMusic-->" + "param1:" + param1
                            + " param2:" + param2);
                    explainerHelper.playeretMusic((int) param1, (int) param2);
                    break;
                case 52:// 麦克风。
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_RECORD, (int) param1);
                    //waitExplain(param1);
                    pauseExplain();
                    break;
                case 53:// 拍照。
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_CAMERA, 1);
                    if (isDisplay) {
                        waitExplain(0.1f);
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                        isDisplay = false;
                    }
                    pauseExplain();
                    break;
                // 陀螺仪
                case 54:
                    Log.e("test", "control 陀螺仪：--->getGyro");
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    float jieguo = explainerHelper.getGyro((int) param1,
                            (int) param2);
                    mValue[reValue] = jieguo;
                    break;
                // 指南针
                case 55:
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    float zhinan = explainerHelper.getCompass();
                    mValue[reValue] = zhinan;
                    break;
                // setDisplay
                case 70:
                    isDisplay = true;
                    String content = "";
                    // Log.e("test",
                    // "开始显示屏幕:"
                    // + Utils.bytesToString(runBuffer,
                    // runBuffer.length));
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);

                    int data_lines = (int) param1;
                    Log.e("test", "control 行号:" + data_lines);
                    for (int k = 0; k < data_lines; k++) {
                        funPos = explainerHelper.getU16(runBuffer, 34) * 40;
                        explainerHelper.readFromFlash(filebuf, funPos, runBuffer,
                                40);
                        // Log.e("test",
                        // "显示屏幕:"
                        // + Utils.bytesToString(runBuffer,
                        // runBuffer.length));
                        byte[] data_bytes = new byte[20];
                        System.arraycopy(runBuffer, 11, data_bytes, 0, 20);
                        // isvariable = 0 变量 ， isvariable = 1引用变量
                        int isvariable = runBuffer[10];

                        byte[] str_byte = null;
                        Log.e("test",
                                "control data_bytes-->显示屏幕:"
                                        + ByteUtils.bytesToString(data_bytes,
                                        data_bytes.length) + ":"
                                        + isvariable);
                        if (isvariable == 0) {
                            for (int l = 0; l < data_bytes.length; l++) {
                                if (data_bytes[l] == 0) {
                                    str_byte = new byte[l];
                                    System.arraycopy(data_bytes, 0, str_byte, 0, l);
                                    break;
                                }
                                if (l == 19) {
                                    str_byte = data_bytes;
                                }
                            }
                            try {
                                content += new String(str_byte, "UTF-8") + "\n";
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (isvariable == 1) {

                            float paramV = explainerHelper.getParam(runBuffer,
                                    mValue, 10);
                            if (Float.isNaN(paramV) || Float.isInfinite(paramV)) {

                                content += ExplainerApplication.instance.getString(R.string.guoda) + "\n";

                            } else {
                                DecimalFormat df = new DecimalFormat("0.000");
                                content += df.format(paramV) + "\n";
                            }
//						DecimalFormat df = new DecimalFormat("0.000");
//						content += df.format(paramV)+"\n";
//						Log.e("test", "paramV-->显示屏幕:" + paramV + ":" +
//						 content);

                        }
                        if (k == 7) {
                            funPos = explainerHelper.getU16(runBuffer, 34) * 40;
                            explainerHelper.readFromFlash(filebuf, funPos,
                                    runBuffer, 40);
                        }
                    }
                    Log.e("test", "control 内容:" + content);
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_DISPLAY, ExplainTracker.ARG_DISPLAY_TEXT, content);
                    break;
                // 播放指定音乐
                case 71:
                    Log.e("播放指定音乐",
                            ByteUtils.bytesToString(runBuffer, runBuffer.length));
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    Log.e("test", "control param1::" + param1 + "  " + "param2::"
                            + param2);
                    explainerHelper.playSound((int) param1, (int) param2);
                    break;
                //头部触摸
                case 72:
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    int istouch = explainerHelper.gettouchhead();
                    LogMgr.e("触摸：----" + istouch + "position: " + reValue);
                    mValue[reValue] = istouch;
                    break;
                // 单个电机：设置电机转速；
                //暂时没有该协议
                case 100:
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    param2 = explainerHelper.getParam(runBuffer, mValue, 15);
                    param3 = explainerHelper.getParam(runBuffer, mValue, 20);
                    param4 = explainerHelper.getParam(runBuffer, mValue, 25);
                    // 第一个参数：类型(0-闭环 1-开环 2-位移)
                    // 第二个参数：模式(0-左电机 1-右电机)
                    // 第三个参数：速度
                    // 第四个参数：位移 int type,int mode,int sudu,int distance
                    explainerHelper.setNewOneWheelMoto((int) param1, (int) param2,
                            (int) param3, (int) param4);
                    break;
                // 所有电机
                case 101:
                    // 第一个参数：类型(0-闭环 1-开环 2-位移)
                    // 第二个参数：(顺序：由高位到低位)
                    // 第1字节：左轮子是否设置(0-不设置，1-设置，占1位)-左轮子速度是否引用变量(0-代表常量，1-引用变量，占1位)-空6位
                    // 第2字节：左轮子速度(类型为0时，速度范围为-60~60；类型为1时，速度范围为-100~100；类型为2时，速度范围为-60~60)
                    // 第3字节：右轮子是否设置(0-不设置，1-设置，占1位)-右轮子速度是否引用变量(0-代表常量，1-引用变量，占1位)-空6位
                    // 第4字节：右轮子速度(类型为0时，速度范围为-60~60；类型为1时，速度范围为-100~100；类型为2时，速度范围为-60~60)
                    // 第三个参数：左轮子位移(0-4095)单位cm
                    // 第四个参数：右轮子位移(0-4095)单位cm

                    int type = (int) explainerHelper.getParam(runBuffer, mValue,
                            10);// 类型。开闭位移。
                    LogMgr.e("leixing    :  " + type);
                    int leftIsSet = (runBuffer[16] >> 7) & 0x01;
                    int leftIsvariable = (runBuffer[16] >> 6) & 0x01;
                    int leftSudu = runBuffer[17] & 0xff;

                    int RightIsSet = (runBuffer[18] >> 7) & 0x01;
                    int RightIsvariable = (runBuffer[18] >> 6) & 0x01;
                    int ringhtSudu = runBuffer[19] & 0xff;
                    // LogMgr.e("leftIsvariable: "+leftIsvariable+"RightIsvariable:"+RightIsvariable+"index: "+leftSudu);
                    int leftDis = (int) explainerHelper.getParam(runBuffer,
                            mValue, 20);
                    int rightDis = (int) explainerHelper.getParam(runBuffer,
                            mValue, 25);
                    // 这里还有两个问题，引用变量的获取。
                    if (leftIsvariable == 1) {
                        // leftSudu = (int)
                        // explainerHelper.getParam(runBuffer,mValue, leftSudu);
                        leftSudu = (int) explainerHelper.getMvalue(mValue,
                                leftSudu);
                    }
                    if (RightIsvariable == 1) {
                        ringhtSudu = (int) explainerHelper.getMvalue(mValue,
                                ringhtSudu);
                    }

                    // LogMgr.e("leftsudu: "+leftSudu +"rightsudu: "+ringhtSudu);

                    explainerHelper.setNewAllWheelMoto(type, leftIsSet, leftSudu,
                            RightIsSet, ringhtSudu, leftDis, rightDis);
                    break;
                case 112:// 校准指南针。
                    LogMgr.e("compass check ");
                    sendExplainMessage(ExplainTracker.MSG_EXPLAIN_COMPASS);
                    //explainerHelper.waitCompassCheck();
                    pauseExplain();
                    break;
                case 113://停止播放。
                    explainerHelper.stopPlaySound();
                    break;
                case 121: //void setSoundNew(char *) 扬声器
                    /*char*类型，表示音频文件名（N<20字节）
                    附：STM32端内置音频命名规则——中文拼音_语言+下标
                    例：中文“打招呼”下的“你好”——dazhaohu_c0
                    英文“打招呼”下的“欢迎”——dazhaohu_e3
                    公共“交通”下的“赛车” —— jiaotong_p1
                    公共“动物”下的“狗” —— dongwu_p3*/
                    String soundName = explainerHelper.getString(runBuffer, 11);
                    final int isNeedWaiting = runBuffer[29] & 0xFF;
                    LogMgr.d("soundName:" + soundName + " isNeedWaiting: " + isNeedWaiting);
                    explainerHelper.playSound(soundName, new PlayerUtils.OnCompletionListener() {
                        @Override
                        public void onCompletion(int state) {
                            LogMgr.d("onCompletion() state: " + state);
                            if (isNeedWaiting == 1) {
                                resumeExplain();
                            }
                        }
                    });
                    if (isNeedWaiting == 1) {
                        pauseExplain();
                    }
                    break;

                case 122: //int GetlookdownM1(int)	下视传感器
                    /*"4字节，参数：int类型，表示下视传感器类型：
                    0x00代表后左，0x01代表后中,0x02代表后右,
                    0x03代表前左,0x04代表前右；
                    0x05代表全部悬空；0x06代表全部不悬空；
                    返回值：0 否   1是"*/
                    param1 = explainerHelper.getParam(runBuffer, mValue, 10);
                    reValue = explainerHelper.getU16(runBuffer, 30);
                    byte[] data2 = explainerHelper.getlook_down();
                    if (data2 != null) {
                        int rd = data2[9];
                        LogMgr.e("control", "reValue:" + reValue + " rd:" + rd + " param1:" + param1);
                        int lookDown = 0;
                        switch ((int) param1) {

                            case 0x00://是否左悬空。
                            case 0x03:
                                if (rd == 2) {
                                    lookDown = 1;
                                } else {
                                    lookDown = 0;
                                }
                                break;
                            case 0x02://是否右悬空
                            case 0x04:
                                if (rd == 1) {
                                    lookDown = 1;
                                } else {
                                    lookDown = 0;
                                }
                                break;
                            case 0x05://是否左右悬空
                            case 0x06:
                            case 0x01:
                                if (rd == 0) {//全悬挂是0.
                                    lookDown = 1;
                                } else {
                                    lookDown = 0;
                                }
                                break;
                        }
                        mValue[reValue] = lookDown;//这里要做测试。
                    }
                    break;

                case 23:
                    // explainerHelper.stopWheelMoto(0);
                    Log.e("test", " control vjc执行结束");
                    if (isDisplay) {
                        sendExplainMessage(ExplainTracker.MSG_EXPLAIN_NO_DISPLAY);
                        isDisplay = false;
                    }
                    return;
            }

            LogMgr.e("isStop:" + isStop);
        } while (!isStop);
    }

    @Override
    public void stopExplain() {
        super.stopExplain();
        LogMgr.d("stop explain");
        explainerHelper.setCompassCheck(false);//退出指南针校准等待。
        try {
            explainerHelper.stopPlaySound();
            // explainerHelper.stopWheelMoto(0);
            // TimeUnit.MILLISECONDS.sleep(8);
            // explainerHelper.stopCollectionMotor();
            // TimeUnit.MILLISECONDS.sleep(8);
            // explainerHelper.turnOutLights();
            // TimeUnit.MILLISECONDS.sleep(8);
            // explainerHelper.turnOutEyeLights();
            // TimeUnit.MILLISECONDS.sleep(8);
            // explainerHelper.resetNeck();
            // TimeUnit.MILLISECONDS.sleep(8);
            // explainerHelper.turnOutWave();
            // TimeUnit.MILLISECONDS.sleep(8);
            // explainerHelper.turnHuidu();
            explainerHelper.startSleepStop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void pauseExplain() {
        super.pauseExplain();

    }

    @Override
    public void resumeExplain() {
        super.resumeExplain();
    }

    @Override
    public void waitExplain(float time) {
        super.waitExplain(time);
    }

}
