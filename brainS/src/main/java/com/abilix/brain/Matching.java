package com.abilix.brain;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.BatteryManager;
import android.os.Handler;

import com.abilix.brain.utils.BrainData;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.Utils;
import com.abilix.explainer.utils.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * “我饿了”音频播放功能实现；温度过高提示功能实现；判断stm32版本是否升级功能事项。
 *
 * @author luox
 */
public class Matching extends BrainInfo {

    private static Matching matching;
    public static String pathName = null;
    public static int STM_VISION = -1;

    /**
     * 定时器延迟时间
     */
    private static final int PERIOD = 20000;
    /**
     * 添加一个定时器
     */
    private Timer level10playMusic;
    private TimerTask level10playMusicTask;
    /**
     * 添加温度对应的定时器
     */
    private Timer TempTimer;
    private TimerTask TempTimerTask;

    private MediaPlayer mediaPlayer;

    /**
     * 定时器只启动一次
     */
    private boolean startOnce = true;
    private boolean startOnceTemp = true;
    /**
     * 是否开始播放音频
     */
    private boolean isStart;
    private boolean isTemp65;

    public static Matching getMatching() {
        if (matching == null) {
            matching = new Matching();
        }
        return matching;
    }

    /**
     * C类型认识机器人部分功能协议匹配，逻辑分发实现。
     *
     * @author luox
     */
    public static class MathingC {
        private static boolean isPlayC;
        private static boolean isWindowC;
        private static boolean isVideo;
        private static boolean isMedia;

        /**
         * 显示窗 C
         *
         * @param data
         */
        public static void sendWindowC(byte[] data) {
            // 开
            if (data[3] == BrainData.H || (data[11] & 0xff) == 0x01) {
                isWindowC = true;
                SendBroadcastActivity(BrainData.DATA_WINDOW_OPEN);
            }// 清空
            else if (data[3] == BrainData.A || (data[11] & 0xff) == 0x03) {
                SendBroadcastActivity(BrainData.DATA_WINDOW_CLEAR);
            }//关闭
            else if ((data[11] & 0xff) == 0x02) {
                SendBroadcastActivity(BrainData.DATA_WINDOW_STOP);
            }

        }

