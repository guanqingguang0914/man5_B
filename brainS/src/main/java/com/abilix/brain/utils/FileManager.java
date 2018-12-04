package com.abilix.brain.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.util.Log;

import com.abilix.brain.GlobalConfig;

/**
 * 文件下载管理 此下载为UDP下载。
 *
 * @author luox
 */
@Deprecated
public class FileManager {
    private FileDownload mFileDownload;
    private FileCallback mCallback;
    private boolean isUpdate = true;
    private static FileManager manager;
    private int stauts;
    private long startTime;
    public static final int OVERTIME = 14800;
    public static final int COPY = 56;
    public static final int buffi = 500;

    // private boolean isSucceed;

    public static FileManager getFileManager() {
        if (manager == null) {
            synchronized (FileManager.class) {
                if (manager == null) {
                    manager = new FileManager();
                }
            }
        }
        return manager;
    }

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            try {
                // stopMonitor();
                if (msg.what == GlobalConfig.FILE_DOWNLOAD) {
                    if (mCallback != null) {
                        mCallback.downloadSuccess(mFileDownload.file, stauts);
                    }
                } else if (msg.what == GlobalConfig.FILE_DOWNLOAD_U) {
                    if (mCallback != null) {
                        mCallback.downloadSuccess(mFileDownload.file, stauts);
                    }
                }
                if (mFileDownload != null) {
                    mFileDownload.close();
                    mFileDownload = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 开始下载文件
     *
     * @param mode = 0x01 vjc , 0x02 program , 0x03 skillplayer , 0x04 scratch
     */
    public void startDownload(int mode) {
        if (mFileDownload != null) {
            mFileDownload.close();
            mFileDownload = null;
        }
        mFileDownload = new FileDownload(mode);
        if (!mFileDownload.isAlive()) {
            mFileDownload.start();
            // startMonitor();
        }
    }

    /**
     * 文件下载
     *
     * @author luox
     */
    class FileDownload extends Thread {
        private DatagramSocket datagramSocket;
        private DatagramPacket datagramPacket;
        private byte readBag[];
        private byte indexBag[];
        private String fileName;
        private String pathName;
        private File file = null;
        private FileOutputStream fos = null;
        private InetAddress mInetAddress;
        private int mode;
        private byte[] data;
        private boolean isFileDownload = true;

        public FileDownload(int mode) {
            if (datagramSocket == null) {
                try {
                    readBag = new byte[buffi];
                    indexBag = new byte[4];
                    datagramSocket = new DatagramSocket(
                            BrainData.DATA_PORT_CONTRL_ROBOT);
                    datagramPacket = new DatagramPacket(readBag, readBag.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.mode = mode;
        }

        // /**
        // * 校验
        // *
        // * @param data
        // * @return
        // */
        // public boolean Check(byte[] data, int count) {
        // int j = 0;
        // int z = 0;
        // int length = data.length - 4;
        // for (int i = 0; i < length; i++) {
        // j += data[4 + i] < 0 ? data[4 + i] + 256 : data[4 + i];
        // }
        // String data3 = Integer.toHexString((data[3] < 0 ? data[3] + 256
        // : data[3]));
        // String data2 = Integer.toHexString((data[2] < 0 ? data[2] + 256
        // : data[2]));
        // if (Integer.valueOf(data2, 16) <= 15) {
        // data2 = "0" + data2;
        // }
        // z = Integer.valueOf(data3 + data2, 16);
        // Log.e("test", "z:" + z + " j:" + j);
        // if (z == j) {
        // return true;
        // } else {
        // LogMgr.e("出错的行号:" + count + " 数据:" + Arrays.toString(data));
        // return false;
        // }
        // }

        @Override
        public void run() {
            try {
                while (isFileDownload) {
                    // startTime = System.currentTimeMillis();
                    datagramSocket.receive(datagramPacket);
                    // Log.e("test", "下载开始");
                    // byte[] readBag=new byte[datagramPacket.getLength()];
                    // System.arraycopy(datagramPacket.getData(), 0, readBag, 0,
                    // datagramPacket.getLength());
                    // LogMgr.e("project filedownload data::"+Utils.bytesToString(readBag,
                    // readBag.length));
                    readBag = datagramPacket.getData();
                    // byte[] cosd = Arrays.copyOfRange(readBag, 46, 86);
                    // Check(cosd, 2);
                    if (readBag.length > 0) {
                        mInetAddress = datagramPacket.getAddress();
                        if (readBag[0] == BrainData.DATA_HEAD) {
                            indexBag[0] = readBag[1];
                            indexBag[1] = readBag[2];
                            indexBag[2] = readBag[3];
                            indexBag[3] = readBag[4];
                            // 判断第几包
                            int index = BrainUtils.bytearaytoint(indexBag);
                            // for (int i = 0; i < indexBag.length; i++) {
                            // Log.e("test", "indexBag[" + i + "]:"
                            // + indexBag[i]);
                            // }
                            // Log.e("test", "index:" + index);
                            if (index == 0) {
                                byte[] tempfile = new byte[24];
                                System.arraycopy(readBag, COPY, tempfile, 0,
                                        tempfile.length);
                                Log.e("test", "tempfile:" + Arrays.toString(tempfile));
                                int conut = 0;
                                for (int i = 0; i < 24; i++) {
                                    if (tempfile[i] != '\0') {
                                        conut++;
                                    } else {
                                        break;
                                    }
                                }
                                fileName = new String(tempfile, 0, conut,
                                        "UTF-8");
                                String name = null;
                                switch (mode) {
                                    case 0x02:
                                        name = BrainUtils.ABILIX_CHART;
                                        break;
                                    case 0x01:
                                        name = BrainUtils.ABILIX_PROJECT_PROGRAM;
                                        break;
                                    case 0x03:
                                        name = BrainUtils.ABILIX_SKILLPLAYER;
                                        break;
                                    case 0x04:
                                        name = BrainUtils.ABILIX_SCRATCH;
                                        break;
                                }
                                if (name == null) {
                                    return;
                                }
                                pathName = BrainUtils.ABILIX + name
                                        + File.separator + fileName + ".bin";
                                // name = null;
                            }
                            // 写入数据
                            if (BrainUtils.overArray(readBag) == BrainUtils.SUCCEED) {
                                Log.e("test", "index:" + index
                                        + " file is null：" + (file == null));
                                if (file == null) {
                                    file = new File(pathName);
                                    if (!file.exists()) {
                                        stauts = 200;
                                        isUpdate = true;
                                        file.createNewFile();
                                    } else {
                                        stauts = 201;
                                        isUpdate = false;
                                        FileWriter fw = new FileWriter(file);
                                        fw.write("");
                                        fw.flush();
                                        fw.close();
                                        fw = null;
                                    }
                                }
                                if (fos == null) {
                                    fos = new FileOutputStream(file, true);
                                }
                                byte[] buff = Arrays.copyOfRange(readBag, 6,
                                        readBag.length);
                                fos.write(buff);
                                fos.flush();
                                TimeUnit.MILLISECONDS.sleep(3);
                                datagramPacket.setAddress(mInetAddress);
                                datagramPacket.setData(readBag);
                                datagramSocket.send(datagramPacket);
                                TimeUnit.MILLISECONDS.sleep(3);
                                continue;
                            }// 写入完成
                            else if (BrainUtils.overArray(readBag) == BrainUtils.OVER) {
                                Log.e("test", "BrainUtils.OVER");
                                if (fos != null) {
                                    fos.close();
                                    fos = null;
                                }
                                isFileDownload = false;
                                mHandler.sendEmptyMessage(GlobalConfig.FILE_DOWNLOAD);
                                return;
                            }
                        }
                    }
                }
            } catch (SocketException e1) {
                e1.printStackTrace();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                error();
                return;
            } finally {
                close();
            }
        }

        public void close() {
            if (datagramSocket != null) {
                datagramSocket.disconnect();
                datagramSocket.close();
                datagramSocket = null;
                datagramPacket = null;
            }

            data = null;
            readBag = null;
            mInetAddress = null;
        }

        public void error() {
            isFileDownload = false;
            stauts = 404;
            if (file != null) {
                if (isUpdate) {
                    file.delete();
                } else {
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.flush();
                        TimeUnit.MILLISECONDS.sleep(3);
                        fos.close();
                        data = null;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
            Log.e("test", "文件下载失败");
            mHandler.sendEmptyMessage(GlobalConfig.FILE_DOWNLOAD_U);
        }
    }

    /**
     * 文件下载成功回调
     *
     * @author luox
     */
    public interface FileCallback {
        /**
         * @param file
         * @param stauts = 0x200 下载成功, 0x404 下载失败 , 0x202 下载成功不更新页面
         */
        public void downloadSuccess(File file, int stauts);
    }

    public void setFileCallback(FileCallback callback) {
        this.mCallback = callback;
    }

    private FileDownloadMonitor monitor;

    private void startMonitor() {
        if (monitor == null) {
            monitor = new FileDownloadMonitor();
            monitor.ismonitor = false;
            if (!monitor.isAlive()) {
                monitor.start();
            }
        }
    }

    private void stopMonitor() {
        if (monitor != null) {
            monitor.ismonitor = true;
            monitor.interrupt();
            monitor = null;
        }
    }

    /**
     * 文件下载监听
     *
     * @author luox
     */
    class FileDownloadMonitor extends Thread {
        private boolean ismonitor;

        @Override
        public void run() {
            while (!ismonitor) {
                try {
                    // if (mFileDownload != null) {
                    // Log.e("test", "阻塞前：mFileDownload.getState():"
                    // + mFileDownload.getState());
                    // }
                    // 休眠15秒 再去查看文件下载线程，如果在阻塞就抛出异常
                    TimeUnit.SECONDS.sleep(15);
                    if (mFileDownload != null) {
                        // Log.e("test", "阻塞后：mFileDownload.getState():"
                        // + mFileDownload.getState());
                        Log.e("test", "System.currentTimeMillis() - startTime:"
                                + (System.currentTimeMillis() - startTime));
                        // if (mFileDownload.getState() == Thread.State.WAITING)
                        // {
                        if (System.currentTimeMillis() - startTime > OVERTIME) {
                            mFileDownload.isFileDownload = false;
                            mFileDownload.close();
                            mFileDownload.interrupt();
                            mFileDownload = null;
                            ismonitor = true;
                            return;
                        } else {
                            ismonitor = false;
                        }
                    }
                } catch (Exception e) {
                    ismonitor = true;
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

}
