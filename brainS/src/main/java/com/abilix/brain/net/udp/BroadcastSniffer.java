package com.abilix.brain.net.udp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.abilix.brain.utils.LogMgr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * 广播接收类。
 * 2017-5-18下午3:40:33
 * @author jingh
 */
public class BroadcastSniffer {

	private static BroadcastSniffer instance;
	private BroadcastSnifferHandler mBroadcastSnifferHandler;
	private HandlerThread mBroadcastSniffThread;
	public int BROADCAST_LOCAL_PORT = 7777;
	// public int UNICAST_LOCAL_PORT = 9999;
	private static IReceiveCallBack receiveBroadcastCallBack;
	// private IReceiveCallBack receiveUnicastCallBack;
	private DatagramSocket mBroadcastDatagramSocket;
	// private DatagramSocket mUnicastDatagramSocket;
	private DatagramPacket mBroadcastDatagramPacket;
	private byte[] b;

	private volatile boolean isReceive = true;

	private static Handler mHandler = new Handler() {
		@Override
        public void handleMessage(Message msg) {
			if (receiveBroadcastCallBack != null) {
				receiveBroadcastCallBack.onReceiveSucess((byte[]) msg.obj);
			} else {
				LogMgr.e("receiveBroadcastCallBack==null");
			}
		};
	};

	private BroadcastSniffer() {
		mBroadcastSniffThread = new HandlerThread("BroadcastSniffThread");
		mBroadcastSniffThread.start();
		mBroadcastSnifferHandler = new BroadcastSnifferHandler(mBroadcastSniffThread.getLooper());
		try {
			mBroadcastDatagramSocket = new DatagramSocket(BROADCAST_LOCAL_PORT);
			// mUnicastDatagramSocket = new DatagramSocket(UNICAST_LOCAL_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public static BroadcastSniffer getInstance() {
		if (instance == null) {
			synchronized (BroadcastSniffer.class) {
				if (instance == null) {
					instance = new BroadcastSniffer();
				}
			}
		}
		return instance;
	}

	public synchronized void  startReceiveBroadcast(IReceiveCallBack receiveCallBack) {
		isReceive = true;
		receiveBroadcastCallBack = receiveCallBack;
		mBroadcastSnifferHandler.sendEmptyMessage(0);
	}

	private class BroadcastSnifferHandler extends Handler {
		public BroadcastSnifferHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			receiveData();
		}
	}

	private void receiveData() {
		while (isReceive) {
			if (receiveBroadcastCallBack == null) {
				return;
			}
			b = new byte[1024];
			mBroadcastDatagramPacket = new DatagramPacket(b, b.length);
			try {
				mBroadcastDatagramSocket.receive(mBroadcastDatagramPacket);
				LogMgr.d("接收成功");
			} catch (IOException e) {
				e.printStackTrace();
			}
			byte[] receiveData = new byte[mBroadcastDatagramPacket.getLength()];
			System.arraycopy(b, 0, receiveData, 0, receiveData.length);
			Message msg = mHandler.obtainMessage();
			msg.obj = receiveData;
			mHandler.sendMessage(msg);
		}

	}

	public void destory() {
		mBroadcastSnifferHandler.removeCallbacksAndMessages(null);
		mBroadcastSnifferHandler = null;
		isReceive = false;
		if (mBroadcastDatagramSocket != null) {
			mBroadcastDatagramSocket.close();
			mBroadcastDatagramSocket = null;
		}
		mBroadcastDatagramPacket = null;
		mBroadcastSniffThread = null;
		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
		receiveBroadcastCallBack = null;
		instance = null;
	}
}
