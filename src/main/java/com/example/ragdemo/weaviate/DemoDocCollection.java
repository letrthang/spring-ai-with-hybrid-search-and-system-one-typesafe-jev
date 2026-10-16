package com.example.ragdemo.weaviate;

import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Tokenization;
import io.weaviate.client6.v1.api.collections.VectorConfig;

import java.io.IOException;
import java.util.Map;

/**
 * The Weaviate collection {@value #NAME}: one object per passage (chunk).
 *
 * <ul>
 *   <li>{@code title}, {@code body}: searchable words → the <b>BM25</b> keyword index.</li>
 *   <li>A vector made by Weaviate's own vectorizer ({@code text2vec-weaviate}) from title + body → <b>nearText</b>.
 *       No embedding key or model on our side.</li>
 *   <li>{@code source} (file name) and {@code chunkNo}: where the passage comes from.</li>
 * </ul>
 */
public final class DemoDocCollection {

    public static final String NAME = "DemoDoc";

    private DemoDocCollection() {
    }

    public static CollectionConfig definition() {
        return definition(VectorConfig.text2vecWeaviate(v -> v.sourceProperties("title", "body")));
    }

    /** Tests pass another vectorizer (a local Weaviate has no access to Weaviate Cloud's embeddings). */
    static CollectionConfig definition(Map.Entry<String, VectorConfig> vectorizer) {
        return CollectionConfig.of(NAME, c -> c
                .description("RAG demo passages (sample-data/*.md, one object per section)")
                .vectorConfig(vectorizer)
                .properties(
                        Property.text("title", p -> p.tokenization(Tokenization.WORD).indexSearchable(true)),
                        Property.text("body", p -> p.tokenization(Tokenization.WORD).indexSearchable(true)),
                        Property.text("source", p -> p.tokenization(Tokenization.FIELD).indexFilterable(true)
                                .indexSearchable(false)),
                        Property.integer("chunkNo", p -> p.indexFilterable(true))));
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

    /** Number of passages, or -1 when the collection does not exist yet. */
    public static long count(WeaviateClient client) throws IOException {
        return client.collections.exists(NAME) ? client.collections.use(NAME).size() : -1;
    }
}
