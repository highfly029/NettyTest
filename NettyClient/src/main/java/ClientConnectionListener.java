
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.TimeUnit;

public class ClientConnectionListener implements ChannelFutureListener {

    private Client client;
    public ClientConnectionListener(Client client) {
        this.client = client;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            System.out.println("connect success! cnt");
//            client.setRetry(Client.MAX_RETRY);
        } else if (client.getRetry() <= 0) {
            System.out.println("retry cnt is zero, over");
        } else {
            // 第几次重连
            int order = (Client.MAX_RETRY - client.getRetry()) + 1;
            // 本次重连的间隔
            int delay = 1 << order;
            client.setRetry(client.getRetry()-1);
            System.out.println("connect fail,and begin the " + order + " times ……");
            final EventLoopGroup loop = future.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    Bootstrap bootstrap = new Bootstrap();
                    client.createBootstrap(bootstrap, loop);
                }
            }, delay, TimeUnit.SECONDS);
        }
    }
}
