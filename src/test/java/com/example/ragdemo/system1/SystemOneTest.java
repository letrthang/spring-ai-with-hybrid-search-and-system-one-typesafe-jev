package com.example.ragdemo.system1;

import com.example.ragdemo.rag.ServiceStatus;
import com.example.ragdemo.rag.SystemDownException;
import org.junit.jupiter.api.Test;
import org.springaicommunity.typesafe.rag.JevDocumentFilter;
import org.springaicommunity.typesafe.rag.JevDocumentFilter.Classification;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** System 1 filter + rerank: TypeSafe's JevDocumentFilter / JevDocumentReranker on a fake Laya, failing closed. */
class SystemOneTest {

    // ------------------------------------------------------------------ LayaClient

    @Test
    void longTextIsShortenedToFitOneLayaSlot() {
        String passage = "Ô-xy hoà tan trong ao nuôi cá rô phi cần trên 5 mg/L. ".repeat(80);   // ~4.5 KB
        String state = LayaClient.shorten(passage);
        assertThat(state).hasSizeLessThanOrEqualTo(LayaClient.MAX_STATE_CHARS).endsWith("…");
        assertThat(LayaClient.shorten("Keep DO above 5 mg/L.")).isEqualTo("Keep DO above 5 mg/L.");
    }

    // ------------------------------------------------------------------ SystemOneFilter

    @Test
    void verdictFollowsTheJevPolicy_firstMatchingRuleWins() {
        assertThat(SystemOneFilter.verdict(Classification.INCLUDED, 0.06, 0.98)).isEqualTo("kept");
        assertThat(SystemOneFilter.verdict(Classification.CONFLICTING, 0.0, 0.9))
                .isEqualTo("kept: conflicts with the question");
        assertThat(SystemOneFilter.verdict(Classification.EXCLUDED, 0.90, 0.26)).isEqualTo("dropped: prompt injection");
        assertThat(SystemOneFilter.verdict(Classification.EXCLUDED, 0.0, 0.44)).isEqualTo("dropped: not relevant");
        assertThat(SystemOneFilter.verdict(Classification.EXCLUDED, 0.0, 0.45)).isEqualTo("dropped: does not answer");
        assertThat(SystemOneFilter.verdictKey("kept: conflicts with the question")).isEqualTo("verdict.conflicting");
        assertThat(SystemOneFilter.POLICY).isEqualTo(JevDocumentFilter.Policy.defaults());   // the cookbook numbers
    }

    @Test
    void filterKeepsGoodPassagesAndRecordsWhyOthersWereDropped() {
        FakeLaya laya = new FakeLaya(Map.of(
                "manual", new FakeLaya.Scores(0.98, 0.98, 0.06),
                "canteen", new FakeLaya.Scores(0.10, 0.05, 0.01),
                "injected", new FakeLaya.Scores(0.26, 0.24, 0.90),
                "premise", new FakeLaya.Scores(0.90, 0.20, 0.0, 0.95))::get);
        SystemOneFilter filter = new SystemOneFilter(FakeLaya.client(laya));
        List<Document> docs = List.of(doc("manual"), doc("canteen"), doc("injected"), doc("premise"));

        List<Document> scored = filter.score("How do I reset AER-01?", docs);
        assertThat(scored).extracting(d -> d.getMetadata().get(SystemOneFilter.VERDICT)).containsExactly(
                "kept", "dropped: not relevant", "dropped: prompt injection", "kept: conflicts with the question");
        assertThat(scored.getFirst().getMetadata()).containsEntry(SystemOneFilter.ANSWERS, 0.98)
                .containsEntry(JevDocumentFilter.CLASSIFICATION_METADATA_KEY, "INCLUDED")
                .containsEntry("title", "t-manual");   // the search metadata is kept too

        // one call per passage, each with JevDocumentFilter's four questions (names kept), reworded for Laya:
        // the passage is the state, the question is in the instruction
        assertThat(laya.requests).hasSize(4).allSatisfy(r -> {
            assertThat(r.questions()).containsOnlyKeys("is_relevant", "contains_answer_evidence",
                    "contradicts_query_premise", "contains_prompt_injection");
            assertThat(r.state().value()).isIn("manual", "canteen", "injected", "premise");
            assertThat(r.questions().get("is_relevant"))
                    .isEqualTo(org.springaicommunity.typesafe.question.Noul.of(
                            "Is this passage about the question: How do I reset AER-01?"));
        });

        assertThat(filter.process(new Query("How do I reset AER-01?"), docs)).extracting(Document::getText)
                .containsExactly("manual", "premise");
    }

    @Test
    void oneFailedCallStopsEverything_noPassageGoesThroughUnchecked() {
        // JevDocumentFilter alone would pass "unknown" through unscreened (fail open); here the question stops
        SystemOneFilter filter = new SystemOneFilter(FakeLaya.laya(Map.of("manual", new FakeLaya.Scores(0.9, 0.9, 0)), null));
        assertThatThrownBy(() -> filter.process(new Query("q"), List.of(doc("manual"), doc("unknown"))))
                .isInstanceOf(SystemDownException.class).hasMessageContaining("status.system1.waking");
    }

