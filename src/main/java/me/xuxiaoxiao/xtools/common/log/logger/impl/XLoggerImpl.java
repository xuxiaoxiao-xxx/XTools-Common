package me.xuxiaoxiao.xtools.common.log.logger.impl;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.log.logger.XLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

/**
 * 默认的JDK日志记录器实现类
 */
public class XLoggerImpl implements XLogger {
    private static final String TAG = "xlog";

    public static final String CFG_HANDLERS = XTools.CFG_PREFIX + "log.handlers";
    public static final String CFG_HANDLERS_DEFAULT = XLoggerImpl.XConsoleHandler.class.getName() + "," + XLoggerImpl.XFileHandler.class.getName();

    public static final String CFG_FORMATTER = XTools.CFG_PREFIX + "log.formatter";
    public static final String CFG_FORMATTER_DEFAULT = XLoggerImpl.XLogFormatter.class.getName();

    public static final String CFG_CONSOLE_LEVEL = XTools.CFG_PREFIX + "log.console.level";
    public static final String CFG_CONSOLE_LEVEL_DEFAULT = "detail";

    public static final String CFG_FILE = XTools.CFG_PREFIX + "log.file";
    public static final String CFG_FILE_DEFAULT = "xlogger.log";

    public static final String CFG_FILE_APPEND = XTools.CFG_PREFIX + "log.file.append";
    public static final String CFG_FILE_APPEND_DEFAULT = "true";

    public static final String CFG_FILE_LEVEL = XTools.CFG_PREFIX + "log.file.level";
    public static final String CFG_FILE_LEVEL_DEFAULT = "warning";


    /**
     * jdk日志根记录器
     */
    private final Logger root = Logger.getLogger(TAG);

    /**
     * 日志tag等级
     */
    private final Map<String, Level> levels = new ConcurrentHashMap<>();

