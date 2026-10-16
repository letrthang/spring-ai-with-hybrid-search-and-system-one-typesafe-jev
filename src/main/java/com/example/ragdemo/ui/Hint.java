package com.example.ragdemo.ui;

import java.util.List;

/**
 * The ℹ️ learning hints — every explanation in one place, so the wording stays consistent and is easy to improve.
 * Each hint answers: what is it · what does it do here · what is it linked to · a little theory · where to read more.
 */
public enum Hint {

    RAG("💡 RAG — Retrieval-Augmented Generation",
            "A way to make an AI model answer from your documents instead of from memory alone.",
            "This app looks up passages in Weaviate, lets System 1 keep the useful ones, and gives those to System 2 "
                    + "to write the answer — with numbered citations you can check.",
            List.of("🗄️ Weaviate", "⚡ System 1", "🧠 System 2", "📚 Sources"),
            "A language model only knows what it saw in training and can invent facts (\"hallucinate\"). Putting the "
                    + "right passages into the prompt (retrieval + augmentation) grounds the answer in real text.",
            "https://en.wikipedia.org/wiki/Retrieval-augmented_generation"),

    WEAVIATE("🗄️ Weaviate — the vector database",
            "A database that stores each passage twice: as words (for keyword search) and as a vector (for meaning search).",
            "Holds the 46 sample passages in the collection DemoDoc and answers two searches for every question: BM25 "
                    + "and nearText. It also turns text into vectors itself (its built-in vectorizer).",
            List.of("🔤 BM25", "🧭 Vector search", "🔀 RRF", "🔎 Search Data"),
            "Classic databases find exact values. A vector database finds the nearest vectors — texts with a similar "
                    + "meaning — using an index so it never has to compare every vector.",
            "https://docs.weaviate.io/weaviate/concepts"),

    SYSTEM1("⚡ System 1 — Laya, a decision model",
            "A small, fast model that answers yes/no questions with a probability (0 to 1). It never writes text.",
            "For each passage it answers three questions — is it relevant? can it answer? is it a prompt injection? — "
                    + "and the passage is kept or dropped. The kept ones are sorted by \"answers\".",
            List.of("🗄️ Weaviate (gives it passages)", "🧠 System 2 (gets the kept ones)", "📚 Sources"),
            "\"System 1 / System 2\" comes from Daniel Kahneman: fast intuition vs slow reasoning. Here a cheap fast "
                    + "model filters, so the expensive model only reads what is worth reading.",
            "https://en.wikipedia.org/wiki/Thinking,_Fast_and_Slow"),

    SYSTEM2("🧠 System 2 — the large language model (LLM)",
            "A general AI model that writes text. Here: free models on OpenRouter, tried in order.",
            "Gets the question plus the passages System 1 kept, numbered [1], [2]…, and writes a short answer that "
                    + "cites them. If a model is busy (429) the next one in the list is tried.",
            List.of("⚡ System 1", "✍️ Augment (the prompt)", "🔬 Trace (model, tokens)"),
            "An LLM predicts the next token (a piece of a word) again and again. It is good at language, but it needs "
                    + "the facts in its prompt to be reliable — that is what RAG gives it.",
            "https://en.wikipedia.org/wiki/Large_language_model"),

    BM25("🔤 BM25 — keyword search",
            "The classic search-engine ranking: passages that share the question's words score higher.",
            "Weaviate ranks passages by BM25 over title (counted twice) and body. The score is shown in 🔎 Search Data "
                    + "and as \"BM25 #rank\" in 📚 Sources.",
            List.of("🧭 Vector search", "🔀 RRF", "🗄️ Weaviate"),
            "BM25 rewards rare words (\"PCR\", \"Cas9\") more than common ones (\"the\"), and words that appear often "
                    + "in a short passage. Great for exact names and codes; blind to synonyms.",
            "https://en.wikipedia.org/wiki/Okapi_BM25"),

    VECTOR("🧭 Vector search — search by meaning",
            "Each text becomes a list of numbers (an embedding / vector). Texts with a similar meaning get nearby vectors.",
            "Weaviate embeds the question and returns the passages whose vectors are closest. \"distance\" is how far "
                    + "apart they are: smaller = more similar.",
            List.of("🔤 BM25", "🔀 RRF", "🧮 Vector (1024 numbers)"),
            "An embedding model is trained so that \"warm water holds less oxygen\" lands near \"gas solubility falls "
                    + "with temperature\" even with no shared words. Distance here is cosine distance (angle between vectors).",
            "https://en.wikipedia.org/wiki/Cosine_similarity"),

