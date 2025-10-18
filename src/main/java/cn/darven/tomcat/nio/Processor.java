package cn.darven.tomcat.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author darven
 * @date 2025/10/18
 * @description TODO
 */
public class Processor implements Runnable{

    private final SocketChannel clientChannel;
    private final ByteBuffer buffer;
    private final SelectionKey key; // 新增SelectionKey引用

    public Processor(SocketChannel socketChannel, ByteBuffer byteBuffer, SelectionKey key){
        this.clientChannel = socketChannel;
        this.buffer = byteBuffer;
        this.key = key;
    }

    @Override
    public void run() {
        try {
            // 检查通道是否打开
            if (!clientChannel.isOpen()) {
                System.out.println("通道已关闭，跳过处理");
                key.cancel(); // 取消SelectionKey
                return;
            }

            int read = clientChannel.read(buffer);
            if (read == -1) {
                clientChannel.close();
                System.out.println("客户端已断开连接");
                key.cancel(); // 取消SelectionKey
                return;
            }

            if (read == 0) {
                // 没有读取到数据，不取消key，等待下次读事件
                return;
            }

            buffer.flip();
            String request = new String(buffer.array(), 0, buffer.limit());
            System.out.println("HTTP请求数据：\n" + request);

            // 重置buffer位置，为写入响应做准备
            buffer.clear();

            // 解析HTTP请求并生成响应
            String httpResponse = generateHttpResponse(request);

            // 确保响应数据不超过buffer容量
            byte[] responseBytes = httpResponse.getBytes();
            if (responseBytes.length <= buffer.capacity()) {
                buffer.put(responseBytes);
            } else {
                // 如果响应太大，需要分块写入或使用更大的buffer
                buffer.put(responseBytes, 0, buffer.capacity());
            }

            buffer.flip();

            // 写入前再次检查通道状态
            if (clientChannel.isOpen()) {
                clientChannel.write(buffer);
            }

            // 关闭连接并取消SelectionKey
            clientChannel.close();
            key.cancel();

        } catch (IOException e) {
            System.out.println("处理请求时发生异常: " + e.getMessage());
            try {
                key.cancel(); // 出现异常也要取消key
                if (clientChannel.isOpen()) {
                    clientChannel.close();
                }
            } catch (IOException closeException) {
                // 忽略关闭异常
            }
        }
    }




    private String generateHttpResponse(String request) {
        // 简单的HTTP响应生成
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 200 OK\r\n");
        response.append("Content-Type: text/html; charset=utf-8\r\n");
        response.append("Connection: close\r\n");
        response.append("Server: SimpleNioServer\r\n");

        String content = "<html><body><h1>Hello from NIO Server!</h1><p>Request received successfully.</p></body></html>";
        response.append("Content-Length: ").append(content.getBytes().length).append("\r\n");
        response.append("\r\n");
        response.append(content);

        return response.toString();
    }
}
