package me.xuxiaoxiao.xtools.common.log.logger;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.log.XLogTools;

import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 默认的日志记录器实现类
 */
public class XLoggerImpl implements XLogger {

    /**
     * 配置信息类
     */
    private final Option option;
    /**
     * jdk日志工具
     */
    private final Logger logger = Logger.getLogger(XTools.md5(String.valueOf(System.nanoTime() + new Random().nextInt())));

    public XLoggerImpl() {
        this(new Option());
    }

    public XLoggerImpl(Option option) {
        this.option = option;
        this.logger.setLevel(Level.ALL);
        this.logger.setUseParentHandlers(false);
        for (Handler handler : config().handlers()) {
            this.logger.addHandler(handler);
        }
    }

    /**
     * 记录错误信息的日志
     *
     * @param error 错误信息
     * @param args  错误信息中的参数
     */
    @Override
    public void logE(String error, Object... args) {
        if (config().loggable()) {
            logger.log(XLogTools.newRecord(Level.SEVERE, String.format(error, args)));
        }
    }

    /**
     * 记录告警信息的日志
     *
     * @param warning 告警信息
     * @param args    告警信息中的参数
     */
    @Override
    public void logW(String warning, Object... args) {
        if (config().loggable()) {
            logger.log(XLogTools.newRecord(Level.WARNING, String.format(warning, args)));
        }
    }

    /**
     * 记录提示信息的日志
     *
     * @param notice 提示信息
     * @param args   提示信息中的参数
     */
    @Override
    public void logN(String notice, Object... args) {
        if (config().loggable()) {
            logger.log(XLogTools.newRecord(Level.INFO, String.format(notice, args)));
        }
    }

    /**
     * 记录详细信息的日志
     *
     * @param detail 详细信息
     * @param args   详细信息中的参数
     */
    @Override
    public void logD(String detail, Object... args) {
        if (config().loggable()) {
            logger.log(XLogTools.newRecord(Level.CONFIG, String.format(detail, args)));
        }
    }

    /**
     * 获得配置信息
     *
     * @return 配置信息对象
     */
    @Override
    public Option config() {
        return this.option;
    }
}
