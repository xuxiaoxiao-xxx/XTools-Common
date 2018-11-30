package me.xuxiaoxiao.xtools.common.log.logger;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.log.XLogTools;

import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XLoggerImpl implements XLogger {

    protected Logger logger = Logger.getLogger(XTools.md5(String.valueOf(System.nanoTime() + new Random().nextInt())));
    protected Option option;

    {
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        for (Handler handler : supply().handlers()) {
            logger.addHandler(handler);
        }
    }

    @Override
    public Option supply() {
        if (this.option == null) {
            this.option = new Option();
        }
        return this.option;
    }

    @Override
    public void logE(String error, Object... args) {
        if (supply().loggable()) {
            logger.log(XLogTools.newRecord(Level.SEVERE, String.format(error, args)));
        }
    }

    @Override
    public void logW(String warning, Object... args) {
        if (supply().loggable()) {
            logger.log(XLogTools.newRecord(Level.WARNING, String.format(warning, args)));
        }
    }

    @Override
    public void logN(String notice, Object... args) {
        if (supply().loggable()) {
            logger.log(XLogTools.newRecord(Level.INFO, String.format(notice, args)));
        }
    }

    @Override
    public void logD(String detail, Object... args) {
        if (supply().loggable()) {
            logger.log(XLogTools.newRecord(Level.CONFIG, String.format(detail, args)));
        }
    }
}
