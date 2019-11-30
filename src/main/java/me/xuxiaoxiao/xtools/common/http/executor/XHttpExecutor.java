package me.xuxiaoxiao.xtools.common.http.executor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

/**
 * http执行器，根据请求的参数，执行http请求，获取请求的结果
 */
public interface XHttpExecutor {
    /**
     * 获取连接超时时间
     *
     * @return 连接超时时间
     */
    int getConnectTimeout();

    /**
     * 设置连接超时时间
     *
     * @param timeout 连接超时时间
     */
    void setConnectTimeout(int timeout);

    /**
     * 获取读取超时时间
     *
     * @return 读取超时时间
     */
    int getReadTimeout();

    /**
     * 设置读取超时时间
     *
     * @param timeout 读取超时时间
     */
    void setReadTimeout(int timeout);

    /**
     * 添加cookie
     *
     * @param uri    cookie的uri
     * @param cookie cookie信息
     */
    void addCookie(@Nullable URI uri, @Nonnull HttpCookie cookie);

    /**
     * 获取cookie
     *
     * @param uri cookie的uri
     * @return cookie信息
     */
    @Nonnull
    List<HttpCookie> getCookies(@Nonnull URI uri);

    /**
     * 获取所有cookie
     *
     * @return cookie信息
     */
    @Nonnull
    List<HttpCookie> getCookies();

    /**
     * 删除cookie信息
     *
     * @param uri    cookie的uri
     * @param cookie cookie信息
     */
    void rmvCookies(@Nullable URI uri, @Nonnull HttpCookie cookie);

    /**
     * 删除所有cookie
     */
    void rmvCookies();

    /**
     * 执行http请求
     *
     * @param request 请求参数
     * @return 请求结果
     * @throws Exception 请求过程中可能会发生异常
     */
    @Nonnull
    Response execute(@Nonnull Request request) throws Exception;

    /**
     * http请求
     */
    interface Request {
        /**
         * 获取请求方法
         *
         * @return 请求方法
         */
        @Nonnull
        String getMethod();

        /**
         * 设置请求方法
         *
         * @param method 请求方法
         */
        void setMethod(@Nonnull String method);

        /**
         * 获取请求url
         *
         * @return 请求url
         */
        @Nonnull
        String getUrl();

        /**
         * 设置请求url
         *
         * @param url 请求url
         */
        void setUrl(@Nonnull String url);

        /**
         * 设置请求头部
         *
         * @param key    请求头键
         * @param value  请求头值
         * @param append 重复请求头是否追加
         */
        void setHeader(@Nonnull String key, @Nullable String value, boolean append);

        /**
         * 获取请求头部
         *
         * @return 请求头部
         */
        @Nullable
        List<KeyValue> getHeaders();

        /**
         * 获取请求体
         *
         * @return 请求体信息
         */
        @Nullable
        Content getContent();

        /**
         * 设置请求体
         *
         * @param content 请求体
         */
        void setContent(@Nonnull Content content);
    }

    /**
     * http响应
     */
    interface Response extends AutoCloseable {

        /**
         * 获取输入流
         *
         * @return 获取输入流
         */
        @Nullable
        InputStream stream();

        /**
         * 获取字符串结果(默认编码)
         *
         * @return 字符串结果
         */
        @Nullable
        String string();

        /**
         * 获取字符串结果
         *
         * @param charset 字符串编码
         * @return 字符串结果
         */
        @Nullable
        String string(String charset);

        /**
         * 获取文件结果
         *
         * @param path 文件路径
         * @return 文件结果
         */
        @Nullable
        File file(String path);
    }

    /**
     * HTTP请求体，需要提供请求体的类型、请求体的长度、请求体写出到输出流的方法
     */
    interface Content {
        /**
         * 请求体字符编码
         *
         * @return 字符编码
         */
        @Nonnull
        String charset() throws IOException;

        /**
         * 请求体的MIME类型
         *
         * @return 请求体的MIME类型
         * @throws IOException IO异常
         */
        @Nonnull
        String contentType() throws IOException;

        /**
         * 请求体的长度，如果不确定长度可以返回-1，这将使用chunked模式传输
         *
         * @return 请求体的长度
         * @throws IOException IO异常
         */
        long contentLength() throws IOException;

        /**
         * 请求体写出到输出流的具体方法
         *
         * @param outStream 目标输出流
         * @throws IOException 将请求体写出到输出流时可能会发生异常
         */
        void contentWrite(@Nonnull OutputStream outStream) throws IOException;
    }

    /**
     * 键值对
     */
    class KeyValue {
        /**
         * 键
         */
        public final String key;
        /**
         * 值
         */
        public final Object value;

        public KeyValue(@Nonnull String key, @Nonnull Object value) {
            this.key = key;
            this.value = value;
        }
    }
}
