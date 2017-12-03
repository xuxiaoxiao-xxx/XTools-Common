package me.xuxiaoxiao.xtools.common.http;

import javax.net.ssl.*;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求的配置类
 */
public class XOption {
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
     * 新建一个配置对象，指定30秒连接超时、30秒读取超时
     */
    public XOption() {
        this(30 * 1000, 30 * 1000);
    }

    /**
     * 新建一个配置对象，并指定连接超时、读取超时
     *
     * @param connectTimeout 　指定的连接超时时间
     * @param readTimeout    　指定的读取超时时间
     */
    public XOption(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * 是否自动重定向
     *
     * @return 是否自动重定向
     */
    public boolean followRedirect() {
        return false;
    }

    /**
     * chunk块大小
     *
     * @return chunk块大小
     */
    public int chunkLength() {
        return 256 * 1024;
    }

    /**
     * 获取SSL上下文
     *
     * @return 默认不进行证书验证
     */
    public SSLContext sslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取主机名验证器
     *
     * @return 默认不进行主机名验证
     */
    public HostnameVerifier hostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
    }

    /**
     * cookie管理器
     *
     * @return 默认使用内存cookie管理器
     */
    public CookieManager cookieManager() {
        return new CookieManager();
    }

    /**
     * 连接之前对http连接进行配置
     *
     * @param connection http连接
     */
    protected void connectionSetting(HttpURLConnection connection) {
        //根据请求选项进行连接配置
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setInstanceFollowRedirects(followRedirect);
        //设置cookie
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接之后对HTTP连接进行解析
     *
     * @param connection HTTP连接
     */
    protected void connectionParsing(HttpURLConnection connection) {
        //解析cookie
        try {
            cookieManager.put(connection.getURL().toURI(), connection.getHeaderFields());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

