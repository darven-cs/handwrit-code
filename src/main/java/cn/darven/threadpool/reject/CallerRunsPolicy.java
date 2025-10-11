package cn.darven.threadpool.reject;


import cn.darven.threadpool.MyThreadPoolExecutor;

/**
 * @author darven
 * @date 2025/10/12
 * @description 如果线程池已满，则直接在调用者线程中运行该任务
 */
public class CallerRunsPolicy implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable var1, MyThreadPoolExecutor var2) {
        if (!var2.isShutdown()) {
            var1.run();
        }
    }
}
