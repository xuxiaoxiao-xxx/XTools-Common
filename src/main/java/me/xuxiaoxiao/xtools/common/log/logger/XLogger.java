package me.xuxiaoxiao.xtools.common.log.logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
     */
    void logE(@Nonnull String tag, @Nonnull String error);

    /**
     * 记录错误信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param error     错误信息
     */
    void logE(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String error);

    /**
     * 记录警告信息的日志
     *
     * @param tag     日志tag
     * @param warning 警告信息
     */
    void logW(@Nonnull String tag, @Nonnull String warning);

    /**
     * 记录警告信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param warning   告警信息
     */
    void logW(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String warning);

    /**
     * 记录提示信息的日志
     *
     * @param tag    日志tag
     * @param notice 提示信息
     */
    void logN(@Nonnull String tag, @Nonnull String notice);

    /**
     * 记录提示信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param notice    提示信息
     */
    void logN(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String notice);

    /**
     * 记录详细信息的日志
     *
     * @param tag    日志tag
     * @param detail 详细信息
     */
    void logD(@Nonnull String tag, @Nonnull String detail);

    /**
     * 记录详细信息的日志
     *
     * @param tag       日志tag
     * @param throwable 异常对象
     * @param detail    详细信息
     */
    void logD(@Nonnull String tag, @Nullable Throwable throwable, @Nonnull String detail);

    /**
     * 是否处理日志
     *
     * @param tag 日志tag
     * @return 是否处理
     */
    boolean accept(@Nonnull String tag);

    /**
     * 获取根节点日志等级
     *
     * @return 根节点日志等级
     */
    @Nonnull
    String getLevel();

    /**
     * 设置根节点日志等级
     *
     * @param level 根节点日志等级
     */
    void setLevel(@Nonnull String level);

    /**
     * 添加日志处理器
     *
     * @param handler 日志处理器
     */
    void addHandler(@Nonnull Handler handler);

    /**
     * 获取日志处理器
     *
     * @return 日志处理器
     */
    @Nonnull
    Handler[] getHandlers();

    /**
     * 日志处理器接口
     */
    interface Handler {
        /**
         * 日志处理方法
         *
         * @param level 日志等级
         * @param tag   日志tag
         * @param msg   日志信息
         */
        void record(@Nonnull String level, @Nonnull String tag, @Nonnull String msg);
    }
}
