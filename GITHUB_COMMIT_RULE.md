> [!IMPORTANT]
> **Do NOT auto-commit or auto-push unless the developer explicitly asks the bot to do so.**
> The bot must always ask the developer for confirmation before committing and pushing code.

> [!IMPORTANT]
> **The bot creates a new release version only when the developer asks for it.**
> Merging into `develop` or `main` is not a release: no PR `main` → `release`, no version change, no tag and no GitHub
> release unless the developer explicitly asks for one. See [Git flow](#git-flow-since-v100).

# GitHub Commit and Push Rules for Bot

When the bot is asked to commit or push code changes to the GitHub repository, it must follow these rules in order (must do from step 1 to step 7). The repo follows [git flow](#git-flow-since-v100): everyday work goes to **`develop`**, not `main`.

1. Try to use GitHub MCP tools first to access and operate on the remote GitHub repository.
2. Start from `develop`, then pull the latest changes from the remote repository into local. The bot must stop and report to the developer if it detects conflicts between remote changes and local working files that it cannot safely resolve.
3. From `develop`, create a new branch using `feat/*`, `bugfix/*` or `docs/*`, and commit code changes only on that new branch.
4. Create a pull request (PR) to merge the new branch into `develop`.
5. Merge PR into `develop` (squash). Need not wait for approval from other developers. But can stop and report if there are many conflicts that the bot determines it cannot safely resolve.
6. After the PR is merged successfully, switch local repo back to `develop` and delete the local `feat/*`, `bugfix/*` or `docs/*` branch.
7. From `develop` branch, pull the latest changes to sync up with the remote repository, build and run the app locally and test the merged change. When the test passes, create a PR `develop` → `main` and merge it (merge commit) — see [Daily work](#daily-work).

## Git flow (since v1.0.0)

Three long-lived branches, in this order: **`develop` → `main` → `release`**.

> [!IMPORTANT]
> **The bot creates a new release version only when the developer asks for it.** Without that request the bot never
> opens a PR `main` → `release`, never changes the version in `pom.xml`, never creates a tag and never publishes a
> GitHub release — however many changes are merged into `develop` and `main`.

### Branches

| Branch | What it holds | Gets its changes from |
|---|---|---|
| `develop` | **Daily work.** Its `pom.xml` version is the next one, as a snapshot (e.g. `1.1.0-SNAPSHOT`). | PRs from `feat/*`, `bugfix/*`, `docs/*` (squash) |
| `main` | **Tested work**: what passed the local test on `develop`. Not deployed. | PR `develop` → `main` (merge commit) |
| `release` | **Production.** What the live site runs; deployed by the release workflow (`deploy.yml`, run by hand), never by a push. | PR `main` → `release` (merge commit) — **only when the developer asks for a new release** |
| `feat/*`, `bugfix/*`, `docs/*` | One issue each, from `develop`. Deleted after the merge. | — |

Never commit directly to `develop`, `main` or `release`: every change arrives by PR. Use a **merge commit** (not squash)
for `develop` → `main` and `main` → `release`: the three branches then share their history, so the next PR between
them carries only the new commits (squash would make every later PR conflict).

### Daily work

1. Steps 1–7 above: a `feat/*`, `bugfix/*` or `docs/*` branch from `develop`, a PR into `develop`, then build, run and
   test the merged `develop` locally.
2. After that test passes: **PR `develop` → `main`**, merge commit. Check out `main`, pull, and check it builds and runs.
3. That is all — nothing goes to `release` and nothing is live until the developer asks for a new release. Say so when
   the developer asks about the live site.

### A new release — only when the developer asks

1. PR into `develop` (e.g. `docs/release-x.y.z`): `pom.xml` version `x.y.z` (drop `-SNAPSHOT`), a
   `## [x.y.z] — YYYY-MM-DD` section in `CHANGELOG.md` (move the *[Unreleased]* items into it). Build and test:
   `./mvnw clean package` (all tests) and `docker build .` (Render builds the Dockerfile).
2. PR `develop` → `main` (merge commit); check out `main`, run it locally and test it.
3. **PR `main` → `release`** (merge commit). This deploys nothing by itself: Render's Auto-Deploy is off.
4. **Run the release workflow** — the only deploy path (#141). Pass every parameter the developer gave (version, tag,
   environment) as inputs; there is no manual approval step:
   ```bash
   gh workflow run deploy.yml --repo letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev \
     --ref release -f version=x.y.z [-f tag=vx.y.z] [-f environment=production]
   gh run list --repo letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev --workflow deploy.yml --limit 1
   ```
   **Tell the developer the run is started, with its link** (`https://github.com/<repo>/actions/runs/<id>`), then
   watch it (`gh run watch <id>`). The workflow checks the ref (`release`), the `pom.xml` version, the CHANGELOG section
   and that the tag is new; runs `./mvnw clean verify`; calls the Render deploy hook (secret `RENDER_DEPLOY_HOOK_URL`);
   waits for `/actuator/health` = UP; then creates the tag and the GitHub release **itself**, with the notes taken
   from the `## [x.y.z]` section of `CHANGELOG.md`. Never run it to test it: every run deploys production.
   The notes follow the format of [iattendance v5.4](https://github.com/letrthang/iattendance/releases/tag/v5.4):
   - No assets, not a draft.
   - Notes: `### Features` / `### Fixes` / `### Security` / `### Reliability` / `### Documentation` as they apply;
     each bullet starts with a **bold lead sentence**, then why and how in plain words, and a "Verified: …" note
     where something was tested. Last line: `Live app version: x.y.z — <live URL>`.
5. PR into `develop`: `pom.xml` → the next `-SNAPSHOT`, a new `## [Unreleased]` section in `CHANGELOG.md`; then
   `develop` → `main` as usual.

### Urgent fix on the live site (hotfix) — only when the developer asks

1. `hotfix/*` from `release`, the fix and its test → PR into `release`, then run the release workflow (step 4 above)
   with the patch version — Render does not deploy a merge by itself.
2. Then PR `release` → `main` and `main` → `develop`, so the fix is not lost. A version change, tag and GitHub release
   only if the developer asks for them.

### Render

- `render.yaml`: `branch: release`, `autoDeploy: false`. **Auto-Deploy must be OFF in the dashboard too** (Settings →
  Build & Deploy → Auto-Deploy = No): `.github/workflows/deploy.yml` is the only deploy path, through the service's
  Deploy Hook, stored as the repo secret `RENDER_DEPLOY_HOOK_URL` (set by the developer; never print it).
- The live service must use the same branch: Render dashboard → the service → **Settings → Build & Deploy → Branch =
  `release`** (a Blueprint sync applies `render.yaml` too).
- History: v1.0.0 was released from a `release/1.0.0` branch (tag `v1.0.0`); the single `release` branch starts at
  that tag. Git cannot hold `release` and `release/…` branches together, so never create `release/*` branches.

## What the Bot May Stage — NEVER `git add -A`

> [!CAUTION]
> **Stage explicit paths. Never `git add -A`, `git add .` or `git add -u`.**
> A bot knows which files it edited; a wildcard stages the ones it did not, and those are exactly
> the files nobody reviews in the diff.

```bash
# WRONG — sweeps whatever else the working tree happens to hold
git add -A && git commit -m "..."

# RIGHT — the files this change actually touched
git add src/main/java/.../SomeView.java src/main/frontend/views/.../some-view.ts
git commit -m "..."
```

### `.json` is never part of a feature commit

Do **not** commit `*.json` unless the developer asks for that file by name. In particular:

| file | why it must not ride along |
|---|---|
| `.mcp.json` | **holds a GitHub personal access token.** It is in `.gitignore` — which does nothing once the file is tracked |
| `package-lock.json` | a lockfile changes from any local `npm` or dev-server run; committing that noise inside a UI change hides a real dependency change when one happens |
| `package.json`, `tsconfig.json`, `settings.json` | build and tool configuration — a deliberate change, never a side effect |

**This rule exists because it was broken** in another project: a commit staged with
`git add -A` **added `.mcp.json` with a live GitHub token** to the repository and stripped 111
lines out of `package-lock.json`, inside a commit whose subject was about dashboard chrome. Both
were invisible in the PR review because neither belonged to the change being read.

### Before every commit

1. `git status --short` — read it. Anything you did not edit does not get staged.
2. Stage by path, then `git diff --cached --stat` and confirm the file list is only your change.
3. If a secret-bearing file is already tracked, untrack it (`git rm --cached <file>`) and tell the
   developer the secret must be **rotated** — removing it from HEAD does not remove it from history.

## How to Create a GitHub Issue

All issues for this project go in **this repo**: <https://github.com/letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev/issues> (`gh issue create -R letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev`). Never file them in another repo.

When a bot or developer needs to create a GitHub issue, follow this structure.

### Issue Title Format

Use a short, descriptive prefix in square brackets followed by a concise summary:

```
[<type>] <Short description of the problem or feature>
```

**Examples:**
```
[bug] Hybrid search returns duplicate chunks for the same document
[feat] Add a re-ranking step after hybrid search
[docs] Document local setup in README
[need-admin-approve] Migrate database schema for v2
```

**Title type prefixes:**

| Prefix | When to use |
|--------|-------------|
| `[bug]` | A confirmed bug or unexpected behavior |
| `[UI bug]` | A visual/layout bug in the frontend |
| `[feat]` | A new feature or enhancement |
| `[docs]` | Documentation update |
| `[refactor]` | Code cleanup or restructuring |
| `[need-admin-approve]` | Any change requiring admin sign-off before merge (bots MUST NOT merge without explicit admin approval) |

---

### Issue Body Structure

Use the following markdown template:

```markdown
## 🐛 Bug Report / ✨ Feature Request / 📄 Docs Update: <Short Title>

### Description
[Clear description of the issue or feature. What is happening? What should happen?]

### Root Cause Analysis (if applicable)
[If known, describe the root cause or hypothesis.]

---

### Requirements for Bots

1. **Design Proposal:** Propose a minimum of **2 alternative technical solutions** in a comment before writing any code.
2. **Implementation & Testing:** Implement the chosen solution and verify it against the requirements.
3. **Closure:** Provide the full `✅ Issue Summary` markdown block before closing the PR and issue.

---

### Files to Review
- [List relevant files the bots should look at]

---

### 🚨 CRITICAL NOTE FOR BOTS (add if needed)
[Any deployment warnings, live URL verification steps, or special constraints.]
```

---

### Adding images to an issue or comment

The GitHub API can't attach an image to an issue. So images are **committed to a git branch** and the issue embeds them by URL. Images go on **this repo**'s evidence branch, never on another repo.

1. **The branch:** use the evidence branch **`docs/issue-evidence-images`**.
   - It is an orphan branch: a `README.md` plus an `images/` folder.
   - It is **never merged into `main`**, and needs no PR. Pushing to it directly is the one exception to "always create a PR".
   - Work on it in its own worktree, never in a checkout someone else is using:
     ```bash
     git fetch origin docs/issue-evidence-images
     git worktree add -B docs/issue-evidence-images ../spring-ai-wt-evidence origin/docs/issue-evidence-images
     ```
   - **First time only,** if `git ls-remote --heads origin docs/issue-evidence-images` prints nothing, create the branch:
     ```bash
     git worktree add --orphan -b docs/issue-evidence-images ../spring-ai-wt-evidence
     cd ../spring-ai-wt-evidence
     mkdir images && echo "# Issue evidence images (never merged into main)" > README.md && touch images/.gitkeep
     git add README.md images/.gitkeep
     git commit -m "evidence: init orphan branch"
     git push -u origin docs/issue-evidence-images
     ```
2. **The file:**
   - **WebP**, convert screenshots from PNG first:
     ```bash
     python3 -c "from PIL import Image; Image.open('in.png').convert('RGB').save('out.webp','WEBP',quality=90,method=6)"
     ```
   - Named `images/yyyy_mm_dd_hh_mm_ss_c<issue>_<what>.webp`, e.g. `images/2026_10_14_14_20_00_c1122_rules_card_menu_contrast_still.webp`.
   - Use the issue number in the name, and never reuse a name: a pushed image may already be embedded somewhere.
3. **Commit and push** only that image, by its path:
   ```bash
   cd ../spring-ai-wt-evidence
   git add images/<file>.webp
   git commit -m "evidence: #<issue> <what the image shows>"
   git push origin docs/issue-evidence-images
   ```
4. **Embed it** in the issue description or comment with this exact URL form. Note the `blob/<branch>/` path and the `?raw=true` suffix:
   ```markdown
   ![<what the image shows>](https://github.com/letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev/blob/docs/issue-evidence-images/images/<file>.webp?raw=true)
   ```
   - Put a sentence above each image that says what to look at.
   - Write meaningful alt text, never just `image` or `screenshot`.
   - To change an existing description or comment: `gh issue edit <n> -F body.md`, or `gh api -X PATCH repos/letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev/issues/comments/<id> -F body=@body.md`.
5. **Clean up:**
   - Remove the worktree (`git worktree remove ../spring-ai-wt-evidence`).
   - Never commit evidence images to a feature branch or to `main` just to show them in an issue. Test-case screenshots that belong to the repo go with their test case.

Never put a screenshot that shows a token, password, key or personal data on this branch. Crop or blur it first, because the branch is permanent history.

---

### Issue Labels

Apply labels at creation time.

#### Type labels (set at creation)
| Label | When to apply |
|-------|---------------|
| `bug` | Bug reports |
| `frontend` | UI / frontend issues |
| `backend` | Server-side / API issues |
| `docs` | Documentation changes |
| `feat` | New feature or enhancement |

#### Status labels (updated by bots as work progresses)
| Label | Meaning |
|-------|---------|
| `status: design-review` | Design posted, awaiting review |
| `status: design-approved` | Design approved, ready for implementation |
| `status: implementing` | Code being written |
| `status: code-review` | PR ready for code review |
| `status: testing` | Code review passed, testing in progress |
| `status: test-passed` | All tests passed |
| `status: test-failed` | Tests failed, needs fix |
| `status: done` | Issue closed, all complete |
| `need-admin-approve` | Admin sign-off required before merge — bots MUST NOT merge without explicit admin comment approving the PR |

> ⚠️ At issue creation, always set at least one **type label** and `status: design-review` to kick off the bot workflow.

---

### Minimal Required Checklist Before Creating

- [ ] Title uses the correct `[type]` prefix
- [ ] Description is clear and unambiguous
- [ ] "Requirements for Bots" section is included if bots will work on this issue
- [ ] At least one type label is applied
- [ ] `status: design-review` label is applied to trigger bot workflow
- [ ] `need-admin-approve` label added if merge must not happen without admin sign-off

---

## Notes

- Do not commit directly to `develop`, `main` or `release`. Always create a PR: feature branches → `develop`, then `develop` → `main` after the test, and `main` → `release` only for a new release.
- Create a new release version (PR `main` → `release`, version, tag, GitHub release) **only when the developer asks**.
- Keep branch names descriptive and scoped to one task.
- Ensure PR status checks pass before merge.
- Do not commit `package.json` and `package-lock.json`.
- Always delete the local feature branch after its PR is merged (never `develop`, `main` or `release`).
- Switch back to the branch the PR went into (`develop`, or `main`) and pull the latest changes after the merge.
