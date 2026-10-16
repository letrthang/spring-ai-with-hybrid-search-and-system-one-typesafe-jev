# Test cases — RAG demo

How to read this file: every case has numbered **steps** (where to click, what to type) and the **expected** result.
The **Results** table at the end records each run with its date and where it ran.
✅ passed · 🟠 partly / with a stand-in · ❌ failed · ⬜ not run yet.

**Environments**
- **Local-Real** — the app on `localhost:8080` with the owner's test keys: **Weaviate Cloud** (1.39.12, `text2vec-weaviate`,
  HFresh index), **OpenRouter** free models, the **real public Laya** route.
- **Local-Docker** — the app + Weaviate 1.40.0 in Docker with a small local vectorizer (model2vec potion-8M, standing in
  for Weaviate Cloud's embeddings) + real Laya; System 2 a local stand-in. Used before the keys were available.
- **Render** — <https://spring-ai-with-hybrid-search-and-system.onrender.com> (default keys from the service's
  environment variables `RAGDEMO_WEAVIATE_URL`, `RAGDEMO_WEAVIATE_KEY`, `RAGDEMO_OPENROUTER_KEY`).

**Sample data** (1,500 passages = 500 × en / vi / ja, `sample-data/<lang>/`): biotech basics, physics basics,
recirculating aquaculture (RAS) and the fish-farm handbook, "how this demo works", and office notes with an off-topic
canteen menu and a planted prompt injection. Searches keep to the question's language.

## Step 0 — start

1. **Local:** `./mvnw spring-boot:run` (or `java -jar target/ragdemo-1.0-SNAPSHOT.jar`) → open <http://localhost:8080>.
   Default keys can come from environment variables or a git-ignored `application-local.yml` (see README).
   **Render:** open the URL; the first visit after 15 idle minutes takes about a minute.
2. If the pills on 💬 Chat are not all 🟢: open 🔑 **Key Management**, enter the Weaviate **cluster URL** + **Admin** key
   and the OpenRouter key, press 💾 **Save** on each card.
3. Open 🔎 **Search Data** → press 📥 **Load sample data** once (≈ 1–2 min for 1,500 passages).

---

## A. 🔑 Key Management

### TC-01 · No keys → chat disabled (fail closed)
1. Start the app with **no** keys (no environment variables, empty database).
2. Open 💬 Chat.

**Expected:** pills 🔴 Weaviate "no URL or API key", 🟢 System 1 "Laya is ready", 🔴 System 2 "no API key"; a ⚠️ banner
"Chat is disabled until every service is 🟢" with a 🔑 link; the question box, 🔎 Ask and the example chips are disabled;
🧭 diagram: Weaviate and System 2 ● offline, their incoming lines red and still.

### TC-02 · Keys are masked and kept
1. 🔑 Key Management → type an OpenRouter key → 💾 Save.
2. Reload the page. 3. Restart the app (local), then reload.

**Expected:** the field is empty with placeholder `••••abcd` (last 4 only) and helper "Saved. Leave empty to keep it.";
the key is never written to the log; it survives a **local** restart; on **Render** a restart forgets saved keys
(the environment default applies again — see TC-03).

### TC-03 · Environment keys are defaults; a saved key wins
1. Run with `RAGDEMO_OPENROUTER_KEY` set and nothing saved → 🔑 Key Management.
2. Type another (test) key → 💾 Save → 🧪 Test.
3. Press 🔄 **Defaults**.

**Expected:** step 1 helper "From the app configuration…", masked `••••` of the env key; step 2 the typed key is used
(status updates; the card badge follows); step 3 the saved key is deleted and the env key is used again.
(Automated: `KeyOverrideTest`.)

### TC-04 · Wrong key
1. 🔑 Key Management → OpenRouter key `sk-or-v1-wrong` → 💾 Save.

**Expected:** System 2 status 🔴 "invalid API key — check 🔑 Key Management"; card badge ● offline; Chat disabled.

### TC-05 · Card status badges
1. Open 🔑 Key Management and wait for the checks.
2. Hover each badge. 3. Press 🧪 Test on one card.

**Expected:** each card shows **● online / ● starting / ● offline** at the top right (AA-contrast colours; the online
dot pulses); the tooltip is the full sentence (e.g. "🟢 ⚡ System 1: Laya is ready"); during a test the badge shows
"checking…" and then the new state, together with the status pill at the bottom.

