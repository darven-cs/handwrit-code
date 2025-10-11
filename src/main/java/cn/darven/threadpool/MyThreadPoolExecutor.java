package cn.darven.threadpool;

import cn.darven.threadpool.reject.AbortPolicy;
import cn.darven.threadpool.reject.RejectedExecutionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author darven
 * @date 2025/10/11
 * @description 自定义线程池
 * 线程池好处就是不用我们手动创建线程线程，线程池会自动创建线程，并且线程会复用，线程池会自动回收线程
 */
public class MyThreadPoolExecutor {
    private final BlockingDeque<Runnable> workQueue;
    private final int corePoolSize;
    private final int maximumPoolSize;
    private final long keepAliveTime;
    private final TimeUnit unit;
    private volatile RejectedExecutionHandler handler;
    private static final RejectedExecutionHandler defaultHandler=new AbortPolicy();
    private boolean isShutdown = false;

    public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize,int keepAliveTime,TimeUnit unit, BlockingDeque<Runnable> workQueue,RejectedExecutionHandler handler) {
        this.workQueue = workQueue;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.handler = handler;
    }

    // 核心线程容器
    private List<CoreThread> coreThreadList = new ArrayList<>();
    // 非核心线程容器
    private List<NonCoreThread> nonCoreThreadList = new ArrayList<>();

    // 将任务放入线程池
    public synchronized void execute(Runnable runnable) {
        // 如果关闭状态，则拒绝
        if(isShutdown){
            handler.rejectedExecution(runnable, this);
            return;
        }

        // 一直创建核心线程，直到核心线程数达到最大值
        if (coreThreadList.size() < corePoolSize) {
            CoreThread coreThread = new CoreThread(runnable);
            coreThreadList.add(coreThread);
            coreThread.start();
            return;
        }
        // 超出核心线程数，将任务放入阻塞队列
        if(workQueue.offer(runnable)){
            return ;
        }

        // 如果超出阻塞队列，创建非核心线程
        if(coreThreadList.size()+nonCoreThreadList.size()<maximumPoolSize){
            NonCoreThread nonCoreThread = new NonCoreThread(runnable);
            nonCoreThreadList.add(nonCoreThread);
            nonCoreThread.start();
            return;
        }

        handler.rejectedExecution(runnable,this);
    }

    // 关闭线程池
    public void shutdown() {
        if (isShutdown) return;
        isShutdown=true;
        // 优雅关闭不中断正在执行的线程，而是让它们执行完任务后自然退出
    }

    // 立马关闭线程池
    public List<Runnable> shutdownNow() {
        if(isShutdown) return new ArrayList<>();
        isShutdown=true;
        // 1. 中断所有正在执行的线程（核心+非核心）
        for (CoreThread coreThread : coreThreadList) {
            coreThread.interrupt();
        }

        for (NonCoreThread nonCoreThread : nonCoreThreadList) {
            nonCoreThread.interrupt();
        }
        // 2. 清空队列，返回未执行的任务
        List<Runnable> unexecutedTasks=new ArrayList<>();
        workQueue.drainTo(unexecutedTasks);
        return unexecutedTasks;
    }

    // 是否关闭
    public boolean isShutdown() {
        return isShutdown;
    }

    // 返回阻塞队列
    public BlockingDeque<Runnable> getQueue() {
        return workQueue;
    }

    /**
     * 核心线程类
     * */
    class CoreThread extends Thread {

        private Runnable firstCommand;

        public CoreThread(Runnable command) {
            this.firstCommand = command;
        }

        @Override
        public void run() {
            if (firstCommand != null) {
                firstCommand.run();
                firstCommand = null;
            }

            // 循环条件：线程池未关闭 OR 队列不为空（需处理剩余任务）
            // 当线程池已关闭且队列为空时，退出循环，线程自然结束
            while (!isShutdown || !workQueue.isEmpty()) {
                Runnable runnable = null;
                try {
                    // 若线程池已关闭且队列空，take() 会一直阻塞吗？不会，因为循环条件会退出
                    runnable = workQueue.take(); // 阻塞获取任务（队列空时等待）
                } catch (InterruptedException e) {
                    // 若优雅关闭时被中断（理论上不会，因为shutdown没调用interrupt），但需处理
                    break; // 响应中断，退出循环
                }
                if (runnable != null) {
                    runnable.run(); // 执行任务
                }
            }
            System.out.println(Thread.currentThread().getName() + " 核心线程优雅退出");
        }
    }

    /**
     * 非核心线程类，当然真的线程池不是区分核心线程和非核心线程的，而是通过Worker实现
     * 我们简化这个
     * 非核心线程的话在执行之后，到时间之后会自动销毁
     *
     * */
    class NonCoreThread extends Thread {
        private Runnable firstCommand;

        public NonCoreThread(Runnable command) {
            this.firstCommand = command;
        }
        @Override
        public void run() {
            if (firstCommand != null) {
                firstCommand.run();
                firstCommand = null;
            }

            while (true) {
                Runnable command = null;
                try {
                    // 非核心线程：超时获取任务（空闲超时后退出）
                    command = workQueue.poll(keepAliveTime, unit);
                    // 若线程池已关闭且队列空，即使没超时也退出
                    if (isShutdown && workQueue.isEmpty()) {
                        break;
                    }
                    if (command == null) {
                        break; // 超时无任务，退出
                    }
                    command.run();
                } catch (InterruptedException e) {
                    break; // 响应中断，退出
                }
            }
            System.out.println(Thread.currentThread().getName() + " 非核心线程优雅退出");
            // 注意：ArrayList 非线程安全，移除操作需加锁（可暂时忽略，先保证关闭逻辑）
            nonCoreThreadList.remove(this);
        }
    }
}


