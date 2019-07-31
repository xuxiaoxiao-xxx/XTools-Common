package me.xuxiaoxiao.xtools.common.http.executor.impl;

import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * HTTP响应类，提供了便捷的方法将输入流转换成字符串或文件
 */
public class XResponse implements XHttpExecutor.Response {
    public static final String CFG_RSP_CHARSET = XTools.CFG_PREFIX + "http.rspCharset";
    public static final String CFG_RSP_CHARSET_DEFAULT = "utf-8";

    private final HttpURLConnection connection;
    private final InputStream stream;

    public XResponse(HttpURLConnection connection, InputStream stream) {
        this.connection = connection;
        this.stream = stream;
    }

    /**
     * 获取Http连接
     *
     * @return Http连接
     */
    public HttpURLConnection connection() {
        return this.connection;
    }

    /**
     * 获取返回的输入流
     *
     * @return 连接的输入流，记得使用XResponse实例的close()方法关闭输入流和连接
     */
    @Override
    public InputStream stream() {
        return this.stream;
    }

    @Override
    public String string() {
        return string(XTools.cfgDef(CFG_RSP_CHARSET, CFG_RSP_CHARSET_DEFAULT).trim());
    }

    /**
     * 将连接返回的输入流中的数据转化成字符串
     *
     * @return 转化后的字符串
     */
    @Override
    public final String string(String charset) {
        try {
            return XTools.streamToStr(stream(), charset);
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
    @Override
    public final File file(String path) {
        try {
            return XTools.streamToFile(stream(), path);
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
    @Override
    public void close() throws Exception {
        if (stream != null) {
            stream.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
    }
}
