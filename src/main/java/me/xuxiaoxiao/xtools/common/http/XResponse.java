package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.XTools;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * HTTP响应类，提供了便捷的方法将输入流转换成字符串或文件
 */
public class XResponse implements AutoCloseable {
    private final HttpURLConnection connection;
    private final InputStream inStream;

    public XResponse(HttpURLConnection connection, InputStream inStream) {
        this.connection = connection;
        this.inStream = inStream;
    }

    /**
     * 获取返回的输入流
     *
     * @return 连接的输入流，记得使用XResponse实例的close()方法关闭输入流和连接
     */
    public final InputStream inStream() {
        return this.inStream;
    }

    /**
     * 将连接返回的输入流中的数据转化成字符串，默认的使用utf-8的编码方式
     *
     * @return 转化后的字符串
     */
    public final String string() {
        return string("utf-8");
    }

    /**
     * 将连接返回的输入流中的数据转化成字符串
     *
     * @return 转化后的字符串
     */
    public final String string(String charset) {
        try {
            return XTools.streamToStr(inStream(), charset);
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
    public final File file(String path) {
        try {
            return XTools.streamToFile(inStream(), path);
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
        if (inStream != null) {
            inStream.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
    }
}
