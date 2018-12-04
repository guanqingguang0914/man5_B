package com.abilix.brain.data;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.abilix.brain.utils.LogMgr;

import android.os.Environment;
import android.util.Log;

/**
 * 没有实际使用的类。
 * @deprecated 不建议在这个类中添加任何功能
 */
public class DataBuffer {
    public static String showtext = "hello world";//服务器是否连接
    public static String showtext0 = "";//服务器是否连接
    /*记录发送时的数据和解析时的数据*/
    public static boolean showSaveData = false;// 是否显示数据
    public static String showAllData = "";// 显示所有通信收发的数据
    public static String showAllDataSave = "";// 保存核心板所有通信的数据
    public static String showMobile = "";// 显示和手机端通信收发的数据
    public static String showI2CPort = "";// 显示I2C和核心板的收发数据
    public static String showSPTPort = "";// 显示核心板和串口通信的收发数据
    public static String showSPTPort0 = "";// 显示核心板和串口通信的收发数据

    public static short Infrared = 0;
    //添加群控对应的
    public static String serverIP = null;
    public static boolean hasclient = false;
    public static boolean StopMusic = true;
    public static boolean stopSction = true;
    public final static String DATA_PATH = Environment.getExternalStorageDirectory() + File.separator + "Download";
    public static List<String> fileList = new ArrayList<String>();

//	public static boolean musicRefresh=false;//监控客户端是否发送音乐控制指令，以用来控制音乐数据的更新

    public static int mslistDataSize = 0;
    public static ArrayList<HashMap<String, String>> mslistData = new ArrayList<HashMap<String, String>>();//歌曲信息列表
    public static int musicVolume = 0;//当前播放的音量
    public static int musicIndex = 0;//当前播放歌曲ID
    public static int musicState = 0;//当前播放状态 0 0、停止0x00  1、暂停0x01    2、播放0x02
    public static int musicModel = 0x06;//当前播放模式      5、单曲循环0x05  6、循环播放0x06   7、随机播放0x07
    public static int musicProgress = 0;//当前播放歌曲进度
    public static int musicSize = 0;//当前播放的总时间长度
    public static String musicName = "";//当前播放的歌曲名称

    public static int specifiedVolume = 0;//指定播放的音量
    public static int specifiedIndex = 0;//指定播放的歌曲ID
    public static int specifiedState = 0;//当前播放状态 0 0、停止0x00  1、暂停0x01    2、播放0x02
    public static int specifiedModel = 0x06;//当前播放模式      5、单曲循环0x05  6、循环播放0x06   7、随机播放0x07
    public static int specifiedUpDown = 0;//指定播放歌曲上一首下一首   3、播放上一首歌曲0x03  4、播放下一首歌曲0x04
    public static int specifiedProgress = 0;//指定播放的进度

    public static byte[] getmusicIdProgress() {
        byte musicIndexLow = (byte) (musicIndex & 0xff);
        byte musicIndexHigh = (byte) ((musicIndex & 0xff00) >> 8);
        return new byte[]{musicIndexLow, musicIndexHigh, (byte) musicProgress, (byte) musicState, (byte) musicModel};
    }

    public static byte[] getmusicInfo(int musicid, int time, String name) {
        byte idLow = (byte) (musicid & 0xff);
        byte idHigh = (byte) ((musicid & 0xff00) >> 8);
        byte timeLow = (byte) (time & 0xff);
        byte timeHigh = (byte) ((time & 0xff00) >> 8);
        byte[] nameByte = null;
//		nameByte = name.getBytes();
        try {
            nameByte = name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogMgr.i("DataBuffer", "汉字转换为UTF_8出错" + e);
        }//将str转换为字节数组
        int namelen = nameByte.length;
        byte[] musicData = new byte[namelen + 4];

        System.arraycopy(nameByte, 0, musicData, 4, namelen);
        musicData[0] = idLow;
        musicData[1] = idHigh;
        musicData[2] = timeLow;
        musicData[3] = timeHigh;
        return musicData;
    }
    
    public static void getFileList(File path, List<String> fileList) {// 获取指定路径下的文件信息
        if (!path.exists()) {
            try {
                path.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (path.isDirectory()) {// 如果是文件夹的话
            // 返回文件夹中有的数据
            File[] files = path.listFiles();
            // 先判断下有没有权限，如果没有权限的话，就不执行了
            if (null == files) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                getFileList(files[i], fileList);
            }
        } else {// 如果是文件的话直接加入
            // 进行文件的处理
            String filePath = path.getAbsolutePath();
            // 文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            // fileName=getFileNameNoEx(fileName);
            if (!fileList.contains(fileName)) {
                fileList.add(fileName);// list
            }
        }
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}
