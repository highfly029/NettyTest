import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

/**
 * @ClassName ClientHandler
 * @Description TODO
 * @Author liyunpeng
 * @Date 2018/11/15 11:54
 **/
public class ClientHandler extends ChannelInboundHandlerAdapter {
//    public static final AttributeKey<Long> CALLBACK_TIME = AttributeKey.newInstance("callback");
public static final AttributeKey<Long> CALLBACK_TIME = AttributeKey.valueOf("callback");
    public static AtomicLong receivedNum = new AtomicLong(0);
    public static AtomicLong allDelta = new AtomicLong(0);
    private static AtomicLong count = new AtomicLong(0);
    private Client client;

    private static final String baseData = "壹贰叁肆伍陆柒捌玖拾";
    private static String data1k;
    private static String data100k;
    private static final String dataSize = ConfigPropUtil.getValue("DATA_SIZE");
    public ClientHandler(Client client) {
        this.client = client;
        if (data1k == null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 34; i++) {
                stringBuilder.append(baseData);
            }
            stringBuilder.append("壹");
            stringBuilder.append("a");
            data1k = stringBuilder.toString();
        }
        if (data100k == null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 3413; i++) {
                stringBuilder.append(baseData);
            }
            stringBuilder.append("壹贰叁");
            stringBuilder.append("a");
            data100k = stringBuilder.toString();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        long cnt = count.incrementAndGet();
        System.out.println("channelActive cnt="+cnt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        count.decrementAndGet();
        EventLoopGroup loop = ctx.channel().eventLoop();
        loop.schedule(new Runnable() {
            @Override
            public void run() {
                client.createBootstrap(new Bootstrap(), loop);
            }
        }, 1L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            long sendTime = ctx.channel().attr(CALLBACK_TIME).get();
            long curTime = System.currentTimeMillis();
            long delta = curTime - sendTime;
            long receviedNum = receivedNum.incrementAndGet();
            long all = allDelta.addAndGet(delta);
//            System.out.println("curTime="+curTime+" sendTime="+sendTime+" delta="+delta +" receviedNum="+receviedNum+ " all="+all);
//            String str = (String)msg;
//            System.out.println("recv data="+str);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                if (dataSize.equalsIgnoreCase("1k")) {
                    ctx.channel().attr(CALLBACK_TIME).set(System.currentTimeMillis());
                    ctx.writeAndFlush(data1k);
                } else if (dataSize.equalsIgnoreCase("100k")) {
                    ctx.channel().attr(CALLBACK_TIME).set(System.currentTimeMillis());
                    ctx.writeAndFlush(data100k);
                }
            } else if (event.state() == IdleState.READER_IDLE) {
                System.out.println("time out");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        ctx.close();
    }
}
