package me.xuxiaoxiao.xtools.common.http.executor;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import me.xuxiaoxiao.xtools.common.http.XRequest;
import me.xuxiaoxiao.xtools.common.http.XResponse;

import java.io.DataOutputStream;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * http执行器的默认实现类
 */
public class XHttpExecutorImpl implements XHttpExecutor {

    /**
     * 请求执行选项
     */
    protected Option option;
    /**
     * 请求拦截器
     */
    protected Interceptor[] interceptors;

    @Override
    public XResponse execute(HttpURLConnection connection, XRequest request) throws Exception {
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
                    connection.setChunkedStreamingMode(option.chunkLength());
                }
                try (DataOutputStream dOutStream = new DataOutputStream(connection.getOutputStream())) {
                    content.contentWrite(dOutStream);
                }
            }
        }
        //获取输入流
        return new XResponse(connection, connection.getInputStream());
    }

    @Override
    public Option option() {
        if (option == null) {
            option = new Option();
        }
        return option;
    }

    /**
     * http拦截器
     *
     * @return 默认有一个Cookie拦截器，为每个请求设置和保存Cookie信息
     */
    @Override
    public Interceptor[] interceptors() {
        if (interceptors == null) {
            String interceptorsStr = XTools.confDef(XHttpTools.CONF_INTERCEPTORS, XHttpTools.CONF_INTERCEPTORS_DEFAULT);
            if (!XTools.strEmpty(interceptorsStr)) {
                List<XHttpExecutor.Interceptor> interceptorList = new LinkedList<>();
                for (String str : interceptorsStr.split(",")) {
                    try {
                        interceptorList.add((XHttpExecutor.Interceptor) Class.forName(str.trim()).newInstance());
                    } catch (Exception e) {
                        XTools.logW("XHttpInterceptor:%s 初始化失败", str);
                        e.printStackTrace();
                    }
                }
                interceptors = interceptorList.toArray(new XHttpExecutor.Interceptor[0]);
            } else {
                interceptors = new Interceptor[0];
            }
        }
        return interceptors;
    }

    /**
     * 默认的Cookie拦截器，拦截每个http请求，自动解析和添加Cookie信息
     */
    public final static class CookieInterceptor implements XHttpExecutor.Interceptor {

        /**
         * 拦截每个http请求，自动解析和添加Cookie信息
         *
         * @param executor   http执行器
         * @param connection http连接
         * @param request    请求参数
         * @return 请求结果
         * @throws Exception 拦截过程中可能会发生异常
         */
        @Override
        public XResponse intercept(XHttpExecutor executor, HttpURLConnection connection, XRequest request) throws Exception {
            CookieManager cookieManager = executor.option().cookieManager();
            if (cookieManager == null) {
                return executor.execute(connection, request);
            } else {
                Map<String, List<String>> cookiesList = cookieManager.get(connection.getURL().toURI(), new HashMap<String, List<String>>());
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
                XResponse response = executor.execute(connection, request);
                cookieManager.put(connection.getURL().toURI(), connection.getHeaderFields());
                return response;
            }
        }
    }
}
