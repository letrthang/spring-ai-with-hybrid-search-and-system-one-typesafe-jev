# Architecture

> **For:** developers new to RAG. **Goal:** after reading this page you can follow one question from the chat box to the
> answer, and you know which class does each step.

## 1. What the app does

A one-page Spring Boot + Vaadin web app. You type a question in **💬 Chat**. The app looks up passages in a
**Weaviate** database in two ways (keywords and meaning), asks a small fast model (**System 1**) which passages are
really useful, and asks a big model (**System 2**) to write the answer from those passages only. The 🔀 **Data flow**
dialog then shows every step that happened, with timings. **🔎 Search Data** lets you look inside Weaviate, and
**🔑 Key Management** is where you enter the two API keys the demo needs.

## 2. The big picture

```mermaid
flowchart LR
    B[🖥️ Browser<br/>Vaadin UI] <--> APP[☕ Spring Boot app]
    APP <--> H2[(💾 H2 file DB<br/>keys + chat history)]
    APP <-->|BM25 + nearText| W[(🗄️ Weaviate Cloud<br/>collection DemoDoc)]
    APP <-->|POST /demo/systemone| S1[⚡ System 1<br/>Laya on Hugging Face]
    APP <-->|OpenAI-compatible chat| S2[🧠 System 2<br/>OpenRouter free model]
```

| Box | What it is | Key needed? |
|---|---|---|
| 🖥️ Browser | the Vaadin page: a left drawer with 💬 Chat, 🔎 Search Data, 🔑 Key Management | — |
| ☕ Spring Boot app | all the logic; runs on Render (Docker) or `localhost:8080` | — |
| 💾 H2 file DB | `./data/ragdemo` — saved keys and chat history | — |
| 🗄️ Weaviate Cloud | the passages, with a keyword index (BM25) and vectors | ✅ URL + Admin key |
| ⚡ System 1 | **Laya**, a *decision* model: answers yes/no questions with a probability | ❌ public route |
| 🧠 System 2 | an OpenRouter **free** chat model: writes text | ✅ API key |

## 3. One question, step by step

```mermaid
sequenceDiagram
    actor U as 👤 User
    participant A as ☕ App (RagService)
    participant W as 🗄️ Weaviate
    participant S1 as ⚡ System 1 (Laya)
    participant S2 as 🧠 System 2 (OpenRouter)

    U->>A: 1. question
    Note over A: 0. all three services 🟢? else chat is disabled
    par 2. retrieve, two ways
        A->>W: BM25 (keywords)
        W-->>A: top 20 by keyword score
    and
        A->>W: nearText (meaning)
        W-->>A: top 20 by vector distance
    end
    Note over A: 3. fuse the two lists with RRF → top 4
    loop 4. for each of the 4 passages
        A->>S1: relevant? answers? injection?
        S1-->>A: three probabilities
    end
    Note over A: keep the good ones, sort by "answers"
    A->>S2: 5. question + kept passages
    S2-->>A: 6. answer with [1] [2] citations
    A-->>U: answer + 📚 Sources + 🔬 Trace + 🔀 Data flow
```

1. **Ask.** The user types a question and presses **Ask**. The chat box is disabled while the app works.
2. **Retrieve — two ways at once.**
   - **BM25** finds passages that share the *words* of the question (good for exact names and codes such as `PCR` or `Cas9`).
   - **nearText** finds passages with the same *meaning*, even with other words ("warm water holds less oxygen" ≈ "gas solubility falls as temperature rises").
     Weaviate turns the text into a vector itself (its built-in vectorizer), so we need no embedding model.
3. **Fuse (RRF).** The two lists are merged with *Reciprocal Rank Fusion*: each passage gets
   `1 / (60 + rank)` from every list it appears in, and the scores are added. A passage high in **both** lists wins.
   The best **4** go on.
