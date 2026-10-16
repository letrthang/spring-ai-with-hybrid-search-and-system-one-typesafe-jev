package com.example.ragdemo.weaviate;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.ServiceStatus;
import com.example.ragdemo.rag.SystemDownException;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.time.Duration;

import static com.example.ragdemo.rag.ServiceStatus.Service.WEAVIATE;

/**
 * Is Weaviate usable for RAG?
 * <ol>
 *   <li>the cluster is ready ({@code /v1/.well-known/ready}, no key),</li>
 *   <li>the key is accepted ({@code /v1/schema} with the key),</li>
 *   <li>the collection exists and has passages — an empty collection is 🟡: a RAG demo with nothing to find shows
 *       nothing.</li>
 * </ol>
 */
@Component
public class WeaviateHealth {

    private final KeyStore keys;
    private final WeaviateConnector weaviate;

    public WeaviateHealth(KeyStore keys, WeaviateConnector weaviate) {
        this.keys = keys;
        this.weaviate = weaviate;
    }

    public ServiceStatus check() {
        String url = keys.weaviateUrl();
        if (url.isBlank() || keys.weaviateKey().isBlank()) {
            return ServiceStatus.down(WEAVIATE, "status.weaviate.noKey");
        }
        String base = withScheme(url);
        try {
            HttpResponse<String> ready = Http.get(base + "/v1/.well-known/ready", null, Duration.ofSeconds(15));
            if (ready.statusCode() != 200) {
                return ServiceStatus.down(WEAVIATE, "status.weaviate.notReady", ready.statusCode());
            }
            HttpResponse<String> schema = Http.get(base + "/v1/schema", keys.weaviateKey(), Duration.ofSeconds(15));
            if (schema.statusCode() == 401 || schema.statusCode() == 403) {
                return ServiceStatus.down(WEAVIATE, "status.invalidKey");
            }
            if (schema.statusCode() != 200) {
                return ServiceStatus.down(WEAVIATE, "status.http", schema.statusCode());
            }
            long count = weaviate.call("count", DemoDocCollection::count);
            if (count < 0) {
                return ServiceStatus.waiting(WEAVIATE, "status.weaviate.noCollection");
            }
            if (count == 0) {
                return ServiceStatus.waiting(WEAVIATE, "status.weaviate.empty");
            }
            return ServiceStatus.ready(WEAVIATE, "status.weaviate.ready", count);
        } catch (SystemDownException e) {
            return e.toStatus();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ServiceStatus.down(WEAVIATE, "status.interrupted");
        } catch (Exception e) {
            return ServiceStatus.down(WEAVIATE, "status.unreachable", Http.describe(e));
        }
    }

    /** Weaviate Cloud shows the REST endpoint without "https://"; people paste it either way. */
    static String withScheme(String url) {
        return url.startsWith("http://") || url.startsWith("https://") ? url : "https://" + url;
    }
}
