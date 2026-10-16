package com.example.ragdemo.system1;

import com.example.ragdemo.rag.SystemDownException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemOneTest {

    // ------------------------------------------------------------------ LayaClient

    @Test
    void requestBodyNeverExceedsTheLimitEvenForLongVietnameseText() {
        String passage = "Ô-xy hoà tan trong ao nuôi cá rô phi cần trên 5 mg/L. ".repeat(80);   // ~4.5 KB
        String body = LayaClient.requestBody("Cá nổi đầu là do thiếu ô-xy phải không?", passage);
        assertThat(body.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(LayaClient.MAX_BODY_BYTES);
        assertThat(body).contains("\"relevant\"", "\"answers\"", "\"injection\"", "\"noul\"", "…");
    }

    @Test
    void shortPassageIsSentUnchanged() {
        assertThat(LayaClient.requestBody("q?", "Keep DO above 5 mg/L.")).contains("\"state\":\"Keep DO above 5 mg/L.\"");
    }

    @Test
    void parsesTheThreeProbabilities() {
        LayaClient.Scores s = LayaClient.parse("""
                {"model":"laya","answers":{"relevant":{"type":"noul","noul":0.98},
                 "answers":{"type":"noul","noul":0.97},"injection":{"type":"noul","noul":0.06}}}""", 12);
        assertThat(s).isEqualTo(new LayaClient.Scores(0.98, 0.97, 0.06, 12));
        assertThatThrownBy(() -> LayaClient.parse("{\"answers\":{}}", 1)).isInstanceOf(SystemDownException.class);
    }

    // ------------------------------------------------------------------ SystemOneFilter

    @Test
    void verdictUsesTheJevThresholds() {
        assertThat(SystemOneFilter.verdict(new LayaClient.Scores(0.98, 0.98, 0.06, 0))).isEqualTo("kept");
        assertThat(SystemOneFilter.verdict(new LayaClient.Scores(0.26, 0.24, 0.90, 0)))
                .isEqualTo("dropped: prompt injection");
        assertThat(SystemOneFilter.verdict(new LayaClient.Scores(0.44, 0.90, 0.0, 0))).isEqualTo("dropped: not relevant");
        assertThat(SystemOneFilter.verdict(new LayaClient.Scores(0.45, 0.54, 0.0, 0)))
                .isEqualTo("dropped: does not answer");
        assertThat(SystemOneFilter.verdict(new LayaClient.Scores(0.45, 0.55, 0.69, 0))).isEqualTo("kept");
    }

    /** A Laya that scores by passage text, without HTTP. */
    static LayaClient fakeLaya(Map<String, LayaClient.Scores> byText) {
        return new LayaClient(null) {
            @Override
            public Scores score(String question, String passage) {
                Scores s = byText.get(passage);
                if (s == null) throw new SystemDownException(com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM1,
                        "status.system1.waking");
                return s;
            }
        };
    }

    @Test
    void filterKeepsGoodPassagesAndRecordsWhyOthersWereDropped() {
        SystemOneFilter filter = new SystemOneFilter(fakeLaya(Map.of(
                "manual", new LayaClient.Scores(0.98, 0.98, 0.06, 5),
                "canteen", new LayaClient.Scores(0.10, 0.05, 0.01, 5),
                "injected", new LayaClient.Scores(0.26, 0.24, 0.90, 5))));
        List<Document> docs = List.of(doc("manual"), doc("canteen"), doc("injected"));

        List<Document> scored = filter.score("How do I reset AER-01?", docs);
        assertThat(scored).extracting(d -> d.getMetadata().get(SystemOneFilter.VERDICT))
                .containsExactly("kept", "dropped: not relevant", "dropped: prompt injection");
        assertThat(scored.getFirst().getMetadata()).containsEntry(SystemOneFilter.ANSWERS, 0.98)
                .containsEntry("title", "t-manual");   // the search metadata is kept too

        assertThat(filter.process(new Query("How do I reset AER-01?"), docs)).extracting(Document::getText)
                .containsExactly("manual");
    }

    @Test
    void oneFailedCallStopsEverything_noPassageGoesThroughUnchecked() {
        SystemOneFilter filter = new SystemOneFilter(fakeLaya(Map.of("manual", new LayaClient.Scores(0.9, 0.9, 0, 1))));
        assertThatThrownBy(() -> filter.process(new Query("q"), List.of(doc("manual"), doc("unknown"))))
                .isInstanceOf(SystemDownException.class).hasMessageContaining("status.system1.waking");
    }

    // ------------------------------------------------------------------ SystemOneReranker

    @Test
    void rerankerSortsByAnswersAndKeepsTheTop() {
        List<Document> docs = List.of(scored("a", 0.60), scored("b", 0.95), scored("c", 0.70), scored("d", 0.99));
        assertThat(new SystemOneReranker().process(new Query("q"), docs)).extracting(Document::getText)
                .containsExactly("d", "b", "c");
    }

    static Document doc(String text) {
        return Document.builder().text(text).metadata(Map.of("title", "t-" + text)).build();
    }

    static Document scored(String text, double answers) {
        return Document.builder().text(text).metadata(Map.of(SystemOneFilter.ANSWERS, answers)).build();
    }
}
