package com.example.ragdemo.system2;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.SystemDownException;
import com.openai.core.http.Headers;
import com.openai.errors.RateLimitException;
import com.openai.errors.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemTwoClientTest {

    KeyStore keys = mock(KeyStore.class);
    List<String> called = new ArrayList<>();

    @BeforeEach
    void keys() {
        when(keys.system2Key()).thenReturn("sk-or-v1-test");
        when(keys.system2BaseUrl()).thenReturn("https://openrouter.ai/api/v1");
        when(keys.system2Models()).thenReturn(List.of("a:free", "b:free", "c:free"));
    }

    /** A client whose models answer as {@code behaviour} says. */
    SystemTwoClient client(Function<String, ChatResponse> behaviour) {
        return new SystemTwoClient(keys) {
            @Override
            protected ChatResponse call(String baseUrl, String key, String model, String system, String user) {
                called.add(model);
                return behaviour.apply(model);
            }
        };
    }

    static ChatResponse answer(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    static RateLimitException busy() {
        return RateLimitException.builder().headers(Headers.builder().build()).build();
    }

    @Test
    void firstModelThatAnswersWins() {
        SystemTwoClient.Answer a = client(m -> answer("hello from " + m)).generate("sys", "q");
        assertThat(a.text()).isEqualTo("hello from a:free");
        assertThat(a.model()).isEqualTo("a:free");
        assertThat(called).containsExactly("a:free");
    }

    @Test
    void busyOrEmptyModelHandsOverToTheNextInOrder() {
        SystemTwoClient.Answer a = client(m -> switch (m) {
            case "a:free" -> throw busy();
            case "b:free" -> answer("  ");
            default -> answer("from c");
        }).generate("sys", "q");

        assertThat(called).containsExactly("a:free", "b:free", "c:free");
        assertThat(a.model()).isEqualTo("c:free");
        assertThat(a.skipped()).containsExactly("a:free: busy (429 rate limit)", "b:free: empty answer");
    }

    @Test
    void wrongKeyStopsAtOnce() {
        assertThatThrownBy(() -> client(m -> {
            throw UnauthorizedException.builder().headers(Headers.builder().build()).build();
        }).generate("sys", "q")).isInstanceOf(SystemDownException.class).hasMessageContaining("invalid API key");
        assertThat(called).containsExactly("a:free");
    }

    @Test
    void allModelsFailingIsSystemDown() {
        assertThatThrownBy(() -> client(m -> {
            throw busy();
        }).generate("sys", "q")).isInstanceOf(SystemDownException.class).hasMessageContaining("every model failed");
        assertThat(called).hasSize(3);
    }

    @Test
    void noKeyIsSystemDownWithoutAnyCall() {
        when(keys.system2Key()).thenReturn("");
        assertThatThrownBy(() -> client(m -> answer("x")).generate("sys", "q"))
                .isInstanceOf(SystemDownException.class).hasMessageContaining("no API key");
        assertThat(called).isEmpty();
    }
}
