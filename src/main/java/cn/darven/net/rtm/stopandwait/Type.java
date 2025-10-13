package cn.darven.net.rtm.stopandwait;

/**
 * @author darven
 * @date 2025/10/13
 * @description TODO
 */
/**
 * 类型枚举
 * */
public enum Type{
    DATA((byte) 0),
    ACK((byte) 1);

    private byte type;

    Type(byte type){
        this.type=type;
    }

    public byte getType(){
        return type;
    }
}
