package cn.darven.net.rtm.demo;

/**
 * @author darven
 * @date 2025/10/13
 * @description TODO
 */

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Receiver {
    public static void main(String[] args) throws Exception {
        while (true){
            try (DatagramSocket socket = new DatagramSocket(9999)) {
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);
                // 获取实际接收的数据
                byte[] receivedData = packet.getData();
                int dataLength = packet.getLength();

                // 提取CRC值（最后4个字节）
                ByteBuffer buffer = ByteBuffer.wrap(receivedData, 0, dataLength);
                int crcOffset = dataLength - 4;
                buffer.position(crcOffset);
                int receivedCrc = buffer.getInt();

                // 提取实际payload数据（除最后4字节外的所有数据）
                byte[] payload = new byte[crcOffset];
                System.arraycopy(receivedData, 0, payload, 0, crcOffset);

                // 重新计算CRC
                CRC32 crc32 = new CRC32();
                crc32.update(payload);
                long calculatedCrc = crc32.getValue();
                System.out.println("校验数："+receivedCrc+" "+calculatedCrc);
                // 比较CRC值
                if ((calculatedCrc & 0xffffffffL) == (receivedCrc & 0xffffffffL)) {
                    System.out.println("接收到数据：" + new String(payload));
                    // 发送ACK
                    byte[] ackData = new byte[1];
                    ackData[0] = 1;
                    DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, packet.getAddress(), 8888);
                    socket.send(ackPacket);
                } else {
                    System.out.println("校验失败");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