    RRF("🔀 RRF — Reciprocal Rank Fusion (hybrid search)",
            "A simple way to merge two ranked lists into one.",
            "Every passage gets 1 / (60 + rank) from the BM25 list and from the vector list; the two parts are added. "
                    + "The top 4 go to System 1.",
            List.of("🔤 BM25", "🧭 Vector search", "⚡ System 1"),
            "RRF only uses ranks, not raw scores, so a BM25 score (0…∞) and a distance (0…2) can be combined without "
                    + "tuning. A passage that is good in both lists wins. Paper: Cormack et al., 2009.",
            "https://plg.uwaterloo.ca/~gvcormac/cormacksigir09-rrf.pdf"),

    LAYA_SCORES("⚡ relevant · answers · injection",
            "The three probabilities System 1 gives each passage.",
            "Kept only if relevant ≥ 0.45, answers ≥ 0.55 and injection < 0.70 (the same thresholds the Spring AI "
                    + "blog uses for Jev). Then sorted by \"answers\"; the best 3 go to System 2.",
            List.of("⚡ System 1", "🛡️ Prompt injection", "🧠 System 2"),
            "These are calibrated yes/no probabilities, not text. A threshold turns a probability into a decision — "
                    + "lower it to keep more, raise it to be stricter.",
            "https://spring.io/blog/2026/10/02/spring-ai-modular-rag-typesafe-jev"),

    INJECTION("🛡️ Prompt injection",
            "Text hidden in a document that tries to give orders to the AI (\"ignore all previous instructions…\").",
            "System 1 scores every passage for it; a likely injection is dropped before System 2 ever sees it. The "
                    + "sample data has one planted note to show this.",
            List.of("⚡ System 1", "🧠 System 2"),
            "RAG puts outside text into the prompt, so that text can try to take control. Filtering before generation "
                    + "is one layer of defence; it is the #1 risk in the OWASP Top 10 for LLM apps.",
            "https://genai.owasp.org/llmrisk/llm01-prompt-injection/"),

    SOURCES("📚 Sources",
            "Every passage the search found for this question, and what happened to it.",
            "Shows each passage's BM25 rank, vector rank, RRF score, System 1's three scores and ✅ kept / 🚫 dropped. "
                    + "[n] marks the passages System 2 was given and may cite.",
            List.of("🔤 BM25", "🧭 Vector", "🔀 RRF", "⚡ System 1"),
            "Showing sources makes an AI answer checkable — a core idea of RAG.",
            "https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html"),

    TRACE("🔬 Trace",
            "Timings and numbers for each step of this answer.",
            "How long Weaviate, System 1 and System 2 took, which model answered, how many tokens went in and out, "
                    + "and which models were skipped.",
            List.of("🔀 Data flow", "🧠 System 2"),
            "A token is a piece of a word (about ¾ of an English word). Models are limited and priced by tokens.",
            "https://openrouter.ai/docs"),

    DATA_FLOW("🔀 Data flow",
            "The whole trip of one question as a sequence diagram.",
            "One column per part (User, App, Weaviate, System 1, System 2), one numbered arrow per step with its "
                    + "start time and duration. Solid = a call, dashed green = a reply, a loop = work inside the app.",
            List.of("🔬 Trace", "📚 Sources"),
            "Sequence diagrams come from UML; they show who talks to whom, in what order — the best way to understand "
                    + "a pipeline.",
            "https://en.wikipedia.org/wiki/Sequence_diagram"),

    PROGRESS("⏳ Progress steps",
            "Where your question is right now.",
            "🗄️ searching Weaviate → ⚡ System 1 checking passages → 🧠 System 2 writing. ⛔ marks the step whose "
                    + "service failed; ➖ means System 2 was not needed (no passage kept).",
            List.of("🗄️ Weaviate", "⚡ System 1", "🧠 System 2"),
            "Each step reports itself as it starts, and the page is updated live (server push).",
            null),

    FAIL_CLOSED("🚦 Service status — fail closed",
            "All three services are required. 🟢 ready · 🟡 waking up / busy / no data · 🔴 down.",
            "If any one is not 🟢, the chat is disabled and the banner says why. The status is checked every 30 s.",
            List.of("🗄️ Weaviate", "⚡ System 1", "🧠 System 2", "🔑 Key Management"),
            "\"Fail closed\" means: when a safety step cannot run, do nothing rather than continue without it — so no "
                    + "unchecked passage ever reaches the LLM.",
            null),

    HISTORY("📂 History",
            "Every conversation, saved in the app's local H2 database.",
            "Click a row for a short card, double-click to read it with all its Sources, Trace and Data flow; "
                    + "▶ Continue here asks more in the same conversation.",
            List.of("💬 Chat"),
            "Each answer is stored with its whole trace as JSON, so it can be shown again exactly as it happened. On "
                    + "Render the database is emptied on every restart.",
            null),

