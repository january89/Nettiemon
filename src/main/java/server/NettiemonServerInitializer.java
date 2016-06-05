package server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

class NettiemonServerInitializer extends ChannelInitializer<SocketChannel>{
    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private static final NettiemonServerHandler NETTIEMON_SERVER_HANDLER = new NettiemonServerHandler();

    @Override
    public void initChannel(SocketChannel channel) throws Exception{

        ChannelPipeline channelPipeline = channel.pipeline();

        channelPipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        channelPipeline.addLast(DECODER);
        channelPipeline.addLast(ENCODER);
        channelPipeline.addLast(NETTIEMON_SERVER_HANDLER);

    }

}