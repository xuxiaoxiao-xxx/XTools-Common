package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;
import me.xuxiaoxiao.xtools.common.http.executor.impl.XHttpExecutorImpl;
import me.xuxiaoxiao.xtools.common.http.executor.impl.XResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

/**
 * HTTP工具类
 */
public final class XHttpTools {
    public static final String CFG_EXECUTOR = XTools.CFG_PREFIX + "http.executor";
    public static final String CFG_EXECUTOR_DEFAULT = XHttpExecutorImpl.class.getName();

    /**
     * 默认的请求执行器
     */
    public static XHttpExecutor EXECUTOR = XConfigTools.supply(XTools.cfgDef(XHttpTools.CFG_EXECUTOR, XHttpTools.CFG_EXECUTOR_DEFAULT).trim());

    private XHttpTools() {
    }

    /**
     * 使用给定的请求选项进行HTTP请求
     *
     * @param executor http请求执行器
     * @param request  HTTP请求
     * @return HTTP响应
     */
    @Nonnull
    public static XHttpExecutor.Response http(@Nonnull XHttpExecutor executor, @Nonnull XHttpExecutor.Request request) {
        try {
            return executor.execute(request);
        } catch (Exception e) {
            return new XResponse(null, null);
        }
    }

    /**
     * 添加HTTP装饰者
     *
     * @param decorator HTTP装饰者
     */
    public static synchronized void addDecorator(@Nonnull Decorator decorator) {
        decorator.setOrigin(XHttpTools.EXECUTOR);
        XHttpTools.EXECUTOR = decorator;
    }

    /**
     * HTTP执行装饰者，对每个执行器的方法进行装饰
     */
    public static abstract class Decorator implements XHttpExecutor {
        private XHttpExecutor origin;

        final void setOrigin(@Nonnull XHttpExecutor origin) {
            this.origin = origin;
        }

        @Nonnull
        public final XHttpExecutor getOrigin() {
            return this.origin;
        }

        @Override
        public int getConnectTimeout() {
            return this.origin.getConnectTimeout();
        }

        @Override
        public void setConnectTimeout(int timeout) {
            this.origin.setConnectTimeout(timeout);
        }

        @Override
        public int getReadTimeout() {
            return this.origin.getReadTimeout();
        }

        @Override
        public void setReadTimeout(int timeout) {
            this.origin.setReadTimeout(timeout);
        }

        @Override
        public void addCookie(@Nullable URI uri, @Nonnull HttpCookie cookie) {
            this.origin.addCookie(uri, cookie);
        }

        @Nonnull
        @Override
        public List<HttpCookie> getCookies(@Nonnull URI uri) {
            return this.origin.getCookies(uri);
        }

        @Nonnull
        @Override
        public List<HttpCookie> getCookies() {
            return this.origin.getCookies();
        }

        @Override
        public void rmvCookies(@Nullable URI uri, @Nonnull HttpCookie cookie) {
            this.origin.rmvCookies(uri, cookie);
        }

        @Override
        public void rmvCookies() {
            this.origin.rmvCookies();
        }

        @Nonnull
        @Override
        public abstract Response execute(@Nonnull Request request) throws Exception;
    }
}
