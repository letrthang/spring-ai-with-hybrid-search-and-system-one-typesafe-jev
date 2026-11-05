package com.example.ragdemo.system1;

import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.SystemDownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.typesafe.JsonContent;
import org.springaicommunity.typesafe.RetryPolicy;
import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.api.TypeSafeApi;
import org.springaicommunity.typesafe.exception.TypeSafeApiConnectionException;
import org.springaicommunity.typesafe.exception.TypeSafeApiException;
import org.springaicommunity.typesafe.exception.TypeSafeApiResponseValidationException;
import org.springaicommunity.typesafe.exception.TypeSafeApiTimeoutException;
import org.springaicommunity.typesafe.exception.TypeSafeException;
import org.springaicommunity.typesafe.question.SystemOneRequest;
import org.springaicommunity.typesafe.response.NoulAnswer;
import org.springaicommunity.typesafe.response.SystemOneResponse;

import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM1;

/**
 * The TypeSafe client as one RAG step sees it: every Laya call goes to the real client ({@code delegate}), is timed and
 * recorded by passage, and <b>any failure is thrown</b> as a {@link SystemDownException}.
 *
 * <p>Why: {@code JevDocumentFilter} and {@code JevDocumentReranker} <i>fail open</i> — a passage whose call failed is
 * passed through unscreened. This app fails closed (System 1 is required; nothing reaches System 2 unchecked), and it
 * shows Laya's scores and timings in 📚 Sources and 🔀 Data flow. Both library classes call
 * {@link #systemOne(SystemOneRequest)} for every passage, so overriding it is enough. {@code systemOneAll} turns every
 * exception into a failed slot, so the first failure is also kept: {@link #throwIfFailed()} re-throws it after the
 * library call, with the real reason (busy, waking up, invalid key…). The library's questions are reworded for Laya
 * on the way out ({@link LayaQuestions}).
 */
public class RecordingTypeSafeClient extends TypeSafeClient {

    private static final Logger log = LoggerFactory.getLogger(RecordingTypeSafeClient.class);

    /** Never called: every request goes to the delegate. TypeSafeClient's constructor only needs an API object. */
    private static final TypeSafeApi UNUSED = TypeSafeApi.builder().baseUrl("http://localhost").apiKey("unused").build();

    /** Laya's answers to one request and how long it took. */
    public record Call(SystemOneResponse response, long ms) {

        public double noul(String name) {
            return response.noulValue(name);
        }
    }

    private final TypeSafeClient delegate;
    private final Map<String, Call> byPassage = new ConcurrentHashMap<>();
    private final AtomicReference<SystemDownException> failure = new AtomicReference<>();

    public RecordingTypeSafeClient(TypeSafeClient delegate) {
        super(UNUSED, delegate.defaultModel(), RetryPolicy.noRetry());
        this.delegate = delegate;
    }

    @Override
    public String defaultModel() {
        return delegate.defaultModel();
    }

    @Override
    public SystemOneResponse systemOne(SystemOneRequest request) {
        long t0 = System.currentTimeMillis();
        SystemOneResponse response;
        try {
            response = delegate.systemOne(LayaQuestions.forLaya(request));   // same names, Laya's wording
        } catch (RuntimeException e) {
            throw remember(failClosed(e));
        }
        long ms = System.currentTimeMillis() - t0;
        for (String name : request.questions().keySet()) {
            // a partial answer would make JevDocumentFilter pass the passage through unscreened
            if (!(response.answers().get(name) instanceof NoulAnswer)) {
                throw remember(new SystemDownException(SYSTEM1, "status.system1.badAnswer"));
            }
        }
        String passage = passage(request.state());
        if (passage != null) byPassage.put(passage, new Call(response, ms));
        log.debug("⚡ Laya /v1/systemone {} → {} in {} ms", request.questions().keySet(), response.answers(), ms);
        return response;
    }

    /** Fail closed after a library call: re-throw the first failure, if any call failed. */
    public void throwIfFailed() {
        SystemDownException first = failure.get();
        if (first != null) throw first;
    }

    private SystemDownException remember(SystemDownException down) {
        failure.compareAndSet(null, down);
        return down;
    }

    /** The recorded call for a passage text (as sent), or null if it was never screened. */
    public Call call(String passage) {
        return byPassage.get(passage);
    }

    /** The {@code passage} field of a {@code {query, passage}} state, or the text of a plain-text state. */
    static String passage(JsonContent state) {
        if (state == null) return null;
        Object value = state.value();
        if (value instanceof Map<?, ?> map) return map.get("passage") instanceof String s ? s : null;
        return value instanceof String s ? s : null;
    }

    /** Any failure of a System 1 call as "System 1 is down", with the status the 🟡/🔴 pill shows. */
    public static SystemDownException failClosed(Throwable e) {
        if (e instanceof CompletionException && e.getCause() != null) return failClosed(e.getCause());
        if (e instanceof SystemDownException down) return down;
        if (Thread.currentThread().isInterrupted()) return new SystemDownException(SYSTEM1, "status.interrupted");
        if (e instanceof TypeSafeApiResponseValidationException) {
            return new SystemDownException(SYSTEM1, "status.system1.badAnswer");
        }
        if (e instanceof TypeSafeApiTimeoutException || e instanceof TypeSafeApiConnectionException) {
            return new SystemDownException(SYSTEM1, "status.unreachable",
                    Http.describe(e.getCause() != null ? e.getCause() : e));
        }
        if (e instanceof TypeSafeApiException api) {
            return switch (api.status()) {
                case 429 -> new SystemDownException(SYSTEM1, "status.system1.busy");
                case 503 -> new SystemDownException(SYSTEM1, "status.system1.waking");
                case 401, 403 -> new SystemDownException(SYSTEM1, "status.invalidKey");
                default -> new SystemDownException(SYSTEM1, "status.http", api.status());
            };
        }
        if (e instanceof TypeSafeException) return new SystemDownException(SYSTEM1, "status.system1.badAnswer");
        // anything else (a bug) also stops the question: fail closed, never pass unchecked text on
        return new SystemDownException(SYSTEM1, "status.unreachable", Http.describe(e));
    }
}
