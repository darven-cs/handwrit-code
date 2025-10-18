package cn.darven.tomcat.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author darven
 * @date 2025/10/18
 * @description nio监听器
 */
public class Acceptor implements Runnable{

    private final int port;
    private final ExecutorService executorService;
    private volatile boolean stop=false;

    public Acceptor(int port,ExecutorService executorService){
        this.port=port;
        this.executorService=executorService;
    }

    @Override
    public void run() {
        // 创建选择器
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ){

            serverSocketChannel.bind(new InetSocketAddress(port));
            // 非阻塞的
            serverSocketChannel.configureBlocking(false);
            // 注册监听，处理接收请求
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动，监听端口"+port+",等待连接....");

            // 循环处理连接/读请求
            while (!stop){
                int select = selector.select();
                if(select==0){
                    continue;
                }
                // 就绪事件队列
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                // 循环处理
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if(!key.isValid()){
                        continue;
                    }

                    if(key.isAcceptable()){
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverChannel.accept();
                        if(socketChannel != null) {
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                            System.out.println("新客户端连接：" + socketChannel.getRemoteAddress());
                        }
                    }

                    else if(key.isReadable()){

                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        // 检查通道是否仍然有效
                        if(socketChannel.isOpen()) {
                            // 处理前先检查是否有待处理数据
                            ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                            System.out.println("处理客户端请求："+socketChannel.getRemoteAddress());
                            // 提交给线程池处理
                            executorService.execute(new Processor(socketChannel, byteBuffer, key));
                            // 注意：这里不再立即处理，让Processor自己处理完后取消注册
                        } else {
                            // 通道已关闭，取消key
                            key.cancel();
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void start(){
        System.out.println("启动监听器");
        new Thread(this,"Accept-Thread").start();
        System.out.println("监听器启动成功，监听端口="+ port);
    }


    public void stop(){
        stop=true;
    }
}
