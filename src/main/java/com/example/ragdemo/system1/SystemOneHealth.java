package com.example.ragdemo.system1;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.ServiceStatus;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.time.Duration;

import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM1;

/**
 * Is Laya up? {@code GET <url>/health} needs no key: {@code {"status":"ok"}} = ready. A free Hugging Face Space sleeps
 * when idle; while it wakes up (about a minute) the server answers 503 or Hugging Face answers with its own page.
 */
@Component
public class SystemOneHealth {

    private final KeyStore keys;

    public SystemOneHealth(KeyStore keys) {
        this.keys = keys;
    }

    public ServiceStatus check() {
        try {
            HttpResponse<String> response = Http.get(keys.system1Url() + "/health", null, Duration.ofSeconds(15));
            if (response.statusCode() == 200 && response.body().contains("\"ok\"")) {
                return ServiceStatus.ready(SYSTEM1, "Laya is ready");
            }
            if (response.statusCode() == 503 || response.statusCode() == 200) {
                return ServiceStatus.waiting(SYSTEM1, "Laya is waking up, about 1 minute");
            }
            return ServiceStatus.down(SYSTEM1, "Laya answered HTTP " + response.statusCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ServiceStatus.down(SYSTEM1, "check interrupted");
        } catch (Exception e) {
            return ServiceStatus.down(SYSTEM1, "Laya unreachable (" + Http.describe(e) + ")");
        }
    }
}
