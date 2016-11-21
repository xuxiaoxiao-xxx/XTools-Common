package me.xuxiaoxiao.xtools.common;

import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * 信息摘要工具，支持MD5和SHA1算法
 *
 * @author XXX
 */
public final class XDigestTools {
    private static final char HEX_CHARS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private XDigestTools() {
    }

    /**
     * 字节数组摘要
     *
     * @param algo  摘要算法
     * @param bytes 被摘要的字节数组
     * @return 摘要后的字符串
     */
    public static String digest(Algo algo, byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algo.name());
            messageDigest.update(bytes);
            return bytesToHex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 字符串摘要
     *
     * @param algo 摘要算法
     * @param str  被摘要的字符串
     * @return 摘要后的字符串
     */
    public static String digest(Algo algo, String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algo.name());
            messageDigest.update(str.getBytes());
            return bytesToHex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件摘要
     *
     * @param algo 摘要算法
     * @param file 被摘要的文件
     * @return 摘要后的字符串
     */
    public static String digest(Algo algo, File file) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algo.name());
            try (FileInputStream fileInputStream = new FileInputStream(file); DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest)) {
                byte[] buffer = new byte[1024];
                while (digestInputStream.read(buffer) > 0) {
                }
                return bytesToHex(digestInputStream.getMessageDigest().digest());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字节数组转换成16进制字符串
     *
     * @param bytes 要转换的字节数组
     * @return 转换后的字符串
     */
    private static String bytesToHex(byte[] bytes) {
        char chars[] = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            chars[i << 1] = HEX_CHARS[b >>> 4 & 0xf];
            chars[(i << 1) + 1] = HEX_CHARS[b & 0xf];
        }
        return new String(chars);
    }

    /**
     * 信息摘要的算法，支持MD5和SHA1
     *
     * @author XXX
     */

    public enum Algo {
        MD5, SHA1
    }
}