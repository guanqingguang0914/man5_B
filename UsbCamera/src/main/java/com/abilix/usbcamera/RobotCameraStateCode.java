package com.abilix.usbcamera;

public class RobotCameraStateCode {
	//拍照时内存不足
	public static final int TAKE_PICTURE_NOT_ENOUGH_MEMORY_ERROR = 0X00;
	//拍照成功
	public static final int TAKE_PICTURE_SUCESS = 0X01;
	//拍照写照片文件失败
	public static final int TAKE_PICTURE_WRITE_FILE_ERROR = 0X02;
	//照片文件地址为空
	public static final int TAKE_PICTURE_PATH_IS_NULL = 0X03;
	//摄像头初始化失败
	public static final int TAKE_PICTURE_CAMERA_INIT_ERROR = 0X04;
	//配置摄像头失败
	public static final int TAKE_PICTURE_CONFIGURED_FAILED= 0X05;
	//未安装SD卡
	public static final int TAKE_PICTURE_NO_SDCARD= 0X06;
	//照片文件不存在
	public static final int TAKE_PICTURE_FILE_IS_NULL= 0X07;
	//Usb摄像头未插入
	public static final int TAKE_PICTURE_USB_CAMERA_IS_NOT_CONNECTED= 0X08;
	//初始化摄像头成功
	public static final int OPEN_CAMERA_SUCESS= 0X09;
	//正在初始化摄像头
	public static final int OPENING_CAMERA= 0X10;
	//保存照片文件成功
	public static final int SAVE_PICTURE_SUCESS= 0X11;
}
