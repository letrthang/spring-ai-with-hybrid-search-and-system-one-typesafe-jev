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
            return ServiceStatus.down(WEAVIATE, "no URL or API key — enter them in 🔑 Key Management");
        }
        String base = withScheme(url);
        try {
            HttpResponse<String> ready = Http.get(base + "/v1/.well-known/ready", null, Duration.ofSeconds(15));
            if (ready.statusCode() != 200) {
                return ServiceStatus.down(WEAVIATE, "cluster not ready (HTTP " + ready.statusCode() + ")");
            }
            HttpResponse<String> schema = Http.get(base + "/v1/schema", keys.weaviateKey(), Duration.ofSeconds(15));
            if (schema.statusCode() == 401 || schema.statusCode() == 403) {
                return ServiceStatus.down(WEAVIATE, "invalid API key — check 🔑 Key Management");
            }
            if (schema.statusCode() != 200) {
                return ServiceStatus.down(WEAVIATE, "answered HTTP " + schema.statusCode());
            }
            long count = weaviate.call("count", DemoDocCollection::count);
            if (count < 0) {
                return ServiceStatus.waiting(WEAVIATE, "no collection yet — load sample data in 🔎 Search Data");
            }
            if (count == 0) {
                return ServiceStatus.waiting(WEAVIATE, "collection is empty — load sample data in 🔎 Search Data");
            }
            return ServiceStatus.ready(WEAVIATE, count + " passages");
        } catch (SystemDownException e) {
            return e.toStatus();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ServiceStatus.down(WEAVIATE, "check interrupted");
        } catch (Exception e) {
            return ServiceStatus.down(WEAVIATE, "unreachable (" + Http.describe(e) + ")");
        }
    }

    /** Weaviate Cloud shows the REST endpoint without "https://"; people paste it either way. */
    static String withScheme(String url) {
        return url.startsWith("http://") || url.startsWith("https://") ? url : "https://" + url;
    }
}
