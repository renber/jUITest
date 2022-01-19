package de.renebergelt.juitest.host.comm;

import de.renebergelt.juitest.core.comm.IPCHandler;
import de.renebergelt.juitest.core.comm.messages.IPCProtocol;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Class which initializes the channels for IPC
 * based on protobuf
 */
public class IPCServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private IPCHandler messageHandler;

    /**
     * Create a new instance of IPCServerChannelInitializer
     * @param messageHandler The message handler to use
     */
    public IPCServerChannelInitializer(IPCHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(IPCProtocol.IPCMessage.getDefaultInstance()));

        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());

        p.addLast(new IPCProtocolServerHandler(messageHandler));
    }
}
