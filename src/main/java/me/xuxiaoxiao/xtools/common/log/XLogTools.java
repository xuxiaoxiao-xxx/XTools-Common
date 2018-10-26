package me.xuxiaoxiao.xtools.common.log;

import me.xuxiaoxiao.xtools.common.XTools;

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
    private static final String LEVEL_CONF = "me.xuxiaoxiao$xtools-common$log.level";
    private static final String LEVEL_DEFAULT = LEVEL_NOTICE;
    private static final String FORMATTER_CONF = "me.xuxiaoxiao$xtools-common$log.formatter";
    private static final String FORMATTER_DEFAULT = LogFormatter.class.getName();
    private static final String FOLDER_CONF = "me.xuxiaoxiao$xtools-common$log.folder";
    private static final String FOLDER_DEFAULT = "./";

    private static final Logger LOGGER = Logger.getLogger(XLogTools.class.getPackage().getName());

    private static boolean loggable;

    static {
        XLogTools.loggable = Boolean.valueOf(XTools.confGet(LOGGABLE_CONF, LOGGABLE_DEFAULT));

        LOGGER.setUseParentHandlers(false);
        Level level = strToLevel(XTools.confGet(LEVEL_CONF, LEVEL_DEFAULT));
        Formatter formatter;
        try {
            formatter = (Formatter) Class.forName(XTools.confGet(FORMATTER_CONF, FORMATTER_DEFAULT)).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            formatter = new SimpleFormatter();
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        handler.setLevel(level);
        LOGGER.addHandler(handler);

        String folder = XTools.confGet(FOLDER_CONF, FOLDER_DEFAULT);
        if (!XTools.strEmpty(folder)) {
            try {
                FileHandler fileHandler = new FileHandler(folder + "logname.log");
                fileHandler.setFormatter(formatter);
                LOGGER.addHandler(fileHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LOGGER.setLevel(level);
    }

    public static void logError(String error) {
        LOGGER.log(newRecord(Level.SEVERE, error));
    }

    public static void logWarning(String warning) {
        LOGGER.log(newRecord(Level.WARNING, warning));
    }

    public static void logNotice(String notice) {
        LOGGER.log(newRecord(Level.INFO, notice));
    }

    public static void logDetail(String detail) {
        LOGGER.log(newRecord(Level.CONFIG, detail));
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
        switch (str) {
            case LEVEL_ERROR:
                return Level.SEVERE;
            case LEVEL_WARNING:
                return Level.WARNING;
            case LEVEL_NOTICE:
                return Level.INFO;
            default:
                return Level.CONFIG;
        }
    }

    private static LogRecord newRecord(Level level, String msg) {
        LogRecord record = new LogRecord(level, msg);
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        if (stack.length > 3) {
            record.setSourceClassName(stack[3].getClassName());
            record.setSourceMethodName(stack[3].getMethodName());
        } else {
            record.setSourceClassName("unknownClass");
            record.setSourceMethodName("unknownMethod");
        }
        return record;
    }

    public static class LogFormatter extends Formatter {
        private static final String DATE_FORMAT = "yy-MM-dd HH:mm:ss.SSS";

        @Override
        public String format(LogRecord record) {
            String date = XTools.dateFormat(DATE_FORMAT, new Date(record.getMillis()));
            char level = Character.toUpperCase(levelToStr(record.getLevel()).charAt(0));

            return date + " " + level + " " + record.getSourceClassName() + "." + record.getSourceMethodName() + ":" + record.getMessage();
        }
    }
}
