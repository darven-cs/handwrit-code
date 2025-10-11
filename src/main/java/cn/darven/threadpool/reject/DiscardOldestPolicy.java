package cn.darven.threadpool.reject;

import cn.darven.threadpool.MyThreadPoolExecutor;

/**
 * @author darven
 * @date 2025/10/12
 * @description 抛弃最旧任务，然后尝试放入新的任务
 */
public class DiscardOldestPolicy implements RejectedExecutionHandler{
    @Override
    public void rejectedExecution(Runnable var1, MyThreadPoolExecutor var2) {
        if(!var2.isShutdown()){
            var2.getQueue().poll();
            var2.execute(var1);
        }
    }
}
