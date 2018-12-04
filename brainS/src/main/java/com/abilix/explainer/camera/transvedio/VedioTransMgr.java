package com.abilix.explainer.camera.transvedio;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.abilix.brain.Application;
import com.abilix.brain.BrainService;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;
import com.abilix.explainer.camera.CameraStateCallBack;
import com.abilix.explainer.camera.IRobotCamera;
import com.abilix.explainer.camera.systemcamera.SystemCamera;
import com.abilix.explainer.camera.usbcamera.UsbCamera;
import com.abilix.usbcamera.client.PreviewCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 视频传输功能类
 */
public class VedioTransMgr {
    private final static int VEDIO_TRANS_PORT = 40004;
    private final static int MAX_DATA = 1024;
    private static int sendTimes;
    private static byte[] backbuf = new byte[]{0x01, 0x02, 0x03, 0x00, 0x00};
    private static YuvImage image;
    private static ByteArrayOutputStream stream;
    private static byte[] new_data;
    private static byte[] readData = new byte[MAX_DATA];
    private static byte[] sendData;
    private static ByteArrayInputStream mByteArrayInputStream;
    public static final Lock lockData = new ReentrantLock(); // 锁对象

    /**视屏传输Thread*/
    private static HandlerThread mVedioTransThread;
    /**预览Thread*/
    private static HandlerThread mPreviewThread;
    /**视屏传输Handler*/
    private static VedioTransHandler mVedioTransHandler;
    /**预览Handler*/
    private static Handler mPreviewHandler;
    private static Object mLock = new Object();
    private static VedioTransMgr instance = new VedioTransMgr();

    //net
    private static DatagramSocket ds;
    private static DatagramPacket dpHead;
    private static DatagramPacket dpData;
    private static InetAddress addr;

    private VedioTransMgr() {
        LogMgr.d("init VedioTransMgr");
        if (mVedioTransHandler == null) {
            mVedioTransThread = new HandlerThread("VedioTransThread");
            mVedioTransThread.start();
            mVedioTransHandler = new VedioTransHandler(mVedioTransThread.getLooper());
            mPreviewThread = new HandlerThread("PreviewThread");
            mPreviewThread.start();
            mPreviewHandler = new Handler(mPreviewThread.getLooper());
        }
    }

