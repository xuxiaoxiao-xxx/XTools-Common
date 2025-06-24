package me.xuxiaoxiao.xtools.common.http.impl;

import com.sun.net.httpserver.HttpServer;
import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

public class XResponseTest {
    static int httpPort = 18080;
    static HttpServer httpServer;

    @BeforeAll
    public static void startServer() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
        httpServer.createContext("/testSuccess", exchange -> {
            String response = "http-success";
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=\"utf-8\"");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        httpServer.createContext("/testFail", exchange -> {
            String response = "http-fail";
            exchange.sendResponseHeaders(400, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        httpServer.start();
    }

    @AfterAll
    public static void stopServer() {
        httpServer.stop(0);
    }

    @Test
    public void testSuccess() throws Exception {
        XExecutor executor = new XExecutor(new XHttpTools.Config());
        XRequest request = new XRequest("POST", "http://localhost:" + httpPort + "/testSuccess");
        try (XResponse response = executor.execute(request)) {
            assertEquals(response.getStatusCode(), 200);
            assertTrue(response.isSuccess());
            assertEquals("http-success", response.asString());
        }
    }

    @Test
    public void testFail() throws Exception {
        XExecutor executor = new XExecutor(new XHttpTools.Config());
        XRequest request = new XRequest("POST", "http://localhost:" + httpPort + "/testFail");
        try (XResponse response = executor.execute(request);) {
            File temp = File.createTempFile("test", ".txt");

            assertEquals(response.getStatusCode(), 400);
            assertFalse(response.isSuccess());
            File ret = response.asFile(temp.getAbsolutePath());
            assertEquals("http-fail", XTools.fileToStr(ret, "utf-8"));
            assertTrue(temp.delete());
        }
    }

    @Test
    public void testGetStatusCodeFail() throws Exception {
        HttpURLConnection connection = Mockito.mock(HttpURLConnection.class);
        Mockito.when(connection.getResponseCode()).thenThrow(new IOException());

        try (XResponse response = new XResponse(connection)) {
            response.getStatusCode();
            throw new IllegalStateException("not reachable");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testAsStringFail() throws Exception {
        HttpURLConnection connection = Mockito.mock(HttpURLConnection.class);
        Mockito.when(connection.getResponseCode()).thenReturn(200);

        Mockito.when(connection.getInputStream()).thenReturn(null);
        try (XResponse response = new XResponse(connection)) {
            assertNull(response.asString());
        }

        Mockito.when(connection.getInputStream()).thenThrow(new IOException());
        try (XResponse response = new XResponse(connection)) {
            String ignore = response.asString();
            throw new IllegalStateException("not reachable");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testAsFileFail() throws Exception {
        HttpURLConnection connection = Mockito.mock(HttpURLConnection.class);
        Mockito.when(connection.getResponseCode()).thenReturn(200);

        Mockito.when(connection.getInputStream()).thenReturn(null);
        try (XResponse response = new XResponse(connection)) {
            assertNull(response.asFile(""));
        }

        Mockito.when(connection.getInputStream()).thenThrow(new IOException());
        try (XResponse response = new XResponse(connection)) {
            File ignore = response.asFile("");
            throw new IllegalStateException("not reachable");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testCloseFail() throws Exception {
        HttpURLConnection connection = Mockito.mock(HttpURLConnection.class);
        InputStream inStream = Mockito.mock(InputStream.class);

        Mockito.when(connection.getResponseCode()).thenReturn(200);
        Mockito.when(connection.getInputStream()).thenReturn(inStream);
        Mockito.doThrow(new IOException()).when(inStream).close();

        try (XResponse response = new XResponse(connection)) {
            InputStream ignore = response.getStream();
        }
        Mockito.verify(inStream, Mockito.times(1)).close();
        Mockito.verify(connection, Mockito.times(1)).disconnect();
    }
}