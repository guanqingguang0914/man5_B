package com.abilix.explainer.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.text.TextUtils;

public class FileUtils {
    public static final String TAG = "FileUtils";
    /**
     * 文件类型
     */
    public static final String _TYPE_JPEG = ".jpg";
    public static final String _TYPE_GIF = ".gif";
    public static final String _TYPE_PNG = ".png";
    public static final String _TYPE_BMP = ".bmp";

    public static final String _TYPE_MP3 = ".mp3";
    public static final String _TYPE_3GPP = ".3gp";
    public static final String _TYPE_WAVE = ".wav";

    public static final String _TYPE_BIN = ".bin";
    public static final String _TYPE_ELF = ".elf";


    /**
     * "/sdcard"
     */
    private static final String DIR_EXTERNAL_STORAGE_ROOT = Environment.getExternalStorageDirectory().getPath();
    /**
     * "/sdcard/Abilix"
     */
    private static final String DIR_ABILIX = getDirectory(DIR_EXTERNAL_STORAGE_ROOT, "Abilix");
    /**
     * "/sdcard/Abilix"
     */
    public static final String DIR_ABILIX_CHART = getDirectory(DIR_ABILIX, "AbilixChart");
    public static final String DIR_ABILIX_MUSIC = getDirectory(DIR_ABILIX, "AbilixMusic");
    public static final String DIR_ABILIX_PROJECT_PROGRAM = getDirectory(DIR_ABILIX, "AbilixProjectProgram");
    public static final String DIR_ABILIX_SCRATCH = getDirectory(DIR_ABILIX, "AbilixScratch");
    public static final String DIR_ABILIX_MEDIA = getDirectory(DIR_ABILIX, "media");
    public static final String DIR_ABILIX_ROBOT_INFO = getDirectory(DIR_ABILIX, "RobotInfo");
    public static final String DIR_ABILIX_UPDATE_FILE = getDirectory(DIR_ABILIX, "UpdateFile");
    public static final String DIR_ABILIX_UPDATE_ONLINE = getDirectory(DIR_ABILIX, "UpdateOnline");
    public static final String DIR_ABILIX_SYSTEM_APP = getDirectory(DIR_ABILIX, "system-apps");
    public final static String DIR_ABILIX_MOVE_BIN = getDirectory(DIR_ABILIX, "MoveBin");


    /**
     * 音乐文件目录
     * "/sdcard/Abilix/AbilixMusic"
     */
    public static final String DIR_ABILIX_MUSIC_C = getDirectory(DIR_ABILIX_MUSIC, "music_c");
    public static final String DIR_ABILIX_MUSIC_H = getDirectory(DIR_ABILIX_MUSIC, "music_h");
    public static final String DIR_ABILIX_MUSIC_M = getDirectory(DIR_ABILIX_MUSIC, "music_m");
    public static final String DIR_ABILIX_MUSIC_S = getDirectory(DIR_ABILIX_MUSIC, "music_s");
    public static final String DIR_ABILIX_MUSIC_F = getDirectory(DIR_ABILIX_MUSIC, "music_f");
    public static final String DIR_ABILIX_MUSIC_AF = getDirectory(DIR_ABILIX_MUSIC, "music_af");

    /**
     * 多媒体目录
     * "/sdcard/Abilix/media"
     */
    public static final String DIR_MEDIA_DEFAULT = getDirectory(DIR_ABILIX_MEDIA, "default");
    public static final String DIR_MEDIA_UPLOAD = getDirectory(DIR_ABILIX_MEDIA, "upload");

    /**
     * 公共多媒体目录
     * "/sdcard/Abilix/media/default"
     */
    public static final String DIR_MEDIA_DEFAULT_IMAGE = getDirectory(DIR_MEDIA_DEFAULT, "image");
    public static final String DIR_MEDIA_DEFAULT_VIDEO = getDirectory(DIR_MEDIA_DEFAULT, "video");
    public static final String DIR_MEDIA_DEFAULT_AUDIO = getDirectory(DIR_MEDIA_DEFAULT, "audio");
    //KU动画
    public final static String DIR_DEFAULT_IMAGE_ANIM_KU = getDirectory(DIR_MEDIA_DEFAULT_IMAGE, "anim_ku");


    /**
     * 用户自定义多媒体目录
     * "/sdcard/Abilix/media/upload"
     */
    public static final String DIR_MEDIA_UPLOAD_IMAGE = getDirectory(DIR_MEDIA_UPLOAD, "image");
    public static final String DIR_MEDIA_UPLOAD_VIDEO = getDirectory(DIR_MEDIA_UPLOAD, "video");
    public static final String DIR_MEDIA_UPLOAD_AUDIO = getDirectory(DIR_MEDIA_UPLOAD, "audio");


