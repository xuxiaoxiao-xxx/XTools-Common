package me.xuxiaoxiao.xtools.common.http.executor;

import me.xuxiaoxiao.xtools.common.http.XRequest;
import me.xuxiaoxiao.xtools.common.http.XResponse;
import me.xuxiaoxiao.xtools.common.http.option.XHttpOption;

import java.net.HttpURLConnection;

public interface XHttpExecutor {
    XResponse execute(XHttpOption option, HttpURLConnection connection, XRequest request) throws Exception;

    interface Interceptor {

        XResponse intercept(XHttpExecutor executor, XHttpOption option, HttpURLConnection connection, XRequest request) throws Exception;
    }
}
