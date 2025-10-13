package cn.darven.net.rtm.stopandwait;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 * @author darven
 * @date 2025/10/13
 * @description TODO
 */
public class Receiver {
    public static void main(String[] args) {
        StopAndWait stopAndWait = new StopAndWait();
        try (DatagramSocket socket=new DatagramSocket(9999)){
            InetSocketAddress address=new InetSocketAddress(8888);
            String receiver = stopAndWait.receiver(socket, address, 10);
            System.out.println("接收数据="+receiver);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}
