package com.example.ragdemo.weaviate;

import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Tokenization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.vectorindex.HFresh;

import java.io.IOException;
import java.util.Map;

/**
 * The Weaviate collection {@value #NAME}: answers the app has <b>learned</b> — each one verified by ⚡ System 1 before
 * it was saved (see {@code AnswerVerifier}). Kept apart from {@code DemoDoc}: a learned answer is generated text,
 * never a source passage.
 *
 * <ul>
 *   <li>{@code question} — the vector is made from the question only, so a new question finds the closest earlier
 *       one by meaning.</li>
 *   <li>{@code answer}, {@code model}, {@code lang} (filter: same language only), the System 1 scores, and
 *       {@code sources} — the passages the answer came from, as JSON, shown again when the answer is reused.</li>
 * </ul>
 */
public final class LearnedAnswerCollection {

    public static final String NAME = "LearnedAnswer";

    private LearnedAnswerCollection() {
    }

    public static CollectionConfig definition() {
        return definition(VectorConfig.text2vecWeaviate(v -> v.sourceProperties("question").vectorIndex(HFresh.of())));
    }

    /** Tests pass a local vectorizer (a local Weaviate has no Weaviate Cloud embeddings). */
    static CollectionConfig definition(Map.Entry<String, VectorConfig> vectorizer) {
        return CollectionConfig.of(NAME, c -> c
                .description("RAG demo: answers verified by System 1, reused for very similar questions")
                .vectorConfig(vectorizer)
                .properties(
                        Property.text("question", p -> p.tokenization(Tokenization.WORD).indexSearchable(true)),
                        Property.text("answer", p -> p.tokenization(Tokenization.WORD).indexSearchable(true)),
                        Property.text("lang", p -> p.tokenization(Tokenization.FIELD).indexFilterable(true)
                                .indexSearchable(false)),
                        Property.text("model", p -> p.tokenization(Tokenization.FIELD).indexSearchable(false)),
                        Property.text("sources", p -> p.indexSearchable(false).indexFilterable(false)),
                        Property.number("answersScore"),
                        Property.number("groundedScore"),
                        Property.number("injectionScore")));
    }

    static boolean ensure(WeaviateClient client, CollectionConfig definition) throws IOException {
        if (client.collections.exists(NAME)) return false;
        client.collections.create(definition);
        return true;
    }

    /** Number of learned answers, 0 when the collection does not exist yet. */
    static long count(WeaviateClient client) throws IOException {
        return client.collections.exists(NAME) ? client.collections.use(NAME).size() : 0;
    }
}