    public XLoggerImpl() {
        this.root.setUseParentHandlers(false);
        defaultHandlers();
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
            logger.log(Level.SEVERE, buildMsg(null, error, args), tag);
        }
    }

    @Override
    public void logE(String tag, Throwable throwable, String error, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, buildMsg(throwable, error, args), tag);
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
            logger.log(Level.WARNING, buildMsg(null, warning, args), tag);
        }
    }

    @Override
    public void logW(String tag, Throwable throwable, String warning, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.WARNING)) {
            logger.log(Level.WARNING, buildMsg(throwable, warning, args), tag);
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
            logger.log(Level.INFO, buildMsg(null, notice, args), tag);
        }
    }

    @Override
    public void logN(String tag, Throwable throwable, String notice, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, buildMsg(throwable, notice, args), tag);
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
            logger.log(Level.CONFIG, buildMsg(null, detail, args), tag);
        }
    }

    @Override
    public void logD(String tag, Throwable throwable, String detail, Object... args) {
        Logger logger = prepareLogger(tag);
        if (logger.isLoggable(Level.CONFIG)) {
            logger.log(Level.CONFIG, buildMsg(throwable, detail, args), tag);
        }
    }

    @Override
    public void setLevel(String level) {
        this.root.setLevel(strToLevel(level));
    }

    @Override
    public String getLevel() {
        return levelToStr(this.root.getLevel());
    }

    @Override
    public void setLevel(String tag, String level) {
        if (!XTools.strEmpty(tag)) {
            if (!XTools.strEmpty(level)) {
                levels.put(tag, strToLevel(level));
            } else {
                levels.remove(tag);
            }
        }
    }

    @Override
    public String getLevel(String tag) {
        return levelToStr(levels.get(tag));
    }

    @Override
    public void addHandler(Handler handler) {
        this.root.addHandler(new HHandler(handler));
    }

    @Override
    public Handler[] getHandlers() {
        java.util.logging.Handler[] handlers = this.root.getHandlers();
        List<Handler> handlerList = new ArrayList<>(handlers.length);
        for (java.util.logging.Handler handler : handlers) {
            if (handler instanceof HHandler) {
                handlerList.add(((HHandler) handler).handler);
            }
        }
        return handlerList.toArray(new Handler[0]);
    }

    private Logger prepareLogger(String tag) {
        Logger logger = Logger.getLogger(TAG + "." + tag);
        if (tag.lastIndexOf('.') > 0) {
            logger.setParent(prepareLogger(tag.substring(0, tag.lastIndexOf('.'))));
        }
        if (levels.containsKey(tag)) {
            logger.setLevel(levels.get(tag));
        } else {
            logger.setLevel(Level.ALL);
        }
        return logger;
    }

    private static String buildMsg(Throwable throwable, String msg, Object... args) {
        StringBuilder sbMsg = new StringBuilder(String.format(msg, args));
        if (throwable != null) {
            sbMsg.append('\n').append(throwable.getMessage());
            for (StackTraceElement element : throwable.getStackTrace()) {
                sbMsg.append('\n').append(element.toString());
            }
        }
        return sbMsg.toString();
    }

    private static Level strToLevel(String level) {
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

    private static String levelToStr(Level level) {
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

    private void defaultHandlers() {
        String handlersStr = XTools.cfgDef(CFG_HANDLERS, CFG_HANDLERS_DEFAULT);
        if (!XTools.strBlank(handlersStr)) {
            for (String handlerClass : handlersStr.split(",")) {
                addHandler((Handler) XConfigTools.supply(handlerClass.trim()));
            }
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
            return String.format("%s | %s | %s | %s\n", time, levelChar(record.getLevel()), tag, record.getMessage());
        }
    }

    private static class HHandler extends java.util.logging.Handler {
        private final Handler handler;

        public HHandler(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void publish(LogRecord record) {
            handler.record(XLoggerImpl.levelToStr(record.getLevel()), (String) record.getParameters()[0], record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        @Override
        public Level getLevel() {
            return XLoggerImpl.strToLevel(handler.getLevel());
        }

        @Override
        public synchronized void setLevel(Level newLevel) throws SecurityException {
            handler.setLevel(XLoggerImpl.levelToStr(newLevel));
        }
    }

    public static class XConsoleHandler implements Handler {
        private java.util.logging.Handler handler;

        public XConsoleHandler() {
            this.handler = new ConsoleHandler();
            handler.setLevel(XLoggerImpl.strToLevel(XTools.cfgDef(CFG_CONSOLE_LEVEL, CFG_CONSOLE_LEVEL_DEFAULT).trim()));
            handler.setFormatter((Formatter) XConfigTools.supply(XTools.cfgDef(CFG_FORMATTER, CFG_FORMATTER_DEFAULT).trim()));
        }

        @Override
        public void setLevel(String level) {
            handler.setLevel(XLoggerImpl.strToLevel(level));
        }

        @Override
        public String getLevel() {
            return XLoggerImpl.levelToStr(handler.getLevel());
        }

        @Override
        public void record(String level, String tag, String msg) {
            LogRecord logRecord = new LogRecord(XLoggerImpl.strToLevel(level), msg);
            logRecord.setParameters(new Object[]{tag});
            handler.publish(logRecord);
        }
    }

    public static class XFileHandler implements Handler {
        private java.util.logging.Handler handler;

        public XFileHandler() {
            try {
                this.handler = new FileHandler(XTools.cfgDef(CFG_FILE, CFG_FILE_DEFAULT).trim(), Boolean.valueOf(XTools.cfgDef(CFG_FILE_APPEND, CFG_FILE_APPEND_DEFAULT).trim()));
                handler.setLevel(XLoggerImpl.strToLevel(XTools.cfgDef(CFG_FILE_LEVEL, CFG_FILE_LEVEL_DEFAULT).trim()));
                handler.setFormatter((Formatter) XConfigTools.supply(XTools.cfgDef(CFG_FORMATTER, CFG_FORMATTER_DEFAULT).trim()));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setLevel(String level) {
            handler.setLevel(XLoggerImpl.strToLevel(level));
        }

        @Override
        public String getLevel() {
            return XLoggerImpl.levelToStr(handler.getLevel());
        }

        @Override
        public void record(String level, String tag, String msg) {
            LogRecord logRecord = new LogRecord(XLoggerImpl.strToLevel(level), msg);
            logRecord.setParameters(new Object[]{tag});
            handler.publish(logRecord);
        }
    }
}
