package me.xuxiaoxiao.xtools.common.http.executor;

import me.xuxiaoxiao.xtools.common.config.XSupplier;

/**
 * HttpExecutor提供者
 */
public class XHttpExecutorSupplier implements XSupplier<XHttpExecutor> {

    @Override
    public XHttpExecutor supply() {
        return new XHttpExecutorImpl(new XHttpExecutor.Option());
    }
}
