package me.xuxiaoxiao.xtools.common.config;

/**
 * 提供者接口
 * 实现此接口的类不能有自定义的构造函数。
 *
 * @param <T> 提供对象
 */
public interface XSupplier<T> {

    /**
     * 提供特定的对象
     *
     * @return 特定对象
     */
    T supply();
}
