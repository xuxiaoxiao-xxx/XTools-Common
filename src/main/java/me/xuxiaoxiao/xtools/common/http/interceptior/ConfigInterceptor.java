package me.xuxiaoxiao.xtools.common.http.interceptior;

import me.xuxiaoxiao.xtools.common.http.XRequest;
import me.xuxiaoxiao.xtools.common.http.XResponse;
import me.xuxiaoxiao.xtools.common.http.option.XHttpOption;

import java.net.HttpURLConnection;

public class ConfigInterceptor implements XHttpInterceptor {

    @Override
    public void onReq(XHttpOption option, HttpURLConnection connection, XRequest request) {
        //根据请求选项进行连接配置
        connection.setConnectTimeout(option.connectTimeout);
        connection.setReadTimeout(option.readTimeout);
        connection.setInstanceFollowRedirects(option.followRedirect);
    }

    @Override
    public void onRsp(XHttpOption option, HttpURLConnection connection, XResponse response) {
    }
}
