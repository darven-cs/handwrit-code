package cn.darven.tomcat.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author darven
 * @date 2025/10/18
 * @description nio模型代码
 */
public class NioSocketServerDemo {
    public static void main(String[] args) {
        try (Selector selector = Selector.open()){
            // 设置服务器通道，设置非阻塞，端口号
            ServerSocketChannel socketChannel=ServerSocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.bind(new InetSocketAddress(8080));

            // 连接就绪事件
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动，监听端口8080,等待连接....");

            while (true){
                // 阻塞等待连接
                int select = selector.select();
                if(select==0){
                    continue;
                }

                // 获取连接就绪事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if(key.isAcceptable()){
                        // 监听客户端连接
                        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                        SocketChannel clientSocket = channel.accept();
                        clientSocket.configureBlocking(false);

                        System.out.println("新客户端连接："+clientSocket.getRemoteAddress());
                        // 读就绪事件
                        clientSocket.register(selector,SelectionKey.OP_READ,ByteBuffer.allocate(1024));
                    }
                    // 处理读就绪事件
                    else if(key.isReadable()){
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer =(ByteBuffer) key.attachment();

                        int read = clientChannel.read(buffer);
                        if(read==-1){
                            clientChannel.close();
                            System.out.println("客户端已断开连接："+clientChannel.getRemoteAddress());
                            continue;
                        }

                        buffer.flip();
                        System.out.println("接收到数据："+new String(buffer.array(),0,buffer.limit()));

                        buffer.clear();
                        buffer.put("服务接收到数据\n".getBytes());
                        buffer.flip();
                        clientChannel.write(buffer);

                        buffer.clear();
                    }

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