4. **System 1 — filter and re-rank.** For each passage, Laya answers three typed questions, each as a probability:

   | Question | Type | Keep the passage if |
   |---|---|---|
   | `relevant` — is this passage about the question? | yes/no | ≥ 0.45 |
   | `answers` — can it answer the question? | yes/no | ≥ 0.55 |
   | `injection` — does it try to give the reader instructions? | yes/no | < 0.70 |

   The kept passages are sorted by `answers` and the best **3** go on. The four Laya calls run in parallel
   (about 3–5 s in total). This is the same job as `JevDocumentFilter` and `JevDocumentReranker`
   in the [Spring blog post](https://spring.io/blog/2026/10/02/spring-ai-modular-rag-typesafe-jev), done by Laya.
5. **Augment.** Spring AI's `ContextualQueryAugmenter` puts the kept passages, numbered `[1]`, `[2]`…, into the prompt
   with the instruction "answer only from this context".
6. **Generate.** System 2 writes the answer. If the first free model fails, the next one in the list is tried.
7. **Show and save.** The answer, its sources, the trace and the data flow are shown and saved to the chat history.

### What the user sees under each answer

| Section | Shows |
|---|---|
| 📚 **Sources — n used of m** | every passage found: BM25 rank, vector rank, RRF score, System 1's three scores, kept or dropped |
| 🔬 **Trace** | the time of each step, which model answered, tokens used |
| 🔀 **Data flow** | a dialog with the sequence diagram above, drawn from what really happened, with ms on each arrow |

### Self-learning: ♻️ reuse first, 🧠 learn last

```
question ─► ♻️ closest learned answer (kind = learned, same language, nearText on the question)
             │ (same question, or distance < 0.45) and ⚡ "does this stored answer answer THIS question?" ≥ 0.70
             ├── yes ─► answer + its original sources, marked ♻️ — System 2 not called
             └── no ──► the normal pipeline ─► answer ─► ⚡ verify ─► 🧠 save (kind = learned)
```

An answer is **learned** only if it cites a passage [n], ⚡ says it answers the question (≥ 0.70), a cited passage
supports it (≥ 0.55) and it gives no orders to an AI (injection < 0.30) — `AnswerVerifier`. Learned answers live in
`DemoDoc` too — the free Weaviate Cloud plan allows **one collection** — as objects with `kind = learned` (title = body
= the question, plus `answer`, `model`, `sources`, scores). Every passage search, list and count filters
`kind = passage`, so generated text never becomes a "fact"; 📥 Load sample data rebuilds the collection and clears them.
If System 1 is busy, the answer is simply not learned (it is still shown).

### 🛠 Agentic mode: System 2 picks its tools

```
question ─► 🧠 System 2 (prompt + 3 tool descriptions)
              │  replies with tool calls, e.g. search_passages("PCR qPCR difference")
              ▼
           ☕ App runs each call ── search_passages ──► 🗄️ Weaviate ─► ⚡ System 1 keeps / drops ─► kept passages [n]
              │                 ── find_learned_answer ─► 🗄️ kind = learned ─► ⚡ fits? ─► answer + its passages [n]
              │                 ── recent_questions ──► 💾 H2 (SELECT, read-only)
              ▼
           🧠 System 2 (conversation + tool results) … repeat — at most 6 tool calls; the last round has no tools
              ▼
           answer citing [n] ─► ⚡ verify ─► 🧠 learn          (same as the pipeline)
```

`RagAgent` runs this loop itself: `SystemTwoClient.step` sends the conversation and the `ToolCallback`s to the
**`ChatModel`** (not `ChatClient`, whose tool-calling advisor would run the tools for us), the model answers with
`AssistantMessage.ToolCall`s, the app calls each `ToolCallback`, adds a `ToolResponseMessage` and asks again. That is
how every call is timed, drawn in 🔀 Data flow, checked by System 1 and limited. Passages keep one number [n] for the
whole question. A required service that fails before any passage was checked stops the question (fail closed); later,
the tool tells the agent "not available, answer from what you have" — unchecked text never reaches the model.

## 4. System 1 vs System 2

| | ⚡ System 1 (Laya) | 🧠 System 2 (OpenRouter LLM) |
|---|---|---|
| Thinks like | a reflex: fast, narrow | slow, careful, general |
| Input → output | a passage + a typed question → **a probability** | a prompt → **free text** |
| Speed | ~1 s per call | several seconds |
| Cost | free, small model | the expensive part (free models here, but rate-limited) |
| Job in this app | decide which passages are worth reading | write the answer |

**Why combine them?** Search always returns some junk. Sending junk to the big model costs tokens and invites wrong
answers or prompt injection. A small decision model removes it cheaply first — "you only pay for the big LLM when the
step needs one".

## 5. Vector vs BM25 vs hybrid

Example collection: *(A)* "PCR makes millions of copies of one short piece of DNA", *(B)* "Warm liquids
hold less dissolved gas than cold ones (Henry's law)".

| Question | BM25 (words) | Vector (meaning) | Hybrid (RRF) |
|---|---|---|---|
| "what does PCR do" | ✅ (A) — exact word "PCR" | 🟠 a general "DNA" passage may rank close | ✅ (A) — top of BM25 |
| "why does a warm pond run short of oxygen" | ❌ few shared words | ✅ (B) — same meaning | ✅ (B) — top of vector |

Each method misses something; the fused list gets both right. That is why the demo runs both and shows both ranks in
📚 Sources and in 🔎 Search Data.

## 6. Code map

```
src/main/java/com/example/ragdemo/
├── RagDemoApplication.java        Spring Boot entry point; Aura theme, push, light/dark
├── keys/                          🔑 the two keys and the endpoints, in the H2 file DB
│   ├── ApiSetting.java (+ repository)  one row: name → value
│   └── KeyStore.java              typed getters; defaults from application.yml / env vars;
│                                  read on EVERY call (no cached keys)
├── weaviate/                      🗄️ retrieval
│   ├── WeaviateConnector.java     the only place that opens a WeaviateClient (rebuilt when the key changes)
│   ├── DemoDocCollection.java     the collection DemoDoc (title, body, source, chunkNo) + built-in vectorizer, HFresh
│   ├── SampleDataLoader.java      resources/sample-data/*.md → one passage per "## section" → Weaviate
│   ├── HybridRetriever.java       Spring AI DocumentRetriever: BM25 + nearText in parallel + RRF
│   ├── RrfFusion.java             1/(60+rank), the whole fusion in one small class
│   ├── WeaviateRecords.java       one stored object by UUID (fields, vector, times) for 🧾 record details
│   ├── WeaviateSchema.java        GET /v1/schema/DemoDoc, read live for the 🧱 Schema dialog
│   ├── Lang.java                  en / vi / ja of a question (by script) → searches keep to that language
│   ├── LearnedAnswers.java        🧠 kind = learned objects: closest question · save (one per question) · list · forget
│   └── WeaviateHealth.java        🟢 N passages / 🟡 empty or loaded by an older version / 🔴 down or bad key
├── system1/                       ⚡ Laya
│   ├── LayaClient.java            POST /demo/systemone via RestClient: yes/no questions about a text → probabilities
│   ├── AnswerVerifier.java        ⚡ checks ANSWERS: before learning (answers · grounded · injection) and before reuse
│   ├── SystemOneFilter.java       Spring AI DocumentPostProcessor: score every passage, keep / drop
│   ├── SystemOneReranker.java     Spring AI DocumentPostProcessor: sort the kept ones by "answers", best 3
│   └── SystemOneHealth.java       GET /health
├── system2/                       🧠 OpenRouter
│   ├── SystemTwoClient.java       Spring AI OpenAiChatModel; one OpenAI client pair per key (stale ones closed first);
│   │                              step(): one 🛠 agent round — conversation + tools → text or tool calls
│   ├── OpenRouterModels.java      the model list (🆓 free first, 💲 paid) with context size and price
│   └── SystemTwoHealth.java       GET /key (OpenRouter) or /models — costs no tokens
├── rag/                           🔗 the pipeline
│   ├── RagService.java            the steps above, top to bottom (+ ♻️ reuse first, 🧠 learn last); logs every step
│   ├── RagAgent.java              🛠 agentic mode: the tool loop and its 3 @Tool methods (search · learned · H2)
│   ├── FlowRecorder.java          collects FlowHop records for 🔀 Data flow
│   ├── RagAnswer.java             text + sources + trace + flow (+ failure, ♻️ reuse, 🧠 learning)
│   ├── Msg.java                   a text as key + arguments, translated only when shown (see i18n below)
│   ├── ServiceStatus.java         🟢 / 🟡 / 🔴 + reason, for the pills
│   ├── SystemDownException.java   a required service failed → stop
│   └── Http.java                  the one JDK HttpClient (one pool) + RestClient on top of it
├── db/                            💾 the app's own database
│   └── DatabaseBrowser.java       tables from INFORMATION_SCHEMA, pages of rows, parameterised UPDATE / DELETE
├── chat/                          💬 history in H2
│   ├── ChatSession.java, ChatMessage.java (+ repositories)
│   └── ChatHistoryService.java    saves each turn; the answer is kept as JSON (sources, trace, flow)
└── ui/                            🖥️ Vaadin
    ├── MainLayout.java            the page: header (title, ocean-wave border, logos, 🌐 language) + left drawer
    ├── ChatView.java              💬 status pills, 🧭 diagram, question, progress steps, answers, 📂 History
    ├── SystemMap.java             server side of <rd-system-map> (src/main/frontend/rd-system-map.ts, Lit)
    ├── AnswerBubble.java          answer + 📚 Sources + 🔬 Trace + 🔀 Data flow button
    ├── DataFlowDiagram.java       the sequence diagram, plain HTML/CSS
    ├── SearchDataView.java        🔎 BM25 / Vector / Hybrid over Weaviate, master-detail with DetailCard
    ├── RecordDialog.java          🧾 one Weaviate record, laid out like the Weaviate console (JSON / fields)
    ├── SchemaDialog.java          🧱 the collection schema, live from /v1/schema
    ├── LearnedCard.java           🧠 the learned answers (lazy grid, 🗑 forget all)
    ├── DatabaseCard.java          💾 H2 tables: read, edit, delete a row
    ├── SplitMasterDetail.java     grid | card with a draggable splitter
    ├── KeyManagementView.java     🔑 the keys (never shown), 🧪 Test, ● online badges (StatusBadge)
    ├── ModelPicker.java           🧩 choose and order the System 2 models
    ├── Hint.java, InfoHint.java   ℹ️ the learning hints (what / does / theory / links) and their buttons
    ├── Tr.java, LanguageSetup.java  🌐 translations; every visitor starts in English
    ├── ErrorHandling.java         every UI error logged + shown; a closed browser is not an error
    └── AppDialog.java, DetailCard.java, StatusPill.java, Background.java (virtual thread → UI.access)

src/main/resources/
├── application.yml                defaults (keys empty → env vars or git-ignored ./application-local.yml), logging
├── sample-data/{en,vi,ja}/*.md    500 passages per language: biotech, physics, RAS / fish farm, demo, office notes
├── vaadin-i18n/translations.properties      🇬🇧 English (default)
├── vaadin-i18n/translations_vi.properties   🇻🇳 Tiếng Việt
├── vaadin-i18n/translations_ja.properties   🇯🇵 日本語
└── META-INF/resources/            styles.css (all rd-* classes) + icons/*.webp
```

**i18n.** Every text on the page comes from `vaadin-i18n/translations*.properties`; the three files have exactly
the same keys (`TranslationsTest` checks it). The backend never builds English sentences: it returns a `Msg`
(key + arguments, e.g. `status.http` + `503`) and the UI turns it into text with `Tr.t(...)` in the page's language.
An argument written `@key` is translated too (e.g. `flow.stopped` + `@service.SYSTEM1`). The 🌐 dropdown in the
header stores the language in the session and reloads the page. Logs stay in English.

## 7. Spring AI — the main classes

Spring AI (`spring-ai-rag` + `spring-ai-openai`, version 2.0.1) gives the app its RAG building blocks. These are the
classes to learn first, in the order a question meets them:

| Class (package `org.springframework.ai…`) | What it is | Where it is used here |
|---|---|---|
| `document.Document` | One piece of text + an id + a `metadata` map (+ an optional score). The unit that moves through RAG. | `HybridRetriever` builds one per passage; `SystemOneFilter` adds the Laya scores and the verdict to its metadata |
| `rag.Query` | The user's question as an object (text + history + context). Every RAG step takes one. | `RagService`: `new Query(question)` |
| `rag.retrieval.search.DocumentRetriever` | Interface: `Query` → `List<Document>`. "Find passages for this question." | `HybridRetriever` — BM25 + nearText in Weaviate, merged with RRF |
| `rag.postretrieval.document.DocumentPostProcessor` | Interface: `(Query, List<Document>)` → `List<Document>`. Filter, rerank or shorten the found passages. | `SystemOneFilter` (⚡ keep / drop), `SystemOneReranker` (⚡ sort by "answers", keep 3) |
| `rag.generation.augmentation.ContextualQueryAugmenter` | Puts the passages into the prompt: `augment(Query, List<Document>)` → a new `Query` whose text is the full prompt. | `RagService` step 5, with our own template and a formatter that numbers the passages `[1]`, `[2]`… |
| `chat.prompt.PromptTemplate` | A text with `{placeholders}` filled at run time. | `RagService.PROMPT` — `{context}` + `{query}`, rules: cite `[n]`, say "I don't know" |
| `chat.model.ChatModel` / `openai.OpenAiChatModel` | The connection to an LLM. `OpenAiChatModel` speaks the OpenAI API, so it works with OpenRouter by changing the base URL. | `SystemTwoClient` builds one **per call** (key read from H2 at that moment) |
| `openai.OpenAiChatOptions` | Settings for one model: base URL, API key, model name, timeout, retries. | `SystemTwoClient`: one model at a time, `maxRetries(0)` so we move on to the next model ourselves |
| `chat.client.ChatClient` | The fluent API on top of a `ChatModel`: `.prompt().system(…).user(…).call()`. | `SystemTwoClient` — sends the augmented prompt |
| `chat.model.ChatResponse` + `chat.metadata.Usage` | The answer text plus metadata: model used, prompt / completion tokens. | `SystemTwoClient` → 🔬 Trace (`tokens in / out`) |
| `tool.annotation.Tool` / `ToolParam`, `support.ToolCallbacks`, `tool.ToolCallback` | A Java method the LLM may call: name, description and JSON parameters are generated from the method; `ToolCallbacks.from(object)` turns its `@Tool` methods into callbacks. | `RagAgent.Tools` — `search_passages`, `find_learned_answer`, `recent_questions` |
| `chat.messages.AssistantMessage.ToolCall`, `ToolResponseMessage` | The model's request to call a tool, and the app's answer to it, added to the conversation. | `RagAgent.run` — the agent loop (🛠 agentic mode) |

How they fit together, as Spring AI sees the chain:

```java
Query query = new Query(question);
List<Document> found = retriever.retrieve(query);                          // DocumentRetriever       🗄️
List<Document> kept  = filter.process(query, found);                       // DocumentPostProcessor   ⚡ keep / drop
List<Document> best  = reranker.process(query, kept);                      // DocumentPostProcessor   ⚡ best 3
Query prompt = ContextualQueryAugmenter.builder()
        .promptTemplate(PROMPT).documentFormatter(RagService::numbered)
        .build().augment(query, best);                                    // QueryAugmenter          ✍️
ChatResponse answer = ChatClient.create(chatModel).prompt()
        .system(SYSTEM_PROMPT).user(prompt.text()).call().chatResponse(); // ChatClient → OpenAiChatModel 🧠
```

`RagService` runs these **one by one, in plain code**, so each step can be timed, logged and drawn in 🔀 Data flow.
For the first two steps it calls the richer twins of the interface methods — `HybridRetriever.search(…)` and
`SystemOneFilter.score(…)` — which do the same work but also return the timings and the dropped passages for
📚 Sources; `retrieve` and `process` stay so the classes plug into any Spring AI RAG setup.
Spring AI's `RetrievalAugmentationAdvisor` wires the very same pieces (`DocumentRetriever` →
`DocumentPostProcessor`s → `QueryAugmenter`) inside one `ChatClient` advisor; once you understand `RagService`, you
understand that advisor.

**Not used, on purpose:** `VectorStore` / `WeaviateVectorStore` (vector search only, no BM25 — so `HybridRetriever`
uses the Weaviate client directly); `QueryTransformer` / `MultiQueryExpander` (rewriting the question — a good next
step, kept out to stay simple); `ChatMemory` (chat history is saved by `ChatHistoryService` in H2 instead);
`ToolCallingAdvisor` (ChatClient's built-in tool loop — `RagAgent` runs the loop itself to time, draw and limit every
call).

## 8. Keys and configuration

| Setting | Where | Secret? |
|---|---|---|
| Weaviate URL + Admin API key | 🔑 Key Management → H2 | key: yes |
| OpenRouter API key | 🔑 Key Management → H2 | yes |
| OpenRouter models (free by default) | 🔑 Key Management → H2 | no |
| System 2 base URL (default OpenRouter), System 1 URL (default public Laya) | 🔑 Key Management → Advanced | no |

- Every API call reads its key from H2 **at the moment of the call**, so a newly saved key works at once.
- Keys are never logged, never shown in full (only `••••abcd`), never put in a trace hop, never committed.
- Order of a key: **saved in 🔑 Key Management** → else **environment variable** (`RAGDEMO_WEAVIATE_URL`,
  `RAGDEMO_WEAVIATE_KEY`, `RAGDEMO_OPENROUTER_KEY`, set on Render under Environment) → else not set. 🗑 Clear deletes
  the saved one, so the environment default applies again.
- On Render the disk is not persistent: a restart, redeploy or wake-up starts with an empty DB → saved keys are gone,
  the environment defaults remain.

### When a service is down

All three services are required. The 💬 Chat page shows a pill for each (🟢 / 🟡 / 🔴), re-checked every 30 s. If any
is not 🟢, a ⚠️ banner says which one and why, and the question box and **Ask** are **disabled**. There is no fallback
model. If a service fails during a question, `SystemDownException` stops the pipeline and the same banner appears.

## 9. Deploy

- One **Docker** web service on **Render** (free plan): `Dockerfile` (Maven + Vaadin production build → JRE 25) and
  `render.yaml`. URL: <https://spring-ai-with-hybrid-search-and-system.onrender.com>.
- Render's GitHub App redeploys on every push to `main`. No Render key is used by the app.
- Free instances sleep after 15 minutes idle; the first visit after that takes about a minute.

## 10. Glossary

| Word | Meaning |
|---|---|
| **RAG** | Retrieval-Augmented Generation: look up passages first, then let the LLM answer from them |
| **Passage / chunk** | a small piece of a document (a few hundred tokens) stored as one Weaviate object |
| **Embedding / vector** | a list of numbers that stands for the meaning of a text; close vectors = similar meaning |
| **BM25** | the classic keyword ranking: rare words that appear often in a passage score high |
| **nearText** | Weaviate's vector search from a text query (it embeds the query for you) |
| **RRF** | Reciprocal Rank Fusion: merge ranked lists by adding `1/(60+rank)` |
| **System 1 / System 2** | fast intuitive vs slow deliberate thinking (Kahneman); here a decision model vs an LLM |
| **Jev / Laya** | System 1 decision models with the same `/v1/systemone` API; this demo uses Laya |
| **Rerank** | re-order the found passages by a better score than the search gave |
| **Augment** | add the passages to the prompt |