        public static void displayPic(byte[] data) {
            int cmd_length = ByteUtils.byte2int_2byteHL(data, 2);
            LogMgr.e("cmd_length:" + cmd_length);
            int path_length = cmd_length - 14;
            byte[] path_byte = new byte[path_length];
            System.arraycopy(data, 17, path_byte, 0, path_length);
            String path = null;
            try {
                path = new String(path_byte, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String fullPath = BrainUtils.ABILIX + BrainUtils.ABILIX_MEDIA + path;
            LogMgr.e("图片路径：" + fullPath);
            File file = new File(fullPath);
            if (file.exists() && file.isFile()) {
                SendBroadcastActivityAnimation(BrainUtils.PICTURE_DISPLAY, fullPath);
            } else {
                byte[] d = {(byte) 0x01};
                byte[] protoco = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0xA6, (byte) 0x01,
                        d);
                mBrainInfo.returnDataToClient(protoco);
            }

        }

        /**
         * END C
         */
        public static void sendEndC() {
            LogMgr.d("isPlayC =" + isPlayC + ";isWindowC = " + isWindowC + ";isVideo = " + isVideo);
            if (isPlayC) {
                isPlayC = false;
                SendBroadcastActivity(BrainData.STOP_RECORD_WINDOW);
            } else if (isWindowC) {
                LogMgr.d("isWindowC ");
                isWindowC = false;
                SendBroadcastActivity(BrainData.DATA_WINDOW_STOP);
            } else if (isVideo) {
                isVideo = false;
                SendBroadcastActivity(BrainData.DATA_VIEDO_STOP);
            }
        }

        /**
         * 视频 S
         *
         * @param data
         */
        public static void closeVideo(byte[] data) {
            isVideo = false;
            SendBroadcastActivity(BrainData.DATA_VIEDO_STOP);
        }

        /**
         * 录音界面 C
         *
         * @param data
         */
        public static void windowRecord(byte[] data) {
            try {
                isPlayC = true;
                SendBroadcastActivity(BrainData.START_RECORD_WINDOW);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 录音 C
         */
        public static void sendRecord(byte[] data) {
            try {
                SendBroadcastActivity(BrainData.START_RECORD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 停止录音 C
         */
        public static void stopRecord(byte[] data) {
            try {
                SendBroadcastActivity(BrainData.STOP_RECORD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 播放录音 C
         *
         * @param data
         */
        public static void playRecord(byte[] data) {
            try {
                SendBroadcastActivity(BrainData.START_RECORD_PLAY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 视频 C/S
         *
         * @param data
         */
        public static void sendVideo(byte[] data) {
            isVideo = true;
            SendBroadcastActivity(BrainData.DATA_VIEDO_OPEN);
        }

        /**
         * 播放动画
         *
         * @param data
         */
        public static void playAnimation(byte[] data) {
            String path = "";
            int index = data[12] + 10;
            if (index == 11) {
                path = FileUtils.SCRATCH_VJC_IMAGE_ + index + FileUtils.SCRATCH_VJC_IMAGE_GIF;
            } else {
                path = FileUtils.SCRATCH_VJC_IMAGE_ + index + FileUtils.SCRATCH_VJC_IMAGE_JPG;
            }
            SendBroadcastActivityAnimation(BrainUtils.SCRATCH_VJC_IMAGEVIEW, path);
        }

        /**
         * 停止播放动画
         */
        public static void stopAnimation() {
            SendBroadcastActivity(BrainUtils.SCRATCH_VJC_IMAGEVIEW_STOP);
        }

        public static void sendMediaPlay(byte[] receiveData) {
            if ((receiveData[11] & 0xff) == 0x00) {
                byte[] data = Arrays.copyOfRange(receiveData, 12, receiveData.length - 1);
                String filePaht = new String(data);
                sendMedia(filePaht);
            } else if ((receiveData[11] & 0xff) == 0x01) {
                stopMedia();
            }

        }

        private static void stopMedia() {
            if (isMedia) {
                isMedia = false;
                SendBroadcastActivity(BrainData.STOP_MEDIA);
            }
        }

        private static void sendMedia(String filePaht) {
            if (!isMedia) {
                isMedia = true;
                SendBroadcastActivity(BrainData.OPEN_MEDIA, filePaht);
            }
        }

        /**
         * //KU201 项目编程 显示电机页面
         */
        public static void openMotorView(byte[] data) {
            try {
                SendBroadcastActivity(BrainUtils.KU_OPEN_MOTOR_VIEW);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * //KU201 项目编程 关闭显示电机页面
         */
        public static void closeMotorView(byte[] data) {
            try {
                SendBroadcastActivity(BrainUtils.KU_CLOSE_MOTOR_VIEW);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * M类型认识机器人部分功能协议匹配，逻辑分发实现。
     *
     * @author luox
     */
    public static class MathingM {
        /**
         * 显示窗 M
         *
         * @param data
         */
        public static void sendWindowM(byte[] data) {
            if (data[5] == (byte) 0x20 && data[6] == (byte) 0x06) {
                if (data[11] == BrainData.DATA_TWO) {
                    SendBroadcastActivity(BrainData.DATA_WINDOW_STOP);
                } else if (data[11] == BrainData.DATA_ONE) {
                    SendBroadcastActivity(BrainData.DATA_WINDOW_OPEN);
                } else {
                    SendBroadcastActivity(BrainData.DATA_WINDOW_CLEAR);
                }
            } else {
                // 关
                if (data[7] == BrainData.DATA_ZERO) {
                    SendBroadcastActivity(BrainData.DATA_WINDOW_STOP);
                }// 开
                else if (data[7] == BrainData.DATA_ONE) {
                    SendBroadcastActivity(BrainData.DATA_WINDOW_OPEN);
                } // 清除
                else if (data[7] == BrainData.DATA_THREE) {
                    SendBroadcastActivity(BrainData.DATA_WINDOW_CLEAR);
                }
            }

        }

        /**
         * 视频 M
         *
         * @param data
         */
        public static void sendVideo(byte[] data) {
            // 关
            if (data[6] == BrainData.DATA_ZERO) {
                SendBroadcastActivity(BrainData.DATA_VIEDO_STOP);
            }// 开
            else if (data[6] == BrainData.DATA_ONE) {
                SendBroadcastActivity(BrainData.DATA_VIEDO_OPEN);
            }
        }

        /* 划线新协议 */
        public static void sendWindowNew(byte[] data) {
            // 关
            if (data[11] == 2) {
                SendBroadcastActivity(BrainData.DATA_WINDOW_STOP);
            }// 开
            else if (data[11] == 1) {
                SendBroadcastActivity(BrainData.DATA_WINDOW_OPEN);
            } // 清除
            else if (data[11] == 3) {
                SendBroadcastActivity(BrainData.DATA_WINDOW_CLEAR);
            }
        }

        /**
         * 视频新协议
         *
         * @param data
         */
        public static void sendVideoNew(byte[] data) {
            // 关
            if (data[11] == BrainData.DATA_ZERO) {
                SendBroadcastActivity(BrainData.DATA_VIEDO_STOP);
            }// 开
            else if (data[11] == BrainData.DATA_ONE) {
                SendBroadcastActivity(BrainData.DATA_VIEDO_OPEN);
            }
        }
    }

    /**
     * H类型认识机器人部分功能协议匹配，逻辑分发实现；目前此类未被使用。
     *
     * @author luox
     */
    public static class MathingH {
        /**
         * 视频 H
         *
         * @param data
         */
        public static void sendVideo(byte[] data) {
            // 关
            if (data[6] == BrainData.DATA_ZERO) {
                SendBroadcastActivity(BrainData.DATA_VIEDO_STOP);
            }// 开
            else if (data[6] == BrainData.DATA_ONE) {
                SendBroadcastActivity(BrainData.DATA_VIEDO_OPEN);
            }
        }
    }

    /**
     * 发送广播 Activity
     *
     * @param mode
     */
    public static void SendBroadcastActivity(int mode) {
        Intent intent = new Intent(GlobalConfig.ACTION_ACTIVITY);
        intent.putExtra(GlobalConfig.ACTION_SERVICE_MODE, mode);
        BrainService.getmBrainService().sendBroadcast(intent);
    }

    /**
     * 发送广播 Activity
     *
     * @param mode
     */
    public static void SendBroadcastActivity(int mode, String args) {
        Intent intent = new Intent(GlobalConfig.ACTION_ACTIVITY);
        intent.putExtra(GlobalConfig.ACTION_SERVICE_MODE, mode);
        intent.putExtra("args", args);
        BrainService.getmBrainService().sendBroadcast(intent);
    }

    /**
     * 发送广播 Activity
     *
     * @param mode
     */
    public static void SendBroadcastActivity(int mode, byte[] data) {
        Intent intent = new Intent(GlobalConfig.ACTION_ACTIVITY);
        intent.putExtra(GlobalConfig.ACTION_SERVICE_MODE, mode);
        intent.putExtra("data", data);
        BrainService.getmBrainService().sendBroadcast(intent);
    }

    public static void SendBroadcastActivityAnimation(int mode, String path) {
        Intent intent = new Intent(GlobalConfig.ACTION_ACTIVITY);
        intent.putExtra(GlobalConfig.ACTION_SERVICE_MODE, mode);
        intent.putExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH, path);
        BrainService.getmBrainService().sendBroadcast(intent);
    }

    // 创建静代码快,提前的确定
    public static void stm32_Update() {
        try {
            switch (GlobalConfig.BRAIN_TYPE) {
                case GlobalConfig.ROBOT_TYPE_C:
                    STM_VISION = GlobalConfig.STM_VISION_C;
                    if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                        STM_VISION = GlobalConfig.STM_VISION_CU;
                    } else if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_C9) {
                        STM_VISION = GlobalConfig.STM_VISION_C9;
                    }
                    break;
                case GlobalConfig.ROBOT_TYPE_M:
                    STM_VISION = GlobalConfig.STM_VISION_M;
                    if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_M3S || GlobalConfig.BRAIN_CHILD_TYPE
                            == GlobalConfig.ROBOT_TYPE_M4S) {
                        STM_VISION = GlobalConfig.STM_VISION_MS;
                    }
                    break;
                case GlobalConfig.ROBOT_TYPE_H:
                    STM_VISION = GlobalConfig.STM_VISION_H;
                    break;
                case GlobalConfig.ROBOT_TYPE_C1:
                    STM_VISION = GlobalConfig.STM_VISION_C1;
                    break;
                case GlobalConfig.ROBOT_TYPE_M1:
                    STM_VISION = GlobalConfig.STM_VISION_M1;
                    break;
                case GlobalConfig.ROBOT_TYPE_S:
                    STM_VISION = GlobalConfig.STM_VISION_S;
                    break;
                case GlobalConfig.ROBOT_TYPE_F:
                    STM_VISION = GlobalConfig.STM_VISION_F;
                    break;
                case GlobalConfig.ROBOT_TYPE_AF:
                    STM_VISION = GlobalConfig.STM_VISION_AF;
                    break;
                case GlobalConfig.ROBOT_TYPE_C9:
                    STM_VISION = GlobalConfig.STM_VISION_C9;
                    break;
                case GlobalConfig.ROBOT_TYPE_H3:
                    STM_VISION = GlobalConfig.STM_VISION_H3;
                    break;
            }

            String stm32VersionInHexString1 = Integer.toHexString(STM_VISION).toUpperCase();
            if (stm32VersionInHexString1.length() == 8) {
                //do nothing
            } else if (stm32VersionInHexString1.length() == 7) {
                stm32VersionInHexString1 = "0" + stm32VersionInHexString1;
            } else {
                LogMgr.e("stm32版本转换成16进制时位数错误");
            }
            final String stm32VersionInHexString2 = stm32VersionInHexString1;
            LogMgr.i("stm32VersionInHexString2 = " + stm32VersionInHexString2);


            new Thread(new Runnable() {
                @Override
                public void run() {
                    switch (GlobalConfig.BRAIN_TYPE) {
                        case GlobalConfig.ROBOT_TYPE_C:
                            pathName = "C5_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                                pathName = "CU_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            } else if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_C9) {
                                pathName = "C9_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            }
                            break;
                        case GlobalConfig.ROBOT_TYPE_M:
                            pathName = "M5_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_M3S) {
                                pathName = "MS_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            }
                            break;
                        case GlobalConfig.ROBOT_TYPE_H:
                            pathName = "H5_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            break;
                        case GlobalConfig.ROBOT_TYPE_C1:// C1是0x09
                            pathName = "C1_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            break;
                        case GlobalConfig.ROBOT_TYPE_M1:// C1是0x09
                            pathName = "M1_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            break;
                        case GlobalConfig.ROBOT_TYPE_S:// 这个bin文件要改掉。
                            LogMgr.e("control pathName" + "S5ControlUpdata");
                            pathName = "S5_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            break;
                        case GlobalConfig.ROBOT_TYPE_C9:
                            pathName = "C9_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            break;
                        case GlobalConfig.ROBOT_TYPE_H3:
                            pathName = "H3_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
                            break;

                        case GlobalConfig.ROBOT_TYPE_F:
                            pathName = "F_ControlUpdata_" + stm32VersionInHexString2 + ".bin";
//                            pathName = "F_ControlUpdata_51133330.bin";
                            break;
                    }
                    if (pathName != null) {
                        FileUtils.saveBinFileToSdCard(Application.getInstance(), pathName);
                    }
                }
            }).start();

        } catch (Exception e) {

        }
    }

    /**
     * @Description 启动定时器
     * @author lz
     * @time 2017-5-10 下午2:49:00
     */
    private void startTimer(final Context context) {
        if (!startOnce) {
            LogMgr.d("定时器只启动一次");
            return;
        }
        stopTimer();
        startOnce = false;
        level10playMusic = new Timer();
        level10playMusicTask = new TimerTask() {

            @Override
            public void run() {
                if (isStart) {
                    LogMgr.d("播放提示音");
                    alert_sound(context, 1);
                }
            }
        };
        level10playMusic.schedule(level10playMusicTask, 0, PERIOD);
    }

    /**
     * @Description 停止定时器
     * @author lz
     * @time 2017-5-5 下午4:41:57
     */
    private void stopTimer() {
        startOnce = true; // 解除限制
        if (level10playMusic != null) {
            level10playMusic.cancel();
            level10playMusic = null;
        }
        if (level10playMusicTask != null) {
            level10playMusicTask.cancel();
            level10playMusicTask = null;
        }
    }

    /**
     * 创建温度定时器
     */
    private void startTimerTemp(final Context context, final Handler mHandler) {
        if (!startOnceTemp) {
            LogMgr.d("定时器只启动一次");
            return;
        }
        stopTimerTemp();
        startOnceTemp = false;
        TempTimer = new Timer();
        TempTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isTemp65) {
                    LogMgr.d("播放提示音+弹出动画");
                    //弹出弹框
                    mHandler.sendEmptyMessage(119);
//                    Utils.showNoButtonDialog(context, "title", "message", true);//判断是否显示
//                    alert_sound(context,3);
                }
            }
        };
        TempTimer.schedule(TempTimerTask, 0, 12 * 1000);
    }

    /**
     * 停止温度定时器；
     */
    private void stopTimerTemp() {

        startOnceTemp = true;// 解除限制
        if (TempTimer != null) {
            TempTimer.cancel();
            ;
            TempTimer = null;
        }
        if (TempTimerTask != null) {
            TempTimerTask.cancel();
            TempTimerTask = null;
        }
    }

    /**
     * @param context
     * @param voltage 电压
     * @param status  充电状态
     * @param level   电量
     * @param plugged 充电模式 1是AC，2是USB，4是无线
     * @Description 播放提示音
     * @author lz
     * @time 2017-5-10 上午10:45:11
     */
    public void playTohes(Context context, int voltage, int status, int level, int plugged, double temperature,
                          Handler mHandler) {
        // H5不充电并且电量大于10%时设置为true,在充电状态或者电量小于10%时才能只执行一次下蹲保护动作
        LogMgr.d("voltage:" + voltage + " status:" + status + " level:" + level + " plugged:" + plugged);
        if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING && level >= 20) {
            Application.getInstance().onceProtectionAction = true;
        }
        if (temperature * 0.1 > 65) {
            isTemp65 = true;
            startTimerTemp(context, mHandler);
        } else {
            mHandler.sendEmptyMessage(120);
            isTemp65 = false;
            stopTimerTemp();
        }
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            LogMgr.d("正在充电");
            isStart = false;
            stopTimer();// 正在充电，停止播放“我饿了”音频
            // H5充电时进入保护状态
//            entryProtectStatusH5(plugged, status);
            return;
        }
        isStart = true;
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C9:
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_S:
            case GlobalConfig.ROBOT_TYPE_H3:
            case GlobalConfig.ROBOT_TYPE_M:
                if (level < 10) {
                    startTimer(context);
                } else {
                    stopTimer();
                }
                break;

            case GlobalConfig.ROBOT_TYPE_H:// H5 电压低于10.8 保护
                LogMgr.e("电压：" + voltage);
                if (level < 20) {
                    startTimer(context);
                    //低电量显示当前电量；
                    Intent intent = new Intent("com.abilix.abilixbattey");
                    intent.setPackage("com.abilix.brainset");
                    intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    context.sendBroadcast(intent);
                    mHandler.sendEmptyMessageDelayed(22,200);
                } else {
                    stopTimer();
                }
                // 低电量进入保护状态
//                entryProtectStatusH5(plugged, status);
                break;
            case GlobalConfig.ROBOT_TYPE_C:
            case GlobalConfig.ROBOT_TYPE_CU:
                if (voltage <= 3605) {
                    startTimer(context);
                } else {
                    stopTimer();
                }
                break;
//            case GlobalConfig.ROBOT_TYPE_M:
//                if (voltage <= 3450) {
//                    startTimer(context);
//                } else {
//                    stopTimer();
//                }
//                break;
            case GlobalConfig.ROBOT_TYPE_M1:
                if (voltage <= 3460) {
                    startTimer(context);
                } else {
                    stopTimer();
                }
                break;
        }
    }

    /**
     * @param context
     * @param i
     * @Description 播放音频
     * @author lz
     * @time 2017-5-10 下午3:38:18
     */
    public void alert_sound(Context context, int i) {
        AssetManager assets = context.getAssets();
        String hungryName = null;
        switch (i) {
            case 2:
                if (Utils.LANGUAGE_TYPE_CN != Utils.getLanguageType() && Utils.LANGUAGE_TYPE_TW != Utils
                        .getLanguageType()) {
                    hungryName = "power_hungry_en.mp3";
                } else {
                    hungryName = "power_hungry.mp3";
                }
                break;
            case 1:
                if (Utils.LANGUAGE_TYPE_CN != Utils.getLanguageType() && Utils.LANGUAGE_TYPE_TW != Utils
                        .getLanguageType()) {
                    hungryName = "cpower_hungry_en.mp3";
                } else {
                    hungryName = "cpower_hungry.mp3";
                }
                break;
            default:
                if (Utils.LANGUAGE_TYPE_CN != Utils.getLanguageType() && Utils.LANGUAGE_TYPE_TW != Utils
                        .getLanguageType()) {
                    hungryName = "power_hungry_en.mp3";
                } else {
                    hungryName = "power_hungry.mp3";
                }
                break;
        }

        try {
            AssetFileDescriptor fileDescriptor = assets.openFd(hungryName);
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                        fileDescriptor.getLength());
                // 播放完成的监听
                mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        LogMgr.d("播放完成");
                        release();
                    }
                });
                // prepare 的监听
                mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        LogMgr.d("onPrepared完成");
                        mediaPlayer.start();
                    }
                });
            }
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            LogMgr.e("Matching播放出错");
            release();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description 释放
     * @author lz
     * @time 2017-5-8 下午1:35:03
     */
    private void release() {
        if (mediaPlayer != null) {
            LogMgr.d("释放mediaPlayer");
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        System.gc();
    }

    /**
     * H5 进入保护状态,只执行一次
     */
    private void entryProtectStatusH5(int plugged, int status) {
        if (GlobalConfig.BRAIN_TYPE != GlobalConfig.ROBOT_TYPE_H) {
            LogMgr.d("不是H5不用进入保护状态");
            return;
        }
        if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
            LogMgr.d("没有充电:");
            return;
        }
        if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
            LogMgr.d("不是AC充电");
            return;
        }
        if (!Application.getInstance().onceProtectionAction) { //只执行一次保护动作
            LogMgr.d("H5保护动作只执行一次");
            return;
        }
        BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null,
                null, GlobalConfig.CHARGE_PROTECTION_MOVE, 0);
        Application.getInstance().onceProtectionAction = false;
        // 打开设置
//        Utils.openApp(Application.getInstance(), GlobalConfig.APP_PKGNAME_BRAINSET, false);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("page", 1);
        ComponentName cn = new ComponentName("com.abilix.brainset", "MainActivity.class");
        intent.setComponent(cn);
        Application.getInstance().startActivity(intent);
    }
}
