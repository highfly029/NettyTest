import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @ClassName Client
 * @Description TODO
 * @Author liyunpeng
 * @Date 2018/11/15 11:19
 **/
public class Client {
    public static final int MAX_RETRY = 5;
    private static EventLoopGroup loop;
    private int retry;
    private String host;
    private int port;

    public Client() {
        if (loop == null) {
            loop = new NioEventLoopGroup(ConfigPropUtil.getIntValue("CLIENT_THREAD_NUM"));
        }
        retry = MAX_RETRY;
    }

    public Bootstrap createBootstrap(Bootstrap bootstrap, EventLoopGroup eventLoopGroup) {
        if (bootstrap != null) {

            ClientHandler clientHandler = new ClientHandler(this);
            bootstrap.group(eventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast("heart", new IdleStateHandler(30000, ConfigPropUtil.getIntValue("WRITE_TIME"), 0, TimeUnit.MILLISECONDS));
                    ch.pipeline().addLast("encode", new StringEncoder(Charset.forName("UTF-8")));
                    ch.pipeline().addLast("decode", new StringDecoder(Charset.forName("UTF-8")));
                    ch.pipeline().addLast("logic", clientHandler);
                }
            });
            bootstrap.remoteAddress(host,port);
            bootstrap.connect().addListener(new ClientConnectionListener(this));

        }
        return bootstrap;
    }

    public void connect(String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        this.host = host;
        this.port = port;
        createBootstrap(bootstrap, loop);
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }
}
