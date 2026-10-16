package com.example.ragdemo.system2;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.ServiceStatus;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.time.Duration;

import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM2;

/**
 * Is the System 2 key usable? Costs no tokens. On OpenRouter {@code GET /key} checks the key itself (the model list is
 * public, so it would say "ok" even for a wrong key); on any other OpenAI-compatible server, {@code GET /models}.
 */
@Component
public class SystemTwoHealth {

    private final KeyStore keys;

    public SystemTwoHealth(KeyStore keys) {
        this.keys = keys;
    }

    public ServiceStatus check() {
        if (keys.system2Key().isBlank()) {
            return ServiceStatus.down(SYSTEM2, "status.system2.noKey");
        }
        String base = keys.system2BaseUrl();
        String path = base.contains("openrouter.ai") ? "/key" : "/models";
        try {
            HttpResponse<String> response = Http.get(base + path, keys.system2Key(), Duration.ofSeconds(15));
            return switch (response.statusCode()) {
                case 200 -> ServiceStatus.ready(SYSTEM2, "status.system2.ready", keys.system2Models().getFirst());
                case 401, 403 -> ServiceStatus.down(SYSTEM2, "status.invalidKey");
                case 429 -> ServiceStatus.waiting(SYSTEM2, "status.system2.rateLimit");
                default -> ServiceStatus.down(SYSTEM2, "status.http", response.statusCode());
            };
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ServiceStatus.down(SYSTEM2, "status.interrupted");
        } catch (Exception e) {
            return ServiceStatus.down(SYSTEM2, "status.unreachable", Http.describe(e));
        }
    }
}
