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

    /**
     * 默认日志记录器
     */
    public static final XLogger LOGGER = XConfigTools.supply(XTools.cfgDef(XLogTools.CFG_LOGGER, XLogTools.CFG_LOGGER_DEFAULT).trim());

    public static boolean acceptLevel(String lvTest, String lvSetting) {
        if (XLogger.LEVEL_OFF.equalsIgnoreCase(lvTest)) {
            return false;
        } else if (XLogger.LEVEL_ERROR.equalsIgnoreCase(lvTest)) {
            return !XLogger.LEVEL_OFF.equalsIgnoreCase(lvSetting);
        } else if (XLogger.LEVEL_WARNING.equalsIgnoreCase(lvTest)) {
            return !XLogger.LEVEL_OFF.equalsIgnoreCase(lvSetting) && !XLogger.LEVEL_ERROR.equalsIgnoreCase(lvSetting);
        } else if (XLogger.LEVEL_NOTICE.equalsIgnoreCase(lvTest)) {
            return XLogger.LEVEL_DETAIL.equalsIgnoreCase(lvSetting) || XLogger.LEVEL_NOTICE.equalsIgnoreCase(lvSetting);
        } else {
            return XLogger.LEVEL_DETAIL.equalsIgnoreCase(lvSetting);
        }
    }
}
