package server;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderUtil.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaderUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

class NettiemonServerHandler extends SimpleChannelInboundHandler<FullHttpMessage>{

    private static final Logger logger = LoggerFactory.getLogger(NettiemonServerHandler.class);
    private HttpRequest request;
    private ObjectNode apiResult;

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private HttpPostRequestDecoder decoder;
    private Map<String, String> reqData = new HashMap<>();
    private static final Set<String> usingHeader = new HashSet<>();
    static{
        usingHeader.add("token");
        usingHeader.add("email");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        logger.info("요청 처리 완료");
        ctx.flush();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, FullHttpMessage msg){

        if (msg instanceof HttpRequest){
            this.request = (HttpRequest) msg;


            if(is100ContinueExpected(request)){
                send100Continue(ctx);
            }


            List<Map.Entry<String, String>> headers = request.headers().entriesConverted();
            if(!headers.isEmpty()){
                for (Map.Entry<String , String > h : headers){
                    String key = h.getKey();
                    if(usingHeader.contains(key)){
                        reqData.put(key,h.getValue());
                    }
                }
            }

            reqData.put("REQUEST_URI",request.uri());
            reqData.put("REQUEST_METHOD", String.valueOf(request.method().name()));
        }

        if(msg instanceof HttpContent){
            HttpContent httpContent = msg;

            ByteBuf content = httpContent.content();

            if(msg instanceof LastHttpContent){
                LastHttpContent trailer = msg;
                readPostData();
                HandlersApi service = ServiceDispatcher.dispatch(reqData);
                try{
                    service.executeService();
                    apiResult = service.getApiResult();
                }
                finally {
                    reqData.clear();
                }

                if(!writeResponse(trailer, ctx)){
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                            .addListener(ChannelFutureListener.CLOSE);
                }
                reset();

            }

        }

    }

    private void reset(){
        request = null;
    }

    private void readPostData() {
        try {
            decoder = new HttpPostRequestDecoder(factory, request);
            for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (InterfaceHttpData.HttpDataType.Attribute == data.getHttpDataType()) {
                    try {
                        Attribute attribute = (Attribute) data;
                        reqData.put(attribute.getName(), attribute.getValue());
                    }
                    catch (IOException e) {
                        logger.error("BODY Attribute: " + data.getHttpDataType().name(), e);
                        return;
                    }
                }
                else {
                    logger.info("BODY data : " + data.getHttpDataType().name() + ": " + data);
                }
            }
        }
        catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {
            logger.error(e.toString());
        }
        finally {
            if (decoder != null) {
                decoder.destroy();
            }
        }
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.

        boolean keepAlive = isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST, Unpooled.copiedBuffer(
                apiResult.toString(), CharsetUtil.UTF_8));


        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // -
            // http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, KEEP_ALIVE);
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.toString());
        ctx.close();
    }

}
