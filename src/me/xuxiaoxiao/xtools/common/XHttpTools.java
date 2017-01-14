package me.xuxiaoxiao.xtools.common;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求工具，支持HTTP和HTTPS
 *
 * @author XXX
 */
public final class XHttpTools {
    /**
     * 默认的请求配置
     */
    public static final XConfig DEFAULT_CONFIG = new XConfig();

    private XHttpTools() {
    }

    /**
     * 使用默认的请求配置进行HTTP请求
     *
     * @param url  请求的url
     * @param body 请求的请求体，RequestMethod=(body == null ? "GET" : "POST")
     * @return 请求的响应体
     */
    @NotNull
    public static XResp request(XUrl url, @Nullable XBody body) {
        return request(DEFAULT_CONFIG, url, body);
    }

    /**
     * 使用给定的请求配置进行HTTP请求
     *
     * @param config 请求的配置
     * @param url    请求的url
     * @param body   请求的请求体，RequestMethod=(body == null ? "GET" : "POST")
     * @return 请求的响应体
     */
    @NotNull
    public static XResp request(XConfig config, XUrl url, @Nullable XBody body) {
        try {
            HttpURLConnection connection = connect(config, url.build(config.charset));
            config.connectionSetting(connection);
            connection.setRequestMethod(body == null ? "GET" : "POST");
            if (body != null) {
                // 请求方法为POST，将请求体写到输出流中
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                XHttpTools.contentLength(config, body, connection);
                XHttpTools.contentWrite(config, body, connection);
            }
            if (config.working()) {
                // 请求没有被停止，获取输入流，返回请求结果
                InputStream inStream = connection.getInputStream();
                config.connectionParsing(connection);
                return new XResp(config, connection, inStream);
            } else {
                // 请求已经被停止，返回空的请求结果
                return new XResp(config, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 请求异常结束，返回空的请求结果
            return new XResp(config, null, null);
        }
    }

    /**
     * 根据请求的url获取请求的连接
     *
     * @param config 请求配置
     * @param url    请求的地址
     * @return 请求的连接
     * @throws Exception 当url不属于HTTP协议或HTTPS协议时抛出异常
     */
    private static HttpURLConnection connect(XConfig config, String url) throws Exception {
        if (url.toLowerCase().startsWith("http://")) {
            return (HttpURLConnection) new URL(url).openConnection();
        } else if (url.toLowerCase().startsWith("https://")) {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, config.trustManagers, new SecureRandom());
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            return connection;
        } else {
            throw new Exception("XHttpTools仅支持HTTP协议和HTTPS协议");
        }
    }

    /**
     * 计算并设置POST请求的Content-Type和Content-Length
     *
     * @param config     请求配置
     * @param body       请求体
     * @param connection 请求的连接
     * @throws UnsupportedEncodingException 当编码格式不支持时抛出异常
     */
    private static void contentLength(XConfig config, XBody body, HttpURLConnection connection) throws UnsupportedEncodingException {
        long contentLength = 0;
        switch (body.type) {
            case JSON:
                connection.setRequestProperty("Content-Type", "application/json;charset=" + config.charset);
                contentLength = String.valueOf(body.params.get(body.type.name())).getBytes(config.charset).length;
                break;
            case XML:
                connection.setRequestProperty("Content-Type", "text/xml");
                contentLength = String.valueOf(body.params.get(body.type.name())).getBytes(config.charset).length;
                break;
            case URLENCODED:
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + config.charset);
                contentLength = urlJoin(body.params, config.charset).getBytes(config.charset).length;
                break;
            default:
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + body.multipartBoundary);
                connection.setChunkedStreamingMode(256 * 1024);
                for (String bodyKey : body.params.keySet()) {
                    Object bodyParam = body.params.get(bodyKey);
                    if (bodyParam instanceof File) {
                        contentLength += (XConfig.MINUS + body.multipartBoundary + XConfig.CRLF).getBytes(config.charset).length;
                        contentLength += String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", bodyKey, ((File) bodyParam).getName(), XConfig.CRLF).getBytes(config.charset).length;
                        contentLength += String.format("Content-Type: %s%s", URLConnection.getFileNameMap().getContentTypeFor(((File) bodyParam).getAbsolutePath()), XConfig.CRLF).getBytes(config.charset).length;
                        contentLength += XConfig.CRLF.getBytes(config.charset).length;
                        contentLength += ((File) bodyParam).length();
                        contentLength += XConfig.CRLF.getBytes(config.charset).length;
                    } else {
                        contentLength += (XConfig.MINUS + body.multipartBoundary + XConfig.CRLF).getBytes(config.charset).length;
                        contentLength += String.format("Content-Disposition: form-data; name=\"%s\"%s", bodyKey, XConfig.CRLF).getBytes(config.charset).length;
                        contentLength += XConfig.CRLF.getBytes(config.charset).length;
                        contentLength += bodyParam.toString().getBytes(config.charset).length;
                        contentLength += XConfig.CRLF.getBytes(config.charset).length;
                    }
                }
                contentLength = contentLength + (XConfig.MINUS + body.multipartBoundary + XConfig.MINUS + XConfig.CRLF).getBytes(config.charset).length;
                break;
        }
        connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
    }

    /**
     * 将POST请求的请求体写到输出流中
     *
     * @param config     请求的配置
     * @param body       请求的请求体
     * @param connection 请求的连接
     * @throws IOException 写出请求体时可能会抛出IO异常
     */
    private static void contentWrite(XConfig config, XBody body, HttpURLConnection connection) throws IOException {
        try (DataOutputStream dOutStream = new DataOutputStream(connection.getOutputStream())) {
            switch (body.type) {
                case JSON:
                case XML:
                    dOutStream.write(String.valueOf(body.params.get(body.type.name())).getBytes(config.charset));
                    break;
                case URLENCODED:
                    dOutStream.write(urlJoin(body.params, config.charset).getBytes(config.charset));
                    break;
                case MULTIPART:
                    for (String key : body.params.keySet()) {
                        Object value = body.params.get(key);
                        if (value instanceof File) {
                            dOutStream.write((XConfig.MINUS + body.multipartBoundary + XConfig.CRLF).getBytes(config.charset));
                            dOutStream.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, ((File) value).getName(), XConfig.CRLF).getBytes(config.charset));
                            dOutStream.write(String.format("Content-Type: %s%s", URLConnection.getFileNameMap().getContentTypeFor(((File) value).getAbsolutePath()), XConfig.CRLF).getBytes(config.charset));
                            dOutStream.write(XConfig.CRLF.getBytes(config.charset));
                            try (FileInputStream fInStream = new FileInputStream((File) value)) {
                                int count;
                                byte[] buffer = new byte[1024];
                                while (config.working() && (count = fInStream.read(buffer)) > 0) {
                                    dOutStream.write(buffer, 0, count);
                                }
                            }
                            dOutStream.write(XConfig.CRLF.getBytes(config.charset));
                        } else {
                            dOutStream.write((XConfig.MINUS + body.multipartBoundary + XConfig.CRLF).getBytes(config.charset));
                            dOutStream.write(String.format("Content-Disposition: form-data; name=\"%s\"%s", key, XConfig.CRLF).getBytes(config.charset));
                            dOutStream.write(XConfig.CRLF.getBytes(config.charset));
                            dOutStream.write(String.valueOf(value).getBytes(config.charset));
                            dOutStream.write(XConfig.CRLF.getBytes(config.charset));
                        }
                    }
                    dOutStream.write((XConfig.MINUS + body.multipartBoundary + XConfig.MINUS + XConfig.CRLF).getBytes(config.charset));
                    break;
            }
            dOutStream.flush();
        }
    }

    /**
     * 将键值对集合经过URL编码后用固定的字符串连接起来
     *
     * @param urlMap  键值对集合
     * @param charset URL的编码类型
     * @return 连接后的字符串
     * @throws UnsupportedEncodingException 给定的编码格式不支持时抛出异常
     */
    private static String urlJoin(Map<?, ?> urlMap, String charset) throws UnsupportedEncodingException {
        StringBuilder sbStr = new StringBuilder();
        for (Object key : urlMap.keySet()) {
            if (sbStr.length() > 0) {
                sbStr.append('&');
            }
            sbStr.append(URLEncoder.encode(String.valueOf(key), charset)).append('=').append(URLEncoder.encode(String.valueOf(urlMap.get(key)), charset));
        }
        return sbStr.toString();
    }

    /**
     * 请求的配置类
     *
     * @author XXX
     */
    public static class XConfig {
        public static final String MINUS = "--";
        public static final String CRLF = "\r\n";

        public final String charset;
        public final int connectTimeout;
        public final int readTimeout;
        public final boolean followRedirect = followRedirect();
        public final TrustManager[] trustManagers = trustManagers();

        /**
         * 新建一个配置对象，并指定UTF-8编码格式、30秒连接超时、30秒读取超时
         */
        public XConfig() {
            this("UTF-8", 30 * 1000, 30 * 1000);
        }

        /**
         * 新建一个配置对象，并指定编码格式、连接超时、读取超时
         *
         * @param charset        　指定的编码格式
         * @param connectTimeout 　指定的连接超时时间
         * @param readTimeout    　指定的读取超时时间
         */
        public XConfig(@NotNull String charset, int connectTimeout, int readTimeout) {
            this.charset = charset;
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
         * 请求的证书管理器
         *
         * @return 证书管理器
         */
        public TrustManager[] trustManagers() {
            return new TrustManager[]{new XTrustManager()};
        }

        /**
         * 请求是否应该继续，返回false将中断该正在进行中的请求
         *
         * @return 请求是否应该继续
         */
        public boolean working() {
            return true;
        }

        /**
         * 请求之前连接的配置
         *
         * @param connection 需要配置的连接
         */
        public void connectionSetting(HttpURLConnection connection) {
            connection.setRequestProperty("Charset", charset);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setInstanceFollowRedirects(followRedirect);
        }

        /**
         * 请求之后连接的解析
         *
         * @param connection 　要解析的连接
         */
        public void connectionParsing(HttpURLConnection connection) {
        }

        public static class XTrustManager implements X509TrustManager {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }
    }

    /**
     * <pre>
     * 请求的url
     * 使用静态的base方法传入基础的url获取XUrl实例
     * 通过调用param(String key, Object value);方法添加参数
     * </pre>
     *
     * @author XXX
     */
    public static final class XUrl {
        private final String base;
        private final HashMap<String, String> params;

        private XUrl(String base) {
            this.base = base;
            this.params = new HashMap<>();
        }

        /**
         * 传入基础的url获取XUrl实例
         *
         * @param base 基础的url地址，一般情况下是url中的?之前的部分
         * @return XUrl实例
         */
        public static XUrl base(String base) {
            if (XStrTools.isEmpty(base)) {
                throw new RuntimeException("基础URL不能为空");
            } else {
                return new XUrl(base);
            }
        }

        /**
         * 添加url中的参数，参数的键和值都会进行URL编码
         *
         * @param key   参数的键
         * @param value 参数的值
         * @return XUrl实例
         */
        public XUrl param(String key, Object value) {
            if (XStrTools.isEmpty(key)) {
                throw new RuntimeException("URL参数键名不能为空");
            } else {
                this.params.put(key, String.valueOf(value));
                return this;
            }
        }

        /**
         * 构造最终的url
         *
         * @param charset 编码格式
         * @return 构造后的url，一般情况下为base?key1=value1&key2=value2
         * @throws UnsupportedEncodingException 当不支持给定的编码格式时抛出异常
         */
        private String build(String charset) throws UnsupportedEncodingException {
            if (params.isEmpty()) {
                return base;
            } else if (base.indexOf('?') < 0) {
                return base + '?' + urlJoin(params, charset);
            } else {
                return base + '&' + urlJoin(params, charset);
            }
        }

        @Override
        public String toString() {
            try {
                return this.build("utf-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
    }

    /**
     * <pre>
     * 请求的请求体
     * 如果一个请求的请求体参数不是null，则认为这个请求是一个POST请求
     * 使用静态的type方法传入请求的的内容类型来获取XBody实例
     * 通过调用param(String value);或param(String key, Object value);方法添加参数
     * </pre>
     *
     * @author XXX
     */
    public static final class XBody {
        private final Type type;
        private final HashMap<String, Object> params;
        private final String multipartBoundary = XDigestTools.digest(XDigestTools.Algo.MD5, XHttpTools.class.getSimpleName() + System.currentTimeMillis()) + (int) (Math.random() * 10000);


        private XBody(Type type) {
            this.type = type;
            this.params = new HashMap<>();
        }

        /**
         * 传入请求体的内容类类型获取XBody实例
         *
         * @param type 请求体的内容类类型，支持4个基础的类型，xml,json,urlencoded,multipart
         * @return XBody实例
         */
        public static XBody type(Type type) {
            if (type == null) {
                throw new RuntimeException("请求体的内容类型不能为空");
            } else {
                return new XBody(type);
            }
        }

        /**
         * 为xml类型和json类型的XBody添加参数
         *
         * @param value xml或json格式的字符串
         * @return XBody实例
         */
        public XBody param(String value) {
            if (type == Type.URLENCODED || type == Type.MULTIPART) {
                throw new RuntimeException("请求体参数必须要有键名");
            } else {
                this.params.put(type.name(), value);
                return this;
            }
        }

        /**
         * 为urlencoded类型或multipart类型的XBody添加参数
         *
         * @param key   参数的键名
         * @param value 参数的值
         * @return XBody实例
         */
        public XBody param(String key, Object value) {
            if (XStrTools.isEmpty(key)) {
                throw new RuntimeException("请求体参数的键名不能为空");
            } else if (type == Type.JSON || type == Type.XML) {
                throw new RuntimeException("请求体参数不能有键名");
            } else {
                this.params.put(key, value);
                return this;
            }
        }

        /**
         * 请求体的类型，支持4种基本的类型，，xml,json,urlencoded,multipart
         */
        public enum Type {
            URLENCODED, MULTIPART, JSON, XML
        }
    }

    /**
     * 请求的结果类，可以使用string()方法或file(String path)方法，将获取到的结果转化成字符串或者文件
     *
     * @author XXX
     */
    public static final class XResp {
        private final XConfig config;
        private final HttpURLConnection connection;
        private final InputStream inStream;

        private XResp(XConfig config, HttpURLConnection connection, InputStream inStream) {
            this.config = config;
            this.connection = connection;
            this.inStream = inStream;
        }

        /**
         * 获取返回的输入流
         *
         * @return 连接的输入流，记得使用XResp实例的close()方法关闭输入流和连接
         */
        @Nullable
        public final InputStream inStream() {
            return this.inStream;
        }

        /**
         * 关闭输入流和连接
         */
        public final void close() {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        /**
         * 将连接返回的输入流中的数据转化成字符串
         *
         * @return 转化后的字符串
         */
        @Nullable
        public final String string() {
            try {
                return XStreamTools.streamToStr(inStream, config.charset);
            } catch (Exception e) {
                return null;
            } finally {
                close();
            }
        }

        /**
         * 将连接返回的输入流中的数据转化成文件
         *
         * @param path 文件存储的路径
         * @return 转化后的文件
         */
        @Nullable
        public final File file(String path) {
            try {
                return XStreamTools.streamToFile(inStream(), path);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                close();
            }
        }
    }
}
