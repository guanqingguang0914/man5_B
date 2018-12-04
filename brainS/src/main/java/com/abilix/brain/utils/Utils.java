package com.abilix.brain.utils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.abilix.brain.Application;
import com.abilix.brain.BrainActivity;
import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.brain.data.DataProcess;
import com.abilix.brain.m.MUtils;
import com.abilix.explainer.utils.ByteUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * 通用工具类
 */
public class Utils {
    public final static int LANGUAGE_TYPE_EN = 0;
    public final static int LANGUAGE_TYPE_CN = 1;
    public final static int LANGUAGE_TYPE_TW = 2;
    public final static int LANGUAGE_TYPE_OTHER = 20;

    private static MediaPlayer mediaPlayer;

    public static final String INTENT_PACKAGE_NAME = "intent_package_name";
    private static final String INTENT_PACKAGE_NAME_TO_INSTALL = "intent_package_name_to_install";

    private final static char[] charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            .toCharArray();

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


    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public static int bytesToInt3(byte[] bytes, int begin) {// 3个字节转化为int
        // 高位在前低位在后
        return (int) ((0x00ff & bytes[begin + 2]) | ((0x00ff & bytes[begin + 1]) << 8) | ((0x00ff & bytes[begin]) << 16));
    }

	/*
     * public static byte [] float2ByteArray (float value) { return
	 * ByteBuffer.allocate(4).putFloat(value).array(); }
	 */


