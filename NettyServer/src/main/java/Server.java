
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * @ClassName Server
 * @Description TODO
 * @Author liyunpeng
 * @Date 2018/11/15 10:50
 **/
public class Server {
    private static final String BOSS_THREA_NAME = "AcceptThread";
    private static final String WORKER_THREAD_NAME = "IO-Thread";
    private final int BOSS_THREAD_NUM;
    private final int WORKER_THREAD_NUM;
    private final int IO_RATIO;
    public Server() {
        BOSS_THREAD_NUM = ConfigPropUtil.getIntValue("BOSS_THREAD_NUM");
        WORKER_THREAD_NUM = ConfigPropUtil.getIntValue("WORKER_THREAD_NUM");
        IO_RATIO = ConfigPropUtil.getIntValue("IO_RATIO");
    }
    public static  boolean isUseEpollET() {
        boolean isLinux = false;
        if (System.getProperty("os.name").startsWith("Linux")) {
            isLinux = true;
        }
        if (isLinux && ConfigPropUtil.getValue("EPOLL_MODE").equalsIgnoreCase("ET")) {
            System.out.println("EPOLL_MODE=ET");
            return true;
        } else {
            return false;
        }
    }

    public void run(int port) throws Exception {
        EventLoopGroup boss = null;
        EventLoopGroup worker = null;
        if (isUseEpollET()) {
            boss = new EpollEventLoopGroup(BOSS_THREAD_NUM, new DefaultThreadFactory(BOSS_THREA_NAME));
            worker = new EpollEventLoopGroup(WORKER_THREAD_NUM, new DefaultThreadFactory(WORKER_THREAD_NAME));
            ((EpollEventLoopGroup)boss).setIoRatio(IO_RATIO);
            ((EpollEventLoopGroup)worker).setIoRatio(IO_RATIO);
        } else {
            boss = new NioEventLoopGroup(BOSS_THREAD_NUM, new DefaultThreadFactory(BOSS_THREA_NAME));
            worker = new NioEventLoopGroup(WORKER_THREAD_NUM, new DefaultThreadFactory(WORKER_THREAD_NAME));
            ((NioEventLoopGroup)boss).setIoRatio(IO_RATIO);
            ((NioEventLoopGroup)worker).setIoRatio(IO_RATIO);
        }
        if (null == boss || null == worker) {
            throw new Exception("eventLoopGroup init error");
        }
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker);
            if (isUseEpollET()) {
                b.channel(EpollServerSocketChannel.class);
            } else {
                b.channel(NioServerSocketChannel.class);
            }
            b.option(ChannelOption.SO_BACKLOG, ConfigPropUtil.getIntValue("SO_BACKLOG"));
            b.childOption(ChannelOption.TCP_NODELAY, true);
            b.childOption(ChannelOption.SO_RCVBUF, ConfigPropUtil.getIntValue("SO_RCVBUF"));
            b.childOption(ChannelOption.SO_SNDBUF, ConfigPropUtil.getIntValue("SO_SNDBUF"));

            b.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast("heart", new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                    socketChannel.pipeline().addLast("strDecoder", new StringDecoder(Charset.forName("UTF-8")));
                    socketChannel.pipeline().addLast("strEncoder", new StringEncoder(Charset.forName("UTF-8")));

                    socketChannel.pipeline().addLast("logic", new ServerHandler());
                }
            });
            ChannelFuture future = b.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
