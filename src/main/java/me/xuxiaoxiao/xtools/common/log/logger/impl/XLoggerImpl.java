package me.xuxiaoxiao.xtools.common.log.logger.impl;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.log.XLogTools;
import me.xuxiaoxiao.xtools.common.log.logger.XLogger;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

/**
 * 默认的JDK日志记录器实现类
 */
public class XLoggerImpl implements XLogger {
    private static final String TAG = "xlog";

    private String level = XLogger.LEVEL_DETAIL;
    private Map<String, Level> levels = new ConcurrentHashMap<>();

    /**
     * jdk日志根记录器
     */
    private final Logger root = Logger.getLogger(TAG);

    public XLoggerImpl() {
        this.root.setUseParentHandlers(false);
        defaultHandlers();
        defaultLevel();
    }

    /**
     * 记录错误信息的日志
     *
     * @param error 错误信息
     * @param args  错误信息中的参数
     */
    @Override
    public void logE(String tag, String error, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, prepareMsg(null, error, args), tag);
        }
    }

    @Override
    public void logE(String tag, Throwable throwable, String error, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, prepareMsg(throwable, error, args), tag);
        }
    }

    /**
     * 记录告警信息的日志
     *
     * @param warning 告警信息
     * @param args    告警信息中的参数
     */
    @Override
    public void logW(String tag, String warning, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.WARNING)) {
            logger.log(Level.WARNING, prepareMsg(null, warning, args), tag);
        }
    }

    @Override
    public void logW(String tag, Throwable throwable, String warning, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.WARNING)) {
            logger.log(Level.WARNING, prepareMsg(throwable, warning, args), tag);
        }
    }

    /**
     * 记录提示信息的日志
     *
     * @param notice 提示信息
     * @param args   提示信息中的参数
     */
    @Override
    public void logN(String tag, String notice, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, prepareMsg(null, notice, args), tag);
        }
    }

    @Override
    public void logN(String tag, Throwable throwable, String notice, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, prepareMsg(throwable, notice, args), tag);
        }
    }

    /**
     * 记录详细信息的日志
     *
     * @param detail 详细信息
     * @param args   详细信息中的参数
     */
    @Override
    public void logD(String tag, String detail, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.CONFIG)) {
            logger.log(Level.CONFIG, prepareMsg(null, detail, args), tag);
        }
    }

    @Override
    public void logD(String tag, Throwable throwable, String detail, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.CONFIG)) {
            logger.log(Level.CONFIG, prepareMsg(throwable, detail, args), tag);
        }
    }

    @Override
    public void setLevel(String level) {
        this.root.setLevel(strToLevel(level));
    }

    @Override
    public String getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(String tag, String level) {
        if (!XTools.strEmpty(tag) && !XTools.strEmpty(level)) {
            levels.put(tag, strToLevel(level));
        }
    }

    @Override
    public String getLevel(String tag) {
        return levelToStr(levels.get(tag));
    }

    private Logger prepareLogger(String tag) {
        Logger logger = Logger.getLogger(TAG + "." + tag);
        if (tag.lastIndexOf('.') > 0) {
            logger.setParent(prepareLogger(tag.substring(0, tag.lastIndexOf('.'))));
        }
        if (levels.containsKey(tag)) {
            logger.setLevel(levels.get(tag));
        }
        return logger;
    }

    private String prepareMsg(Throwable throwable, String msg, Object... args) {
        StringBuilder sbMsg = new StringBuilder(String.format(msg, args));
        if (throwable != null) {
            sbMsg.append('\n').append(throwable.getMessage());
            for (StackTraceElement element : throwable.getStackTrace()) {
                sbMsg.append('\n').append(element.toString());
            }
        }
        return sbMsg.toString();
    }

    private Level strToLevel(String level) {
        switch (level) {
            case XLogger.LEVEL_OFF:
                return Level.OFF;
            case XLogger.LEVEL_ERROR:
                return Level.SEVERE;
            case XLogger.LEVEL_WARNING:
                return Level.WARNING;
            case XLogger.LEVEL_NOTICE:
                return Level.INFO;
            default:
                return Level.ALL;
        }
    }

    private String levelToStr(Level level) {
        if (level.equals(Level.OFF)) {
            return XLogger.LEVEL_OFF;
        } else if (level.equals(Level.SEVERE)) {
            return XLogger.LEVEL_ERROR;
        } else if (level.equals(Level.WARNING)) {
            return XLogger.LEVEL_WARNING;
        } else if (level.equals(Level.INFO)) {
            return XLogger.LEVEL_NOTICE;
        } else {
            return XLogger.LEVEL_DETAIL;
        }
    }

    private void defaultLevel() {
        this.root.setLevel(strToLevel(XTools.cfgGet(XLogTools.CFG_LEVEL)));
    }

    private void defaultHandlers() {
        Formatter formatter;
        try {
            formatter = (Formatter) Class.forName(XTools.cfgGet(XLogTools.CFG_FORMATTER)).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            formatter = new XLogFormatter();
        }

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(formatter);
        this.root.addHandler(consoleHandler);

        try {
            Handler fileHandler = new FileHandler(XTools.cfgGet(XLogTools.CFG_PATTERN));
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(formatter);
            this.root.addHandler(fileHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 日志格式化器
     */
    public static class XLogFormatter extends Formatter {
        private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

        private char levelChar(Level level) {
            if (level.equals(Level.SEVERE)) {
                return 'E';
            } else if (level.equals(Level.WARNING)) {
                return 'W';
            } else if (level.equals(Level.INFO)) {
                return 'N';
            } else {
                return 'D';
            }
        }

        @Override
        public String format(LogRecord record) {
            String time = XTools.dateFormat(TIME_FORMAT, new Date(record.getMillis()));
            String tag = (record.getParameters() == null || record.getParameters()[0] == null) ? "xlog" : String.valueOf(record.getParameters()[0]);
            return String.format("%s | %s | %s | %s\n", time, tag, levelChar(record.getLevel()), record.getMessage());
        }
    }
}
