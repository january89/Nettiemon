package server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.ssl.SslContext;

class NettiemonServerInitializer extends ChannelInitializer<SocketChannel>{
    private final SslContext sslContext;
    NettiemonServerInitializer(SslContext sslContext){
        this.sslContext = sslContext;
    }

    @Override
    public void initChannel(SocketChannel ch){
        ChannelPipeline channelpipeline = ch.pipeline();
        if(sslContext != null)
            channelpipeline.addLast(sslContext.newHandler(ch.alloc()));

        channelpipeline.addLast(new HttpRequestDecoder());
        channelpipeline.addLast(new HttpObjectAggregator(65536));
        channelpipeline.addLast(new HttpRequestEncoder());
        channelpipeline.addLast(new HttpContentCompressor());
        channelpipeline.addLast(new NettiemonServerHandler());
    }

}