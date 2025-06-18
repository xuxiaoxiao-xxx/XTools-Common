package me.xuxiaoxiao.xtools.common.http.impl;

import me.xuxiaoxiao.xtools.common.XTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP请求类，记录了HTTP请求的请求方法，请求地址，请求头，请求体
 */
public class XRequest {
    public static final String MIME_URLENCODED = "application/x-www-form-urlencoded";
    public static final String MIME_MULTIPART = "multipart/form-data";
    public static final String MIME_JSON = "application/json";
    public static final String MIME_XML = "text/xml";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_HEAD = "HEAD";

    /**
     * 请求编码方式
     */
    private String charset = "utf-8";
    /**
     * 请求方法
     */
    private String requestMethod;
    /**
     * 请求地址
     */
    private String requestUrl;
    /**
     * 请求地址参数
     */
    private final List<KeyValue> requestQueries = new LinkedList<>();
    /**
     * 请求头
     */
    private final List<KeyValue> requestHeaders = new LinkedList<>();
    /**
     * 请求体
     */
    private Content requestContent;

    public XRequest(@Nonnull String method, @Nonnull String url) {
        this.setMethod(method);
        this.setUrl(url);
    }

    /**
     * 新建一个GET请求
     *
     * @param url 请求url
     * @return GET请求
     */
    @Nonnull
    public static XRequest GET(@Nonnull String url) {
        return new XRequest(METHOD_GET, url);
    }

    /**
     * 新建一个POST请求
     *
     * @param url 请求url
     * @return POST请求
     */
    @Nonnull
    public static XRequest POST(@Nonnull String url) {
        return new XRequest(METHOD_POST, url);
    }

    /**
     * 新建一个PUT请求
     *
     * @param url 请求url
     * @return PUT请求
     */
    @Nonnull
    public static XRequest PUT(@Nonnull String url) {
        return new XRequest(METHOD_PUT, url);
    }

    /**
     * 新建一个PATCH请求
     *
     * @param url 请求url
     * @return PATCH请求
     */
    @Nonnull
    public static XRequest PATCH(@Nonnull String url) {
        return new XRequest(METHOD_PATCH, url);
    }

    /**
     * 新建一个DELETE请求
     *
     * @param url 请求url
     * @return DELETE请求
     */
    @Nonnull
    public static XRequest DELETE(@Nonnull String url) {
        return new XRequest(METHOD_DELETE, url);
    }

    /**
     * 新建一个HEAD请求
     *
     * @param url 请求url
     * @return HEAD请求
     */
    @Nonnull
    public static XRequest HEAD(@Nonnull String url) {
        return new XRequest(METHOD_HEAD, url);
    }

    /**
     * 新建一个OPTIONS请求
     *
     * @param url 请求url
     * @return OPTIONS请求
     */
    @Nonnull
    public static XRequest OPTIONS(@Nonnull String url) {
        return new XRequest(METHOD_OPTIONS, url);
    }

