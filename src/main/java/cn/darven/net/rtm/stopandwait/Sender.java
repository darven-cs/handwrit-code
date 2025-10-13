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
public class Sender {
    public static void main(String[] args) {
        StopAndWait stopAndWait = new StopAndWait();
//        Scanner scanner=new Scanner(System.in);
//        String s = scanner.next();
//        byte[] data = s.getBytes();
        StringBuilder testData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            testData.append("DataPacket_").append(i).append("_");
        }

        String dataStr = testData.toString();
        byte[] data = dataStr.getBytes();
        try (DatagramSocket socket=new DatagramSocket(8888)){
            InetSocketAddress address=new InetSocketAddress(9999);
            stopAndWait.send(data,10,socket,address);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}
