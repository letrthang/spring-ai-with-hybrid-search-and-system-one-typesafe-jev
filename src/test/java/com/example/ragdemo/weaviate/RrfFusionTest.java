package com.example.ragdemo.weaviate;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RrfFusionTest {

    @Test
    void passageInBothListsBeatsPassageFirstInOnlyOne() {
        // BM25: A, B, C   vector: C, D, A   (the example in the RrfFusion javadoc)
        List<RrfFusion.Fused> fused = RrfFusion.fuse(List.of("A", "B", "C"), List.of("C", "D", "A"));

        assertThat(fused).extracting(RrfFusion.Fused::id).containsExactly("A", "C", "B", "D");
        assertThat(fused.get(0).score()).isCloseTo(1.0 / 61 + 1.0 / 63, within(1e-12));
        assertThat(fused.get(2).score()).isCloseTo(1.0 / 62, within(1e-12));
        assertThat(fused.get(0).bm25Rank()).isEqualTo(1);
        assertThat(fused.get(0).vectorRank()).isEqualTo(3);
        assertThat(fused.get(3).bm25Rank()).isNull();
    }

    @Test
    void architectureExample_eachMethodWinsOneQuestion() {
        // "how to reset AER-01": BM25 finds the manual first, vector prefers the oxygen passage
        assertThat(RrfFusion.fuse(List.of("manual"), List.of("oxygen", "manual")).getFirst().id())
                .isEqualTo("manual");
        // "fish breathing at the top": BM25 finds nothing useful, vector finds the oxygen passage
        assertThat(RrfFusion.fuse(List.of(), List.of("oxygen", "manual")).getFirst().id()).isEqualTo("oxygen");
    }

    @Test
    void oneListOnlyKeepsItsOrder() {
        assertThat(RrfFusion.fuse(List.of("x", "y", "z"), List.of())).extracting(RrfFusion.Fused::id)
                .containsExactly("x", "y", "z");
    }
}
