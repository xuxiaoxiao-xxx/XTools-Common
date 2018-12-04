package me.xuxiaoxiao.xtools.common.log.logger;

import me.xuxiaoxiao.xtools.common.config.XSupplier;

/**
 * XLogger提供者
 */
public class XLoggerSupplier implements XSupplier<XLogger> {

    @Override
    public XLogger supply() {
        return new XLoggerImpl(new XLogger.Option());
    }
}
