package me.xuxiaoxiao.xtools.common.log.logger;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XSupplier;
import me.xuxiaoxiao.xtools.common.log.XLogTools;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.*;

public interface XLogger extends XSupplier<XLogger.Option> {

    void logE(String error, Object... args);

    void logW(String warning, Object... args);

    void logN(String notice, Object... args);

    void logD(String detail, Object... args);

    class Option {

        protected Boolean loggable;

        protected Handler[] handlers;

        public boolean loggable() {
            if (loggable == null) {
                loggable = Boolean.valueOf(XTools.cfgDef(XLogTools.CONF_LOGGABLE, XLogTools.CONF_LOGGABLE_DEFAULT));
            }
            return loggable;
        }

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
