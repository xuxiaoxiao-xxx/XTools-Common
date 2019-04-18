package me.xuxiaoxiao.xtools.common.http.executor;

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
    void setConnectTimeout(int timeout);

    int getConnectTimeout();

    void setReadTimeout(int timeout);

    int getReadTimeout();

    void addCookie(URI uri, HttpCookie cookie);

    List<HttpCookie> getCookies(URI uri);

    List<HttpCookie> getCookies();

    void rmvCookies(URI uri, HttpCookie cookie);

    void rmvCookies();

    void setInterceptors(Interceptor... interceptors);

    Interceptor[] getInterceptors();

    /**
     * 执行http请求
     *
     * @param request 请求参数
     * @return 请求结果
     * @throws Exception 请求过程中可能会发生异常
     */
    Response execute(Request request) throws Exception;

    /**
     * http拦截器，能够拦截http执行器执行的每个请求。
     * 在拦截器的实现类中，你可以更改请求的参数，甚至进行多次其他的http请求。
     * 在拦截器的实现类中，你需要手动的调用执行器的execute方法来完成整个执行链。
     */
    interface Interceptor {

        /**
         * 拦截方法。拦截某个http执行器正在执行的请求。你可以更改请求的参数，甚至进行多次其他的http请求。
         * 需要注意的是，你需要手动的调用执行器的execute方法来完成整个执行链。
         *
         * @param executor http执行器
         * @param request  请求参数
         * @return 请求结果
         * @throws Exception 拦截过程中可能会发生异常
         */
        Response intercept(XHttpExecutor executor, Request request) throws Exception;
    }

    interface Request {
        void setMethod(String method);

        String getMethod();

        void setUrl(String url);

        String getUrl();

        void setHeader(String key, String value, boolean append);

        List<KeyValue> getHeaders();

        void setContent(Content content);

        Content getContent();
    }

    interface Response {

        InputStream stream();

        String string(String charset);

        File file(String path);
    }

    /**
     * HTTP请求体，需要提供请求体的类型、请求体的长度、请求体写出到输出流的方法
     */
    interface Content {
        /**
         * 请求体的MIME类型
         *
         * @return 请求体的MIME类型
         */
        String contentType() throws IOException;

        /**
         * 请求体的长度，如果不确定长度可以返回-1，这将使用chunked模式传输
         *
         * @return 请求体的长度
         */
        long contentLength() throws IOException;

        /**
         * 请求体写出到输出流的具体方法
         *
         * @param outStream 目标输出流
         * @throws IOException 将请求体写出到输出流时可能会发生异常
         */
        void contentWrite(OutputStream outStream) throws IOException;
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

        public KeyValue(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }
}
