package com.example.ragdemo.system2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.SystemDownException;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.errors.OpenAIServiceException;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PreDestroy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.setup.OpenAiSetup;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM2;

/**
 * System 2: the large language model that writes the answer, on OpenRouter (or any OpenAI-compatible endpoint).
 *
 * <p>For every call it reads the base URL, key and model list from {@link KeyStore}, so a key saved a second ago is
 * used at once. The HTTP clients (OpenAI SDK on OkHttp, each with its own connection pool) are made <b>once per base
 * URL + key</b> and reused; when the key or URL changes, the old clients are <b>closed first</b> and new ones opened —
 * no connection pool is left behind. The model is a per-call option. The models are tried <b>in order</b>: a free
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

    /** The clients for the current base URL + key; replaced (old ones closed) when either changes. */
    private Clients clients;

    private record Clients(String identity, OpenAIClient sync, OpenAIClientAsync async, ChatModel model) {

        void close() {
            try {
                sync.close();
            } finally {
                async.close();
            }
        }
    }

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

    /** One step of 🛠 agentic mode: the model's reply — a final text, or tool calls — and which model gave it. */
    public record Step(AssistantMessage message, String model, Integer tokensIn, Integer tokensOut, long ms,
                       List<String> skipped) {
    }

    /**
     * 🛠 One agent step: the conversation so far and the tools on offer → the model's reply. The tools are only
     * <b>described</b> to the model; it answers with the calls it wants, and the app runs them (the chat model alone
     * never executes a tool — that is ChatClient's advisor, not used here), so every call can be timed, drawn and
     * limited. {@code model} null tries the models in order (a model without tool support is skipped like a busy
     * one); otherwise the model chosen in the first step answers the whole conversation.
     */
    public Step step(List<Message> conversation, List<ToolCallback> tools, String model) {
        String key = keys.system2Key();
        if (key.isBlank()) throw new SystemDownException(SYSTEM2, "status.system2.noKey");
        String baseUrl = keys.system2BaseUrl();

        List<String> skipped = new ArrayList<>();
        for (String m : model == null ? keys.system2Models() : List.of(model)) {
            long t0 = System.currentTimeMillis();
            log.debug("🧠🛠 step with {} ({} messages, {} tools)", m, conversation.size(), tools.size());
            try {
                Prompt prompt = new Prompt(conversation, OpenAiChatOptions.builder().model(m).toolCallbacks(tools).build());
                ChatResponse response = callModel(baseUrl, key, prompt);
                AssistantMessage message = response.getResult() == null ? null : response.getResult().getOutput();
                if (message == null || (!message.hasToolCalls() && (message.getText() == null
                        || message.getText().isBlank()))) {
                    skipped.add(m + ": empty answer");
                    continue;
                }
                Usage usage = response.getMetadata().getUsage();
                return new Step(message, m, usage == null ? null : usage.getPromptTokens(),
                        usage == null ? null : usage.getCompletionTokens(), System.currentTimeMillis() - t0, skipped);
            } catch (RuntimeException e) {
                int status = statusOf(e);
                if (status == 401 || status == 403) {
                    throw new SystemDownException(SYSTEM2, "status.invalidKey");
                }
                skipped.add(m + ": " + reason(e, status));
                log.info("🧠🛠 {} failed: {} → next model", m, reason(e, status));
            }
        }
        throw new SystemDownException(SYSTEM2, "status.system2.allFailed", String.join("; ", skipped));
    }

    /** One agent request to the chat model (no ChatClient, so no tool is run for us). Tests override this. */
    protected ChatResponse callModel(String baseUrl, String key, Prompt prompt) {
        return chatModel(baseUrl, key).call(prompt);
    }

    /** One request to one model. Tests override this with a fake. */
    protected ChatResponse call(String baseUrl, String key, String model, String system, String user) {
        return ChatClient.create(chatModel(baseUrl, key)).prompt().system(system).user(user)
                .options(OpenAiChatOptions.builder().model(model))
                .call().chatResponse();
    }

    /**
     * The chat model for this base URL + key: reused while they stay the same. If they changed, the stale clients
     * are closed <b>before</b> new ones are opened.
     */
    synchronized ChatModel chatModel(String baseUrl, String key) {
        String identity = baseUrl + "\n" + key;
        if (clients != null && clients.identity().equals(identity)) return clients.model();
        close();   // stale: another key or URL
        OpenAIClient sync = OpenAiSetup.setupSyncClient(baseUrl, key, null, null, null, null, false, false, null,
                PER_MODEL_TIMEOUT, 0, null, Map.of(), ObservationRegistry.NOOP, null, List.of());   // 0 retries: we
        OpenAIClientAsync async = OpenAiSetup.setupAsyncClient(baseUrl, key, null, null, null, null, false, false,  // move
                null, PER_MODEL_TIMEOUT, 0, null, Map.of(), ObservationRegistry.NOOP, null, List.of());     // on ourselves
        ChatModel model = OpenAiChatModel.builder().openAiClient(sync).openAiClientAsync(async)
                .options(OpenAiChatOptions.builder().maxRetries(0).timeout(PER_MODEL_TIMEOUT).build()).build();
        clients = new Clients(identity, sync, async, model);
        log.info("🧠 opened OpenAI clients for {}", baseUrl);   // never the key
        return model;
    }

    /** Closes the current clients (their connection pools and threads). Also on shutdown. */
    @PreDestroy
    synchronized void close() {
        if (clients == null) return;
        try {
            clients.close();
            log.info("🧠 closed the OpenAI clients of the previous key / URL");
        } catch (RuntimeException e) {
            log.debug("🧠 closing old clients: {}", e.getMessage());
        } finally {
            clients = null;
        }
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
