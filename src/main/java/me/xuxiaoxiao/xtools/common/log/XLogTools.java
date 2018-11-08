package me.xuxiaoxiao.xtools.common.log;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class XLogTools {
    public static final String LEVEL_ERROR = "error";
    public static final String LEVEL_WARNING = "warning";
    public static final String LEVEL_NOTICE = "notice";
    public static final String LEVEL_DETAIL = "detail";

    public static final String CONF_LOGGABLE = "me.xuxiaoxiao$xtools-common$log.loggable";
    public static final String CONF_LOGGABLE_DEFAULT = "true";

    public static final String CONF_CONSOLE_LEVEL = "me.xuxiaoxiao$xtools-common$log.console.level";
    public static final String CONF_CONSOLE_LEVEL_DEFAULT = LEVEL_NOTICE;
    public static final String CONF_CONSOLE_FORMATTER = "me.xuxiaoxiao$xtools-common$log.console.formatter";
    public static final String CONF_CONSOLE_FORMATTER_DEFAULT = LogFormatter.class.getName();

    public static final String CONF_FILE_PATH = "me.xuxiaoxiao$xtools-common$log.file.path";
    public static final String CONF_FILE_PATH_DEFAULT = "";
    public static final String CONF_FILE_LEVEL = "me.xuxiaoxiao$xtools-common$log.file.level";
    public static final String CONF_FILE_LEVEL_DEFAULT = LEVEL_DETAIL;
    public static final String CONF_FILE_FORMATTER = "me.xuxiaoxiao$xtools-common$log.file.formatter";
    public static final String CONF_FILE_FORMATTER_DEFAULT = LogFormatter.class.getName();

    private static final Logger LOGGER = Logger.getLogger(XLogTools.class.getPackage().getName());

    private static boolean loggable;

    static {
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);

        XLogTools.loggable = Boolean.valueOf(XConfigTools.confDef(CONF_LOGGABLE, CONF_LOGGABLE_DEFAULT));

        Level consoleLevel = strToLevel(XTools.confDef(CONF_CONSOLE_LEVEL, CONF_CONSOLE_LEVEL_DEFAULT).toLowerCase());
        Formatter consoleFormatter;
        try {
            consoleFormatter = (Formatter) Class.forName(XTools.confDef(CONF_CONSOLE_FORMATTER, CONF_CONSOLE_FORMATTER_DEFAULT)).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            consoleFormatter = new LogFormatter();
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(consoleFormatter);
        handler.setLevel(consoleLevel);
        LOGGER.addHandler(handler);

        String filePath = XTools.confDef(CONF_FILE_PATH, CONF_FILE_PATH_DEFAULT);
        if (!XTools.strEmpty(filePath)) {
            try {
                Formatter fileFormatter;
                try {
                    fileFormatter = (Formatter) Class.forName(XTools.confDef(CONF_FILE_FORMATTER, CONF_FILE_FORMATTER_DEFAULT)).newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    fileFormatter = new LogFormatter();
                }
                Level fileLevel = strToLevel(XTools.confDef(CONF_FILE_LEVEL, CONF_FILE_LEVEL_DEFAULT));

                FileHandler fileHandler = new FileHandler(filePath);
                fileHandler.setFormatter(fileFormatter);
                fileHandler.setLevel(fileLevel);
                LOGGER.addHandler(fileHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打印错误日志
     *
     * @param error 错误日志信息
     * @param args  信息的参数
     */
    public static void logError(String error, Object... args) {
        if (loggable) {
            LOGGER.log(newRecord(Level.SEVERE, String.format(error, args)));
        }
    }

    /**
     * 打印警告日志
     *
     * @param warning 警告日志信息
     * @param args    信息的参数
     */
    public static void logWarning(String warning, Object... args) {
        if (loggable) {
            LOGGER.log(newRecord(Level.WARNING, String.format(warning, args)));
        }
    }

    /**
     * 打印提示日志
     *
     * @param notice 提示日志信息
     * @param args   信息的参数
     */
    public static void logNotice(String notice, Object... args) {
        if (loggable) {
            LOGGER.log(newRecord(Level.INFO, String.format(notice, args)));
        }
    }

    /**
     * 打印详细日志
     *
     * @param detail 详细日志信息
     * @param args   信息的参数
     */
    public static void logDetail(String detail, Object... args) {
        if (loggable) {
            LOGGER.log(newRecord(Level.CONFIG, String.format(detail, args)));
        }
    }

    private static String levelToStr(Level level) {
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

    private static Level strToLevel(String str) {
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

    private static LogRecord newRecord(Level level, String msg) {
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

    /**
     * 日志格式化器
     */
    public static class LogFormatter extends Formatter {
        private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

        @Override
        public String format(LogRecord record) {
            String time = XTools.dateFormat(TIME_FORMAT, new Date(record.getMillis()));
            char level = Character.toUpperCase(levelToStr(record.getLevel()).charAt(0));
            int line = (record.getParameters() != null && record.getParameters().length > 0) ? (int) record.getParameters()[0] : 0;
            return String.format("%s | %s | %s.%s L%d: %s\n", time, level, record.getSourceClassName(), record.getSourceMethodName(), line, record.getMessage());
        }
    }
}
