package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;
import me.xuxiaoxiao.xtools.common.http.executor.impl.XHttpExecutorImpl;
import me.xuxiaoxiao.xtools.common.http.executor.impl.XResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * HTTP工具类
 */
public final class XHttpTools {
    public static final String CFG_EXECUTOR = XTools.CFG_PREFIX + "http.executor";
    public static final String CFG_EXECUTOR_DEFAULT = XHttpExecutorImpl.class.getName();

    /**
     * 默认的请求执行器
     */
    public static final XHttpExecutor EXECUTOR;

    static {
        XConfigTools.X_CONFIGS.cfgDef(XHttpTools.CFG_EXECUTOR, XHttpTools.CFG_EXECUTOR_DEFAULT);

        String executorStr = XTools.cfgGet(CFG_EXECUTOR);
        XHttpExecutor executor;
        try {
            executor = XConfigTools.supply(executorStr);
        } catch (Exception e) {
            e.printStackTrace();
            XTools.logW("XHttpExecutor:%s 初始化失败, 将使用默认的XHttpExecutorImpl", executorStr);
            executor = new XHttpExecutorImpl();
        }
        EXECUTOR = executor;
    }

    private XHttpTools() {
    }

    /**
     * 使用给定的请求选项进行HTTP请求
     *
     * @param request HTTP请求
     * @return HTTP响应
     */
    public static XHttpExecutor.Response http(XHttpExecutor executor, XHttpExecutor.Request request) {
        try {
            return execute(executor, request);
        } catch (Exception e) {
            return new XResponse(null, null);
        }
    }

    /**
     * 执行http请求
     *
     * @param request 请求参数
     * @return 请求结果
     * @throws Exception 请求过程中可能会发生异常
     */
    public static XHttpExecutor.Response execute(XHttpExecutor executor, XHttpExecutor.Request request) throws Exception {
        XHttpExecutor.Interceptor[] interceptors = executor.getInterceptors();
        if (interceptors != null && interceptors.length > 0) {
            for (XHttpExecutor.Interceptor interceptor : interceptors) {
                executor = (XHttpExecutor) Proxy.newProxyInstance(XHttpTools.class.getClassLoader(), new Class[]{XHttpExecutor.class}, new ExecuteHandler(executor, interceptor));
            }
        }
        return executor.execute(request);
    }

    private static class ExecuteHandler implements InvocationHandler {
        XHttpExecutor target;
        XHttpExecutor.Interceptor interceptor;

        public ExecuteHandler(XHttpExecutor target, XHttpExecutor.Interceptor interceptor) {
            this.target = target;
            this.interceptor = interceptor;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (XHttpExecutor.class.equals(method.getDeclaringClass())) {
                return interceptor.intercept(target, (XHttpExecutor.Request) args[0]);
            } else {
                return method.invoke(target, args);
            }
        }
    }
}
