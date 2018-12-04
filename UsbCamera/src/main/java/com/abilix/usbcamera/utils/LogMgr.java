package com.abilix.usbcamera.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class LogMgr {

    public static final String SEPARATOR = ",";
    private static final String LOG_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "Abilix" + 
    							File.separator + "system-apps" + File.separator + "UsbCamera";
    private static final String LOG_FILE_1 = LOG_FILE_PATH + File.separator + "UsbCameraLog1.log";
    private static final String LOG_FILE_2 = LOG_FILE_PATH + File.separator + "UsbCameraLog2.log";
    
    /** 允许开始写log的最小存储剩余空间*/
    private static final int MIN_AVAILABLE_STORAGE_FOR_LOG = 20;
    
    private final static int ERROR = 1;
    private final static int WARN = 2;
    private final static int INFO = 3;
    private final static int DEBUG = 4;
    private final static int VERBOSE = 5;
    
    /** 写入log文件的最低等级*/
    private static final int LOG_LEVEL_TO_STORE = INFO;
    /** 单个log文件的最大size，超过后更换另一个log文件*/
    private final static int MAX_LOG_MB = 5;
    /** 每次写入的log条数，之后读取当前log文件的大小判断是否需要更换log文件*/
    private final static int MAX_LOG_SIZE_PER_TIME = 5000;
    
    
    private static boolean isExportLog = false;
    private static FileOutputStream fosLog;
    private static int logCount = 0;
    private static int currentFileNum;
    private static File logFile1;
	private static File logFile2;
    
    public synchronized static void startExportLog(){
    	if(CommonUtils.getAvailableSize() < MIN_AVAILABLE_STORAGE_FOR_LOG){
    		LogMgr.w("机器剩余大小不足 "+ MIN_AVAILABLE_STORAGE_FOR_LOG +" MB,不进行log写入");
    		return;
    	}
    	File logFilePath = new File(LOG_FILE_PATH);
    	if(!logFilePath.exists() || (logFilePath.exists() && logFilePath.isFile()) ){
    		logFilePath.mkdirs();
    	}
    	logFile1 = new File(LOG_FILE_1);
    	logFile2 = new File(LOG_FILE_2);
    	try {
			if(!logFile1.exists()){
				logFile1.createNewFile();
			}
			if(!logFile2.exists()){
				logFile2.createNewFile();
			}
			
			if(logFile1.length() < MAX_LOG_MB*1024*1024){
				fosLog = new FileOutputStream(logFile1, true);
				currentFileNum = 1;
			}else if(logFile1.length() > MAX_LOG_MB*1024*1024 && logFile2.length() < MAX_LOG_MB*1024*1024){
				fosLog = new FileOutputStream(logFile2, true);
				currentFileNum = 2;
			}else{
				fosLog = new FileOutputStream(logFile1, false);
				currentFileNum = 1;
			}
			
			isExportLog = true;
		} catch (Exception e) {
			LogMgr.e("开启log输出异常");
			isExportLog = false;
			e.printStackTrace();
		}
    }
    
    public synchronized static void stopExportLog(){
    	if(null != fosLog){
    		try {
				fosLog.close();
			} catch (IOException e) {
				LogMgr.e("关闭log输出异常");
				e.printStackTrace();
			}
    	}
    	isExportLog = false;
    }
    
    private synchronized static void exportLog(String logMessage) {
		if(isExportLog == false){
			return;
		}
		if(null == fosLog){
			return;
		}
		try {
			if(logCount > MAX_LOG_SIZE_PER_TIME){
				boolean isNeedToChangeLogFile = false;
				if(currentFileNum == 1){
					if(logFile1.length() > MAX_LOG_MB*1024*1024){
						isNeedToChangeLogFile = true;
					}
				}else if(currentFileNum == 2){
					if(logFile2.length() > MAX_LOG_MB*1024*1024){
						isNeedToChangeLogFile = true;
					}
				}else{
					throw new Exception("当前log文件的序号错误1");
				}
				if(isNeedToChangeLogFile){
					fosLog.close();
					if(currentFileNum == 1){
						fosLog = new FileOutputStream(logFile2, false);
						currentFileNum = 2;
					}else if(currentFileNum == 2){
						fosLog = new FileOutputStream(logFile1, false);
						currentFileNum = 1;
					}else{
						throw new Exception("当前log文件的序号错误2");
					}
				}
				
				logCount = 0;
			}
			fosLog.write(logMessage.getBytes("UTF-8"));
			logCount++;
		} catch (Exception e) {
			isExportLog = false;
			stopExportLog();
			LogMgr.e("写入log时异常");
			e.printStackTrace();
		}
	}

    public static void v(String message) {
        if (GlobalConfig.LOG_LEVEL <= VERBOSE) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            if(VERBOSE <= LOG_LEVEL_TO_STORE){
            	 exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " V "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.v(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    

	public static void v(String tag, String message) {
        if (GlobalConfig.LOG_LEVEL <= VERBOSE) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            tag = "UsbCamera-" + tag;
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            if(VERBOSE <= LOG_LEVEL_TO_STORE){
            	exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " V "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.v(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void d(String message) {
        if (GlobalConfig.LOG_LEVEL <= DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            if(DEBUG <= LOG_LEVEL_TO_STORE){
            	exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " D "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.d(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void d(String tag, String message) {
        if (GlobalConfig.LOG_LEVEL <= DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            tag = "UsbCamera-" + tag;
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            if(DEBUG <= LOG_LEVEL_TO_STORE){
            	exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " D "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.d(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void i(String message) {
        if (GlobalConfig.LOG_LEVEL <= INFO) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            if(INFO <= LOG_LEVEL_TO_STORE){
            	exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " I "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.i(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void i(String tag, String message) {
        if (GlobalConfig.LOG_LEVEL <= INFO) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            tag = "UsbCamera-" + tag;
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            if(INFO <= LOG_LEVEL_TO_STORE){
            	exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " I "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.i(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void w(String message) {
        if (GlobalConfig.LOG_LEVEL <= WARN) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            if(WARN <= LOG_LEVEL_TO_STORE){
            	exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " W "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.w(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void w(String tag, String message) {
        if (GlobalConfig.LOG_LEVEL <= WARN) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            tag = "UsbCamera-" + tag;
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            if(WARN <= LOG_LEVEL_TO_STORE){
            	exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " W "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.w(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void e(String message) {
        if (GlobalConfig.LOG_LEVEL <= ERROR) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            if(ERROR <= LOG_LEVEL_TO_STORE){
            	exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " E "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.e(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void e(String tag, String message) {
        if (GlobalConfig.LOG_LEVEL <= ERROR) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            tag = "UsbCamera-" + tag;
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            if(ERROR <= LOG_LEVEL_TO_STORE){
            	exportLog(CommonUtils.getDateTimeFromMillisecond2(System.currentTimeMillis())+ " " + tag + " E "+ getLogInfo(stackTraceElement) + message + "\n");
            }
            Log.e(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    /**
     * Get default tag name
     */
    private static String getDefaultTag(StackTraceElement stackTraceElement) {
        String fileName = stackTraceElement.getFileName();
        String stringArray[] = fileName.split("\\.");
        String tag = stringArray[0];
        return "UsbCamera-" + tag;
    }

    /**
     * get stack info
     */
    private static String getLogInfo(StackTraceElement stackTraceElement) {
        StringBuilder logInfoStringBuilder = new StringBuilder();
        // thread name
        String threadName = Thread.currentThread().getName();
        // thread ID
        long threadID = Thread.currentThread().getId();
        // file name
        String fileName = stackTraceElement.getFileName();
        // class name
        String className = stackTraceElement.getClassName();
        // method
        String methodName = stackTraceElement.getMethodName();
        // code line
        int lineNumber = stackTraceElement.getLineNumber();

        logInfoStringBuilder.append("[ ");
        /*
         * logInfoStringBuilder.append("threadID=" + threadID).append(SEPARATOR); 
         * logInfoStringBuilder.append("threadName=" + threadName).append(SEPARATOR);
         * logInfoStringBuilder.append("fileName=" + fileName).append(SEPARATOR); 
         * logInfoStringBuilder.append("className=" + className).append(SEPARATOR);
         * logInfoStringBuilder.append("methodName=" + methodName).append(SEPARATOR);
         */
        logInfoStringBuilder.append("lineNumber =" + lineNumber);
        logInfoStringBuilder.append(" ] ");
        methodName = logInfoStringBuilder.toString();
        logInfoStringBuilder = null;
        return methodName;
    }
}
