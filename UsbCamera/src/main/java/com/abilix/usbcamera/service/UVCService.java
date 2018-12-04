package com.abilix.usbcamera.service;

/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: UVCService.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb, jin/libuvc, jni/rapidjson folder may have a different license, see the respective files.
 */

import android.app.Service;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.SparseArray;
import android.view.Surface;

import com.abilix.usbcamera.IPreviewCallback;
import com.abilix.usbcamera.ITakePictureCallback;
import com.abilix.usbcamera.IUVCService;
import com.abilix.usbcamera.IUVCServiceCallback;
import com.abilix.usbcamera.IUVCServiceOnFrameAvailable;
import com.abilix.usbcamera.IUVCSlaveService;
import com.abilix.usbcamera.utils.LogMgr;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;

public class UVCService extends Service {
	private USBMonitor mUSBMonitor;

	public UVCService() {
		LogMgr.d("Constructor:");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogMgr.d("onCreate:");
		if (mUSBMonitor == null) {
			mUSBMonitor = new USBMonitor(getApplicationContext(), mOnDeviceConnectListener);
			mUSBMonitor.register();
		}
	}

	@Override
	public void onDestroy() {
		LogMgr.e("onDestroy:");
		if (checkReleaseService()) {
			if (mUSBMonitor != null) {
				mUSBMonitor.unregister();
				mUSBMonitor = null;
			}
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		LogMgr.d("onBind:" + intent);
		if (IUVCService.class.getName().equals(intent.getAction())) {
			LogMgr.i("return mBasicBinder");
			return mBasicBinder;
		}
		if (IUVCSlaveService.class.getName().equals(intent.getAction())) {
			LogMgr.i("return mSlaveBinder");
			return mSlaveBinder;
		}
		return null;
	}

	@Override
	public void onRebind(final Intent intent) {

		LogMgr.d("onRebind:" + intent);
	}

	@Override
	public boolean onUnbind(final Intent intent) {
		LogMgr.e("onUnbind:" + intent);
		/*if (checkReleaseService()) {
			if (mUSBMonitor != null) {
				mUSBMonitor.unregister();
				mUSBMonitor = null;
				LogMgr.d("USBMonitor unregister");
			}
		//	LogMgr.e("killProcess!!!");
		//	Process.killProcess(Process.myPid());
		}*/
		return true;
	}

	// ********************************************************************************
	private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
		@Override
		public void onAttach(final UsbDevice device) {

			LogMgr.d("OnDeviceConnectListener#onAttach:");
		}

		@Override
		public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {

			LogMgr.d("OnDeviceConnectListener#onConnect:");

			final int key = device.hashCode();
			CameraServer service;
			synchronized (sServiceSync) {
				service = sCameraServers.get(key);
				if (service == null) {
					service = CameraServer.createServer(UVCService.this, ctrlBlock, device.getVendorId(), device.getProductId());
					sCameraServers.append(key, service);
				} else {
					LogMgr.w("service already exist before connection");
				}
				sServiceSync.notifyAll();
			}
		}

		@Override
		public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {

			LogMgr.d("OnDeviceConnectListener#onDisconnect:");
			removeService(device);
		}

		@Override
		public void onDettach(final UsbDevice device) {

			LogMgr.d("OnDeviceConnectListener#onDettach:");
			removeService(device);
		}

		@Override
		public void onCancel() {

			LogMgr.d("OnDeviceConnectListener#onCancel:");
			synchronized (sServiceSync) {
				sServiceSync.notifyAll();
			}
		}
	};

	private void removeService(final UsbDevice device) {
		LogMgr.e("removeService===>");
		final int key = device.hashCode();
		synchronized (sServiceSync) {
			final CameraServer service = sCameraServers.get(key);
			if (service != null)
				service.release();
			sCameraServers.remove(key);
			sServiceSync.notifyAll();
		}
		/*if (checkReleaseService()) {
			if (mUSBMonitor != null) {
				mUSBMonitor.unregister();
				mUSBMonitor = null;
			}
		}*/
		LogMgr.e("removeService<===");
	}

	// ********************************************************************************
	private static final Object sServiceSync = new Object();
	private static final SparseArray<CameraServer> sCameraServers = new SparseArray<CameraServer>();

