package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
public class NettiemonServer {

    private static final boolean SSL = System.getProperty("SSL") != null;
    private static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));

    public static void main(String[] args) throws Exception {

        final SslContext sslContext;

        if(SSL){

            SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
            sslContext = SslContext.newServerContext(selfSignedCertificate.certificate(),selfSignedCertificate.privateKey());

        } else
            sslContext = null;


        try(
                EventLoopGroup bossGroup = new NioEventLoopGroup(4);
                EventLoopGroup workerGroup = new NioEventLoopGroup()
        ){

            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.option(ChannelOption.SO_BACKLOG , 1024);
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new NettiemonServerInitializer(sslContext));

            Channel channel = bootstrap.bind(PORT).sync().channel();

            System.err.println("Open your web browser and navigate to " + (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');
            channel.closeFuture().sync();

        }

    }

}
