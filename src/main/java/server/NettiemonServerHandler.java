package server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

class NettiemonServerHandler extends SimpleChannelInboundHandler<String>{

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        ctx.write(ResponseGenerator.makeHello());
        ctx.flush();
    }
    @Override
    public void messageReceived(ChannelHandlerContext ctx,String request) throws Exception{
        ResponseGenerator generator = new ResponseGenerator(request);
        String response = generator.response();
        ChannelFuture future = ctx.write(response);

        if (generator.isClose()){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
