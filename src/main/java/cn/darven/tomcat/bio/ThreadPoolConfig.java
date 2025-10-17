package cn.darven.tomcat.bio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author darven
 * @date 2025/10/17
 * @description 线程池配置类
 */
public class ThreadPoolConfig {

    private static final int corePoolSize=10;
    private static final int maximumPoolSize=20;
    private static final long keepAliveTime=2;
    private static final int queueCapacity=100;

    private static int threadNum=0;


    public static ThreadPoolExecutor createThreadPool(){
        System.out.println("开始初始化线程池");
        ThreadFactory factory=(r)->{
            Thread thread = new Thread(r, "tomcat-process-thread-"+threadNum++);
            thread.setUncaughtExceptionHandler((t,e)->{
                System.out.println("线程异常："+t.getName());
                e.printStackTrace();
            });
            return thread;
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),  // 阻塞队列
                factory,
                new ThreadPoolExecutor.DiscardPolicy()  // 执行新任务
        );
        System.out.println("初始化线程池完成");
        return threadPoolExecutor;
    }
}
