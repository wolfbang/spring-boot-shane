package com.buzz.test.core.nio.cs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;

public class ServerSocketTest {
    private Selector selector;
    private Map<SocketChannel, List> dateMap;
    private InetSocketAddress listenAddress;

    public ServerSocketTest(String address, int port) {
        this.listenAddress = new InetSocketAddress(address, port);
        this.dateMap = new HashMap<>();
    }

    private void startServer() throws IOException {
        this.selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(listenAddress);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server starts now...");

        while (true) {
            this.selector.select();
            Iterator iterator = this.selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();
                iterator.remove();
                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    this.accept(key);
                } else if (key.isReadable()) {
                    this.read(key);
                } else if (key.isWritable()) {
                    this.write(key);
                }
            }
        }

    }


    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddress = socket.getRemoteSocketAddress();
        System.out.println("Received from remoteAddress:" + remoteAddress);
        dateMap.put(channel, new ArrayList<>());
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);
        if (numRead == -1) {
            this.dateMap.remove(channel);
            Socket socket = channel.socket();
            SocketAddress remoteAddress = socket.getRemoteSocketAddress();
            System.out.println("Connected closed by client :" + remoteAddress);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        System.out.println("Got:" + new String(data));
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        String message = new Date().toString();
        buffer.put(message.getBytes(Charset.forName("utf-8")));
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public static void main(String[] args) {
        Runnable serverBootstrap = () -> {
            try {
                new ServerSocketTest("localhost", 8090).startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(serverBootstrap).start();
    }
}
