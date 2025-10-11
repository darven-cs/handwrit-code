package cn.darven.threadpool;

import cn.darven.threadpool.reject.AbortPolicy;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author darven
 * @date 2025/10/11
 * @description TODO
 */
public class ThreadPoolTest {
    public static void main(String[] args) {

        MyThreadPoolExecutor threadExecutor = new MyThreadPoolExecutor(3,
                6,
                5,
                TimeUnit.SECONDS
                ,new LinkedBlockingDeque<>(3)
                ,new AbortPolicy());
        for(int i=0;i<100;i++){
            Runnable runnable=()->{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName()+"start");
            };
            threadExecutor.execute(runnable);
        }
    }
}
