package de.renebergelt.juitest.monitor.comm;

import de.renebergelt.juitest.core.comm.IPCMessageListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import de.renebergelt.juitest.core.comm.messages.IPCProtocol;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IPCClientHandler extends SimpleChannelInboundHandler<IPCProtocol.IPCMessage> {

    private Channel channel;
    private IPCProtocol.IPCMessage resp;
    BlockingQueue<IPCProtocol.IPCMessage> resps = new LinkedBlockingQueue<IPCProtocol.IPCMessage>();

    IPCMessageListener messageListener;

    public void setMessageListener(IPCMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void send(IPCProtocol.IPCMessage message) {
        channel.writeAndFlush(message);
    }

    public IPCProtocol.IPCMessage sendRequest(IPCProtocol.IPCMessage message) {
        // Send request
        channel.writeAndFlush(message);

        // Now wait for response from server
        boolean interrupted = false;
        for (;;) {
            try {
                resp = resps.take();
                break;
            } catch (InterruptedException ignore) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return resp;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IPCProtocol.IPCMessage msg)
            throws Exception {

        if (messageListener != null) {
            if (messageListener.onMessageReceived(msg)) {
                // message has already been handled
                return;
            }
        }

        resps.add(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();

        if (messageListener != null) {
            messageListener.connectionTerminated(cause);
        }
    }
}