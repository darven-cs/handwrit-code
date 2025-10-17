package cn.darven.tomcat.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author darven
 * @date 2025/10/17
 * @description 监听器
 */
public class Acceptor implements Runnable{
    private final int port;
    private final ExecutorService executorService;
    private volatile boolean stop=false;

    public Acceptor(int port, ExecutorService executorService) {
        this.port = port;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        while (!stop){
            try(ServerSocket serverSocket=new ServerSocket(port)){

                // 接收连接
                Socket socket= serverSocket.accept();

                System.out.println("接收到请求="+socket.getRemoteSocketAddress());

                // 独立开出线程池进行处理
                executorService.execute(new Processor(socket));

            } catch (IOException e) {
                System.err.println("连接中断：");
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        stop=true;
    }

    // 启动监听器
    public void start(){
        System.out.println("启动监听器");
        new Thread(this,"Accept-Thread").start();
        System.out.println("监听器启动成功，监听端口="+ port);
    }
}
