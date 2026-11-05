package com.example.ragdemo.system1;

import com.example.ragdemo.rag.SystemDownException;
import org.springaicommunity.typesafe.JevBatchOptions;
import org.springaicommunity.typesafe.rag.JevDocumentReranker;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM1;

/**
 * Step 4b of the RAG flow — System 1 <b>rerank</b>: TypeSafe's {@link JevDocumentReranker} (a Spring AI
 * {@link DocumentPostProcessor}).
 *
 * <p>Search ranks passages by how they <i>match</i> the question; Laya says how well each one could <i>answer</i> it
 * (one {@code answers_query} question per kept passage, in parallel) — a better order for the LLM. The passages are
 * sorted by that score, best first, and the top {@value #TOP_K} go on, each with its score under {@link #SCORE}.
 * Unlike the library, an unscored passage is not passed on: a failed call stops the question.
 */
@Component
public class SystemOneReranker implements DocumentPostProcessor {

    public static final int TOP_K = 3;
    /** Laya's rerank score (0…1) in the metadata. */
    public static final String SCORE = JevDocumentReranker.SCORE_METADATA_KEY;

    private final LayaClient laya;

    public SystemOneReranker(LayaClient laya) {
        this.laya = laya;
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        if (documents.isEmpty()) return List.of();
        Map<String, Document> byId = new LinkedHashMap<>();
        for (Document d : documents) byId.putIfAbsent(d.getId(), d);
        // what Laya reads: the passage, shortened to fit one Laya slot; the originals (whole text) go on
        List<Document> screened = byId.values().stream()
                .map(d -> Document.builder().id(d.getId()).text(LayaClient.shorten(d.getText())).build())
                .toList();
        List<Document> ranked;
        RecordingTypeSafeClient calls = laya.recorder();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            ranked = JevDocumentReranker.builder(calls).topK(TOP_K)
                    .batchOptions(JevBatchOptions.ofConcurrency(screened.size()).withExecutor(pool))
                    .build()
                    .process(query, screened);
            calls.throwIfFailed();   // the library keeps an unscored passage: stop here instead
        } catch (RuntimeException e) {
            throw RecordingTypeSafeClient.failClosed(e);
        }
        return ranked.stream().map(r -> {
            if (!(r.getMetadata().get(SCORE) instanceof Number score)) {
                throw new SystemDownException(SYSTEM1, "status.system1.badAnswer");   // never scored
            }
            Document copy = byId.get(r.getId()).mutate().build();
            copy.getMetadata().put(SCORE, score.doubleValue());
            return copy;
        }).toList();
    }

    /** Laya's rerank score of a passage, or null if it was not reranked. */
    public static Double score(Document d) {
        return d.getMetadata().get(SCORE) instanceof Number n ? n.doubleValue() : null;
    }
}