### TC-06 · "Use your own keys" hint
1. Click the ℹ️ inside the Weaviate URL, the Weaviate key and the OpenRouter key fields.

**Expected:** the lesson "🔁 Use your own keys": defaults from the owner; type yours + 💾 to use it instead;
🗑 Clear / 🔄 Defaults to go back; settings are shared by everyone (use a test key).

### TC-07 · Model picker
1. System 2 card → open "➕ Add a model".
2. Type `gemma` in the search box. 3. Add one paid model, move it with ↑ / ↓, remove one with ✕. 4. 💾 Save, reload.

**Expected:** 🆓 free models first, 💲 paid below; each item shows name, id and a details footer (price, context,
🛠 tools); the default order starts with `nvidia/nemotron-3-ultra-550b-a55b:free`; after reload the saved order is kept.

---

## B. 🔎 Search Data

### TC-08 · Load sample data replaces everything
1. 🔎 Search Data → 📥 Load sample data.
2. (Optional) add a record by hand in the Weaviate console first, then load again.

**Expected:** "⏳ deleting the old collection and loading the sample data…", then "📥 Old data removed, 1500 passages
loaded"; pill 🟢 "1500 passages"; a hand-added record is gone. (Automated: `WeaviateIT` stray-record check.)

### TC-09 · Lazy list of all passages
1. Leave the query empty → 🔎 Search.
2. Scroll the grid to the bottom.

**Expected:** "1500 passages (no query)"; rows load page by page while scrolling; # numbers continue (1…1500);
columns #, Title, Source only.

### TC-10 · Columns per search mode
1. Type `Newton's second law`. 2. Choose **BM25** → 🔎. 3. **Vector** → 🔎. 4. **Hybrid** → 🔎.

**Expected:** BM25 adds "BM25 rank · score"; Vector adds "vector rank · distance"; Hybrid shows both + "RRF score";
the info line ends with 🌐 only "en" passages.

### TC-11 · Meaning vs keyword
1. Query `why does warm water hold less oxygen` in each mode.

**Expected:** the Henry's-law / warm-water passage ranks first in Vector and Hybrid; Hybrid merges both lists.

### TC-12 · Search keeps to the question's language
1. Query `PCR dùng để làm gì?` (Hybrid). 2. Query `PCR は何をするもの？`.

**Expected:** only `vi/…` passages, then only `ja/…` passages; the info line says 🌐 "vi" / "ja".

### TC-13 · Resizable grid + detail card with schema
1. Click a row. 2. Drag the splitter between grid and card. 3. Click another row. 4. Press ✕ on the card.

