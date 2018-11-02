package me.xuxiaoxiao.xtools.common.http.interceptior;

import me.xuxiaoxiao.xtools.common.http.XRequest;
import me.xuxiaoxiao.xtools.common.http.XResponse;
import me.xuxiaoxiao.xtools.common.http.option.XHttpOption;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookieInterceptor implements XHttpInterceptor {
    @Override
    public void onReq(XHttpOption option, HttpURLConnection connection, XRequest request) {
        //设置cookie
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRsp(XHttpOption option, HttpURLConnection connection, XResponse response) {
        //解析cookie
        try {
            option.cookieManager.put(connection.getURL().toURI(), connection.getHeaderFields());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
