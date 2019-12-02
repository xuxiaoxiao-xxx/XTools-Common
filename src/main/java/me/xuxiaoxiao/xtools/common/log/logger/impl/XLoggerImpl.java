package me.xuxiaoxiao.xtools.common.log.logger.impl;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.log.logger.XLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.*;

/**
 * 默认的JDK日志记录器实现类
 */
public class XLoggerImpl implements XLogger {
    public static final String CFG_LEVEL = XTools.CFG_PREFIX + "log.level";
    public static final String CFG_LEVEL_DEFAULT = "detail";
    public static final String CFG_HANDLERS = XTools.CFG_PREFIX + "log.handlers";
    public static final String CFG_HANDLERS_DEFAULT = XLoggerImpl.XConsoleHandler.class.getName() + "," + XLoggerImpl.XFileHandler.class.getName();
    public static final String CFG_FORMATTER = XTools.CFG_PREFIX + "log.formatter";
    public static final String CFG_FORMATTER_DEFAULT = XLoggerImpl.XLogFormatter.class.getName();
    public static final String CFG_CONSOLE_LEVEL = XTools.CFG_PREFIX + "log.console.level";
    public static final String CFG_CONSOLE_LEVEL_DEFAULT = "detail";
    public static final String CFG_FILE = XTools.CFG_PREFIX + "log.file.name";
    public static final String CFG_FILE_DEFAULT = "xlogger.log";
    public static final String CFG_FILE_APPEND = XTools.CFG_PREFIX + "log.file.append";
    public static final String CFG_FILE_APPEND_DEFAULT = "true";
    public static final String CFG_FILE_LEVEL = XTools.CFG_PREFIX + "log.file.level";
    public static final String CFG_FILE_LEVEL_DEFAULT = "detail";
    private static final String TAG = "xlog";
    /**
     * jdk日志根记录器
     */
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Logger logger = Logger.getLogger(TAG);
    private final ArrayList<Handler> handlers = new ArrayList<>();

    public XLoggerImpl() {
        this.logger.setUseParentHandlers(false);
        this.setLevel(XTools.cfgDef(CFG_LEVEL, CFG_LEVEL_DEFAULT));
        String handlersStr = XTools.cfgDef(CFG_HANDLERS, CFG_HANDLERS_DEFAULT);
        if (!XTools.strBlank(handlersStr)) {
            for (String handlerClass : handlersStr.split(",")) {
                addHandler((Handler) XConfigTools.supply(handlerClass.trim()));
            }
        }
    }

    @Nonnull
    private static String buildMsg(@Nullable Throwable throwable, @Nonnull String msg, @Nullable Object... args) {
        StringBuilder sbMsg = new StringBuilder(String.format(msg, args));
        if (throwable != null) {
            sbMsg.append('\n').append(throwable.getMessage());
            for (StackTraceElement element : throwable.getStackTrace()) {
                sbMsg.append('\n').append(element.toString());
            }
        }
        return sbMsg.toString();
    }

