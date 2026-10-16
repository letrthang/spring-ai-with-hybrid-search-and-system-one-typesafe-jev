package com.example.ragdemo.weaviate;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.ServiceStatus;
import io.weaviate.client6.v1.api.Authentication;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The real thing in Docker: Weaviate with API-key auth and a small local vectorizer (model2vec), because a local
 * Weaviate cannot use Weaviate Cloud's built-in embeddings. Everything else — collection, loading, BM25, nearText,
 * RRF, health — is the production code. Skipped when Docker is not running.
 */
class WeaviateIT {

    static final String KEY = "test-admin-key";
    static Network network;
    static GenericContainer<?> vectorizer;
    static GenericContainer<?> weaviate;
    static WeaviateConnector connector;
    static HybridRetriever retriever;
    static KeyStore keys;
    static String url;

    @BeforeAll
    static void start() throws Exception {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not running");
        network = Network.newNetwork();
        vectorizer = new GenericContainer<>("cr.weaviate.io/semitechnologies/model2vec-inference:minishlab-potion-base-8M")
                .withNetwork(network).withNetworkAliases("m2v").withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/.well-known/ready").forPort(8080).forStatusCode(204)
                        .withStartupTimeout(Duration.ofMinutes(3)));
        vectorizer.start();
        weaviate = new GenericContainer<>("cr.weaviate.io/semitechnologies/weaviate:1.40.0")
                .withNetwork(network).withExposedPorts(8080, 50051)
                .withEnv("ENABLE_MODULES", "text2vec-model2vec")
                .withEnv("MODEL2VEC_INFERENCE_API", "http://m2v:8080")
                .withEnv("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "false")
                .withEnv("AUTHENTICATION_APIKEY_ENABLED", "true")
                .withEnv("AUTHENTICATION_APIKEY_ALLOWED_KEYS", KEY)
                .withEnv("AUTHENTICATION_APIKEY_USERS", "demo")
                .withEnv("PERSISTENCE_DATA_PATH", "/var/lib/weaviate")
                .waitingFor(Wait.forHttp("/v1/.well-known/ready").forPort(8080).withStartupTimeout(Duration.ofMinutes(2)));
        weaviate.start();

        url = "http://" + weaviate.getHost() + ":" + weaviate.getMappedPort(8080);
        keys = mock(KeyStore.class);
        when(keys.weaviateUrl()).thenReturn(url);
        when(keys.weaviateKey()).thenReturn(KEY);
        int grpc = weaviate.getMappedPort(50051);
        connector = new WeaviateConnector(keys) {
            @Override
            protected WeaviateClient open(String u, String key) {
                URI uri = URI.create(u);
                return WeaviateClient.connectToCustom(c -> c.scheme("http").httpHost(uri.getHost())
                        .httpPort(uri.getPort()).grpcHost(uri.getHost()).grpcPort(grpc)
                        .authentication(Authentication.apiKey(key)).timeout(10, 30, 60));
            }
        };
        retriever = new HybridRetriever(connector);
    }

    @AfterAll
    static void stop() {
        if (connector != null) connector.close();
        if (weaviate != null) weaviate.stop();
        if (vectorizer != null) vectorizer.stop();
        if (network != null) network.close();
    }

    @Test
    void loadSearchAndHealth() {
        WeaviateHealth health = new WeaviateHealth(keys, connector);
        assertThat(health.check().reason()).contains("no collection yet");

        // the local vectorizer instead of text2vec-weaviate (a local Weaviate has no Weaviate Cloud embeddings)
        var local = DemoDocCollection.definition(VectorConfig.text2vecModel2Vec(v -> v.sourceProperties("title", "body")));
        connector.call("create", c -> DemoDocCollection.ensure(c, local));
        assertThat(health.check().state()).isEqualTo(ServiceStatus.State.WAITING);
        new SampleDataLoader(connector).load(local);
        int loaded = new SampleDataLoader(connector).load(local);   // loading again starts fresh, never duplicates
        assertThat(connector.call("count", DemoDocCollection::count)).isEqualTo(loaded);
        assertThat(health.check().isReady()).isTrue();

        // keyword question: BM25 finds the CRISPR passage by its exact terms
        HybridRetriever.Result keyword = retriever.search("Cas9 PAM NGG", HybridRetriever.Mode.HYBRID, 4);
        assertThat(keyword.hits().getFirst().title()).contains("How Cas9 cuts DNA");
        assertThat(keyword.hits().getFirst().bm25Rank()).isEqualTo(1);
        assertThat(keyword.bm25Count()).isPositive();
        assertThat(keyword.vectorCount()).isPositive();

        // every hybrid hit has an RRF score; BM25-only mode has no vector ranks
        assertThat(keyword.hits()).allMatch(h -> h.rrfScore() != null);
        assertThat(retriever.search("polymerase", HybridRetriever.Mode.BM25, 5).hits())
                .allMatch(h -> h.vectorRank() == null && h.bm25Rank() != null);

        // Spring AI DocumentRetriever: top 4 as Documents with the ranks in metadata
        assertThat(retriever.retrieve(new org.springframework.ai.rag.Query("why does warm water hold less oxygen")))
                .hasSize(HybridRetriever.TOP_K)
                .allMatch(d -> d.getMetadata().containsKey("rrfScore"));

        assertThat(retriever.list(0, 5)).hasSize(5);
    }

    @Test
    void wrongKeyIsReportedAsInvalid() {
        KeyStore wrong = mock(KeyStore.class);
        when(wrong.weaviateUrl()).thenReturn(url);
        when(wrong.weaviateKey()).thenReturn("not-the-key");
        assertThat(new WeaviateHealth(wrong, connector).check().reason()).contains("invalid API key");
    }
}
