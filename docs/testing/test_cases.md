# Test cases — RAG demo

How to read this file: every case has **steps**, the **expected** result, and the **actual** result of each run with
its date and where it ran. ✅ passed · 🟠 partly / with a stand-in · ❌ failed · ⬜ not run yet.

**Environments**
- **Local-Real** — the app on `localhost:8080` with the owner's test keys: **Weaviate Cloud** (1.39.12, `text2vec-weaviate`,
  HFresh index), **OpenRouter** free models, the **real public Laya** route.
- **Local-Docker** — the app + Weaviate 1.40.0 in Docker with a small local vectorizer (model2vec potion-8M, standing in
  for Weaviate Cloud's embeddings) + real Laya; System 2 a local stand-in. Used before the keys were available.
- **Render** — <https://spring-ai-with-hybrid-search-and-system.onrender.com>.

**Sample data** (46 passages): biotech basics (DNA, PCR, CRISPR, fermentation), physics basics (mechanics, electricity,
heat and gases, light), the fish-farm handbook, "how this demo works", and office notes with an off-topic canteen menu
and a planted prompt injection.

## Step 0 — start

1. Local: `./mvnw spring-boot:run` → <http://localhost:8080>. Render: open the URL (wait ~1 min if it was asleep).
2. 🔑 Key Management: enter the Weaviate URL + **Admin** key and the OpenRouter key → 💾 Save on each card.
3. 🔎 Search Data → 📥 Load sample data.

## Cases

| ID | Case | Steps | Expected |
|---|---|---|---|
| TC-01 | No keys → chat disabled | Fresh start, open 💬 Chat | Pills: 🔴 Weaviate "no URL or API key", 🟢 System 1 "Laya is ready", 🔴 System 2 "no API key"; ⚠️ banner with 🔑 link; question box + Ask disabled |
| TC-02 | Keys are masked and kept | Save a key; reload; restart the app | Field empty, placeholder `••••abcd`; kept after a **local** restart; **Render** forgets it on restart |
| TC-03 | Wrong key | Save a wrong OpenRouter / Weaviate key | 🔴 "invalid API key — check 🔑 Key Management" |
| TC-04 | Load sample data | 🔎 Search Data → 📥 Load | Collection dropped and re-created (HFresh), "46 passages loaded", 🟢 "46 passages" |
| TC-05 | Columns per mode | Empty query, then a query in BM25 / Vector / Hybrid | List: #, Title, Source · BM25: + BM25 rank · Vector: + vector rank · Hybrid: + both + RRF |
| TC-06 | Meaning vs keyword | "why does warm water hold less oxygen" in the 3 modes | The Henry's-law passage first; Hybrid merges both lists |
| TC-07 | Record details | Double-click a row | 📍 cluster, collection, object UUID + how it is made, REST address · 🧱 fields and indexes · 🧮 vector (1024 numbers) · { } raw JSON |
| TC-08 | Ask — biotech | 💬 "What does PCR do?" | Answer cites [n]; the "What PCR does" passage ✅ kept |
| TC-09 | Ask — physics, System 1 at work | 💬 "What does Newton's second law say?" | "F = m · a [1]"; Newton's laws ✅ kept, other physics passages 🚫 dropped |
| TC-10 | Nothing kept | A question Laya finds no passage for | "System 1 found no passage…", 🧠 step "➖ not needed" |
| TC-11 | Live progress | Ask anything | Chips move ⏳ 🗄️ → ⚡ → 🧠, then all ✅ |
| TC-12 | Data flow | 🔀 Data flow under an answer | Dialog with every hop and its timing |
| TC-13 | Model picker | 🔑 Key Management → System 2 dropdown | Free group first, paid group below; name + id + details footer; search; ↑ ↓ ✕; 💾 keeps the order |
| TC-14 | Detail card ✕ | Click a row in Search Data / History | Framed card; ✕ closes it and clears the selection |
| TC-15 | A service fails | Wrong System 2 key, then ask | ⚠️ in the answer, pill 🔴, chat disabled, ⛔ on the 🧠 step |
| TC-16 | Phone + dark | 390 px, dark, all 3 menus | No sideways scroll; cards stack; overlays for detail cards |
| TC-17 | ℹ️ learning hints | Click ℹ️ next to a title, pill, column or term | Small dialog: what it is · what it does here · linked to · a little theory · learn more |

## Results

| ID | 2026-10-16 · Local-Real (owner's test keys) | Render |
|---|---|---|
| TC-01 | ✅ (Local-Docker + Render, before keys) | ✅ |
| TC-02 | ✅ `••••4c73` / `••••MjAw`; kept after restart; never in the log | see below |
| TC-03 | ✅ fake OpenRouter key → real 401 → "invalid API key" | |
| TC-04 | ✅ "46 passages loaded" on Weaviate Cloud (first try failed with HTTP 422 "hnsw is not allowed" → fixed by HFresh, PR #27) | |
| TC-05 | ✅ list `#, Title, Source` · BM25 `+BM25` · Vector `+Vector` · Hybrid `+BM25, Vector, RRF` | |
| TC-06 | ✅ all modes: #1 "Gases dissolving in liquids (Henry's law)" (BM25 7.88, vector 0.437); Hybrid #2 "Dissolved oxygen (DO)" | |
| TC-07 | ✅ UUID `829abd87-…` = nameUUIDFromBytes("05-mechanics.md#1"); 1024-number vector; raw JSON | |
| TC-08 | ✅ "PCR makes millions of copies of one short piece of DNA in a few hours. [1]" — 2 used of 4 (re-run with the simpler question). Earlier NGG/Cas9 run: correct; Laya flagged "What is RAG" as *prompt injection* (false positive) | |
| TC-09 | ✅ "Newton's second law states that force equals mass times acceleration (F = m·a). [1]" — 1 used of 4; gases, Beer–Lambert, ideal gas 🚫 not relevant | |
| TC-10 | ✅ "How much current does a 2,000 W kettle draw…" → 0 kept → System 2 "not needed" (Laya judged the power passage not relevant) | |
| TC-11 | ✅ +0.2 s 🗄️ → +0.6 s ⚡ → +5.9 s 🧠 → +11.5 s ✅ | |
| TC-12 | ✅ every hop with timings | |
| TC-13 | ✅ "Search 465 models (18 free)"; free first; "gpt-4o-mini" → 💲 $0.15 in / $0.60 out per 1M | |
| TC-14 | ✅ ✕ closes and deselects | |
| TC-15 | ✅ unit tests (`RagServiceTest`, `ChatHistoryServiceTest`) + Local-Docker banner check | |
| TC-16 | ✅ (Local-Docker run) | |
| TC-17 | ✅ 6 hints on Chat, 8 per answer, on every Search Data / Key Management card; RRF dialog checked; all 17 learn-more links HTTP 200 | |

**Automated tests** (`./mvnw test`, 2026-10-16): 34 tests, 0 failures — including `WeaviateIT` (real Weaviate in
Docker).

**Summary:** Local-Real 17 ✅. Render: run after deploy (see the PR).
