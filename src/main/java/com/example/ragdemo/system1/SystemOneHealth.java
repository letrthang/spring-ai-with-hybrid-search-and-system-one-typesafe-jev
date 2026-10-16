package com.example.ragdemo.system1;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.ServiceStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM1;

/**
 * Is Laya up? {@code GET <url>/health} needs no key: {@code {"status":"ok"}} = ready. A free Hugging Face Space sleeps
 * when idle; while it wakes up (about a minute) the server answers 503 or Hugging Face answers with its own page.
 */
@Component
public class SystemOneHealth {

    private final KeyStore keys;
    private final RestClient rest = Http.restClient(Duration.ofSeconds(15));

    public SystemOneHealth(KeyStore keys) {
        this.keys = keys;
    }

    public ServiceStatus check() {
        try {
            Http.Reply response = rest.get().uri(keys.system1Url() + "/health")
                    .exchange((request, reply) -> Http.Reply.of(reply));
            if (response.status() == 200 && response.body().contains("\"ok\"")) {
                return ServiceStatus.ready(SYSTEM1, "status.system1.ready");
            }
            if (response.status() == 503 || response.status() == 200) {
                return ServiceStatus.waiting(SYSTEM1, "status.system1.waking");
            }
            return ServiceStatus.down(SYSTEM1, "status.http", response.status());
        } catch (RestClientException e) {
            if (Thread.currentThread().isInterrupted()) return ServiceStatus.down(SYSTEM1, "status.interrupted");
            return ServiceStatus.down(SYSTEM1, "status.unreachable", Http.describe(e.getCause() != null ? e.getCause() : e));
        } catch (RuntimeException e) {
            return ServiceStatus.down(SYSTEM1, "status.unreachable", Http.describe(e));
        }
    }
}
