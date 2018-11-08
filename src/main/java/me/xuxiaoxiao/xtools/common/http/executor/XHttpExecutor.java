package me.xuxiaoxiao.xtools.common.http.executor;

import me.xuxiaoxiao.xtools.common.http.XRequest;
import me.xuxiaoxiao.xtools.common.http.XResponse;
import me.xuxiaoxiao.xtools.common.http.option.XHttpOption;

import java.net.HttpURLConnection;

/**
 * http执行器，根据请求的参数，执行http请求，获取请求的结果
 */
public interface XHttpExecutor {

    /**
     * 执行http请求
     *
     * @param option     http配置项
     * @param connection http连接
     * @param request    请求参数
     * @return 请求结果
     * @throws Exception 请求过程中可能会发生异常
     */
    XResponse execute(XHttpOption option, HttpURLConnection connection, XRequest request) throws Exception;

    /**
     * http拦截器，能够拦截http执行器执行的每个请求。
     * 在拦截器的实现类中，你可以更改请求的参数，甚至进行多次其他的http请求。
     * 在拦截器的实现类中，你需要手动的调用执行器的execute方法来完成整个执行链。
     */
    interface Interceptor {

        /**
         * 拦截方法。拦截某个http执行器正在执行的请求。你可以更改请求的参数，甚至进行多次其他的http请求。
         * 需要注意的是，你需要手动的调用执行器的execute方法来完成整个执行链。
         *
         * @param executor   http执行器
         * @param option     http配置项
         * @param connection http连接
         * @param request    请求参数
         * @return 请求结果
         * @throws Exception 拦截过程中可能会发生异常
         */
        XResponse intercept(XHttpExecutor executor, XHttpOption option, HttpURLConnection connection, XRequest request) throws Exception;
    }
}
