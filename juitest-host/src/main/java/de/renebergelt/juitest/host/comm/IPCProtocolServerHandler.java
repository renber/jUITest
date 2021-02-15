package de.renebergelt.juitest.host.comm;

import de.renebergelt.juitest.core.comm.IPCHandler;
import de.renebergelt.juitest.core.comm.messages.IPCProtocol;
import de.renebergelt.juitest.core.services.IPCTransmitter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IPCProtocolServerHandler extends SimpleChannelInboundHandler<IPCProtocol.IPCMessage> implements IPCTransmitter {

    ExecutorService workers = Executors.newCachedThreadPool();

    private IPCHandler messageHandler;

    private Channel activeChannel;

    public IPCProtocolServerHandler(IPCHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IPCProtocol.IPCMessage msg)
            throws Exception {
        // handle in thread
        workers.submit( () -> {

            if (msg.hasAttach()) {
                // new client connection -> store the channel
                // to be able to send
                activeChannel = ctx.channel();

                messageHandler.registerTransmitter(this);
            }

            IPCProtocol.IPCMessage response = messageHandler.handleMessage(msg);

            if (response != null) {
                ctx.writeAndFlush(response);
            }
        });
    }

    public void sendMessage(IPCProtocol.IPCMessage message) {
        if (activeChannel != null) {
            activeChannel.writeAndFlush(message);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        if (ctx.channel() == activeChannel) {
            activeChannel = null;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}