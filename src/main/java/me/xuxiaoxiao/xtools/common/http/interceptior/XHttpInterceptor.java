package me.xuxiaoxiao.xtools.common.http.interceptior;

import me.xuxiaoxiao.xtools.common.http.XRequest;
import me.xuxiaoxiao.xtools.common.http.XResponse;
import me.xuxiaoxiao.xtools.common.http.option.XHttpOption;

import java.net.HttpURLConnection;

public interface XHttpInterceptor {

    void onReq(XHttpOption option, HttpURLConnection connection, XRequest request);

    void onRsp(XHttpOption option, HttpURLConnection connection, XResponse response);
}
