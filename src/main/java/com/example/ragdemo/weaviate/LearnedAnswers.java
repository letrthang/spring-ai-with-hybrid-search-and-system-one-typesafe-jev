package com.example.ragdemo.weaviate;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.FetchObjects;
import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 🧠 Self-learning store: verified answers in the {@value LearnedAnswerCollection#NAME} collection.
 *
 * <ul>
 *   <li>{@link #closest} — the learned question nearest to a new one (same language, by meaning).</li>
 *   <li>{@link #save} — one object per question (the id comes from language + question), so asking the same question
 *       again replaces the old answer instead of adding duplicates.</li>
 * </ul>
 */
@Component
public class LearnedAnswers {

    private static final Logger log = LoggerFactory.getLogger(LearnedAnswers.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** A passage a learned answer came from: shown again (as its source) when the answer is reused. */
    public record LearnedSource(String title, String source, Integer chunkNo, String text, Integer citation) {
    }

    /** One learned answer, and how close its question is to the asked one (cosine distance, smaller = closer). */
    public record Learned(String id, String question, String answer, String lang, String model,
                          List<LearnedSource> sources, Double answersScore, Double groundedScore,
                          Double injectionScore, Float distance) {
    }

    private final WeaviateConnector weaviate;
    private CollectionConfig definition = LearnedAnswerCollection.definition();

    public LearnedAnswers(WeaviateConnector weaviate) {
        this.weaviate = weaviate;
    }

    /** Tests use a local vectorizer. */
    void useDefinition(CollectionConfig local) {
        this.definition = local;
    }

    /** The nearest learned question in the same language, if any answer was learned yet. */
    public Optional<Learned> closest(String question) {
        String lang = Lang.of(question);
        return weaviate.call("learned lookup", client -> {
            if (!client.collections.exists(LearnedAnswerCollection.NAME)) return Optional.<Learned>empty();
            var found = client.collections.use(LearnedAnswerCollection.NAME).query
                    .nearText(question, q -> q.limit(1).filters(Filter.property("lang").eq(lang))
                            .returnMetadata(Metadata.DISTANCE)).objects();
            return found.stream().findFirst().map(LearnedAnswers::learned);
        });
    }

    /** Saves (or replaces) the answer to this question. Creates the collection the first time. */
    public String save(String question, String answer, String model, List<LearnedSource> sources,
                       double answersScore, double groundedScore, double injectionScore) {
        String lang = Lang.of(question);
        String id = idOf(lang, question);
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("question", question.strip());
        p.put("answer", answer.strip());
        p.put("lang", lang);
        p.put("model", model == null ? "" : model);
        p.put("sources", JSON.writeValueAsString(sources));
        p.put("answersScore", answersScore);
        p.put("groundedScore", groundedScore);
        p.put("injectionScore", injectionScore);
        return weaviate.call("learn", client -> {
            LearnedAnswerCollection.ensure(client, definition);
            var data = client.collections.use(LearnedAnswerCollection.NAME).data;
            if (data.exists(id)) data.deleteById(id);   // the same question again: the newest answer wins
            data.insert(p, o -> o.uuid(id));
            log.info("🧠 learned: \"{}\" ({})", question.length() > 80 ? question.substring(0, 80) + "…" : question, lang);
            return id;
        });
    }

    public long count() {
        return weaviate.call("learned count", LearnedAnswerCollection::count);
    }

    /** A page of learned answers (Search Data → 🧠 Learned answers). */
    public List<Learned> list(int offset, int limit) {
        return weaviate.call("learned list", client -> {
            if (!client.collections.exists(LearnedAnswerCollection.NAME)) return List.<Learned>of();
            return client.collections.use(LearnedAnswerCollection.NAME).query
                    .fetchObjects(FetchObjects.of(f -> f.offset(offset).limit(limit))).objects().stream()
                    .map(LearnedAnswers::learned).toList();
        });
    }

    /** Forgets everything learned (drops the collection). */
    public void forgetAll() {
        weaviate.call("forget learned", client -> {
            if (client.collections.exists(LearnedAnswerCollection.NAME)) {
                client.collections.delete(LearnedAnswerCollection.NAME);
            }
            return null;
        });
    }

    /** Same language + same question (case and spaces ignored) → same id. */
    static String idOf(String lang, String question) {
        String key = lang + "|" + question.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static Learned learned(WeaviateObject<Map<String, Object>> o) {
        Map<String, Object> p = o.properties() == null ? Map.of() : o.properties();
        List<LearnedSource> sources = List.of();
        Object raw = p.get("sources");
        if (raw != null && !raw.toString().isBlank()) {
            try {
                sources = JSON.readValue(raw.toString(), new TypeReference<List<LearnedSource>>() { });
            } catch (RuntimeException e) {
                log.debug("🧠 learned sources unreadable: {}", e.getMessage());
            }
        }
        return new Learned(o.uuid(), str(p.get("question")), str(p.get("answer")), str(p.get("lang")),
                str(p.get("model")), sources, num(p.get("answersScore")), num(p.get("groundedScore")),
                num(p.get("injectionScore")), o.queryMetadata() == null ? null : o.queryMetadata().distance());
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private static Double num(Object o) {
        return o instanceof Number n ? n.doubleValue() : null;
    }
}
