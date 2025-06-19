package me.xuxiaoxiao.xtools.common.http.impl;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.security.x509.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XExecutorTest {
    static int httpsPort = 18080;
    static SSLContext sslContext;
    static HttpsServer httpsServer;

    static SSLContext genSslContext() throws Exception {
        char[] password = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, password);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X500Name owner = new X500Name("CN=localhost");
        X509CertInfo certInfo = new X509CertInfo();
        certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber((int) (System.currentTimeMillis() / 1000)));
        certInfo.set(X509CertInfo.SUBJECT, owner);
        certInfo.set(X509CertInfo.ISSUER, owner);
        certInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(new java.util.Date(), new java.util.Date(System.currentTimeMillis() + 86400000L)));
        certInfo.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
        certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid)));
        X509CertImpl cert = new X509CertImpl(certInfo);
        cert.sign(keyPair.getPrivate(), "SHA256withRSA");

        ks.setKeyEntry("alias", keyPair.getPrivate(), password, new java.security.cert.Certificate[]{cert});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);
        TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] xcs, String string) {
            }

            public void checkServerTrusted(X509Certificate[] xcs, String string) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), trustAll, new java.security.SecureRandom());

        return sslContext;
    }

    @BeforeAll
    static void startServer() throws Exception {
        sslContext = genSslContext();
        httpsServer = HttpsServer.create(new InetSocketAddress(httpsPort), 0);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        httpsServer.createContext("/test", exchange -> {
            String response = "hello-https";
            if ("reqCookie=hello".equals(exchange.getRequestHeaders().getFirst("Cookie"))) {
                exchange.getResponseHeaders().set("Set-Cookie", "rspCookie=world; Path=/;");
            }
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        httpsServer.start();
    }

    @AfterAll
    static void stopServer() {
        httpsServer.stop(0);
    }

    @Test
    void testExecutePost() throws Exception {
        URI uri = URI.create("https://localhost:" + httpsPort + "/test");

        XHttpTools.Config config = new XHttpTools.Config();
        config.getCookieManager().put(uri, new HashMap<String, List<String>>() {{
            put("Set-Cookie", java.util.Collections.singletonList("reqCookie=hello; Path=/;"));
        }});

        XExecutor executor = new XExecutor(config);

        XRequest request = new XRequest("POST", uri.toString());
        request.setContent(new XRequest.StringContent(XRequest.MIME_JSON, "{\"hello\":\"world\"}") {
            @Override
            public long contentLength() {
                return -1;
            }
        });

        XResponse response = executor.execute(request);

        List<String> cookies = config.getCookieManager().get(uri, new java.util.HashMap<>()).get("Cookie");
        assertTrue(cookies.contains("reqCookie=hello"));
        assertTrue(cookies.contains("rspCookie=world"));

        assertEquals(200, response.getStatusCode());
        assertEquals("hello-https", response.asString());
    }
}