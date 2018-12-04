package com.abilix.brain.control;

import com.abilix.brain.utils.LogMgr;

import android.util.Log;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Skip;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 在10086端口启动心跳TCP服务端。
 */
public class ServerRunnable implements Runnable {

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    public ChannelFuture channelFuture01;//开端口号
    public static boolean isRunning = false;

    //		public ChannelFuture channelFuture02;
    @Override
    public void run() {
        while (true) {//不允许线程结束，用于断线重连
            try {
                if (isRunning == false) {
                    synchronized (ServerRunnable.class) {
                        if (isRunning == true) {
                            LogMgr.e("心跳tcp服务端已建立，不重复建立");
                            break;
                        }
                        runServer();
                    }
                } else {
                    LogMgr.e("心跳tcp服务端已建立，不重复建立");
                    break;
                }
            } catch (Exception e) {
                LogMgr.i("ServerRunnable", "  Server建立服务器 出错了  " + e);
            } finally {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void runServer() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            serverBootstrap = new ServerBootstrap();// 引导辅助程序
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                             /*	
                              * LengthFieldBasedFrameDecoder 说明	                     
                              * int maxFrameLength：定义接收数据包的最大长度，如果发送的数据包超过此值，则抛出异常；
                              * int lengthFieldOffset：长度属性部分的偏移值，0表示长度属性位于数据包头部；
                              * int lengthFieldLength：长度属性的字节长度，如果设置为4，就是我们用4个字节存放数据包的长度；
                              * int lengthAdjustment：协议体长度调节值，修正信息长度，如果设置为4，那么解码时再向后推4个字节；
                              * int initialBytesToStrip：跳过字节数，如我们想跳过长度属性部分。*/
                            pipeline.addFirst("framedecoder", new LengthFieldBasedFrameDecoder(1024 * 1024 * 1024, 2, 2, 0, 4));
                            pipeline.addLast(new ServerHandler());
                        }
                    });

            channelFuture01 = serverBootstrap.bind(10086).sync();//开端口号,可以指定服务器地址也可以默认不指定，如何保证服务器端和客户端地址唯一？？
//		             channelFuture02 = serverBootstrap.bind(40002).sync();
            if (channelFuture01.isSuccess()) {
                isRunning = true;
                LogMgr.d("启动Netty服务成功，端口号：" + 10086);
            }
            channelFuture01.channel().closeFuture().sync();
//		            channelFuture02.channel().closeFuture().sync();
        } catch (Exception e) {
            LogMgr.e("启动Netty服务错误，端口号：" + 10086);
            e.printStackTrace();
        } finally {
            isRunning = false;
            LogMgr.i("关闭Netty服务，端口号：" + 10086);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
