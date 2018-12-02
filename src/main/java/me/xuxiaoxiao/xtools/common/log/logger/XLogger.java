package me.xuxiaoxiao.xtools.common.log.logger;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XSupplier;
import me.xuxiaoxiao.xtools.common.log.XLogTools;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.*;

/**
 * 日志记录器，底层使用jdk的日志工具。
 * 能够记录四种不同等级的日志（详细，提醒，告警，错误）。
 * 并且能够添加不同的处理器，来将日志输出到控制台或文件中
 */
public interface XLogger extends XSupplier<XLogger.Option> {

    /**
     * 记录错误信息的日志
     *
     * @param error 错误信息
     * @param args  错误信息中的参数
     */
    void logE(String error, Object... args);

    /**
     * 记录告警信息的日志
     *
     * @param warning 告警信息
     * @param args    告警信息中的参数
     */
    void logW(String warning, Object... args);

    /**
     * 记录提示信息的日志
     *
     * @param notice 提示信息
     * @param args   提示信息中的参数
     */
    void logN(String notice, Object... args);

    /**
     * 记录详细信息的日志
     *
     * @param detail 详细信息
     * @param args   详细信息中的参数
     */
    void logD(String detail, Object... args);

    class Option {

        /**
         * 是否记录日志
         */
        protected Boolean loggable;

        /**
         * 日志处理器，可以指定多个日志处理器，来将日志输出到文件和控制台或其他地方
         */
        protected Handler[] handlers;

        /**
         * 是否记录日志，默认记录日志
         *
         * @return 是否记录日志
         */
        public boolean loggable() {
            if (loggable == null) {
                loggable = Boolean.valueOf(XTools.cfgDef(XLogTools.CONF_LOGGABLE, XLogTools.CONF_LOGGABLE_DEFAULT));
            }
            return loggable;
        }

        /**
         * 日志处理器，默认使用控制台日志处理器，和文件日志处理器
         *
         * @return 日志处理器
         */
        public Handler[] handlers() {
            if (handlers == null) {
                String handlersStr = XTools.cfgDef(XLogTools.CONF_HANDLERS_SUPPLIER, XLogTools.CONF_HANDLERS_SUPPLIER_DEFAULT);
                if (!XTools.strEmpty(handlersStr)) {
                    try {
                        XHandlersSupplier handlersSupplier = (XHandlersSupplier) Class.forName(handlersStr.trim()).newInstance();
                        handlers = handlersSupplier.supply();
                    } catch (Exception e) {
                        e.printStackTrace();
                        handlers = new Handler[0];
                    }
                } else {
                    handlers = new Handler[0];
                }
            }
            return handlers;
        }

        /**
         * 日志格式化器
         */
        public static class XLogFormatter extends Formatter {
            private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

            @Override
            public String format(LogRecord record) {
                String time = XTools.dateFormat(TIME_FORMAT, new Date(record.getMillis()));
                char level = Character.toUpperCase(XLogTools.levelToStr(record.getLevel()).charAt(0));
                int line = (record.getParameters() != null && record.getParameters().length > 0) ? (int) record.getParameters()[0] : 0;
                return String.format("%s | %s | %s.%s L%d: %s\n", time, level, record.getSourceClassName(), record.getSourceMethodName(), line, record.getMessage());
            }
        }

        /**
         * 默认的日志处理器提供者，提供记录详细及以上的控制台日志处理器，和记录提醒及以上的文件日志处理器
         */
        public static class XHandlersSupplier implements XSupplier<Handler[]> {

            @Override
            public Handler[] supply() {
                List<Handler> handlerList = new LinkedList<>();
                try {
                    Handler consoleHandler = new ConsoleHandler();
                    consoleHandler.setLevel(XLogTools.strToLevel(XTools.cfgDef(XLogTools.CONF_CONSOLE_LEVEL, XLogTools.CONF_CONSOLE_LEVEL_DEFAULT).toLowerCase()));
                    consoleHandler.setFormatter((Formatter) Class.forName(XTools.cfgDef(XLogTools.CONF_CONSOLE_FORMATTER, XLogTools.CONF_CONSOLE_FORMATTER_DEFAULT)).newInstance());
                    handlerList.add(consoleHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Handler fileHandler = new FileHandler(XTools.cfgDef(XLogTools.CONF_FILE_PATTERN, XLogTools.CONF_FILE_PATTERN_DEFAULT));
                    fileHandler.setLevel(XLogTools.strToLevel(XTools.cfgDef(XLogTools.CONF_FILE_LEVEL, XLogTools.CONF_FILE_LEVEL_DEFAULT)));
                    fileHandler.setFormatter((Formatter) Class.forName(XTools.cfgDef(XLogTools.CONF_FILE_FORMATTER, XLogTools.CONF_FILE_FORMATTER_DEFAULT)).newInstance());
                    handlerList.add(fileHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return handlerList.toArray(new Handler[0]);
            }
        }
    }
}
