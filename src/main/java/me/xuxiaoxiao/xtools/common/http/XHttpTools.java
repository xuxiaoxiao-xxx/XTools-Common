package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.http.interceptior.ConfigInterceptor;
import me.xuxiaoxiao.xtools.common.http.interceptior.CookieInterceptor;
import me.xuxiaoxiao.xtools.common.http.interceptior.XHttpInterceptor;
import me.xuxiaoxiao.xtools.common.http.option.XHttpOption;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 常用的基本的关于HTTP的函数的集合
 */
public final class XHttpTools {
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

    public static final String CONF_INTERCEPTORS = "me.xuxiaoxiao$xtools-common$http.interceptors";
    public static final String CONF_INTERCEPTORS_DEFAULT = ConfigInterceptor.class.getName() + ", " + CookieInterceptor.class.getName();
    /**
     * 默认的请求配置
     */
    public static XHttpOption DEFAULT_OPTION = new XHttpOption();

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
            //根据请求url获取连接
            HttpURLConnection connection = connect(option, request.requestUrl());
            //配置HTTP请求
            if (option.interceptors != null && option.interceptors.length > 0) {
                for (XHttpInterceptor interceptor : option.interceptors) {
                    interceptor.onReq(option, connection, request);
                }
            }
            //设置请求方法
            connection.setRequestMethod(request.requestMethod());
            //设置请求头
            List<XRequest.KeyValue> headers = request.requestHeaders();
            if (headers != null) {
                for (XRequest.KeyValue keyValue : headers) {
                    connection.addRequestProperty(keyValue.key, String.valueOf(keyValue.value));
                }
            }
            //如果为POST或PUT方法则输出请求体
            if (XRequest.METHOD_POST.equals(request.requestMethod()) || XRequest.METHOD_PUT.equals(request.requestMethod())) {
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                XRequest.Content content = request.requestContent();
                if (content != null) {
                    if (content.contentLength() < 0) {
                        connection.setChunkedStreamingMode(option.chunkLength);
                    }
                    try (DataOutputStream dOutStream = new DataOutputStream(connection.getOutputStream())) {
                        content.contentWrite(dOutStream);
                    }
                }
            }
            //获取输入流
            InputStream inStream = connection.getInputStream();
            XResponse response = new XResponse(connection, inStream);
            //解析HTTP请求
            if (option.interceptors != null && option.interceptors.length > 0) {
                for (XHttpInterceptor interceptor : option.interceptors) {
                    interceptor.onRsp(option, connection, response);
                }
            }
            //返回请求结果
            return response;
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
}
