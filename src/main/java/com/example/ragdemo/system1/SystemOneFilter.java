package com.example.ragdemo.system1;

import com.example.ragdemo.rag.SystemDownException;
import org.springaicommunity.typesafe.JevBatchOptions;
import org.springaicommunity.typesafe.rag.JevDocumentFilter;
import org.springaicommunity.typesafe.rag.JevDocumentFilter.Classification;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM1;

/**
 * Step 4a of the RAG flow — System 1 <b>filter</b>: TypeSafe's {@link JevDocumentFilter} (a Spring AI
 * {@link DocumentPostProcessor}) asks Laya four questions about every passage, in parallel, and applies its policy —
 * the first rule that matches wins:
 * <pre>
 *   injection &gt; 0.70  → dropped        contradicts the question's premise &gt; 0.70 → kept, marked conflicting
 *   relevant  &lt; 0.45  → dropped        answers &gt; 0.55 → kept, else dropped
 * </pre>
 * (the thresholds of the TypeSafe cookbook and the Spring blog). The scores and the verdict go into the document
 * metadata, so 📚 Sources can show why a passage was kept or dropped. Unlike the library, it <b>fails closed</b>
 * ({@link RecordingTypeSafeClient}): one failed call stops the question.
 */
@Component
public class SystemOneFilter implements DocumentPostProcessor {

    public static final double MIN_RELEVANT = 0.45;
    public static final double MIN_ANSWERS = 0.55;
    public static final double MAX_INJECTION = 0.70;
    public static final double MAX_CONTRADICTION = 0.70;
    static final JevDocumentFilter.Policy POLICY =
            new JevDocumentFilter.Policy(MAX_INJECTION, MAX_CONTRADICTION, MIN_RELEVANT, MIN_ANSWERS);

    // Laya's answers, named as JevDocumentFilter asks them
    static final String IS_RELEVANT = "is_relevant";
    static final String CONTAINS_ANSWER = "contains_answer_evidence";
    static final String CONTRADICTS = "contradicts_query_premise";
    static final String INJECTION_ATTEMPT = "contains_prompt_injection";

    // metadata keys
    public static final String RELEVANT = "s1Relevant";
    public static final String ANSWERS = "s1Answers";
    public static final String INJECTION = "s1Injection";
    public static final String CONTRADICTION = "s1Contradicts";
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
        if (documents.isEmpty()) return List.of();
        RecordingTypeSafeClient calls = laya.recorder();
        // what Laya reads: the passage, shortened to fit one Laya slot (the prompt still gets the whole passage)
        List<Document> screened = documents.stream()
                .map(d -> Document.builder().id(d.getId()).text(LayaClient.shorten(d.getText())).build())
                .toList();
        Set<String> kept;
        JevDocumentFilter jev;
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            jev = JevDocumentFilter.builder(calls).policy(POLICY)
                    .batchOptions(JevBatchOptions.ofConcurrency(documents.size()).withExecutor(pool))
                    .build();
            kept = jev.process(new Query(question), screened).stream().map(Document::getId)
                    .collect(Collectors.toSet());
            calls.throwIfFailed();   // the library passed a failed passage through: stop here instead
        } catch (RuntimeException e) {
            throw RecordingTypeSafeClient.failClosed(e);
        }
        List<Document> scored = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            RecordingTypeSafeClient.Call call = calls.call(screened.get(i).getText());
            if (call == null) throw new SystemDownException(SYSTEM1, "status.system1.badAnswer");   // never screened
            scored.add(withScores(documents.get(i), call, jev.classify(call.response()),
                    kept.contains(screened.get(i).getId())));
        }
        return scored;
    }

    public static boolean kept(Document d) {
        return Boolean.TRUE.equals(d.getMetadata().get(KEPT));
    }

    /** A copy with Laya's four scores, JevDocumentFilter's classification and the verdict in the metadata. */
    static Document withScores(Document d, RecordingTypeSafeClient.Call call, Classification c, boolean kept) {
        Document copy = d.mutate().build();
        copy.getMetadata().put(RELEVANT, call.noul(IS_RELEVANT));
        copy.getMetadata().put(ANSWERS, call.noul(CONTAINS_ANSWER));
        copy.getMetadata().put(INJECTION, call.noul(INJECTION_ATTEMPT));
        copy.getMetadata().put(CONTRADICTION, call.noul(CONTRADICTS));
        copy.getMetadata().put(KEPT, kept);
        copy.getMetadata().put(VERDICT, verdict(c, call.noul(INJECTION_ATTEMPT), call.noul(IS_RELEVANT)));
        copy.getMetadata().put(MS, call.ms());
        copy.getMetadata().put(JevDocumentFilter.CLASSIFICATION_METADATA_KEY, c.name());
        return copy;
    }

    /** "kept", "kept: conflicts with the question", or why it was dropped — the first rule that matched. */
    static String verdict(Classification c, double injection, double relevant) {
        return switch (c) {
            case INCLUDED -> "kept";
            case CONFLICTING -> "kept: conflicts with the question";
            case EXCLUDED -> injection > MAX_INJECTION ? "dropped: prompt injection"
                    : relevant < MIN_RELEVANT ? "dropped: not relevant" : "dropped: does not answer";
        };
    }

    /** The i18n key of a verdict, e.g. "dropped: prompt injection" → "verdict.injection". */
    public static String verdictKey(Object verdict) {
        String v = verdict == null ? "" : verdict.toString();
        if (v.equals("kept")) return "verdict.kept";
        if (v.contains("conflicts")) return "verdict.conflicting";
        if (v.contains("injection")) return "verdict.injection";
        if (v.contains("not relevant")) return "verdict.notRelevant";
        if (v.contains("does not answer")) return "verdict.noAnswer";
        return "verdict.unknown";
    }
}
