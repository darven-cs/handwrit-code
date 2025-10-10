//package cn.darven.spring.sub;
//
//import cn.darven.spring.BeanPostProcessor;
//import cn.darven.spring.Component;
//import cn.darven.spring.PostConstruct;
//
///**
// * @author darven
// * @date 2025/10/11
// * @description TODO
// */
//@Component
//public class MyPostProcesser1 implements BeanPostProcessor {
//
//    @Override
//    public Object postProcessBeforeInitialization(Object bean, String beanName) {
//        System.out.println(beanName+"初始化完成");
//        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
//    }
//
//    @PostConstruct
//    public void init(){
//        System.out.println("1初始化中");
//    }
//
//    @Override
//    public Object postProcessAfterInitialization(Object bean, String beanName) {
//        System.out.println(beanName+"初始化完成");
//        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
//    }
//}
