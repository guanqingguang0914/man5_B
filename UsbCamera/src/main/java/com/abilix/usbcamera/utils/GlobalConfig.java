/**
 * 
 */
package com.abilix.usbcamera.utils;

import java.io.File;

import android.os.Build;
import android.os.Environment;

/**
 * @Descripton:
 * @author jingh
 * @date2017-2-4下午1:36:29
 * 
 */
public class GlobalConfig {
	public static final int LOG_LEVEL=1;
	public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath()+ File.separator + "Download";

}
