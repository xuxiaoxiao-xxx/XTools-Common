package me.xuxiaoxiao.xtools.common.code;

import me.xuxiaoxiao.xtools.common.XTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 常用的基本的关于编码和解码的函数的集合
 */
public final class XCodeTools {
    public static final String CFG_BUFFER = XTools.CFG_PREFIX + "code.buffer";
    public static final String CFG_BUFFER_DEFAULT = "131072";
    /**
     * 散列算法-MD5
     */
    public static final String HASH_MD5 = "MD5";
    /**
     * 散列算法-SHA1
     */
    public static final String HASH_SHA1 = "SHA-1";
    /**
     * 散列算法-SHA256
     */
    public static final String HASH_SHA256 = "SHA-256";

    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private XCodeTools() {
    }

    /**
     * 字节数组散列
     *
     * @param algorithm 散列算法
     * @param bytes     被散列的字节数组
     * @return 散列结果，全小写字母
     * @throws NoSuchAlgorithmException 当未找到指定的散列算法时抛出异常
     */
    public static String hash(String algorithm, byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(bytes);
        return bytesToHex(messageDigest.digest());
    }

    /**
     * 文件散列
     *
     * @param algorithm 散列算法
     * @param file      被散列的文件
     * @return 散列结果，全小写字母
     * @throws IOException              当文件未找到或者输入输出时错误时抛出异常
     * @throws NoSuchAlgorithmException 当未找到指定的散列算法时抛出异常
     */
    public static String hash(String algorithm, File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        try (FileInputStream fileInputStream = new FileInputStream(file); DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest)) {
            byte[] buffer = new byte[Integer.valueOf(XTools.cfgDef(CFG_BUFFER, CFG_BUFFER_DEFAULT))];
            while (true) {
                if (digestInputStream.read(buffer) <= 0) {
                    break;
                }
            }
            return bytesToHex(digestInputStream.getMessageDigest().digest());
        }
    }

    /**
     * 将字节数组转换成16进制字符串
     *
     * @param bytes 要转换的字节数组
     * @return 转换后的字符串，全小写字母
     */
    public static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            chars[i << 1] = HEX[b >>> 4 & 0xf];
            chars[(i << 1) + 1] = HEX[b & 0xf];
        }
        return new String(chars);
    }
}
