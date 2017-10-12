package me.xuxiaoxiao.xtools;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import javax.net.ssl.*;
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
 * 常用的基本的关于HTTP的函数的集合
 */
public final class XHttpTools {
    /**
     * 默认的请求配置
     */
    public static final XOption DEFAULT_OPTION = new XOption();

    private XHttpTools() {
    }

    /**
     * 使用给定的请求选项进行HTTP请求
     *
     * @param option 请求的选项
     * @param url    请求的url
     * @param body   请求的请求体，RequestMethod=(body == null ? "GET" : "POST")
     * @return 请求的响应体
     */
    @NotNull
    public static XResp request(XOption option, XUrl url, @Nullable XBody body) {
        try {
            HttpURLConnection connection = connect(option, url.build(option.charset));
            option.connectionSetting(connection);
            connection.setRequestMethod(body == null ? "GET" : "POST");
            if (body != null) {
                // 请求方法为POST
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                body.chunkedMode(connection);
                body.contentType(connection, option);
                body.contentLength(connection, option);
                try (DataOutputStream dOutStream = new DataOutputStream(connection.getOutputStream())) {
                    body.contentWrite(dOutStream, option);
                }
            }
            if (option.working()) {
                // 请求没有被停止，获取输入流，返回请求结果
                InputStream inStream = connection.getInputStream();
                option.connectionParsing(connection);
                return new XResp(connection, inStream, option);
            } else {
                // 请求已经被停止，返回空的请求结果
                return new XResp(connection, null, option);
            }
        } catch (Exception e) {
            // 请求异常结束，返回空的请求结果
            e.printStackTrace();
            return new XResp(null, null, option);
        }
    }

