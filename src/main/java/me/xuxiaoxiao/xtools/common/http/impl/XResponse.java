package me.xuxiaoxiao.xtools.common.http.impl;

import me.xuxiaoxiao.xtools.common.XTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP响应类，提供了便捷的方法将输入流转换成字符串或文件
 */
public class XResponse implements AutoCloseable {
    private static final Pattern P_CHARSET = Pattern.compile("charset\\s*=\\s*\"?([^;\\s\"]+)\"?", Pattern.CASE_INSENSITIVE);

    @Nonnull
    private final HttpURLConnection connection;
    private InputStream inStream;

    public XResponse(@Nonnull HttpURLConnection connection) {
        this.connection = connection;
    }

    /**
     * 获取Http连接
     *
     * @return Http连接
     */
    @Nonnull
    public HttpURLConnection getConnection() {
        return this.connection;
    }

    /**
     * 获取响应状态码
     *
     * @return 响应状态码
     */
    public int getStatusCode() {
        try {
            return this.connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断请求是否成功
     *
     * @return 请求是否成功
     */
    public boolean isSuccess() {
        int code = this.getStatusCode();
        return code >= 200 && code < 300;
    }

    /**
     * 获取返回的输入流，只允许获取一次
     *
     * @return 连接的输入流，记得使用XResponse实例的close()方法关闭输入流和连接
     * @throws IOException 如果获取输入流失败
     */
    @Nullable
    public InputStream getStream() throws IOException {
        if (this.connection.getResponseCode() >= 200 && this.connection.getResponseCode() < 300) {
            if (this.inStream == null) {
                this.inStream = this.connection.getInputStream();
            }
        } else {
            if (this.inStream == null) {
                this.inStream = this.connection.getErrorStream();
            }
        }
        return this.inStream;
    }

    /**
     * 将连接返回的输入流中的数据转化成字符串，自动识别字符集
     *
     * @return 转化后的字符串
     */
    @Nullable
    public String asString() {
        String charset = "utf-8";
        String contentType = connection.getContentType();
        if (contentType != null) {
            Matcher matcher = P_CHARSET.matcher(contentType);
            if (matcher.find()) {
                charset = matcher.group(1);
            }
        }
        return asString(charset);
    }

    /**
     * 将连接返回的输入流中的数据转化成字符串，并自动关闭输入流
     *
     * @param charset 字符集
     * @return 转化后的字符串
     */
    @Nullable
    public final String asString(@Nonnull String charset) {
        try (InputStream inStream = getStream()) {
            if (inStream == null) {
                return null;
            } else {
                return XTools.streamToStr(inStream, charset);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.close();
        }
    }

    /**
     * 将连接返回的输入流中的数据转化成文件
     *
     * @param path 文件存储的路径
     * @return 转化后的文件
     */
    @Nullable
    public final File asFile(@Nonnull String path) {
        try (InputStream inStream = getStream()) {
            if (inStream == null) {
                return null;
            } else {
                return XTools.streamToFile(inStream, path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.close();
        }
    }

    /**
     * 关闭输入流和连接
     */
    @Override
    public void close() {
        if (this.inStream != null) {
            try {
                this.inStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.connection.disconnect();
    }
}
