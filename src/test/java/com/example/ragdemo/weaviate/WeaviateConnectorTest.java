package com.example.ragdemo.weaviate;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.SystemDownException;
import io.weaviate.client6.v1.api.WeaviateClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeaviateConnectorTest {

    final KeyStore keys = mock(KeyStore.class);
    final List<WeaviateClient> opened = new ArrayList<>();

    WeaviateConnector connector() {
        when(keys.weaviateUrl()).thenReturn("https://demo.weaviate.cloud");
        when(keys.weaviateKey()).thenReturn("key");
        return new WeaviateConnector(keys) {
            @Override
            protected WeaviateClient open(String url, String key) {
                WeaviateClient c = mock(WeaviateClient.class);
                opened.add(c);
                return c;
            }
        };
    }

    @Test
    void aStaleConnectionIsClosedThenTheCallRunsOnceMoreOnAFreshClient() throws Exception {
        WeaviateConnector connector = connector();
        AtomicInteger tries = new AtomicInteger();
        String result = connector.call("search", c -> {
            if (tries.incrementAndGet() == 1) throw new IOException("Connection reset");
            return "ok";
        });
        assertThat(result).isEqualTo("ok");
        assertThat(opened).hasSize(2);
        verify(opened.getFirst()).close();   // the stale one was closed before the fresh one was used
    }

    @Test
    void anAnswerFromWeaviateIsNotRetried() {
        WeaviateConnector connector = connector();
        AtomicInteger tries = new AtomicInteger();
        assertThatThrownBy(() -> connector.call("search", c -> {
            tries.incrementAndGet();
            throw new IllegalArgumentException("HTTP 422: invalid filter");
        })).isInstanceOf(SystemDownException.class);
        assertThat(tries).hasValue(1);
    }

    @Test
    void anAnswerFromWeaviateKeepsTheClientForTheCallsRunningOnIt() throws Exception {
        WeaviateConnector connector = connector();
        assertThatThrownBy(() -> connector.call("count", c -> {
            throw new IllegalStateException("UNKNOWN: no such prop with name 'kind' found in class 'DemoDoc'");
        })).isInstanceOf(SystemDownException.class);
        String result = connector.call("search", c -> "ok");
        assertThat(result).isEqualTo("ok");
        assertThat(opened).hasSize(1);   // the same client: not closed, not reopened
        verify(opened.getFirst(), never()).close();
    }

    @Test
    void aClientClosedMeanwhileIsRetriedOnAFreshOne() {
        WeaviateConnector connector = connector();
        AtomicInteger tries = new AtomicInteger();
        String result = connector.call("count", c -> {
            if (tries.incrementAndGet() == 1) throw new IllegalStateException("Connection pool shut down");
            return "ok";
        });
        assertThat(result).isEqualTo("ok");
        assertThat(opened).hasSize(2);
    }

    @Test
    void recognisesBrokenConnections() {
        assertThat(WeaviateConnector.staleConnection(new RuntimeException(new IOException("closed")))).isTrue();
        assertThat(WeaviateConnector.staleConnection(new RuntimeException("UNAVAILABLE: io exception"))).isTrue();
        assertThat(WeaviateConnector.staleConnection(new IllegalStateException("HTTP 401"))).isFalse();
    }
}
