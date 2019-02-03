import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName ServerHandler
 * @Description TODO
 * @Author liyunpeng
 * @Date 2018/11/15 11:13
 **/
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private static AtomicLong count = new AtomicLong(0);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        long cnt = count.incrementAndGet();
        logger.info("channelActive all cnt={} ,IP={}", cnt, getIP(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        long cnt = count.decrementAndGet();
        logger.info("channelInactive all cnt={} ,IP={}", cnt, getIP(ctx));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String str = (String)msg;
//            logger.info(str);
            ctx.writeAndFlush(str);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.info("timeout {} leave");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("exceptionCaught");
        ctx.close();
    }

    public static String getIP(ChannelHandlerContext ctx) {
        String IP = "null";
        if (ctx != null && ctx.channel() != null) {
            SocketAddress socketAddress = ctx.channel().remoteAddress();
            IP = socketAddress != null ? socketAddress.toString() : IP;
        }
        return IP;
    }
}
