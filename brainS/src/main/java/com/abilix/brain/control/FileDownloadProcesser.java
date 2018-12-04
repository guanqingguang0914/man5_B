package com.abilix.brain.control;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;

import com.abilix.brain.Application;
import com.abilix.brain.BrainActivity;
import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.brain.data.BrainDatabaseHelper;
import com.abilix.brain.data.DataProcess;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * 处理文件下载功能类
 * @author yangz
 * @date 2017/12/4
 */
public class FileDownloadProcesser {

    private static final String BIN = ".bin";
    private static final String ELF = ".elf";
    /* skillplayer文件类别 */
    public static final byte FILE_TYPE_LIGHT = (byte) 0x01;
    public static final byte FILE_TYPE_ACTION = (byte) 0x02;

    public static Context context = Application.getInstance();

    private BrainDatabaseHelper mBrainDatabaseHelper;
    private SQLiteDatabase mBrainDatabase;


    private static FileDownloadProcesser instance;
    private FileDownloadProcesser(){

    }

    public static FileDownloadProcesser getInstance() {
        // 单例
        if (instance == null) {
            synchronized (FileDownloadProcesser.class) {
                if (instance == null) {
                    instance = new FileDownloadProcesser();
                }
            }
        }
        return instance;
    }

    /**用于判断文件长度是否正常，长度 = 0 时不正常*/
    private boolean isFileLengthError = false;
    /**下载时是否有同名文件 有的话需要进行一次转移*/
    private boolean hasSameNameFile = false;
    //文件传输相关
    /**机器人类型*/
    private byte robotType;
    /**文件类型*/
    private byte fileType;
    /**协议命令里的第二，第三位byte表示的长度*/
    private int cmdLength;
    /**客户端传过来的crc值*/
    private int crcCheck;
    /**请求命令中的文件名*/
    private String totalFileName;
    private String fileNameWithoutEndfix;
    /**app类型*/
    private byte appType;
    /**文件总长度*/
    private int fileLength;
    private FileOutputStream fos;
    /**保存文件时文件写入流*/
    private BufferedOutputStream bufferedOutputStream;
    /**下载时是否有同名文件 有的话需要进行一次转移 转移前的文件*/
    private File mFileToMove;
    /**本地计算出的crc值*/
    CRC32 crc32 = null;
    /**保存发送文件的文件对象*/
    private File mReceiveFile;
    private String originalFileName;
    /**下载文件的绝对路径，数据库表中的唯一标识*/
    private String mFileAbsolutePath;
    /**断点续传的位置*/
    private int mBrokenPoint;
    /**接受到apk后待删除的文件*/
    private String mNeedToDeleteFilePath;
    public String getmNeedToDeleteFilePath() {
        return mNeedToDeleteFilePath;
    }

