package de.renebergelt.juitest.host.comm;

import de.renebergelt.juitest.core.comm.IPCHandler;
import de.renebergelt.juitest.core.utils.NullGuard;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * test host which receives test commands through a network socket
 * and executes them on an attached instance of the application under test
 */
public class IPCServer {

    private String host;
    private int port;
    private IPCHandler messageHandler;

    public IPCServer(String host, int port, IPCHandler messageHandler) {
        this.host = NullGuard.forArgument("host", host);
        this.port = port;
        this.messageHandler = NullGuard.forArgument("messageHandler", messageHandler);
    }

    public void run() {
        // Create event loop groups. One for incoming connections handling and
        // second for handling actual event by workers
        EventLoopGroup serverGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootStrap = new ServerBootstrap();
            bootStrap.group(serverGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new IPCServerChannelInitializer(messageHandler));

            // Bind to port
            ChannelFuture f = bootStrap.bind(host, port).sync();
            System.out.println("TestHost listening for connection by monitor on " + host + ":" + String.valueOf(port));

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            serverGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
