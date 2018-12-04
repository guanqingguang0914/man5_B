package com.abilix.brain.ui;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.abilix.brain.utils.BrainData;

/**
 * 发送Camera数据线程,未被使用。
 */
public class SendCameraRunnable implements Runnable {
    /**
     * 发送Camera数据线程
     */
    private final int MAXlen = 1024;
    private byte byteBuffer[] = new byte[MAXlen];

    @Override
    public void run() {
        // 创建一个DatagramSocket对象，并指定监听的端口号
        DatagramSocket socket = null;
        DatagramPacket sendPacket = null;
        byte[] backbuf = new byte[]{0x01, 0x02, 0x03, 0x00, 0x00};
        ByteArrayInputStream inputstream = null;
        int i = 0, sleeptime = 0, datalen = 0;
        int amount;
        VideoBuffer.outstream1 = null;
        VideoBuffer.outstream2 = null;
        while (VideoBuffer.cameraRefresh) {
            try {
                while ((VideoBuffer.mInetAddress == null || VideoBuffer.outstream1 == null) && VideoBuffer.cameraRefresh) {
                    Thread.sleep(100);
                    continue;
                }

                if (socket == null) {
                    socket = new DatagramSocket(BrainData.VIDEO_PORT);
                }
                while (VideoBuffer.mInetAddress != null && VideoBuffer.cameraRefresh && VideoBuffer.outstream1 != null) { // 如果发送速度快于接收速度，接收端会丢失数据

                    i = 0;
                    if (VideoBuffer.lockData.tryLock()) {
                        try {
                            if (VideoBuffer.outstream1.size() < 1) {
                                Thread.sleep(10);
                            } else {
                                inputstream = new ByteArrayInputStream(VideoBuffer.outstream1.toByteArray());
                                if (inputstream.available() % MAXlen > 0) {
                                    datalen = inputstream.available() / MAXlen + 1;// 长度
                                } else {
                                    datalen = inputstream.available() / MAXlen;
                                }
                            }

                        } finally {
                            VideoBuffer.lockData.unlock();
                        }
                    } else if (VideoBuffer.lockData.tryLock()) {
                        try {
                            if (VideoBuffer.outstream1.size() < 1) {
                                Thread.sleep(10);
                            } else {
                                inputstream = new ByteArrayInputStream(
                                        VideoBuffer.outstream2.toByteArray());
                                if (inputstream.available() % MAXlen > 0) {
                                    datalen = inputstream.available() / MAXlen
                                            + 1;// 长度
                                } else {
                                    datalen = inputstream.available() / MAXlen;
                                }
                            }
                        } finally {
                            VideoBuffer.lockData.unlock();
                        }
                    } else {
                        try {
                            VideoBuffer.lockData.lock();
                            if (VideoBuffer.outstream1.size() < 1) {
                                Thread.sleep(10);
                            } else {
                                inputstream = new ByteArrayInputStream(
                                        VideoBuffer.outstream1.toByteArray());
                                if (inputstream.available() % MAXlen > 0) {
                                    datalen = inputstream.available() / MAXlen
                                            + 1;// 长度
                                } else {
                                    datalen = inputstream.available() / MAXlen;
                                }
                            }
                        } finally {
                            VideoBuffer.lockData.unlock();
                        }
                    }

                    backbuf[3] = (byte) ((datalen & 0xff00) >> 8);
                    backbuf[4] = (byte) (datalen & 0xff);

                    sendPacket = new DatagramPacket(backbuf, 5, VideoBuffer.mInetAddress, BrainData.VIDEO_PORT);
                    socket.send(sendPacket); // 发送报头
                    while ((amount = inputstream.read(byteBuffer)) != -1) {// 将inputstream分为n个byteBuffer发送出去
                        sendPacket = new DatagramPacket(byteBuffer, amount, VideoBuffer.mInetAddress, BrainData.VIDEO_PORT);// 封装返回给客户端的数据
                        socket.send(sendPacket); // 通过套接字反馈服务器数据
                        i++;
                        if (datalen < 36) {
                            if (i % 2 == 0) {// datalen<32时丢帧很大，越大反而效果越好
                                Thread.sleep(2);
                                sleeptime = 4;
                            }
                        } else {
                            if (i % 16 == 0) {// datalen<32时丢帧很大，越大反而效果越好
                                sleeptime = i / 8;
                                Thread.sleep(2);
                            }
                        }
                    }
                    // "Brain_yq ++++++++++ 发送了一张图片 +++++++++++");
                    Thread.sleep(sleeptime);
                }
            } catch (Exception e) {
                if (socket != null) {
                    socket.close(); // 关闭套接字
                }
                socket = null;
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close();
                }
                sendPacket = null;
                socket = null;
            }
        }
        if (socket != null) {
            socket.close();
        }
        socket = null;
        sendPacket = null;
        inputstream = null;
    }
}
