package cn.darven.threadpool.reject;

import cn.darven.threadpool.MyThreadPoolExecutor;

/**
 * @author darven
 * @date 2025/10/12
 * @description 直接抛弃拒绝的任务
 */
public class DiscardPolicy implements RejectedExecutionHandler{
    @Override
    public void rejectedExecution(Runnable var1, MyThreadPoolExecutor var2) {
        // 不做操作，不放到队列就行
    }
}
