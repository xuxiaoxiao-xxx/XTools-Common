package me.xuxiaoxiao.xtools.common.log;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class XLogTools {
    private static final String LEVEL_ERROR = "error";
    private static final String LEVEL_WARNING = "warning";
    private static final String LEVEL_NOTICE = "notice";
    private static final String LEVEL_DETAIL = "detail";

    private static final String LOGGABLE_CONF = "me.xuxiaoxiao$xtools-common$log.loggable";
    private static final String LOGGABLE_DEFAULT = "true";
    private static final String CONSOLE_LEVEL_CONF = "me.xuxiaoxiao$xtools-common$log.console.level";
    private static final String CONSOLE_LEVEL_DEFAULT = LEVEL_NOTICE;
    private static final String CONSOLE_FORMATTER_CONF = "me.xuxiaoxiao$xtools-common$log.console.formatter";
    private static final String CONSOLE_FORMATTER_DEFAULT = LogFormatter.class.getName();
    private static final String FILE_PATH_CONF = "me.xuxiaoxiao$xtools-common$log.file.path";
    private static final String FILE_PATH_DEFAULT = "";
    private static final String FILE_LEVEL_CONF = "me.xuxiaoxiao$xtools-common$log.file.level";
    private static final String FILE_LEVEL_DEFAULT = LEVEL_DETAIL;
    private static final String FILE_FORMATTER_CONF = "me.xuxiaoxiao$xtools-common$log.file.formatter";
    private static final String FILE_FORMATTER_DEFAULT = LogFormatter.class.getName();

    private static final Logger LOGGER = Logger.getLogger(XLogTools.class.getPackage().getName());

    private static boolean loggable;

    static {
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);
        XLogTools.loggable = Boolean.valueOf(XConfigTools.confDef(LOGGABLE_CONF, LOGGABLE_DEFAULT));

        Level level = strToLevel(XTools.confDef(CONSOLE_LEVEL_CONF, CONSOLE_LEVEL_DEFAULT).toLowerCase());
        Formatter formatter;
        try {
            formatter = (Formatter) Class.forName(XTools.confDef(CONSOLE_FORMATTER_CONF, CONSOLE_FORMATTER_DEFAULT)).newInstance();
        } catch (Exception e) {
            formatter = new LogFormatter();
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        handler.setLevel(level);
        LOGGER.addHandler(handler);

        String filePath = XTools.confDef(FILE_PATH_CONF, FILE_PATH_DEFAULT);
        if (!XTools.strEmpty(filePath)) {
            try {
                Formatter fileFormatter;
                try {
                    fileFormatter = (Formatter) Class.forName(XTools.confDef(FILE_FORMATTER_CONF, FILE_FORMATTER_DEFAULT)).newInstance();
                } catch (Exception e) {
                    fileFormatter = new LogFormatter();
                }
                Level fileLevel = strToLevel(XTools.confDef(FILE_LEVEL_CONF, FILE_LEVEL_DEFAULT));

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
     */
    public static void logError(String error) {
        if (loggable) {
            LOGGER.log(newRecord(Level.SEVERE, error));
        }
    }

    /**
     * 打印警告日志
     *
     * @param warning 警告日志信息
     */
    public static void logWarning(String warning) {
        if (loggable) {
            LOGGER.log(newRecord(Level.WARNING, warning));
        }
    }

    /**
     * 打印提示日志
     *
     * @param notice 提示日志信息
     */
    public static void logNotice(String notice) {
        if (loggable) {
            LOGGER.log(newRecord(Level.INFO, notice));
        }
    }

    /**
     * 打印详细日志
     *
     * @param detail 详细日志信息
     */
    public static void logDetail(String detail) {
        if (loggable) {
            LOGGER.log(newRecord(Level.CONFIG, detail));
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
