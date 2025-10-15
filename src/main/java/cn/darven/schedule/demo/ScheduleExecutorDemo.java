package cn.darven.schedule.demo;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author darven
 * @date 2025/10/15
 * @description TODO
 */
public class ScheduleExecutorDemo {
    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(6);

        scheduledExecutorService.scheduleAtFixedRate(()->{
            System.out.println("定时任务执行1"+System.currentTimeMillis());
        }, 1, 1,TimeUnit.SECONDS);

        scheduledExecutorService.schedule(()->{
            scheduledExecutorService.shutdown();
        }, 5, TimeUnit.SECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(()->{
            System.out.println("定时任务执行2"+System.currentTimeMillis());
        }, 1, 1,TimeUnit.SECONDS);

        scheduledExecutorService.schedule(()->{
            scheduledExecutorService.shutdown();
        }, 5, TimeUnit.SECONDS);
    }
}
