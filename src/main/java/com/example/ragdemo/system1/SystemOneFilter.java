package com.example.ragdemo.system1;

import com.example.ragdemo.rag.SystemDownException;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Step 4a of the RAG flow — System 1 <b>filter</b>, a Spring AI {@link DocumentPostProcessor}.
 *
 * <p>Laya scores every passage (in parallel; Laya queues what it can't take at once) and the passage is kept only if
 * <pre>
 *   relevant ≥ 0.45   and   answers ≥ 0.55   and   injection &lt; 0.70
 * </pre>
 * (the thresholds the Spring blog uses for Jev). The three scores and the verdict go into the document metadata, so
 * 📚 Sources can show why a passage was kept or dropped.
 */
@Component
public class SystemOneFilter implements DocumentPostProcessor {

    public static final double MIN_RELEVANT = 0.45;
    public static final double MIN_ANSWERS = 0.55;
    public static final double MAX_INJECTION = 0.70;

    // metadata keys
    public static final String RELEVANT = "s1Relevant";
    public static final String ANSWERS = "s1Answers";
    public static final String INJECTION = "s1Injection";
    public static final String KEPT = "s1Kept";
    public static final String VERDICT = "s1Verdict";
    public static final String MS = "s1Ms";

    private final LayaClient laya;

    public SystemOneFilter(LayaClient laya) {
        this.laya = laya;
    }

    /** Spring AI's entry point: only the kept passages. */
    @Override
    public List<Document> process(Query query, List<Document> documents) {
        return score(query.text(), documents).stream().filter(SystemOneFilter::kept).toList();
    }

    /** Every passage, scored and marked kept or dropped (for 📚 Sources). Fails closed: one failure stops all. */
    public List<Document> score(String question, List<Document> documents) {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<LayaClient.Scores>> calls = new ArrayList<>();
            for (Document d : documents) calls.add(pool.submit(() -> laya.score(question, d.getText())));
            List<Document> scored = new ArrayList<>();
            for (int i = 0; i < documents.size(); i++) scored.add(withScores(documents.get(i), result(calls.get(i))));
            return scored;
        }
    }

    public static boolean kept(Document d) {
        return Boolean.TRUE.equals(d.getMetadata().get(KEPT));
    }

    static Document withScores(Document d, LayaClient.Scores s) {
        String verdict = verdict(s);
        Document copy = d.mutate().build();
        copy.getMetadata().put(RELEVANT, s.relevant());
        copy.getMetadata().put(ANSWERS, s.answers());
        copy.getMetadata().put(INJECTION, s.injection());
        copy.getMetadata().put(KEPT, verdict.equals("kept"));
        copy.getMetadata().put(VERDICT, verdict);
        copy.getMetadata().put(MS, s.ms());
        return copy;
    }

    /** The i18n key of a verdict, e.g. "dropped: prompt injection" → "verdict.injection". */
    public static String verdictKey(Object verdict) {
        String v = verdict == null ? "" : verdict.toString();
        if (v.equals("kept")) return "verdict.kept";
        if (v.contains("injection")) return "verdict.injection";
        if (v.contains("not relevant")) return "verdict.notRelevant";
        if (v.contains("does not answer")) return "verdict.noAnswer";
        return "verdict.unknown";
    }

    /** "kept", or why it was dropped — the first rule that failed. */
    static String verdict(LayaClient.Scores s) {
        if (s.injection() >= MAX_INJECTION) return "dropped: prompt injection";
        if (s.relevant() < MIN_RELEVANT) return "dropped: not relevant";
        if (s.answers() < MIN_ANSWERS) return "dropped: does not answer";
        return "kept";
    }

    private static LayaClient.Scores result(Future<LayaClient.Scores> call) {
        try {
            return call.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof SystemDownException down) throw down;
            throw new IllegalStateException(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
