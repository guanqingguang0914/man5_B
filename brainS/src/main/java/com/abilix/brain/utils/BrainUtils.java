package com.abilix.brain.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.WindowManager;
import android.widget.Toast;

import com.abilix.brain.Application;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.zip.CRC32;

/**
 * Brain 工具类，
 *
 *
 * @author luox
 */
public class BrainUtils {

    public static final String ABILIX = Environment.getExternalStorageDirectory().getPath() + File.separator + "Abilix" + File.separator;
    public static final String ABILIX_PROJECT_PROGRAM = "AbilixProjectProgram";//APP_TYPE_KNOW_ROBOT
    public static final String ABILIX_KNOW_ROBOT = "AbilixKnowRobot";
    public static final String ABILIX_PROJECT_PROGRAM_FOR_S = "media" + File.separator + "default" + File.separator + "movementOfProgramS";
    public static final String ABILIX_CHART = "AbilixChart";
    public static final String ABILIX_APP_STORE = "app_store";
    public static final String ABILIX_SCRATCH = "AbilixScratch";
    public static final String ABILIX_SKILLPLAYER = "Abilix_Skillplayer";
    public static final String ABILIX_SKILL_CREATOR = "Abilix_Skillcreator";
    public static final String ABILIX_MEDIA = "media";
    public static final String RECORD_NAME = "record.3gp";
    public static final String RECORD_ = "record";
    public static final String RECORD_PATH = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + RECORD_
            + File.separator + RECORD_NAME;
    public static final String DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + "Download";
    public static final String ABILIX_SKILLPLAYER_PATH = ABILIX
            + ABILIX_SKILLPLAYER;
    /**
     * 家长模式
     */
    public static final String ROBOTINFO = ABILIX + "RobotInfo"
            + File.separator + "parent.txt";
    /**
     * 自动休息模式
     */
    public static final String AUTORESTINFO = ABILIX + "RobotInfo"
            + File.separator + "autoreset.txt";
    /**
     * 超声波数据
     */
    public final static int ULTRASONIC = 0x101;

    /**
     * 地面灰度数据
     */
    public final static int GROUND_GRAYSCALE = 0x102;

    /**
     * 下视
     */
    public final static int DOWN_WATCH = 0x103;

    /**
     * 前端碰撞
     */
    public final static int FRONT_CRASH = 0x104;

    /**
     * 后端红外
     */
    public final static int REAR_END_INFRARED = 0x105;

    /**
     * 其他
     */
    public final static int ELSE = 106;

    /**
     * 音乐
     */
    public final static int MUSIC = 0x107;

    /**
     * 显示窗
     */
    public final static int WINDOW = 0x109;

    /**
     * 结束
     */
    public final static int OVER = 0x110;

    /**
     * 视频
     */
    public final static int VIDEO = 0x108;

    /**
     * 指南针 与 陀螺仪
     */
    public final static int POSITION = 0x111;

    /**
     * 录音
     */
    public final static int RECORD = 0x112;

    /**
     * 播放录音
     */
    public final static int PLAY_RECORD = 0x113;

    /**
     * 界面
     */
    public final static int RECORD_WINDOW = 0x114;

    /**
     * 显示窗 C 清空
     */
    public final static int WINDOW_CLEAR = 0x116;

    /**
     * c open 打开 vjc
     */
    public final static int C_OPEN_CJV = 0x119;

    /**
     * c open 打开 scratch
     */
    public final static int C_OPEN_SCRATCH = 0x125;

    /**
     * c stop  vjc scratch
     */
    public final static int C_STOP_CJV = 0x121;

    /**
     * 显示图片
     */
    public final static int SCRATCH_VJC_IMAGEVIEW = 0x122;

    /**
     * 关闭图片
     */
    public final static int SCRATCH_VJC_IMAGEVIEW_STOP = 0x123;

    //显示编程图片
    public final static int PICTURE_DISPLAY= 0x124;

    /**
     * 显示KU201项目编程电机图片
     */
    public final static int KU_OPEN_MOTOR_VIEW = 0x12a;

    /**
     * 关闭KU201项目编程电机图片
     */
    public final static int KU_CLOSE_MOTOR_VIEW = 0x12b;

    /**
     * C END
     */
    public final static int END_C = 0x115;