**Expected:** a framed card with the passage, rank chips and **🧱 Data structure** (title, body, source, chunkNo, lang,
vector — type, index and this record's value); the splitter resizes the grid and the width is kept for the next card;
✕ closes the card and clears the selection; with no card the grid has the full width.

### TC-14 · Record dialog (Weaviate console look)
1. Double-click a row (or right-click → 🔍 Details…).
2. Look at the JSON view, then press **Show fields**. 3. Press 📋 next to the ID and **Copy** next to the vector.

**Expected:** the dialog opens at its final size (no flicker); `ID uuid 📋` + JSON (uuid, metadata, properties,
vectors.default with every number); fields view: `metadata:` (folded), `properties:` chips, `vectors:` →
`default (1024d)` + `[a, b, c, … , x, y, z]`; Copy puts the full vector on the clipboard ("Copied ✓"); below:
📍 where the record lives and 🧱 data structure.

### TC-15 · 🧱 Schema button
1. Press 🧱 **Schema** in the toolbar. 2. Open "{ } Raw JSON".

**Expected:** "📡 GET /v1/schema/DemoDoc" + the full address; collection `DemoDoc`, vectorizer `text2vec-weaviate`,
index `hfresh`; 5 properties with type, tokenization, BM25 ✅ (title, body) and filter ✅ (all, incl. `lang`);
the raw JSON exactly as Weaviate answered.

### TC-16 · 💾 App database — read
1. Scroll below the grid to **💾 App database (H2)**.
2. Pick `CHAT_MESSAGE`, then `CHAT_SESSION`, then `API_SETTING`. 3. Press 🔄 Refresh after asking a question in Chat.

**Expected:** each table with its row count; the SQL shown (`SELECT * FROM "…" ORDER BY 1 DESC LIMIT 50 OFFSET 0`);
newest rows first; in `API_SETTING` key values show as `••••abcd`; after a chat the counts grow.

### TC-17 · 💾 App database — edit and delete
1. `API_SETTING` → double-click a non-key row (e.g. `system2.models`) → change the value → watch the SQL preview →
   💾 Save changes.
2. Double-click a key row (`system2.key`).
3. Double-click a row → 🗑 Delete row → confirm.

**Expected:** step 1 preview `UPDATE "API_SETTING" SET "SETTING_VALUE" = ? WHERE "NAME" = ?`, "💾 Saved (1 row)",
the grid refreshes and the app uses the new value; step 2 `NAME` and the masked value are read-only with an
explanation; step 3 the confirmation shows the `DELETE`, then "🗑 Deleted (1 row)". (Automated: `DatabaseBrowserTest`.)

---

## C. 💬 Chat

### TC-18 · 🧭 How it works diagram
1. Open 💬 Chat with all services 🟢. 2. Click the ℹ️ on each stage. 3. Make one service fail (e.g. wrong key).

**Expected:** Question → Weaviate → *merge with RRF* → Top passages → System 1 (+ "🚫 nothing kept → no answer")
→ System 2 → Answer; moving dashed lines with a glowing particle toward each arrow; ● online pills; each ℹ️ opens its
lesson; a failing service turns ● offline and its incoming line red and still; no text overlaps at any width.

### TC-19 · Ask — RAS (first example chip)
1. Click **What does a RAS system do?** → 🔎 Ask.

**Expected:** a cited answer ([1]…) about recirculating aquaculture; 📚 Sources shows the RAS overview passage ✅ kept.

### TC-20 · Ask — biotech
1. Type `What does PCR do?` → 🔎 Ask.

**Expected:** "PCR makes millions of copies…" with [n]; the "What PCR does" passage ✅ kept.

### TC-21 · Ask — physics, System 1 at work
1. Click **What does Newton's second law say?**

**Expected:** "F = m · a" with [1]; Newton's laws ✅ kept, other physics passages 🚫 not relevant.

### TC-22 · Nothing kept
1. Ask something the data does not cover (e.g. `What is the capital of Peru?`).

**Expected:** "System 1 found no passage that can answer…"; 🧠 step "➖ not needed"; no System 2 call in 🔬 Trace.

### TC-23 · Live progress steps
1. Ask any question and watch the chips under the question box.

**Expected:** ⏳ 🗄️ search → ⚡ check passages (N) → 🧠 write answer, then all ✅; on a failure ⛔ on that step.

### TC-24 · Answer card, Sources, Trace, Data flow
1. After an answer, look at the card. 2. Open 📚 Sources and 🔬 Trace. 3. Press 🔀 Data flow.

**Expected:** the answer card has a ✨ badge at its top-left corner and a violet → teal border (the question card stays
plain blue); Sources list every passage with BM25 / vector / RRF chips, Laya scores and ✅ / 🚫; Trace shows times,
model and tokens; Data flow shows every hop with start time and duration.

### TC-25 · 📂 History
1. Ask two questions. 2. In 📂 History click the row, drag the splitter. 3. Double-click (or right-click → 🔍 Details…).
4. Press ▶ Continue here.

**Expected:** one row per conversation; the card shows its questions; the dialog shows the whole chat with the same
✨ answer cards; ▶ continues the same conversation.

### TC-26 · A service fails during a question
1. Save a wrong OpenRouter key without pressing 🧪 Test. 2. Ask a question.

**Expected:** ⚠️ in the answer, pill 🔴, chat disabled, ⛔ on the 🧠 step; nothing crashes; the error is logged once.

---

## D. Languages, layout, help

### TC-27 · Three languages
1. Switch the language (top right) to 🇻🇳 Tiếng Việt, then 🇯🇵 日本語.
2. Click each example question. 3. Open a few ℹ️ lessons.

**Expected:** the whole page, pills, diagram, hints and dialogs are translated; the example questions get cited answers
from passages in that language (⚡ keeps the Vietnamese / Japanese passage).

### TC-28 · ℹ️ learning hints
1. Click ℹ️ next to titles, pills, columns, score chips and terms on all three pages.

**Expected:** a small dialog: what it is · what it does here · linked to · a little theory · 🔗 learn more.

### TC-29 · Header
1. View the page at 1280, 1440, 1800 and 1920 px wide.

**Expected:** centred title with the ocean-wave border; logos Spring AI → Vaadin → Hugging Face → GitHub; names next to
the logos only from 1800 px; the title never overlaps the logos (≥ 40 px gap).

### TC-30 · Phone + dark mode
1. 390 px wide, dark mode; open all three pages.

**Expected:** no sideways scroll; cards stack; the diagram becomes compact rows; the split cards go below the grid;
text readable (AA contrast).

---

## D2. 🧠 Self-learning

### TC-33 · A verified answer is learned
1. Ask **What does a RAS system do?** 2. Look at the chip under the answer, the 🧠 Learn step, 🔬 Trace and 🔀 Data flow.
3. 🔎 Search Data → 🧠 Learned answers → 🔄. 4. Press ⛶, then Esc (and ⛶ again).

**Expected:** "🧠 Learned ✅ — ⚡ answers 0.9x · grounded 0.8x · injection 0.0x"; the 🧠 Learn chip ✅; Trace line
"🧠 Learn — saved…"; Data flow hops "7. ⚡ verify the answer" → "8. 🧠 learn"; the question appears in 🧠 Learned answers (a `kind = learned` object in DemoDoc, never in the Passages grid);
the 🧭 diagram's save-back line shows the new count; ⛶ fills the window with the card and its grid grows with it,
Esc or ⛶ brings it back.

### TC-34 · A similar question reuses it
1. Ask **what does a RAS system do** (same meaning, other words / case). 2. Look at the chip, steps and Trace.

**Expected:** "♻️ Reused a learned answer (distance 0.0x · ⚡ answers 0.9x)" (tooltip: the learned question); steps
♻️ 🗄️ ✅ → ⚡ ✅ → 🧠 ➖ → 🧠 Learn ➖; Trace "♻️ Reused … System 2 not called"; the answer's sources are the original
passages. In 🇻🇳 / 🇯🇵 the English answer is never reused (same language only).

### TC-35 · What is not learned
1. Ask a question the data cannot answer (e.g. `What is the capital of Peru?`).
2. Ask while System 1 is busy (several questions within a minute on the public Laya route).

**Expected:** step 1 nothing kept → no 🧠 chip (nothing to learn); an answer without [n] or saying it does not know →
"🧠 Not learned — …"; step 2 "🧠 Not learned — System 1 was busy" while the answer itself is shown normally.
(Automated: `AnswerVerifierTest`, `RagServiceTest` reuse / no-reuse / rejected, `WeaviateIT` save + closest.)

## E. Robustness

### TC-31 · No dead threads, no leaking connections
1. Note the JVM's platform threads (`jcmd <pid> Thread.print | grep -c '^"'`) and established TCP connections
   (`lsof -a -p <pid> -iTCP -sTCP:ESTABLISHED`).
2. Load the three pages ~15 times in a browser and ask a few questions. 3. Wait 75 s and count again.

**Expected:** threads rise under load and fall back to about the baseline when idle; connections drop back to a few
pooled keep-alives (Weaviate Cloud, OpenRouter, Laya). (Automated: `LayaClientHttpTest`, `SystemTwoClientHttpTest`.)

### TC-32 · Redeploy on Render
1. Merge a PR to `main`; watch Render's deploy. 2. Keep a page open during the switch-over.

**Expected:** the new instance goes live; the log of the old one shows "Commencing graceful shutdown" → "Graceful
shutdown complete" and no "Database is already closed"; keys still work (environment defaults).

---

## Results

| ID | Result · run | Notes |
|---|---|---|
| TC-01 | ✅ 2026-10-16 Local-Docker + Render (before keys) | |
| TC-02 | ✅ 2026-10-16 Local-Real | `••••4c73` / `••••MjAw`; kept after restart; never in the log |
| TC-03 | ✅ 2026-10-16 automated (`KeyOverrideTest`) + Render env keys live | Render: 🟢 System 2 "key OK" with no key typed |
| TC-04 | ✅ 2026-10-16 Local-Real | fake OpenRouter key → real 401 → "invalid API key" |
| TC-05 | ✅ 2026-10-16 Local-Real | Weaviate ● online, System 1 ● online, System 2 ● offline (invalid local key) |
| TC-06 | ✅ 2026-10-16 Local | lesson opens from all three fields |
| TC-07 | ✅ 2026-10-16 Local-Real | "Search 466 models (18 free)"; first default nemotron-3-ultra-550b |
| TC-08 | ✅ 2026-10-16 Render + automated | "1500 passages"; stray record gone (`WeaviateIT`) |
| TC-09 | ✅ 2026-10-16 automated + Local | count first, then pages |
| TC-10 | ✅ 2026-10-16 Local-Real | list `#, Title, Source` · BM25 `+BM25` · Vector `+Vector` · Hybrid `+BM25, Vector, RRF` |
| TC-11 | ✅ 2026-10-16 Local-Real | #1 "Gases dissolving in liquids (Henry's law)" (BM25 7.88, vector 0.437) |
| TC-12 | ✅ 2026-10-16 automated (`LangTest`, `WeaviateIT` [en]) | |
| TC-13 | ✅ 2026-10-16 Local | full width without a card; splitter drag; schema on the card |
| TC-14 | ✅ 2026-10-16 Render | JSON + fields views; `default (1024d)` |
| TC-15 | ✅ 2026-10-16 Local-Real (Weaviate Cloud) | text2vec-weaviate, hfresh, 5 properties, `lang` filterable |
| TC-16 | ✅ 2026-10-16 Local | API_SETTING `••••MjAw`, `••••4c73` |
| TC-17 | ✅ 2026-10-16 automated (`DatabaseBrowserTest`) + Local form | key value and NAME locked |
| TC-18 | ✅ 2026-10-16 Local | 1440 / 1000 / 390 px, light + dark; System 2 offline → red still line |
| TC-19 | ✅ 2026-10-16 Render | "A recirculating aquaculture system (RAS) is an indoor fish farm that … [1]" — 3 used of 4 |
| TC-20 | ✅ 2026-10-16 Render | "PCR makes millions of copies of a short piece of DNA in a few hours. [1]" — 2 used of 4 |
| TC-21 | ✅ 2026-10-16 Local-Real | "F = m·a. [1]" — 1 used of 4; gases, Beer–Lambert 🚫 not relevant |
| TC-22 | ✅ 2026-10-16 Local-Real | kettle question → 0 kept → System 2 "not needed" |
| TC-23 | ✅ 2026-10-16 Local-Real | +0.2 s 🗄️ → +0.6 s ⚡ → +5.9 s 🧠 → +11.5 s ✅ |
| TC-24 | ✅ 2026-10-16 Local + Render | ✨ badge and gradient border in chat and history dialog |
| TC-25 | ✅ 2026-10-16 Local | |
| TC-26 | ✅ 2026-10-16 automated (`RagServiceTest`, `ChatHistoryServiceTest`) + Local-Docker banner | |
| TC-27 | ✅ 2026-10-16 Render + Laya checks | vi / ja examples kept at relevant 0.89–0.99 |
| TC-28 | ✅ 2026-10-16 Local | all learn-more links HTTP 200 |
| TC-29 | ✅ 2026-10-16 Local (measured) | gap 40 / 120 / 61 / 121 px |
| TC-30 | ✅ 2026-10-16 Local | |
| TC-31 | ✅ 2026-10-16 Local | threads 83 → 121 under load → 67 idle; TCP 11 → 9 → 4 |
| TC-32 | ✅ 2026-10-16 Render logs | 18:29:26 graceful shutdown, no "already closed" |
| TC-33 | ⬜ after deploy (automated ✅: `RagServiceTest`, `WeaviateIT`) | |
| TC-34 | ⬜ after deploy (automated ✅: `RagServiceTest` reuse, `WeaviateIT` closest + language) | |
| TC-35 | ✅ automated (`AnswerVerifierTest`, `RagServiceTest`) | |

**Automated tests** (`./mvnw test`, 2026-10-16): **64 tests, 0 failures** — incl. `WeaviateIT` (real Weaviate 1.40 in
Docker, 1,500 passages), `DatabaseBrowserTest` (real H2), `LayaClientHttpTest` / `SystemTwoClientHttpTest` (fake HTTP
servers on localhost).
