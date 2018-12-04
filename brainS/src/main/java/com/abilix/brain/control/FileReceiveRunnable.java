package com.abilix.brain.control;

import android.util.Log;

import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.WifiUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 在8890端口启动文件传输TCP服务端。
 */
public class FileReceiveRunnable implements Runnable {

    private static final String TAG = "FileReceiveRunnable";

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    private ChannelFuture channelFuture;//开端口号

    private static final int PORT = 8890;
    public static boolean isRunning = false;

    @Override
    public void run() {

        while (true) {//不允许线程结束，用于断线重连
            try {
                if (isRunning == false) {
                    synchronized (FileReceiveRunnable.class) {
                        if (isRunning == true) {
                            LogMgr.e("文件传输tcp服务端已建立，不重复建立");
                            break;
                        }
                        beginReceive();
                    }
                } else {
                    LogMgr.e("文件传输tcp服务端已建立，不重复建立");
                    break;
                }
            } catch (Exception e) {
                Log.i(TAG, "  FileReceiveRunnable建立服务器 出错了  " + e);
            } finally {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized void beginReceive() {
        LogMgr.d(TAG, "beginReceive()");


        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            serverBootstrap = new ServerBootstrap();// 引导辅助程序
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_REUSEADDR, true)
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
                            pipeline.addFirst("framedecoder", new LengthFieldBasedFrameDecoder(1024 * 1024 * 1, 2, 2, 0, 0));
                            pipeline.addLast(new FileReceiveHandler());
                        }
                    });

            channelFuture = serverBootstrap.bind(PORT).sync();//开端口号,可以指定服务器地址也可以默认不指定，如何保证服务器端和客户端地址唯一？？
//             channelFuture02 = serverBootstrap.bind(40002).sync();
            if (channelFuture.isSuccess()) {
                isRunning = true;
                LogMgr.d("启动Netty服务成功，端口号：" + PORT);
                LogMgr.d(TAG, "ip = " + WifiUtils.getLocalIpAddress());
            }
            channelFuture.channel().closeFuture().sync();
//            channelFuture02.channel().closeFuture().sync();
        } catch (Exception e) {
            LogMgr.e("启动Netty服务错误，端口号：" + PORT);
            e.printStackTrace();
        } finally {
            isRunning = false;
            LogMgr.i("关闭Netty服务，端口号：" + PORT);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
