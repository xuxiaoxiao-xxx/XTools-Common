package me.xuxiaoxiao.xtools.common.ioc.injector;

/**
 * 注入器，为类的实例注入数据
 */
public interface XInjector {
    /**
     * 为类的实例注入数据
     *
     * @param target 需要注入数据的类的实例
     * @param <T>    类模板
     * @return 注入数据后的实例
     * @throws Exception 注入数据时可能发生异常
     */
    <T> T inject(T target) throws Exception;
}
