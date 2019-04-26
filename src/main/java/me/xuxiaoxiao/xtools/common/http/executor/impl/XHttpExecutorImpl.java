package me.xuxiaoxiao.xtools.common.http.executor.impl;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.config.XConfigTools;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.*;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * http执行器的默认实现类
 */
public class XHttpExecutorImpl implements XHttpExecutor {
    public static final String CFG_CONNECT_TIMEOUT = XTools.CFG_PREFIX + "http.connectTimeout";
    public static final String CFG_CONNECT_TIMEOUT_DEFAULT = "20000";
    public static final String CFG_READ_TIMEOUT = XTools.CFG_PREFIX + "http.readTimeout";
    public static final String CFG_READ_TIMEOUT_DEFAULT = "60000";
    public static final String CFG_FOLLOW_REDIRECT = XTools.CFG_PREFIX + "http.followRedirect";
    public static final String CFG_FOLLOW_REDIRECT_DEFAULT = "false";
    public static final String CFG_CHUNK_LENGTH = XTools.CFG_PREFIX + "http.chunkLength";
    public static final String CFG_CHUNK_LENGTH_DEFAULT = "262144";

    public static final String CFG_COOKIE_MANAGER = XTools.CFG_PREFIX + "http.cookieManager";
    public static final String CFG_COOKIE_MANAGER_DEFAULT = XHttpExecutorImpl.XCookieManager.class.getName();

    public static final String CFG_INTERCEPTORS = XTools.CFG_PREFIX + "http.interceptors";
    public static final String CFG_INTERCEPTORS_DEFAULT = "";

    public static final String CFG_HOSTNAME_VERIFIER = XTools.CFG_PREFIX + "http.hostnameVerifier";
    public static final String CFG_HOSTNAME_VERIFIER_DEFAULT = XHttpExecutorImpl.XHostnameVerifier.class.getName();

    public static final String CFG_SSL_ALGORITHM = XTools.CFG_PREFIX + "http.ssl.algorithm";
    public static final String CFG_SSL_ALGORITHM_DEFAULT = "TLS";
    public static final String CFG_SSL_KEY_MANAGERS = XTools.CFG_PREFIX + "http.ssl.keyManagers";
    public static final String CFG_SSL_KEY_MANAGERS_DEFAULT = "";
    public static final String CFG_SSL_TRUST_MANAGERS = XTools.CFG_PREFIX + "http.ssl.trustManagers";
    public static final String CFG_SSL_TRUST_MANAGERS_DEFAULT = "";
    public static final String CFG_SSL_SECURE_RANDOM = XTools.CFG_PREFIX + "http.ssl.secureRandom";
    public static final String CFG_SSL_SECURE_RANDOM_DEFAULT = "";

    private int connectTimeout;
    private int readTimeout;
    private int chunkLength;
    private boolean followRedirect;
    private CookieManager cookieManager;
    private Interceptor[] interceptors;
    private HostnameVerifier hostnameVerifier;
    private SSLContext sslContext;

    static {
        XConfigTools.X_CONFIGS.cfgDef(CFG_CONNECT_TIMEOUT, CFG_CONNECT_TIMEOUT_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_READ_TIMEOUT, CFG_READ_TIMEOUT_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_FOLLOW_REDIRECT, CFG_FOLLOW_REDIRECT_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_CHUNK_LENGTH, CFG_CHUNK_LENGTH_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_COOKIE_MANAGER, CFG_COOKIE_MANAGER_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_INTERCEPTORS, CFG_INTERCEPTORS_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_HOSTNAME_VERIFIER, CFG_HOSTNAME_VERIFIER_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_SSL_ALGORITHM, CFG_SSL_ALGORITHM_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_SSL_KEY_MANAGERS, CFG_SSL_KEY_MANAGERS_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_SSL_TRUST_MANAGERS, CFG_SSL_TRUST_MANAGERS_DEFAULT);
        XConfigTools.X_CONFIGS.cfgDef(CFG_SSL_SECURE_RANDOM, CFG_SSL_SECURE_RANDOM_DEFAULT);
    }

    public XHttpExecutorImpl() {
        defaultConnectTimeout();
        defaultReadTimeout();
        defaultChunkLength();
        defaultFollowRedirect();
        defaultCookieManager();
        defaultHostnameVerifier();
        defaultInterceptors();
        defaultSSL();
    }

    public void defaultConnectTimeout() {
        this.connectTimeout = Integer.valueOf(XTools.cfgGet(CFG_CONNECT_TIMEOUT));
    }

    public void defaultReadTimeout() {
        this.readTimeout = Integer.valueOf(XTools.cfgGet(CFG_READ_TIMEOUT));
    }

    public void defaultChunkLength() {
        this.chunkLength = Integer.valueOf(XTools.cfgGet(CFG_CHUNK_LENGTH));
    }

    public void defaultFollowRedirect() {
        this.followRedirect = Boolean.valueOf(XTools.cfgGet(CFG_FOLLOW_REDIRECT));
    }

    private void defaultCookieManager() {
        this.cookieManager = XConfigTools.supply(XTools.cfgGet(CFG_COOKIE_MANAGER));
        CookieHandler.setDefault(this.cookieManager);
    }

    private void defaultHostnameVerifier() {
        String hostnameVerifierStr = XTools.cfgGet(CFG_HOSTNAME_VERIFIER);
        try {
            hostnameVerifier = XConfigTools.supply(hostnameVerifierStr);
        } catch (Exception e) {
            e.printStackTrace();
            XTools.logW("HostnameVerifier:%s 初始化失败, 将使用默认的HostnameVerifier", hostnameVerifierStr);
            hostnameVerifier = new XHostnameVerifier();
        }
    }