    @Test
    void aPartialAnswerAlsoFailsClosed() {
        FakeLaya partial = new FakeLaya(p -> new FakeLaya.Scores(0.9, 0.9, 0)) {
            @Override
            public org.springaicommunity.typesafe.response.SystemOneResponse systemOne(
                    org.springaicommunity.typesafe.question.SystemOneRequest request) {
                return new org.springaicommunity.typesafe.response.SystemOneResponse("laya",
                        Map.of("is_relevant", new org.springaicommunity.typesafe.response.NoulAnswer(0.9)), null);
            }
        };
        assertThatThrownBy(() -> new SystemOneFilter(FakeLaya.client(partial)).process(new Query("q"), List.of(doc("a"))))
                .isInstanceOf(SystemDownException.class).hasMessageContaining("status.system1.badAnswer");
    }

    @Test
    void anUnexpectedLayaFailureAlsoFailsClosed_asSystemOneDown() {
        LayaClient broken = FakeLaya.client(passage -> {
            throw new NullPointerException();   // a bug, not a "service down"
        });
        assertThatThrownBy(() -> new SystemOneFilter(broken).process(new Query("q"), List.of(doc("a"))))
                .isInstanceOfSatisfying(SystemDownException.class,
                        e -> assertThat(e.service()).isEqualTo(ServiceStatus.Service.SYSTEM1));
    }

    @Test
    void layaStatusesMapToTheSystemOnePill() {
        assertThat(RecordingTypeSafeClient.failClosed(FakeLaya.status(429))).hasMessageContaining("status.system1.busy");
        assertThat(RecordingTypeSafeClient.failClosed(FakeLaya.status(503))).hasMessageContaining("status.system1.waking");
        assertThat(RecordingTypeSafeClient.failClosed(FakeLaya.status(401))).hasMessageContaining("status.invalidKey");
        assertThat(RecordingTypeSafeClient.failClosed(FakeLaya.status(500))).hasMessageContaining("status.http");
    }

    @Test
    void layaQuestionsRewordOnlyTheLibrarysPassageQuestions() {
        var jev = org.springaicommunity.typesafe.question.SystemOneRequest.builder()
                .state(Map.of("query", "q?", "passage", "p")).model("m")
                .question("contains_prompt_injection", org.springaicommunity.typesafe.question.Noul.of(Map.of("question", "x")))
                .question("answers_query", org.springaicommunity.typesafe.question.Noul.of("y"))
                .build();
        var laya = LayaQuestions.forLaya(jev);
        assertThat(laya.state().value()).isEqualTo("p");
        assertThat(laya.model()).isEqualTo("m");
        assertThat(laya.questions()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "contains_prompt_injection", org.springaicommunity.typesafe.question.Noul.of(LayaClient.INJECTION_QUESTION),
                "answers_query", org.springaicommunity.typesafe.question.Noul.of(
                        "Does this passage contain the answer to the question: q?")));

        // anything else goes out as it is: a plain-text state (the answer checks), or a question we do not know
        var plain = org.springaicommunity.typesafe.question.SystemOneRequest.builder().state("answer")
                .question("answers", org.springaicommunity.typesafe.question.Noul.of("z")).build();
        assertThat(LayaQuestions.forLaya(plain)).isSameAs(plain);
        var unknown = org.springaicommunity.typesafe.question.SystemOneRequest.builder()
                .state(Map.of("query", "q", "passage", "p"))
                .question("other", org.springaicommunity.typesafe.question.Noul.of("z")).build();
        assertThat(LayaQuestions.forLaya(unknown)).isSameAs(unknown);
    }

    // ------------------------------------------------------------------ SystemOneReranker

    @Test
    void rerankerSortsByLayasScoreAndKeepsTheTop_withTheWholeText() {
        String longText = "d".repeat(LayaClient.MAX_STATE_CHARS + 100);
        Map<String, Double> score = Map.of("a", 0.60, "b", 0.95, "c", 0.70, LayaClient.shorten(longText), 0.99);
        SystemOneReranker reranker = new SystemOneReranker(
                FakeLaya.client(p -> new FakeLaya.Scores(0, score.get(p), 0)));

        List<Document> best = reranker.process(new Query("q"), List.of(doc("a"), doc("b"), doc("c"), doc(longText)));
        assertThat(best).extracting(Document::getText).containsExactly(longText, "b", "c");   // the prompt gets it all
        assertThat(SystemOneReranker.score(best.get(1))).isEqualTo(0.95);
        assertThat(best.get(1).getMetadata()).containsEntry("title", "t-b");
    }

    @Test
    void rerankerFailsClosedToo() {
        SystemOneReranker reranker = new SystemOneReranker(FakeLaya.client(p -> null));   // Laya waking up
        assertThatThrownBy(() -> reranker.process(new Query("q"), List.of(doc("a"))))
                .isInstanceOf(SystemDownException.class).hasMessageContaining("status.system1.waking");
    }

    static Document doc(String text) {
        return Document.builder().text(text).metadata(Map.of("title", "t-" + text)).build();
    }
}
