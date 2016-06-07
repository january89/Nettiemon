package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
class NettiemonBySpring{

    @Autowired
    @Qualifier("tcpSocketAddress")
    private InetSocketAddress port;

    @Autowired
    @Qualifier("workerThreadCount")
    private int workerThreadCount;

    @Autowired
    @Qualifier("bossThreadCount")
    private int bossThreadCount;

    void start() throws Exception {

        try(
                EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadCount);
                EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadCount)
                ){

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            ChannelFuture channelFuture;

            serverBootstrap
                    .group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new NettiemonServerInitializer(null));
            Channel channel = serverBootstrap.bind(port).sync().channel();
            channelFuture = channel.closeFuture();
            channelFuture.sync();

        }
        catch (InterruptedException e){

            e.printStackTrace();

        }

    }

}
