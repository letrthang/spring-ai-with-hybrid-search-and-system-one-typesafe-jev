# Multi-task management and agent memory

How an AI agent plans, tracks, remembers and delivers a **big module with many tasks** in this project.

---

## 0. Before anything: read the design docs, then pull

1. **Read the project's `.md` design files before you design or plan anything.** At least read:
   - `README.md`: the project overview and setup.
   - `GITHUB_COMMIT_RULE.md`: commit, branch and PR rules.
   - this file.
   - any design `.md` next to the module you are changing.
2. **Pull the latest `main`:** `git fetch origin && git pull --ff-only` on `main`. Do your work in a fresh git worktree off `origin/main`, never in a stale branch.
3. **Design docs are not gospel.** Check each decision against what is actually built (code, the running apps, the localhost data). If the implementation is right and the doc is stale, update the doc.

## 1. Plan before you build

When a module has more than a couple of tasks, **write a detailed plan first.** List the tasks, in order, small enough that each one is a single pull request.

For each task, write:
- **what it delivers:** behaviour, screens, APIs, tables;
- **which repo(s)** it changes;
- **its dependencies:** what must be merged first, including cross-repo ones;
- **how it will be verified:** which tests, which live check on which port;
- **the test cases** it adds or updates.

Prefer **small, independently mergeable slices** over one big change. Keep the plan in the GitHub issues (section 2), not only in a local file or in the agent's head.

## 2. One main GitHub issue + one sub-issue per task

1. **Create one main (parent) issue for the module** in the repo that owns most of the work. It holds:
   - the goal and scope;
   - the owner's rulings (dated);
   - the plan as a checklist, one line per task, each linking its sub-issue;
   - the cross-repo dependencies;
   - the "read first" list of docs.
2. **Create one sub-issue per task** from the plan, using GitHub's sub-issues:
   - the `gh` CLI has no sub-issue command yet, so use `gh api repos/{owner}/{repo}/issues/{parent}/sub_issues -X POST -F sub_issue_id=<issue id>` (the numeric issue **id**, not the number);
   - a task in a sibling repo gets its issue in **that** repo and is linked from the parent's checklist (and as a sub-issue where GitHub allows it).
3. **Each sub-issue is self-contained,** so another agent can start from it cold. It holds:
   - what to build and why;
   - the exact contract or API if it crosses apps;
   - the files to read first;
   - the implementation rules from §8, copied in or linked (Tailwind + bento, virtual threads, lazy loading, master-detail grids with ⛶ full screen, dialogs with ⛶ ✕, EN + VI);
   - the acceptance criteria as a checklist;
   - its dependency ("Blocked by #n");
   - how to test it.
4. **Every sub-issue must be labelled when it is created.** No unlabelled sub-issue. At least:
   - the module's shared label, the same one as the parent, e.g. `module:<name>` (§5);
   - a type label: `enhancement`, `bug` or `documentation`;
   - a severity label: `severity:low|medium|high|critical`, usually the parent's;
   - an area label where the repo has one: `backend`, `frontend`…

   Use `gh issue create … --label <a> --label <b>`, or `gh issue edit <n> --add-label <x>` for an existing one. If a label is missing in that repo, create it first with `gh label create`, with the same name and colour as in the other repos. When a sub-issue's scope changes, update its labels too.
5. **Keep issues current:**
   - comment on progress;
   - tick the checklist items;
   - record each new owner ruling in the issue (with its date) as soon as it is given;
   - link every PR.

## 3. Agent memory vs GitHub issues

- **Memory is allowed.** An agent may save notes in its own memory, in whatever form its AI model provides (Claude's file memory, another assistant's notes).
- **The GitHub issue is the source of truth.** It's the only memory every agent, human and model can read. **Always put the information into the GitHub issue too:**
  - decisions and owner rulings, with dates;
  - the plan and its status;
  - the PRs and their merge state;
  - the test results, including what was **not** checked live;
  - blockers and open questions;
  - a hand-off note when you stop.
- **Never put secrets** (tokens, passwords, keys) into issues, PRs, memory or commits. Point to where a credential is stored instead, e.g. a local, untracked `.env` file.
- When memory and an issue disagree, **the issue wins.** Fix the memory.

