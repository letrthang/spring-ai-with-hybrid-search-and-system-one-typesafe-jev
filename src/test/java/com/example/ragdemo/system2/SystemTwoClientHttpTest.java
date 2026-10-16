package com.example.ragdemo.system2;

import com.example.ragdemo.keys.KeyStore;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The real System 2 client against a fake OpenAI-compatible server on localhost: the model goes in the request, the
 * clients are reused while the key stays the same, and a new key gets new clients (the stale ones closed first).
 */
class SystemTwoClientHttpTest {

    static final String ANSWER = """
            {"id": "c1", "object": "chat.completion", "created": 1, "model": "a:free",
             "choices": [{"index": 0, "finish_reason": "stop", "message": {"role": "assistant", "content": "Hello [1]"}}],
             "usage": {"prompt_tokens": 5, "completion_tokens": 2, "total_tokens": 7}}
            """;

    HttpServer server;
    final List<String> seen = new CopyOnWriteArrayList<>();   // "Bearer key | request body"
    final KeyStore keys = mock(KeyStore.class);
    SystemTwoClient client;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            seen.add(exchange.getRequestHeaders().getFirst("Authorization") + " | "
                    + new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = ANSWER.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        when(keys.system2BaseUrl()).thenReturn("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
        when(keys.system2Models()).thenReturn(List.of("a:free", "b:free"));
        client = new SystemTwoClient(keys);
    }

    @AfterEach
    void stop() {
        client.close();
        server.stop(0);
    }

    @Test
    void sendsTheModelAndKey_andReusesTheClientsForTheSameKey() {
        when(keys.system2Key()).thenReturn("key-one");
        SystemTwoClient.Answer answer = client.generate("system", "question");
        assertThat(answer.text()).isEqualTo("Hello [1]");
        assertThat(answer.model()).isEqualTo("a:free");
        assertThat(seen.getFirst()).startsWith("Bearer key-one | ").contains("\"model\":\"a:free\"");

        String base = keys.system2BaseUrl();
        assertThat(client.chatModel(base, "key-one")).isSameAs(client.chatModel(base, "key-one"));
    }

    @Test
    void aNewKeyClosesTheStaleClientsAndOpensNewOnes() {
        String base = keys.system2BaseUrl();
        var first = client.chatModel(base, "key-one");
        var second = client.chatModel(base, "key-two");   // key-one's clients are closed before these open
        assertThat(second).isNotSameAs(first);

        when(keys.system2Key()).thenReturn("key-two");
        assertThat(client.generate("system", "question").text()).isEqualTo("Hello [1]");
        assertThat(seen.getLast()).startsWith("Bearer key-two | ");
    }
}
