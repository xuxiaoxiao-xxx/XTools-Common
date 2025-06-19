package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.http.impl.XExecutor;
import me.xuxiaoxiao.xtools.common.http.impl.XRequest;
import me.xuxiaoxiao.xtools.common.http.impl.XResponse;

import javax.annotation.Nonnull;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * HTTP工具类
 */
public class XHttpTools {

    private final Config config;

    public XHttpTools(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    /**
     * 使用给定的请求选项进行HTTP请求
     *
     * @param request HTTP请求
     * @return HTTP响应
     */
    @Nonnull
    public XResponse http(@Nonnull XRequest request) {
        try {
            return getConfig().executor.execute(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Config {
        private int connectTimeout;
        private int readTimeout;
        private int chunkLength;
        private boolean followRedirect;
        private CookieManager cookieManager;
        private HostnameVerifier hostnameVerifier;
        private SSLContext sslContext;
        private XExecutor executor;

        public Config() {
            this.connectTimeout = 10000;
            this.readTimeout = 30000;
            this.chunkLength = 262144;
            this.followRedirect = false;
            this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            this.hostnameVerifier = (s, sslSession) -> true;
            try {
                this.sslContext = SSLContext.getInstance("TLS");
                this.sslContext.init(null, new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                }, new SecureRandom());
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            this.executor = new XExecutor(this);
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public int getChunkLength() {
            return chunkLength;
        }

        public void setChunkLength(int chunkLength) {
            this.chunkLength = chunkLength;
        }

        public boolean isFollowRedirect() {
            return followRedirect;
        }

        public void setFollowRedirect(boolean followRedirect) {
            this.followRedirect = followRedirect;
        }

        public CookieManager getCookieManager() {
            return cookieManager;
        }

        public void setCookieManager(CookieManager cookieManager) {
            this.cookieManager = cookieManager;
        }

        public HostnameVerifier getHostnameVerifier() {
            return hostnameVerifier;
        }

        public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
        }

        public SSLContext getSslContext() {
            return sslContext;
        }

        public void setSslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
        }

        public XExecutor getExecutor() {
            return executor;
        }

        public void setExecutor(XExecutor executor) {
            this.executor = executor;
        }
    }
}