    public static float byte2float(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

    public static float byte2float2(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index - 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index - 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index - 3] << 24);
        return l;
    }
    public static byte[] intToBytesLH(int value) {
        byte[] src = new byte[2];
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }
    public static int byte2int_2byte(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= (0 << 16);
        l &= 0xffffff;
        l |= (0 << 24);
        return l;
    }

    public static int byteAray2Int(byte[] b) {
        return b[3] & 0xFF | (b[2] & 0XFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    }

    /**
     * 将字节数组转换成字符串
     * @param buf
     * @return
     */
    public static String bytesToString(byte[] buf) {
        if(buf == null){
            return "null";
            }
        if (LogMgr.getLogLevel() == LogMgr.NOLOG || buf.length > 64) {
            return "len = " + buf.length;
        }
        return ByteUtils.bytesToHexString(buf, false);
    }

    public static Long getCRC32(String fileUri) {
        CRC32 crc32 = new CRC32();
        FileInputStream fileinputstream = null;
        CheckedInputStream checkedinputstream = null;
        Long crc = null;
        try {
            fileinputstream = new FileInputStream(new File(fileUri));
            checkedinputstream = new CheckedInputStream(fileinputstream, crc32);
            while (checkedinputstream.read() != -1) {
            }
            // crc = Long.toHexString(crc32.getValue()).toUpperCase();
            crc = crc32.getValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileinputstream != null) {
                try {
                    fileinputstream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (checkedinputstream != null) {
                try {
                    checkedinputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return crc;
    }

    public static long getCRC32(byte[] src){
        CRC32 crc = new CRC32();
        crc.update(src, 0, src.length);
        return crc.getValue();
    }

    /**
     * 将int数值转换为占两个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。 和bytesToInt（）配套使用
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }

    // 低位在后高位在前。
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24) | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
        return value;
    }


    // 获取应用的版本号
    public String getVersion() {
        try {
            PackageManager manager = Application.getInstance().getPackageManager();
            PackageInfo info = manager.getPackageInfo(Application.getInstance().getPackageName(), 0);
            String version = info.versionName;
            return "Brain版本号:" + version;
        } catch (Exception e) {
            e.printStackTrace();
            return "无法获取到Brain版本号";
        }
    }

    // 获取系统的版本号
    public String getSystemVersion() {
        return "系统版本号:" + android.os.Build.VERSION.RELEASE;
    }

    /**
     * 将毫秒转化成固定格式的时间
     * 时间格式: yyyy-MM-dd HH:mm:ss
     *
     * @param millisecond
     */
    public static String getDateTimeFromMillisecond(Long millisecond) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    /**
     * 将毫秒转化成固定格式的时间
     * 时间格式: yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param millisecond
     */
    public static String getDateTimeFromMillisecond2(Long millisecond) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    /**
     * get the version code
     *
     * @param context
     */
    public static int getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int versionCode = info.versionCode;
            return versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * int 转成 四字节数组 高位在前 低位在后
     *
     * @param a
     */
    public static byte[] getByteArrayFromInt(int a) {
        byte[] result = new byte[4];
        result[0] = (byte) ((a >> 24) & 0xFF);
        result[1] = (byte) ((a >> 16) & 0xFF);
        result[2] = (byte) ((a >> 8) & 0xFF);
        result[3] = (byte) (a & 0xFF);
        return result;
    }

    /*
     * 判断当前语言环境是否是英语
	 */
    public static int getLanguageType() {
        int result = LANGUAGE_TYPE_EN;

        Locale locale = Application.getInstance().getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry().toLowerCase();
        LogMgr.d("language = " + language + " country = " + country);
        if (language.contains("en")) {
            LogMgr.d("英文环境");
            result = LANGUAGE_TYPE_EN;
        } else if (language.contains("zh")) {
            if (country.contains("cn")) {
                LogMgr.d("简体中文环境");
                result = LANGUAGE_TYPE_CN;
            } else if (country.contains("tw")) {
                LogMgr.d("繁体中文环境");
                result = LANGUAGE_TYPE_TW;
            } else {
                LogMgr.e("中文环境地区异常");
                result = LANGUAGE_TYPE_CN;
            }
        } else {
            LogMgr.d("其他语言");
            result = LANGUAGE_TYPE_OTHER;
        }
        return result;

//		if (language != null) {
//			if (language.endsWith("en"))
//				return true;
//			else
//				return false;
//		}
//		return false;
    }

    /**
     * 判断当前通信版本是否比特定版本高或相等
     *
     * @param appCommunicationVersion    当前的通讯版本号
     * @param certanCommunicationVersion 特定版本
     */
    public static boolean isLaterThanCertanCommunicationVersion(
            int[] appCommunicationVersion,
            int[] certanCommunicationVersion) {
        if (appCommunicationVersion == null || certanCommunicationVersion == null ||
                appCommunicationVersion.length != 4 || certanCommunicationVersion.length != 4) {
            LogMgr.e("isLaterThanCertanCommunicationVersion 参数错误");
            return false;
        }

        boolean result;

        if (appCommunicationVersion[0] > certanCommunicationVersion[0]) {
            result = true;
        } else if (appCommunicationVersion[0] == certanCommunicationVersion[0]
                && appCommunicationVersion[1] > certanCommunicationVersion[1]) {
            result = true;
        } else if (appCommunicationVersion[0] == certanCommunicationVersion[0]
                && appCommunicationVersion[1] == certanCommunicationVersion[1]
                && appCommunicationVersion[2] > certanCommunicationVersion[2]) {
            result = true;
        } else if (appCommunicationVersion[0] == certanCommunicationVersion[0]
                && appCommunicationVersion[1] == certanCommunicationVersion[1]
                && appCommunicationVersion[2] == certanCommunicationVersion[2]
                && appCommunicationVersion[3] >= certanCommunicationVersion[3]) {
            result = true;
        } else {
            result = false;
        }

        return result;
    }

    /*
    * 停止播放的音频；
    * 点击温度提醒图片时可以停止提示语音
    */
    public static void releaseMedia() {
        if (mediaPlayer != null) {
            LogMgr.d("释放mediaPlayer2");
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        System.gc();
    }

    /**
     * 播放音频文件
     *
     * @param context
     * @param fileNameZh 待播放的中文音频文件名
     * @param fileNameEn 待播放的英文音频文件名
     */
    public static void playSoundFile(Context context, String fileNameZh, String fileNameEn) {
        AssetManager assets = context.getAssets();
        final AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//		int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int currentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, GlobalConfig.MAX_VOL, 0);
        // 区分当前的语言环境
        String fileName;
        int languageType = getLanguageType();
        if (languageType == LANGUAGE_TYPE_CN || languageType == LANGUAGE_TYPE_TW) {
            fileName = fileNameZh;
        } else {
            fileName = fileNameEn;
        }
        LogMgr.d("playSoundFile() fileName = " + fileName + " currentVol = " + currentVol);
        try {
            AssetFileDescriptor fileDescriptor = assets.openFd(fileName);
            mediaPlayer = new MediaPlayer();

            mediaPlayer.reset();
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }

                    am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVol, 0);
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 判断当前语言环境是否是英语
     */
    public static boolean isEn() {
        Locale locale = Application.instance.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language != null) {
            if (language.endsWith("en")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * 获取 SDCard 总容量大小
     */
    public static long getExternalTotalSize() {
        String sdcard = Environment.getExternalStorageState();
        String state = Environment.MEDIA_MOUNTED;
        if (sdcard.equals(state)) {
            File file = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(file.getPath());
            //获得sdcard上 block的总数
            long blockCount = statFs.getBlockCountLong();
            //获得sdcard上每个block 的大小
            long blockSize = statFs.getBlockSizeLong();
            //计算标准大小使用：1024，当然使用1000也可以
            long blockTotalSize = blockCount * blockSize / 1024 / 1024;
            LogMgr.i("getExternalTotalSize() 总容量大小 blockTotalSize = " + blockTotalSize + " MB");
            return blockTotalSize;
        } else {
            LogMgr.e("getExternalTotalSize() 存储状态异常 sdcard = " + sdcard + " state = " + state);
            return -1;
        }
    }

    /**
     * 获取 SDCard 剩余容量大小
     */
    public static long getExternalAvailableSize() {
        String sdcard = Environment.getExternalStorageState();
        String state = Environment.MEDIA_MOUNTED;
        if (sdcard.equals(state)) {
            File file = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(file.getPath());
            //获得可供程序使用的Block数量
            long blockAvailable = statFs.getAvailableBlocksLong();
            //获得sdcard上每个block 的大小
            long blockSize = statFs.getBlockSizeLong();
            //计算标准大小使用：1024，当然使用1000也可以
            long blockAvailableSize = blockAvailable * blockSize / 1024 / 1024;
            LogMgr.i("getExternalAvailableSize() 剩余容量大小 blockAvailableSize = " + blockAvailableSize + " MB");
            return blockAvailableSize;
        } else {
            LogMgr.e("getExternalAvailableSize() 存储状态异常 sdcard = " + sdcard + " state = " + state);
            return -1;
        }
    }

    /**
     * 获取数据目录的可用大小
     */
    public static long getDataAvailableSize() {
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        // 获得Data上可供程序使用的Block数量
        long blockCount = statFs.getAvailableBlocksLong();
        // 获得Data上每个block 的大小
        long blockSize = statFs.getBlockSizeLong();
        // 计算标准大小使用：1024，当然使用1000也可以
        long blockTotalSize = blockCount * blockSize / 1024 / 1024;
        LogMgr.i("getDataAvailableSize() 剩余容量大小 blockAvailableSize = " + blockTotalSize + " MB");
        return blockTotalSize;
    }

    /**
     * 获取数据目录的总大小
     */
    public static long getDataTotalSize() {
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        // 获得Data上 block的总数
        long blockCount = statFs.getBlockCountLong();
        // 获得Data上每个block 的大小
        long blockSize = statFs.getBlockSizeLong();
        // 计算标准大小使用：1024，当然使用1000也可以
        long blockTotalSize = blockCount * blockSize / 1024 / 1024;
        LogMgr.i("getDataTotalSize() 总容量大小 blockTotalSize = " + blockTotalSize + " MB");
        return blockTotalSize;
    }

    /**
     * 判断当前应用是否在前台
     *
     * @param context
     */
    public static boolean isAppIsInBackground(Context context) {
        LogMgr.i("isAppIsInBackground()");
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            LogMgr.i("Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT + " runningProcesses.size() = " +
                    runningProcesses.size());
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                // 前台程序
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        LogMgr.i("在前台的应用名 = " + activeProcess);
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            LogMgr.i("Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                LogMgr.i("在前台的应用名 = " + componentInfo.getPackageName());
                isInBackground = false;
            }
        }

        return isInBackground;
    }


    /**
     * 获取文件的CRC值
     *
     * @param f
     */
    public static long getCRC32(File f) {
        try {
            FileInputStream in = new FileInputStream(f);
            CRC32 crc = new CRC32();
            byte[] bytes = new byte[1024];
            int byteCount;
            crc.reset();
            while ((byteCount = in.read(bytes)) > 0) {
                crc.update(bytes, 0, byteCount);
            }
            in.close();
            long sum = crc.getValue();
            return sum;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 接收到appstore命令 打开应用
     * 如果当前最上方应用为Brain，直接打开应用
     * 如果当前最上方应用为其他应用，发送广播至BrainSet，关闭最上方应用，打开目标应用
     *
     * @param context
     * @param packageName
     */
    public static void openAppForAppStore(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            LogMgr.e("包名错误");
            return;
        }
        if (!isAppInstalled(context, packageName)) {
            LogMgr.e("应用 " + packageName + " 未安装");
            byte[] returnCmd = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE,
                    GlobalConfig.APP_STORE_OUT_CMD_1, GlobalConfig.APP_STORE_OUT_CMD_2_OPEN_APP, new byte[]{(byte)
                            0x01});
            DataProcess.GetManger().sendMsg(returnCmd);
            return;
        }

        try {
            if (BrainActivity.getmBrainActivity() != null) {
                BrainActivity.getmBrainActivity().stopAnimaAndFuncFromOutside();
            }
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //打开赵辉那边应用前 先关闭掉串口
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                        long time = System.currentTimeMillis();
                        while (System.currentTimeMillis() - time < 1500) {
                        }
                    }
                    LogMgr.e("关闭串口");
                    BrainService.getmBrainService().getControlService().destorySP();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        if (BrainActivity.getmBrainActivity() != null && BrainActivity.getmBrainActivity().isForegroundFirst()) {
            //Brain在界面的最前方
            LogMgr.i("Brain在界面的最前方");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            openAppForAppStore2(context, packageName);
        } else {
            //Brain不在界面的最前方
            LogMgr.i("Brain不在界面的最前方");
            Intent intent = new Intent("com.abilix.brainset.openapp");
            intent.putExtra(INTENT_PACKAGE_NAME, packageName);
            intent.setPackage("com.abilix.brainset");
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(intent);

//			Intent intent = new Intent("com.abilix.brainset.openapp");
//			intent.setPackage("com.abilix.brainset");
//			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//			intent.putExtra(INTENT_PACKAGE_NAME, packageName);
//			context.sendBroadcast(intent);
        }

        byte[] returnCmd = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE,
                GlobalConfig.APP_STORE_OUT_CMD_1, GlobalConfig.APP_STORE_OUT_CMD_2_OPEN_APP, new byte[]{(byte) 0x00});
        DataProcess.GetManger().sendMsg(returnCmd);
    }

    /**
     * 接收到appstore命令 关闭应用
     * 送广播至BrainSet，关闭目标应用
     *
     * @param context
     * @param packageName
     */
    public static void closeAppForAppStore(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            LogMgr.e("包名错误");
            return;
        }
        if (!isAppInstalled(context, packageName)) {
            LogMgr.e("应用 " + packageName + " 未安装");
            byte[] returnCmd = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE,
                    GlobalConfig.APP_STORE_OUT_CMD_1, GlobalConfig.APP_STORE_OUT_CMD_2_CLOSE_APP, new byte[]{(byte) 0x01});
            DataProcess.GetManger().sendMsg(returnCmd);
            return;
        }
        Intent intent = new Intent("com.abilix.brainset.closeapp");
        intent.putExtra(INTENT_PACKAGE_NAME, packageName);
        intent.setPackage("com.abilix.brainset");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);

        byte[] returnCmd = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE,
                GlobalConfig.APP_STORE_OUT_CMD_1, GlobalConfig.APP_STORE_OUT_CMD_2_CLOSE_APP, new byte[]{(byte) 0x00});
        DataProcess.GetManger().sendMsg(returnCmd);
    }


    /**
     * 打开app 使用Intent.FLAG_ACTIVITY_NEW_TASK
     * 用于从Brain页面点击打开应用
     *
     * @param packageName
     * @param gotoWifiPage 打开设置时，是否打开wifi界面
     */
    public static void openApp(Context context, String packageName,String activityName, boolean gotoWifiPage) {
        LogMgr.i("Brain Utils openApp() packageName = " + packageName);
        if (!TextUtils.equals(packageName, GlobalConfig.APP_PKGNAME_BRAINSET)
                && !TextUtils.equals(packageName, GlobalConfig.APP_PKGNAME_UPDATEONLINETEST)
                && !TextUtils.equals(packageName, GlobalConfig.APP_PACKAGE_NAME_UPDATE_STM32_APK)) {
            //打开赵辉那边应用前 先关闭掉串口
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
                            long time = System.currentTimeMillis();
                            while (System.currentTimeMillis() - time < 1500) {
                            }
                        }
                        LogMgr.e("关闭串口");
                        if (BrainService.getmBrainService() != null & BrainService.getmBrainService().getControlService() != null) {
                            BrainService.getmBrainService().getControlService().destorySP();
                            //启动M系列小应用的时候需要发送状态值给他们
                            if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M
                                    && BrainService.getmBrainService().getMService() != null) {
                                BrainService.getmBrainService().getMService().handAction(MUtils.B_GO_TO_APP);
                                LogMgr.d("FZXX", "handAction(MUtils.B_GO_TO_APP)");
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            //打开设置应用 如果Control循环读取串口的功能被停止，发送一条命令打开
            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_OLD_PROTOCOL,
                    ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, (byte) 0x11, (byte) 0x09, null), null, 0, 0);
        }
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            PackageManager pm = context.getApplicationContext().getPackageManager();

            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = null;
            Iterator<ResolveInfo> it = apps.iterator();
            while (it.hasNext()){
                ri = it.next();
                LogMgr.d("==Utils==",ri.activityInfo.name);
                if (ri.activityInfo.packageName.equals(packageName) &&
                        activityName!=null && ri.activityInfo.name.equals(activityName)){
                    break;
                }
            }
            if (ri != null) {
                String pkName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (packageName.equals(GlobalConfig.APP_PKGNAME_BRAINSET) && gotoWifiPage) {
//                    forceStopApp(context, packageName);
                    intent.putExtra(GlobalConfig.BRAIN_SET_GOTO_WIFI_PAGE, gotoWifiPage);
                }

                ComponentName cn = new ComponentName(pkName, className);

//                if (!TextUtils.isEmpty(filePath)) {
//                    intent.putExtra(GlobalConfig.CHART_APP_FILE_PATH_EXTRA_NAME, filePath);
//                }
//
//                if (!TextUtils.isEmpty(pageName)) {
//                    intent.putExtra(GlobalConfig.CHART_APP_PAGE_NAME_EXTRA, pageName);
//                }

                intent.setComponent(cn);
//                if (GlobalConfig.CHART_AND_PROJECT_PROGRAM_APP_PACKAGE_NAME.equals(packageName)) {
//                    LogMgr.d("start explainer");
//                    ((BrainActivity) context).startActivityForResult(intent, BrainActivity
// .REQUEST_CODE_FOR_VJC_AND_PROGRAM_JROJECT);
//                } else {
                context.startActivity(intent);
//                }
            }
        } catch (NameNotFoundException e) {
            LogMgr.e("getPackInfo failed for package " + packageName);
        }
    }

    public static void forceStopApp(Context context, String packageName) {
        ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(activityMgr, packageName);  //packageName是需要强制停止的应用程序包名
        } catch (Exception e) {
            LogMgr.e("关闭应用异常" + packageName);
            e.printStackTrace();
        }
    }

    public static void openAppChart(Context context, String packageName, String filePath, String pageName) {
        LogMgr.i("Brain Utils openApp() packageName = " + packageName);
        try {
            Intent intent = new Intent(context, com.abilix.explainer.view.MainActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (!TextUtils.isEmpty(filePath)) {
                intent.putExtra(GlobalConfig.CHART_APP_FILE_PATH_EXTRA_NAME, filePath);
            }

            if (!TextUtils.isEmpty(pageName)) {
                intent.putExtra(GlobalConfig.CHART_APP_PAGE_NAME_EXTRA, pageName);
            }

            context.startActivity(intent);
//                }
        } catch (Exception e) {
            LogMgr.e("getPackInfo failed for package " + packageName);
        }
    }

    /**
     * 是否安装了应用
     *
     * @param context
     * @param packagename
     */
    public static boolean isAppInstalled(Context context, String packagename) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            // System.out.println("没有安装");
            return false;
        } else {
            // System.out.println("已经安装");
            return true;
        }
    }

    /**
     * 打开app 使用Intent.FLAG_ACTIVITY_NEW_TASK
     * 用于打开appStore要求打开的应用
     *
     * @param context
     * @param packageName 包名
     */
    public static void openAppForAppStore2(Context context, String packageName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            PackageManager pm = context.getApplicationContext().getPackageManager();

            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                String pkName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                ComponentName cn = new ComponentName(pkName, className);

                intent.setComponent(cn);
                context.startActivity(intent);
                LogMgr.e("打开应用packageName = " + packageName);
            }
        } catch (NameNotFoundException e) {
            LogMgr.e("getPackInfo failed for package " + packageName);
        }
    }

    /**
     * 校验
     */
    public static byte check(byte[] bs) {
        byte b = 0;
        for (int i = 8; i < bs.length - 3; i++) {
            b += bs[i];
        }
        b = (byte) ~b;
        b = (byte) (b & 0xFF);
        return b;
    }

    /**
     * 发送广播 在即将安装某个应用前1.5秒
     *
     * @param context
     * @param action
     */
    public static void sendBroadcastBeforeInstallApk(Context context, String action, String packageName) {
        Intent intent = new Intent(action);
        intent.putExtra(INTENT_PACKAGE_NAME_TO_INSTALL, packageName);
        context.sendBroadcast(intent);
    }

    /**
     * 发送广播
     *
     * @param context
     * @param action
     */
    public static void sendBroadcast(Context context, String action) {
        LogMgr.i("发送广播 action = " + action);
        Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }

    /**
     * arrayA 是否包含 arrayB
     *
     * @param arrayA
     * @param arrayB
     * @return 不包含时返回 -1，包含时返回arrayB的起始位置
     */
    public static int isArrayAContainsArrayB(byte[] arrayA, byte[] arrayB) {
        if (arrayA == null || arrayB == null || arrayA.length < arrayB.length) {
            return -1;
        }
        int result = -1;
        for (int i = 0; i <= arrayA.length - arrayB.length; i++) {
            for (int j = 0; j < arrayB.length; j++) {
                if (arrayA[i + j] != arrayB[j]) {
                    break;
                } else {
                    if (j == arrayB.length - 1) {
                        result = i;
                        break;
                    }
                }
            }
            if (result >= 0) {
                break;
            }
        }
        return result;
    }

    /**
     * 时间 long类型转换为String类型
     *
     * @param currentTime
     */
    public static String longToString(long currentTime) {
        Date date = new Date(currentTime);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * 创建并展示单按钮窗口
     *
     * @param context
     * @param title           窗口标题
     * @param message         窗口信息
     * @param buttonString    按钮文字
     * @param isSystemDialog  是否系统窗口
     * @param onClickListener 按钮点击事件
     */
    public static AlertDialog showSingleButtonDialog(Context context, String title, String message, String
            buttonString, boolean isSystemDialog,
                                                     final OnClickListener onClickListener) {
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.dialog).create();
        if (isSystemDialog) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.single_button_dialog);

        RelativeLayout relativeLayout = (RelativeLayout) window.findViewById(R.id.rl_single_button_dialog);
        TextView tv_title = (TextView) window.findViewById(R.id.tv_single_dialog_title);
        TextView tv_message = (TextView) window.findViewById(R.id.tv_single_dialog_message);
        tv_message.setMovementMethod(ScrollingMovementMethod.getInstance());
        Button btn = (Button) window.findViewById(R.id.btn_single_dialog);
        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
            tv_title.setTextColor(Color.BLACK);
            tv_message.setTextColor(Color.BLACK);
            btn.setTextColor(Color.WHITE);
        }

        relativeLayout.setBackgroundResource(BrainUtils.getButtonDialogBackgroundDrawableResource());
        tv_title.setText(title);
        tv_message.setText(message);
        btn.setText(buttonString);
        btn.setBackgroundResource(BrainUtils.getSingleButtonDialogButtonBackgroundDrawableResource());
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onClickListener.onClick(v);
                dialog.dismiss();
            }
        });

        return dialog;
    }


    /**
     * 创建并展示双按钮窗口
     *
     * @param context
     * @param title                      窗口标题
     * @param message                    窗口信息
     * @param leftButtonString           左按钮文字
     * @param rightButtonString          右按钮文字
     * @param isSystemDialog             是否系统窗口
     * @param leftButtonOnClickListener  左按钮点击事件
     * @param rightButtonOnClickListener 右按钮点击事件
     */
    public static AlertDialog showTwoButtonDialog(Context context, String title, String message, String
            leftButtonString, String rightButtonString, boolean isSystemDialog,
                                                  final OnClickListener leftButtonOnClickListener, final
                                                  OnClickListener rightButtonOnClickListener) {
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.dialog).create();
        if (isSystemDialog) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.two_button_dialog);

        RelativeLayout relativeLayout = (RelativeLayout) window.findViewById(R.id.rl_two_button_dialog);
        TextView tv_title = (TextView) window.findViewById(R.id.tv_two_dialog_title);
        TextView tv_message = (TextView) window.findViewById(R.id.tv_two_dialog_message);
        tv_message.setMovementMethod(ScrollingMovementMethod.getInstance());
        Button btn_left = (Button) window.findViewById(R.id.btn_left_two_dialog);
        Button btn_right = (Button) window.findViewById(R.id.btn_right_two_dialog);

        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
            tv_title.setTextColor(Color.BLACK);
            tv_message.setTextColor(Color.BLACK);
            btn_left.setTextColor(Color.WHITE);
            btn_right.setTextColor(Color.WHITE);
        }
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
            relativeLayout.setBackground(null);
        } else {
            relativeLayout.setBackgroundResource(BrainUtils.getButtonDialogBackgroundDrawableResource());
        }

        tv_title.setText(title);
        tv_message.setText(message);
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
            btn_left.setBackgroundResource(R.drawable.m_wrong);
            btn_right.setBackgroundResource(R.drawable.m_right);
        } else {
            btn_left.setText(leftButtonString);
            btn_left.setBackgroundResource(BrainUtils.getTwoButtonDialogButtonBackgroundDrawableResource(0));
            btn_right.setText(rightButtonString);
            btn_right.setBackgroundResource(BrainUtils.getTwoButtonDialogButtonBackgroundDrawableResource(1));
        }


        btn_left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                leftButtonOnClickListener.onClick(v);
                dialog.dismiss();
            }
        });
        btn_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rightButtonOnClickListener.onClick(v);
                dialog.dismiss();
            }
        });

        return dialog;
    }


    /**
     * 创建并展示无按钮窗口
     *
     * @param context
     * @param title          窗口标题
     * @param message        窗口信息
     * @param isSystemDialog 是否系统窗口
     */
    public static AlertDialog showNoButtonDialog(Context context, String title, String message, boolean
            isSystemDialog) {
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.dialog).create();
        if (isSystemDialog) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.no_button_dialog);

        RelativeLayout relativeLayout = (RelativeLayout) window.findViewById(R.id.rl_no_button_dialog);
        TextView tv_title = (TextView) window.findViewById(R.id.tv_no_button_dialog_title);
        TextView tv_message = (TextView) window.findViewById(R.id.tv_no_button_dialog_message);
        tv_message.setMovementMethod(ScrollingMovementMethod.getInstance());

        if (GlobalConfig.BRAIN_CHILD_TYPE == GlobalConfig.ROBOT_TYPE_CU) {
            tv_title.setTextColor(Color.BLACK);
            tv_message.setTextColor(Color.BLACK);
        }
        relativeLayout.setBackgroundResource(BrainUtils.getNoButtonDialogBackgroundDrawableResource());
        tv_title.setText(title);
        tv_message.setText(message);

        return dialog;

