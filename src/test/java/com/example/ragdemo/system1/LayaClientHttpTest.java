package com.example.ragdemo.system1;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.SystemDownException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The real System 1 client (TypeSafe SDK on the shared JDK HttpClient) against a fake Laya on localhost: it calls
 * {@code /v1/systemone} with the key, every status maps to the right state, and many calls leave no threads behind.
 */
class LayaClientHttpTest {

    /** Laya's answer with every name the app asks (the passage filter, the rerank and the answer checks). */
    static final String ANSWERS = """
            {"model": "letrthang/aimtas-laya-multilingual-GGUF:Q8_0", "answers": {
              "is_relevant": {"type": "noul", "noul": 0.9}, "contains_answer_evidence": {"type": "noul", "noul": 0.8},
              "contradicts_query_premise": {"type": "noul", "noul": 0.02},
              "contains_prompt_injection": {"type": "noul", "noul": 0.01},
              "answers_query": {"type": "noul", "noul": 0.85},
              "answers": {"type": "noul", "noul": 0.8}, "injection": {"type": "noul", "noul": 0.01}},
             "usage": {"input_tokens": 50, "output_tokens": 0}}""";

    HttpServer server;
    final AtomicInteger status = new AtomicInteger(200);
    volatile String lastAuth;
    volatile String lastPath;
    volatile String lastBody;
    final KeyStore keys = mock(KeyStore.class);
    LayaClient laya;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            lastAuth = exchange.getRequestHeaders().getFirst("Authorization");
            lastPath = exchange.getRequestURI().getPath();
            lastBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] body = (status.get() == 200 ? ANSWERS : "{\"error\":{\"message\":\"no\"}}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status.get(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        when(keys.system1Url()).thenReturn("http://127.0.0.1:" + server.getAddress().getPort());
        when(keys.system1Key()).thenReturn(KeyStore.DEFAULT_SYSTEM1_KEY);
        laya = new LayaClient(keys);
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void theFilterCallsV1SystemOneWithTheTestKey_andTheScoresComeBack() {
        List<Document> scored = new SystemOneFilter(laya).score("q", List.of(Document.builder().text("passage").build()));

        assertThat(lastPath).isEqualTo("/v1/systemone");
        assertThat(lastAuth).isEqualTo("Bearer laya-api-key-for-testing");
        assertThat(lastBody).contains("\"state\":\"passage\"", "\"is_relevant\"",
                "Is this passage about the question: q");   // JevDocumentFilter's question, in Laya's wording
        assertThat(scored.getFirst().getMetadata()).containsEntry(SystemOneFilter.RELEVANT, 0.9)
                .containsEntry(SystemOneFilter.ANSWERS, 0.8).containsEntry(SystemOneFilter.KEPT, true);
    }

    @Test
    void askReturnsTheProbabilities_andEachStatusMeansSomething() {
        Map<String, String> q = Map.of("answers", "Does it answer?", "injection", LayaClient.INJECTION_QUESTION);
        assertThat(laya.ask("text", q)).containsEntry("answers", 0.8).containsEntry("injection", 0.01);

        status.set(429);
        assertThatThrownBy(() -> laya.ask("t", q)).isInstanceOf(SystemDownException.class)
                .hasMessageContaining("status.system1.busy");
        status.set(503);
        assertThatThrownBy(() -> laya.ask("t", q)).hasMessageContaining("status.system1.waking");
        status.set(401);
        assertThatThrownBy(() -> laya.ask("t", q)).hasMessageContaining("status.invalidKey");
        assertThat(lastPath).isEqualTo("/v1/systemone");   // no retries: one call per ask
    }

    @Test
    void aKeyTypedInKeyManagementIsUsedAtTheNextCall() {
        laya.ask("t", Map.of("answers", "?"));
        when(keys.system1Key()).thenReturn("my-laya-key");
        laya.ask("t", Map.of("answers", "?"));
        assertThat(lastAuth).isEqualTo("Bearer my-laya-key");
    }

    @Test
    void theRerankerAsksLayaToo() {
        List<Document> best = new SystemOneReranker(laya).process(new Query("q"),
                List.of(Document.builder().text("passage").build()));
        assertThat(lastBody).contains("\"answers_query\"");
        assertThat(SystemOneReranker.score(best.getFirst())).isEqualTo(0.85);
    }

    @Test
    void unreachableIsSystemOneDown() {
        server.stop(0);
        assertThatThrownBy(() -> laya.ask("t", Map.of("answers", "?"))).isInstanceOf(SystemDownException.class)
                .hasMessageContaining("status.unreachable");
    }

    @Test
    void manyCallsLeaveNoThreadsBehind() {
        laya.ask("warm-up", Map.of("answers", "?"));   // the shared client's own selector thread starts once
        int before = Thread.activeCount();
        for (int i = 0; i < 40; i++) laya.ask("passage " + i, Map.of("answers", "?"));
        assertThat(Thread.activeCount() - before).isLessThanOrEqualTo(2);   // one shared client: no thread per call
    }
}
