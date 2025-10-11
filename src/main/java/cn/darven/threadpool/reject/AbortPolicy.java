package cn.darven.threadpool.reject;

import cn.darven.threadpool.MyThreadPoolExecutor;


/**
 * @author darven
 * @date 2025/10/12
 * @description 默认拒绝策略，抛出异常
 */
public class AbortPolicy implements RejectedExecutionHandler{
    @Override
    public void rejectedExecution(Runnable var1, MyThreadPoolExecutor var2) {
        throw new RuntimeException("Task "+var1+" rejected from "+var2);
    }
}
