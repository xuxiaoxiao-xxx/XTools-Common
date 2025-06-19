package me.xuxiaoxiao.xtools.common.hash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

public class XHashToolsTest {

    private XHashTools hashTools;

    @BeforeEach
    void setUp() {
        hashTools = new XHashTools(new XHashTools.Config());
    }

    @Test
    void getConfig() {
        assertNotNull(hashTools.getConfig());
        assertEquals(131072, hashTools.getConfig().getBufferSize());
        hashTools.getConfig().setBufferSize(123456);
        assertEquals(123456, hashTools.getConfig().getBufferSize());
    }

    @Test
    void hash_byteArray() {
        String data = "hello";
        String md5 = hashTools.hash(XHashTools.HASH_MD5, data.getBytes(StandardCharsets.UTF_8));
        assertEquals("5d41402abc4b2a76b9719d911017c592", md5);

        String sha1 = hashTools.hash(XHashTools.HASH_SHA1, data.getBytes(StandardCharsets.UTF_8));
        assertEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d", sha1);
    }

    @Test
    void hash_byteArray_invalidAlgorithm() {
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            hashTools.hash("NO_SUCH_ALGO", data);
        });
        assertTrue(exception.getCause() instanceof NoSuchAlgorithmException);
    }

    @Test
    void hash_file() throws Exception {
        File temp = File.createTempFile("xhashtools", ".txt");
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write("hello".getBytes(StandardCharsets.UTF_8));
        }
        String md5 = hashTools.hash(XHashTools.HASH_MD5, temp);
        assertEquals("5d41402abc4b2a76b9719d911017c592", md5);
        assertTrue(temp.delete());
    }

    @Test
    void hash_file_ioException() {
        File notExist = new File("this_file_should_not_exist_123456789.txt");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            hashTools.hash(XHashTools.HASH_MD5, notExist);
        });
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void bytesToHex() {
        byte[] bytes = {0x12, 0x34, (byte) 0xab, (byte) 0xcd};
        String hex = XHashTools.bytesToHex(bytes);
        assertEquals("1234abcd", hex);
    }
}