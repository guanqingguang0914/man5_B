package com.abilix.brain.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.abilix.brain.GlobalConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.abilix.explainer.utils.FileUtils.buildDirectory;

/**
 * 文件工具类
 */
public class FileUtils {
    public final static String TAG = FileUtils.class.getSimpleName();
    public final static String DATA_PATH = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "Abilix"
            + File.separator + "RobotInfo";
    // public final static String SCRATCH_VJC_IMAGE = DATA_PATH + File.separator
    // + "m_Image.jpg";
    public final static String SCRATCH_VJC_IMAGE_ = DATA_PATH + File.separator
            + "Photo" + File.separator;
    public final static String SCRATCH_VJC_IMAGE_U201 = com.abilix.explainer.utils.FileUtils.DIR_DEFAULT_IMAGE_ANIM_KU + File.separator;
    public final static String SCRATCH_VJC_IMAGE_JPG = ".jpg";
    public final static String SCRATCH_VJC_IMAGE_GIF = ".gif";
    public final static String DATA_UPDATE = Environment
            .getExternalStorageDirectory() + File.separator + "Podcasts";
    public static final String SSID_PATH = DATA_PATH + File.separator
            + "ssid.txt";
    public static final String PASS_PATH = DATA_PATH + File.separator
            + "pass.txt";
    public static final String WIFI_SSID_PATH = DATA_PATH + File.separator
            + "wifi_ssid.txt";
    public static final String WIFI_PASS_PATH = DATA_PATH + File.separator
            + "wifi_pass.txt";
    public static final String LOG_PATH = DATA_PATH + File.separator
            + "log.txt";
    public static final String AI_ENVIRONMENT1 = DATA_PATH + File.separator
            + "environment1.txt";
    public static final String AI_ENVIRONMENT2 = DATA_PATH + File.separator
            + "environment2.txt";
    public static final String STM_VERSION_BRAIN = DATA_PATH + File.separator
            + "stmversion_brain.txt";
    public final static String SDCARD = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator;
    public final static String AUDIOPATH = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + "Abilix" + File.separator + "AbilixMusic" + File.separator;
    public final static String MOVEBIN = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + "Abilix" + File.separator;
    public static final String SHUTDOWN = DATA_PATH + File.separator + "shutdown.txt"; // 默认定时关机时间文件
    public final static String FILE_FORMAT_BIN=".bin";
    public final static String FILE_FORMAT_ELF=".elf";
    public final static String FILE_ENDFIX_C = "[C]";
    /* stm存放的文件路径 */
    public static final String UPGRADE_FILE = BrainUtils.ABILIX + "UpdateFile";
    // + File.separator
    // + "C5ControlUpdata.bin";
    private final static String SKILLPLAYER_INFO_INI_STRING_NAME = "info.ini";
    private final static String SKILLPALYER_SECTION_NAME = "INFO";
    private final static String SKILLPLAYER_SKILL_NAME = "SkillName";
    private final static String SKILLPLAYER_SKILL_NAME_TW = "SkillNameTradChn";
    private final static String SKILLPLAYER_SKILL_NAME_ENG = "SkillNameEng";