    /**
     * AbilixChart
     */
    public final static int ABILIX_SCRATCH_CODE = 0x114;

    /**
     * Abilix_Project_Program
     */
    public final static int ABILIX_PROJECT_PROGRAM_CODE = 0x115;

    /**
     * 成功
     */
    public static final int SUCCEED = 200;

    /**
     * 匹配失败
     */
    public final static int ERROR = -1;

    /**
     * 合并数组
     *
     * @param a
     * @param b
     * @return 合并结果 长度固定为20
     */
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[20];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /**
     * 匹配
     *
     * @param a
     */
    public static int matchingArray(byte[] a) {
        /*
		 * if (a[0] == DATA_HEAD) { if (a[1] == A && a[2] == C && a[3] == K) {
		 * return ELSE; }// 音乐播放 else if (a[1] == M && a[2] == U && a[3] == S &&
		 * a[4] == T && a[5] == C) { return MUSIC; } // 超声波 else if (a[1] == A
		 * && a[2] == I && a[3] == DR) { return ULTRASONIC; }// 地面灰度 else if
		 * (a[1] == G && a[2] == DR && a[3] == A && a[4] == Y && a[5] == DR &&
		 * a[6] == D) { return GROUND_GRAYSCALE; }// 下视 else if (a[1] == L &&
		 * a[2] == O && a[3] == O && a[4] == K && a[5] == D && a[6] == O && a[7]
		 * == W && a[8] == N) { return DOWN_WATCH; }// 碰撞 else if (a[1] == C &&
		 * a[2] == O && a[3] == L && a[4] == L && a[5] == I && a[6] == S && a[7]
		 * == I && a[8] == O && a[9] == N) { return FRONT_CRASH; }// 后端红外 else
		 * if (a[1] == I && a[2] == N && a[3] == F && a[4] == DR && a[5] == A) {
		 * return FRONT_CRASH; } }
		 */

        if (a[0] == BrainData.DATA_HEAD) {
            // 超声波
            if (a[1] == BrainData.A && a[2] == BrainData.I
                    && a[3] == BrainData.DR) {
                return ULTRASONIC;
            }// 地面灰度
            else if (a[1] == BrainData.G && a[2] == BrainData.DR
                    && a[3] == BrainData.A) {
                return GROUND_GRAYSCALE;
            }// 下视
            else if (a[1] == BrainData.L && a[2] == BrainData.O
                    && a[3] == BrainData.O) {
                return DOWN_WATCH;
            }// 碰撞
            else if (a[1] == BrainData.C && a[2] == BrainData.O
                    && a[3] == BrainData.L) {
                return FRONT_CRASH;
            }// 后端红外
            else if (a[1] == BrainData.I && a[2] == BrainData.N
                    && a[3] == BrainData.F) {
                return REAR_END_INFRARED;
            }
        }

        return ERROR;
    }

    /**
     * 匹配音乐
     *
     * @param a
     * @return MUSIC:成功 ERROR:失败
     */
    public static int matchingMusicArray(byte[] a) {
        if (a[1] == BrainData.M && a[2] == BrainData.U && a[3] == BrainData.S) {
            return MUSIC;
        }

        return ERROR;
    }

    /**
     * 匹配指南针与陀螺仪
     *
     * @param a
     * @return POSITION:成功 ERROR:失败
     */
    public static int matchingPositionArrayM(byte[] a) {
        if (a[1] == BrainData.P && a[2] == BrainData.O && a[3] == BrainData.S) {
            return POSITION;
        }

        return ERROR;
    }

    /**
     * 匹配视频 M
     *
     * @param a
     * @return VIDEO:成功 ERROR:失败
     */
    public static int matchingVideoArrayM(byte[] a) {
        if (a[1] == BrainData.V && a[2] == BrainData.I && a[3] == BrainData.D) {
            return VIDEO;
        }

        return ERROR;
    }

    /**
     * 匹配显示窗 M
     *
     * @param a
     * @return WINDOW:成功 ERROR:失败
     */
    public static int matchingWindowArrayM(byte[] a) {
        if (a[1] == BrainData.W && a[2] == BrainData.I && a[3] == BrainData.N) {
            return WINDOW;
        }else if(a[5] == (byte)0x20 && a[6] == (byte) 0x06){
            return WINDOW;
        }

        return ERROR;
    }

