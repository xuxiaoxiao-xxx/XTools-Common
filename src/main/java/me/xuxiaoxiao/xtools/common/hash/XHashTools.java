package me.xuxiaoxiao.xtools.common.hash;

import me.xuxiaoxiao.xtools.common.XToolsException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 常用的基本的关于编码和解码的函数的集合
 */
public class XHashTools {
    public static final String HASH_MD5 = "MD5";
    public static final String HASH_SHA1 = "SHA-1";
    public static final String HASH_SHA256 = "SHA-256";
    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Nonnull
    private final Config config;

    public XHashTools(@Nonnull Config config) {
        this.config = config;
    }

    @Nonnull
    public Config getConfig() {
        return config;
    }

    /**
     * 字节数组散列
     *
     * @param algorithm 散列算法
     * @param bytes     被散列的字节数组
     * @return 散列结果，全小写字母
     */
    @Nonnull
    public String hash(@Nonnull String algorithm, @Nonnull byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(bytes);
            return bytesToHex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new XToolsException(e);
        }
    }

    /**
     * 文件散列
     *
     * @param algorithm 散列算法
     * @param file      被散列的文件
     * @return 散列结果，全小写字母
     */
    @Nonnull
    public String hash(@Nonnull String algorithm, @Nonnull File file) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            try (FileInputStream fileInputStream = new FileInputStream(file); DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest)) {
                byte[] buffer = new byte[this.getConfig().getBufferSize()];
                while (true) {
                    if (digestInputStream.read(buffer) <= 0) {
                        break;
                    }
                }
                return bytesToHex(digestInputStream.getMessageDigest().digest());
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new XToolsException(e);
        }
    }

    /**
     * 将字节数组转换成16进制字符串
     *
     * @param bytes 要转换的字节数组
     * @return 转换后的字符串，全小写字母
     */
    @Nonnull
    public static String bytesToHex(@Nonnull byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            chars[i << 1] = HEX[b >>> 4 & 0xf];
            chars[(i << 1) + 1] = HEX[b & 0xf];
        }
        return new String(chars);
    }

    public static class Config {
        private int bufferSize;

        public Config() {
            this.bufferSize = 131072;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }
    }
}
