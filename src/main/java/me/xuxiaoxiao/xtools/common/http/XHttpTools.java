package me.xuxiaoxiao.xtools.common.http;

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
    /**
     * 默认的请求配置
     */
    public static XOption DEFAULT_OPTION = new XOption();

    private XHttpTools() {
    }

    /**
     * 使用给定的请求选项进行HTTP请求
     *
     * @param option  给定的请求选项
     * @param request HTTP请求
     * @return HTTP响应
     */
    public static XResponse http(XOption option, XRequest request) {
        try {
            //根据请求url获取连接
            HttpURLConnection connection = connect(option, request.requestUrl());
            //配置HTTP请求
            option.connectionSetting(connection);
            //设置请求方法
            connection.setRequestMethod(request.requestMethod());
            //设置请求头
            List<XRequest.KeyValue> headers = request.requestHeaders();
            if (headers != null) {
                for (XRequest.KeyValue keyValue : headers) {
                    connection.addRequestProperty(keyValue.key, String.valueOf(keyValue.value));
                }
            }
            //如果为PUT和POST方法则输出请求体
            if (XRequest.METHOD_PUT.equals(request.requestMethod()) || XRequest.METHOD_POST.equals(request.requestMethod())) {
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
            //解析HTTP请求
            option.connectionParsing(connection);
            //返回请求结果
            return new XResponse(connection, inStream);
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
    private static HttpURLConnection connect(XOption option, String url) throws Exception {
        if (url.toLowerCase().startsWith("http://")) {
            return (HttpURLConnection) new URL(url).openConnection();
        } else if (url.toLowerCase().startsWith("https://")) {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setSSLSocketFactory(option.sslContext.getSocketFactory());
            connection.setHostnameVerifier(option.hostnameVerifier);
            return connection;
        } else {
            throw new Exception("XHttpTools仅支持HTTP协议和HTTPS协议");
        }
    }

    /**
     * 设置默认的Http请求配置
     *
     * @param option Http请求配置
     */
    public static void setDefaultOption(XOption option) {
        XHttpTools.DEFAULT_OPTION = option;
    }

    /**
     * 获取默认的Http请求配置
     *
     * @return 默认的Http请求配置
     */
    public static XOption getDefaultOption() {
        return XHttpTools.DEFAULT_OPTION;
    }
}
