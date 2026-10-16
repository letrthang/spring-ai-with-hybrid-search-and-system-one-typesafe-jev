package com.example.ragdemo.weaviate;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.ServiceStatus;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.time.Duration;

import static com.example.ragdemo.rag.ServiceStatus.Service.WEAVIATE;

/**
 * Is Weaviate usable? 1) the server is ready ({@code /v1/.well-known/ready}, no key), 2) the key is accepted
 * ({@code /v1/schema} with the key).
 */
@Component
public class WeaviateHealth {

    private final KeyStore keys;

    public WeaviateHealth(KeyStore keys) {
        this.keys = keys;
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
            return switch (schema.statusCode()) {
                case 200 -> ServiceStatus.ready(WEAVIATE, "cluster ready, key OK");
                case 401, 403 -> ServiceStatus.down(WEAVIATE, "invalid API key — check 🔑 Key Management");
                default -> ServiceStatus.down(WEAVIATE, "answered HTTP " + schema.statusCode());
            };
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
