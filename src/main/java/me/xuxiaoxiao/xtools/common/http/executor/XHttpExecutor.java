package me.xuxiaoxiao.xtools.common.http.executor;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigurable;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import me.xuxiaoxiao.xtools.common.http.XRequest;
import me.xuxiaoxiao.xtools.common.http.XResponse;

import javax.net.ssl.*;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

/**
 * http执行器，根据请求的参数，执行http请求，获取请求的结果
 */
public interface XHttpExecutor extends XConfigurable<XHttpExecutor.Option> {

    /**
     * 执行http请求
     *
     * @param connection http连接
     * @param request    请求参数
     * @return 请求结果
     * @throws Exception 请求过程中可能会发生异常
     */
    XResponse execute(HttpURLConnection connection, XRequest request) throws Exception;

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
         * @param connection http连接
         * @param request    请求参数
         * @return 请求结果
         * @throws Exception 拦截过程中可能会发生异常
         */
        XResponse intercept(XHttpExecutor executor, HttpURLConnection connection, XRequest request) throws Exception;
    }

    class Option {
        /**
         * 连接的超时时间
         */
        protected Integer connectTimeout;
        /**
         * 读取的超时时间
         */
        protected Integer readTimeout;
        /**
         * 是否自动重定向
         */
        protected Boolean followRedirect;
        /**
         * chunk块大小
         */
        protected Integer chunkLength;
        /**
         * SSL上下文
         */
        protected SSLContext sslContext;
        /**
         * 主机名验证器
         */
        protected HostnameVerifier hostnameVerifier;
        /**
         * cookie管理器
         */
        protected CookieManager cookieManager;
        /**
         * 获取http拦截器
         */
        protected Interceptor[] interceptors;

        public Option() {
            this(Integer.valueOf(XTools.cfgDef(XHttpTools.CFG_CONNECT_TIMEOUT, XHttpTools.CFG_CONNECT_TIMEOUT_DEFAULT)), Integer.valueOf(XTools.cfgDef(XHttpTools.CFG_READ_TIMEOUT, XHttpTools.CFG_READ_TIMEOUT_DEFAULT)));
        }

        public Option(int connectTimeout, int readTimeout) {
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
        }

        /**
         * 获取连接超时时间，毫秒
         *
         * @return 连接超时时间，毫秒
         */
        public Integer connectTimeout() {
            return connectTimeout;
        }

        /**
         * 获取读取超时时间，毫秒
         *
         * @return 读取超时时间，毫秒
         */
        public Integer readTimeout() {
            return readTimeout;
        }

        /**
         * 获取是否自动重定向
         *
         * @return 是否自动重定向
         */
        public boolean followRedirect() {
            if (followRedirect == null) {
                followRedirect = Boolean.valueOf(XTools.cfgDef(XHttpTools.CFG_FOLLOW_REDIRECT, XHttpTools.CFG_FOLLOW_REDIRECT_DEFAULT));
            }
            return followRedirect;
        }

        /**
         * 获取chunk块大小
         *
         * @return chunk块大小
         */
        public int chunkLength() {
            if (chunkLength == null) {
                chunkLength = Integer.valueOf(XTools.cfgDef(XHttpTools.CFG_CHUNK_LENGTH, XHttpTools.CFG_CHUNK_LENGTH_DEFAULT));
            }
            return chunkLength;
        }

        /**
         * 获取SSL上下文
         *
         * @return 默认不进行证书验证
         */
        public SSLContext sslContext() {
            if (sslContext == null) {
                try {
                    sslContext = SSLContext.getInstance(XTools.cfgDef(XHttpTools.CFG_SSL_PROVIDER, XHttpTools.CFG_SSL_PROVIDER_DEFAULT));
                    KeyManager[] keyManagers = null;
                    String keyManagersStr = XTools.cfgDef(XHttpTools.CFG_KEY_MANAGERS, XHttpTools.CFG_KEY_MANAGERS_DEFAULT);
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
                    String trustManagersStr = XTools.cfgDef(XHttpTools.CFG_TRUST_MANAGERS, XHttpTools.CFG_TRUST_MANAGERS_DEFAULT);
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
                    String secureRandomStr = XTools.cfgDef(XHttpTools.CFG_SECURE_RANDOM, XHttpTools.CFG_SECURE_RANDOM_DEFAULT);
                    try {
                        if (!XTools.strEmpty(secureRandomStr)) {
                            secureRandom = (SecureRandom) Class.forName(secureRandomStr.trim()).newInstance();
                        }
                    } catch (Exception e) {
                        XTools.logW("SecureRandom:%s 初始化失败, 将使用默认的SecureRandom", secureRandomStr);
                        e.printStackTrace();
                    }
                    sslContext.init(keyManagers, trustManagers, secureRandom);
                } catch (Exception e) {
                    XTools.logE("SSLContext初始化失败");
                    e.printStackTrace();
                    throw new RuntimeException("SSLContext初始化失败");
                }
            }
            return sslContext;
        }

        /**
         * 获取主机名验证器
         *
         * @return 默认不进行主机名验证
         */
        public HostnameVerifier hostnameVerifier() {
            if (hostnameVerifier == null) {
                String hostnameVerifierStr = XTools.cfgDef(XHttpTools.CFG_HOSTNAME_VERIFIER, XHttpTools.CFG_HOSTNAME_VERIFIER_DEFAULT);
                try {
                    hostnameVerifier = (HostnameVerifier) Class.forName(hostnameVerifierStr).newInstance();
                } catch (Exception e) {
                    XTools.logW("HostnameVerifier:%s初始化失败, 将使用默认的HostnameVerifier", hostnameVerifierStr);
                    e.printStackTrace();
                    hostnameVerifier = new XHostnameVerifier();
                }
            }
            return hostnameVerifier;
        }

        /**
         * cookie管理器
         *
         * @return 默认使用内存cookie管理器
         */
        public CookieManager cookieManager() {
            if (cookieManager == null) {
                try {
                    cookieManager = (CookieManager) Class.forName(XTools.cfgDef(XHttpTools.CFG_COOKIE_MANAGER, XHttpTools.CFG_COOKIE_MANAGER_DEFAULT)).newInstance();
                } catch (Exception e) {
                    XTools.logW("CookieManager:%s初始化失败, 将使用默认的CookieManager");
                    e.printStackTrace();
                    cookieManager = new XCookieManager();
                }
            }
            return cookieManager;
        }

        /**
         * http拦截器
         *
         * @return 默认有一个Cookie拦截器，为每个请求设置和保存Cookie信息
         */
        public Interceptor[] interceptors() {
            if (interceptors == null) {
                String interceptorsStr = XTools.cfgDef(XHttpTools.CFG_INTERCEPTORS, XHttpTools.CFG_INTERCEPTORS_DEFAULT);
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
}
