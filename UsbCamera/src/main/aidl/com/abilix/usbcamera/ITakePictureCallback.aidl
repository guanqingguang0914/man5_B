package com.abilix.usbcamera;
interface ITakePictureCallback {
	oneway void onState(int state,in byte[] data);
}