    /**
     * 处理文件传输的命令
     *
     * @param fileReceiveCmd
     */
    public synchronized void handleFileReceiveCmd(byte[] fileReceiveCmd) {
        LogMgr.d("handleFileReceiveCmd() fileReceiveCmd = "+Utils.bytesToString(fileReceiveCmd));
//        LogMgr.d("fileReceiveCmd:" + fileReceiveCmd.length);
        // if(!isTestHeartBeatCount){
        ServerHeartBeatProcesser.getInstance().setLastReceiveHeartTime(System.currentTimeMillis());
        // }

        if (fileReceiveCmd[0] == GlobalConfig.CMD_0
                && fileReceiveCmd[1] == GlobalConfig.CMD_1) {
            if (fileReceiveCmd[4] == (byte) GlobalConfig.BRAIN_TYPE || fileReceiveCmd[4] == (byte) GlobalConfig.BRAIN_CHILD_TYPE
                    || fileReceiveCmd[4] == GlobalConfig.ROBOT_TYPE_COMMON) {
                LogMgr.d("handleFileReceiveCmd () 文件传输  协议头 机器人类型正确");
            } else {
                byte[] temp = Arrays.copyOfRange(fileReceiveCmd, 0, Math.min(7, fileReceiveCmd.length));
                LogMgr.e("handleFileReceiveCmd () 文件传输  协议头 机器人类型不正确1 fileReceiveCmd = " + Arrays.toString(temp));
                closeConnect(false);
                return;
            }
        } else {
            byte[] temp = Arrays.copyOfRange(fileReceiveCmd, 0, Math.min(7, fileReceiveCmd.length));
            LogMgr.e("handleFileReceiveCmd () 文件传输  协议头 机器人类型不正确2 fileReceiveCmd = " + Arrays.toString(temp));
            closeConnect(false);
            return;
        }
        try {
            if (null == mBrainDatabaseHelper || null == mBrainDatabase) {
                mBrainDatabaseHelper = new BrainDatabaseHelper(context);
                mBrainDatabase = mBrainDatabaseHelper.getWritableDatabase();
            }

            byte cmd1 = fileReceiveCmd[5];
            byte cmd2 = fileReceiveCmd[6];
            // 对方请求发送文件或强制重传
            if ((cmd1 == GlobalConfig.FILE_RECEIVE_IN_CMD_1 && cmd2 == GlobalConfig.FILE_RECEIVE_IN_CMD_2_REQUEST)
                    || (cmd1 == GlobalConfig.FILE_RECEIVE_IN_CMD_1 && cmd2 == GlobalConfig.FILE_RECEIVE_IN_CMD_2_REQUEST_FROM_START)) {
                LogMgr.i("handleFileReceiveCmd() 对方请求发送文件 cmd2 = " + cmd2);

                analyzeFileReceiveHeader(fileReceiveCmd, true);
                int availableStore = (int) Utils.getExternalAvailableSize();
                byte[] availableStoreByteArray = Utils.getByteArrayFromInt(availableStore);
                byte[] sendMsg = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, GlobalConfig.FILE_RECEIVE_OUT_CMD_1, GlobalConfig.FILE_RECEIVE_OUT_CMD_2_NOTIFY, availableStoreByteArray);
                LogMgr.i("回复发送请求 = " + Utils.bytesToString(sendMsg));
                DataProcess.GetManger().sendMsgFileReceive(sendMsg);
            }
            // 对方请求发送文件，从断点位置开始传输
            else if (cmd1 == GlobalConfig.FILE_RECEIVE_IN_CMD_1 && cmd2 == GlobalConfig.FILE_RECEIVE_IN_CMD_2_REQUEST_FROM_BROKEN_POINT) {
                LogMgr.i("handleFileReceiveCmd() 对方请求发送文件，从断点位置开始传输 ");

                analyzeFileReceiveHeader(fileReceiveCmd, false);
                int availableStore = (int) Utils.getExternalAvailableSize();
                byte[] availableStoreByteArray = Utils.getByteArrayFromInt(availableStore);
                byte[] brokenPointByteArray = Utils.getByteArrayFromInt(mBrokenPoint);
                byte[] dataToSend = new byte[availableStoreByteArray.length + brokenPointByteArray.length];
                System.arraycopy(availableStoreByteArray, 0, dataToSend, 0, availableStoreByteArray.length);
                System.arraycopy(brokenPointByteArray, 0, dataToSend, availableStoreByteArray.length, brokenPointByteArray.length);
                byte[] sendMsg = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, GlobalConfig.FILE_RECEIVE_OUT_CMD_1, GlobalConfig.FILE_RECEIVE_OUT_CMD_2_NOTIFY_FROM_BROKEN_POINT, dataToSend);
                LogMgr.i("回复发送请求 = " + Arrays.toString(sendMsg));
                DataProcess.GetManger().sendMsgFileReceive(sendMsg);
            }
            // 对方请求发送文件，清除断点信息，强制重传
//			else if (cmd1 == GlobalConfig.FILE_RECEIVE_IN_CMD_1 && cmd2 == GlobalConfig.FILE_RECEIVE_IN_CMD_2_REQUEST_FROM_START){
//				LogMgr.i("handleFileReceiveCmd() 对方请求发送文件，清除断点信息，强制重传 ");
//			}
            // 对方取消发送文件
            else if (cmd1 == GlobalConfig.FILE_RECEIVE_IN_CMD_1 && cmd2 == GlobalConfig.FILE_RECEIVE_IN_CMD_2_CANCEL) {
                LogMgr.i("handleFileReceiveCmd() 对方取消发送文件 ");

                if (!hasSameNameFile) {
                    mBrainDatabaseHelper.deleteTransferringFileInfo(mBrainDatabase, mFileAbsolutePath);
                }
                byte[] sendMsg = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, GlobalConfig.FILE_RECEIVE_OUT_CMD_1, GlobalConfig.FILE_RECEIVE_OUT_CMD_2_CANCEL_OK, null);
                LogMgr.i("回复对方取消发送文件请求 = " + Arrays.toString(sendMsg));
                DataProcess.GetManger().sendMsgFileReceive(sendMsg);

