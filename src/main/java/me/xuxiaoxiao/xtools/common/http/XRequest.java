package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.XTools;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * HTTP请求类，记录了HTTP请求的请求方法，请求地址，请求头，请求体
 */
public final class XRequest {
    public static final String MIME_URLENCODED = "application/x-www-form-urlencoded";
    public static final String MIME_MULTIPART = "multipart/form-data";
    public static final String MIME_JSON = "application/json";
    public static final String MIME_XML = "text/xml";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private static final String CHARSET_UTF8 = "utf-8";

    /**
     * 请求方法
     */
    private final String requestMethod;
    /**
     * 请求地址
     */
    private final String requestUrl;
    /**
     * 请求地址参数
     */
    private List<KeyValue> requestQueries;
    /**
     * 请求头
     */
    private List<KeyValue> requestHeaders;
    /**
     * 请求体
     */
    private Content requestContent;

    private XRequest(String method, String url) {
        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            throw new IllegalArgumentException("XHttpTools仅支持HTTP协议和HTTPS协议");
        }
        this.requestMethod = method;
        if (url.indexOf('?') >= 0) {
            this.requestUrl = url.substring(0, url.indexOf('?'));
            try {
                for (String keyValue : url.substring(url.indexOf('?') + 1).split("&")) {
                    int eqIndex = keyValue.indexOf('=');
                    if (eqIndex < 0) {
                        throw new IllegalArgumentException("请求的url格式有误");
                    } else {
                        query(URLDecoder.decode(keyValue.substring(0, eqIndex), CHARSET_UTF8), URLDecoder.decode(keyValue.substring(eqIndex + 1), CHARSET_UTF8));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            this.requestUrl = url;
        }
    }

    /**
     * 新建一个GET请求
     *
     * @param url 请求url
     * @return GET请求
     */
    public static XRequest GET(String url) {
        return new XRequest(METHOD_GET, url);
    }

    /**
     * 新建一个POST请求
     *
     * @param url 请求url
     * @return POST请求
     */
    public static XRequest POST(String url) {
        return new XRequest(METHOD_POST, url);
    }

    /**
     * 将键值对集合经过url编码后用固定的字符串连接起来
     *
     * @return 连接后的字符串
     * @throws UnsupportedEncodingException 给定的编码格式不支持时抛出异常
     */
    private static String kvJoin(List<KeyValue> keyValues) throws UnsupportedEncodingException {
        StringBuilder sbStr = new StringBuilder();
        for (KeyValue keyValue : keyValues) {
            if (sbStr.length() > 0) {
                sbStr.append('&');
            }
            sbStr.append(URLEncoder.encode(keyValue.key, CHARSET_UTF8)).append('=').append(URLEncoder.encode(String.valueOf(keyValue.value), CHARSET_UTF8));
        }
        return sbStr.toString();
    }

    /**
     * 添加值不为null的HTTP请求地址参数，允许同名的请求地址参数
     *
     * @param key   请求地址参数名称
     * @param value 请求地址参数值，为null则不会被添加
     * @return HTTP请求实例
     */
    public XRequest query(String key, Object value) {
        return query(key, value, false);
    }

    /**
     * 添加值不为null的HTTP请求地址参数，可选择对于同名的请求地址参数的处理方式
     *
     * @param key   请求地址参数名称
     * @param value 请求地址参数值，为null则不会被添加
     * @param clear true：清除已经存在的同名的请求地址参数，false：追加同名的请求地址参数
     * @return HTTP请求实例
     */
    public XRequest query(String key, Object value, boolean clear) {
        Objects.requireNonNull(key);
        if (this.requestQueries == null) {
            this.requestQueries = new LinkedList<>();
        }
        if (clear) {
            Iterator<KeyValue> iterator = this.requestQueries.iterator();
            while (iterator.hasNext()) {
                KeyValue keyValue = iterator.next();
                if (keyValue.key.equals(key)) {
                    iterator.remove();
                }
            }
        }
        if (value != null) {
            this.requestQueries.add(new KeyValue(key, value));
        }
        return this;
    }

    /**
     * 添加值不为null的HTTP请求头，允许同名的请求头
     *
     * @param key   请求头名称
     * @param value 请求头值，为null则不会被添加
     * @return HTTP请求实例
     */
    public XRequest header(String key, String value) {
        return header(key, value, false);
    }

    /**
     * 添加值不为null的HTTP请求头，可选择对于同名的请求头的处理方式
     *
     * @param key   请求头名称
     * @param value 请求头值，为null则不会被添加
     * @param clear true：清除已经存在的同名的请求头，false：追加同名的请求头
     * @return HTTP请求实例
     */
    public XRequest header(String key, String value, boolean clear) {
        Objects.requireNonNull(key);
        if (this.requestHeaders == null) {
            this.requestHeaders = new LinkedList<>();
        }
        if (clear) {
            Iterator<KeyValue> iterator = this.requestHeaders.iterator();
            while (iterator.hasNext()) {
                KeyValue keyValue = iterator.next();
                if (keyValue.key.equals(key)) {
                    iterator.remove();
                }
            }
        }
        if (value != null) {
            requestHeaders.add(new KeyValue(key, value));
        }
        return this;
    }

    /**
     * 添加值不为null的HTTP请求体参数，允许同名的请求体参数
     *
     * @param key   请求体参数名
     * @param value 请求体参数值，为null则不会被添加
     * @return HTTP请求实例
     */
    public XRequest content(String key, Object value) {
        return content(key, value, false);
    }

    /**
     * 添加值不为null的HTTP请求体参数，可选择对同名的请求体参数的处理方式
     *
     * @param key   请求体参数名
     * @param value 请求体参数值，为null则不会被添加
     * @param clear true：清除已经存在的同名的请求体参数，false：追加同名请求体参数
     * @return HTTP请求实例
     */
    public XRequest content(String key, Object value, boolean clear) {
        Objects.requireNonNull(key);
        if (this.requestContent == null || !(this.requestContent instanceof ParamsContent)) {
            this.requestContent = new ParamsContent();
        }
        ParamsContent paramsContent = ((ParamsContent) this.requestContent);
        if (clear) {
            Iterator<KeyValue> iterator = paramsContent.params.iterator();
            while (iterator.hasNext()) {
                KeyValue keyValue = iterator.next();
                if (keyValue.key.equals(key)) {
                    iterator.remove();
                }
            }
        }
        if (value != null) {
            paramsContent.params.add(new KeyValue(key, value));
        }
        return this;
    }

    /**
     * 添加自定义的HTTP请求体
     *
     * @param content 自定义的HTTP请求体
     * @return HTTP请求实例
     */
    public XRequest content(Content content) {
        Objects.requireNonNull(content);
        this.requestContent = content;
        return this;
    }

    /**
     * 获得HTTP请求的请求url，如果有请求地址参数则自动拼接成带参数的url
     *
     * @return HTTP请求的请求url
     */
    String requestUrl() {
        try {
            if (this.requestQueries != null) {
                return String.format("%s?%s", this.requestUrl, kvJoin(this.requestQueries));
            } else {
                return this.requestUrl;
            }
        } catch (Exception e) {
            throw new IllegalStateException("生成请求url时出错");
        }
    }

    /**
     * 获得HTTP请求的请求方法
     *
     * @return HTTP请求的请求方法
     */
    String requestMethod() {
        return this.requestMethod;
    }

    /**
     * 获得HTTP请求的请求头列表
     *
     * @return HTTP请求的请求头列表
     */
    List<KeyValue> requestHeaders() {
        if (this.requestMethod.equals(METHOD_POST) && this.requestContent != null) {
            header("Content-Type", this.requestContent.contentType(), true);
            long contentLength = requestContent.contentLength();
            if (contentLength > 0) {
                header("Content-Length", String.valueOf(contentLength), true);
            } else {
                header("Transfer-Encoding", "chunked", true);
            }
        }
        return this.requestHeaders;
    }

    /**
     * 获得HTTP请求的请求体
     *
     * @return HTTP请求的请求体
     */
    Content requestContent() {
        return this.requestContent;
    }

    /**
     * HTTP请求体，需要提供请求体的类型、请求体的长度、请求体写出到输出流的方法
     */
    public interface Content {
        /**
         * 请求体的MIME类型
         *
         * @return 请求体的MIME类型
         */
        String contentType();

        /**
         * 请求体的长度，如果不确定长度可以返回-1，这将使用chunked模式传输
         *
         * @return 请求体的长度
         */
        long contentLength();

        /**
         * 请求体写出到输出流的具体方法
         *
         * @param outStream 目标输出流
         * @throws Exception 将请求体写出到输出流时可能会发生异常
         */
        void contentWrite(DataOutputStream outStream) throws Exception;
    }

    /**
     * 带参数的请求体。
     * 如果不包含文件参数，则自动使用urlencoded方式。
     * 如果包含文件参数，则自动使用multipart方式。
     */
    public static class ParamsContent implements Content {
        public static final String MINUS = "--";
        public static final String CRLF = "\r\n";

        public final List<KeyValue> params = new LinkedList<>();
        public String boundary;
        public byte[] urlencoded;

        public boolean isMultipart() {
            if (boundary == null && urlencoded == null) {
                for (KeyValue keyValue : params) {
                    if (keyValue.value instanceof File) {
                        boundary = XTools.md5(String.format("multipart-%d-%d", System.currentTimeMillis(), new Random().nextInt()));
                        return true;
                    }
                }
                try {
                    urlencoded = kvJoin(params).getBytes();
                } catch (Exception e) {
                    e.printStackTrace();
                    urlencoded = new byte[0];
                }
                return false;
            } else {
                return boundary != null;
            }
        }

        @Override
        public String contentType() {
            if (isMultipart()) {
                return MIME_MULTIPART + "; boundary=" + boundary;
            } else {
                return MIME_URLENCODED + "; charset=utf-8";
            }
        }

        @Override
        public long contentLength() {
            if (isMultipart()) {
                return -1;
            } else {
                return urlencoded.length;
            }
        }

        @Override
        public void contentWrite(DataOutputStream doStream) throws Exception {
            if (isMultipart()) {
                for (KeyValue keyValue : params) {
                    if (keyValue.value instanceof File) {
                        doStream.write((MINUS + boundary + CRLF).getBytes());
                        doStream.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", keyValue.key, ((File) keyValue.value).getName(), CRLF).getBytes());
                        doStream.write(String.format("Content-Type: %s%s", URLConnection.getFileNameMap().getContentTypeFor(((File) keyValue.value).getAbsolutePath()), CRLF).getBytes());
                        doStream.write(CRLF.getBytes());
                        try (FileInputStream fiStream = new FileInputStream((File) keyValue.value)) {
                            XTools.streamToStream(fiStream, doStream);
                        }
                        doStream.write(CRLF.getBytes());
                    } else {
                        doStream.write((MINUS + boundary + CRLF).getBytes());
                        doStream.write(String.format("Content-Disposition: form-data; name=\"%s\"%s", keyValue.key, CRLF).getBytes());
                        doStream.write(CRLF.getBytes());
                        doStream.write(String.valueOf(keyValue.value).getBytes());
                        doStream.write(CRLF.getBytes());
                    }
                }
                doStream.write((MINUS + boundary + MINUS + CRLF).getBytes(CHARSET_UTF8));
            } else {
                doStream.write(urlencoded);
            }
        }
    }

    /**
     * 字符串类型请求体
     */
    public static class StringContent implements Content {

        public final String mime;
        public final byte[] bytes;

        public StringContent(String mime, String str) {
            this.mime = mime;
            this.bytes = str.getBytes();
        }

        @Override
        public String contentType() {
            return mime;
        }

        @Override
        public long contentLength() {
            return bytes.length;
        }

        @Override
        public void contentWrite(DataOutputStream outStream) throws Exception {
            outStream.write(bytes);
        }
    }

    /**
     * 文件类型请求体
     */
    public static class FileContent implements Content {
        public final String mime;
        public final File file;

        public FileContent(String mime, File file) {
            this.mime = mime;
            this.file = file;
        }

        @Override
        public String contentType() {
            return mime;
        }

        @Override
        public long contentLength() {
            return file.length();
        }

        @Override
        public void contentWrite(DataOutputStream outStream) throws Exception {
            try (FileInputStream finStream = new FileInputStream(file)) {
                XTools.streamToStream(finStream, outStream);
            }
        }
    }

    /**
     * 键值对
     */
    public static class KeyValue {
        public final String key;
        public final Object value;

        public KeyValue(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }
}
