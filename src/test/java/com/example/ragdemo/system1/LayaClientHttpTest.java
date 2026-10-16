package com.example.ragdemo.system1;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.SystemDownException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The real System 1 client (Spring RestClient on the shared JDK HttpClient) against a fake Laya on localhost: every
 * status maps to the right state, the key is sent, and many calls leave no threads or clients behind.
 */
class LayaClientHttpTest {

    static final String SCORES = """
            {"answers": {"relevant": {"noul": 0.9}, "answers": {"noul": 0.8}, "injection": {"noul": 0.01}}}""";

    HttpServer server;
    final AtomicInteger status = new AtomicInteger(200);
    volatile String lastAuth;
    final KeyStore keys = mock(KeyStore.class);
    LayaClient laya;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            lastAuth = exchange.getRequestHeaders().getFirst("Authorization");
            exchange.getRequestBody().readAllBytes();
            byte[] body = (status.get() == 200 ? SCORES : "{}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        when(keys.system1Url()).thenReturn("http://127.0.0.1:" + server.getAddress().getPort());
        when(keys.system1Key()).thenReturn("");
        laya = new LayaClient(keys);
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void scoresComeBack_andEachStatusMeansSomething() {
        LayaClient.Scores s = laya.score("q", "passage");
        assertThat(s.relevant()).isEqualTo(0.9);
        assertThat(s.answers()).isEqualTo(0.8);
        assertThat(lastAuth).isNull();   // the public route: no key

        status.set(429);
        assertThatThrownBy(() -> laya.score("q", "p")).isInstanceOf(SystemDownException.class)
                .hasMessageContaining("status.system1.busy");
        status.set(503);
        assertThatThrownBy(() -> laya.score("q", "p")).hasMessageContaining("status.system1.waking");
        status.set(401);
        assertThatThrownBy(() -> laya.score("q", "p")).hasMessageContaining("status.invalidKey");
    }

    @Test
    void ownKeyIsSentAsBearer() {
        when(keys.system1Key()).thenReturn("my-laya-key");
        laya.score("q", "p");
        assertThat(lastAuth).isEqualTo("Bearer my-laya-key");
    }

    @Test
    void unreachableIsSystemOneDown() {
        server.stop(0);
        assertThatThrownBy(() -> laya.score("q", "p")).isInstanceOf(SystemDownException.class)
                .hasMessageContaining("status.unreachable");
    }

    @Test
    void manyCallsLeaveNoThreadsBehind() {
        laya.score("q", "warm-up");   // the shared client's own selector thread starts once
        int before = Thread.activeCount();
        for (int i = 0; i < 40; i++) laya.score("q", "passage " + i);
        assertThat(Thread.activeCount() - before).isLessThanOrEqualTo(2);   // one shared client: no thread per call
    }
}
