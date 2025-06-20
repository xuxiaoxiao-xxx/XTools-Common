package me.xuxiaoxiao.xtools.common.http.impl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

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
    void testCharsetQueryHeaderContent() throws Exception {
        File temp = File.createTempFile("test", ".txt");
        Files.write(temp.toPath(), Collections.singletonList("filecontent"));

        XRequest req = XRequest.POST("http://a.com/api")
                .charset("utf-8")
                .query("q1", "q1Value")
                .query("q1", "q1Append")
                .query("q2", "q2Value")
                .query("q2", "q2 override", true)
                .header("H1", "H1Value")
                .header("H1", "H1Append")
                .header("H2", "H2Value")
                .header("h2", "H2Override", true)
                .content("c1", "c1Value")
                .content("c2", "c2Value")
                .content("c2", "c2Override", true)
                .content("c3", temp)
                .content("c4", new XRequest.MultipartContent.Part("c4", 4))
                .content("c4", new XRequest.MultipartContent.Part("c4", 4, "utf-8"), true);

        assertEquals("http://a.com/api?q1=q1Value&q1=q1Append&q2=q2+override", req.getUrl());

        List<XRequest.KeyValue> headers = req.getHeaders();
        assertEquals(headers.size(), 5);
        assertEquals(headers.get(0), new XRequest.KeyValue("H1", "H1Value"));
        assertEquals(headers.get(1), new XRequest.KeyValue("H1", "H1Append"));
        assertEquals(headers.get(2), new XRequest.KeyValue("h2", "H2Override"));
        assertEquals(headers.get(3).getKey(), "Content-Type");
        assertTrue(headers.get(3).getValue().toString().startsWith("multipart/form-data; boundary="));
        assertEquals(headers.get(4).getKey(), "Content-Length");
        assertTrue(Long.parseLong(headers.get(4).getValue().toString()) > 0);

        assertTrue(req.getContent() instanceof XRequest.MultipartContent);
        assert temp.delete();
    }

    @Test
    void testSetUrl() {
        XRequest req = XRequest.GET("http://a.com/api?q1=q1Value&q2=&q3=q3Value&q3=q3+override");
        assertEquals("http://a.com/api?q1=q1Value&q2=&q3=q3Value&q3=q3+override", req.getUrl());
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
        Files.write(temp.toPath(), Collections.singletonList("filecontent"));
        XRequest.MultipartContent content = new XRequest.MultipartContent();
        content.part("file", temp);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        content.writeToStream(out);

        String result = out.toString(content.charset());
        assertTrue(result.contains("filename="));
        assertTrue(result.contains("filecontent"));
        assert temp.delete();
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
        Files.write(temp.toPath(), fileStr.getBytes(StandardCharsets.UTF_8));
        XRequest.FileContent content = new XRequest.FileContent(temp);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        content.writeToStream(out);

        assertEquals(fileStr, out.toString(content.charset()));
        assertEquals(content.contentLength(), temp.length());
        assertTrue(temp.delete());
    }
}