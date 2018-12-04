package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutorImpl;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutorSupplier;

import javax.net.ssl.HttpsURLConnection;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP工具类
 */
public final class XHttpTools {

    public static final String CONF_REQ_CHARSET = "me.xuxiaoxiao$xtools-common$http.reqCharset";
    public static final String CONF_REQ_CHARSET_DEFAULT = "utf-8";

    public static final String CONF_RSP_CHARSET = "me.xuxiaoxiao$xtools-common$http.rspCharset";
    public static final String CONF_RSP_CHARSET_DEFAULT = "utf-8";

    public static final String CONF_CONNECT_TIMEOUT = "me.xuxiaoxiao$xtools-common$http.connectTimeout";
    public static final String CONF_CONNECT_TIMEOUT_DEFAULT = "10000";

    public static final String CONF_READ_TIMEOUT = "me.xuxiaoxiao$xtools-common$http.readTimeout";
    public static final String CONF_READ_TIMEOUT_DEFAULT = "10000";

    public static final String CONF_FOLLOW_REDIRECT = "me.xuxiaoxiao$xtools-common$http.followRedirect";
    public static final String CONF_FOLLOW_REDIRECT_DEFAULT = "false";

    public static final String CONF_CHUNK_LENGTH = "me.xuxiaoxiao$xtools-common$http.chunkLength";
    public static final String CONF_CHUNK_LENGTH_DEFAULT = "262144";

    public static final String CONF_SSL_PROVIDER = "me.xuxiaoxiao$xtools-common$http.sslProvider";
    public static final String CONF_SSL_PROVIDER_DEFAULT = "TLS";

    public static final String CONF_KEY_MANAGERS = "me.xuxiaoxiao$xtools-common$http.keyManagers";
    public static final String CONF_KEY_MANAGERS_DEFAULT = "";

    public static final String CONF_TRUST_MANAGERS = "me.xuxiaoxiao$xtools-common$http.trustManagers";
    public static final String CONF_TRUST_MANAGERS_DEFAULT = "";

    public static final String CONF_SECURE_RANDOM = "me.xuxiaoxiao$xtools-common$http.secureRandom";
    public static final String CONF_SECURE_RANDOM_DEFAULT = "";

    public static final String CONF_HOSTNAME_VERIFIER = "me.xuxiaoxiao$xtools-common$http.hostnameVerifier";
    public static final String CONF_HOSTNAME_VERIFIER_DEFAULT = XHttpExecutor.Option.XHostnameVerifier.class.getName();

    public static final String CONF_COOKIE_MANAGER = "me.xuxiaoxiao$xtools-common$http.cookieManager";
    public static final String CONF_COOKIE_MANAGER_DEFAULT = XHttpExecutor.Option.XCookieManager.class.getName();

    public static final String CONF_EXECUTOR_SUPPLIER = "me.xuxiaoxiao$xtools-common$http.executorSupplier";
    public static final String CONF_EXECUTOR_SUPPLIER_DEFAULT = XHttpExecutorSupplier.class.getName();

    public static final String CONF_INTERCEPTORS = "me.xuxiaoxiao$xtools-common$http.interceptors";
    public static final String CONF_INTERCEPTORS_DEFAULT = XHttpExecutorImpl.CookieInterceptor.class.getName();

    /**
     * 默认的请求执行器
     */
    public static final XHttpExecutor EXECUTOR;

    static {
        String executorSupplierStr = XTools.cfgDef(XHttpTools.CONF_EXECUTOR_SUPPLIER, XHttpTools.CONF_EXECUTOR_SUPPLIER_DEFAULT);
        XHttpExecutor executor;
        try {
            executor = ((XHttpExecutorSupplier) Class.forName(executorSupplierStr.trim()).newInstance()).supply();
        } catch (Exception e) {
            XTools.logW("XHttpExecutor:%s 初始化失败, 将使用默认的XHttpExecutor", executorSupplierStr);
            e.printStackTrace();
            executor = new XHttpExecutorSupplier().supply();
        }
        EXECUTOR = executor;
    }

    private XHttpTools() {
    }

    /**
     * 使用给定的请求选项进行HTTP请求
     *
     * @param request HTTP请求
     * @return HTTP响应
     */
    public static XResponse http(XHttpExecutor executor, XRequest request) {
        HttpURLConnection connection = null;
        try {
            connection = connect(executor, request.requestUrl());
            return execute(executor, connection, request);
        } catch (Exception e) {
            // 请求异常结束，返回空的请求结果
            e.printStackTrace();
            return new XResponse(connection, null);
        }
    }

    /**
     * 获取http请求的连接
     *
     * @param executor http配置项
     * @param url      请求的地址
     * @return 请求的连接，配置了连接超时时间、读取超时时间、自动重定向
     * @throws Exception 当url不属于HTTP协议或HTTPS协议时抛出异常
     */
    public static HttpURLConnection connect(XHttpExecutor executor, String url) throws Exception {
        XHttpExecutor.Option option = executor.config();
        if (url.toLowerCase().startsWith("http://")) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            //根据请求选项进行连接配置
            connection.setConnectTimeout(option.connectTimeout());
            connection.setReadTimeout(option.readTimeout());
            connection.setInstanceFollowRedirects(option.followRedirect());
            return connection;
        } else if (url.toLowerCase().startsWith("https://")) {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setSSLSocketFactory(option.sslContext().getSocketFactory());
            connection.setHostnameVerifier(option.hostnameVerifier());
            //根据请求选项进行连接配置
            connection.setConnectTimeout(option.connectTimeout());
            connection.setReadTimeout(option.readTimeout());
            connection.setInstanceFollowRedirects(option.followRedirect());
            return connection;
        } else {
            throw new IllegalArgumentException("XHttpTools仅支持HTTP协议和HTTPS协议");
        }
    }

    /**
     * 执行http请求
     *
     * @param connection http连接
     * @param request    请求参数
     * @return 请求结果
     * @throws Exception 请求过程中可能会发生异常
     */
    public static XResponse execute(XHttpExecutor executor, HttpURLConnection connection, XRequest request) throws Exception {
        XHttpExecutor.Interceptor[] interceptors = executor.config().interceptors();
        if (interceptors != null && interceptors.length > 0) {
            for (XHttpExecutor.Interceptor interceptor : interceptors) {
                executor = (XHttpExecutor) Proxy.newProxyInstance(XHttpTools.class.getClassLoader(), new Class[]{XHttpExecutor.class}, new ExecuteHandler(executor, interceptor));
            }
        }
        return executor.execute(connection, request);
    }

    private static class ExecuteHandler implements InvocationHandler {

        XHttpExecutor target;
        XHttpExecutor.Interceptor interceptor;

        public ExecuteHandler(XHttpExecutor target, XHttpExecutor.Interceptor interceptor) {
            this.target = target;
            this.interceptor = interceptor;
        }

        @Override
        public XResponse invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return interceptor.intercept(target, (HttpURLConnection) args[0], (XRequest) args[1]);
        }
    }
}