    private void defaultInterceptors() {
        String interceptorsStr = XTools.cfgGet(CFG_INTERCEPTORS);
        if (!XTools.strEmpty(interceptorsStr)) {
            List<XHttpExecutor.Interceptor> interceptorList = new LinkedList<>();
            for (String str : interceptorsStr.split(",")) {
                try {
                    interceptorList.add((XHttpExecutor.Interceptor) XConfigTools.supply(str));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setInterceptors(interceptorList.toArray(new XHttpExecutor.Interceptor[0]));
        } else {
            setInterceptors();
        }
    }

    private void defaultSSL() {
        try {
            this.sslContext = SSLContext.getInstance(XTools.cfgGet(CFG_SSL_ALGORITHM));
            KeyManager[] keyManagers = null;
            String keyManagersStr = XTools.cfgGet(CFG_SSL_KEY_MANAGERS);
            if (!XTools.strEmpty(keyManagersStr)) {
                List<KeyManager> keyManagerList = new LinkedList<>();
                for (String str : keyManagersStr.split(",")) {
                    try {
                        keyManagerList.add((KeyManager) XConfigTools.supply(str));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                keyManagers = keyManagerList.toArray(new KeyManager[0]);
            }

            TrustManager[] trustManagers = null;
            String trustManagersStr = XTools.cfgGet(CFG_SSL_TRUST_MANAGERS);
            if (!XTools.strEmpty(trustManagersStr)) {
                List<TrustManager> trustManagerList = new LinkedList<>();
                for (String str : trustManagersStr.split(",")) {
                    try {
                        trustManagerList.add((TrustManager) XConfigTools.supply(str));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                trustManagers = trustManagerList.toArray(new TrustManager[0]);
            }

            SecureRandom secureRandom = null;
            String secureRandomStr = XTools.cfgGet(CFG_SSL_SECURE_RANDOM);
            try {
                if (!XTools.strEmpty(secureRandomStr)) {
                    secureRandom = XConfigTools.supply(secureRandomStr);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sslContext.init(keyManagers, trustManagers, secureRandom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    @Override
    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    @Override
    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }

    @Override
    public int getReadTimeout() {
        return this.readTimeout;
    }

    @Override
    public void addCookie(URI uri, HttpCookie cookie) {
        this.cookieManager.getCookieStore().add(uri, cookie);
    }

    @Override
    public List<HttpCookie> getCookies(URI uri) {
        return this.cookieManager.getCookieStore().get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return this.cookieManager.getCookieStore().getCookies();
    }

    @Override
    public void rmvCookies(URI uri, HttpCookie cookie) {
        this.cookieManager.getCookieStore().remove(uri, cookie);
    }

    @Override
    public void rmvCookies() {
        this.cookieManager.getCookieStore().removeAll();
    }

    public void setChunkLength(int chunkLength) {
        this.chunkLength = chunkLength;
    }

    public int getChunkLength() {
        return this.chunkLength;
    }

    public void setFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
    }

    public boolean getFollowRedirect() {
        return this.followRedirect;
    }

    @Override
    public void setInterceptors(Interceptor... interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public Interceptor[] getInterceptors() {
        return this.interceptors;
    }

    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public SSLContext getSSLContext() {
        return this.sslContext;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    protected HttpURLConnection connect(Request request) throws Exception {
        String url = request.getUrl();
        if (url.toLowerCase().startsWith("http://")) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            //根据请求选项进行连接配置
            connection.setConnectTimeout(getConnectTimeout());
            connection.setReadTimeout(getReadTimeout());
            connection.setInstanceFollowRedirects(getFollowRedirect());
            return connection;
        } else if (url.toLowerCase().startsWith("https://")) {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setSSLSocketFactory(getSSLContext().getSocketFactory());
            connection.setHostnameVerifier(getHostnameVerifier());
            //根据请求选项进行连接配置
            connection.setConnectTimeout(getConnectTimeout());
            connection.setReadTimeout(getReadTimeout());
            connection.setInstanceFollowRedirects(getFollowRedirect());
            return connection;
        } else {
            throw new IllegalArgumentException("XHttpExecutorImpl仅支持HTTP协议和HTTPS协议");
        }
    }

    @Override
    public Response execute(Request request) throws Exception {
        HttpURLConnection connection = connect(request);
        //设置请求方法
        connection.setRequestMethod(request.getMethod());

        List<KeyValue> headers = request.getHeaders();
        if (headers != null) {
            //设置请求头
            for (KeyValue keyValue : headers) {
                connection.addRequestProperty(keyValue.key, String.valueOf(keyValue.value));
            }
        }

        if (cookieManager != null) {
            //添加cookie
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
        }

        //如果为POST或PUT方法则输出请求体
        if (XRequest.METHOD_POST.equals(request.getMethod()) || XRequest.METHOD_PUT.equals(request.getMethod())) {
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            XHttpExecutor.Content content = request.getContent();
            if (content != null) {
                if (content.contentLength() < 0) {
                    connection.setChunkedStreamingMode(getChunkLength());
                }
                try (DataOutputStream dOutStream = new DataOutputStream(connection.getOutputStream())) {
                    content.contentWrite(dOutStream);
                }
            }
        }

        //获取输入流
        InputStream inStream = connection.getInputStream();
        if (cookieManager != null) {
            //读取cookie
            cookieManager.put(connection.getURL().toURI(), connection.getHeaderFields());
        }
        return new XResponse(connection, inStream);
    }

    /**
     * 默认的Cookie管理器，接收所有Cookie信息并存储在内存中
     */
    public static class XCookieManager extends CookieManager {

        public XCookieManager() {
            super(null, CookiePolicy.ACCEPT_ALL);
        }
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
}
