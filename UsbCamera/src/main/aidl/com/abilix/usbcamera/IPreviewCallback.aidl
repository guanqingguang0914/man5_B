package com.abilix.usbcamera;
interface IPreviewCallback {
	oneway void onPreviewFrame(in byte[] data);
}