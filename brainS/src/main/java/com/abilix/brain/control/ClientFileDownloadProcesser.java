package com.abilix.brain.control;

import com.abilix.brain.Application;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.data.DataBuffer;
import com.abilix.brain.data.DataProcess;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

/**
 * 群控对应的文件下载
 * @author yangz
 * @date 2017/12/4
 */

public class ClientFileDownloadProcesser {

    //添加群控对应的
    public static String serverIP = null;
    public static boolean hasclient = false;

    private static ClientFileDownloadProcesser instance;
    private ClientFileDownloadProcesser(){

    }

    public static ClientFileDownloadProcesser getInstance() {
        // 单例
        if (instance == null) {
            synchronized (ClientFileDownloadProcesser.class) {
                if (instance == null) {
                    instance = new ClientFileDownloadProcesser();
                }
            }
        }
        return instance;
    }

    //群控对应的获取文件
    private long crcCheckG;
    public static long binLen = 0;
    private String binName = null;
    public String binPath = FileUtils.DATA_PATH;
    public int recevLen0 = 0;
    public long recevLenAll = 0;
    private long lastRecTime = 0;
    public boolean ifFirstRec = false;
    public boolean reConection = false;

    /**本地计算出的crc值*/
    CRC32 crc32 = null;
    /**保存发送文件的文件对象*/
    private File mReceiveFile;
    private FileOutputStream fos;
    /**保存文件时文件写入流*/
    private BufferedOutputStream bufferedOutputStream;

