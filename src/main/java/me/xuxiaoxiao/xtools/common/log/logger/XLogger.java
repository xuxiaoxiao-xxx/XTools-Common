package me.xuxiaoxiao.xtools.common.log.logger;

/**
 * 日志记录器。
 * <ul>
 * <li>能够记录四种不同等级的日志（详细，提醒，告警，错误）</li>
 * <li>能够为不同的tag设置不同的记录等级，来控制不同tag的日志记录</li>
 * </ul>
 */
public interface XLogger {
    String LEVEL_OFF = "off";
    String LEVEL_ERROR = "error";
    String LEVEL_WARNING = "warning";
    String LEVEL_NOTICE = "notice";
    String LEVEL_DETAIL = "detail";

    /**
     * 记录错误信息的日志
     *
     * @param tag   日志tag
     * @param error 错误信息
     * @param args  错误信息中的参数
     */
    void logE(String tag, String error, Object... args);

    /**
     * 记录错误信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param error     错误信息
     * @param args      错误信息中的参数
     */
    void logE(String tag, Throwable throwable, String error, Object... args);

    /**
     * 记录警告信息的日志
     *
     * @param tag     日志tag
     * @param warning 警告信息
     * @param args    警告信息中的参数
     */
    void logW(String tag, String warning, Object... args);

    /**
     * 记录警告信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param warning   告警信息
     * @param args      告警信息中的参数
     */
    void logW(String tag, Throwable throwable, String warning, Object... args);

    /**
     * 记录提示信息的日志
     *
     * @param tag    日志tag
     * @param notice 提示信息
     * @param args   提示信息中的参数
     */
    void logN(String tag, String notice, Object... args);

    /**
     * 记录提示信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param notice    提示信息
     * @param args      提示信息中的参数
     */
    void logN(String tag, Throwable throwable, String notice, Object... args);

    /**
     * 记录详细信息的日志
     *
     * @param tag    日志tag
     * @param detail 详细信息
     * @param args   详细信息中的参数
     */
    void logD(String tag, String detail, Object... args);

    /**
     * 记录详细信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param detail    详细信息
     * @param args      详细信息中的参数
     */
    void logD(String tag, Throwable throwable, String detail, Object... args);

    /**
     * 设置根节点日志等级
     *
     * @param level 根节点日志等级
     */
    void setLevel(String level);

    /**
     * 获取根节点日志等级
     *
     * @return 根节点日志等级
     */
    String getLevel();

    /**
     * 设置某个tag及其子级的日志等级
     *
     * @param tag   日志tag
     * @param level 日志等级
     */
    void setLevel(String tag, String level);

    /**
     * 获取某个tag及其子级的日志等级
     *
     * @return 根节点日志等级
     */
    String getLevel(String tag);

    /**
     * 添加日志处理器
     *
     * @param handler 日志处理器
     */
    void addHandler(Handler handler);

    /**
     * 获取日志处理器
     *
     * @return 日志处理器
     */
    Handler[] getHandlers();

    /**
     * 日志处理器接口
     */
    interface Handler {

        /**
         * 设置处理器的处理等级
         *
         * @param level 处理等级
         */
        void setLevel(String level);

        /**
         * 获取处理器的处理等级
         *
         * @return 处理等级
         */
        String getLevel();

        /**
         * 日志处理方法
         *
         * @param level 日志等级
         * @param tag   日志tag
         * @param msg   日志信息
         */
        void record(String level, String tag, String msg);
    }
}
