package com.example.ragdemo.system1;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Step 4b of the RAG flow — System 1 <b>rerank</b>, a Spring AI {@link DocumentPostProcessor}.
 *
 * <p>Search ranks passages by how they <i>match</i> the question; Laya's {@code answers} score says how well they can
 * <i>answer</i> it — a better order for the LLM. The kept passages are sorted by that score, best first, and the top
 * {@value #TOP_K} go on. (The scores are already in the metadata: {@link SystemOneFilter} asked all three questions
 * in one call, so reranking costs no extra call.)
 */
@Component
public class SystemOneReranker implements DocumentPostProcessor {

    public static final int TOP_K = 3;

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        return documents.stream()
                .sorted(Comparator.comparingDouble(SystemOneReranker::answers).reversed())
                .limit(TOP_K)
                .toList();
    }

    private static double answers(Document d) {
        return d.getMetadata().get(SystemOneFilter.ANSWERS) instanceof Number n ? n.doubleValue() : 0;
    }
}
