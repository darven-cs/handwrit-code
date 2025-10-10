package cn.darven.spring.sub;

import cn.darven.spring.BeanPostProcessor;
import cn.darven.spring.Component;
import cn.darven.spring.PostConstruct;

/**
 * @author darven
 * @date 2025/10/11
 * @description TODO
 */
@Component
public class MyPostProcesser implements BeanPostProcessor {


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println(beanName+"初始化完成");
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