    /**
     * 机器人信息目录
     * "/sdcard/Abilix/RobotInfo"
     */
    public static final String DIR_ABILIX_PHOTO = getDirectory(DIR_ABILIX_ROBOT_INFO, "Photo");
    public static final String DIR_ABILIX_RECORD = getDirectory(DIR_ABILIX_ROBOT_INFO, "Record");

    public static final String AI_ENVIRONMENT1 = getFilePath(DIR_ABILIX_ROBOT_INFO, "environment1.txt");
    public static final String AI_ENVIRONMENT2 = getFilePath(DIR_ABILIX_ROBOT_INFO, "environment2.txt");
    public static final String IO_CONFIG = getFilePath(DIR_ABILIX_ROBOT_INFO, "ioconfig.txt");


    /**
     * 获取文件夹路径
     *
     * @param parent 上级目录
     * @param child  文件夹名
     * @return 文件夹路径[不带后缀'/']
     */
    public static String getDirectory(String parent, String child) {
        return getFilePath(parent, child);
    }

    /**
     * 获取文件路径
     *
     * @param parent   上级目录
     * @param fileName 文件名
     * @return 文件路径
     */
    public static String getFilePath(String parent, String fileName) {
        String path = parent;
        if (!TextUtils.isEmpty(parent) && !parent.endsWith(File.separator)) {
            path += File.separator;
        }
        path += fileName;
        return path;
    }

    public static String getFilePath(String parent, String fileName, String fileType) {
        return getFilePath(parent, fileName) + fileType;
    }

    /**
     * 获取文件
     *
     * @param parent   上级目录
     * @param fileName 文件名
     * @return 文件对象
     */
    public static File getFile(String parent, String fileName) {
        String path = getFilePath(parent, fileName);
        return new File(path);
    }

    public static File getFile(String parent, String fileName, String fileType) {
        String path = getFilePath(parent, fileName, fileType);
        return new File(path);
    }

    /**
     * 构建文件夹目录
     *
     * @param dir 文件夹对象
     */
    public static void buildDirectory(File dir) {
        if (dir.exists()) {
            LogMgr.d("buildDirectory(" + dir.getPath() + ") already exists!");
            return;
        }
        if (dir.getParentFile().exists()) {
            boolean result = dir.mkdir();
            LogMgr.d("buildDirectory(" + dir.getPath() + ") --> " + result);
        } else {
            buildDirectory(dir.getParentFile());
            boolean result = dir.mkdir();
            LogMgr.e("buildDirectory(" + dir.getPath() + ") --> " + result);
        }
    }

    /**
     * 构建文件上级目录
     *
     * @param path 文件路径
     */
    public static void buildDirectory(String path) {
        File file = new File(path);
        buildDirectory(file.getParentFile());
    }

    /**
     * 获取照片路径
     *
     * @param name 文件名
     * @return 照片路径
     */
    public static String getPicturePath(String name) {
        String path = getFilePath(DIR_ABILIX_PHOTO, name, _TYPE_JPEG);
        buildDirectory(path);
        return path;
    }

    public static void saveFile(String data, String path) {
        File file = new File(path);
        FileOutputStream fo = null;
        try {
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            fo = new FileOutputStream(file);
            fo.write(data.getBytes());
            fo.flush();
            fo.close();
        } catch (IOException e) {
            if (fo != null) {
                try {
                    fo.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }

    }

    public static void writeData(String data, String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                LogMgr.e("file not exist ,create new file");
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(osw);
            LogMgr.e("写文件");
            bufferedWriter.write(data);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            LogMgr.e(" delete file::" + filePath);
            file.delete();
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件存在返回true，否则返回false
     */
    public static boolean isFileExist(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                return true;
            }
        }
        return false;
    }

    public static String readFile(String path) {
        String content = ""; // 文件内容字符串
        File file = new File(path);

        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            LogMgr.d("The File does not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                LogMgr.e("The File does not exist.");
            } catch (IOException e) {
                LogMgr.e("IOException::" + e.getMessage());
            }
        }
        return content;
    }

    public static void writeLog(String data) {
        try {
            File sdCardDir = Environment.getExternalStorageDirectory();
            File file = new File(sdCardDir.getCanonicalPath() + "/" + "log.txt");
            if (!file.exists()) {
                LogMgr.e("file not exist ,create new file");
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(osw);
            bufferedWriter.write(data);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
