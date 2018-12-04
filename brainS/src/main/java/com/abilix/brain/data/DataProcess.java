package com.abilix.brain.data;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.abilix.brain.Application;
import com.abilix.brain.BrainActivity;
import com.abilix.brain.BrainInfo;
import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.utils.GetAppInfoThread;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.ProtocolUtil;
import com.abilix.brain.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理、回复TCP连接的信息的功能类。
 */
public class DataProcess {

    private static DataProcess dataProcess = null;

    public static ChannelHandlerContext ctx01;// 心跳tcp
    public static ChannelHandlerContext ctx02;// 文件传输tcp
    public static ChannelHandlerContext ctx03;// 文件传输tcp

    public static Channel currentChannel;
    public static boolean isW;
    public static boolean isC;
    public static ByteBuf encoded;

    public static Context context = Application.getInstance();

    public static DataProcess GetManger() {
        // 单例
        if (dataProcess == null) {
            synchronized (DataProcess.class) {
                if (dataProcess == null) {
                    dataProcess = new DataProcess();
                }
            }
        }
        return dataProcess;
    }

    private synchronized void sendMsgCtx(byte[] send, ChannelHandlerContext ctx) {
        currentChannel = ctx.channel();
        isW = currentChannel.isWritable();
        isC = currentChannel.isActive();
        if ((isW && isC)) {// 通过Netty传递，都需要基于流，以ChannelBuffer的形式传递。所以，Object
            // ->ChannelBuffer
            // Netty框架中，所有消息的传输都依赖于ByteBuf接口，ByteBuf是Netty NIO框架中的缓冲区
            encoded = currentChannel.alloc().buffer(send.length);// 生成send.length个字节的buf对象
            encoded.writeBytes(send);// 将数据写入向缓冲区
            currentChannel.write(encoded);// 消息传递都是基于流，通过ChannelBuffer传递的
            currentChannel.flush();// 发送数据
            String ctxkind = (ctx == ctx01)?"心跳TCP":((ctx == ctx02)?"文件传输TCP":((ctx==ctx03)?"群控TCP":"未知TCP"));
            LogMgr.d(ctxkind + "回复了数据 长度 = " + send.length + " 内容 = " + Utils.bytesToString(send));
        } else {
            LogMgr.e("sendMsgCtx() 失败 isW = "+isW+" isC = "+isC);
        }
    }
    public synchronized void sendMsg3(byte[] send) { // I2C的数据不解析直接转发给Mobile
        if (ctx03 != null && null != ctx03.channel()) {
            try {
                sendMsgCtx(send, ctx03);
            } catch (Exception ex) {
                LogMgr.e("sendMsg 出错了 Exception = " + ex);
            }
        } else {
            LogMgr.e("sendMsg() 失败 通道异常");
        }
    }
    public synchronized void sendMsg(byte[] send) { // I2C的数据不解析直接转发给Mobile
        if (ctx01 != null && null != ctx01.channel()) {
            try {
                sendMsgCtx(send, ctx01);
            } catch (Exception ex) {
                LogMgr.e("sendMsg 出错了 Exception = " + ex);
            }
        } else {
            LogMgr.e("sendMsg() 失败 通道异常");
        }
    }

    public synchronized void sendMsgFileReceive(byte[] send) { // I2C的数据不解析直接转发给Mobile
        if (ctx02 != null && null != ctx02.channel()) {
            try {
                sendMsgCtx(send, ctx02);
            } catch (Exception ex) {
                LogMgr.e("sendMsgFileReceive出错了 Exception = " + ex);
            }
        } else {
            LogMgr.e("sendMsgFileReceive() 失败 通道异常");
        }
    }

    public void setCtx01(ChannelHandlerContext ctx) {
        DataProcess.ctx01 = ctx;
    }
    public void setCtx03(ChannelHandlerContext ctx) {
        DataProcess.ctx03 = ctx;
    }
    public void setCtx02(ChannelHandlerContext ctx) {
        DataProcess.ctx02 = ctx;
    }





    /**
     * 发送广播
     */
    public void sendBroadcast(String filePath, int mode) {
        Intent sendIntent = new Intent(GlobalConfig.ACTION_ACTIVITY);
        sendIntent.putExtra(GlobalConfig.ACTION_ACTIVITY_MODE, mode);
        sendIntent.putExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH, filePath);
        BrainActivity.getmBrainActivity().sendBroadcast(sendIntent);
        LogMgr.i("SendBroadCastToActivity mode=" + mode + " filePath=" + filePath);
    }

    /**
     * 关闭所有当前连接的tcp连接
     */
    public void closeAllTcpConnecting() {
        LogMgr.w("closeAllTcpConnecting() ctx01 != null is " + (ctx01 != null));
        if (ctx01 != null) {
            ctx01.channel().close();
            // setCtx01(null);
        }
        if (ctx02 != null) {
            ctx02.channel().close();
            // setCtx02(null);
        }
    }
}
