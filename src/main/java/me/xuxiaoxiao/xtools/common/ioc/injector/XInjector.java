package me.xuxiaoxiao.xtools.common.ioc.injector;

public interface XInjector {
    <T> T inject(T target) throws Exception;
}
