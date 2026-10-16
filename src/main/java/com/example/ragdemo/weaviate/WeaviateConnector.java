package com.example.ragdemo.weaviate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.SystemDownException;
import io.weaviate.client6.v1.api.Authentication;
import io.weaviate.client6.v1.api.WeaviateClient;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.example.ragdemo.rag.ServiceStatus.Service.WEAVIATE;

/**
 * The only place that opens a {@link WeaviateClient}. Every call reads the URL and key from {@link KeyStore} first;
 * the client is reused only while they stay the same (opening one costs a few round trips), and is rebuilt as soon
 * as a new key is saved.
 *
 * <p>Any failure becomes a {@link SystemDownException}: Weaviate is required, so the caller stops.
 */
@Component
public class WeaviateConnector {

    private static final Logger log = LoggerFactory.getLogger(WeaviateConnector.class);

    /** A piece of work against Weaviate. */
    @FunctionalInterface
    public interface Work<T> {
        T apply(WeaviateClient client) throws Exception;
    }

    private final KeyStore keys;
    private WeaviateClient client;
    private String builtFor;   // url + key the current client was opened with

    public WeaviateConnector(KeyStore keys) {
        this.keys = keys;
    }

    public <T> T call(String what, Work<T> work) {
        WeaviateClient used = null;
        try {
            used = client();
            return work.apply(used);
        } catch (SystemDownException e) {
            throw e;
        } catch (Exception e) {
            log.warn("🗄️ Weaviate {} failed: {}", what, Http.describe(e));
            if (used != null) reset(used);   // the next call opens a fresh client
            throw new SystemDownException(WEAVIATE, "status.weaviate.failed", what, Http.describe(e));
        }
    }

    /**
     * Drops {@code failed} only if it is still the current client. Calls running in parallel (BM25 + vector, the
     * health checks) may already use a newer client — closing that one would break them too.
     */
    private synchronized void reset(WeaviateClient failed) {
        if (client == failed) close();
    }

    private synchronized WeaviateClient client() throws Exception {
        String url = keys.weaviateUrl();
        String key = keys.weaviateKey();
        if (url.isBlank() || key.isBlank()) {
            throw new SystemDownException(WEAVIATE, "status.weaviate.noKey");
        }
        String identity = url + "\n" + key;
        if (client == null || !identity.equals(builtFor)) {
            close();
            log.info("🗄️ opening a Weaviate client for {}", WeaviateHealth.withScheme(url));   // never the key
            client = open(WeaviateHealth.withScheme(url), key);
            builtFor = identity;
        }
        return client;
    }

    /**
     * Weaviate Cloud (https) or, for local experiments, a plain {@code http://host:port} Weaviate with gRPC on 50051.
     * Timeouts in seconds: connect 10, query 30, insert 60. (Tests override this to reach a Docker Weaviate.)
     */
    protected WeaviateClient open(String url, String key) {
        if (url.startsWith("https://")) {
            return WeaviateClient.connectToWeaviateCloud(url, key, c -> c.timeout(10, 30, 60));
        }
        URI uri = URI.create(url);
        int port = uri.getPort() > 0 ? uri.getPort() : 80;
        return WeaviateClient.connectToCustom(c -> c.scheme("http").httpHost(uri.getHost()).httpPort(port)
                .grpcHost(uri.getHost()).grpcPort(50051).authentication(Authentication.apiKey(key)).timeout(10, 30, 60));
    }

    @PreDestroy
    synchronized void close() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception ignored) {
                // closing a dead connection: nothing to do
            }
        }
        client = null;
        builtFor = null;
    }
}
