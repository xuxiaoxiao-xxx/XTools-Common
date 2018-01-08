package me.xuxiaoxiao.xtools.common.ioc.supplier;

public interface XSupplier {
    <T> T supply(Class<T> clazz, XContext context) throws Exception;
}