    /**
     * 匹配指南针与陀螺仪 C
     *
     * @param a
     * @return POSITION:成功 ERROR:失败
     */
    public static int matchingPositionArrayC(byte[] a) {
        if (a[1] == BrainData.P && a[2] == BrainData.O && a[3] == BrainData.S) {
            return POSITION;
        }

        return ERROR;
    }

    /**
     * 匹配视频 C
     *
     * @param a
     * @return VIDEO:成功 ERROR:失败
     */
    public static int matchingVideoArrayC(byte[] a) {
        if (a[0] == BrainData.O && a[1] == BrainData.P && a[2] == BrainData.E) {
            return VIDEO;
        }
        //xiongxin@20171114 fix start
        //else if((a[5] & 0xff) == 0x20 && (a[6] & 0xff) == 0x05 && (a[11] & 0xff) == 0x01){
        //    return VIDEO;
        //}
        //end
        return ERROR;
    }

    /**
     * 匹配 清空 显示窗 C
     *
     * @param a
     * @return WINDOW_CLEAR:成功 ERROR:失败
     */
    public static int matchingWindowArrayClearC(byte[] a) {
        if (a[0] == BrainData.C && a[1] == BrainData.L && a[2] == BrainData.E) {
            return WINDOW_CLEAR;
        } else if ((a[5] & 0xff) == 0x20 && (a[6] & 0xff) == 0x06 && (a[11] & 0xff) == 0x03) {
            return WINDOW_CLEAR;
        }

        return ERROR;
    }

    /**
     * 匹配显示窗 C
     *
     * @param a
     * @return WINDOW:成功 ERROR:失败
     */
    public static int matchingWindowArrayC(byte[] a) {
        if (a[0] == BrainData.P && a[1] == BrainData.A && a[2] == BrainData.T) {
            return WINDOW;
        } else if ((a[5] & 0xff) == 0x20 && (a[6] & 0xff) == 0x06 /*&& (a[11] & 0xff) == 0x01*/) {
            return WINDOW;
        }

        return ERROR;
    }

    /**
     * 匹配录音界面
     *
     * @param a
     * @return RECORD_WINDOW:成功 ERROR:失败
     */
    public static int matchingRecordWindowArrayC(byte[] a) {
        if (a[0] == BrainData.A && a[1] == BrainData.U && a[2] == BrainData.D) {
            return RECORD_WINDOW;
        }

        return ERROR;
    }

    /**
     * 匹配录音C
     *
     * @param a
     * @return RECORD:成功 ERROR:失败
     */
    public static int matchingRecordArrayC(byte[] a) {
        if (a[0] == BrainData.DR && a[1] == BrainData.E && a[2] == BrainData.C) {
            return RECORD;
        }

        return ERROR;
    }

    /**
     * 匹配播放录音C
     *
     * @param a
     * @return PLAY_RECORD:成功 ERROR:失败
     */
    public static int matchingPlayRecordArrayC(byte[] a) {
        if (a[0] == BrainData.P && a[1] == BrainData.L && a[2] == BrainData.A) {
            return PLAY_RECORD;
        }

        return ERROR;
    }

    /**
     * 匹配 C END
     *
     * @param a
     * @return END_C:成功 ERROR:失败
     */
    public static int matchingENDC(byte[] a) {
        if (a[0] == BrainData.E && a[1] == BrainData.N && a[2] == BrainData.D) {
            return END_C;
        }

        return ERROR;
    }

    /**
     * 匹配 AbilixChart
     *
     * @param a
     * @return ABILIX_SCRATCH_CODE:成功 ERROR:失败
     */
    public static int matchingAbilixScratchArray(byte[] a) {
        if (a[5] == BrainData.DATA_FOUR || a[5] == BrainData.DATA_FIVE
                || a[5] == BrainData.DATA_SIX || a[5] == 0x0A) {
            return ABILIX_SCRATCH_CODE;
        }
        return ERROR;
    }

