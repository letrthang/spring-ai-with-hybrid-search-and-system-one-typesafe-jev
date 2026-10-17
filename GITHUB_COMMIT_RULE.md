> [!IMPORTANT]
> **Do NOT auto-commit or auto-push unless the developer explicitly asks the bot to do so.**
> The bot must always ask the developer for confirmation before committing and pushing code.

> [!IMPORTANT]
> **The bot creates a new release version only when the developer asks for it.**
> Merging into `develop` or `main` is not a release: no version change, no `release/*` branch, no tag and no GitHub
> release unless the developer explicitly asks for one. See [Git flow](#git-flow-since-v100).

# GitHub Commit and Push Rules for Bot

When the bot is asked to commit or push code changes to the GitHub repository, it must follow these rules in order (must do from step 1 to step 7). The repo follows [git flow](#git-flow-since-v100): everyday work goes to **`develop`**, not `main`.

1. Try to use GitHub MCP tools first to access and operate on the remote GitHub repository.
2. Start from `develop`, then pull the latest changes from the remote repository into local. The bot must stop and report to the developer if it detects conflicts between remote changes and local working files that it cannot safely resolve.
3. From `develop`, create a new branch using `feat/*`, `bugfix/*` or `docs/*`, and commit code changes only on that new branch.
4. Create a pull request (PR) to merge the new branch into `develop`.
5. Merge PR into `develop` (squash). Need not wait for approval from other developers. But can stop and report if there are many conflicts that the bot determines it cannot safely resolve.
6. After the PR is merged successfully, switch local repo back to `develop` and delete the local `feat/*`, `bugfix/*` or `docs/*` branch.
7. From `develop` branch, pull the latest changes to sync up with the remote repository, build and run the app locally and test the merged change before starting the next branch.

## Git flow (since v1.0.0)

The repo uses [git flow](https://nvie.com/posts/a-successful-git-branching-model/) since release **v1.0.0**.

> [!IMPORTANT]
> **The bot creates a new release version only when the developer asks for it.** Without that request the bot never
> creates a `release/*` branch, never changes the version in `pom.xml`, never creates a tag and never publishes a
> GitHub release — even after many merges into `develop`, and even when a change is merged into `main`.

### Branches

| Branch | Comes from | Merges into | What it holds |
|---|---|---|---|
| `main` | — | — | **Production.** Render deploys every push to `main`, so `main` is what the live site runs. Only `release/*`, `hotfix/*` and changes the developer explicitly asks to put on `main` reach it. |
| `develop` | `main` | — | **Integration.** Everyday work. Its `pom.xml` version is the next one, as a snapshot (e.g. `1.1.0-SNAPSHOT`). |
| `feat/*`, `bugfix/*`, `docs/*` | `develop` | `develop` (PR, squash) | One issue each. Deleted after the merge. |
| `release/x.y.z` | `develop` | `main` (PR, **merge commit**) | A release being prepared — **only when the developer asks**. Kept after the release: the tag points at its head. |
| `hotfix/x.y.z` | `main` | `main` (PR, merge commit), then `develop` | An urgent fix for the live site. |

### Everyday work

Steps 1–7 above: a branch from `develop`, a PR into `develop`, then test the merged `develop` locally. New features are
**not live** until the next release — say so when the developer asks about the live site.

### A change the developer wants on `main` without a release

For example a docs update the developer asks to merge to `main` now:

1. Branch from **`main`** (not `develop`: that would carry `develop`'s snapshot version and unreleased work into `main`).
2. PR into `main`, squash merge. No version change, no tag, no release.
3. Bring `main` back into `develop` with a PR `main` → `develop` (merge commit; never delete `main`), so `develop`
   does not fall behind.

### Creating a release — only when the developer asks

1. `release/x.y.z` from the latest `develop`: `pom.xml` version `x.y.z` (drop `-SNAPSHOT`), a `## [x.y.z] — YYYY-MM-DD`
   section in `CHANGELOG.md` (move the *[Unreleased]* items into it). Build and test: `./mvnw clean package` (all
   tests) and `docker build .` (Render builds the Dockerfile).
2. PR `release/x.y.z` → `main`, merged with a **merge commit** (not squash), so the release branch's head is in `main`.
3. Check out `main`, pull, run it locally and test it.
4. GitHub release, in the format of
   [iattendance v5.4](https://github.com/letrthang/iattendance/releases/tag/v5.4):
   ```bash
   gh release create vx.y.z --target release/x.y.z --title "vx.y.z — YYYY.MM.DD" --notes-file notes.md
   ```
   - The tag `vx.y.z` is created on the head of `release/x.y.z` (the commit `main` runs). No assets, not a draft.
   - Notes: `### Features` / `### Fixes` / `### Security` / `### Reliability` / `### Documentation` as they apply;
     each bullet starts with a **bold lead sentence**, then why and how in plain words, and a "Verified: …" note
     where something was tested. Last line: `Live app version: x.y.z — <live URL>`.
5. Bring `main` into `develop` (fast-forward when possible, else a PR `main` → `develop`), then a PR into `develop`
   that bumps `pom.xml` to the next `-SNAPSHOT` and opens a new `## [Unreleased]` section in `CHANGELOG.md`.
6. Keep `release/x.y.z` (do not delete it).

### Hotfix

1. `hotfix/x.y.z` from `main` (`x.y.z` = the released version + 1 in the last number), the fix and its test.
2. PR into `main` (merge commit). A version change, tag and GitHub release only if the developer asks for them.
3. Bring `main` back into `develop` (PR `main` → `develop`).

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

- Do not commit directly to `main` or `develop`. Always create a PR: into `develop` for everyday work, into `main` only for `release/*`, `hotfix/*` and changes the developer asks to put on `main`.
- Create a new release version (release branch, version, tag, GitHub release) **only when the developer asks**.
- Keep branch names descriptive and scoped to one task.
- Ensure PR status checks pass before merge.
- Do not commit `package.json` and `package-lock.json`.
- Always delete the local branch after its PR is merged (except `release/*`, which is kept).
- Switch back to the branch the PR went into (`develop`, or `main`) and pull the latest changes after the merge.
