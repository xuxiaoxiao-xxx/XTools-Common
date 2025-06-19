package me.xuxiaoxiao.xtools.common.http.impl;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;

import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class XExecutor {

    @Nonnull
    private final XHttpTools.Config config;

    public XExecutor(@Nonnull XHttpTools.Config config) {
        this.config = config;
    }

    @Nonnull
    public XHttpTools.Config getConfig() {
        return config;
    }

    @Nonnull
    public XResponse execute(@Nonnull XRequest request) throws Exception {
        XHttpTools.Config config = getConfig();

        String url = request.getUrl();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        //根据请求选项进行连接配置
        connection.setConnectTimeout(config.getConnectTimeout());
        connection.setReadTimeout(config.getReadTimeout());
        connection.setInstanceFollowRedirects(config.isFollowRedirect());
        if (url.toLowerCase().startsWith("https://")) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(config.getSslContext().getSocketFactory());
            ((HttpsURLConnection) connection).setHostnameVerifier(config.getHostnameVerifier());
        }

        //设置请求方法
        connection.setRequestMethod(request.getMethod());

        List<XRequest.KeyValue> headers = request.getHeaders();
        if (headers != null) {
            //设置请求头
            for (XRequest.KeyValue keyValue : headers) {
                connection.addRequestProperty(keyValue.getKey(), String.valueOf(keyValue.getValue()));
            }
        }

        if (config.getCookieManager() != null) {
            // 添加 cookie
            Map<String, List<String>> allTypeCookies = config.getCookieManager().get(connection.getURL().toURI(), new HashMap<>());
            if (!XTools.isBlank(allTypeCookies)) {
                for (String cookieType : allTypeCookies.keySet()) {
                    List<String> cookieValues = allTypeCookies.get(cookieType);
                    if (!XTools.isBlank(cookieValues)) {
                        StringJoiner joiner = new StringJoiner(";");
                        for (String cookieStr : cookieValues) {
                            joiner.add(cookieStr);
                        }
                        connection.setRequestProperty(cookieType, joiner.toString());
                    }
                }
            }
        }

        XRequest.Content content = request.getContent();
        if (content != null) {
            //输出请求体
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            if (content.contentLength() < 0) {
                connection.setChunkedStreamingMode(config.getChunkLength());
            }
            try (DataOutputStream dOutStream = new DataOutputStream(connection.getOutputStream())) {
                content.writeToStream(dOutStream);
            }
        }

        // 执行请求
        connection.getResponseCode();

        if (config.getCookieManager() != null) {
            //处理返回的cookie信息
            config.getCookieManager().put(connection.getURL().toURI(), connection.getHeaderFields());
        }
        return new XResponse(connection);
    }
}