//    	final AlertDialog dialog = new AlertDialog.Builder(context).create();
//    	if(isSystemDialog){
//    		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//    	}
//		dialog.setCancelable(false);
//		dialog.setCanceledOnTouchOutside(false);
//		dialog.show();
//		Window window = dialog.getWindow();  
//		window.setContentView(R.layout.single_button_dialog);  
//		
//		RelativeLayout relativeLayout = (RelativeLayout)window.findViewById(R.id.rl_single_button_dialog);
//		TextView tv_title = (TextView)window.findViewById(R.id.tv_single_dialog_title);
//		TextView tv_message = (TextView)window.findViewById(R.id.tv_single_dialog_message);
//		Button btn = (Button)window.findViewById(R.id.btn_single_dialog);
//		
//		relativeLayout.setBackgroundResource(BrainUtils.getButtonDialogBackgroundDrawableResource());
//		tv_title.setText(title);
//		tv_message.setText(message);
//		btn.setText(title);
//		btn.setBackgroundResource(BrainUtils.getSingleButtonDialogButtonBackgroundDrawableResource());
//		
//		return dialog;
    }

    /**
     * 创建并展示无按钮进度条窗口
     *
     * @param context
     * @param title          窗口标题
     * @param message        窗口信息
     * @param isSystemDialog 是否系统窗口
     */
    public static AlertDialog showNoButtonProgressDialog(Context context, String title, String message, boolean isSystemDialog) {
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.dialog).create();
        if (isSystemDialog) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();

