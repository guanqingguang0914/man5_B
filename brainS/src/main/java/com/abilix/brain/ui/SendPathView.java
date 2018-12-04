package com.abilix.brain.ui;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.abilix.brain.ui.PathView.onPathListener;
import com.abilix.brain.utils.BrainData;
import com.abilix.brain.utils.LogMgr;

/**
 * 发送画线数据功能类。
 *
 * @author luox
 */
public class SendPathView {

    private PathViewThread mPathViewThread;
    volatile private boolean isPathView;
    private InetAddress mInetAddress;
    private PathView mPathView;
    private Object mObject = new Object();

    public SendPathView(PathView pathView, InetAddress inetAddress) {
        this.mPathView = pathView;
        this.mInetAddress = inetAddress;
    }

    /**
     * 打开显示窗
     */
    public void openPathView() {
        isPathView = true;
        mPathView.clear();
        mPathView.initCanvas();
        mPathView.setOnPathListener(new onPathListener() {
            @Override
            public void onPath(byte[] path) {
                // Log.e("test", Arrays.toString(path));
                if (mPathViewThread == null) {
                    mPathViewThread = new PathViewThread(path);
                    if (!mPathViewThread.isAlive()) {
                        mPathViewThread.start();
                    }
                }
                mPathViewThread.setByte(path);
                synchronized (mObject) {
//					if (mPathViewThread.getState() == Thread.State.WAITING)
                    mObject.notify();
                }
            }
        });
    }

    /**
     * 退出显示窗
     */
    public void stopPathView() {
        isPathView = false;
        if (mPathViewThread != null) {
            mPathViewThread.setByte(null);
            mPathViewThread.interrupt();
            mPathViewThread.close();
            mPathViewThread = null;
        }
        if (mPathView != null) {
            mPathView.destroy();
            mPathView.setOnPathListener(null);
            mPathView = null;
        }
    }

    /**
     * 清空显示窗
     */
    public void clearPathView() {
        mPathView.clear();
    }

    /**
     * 发送显示窗数据
     *
     * @author luox
     */
    class PathViewThread extends Thread {
        private DatagramSocket ds = null;
        private DatagramPacket dp = null;
        private byte[] data;
        private byte[] mOldData;

        public PathViewThread(byte[] data) {
            this.data = data;
        }

        public void setByte(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {
            try {
                if (ds == null) {
                    ds = new DatagramSocket(BrainData.WINDOW_PORT);
                    if (mInetAddress != null) {
                        dp = new DatagramPacket(data, data.length,
                                mInetAddress, BrainData.WINDOW_PORT);
                    }
                }
                while (isPathView) {
//					LogMgr.i("发送划线数据到pad data = "+ Utils.bytesToString(data,data.length));
                    dp.setData(data);
                    ds.send(dp);

                    synchronized (mObject) {
                        mObject.wait();
                    }

//					if (!Arrays.equals(data, mOldData)) {
//						dp.setData(data);
//						ds.send(dp);
//						mOldData = Arrays.copyOf(data, data.length);
//					} else {
//						synchronized (mObject) {
//							TimeUnit.MILLISECONDS.sleep(6);
//							mObject.wait();
//						}
//					}
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                if (ds != null) {
                    ds.close();
                }
                ds = null;
                isPathView = false;
                mOldData = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