## 4. Finish each task: PR → test → merge → back to main

When a sub-issue (or an issue) is done:
1. **Open a pull request** from its branch with the code, tests and docs. The PR body:
   - says `Closes #<sub-issue>`;
   - lists the tests run, with results;
   - lists the live check, with screenshots or evidence;
   - says what was **not** done.

   Follow that repo's `GITHUB_COMMIT_RULE.md`. Stage explicit paths only, and never commit build-generated changes (e.g. a `package-lock.json` rewrite) or secrets.
2. **Test:**
   - **Tests:** run the affected test classes (not the whole suite unless asked);
   - **live check:** on the running app, logged in with the test credentials;
   - **logs:** confirm 0 ERROR lines and no browser console errors.
3. **Merge to `main`** once it's verified. Small PRs, merged fast. Delete the branch and remove the worktree.
4. **Switch to `main`, pull the latest code, rebuild and restart the app on its port from `main`,** then continue with the **next** task from the updated `main`.
5. **Update the parent issue:** tick the task, link the merged PR, and note anything the next task must know.

## 5. Link related issues with GitHub labels

- **Give every module one shared label,** e.g. `module:<name>`, and put it on the parent issue and on **every** sub-issue, **in every repo** the module touches. **A sub-issue without labels is not allowed** (§2 item 4). Create the label in each repo if it's missing, with the same name and colour everywhere.
- **Also use the repo's usual labels:**
  - type: `enhancement`, `bug`, `documentation`;
  - area: `backend`, `frontend`;
  - severity: `severity:low|medium|high|critical`.
- **Cross-link in the text too:**
  - `Blocked by owner/repo#n`;
  - `Follows owner/repo#n`;
  - `Part of #parent`;
  - `Closes #n` in PRs.

  A label groups issues; a link states their order.
- **Find a module's issues** with `gh issue list -R <repo> --label module:<name> --state all`, run in each repo.

## 6. Testing and test cases

- **Test cases live in the repo's testing folder:**
  - keep them under `docs/testing/` (create it if missing).
- **Every task adds or updates its test cases:**
  - steps start with Step 0, which starts the app and its dependencies;
  - each case has the expected results, and the actual result with a date (✅ / 🟠 Partial / ❌);
  - recalculate the summary tables (the module row and the TOTAL row).
- **Owner changes during a round also become dated steps** in the relevant case. That includes a screenshot fix or a tweak, so nothing is changed without a test that shows it.
- **Test what changed:** the targeted test classes plus the affected live steps. Run the full suite or a full live re-run only when asked.
- **Report the test results in the issue or sub-issue you were testing.** Post a comment with:
  - the date and the commit / PR tested;
  - the test classes run, with pass / fail counts;
  - the live steps run on which port, each with its result, plus screenshots or evidence paths;
  - the ERROR-line and console-error counts;
  - the failures, with what you saw;
  - what was **not** tested and why;
  - the test cases added or updated (file names, case IDs).

  Then tick the matching acceptance-criteria items. A test result that lives only in a PR or in an agent's memory doesn't count.

## 7. A good hand-off (when you stop, pause or run out of context)

Write it in the parent issue (and in your memory if you have one):
- what is merged, with the PR numbers;
- what is in flight: branch, worktree, last commit, pushed or not;
- what was tested, and what was not;
- the next task to pick up;
- the open questions for the owner.

**Also write the detailed hand-off on the sub-issue (or issue) of the task you left.** The parent gets the summary; the task itself gets the detail. Comment on that sub-issue with:
- **where the work stands:** what is done, what is half-done, and the exact next step;
- **the code state:** branch, worktree path, last commit, pushed or not, open PR (if any), and anything uncommitted;
- **the environment:** which app is running on which port and from which build, held locks or leases, temporary test data or settings you changed and whether you restored them;
- **the test state:** which tests passed or failed and which live steps were or weren't run (see §6);
- **anything surprising** you learned, and the owner's latest instructions for this task.

The next agent should be able to continue that task from its sub-issue alone, and the whole module from the parent issue.

