package me.xuxiaoxiao.xtools.common.log;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.log.logger.XLogger;
import me.xuxiaoxiao.xtools.common.log.logger.impl.XLoggerImpl;

/**
 * 日志工具类
 */
public class XLogTools {
    public static final String CFG_LOGGER = XTools.CFG_PREFIX + "log.logger";
    public static final String CFG_LOGGER_DEFAULT = XLoggerImpl.class.getName();

    public static final String CFG_LEVEL = XTools.CFG_PREFIX + "log.level";
    public static final String CFG_LEVEL_DEFAULT = "detail";

    private static final String TAG_LEVEL_PREFIX = CFG_LEVEL + ".";
    /**
     * 默认日志记录器
     */
    public static final XLogger LOGGER;

    static {
        XTools.cfgDef(XLogTools.CFG_LOGGER, XLogTools.CFG_LOGGER_DEFAULT);
        XTools.cfgDef(XLogTools.CFG_LEVEL, XLogTools.CFG_LEVEL_DEFAULT);

        LOGGER = XConfigTools.supply(XTools.cfgGet(CFG_LOGGER));
        LOGGER.setLevel(XTools.cfgGet(CFG_LEVEL));
        XConfigTools.cfgIterate(new XConfigTools.Iteration() {

            @Override
            public boolean iterate(String key, String value) {
                if (key.startsWith(TAG_LEVEL_PREFIX)) {
                    String tag = key.substring(TAG_LEVEL_PREFIX.length());
                    if (!XTools.strEmpty(tag)) {
                        LOGGER.setLevel(tag, value);
                    }
                }
                return false;
            }
        });
    }
}
