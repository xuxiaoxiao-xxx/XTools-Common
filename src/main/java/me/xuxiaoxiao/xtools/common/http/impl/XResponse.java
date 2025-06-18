package me.xuxiaoxiao.xtools.common.http.impl;

import me.xuxiaoxiao.xtools.common.XTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * HTTP响应类，提供了便捷的方法将输入流转换成字符串或文件
 */
public class XResponse {
    private final HttpURLConnection connection;
    private final InputStream stream;

    public XResponse(@Nullable HttpURLConnection connection, @Nullable InputStream stream) {
        this.connection = connection;
        this.stream = stream;
    }

    /**
     * 获取Http连接
     *
     * @return Http连接
     */
    @Nullable
    public HttpURLConnection connection() {
        return this.connection;
    }

    /**
     * 获取返回的输入流
     *
     * @return 连接的输入流，记得使用XResponse实例的close()方法关闭输入流和连接
     */
    @Nullable
    public InputStream stream() {
        return this.stream;
    }

    @Nullable
    public String string() {
        return string("utf-8");
    }

    /**
     * 将连接返回的输入流中的数据转化成字符串
     *
     * @return 转化后的字符串
     */
    @Nullable
    public final String string(@Nonnull String charset) {
        try {
            InputStream inStream = stream();
            if (inStream == null) {
                return null;
            } else {
                return XTools.streamToStr(inStream, charset);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将连接返回的输入流中的数据转化成文件
     *
     * @param path 文件存储的路径
     * @return 转化后的文件
     */
    @Nullable
    public final File file(@Nonnull String path) {
        try {
            InputStream inStream = stream();
            if (inStream == null) {
                return null;
            } else {
                return XTools.streamToFile(inStream, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭该HTTP响应
     *
     * @throws Exception 在关闭该HTTP响应时可能会出现异常
     */

    public void close() throws Exception {
        if (stream != null) {
            stream.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
    }
}
