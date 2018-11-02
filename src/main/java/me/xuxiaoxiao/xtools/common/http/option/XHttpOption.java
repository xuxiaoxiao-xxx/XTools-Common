package me.xuxiaoxiao.xtools.common.http.option;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import me.xuxiaoxiao.xtools.common.http.interceptior.XHttpInterceptor;

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
     * http拦截器
     */
    public final XHttpInterceptor[] interceptors = interceptors();

    /**
     * 新建一个配置对象，指定10秒连接超时、10秒读取超时
     */
    public XHttpOption() {
        this(Integer.valueOf(XTools.confDef(XHttpTools.CONF_CONNECT_TIMEOUT, XHttpTools.CONF_CONNECT_TIMEOUT_DEFAULT)), Integer.valueOf(XTools.confDef(XHttpTools.CONF_READ_TIMEOUT, XHttpTools.CONF_READ_TIMEOUT_DEFAULT)));
    }

    /**
     * 新建一个配置对象，并指定连接超时、读取超时
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
                        XTools.logW("KeyManager:%s初始化失败", str);
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
                        XTools.logW("TrustManager:%s初始化失败", str);
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
                XTools.logW("SecureRandom初始化失败");
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
        try {
            return (HostnameVerifier) Class.forName(XTools.confDef(XHttpTools.CONF_HOSTNAME_VERIFIER, XHttpTools.CONF_HOSTNAME_VERIFIER_DEFAULT)).newInstance();
        } catch (Exception e) {
            XTools.logW("HostnameVerifier初始化失败");
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
            return new XCookieManager();
        }
    }

    public XHttpInterceptor[] interceptors() {
        XHttpInterceptor[] interceptors = null;
        String interceptorsStr = XTools.confDef(XHttpTools.CONF_INTERCEPTORS, XHttpTools.CONF_INTERCEPTORS_DEFAULT);
        if (!XTools.strEmpty(interceptorsStr)) {
            List<XHttpInterceptor> interceptorList = new LinkedList<>();
            for (String str : interceptorsStr.split(",")) {
                try {
                    interceptorList.add((XHttpInterceptor) Class.forName(str.trim()).newInstance());
                } catch (Exception e) {
                    XTools.logW("XHttpInterceptor:%s初始化失败", str);
                    e.printStackTrace();
                }
            }
            interceptors = interceptorList.toArray(new XHttpInterceptor[0]);
        }
        return interceptors;
    }

    public static class XHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

    public static class XCookieManager extends CookieManager {

        public XCookieManager() {
            super(null, CookiePolicy.ACCEPT_ALL);
        }
    }
}