    /**
     * 根据请求的url获取请求的连接
     *
     * @param option 请求配置
     * @param url    请求的地址
     * @return 请求的连接
     * @throws Exception 当url不属于HTTP协议或HTTPS协议时抛出异常
     */
    private static HttpURLConnection connect(XOption option, String url) throws Exception {
        if (url.toLowerCase().startsWith("http://")) {
            return (HttpURLConnection) new URL(url).openConnection();
        } else if (url.toLowerCase().startsWith("https://")) {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, option.trustManagers, new SecureRandom());
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier(option.hostnameVerifier);
            return connection;
        } else {
            throw new Exception("XHttpTools仅支持HTTP协议和HTTPS协议");
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
     */
    public static class XOption {
        public static final String MINUS = "--";
        public static final String CRLF = "\r\n";

        /**
         * 交换数据的编码方式
         */
        public final String charset;
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
         * 证书管理器
         */
        public final TrustManager[] trustManagers = trustManagers();
        /**
         * 主机名验证器
         */
        public final HostnameVerifier hostnameVerifier = hostnameVerifier();

        /**
         * 新建一个配置对象，并指定UTF-8编码格式、30秒连接超时、30秒读取超时
         */
        public XOption() {
            this("UTF-8", 30 * 1000, 30 * 1000);
        }

        /**
         * 新建一个配置对象，并指定编码格式、连接超时、读取超时
         *
         * @param charset        　指定的编码格式
         * @param connectTimeout 　指定的连接超时时间
         * @param readTimeout    　指定的读取超时时间
         */
        public XOption(@NotNull String charset, int connectTimeout, int readTimeout) {
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
            return new TrustManager[]{new X509TrustManager() {
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
            }};
        }

        public HostnameVerifier hostnameVerifier() {
            return (s, sslSession) -> true;
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
    }

    /**
     * 请求的url，
     * 使用静态的base方法传入基础的url获取XUrl实例，
     * 通过调用param(String key, Object value);方法添加参数，
     */
    public static class XUrl {
        // 基础的url地址，一般情况下是url中的?之前的部分
        private final String base;
        // url中的参数，参数的键和值都会进行URL编码
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
            if (XTools.strEmpty(base)) {
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
        public final XUrl param(String key, Object value) {
            if (XTools.strEmpty(key)) {
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
     * POST请求的请求体，
     * 使用静态的type方法传入请求的Content-Type来获取XBody实例，
     * 通过调用param(String value);或param(String key, Object value);方法添加参数，
     */
    public static class XBody {
        public static final String URLENCODED = "application/x-www-form-urlencoded";
        public static final String MULTIPART = "multipart/form-data";
        public static final String JSON = "application/json";
        public static final String XML = "text/xml";

        // 请求的Content-Type
        private final String type;
        // POST的参数
        private final HashMap<String, Object> params;
        // 如果请求体的Content-Type是multipart/form-data，该成员是分割线
        private final String multipartBoundary = XTools.md5(XHttpTools.class.getSimpleName() + System.currentTimeMillis()) + (int) (Math.random() * 10000);

        private XBody(String type) {
            this.type = type;
            this.params = new HashMap<>();
        }

        /**
         * 传入请求体的内容类类型获取XBody实例
         *
         * @param type 请求体的内容类类型，支持4个基础的类型，urlencoded,multipart,json,xml
         * @return XBody实例
         * @see #URLENCODED
         * @see #MULTIPART
         * @see #JSON
         * @see #XML
         */
        public static XBody type(String type) {
            if (XTools.strBlank(type)) {
                throw new RuntimeException("请求体的内容类型不能为空");
            } else {
                return new XBody(type);
            }
        }

        /**
         * 为json或xml类型的XBody添加参数
         *
         * @param value json或xml格式的字符串
         * @return XBody实例
         */
        public final XBody param(String value) {
            if (type.equals(JSON) || type.equals(XML)) {
                this.params.put(type, value);
                return this;
            } else {
                throw new RuntimeException("XBody param(String value);函数只能为Content-Type为JSON或XML的请求体添加参数");
            }
        }

        /**
         * 为urlencoded类型或multipart类型的XBody添加参数
         *
         * @param key   参数的键名
         * @param value 参数的值
         * @return XBody实例
         */
        public final XBody param(String key, Object value) {
            if (XTools.strBlank(key)) {
                throw new RuntimeException("请求体参数的键名不能为空");
            } else if (type.equals(URLENCODED) || type.equals(MULTIPART)) {
                this.params.put(key, value);
                return this;
            } else {
                throw new RuntimeException("XBody param(String key, Object value);函数只能为Content-Type为URLENCODED或MULTIPART的请求体添加参数");
            }
        }

        /**
         * 计算并设置POST请求的ChunkedStreamingMode
         *
         * @param connection 请求连接
         */
        public void chunkedMode(HttpURLConnection connection) {
            for (String key : params.keySet()) {
                if (params.get(key) instanceof File && type.equals(MULTIPART)) {
                    connection.setChunkedStreamingMode(1024 * 1024);
                }
            }
        }

        /**
         * 计算并设置POST请求的Content-Type
         *
         * @param connection 请求的连接
         * @param option     请求配置
         * @throws IOException 当编码格式不支持时抛出异常
         */
        public void contentType(HttpURLConnection connection, XOption option) throws IOException {
            switch (type) {
                case URLENCODED:
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + option.charset);
                    break;
                case MULTIPART:
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + multipartBoundary);
                    break;
                case JSON:
                    connection.setRequestProperty("Content-Type", "application/json;charset=" + option.charset);
                    break;
                case XML:
                    connection.setRequestProperty("Content-Type", "text/xml");
                    break;
            }
        }

        /**
         * 计算并设置POST请求的Content-Length
         *
         * @param connection 请求的连接
         * @param option     请求配置
         * @throws IOException 当编码格式不支持时抛出异常
         */
        public void contentLength(HttpURLConnection connection, XOption option) throws IOException {
            switch (type) {
                case URLENCODED:
                    connection.setRequestProperty("Content-Length", String.valueOf(urlJoin(params, option.charset).getBytes(option.charset).length));
                    break;
                case MULTIPART:
                    long contentLength = 0;
                    for (String bodyKey : params.keySet()) {
                        Object bodyParam = params.get(bodyKey);
                        if (bodyParam instanceof File) {
                            contentLength += (XOption.MINUS + multipartBoundary + XOption.CRLF).getBytes(option.charset).length;
                            contentLength += String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", bodyKey, ((File) bodyParam).getName(), XOption.CRLF).getBytes(option.charset).length;
                            contentLength += String.format("Content-Type: %s%s", URLConnection.getFileNameMap().getContentTypeFor(((File) bodyParam).getAbsolutePath()), XOption.CRLF).getBytes(option.charset).length;
                            contentLength += XOption.CRLF.getBytes(option.charset).length;
                            contentLength += ((File) bodyParam).length();
                            contentLength += XOption.CRLF.getBytes(option.charset).length;
                        } else {
                            contentLength += (XOption.MINUS + multipartBoundary + XOption.CRLF).getBytes(option.charset).length;
                            contentLength += String.format("Content-Disposition: form-data; name=\"%s\"%s", bodyKey, XOption.CRLF).getBytes(option.charset).length;
                            contentLength += XOption.CRLF.getBytes(option.charset).length;
                            contentLength += bodyParam.toString().getBytes(option.charset).length;
                            contentLength += XOption.CRLF.getBytes(option.charset).length;
                        }
                    }
                    contentLength = contentLength + (XOption.MINUS + multipartBoundary + XOption.MINUS + XOption.CRLF).getBytes(option.charset).length;
                    connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
                    break;
                case JSON:
                    connection.setRequestProperty("Content-Length", String.valueOf(String.valueOf(params.get(type)).getBytes(option.charset).length));
                    break;
                case XML:
                    connection.setRequestProperty("Content-Length", String.valueOf(String.valueOf(params.get(type)).getBytes(option.charset).length));
                    break;
            }
        }

        /**
         * 将POST请求的请求体写出到连接中
         *
         * @param dOutStream 请求的输出流
         * @param option     请求的配置
         * @throws IOException 写出请求体时可能会抛出IO异常
         */
        private void contentWrite(DataOutputStream dOutStream, XOption option) throws IOException {
            switch (type) {
                case URLENCODED:
                    dOutStream.write(urlJoin(params, option.charset).getBytes(option.charset));
                    break;
                case MULTIPART:
                    for (String key : params.keySet()) {
                        Object value = params.get(key);
                        if (value instanceof File) {
                            dOutStream.write((XOption.MINUS + multipartBoundary + XOption.CRLF).getBytes(option.charset));
                            dOutStream.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, ((File) value).getName(), XOption.CRLF).getBytes(option.charset));
                            dOutStream.write(String.format("Content-Type: %s%s", URLConnection.getFileNameMap().getContentTypeFor(((File) value).getAbsolutePath()), XOption.CRLF).getBytes(option.charset));
                            dOutStream.write(XOption.CRLF.getBytes(option.charset));
                            try (FileInputStream fInStream = new FileInputStream((File) value)) {
                                int count;
                                byte[] buffer = new byte[1024];
                                while (option.working() && (count = fInStream.read(buffer)) > 0) {
                                    dOutStream.write(buffer, 0, count);
                                }
                            }
                            dOutStream.write(XOption.CRLF.getBytes(option.charset));
                        } else {
                            dOutStream.write((XOption.MINUS + multipartBoundary + XOption.CRLF).getBytes(option.charset));
                            dOutStream.write(String.format("Content-Disposition: form-data; name=\"%s\"%s", key, XOption.CRLF).getBytes(option.charset));
                            dOutStream.write(XOption.CRLF.getBytes(option.charset));
                            dOutStream.write(String.valueOf(value).getBytes(option.charset));
                            dOutStream.write(XOption.CRLF.getBytes(option.charset));
                        }
                    }
                    dOutStream.write((XOption.MINUS + multipartBoundary + XOption.MINUS + XOption.CRLF).getBytes(option.charset));
                    break;
                case JSON:
                case XML:
                    dOutStream.write(String.valueOf(params.get(type)).getBytes(option.charset));
                    break;
            }
        }
    }

    /**
     * 请求的结果类，可以使用string()方法或file(String path)方法，将获取到的结果保存成字符串或者文件
     */
    public static class XResp {
        private final HttpURLConnection connection;
        private final InputStream inStream;
        private final XOption config;

        private XResp(HttpURLConnection connection, InputStream inStream, XOption config) {
            this.connection = connection;
            this.inStream = inStream;
            this.config = config;
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
         * 获取返回的输入流
         *
         * @return 连接的输入流，记得使用XResp实例的close()方法关闭输入流和连接
         */
        @Nullable
        public final InputStream inStream() {
            return this.inStream;
        }

        /**
         * 将连接返回的输入流中的数据转化成字符串
         *
         * @return 转化后的字符串
         */
        @Nullable
        public final String string() {
            try {
                return XTools.streamToStr(inStream(), config.charset);
            } catch (Exception e) {
                e.printStackTrace();
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
                return XTools.streamToFile(inStream(), path);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                close();
            }
        }
    }
}
