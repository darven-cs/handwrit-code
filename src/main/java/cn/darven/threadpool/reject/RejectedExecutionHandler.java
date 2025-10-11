package cn.darven.threadpool.reject;


import cn.darven.threadpool.MyThreadPoolExecutor;

/**
 * @author darven
 * @date 2025/10/12
 * @description 拒绝策略
 */
public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable var1, MyThreadPoolExecutor var2);
}