	/**
	 * get CameraService that has specific ID<br>
	 * if zero is provided as ID, just return top of CameraServer
	 * instance(non-blocking method) if exists or null.<br>
	 * if non-zero ID is provided, return specific CameraService if exist. block
	 * if not exists.<br>
	 * return null if not exist matched specific ID<br>
	 * 
	 * @param serviceId
	 * @return
	 */
	private static CameraServer getCameraServer(final int serviceId) {
		synchronized (sServiceSync) {
			CameraServer server = null;
			if ((serviceId == 0) && (sCameraServers.size() > 0)) {
				server = sCameraServers.valueAt(0);
			} else {
				server = sCameraServers.get(serviceId);
				if (server == null)
					try {
						LogMgr.i("waitting for service is ready");
						sServiceSync.wait();
					} catch (final InterruptedException e) {
					}
				server = sCameraServers.get(serviceId);
			}
			return server;
		}
	}

	/**
	 * @return true if there are no camera connection
	 */
	private static boolean checkReleaseService() {
		LogMgr.e("checkReleaseService===>");
		CameraServer server = null;
		synchronized (sServiceSync) {
			final int n = sCameraServers.size();

			LogMgr.d("checkReleaseService:number of service=" + n);
			for (int i = 0; i < n; i++) {
				server = sCameraServers.valueAt(i);
				LogMgr.i("checkReleaseService:server=" + server + ",isConnected=" + (server != null ? server.isConnected() : false));
				if (server != null && !server.isConnected()) {
					sCameraServers.removeAt(i);
					server.release();
				}
			}
			LogMgr.e("checkReleaseService<===");
			return sCameraServers.size() == 0;
		}

	}

	// ********************************************************************************
	private final IUVCService.Stub mBasicBinder = new IUVCService.Stub() {
		private IUVCServiceCallback mCallback;

		@Override
		public int select(final UsbDevice device, final IUVCServiceCallback callback) throws RemoteException {

			LogMgr.d("mBasicBinder#select:device=" + (device != null ? device.getDeviceName() : null));
			mCallback = callback;
			final int serviceId = device.hashCode();
			CameraServer server = null;
			synchronized (sServiceSync) {
				server = sCameraServers.get(serviceId);
				if (server == null) {
					LogMgr.i("request permission");

					mUSBMonitor.requestPermission(device);
					LogMgr.i("wait for getting permission");
					try {
						sServiceSync.wait();
					} catch (final Exception e) {
						LogMgr.e("connect:" + e);
					}
					LogMgr.i("check service again");
					server = sCameraServers.get(serviceId);
					if (server == null) {
						throw new RuntimeException("failed to open USB device(has no permission)");
					}
				}
			}
			if (server != null) {
				LogMgr.i("success to get service:serviceId=" + serviceId);
				server.registerCallback(callback);
			}
			return serviceId;
		}

		@Override
		public void release(final int serviceId) throws RemoteException {

			LogMgr.d("mBasicBinder#release:");
			synchronized (sServiceSync) {
				final CameraServer server = sCameraServers.get(serviceId);
				if (server != null) {
					if (server.unregisterCallback(mCallback)) {
						if (!server.isConnected()) {
							sCameraServers.remove(serviceId);
							if (server != null) {
								server.release();
							}
							final CameraServer srv = sCameraServers.get(serviceId);
							LogMgr.w("srv=" + srv);
						}
					}
				}
			}
			mCallback = null;
		}

		@Override
		public boolean isSelected(final int serviceId) throws RemoteException {
			return getCameraServer(serviceId) != null;
		}

		@Override
		public void releaseAll() throws RemoteException {

			LogMgr.d("mBasicBinder#releaseAll:");
			CameraServer server;
			synchronized (sServiceSync) {
				final int n = sCameraServers.size();
				for (int i = 0; i < n; i++) {
					server = sCameraServers.valueAt(i);
					sCameraServers.removeAt(i);
					if (server != null) {
						server.release();
					}
				}
			}
		}

		@Override
		public void resize(final int serviceId, final int width, final int height) {
			LogMgr.d("mBasicBinder#resize:");
			final CameraServer server = getCameraServer(serviceId);
			if (server == null) {
				throw new IllegalArgumentException("invalid serviceId");
			}
			server.resize(width, height);
		}

		@Override
		public void connect(final int serviceId) throws RemoteException {

			LogMgr.d("mBasicBinder#connect:");
			final CameraServer server = getCameraServer(serviceId);
			if (server == null) {
				throw new IllegalArgumentException("invalid serviceId");
			}
			server.connect();
		}

		@Override
		public void disconnect(final int serviceId) throws RemoteException {

			LogMgr.d("mBasicBinder#disconnect:");
			final CameraServer server = getCameraServer(serviceId);
			if (server == null) {
				throw new IllegalArgumentException("invalid serviceId");
			}
			server.disconnect();
		}

		@Override
		public boolean isConnected(final int serviceId) throws RemoteException {
			final CameraServer server = getCameraServer(serviceId);
			return (server != null) && server.isConnected();
		}


		@Override
		public void addSurface(final int serviceId, final int id_surface, final Surface surface, final boolean isRecordable) throws RemoteException {

			LogMgr.d("mBasicBinder#addSurface:id=" + id_surface + ",surface=" + surface);
			final CameraServer server = getCameraServer(serviceId);
			if (server != null)
				server.addSurface(id_surface, surface, isRecordable, null);
		}

		@Override
		public void removeSurface(final int serviceId, final int id_surface) throws RemoteException {

			LogMgr.d("mBasicBinder#removeSurface:id=" + id_surface);
			final CameraServer server = getCameraServer(serviceId);
			if (server != null)
				server.removeSurface(id_surface);
		}

		@Override
		public boolean isRecording(final int serviceId) throws RemoteException {
			final CameraServer server = getCameraServer(serviceId);
			return server != null && server.isRecording();
		}

		@Override
		public void startRecording(final int serviceId) throws RemoteException {

			LogMgr.d("mBasicBinder#startRecording:");
			final CameraServer server = getCameraServer(serviceId);
			if ((server != null) && !server.isRecording()) {
				server.startRecording();
			}
		}

		@Override
		public void stopRecording(final int serviceId) throws RemoteException {

			LogMgr.d("mBasicBinder#stopRecording:");
			final CameraServer server = getCameraServer(serviceId);
			if ((server != null) && server.isRecording()) {
				server.stopRecording();
			}
		}

		@Override
		public void captureStillImage(final int serviceId, final String path) throws RemoteException {

			LogMgr.d("mBasicBinder#captureStillImage:" + path);
			final CameraServer server = getCameraServer(serviceId);
			if (server != null) {
				//server.captureStill(path);
			}
		}

		@Override
		public void preview(int serviceId, IPreviewCallback callback) throws RemoteException {
			LogMgr.e("preview");
			final CameraServer server = getCameraServer(serviceId);
			if (server != null) {
				server.preview(callback);
			}
		}

		@Override
		public void takePicture(int serviceId, String path, ITakePictureCallback callback) throws RemoteException {
			LogMgr.e("UVCService===>拍照路径：" + path);
			final CameraServer server = getCameraServer(serviceId);
			if (server != null) {
				if (callback == null) {
					LogMgr.e("UVCService===>callback is null");
				}
				LogMgr.e("UVCService===>callback is null：" + (callback == null ? true : false));
				server.takePicture(path, callback);
			}

		}
		@Override
		public void setBrightnessS(final int serviceId,final int brightness){
			final CameraServer server = getCameraServer(serviceId);
			if (server != null) {
				server.setBrightnessS(brightness);
			}
		}
		@Override
		public int getBrightnessS(final int serviceId){
			final CameraServer server = getCameraServer(serviceId);
			if (server != null) {
				return server.getBrightnessS();
			}else return -1;
		}

	};

