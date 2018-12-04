package com.abilix.brain.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.abilix.brain.utils.LogMgr;

public class UnicastSniffer {

	private static UnicastSniffer instance;
	private BroadcastSnifferHandler mBroadcastSnifferHandler;
	private HandlerThread mBroadcastSniffThread;
	public int UNICAST_LOCAL_PORT = 9999;
	private static IReceiveCallBack receiveUnicastCallBack;
	private DatagramSocket mUnicastDatagramSocket;
	private DatagramPacket mUnicastDatagramPacket;
	private byte[] b;

	private volatile boolean isReceive = true;

	private static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (receiveUnicastCallBack != null) {
				receiveUnicastCallBack.onReceiveSucess((byte[]) msg.obj);
			} else {
				LogMgr.e("receiveBroadcastCallBack==null");
			}
		};
	};

	private UnicastSniffer() {
		mBroadcastSniffThread = new HandlerThread("BroadcastSniffThread");
		mBroadcastSniffThread.start();
		mBroadcastSnifferHandler = new BroadcastSnifferHandler(mBroadcastSniffThread.getLooper());
		try {
			mUnicastDatagramSocket = new DatagramSocket(UNICAST_LOCAL_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public static UnicastSniffer getInstance() {
		if (instance == null) {
			synchronized (BroadcastSniffer.class) {
				if (instance == null) {
					instance = new UnicastSniffer();
				}
			}
		}
		return instance;
	}

	public synchronized void startReceiveBroadcast(IReceiveCallBack receiveCallBack) {
		isReceive = true;
		receiveUnicastCallBack = receiveCallBack;
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
			if (receiveUnicastCallBack == null) {
				return;
			}
			b = new byte[1024];
			mUnicastDatagramPacket = new DatagramPacket(b, b.length);
			try {
				mUnicastDatagramSocket.receive(mUnicastDatagramPacket);
				LogMgr.d("接收成功");
			} catch (IOException e) {
				e.printStackTrace();
			}
			byte[] receiveData = new byte[mUnicastDatagramPacket.getLength()];
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
		if (mUnicastDatagramSocket != null) {
			mUnicastDatagramSocket.close();
			mUnicastDatagramSocket = null;
		}
		mUnicastDatagramPacket = null;
		mBroadcastSniffThread = null;
		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
		receiveUnicastCallBack = null;
		instance = null;
	}
}
