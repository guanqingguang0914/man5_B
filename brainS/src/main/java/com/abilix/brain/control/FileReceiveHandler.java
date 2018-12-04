package com.abilix.brain.control;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.data.DataBuffer;
import com.abilix.brain.data.DataProcess;
import com.abilix.brain.utils.LogMgr;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 维护文件传输TCP的状态，分发收到TCP信息。
 */
public class FileReceiveHandler extends ChannelHandlerAdapter {

    private static final String TAG = "FileReceiveHandler";

    private static SocketAddress address;
    private static InetSocketAddress socket;
    private static ByteBuf data;
    private static byte[] receive;
    private static int port;

    //架包里应该有个循环在重复调用这个方法
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        LogMgr.d(TAG, "channelRead()0");
        address = ctx.channel().remoteAddress();
        socket = (InetSocketAddress) address;
        port = socket.getPort();
        data = (ByteBuf) msg;
        receive = new byte[data.readableBytes()];
        data.readBytes(receive);

        FileDownloadProcesser.getInstance().handleFileReceiveCmd(receive);
        //清空缓冲区
        data.clear();
    }

    private String getHexstr(byte[] data) {//将接收的数据转换为16进制String
        int v;
        String hv = "";
        for (int i = 0; i < data.length; i++) {
            v = data[i] & 0xFF;
            if (v <= 0x0f) {
                hv = hv + " 0" + Integer.toHexString(v);
            } else {
                hv = hv + " " + Integer.toHexString(v);
            }
        }
        return hv;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        address = ctx.channel().remoteAddress();
        socket = (InetSocketAddress) address;
        LogMgr.i("CanBusHandler exceptionCaught", "端口号 " + socket.getPort() + " 文件传输连接异常");

        DataProcess.ctx02.channel().close();
//		DataProcess.GetManger().setCtx02(null);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        address = ctx.channel().remoteAddress();
        socket = (InetSocketAddress) address;
        LogMgr.i(TAG, "端口号 " + socket.getPort() + " 文件传输连接上了");

        DataProcess.GetManger().setCtx02(ctx);
//		DataProcess.GetManger().setInetAddress(socket.getAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        address = ctx.channel().remoteAddress();
        socket = (InetSocketAddress) address;
        LogMgr.i(TAG, "端口号 " + socket.getPort() + " 文件传输中断连接了");

        DataProcess.ctx02.channel().close();
        DataProcess.GetManger().setCtx02(null);
    }


}
