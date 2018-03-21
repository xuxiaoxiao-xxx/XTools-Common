package me.xuxiaoxiao.xtools.common.ioc.factory;

/**
 * 工厂接口，定义了怎样创建一个类的实例，和怎样回收一个类的实例
 */

public interface XFactory {
    /**
     * 根据给定的类，创建这个类的实例
     *
     * @param clazz   需要创建实例的类
     * @param context 创建实例的上下文
     * @param <T>     类模板
     * @return 创建的实例
     * @throws Exception 创建实例时可能发生异常
     */
    <T> T supply(Class<T> clazz, XContext context) throws Exception;

    /**
     * 回收给定的类的实例
     *
     * @param object  需要回收的类的实例
     * @param context 回收实例的上下文
     * @param <T>     类模板
     * @return 回收后的实例
     * @throws Exception 回收实例时可能发生异常
     */
    <T> T recycle(T object, XContext context) throws Exception;
}

