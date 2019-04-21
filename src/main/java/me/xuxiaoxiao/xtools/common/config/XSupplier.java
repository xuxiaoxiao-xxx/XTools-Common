package me.xuxiaoxiao.xtools.common.config;


/**
 * 根据1.3版本的设计思想。
 * <ul>
 * <li>大部分工具都必须先定义使用接口</li>
 * <li>定义了使用接口的工具，必须有默认实现</li>
 * <li>每种工具都可以通过配置文件配置相关参数</li>
 * <li>每种工具都要提供开箱即用的公共静态方法</li>
 * </ul>
 * 此接口的作用是：用于提供实例化步骤比较复杂的类的实例，大部分的情况都是用于实例化没有无参构造函数的类。
 *
 * @param <T> 类的实例
 */
public interface XSupplier<T> {

    /**
     * 提供配置类的对象
     *
     * @return 配置类的对象
     */
    T supply();
}
