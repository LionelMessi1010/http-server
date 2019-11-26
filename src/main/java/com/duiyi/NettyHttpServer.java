package com.duiyi;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @time: 2019/11/22 20:53
 * @version: 1.00
 * @author: duiyi
 */
public class NettyHttpServer {

    private final int PORT;

    public NettyHttpServer(int inetPort) {
        this.PORT = inetPort;
    }

    public void init() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            // 1. 绑定两个线程组分别用来处理客户端的连接和连接处理
            server.group(bossGroup, workGroup)
                    // 2. 绑定服务端通道NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // 3. 给读写事件的线程通道绑定handler去真正处理读写
                    // ChannelInitializer初始化通道用SocketChannel
                    .childHandler(new NettyHttpServerInitializer());

            // 4. 监听端口（服务器host和port端口），同步返回
            ChannelFuture future = server.bind(this.PORT).sync();
            System.out.println("Server started in the port："+ this.PORT);
            // 当通道关闭时继续向后执行，这是一个阻塞方法
            future.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}