    @Nonnull
    private static Level strToLevel(@Nullable String level) {
        if (XTools.strEmpty(level)) {
            return Level.ALL;
        } else {
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
    }

    @Nonnull
    private static String levelToStr(@Nullable Level level) {
        if (Level.OFF.equals(level)) {
            return XLogger.LEVEL_OFF;
        } else if (Level.SEVERE.equals(level)) {
            return XLogger.LEVEL_ERROR;
        } else if (Level.WARNING.equals(level)) {
            return XLogger.LEVEL_WARNING;
        } else if (Level.INFO.equals(level)) {
            return XLogger.LEVEL_NOTICE;
        } else {
            return XLogger.LEVEL_DETAIL;
        }
    }

    @Override
    public void logE(@Nonnull String tag, @Nonnull String error, @Nullable Object... args) {
        this.logE(tag, null, error, args);
    }

    @Override
    public void logE(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String error, @Nullable Object... args) {
        this.rwLock.readLock().lock();
        try {
            if (this.logger.isLoggable(strToLevel(LEVEL_ERROR))) {
                for (Handler handler : this.handlers) {
                    if (handler.accept(tag)) {
                        this.logger.log(Level.SEVERE, buildMsg(throwable, error, args), tag);
                        break;
                    }
                }
            }
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    @Override
    public void logW(@Nonnull String tag, @Nonnull String warning, @Nullable Object... args) {
        this.logW(tag, null, warning, args);
    }

    @Override
    public void logW(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String warning, @Nullable Object... args) {
        this.rwLock.readLock().lock();
        try {
            if (this.logger.isLoggable(strToLevel(LEVEL_WARNING))) {
                for (Handler handler : this.handlers) {
                    if (handler.accept(tag)) {
                        this.logger.log(Level.WARNING, buildMsg(throwable, warning, args), tag);
                        break;
                    }
                }
            }
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    @Override
    public void logN(@Nonnull String tag, @Nonnull String notice, @Nullable Object... args) {
        this.logN(tag, null, notice, args);
    }

    @Override
    public void logN(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String notice, @Nullable Object... args) {
        this.rwLock.readLock().lock();
        try {
            if (this.logger.isLoggable(strToLevel(LEVEL_NOTICE))) {
                for (Handler handler : this.handlers) {
                    if (handler.accept(tag)) {
                        this.logger.log(Level.INFO, buildMsg(throwable, notice, args), tag);
                        break;
                    }
                }
            }
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    @Override
    public void logD(@Nonnull String tag, @Nonnull String detail, @Nullable Object... args) {
        this.logD(tag, null, detail, args);
    }

    @Override
    public void logD(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String detail, @Nullable Object... args) {
        this.rwLock.readLock().lock();
        try {
            if (this.logger.isLoggable(strToLevel(LEVEL_DETAIL))) {
                for (Handler handler : this.handlers) {
                    if (handler.accept(tag)) {
                        this.logger.log(Level.CONFIG, buildMsg(throwable, detail, args), tag);
                        break;
                    }
                }
            }
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    @Nonnull
    @Override
    public String getLevel() {
        return levelToStr(this.logger.getLevel());
    }

    @Override
    public void setLevel(@Nonnull String level) {
        this.logger.setLevel(strToLevel(level));
    }

    @Override
    public void addHandler(@Nonnull Handler handler) {
        rwLock.writeLock().lock();
        try {
            this.handlers.add(handler);
            if (handler instanceof java.util.logging.Handler) {
                this.logger.addHandler((java.util.logging.Handler) handler);
            } else {
                this.logger.addHandler(new HandlerWrapper(handler));
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Nonnull
    @Override
    public Handler[] getHandlers() {
        rwLock.readLock().lock();
        try {
            return this.handlers.toArray(new Handler[0]);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Hander包装器，将XLogger.Handler包装成JdkLogHandler
     */
    private static class HandlerWrapper extends java.util.logging.Handler {

        @Nonnull
        private final Handler handler;

        public HandlerWrapper(@Nonnull Handler handler) {
            this.handler = handler;
        }

        @Override
        public void publish(LogRecord record) {
            if (handler.accept((String) record.getParameters()[0])) {
                handler.record(XLoggerImpl.levelToStr(record.getLevel()), (String) record.getParameters()[0], record.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    /**
     * 日志格式化器
     */
    public static class XLogFormatter extends Formatter {
        private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

        private char levelChar(@Nullable Level level) {
            if (Level.SEVERE.equals(level)) {
                return 'E';
            } else if (Level.WARNING.equals(level)) {
                return 'W';
            } else if (Level.INFO.equals(level)) {
                return 'N';
            } else {
                return 'D';
            }
        }

        @Override
        public String format(LogRecord record) {
            String time = XTools.dateFormat(TIME_FORMAT, new Date(record.getMillis()));
            String tag = (record.getParameters() == null || record.getParameters()[0] == null) ? "xlog" : String.valueOf(record.getParameters()[0]);
            return String.format("%s|%s|%s| %s | %s\n", time, levelChar(record.getLevel()), Thread.currentThread().getName(), tag, record.getMessage());
        }
    }

    /**
     * 控制台日志处理器
     */
    public static class XConsoleHandler extends ConsoleHandler implements Handler {

        public XConsoleHandler() {
            this.setLevel(XLoggerImpl.strToLevel(XTools.cfgDef(CFG_CONSOLE_LEVEL, CFG_CONSOLE_LEVEL_DEFAULT).trim()));
            this.setFormatter((Formatter) XConfigTools.supply(XTools.cfgDef(CFG_FORMATTER, CFG_FORMATTER_DEFAULT).trim()));
        }

        @Override
        public boolean accept(@Nonnull String tag) {
            return true;
        }

        @Override
        public void record(@Nonnull String level, @Nonnull String tag, @Nonnull String msg) {
            if (accept(tag)) {
                LogRecord logRecord = new LogRecord(XLoggerImpl.strToLevel(level), msg);
                logRecord.setParameters(new Object[]{tag});
                this.publish(logRecord);
            }
        }
    }

    /**
     * 文件日志处理器
     */
    public static class XFileHandler extends FileHandler implements Handler {

        public XFileHandler() throws IOException {
            super(XTools.cfgDef(CFG_FILE, CFG_FILE_DEFAULT).trim(), Boolean.parseBoolean(XTools.cfgDef(CFG_FILE_APPEND, CFG_FILE_APPEND_DEFAULT).trim()));
            this.setLevel(XLoggerImpl.strToLevel(XTools.cfgDef(CFG_FILE_LEVEL, CFG_FILE_LEVEL_DEFAULT).trim()));
            this.setFormatter((Formatter) XConfigTools.supply(XTools.cfgDef(CFG_FORMATTER, CFG_FORMATTER_DEFAULT).trim()));
        }

        @Override
        public boolean accept(@Nonnull String tag) {
            return true;
        }

        @Override
        public void record(@Nonnull String level, @Nonnull String tag, @Nonnull String msg) {
            if (accept(tag)) {
                LogRecord logRecord = new LogRecord(XLoggerImpl.strToLevel(level), msg);
                logRecord.setParameters(new Object[]{tag});
                this.publish(logRecord);
            }
        }
    }
}
