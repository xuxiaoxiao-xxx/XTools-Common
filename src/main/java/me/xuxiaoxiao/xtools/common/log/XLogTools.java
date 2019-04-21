package me.xuxiaoxiao.xtools.common.log;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.log.logger.XLogger;
import me.xuxiaoxiao.xtools.common.log.logger.impl.XLoggerImpl;

/**
 * 日志工具类
 */
public class XLogTools {
    public static final String CFG_PREFIX = "me.xuxiaoxiao$xtools-common$log";

    public static final String CFG_LOGGER = CFG_PREFIX + ".logger";
    public static final String CFG_LOGGER_DEFAULT = XLoggerImpl.class.getName();

    public static final String CFG_LEVEL = CFG_PREFIX + ".level";
    public static final String CFG_LEVEL_DEFAULT = "detail";

    public static final String CFG_PATTERN = CFG_PREFIX + ".file";
    public static final String CFG_PATTERN_DEFAULT = "xlogger.log";

    public static final String CFG_FORMATTER = CFG_PREFIX + ".formatter";
    public static final String CFG_FORMATTER_DEFAULT = XLoggerImpl.XLogFormatter.class.getName();

    private static final String TAG_LEVEL_PREFIX = CFG_LEVEL + ".";
    /**
     * 默认日志记录器
     */
    public static final XLogger LOGGER;

    static {
        XTools.cfgDef(XLogTools.CFG_LOGGER, XLogTools.CFG_LOGGER_DEFAULT);
        XTools.cfgDef(XLogTools.CFG_FORMATTER, XLogTools.CFG_FORMATTER_DEFAULT);
        XTools.cfgDef(XLogTools.CFG_PATTERN, XLogTools.CFG_PATTERN_DEFAULT);
        XTools.cfgDef(XLogTools.CFG_LEVEL, XLogTools.CFG_LEVEL_DEFAULT);

        XLogger logger;
        try {
            logger = (XLogger) Class.forName(XTools.cfgGet(XLogTools.CFG_LOGGER).trim()).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            logger = new XLoggerImpl();
        }
        LOGGER = logger;
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