                closeConnect(true);
            }
            // 对方暂停发送文件
            else if (cmd1 == GlobalConfig.FILE_RECEIVE_IN_CMD_1 && cmd2 == GlobalConfig.FILE_RECEIVE_IN_CMD_2_PAUSE) {
                LogMgr.i("handleFileReceiveCmd() 对方暂停发送文件 ");
                bufferedOutputStream.flush();

                byte[] sendMsg = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE, GlobalConfig.FILE_RECEIVE_OUT_CMD_1, GlobalConfig.FILE_RECEIVE_OUT_CMD_2_PAUSE_OK, null);
                LogMgr.i("回复对方暂停发送文件请求 = " + Arrays.toString(sendMsg));
                DataProcess.GetManger().sendMsgFileReceive(sendMsg);

                closeConnect(false);
            }
            // 对方发送文件数据
            else if (cmd1 == GlobalConfig.FILE_RECEIVE_IN_CMD_1 && cmd2 == GlobalConfig.FILE_RECEIVE_IN_CMD_2_SEND) {
                LogMgr.d("handleFileReceiveCmd() 对方发送文件数据 ");
                int tempInt = (int) (((fileReceiveCmd[2] & 0xFF) << 8) | (fileReceiveCmd[3] & 0xFF));
                LogMgr.d("fileReceiveCmd 协议长度 = " + tempInt);
                LogMgr.d("fileReceiveCmd length = " + fileReceiveCmd.length);
                crc32.update(fileReceiveCmd, 11, fileReceiveCmd.length - 12);
                bufferedOutputStream.write(fileReceiveCmd, 11, fileReceiveCmd.length - 12);
            }
            // 对方发送完成
            else if (cmd1 == GlobalConfig.FILE_RECEIVE_IN_CMD_1 && cmd2 == GlobalConfig.FILE_RECEIVE_IN_CMD_2_COMPLETE) {
                LogMgr.i("handleFileReceiveCmd() 对方发送完成1 ");
                try {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    fos.close();
                    mBrainDatabaseHelper.deleteTransferringFileInfo(mBrainDatabase, mFileAbsolutePath);
                    int tempCrc = (int) crc32.getValue();
                    int crcOfFile = 0;
                    if (null != mReceiveFile && mReceiveFile.exists()) {
                        crcOfFile = (int) Utils.getCRC32(mReceiveFile);
                    }
                    LogMgr.i("tempCrc = " + tempCrc + " crcCheck = " + crcCheck);
                    if (crcCheck == tempCrc || crcCheck == crcOfFile) {
                        LogMgr.v("文件校验成功");

                        if (appType == GlobalConfig.APP_TYPE_SKILL_PLAYER) {
                            LogMgr.i("对方发送完成 按SkillPlayer的文件处理");
                            fileNameWithoutEndfix = totalFileName.substring(0, totalFileName.length() - 4);
                            FileUtils.deleteOldSkillPlayerFile(fileNameWithoutEndfix, totalFileName);
                            DataProcess.GetManger().sendBroadcast(BrainUtils.ABILIX_SKILLPLAYER_PATH + File.separator + totalFileName.substring(0, totalFileName.lastIndexOf(".")),
                                    GlobalConfig.FILE_DOWNLOAD);
                        }
                        // app store
                        else if (appType == GlobalConfig.INNER_FILE_TRANSPORT_APP_STORE) {
                            LogMgr.i("对方发送完成  app store 传输命令处理");
                            if (totalFileName.endsWith(".apk")) {
                                LogMgr.i("当前收到的是apk文件  进行安装处理");
                                String path = BrainUtils.ABILIX + BrainUtils.ABILIX_APP_STORE + File.separator + totalFileName;
                                LogMgr.i("安装文件路径 = " + path);
                                mNeedToDeleteFilePath = path;
                                String packageName = BrainUtils.getPackageName(path);
                                DataProcess.GetManger().sendBroadcast(path, GlobalConfig.FILE_DOWNLOAD_S);
                                installApk(path, packageName);
//                                if (BrainActivity.getmBrainActivity().getmInstallService() != null) {
//                                    Utils.sendBroadcastBeforeInstallApk(context, GlobalConfig.ACTION_PACKAGE_NAME_TO_INSTALL, packageName);
//                                    Thread.sleep(1500);
//                                    BrainActivity.getmBrainActivity().getmInstallService().installApk(path, packageName);
//                                }
                            }
                        }
                        // 用于内部传输文件
                        else if (appType == GlobalConfig.INNER_FILE_TRANSPORT_APP_FLAG) {
                            LogMgr.i("对方发送完成 按内部文件传输命令处理");
                            if (totalFileName.endsWith(".apk")) {
                                LogMgr.i("当前收到的是apk文件  进行安装处理");
                                String path = Environment.getExternalStorageDirectory().getPath() + totalFileName;
                                LogMgr.i("安装文件路径 = " + path);
                                mNeedToDeleteFilePath = path;
                                String packageName = BrainUtils.getPackageName(path);
                                DataProcess.GetManger().sendBroadcast(path, GlobalConfig.FILE_DOWNLOAD_S);
                                installApk(path, packageName);
//                                if (BrainActivity.getmBrainActivity().getmInstallService() != null) {
//                                    Utils.sendBroadcastBeforeInstallApk(context, GlobalConfig.ACTION_PACKAGE_NAME_TO_INSTALL, packageName);
//                                    Thread.sleep(1500);
//                                    BrainActivity.getmBrainActivity().getmInstallService().installApk(path, packageName);
//                                }
                            }
                        }
                        // 用于多媒体传输文件
                        else if (appType == GlobalConfig.APP_TYPE_MULTI_MEDIA) {
                            LogMgr.i("对方发送完成 按多媒体文件传输命令处理");
                            String path = BrainUtils.ABILIX + BrainUtils.ABILIX_MEDIA + File.separator + totalFileName;
                            DataProcess.GetManger().sendBroadcast(path, GlobalConfig.FILE_DOWNLOAD_S);
                        }
                        // 用于S系列项目编程
                        else if (appType == GlobalConfig.APP_TYPE_PROGRAM_ROBOT_FOR_S) {
                            LogMgr.i("对方发送完成 按S系列项目编程文件传输命令处理");
                            FileUtils.deleteOldSProgramFile(totalFileName);
                        }
                        //用于认识机器人
                        else if (appType == GlobalConfig.APP_TYPE_KNOW_ROBOT) {
                            LogMgr.i("对方发送完成 按认识机器人文件传输命令处理");
                            String path = mFileAbsolutePath;
                            DataProcess.GetManger().sendBroadcast(path, GlobalConfig.FILE_DOWNLOAD_S);
                        }
                        // 用于skill creator
                        else if (appType == GlobalConfig.APP_TYPE_SKILL_CREATOR) {
                            LogMgr.i("对方发送完成 按skill creator文件传输命令处理");
                            String path = BrainUtils.ABILIX + BrainUtils.ABILIX_SKILL_CREATOR + File.separator + totalFileName;
                            DataProcess.GetManger().sendBroadcast(path, GlobalConfig.FILE_DOWNLOAD_S);
                            BrainService.getmBrainService().sendMessageToControl(GlobalConfig.CONTROL_CALLBACKMODE_PATCH_CMD, null, path, 21, 0);
                        } else {
                            if (!isFileLengthError) {
                                downloadSuccess();
                            } else {
                                if (hasSameNameFile) {
                                    FileUtils.deleteFile(mFileToMove);
                                } else {
                                    FileUtils.deleteFile(mReceiveFile);
                                }
                            }
                            isFileLengthError = false;
                            // LogMgr.d(TAG, "appType:"+appType);
                        }

                        byte[] sendMsg = ProtocolUtil.buildProtocol(
                                (byte) GlobalConfig.BRAIN_TYPE,
                                GlobalConfig.FILE_RECEIVE_OUT_CMD_1,
                                GlobalConfig.FILE_RECEIVE_OUT_CMD_2_RECEIVE_SUCCESS,
                                null);
                        DataProcess.GetManger().sendMsgFileReceive(sendMsg);
                    } else {
                        LogMgr.e("文件校验失败");
                        byte[] sendMsg = ProtocolUtil.buildProtocol(
                                (byte) GlobalConfig.BRAIN_TYPE,
                                GlobalConfig.FILE_RECEIVE_OUT_CMD_1,
                                GlobalConfig.FILE_RECEIVE_OUT_CMD_2_CHECK_WRONG,
                                null);
                        DataProcess.GetManger().sendMsgFileReceive(sendMsg);
                        closeConnect(true);
                    }
                } catch (Exception e) {
                    LogMgr.e("文件save失败" + e);
                    byte[] sendMsg = ProtocolUtil.buildProtocol(
                            (byte) GlobalConfig.BRAIN_TYPE,
                            GlobalConfig.FILE_RECEIVE_OUT_CMD_1,
                            GlobalConfig.FILE_RECEIVE_OUT_CMD_2_CHECK_WRONG,
                            null);
                    DataProcess.GetManger().sendMsgFileReceive(sendMsg);
                    closeConnect(true);
                    e.printStackTrace();
                }
                appType = GlobalConfig.APP_TYPE_UNKNOWN;
                mReceiveFile = null;
                LogMgr.i("handleFileReceiveCmd() 对方发送完成2 ");
            } else if ((cmd1 == GlobalConfig.FILE_RECEIVE_IN_CMD_1 && cmd2 == GlobalConfig.FILE_RECEIVE_IN_CMD_2_QUERY)) {
                LogMgr.i("handleFileReceiveCmd() 对方查询文件是否存在 ");

                byte typeOfApp = fileReceiveCmd[11];
                int lengthOfCmd = (int) ((fileReceiveCmd[2] & 0xFF << 8) | (fileReceiveCmd[3] & 0xFF));
                byte[] tempBuffer = new byte[lengthOfCmd - 18];
                System.arraycopy(fileReceiveCmd, 21, tempBuffer, 0, tempBuffer.length);
                String fileName = new String(tempBuffer, "UTF-8");
                File queryFile = null;

                if (typeOfApp == GlobalConfig.APP_TYPE_PROGRAM_ROBOT_FOR_S) {
                    LogMgr.i("handleFileReceiveCmd() 对方APP为S的项目编程 ");
                    queryFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_PROJECT_PROGRAM_FOR_S + File.separator + fileName);
                }else if(typeOfApp == GlobalConfig.APP_TYPE_MULTI_MEDIA){
                    LogMgr.i("handleFileReceiveCmd() 对方APP为扩展媒体传输 ");
                    queryFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_MEDIA + fileName);
                }

                byte[] returnData = new byte[1];
                if (queryFile!=null && queryFile.exists()) {
                    int crcCheckInCmd = Utils.bytesToInt4(fileReceiveCmd, 17);
                    int crcOfFile = (int) Utils.getCRC32(queryFile);
                    if (crcCheckInCmd == crcOfFile) {
                        LogMgr.i("handleFileReceiveCmd() 对方查询文件是否存在 结果 存在 fileName = " + fileName);
                        returnData[0] = (byte) 0x00;
                    } else {
                        LogMgr.i("handleFileReceiveCmd() 对方查询文件是否存在 结果 存在 但CRC值不同 fileName = " + fileName);
                        FileUtils.deleteFile(queryFile);
                        returnData[0] = (byte) 0x01;
                    }
                } else {
                    LogMgr.i("handleFileReceiveCmd() 对方查询文件是否存在 结果 不存在 fileName = " + fileName);
                    returnData[0] = (byte) 0x01;
                }
                byte[] sendMsg = ProtocolUtil.buildProtocol((byte) GlobalConfig.BRAIN_TYPE,
                        GlobalConfig.FILE_RECEIVE_OUT_CMD_1, GlobalConfig.FILE_RECEIVE_OUT_CMD_2_QUERY_OK, returnData);
                LogMgr.i("回复查询请求 = " + Arrays.toString(sendMsg));
                DataProcess.GetManger().sendMsgFileReceive(sendMsg);
            } else {
                LogMgr.e("文件传输请求头错误");
                closeConnect(false);
            }
        } catch (Exception e) {
            LogMgr.e("文件传输失败" + e);
            e.printStackTrace();
            closeConnect(false);
        }

    }


    /**
     * 接受到发送请求时解析发送请求命令 创建文件 打开文件输入流
     *
     * @param fileReceiveCmd      收到的完整请求命令
     * @param isDownloadFromStart 是否强制从头开始传送，当为false时，会优先考虑断点续传
     * @throws Exception
     */
    private void analyzeFileReceiveHeader(byte[] fileReceiveCmd, boolean isDownloadFromStart) throws Exception {
        isFileLengthError = false;
        hasSameNameFile = false;
        //保存文件信息
        cmdLength = (int) ((fileReceiveCmd[2] & 0xFF << 8) | (fileReceiveCmd[3] & 0xFF));
        robotType = fileReceiveCmd[4];
        appType = fileReceiveCmd[11];
        fileType = fileReceiveCmd[12];
        fileLength = Utils.bytesToInt4(fileReceiveCmd, 13);
        LogMgr.i("文件长度:" + fileLength);
        if (fileLength == 0) {
            isFileLengthError = true;
            BrainActivity.getmBrainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BrainUtils.utilisToast(BrainActivity.getmBrainActivity().getString((R.string.wenjianchangducuowu)), BrainActivity.getmBrainActivity());
                }
            });
        }
        crcCheck = Utils.bytesToInt4(fileReceiveCmd, 17);
        LogMgr.i("收到的文件 crc 四字节" + fileReceiveCmd[17] + "," + fileReceiveCmd[18] + "," + fileReceiveCmd[19] + "," + fileReceiveCmd[20]);
        crc32 = new CRC32();
        byte[] tempBuffer = new byte[cmdLength - 18];
        System.arraycopy(fileReceiveCmd, 21, tempBuffer, 0, tempBuffer.length);
        totalFileName = new String(tempBuffer, "UTF-8");
        LogMgr.i("analyzeFileReceiveHeader() fileName = " + totalFileName + " appType:" + appType);
        // 根据不同的类型 放到不同的文件夹中
        switch (appType) {
            case GlobalConfig.APP_TYPE_SKILL_PLAYER:
                LogMgr.i("当前是SkillPlayer命令");
                mReceiveFile = new File(BrainUtils.ABILIX_SKILLPLAYER_PATH + File.separator + totalFileName);
                break;
            case GlobalConfig.APP_TYPE_INNER_FILE:
                LogMgr.i("当前是内部文件传输命令");
                mReceiveFile = new File(Environment.getExternalStorageDirectory().getPath() + totalFileName);
                break;
            case GlobalConfig.APP_TYPE_STORE:
                LogMgr.i("当前是APP_TYPE_STORE");
                mReceiveFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_APP_STORE + File.separator + totalFileName);
                break;
            // vjc
            case GlobalConfig.APP_TYPE_ABILIX_CHART:
                LogMgr.i("当前是APP_TYPE_ABILIX_CHART命令");
                //ELF文件
                if(fileType==0x0E){
                    mReceiveFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_CHART + File.separator + totalFileName + ELF);
                }else{
                    mReceiveFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_CHART + File.separator + totalFileName + BIN);
                }
                break;
            // scratch
            case GlobalConfig.APP_TYPE_ABILIX_SCRATCH:
                LogMgr.i("当前是APP_TYPE_ABILIX_SCRATCH命令");
                mReceiveFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_SCRATCH + File.separator + totalFileName + BIN);
                break;
            // 项目编程
            case GlobalConfig.APP_TYPE_PROGRAM_ROBOT:
                LogMgr.i("当前是APP_TYPE_PROGRAM_ROBOT命令");
                mReceiveFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_PROJECT_PROGRAM + File.separator + totalFileName + BIN);
                break;
            // 多媒体
            case GlobalConfig.APP_TYPE_MULTI_MEDIA:
                LogMgr.i("当前是APP_TYPE_MULTI_MEDIA命令");
                mReceiveFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_MEDIA + totalFileName);
                break;
            //认识机器人
            case GlobalConfig.APP_TYPE_KNOW_ROBOT:
                LogMgr.i("当前是APP_TYPE_KNOW_ROBOT命令：认识机器人");
                mReceiveFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_KNOW_ROBOT + File.separator + totalFileName);
                if (mReceiveFile.exists() && mReceiveFile.isFile()) {
                    FileUtils.deleteFile(mReceiveFile);
                }
                break;
            // S系列项目编程
            case GlobalConfig.APP_TYPE_PROGRAM_ROBOT_FOR_S:
                LogMgr.i("当前是S系列项目编程命令");
                mReceiveFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_PROJECT_PROGRAM_FOR_S + File.separator + totalFileName);
                break;
            case GlobalConfig.APP_TYPE_SKILL_CREATOR:
                mReceiveFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_SKILL_CREATOR + File.separator + totalFileName);
                LogMgr.d("传输的文件时skillcreator文件时");
                if (mReceiveFile.exists() && mReceiveFile.isFile()) {
                    FileUtils.deleteFile(mReceiveFile);
                } else if (mReceiveFile.exists() && mReceiveFile.isDirectory()) {
                    FileUtils.deleteDirectory(mReceiveFile.getAbsolutePath(), true);
                }
                break;
            default:
                break;
        }

        // 补全路径上的文件夹
        if (!mReceiveFile.getParentFile().exists()) {
            mReceiveFile.getParentFile().mkdirs();
        }

        mFileAbsolutePath = mReceiveFile.getAbsolutePath();

        if (!mReceiveFile.exists()) {
            LogMgr.i("analyzeFileReceiveHeader() 本地不存在文件" + mFileAbsolutePath);
            mReceiveFile.createNewFile();
            fos = new FileOutputStream(mReceiveFile);
            bufferedOutputStream = new BufferedOutputStream(fos);
            //在数据库中插入下载信息
            int queryResult = mBrainDatabaseHelper.queryTransferringFileInfo(mBrainDatabase, mFileAbsolutePath, crcCheck);
            if (queryResult == BrainDatabaseHelper.TRANSFERRING_FILE_STATE_EXIST_DIFFERENT_CRC || queryResult == BrainDatabaseHelper.TRANSFERRING_FILE_STATE_EXIST_SAME_CRC) {
                LogMgr.w("本地没有文件，但数据库中有记录，现删除数据库中的记录，再重新插入新纪录1");
                mBrainDatabaseHelper.deleteTransferringFileInfo(mBrainDatabase, mFileAbsolutePath);
            }
            boolean insertResult = mBrainDatabaseHelper.insertTransferringFileInfo(mBrainDatabase, mFileAbsolutePath, crcCheck);
            if (insertResult == false) {
                LogMgr.e("处理发送请求时插入失败1");
            }
            //设置返回客户端的断点位置
            mBrokenPoint = 0;
        } else {
            LogMgr.i("analyzeFileReceiveHeader() 本地存在文件" + mFileAbsolutePath);
            if (appType != GlobalConfig.APP_TYPE_INNER_FILE && appType != GlobalConfig.APP_TYPE_SKILL_PLAYER && appType != GlobalConfig.APP_TYPE_STORE) {
                LogMgr.i("analyzeFileReceiveHeader() App不是内部文件传输，skillplayer，appStore类型");
                String newFileName = mReceiveFile.getName();
                originalFileName = FileUtils.getOriginalName(mReceiveFile, appType);
                hasSameNameFile = true;
                if (newFileName.equals(originalFileName)) {
                    LogMgr.i("新老文件文件名相同");
                } else {
                    LogMgr.i("新老文件文件名不相同");
                }
                mFileToMove = new File(BrainUtils.ABILIX + "Debug" + File.separator + newFileName);
                if (!mFileToMove.getParentFile().exists()) {
                    mFileToMove.getParentFile().mkdirs();
                }
                if (mFileToMove.exists()) {
                    FileUtils.deleteFile(mFileToMove);
                }
                mFileToMove.createNewFile();
                fos = new FileOutputStream(mFileToMove);
                bufferedOutputStream = new BufferedOutputStream(fos);

                //设置返回客户端的断点位置
                mBrokenPoint = 0;
            } else {
                LogMgr.i("analyzeFileReceiveHeader() App是内部文件传输，skillplayer，appStore类型");
                int queryResult = mBrainDatabaseHelper.queryTransferringFileInfo(mBrainDatabase, mFileAbsolutePath, crcCheck);
                if (queryResult == BrainDatabaseHelper.TRANSFERRING_FILE_STATE_EXIST_DIFFERENT_CRC) {
                    //数据库中存在相同文件名记录，但crc值不同
                    LogMgr.w("analyzeFileReceiveHeader() 数据库中存在相同文件名记录，但crc值不同");
                    mBrainDatabaseHelper.deleteTransferringFileInfo(mBrainDatabase, mFileAbsolutePath);
                    normalHandleWithDbAndFile();
                } else if (queryResult == BrainDatabaseHelper.TRANSFERRING_FILE_STATE_EXIST_SAME_CRC) {
                    LogMgr.i("analyzeFileReceiveHeader() 数据库中存在相同文件名记录，crc值相同");
                    if (isDownloadFromStart) {
                        LogMgr.i("analyzeFileReceiveHeader() 客户端请求从头开始传送");
                        normalHandleWithDbAndFile();
                    } else {
                        LogMgr.i("analyzeFileReceiveHeader() 客户端没有请求从头开始传送，进行断点续传");
                        fos = new FileOutputStream(mReceiveFile, true);
                        bufferedOutputStream = new BufferedOutputStream(fos);
                        //设置返回客户端的断点位置
                        mBrokenPoint = getBrokenPoint(mReceiveFile);
                    }
                } else if (queryResult == BrainDatabaseHelper.TRANSFERRING_FILE_STATE_NOT_EXIST) {
                    LogMgr.w("analyzeFileReceiveHeader() 数据库中不存在相同文件名记录");
                    normalHandleWithDbAndFile();
                } else {
                    LogMgr.e("analyzeFileReceiveHeader() 数据库状态异常");
                    normalHandleWithDbAndFile();
                }

//				FileUtils.deleteFile(mReceiveFile);
//				mReceiveFile.createNewFile();
//				fos = new FileOutputStream(mReceiveFile);
//				bufferedOutputStream = new BufferedOutputStream(fos);
            }
        }
    }

    /**
     * 处理下载文件的DB操作和文件流操作
     *
     * @throws IOException
     */
    private void normalHandleWithDbAndFile() throws IOException {
        FileUtils.deleteFile(mReceiveFile);
        mReceiveFile.createNewFile();
        fos = new FileOutputStream(mReceiveFile);
        bufferedOutputStream = new BufferedOutputStream(fos);
        mBrainDatabaseHelper.insertTransferringFileInfo(mBrainDatabase, mFileAbsolutePath, crcCheck);
        //设置返回客户端的断点位置
        mBrokenPoint = 0;
    }

    /**
     * 获取文件的大小
     *
     * @param file
     * @throws IOException
     */
    private int getBrokenPoint(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        int size = fileInputStream.available();
        LogMgr.i("getBrokenPoint() 断点位置 = " + size);
        fileInputStream.close();
        return size;
    }

    public static int initDeleteAndInstallServiceCount = 0;
    private void installApk(String path, String packageName){
        LogMgr.i("开始安装应用 path = "+path+" packageName = "+packageName);
        initDeleteAndInstallServiceCount++;
        try {
            if (BrainActivity.getmBrainActivity().getmInstallService() != null) {
                Utils.sendBroadcastBeforeInstallApk(context, GlobalConfig.ACTION_PACKAGE_NAME_TO_INSTALL, packageName);
                Thread.sleep(1500);
                BrainActivity.getmBrainActivity().getmInstallService().installApk(path, packageName);
                initDeleteAndInstallServiceCount = 0;
            }else {
                if(initDeleteAndInstallServiceCount > 5){
                    LogMgr.e("安装服务初始化5次失败，安装失败");
                    initDeleteAndInstallServiceCount = 0;
                    return;
                }
                LogMgr.e("安装服务尚未初始化，初始化后安装");
                BrainActivity.getmBrainActivity().initDeleteAndInstallService();
                Thread.sleep(500);
                installApk(path, packageName);
            }
        } catch (Exception e) {
            LogMgr.e("安装应用异常");
            e.printStackTrace();
        }
    }

    /**
     * 下载成功更新ui
     */
    private void downloadSuccess() {
        int mode = GlobalConfig.APP_DEFAULT;

        String filePath = mReceiveFile.getAbsolutePath();
        if (hasSameNameFile == true) {
            LogMgr.i("downloadSuccess() 不增加页面");
            String path = mReceiveFile.getParentFile().getAbsolutePath();
            FileUtils.deleteFile(mReceiveFile);
            String newFileName = mFileToMove.getName();
            File newFile = new File(path + File.separator + newFileName);
            mFileToMove.renameTo(newFile);
            filePath = newFile.getAbsolutePath();
            mode = GlobalConfig.FILE_DOWNLOAD_UPDATE;
        } else {
            LogMgr.i("downloadSuccess() 增加页面");
            mode = GlobalConfig.FILE_DOWNLOAD;
        }
        DataProcess.GetManger().sendBroadcast(filePath, mode);
    }

    /**
     * 关闭当前文件传输通道 关闭文件输入流 删除zip文件 删除文件夹
     */
    public void closeConnect(boolean deleteDownloadingFile) {
        if (DataProcess.ctx02 != null) {
            DataProcess.ctx02.channel().close();
        }

        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bufferedOutputStream != null) {
            try {
                bufferedOutputStream.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        if (hasSameNameFile && mFileToMove != null) {
            FileUtils.deleteFile(mFileToMove);
        }

        if (appType == GlobalConfig.APP_TYPE_SKILL_PLAYER) {
            LogMgr.i("SkillPlayer 文件删除处理");
            if (!TextUtils.isEmpty(totalFileName) && deleteDownloadingFile) {
                File zipFile = new File(BrainUtils.ABILIX_SKILLPLAYER_PATH + File.separator + totalFileName);
                if (zipFile.exists()) {
                    FileUtils.deleteFile(zipFile);
                }
            }
            if (!TextUtils.isEmpty(fileNameWithoutEndfix)) {
                File folder = new File(BrainUtils.ABILIX_SKILLPLAYER_PATH + File.separator + fileNameWithoutEndfix);
                if (folder.exists()) {
                    FileUtils.deleteDirectory(BrainUtils.ABILIX_SKILLPLAYER_PATH + File.separator + fileNameWithoutEndfix, true);
                }
            }
        } else if (appType == GlobalConfig.APP_TYPE_INNER_FILE
                || appType == GlobalConfig.APP_TYPE_STORE) {
            LogMgr.i("内部文件传输 appStore删除处理");
            if (mReceiveFile != null && mReceiveFile.exists() && deleteDownloadingFile) {
                FileUtils.deleteFile(mReceiveFile);
            }
        } else if (appType == GlobalConfig.APP_TYPE_MULTI_MEDIA) {
            LogMgr.i("多媒体传输删除处理");
            if (mReceiveFile != null && mReceiveFile.exists() && !hasSameNameFile) {
                //不允许存在不正确的多媒体文件
                FileUtils.deleteFile(mReceiveFile);
            }
        } else {
            LogMgr.i("其他关闭处理");
            if (mReceiveFile != null && mReceiveFile.exists() && !hasSameNameFile) {
                FileUtils.deleteFile(mReceiveFile);
                mReceiveFile = null;
            }
        }
    }
}
