package cn.darven.spring.ioc1;

/**
 * @author darven
 * @date 2025/10/11
 * @description 函数接口，因为定义的方法不是立即调用，所以适用延迟生成特性
 * 使用方式（配合 Lambda 表达式 / 方法引用）允许我们将 “对象创建逻辑” 封装起来，推迟到getObject()被调用时执行。
 */
@FunctionalInterface
public interface ObjectFactory<T> {
    T getObject();
}
