package de.renebergelt.juitest.monitor.comm;

import de.renebergelt.juitest.core.comm.IPCMessageListener;
import de.renebergelt.juitest.core.comm.messages.IPCProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class IPCClient {

    EventLoopGroup group;
    IPCClientHandler handler;
    Channel channel;

    public void connect(String host, int port, IPCMessageListener messageListener) {

        group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new IPCClientInitializer());

            // Create connection
            channel = bootstrap.connect(host, port).sync().channel();

            // Get handle to handler so we can send message
            handler = channel.pipeline().get(IPCClientHandler.class);

            if (messageListener != null) {
                handler.setMessageListener(messageListener);
            }

            System.out.println("Connected to TestHost");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isOpen();
    }

    public void close() {
        if (channel != null) {
            channel.close();
            channel = null;
        }

        if (group != null) {
            group.shutdownGracefully();
            group = null;
        }
    }

    public IPCProtocol.IPCMessage send(IPCProtocol.IPCMessage message) {
        return handler.sendRequest(message);
    }

    public void sendAndForget(IPCProtocol.IPCMessage message) {
        handler.send(message);
    }
}