    /**
     * 开始USB摄像头传输
     */
    public synchronized static void startUsbVedioTrans() {
        com.abilix.explainer.utils.LogMgr.e("start vedio trans");
        if (instance == null) {
            instance = new VedioTransMgr();
        }
        mVedioTransHandler.post(new Runnable() {

            @Override
            public void run() {
                if (ds == null) {
                    initVedioTransSocket(BrainService.getmBrainService().getIp());
                }
                UsbCamera.create().preview(Application.getInstance(), new PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data) {
                        synchronized (mLock) {
                            LogMgr.e("get preview data");
                            image = new YuvImage(data, ImageFormat.NV21, UsbCamera.getWidth(), UsbCamera.getHeight(), null);
                            if (image != null) {
                                if (lockData.tryLock()) {
                                    LogMgr.d("new ByteArrayOutputStream");
                                    if (stream==null){
                                        LogMgr.d("send message to start while");
                                        mVedioTransHandler.sendEmptyMessage(0);
                                    }
                                    stream = new ByteArrayOutputStream();
                                    image.compressToJpeg(new Rect(0, 0, UsbCamera.getWidth(), UsbCamera.getHeight()), 80, stream);
                                    lockData.unlock();
                                }
                            }
                        }
                    }
                }, new CameraStateCallBack() {
                    @Override
                    public void onState(int state) {

                    }
                });
            }
        });

    }



    /**
     * 开始系统摄像头的视屏传输
     */
    public synchronized static void startVedioTrans() {
        LogMgr.e("start vedio trans");
        if (instance == null) {
            instance = new VedioTransMgr();
        }
        mPreviewHandler.post(new Runnable() {
            @Override
            public void run() {
                if (ds == null) {
                    initVedioTransSocket(BrainService.getmBrainService().getIp());
                }

                ((SystemCamera) SystemCamera.create()).preview(Application.getInstance(), new PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data) {
                        LogMgr.e("get preview data");
                        image = new YuvImage(data, SystemCamera.getFormat(), SystemCamera.getWidth(), SystemCamera.getHeight(), null);
                        if (image != null) {
                            if (lockData.tryLock()) {
                                LogMgr.d("new ByteArrayOutputStream");
                                if (stream==null){
                                    LogMgr.d("send message to start while");
                                    mVedioTransHandler.sendEmptyMessage(0);
                                }
                                stream = new ByteArrayOutputStream();
                                image.compressToJpeg(new Rect(0, 0, SystemCamera.getWidth(), SystemCamera.getHeight()), 80, stream);
                                lockData.unlock();
                            }
                        }
                    }
                });
            }
        });
    }

    public synchronized static void stopVedioTrans() {
        com.abilix.explainer.utils.LogMgr.e("stop vedio trans");
        isSend = false;
        mPreviewHandler.post(new Runnable() {
            @Override
            public void run() {
                destoryVedioTransSocket();
                UsbCamera.create().stopPreview();
                IRobotCamera camera;
                if ((camera =SystemCamera.getSystemCameraInstance()) !=null)
                        camera.destory();
                stream=null;
            }
        });
    }

    private class VedioTransHandler extends Handler {
        public VedioTransHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogMgr.e("handleMessage");
            isSend = true;
            sendVedioData();
        }
    }

    private static boolean isSend = true;

    private void sendVedioData() {
        while (isSend) {
            try {
                LogMgr.d("while 循环发送img");
                if (lockData.tryLock()) {
                    LogMgr.d("getLock");
                    if (stream != null) {
                        mByteArrayInputStream = new ByteArrayInputStream(stream.toByteArray());
                        stream.flush();
                    } else {
                        lockData.unlock();
                        Thread.sleep(50);
                        continue;
                    }
                    lockData.unlock();
                } else {
                    Thread.sleep(50);
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mByteArrayInputStream.available() % MAX_DATA > 0) {
                sendTimes = mByteArrayInputStream.available() / MAX_DATA + 1;
            } else {
                sendTimes = mByteArrayInputStream.available() / MAX_DATA;
            }
            byte[] byte_sendTimes = Utils.intToBytes(sendTimes);
            System.arraycopy(byte_sendTimes, 0, backbuf, 3, 2);
            LogMgr.d("img send times:" + sendTimes);
            try {
                dpHead = new DatagramPacket(backbuf, backbuf.length, addr, 40004);
                ds.send(dpHead);
               // LogMgr.e("send dataHead sucess");
                int length = 0;
                readData = new byte[MAX_DATA];
                // 分次发送图片数据
                while ((length = mByteArrayInputStream.read(readData)) != -1) {
                    dpData = new DatagramPacket(readData, readData.length, addr, 40004);
                    ds.send(dpData);
                    Thread.sleep(2);
                }
                long gapTime = System.currentTimeMillis() - sendTime;
                sendTime = System.currentTimeMillis();
                if (gapTime > 2000) {
                    LogMgr.e("send data sucess:+++++" + gapTime + "++++++");
                }
          //      LogMgr.e("send data sucess:" + gapTime + "---------");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private long sendTime = 0;

    /**
     * 初始化传输地址，socket
     * @param receiverIP
     */
    private static void initVedioTransSocket(String receiverIP) {
        try {
            addr = InetAddress.getByName(receiverIP);
            ds = new DatagramSocket(8886);
            ds.setBroadcast(true);
            LogMgr.e("init vedio trans port sucess");
        } catch (Exception e) {
            e.printStackTrace();
            LogMgr.e("init vedio trans port error:" + e.toString());
        }


    }

    private static void destoryVedioTransSocket() {
        if (ds != null) {
            ds.close();
            ds = null;
        }
        if (addr != null) {
            addr = null;
        }
    }

}
