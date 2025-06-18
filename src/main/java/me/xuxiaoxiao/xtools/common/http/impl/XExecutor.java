package me.xuxiaoxiao.xtools.common.http.impl;

import me.xuxiaoxiao.xtools.common.http.XHttpTools;

import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XExecutor {
    private final XHttpTools.Config config;

    public XExecutor(XHttpTools.Config config) {
        this.config = config;
    }

    @Nonnull
    public XResponse execute(@Nonnull XRequest request) throws Exception {
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
                connection.addRequestProperty(keyValue.key, String.valueOf(keyValue.value));
            }
        }

        if (config.getCookieManager() != null) {
            //添加cookie
            Map<String, List<String>> cookiesList = config.getCookieManager().get(connection.getURL().toURI(), new HashMap<String, List<String>>());
            for (String cookieType : cookiesList.keySet()) {
                StringBuilder sbCookie = new StringBuilder();
                for (String cookieStr : cookiesList.get(cookieType)) {
                    if (sbCookie.length() > 0) {
                        sbCookie.append(';');
                    }
                    sbCookie.append(cookieStr);
                }
                if (sbCookie.length() > 0) {
                    connection.setRequestProperty(cookieType, sbCookie.toString());
                }
            }
        }

        //如果请求体不为null，则输出请求体
        if (request.getContent() != null) {
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            XRequest.Content content = request.getContent();
            if (content != null) {
                if (content.contentLength() < 0) {
                    connection.setChunkedStreamingMode(config.getChunkLength());
                }
                try (DataOutputStream dOutStream = new DataOutputStream(connection.getOutputStream())) {
                    content.writeToStream(dOutStream);
                }
            }
        }

        //获取输入流
        InputStream inStream = connection.getInputStream();
        if (config.getCookieManager() != null) {
            //读取cookie
            config.getCookieManager().put(connection.getURL().toURI(), connection.getHeaderFields());
        }
        return new XResponse(connection, inStream);
    }
}
