# Changelog

All notable changes to this project. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and
the project uses [Semantic Versioning](https://semver.org/). Branches: `develop` (daily work) → `main` (tested) →
`release` (production, deployed by Render, tagged `vx.y.z`).

## [1.1.2] — 2026-10-17

### Fixed
- **System 2 🔄 Defaults also forgets a typed OpenRouter key** (#133). It reset the models and the base URL but kept a key
  a visitor had typed (in memory only), so the only way back to the site owner's default was a restart — on the live
  site a wrong key would have blocked the chat for everyone. Found by test case TC-03 while running the test cases (#13).

## [1.1.1] — 2026-10-17

### Changed
- **The header version reads "Version v1.1.1"** (EN; "Phiên bản …" in Vietnamese, "バージョン …" in Japanese), still
  linked to the release notes. The longer text moves into the menu drawer below 1100 px (was 900 px), so it never
  touches the centred title — measured in all three languages.

## [1.1.0] — 2026-10-17

### Added
- **The header shows the version the web app runs** — a small `vX.Y.Z` pill on the left, after ☰, linked to that
  version's release notes (in the drawer below 900 px, where the centred title needs the room). It is read from the
  build (`pom.xml` → Spring Boot `build-info`), so the live site always shows the release it runs.

### Changed
- **Deployment follows `develop` → `main` → `release`.** Daily work goes to `develop`; after testing, `develop` →
  `main`; a new release is a PR `main` → `release`, and **Render now deploys the `release` branch** (`render.yaml`
  and the service setting). A new release is made only when the developer asks (`GITHUB_COMMIT_RULE.md`).
- **README says up front that the app supports an AI agentic mode** — tagline, badges, "Two ways to answer" (fixed
  pipeline vs 🛠 agentic mode) and the 5-minute demo.

## [1.0.0] — 2026-10-17

The first release of the RAG demo: Spring AI hybrid search on Weaviate, ⚡ System 1 (Laya) as a typed decision filter,
🧠 System 2 (OpenRouter free models) writing the answer — with every step visible.
Live: <https://spring-ai-with-hybrid-search-and-system.onrender.com>

### 💬 Chat — the RAG pipeline
- Hybrid retrieval in Weaviate: **BM25 + nearText in parallel, fused with RRF** → top 4 passages, in the question's
  language (en / vi / ja) only.
- **⚡ System 1 (Laya)** scores every passage — relevant · answers · injection — keeps or drops it and re-ranks the kept
  ones (Spring AI `DocumentPostProcessor`s).
- **🧠 System 2** writes the answer from the numbered passages with `[n]` citations (Spring AI
  `ContextualQueryAugmenter` + `ChatClient` on `OpenAiChatModel`), trying the free models in order when one is busy.
- Under each answer: **📚 Sources** (every passage with BM25 / vector / RRF / System 1 scores), **🔬 Trace** (timings,
  model, tokens), **🔀 Data flow** (a sequence diagram of what really happened, with ms on each arrow).
- Live progress steps 🗄️ → ⚡ → 🧠 → 🧠 Learn; 📂 History of conversations (H2), lazy, ⛶ full screen.
- **Fail closed**: all three services are required — status pills re-checked every 30 s; the question box is disabled
  and a banner says which service is down and why.
- **🧭 How it works**: a live diagram of the pipeline (Lit web component) with animated connectors, online / offline
  pills and the self-learning and agent branches.

### 🧠 Self-learning
- An answer is **learned** only if ⚡ System 1 verifies it: it cites a passage, answers the question (≥ 0.70), a cited
  passage supports it (≥ 0.55) and it gives no orders to an AI (injection < 0.30).
- The **same or a close question** (distance < 0.45, measured on Weaviate Cloud's vectorizer) **reuses** it in ~1.5 s
  without System 2 — if System 1 says it fits the new question.
- Learned answers live in `DemoDoc` as `kind = learned` (the free Weaviate plan allows one collection), never mixed
  with the passages; one per language + question.
- 🔎 Search Data → 🧠 Learned answers: lazy grid, ⛶ full screen, 🗑 forget all, 🔍 Details (double-click / right click).

### 🛠 Agentic mode
- A switch next to 🔎 Ask: System 2 gets tools — `search_passages` (with ⚡ System 1 inside), `find_learned_answer`,
  `recent_questions` (H2, read-only) — and decides what to look up, as often as it needs (≤ 6 tool calls).
- Built with Spring AI `@Tool` / `ToolCallbacks` / `ToolCallback`; the loop runs in the app against the `ChatModel`, so
  every round and tool call is timed, drawn in 🔀 Data flow (💾 H2 lifeline when used), checked and limited.
- Models without tool support are skipped; a busy System 1 after the first search no longer stops the answer.

### 🔎 Search Data
- BM25 / vector / hybrid search over Weaviate; empty query lists all passages lazily; resizable grid + detail card.
- 🔍 Record dialog laid out like the Weaviate console (JSON / fields, vector, where the record lives); 🧱 schema read
  live from `GET /v1/schema/DemoDoc`.
- 📥 **Load sample data**: **3,000 passages — 1,000 each in English, Vietnamese and Japanese** (110 topics: basic
  biotech, physics, RAS fish farming incl. AI monitoring, demo and office notes); drops the old collection first.
- 💾 **App database (H2)**: read and edit what the app saved (parameterised UPDATE / DELETE, keys locked), ⛶ full screen.

### 🔑 Keys and security
- Visitors can use **their own keys**; otherwise the site owner's defaults from environment variables (`RAGDEMO_*`).
- Keys typed in 🔑 Key Management are **kept in memory only** — never written to H2, never logged, never shown (not
  even in part), never in a trace. Only the System 2 model list is stored.
- 🧪 Test buttons and ● online / starting / offline badges per service; 🧩 model picker for System 2.
- Planted prompt injection in the sample data is dropped by System 1 and never reaches System 2.

### 🌐 Languages and learning
- The whole page in **English, Tiếng Việt, 日本語**, including every ℹ️ lesson (what · does · links · theory · learn more).
- ℹ️ hints on every part of the UI, chips and generated data.

### 🧰 Reliability
- Every exception handled and shown; a closed browser is not an error; graceful shutdown on redeploy.
- One HTTP client pool; System 1 via Spring `RestClient` on virtual threads; OpenAI clients reused per key and closed
  when the key changes; Weaviate client closed only for a broken connection, a stale one retried once; HikariCP.
- Memory budget for Render's 512 MB free plan.

### 📚 Docs and tests
- `README.md`, `architecture.md` (incl. *which Spring AI classes handle each feature*), `docs/testing/test_cases.md`
  (39 cases with results).
- 85 automated tests, incl. a real Weaviate in Docker (`WeaviateIT`, 3,000 passages), a real H2, fake HTTP servers for
  Laya and OpenRouter, and a scripted model for the agent.

[1.1.2]: https://github.com/letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev/releases/tag/v1.1.2
[1.1.1]: https://github.com/letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev/releases/tag/v1.1.1
[1.1.0]: https://github.com/letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev/releases/tag/v1.1.0
[1.0.0]: https://github.com/letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev/releases/tag/v1.0.0
