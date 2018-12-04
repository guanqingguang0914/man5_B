package com.abilix.brain.control;

import com.abilix.brain.data.DataBuffer;
import com.abilix.brain.data.DataProcess;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ClientHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {// 架包里应该有个循环在重复调用这个方法
        LogMgr.i("channelRead");
        ByteBuf data = (ByteBuf) msg;
        byte[] receive = new byte[data.readableBytes()];
        data.readBytes(receive);
//        LogMgr.i("receive = " + Utils.bytesToString(receive));
        ClientFileDownloadProcesser.getInstance().DataTypeGroup(receive);// 处理数据
        data.clear();// 清空缓冲区
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LogMgr.i("exceptionCaught");
        ClientFileDownloadProcesser.hasclient = false;
        ClientFileDownloadProcesser.getInstance().stopCtx01(44);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogMgr.i("channelActive", "ClientUdp_YQ   连接上了 ");
        ClientFileDownloadProcesser.hasclient = true;
        DataProcess.GetManger().setCtx03(ctx);
        ClientFileDownloadProcesser.getInstance().sendData();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogMgr.i("channelInactive", "ClientUdp_YQ  端口号   中断连接");
        ClientFileDownloadProcesser.getInstance().stopCtx01(88);
        ClientFileDownloadProcesser.hasclient = false;
    }
}