	// ********************************************************************************
	private final IUVCSlaveService.Stub mSlaveBinder = new IUVCSlaveService.Stub() {
		@Override
		public boolean isSelected(final int serviceID) throws RemoteException {
			return getCameraServer(serviceID) != null;
		}

		@Override
		public boolean isConnected(final int serviceID) throws RemoteException {
			final CameraServer server = getCameraServer(serviceID);
			return server != null ? server.isConnected() : false;
		}

		@Override
		public void addSurface(final int serviceID, final int id_surface, final Surface surface, final boolean isRecordable, final IUVCServiceOnFrameAvailable callback) throws RemoteException {

			LogMgr.d("mSlaveBinder#addSurface:id=" + id_surface + ",surface=" + surface);
			final CameraServer server = getCameraServer(serviceID);
			if (server != null) {
				server.addSurface(id_surface, surface, isRecordable, callback);
			} else {
				LogMgr.e("failed to get CameraServer:serviceID=" + serviceID);
			}
		}

		@Override
		public void removeSurface(final int serviceID, final int id_surface) throws RemoteException {

			LogMgr.d("mSlaveBinder#removeSurface:id=" + id_surface);
			final CameraServer server = getCameraServer(serviceID);
			if (server != null) {
				server.removeSurface(id_surface);
			} else {
				LogMgr.e("failed to get CameraServer:serviceID=" + serviceID);
			}
		}
	};

}
