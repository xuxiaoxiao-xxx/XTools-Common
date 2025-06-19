package me.xuxiaoxiao.xtools.common.http.impl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XRequestTest {

    @Test
    void testGET() {
        XRequest req = XRequest.GET("http://a.com/api");
        assertEquals("GET", req.getMethod());
        assertEquals("http://a.com/api", req.getUrl());
    }

    @Test
    void testPOST() {
        XRequest req = XRequest.POST("http://a.com/api");
        assertEquals("POST", req.getMethod());
        assertEquals("http://a.com/api", req.getUrl());
    }

    @Test
    void testPUT() {
        XRequest req = XRequest.PUT("http://a.com/api");
        assertEquals("PUT", req.getMethod());
        assertEquals("http://a.com/api", req.getUrl());
    }

    @Test
    void testPATCH() {
        XRequest req = XRequest.PATCH("http://a.com/api");
        assertEquals("PATCH", req.getMethod());
        assertEquals("http://a.com/api", req.getUrl());
    }

    @Test
    void testDELETE() {
        XRequest req = XRequest.DELETE("http://a.com/api");
        assertEquals("DELETE", req.getMethod());
        assertEquals("http://a.com/api", req.getUrl());
    }

    @Test
    void testHEAD() {
        XRequest req = XRequest.HEAD("http://a.com/api");
        assertEquals("HEAD", req.getMethod());
        assertEquals("http://a.com/api", req.getUrl());
    }

    @Test
    void testOPTIONS() {
        XRequest req = XRequest.OPTIONS("http://a.com/api");
        assertEquals("OPTIONS", req.getMethod());
        assertEquals("http://a.com/api", req.getUrl());
    }

    @Test
    void testCharsetQueryHeaderContent() {
        XRequest req = XRequest.GET("http://a.com/api")
                .charset("utf-8")
                .query("key", "test")
                .query("key", "over ride", true);
        assertTrue(req.getUrl().contains("key=over+ride"));
    }

    @Test
    void testUrlencodedContentParamAndWrite() throws IOException {
        XRequest.UrlencodedContent content = new XRequest.UrlencodedContent();
        content.param("a", "1");
        content.param("b", "2");
        content.param("a", "3", true); // 覆盖a

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        content.writeToStream(out);

        String result = out.toString(content.charset());
        assertTrue(result.contains("a=3"));
        assertTrue(result.contains("b=2"));
        assertEquals(content.contentType(), "application/x-www-form-urlencoded; charset=utf-8");
        assertEquals(content.contentLength(), result.getBytes(content.charset()).length);
    }

    @Test
    void testMultipartContentPartAndWrite() throws IOException {
        XRequest.MultipartContent content = new XRequest.MultipartContent();
        content.part("foo", "bar");
        content.part("foo", "baz", true); // 覆盖foo

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        content.writeToStream(out);

        String result = out.toString(content.charset());
        assertTrue(result.contains("name=\"foo\""));
        assertTrue(result.contains("baz"));
        assertTrue(content.contentType().startsWith("multipart/form-data; boundary="));
        assertEquals(content.contentLength(), out.size());
    }

    @Test
    void testMultipartContentWithFile() throws IOException {
        File temp = File.createTempFile("test", ".txt");
        Files.write(temp.toPath(), Arrays.asList("filecontent"));
        XRequest.MultipartContent content = new XRequest.MultipartContent();
        content.part("file", temp);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        content.writeToStream(out);

        String result = out.toString(content.charset());
        assertTrue(result.contains("filename="));
        assertTrue(result.contains("filecontent"));
        temp.delete();
    }

    @Test
    void testStringContent() throws IOException {
        String str = "hello世界";
        XRequest.StringContent content = new XRequest.StringContent("text/plain", str);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        content.writeToStream(out);

        assertEquals(str, out.toString(content.charset()));
        assertEquals(content.contentType(), "text/plain; charset=utf-8");
        assertEquals(content.contentLength(), str.getBytes(content.charset()).length);
    }

    @Test
    void testFileContent() throws IOException {
        File temp = File.createTempFile("test", ".txt");
        String fileStr = "abc123";
        Files.write(temp.toPath(), fileStr.getBytes("utf-8"));
        XRequest.FileContent content = new XRequest.FileContent(temp);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        content.writeToStream(out);

        assertEquals(fileStr, out.toString(content.charset()));
        assertEquals(content.contentLength(), temp.length());
        temp.delete();
    }
}