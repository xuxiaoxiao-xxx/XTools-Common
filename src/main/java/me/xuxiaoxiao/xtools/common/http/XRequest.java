package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.XTools;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

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
     * 新建一个PUT请求
     *
     * @param url 请求url
     * @return PUT请求
     */
    public static XRequest PUT(String url) {
        return new XRequest(METHOD_PUT, url);
    }

    /**
     * 新建一个DELETE请求
     *
     * @param url 请求url
     * @return DELETE请求
     */
    public static XRequest DELETE(String url) {
        return new XRequest(METHOD_DELETE, url);
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
     * 添加值不为null的HTTP请求体参数，允许同名的请求体参数。
     * 如果有文件参数，则会使用multipart请求体，否则使用urlencoded请求体
     *
     * @param key   请求体参数名
     * @param value 请求体参数值，为null则不会被添加
     * @return HTTP请求实例
     */
    public XRequest content(String key, Object value) {
        return content(key, value, false);
    }

    /**
     * 添加值不为null的HTTP请求体参数，可选择对同名的请求体参数的处理方式。
     * 如果有文件参数，则会使用multipart请求体，否则使用urlencoded请求体
     *
     * @param key   请求体参数名
     * @param value 请求体参数值，为null则不会被添加
     * @param clear true：清除已经存在的同名的请求体参数，false：追加同名请求体参数
     * @return HTTP请求实例
     */
    public XRequest content(String key, Object value, boolean clear) {
        Objects.requireNonNull(key);
        if (METHOD_GET.equals(requestMethod) || METHOD_DELETE.equals(requestMethod)) {
            throw new IllegalArgumentException(String.format("%s方法不能添加请求体", requestMethod));
        }
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
                urlencodedContent.param(key, value, clear);
                return this;
            }
        }
        if (this.requestContent instanceof MultipartContent) {
            MultipartContent multipartContent = (MultipartContent) this.requestContent;
            if (value instanceof MultipartContent.Part) {
                MultipartContent.Part part = (MultipartContent.Part) value;
                if (key.equals(part.name)) {
                    multipartContent.part((MultipartContent.Part) value, clear);
                } else {
                    throw new IllegalArgumentException(String.format("参数的key：%s与表单的名称：%s不相等", key, part.name));
                }
            } else {
                multipartContent.part(key, value, clear);
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
    public XRequest content(Content content) {
        Objects.requireNonNull(content);
        this.requestContent = content;
        return this;
    }

    /**
     * 获得HTTP请求的请求方法
     *
     * @return HTTP请求的请求方法
     */
    public String requestMethod() {
        return this.requestMethod;
    }

    /**
     * 获得HTTP请求的请求url，如果有请求地址参数则自动拼接成带参数的url
     *
     * @return HTTP请求的请求url
     */
    public String requestUrl() {
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
     * 获得HTTP请求的请求头列表
     *
     * @return HTTP请求的请求头列表
     */
    public List<KeyValue> requestHeaders() throws IOException {
        if ((this.requestMethod.equals(METHOD_POST) || this.requestMethod.equals(METHOD_PUT)) && this.requestContent != null) {
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
    public Content requestContent() {
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
        String contentType() throws IOException;

        /**
         * 请求体的长度，如果不确定长度可以返回-1，这将使用chunked模式传输
         *
         * @return 请求体的长度
         */
        long contentLength() throws IOException;

        /**
         * 请求体写出到输出流的具体方法
         *
         * @param outStream 目标输出流
         * @throws IOException 将请求体写出到输出流时可能会发生异常
         */
        void contentWrite(DataOutputStream outStream) throws IOException;
    }

    /**
     * urlencoded类型请求体
     */
    public static class UrlencodedContent implements Content {
        private final List<KeyValue> params = new LinkedList<>();
        private byte[] urlencoded;

        public UrlencodedContent param(String key, Object value) {
            return this.param(key, value, false);
        }

        public UrlencodedContent param(String key, Object value, boolean clear) {
            Objects.requireNonNull(key);
            this.urlencoded = null;
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

        @Override
        public String contentType() {
            return MIME_URLENCODED + "; charset=" + CHARSET_UTF8;
        }

        @Override
        public long contentLength() throws IOException {
            if (urlencoded == null) {
                urlencoded = kvJoin(params).getBytes(CHARSET_UTF8);
            }
            return urlencoded.length;
        }

        @Override
        public void contentWrite(DataOutputStream doStream) throws IOException {
            if (urlencoded == null) {
                urlencoded = kvJoin(params).getBytes(CHARSET_UTF8);
            }
            doStream.write(urlencoded);
        }
    }

    /**
     * multipart类型请求体
     */
    public static class MultipartContent implements Content {
        public static final String HYPHENS = "--";
        public static final String CRLF = "\r\n";

        private final List<Part> parts = new LinkedList<>();
        private String boundary = XTools.md5(String.format("multipart-%d-%d", System.currentTimeMillis(), new Random().nextInt()));

        public MultipartContent part(String key, Object value) {
            return this.part(new Part(key, value), false);
        }

        public MultipartContent part(String key, Object value, boolean clear) {
            return this.part(new Part(key, value), clear);
        }

        public MultipartContent part(Part part) {
            return this.part(part, false);
        }

        public MultipartContent part(Part part, boolean clear) {
            Objects.requireNonNull(part);
            Objects.requireNonNull(part.name);
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

        @Override
        public String contentType() {
            return MIME_MULTIPART + "; boundary=" + boundary;
        }

        @Override
        public long contentLength() throws IOException {
            long contentLength = 0;
            for (Part part : parts) {
                contentLength += (HYPHENS + boundary + CRLF).getBytes(CHARSET_UTF8).length;
                for (String header : part.headers()) {
                    contentLength += String.format("%s%s", header, CRLF).getBytes(CHARSET_UTF8).length;
                }
                contentLength += CRLF.getBytes(CHARSET_UTF8).length;
                contentLength += part.partLength();
                contentLength += CRLF.getBytes(CHARSET_UTF8).length;
            }
            contentLength = contentLength + (HYPHENS + boundary + HYPHENS + CRLF).getBytes(CHARSET_UTF8).length;
            return contentLength;
        }

        @Override
        public void contentWrite(DataOutputStream doStream) throws IOException {
            for (Part part : parts) {
                doStream.write((HYPHENS + boundary + CRLF).getBytes(CHARSET_UTF8));
                for (String header : part.headers()) {
                    doStream.write(String.format("%s%s", header, CRLF).getBytes(CHARSET_UTF8));
                }
                doStream.write(CRLF.getBytes(CHARSET_UTF8));
                part.partWrite(doStream);
                doStream.write(CRLF.getBytes(CHARSET_UTF8));
            }
            doStream.write((HYPHENS + boundary + HYPHENS + CRLF).getBytes(CHARSET_UTF8));
        }

        public static class Part {
            public final String name;
            public final Object value;

            public Part(String name, Object value) {
                this.name = name;
                this.value = value;
            }

            public String[] headers() throws IOException {
                if (value instanceof File) {
                    String disposition = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", name, URLEncoder.encode(((File) value).getName(), CHARSET_UTF8));
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
                    return String.valueOf(value).getBytes(CHARSET_UTF8).length;
                }
            }

            public void partWrite(DataOutputStream doStream) throws IOException {
                if (value instanceof File) {
                    try (FileInputStream fiStream = new FileInputStream((File) value)) {
                        XTools.streamToStream(fiStream, doStream);
                    }
                } else {
                    doStream.write(String.valueOf(value).getBytes(CHARSET_UTF8));
                }
            }
        }
    }

    /**
     * 字符串类型请求体
     */
    public static class StringContent implements Content {
        private static Pattern P_CHARSET = Pattern.compile("charset\\s*=\\s*\"?(.+)\"?\\s*;?");
        public final String mime;
        public final byte[] bytes;

        public StringContent(String mime, String str) {
            Matcher matcher = P_CHARSET.matcher(mime);
            if (matcher.find()) {
                try {
                    this.mime = mime;
                    this.bytes = str.getBytes(matcher.group(1));
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(String.format("无法将字符串以指定的编码方式【%s】进行编码", matcher.group(1)));
                }
            } else {
                try {
                    this.mime = mime + "; charset=" + CHARSET_UTF8;
                    this.bytes = str.getBytes(CHARSET_UTF8);
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(String.format("无法将字符串以指定的编码方式【%s】进行编码", CHARSET_UTF8));
                }
            }
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
        public void contentWrite(DataOutputStream outStream) throws IOException {
            outStream.write(bytes);
        }
    }

    /**
     * 文件类型请求体
     */
    public static class FileContent implements Content {
        public final File file;

        public FileContent(File file) {
            this.file = file;
        }

        @Override
        public String contentType() throws IOException {
            return Files.probeContentType(Paths.get(file.getAbsolutePath()));
        }

        @Override
        public long contentLength() {
            return file.length();
        }

        @Override
        public void contentWrite(DataOutputStream outStream) throws IOException {
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
