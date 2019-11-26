package com.duiyi;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @time: 2019/11/22 21:42
 * @version: 1.00
 * @author: duiyi
 *
 * Init Channel
 */
public class NettyHttpServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 请求解码器
        socketChannel.pipeline().addLast("http-decoder", new HttpRequestDecoder());
        // 聚合器，将HTTP消息的多个部分合成一条完整的HTTP消息，设置最大聚合字节长度为1M
        socketChannel.pipeline().addLast("http-aggregator", new HttpObjectAggregator(1024 * 1024));
        // 响应编码器
        socketChannel.pipeline().addLast("http-encoder", new HttpResponseEncoder());
        // 解决大码流的问题，ChunkedWriteHandler：向客户端发送HTML文件
        socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        // 自定义处理handler
        socketChannel.pipeline().addLast("http-server", new NettyHttpServerHandler());
    }
}
