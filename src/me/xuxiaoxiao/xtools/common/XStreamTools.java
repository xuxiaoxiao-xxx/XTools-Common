package me.xuxiaoxiao.xtools.common;

import java.io.*;

public class XStreamTools {

    /**
     * 将输入流中的全部数据读取成字符串
     *
     * @param inStream 要读取的输入流，不会关闭该输入流
     * @param charset  输入流的编码格式
     * @return 读取出的字符串
     * @throws IOException 在读取输入流时可能会发生IO异常
     */
    public static String streamToStr(InputStream inStream, String charset) throws IOException {
        int count;
        char[] buf = new char[512];
        StringBuilder sbStr = new StringBuilder();
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(inStream, charset));
        while ((count = bufReader.read(buf)) > 0) {
            sbStr.append(buf, 0, count);
        }
        return sbStr.toString();
    }

    /**
     * 将输入流中的全部数据读取成文件
     *
     * @param inStream 要读取的输入流，不会关闭该输入流
     * @param path     要保存的文件的位置
     * @return 读取出的文件
     * @throws IOException 输入输出时时可能会发生IO异常
     */
    public static File streamToFile(InputStream inStream, String path) throws IOException {
        int count;
        byte[] buffer = new byte[1024];
        File file = new File(path);
        BufferedInputStream bufInStream = new BufferedInputStream(inStream);
        try (FileOutputStream fOutStream = new FileOutputStream(file)) {
            while ((count = bufInStream.read(buffer)) > 0) {
                fOutStream.write(buffer, 0, count);
            }
            fOutStream.flush();
        }
        return file;
    }

    /**
     * 将输入流中的全部数据读取到输出流
     *
     * @param inStream  要读取的输入流，不会关闭该输入流
     * @param outStream 要卸任的输出流，不会关闭该输出流
     * @throws IOException 输入输出时时可能会发生IO异常
     */
    public static void streamToStream(InputStream inStream, OutputStream outStream) throws IOException {
        int count;
        byte[] buffer = new byte[1024];
        BufferedInputStream bufInStream = new BufferedInputStream(inStream);
        BufferedOutputStream bufOutStream = new BufferedOutputStream(outStream);
        while ((count = bufInStream.read(buffer)) > 0) {
            bufOutStream.write(buffer, 0, count);
        }
    }
}
