# Test cases — RAG demo

How to read this file: every case has **steps**, the **expected** result, and the **actual** result of each run with
its date and where it ran. ✅ passed · 🟠 partly / with a stand-in · ❌ failed · ⬜ not run yet.

**Environments**
- **Local-Docker** — the app in Docker + Weaviate 1.40.0 in Docker with a small local vectorizer (model2vec
  potion-8M, standing in for Weaviate Cloud's embeddings) + the **real public Laya** route. System 2 is a local
  OpenAI-compatible stand-in when no OpenRouter key is available.
- **Render** — <https://spring-ai-with-hybrid-search-and-system.onrender.com>, Weaviate Cloud, OpenRouter free models.

## Step 0 — start

1. Local: `./mvnw spring-boot:run` → <http://localhost:8080>. Render: open the URL (wait ~1 min if it was asleep).
2. 🔑 Key Management: enter the Weaviate URL + **Admin** key and the OpenRouter key → 💾 Save on each card.
3. 🔎 Search Data → 📥 Load sample data.

## Cases

| ID | Case | Steps | Expected |
|---|---|---|---|
| TC-01 | No keys → chat disabled | Fresh start, open 💬 Chat | Pills: 🔴 Weaviate "no URL or API key", 🟢 System 1 "Laya is ready", 🔴 System 2 "no API key"; ⚠️ banner with 🔑 link; question box + Ask disabled |
| TC-02 | Keys are masked and kept | Save a key; reload the page; restart the app | Field empty, placeholder `••••abcd`; after a **local** restart the key is still there; on **Render** a restart forgets it |
| TC-03 | Wrong key | Save a wrong OpenRouter key / Weaviate key | 🔴 "invalid API key — check 🔑 Key Management" |
| TC-04 | Load sample data | 🔎 Search Data with an empty collection → 📥 Load | 🟡 "collection is empty" → "22 passages loaded" → 🟢 "22 passages"; loading again does not duplicate |
| TC-05 | Hybrid search, keyword question | Search "reset AER-01", mode Hybrid | Aerator manual passages first, with BM25 rank, vector rank and RRF score |
| TC-06 | Compare modes, meaning question | Search "my fish seem to be suffocating" in BM25, Vector, Hybrid | BM25 has few useful hits; Vector puts "Signs of low oxygen" high; Hybrid merges both |
| TC-07 | Ask — keyword question | 💬 "How do I reset aerator AER-01 after error E-17?" | Answer cites [n]; the "Reset after a trip (error E-17)" passage is ✅ kept and cited |
| TC-08 | System 1 drops junk | Same question; open 📚 Sources | Off-topic passages 🚫 "dropped: not relevant"; if the injected email note is found it is 🚫 "dropped: prompt injection" and never sent to System 2 |
| TC-09 | Nothing kept | 💬 "What is the capital of France?" | "System 1 found no passage that can answer this question…"; 🔬 Trace "System 2 — not called" |
| TC-10 | Data flow | Open 🔀 Data flow under an answer | Dialog (⛶ ✕): question → BM25 → nearText → lists → RRF → one call + verdict per passage → rerank → augment → System 2 → answer, with timings |
| TC-11 | History | Ask twice; ＋ New chat; double-click the first row in 📂 History | Grid shows both conversations with counts; dialog shows the saved answer with Sources / Trace / Data flow |
| TC-12 | A service fails during a question | Stop System 2 (wrong key) and ask | Answer bubble ⚠️ "🧠 System 2 is not available…", pill turns 🔴, chat disabled; saved as FAILED in history |
| TC-13 | Phone + dark | 390 px wide, dark mode, all 3 menus | No sideways scroll; cards stack; detail cards open as overlays; text readable |

## Results

| ID | 2026-10-16 · Local-Docker | Render |
|---|---|---|
| TC-01 | ✅ banner + disabled Ask exactly as expected | ✅ same on the Render URL (no keys entered) |
| TC-02 | ✅ `••••9999` after reload and after a jar restart; never in the log | ⬜ needs real keys |
| TC-03 | ✅ OpenRouter fake key → 🔴 invalid (real 401 from OpenRouter); Weaviate wrong key → invalid (WeaviateIT) | ⬜ |
| TC-04 | ✅ 🟡 empty → "22 passages loaded" → 🟢; second load = 22 | ⬜ needs Weaviate Cloud key |
| TC-05 | ✅ #1 "Aerator AER-01 to AER-06" (BM25 #1, vector #1), #2 "Reset after a trip (error E-17)" (BM25 #2, vector #2) | ⬜ |
| TC-06 | 🟠 BM25 and vector gave the **same** top 3 ("Daily ration", "Ammonia", "Signs of low oxygen") — confirmed directly in Weaviate: the local stand-in vectorizer is a small static word-embedding model that ranks almost like keywords. Re-check on Weaviate Cloud | ⬜ |
| TC-07 | 🟠 pipeline ✅ with the System 2 stand-in: answer cited [1] "Reset after a trip (error E-17)" | ⬜ needs OpenRouter key |
| TC-08 | ✅ real Laya: canteen menu dropped "not relevant" (0.12 / 0.20); injected note dropped "prompt injection" (injection 0.99); E-17 passage kept (0.93 / 0.94) | ⬜ |
| TC-09 | ✅ covered by `RagServiceTest.nothingKept_systemTwoIsNotCalled` | ⬜ |
| TC-10 | ✅ every hop drawn with timings (4 Laya calls of 2.1–5.7 s, run in parallel) | ⬜ |
| TC-11 | ✅ "Conversations: 1", double-click dialog shows the saved answer + sections | ⬜ |
| TC-12 | ✅ covered by `RagServiceTest` (Weaviate down / System 1 down) and `ChatHistoryServiceTest` (FAILED message) | ⬜ |
| TC-13 | ✅ all three menus at 390 px dark, `scrollWidth = clientWidth = 390`; console 0 errors | ⬜ |

**Automated tests** (`./mvnw test`, 2026-10-16): 32 tests, 0 failures — including `WeaviateIT` (real Weaviate in
Docker).

**Summary:** Local-Docker 11 ✅ · 2 🟠 · 0 ❌ — Render 1 ✅ · 12 ⬜ (waiting for the Weaviate and OpenRouter keys).
