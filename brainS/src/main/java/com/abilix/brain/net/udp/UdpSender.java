package com.abilix.brain.net.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.LogManager;

import com.abilix.brain.utils.LogMgr;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * UDP发送类，提供sendByUicast单播发送方法和sendByBroadcast广播发送方法。
 * 2017-5-18下午3:40:56
 * @author jingh
 */
public class UdpSender {
	private final static int SEND_BY_BROADCAST = 1;
	private final static int SEND_BY_UNICAST = 2;
	private static int LOCAL_PORT = 8887;
	private HandlerThread mUdpSenderThread;
	private UdpSenderThreadHandler mUdpSenderThreadHandler;
	private static UdpSender instance=new UdpSender();
	private DatagramSocket ds;
	private DatagramPacket mBroadcastPacket;
	private DatagramPacket mUnicastPacket;
	private String BROADCAST_IP = "255.255.255.255";
	private InetAddress unicastAddress;
	private String receiverIP;
	private int target_port;

	private UdpSender() {
		mUdpSenderThread = new HandlerThread("UdpSenderThread");
		mUdpSenderThread.start();
		mUdpSenderThreadHandler = new UdpSenderThreadHandler(mUdpSenderThread.getLooper());
		try {
			ds = new DatagramSocket(LOCAL_PORT);
			ds.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public static UdpSender getInstance() {
		return instance;
	}

	/**
	 * 通过单播发送
	 * 
	 * @param b
	 *            发送的数据
	 * @param ip
	 *            发送目标的IP地址
	 * @param port
	 *            发送目标的接收端口
	 */
	public synchronized void sendByUicast(byte[] b, String ip, int port) {
		Message msg = mUdpSenderThreadHandler.obtainMessage();
		msg.obj = b;
		receiverIP = ip;
		target_port = port;
		msg.what = SEND_BY_UNICAST;
		mUdpSenderThreadHandler.sendMessage(msg);
	}

	/**
	 * 通过广播发送
	 * 
	 * @param b
	 *            发送的数据
	 * @param port
	 *            发送目标的接收端口
	 */
	public synchronized void sendByBroadcast(byte[] b, int port) {
		target_port = port;
		Message msg = mUdpSenderThreadHandler.obtainMessage();
		msg.obj = b;
		msg.what = SEND_BY_BROADCAST;
		mUdpSenderThreadHandler.sendMessage(msg);
	}

	private class UdpSenderThreadHandler extends Handler {
		public UdpSenderThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SEND_BY_BROADCAST:
				byte[] data_b = (byte[]) msg.obj;
				sendByBroadCast(data_b);
				break;
			case SEND_BY_UNICAST:
				byte[] data_u = (byte[]) msg.obj;
				sendByUniCast(data_u);
				break;
			}
		}
	}

	private synchronized void sendByBroadCast(byte[] b) {
		try {
			mBroadcastPacket = new DatagramPacket(b, b.length, InetAddress.getByName(BROADCAST_IP), target_port);
			ds.send(mBroadcastPacket);
			LogMgr.d("broadcast send sucess");
		   Thread.sleep(5);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void sendByUniCast(byte[] b) {
		if (receiverIP == null) {
			return;
		}
		try {
			LogMgr.d("发送目的IP：" + receiverIP);
			unicastAddress = InetAddress.getByName(receiverIP);
			mUnicastPacket = new DatagramPacket(b, b.length, unicastAddress, target_port);
			ds.send(mUnicastPacket);
			LogMgr.d("unicast send sucess");
		    Thread.sleep(5);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destory() {
		mUdpSenderThreadHandler.removeCallbacksAndMessages(null);
		mUdpSenderThreadHandler = null;
		if (ds != null) {
			ds.close();
			ds = null;
		}
		mBroadcastPacket = null;
		mUnicastPacket = null;
		mUdpSenderThread = null;
		instance = null;
	}
}