    public final static int SKILL_PLAYER_ACTION_STOP = 0x10; //停止
    public final static int SKILL_PLAYER_ACTION_PLAY = 0x11; //开始
    public final static int SKILL_PLAYER_ACTION_PUASE = 0x12; //暂停
    public final static int SKILL_PLAYER_ACTION_RESUME = 0x13; //继续
    public final static String MOVEBIN_DIR = "Abilix" + File.separator + "MoveBin" + File.separator;
    public final static String DIR_MEDIA_DEFAULT_IMAGE = com.abilix.explainer.utils.FileUtils.DIR_MEDIA_DEFAULT_IMAGE;
    /**
     * 外部依赖库依赖文件
     */
    public final static String LIBRARY_RES = MOVEBIN +"LibraryRes" + File.separator;
    /**
     * 视觉跟踪依赖文件
     */
    public final static String VISION_LIB_FILE = LIBRARY_RES + "vision" + File.separator + "vision_lib" + File.separator;
    /**
     * 视觉跟踪音频文件路径
     */
    public final static String VISION_RES_FILE = LIBRARY_RES + "vision"  + File.separator  + "vision_res";
    /**
     *  保存机器人stm32的版本号到文件中
     */
    public static void saveSTMtm32VersionFile(String path) {
        String data = "";
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C:
                data = GlobalConfig.STM_VISION_C + "";
                break;
            case GlobalConfig.ROBOT_TYPE_C9:
                data = GlobalConfig.STM_VISION_C9 + "";
                break;
            case GlobalConfig.ROBOT_TYPE_C1:
                data = GlobalConfig.STM_VISION_C1 + "";
                break;
            case GlobalConfig.ROBOT_TYPE_M1:
                data = GlobalConfig.STM_VISION_M1 + "";
                break;
            case GlobalConfig.ROBOT_TYPE_M:
                data = GlobalConfig.STM_VISION_M + "";
                break;
            case GlobalConfig.ROBOT_TYPE_F:
                data = GlobalConfig.STM_VISION_F + "";
                break;
            case GlobalConfig.ROBOT_TYPE_AF:
                data = GlobalConfig.STM_VISION_AF + "";
                break;
            case GlobalConfig.ROBOT_TYPE_S:
                data = GlobalConfig.STM_VISION_S + "";
                break;
            default:
                return;
        }
        saveStringToFile(data ,path);
    }
    /**
     *  保存Data到文件中
     */
    public static void saveStringToFile(String data, String path) {
        //调用saveByteDateToFile，实现String的保存
        saveByteDateToFile(data.getBytes() ,path ,false);
    }

    /**
     *  保存Data到文件中，isUseNewFile为true则重新创建文件
     */
    public static boolean saveByteDateToFile(byte[] data, String path, boolean isUseNewFile) {
        try {
            File file = new File(path);
            //如果需要创建新文件进行保存，而同名文件已经存在，则先删除
            if(isUseNewFile && file.exists()){
                file.delete();
                file.createNewFile();
            }else{
                if (!file.exists()) {
                    LogMgr.d("file not exist ,create new file");
                    file.createNewFile();
                }
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            // OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            // BufferedWriter bufferedWriter = new BufferedWriter(osw);
            LogMgr.d("写文件");
            fos.write(data);
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

    // public static void deleteFile(String filePath) {
    // File file = new File(filePath);
    // if (file.exists()) {
    // LogMgr.e(" delete file::" + filePath);
    // file.delete();
    // }
    // }

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
                    InputStreamReader inputreader = new InputStreamReader(
                            instream);
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

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @param isDeleteRoot 是否删除根目录
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath, boolean isDeleteRoot) {
        boolean flag = false;
        // 如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        LogMgr.d("deleteDirectory: " + filePath);
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        // 遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                // 删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                // 删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath(),true);
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        // 删除当前空目录
        if(isDeleteRoot){
            return dirFile.delete();
        }else{
            return true;
        }

    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     */
    public static void deleteDirectorySkillPlay(String filePath) {
        boolean flag = false;
        // 如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        // 遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                // 删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                // 删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath(),true);
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return;
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        LogMgr.d("deleteFile01 filePath = " + filePath);
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            final File to = new File(file.getAbsolutePath()
                    + System.currentTimeMillis());
            file.renameTo(to);
            return to.delete();
            // return file.delete();
        }
        return false;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件
     * @return 文件删除成功返回true，否则返回false
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

    public static boolean deleteFile(File file) {
        LogMgr.d("deleteFile02 file = " + file.getPath());
        if (file.isFile() && file.exists()) {
            final File to = new File(file.getAbsolutePath()
                    + System.currentTimeMillis());
            file.renameTo(to);
            return to.delete();
            // return file.delete();
        }
        return false;
    }

    /**
     * install slient
     *
     * @param context
     * @param filePath
     * @return 0 means normal, 1 means file not exist, 2 means other exception
     * error
     */
    public static int installSlient(Context context, String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0
                || (file = new File(filePath)) == null || file.length() <= 0
                || !file.exists() || !file.isFile()) {
            return 1;
        }

        String[] args = {"pm", "install", "-r", filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String s;

            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = 2;
        } catch (Exception e) {
            e.printStackTrace();
            result = 2;
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        // TODO should add memory is not enough here
        if (successMsg.toString().contains("Success")
                || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        LogMgr.d("installSlient " + "successMsg:" + successMsg + ", ErrorMsg:"
                + errorMsg);
        return result;
    }

    public static void saveBinFileToSdCard(Context context, String pathName) {
        try {
            File dir = new File(UPGRADE_FILE);
            if (!dir.exists()) {
                dir.mkdirs();
                // file.createNewFile();
            }else{
                File file = new File(dir, pathName);
                LogMgr.d("需要保存的文件："+file.getAbsolutePath());
                if (file.exists()) {
                    LogMgr.e("升级固件文件已经存在");
                    return;
                }
                FileUtils.deleteDirectory(UPGRADE_FILE, false);
            }
            File file = new File(dir, pathName);
            if (!file.exists()) {
               LogMgr.e("create new bin file");
                file.createNewFile();
            }
            InputStream inputStream = context.getAssets().open(pathName);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            inputStream.close();
            fos.close();
        } catch (Exception e) {

        }

    }

    public static String getSkillplayerMoveName(File f)
            throws FileNotFoundException {
        String result = "";
        File infoFile = new File(f, SKILLPLAYER_INFO_INI_STRING_NAME);

        IniFile.getInstance().load(infoFile);
        int currentLanguageType = Utils.getLanguageType();
        if (Utils.LANGUAGE_TYPE_CN == currentLanguageType) {
            result = (String) IniFile.getInstance().get(
                    SKILLPALYER_SECTION_NAME, SKILLPLAYER_SKILL_NAME);
        } else if (Utils.LANGUAGE_TYPE_EN == currentLanguageType) {
            result = (String) IniFile.getInstance().get(
                    SKILLPALYER_SECTION_NAME, SKILLPLAYER_SKILL_NAME_ENG);
        } else if (Utils.LANGUAGE_TYPE_TW == currentLanguageType) {
            result = (String) IniFile.getInstance().get(
                    SKILLPALYER_SECTION_NAME, SKILLPLAYER_SKILL_NAME_TW);
        } else {
            result = (String) IniFile.getInstance().get(
                    SKILLPALYER_SECTION_NAME, SKILLPLAYER_SKILL_NAME_ENG);
        }

        result = result.replace("\"", "");

        LogMgr.e("result1 = "+result );
        //skill creator中只填写了中文简体项，在其他语言时，读不到任何东西，重新尝试读取中文简体项
        if(TextUtils.isEmpty(result)){
            LogMgr.e("result2 = "+result );
            result = (String) IniFile.getInstance().get(
                    SKILLPALYER_SECTION_NAME, SKILLPLAYER_SKILL_NAME);
            result = result.replace("\"", "");
        }
        LogMgr.e("result3 = "+result );

        return result;
    }

    /**
     * 根据新文件的全路径，获取到老文件的文件名
     *
     * @param file    新文件
     * @param appType 文件类型
     * @return 老文件的文件名
     */
    public static String getOriginalName(File file, byte appType) {
        if (!file.exists()
                || (appType != GlobalConfig.APP_TYPE_ABILIX_CHART&&appType != GlobalConfig.APP_TYPE_MULTI_MEDIA
                && appType != GlobalConfig.APP_TYPE_ABILIX_SCRATCH && appType != GlobalConfig.APP_TYPE_PROGRAM_ROBOT)) {
            LogMgr.e("参数异常");
            return null;
        }
        String originalName = null;
        String newName = file.getName();

        String filePath = null;
        if (appType == GlobalConfig.APP_TYPE_ABILIX_CHART) {
            filePath = BrainUtils.ABILIX + BrainUtils.ABILIX_CHART;
        } else if (appType == GlobalConfig.APP_TYPE_ABILIX_SCRATCH) {
            filePath = BrainUtils.ABILIX + BrainUtils.ABILIX_SCRATCH;
        } else if (appType == GlobalConfig.APP_TYPE_PROGRAM_ROBOT) {
            filePath = BrainUtils.ABILIX + BrainUtils.ABILIX_PROJECT_PROGRAM;
        }else if(appType== GlobalConfig.APP_TYPE_MULTI_MEDIA){
            filePath=BrainUtils.ABILIX+BrainUtils.ABILIX_MEDIA;
        }
        LogMgr.d("getOriginalName filePath = " + filePath + " newName = "
                + newName);
        File dirFile = new File(filePath);
        String[] fileNames = dirFile.list();
        if (null == fileNames) {
            LogMgr.e("getOriginalName 获取所有文件列表异常");
            return null;
        }
        for (int i = 0; i < fileNames.length; i++) {
            if (fileNames[i].equalsIgnoreCase(newName)) {
                originalName = fileNames[i];
                break;
            }
        }
        LogMgr.d("getOriginalName originalName = " + originalName);

        return originalName;
    }

    public static void saveSoundFileToSdCard(Context context, String path) {
        try {
            String[] fileList = context.getAssets().list(path);
            if (fileList.length > 1) {
                File dir = new File(AUDIOPATH + path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                for (String str : fileList) {
                    String filePath = path + "/" + str;
                    saveSoundFileToSdCard(context, filePath);
                }
            } else {
                File file = new File(AUDIOPATH + path);
                if (!file.exists()) {
                    LogMgr.d("file does not exist, create new file::"
                            + AUDIOPATH + path);
                    file.createNewFile();
                }
                InputStream in = context.getAssets().open(path);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = in.read(buffer)) != -1) {// 循环从输入流读取
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                in.close();
                fos.close();
            }
        } catch (IOException e) {
            LogMgr.e("save music file to sdcard error::" + e);
            e.printStackTrace();
        }
    }


    public static void saveSoundFileToSdCard(Context context, String PATH, String path) {
        try {
            String[] fileList = context.getAssets().list(path);
            if (fileList.length > 1) {
                File dir = new File(PATH + path);
                // 补全路径上的文件夹
                buildDirectory(dir);
                for (String str : fileList) {
                    String filePath = path + "/" + str;
                    saveSoundFileToSdCard(context, PATH,filePath);
                }
            } else {
                File file = new File(PATH + path);
                // 补全路径上的文件夹
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (!file.exists()) {
                    LogMgr.d("file does not exist, create new file::"
                            + PATH + path);
                    file.createNewFile();
                }
                InputStream in = context.getAssets().open(path);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = in.read(buffer)) != -1) {// 循环从输入流读取
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                in.close();
                fos.close();
            }
        } catch (IOException e) {
            LogMgr.e("save music file to sdcard error::" + e);
            e.printStackTrace();
        }
    }

    public final static String CLEAN_UP_PATH = DATA_PATH + File.separator + "clean_up.txt";

    public final static String ABILIX_CHART_DIR = BrainUtils.ABILIX + BrainUtils.ABILIX_CHART;
    public final static String ABILIX_SCRATCH_DIR = BrainUtils.ABILIX + BrainUtils.ABILIX_SCRATCH;
    public final static String ABILIX_PROGRAM_DIR = BrainUtils.ABILIX + BrainUtils.ABILIX_PROJECT_PROGRAM;
    public final static String ABILIX_SKILLPLAYER_DIR = BrainUtils.ABILIX + BrainUtils.ABILIX_SKILLPLAYER;
    public final static String ABILIX_SKILL_CREATOR_DIR = BrainUtils.ABILIX + BrainUtils.ABILIX_SKILL_CREATOR;
    public final static String ABILIX_MEDIA_DIR = BrainUtils.ABILIX + BrainUtils.ABILIX_MEDIA;
    public final static String ABILIX_MEDIA_UPLOAD_DIR = ABILIX_MEDIA_DIR + File.separator + "upload";

    public final static String ABILIX_PHOTO_DIR = com.abilix.explainer.utils.FileUtils.DIR_ABILIX_PHOTO;
    public final static String ABILIX_RECORD_DIR = com.abilix.explainer.utils.FileUtils.DIR_ABILIX_RECORD;

    public static void cleanUpUserFiles() {
        try {
            deleteDirectory(ABILIX_CHART_DIR, false);
            deleteDirectory(ABILIX_SCRATCH_DIR, false);
            deleteDirectory(ABILIX_PROGRAM_DIR, false);
            deleteDirectory(ABILIX_SKILLPLAYER_DIR, false);
            deleteDirectory(ABILIX_SKILL_CREATOR_DIR, false);
            //deleteDirectory(ABILIX_MEDIA_DIR, false);
            deleteDirectory(ABILIX_MEDIA_UPLOAD_DIR, false);
            deleteDirectory(ABILIX_PHOTO_DIR, false);
            deleteDirectory(ABILIX_RECORD_DIR, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getCleanUpFlag() {
        boolean result = false;
        String string = readFile(CLEAN_UP_PATH);
        LogMgr.d("getCleanUpFlag = " + string);
        if (!TextUtils.isEmpty(string) && string.contains("true")) {
            result = true;
        }
        return result;
    }

    /**
     * 获取文件修改时间
     *
     * @return 文件修改时间
     */
    public static String getModiFied(File file) {
        Calendar cal = Calendar.getInstance();
        long time = file.lastModified();
        cal.setTimeInMillis(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(cal.getTime());
    }

    /**
     * 读取文件中的内容
     *
     * @param file
     * @return 文件中的内容
     */
    public static String readFile(File file) {
        String content = "";
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 获取文件中的内容并已字节数组的形式返回
     *
     * @param file
     * @return 文件中的内容
     */
    public static byte[] readFileRetrunByteArray(File file) {
        if (!file.exists() || !file.isFile()) {
            LogMgr.e("文件不存在或不是一个文件");
            return null;
        }
        byte[] result = null;
        try {
            long fileLength = file.length();
            FileInputStream fileRead = new FileInputStream(file);
            result = new byte[(int) fileLength];

            int fileReadLineNum = fileRead.read(result);
            fileRead.close();
        } catch (Exception e) {
            LogMgr.e("readFileRetrunByteArray() 异常");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取文件中的内容并已字节数组的形式返回
     *
     * @param filePath
     * @return 文件中的内容
     */
    public static byte[] readFileRetrunByteArray(String filePath) {
        File file = new File(filePath);
        return readFileRetrunByteArray(file);
    }

    /**
     * skillplayer文件下载成功后的旧文件删除处理
     *
     * @param fileNameWithoutEndfix
     * @param totalFileName
     * @throws Exception
     */
    public static void deleteOldSkillPlayerFile(String fileNameWithoutEndfix, String totalFileName) throws Exception {
        int lastIndex = fileNameWithoutEndfix.lastIndexOf("_");
        if (lastIndex == -1) {
            throw new Exception("文件的格式不正确");
        }
        String fileNameWithoutVersion = fileNameWithoutEndfix.substring(0, lastIndex);
        int newVersion = Integer.valueOf(fileNameWithoutEndfix.substring(lastIndex + 1, fileNameWithoutEndfix.length()));
        LogMgr.i("fileNameWithoutVersion = " + fileNameWithoutVersion + " newVersion = " + newVersion);
        // 如果有老版本的 删除
        deleteOldVersionFile(fileNameWithoutVersion, newVersion, totalFileName);
        // 解压缩
        ZipUtils.UnZipFolder(BrainUtils.ABILIX_SKILLPLAYER_PATH + File.separator + totalFileName,
                BrainUtils.ABILIX_SKILLPLAYER_PATH + File.separator + fileNameWithoutEndfix);
        // 删除zip包
        if (FileUtils.deleteFile(BrainUtils.ABILIX_SKILLPLAYER_PATH + File.separator + totalFileName)) {
            LogMgr.i("解压缩完成后 删除zip文件成功");
        } else {
            LogMgr.e("解压缩完成后 删除zip文件失败");
        }
    }

    /**
     * 删除老版本的文件夹
     *
     * @param fileNameWithoutVersion
     * @param newVersion
     * @param totalFileName
     */
    private static void deleteOldVersionFile(String fileNameWithoutVersion, int newVersion, String totalFileName) {
        // String filePath = GlobalConfig.SKILLPLAYER_PATH;
        File filePath = new File(BrainUtils.ABILIX_SKILLPLAYER_PATH);
        if (filePath.exists() && filePath.isDirectory()) {
            String[] fileArray = filePath.list();
            if (null == fileArray || fileArray.length <= 0) {
                return;
            }
            // 遍历文件夹
            for (int i = 0; i < fileArray.length; i++) {

                if (fileArray[i].startsWith(fileNameWithoutVersion)) {
                    if (fileArray[i].endsWith(".zip")) {
                        if (fileArray[i].equals(totalFileName)) {
                            // do nothing 是此次下载的zip文件
                        } else {
                            // delete 不是此次下载的zip文件
                            if (FileUtils.deleteFile(filePath + File.separator + fileArray[i])) {
                                LogMgr.i("deleteOldVersionFile() 删除文件成功: " + fileArray[i]);
                            } else {
                                LogMgr.e("deleteOldVersionFile() 删除文件失败: " + fileArray[i]);
                            }
                        }
                    } else {
                        // delete 别的版本的文件夹
                        File tempFile = new File(filePath + File.separator + fileArray[i]);
                        if (tempFile.isDirectory()) {
                            if (FileUtils.deleteDirectory(filePath + File.separator + fileArray[i],true)) {
//								sendBroadcast(tempFile.getAbsolutePath(), GlobalConfig.DELETE_SKILL_OLD_FILE);
                                LogMgr.i("deleteOldVersionFile() 删除文件夹成功: " + fileArray[i]);
                            } else {
                                LogMgr.e("deleteOldVersionFile() 删除文件夹失败: " + fileArray[i]);
                            }
                        } else {
                            if (FileUtils.deleteFile(filePath + File.separator + fileArray[i])) {
                                LogMgr.i("deleteOldVersionFile() 删除文件成功: " + fileArray[i]);
                            } else {
                                LogMgr.e("deleteOldVersionFile() 删除文件失败: " + fileArray[i]);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * S系列文件下载成功后删除旧文件
     *
     * @param totalFileName
     * @throws Exception
     */
    public static void deleteOldSProgramFile(String totalFileName) throws Exception {
        String fileNameWithoutEndfix = totalFileName.substring(0, totalFileName.length() - 4);

        int lastIndex = fileNameWithoutEndfix.lastIndexOf("_");
        if (lastIndex == -1) {
            throw new Exception("文件的格式不正确");
        }

        String fileNameWithoutVersion = fileNameWithoutEndfix.substring(0, lastIndex);
        int newVersion = Integer.valueOf(fileNameWithoutEndfix.substring(lastIndex + 1, fileNameWithoutEndfix.length()));

        LogMgr.i("fileNameWithoutVersion = " + fileNameWithoutVersion + " newVersion = " + newVersion);

        File filePath = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_PROJECT_PROGRAM_FOR_S);
        if (filePath.exists() && filePath.isDirectory()) {
            String[] fileArray = filePath.list();
            if (null == fileArray || fileArray.length <= 0) {
                return;
            }
            // 遍历文件夹
            for (int i = 0; i < fileArray.length; i++) {
                if (fileArray[i].startsWith(fileNameWithoutVersion)) {
                    // delete 别的版本的文件
                    if (fileArray[i].equals(totalFileName)) {
                        // do nothing 是此次下载的文件
                    } else {
                        if (FileUtils.deleteFile(filePath + File.separator + fileArray[i])) {
                            LogMgr.i("deleteOldVersionFile() 删除文件成功: " + fileArray[i]);
                        } else {
                            LogMgr.e("deleteOldVersionFile() 删除文件失败: " + fileArray[i]);
                        }
                    }
                }
            }
        }
    }

}