    /**
     * 设置请求的字符集
     *
     * @param charset 字符集
     * @return HTTP请求实例
     */
    @Nonnull
    public XRequest charset(@Nonnull String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 添加HTTP请求地址参数，允许同名的请求地址参数
     *
     * @param key   请求地址参数名称
     * @param value 请求地址参数值，为null则不会被添加
     * @return HTTP请求实例
     */
    @Nonnull
    public XRequest query(@Nonnull String key, @Nullable Object value) {
        return query(key, value, false);
    }

    /**
     * 添加HTTP请求地址参数，可选择对于同名的请求地址参数的处理方式
     *
     * @param key      请求地址参数名称
     * @param value    请求地址参数值，为null则不会被添加
     * @param override true：覆盖同名的请求地址参数，false：追加同名的请求地址参数
     * @return HTTP请求实例
     */
    @Nonnull
    public XRequest query(@Nonnull String key, @Nullable Object value, boolean override) {
        if (override) {
            this.requestQueries.removeIf(keyValue -> keyValue.key.equals(key));
        }
        if (value != null) {
            this.requestQueries.add(new KeyValue(key, value));
        }
        return this;
    }

    /**
     * 添加HTTP请求头，允许同名的请求头
     *
     * @param key   请求头名称
     * @param value 请求头值，为null则不会被添加
     * @return HTTP请求实例
     */
    @Nonnull
    public XRequest header(@Nonnull String key, @Nullable String value) {
        return header(key, value, false);
    }

    /**
     * 添加HTTP请求头，可选择对于同名的请求头的处理方式
     *
     * @param key      请求头名称
     * @param value    请求头值，为null则不会被添加
     * @param override true：覆盖同名的请求头，false：追加同名的请求头
     * @return HTTP请求实例
     */
    @Nonnull
    public XRequest header(@Nonnull String key, @Nullable String value, boolean override) {
        setHeader(key, value, override);
        return this;
    }

    /**
     * 添加HTTP请求体参数，允许同名的请求体参数。
     * 如果有文件参数，则会使用multipart请求体，否则使用urlencoded请求体
     *
     * @param key   请求体参数名
     * @param value 请求体参数值，为null则不会被添加
     * @return HTTP请求实例
     */
    @Nonnull
    public XRequest content(@Nonnull String key, @Nullable Object value) {
        return content(key, value, false);
    }

    /**
     * 添加HTTP请求体参数，可选择对同名的请求体参数的处理方式。
     * 如果有文件参数，则会使用multipart请求体，否则使用urlencoded请求体
     *
     * @param key      请求体参数名
     * @param value    请求体参数值，为null则不会被添加
     * @param override true：覆盖同名的请求体参数，false：追加同名请求体参数
     * @return HTTP请求实例
     */
    @Nonnull
    public XRequest content(@Nonnull String key, @Nullable Object value, boolean override) {
        if (this.requestContent == null) {
            this.requestContent = new UrlencodedContent();
        }
        if (this.requestContent instanceof UrlencodedContent) {
            UrlencodedContent urlencodedContent = ((UrlencodedContent) this.requestContent);
            if (value instanceof File || value instanceof MultipartContent.Part) {
                //如果请求体一开始是urlencoded类型的，现在来了一个文件，则自动转换成multipart类型的，然后交给multipart类型的处理逻辑处理
                MultipartContent multipartContent = new MultipartContent();
                for (KeyValue keyValue : urlencodedContent.params) {
                    multipartContent.part(keyValue.key, keyValue.value);
                }
                this.requestContent = multipartContent;
            } else {
                urlencodedContent.param(key, value, override);
                return this;
            }
        }
        if (this.requestContent instanceof MultipartContent) {
            MultipartContent multipartContent = (MultipartContent) this.requestContent;
            if (value instanceof MultipartContent.Part) {
                MultipartContent.Part part = (MultipartContent.Part) value;
                if (key.equals(part.name)) {
                    multipartContent.part((MultipartContent.Part) value, override);
                } else {
                    throw new IllegalArgumentException(String.format("参数的key：%s与表单的名称：%s不相等", key, part.name));
                }
            } else {
                multipartContent.part(key, value, override);
            }
            return this;
        }
        throw new IllegalStateException(String.format("%s不能接受键值对请求体", this.requestContent.getClass().getName()));
    }

    /**
     * 添加自定义的HTTP请求体
     *
     * @param content 自定义的HTTP请求体
     * @return HTTP请求实例
     */
    @Nonnull
    public XRequest content(@Nonnull Content content) {
        setContent(content);
        return this;
    }

    public void setCharset(@Nonnull String charset) {
        this.charset = charset;
    }

    @Nonnull
    public String getCharset() {
        return this.charset;
    }

    public void setMethod(@Nonnull String method) {
        this.requestMethod = method.toUpperCase();
    }

    /**
     * 获得HTTP请求的请求方法
     *
     * @return HTTP请求的请求方法
     */
    @Nonnull
    public String getMethod() {
        return this.requestMethod;
    }

    public void setUrl(@Nonnull String url) {
        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            throw new IllegalArgumentException("XRequest仅支持HTTP协议和HTTPS协议");
        } else if (url.indexOf('?') >= 0) {
            this.requestUrl = url.substring(0, url.indexOf('?'));
            try {
                for (String keyValue : url.substring(url.indexOf('?') + 1).split("&")) {
                    int eqIndex = keyValue.indexOf('=');
                    if (eqIndex < 0) {
                        throw new IllegalArgumentException("请求的url格式有误");
                    } else {
                        query(URLDecoder.decode(keyValue.substring(0, eqIndex), charset), URLDecoder.decode(keyValue.substring(eqIndex + 1), charset));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(String.format("不支持编码方式：%s", charset));
            }
        } else {
            this.requestUrl = url;
        }
    }

    /**
     * 获得HTTP请求的请求url，如果有请求地址参数则自动拼接成带参数的url
     *
     * @return HTTP请求的请求url
     */
    @Nonnull
    public String getUrl() {
        try {
            if (!XTools.isEmpty(this.requestUrl)) {
                return String.format("%s?%s", this.requestUrl, kvJoin(this.requestQueries, charset));
            } else {
                return this.requestUrl;
            }
        } catch (Exception e) {
            throw new IllegalStateException("生成请求url时出错");
        }
    }

    public void setHeader(@Nonnull String key, @Nullable String value, boolean append) {
        if (append) {
            this.requestHeaders.removeIf(keyValue -> keyValue.key.equals(key));
        }
        if (value != null) {
            requestHeaders.add(new KeyValue(key, value));
        }
    }

    /**
     * 获得HTTP请求的请求头列表
     *
     * @return HTTP请求的请求头列表
     */
    @Nullable
    public List<KeyValue> getHeaders() {
        if (this.requestContent != null) {
            try {
                header("Content-Type", this.requestContent.contentType(), true);
                long contentLength = requestContent.contentLength();
                if (contentLength > 0) {
                    header("Content-Length", String.valueOf(contentLength), true);
                } else {
                    header("Transfer-Encoding", "chunked", true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.requestHeaders;
    }

    public void setContent(@Nonnull Content content) {
        this.requestContent = content;
    }

    /**
     * 获得HTTP请求的请求体
     *
     * @return HTTP请求的请求体
     */
    @Nullable
    public Content getContent() {
        return this.requestContent;
    }

    /**
     * 将键值对集合经过url编码后用固定的字符串连接起来
     *
     * @return 连接后的字符串
     * @throws UnsupportedEncodingException 给定的编码格式不支持时抛出异常
     */
    @Nonnull
    private static String kvJoin(@Nonnull List<KeyValue> keyValues, @Nonnull String charset) throws UnsupportedEncodingException {
        StringBuilder sbStr = new StringBuilder();
        for (KeyValue keyValue : keyValues) {
            if (sbStr.length() > 0) {
                sbStr.append('&');
            }
            sbStr.append(URLEncoder.encode(keyValue.key, charset)).append('=').append(URLEncoder.encode(String.valueOf(keyValue.value), charset));
        }
        return sbStr.toString();
    }

    /**
     * HTTP请求体，需要提供请求体的类型、请求体的长度、请求体写出到输出流的方法
     */
    public interface Content {
        /**
         * 请求体字符编码
         *
         * @return 字符编码
         * @throws IOException IO异常
         */
        @Nonnull
        String charset() throws IOException;

        /**
         * 请求体的MIME类型
         *
         * @return 请求体的MIME类型
         * @throws IOException IO异常
         */
        @Nonnull
        String contentType() throws IOException;

        /**
         * 请求体的长度，如果不确定长度可以返回-1，这将使用chunked模式传输
         *
         * @return 请求体的长度
         * @throws IOException IO异常
         */
        long contentLength() throws IOException;

        /**
         * 请求体写出到输出流的具体方法
         *
         * @param outStream 目标输出流
         * @throws IOException 将请求体写出到输出流时可能会发生异常
         */
        void writeToStream(@Nonnull OutputStream outStream) throws IOException;
    }

    /**
     * 键值对
     */
    public static class KeyValue {
        /**
         * 键
         */
        public final String key;
        /**
         * 值
         */
        public final Object value;

        public KeyValue(@Nonnull String key, @Nonnull Object value) {
            this.key = key;
            this.value = value;
        }

        @Nonnull
        public String getKey() {
            return key;
        }

        @Nonnull
        public Object getValue() {
            return value;
        }
    }

    /**
     * urlencoded类型请求体
     */
    public static class UrlencodedContent implements Content {
        private final List<KeyValue> params = new LinkedList<>();
        private byte[] content;

        @Nonnull
        public UrlencodedContent param(@Nonnull String key, @Nullable Object value) {
            return this.param(key, value, false);
        }

        @Nonnull
        public UrlencodedContent param(@Nonnull String key, @Nullable Object value, boolean clear) {
            this.content = null;
            if (clear) {
                Iterator<KeyValue> iterator = this.params.iterator();
                while (iterator.hasNext()) {
                    KeyValue keyValue = iterator.next();
                    if (keyValue.key.equals(key)) {
                        iterator.remove();
                    }
                }
            }
            if (value != null) {
                this.params.add(new KeyValue(key, value));
            }
            return this;
        }

        @Nonnull
        public String charset() {
            return "utf-8";
        }

        @Nonnull
        public String contentType() {
            return MIME_URLENCODED + "; charset=" + charset();
        }

        public long contentLength() throws IOException {
            if (content == null) {
                content = kvJoin(params, charset()).getBytes(charset());
            }
            return content.length;
        }

        public void writeToStream(@Nonnull OutputStream outStream) throws IOException {
            if (content == null) {
                content = kvJoin(params, charset()).getBytes(charset());
            }
            outStream.write(content);
        }
    }

    /**
     * multipart类型请求体
     */
    public static class MultipartContent implements Content {
        public static final String HYPHENS = "--";
        public static final String CRLF = "\r\n";
        private final List<Part> parts = new LinkedList<>();
        private final String boundary = XTools.md5(String.format("multipart-%d-%d", System.currentTimeMillis(), new Random().nextInt()));

        @Nonnull
        public MultipartContent part(@Nonnull String key, @Nullable Object value) {
            return this.part(new Part(key, value, charset()), false);
        }

        @Nonnull
        public MultipartContent part(@Nonnull String key, @Nullable Object value, boolean clear) {
            return this.part(new Part(key, value, charset()), clear);
        }

        @Nonnull
        public MultipartContent part(@Nonnull Part part) {
            return this.part(part, false);
        }

        @Nonnull
        public MultipartContent part(@Nonnull Part part, boolean clear) {
            if (clear) {
                Iterator<Part> iterator = this.parts.iterator();
                while (iterator.hasNext()) {
                    Part temp = iterator.next();
                    if (part.name.equals(temp.name)) {
                        iterator.remove();
                    }
                }
            }
            if (part.value != null) {
                this.parts.add(part);
            }
            return this;
        }

        @Nonnull
        public String charset() {
            return "utf-8";
        }

        @Nonnull
        public String contentType() {
            return MIME_MULTIPART + "; boundary=" + boundary;
        }

        public long contentLength() throws IOException {
            long contentLength = 0;
            for (Part part : parts) {
                contentLength += (HYPHENS + boundary + CRLF).getBytes(charset()).length;
                for (String header : part.headers()) {
                    contentLength += String.format("%s%s", header, CRLF).getBytes(charset()).length;
                }
                contentLength += CRLF.getBytes(charset()).length;
                contentLength += part.partLength();
                contentLength += CRLF.getBytes(charset()).length;
            }
            contentLength = contentLength + (HYPHENS + boundary + HYPHENS + CRLF).getBytes(charset()).length;
            return contentLength;
        }

        public void writeToStream(@Nonnull OutputStream outStream) throws IOException {
            for (Part part : parts) {
                outStream.write((HYPHENS + boundary + CRLF).getBytes(charset()));
                for (String header : part.headers()) {
                    outStream.write(String.format("%s%s", header, CRLF).getBytes(charset()));
                }
                outStream.write(CRLF.getBytes(charset()));
                part.partWrite(outStream);
                outStream.write(CRLF.getBytes(charset()));
            }
            outStream.write((HYPHENS + boundary + HYPHENS + CRLF).getBytes(charset()));
        }

        public static class Part {
            @Nonnull
            public final String name;
            @Nullable
            public final Object value;
            @Nonnull
            public final String charset;

            public Part(@Nonnull String name, @Nullable Object value) {
                this(name, value, "utf-8");
            }

            public Part(@Nonnull String name, @Nullable Object value, @Nonnull String charset) {
                this.name = name;
                this.value = value;
                this.charset = charset;
            }

            @Nonnull
            public String[] headers() throws IOException {
                if (value instanceof File) {
                    String disposition = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", name, URLEncoder.encode(((File) value).getName(), charset));
                    String type = String.format("Content-Type: %s", Files.probeContentType(Paths.get(((File) value).getAbsolutePath())));
                    return new String[]{disposition, type};
                } else {
                    return new String[]{String.format("Content-Disposition: form-data; name=\"%s\"", name)};
                }
            }

            public long partLength() throws IOException {
                if (value instanceof File) {
                    return ((File) value).length();
                } else {
                    return String.valueOf(value).getBytes(charset).length;
                }
            }

            public void partWrite(@Nonnull OutputStream outStream) throws IOException {
                if (value instanceof File) {
                    try (FileInputStream fiStream = new FileInputStream((File) value)) {
                        XTools.streamToStream(fiStream, outStream);
                    }
                } else {
                    outStream.write(String.valueOf(value).getBytes(charset));
                }
            }
        }
    }

    /**
     * 字符串类型请求体
     */
    public static class StringContent implements Content {
        private static final Pattern P_CHARSET = Pattern.compile("charset\\s*=\\s*\"?([^;\\s\"]+)\"?", Pattern.CASE_INSENSITIVE);
        public final String mime;
        public final byte[] bytes;

        public StringContent(@Nonnull String mime, @Nonnull String str) {
            try {
                Matcher matcher = P_CHARSET.matcher(mime);
                if (matcher.find()) {
                    this.mime = mime;
                    this.bytes = str.getBytes(matcher.group(1));
                } else {
                    this.mime = mime + "; charset=" + charset();
                    this.bytes = str.getBytes(charset());
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(String.format("无法将字符串以指定的编码方式【%s】进行编码", charset()));
            }
        }

        @Nonnull
        public String charset() {
            return "utf-8";
        }

        @Nonnull
        public String contentType() {
            return mime;
        }

        public long contentLength() {
            return bytes.length;
        }

        public void writeToStream(@Nonnull OutputStream outStream) throws IOException {
            outStream.write(bytes);
        }
    }

    /**
     * 文件类型请求体
     */
    public static class FileContent implements Content {
        public final File file;

        public FileContent(@Nonnull File file) {
            this.file = file;
        }

        @Nonnull
        public String charset() {
            return "utf-8";
        }

        @Nonnull
        public String contentType() throws IOException {
            return Files.probeContentType(Paths.get(file.getAbsolutePath()));
        }

        public long contentLength() {
            return file.length();
        }

        public void writeToStream(@Nonnull OutputStream outStream) throws IOException {
            try (FileInputStream finStream = new FileInputStream(file)) {
                XTools.streamToStream(finStream, outStream);
            }
        }
    }
}
