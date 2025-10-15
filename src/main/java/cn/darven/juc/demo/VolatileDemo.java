package cn.darven.juc.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author darven
 * @date 2025/10/15
 * @description TODO
 */
public class VolatileDemo {
    // 不使用volatile关键字
    private  static int counter = 0;
    // 控制线程是否继续运行
    private static boolean flag = true;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // 启动一个线程不断累加counter
        executorService.execute(() -> {
            while (flag) {
                counter++;
                try {
                    Thread.sleep(10); // 让出CPU时间片
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("计数线程结束, 最终counter=" + counter);
        });

        // 启动另一个线程在3秒后设置flag为false
        executorService.execute(() -> {
            try {
                Thread.sleep(3000); // 3秒后停止计数
                flag = false;
                System.out.println("flag已设为false, counter=" + counter);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executorService.shutdown();

        // 等待足够时间观察结果
        Thread.sleep(5000);
        System.out.println("主线程结束, 最终counter=" + counter);
    }
}
