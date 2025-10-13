package cn.darven.net.rtm.stopandwait;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author darven
 * @date 2025/10/13
 * @description 数据包
 */
public class DataPacket {
    private byte type;   // 类型 data,ack
    private boolean ack;  // ack符号位
    private int sequenceNumber;  // 序列号
    private byte[] payload;  //数据
    private int crc;   // crc校验数
    private boolean isLast;  //是否是最后一个

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    public boolean isLast() {
        return isLast;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DataPacket packet = (DataPacket) o;
        return type == packet.type && sequenceNumber == packet.sequenceNumber && crc == packet.crc && isLast == packet.isLast && ack == packet.ack && Objects.deepEquals(payload, packet.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sequenceNumber, Arrays.hashCode(payload), crc, isLast, ack);
    }
}
