package me.xuxiaoxiao.xtools.common.log;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.log.logger.XLogger;
import me.xuxiaoxiao.xtools.common.log.logger.XLoggerImpl;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class XLogTools {
    public static final String LEVEL_ERROR = "error";
    public static final String LEVEL_WARNING = "warning";
    public static final String LEVEL_NOTICE = "notice";
    public static final String LEVEL_DETAIL = "detail";

    public static final String CONF_LOGGER = "me.xuxiaoxiao$xtools-common$log.logger";
    public static final String CONF_LOGGER_DEFAULT = XLoggerImpl.class.getName();

    public static final String CONF_LOGGABLE = "me.xuxiaoxiao$xtools-common$log.loggable";
    public static final String CONF_LOGGABLE_DEFAULT = "true";

    public static final String CONF_HANDLERS_SUPPLIER = "me.xuxiaoxiao$xtools-common$log.handlersSupplier";
    public static final String CONF_HANDLERS_SUPPLIER_DEFAULT = XLogger.Option.XHandlersSupplier.class.getName();

    public static final String CONF_CONSOLE_LEVEL = "me.xuxiaoxiao$xtools-common$log.console.level";
    public static final String CONF_CONSOLE_LEVEL_DEFAULT = LEVEL_DETAIL;
    public static final String CONF_CONSOLE_FORMATTER = "me.xuxiaoxiao$xtools-common$log.console.formatter";
    public static final String CONF_CONSOLE_FORMATTER_DEFAULT = XLogger.Option.XLogFormatter.class.getName();

    public static final String CONF_FILE_PATTERN = "me.xuxiaoxiao$xtools-common$log.file.pattern";
    public static final String CONF_FILE_PATTERN_DEFAULT = "xlogger%u.log";
    public static final String CONF_FILE_LEVEL = "me.xuxiaoxiao$xtools-common$log.file.level";
    public static final String CONF_FILE_LEVEL_DEFAULT = LEVEL_NOTICE;
    public static final String CONF_FILE_FORMATTER = "me.xuxiaoxiao$xtools-common$log.file.formatter";
    public static final String CONF_FILE_FORMATTER_DEFAULT = XLogger.Option.XLogFormatter.class.getName();

    /**
     * 默认日志
     */
    public static final XLogger LOGGER;

    static {
        String loggerStr = XTools.cfgDef(XLogTools.CONF_LOGGER, XLogTools.CONF_LOGGER_DEFAULT);
        XLogger logger;
        try {
            if (!XTools.strEmpty(loggerStr)) {
                logger = (XLogger) Class.forName(loggerStr.trim()).newInstance();
            } else {
                logger = new XLoggerImpl();
            }
        } catch (Exception e) {
            e.printStackTrace();
            XTools.logW("XLogger:%s 初始化失败, 将使用默认的XLogger", loggerStr);
            logger = new XLoggerImpl();
        }
        LOGGER = logger;
    }

    public static String levelToStr(Level level) {
        if (Level.SEVERE.equals(level)) {
            return LEVEL_ERROR;
        } else if (Level.WARNING.equals(level)) {
            return LEVEL_WARNING;
        } else if (Level.INFO.equals(level)) {
            return LEVEL_NOTICE;
        } else {
            return LEVEL_DETAIL;
        }
    }

    public static Level strToLevel(String str) {
        if (LEVEL_ERROR.equals(str)) {
            return Level.SEVERE;
        } else if (LEVEL_WARNING.equals(str)) {
            return Level.WARNING;
        } else if (LEVEL_NOTICE.equals(str)) {
            return Level.INFO;
        } else {
            return Level.CONFIG;
        }
    }

    public static LogRecord newRecord(Level level, String msg) {
        LogRecord logRecord = new LogRecord(level, msg);
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 3) {
            if (stack[3].getClassName().equals(XTools.class.getName()) && stack[3].getMethodName().startsWith("log")) {
                if (stack.length > 4) {
                    logRecord.setSourceClassName(stack[4].getClassName());
                    logRecord.setSourceMethodName(stack[4].getMethodName());
                    logRecord.setParameters(new Object[]{stack[4].getLineNumber()});
                    return logRecord;
                }
            } else {
                logRecord.setSourceClassName(stack[3].getClassName());
                logRecord.setSourceMethodName(stack[3].getMethodName());
                logRecord.setParameters(new Object[]{stack[3].getLineNumber()});
                return logRecord;
            }
        }
        logRecord.setSourceClassName("unknown");
        logRecord.setSourceMethodName("unknown");
        logRecord.setParameters(new Object[]{0});
        return logRecord;
    }
}
