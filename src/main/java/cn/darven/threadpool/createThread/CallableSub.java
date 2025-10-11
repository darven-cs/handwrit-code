package cn.darven.threadpool.createThread;

import java.util.concurrent.Callable;

/**
 * @author darven
 * @date 2025/10/11
 * @description TODO
 */
public class CallableSub implements Callable {
    @Override
    public Object call() throws Exception {
        for (int i=0;i<5;i++){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName()+" callable start");
        }
        return "callable sub";
    }
}