## 8. Implementation rules (UI and code)

These are the owner's standing rules for **every screen and every task in this project**. Put them in each sub-issue (§2), and check them before you call a task done.

### Layout and styling: bento + Tailwind
- **Bento:** every screen that summarises or operates something is a bento, meaning a responsive grid of rounded cards of different sizes. One column on a phone, a 12-column grid on desktop.
- **Tailwind CSS first** for layout and spacing inside cards. **Aura theme tokens** for colour, radius and text, so light and dark themes both work.
  - Hand-written CSS only where Tailwind can't reach (e.g. grid rows through `::part()`), added once in the module's section of the stylesheet.
  - A global CSS change can affect every view; check other modules when you touch it.
- **Layout checks:** check every screen at 390 px wide (phone) with **no sideways page scroll**, and in **light and dark** themes.
- **Glass button style:** the house style.
- **One label and one icon per button state.** Never a combined name like "Create/Update", "Pause/Resume" or "Approve/Reject"; swap the label and icon by state.
- **Emoji prefixes** on card titles and section titles where the screen already uses them, distinct per card.

### Threads and data loading
- **Virtual threads wherever possible:** every blocking call runs **off the UI thread** on a virtual thread. That includes database reads and writes, HTTP calls, AI model calls, file and S3 work, and long counts.
- **Paint the result back with `UI.access`,** only after a liveness check (the page is still attached). Coalesce bursts (one queued `UI.access` at a time, latest state wins), and drop the update for a closed or dead UI.
- **Remove timers and listeners on detach,** so a closed page never leaks.
- **Lazy data loading:**
  - grids use a lazy data provider with **paging** (a page at a time, a real total count, one batched lookup per page for names; never one query per row);
  - totals and card counts come from aggregate queries, never from loading every row;
  - big details load on demand, when a row or section is opened.

### Grids: Vaadin master-detail, always
Every grid follows the same pattern:
- It's a **Vaadin master-detail** (`MasterDetailLayout` or the app's master-detail helper).
- **Single click** selects the row and shows a **nicely decorated detail card in the right panel / drawer**, which opens over the grid on narrow screens.
- **Click the same row again to deselect it**. A single click on the selected row (the one whose card is showing) deselects it and closes the card, so the grid is back to normal. The card's × does the same.
  - A **right-click never deselects**: it selects the row and opens the menu.
  - A **double-click** opens the dialog and keeps the row selected and its card open.
  - A refresh after a deselect does not re-open the card.
  - **Build it once in the app's shared master-detail helper,** never per grid. Many grids set `setDeselectAllowed(false)`, so the helper handles this click itself.
- **Double-click** opens the **details dialog**.
- **Right-click** opens the row's **context menu**: 🔍 Details… first, then the row's other actions.
- **Every column is resizable.**
- **Every grid has a ⛶ full-screen button**, which toggles the grid to the full viewport and back; Esc exits.
  - **Exception:** a grid **inside a dialog** has no ⛶ of its own; the dialog's ⛶ is the one.
- A short hint under the toolbar: "Click selects · click again to close · double-click or right-click a row for 🔍 Details…".
- **Show names, not raw UUIDs:** a device shows its name, with its id / serial as small muted text.
- **Every dropdown (combo box)** with ids shows the name too, with the id as small text under it, and lets you search by either.

### Dialogs
- **Every dialog has ⛶ full screen and ✕ close at the top-right.** Never a text "Close" button.
- **Confirm dialogs** use the same header, where ✕ means Cancel.
- **Fixed-width form dialogs** keep their width; big content dialogs scroll inside, not the page.
- **Unsaved changes ask** before the dialog closes.

### Components and authoring
- **Check current docs first.** Before you use a library API you haven't used yet, check its current documentation.
- **Text:** every user-facing string goes in the i18n bundles, never hard-coded.

### Safety and secrets
- **Never put a token, password or key** in code, commits, logs, issues, PRs or chat. Credentials live in a local, untracked `.env` file.
- **Safety-relevant logic stays deterministic and tested.** An AI model's answer is advice; it never bypasses validation or permission checks.

