package cn.darven.juc.demo;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantLocKDemo {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("\n=== 测试 ReentrantLock ===");
        testLockPerformance(new ReentrantLocKDemo2());

        System.out.println("=== 测试 ReentrantReadWriteLock ===");
        testLockPerformance(new ReentrantLocKDemo1());


    }

    private static void testLockPerformance(Counter counter) throws InterruptedException {
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        long start = System.currentTimeMillis();

        // 启动 19 个读线程
        for (int i = 0; i < 19; i++) {
            pool.execute(() -> {
                for (int j = 0; j < 1_000_000; j++) {
                    int v = counter.get();
                    // 模拟读取代价更高（I/O、复杂计算）
                    for (int k = 0; k < 100; k++) {
                        Math.sqrt(v * k);
                    }
                }
                latch.countDown();
            });
        }

        // 启动 1 个写线程
        pool.execute(() -> {
            for (int j = 0; j < 100; j++) {
                counter.add();
                try {
                    Thread.sleep(10); // 模拟写操作不频繁
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            latch.countDown();
        });

        latch.await();
        pool.shutdown();

        System.out.println("最终值: " + counter.get());
        System.out.println("耗时: " + (System.currentTimeMillis() - start) + "ms");
    }
}

interface Counter {
    void add();

    void decrement();

    int get();
}

class ReentrantLocKDemo1 implements Counter {
    private int count = 0;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public void add() {
        writeLock.lock();
        try {
            count++;
        } finally {
            writeLock.unlock();
        }
    }

    public void decrement() {
        writeLock.lock();
        try {
            count--;
        } finally {
            writeLock.unlock();
        }
    }

    public int get() {
        readLock.lock();
        try {
            return count;
        } finally {
            readLock.unlock();
        }
    }
}

class ReentrantLocKDemo2 implements Counter {
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public void add() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    public void decrement() {
        lock.lock();
        try {
            count--;
        } finally {
            lock.unlock();
        }
    }

    public int get() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}
