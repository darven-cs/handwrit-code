package cn.darven.spring.ioc1.sub;

import cn.darven.spring.ioc1.BeanPostProcessor;
import cn.darven.spring.ioc1.Component;

/**
 * @author darven
 * @date 2025/10/11
 * @description BeanPostProcessor是全局的初始化
 * 为什么要全局化，其他模块一次拓展
 */
@Component
public class MyPostProcesser implements BeanPostProcessor {


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println(beanName+"初始化完成");
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
