package cn.darven.juc.demo;

/**
 * @author darven
 * @date 2025/10/16
 * @description 线程状态demo
 * 线程状态：NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
 */
public class ThreadStateDemo {

    public static void main(String[] args) {
        Object lock=new Object();
        Thread t=new Thread(()->{
            synchronized (lock){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//        synchronized (lock) {
//            lock.notify();  // 唤醒 WAITING 线程
//        }
    }




//    public static void main(String[] args) {
//        ThreadStateDemo demo = new ThreadStateDemo();
//        new Thread(demo::run1,"T1").start();
//        new Thread(demo::run1,"T2").start();  // 处于阻塞状态
//    }
//
//    synchronized void run1(){
//        while (true){
//            System.out.println(Thread.currentThread().getName()+"拿到线程");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
