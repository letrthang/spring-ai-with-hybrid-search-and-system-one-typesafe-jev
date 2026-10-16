package com.example.ragdemo.ui;

import java.util.Arrays;
import java.util.List;

/**
 * The ℹ️ learning hints. Each one answers: what is it · what does it do here · what is it linked to · a little theory ·
 * where to read more. The texts live in the i18n files ({@code hint.<NAME>.name / .title / .what / .does / .theory}),
 * so every lesson is in English, Vietnamese and Japanese; this enum only holds the links between lessons and the
 * "learn more" address.
 */
public enum Hint {

    RAG("https://en.wikipedia.org/wiki/Retrieval-augmented_generation", "WEAVIATE", "SYSTEM1", "SYSTEM2", "SOURCES"),
    WEAVIATE("https://docs.weaviate.io/weaviate/concepts", "BM25", "VECTOR", "RRF", "SEARCH_MODES"),
    SYSTEM1("https://en.wikipedia.org/wiki/Thinking,_Fast_and_Slow", "WEAVIATE", "SYSTEM2", "SOURCES"),
    SYSTEM2("https://en.wikipedia.org/wiki/Large_language_model", "SYSTEM1", "RAG", "TRACE"),
    BM25("https://en.wikipedia.org/wiki/Okapi_BM25", "VECTOR", "RRF", "WEAVIATE"),
    VECTOR("https://en.wikipedia.org/wiki/Cosine_similarity", "BM25", "RRF", "EMBEDDING"),
    RRF("https://plg.uwaterloo.ca/~gvcormac/cormacksigir09-rrf.pdf", "BM25", "VECTOR", "SYSTEM1"),
    LAYA_SCORES("https://spring.io/blog/2026/10/02/spring-ai-modular-rag-typesafe-jev", "SYSTEM1", "INJECTION", "SYSTEM2"),
    INJECTION("https://genai.owasp.org/llmrisk/llm01-prompt-injection/", "SYSTEM1", "SYSTEM2"),
    SOURCES("https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html", "BM25", "VECTOR", "RRF", "SYSTEM1"),
    TRACE("https://openrouter.ai/docs", "DATA_FLOW", "SYSTEM2"),
    DATA_FLOW("https://en.wikipedia.org/wiki/Sequence_diagram", "TRACE", "SOURCES"),
    PROGRESS(null, "WEAVIATE", "SYSTEM1", "SYSTEM2"),
    FAIL_CLOSED(null, "WEAVIATE", "SYSTEM1", "SYSTEM2", "KEYS"),
    HISTORY(null, "RAG"),
    SEARCH_MODES("https://docs.weaviate.io/weaviate/concepts/search/hybrid-search", "BM25", "VECTOR", "RRF"),
    ROW_ORDER(null, "RRF", "BM25", "VECTOR"),
    CHUNKS(null, "WEAVIATE", "RECORD"),
    RECORD("https://docs.weaviate.io/weaviate/concepts/data", "SCHEMA", "EMBEDDING"),
    SCHEMA("https://docs.weaviate.io/weaviate/concepts/vector-index", "BM25", "VECTOR", "RECORD"),
    EMBEDDING("https://en.wikipedia.org/wiki/Word_embedding", "VECTOR", "SCHEMA"),
    KEYS(null, "WEAVIATE", "SYSTEM2", "FAIL_CLOSED", "OWN_KEYS"),
    OWN_KEYS(null, "KEYS", "WEAVIATE", "SYSTEM2"),
    DATABASE("https://www.h2database.com/html/main.html", "HISTORY", "KEYS"),
    LEARNING(null, "SYSTEM1", "SYSTEM2", "WEAVIATE", "INJECTION"),
    MODELS("https://openrouter.ai/models", "SYSTEM2", "TRACE"),
    AGENTIC("https://docs.spring.io/spring-ai/reference/api/tools.html", "SYSTEM2", "WEAVIATE", "SYSTEM1", "LEARNING",
            "DATABASE", "DATA_FLOW");

    /** Where to read more (may be null). */
    public final String learnMore;
    private final String[] linkNames;

    Hint(String learnMore, String... links) {
        this.learnMore = learnMore;
        this.linkNames = links;
    }

    /** The lessons this one links to ("Linked to" chips). */
    public List<Hint> links() {
        return Arrays.stream(linkNames).map(Hint::valueOf).toList();
    }

    public String title() {
        return Tr.t("hint." + name() + ".title");
    }

    public String what() {
        return Tr.t("hint." + name() + ".what");
    }

    public String does() {
        return Tr.t("hint." + name() + ".does");
    }

    public String theory() {
        return Tr.t("hint." + name() + ".theory");
    }

    /** A short name, for "Linked to" chips. */
    public String shortName() {
        return Tr.t("hint." + name() + ".name");
    }
}
