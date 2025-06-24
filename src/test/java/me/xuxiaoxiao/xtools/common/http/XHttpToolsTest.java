package me.xuxiaoxiao.xtools.common.http;

import me.xuxiaoxiao.xtools.common.http.impl.XExecutor;
import me.xuxiaoxiao.xtools.common.http.impl.XRequest;
import me.xuxiaoxiao.xtools.common.http.impl.XResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.KeyManagementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XHttpToolsTest {
    @Test
    public void testGetConfig() throws Exception {
        XHttpTools.Config config = new XHttpTools.Config();
        config.setConnectTimeout(12345);
        config.setReadTimeout(23456);
        config.setChunkLength(34567);
        config.setFollowRedirect(true);
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_NONE);
        config.setCookieManager(cm);
        HostnameVerifier hv = (s, sslSession) -> true;
        config.setHostnameVerifier(hv);
        SSLContext sc = SSLContext.getInstance("TLS");
        config.setSslContext(sc);
        XExecutor xe = new XExecutor(new XHttpTools.Config());
        config.setExecutor(xe);

        XHttpTools httpTools = new XHttpTools(config);
        XHttpTools.Config test = httpTools.getConfig();
        assertEquals(test.getConnectTimeout(), 12345);
        assertEquals(test.getReadTimeout(), 23456);
        assertEquals(test.getChunkLength(), 34567);
        assertTrue(test.isFollowRedirect());
        assertEquals(test.getCookieManager(), cm);
        assertEquals(test.getHostnameVerifier(), hv);
        assertEquals(test.getSslContext(), sc);
        assertEquals(test.getExecutor(), xe);
    }

    @Test
    public void testHttp() throws Exception {
        XExecutor executor = Mockito.mock(XExecutor.class);
        XHttpTools.Config config = new XHttpTools.Config();
        config.setExecutor(executor);

        try (XResponse ignored = new XHttpTools(config).http(XRequest.GET("http://a.com/api"))) {
            Mockito.verify(executor, Mockito.times(1)).execute(Mockito.any());
        }

        Mockito.when(executor.execute(Mockito.any())).thenThrow(new IOException());
        try (XResponse ignored = new XHttpTools(config).http(XRequest.GET("http://a.com/api"))) {
            throw new IllegalStateException("not reachable");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testSSLContextError() throws Exception {
        SSLContext sslContext = Mockito.mock(SSLContext.class);
        try (MockedStatic<SSLContext> sslContextMockedStatic = Mockito.mockStatic(SSLContext.class)) {
            sslContextMockedStatic.when(() -> SSLContext.getInstance(Mockito.any())).thenReturn(sslContext);
            Mockito.doThrow(new KeyManagementException()).when(sslContext).init(Mockito.any(), Mockito.any(), Mockito.any());
            try {
                XHttpTools.Config ignore = new XHttpTools.Config();
            } catch (Exception e) {
                assertTrue(e instanceof IllegalArgumentException);
            }
        }
    }
}