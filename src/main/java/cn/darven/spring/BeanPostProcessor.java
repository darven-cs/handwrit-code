package cn.darven.spring;

/**
 * @author darven
 * @date 2025/10/11
 * @description 创建对象的扩展点
 */
public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName){
        return bean;
    }


    default Object postProcessAfterInitialization(Object bean, String beanName){
        return bean;
    }

}
