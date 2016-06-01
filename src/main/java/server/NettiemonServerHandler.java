package server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

class NettiemonServerHandler extends ChannelHandlerAdapter {
    private static final byte[] CONTENT = {'h','e','l','l','o'};
    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        if(msg instanceof HttpRequest){
            HttpRequest req = (HttpRequest) msg;

            if(HttpHeaderUtil.is100ContinueExpected(req))
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));

            boolean keepAlive = HttpHeaderUtil.isKeepAlive(req);
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
            fullHttpResponse.headers().set(CONTENT_TYPE, "text/plain");
            fullHttpResponse.headers().set(CONTENT_LENGTH,fullHttpResponse.content().toString());
            if (!keepAlive)
                ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
            else {

                fullHttpResponse.headers().set(CONNECTION, KEEP_ALIVE);
                ctx.write(fullHttpResponse);

            }

        }
    }
}
