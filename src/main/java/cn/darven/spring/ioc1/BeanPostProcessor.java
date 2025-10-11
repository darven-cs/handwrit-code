package cn.darven.spring.ioc1;

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
