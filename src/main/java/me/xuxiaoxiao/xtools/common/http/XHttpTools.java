package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutorImpl;
import me.xuxiaoxiao.xtools.common.http.option.XHttpOption;

import javax.net.ssl.HttpsURLConnection;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 常用的基本的关于HTTP的函数的集合
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
    public static final String CONF_HOSTNAME_VERIFIER_DEFAULT = XHttpOption.XHostnameVerifier.class.getName();

    public static final String CONF_COOKIE_MANAGER = "me.xuxiaoxiao$xtools-common$http.cookieManager";
    public static final String CONF_COOKIE_MANAGER_DEFAULT = XHttpOption.XCookieManager.class.getName();

    public static final String CONF_EXECUTOR = "me.xuxiaoxiao$xtools-common$http.executor";
    public static final String CONF_EXECUTOR_DEFAULT = XHttpExecutorImpl.class.getName();

    public static final String CONF_INTERCEPTORS = "me.xuxiaoxiao$xtools-common$http.interceptors";
    public static final String CONF_INTERCEPTORS_DEFAULT = XHttpExecutorImpl.CookieInterceptor.class.getName();

    /**
     * 默认的请求配置
     */
    public static final XHttpOption DEFAULT_OPTION = new XHttpOption();

    private XHttpTools() {
    }

    /**
     * 使用给定的请求选项进行HTTP请求
     *
     * @param option  给定的请求选项
     * @param request HTTP请求
     * @return HTTP响应
     */
    public static XResponse http(XHttpOption option, XRequest request) {
        try {
            return execute(option, config(option, connect(option, request.requestUrl())), request);
        } catch (Exception e) {
            // 请求异常结束，返回空的请求结果
            e.printStackTrace();
            return new XResponse(null, null);
        }
    }

    /**
     * 根据请求的url获取请求的连接
     *
     * @param option 请求配置
     * @param url    请求的地址
     * @return 请求的连接
     * @throws Exception 当url不属于HTTP协议或HTTPS协议时抛出异常
     */
    private static HttpURLConnection connect(XHttpOption option, String url) throws Exception {
        if (url.toLowerCase().startsWith("http://")) {
            return (HttpURLConnection) new URL(url).openConnection();
        } else if (url.toLowerCase().startsWith("https://")) {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setSSLSocketFactory(option.sslContext.getSocketFactory());
            connection.setHostnameVerifier(option.hostnameVerifier);
            return connection;
        } else {
            throw new IllegalArgumentException("XHttpTools仅支持HTTP协议和HTTPS协议");
        }
    }

    private static HttpURLConnection config(XHttpOption option, HttpURLConnection connection) throws Exception {
        //根据请求选项进行连接配置
        connection.setConnectTimeout(option.connectTimeout);
        connection.setReadTimeout(option.readTimeout);
        connection.setInstanceFollowRedirects(option.followRedirect);
        return connection;
    }

    private static XResponse execute(XHttpOption option, HttpURLConnection connection, XRequest request) throws Exception {
        XHttpExecutor executor = (XHttpExecutor) Class.forName(XTools.confDef(CONF_EXECUTOR, CONF_EXECUTOR_DEFAULT)).newInstance();
        if (option.interceptors != null && option.interceptors.length > 0) {
            for (XHttpExecutor.Interceptor interceptor : option.interceptors) {
                executor = (XHttpExecutor) Proxy.newProxyInstance(XHttpTools.class.getClassLoader(), new Class[]{XHttpExecutor.class}, new ExecuteHandler(executor, interceptor));
            }
        }
        return executor.execute(option, connection, request);
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
            return interceptor.intercept(target, (XHttpOption) args[0], (HttpURLConnection) args[1], (XRequest) args[2]);
        }
    }
}