    /**
     * 匹配 Abilix_Project_Program
     *
     * @param a
     * @return ABILIX_PROJECT_PROGRAM_CODE:成功 ERROR:失败
     */
    public static int matchingAbilixProjectProgramArray(byte[] a) {
        if (a[0] == BrainData.A && a[3] == BrainData.I && a[6] == BrainData.S
                && a[13] == BrainData.C) {
            return ABILIX_PROJECT_PROGRAM_CODE;
        }
        return ERROR;
    }

    /**
     * 当前命令是否是SkillPlayer命令
     *
     * @param data
     * @return true or false
     */
    public static boolean isSkillPlayerCmd(byte[] data) {
        LogMgr.d("isSkillPlayerCmd()");

        if (data == null || data.length <= 0) {
            return false;
        }
        if (data[0] != (byte) 0xAA || data[1] != (byte) 0x55
                || data.length < 12) {
            return false;
        }
        boolean result = false;

        LogMgr.d("APP_FLAG = " + ((int) data[11] & 0xFF));
        if (((int) data[11] & 0xFF) == GlobalConfig.SKILL_PLAYER_APP_FLAG) {
            result = true;
        }
        return result;
    }

    /**
     * 当前命令是否是内部文件传输命令
     *
     * @param data
     * @return true or false
     */
    public static boolean isInnerFileTransportCmd(byte[] data) {

        if (data == null || data.length <= 0) {
            return false;
        }
        boolean result = false;
        if (((int) data[11] & 0xFF) == GlobalConfig.INNER_FILE_TRANSPORT_APP_FLAG) {
            result = true;
        }
        return result;
    }

    /**
     * 当前命令是否是播放动作/音频命令
     *
     * @param data
     * @return true or false
     */
    public static boolean isSkillPlayerPlayCmd(byte[] data) {
        if (data == null || data.length <= 0) {
            return false;
        }

        boolean result = false;
        // 帧头正确 命令字1命令字2表明是播放动作/音频命令
        if (data[0] == (byte) 0xAA && data[1] == (byte) 0x55
                && data[5] == (byte) 0x02 && data[6] == (byte) 0x01) {
            result = true;
        }
        return result;
    }

    /**
     * 从SkillPlay命令中获取要播放的文件名
     *
     * @param data
     * @return 文件名
     */
    public static String getFileNameFromCmd(byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }

