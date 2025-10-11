package cn.darven.spring.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author darven
 * @date 2025/10/10
 * @description bean的定义对象
 * 自定义这个类是为了创建对象，方便拓展
 */
public class BeanDefinition {

    // 类
    private final Class<?> type;
    // bean的名称
    private String name;
    // 获得自动注入的方法
    private Method postConstruct;
    // 获得自动注入的字段
    private Field[] autowiredFields;

    public BeanDefinition(Class<?> type){
        this.type = type;
        Component component = type.getDeclaredAnnotation(Component.class);
        this.name= component.name().isEmpty() ?type.getName():component.name();

        this.postConstruct = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                .findFirst()
                .orElse(null);

        this.autowiredFields=Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .toArray(Field[]::new);
    }

    // 获得bean的名称
    public String getName(){
        return name;
    }

    // 反射获得构造函数，用于创建对象
    public Constructor<?> getConstructor(){
        try {
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    // 获得生命周期方法
    public Method getPostConstruct(){
        return postConstruct;
    }

    // 获得自动注入的字段
    public Field[] getAutowiredFields(){
        return autowiredFields;
    }

    // 获得bean的类型
    public Class<?> getType(){
        return type;
    }
}
