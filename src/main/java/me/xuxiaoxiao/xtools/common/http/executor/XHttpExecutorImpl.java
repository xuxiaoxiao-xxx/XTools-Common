package me.xuxiaoxiao.xtools.common.http.executor;

import me.xuxiaoxiao.xtools.common.http.XRequest;
import me.xuxiaoxiao.xtools.common.http.XResponse;
import me.xuxiaoxiao.xtools.common.http.option.XHttpOption;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XHttpExecutorImpl implements XHttpExecutor {

    @Override
    public XResponse execute(XHttpOption option, HttpURLConnection connection, XRequest request) throws Exception {
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
        return new XResponse(connection, connection.getInputStream());
    }

    public static class CookieInterceptor implements XHttpExecutor.Interceptor {

        @Override
        public XResponse intercept(XHttpExecutor executor, XHttpOption option, HttpURLConnection connection, XRequest request) throws Exception {
            Map<String, List<String>> cookiesList = option.cookieManager.get(connection.getURL().toURI(), new HashMap<String, List<String>>());
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
            XResponse response = executor.execute(option, connection, request);
            option.cookieManager.put(connection.getURL().toURI(), connection.getHeaderFields());
            return response;
        }
    }
}
