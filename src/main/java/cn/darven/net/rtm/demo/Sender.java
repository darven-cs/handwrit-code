package cn.darven.net.rtm.demo;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.zip.CRC32;

/**
 * @author darven
 * @date 2025/10/13
 * @description 实现可靠传输机制中的校验，重传，ack
 */
public class Sender {
    // 最大重试次数
    private static final int MAX_RETRY = 3;
    // 超时时间
    private static final int TIMEOUT = 2000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String next = scanner.next();
        System.out.println("传输内容:"+next);

        // 先实现校验，重传，ack机制，不管序号先，简化版停传
        // 数据
        byte[] payload = next.getBytes();

        // 加上校验
        CRC32 crc32 = new CRC32();
        crc32.update(payload);
        long crc32Value = crc32.getValue();

        // 封装data
        ByteBuffer byteBuffer = ByteBuffer.allocate(payload.length+4);
        byteBuffer.put(payload);
        System.out.println("校验数："+crc32Value);
        byteBuffer.putInt((int)crc32Value);

        // bytebuffer转化成数组
        byte[] data = byteBuffer.array();
        // 使用udp模仿ip
        try (DatagramSocket socket = new DatagramSocket(8888)){
            for (int i=0;i<MAX_RETRY;i++){
                try {
                    System.out.println("重试发送第"+(i+1)+"次");                    // 准备发送信息数据
                    DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("127.0.0.1"), 9999);
                    socket.send(packet);

                    // 获取返回数据
                    byte[] buffer=new byte[1024];
                    DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
                    // 设置超时
                    socket.setSoTimeout(TIMEOUT);
                    socket.receive(packet1);
                    // 规定第一位是ack标志位
                    byte[] data1 = packet1.getData();
                    if(data1[0]==1){
                        System.out.println("接收到ACK信号，传送成功");
                        break;
                    }else{
                        System.out.println("失败");
                    }
                }catch (SocketTimeoutException e){
                    continue;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
