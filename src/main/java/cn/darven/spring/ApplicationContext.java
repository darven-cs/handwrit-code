package cn.darven.spring;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author darven
 * @date 2025/10/10
 * @description Spring中是接口，我们这里简化为类
 */
public class ApplicationContext {

    private Map<String, Object> ioc;

    private Map<String, Object> loadIoc; // 缓存加载的bean

    private Map<String, BeanDefinition> beanDefinitionMap;

    // 添加全局 BeanPostProcessor 列表
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public ApplicationContext(String packageName) throws IOException, URISyntaxException {
        this.ioc = new HashMap<>();
        this.loadIoc = new HashMap<>();
        this.beanDefinitionMap = new HashMap<>();
        initContext(packageName);
    }

    // 创建对象
    public void initContext(String packageName) throws IOException, URISyntaxException {
        // 造什么对象，spring中是注解和xml配置，这里简化为注解Component注解
        // 怎么造对象，参数，自动注入，生命周期

        // 先注册beanDefinition
        scanPackage(packageName)
                .stream()
                .filter(this::hasAnnotation)  // 过滤
                .map(this::createBeanDefinition) // 映射  .forEach(this::registerBean);   // 添加
                .toList();

        beanDefinitionMap.values().stream()
                .filter(bd->BeanPostProcessor.class.isAssignableFrom(bd.getType()))
                .map(this::registerBean)
                .map(BeanPostProcessor.class::cast)
                .forEach(beanPostProcessors::add);

        // 然后注册 bean，可以避免出现注入依赖的时候不存在问题
        beanDefinitionMap.values().stream()
                .forEach(this::registerBean);

    }

    // 创建完成对象之后，将他注册
    protected Object registerBean(BeanDefinition beanDefinition) {
        String name = beanDefinition.getName();
        if(ioc.containsKey( name)){
            return ioc.get(name);
        }
        if(loadIoc.containsKey(name)){
            return loadIoc.get(name);
        }
        return doCreateBean(beanDefinition);
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean=null;
        try {
            // 创建对象
            bean = constructor.newInstance();

            // 缓存记载bean
            loadIoc.put(beanDefinition.getName(), bean);

            // 实现依赖注入，通过beanDefinition的参数注入
            autowired(beanDefinition, bean);

            // 反射调用postStruct函数
            bean = initBean(beanDefinition, bean);

            loadIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(), bean);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return bean;
    }


    /**
     * 依赖注入
     * */
    private void autowired(BeanDefinition beanDefinition, Object bean) {
        for (Field autowiredField : beanDefinition.getAutowiredFields()) {
            autowiredField.setAccessible(true); // 允许访问,关闭私有属性检查
            try {
                // 注入
                autowiredField.set(bean, getBean(autowiredField.getType()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 初始化bean
     * */
    private Object initBean(BeanDefinition beanDefinition, Object bean) throws InvocationTargetException, IllegalAccessException {
        // 遍历所有 BeanPostProcessor
        for (BeanPostProcessor processor : beanPostProcessors) {
            bean = processor.postProcessBeforeInitialization(bean, beanDefinition.getName());
        }

        Method postConstruct = beanDefinition.getPostConstruct();
        if(postConstruct!=null){
            postConstruct.invoke(bean);
        }

        for (BeanPostProcessor processor : beanPostProcessors) {
            bean = processor.postProcessAfterInitialization(bean, beanDefinition.getName());
        }

        return bean;
    }

    // 如果是Component注解的类，创建BeanDefinition
    protected BeanDefinition createBeanDefinition(Class<?> clazz) {
        // 解决bean名字重复问题
        BeanDefinition beanDefinition = new BeanDefinition(clazz);
        if(beanDefinitionMap.containsKey(beanDefinition.getName())){
            throw new RuntimeException("bean name repeat");
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    // 是否包含注解
    private boolean hasAnnotation(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    // 扫描包下面的类，然后需要判断是否携带注解
    private List<Class<?>> scanPackage(String packageName) throws IOException, URISyntaxException {
        List<Class<?> > classes = new ArrayList<>();

        // 传进来的是 com.darven.spring 我们需要转化成 /com/darven/spring
        String replace = packageName.replace('.', File.separatorChar);
        URL resource = this.getClass().getClassLoader().getResource(replace);
        Path path = Paths.get(resource.toURI());
//        System.out.println(path);

        // 递归遍历包下面的所有文件
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                System.out.println( file.toAbsolutePath());
//                System.out.println(file.toString().endsWith(".class"));
                // 获取文件名
                if(file.toString().endsWith(".class")){
                    // 获得cn/darven/spring/ApplicationContext.class
                    String classPath = file.toAbsolutePath().toString();
                    int index = classPath.indexOf(replace);
                    // 获取包下面的类的路径，并转化称为cn.darven.spring.ApplicationContext
                    String className = classPath.substring(index, classPath.length() - ".class".length()).replace(File.separatorChar,'.');
                    try {
                        // 加载类
                        Class<?> aClass = ApplicationContext.class.getClassLoader().loadClass(className);
//                        System.out.println(aClass);
                        // 存储
                        classes.add(aClass);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classes;
    }

    // 通过名字获得对象
    public Object getBean(String beanName) {
        if (beanName.isBlank()){
            throw new RuntimeException("bean name is blank");
        }
//        return ioc.get(beanName);
        if(ioc.containsKey(beanName)){
            return ioc.get(beanName);
        }

        // 如果不存在，创建对象
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            return registerBean(beanDefinition);
        }
        return null;
    }

    // 通过类型获得对象
    public <T> T getBean(Class<T> beanType) {
        // 循环获取 问题：如果自动注入的顺序问题，我们先记载beanDefinition,然后再创建对象
//        return this.ioc.values()
//                .stream()
//                .filter(bean->beanType.isAssignableFrom(bean.getClass()))
//                .map(bean->(T)bean)
//                .findAny()
//                .orElse(null);
        String beanName = this.beanDefinitionMap.values()
                .stream()
                .filter(beanDefinition -> beanType.isAssignableFrom(beanDefinition.getType()))
                .map(BeanDefinition::getName)
                .findFirst()
                .orElse(null);

        T bean = (T) getBean(beanName);
        return bean;
    }

    // 通过类型获得对象列表
    public <T> List<T> getBeanS(Class<T> beanType) {
        return this.beanDefinitionMap.values()
                .stream()
                .filter(beanDefinition -> beanType.isAssignableFrom(beanDefinition.getType()))
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map(bean->(T)bean)
                .toList();
    }
}
