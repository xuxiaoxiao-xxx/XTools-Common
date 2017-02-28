package me.xuxiaoxiao.xtools;

import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * XuXiaoXiao的常用的基本的关于编码和解码的函数的集合
 */
public final class XUCode {
    /**
     * 散列算法-MD5
     */
    public static final String HASH_MD5 = "MD5";
    /**
     * 散列算法SHA1
     */
    public static final String HASH_SHA1 = "SHA1";

    private static final char HEX[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private XUCode() {
    }

    /**
     * 字节数组散列
     *
     * @param algorithm 散列算法
     * @param bytes     被散列的字节数组
     * @return 散列结果，全小写字母
     */
    public static String hash(String algorithm, byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(bytes);
            return bytesToHex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件散列
     *
     * @param algorithm 散列算法
     * @param file      被散列的文件
     * @return 散列结果，全小写字母
     */
    public static String hash(String algorithm, File file) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            try (FileInputStream fileInputStream = new FileInputStream(file); DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest)) {
                byte[] buffer = new byte[1024];
                while (true) {
                    if (digestInputStream.read(buffer) <= 0) {
                        break;
                    }
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
     * @return 转换后的字符串，全小写字母
     */
    private static String bytesToHex(byte[] bytes) {
        char chars[] = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            chars[i << 1] = HEX[b >>> 4 & 0xf];
            chars[(i << 1) + 1] = HEX[b & 0xf];
        }
        return new String(chars);
    }
}
