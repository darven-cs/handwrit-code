package cn.darven.threadpool.createThread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author darven
 * @date 2025/10/11
 * @description 线程创建方式
 */
public class ThreadCreateMain {
    public static void main(String[] args) {
        // runnable
        Runnable runnable=()->{
            for (int i=0;i<5;i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName()+"runnable start");
            }
        };
        Thread thread = new Thread(runnable);

        thread.start();

        System.out.println("主线程未阻塞");

        // 继承Thread
        ThreadSub threadSub = new ThreadSub();
        threadSub.start();

        // callable创建，future管理
        CallableSub callableSub = new CallableSub();
        FutureTask<CallableSub> future=new FutureTask<>(callableSub);
        new Thread(future).start();
        try {
            System.out.println(future.get());;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
