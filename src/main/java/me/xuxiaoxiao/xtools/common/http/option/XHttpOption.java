package me.xuxiaoxiao.xtools.common.http.option;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutorImpl;

import javax.net.ssl.*;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

/**
 * 请求的配置类
 */
public class XHttpOption {
    /**
     * 连接的超时时间
     */
    public final int connectTimeout;
    /**
     * 读取的超时时间
     */
    public final int readTimeout;
    /**
     * 是否自动重定向
     */
    public final boolean followRedirect = followRedirect();
    /**
     * chunk块大小
     */
    public final int chunkLength = chunkLength();
    /**
     * SSL上下文
     */
    public final SSLContext sslContext = sslContext();
    /**
     * 主机名验证器
     */
    public final HostnameVerifier hostnameVerifier = hostnameVerifier();
    /**
     * cookie管理器
     */
    public final CookieManager cookieManager = cookieManager();
    /**
     * http执行器
     */
    public final XHttpExecutor executor = executor();
    /**
     * http拦截器
     */
    public final XHttpExecutor.Interceptor[] interceptors = interceptors();

    /**
     * 新建一个配置对象, 指定10秒连接超时、10秒读取超时
     */
    public XHttpOption() {
        this(Integer.valueOf(XTools.confDef(XHttpTools.CONF_CONNECT_TIMEOUT, XHttpTools.CONF_CONNECT_TIMEOUT_DEFAULT)), Integer.valueOf(XTools.confDef(XHttpTools.CONF_READ_TIMEOUT, XHttpTools.CONF_READ_TIMEOUT_DEFAULT)));
    }

    /**
     * 新建一个配置对象, 并指定连接超时、读取超时
     *
     * @param connectTimeout 　指定的连接超时时间
     * @param readTimeout    　指定的读取超时时间
     */
    public XHttpOption(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * 是否自动重定向
     *
     * @return 是否自动重定向
     */
    public boolean followRedirect() {
        return Boolean.valueOf(XTools.confDef(XHttpTools.CONF_FOLLOW_REDIRECT, XHttpTools.CONF_FOLLOW_REDIRECT_DEFAULT));
    }

    /**
     * chunk块大小
     *
     * @return chunk块大小
     */
    public int chunkLength() {
        return Integer.valueOf(XTools.confDef(XHttpTools.CONF_CHUNK_LENGTH, XHttpTools.CONF_CHUNK_LENGTH_DEFAULT));
    }

    /**
     * 获取SSL上下文
     *
     * @return 默认不进行证书验证
     */
    public SSLContext sslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance(XTools.confDef(XHttpTools.CONF_SSL_PROVIDER, XHttpTools.CONF_SSL_PROVIDER_DEFAULT));
            KeyManager[] keyManagers = null;
            String keyManagersStr = XTools.confDef(XHttpTools.CONF_KEY_MANAGERS, XHttpTools.CONF_KEY_MANAGERS_DEFAULT);
            if (!XTools.strEmpty(keyManagersStr)) {
                List<KeyManager> keyManagerList = new LinkedList<>();
                for (String str : keyManagersStr.split(",")) {
                    try {
                        keyManagerList.add((KeyManager) Class.forName(str.trim()).newInstance());
                    } catch (Exception e) {
                        XTools.logW("KeyManager:%s 初始化失败", str);
                        e.printStackTrace();
                    }
                }
                keyManagers = keyManagerList.toArray(new KeyManager[0]);
            }

            TrustManager[] trustManagers = null;
            String trustManagersStr = XTools.confDef(XHttpTools.CONF_TRUST_MANAGERS, XHttpTools.CONF_TRUST_MANAGERS_DEFAULT);
            if (!XTools.strEmpty(trustManagersStr)) {
                List<TrustManager> trustManagerList = new LinkedList<>();
                for (String str : trustManagersStr.split(",")) {
                    try {
                        trustManagerList.add((TrustManager) Class.forName(str.trim()).newInstance());
                    } catch (Exception e) {
                        XTools.logW("TrustManager:%s 初始化失败", str);
                        e.printStackTrace();
                    }
                }
                trustManagers = trustManagerList.toArray(new TrustManager[0]);
            }

            SecureRandom secureRandom = null;
            String secureRandomStr = XTools.confDef(XHttpTools.CONF_SECURE_RANDOM, XHttpTools.CONF_SECURE_RANDOM_DEFAULT);
            try {
                if (!XTools.strEmpty(secureRandomStr)) {
                    secureRandom = (SecureRandom) Class.forName(secureRandomStr.trim()).newInstance();
                }
            } catch (Exception e) {
                XTools.logW("SecureRandom:%s 初始化失败, 将使用默认的SecureRandom", secureRandomStr);
                e.printStackTrace();
            }
            sslContext.init(keyManagers, trustManagers, secureRandom);
            return sslContext;
        } catch (Exception e) {
            XTools.logE("SSLContext初始化失败");
            e.printStackTrace();
            throw new RuntimeException("SSLContext初始化失败");
        }
    }

    /**
     * 获取主机名验证器
     *
     * @return 默认不进行主机名验证
     */
    public HostnameVerifier hostnameVerifier() {
        String hostnameVerifierStr = XTools.confDef(XHttpTools.CONF_HOSTNAME_VERIFIER, XHttpTools.CONF_HOSTNAME_VERIFIER_DEFAULT);
        try {
            return (HostnameVerifier) Class.forName(hostnameVerifierStr).newInstance();
        } catch (Exception e) {
            XTools.logW("HostnameVerifier:%s初始化失败, 将使用默认的HostnameVerifier", hostnameVerifierStr);
            e.printStackTrace();
            return new XHostnameVerifier();
        }
    }

    /**
     * cookie管理器
     *
     * @return 默认使用内存cookie管理器
     */
    public CookieManager cookieManager() {
        try {
            return (CookieManager) Class.forName(XTools.confDef(XHttpTools.CONF_COOKIE_MANAGER, XHttpTools.CONF_COOKIE_MANAGER_DEFAULT)).newInstance();
        } catch (Exception e) {
            XTools.logW("CookieManager:%s初始化失败, 将不进行Cookie管理");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * http执行器
     *
     * @return 默认是用的是自带的执行器
     */
    public XHttpExecutor executor() {
        String executorStr = XTools.confDef(XHttpTools.CONF_EXECUTOR, XHttpTools.CONF_EXECUTOR_DEFAULT);
        try {
            return (XHttpExecutor) Class.forName(executorStr).newInstance();
        } catch (Exception e) {
            XTools.logW("XHttpExecutor:%s 初始化失败, 将使用默认的XHttpExecutor", executorStr);
            e.printStackTrace();
            return new XHttpExecutorImpl();
        }
    }

    /**
     * http拦截器
     *
     * @return 默认有一个Cookie拦截器，为每个请求设置和保存Cookie信息
     */
    public XHttpExecutor.Interceptor[] interceptors() {
        XHttpExecutor.Interceptor[] interceptors = null;
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
        }
        return interceptors;
    }

    /**
     * 默认的主机名验证器，不进行主机名校验
     */
    public static class XHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

    /**
     * 默认的Cookie管理器，接收所有Cookie信息并存储在内存中
     */
    public static class XCookieManager extends CookieManager {

        public XCookieManager() {
            super(null, CookiePolicy.ACCEPT_ALL);
        }
    }
}