    SEARCH_MODES("🔎 BM25 · Vector · Hybrid",
            "Three ways to search the same passages.",
            "BM25 = by words, Vector = by meaning, Hybrid = both merged with RRF (what the chat uses). Only the "
                    + "columns a mode fills are shown.",
            List.of("🔤 BM25", "🧭 Vector", "🔀 RRF"),
            "Try a keyword question (\"Newton's second law\") and a meaning question (\"why does warm water hold less "
                    + "oxygen\") in each mode and compare the ranks.",
            "https://docs.weaviate.io/weaviate/concepts/search/hybrid-search"),

    ROW_ORDER("# — the position in this list",
            "Where this passage stands in the list you are looking at.",
            "After a search it is the final order: by RRF score in Hybrid, by BM25 score in BM25, by vector distance "
                    + "in Vector. With an empty query it is just the order Weaviate returns the objects in — no ranking.",
            List.of("🔀 RRF", "🔤 BM25", "🧭 Vector"),
            "A search engine's job is ordering: the top of the list is what it thinks fits best. Compare the three "
                    + "modes to see that different methods give different orders for the same question.",
            null),

    CHUNKS("📥 Passages (chunks)",
            "Small pieces of the documents — here one per \"## section\" of each sample file.",
            "📥 Load sample data drops the collection, creates it again and inserts every passage. Weaviate makes "
                    + "each passage's vector while inserting.",
            List.of("🗄️ Weaviate", "🧾 Record"),
            "Search works on passages, not whole documents: a small focused passage matches a question better and "
                    + "fits in the prompt. Splitting at headings keeps each passage about one topic.",
            null),

    RECORD("🧾 A Weaviate record (object)",
            "One stored passage: its id (UUID), its fields (properties) and its vector.",
            "Lives in cluster → collection DemoDoc → object UUID. The UUID is made from \"file#section\", so loading "
                    + "again gives the same id. You can fetch it at the REST address shown.",
            List.of("🧱 Data structure", "🧮 Vector"),
            "A UUID is a 128-bit id; a name-based UUID (version 3) always gives the same id for the same name.",
            "https://docs.weaviate.io/weaviate/concepts/data"),

    SCHEMA("🧱 Data structure (collection schema)",
            "The fields every passage has and how Weaviate indexes each one.",
            "title and body are split into words for BM25 (title counts twice) and are what the vector is made from; "
                    + "source and chunkNo are only for filtering.",
            List.of("🔤 BM25", "🧭 Vector", "🧾 Record"),
            "Two kinds of index: an inverted index (word → passages) for BM25, and a vector index (here HFresh, which "
                    + "Weaviate Cloud requires) to find nearest vectors without comparing all of them.",
            "https://docs.weaviate.io/weaviate/concepts/vector-index"),

    EMBEDDING("🧮 Vector (embedding)",
            "The list of numbers Weaviate made for this passage — 1024 of them.",
            "Made by Weaviate's built-in vectorizer from title + body. The question gets a vector the same way; the "
                    + "closest passage vectors are the vector-search hits.",
            List.of("🧭 Vector search", "🧱 Data structure"),
            "Each number is one learned dimension of meaning; no single number means anything alone. Similar texts "
                    + "point in similar directions.",
            "https://en.wikipedia.org/wiki/Word_embedding"),

    KEYS("🔑 Keys",
            "Only two services need a key: Weaviate and OpenRouter. Laya (System 1) is public.",
            "A saved key goes into the app's local H2 database and is read on every call; the page only ever shows its "
                    + "last 4 characters. Locally you can also set defaults with environment variables.",
            List.of("🗄️ Weaviate", "🧠 System 2", "🚦 Service status"),
            "Never put a key in code or in git. On Render the disk is not persistent, so keys must be entered again "
                    + "after a restart.",
            null),

    MODELS("🧩 System 2 models",
            "The OpenRouter models System 2 may use, tried top to bottom.",
            "Pick from the dropdown — 🆓 free models first, 💲 paid below (a key with a $0 limit can only use free ones). "
                    + "↑ ↓ set the fallback order; a busy model hands over to the next.",
            List.of("🧠 System 2", "🔬 Trace"),
            "Context = how many tokens a model can read at once. \"tools\" = it can call functions. Price is per "
                    + "million tokens in (prompt) and out (answer).",
            "https://openrouter.ai/models");

    public final String title;
    public final String what;
    public final String does;
    public final List<String> links;
    public final String theory;
    public final String learnMore;

    Hint(String title, String what, String does, List<String> links, String theory, String learnMore) {
        this.title = title;
        this.what = what;
        this.does = does;
        this.links = links;
        this.theory = theory;
        this.learnMore = learnMore;
    }
}
