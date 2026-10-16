package com.example.ragdemo.system2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.SystemDownException;
import com.openai.errors.OpenAIServiceException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM2;

/**
 * System 2: the large language model that writes the answer, on OpenRouter (or any OpenAI-compatible endpoint).
 *
 * <p>For every call it reads the base URL, key and model list from {@link KeyStore} and builds a Spring AI
 * {@link OpenAiChatModel} — so a key saved a second ago is used at once. The models are tried <b>in order</b>: a free
 * model that is busy (429), down (5xx) or slow (timeout) hands over to the next one. A wrong key (401/403) stops at
 * once — every model would fail the same way. If all fail, {@link SystemDownException}: System 2 is required.
 */
@Component
public class SystemTwoClient {

    private static final Logger log = LoggerFactory.getLogger(SystemTwoClient.class);

    /** Longest wait for one model. Free models can be slow; the next model gets its own time. */
    public static final Duration PER_MODEL_TIMEOUT = Duration.ofSeconds(45);

    /** The answer, and which model wrote it (for 🔬 Trace). Token counts may be null if the server sends none. */
    public record Answer(String text, String model, Integer tokensIn, Integer tokensOut, long ms, List<String> skipped) {
    }

    private final KeyStore keys;

    public SystemTwoClient(KeyStore keys) {
        this.keys = keys;
    }

    public Answer generate(String system, String user) {
        String key = keys.system2Key();
        if (key.isBlank()) throw new SystemDownException(SYSTEM2, "status.system2.noKey");
        String baseUrl = keys.system2BaseUrl();

        List<String> skipped = new ArrayList<>();
        for (String model : keys.system2Models()) {
            long t0 = System.currentTimeMillis();
            log.debug("🧠 trying {} at {}", model, baseUrl);
            try {
                ChatResponse response = call(baseUrl, key, model, system, user);
                String text = response.getResult() == null ? null : response.getResult().getOutput().getText();
                if (text == null || text.isBlank()) {
                    skipped.add(model + ": empty answer");
                    continue;
                }
                Usage usage = response.getMetadata().getUsage();
                return new Answer(text.strip(), model, usage == null ? null : usage.getPromptTokens(),
                        usage == null ? null : usage.getCompletionTokens(), System.currentTimeMillis() - t0, skipped);
            } catch (RuntimeException e) {
                int status = statusOf(e);
                if (status == 401 || status == 403) {
                    throw new SystemDownException(SYSTEM2, "status.invalidKey");
                }
                skipped.add(model + ": " + reason(e, status));
                log.info("🧠 {} failed: {} → next model", model, reason(e, status));
            }
        }
        throw new SystemDownException(SYSTEM2, "status.system2.allFailed", String.join("; ", skipped));
    }

    /** One request to one model. Tests override this with a fake. */
    protected ChatResponse call(String baseUrl, String key, String model, String system, String user) {
        ChatModel chatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(baseUrl)
                        .apiKey(key)
                        .model(model)
                        .maxRetries(0)                 // we move on to the next model ourselves
                        .timeout(PER_MODEL_TIMEOUT)
                        .build())
                .build();
        return ChatClient.create(chatModel).prompt().system(system).user(user).call().chatResponse();
    }

    private static int statusOf(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof OpenAIServiceException service) return service.statusCode();
        }
        return -1;
    }

    private static String reason(RuntimeException e, int status) {
        return switch (status) {
            case 429 -> "busy (429 rate limit)";
            case 404 -> "model not found (404)";
            case -1 -> Http.describe(e);
            default -> "HTTP " + status;
        };
    }
}
