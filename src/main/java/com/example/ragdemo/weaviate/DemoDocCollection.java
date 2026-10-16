package com.example.ragdemo.weaviate;

import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Tokenization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.vectorindex.HFresh;

import io.weaviate.client6.v1.api.collections.query.FetchObjects;
import io.weaviate.client6.v1.api.collections.query.Filter;

import java.io.IOException;
import java.util.Map;

/**
 * The Weaviate collection {@value #NAME}: one object per passage (chunk).
 *
 * <ul>
 *   <li>{@code title}, {@code body}: searchable words → the <b>BM25</b> keyword index.</li>
 *   <li>A vector made by Weaviate's own vectorizer ({@code text2vec-weaviate}) from title + body → <b>nearText</b>.
 *       No embedding key or model on our side. Index type HFresh (what Weaviate Cloud requires).</li>
 *   <li>{@code source} ("en/02-pcr.md") and {@code chunkNo}: where the passage comes from.</li>
 *   <li>{@code lang} (en / vi / ja): searches keep to the question's language (see {@link Lang}).</li>
 *   <li>{@code kind}: {@code passage} (sample data) or {@code learned} (an answer verified by System 1 — the free
 *       Weaviate Cloud plan allows one collection, so learned answers live here, kept apart by this field: every
 *       passage search filters {@code kind = passage}, the reuse lookup {@code kind = learned}). A learned object's
 *       title and body are its question (that is what gets vectorized); {@code answer}, {@code model},
 *       {@code sources} (JSON) and the three System 1 scores are only set on learned objects.</li>
 * </ul>
 */
public final class DemoDocCollection {

    public static final String NAME = "DemoDoc";
    public static final String KIND_PASSAGE = "passage";
    public static final String KIND_LEARNED = "learned";

    private DemoDocCollection() {
    }

    public static CollectionConfig definition() {
        // Weaviate Cloud's newer clusters accept only the HFresh vector index (HTTP 422 "hnsw is not allowed"),
        // and the client still defaults to HNSW — so the index type is set explicitly.
        return definition(VectorConfig.text2vecWeaviate(v -> v.sourceProperties("title", "body")
                .vectorIndex(HFresh.of())));
    }

    /** Tests pass another vectorizer (a local Weaviate has no access to Weaviate Cloud's embeddings). */
    static CollectionConfig definition(Map.Entry<String, VectorConfig> vectorizer) {
        return CollectionConfig.of(NAME, c -> c
                .description("RAG demo passages (sample-data/{en,vi,ja}/*.md, one object per section)")
                .vectorConfig(vectorizer)
                .properties(
                        Property.text("title", p -> p.tokenization(Tokenization.WORD).indexSearchable(true)),
                        Property.text("body", p -> p.tokenization(Tokenization.WORD).indexSearchable(true)),
                        Property.text("source", p -> p.tokenization(Tokenization.FIELD).indexFilterable(true)
                                .indexSearchable(false)),
                        Property.integer("chunkNo", p -> p.indexFilterable(true)),
                        Property.text("lang", p -> p.tokenization(Tokenization.FIELD).indexFilterable(true)
                                .indexSearchable(false)),
                        Property.text("kind", p -> p.tokenization(Tokenization.FIELD).indexFilterable(true)
                                .indexSearchable(false)),
                        Property.text("answer", p -> p.indexSearchable(false).indexFilterable(false)),
                        Property.text("model", p -> p.indexSearchable(false).indexFilterable(false)),
                        Property.text("sources", p -> p.indexSearchable(false).indexFilterable(false)),
                        Property.number("answersScore"),
                        Property.number("groundedScore"),
                        Property.number("injectionScore")));
    }

    /** Only sample passages — never a learned answer. */
    public static Filter passages() {
        return Filter.property("kind").eq(KIND_PASSAGE);
    }

    /** Only learned answers. */
    public static Filter learned() {
        return Filter.property("kind").eq(KIND_LEARNED);
    }

    /** How many learned answers (a small set: counted by fetching their ids). */
    static long learnedCount(WeaviateClient client) throws IOException {
        if (!client.collections.exists(NAME)) return 0;
        return client.collections.use(NAME).query
                .fetchObjects(FetchObjects.of(f -> f.filters(learned()).limit(10_000))).objects().size();
    }

    /** Creates the collection if it does not exist. Returns true if it was created now. */
    public static boolean ensure(WeaviateClient client) throws IOException {
        return ensure(client, definition());
    }

    static boolean ensure(WeaviateClient client, CollectionConfig definition) throws IOException {
        if (client.collections.exists(NAME)) return false;
        client.collections.create(definition);
        return true;
    }

    /** Number of passages (learned answers not counted), or -1 when the collection does not exist yet. */
    public static long count(WeaviateClient client) throws IOException {
        if (!client.collections.exists(NAME)) return -1;
        return client.collections.use(NAME).size() - learnedCount(client);
    }
}
