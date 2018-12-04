package com.abilix.clrdet;

public class clrdetect {
	private static clrdetect instance = null;

	public static clrdetect getInstance() {
		if (instance == null) {
			instance = new clrdetect();
		}
		return instance;
	}
	/*
	 * byte[]imgYuvArry: image data,data format  yuv
	 * int imgh: image's height
	 * int imgw: image's width
	 * int[] Roiarea: object area, size ==4; 
	 * 				  if  Roiarea[0]==-1||Roiarea[1]==-1||Roiarea[2]==-1|| Roiarea[3]==-1 ,the object is the whole image
	 * 				 otherwise
	 *                 left top point is (Roiarea[0],Roiarea[1]), right bottom point is (Roiarea[2],Roiarea[3])
	 * return  	 	 
	 * 		: 未知(-1),红(0),橙(1),黄(2),绿(3),青(4),蓝(5),紫(6),黑(7),白(8)
	 */
	public native int clrdetect(byte[] imgYuvArry, int imgh,int imgw, int[] Roiarea);
							
	 /*
	  * save image to dir,for debug
	  * byte[]imgYuvArry: image data,data format  yuv
	  * int imgh: image's height
	  * int imgw: image's width
	  * String filename,file's full path, eg. "/mnt/sdcard/test.jpg"
	  */
	public native void saveclrimg(byte[] imgYuvArry,int imgh,int imgw,String filename);
		
	static {
		System.loadLibrary("clrdet");
		System.loadLibrary("opencv_java");
	}
}
