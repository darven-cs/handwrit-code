package cn.darven.tomcat.nio;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author darven
 * @date 2025/10/18
 * @description TODO
 */
public class Main {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = ThreadPoolConfig.createThreadPool();
        Acceptor acceptor = new Acceptor(8080, executor);
        acceptor.start();

        // 3. 注册关闭钩子（优雅退出）
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("开始关闭容器...");
            acceptor.stop();  // 停止接收新连接
            executor.shutdown();  // 关闭线程池（等待任务完成）
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();  // 超时强制关闭
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
            System.out.println("容器已关闭");
        }));
    }
}
