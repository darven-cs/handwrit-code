package cn.darven.net.rtm.pipe;

/**
 * @author darven
 * @date 2025/10/14
 * @description TODO
 */

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.concurrent.locks.LockSupport;

public class SRProtocol {

    /* ========== 协议常量 ========== */
    static final int MAX_SEQ = 32;          // 序号空间 0..31
    static final int WIN_SIZE = MAX_SEQ / 2; // 16
    static final int PKT_DATA_LEN = 32;       // 数据部分字节数
    static final int BASE_PORT = 8888;
    static final double LOSS_RATE = 0.10;     // 模拟丢包
    static final double SHUFFLE_RATE = 0.15;  // 模拟乱序

    /* ========== 数据分组 ========== */
    static class Packet implements Serializable {
        int seq;
        int ack;
        byte[] data;
        int checksum;

        Packet(int seq, byte[] d) {
            this.seq = seq;
            this.data = d == null ? new byte[0] : d;
            this.checksum = Arrays.hashCode(this.data) + seq;
        }

        boolean isCorrupt() {
            return checksum != (Arrays.hashCode(data) + seq);
        }
    }

    /* ========== 发送方 ========== */
    static class Sender implements Runnable {
        private final InetSocketAddress dst;
        private DatagramSocket socket;
        private final ConcurrentHashMap<Integer, Packet> pktBuf = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Integer, Timer> timers = new ConcurrentHashMap<>();
        private volatile int base = 0, nextSeq = 0;

        Sender(InetSocketAddress dst) {
            this.dst = dst;
        }

        public void run() {
            try {
                socket = new DatagramSocket();
                new Thread(new AckReceiver()).start();
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = in.readLine()) != null) {
                    send(line.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /* 发送逻辑 */
        private void send(byte[] data) throws IOException {
            while (nextSeq >= base + WIN_SIZE) {  // 窗口已满
                LockSupport.parkNanos(1_000_000); // 1ms 自旋
            }
            Packet pkt = new Packet(nextSeq % MAX_SEQ, Arrays.copyOf(data, PKT_DATA_LEN));
            pktBuf.put(pkt.seq, pkt);  // 缓存包，避免发送失败
            udtSend(pkt);  // 发送包
            startTimer(pkt.seq);  // 设置定时器
            nextSeq++;
        }

        /* 收到 ACK */
        private void handleAck(int ack) {
            if (inWindow(ack, base, WIN_SIZE)) { // 如果在窗口内
                timers.remove(ack); // 删除定时器
                pktBuf.remove(ack); // 删除缓存
                while (!pktBuf.containsKey(base % MAX_SEQ) && base < nextSeq) {  // 如果窗口内没有包，则更新 base
                    base = (base + 1) % MAX_SEQ;
                }
            }
        }

        /* 定时器 */
        private void startTimer(int seq) {
            Timer t = new Timer();
            timers.put(seq, t);
            t.schedule(new TimerTask() {
                public void run() {
                    timeout(seq);
                }
            }, 300); // 300 ms
        }

        private void stopTimer(int seq) {
            Optional.ofNullable(timers.remove(seq)).ifPresent(Timer::cancel);
        }

        // 如果超时，重新发送包
        private void timeout(int seq) {
            Packet p = pktBuf.get(seq);
            if (p != null) {
                try {
                    udtSend(p);
                    startTimer(seq);
                } catch (IOException ignored) {
                }
            }
        }

        /* 下层 UDP 发送 + 模拟丢包 */
        private void udtSend(Packet p) throws IOException {
            if (Math.random() < LOSS_RATE) { /* 故意丢 */
                return;
            }
            byte[] buf = toBytes(p);
            DatagramPacket dp = new DatagramPacket(buf, buf.length, dst);
            socket.send(dp);
        }

        /* ACK 接收线程 */
        class AckReceiver implements Runnable {
            public void run() {
                try {
                    while (true) {
                        DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
                        socket.receive(dp);
                        Packet ackPkt = (Packet) fromBytes(dp.getData());
                        if (!ackPkt.isCorrupt()) handleAck(ackPkt.ack);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    /* ========== 接收方 ========== */
    static class Receiver implements Runnable {
        private DatagramSocket socket;
        private final int localPort;
        private volatile int rcvBase = 0;
        private final ConcurrentHashMap<Integer, Packet> rcvBuf = new ConcurrentHashMap<>();

        Receiver(int port) {
            this.localPort = port;
        }

        public void run() {
            try {
                socket = new DatagramSocket(localPort);
                System.out.println("Receiver ready @ " + localPort);
                while (true) {
                    DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(dp);
                    Packet p = (Packet) fromBytes(dp.getData());
                    if (p.isCorrupt()) continue;
                    deliver(p, dp.getSocketAddress());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /* SR 接收逻辑 */
        private void deliver(Packet p, SocketAddress sender) throws IOException {
            int seq = p.seq;
            if (inWindow(seq, rcvBase, WIN_SIZE) || inWindow(seq, rcvBase - WIN_SIZE, WIN_SIZE)) {
                // 回 ACK
                sendAck(seq, sender);
                if (inWindow(seq, rcvBase, WIN_SIZE)) {
                    rcvBuf.put(seq, p);
                    // 连续交付
                    while (rcvBuf.containsKey(rcvBase)) {
                        byte[] data = rcvBuf.remove(rcvBase).data;
                        System.out.write(data, 0, data.length);
                        System.out.println();
                        rcvBase = (rcvBase + 1) % MAX_SEQ;
                    }
                }
            } else {
                // 窗口外的重复，再 ACK 一次
                sendAck(seq, sender);
            }
        }

        private void sendAck(int ack, SocketAddress to) throws IOException {
            Packet ackPkt = new Packet(0, null);
            ackPkt.ack = ack;
            byte[] buf = toBytes(ackPkt);
            // 模拟乱序：延迟随机 0/50/100 ms
            if (Math.random() < SHUFFLE_RATE) {
                new Thread(() -> {
                    try {
                        Thread.sleep(50 + new Random().nextInt(3) * 50);
                    } catch (InterruptedException ignored) {
                    }
                    try {
                        socket.send(new DatagramPacket(buf, buf.length, to));
                    } catch (IOException ignored) {
                    }
                }).start();
            } else {
                socket.send(new DatagramPacket(buf, buf.length, to));
            }
        }
    }

    /* ========== 工具 ========== */
    private static boolean inWindow(int seq, int base, int size) {
        return (seq >= base && seq < base + size) || (base + size >= MAX_SEQ && seq < (base + size) % MAX_SEQ);
    }

    // 序列化
    private static byte[] toBytes(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(o);
        }
        return baos.toByteArray();
    }

    // 反序列化
    private static Object fromBytes(byte[] buf) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf))) {
            return ois.readObject();
        }
    }

    /* ========== 启动入口 ========== */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage:\n  java SRProtocol sender  <receiverHost>\n  java SRProtocol receiver");
            return;
        }
        if (args[0].equals("sender")) {
            InetSocketAddress dst = new InetSocketAddress(args[1], BASE_PORT);
            new Thread(new Sender(dst)).start();
        } else if (args[0].equals("receiver")) {
            new Thread(new Receiver(BASE_PORT)).start();
        }
    }
}