package com.example.ragdemo.weaviate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reciprocal Rank Fusion: merge two ranked lists into one.
 *
 * <p>Each passage gets {@code 1 / (K + rank)} from every list it is in (rank 1 = best), and the parts are added.
 * A passage high in <b>both</b> lists beats one that is first in only one list. RRF only looks at ranks, never at the
 * raw scores, so a BM25 score (0..∞) and a vector distance (0..2) can be merged without any tuning.
 *
 * <pre>
 *   BM25:   A, B, C        vector: C, D, A
 *   A = 1/61 + 1/63 = 0.0323   ← in both lists
 *   C = 1/63 + 1/61 = 0.0323
 *   B = 1/62        = 0.0161
 *   D = 1/62        = 0.0161
 * </pre>
 */
public final class RrfFusion {

    /** The usual constant from the RRF paper; it stops rank 1 from dominating. */
    public static final int K = 60;

    /** One fused entry. A rank is null when the passage is not in that list. */
    public record Fused(String id, double score, Integer bm25Rank, Integer vectorRank) {
    }

    private RrfFusion() {
    }

    /** Best first; ties keep the order in which the passage was first seen (BM25 list first). */
    public static List<Fused> fuse(List<String> bm25Ids, List<String> vectorIds) {
        Map<String, Integer> bm25 = ranks(bm25Ids);
        Map<String, Integer> vector = ranks(vectorIds);
        LinkedHashMap<String, Boolean> all = new LinkedHashMap<>();
        bm25Ids.forEach(id -> all.put(id, true));
        vectorIds.forEach(id -> all.put(id, true));

        List<Fused> fused = new ArrayList<>();
        for (String id : all.keySet()) {
            Integer b = bm25.get(id);
            Integer v = vector.get(id);
            double score = (b == null ? 0 : 1.0 / (K + b)) + (v == null ? 0 : 1.0 / (K + v));
            fused.add(new Fused(id, score, b, v));
        }
        fused.sort(Comparator.comparingDouble(Fused::score).reversed());   // stable: ties keep insertion order
        return fused;
    }

    /** id → rank (1-based). A duplicate id keeps its best rank. */
    private static Map<String, Integer> ranks(List<String> ids) {
        Map<String, Integer> ranks = new LinkedHashMap<>();
        for (int i = 0; i < ids.size(); i++) ranks.putIfAbsent(ids.get(i), i + 1);
        return ranks;
    }
}
