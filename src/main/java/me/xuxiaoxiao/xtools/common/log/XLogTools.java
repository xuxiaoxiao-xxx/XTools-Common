package me.xuxiaoxiao.xtools.common.log;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;

import java.util.logging.*;

public class XLogTools {
    private static final String LEVEL_ERROR = "error";
    private static final String LEVEL_WARNING = "warning";
    private static final String LEVEL_NOTICE = "notice";
    private static final String LEVEL_DETAIL = "detail";

    private static final String LOGGABLE_CONF = "me.xuxiaoxiao$xtools-common$loggable";
    private static final String LOGGABLE_DEFAULT = "true";
    private static final String LEVEL_CONF = "me.xuxiaoxiao$xtools-common$level";
    private static final String LEVEL_DEFAULT = LEVEL_NOTICE;
    private static final String FORMATTER_CONF = "me.xuxiaoxiao$xtools-common$formatter";
    private static final String FORMATTER_DEFAULT = LogFormatter.class.getName();

    private static final Logger LOGGER = Logger.getLogger(XLogTools.class.getPackage().getName());

    private static boolean loggable;

    static {
        LOGGER.setUseParentHandlers(false);

        XLogTools.loggable = Boolean.valueOf(XConfigTools.confGet(LOGGABLE_CONF, LOGGABLE_DEFAULT));
        Level level;
        switch (XTools.confGet(LEVEL_CONF, LEVEL_DEFAULT).toLowerCase()) {
            case LEVEL_DETAIL:
                level = Level.CONFIG;
                break;
            case LEVEL_WARNING:
                level = Level.WARNING;
                break;
            case LEVEL_ERROR:
                level = Level.SEVERE;
                break;
            default:
                level = Level.INFO;
                break;
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(level);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(level);
    }

    public static void logError(String error) {
        LOGGER.log(Level.SEVERE, error);
    }

    public static void logWarning(String warning) {
        LOGGER.log(Level.WARNING, warning);
    }

    public static void logNotice(String notice) {
        LOGGER.log(Level.INFO, notice);
    }

    public static void logDetail(String detail) {
        LOGGER.log(Level.CONFIG, detail);
    }

    public static class LogFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            return null;
        }
    }
}
