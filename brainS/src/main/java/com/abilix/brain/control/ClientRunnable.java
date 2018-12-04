package com.abilix.brain.control;

import android.util.Log;

import com.abilix.brain.data.DataBuffer;
import com.abilix.brain.utils.LogMgr;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ClientRunnable implements Runnable {
    private EventLoopGroup group;
    private static Bootstrap bootstrap;
    public static int port = 50001;
    private static ChannelFutureListener channelFutureListener = null;
    private ChannelFuture channelFuture;
    private static SocketChannel socketChannel;

    public static ChannelHandlerContext ctx01 = null;
    public static Channel currentChannel;
    public static boolean isW;
    public static boolean isC;
    public static ByteBuf encoded;

    @Override
    public void run() {// 数据接收完成后断开连接
        try {
            LogMgr.d("ClientRunnable");
            group = new NioEventLoopGroup();
            bootstrap = new Bootstrap();

            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    LogMgr.d("initChannel ");
                   socketChannel.pipeline().addLast(new ClientHandler());// ChannelPipeline控制ChannelEvent事件分发和传递的。事件在管道中流转，第一站到哪，第二站到哪，到哪是终点，就是用这个ChannelPipeline
                    // 处理的
                }
            });
            try {
                LogMgr.d("ClientFileDownloadProcesser.serverIP = " + ClientFileDownloadProcesser.serverIP);
                channelFuture = bootstrap.connect(new InetSocketAddress(ClientFileDownloadProcesser.serverIP,port)).sync();
                channelFuture.addListener(channelFutureListener);
                channelFuture.channel().closeFuture().sync();// 等待服务端监听端口关闭 //
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            LogMgr.i("ClientRunnable  ", "ClientUdp_YQ ClientRunnable  client建立长连接 出错了  " + e);
        } finally {
            try {
                group.shutdownGracefully();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        channelFutureListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    socketChannel = (SocketChannel) f.channel();
                    LogMgr.i("ClientRunnable  ", "ClientUdp_YQ ClientRunnable 连接服务器成功");
                } else {
                    LogMgr.i("ClientRunnable  ", "ClientUdp_YQ ClientRunnable 连接服务器失败");
                }
            }
        };

    }
}
