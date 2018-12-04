package com.abilix.brain.control;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import com.abilix.brain.Application;
import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.data.DataProcess;
import com.abilix.brain.utils.LogMgr;

/**
 * 维护心跳TCP的状态，分发收到TCP信息。
 */
public class ServerHandler extends ChannelHandlerAdapter {
    private static final String TAG = "ServerHandler";

    private static SocketAddress address;
    private static InetSocketAddress socket;
    private static ByteBuf data;
    private static byte[] receive;
    private static int port;

    private Timer mHeartBeatTimer;
    private TimerTask mHeartBeatTimerTask;

    public static final int HEART_BEAT_TYPE_NORMAL = 150;
    public static final int HEART_BEAT_TYPE_TEMP = 151;

    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) {//架包里应该有个循环在重复调用这个方法
        LogMgr.d(TAG, "channelRead()");
        address = ctx.channel().remoteAddress();
        socket = (InetSocketAddress) address;
        port = socket.getPort();
        data = (ByteBuf) msg;
        receive = new byte[data.readableBytes()];
        data.readBytes(receive);
        ServerHeartBeatProcesser.getInstance().DataType(receive);//处理数据，并将同类数据分到各自缓冲区
        data.clear();//清空缓冲区
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        address = ctx.channel().remoteAddress();
        socket = (InetSocketAddress) address;
        LogMgr.e(TAG, "端口号 " + socket.getPort() + " 连接异常" + " cause = " + cause.getMessage());
        DataProcess.ctx01.channel().close();
//				 DataProcess.GetManger().setCtx01(null);
    }


    @Override
    public synchronized void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogMgr.d(TAG, "channelActive() " + ((InetSocketAddress) (ctx.channel().remoteAddress())).getHostString());
        if (!Application.getInstance().isTcpConnecting() || !ServerHeartBeatProcesser.getInstance().isNeedToLimitClientToOne() || !GlobalConfig.isLimitClientToOne) {
            Application.getInstance().setTcpConnecting(true);
            BrainService.getmBrainService().stopRecreateHotSpotTimer();
            BrainService.getmBrainService().setReturnChildType();
            address = ctx.channel().remoteAddress();
            socket = (InetSocketAddress) address;
            LogMgr.i(TAG, "端口号 " + socket.getPort() + " 连接上了");
            DataProcess.GetManger().setCtx01(ctx);
            ServerHeartBeatProcesser.getInstance().setInetAddress(socket.getAddress());
//			DataProcess.GetManger().sendPadAppConnectStateBroadcast(GlobalConfig.TCP_CONNECT_SUCCESS);
            startHeartBeatTimer();
//			DataProcess.GetManger().heartBeatCount = new int[11];
//			DataProcess.GetManger().receiveHeartBeatCountForTest = 0;

        } else {
            ctx.channel().close();
            LogMgr.i(TAG, "因为当前已有连接，主动断开");
        }
    }

    @Override
    public synchronized void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogMgr.d(TAG, "channelInactive() " + ((InetSocketAddress) (ctx.channel().remoteAddress())).getHostString());
        if (Application.getInstance().isTcpConnecting()) {
            LogMgr.i(TAG, "DataProcess.ctx01 == null is " + (DataProcess.ctx01 == null));
            if (ctx != DataProcess.ctx01) {
                // 断开的连接并不是当前已连接的连接
                LogMgr.i(TAG, "因为当前已有连接，而主动断开的连接不做别的处理");
                return;
            }
            Application.getInstance().setTcpConnecting(false);
            stopHeartBeatTimer();
            ServerHeartBeatProcesser.getInstance().stopReceiveHeartBeatReplyTimer();
            address = ctx.channel().remoteAddress();
            socket = (InetSocketAddress) address;
            LogMgr.i(TAG, "端口号 " + socket.getPort() + " 中断连接了");
            ServerHeartBeatProcesser.getInstance().sendPadAppConnectStateBroadcast(GlobalConfig.PAD_APP_DISCONNECT);
            DataProcess.ctx01.channel().close();
            DataProcess.GetManger().setCtx01(null);
        } else {
            LogMgr.e(TAG, "当前连接断开时 Application.getInstance().isTcpConnecting() == false 异常");
        }

    }

    private void startHeartBeatTimer() {
        stopHeartBeatTimer();
        mHeartBeatTimer = new Timer();
        mHeartBeatTimerTask = new TimerTask() {
            @Override
            public void run() {
                ServerHeartBeatProcesser.getInstance().sendHeartBeat(ServerHandler.HEART_BEAT_TYPE_NORMAL);
            }
        };
        mHeartBeatTimer.schedule(mHeartBeatTimerTask, 80, GlobalConfig.HEART_BEAT_TIME * 1000);
    }

    private void stopHeartBeatTimer() {
        if (mHeartBeatTimer != null) {
            mHeartBeatTimer.cancel();
        }
        if (mHeartBeatTimerTask != null) {
            mHeartBeatTimerTask.cancel();
        }
    }

}
