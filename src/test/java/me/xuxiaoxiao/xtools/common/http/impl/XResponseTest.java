package me.xuxiaoxiao.xtools.common.http.impl;

import com.sun.net.httpserver.HttpServer;
import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class XResponseTest {
    static int httpPort = 18080;
    static HttpServer httpServer;

    @BeforeAll
    static void startServer() throws Exception {
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
    static void stopServer() {
        httpServer.stop(0);
    }

    @Test
    void testSuccess() throws Exception {
        XExecutor executor = new XExecutor(new XHttpTools.Config());
        XRequest request = new XRequest("POST", "http://localhost:" + httpPort + "/testSuccess");
        try (XResponse response = executor.execute(request)) {
            assertEquals(response.getStatusCode(), 200);
            assertTrue(response.isSuccess());
            assertEquals("http-success", response.asString());
        }
    }

    @Test
    void testFail() throws Exception {
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
}