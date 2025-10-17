package cn.darven.juc.demo;


import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author darven
 * @date 2025/10/16
 * @description 模拟atomic包使用
 */
public class AtomicDemo {
    // 共享变量（需要通过CAS操作修改）
    private volatile int value;
    // 获取Unsafe实例（Java中禁止直接实例化，需通过反射）
    private static final Unsafe unsafe;
    // 共享变量value在类中的内存偏移量（用于CAS操作定位内存地址）
    private static final long valueOffset;

    static {
        try {
            // 通过反射获取Unsafe的私有实例
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);

            // 获取value字段的内存偏移量
            valueOffset = unsafe.objectFieldOffset(AtomicDemo.class.getDeclaredField("value"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public AtomicDemo(int initialValue) {
        this.value = initialValue;
    }

    // 自旋+CAS实现自增操作
    public int incrementAndGet() {
        int current;
        int next;
        do {
            // 1. 获取当前值（可能被其他线程修改，需重新读取）
            current = value;
            // 2. 计算目标值
            next = current + 1;
            // 3. CAS操作：若当前内存中的值等于current，则更新为next
            //    若成功则退出循环，失败则重试（自旋）
        } while (!unsafe.compareAndSwapInt(this, valueOffset, current, next));
        return next;
    }

    public int getValue() {
        return value;
    }

    public static void main(String[] args) {
        AtomicDemo example = new AtomicDemo(0);
        // 多线程测试
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    example.incrementAndGet();
                }
            }).start();
        }
        // 等待所有线程执行完毕
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("最终值：" + example.getValue()); // 预期输出 5000
    }
}

