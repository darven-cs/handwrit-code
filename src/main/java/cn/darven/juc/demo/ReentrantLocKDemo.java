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
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        long start = System.currentTimeMillis();

//        for (int i = 0; i < threads; i++) {
//            pool.execute(() -> {
//                for (int j = 0; j < 10000; j++) {
//                    if (j % 10 == 0) {
//                        counter.add();       // 少量写操作
//                    } else {
//                        counter.get();       // 大量读操作
//                    }
//                }
//                latch.countDown();
//            });
//        }
        for (int i = 0; i < 10; i++) {
            pool.execute(() -> {
                for (int j = 0; j < 10000; j++) {
                    counter.get(); // 只读
                }
                latch.countDown();
            });
        }
        pool.execute(() -> {
            for (int j = 0; j < 1000; j++) {
                counter.add(); // 写操作很少
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
