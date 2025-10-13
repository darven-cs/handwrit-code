package cn.darven.net.rtm.stopandwait;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;

/**
 * @author darven
 * @date 2025/10/13
 * @description 停等
 */
public class StopAndWait {
    private static final int TIMEOUT = 1000; // 1秒超时
    private static final int MAX_COUNT = 3;

    // 发送
    public void send(byte[] data, int packetSize, DatagramSocket socket, InetSocketAddress socketAddress) {
        // 切片
        List<DataPacket> packetList = packetDataPacket(data, packetSize);
        // 循环发送包
        for (DataPacket packet : packetList) {
            // 最大重试次数
            for (int i = 0; i < MAX_COUNT; i++) {
                try {
                    // 发送包,序列化
                    byte[] buf = packPacket(packet);
                    // 发送数据包
                    socket.send(new DatagramPacket(buf, buf.length, socketAddress));
                    // 接收ack
                    byte[] ack = new byte[1024];
                    DatagramPacket ackReceiver = new DatagramPacket(ack, ack.length);
                    socket.setSoTimeout(TIMEOUT);
                    socket.receive(ackReceiver);
                    DataPacket ackUnPacket = unpackPacket(ackReceiver.getData());
                    // 确定接收
                    if (Objects.equals(ackUnPacket.getType(), Type.ACK.getType()) &&
                            ackUnPacket.getSequenceNumber() == packet.getSequenceNumber()) {
                        System.out.println("数据包=" + ackUnPacket.getSequenceNumber() + "发送成功");
                        break;
                    } else {
                        System.out.println("数据包发送失败" + packet.getSequenceNumber() + "，重试次数：" + (i + 1));
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("发送超时，重试次数：" + (i + 1));
                    if (i == MAX_COUNT - 1) {
                        System.out.println("数据包" + packet.getSequenceNumber() + "发送失败，达到最大重试次数");
                    }
                } catch (IOException e) {
                    System.out.println("发送异常：" + e.getMessage());
                    if (i == MAX_COUNT - 1) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    // 接收
    public String receiver(DatagramSocket socket, InetSocketAddress address, int packetSize) {
        int expectSeq = 0;
        // 数据包
        List<DataPacket> packetList = new ArrayList<>();
        while (true) {
            // 接收数据
            byte[] buf = new byte[packetSize];
            try {
                DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
                socket.receive(receivePacket);
                DataPacket packet = unpackPacket(Arrays.copyOf(buf, receivePacket.getLength()));

                // 校验数据
                if (!verifyChecksum(packet.getPayload(), packet.getCrc())) {
                    System.out.println("数据校验失败，数据包=" + packet.getSequenceNumber());
                    // 发送NAK或者不发送ACK
                    continue;
                }

                // 检查序列号
                if (packet.getSequenceNumber() == expectSeq) {
                    // 创建ACK包
                    DataPacket ackPacket = new DataPacket();
                    ackPacket.setType(Type.ACK.getType());
                    ackPacket.setSequenceNumber(expectSeq);

                    // 序列化并发送ACK
                    byte[] ackBytes = packAckPacket(ackPacket);
                    socket.send(new DatagramPacket(ackBytes, ackBytes.length, address));

                    System.out.println("接收成功，数据包="+packet.getSequenceNumber()+"数据="+new String(packet.getPayload()));

                    // 缓存数据包
                    packetList.add(packet);
                    expectSeq++;

                    // 到达最后一个退出
                    if (packet.isLast()) {
                        break;
                    }
                } else {
                    // 序号不匹配，可能是重复包，重新发送ACK
                    DataPacket ackPacket = new DataPacket();
                    ackPacket.setType(Type.ACK.getType());
                    ackPacket.setSequenceNumber(expectSeq - 1); // 确认最近正确接收的序号

                    byte[] ackBytes = packAckPacket(ackPacket);
                    socket.send(new DatagramPacket(ackBytes, ackBytes.length, address));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // 组合切片
        return unpackDataPacket(packetList);
    }

    // 切片
    public List<DataPacket> packetDataPacket(byte[] data, int dataSize) {
        int offset = 0;
        int seqNum = 0;
        List<DataPacket> packetList = new ArrayList<>();

        while (offset < data.length) {
            int remaining = data.length - offset;
            int currentSize = Math.min(dataSize, remaining);
            byte[] payload = new byte[currentSize];
            // copy
            System.arraycopy(data, offset, payload, 0, currentSize);

            DataPacket packet = new DataPacket();
            packet.setPayload(payload);
            packet.setCrc(calculateChecksum(payload)); // 只对payload计算CRC
            packet.setType(Type.DATA.getType());
            packet.setLast(offset + currentSize >= data.length);
            packet.setSequenceNumber(seqNum++);
            packet.setAck(false);

            offset += currentSize;
            packetList.add(packet);
        }
        return packetList;
    }

    // 组合切片
    public String unpackDataPacket(List<DataPacket> packetList) {
        StringBuilder sb = new StringBuilder();
        for (DataPacket packet : packetList) {
            sb.append(new String(packet.getPayload()));
        }
        return sb.toString();
    }

    // 序列化数据包
    public byte[] packPacket(DataPacket packet) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + packet.getPayload().length + 4 + 1);
        buffer.put(packet.getType());
        buffer.putInt(packet.getSequenceNumber());
        buffer.put(packet.getPayload());
        buffer.putInt(packet.getCrc());
        buffer.put((byte) (packet.isLast() ? 1 : 0));
        return buffer.array();
    }

    // 序列化ACK包
    public byte[] packAckPacket(DataPacket ackPacket) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4); // Type(1) + SequenceNumber(4)
        buffer.put(ackPacket.getType());
        buffer.putInt(ackPacket.getSequenceNumber());
        return buffer.array();
    }

    // 反序列化数据包
    public DataPacket unpackPacket(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        DataPacket packet = new DataPacket();
        packet.setType(buffer.get());           // 读取类型
        packet.setSequenceNumber(buffer.getInt()); // 读取序列号

        // 根据类型判断是否为数据包还是ACK包
        if (packet.getType() == Type.DATA.getType()) {
            byte[] payload = new byte[buffer.remaining() - 5]; // 剩余长度减去CRC(4)和isLast(1)
            buffer.get(payload);                    // 读取载荷数据
            packet.setPayload(payload);
            packet.setCrc(buffer.getInt());         // 读取CRC校验码
            packet.setLast(buffer.get() == 1);      // 读取是否为最后一个分片标志
        }

        return packet;
    }

    // 校验
    public boolean verifyChecksum(byte[] data, int expectedCrc) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return (int) crc32.getValue() == expectedCrc;
    }

    // 生成校验数
    public int calculateChecksum(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return (int) crc32.getValue();
    }
}