        String result = null;
        try {
            int dataLength = (int) ((data[3] & 0xFF) | ((data[2] & 0xFF) << 8));
            byte[] tempbuff = new byte[dataLength - 13];
            System.arraycopy(data, 16, tempbuff, 0, tempbuff.length); // 文件名
            result = new String(tempbuff, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return result;
    }

    /**
     * vjc
     *
     * @param a
     * @return  OVER:结束 SUCCEED:继续
     */
    public static int overArray(byte[] a) {

        if (a[1] == BrainData.O && a[2] == BrainData.V && a[3] == BrainData.E
                && a[4] == BrainData.DR) {
            return OVER;
        }

        return SUCCEED;
    }

    public static byte[] floatsToByte(float[] values) {
        byte[] byteX = float2byte(values[0]);
        byte[] byteY = float2byte(values[1]);
        byte[] byteZ = float2byte(values[2]);
        byte[] byte2 = byteMerger(byteX, byteY);
        byte[] byte3 = byteMerger(byte2, byteZ);
        return byte3;
    }

    public static byte[] float2byte(float f) {
        int fbit = Float.floatToIntBits(f);
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }
        int len = b.length;
        byte[] dest = new byte[len];
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }
        return dest;

    }

    // 合并数组
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * 字节转换为浮点
     *
     * @param b     字节（至少4个字节）
     * @param index 开始位置
     * @return float值
     */
    public static float byte2float(byte[] b, int index) {
        int l = 0;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

    public static int bytearaytoint(byte[] b) {
        return b[3] & 0xFF | (b[2] & 0XFF) << 8 | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
    }

    public static byte[] inttobytearray(int a) {
        return new byte[]{(byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF), (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF),};
    }

    public static File getDiskCacheDir(String uniqueName, Context context) {
        File file = null;
        try {
            file = new File(ABILIX + uniqueName);
            if (!file.exists()) {
                file.mkdir();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getDiskCacheDirS(String uniqueName, Context context) {
        File file = null;
        try {
            file = new File(uniqueName);
            if (!file.exists()) {
                file.mkdir();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getDiskCacheDir(Context context) {
        File file = null;
        try {
            file = new File(ABILIX);
            if (!file.exists()) {
                file.mkdir();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 两个字节 高位在前低位在后 转换为int型
    public static int ProtocolByteToInt(byte[] b) {
        int l = 0;
        l = (b[0] << 8) & 0xFF00;
        l |= b[1];
        return l;

    }

    /**
     * 获取本机ip
     *
     * @return 本机ip 失败返回null
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.getHostAddress().endsWith(".1")) {
                            return inetAddress.getHostAddress().toString();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置背景，默认使用C系列的背景
     * @return
     */
    public static int getAppBg() {
        int bg = R.drawable.main_bg_c; // 默认使用C系列的背景
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_C9:
            case GlobalConfig.ROBOT_TYPE_CU:
                bg = R.drawable.main_bg_c;
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
                bg = R.drawable.main_bg_m;
                break;
            case GlobalConfig.ROBOT_TYPE_M1:
                bg = R.drawable.main_bg_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
            case GlobalConfig.ROBOT_TYPE_H3:
                bg = R.drawable.main_bg_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                bg = R.drawable.main_bg_f;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                bg = R.drawable.main_bg_af;
                break;
            case 5: // s
                bg = R.drawable.main_bg_s;
                break;
            default:
                bg = R.drawable.main_bg_c;
                break;
        }
        return bg;
    }

    /**
     * 获取录音初始界面图片资源ID
     *
     */
    public static int getRecordInitDrawableResource() {
        int drawableResourceID = R.drawable.record_init_c; // 默认使用C系列的背景
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                drawableResourceID = R.drawable.record_init_c;
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
            case GlobalConfig.ROBOT_TYPE_M1:
                drawableResourceID = R.drawable.record_init_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
            case GlobalConfig.ROBOT_TYPE_H3:
                drawableResourceID = R.drawable.record_init_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                drawableResourceID = R.drawable.record_init_f;
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.record_init_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.record_init_af;
                break;
            default:
                drawableResourceID = R.drawable.record_init_c;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取录音初始界面选装光圈ID
     *
     */
    public static int getRecordLightRingDrawableResource() {
        int drawableResourceID = R.drawable.lightring_c; // 默认使用C系列的背景
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                drawableResourceID = R.drawable.lightring_c;
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
            case GlobalConfig.ROBOT_TYPE_M1:
                drawableResourceID = R.drawable.lightring_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
            case GlobalConfig.ROBOT_TYPE_H3:
                drawableResourceID = R.drawable.lightring_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                drawableResourceID = R.drawable.lightring_f;
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.lightring_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.lightring_af;
                break;
            default:
                drawableResourceID = R.drawable.lightring_c;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取录音初始界面选装光圈ID
     *
     */
    public static int getRecordCompleteDrawableResource() {
        int drawableResourceID = R.drawable.record_complete_c; // 默认使用C系列的背景
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                drawableResourceID = R.drawable.record_complete_c;
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
            case GlobalConfig.ROBOT_TYPE_M1:
                drawableResourceID = R.drawable.record_complete_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
            case GlobalConfig.ROBOT_TYPE_H3:
                drawableResourceID = R.drawable.record_complete_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                drawableResourceID = R.drawable.record_complete_f;
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.record_complete_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.record_complete_af;
                break;
            default:
                drawableResourceID = R.drawable.record_complete_c;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取按钮弹窗的背景图id
     *
     */
    public static int getButtonDialogBackgroundDrawableResource() {
        int drawableResourceID = R.drawable.button_dialog_background_c; // 默认使用C系列的背景
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                drawableResourceID = R.drawable.button_dialog_background_c;
                if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                    drawableResourceID = R.drawable.button_dialog_background_ku;
                }
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
            case GlobalConfig.ROBOT_TYPE_M1:
                drawableResourceID = R.drawable.button_dialog_background_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
            case GlobalConfig.ROBOT_TYPE_H3:
                drawableResourceID = R.drawable.button_dialog_background_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                drawableResourceID = R.drawable.button_dialog_background_f;
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.button_dialog_background_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.button_dialog_background_af;
                break;
            default:
                drawableResourceID = R.drawable.button_dialog_background_c;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取无按钮弹窗的背景图id
     *
     */
    public static int getNoButtonDialogBackgroundDrawableResource() {
        int drawableResourceID = R.drawable.no_button_dialog_background_c; // 默认使用C系列的背景
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                drawableResourceID = R.drawable.no_button_dialog_background_c;
                if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                    drawableResourceID = R.drawable.no_button_dialog_background_ku;
                }
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
            case GlobalConfig.ROBOT_TYPE_M1:
                drawableResourceID = R.drawable.no_button_dialog_background_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
            case GlobalConfig.ROBOT_TYPE_H3:
                drawableResourceID = R.drawable.no_button_dialog_background_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                drawableResourceID = R.drawable.no_button_dialog_background_f;
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.no_button_dialog_background_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.no_button_dialog_background_af;
                break;
            default:
                drawableResourceID = R.drawable.no_button_dialog_background_c;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取单按钮弹窗的按钮背景图id
     *
     */
    public static int getSingleButtonDialogButtonBackgroundDrawableResource() {
        int drawableResourceID = R.drawable.single_button_dialog_button_background_c; // 默认使用C系列的背景
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                drawableResourceID = R.drawable.single_button_dialog_button_background_c;
                if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                    drawableResourceID = R.drawable.single_button_dialog_button_background_ku;
                }
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
            case GlobalConfig.ROBOT_TYPE_M1:
                drawableResourceID = R.drawable.single_button_dialog_button_background_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
            case GlobalConfig.ROBOT_TYPE_H3:
                drawableResourceID = R.drawable.single_button_dialog_button_background_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                drawableResourceID = R.drawable.single_button_dialog_button_background_f;
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.single_button_dialog_button_background_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.single_button_dialog_button_background_af;
                break;
            default:
                drawableResourceID = R.drawable.single_button_dialog_button_background_c;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取双按钮弹窗的按钮背景图id
     *
     * @param leftOrRignt f,h需要区分左右。0表示左，1表示右。其他系列设置后并无影响。
     */
    public static int getTwoButtonDialogButtonBackgroundDrawableResource(int leftOrRignt) {
        final int left = 0;
        final int right = 1;
        int side = leftOrRignt;

        int drawableResourceID = R.drawable.two_button_dialog_button_background_c; // 默认使用C系列的背景
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                drawableResourceID = R.drawable.two_button_dialog_button_background_c;
                if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                    if (side == left) {
                        drawableResourceID = R.drawable.two_button_dialog_left_button_background_ku;
                    } else if (side == right) {
                        drawableResourceID = R.drawable.two_button_dialog_right_button_background_ku;
                    } else {
                        LogMgr.e("参数错误");
                        drawableResourceID = R.drawable.two_button_dialog_button_background_ku;
                    }
                }
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
            case GlobalConfig.ROBOT_TYPE_M1:
                drawableResourceID = R.drawable.two_button_dialog_button_background_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
            case GlobalConfig.ROBOT_TYPE_H3:
                if (side == left) {
                    drawableResourceID = R.drawable.two_button_dialog_left_button_background_h;
                } else if (side == right) {
                    drawableResourceID = R.drawable.two_button_dialog_right_button_background_h;
                } else {
                    LogMgr.e("参数错误");
                    drawableResourceID = R.drawable.two_button_dialog_left_button_background_h;
                }
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                if (side == left) {
                    drawableResourceID = R.drawable.two_button_dialog_left_button_background_f;
                } else if (side == right) {
                    drawableResourceID = R.drawable.two_button_dialog_right_button_background_f;
                } else {
                    LogMgr.e("参数错误");
                    drawableResourceID = R.drawable.two_button_dialog_left_button_background_f;
                }
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.two_button_dialog_button_background_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.two_button_dialog_button_background_af;
                break;
            default:
                drawableResourceID = R.drawable.two_button_dialog_button_background_c;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取vjc显示界面图id
     *
     */
    public static int getVJCTextViewBackgroundDrawableResource() {
        int drawableResourceID = R.drawable.vjc_textview_background_c; // 默认使用C系列的背景
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                drawableResourceID = R.drawable.vjc_textview_background_c;
                if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                    drawableResourceID = R.drawable.vjc_textview_background_ku;
                }
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
            case GlobalConfig.ROBOT_TYPE_M1:
                drawableResourceID = R.drawable.vjc_textview_background_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
            case GlobalConfig.ROBOT_TYPE_H3:
                drawableResourceID = R.drawable.vjc_textview_background_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                drawableResourceID = R.drawable.vjc_textview_background_f;
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.vjc_textview_background_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.vjc_textview_background_af;
                break;
            default:
                drawableResourceID = R.drawable.vjc_textview_background_c;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取进度条背景
     *
     * @return
     */
    public static int getProgressBackgroundDrawableResource() {
        int drawableResourceID = 0;
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
                drawableResourceID = R.drawable.progress_background_c;
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
                drawableResourceID = R.drawable.progress_background_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
                drawableResourceID = R.drawable.progress_background_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                drawableResourceID = R.drawable.progress_background_f;
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.progress_background_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.progress_background_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 获取进度条滑块背景
     *
     * @return
     */
    public static int getProgressThumbDrawableResource() {
        int drawableResourceID = 0;
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C: // c
                drawableResourceID = R.drawable.progress_thumb_c;
                break;
            case GlobalConfig.ROBOT_TYPE_M: // m
                drawableResourceID = R.drawable.progress_thumb_m;
                break;
            case GlobalConfig.ROBOT_TYPE_H: // h
                drawableResourceID = R.drawable.progress_thumb_h;
                break;
            case GlobalConfig.ROBOT_TYPE_F: // f
                drawableResourceID = R.drawable.progress_thumb_f;
                break;
            case GlobalConfig.ROBOT_TYPE_S: // s
                drawableResourceID = R.drawable.progress_thumb_s;
                break;
            case GlobalConfig.ROBOT_TYPE_AF: // af
                drawableResourceID = R.drawable.progress_thumb_af;
                break;
        }
        return drawableResourceID;
    }

    /**
     * 计算产生校验码
     *
     * @param data 需要校验的数据
     * @return CRC校验码
     */
    public static String Make_CRC(byte[] data) {
        byte[] buf = new byte[data.length];// 存储需要产生校验码的数据
        for (int i = 0; i < data.length; i++) {
            buf[i] = data[i];
        }
        int len = buf.length;
        int crc = 0xFFFF;
        for (int pos = 0; pos < len; pos++) {
            if (buf[pos] < 0) {
                crc ^= (int) buf[pos] + 256; // XOR byte into least sig. byte of
                // crc
            } else {
                crc ^= (int) buf[pos]; // XOR byte into least sig. byte of crc
            }
            for (int i = 8; i != 0; i--) { // Loop over each bit
                if ((crc & 0x0001) != 0) { // If the LSB is set
                    crc >>= 1; // Shift right and XOR 0xA001
                    crc ^= 0xA001;
                } else
                    // Else LSB is not set
                {
                    crc >>= 1; // Just shift right
                }
            }
        }
        String c = Integer.toHexString(crc);
        if (c.length() == 4) {
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 3) {
            c = "0" + c;
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 2) {
            c = "0" + c.substring(1, 2) + "0" + c.substring(0, 1);
        }
        return c;
    }

    public static int getCRC(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data, 11, data.length - 12);
        crc32.update(data);
        return (int) crc32.getValue();
    }

    /**
     * 根据apk文件获取包名
     *
     * @param path
     * @return 包名
     */
    public static String getPackageName(String path) {
        String result = null;

        PackageManager pm = Application.getInstance().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path,
                PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            result = appInfo.packageName;
            return result;
        }
        return result;
    }

    /**
     * toast
     *
     * @param text
     * @param mContext
     */
    public static void utilisToast(String text, Context mContext) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    // /**
    // * 获取屏幕的亮度
    // *
    // * @param activity
    // * @return
    // */
    // public static int getScreenBrightness(Activity activity) {
    // int nowBrightnessValue = 0;
    // ContentResolver resolver = activity.getContentResolver();
    // try {
    // nowBrightnessValue = android.provider.Settings.System.getInt(
    // resolver, Settings.System.SCREEN_BRIGHTNESS);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // return nowBrightnessValue;
    // }

    /**
     * 设置亮度
     *
     * @param activity
     * @param brightness
     */
    public static void setBrightness(Activity activity, float brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        activity.getWindow().setAttributes(lp);
    }
}
