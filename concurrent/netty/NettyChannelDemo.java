package com.example.dovernetty.nio;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author dover
 * @since 2021/9/28
 */
public class NettyChannelDemo {

    @SneakyThrows
    public static void main(String[] args) {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(InetAddress.getByName("IP"), 8080));
        serverChannel.configureBlocking(false);
        // 启动 selector
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        new Thread(() -> {
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey : selectionKeys) {
                if (selectionKey.isValid()) {
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                        try {
                            // channel 监听客户端连接请求，握手成功后将客户端连接注册到 selector
                            SocketChannel acceptChannel = channel.accept();
                            acceptChannel.configureBlocking(false);
                            acceptChannel.register(selector, SelectionKey.OP_READ);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (selectionKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                    }
                }
            }
        }).start();
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.socket().setReuseAddress(true);
    }
}