//        WindowManager.LayoutParams winParams = window.getAttributes();
//        winParams.gravity = Gravity.CENTER_VERTICAL;
//        winParams.flags |= GrandarUtils.FLAG_HOMEKEY_DISPATCHED;
//        window.setAttributes(winParams);

        window.setContentView(R.layout.no_button_progress_dialog);

        RelativeLayout relativeLayout = (RelativeLayout) window.findViewById(R.id.rl_no_button_progress_dialog);
        TextView tv_title = (TextView) window.findViewById(R.id.tv_no_button_progress_dialog_title);
        TextView tv_message = (TextView) window.findViewById(R.id.tv_no_button_progress_dialog_message);
        SeekBar seekBar = (SeekBar) window.findViewById(R.id.progressbar_no_button_progress_dialog);

        relativeLayout.setBackgroundResource(BrainUtils.getNoButtonDialogBackgroundDrawableResource());
        tv_title.setText(title);
        tv_message.setText(message);
        seekBar.setProgressDrawable(context.getResources().getDrawable(BrainUtils.getProgressBackgroundDrawableResource()));
        seekBar.setThumb(context.getResources().getDrawable(BrainUtils.getProgressThumbDrawableResource()));

        return dialog;
    }

    public static String getLocalMacAddressFromWifiInfo(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }


    /**
     * 将String 类型的mac地址转换为６位的byte数组,如mac为:"00:13:e4:c4:4c:67"
     * @param mac
     * @return
     */
    public static byte [] getMacBytes(String mac){
        byte []macBytes = new byte[6];
        String [] strArr = mac.split(":");

        for(int i = 0;i < strArr.length; i++){
            int value = Integer.parseInt(strArr[i],16);
            macBytes[i] = (byte) value;
        }

        byte bytes[] = new byte[macBytes.length + 2];
        bytes[0] = (byte) 0x02;
        bytes[1] = (byte) macBytes.length;
        System.arraycopy(macBytes , 0, bytes,2, macBytes.length);
        return bytes;
    }

    /**
     * 将６位的byte数组mac地址，转为"xx:xx:xx:xx:xx:xx"形式的mac串
     * @param macBytes
     * @return
     */
    public static String getMacString(byte[] macBytes){
        String value = "";
        for(int i = 0;i < macBytes.length; i++){
            String sTemp = Integer.toHexString(0xFF &  macBytes[i]);
            value = value+sTemp+":";
        }

        value = value.substring(0,value.lastIndexOf(":"));
        return value;
    }



    /**
     * 将10进制转化为62进制
     *
     * @param number
     * @param length 转化成的62进制长度，不足length长度的话高位补0，否则不改变什么
     */
    public static String d10To62(long number, int length) {
        Long rest = number;
        Stack<Character> stack = new Stack<Character>();
        StringBuilder result = new StringBuilder(0);
        while (rest != 0) {
            stack.add(charSet[new Long((rest - (rest / 62) * 62)).intValue()]);
            rest = rest / 62;
        }
        for (int i = 0; !stack.isEmpty() && i < length; i++) {
            result.append(stack.pop());
        }
        int result_length = result.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - result_length; i++) {
            sb.append('0');
        }
        return sb.toString() + result.toString();
    }

    /**
     * 生成62进制的热点后缀
     *
     * @param length 生成的后缀长度
     */
    @NonNull
    public static String createRandomHotSSidSuffix(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(charSet[random.nextInt(charSet.length)]);
        }
        return sb.toString();
    }

    /**
     * 创建并展示无按钮窗口
     *
     * @param context
     * @param isSystemDialog 是否系统窗口
     */
    public static AlertDialog showNoButtonDialogTempH(Context context, boolean isSystemDialog, final OnClickListener
            ClickListener) {
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.WelcomeStyle).create();
        if (isSystemDialog) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Window window = dialog.getWindow();
