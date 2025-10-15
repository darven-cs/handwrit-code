package cn.darven.schedule.demo;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author darven
 * @date 2025/10/15
 * @description 早期使用的TImer和TimerTask
 * 存在问题：单线程复用，容易阻塞
 * 依赖时钟
 */
public class TimerDemo {
    
    public static void main(String[] args) {
        Timer timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("执行定时任务");
            }
        };

        timer.schedule(timerTask, 1000, 1000);  // 1秒后执行，固定1秒执行

        // 5秒后关闭定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                System.out.println("定时器关闭");
            }
        }, 5000);
    }
}
