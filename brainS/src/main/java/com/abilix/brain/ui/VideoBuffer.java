package com.abilix.brain.ui;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Environment;

/**
 * 未被使用。
 */
public class VideoBuffer {
    public static ByteArrayOutputStream outstream1;
    public static ByteArrayOutputStream outstream2;
    public static final Lock lockData = new ReentrantLock(); // 锁对象
    public static boolean cameraRefresh = false;// 监控客户端是否发送视频控制指令
    //	public static boolean cameraRunning = false;// 视频正在运行
    public static InetAddress mInetAddress = null;

}