//        window.setLayout(320,320);
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        WindowManager.LayoutParams lp = window.getAttributes();
//        window.setGravity(Gravity.CENTER);
//        lp.x = 0;
//        lp.y = 0;
//        lp.width = 320;
//        lp.height = 320;
//        window.setAttributes(lp);
        window.setContentView(R.layout.no_button_dialog_heitmep);

        ImageView heitemp = (ImageView) window.findViewById(R.id.rl_no_button_dialog_heitemp);
        heitemp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickListener.onClick(v);
//                if(mediaPlayer.isPlaying() && mediaPlayer != null){
//                    mediaPlayer.stop();
//                    mediaPlayer.release();
//                }
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static boolean appIsDebugable(Application application) {

        try {
            ApplicationInfo info = application.getApplicationInfo();

            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String showDataHex(byte[] data) {//将接收或者发送的数据转换为16进制String
        String str0 = "";
        int v;
        String hv = "";
        for (int i = 0; i < data.length; i++) {
            v = data[i] & 0xFF;
            if (v <= 0x0f) {
                hv = hv + " 0" + Integer.toHexString(v);
            } else {
                hv = hv + " " + Integer.toHexString(v);
            }
        }

        return str0 + hv + " \n ";
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getWindowWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getWindowHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        return height;
    }

    /**
     * 获取本机IP
     *
     * @return
     */
    public static String getIpAddress() {
        for (int i = 0; i < 10; i++) {
            String sIp = BrainUtils.getLocalIpAddress();
            String sIp1 = WifiUtils.getLocalIpAddress();
            if (sIp != null) {
                return sIp;
            } else if (sIp1 != null) {
                return sIp1;
            }
        }
        LogMgr.e("getIpAddress()获取10次以上，没有获取到正确本机IP");
        return null;
    }

//    /**
//     * 获取系统亮度
//     * @param context
//     * @return
//     */
//    public static int getSystemBrightness(Context context) {
//        int value = 0;
//        ContentResolver cr = context.getContentResolver();
//        try {
//            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
//        } catch (Settings.SettingNotFoundException e) {
//
//        }
//        return value;
//    }

//    public static int getSystemBrightMode(Activity activity){
//        int value = 0;
//        ContentResolver cr = activity.getContentResolver();
//        try {
//            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE);
//        } catch (Settings.SettingNotFoundException e) {
//
//        }
//        return value;
//    }

//    /**
//     * 设置屏幕亮度
//     * @param activity
//     * @param value
//     */
//    public static void setScreenBrightness(Activity activity, @IntRange(from = 0,to = 255) int value) {
//        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
//        params.screenBrightness = value / 255f;
//        activity.getWindow().setAttributes(params);
//    }

//    /**
//     * 获取屏幕亮度
//     * @param activity
//     */
//    public static float getScreenBrightness(Activity activity) {
//        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
//        return params.screenBrightness;
//    }

    /**
     * 获取产品系列号
     *
     * @return
     */
    public static String getProductSerial(String buildDisplay) {
        String result = "";
        try {
            String[] splitResult = buildDisplay.split("_");
            if (splitResult != null && splitResult.length >= 3) {
                result = splitResult[0];
            } else {
                result = buildDisplay.charAt(0) + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = buildDisplay.charAt(0) + "";
        }
        LogMgr.i("产品系列号 = " + result);
        return result;
    }

    /**
     * 获取核心版本号
     *
     * @return
     */
    public static int getCoreVersionNumber(String buildDisplay) {
        int result = -1;
        try {
            String[] splitResult = buildDisplay.split("_");
            if (splitResult != null && splitResult.length >= 3) {
                String middleResult = splitResult[1];
                String[] splitResult2 = middleResult.split("\\.");
                if (splitResult2 != null && splitResult2.length >= 3) {
                    result = Integer.valueOf(splitResult2[0]);
                } else {
                    result = -1;
                }
            } else {
                result = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }
        LogMgr.i("核心版本号 = " + result);
        return result;
    }

    /**
     * 获取功能版本号
     *
     * @return
     */
    public static int getFunctionVersionNumber(String buildDisplay) {
        int result = -1;
        try {
            String[] splitResult = buildDisplay.split("_");
            if (splitResult != null && splitResult.length >= 3) {
                String middleResult = splitResult[1];
                String[] splitResult2 = middleResult.split("\\.");
                if (splitResult2 != null && splitResult2.length >= 3) {
                    result = Integer.valueOf(splitResult2[1]);
                } else {
                    result = -1;
                }
            } else {
                result = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }
        LogMgr.i("功能版本号 = " + result);
        return result;
    }

    /**
     * 获取升级版本号
     *
     * @return
     */
    public static int getUpdateVersionNumber(String buildDisplay) {
        int result = -1;
        try {
            String[] splitResult = buildDisplay.split("_");
            if (splitResult != null && splitResult.length >= 3) {
                String middleResult = splitResult[1];
                String[] splitResult2 = middleResult.split("\\.");
                if (splitResult2 != null && splitResult2.length >= 3) {
                    result = Integer.valueOf(splitResult2[2]);
                } else {
                    result = -1;
                }
            } else {
                result = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }
        LogMgr.i("升级版本号 = " + result);
        return result;
    }

    /**
     * 获取编译版本号
     *
     * @return
     */
    public static String getCompileVersionNumber(String buildDisplay) {
        String result = "";
        try {
            String[] splitResult = buildDisplay.split("_");
            if (splitResult != null && splitResult.length >= 3) {
                result = splitResult[2];
            } else {
                result = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = "";
        }
        LogMgr.i("编译版本号 = " + result);
        return result;
    }

//    public static void getAll(){
//        Utils.getProductSerial(GlobalConfig.ROBOT_BUILD_TYPE);
//        Utils.getCoreVersionNumber(GlobalConfig.ROBOT_BUILD_TYPE);
//        Utils.getFunctionVersionNumber(GlobalConfig.ROBOT_BUILD_TYPE);
//        Utils.getUpdateVersionNumber(GlobalConfig.ROBOT_BUILD_TYPE);
//        Utils.getCompileVersionNumber(GlobalConfig.ROBOT_BUILD_TYPE);
//        LogMgr.i("GlobalConfig.BRAIN_TYPE = "+GlobalConfig.BRAIN_TYPE);
//    }

    public static int bytesToInt4(byte[] bytes, int begin) {// 四字节转换为int
        // 低位在前高位在后
        return (int) ((int) 0xff & bytes[begin])
                | (((int) 0xff & bytes[begin + 1]) << 8)
                | (((int) 0xff & bytes[begin + 2]) << 16)
                | (((int) 0xff & bytes[begin + 3]) << 24);
}
    public static long bytesToLong8HL(byte[] bytes, int begin) {// 8字节转换为float
        // 高位在前低位在后
        long longdata = ((long) 0xff & bytes[begin + 7])
                | (((long) 0xff & bytes[begin + 6]) << 8)
                | (((long) 0xff & bytes[begin + 5]) << 16)
                | (((long) 0xff & bytes[begin + 4]) << 24)
                | (((long) 0xff & bytes[begin + 3]) << 32)
                | (((long) 0xff & bytes[begin + 2]) << 40)
                | (((long) 0xff & bytes[begin + 1]) << 48)
                | (((long) 0xff & bytes[begin]) << 56);
        return longdata;
    }
    public static long bytesToLong8(byte[] bytes, int begin) {// 8字节转换为float
        // 低位在前高位在后
        long longdata = ((long) 0xff & bytes[begin])
                | (((long) 0xff & bytes[begin + 1]) << 8)
                | (((long) 0xff & bytes[begin + 2]) << 16)
                | (((long) 0xff & bytes[begin + 3]) << 24)
                | (((long) 0xff & bytes[begin + 4]) << 32)
                | (((long) 0xff & bytes[begin + 5]) << 40)
                | (((long) 0xff & bytes[begin + 6]) << 48)
                | (((long) 0xff & bytes[begin + 7]) << 56);
        return longdata;
    }

    public static String bytesToString(byte[] bytes, int begin, int end) {// byte转化为string
        int len = end - begin + 1;
        byte[] str = new byte[len];
        System.arraycopy(bytes, begin, str, 0, len);
        return new String(str);
    }

    public static int bytesToInt1(byte[] bytes, int begin) {// 一个字节转int
        return (int) ((bytes[begin] & 0xff));
    }

//    public static int bytesToInt2(byte[] bytes, int begin) {// 两个字节转化为int
//        // 低位在前高位在后
//        return (int) (0x00ff & bytes[begin])
//                | ((0x00ff & bytes[begin + 1]) << 8);
//    }

//    public static int bytesToInt3(byte[] bytes, int begin) {// 三字节转换为int
//        // 低位在前高位在后
//        return (int) (0x0000ff & bytes[begin])
//                | ((0x0000ff & bytes[begin + 1]) << 8)
//                | ((0x0000ff & bytes[begin + 2]) << 16);
//    }

    /* short 2字节、int 4字节、float 4字节、long 8字节。 */


    public static short bytesToShort2(byte[] bytes, int begin) {// 两个字节转化为short
        // 低位在前高位在后
        return (short) (((short) 0xff & bytes[begin]) | (((short) 0xff & bytes[begin + 1]) << 8));
    }

    public static float bytesToFloat4(byte[] bytes, int begin) {// 四字节转换为float
        // 低位在前高位在后
        int floatInt = Utils.bytesToInt4(bytes, begin);// 先转换为int型，再转换为float
        return Float.intBitsToFloat(floatInt);
    }



    public static byte IntTobyte(int data, int allNum, int byteNum) {// int转换为字节
        // 低位在前高位在后
        // ,
        // data数据，allNum转化成的字节个数，byteNum第几个字节
        if (allNum < 1 || byteNum < 1) {
            return (byte) 0x00;
        }
        int bytedata = 0;
        switch (allNum) {
            case 4:
                return (byte) (data >> (8 * (byteNum - 1)));
            case 3:
                return (byte) (data >> (8 * (byteNum - 1)));
            case 2:
                return (byte) (data >> (8 * (byteNum - 1)));
            case 1:
                return (byte) (data >> (8 * (byteNum - 1)));
            default:
                return (byte) 0x00;
        }
    }

    public static int FloatToInt(float fdata) {
        return Float.floatToIntBits(fdata);
    }

    public static byte[] FloatTobyte(float fdata) {// float转换为字节 高位在前低位在后 ,
        // data数据，allNum转化成的字节个数，byteNum第几个字节
        // 把float转换为byte[]
        int fbit = FloatToInt(fdata);
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }
        return b;
    }

    public static byte[] LongTobyte(long dataD) {// 低字节在前
        byte[] data = new byte[8];
        for (int i = 0; i < 8; i++) {
            data[i] = (byte) (dataD >> (8 * i));
        }
        return data;
    }

    /**
	 * 去掉文件名后缀
	 *
	 * @param n
	 * @return
	 */
	private static String getExtensionName(String n) {
		if (n != null && n.length() > 0) {
			int dot = n.lastIndexOf(".");
			if ((dot > -1) && (dot < (n.length() - 1))) {
				return n.substring(0, dot);
			}
		}
		return n;
	}

    private static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
    public static String getNowTime() {
        long sysTime = System.currentTimeMillis();
        Date currentTime = new Date(sysTime);
        return " " + formatter.format(currentTime);
    }
}

