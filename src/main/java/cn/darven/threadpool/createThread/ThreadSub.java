package cn.darven.threadpool.createThread;

/**
 * @author darven
 * @date 2025/10/11
 * @description TODO
 */
public class ThreadSub extends  Thread{

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(Thread.currentThread().getName()+" 子线程开始执行");
    }

}
