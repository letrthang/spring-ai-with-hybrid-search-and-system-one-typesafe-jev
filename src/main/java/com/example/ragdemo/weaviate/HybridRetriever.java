package com.example.ragdemo.weaviate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.weaviate.client6.v1.api.collections.CollectionHandle;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.FetchObjects;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Step 2 + 3 of the RAG flow: find passages in Weaviate <b>two ways at once</b> and merge the lists.
 *
 * <ol>
 *   <li><b>BM25</b> — passages that share the words of the question (title counts double).</li>
 *   <li><b>nearText</b> — passages whose vector is closest to the question's vector (same meaning).</li>
 *   <li><b>RRF</b> — {@link RrfFusion} merges the two rankings.</li>
 * </ol>
 *
 * <p>It is a Spring AI {@link DocumentRetriever}, so it plugs into Spring AI's modular RAG like any vector store
 * retriever. {@link #search} returns the same passages with every rank and timing, for the trace and Search Data.
 */
@Component
public class HybridRetriever implements DocumentRetriever {

    private static final Logger log = LoggerFactory.getLogger(HybridRetriever.class);

    /** How many passages each search returns before fusion. */
    public static final int CANDIDATES = 20;
    /** How many fused passages go on to System 1. */
    public static final int TOP_K = 4;
    public static final List<String> BM25_PROPERTIES = List.of("title^2", "body");

    public enum Mode { BM25, VECTOR, HYBRID }

    /** One found passage with everything we know about how it was found. Null = not in that list. */
    public record Hit(String id, String title, String body, String source, Integer chunkNo,
                      Integer bm25Rank, Float bm25Score, Integer vectorRank, Float vectorDistance, Double rrfScore) {
    }

    /** The result of one search, with the size and time of each half (for 🔬 Trace and 🔀 Data flow). */
    public record Result(List<Hit> hits, int bm25Count, long bm25Ms, int vectorCount, long vectorMs) {
    }

    private final WeaviateConnector weaviate;

    public HybridRetriever(WeaviateConnector weaviate) {
        this.weaviate = weaviate;
    }

    /** Spring AI's entry point: the best {@value #TOP_K} fused passages as Documents. */
    @Override
    public List<Document> retrieve(Query query) {
        return search(query.text(), Mode.HYBRID, TOP_K).hits().stream().map(HybridRetriever::toDocument).toList();
    }

    /** BM25 only, vector only, or both fused; at most {@code limit} hits. */
    public Result search(String text, Mode mode, int limit) {
        return weaviate.call("search", client -> {
            CollectionHandle<Map<String, Object>> docs = client.collections.use(DemoDocCollection.NAME);
            try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
                // the two searches run at the same time, each on its own virtual thread
                Future<Timed> bm25 = mode == Mode.VECTOR ? null : pool.submit(timed(() ->
                        docs.query.bm25(text, q -> q.queryProperties(BM25_PROPERTIES).limit(CANDIDATES)
                                .returnMetadata(Metadata.SCORE)).objects()));
                Future<Timed> vector = mode == Mode.BM25 ? null : pool.submit(timed(() ->
                        docs.query.nearText(text, q -> q.limit(CANDIDATES)
                                .returnMetadata(Metadata.DISTANCE)).objects()));
                Timed b = get(bm25);
                Timed v = get(vector);
                List<Hit> hits = merge(b.objects, v.objects, mode);
                log.debug("🗄️ search {} \"{}\": bm25 {} ({} ms), vector {} ({} ms) → {} merged", mode, text,
                        b.objects.size(), b.ms, v.objects.size(), v.ms, hits.size());
                return new Result(hits.subList(0, Math.min(limit, hits.size())),
                        bm25 == null ? -1 : b.objects.size(), b.ms, vector == null ? -1 : v.objects.size(), v.ms);
            }
        });
    }

    /** A page of passages in id order, no search (Search Data with an empty query). */
    public List<Hit> list(int offset, int limit) {
        return weaviate.call("list", client -> {
            if (!client.collections.exists(DemoDocCollection.NAME)) return List.<Hit>of();
            return client.collections.use(DemoDocCollection.NAME).query
                    .fetchObjects(FetchObjects.of(f -> f.offset(offset).limit(limit))).objects().stream()
                    .map(o -> hit(o, null, null, null, null, null)).toList();
        });
    }

    // ------------------------------------------------------------------ merging the two lists

    static List<Hit> merge(List<WeaviateObject<Map<String, Object>>> bm25,
                           List<WeaviateObject<Map<String, Object>>> vector, Mode mode) {
        Map<String, WeaviateObject<Map<String, Object>>> byId = new LinkedHashMap<>();
        Map<String, Float> bm25Score = new LinkedHashMap<>();
        Map<String, Float> distance = new LinkedHashMap<>();
        List<String> bm25Ids = new ArrayList<>();
        List<String> vectorIds = new ArrayList<>();
        for (WeaviateObject<Map<String, Object>> o : bm25) {
            byId.putIfAbsent(o.uuid(), o);
            bm25Ids.add(o.uuid());
            bm25Score.put(o.uuid(), o.queryMetadata() == null ? null : o.queryMetadata().score());
        }
        for (WeaviateObject<Map<String, Object>> o : vector) {
            byId.putIfAbsent(o.uuid(), o);
            vectorIds.add(o.uuid());
            distance.put(o.uuid(), o.queryMetadata() == null ? null : o.queryMetadata().distance());
        }
        List<Hit> hits = new ArrayList<>();
        for (RrfFusion.Fused f : RrfFusion.fuse(mode == Mode.VECTOR ? List.of() : bm25Ids,
                mode == Mode.BM25 ? List.of() : vectorIds)) {
            hits.add(hit(byId.get(f.id()), f.bm25Rank(), bm25Score.get(f.id()), f.vectorRank(), distance.get(f.id()),
                    mode == Mode.HYBRID ? f.score() : null));
        }
        return hits;
    }

    private static Hit hit(WeaviateObject<Map<String, Object>> o, Integer bm25Rank, Float bm25Score,
                           Integer vectorRank, Float distance, Double rrf) {
        Map<String, Object> p = o.properties() == null ? Map.of() : o.properties();
        Object chunkNo = p.get("chunkNo");
        return new Hit(o.uuid(), str(p.get("title")), str(p.get("body")), str(p.get("source")),
                chunkNo instanceof Number n ? n.intValue() : null, bm25Rank, bm25Score, vectorRank, distance, rrf);
    }

    /** A hit as a Spring AI Document: the body is the text, everything else is metadata. */
    public static Document toDocument(Hit hit) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", hit.title());
        meta.put("source", hit.source());
        if (hit.chunkNo() != null) meta.put("chunkNo", hit.chunkNo());
        if (hit.bm25Rank() != null) meta.put("bm25Rank", hit.bm25Rank());
        if (hit.vectorRank() != null) meta.put("vectorRank", hit.vectorRank());
        if (hit.rrfScore() != null) meta.put("rrfScore", hit.rrfScore());
        return Document.builder().id(hit.id()).text(hit.body()).metadata(meta).build();
    }

    // ------------------------------------------------------------------ small helpers

    private record Timed(List<WeaviateObject<Map<String, Object>>> objects, long ms) {
    }

    private static Callable<Timed> timed(Callable<List<WeaviateObject<Map<String, Object>>>> search) {
        return () -> {
            long t0 = System.currentTimeMillis();
            List<WeaviateObject<Map<String, Object>>> objects = search.call();
            return new Timed(objects, System.currentTimeMillis() - t0);
        };
    }

    private static Timed get(Future<Timed> future) throws Exception {
        if (future == null) return new Timed(List.of(), 0);
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
    }

    private static String str(Object value) {
        return value == null ? "" : value.toString();
    }
}
