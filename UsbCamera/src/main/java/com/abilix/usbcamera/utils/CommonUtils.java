package com.abilix.usbcamera.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.os.StatFs;



public class CommonUtils {
	
	
	/**
	* 获取现在时间
	*
	* @return 返回时间类型 yyyy-MM-dd HH:mm:ss
	*/
	public static String getNowDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
//		ParsePosition pos = new ParsePosition(8);
//		Date currentTime_2 = formatter.parse(dateString, pos);
//		return currentTime_2;
	}
	
	
	/**
	* 获取现在时间
	*
	* @return 返回时间类型 MM_dd_HH_mm
	*/
	public static String getNowDate1() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MM_dd_HH_mm");
		String dateString = formatter.format(currentTime);
		return dateString;
//		ParsePosition pos = new ParsePosition(8);
//		Date currentTime_2 = formatter.parse(dateString, pos);
//		return currentTime_2;
	}
	
	/**
     * 将毫秒转化成固定格式的时间
     * 时间格式: yyyy-MM-dd HH:mm:ss
     *
     * @param millisecond
     * @return
     */
    public static String getDateTimeFromMillisecond(Long millisecond){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }
    
    /**
     * 将毫秒转化成固定格式的时间
     * 时间格式: yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param millisecond
     * @return
     */
    public static String getDateTimeFromMillisecond2(Long millisecond){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }
    
    /**
	 * 获取 SDCard 总容量大小 
	 * @return
	 */
	public static long getTotalSize(){
		String sdcard = Environment.getExternalStorageState();
		String state  = Environment.MEDIA_MOUNTED;
		if(sdcard.equals(state)){
			File file = Environment.getExternalStorageDirectory();
			StatFs statFs = new StatFs(file.getPath());
			//获得sdcard上 block的总数
			long blockCount = statFs.getBlockCountLong();
			//获得sdcard上每个block 的大小
			long blockSize  = statFs.getBlockSizeLong();
			//计算标准大小使用：1024，当然使用1000也可以
			long blockTotalSize = blockCount*blockSize/1024/1024;
			LogMgr.i("getTotalSize() 总容量大小 blockTotalSize = " + blockTotalSize + " MB");
			return blockTotalSize;
		}else{
			LogMgr.e("getTotalSize() 存储状态异常 sdcard = "+sdcard+" state = "+state);
			return -1;
		}
	}
	
	/**
	 * 获取 SDCard 剩余容量大小 
	 * @return
	 */
	public static long getAvailableSize(){
		String sdcard = Environment.getExternalStorageState();
		String state  = Environment.MEDIA_MOUNTED;
		if(sdcard.equals(state)){
			File file = Environment.getExternalStorageDirectory();
			StatFs statFs = new StatFs(file.getPath());
			//获得可供程序使用的Block数量
			long blockAvailable = statFs.getAvailableBlocksLong();
			//获得sdcard上每个block 的大小
			long blockSize  = statFs.getBlockSizeLong();
			//计算标准大小使用：1024，当然使用1000也可以
			long blockAvailableSize = blockAvailable*blockSize/1024/1024;
			LogMgr.i("getAvailableSize() 剩余容量大小 blockAvailableSize = " + blockAvailableSize + " MB");
			return blockAvailableSize;
		}else{
			LogMgr.e("getAvailableSize() 存储状态异常 sdcard = "+sdcard+" state = "+state);
			return -1;
		}
	}
}