    public boolean initGetFile(byte[] recieve, int recLen) {// 开始接收bin文件
        if (recLen <= 32){
            return false;
        }
//        LogMgr.e("initGetFile");
        try {
            crc32 = new CRC32();
            int ipAddress = Utils.bytesToInt4(recieve, 10);
//            DataBuffer.serverIP = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
//                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
            serverIP = String.format("%d.%d.%d.%d", (ipAddress >> 24 & 0xff), (ipAddress >> 16 & 0xff),
                    (ipAddress >> 8 & 0xff), (ipAddress  & 0xff));
            crcCheckG = Utils.bytesToLong8HL(recieve, 14);
            binLen = Utils.bytesToLong8HL(recieve, 22);
//            if (binLen < 10){
//                return false;
//            }
            byte[] binNamebyte = new byte[recLen - 32];
            System.arraycopy(recieve, 30, binNamebyte, 0, binNamebyte.length);
            binName = new String(binNamebyte, "UTF-8");
            binPath = FileUtils.MOVEBIN +"Download"+ File.separator + binName;
//            LogMgr.e("binName = " + binName + ";crcCheckG = " + crcCheckG + "DataBuffer.serverIP = " + DataBuffer.serverIP);
//            Log.i("ClientUdp_YQ", "ClientUdp_YQ123  接收到的bin文件名" + binName + "   路径" + binPath + " IP "
//                    + DataBuffer.serverIP);
            return recvFile(binPath, crcCheckG);
        } catch (Exception e) {
//            Log.i("ClientUdp_YQ", "ClientUdp_YQ准备接收文件出错" + e);
            return false;
        }
    }
    public boolean recvFile(String binPath00, long crcCheck00) {
        try {
            mReceiveFile = new File(binPath00);
            // 补全路径上的文件夹
            if (!mReceiveFile.getParentFile().exists()) {
                mReceiveFile.getParentFile().mkdirs();
            }
            if (!mReceiveFile.exists()) {
                mReceiveFile.createNewFile();
            } else {// 存在
                if (crcCheck00 == Utils.getCRC32(binPath00)) {
                    return false;// 文件已经存在不再接收文件
                } else {
                    FileUtils.deleteFile(mReceiveFile);
                    mReceiveFile.createNewFile();
                }
            }
            recevLenAll = 0;
            fos = new FileOutputStream(mReceiveFile);
            bufferedOutputStream = new BufferedOutputStream(fos);
            return true;
        } catch (Exception e) {
//            Log.i("ClientUdp_YQ", "ClientUdp_YQ准备接收文件出错" + e);
            return false;
        }
    }
    public void closeFile() {
        try {
            recevLenAll = 0;
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized void DataTypeGroup(byte[] recv) {// 接收数据存文件
        ifFirstRec = false;
        recevLen0 = recv.length;
        recevLenAll = recevLenAll + (long) recevLen0;
        try {
            crc32.update(recv, 0, recevLen0);
            bufferedOutputStream.write(recv, 0, recevLen0);
        } catch (Exception e) {
//            Log.i("DataProcess", "ClientUdp_YQ  接收的文件校验出错" + e);
        }
        LogMgr.i("DataProcess", "ClientUdp_YQ sendData 已接收长度 " + recevLenAll + "  总数据长度 " + binLen);
        if (recevLenAll >= binLen) {
            try {
//				Log.i("DataProcess", "ClientUdp_YQ sendData 已接收长度 " + recevLenAll + "  总数据长度 " + binLen);
                if (recevLenAll == binLen) {// 接收完毕　
                    if (crcCheckG == crc32.getValue()) {
                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();
                        fos.close();
                        recevLenAll = 0;
                        reConection = true;
                    } else {// 校验不通过，重新建立tcp连接请求

                    }
                    recevLenAll = 0;
                    reConection = true;
                } else{
                    reConection = false;
                }
                LogMgr.i("sendBroadcast");
                DataProcess.GetManger().sendBroadcast(null, GlobalConfig.FILE_DOWNLOAD_GROUP);
                Application.getInstance().setisKeepGroupLoadingComPelte(true);
                stopCtx01(88);
                // stopCtx01();//不再立马关闭，等待2s后再关闭
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {//
            lastRecTime = System.currentTimeMillis();
            reConection = false;
        }
    }
    public void stopCtx01(int n) {// 断开连接
        if (DataProcess.ctx03 != null) {
            DataProcess.ctx03.channel().close();
//            Log.i("DataProcess", "ClientUdp_YQ 强制断开tcp连接  " + n);
            DataProcess.GetManger().setCtx03(null);
        }
        if (n == 88 && !reConection) {
            closeFile();
            if (recvFile(binPath, crcCheckG)) {
                new Thread(new ClientRunnable()).start();// 没传成功再次重连
            }
        }
    }
    /*
     * arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
     * 实现数组之间的复制, src:源数组； srcPos:源数组要复制的起始位置； dest:目的数组； destPos:目的数组放置的起始位置；
     * length:复制的长度。 注意：src and dest都必须是同类型或者可以进行转换类型的数组． 有趣的是这个函数可以实现自己到自己复制
     */
    public synchronized void sendData() {// 数据发送命令
        LogMgr.d("sendData");
        DataProcess.GetManger().sendMsg3(getDateByte(102));//
        ifFirstRec = true;
//        new Thread() {// 超时未收到数据断开连接
//            @Override
//            public void run() {
//                lastRecTime = System.currentTimeMillis();
//                while (ifFirstRec && (System.currentTimeMillis() - lastRecTime <= 6000)) {// 发送数据6000s还没有再接收到数据就中断tcp连接
//                    try {
//                        sleep(100);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (ifFirstRec) {
//                    stopCtx01(22);
//                }
//
//                while (!ifFirstRec && (System.currentTimeMillis() - lastRecTime <= 2000)) {// 接收到数据后2000ms还没有再接收到数据就中断tcp连接
//                    try {
//                        sleep(100);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                stopCtx01(22);
//            }
//        }.start();
    }
    public byte[] getDateByte(int cmd) {// 指令打包
        byte[] data0 = new byte[12]; // 发送数据
        data0[0] = (byte) 0xff;// 报头
        data0[1] = (byte) (0xff & ((12 - 3) >> 8));// 长度高位
        data0[2] = (byte) (0xff & (12 - 3));// 长度低位
        data0[3] = (byte) (0);//
        data0[4] = (byte) (0);//
        data0[5] = (byte) cmd;// 指令种类
        data0[8] = (byte) 3;// 机器人类型
        data0[10] = XORcheckSend(data0);
        data0[11] = (byte) 0xAA;// 报尾
        return data0;
    }
    public static byte XORcheckSend(byte[] buf) {// 传参是完整报文,生成CRC
        int len = buf.length;
        if (len < 12) {
            return -1;
        }

        byte crc = buf[0];
        for (int i = 1; i <= len - 3; i++) {
            crc = (byte) (crc ^ (buf[i]));
        }
        return crc;
    }
}
