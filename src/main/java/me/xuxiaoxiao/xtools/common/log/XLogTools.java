package me.xuxiaoxiao.xtools.common.log;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.config.configs.XConfigs;
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
        XConfigTools.X_CONFIGS.cfgDef(XLogTools.CFG_LOGGER, XLogTools.CFG_LOGGER_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(XLogTools.CFG_LEVEL, XLogTools.CFG_LEVEL_DEFAULT);

        LOGGER = XConfigTools.supply(XTools.cfgGet(CFG_LOGGER));
        LOGGER.setLevel(XTools.cfgGet(CFG_LEVEL));
        XConfigTools.X_CONFIGS.cfgIterate(new XConfigs.Iteration() {

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
        XTools.cfgWatch(CFG_LEVEL, new XConfigs.Watcher() {
            @Override
            public void onCfgAdd(XConfigs configs, String key, String val) {
                if (key.equals(CFG_LEVEL)) {
                    LOGGER.setLevel(val);
                } else {
                    LOGGER.setLevel(key.substring(TAG_LEVEL_PREFIX.length()), val);
                }
            }

            @Override
            public void onCfgDel(XConfigs configs, String key, String val) {
                LOGGER.setLevel(key.substring(TAG_LEVEL_PREFIX.length()), null);
            }

            @Override
            public void onCfgChange(XConfigs configs, String key, String valOld, String valNew) {
                if (key.equals(CFG_LEVEL)) {
                    LOGGER.setLevel(valNew);
                } else {
                    LOGGER.setLevel(key.substring(TAG_LEVEL_PREFIX.length()), valNew);
                }
            }
        });
    }
}
