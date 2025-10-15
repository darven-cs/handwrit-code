package cn.darven.juc.demo;

import java.util.concurrent.CountDownLatch;

/**
 * @author darven
 * @date 2025/10/15
 * @description 同步工具类，协调多个线程之间执行的顺序
 * 阻塞主线程，等待其他线程完成
 */
public class CountDownLatchDemo {
    public static void main(String[] args) {
        int count = 5;
        CountDownLatch countDownLatch = new CountDownLatch(count);  // 创建一个计数器，初始值为5


        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " 开始执行任务");
                    Thread.sleep(1000); // 模拟任务执行
                    System.out.println(Thread.currentThread().getName() + " 任务完成");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }finally {
                    countDownLatch.countDown();  // 线程执行完毕，计数器减1
                }

            }).start();
        }

        System.out.println("等待线程执行");
        try {
            countDownLatch.await();  // 等待所有线程执行完毕，阻塞等待其他线程完成
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("所有线程执行完毕");
    }
}
