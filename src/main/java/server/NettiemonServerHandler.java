package server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.json.JsonObjectDecoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderUtil.is100ContinueExpected;

class NettiemonServerHandler extends SimpleChannelInboundHandler<FullHttpMessage>{
//    private static final Logger logger = LogManager.getLogManager().getLogger(NettiemonServerHandler.class);

    private HttpRequest request;
    private JsonObjectDecoder apiResult;

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private HttpPostRequestDecoder decoder;
    private Map<String,String> reqData = new HashMap<String, String>();
    private static final Set<String> usingHeader = new HashSet<String>();
    static{
        usingHeader.add("token");
        usingHeader.add("email");
    }


//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception{
//        ctx.write(ResponseGenerator.makeHello());
//        ctx.flush();
//    }

    @Override
    public void messageReceived(ChannelHandlerContext channelHandlerContext,FullHttpMessage msg){


        if (msg instanceof HttpRequest){
            this.request = (HttpRequest) msg;

            if(is100ContinueExpected(request)){
            }

            HttpHeaders httpHeaders = request.headers();
            if(!httpHeaders.isEmpty()){
                for (Map.Entry<String ,String> h : httpHeaders){
                    String key = h.getKey();
                    if(usingHeader.contains(key)){
                        reqData.put(key,h.getValue());
                    }
                }
            }

            reqData.put("REQUEST_URI",request.getUri());
            reqData.put("REQUEST_METHOD",request.getMethod().name());
        }

        if(msg instanceof HttpContent){
            HttpContent httpContent = msg;

            ByteBuf content = httpContent.content();

            if(msg instanceof LastHttpContent){
                LastHttpContent trailer = msg;
                readPostData();
                HandlersApi service = ServiceDispatcher.dispatch(reqData);
                try(
                    service.executeService();
                    apiResult = service.getApiResult()){}

                reqData.clear();

                if(!writeResponse(trailer,channelHandlerContext)){
                    channelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER)
                            .addListener(ChannelFutureListener.CLOSE);
                }
                reset();

            }

        }

    }

    private void reset(){
        request = null;
    }

    private void readPostData(){

    }
}
