# NyasaBlog — Worktree Refactor Guide

Execution runbook for the H1–H9 hardening refactor. Companion to `COMPOSE_REFACTOR.md` (architecture reference).

- **Branch:** `Deploy_0.01`
- **Status:** Phase 9 complete — all UI is Jetpack Compose. No Fragments, no layout XML.
- **Scope:** H1–H9 hardening. H10 (growth features) is backend-blocked.

> **Phase 9 (Compose migration) is complete.** This guide covers H1–H10 hardening only.

---

## Hardening phases at a glance

| Phase | Name | Risk | Est. days |
|---|---|---|---|
| H1 | Error & network hardening | Medium | 2–3 |
| H2 | HTML renderer | **CRITICAL** | 4–5 |
| H3 | Design system tokens | Low | 2 |
| H4 | Comments persistence | Low | 1–2 |
| H5 | Modularization | Medium | 5–7 |
| H6 | Background uploads | Low | 1–2 |
| H7 | Adaptive layout | Low | 3–4 |
| H8 | Observability | Low | 1 |
| H9 | Test stack | Low | 3–5 |
| H10 | Growth features | Blocked | Backend TBD |

> **H2 starts Day 1 in parallel with H1.** Do not wait until H1 is merged.

---

## Part 1 — One-time setup

### Step 1. Verify Git version
Worktrees require Git 2.15+.
```bash
git --version
# If below 2.15: brew upgrade git
```

### Step 2. Create the worktree directory
Do **not** put worktrees inside `.git`. Use `.claude/worktrees/` — close to the project but outside Gradle's scan path.

```bash
cd ~/StudioProjects/Nyasa
mkdir -p .claude/worktrees
echo ".claude/worktrees/" >> .gitignore
```

### Step 3. Create all worktrees
Run once from the project root. Each command creates a directory **and** a new branch.

```bash
git worktree add .claude/worktrees/h1-error-hardening  hardening/h1-error
git worktree add .claude/worktrees/h2-html-renderer    hardening/h2-html-renderer
git worktree add .claude/worktrees/h3-design-tokens    hardening/h3-tokens
git worktree add .claude/worktrees/h4-comments-room    hardening/h4-comments
git worktree add .claude/worktrees/h5-modularization   hardening/h5-modularize
git worktree add .claude/worktrees/h6-workmanager      hardening/h6-work
git worktree add .claude/worktrees/h7-adaptive         hardening/h7-adaptive
git worktree add .claude/worktrees/h8-observability    hardening/h8-observability
git worktree add .claude/worktrees/h9-tests            hardening/h9-tests

git worktree list   # verify
```

### Step 4. Copy `local.properties` to each worktree
Each worktree needs its own SDK path.
```bash
for dir in .claude/worktrees/*/; do
  cp local.properties "$dir"local.properties
done
```

### Step 5. Exclude worktrees from Android Studio indexing
In AS: **File → Project Structure → Modules** → find `.claude` → right-click → **Mark directory as → Excluded**. Prevents AS from re-indexing every copy of the source tree.

### Step 6. Confirm supporting skills exist
Three skills must be installed before H1 starts. They live at `~/.claude/skills/`:

- `nyasablog-architecture` — machine-readable version of `COMPOSE_REFACTOR.md`
- `nyasablog-hardening` — `AppError` mapping, `OptimisticAction`, `CommentEntity`, Hilt-safe moves
- `module-extraction-checklist` — per-module H5 checklist

Check: `ls ~/.claude/skills/ | grep -E "nyasablog|module-extraction"` — expect three matches.

---

## Part 2 — The Claude Code session loop

Every hardening task follows the same loop.

| Step | Action | Tool | Blocks next? |
|---|---|---|---|
| 1 | Implement the phase | Claude Code + skills | Yes |
| 2 | Code review | `/android-code-review` skill | Yes — must be ✅ Approved |
| 3 | Simplify | `/simplify` | Yes — runs after review |
| 4 | Commit & push | `git` | No |
| 5 | Merge to `Deploy_0.01` | `git merge --no-ff` | After 1–4 done |

> **Never skip step 2 or 3. `/simplify` runs AFTER `/android-code-review`, not before.** Review catches correctness; simplify cleans what passes review. Reversing the order polishes code that may still need structural fixes.

### Open each worktree as a separate AS window
1. **File → Open** → `.claude/worktrees/hX-phase-name`
2. Open as **new window** (not in current)
3. Let Gradle sync

> Run the app / debugger from the **main project window only**. Worktree windows are for editing and review. Do not fight over the emulator from two AS windows.

### Start Claude Code in the worktree
One terminal tab per active worktree:
```bash
cd ~/StudioProjects/Nyasa/.claude/worktrees/h1-error-hardening
claude
```

### Session prompt template
Paste at the start of every session:
```
Read ~/.claude/skills/nyasablog-architecture/SKILL.md first.
Read ~/.claude/skills/nyasablog-hardening/SKILL.md.
Current state: single :app module, 100% Compose, no XML UI.
Task: [specific task — e.g. "introduce AppError and rewrite safeApiCall"]
Follow the hardening skill patterns.
```

### Full workflow (exact commands)
```bash
# 1. Implement
cd .claude/worktrees/hX-phase-name && claude
# [paste prompt template + task description]

# 2. Review (must pass before /simplify)
#    /android-code-review — block on 🔁 Needs changes

# 3. Simplify
#    /simplify

# 4. Commit + push
git add .
git commit -m "hardening(hX): <description>"
git push origin hardening/hX-branch-name

# 5. Merge (from main project root, NOT from worktree)
git checkout Deploy_0.01
git merge --no-ff hardening/hX-branch-name -m "HX: <description>"
```

### Pre-merge automated gates
```bash
./gradlew assembleDebug

# AppError must not leak past data layer
grep -r "HttpException\|IOException" app/src/main/java/com/kanyandula/nyasa/ui/
# expect: no results

# No raw hex in composables (post-H3)
grep -r "#[0-9A-Fa-f]\{6\}" app/src/main/java/com/kanyandula/nyasa/ui/
# expect: no results

# No cross-feature imports (post-H5)
grep -r "import com.kanyandula.nyasa.feature" app/src/main/java/com/kanyandula/nyasa/feature/
# expect: no results
```

---

## Part 3 — Phase-by-phase execution

### H1 — Error & network hardening

> **The Gson → kotlinx-serialization swap is part of H1.** Treat as an explicit subtask with a DTO parse test gate before merge. Silently breaks DTOs if a field is missed.

**What Claude Code generates**
- `AppError` sealed interface (Offline, Timeout, Unauthorized, Forbidden, NotFound, Validation, Server, Unknown)
- `safeApiCall` rewrite mapping `HttpException`/`IOException` → `AppError`
- OkHttp retry interceptor (exponential backoff, GETs only, 3 attempts)
- `AuthInterceptor` wiring 401 → `SessionManager.invalidate()`
- Singleton `ImageLoader` sharing OkHttp with Retrofit
- `kotlinx-serialization-converter` replacing Gson across all DTOs

**Session prompt**
```
Task: introduce AppError sealed interface, rewrite safeApiCall,
      add OkHttp retry interceptor for GETs only, wire 401 to
      SessionManager.invalidate(), swap Gson for kotlinx-serialization.
```

**Manual verification**
- 401 → nav pops to Auth (test manually)
- Every DTO parses correctly against live API — run app and check Logcat
- Retry interceptor skips POST/PUT/DELETE (read the code)

**Review gate — must all pass:**
1. `./gradlew assembleDebug` passes
2. No `HttpException`/`IOException` in `ui/` (grep)
3. `safeApiCall` maps all HTTP codes to correct `AppError` variants
4. `AuthInterceptor` 401 path calls `SessionManager.invalidate()` — not just returns
5. Retry interceptor allow-list excludes POST/PUT/DELETE
6. All DTOs parse against live API — no Logcat errors

---

### H2 — HTML renderer (spike first)

> **Highest-risk item in the refactor.** Start Day 1 in parallel with H1. If the spike drags past 3 days, fall back to custom Jsoup — do not block H3/H4.

**Phase 2a — library evaluation spike**
Render 20 real posts from `/api/blog/list`:
1. `compose-richtext`
2. Markwon (if backend is markdown)
3. Custom Jsoup fallback

**Phase 2b — production renderer**
Hybrid: Compose for `<p>`, `<h*>`, `<blockquote>`, `<li>`, `<code>`; Coil for `<img>`; per-block `AndroidView<WebView>` for `<iframe>` / `<table>` / complex embeds.

**Non-negotiable before merge**
- 20-post Paparazzi golden suite passes
- WebView is **per-block**, never per-post (memory leak risk)
- Plain-text posts render with zero WebView instances

**Review gate**
1. Build passes
2. Paparazzi goldens pass on all 20 samples
3. No `AndroidView<WebView>` wraps entire body
4. Open 10 post detail screens back-to-back → LeakCanary clean
5. Plain-text posts: 0 WebView instances

---

### H3 — Design system tokens

Largely mechanical. Pull Stitch tokens into `designsystem/`, replace raw hex with semantic names.

**Session prompt**
```
Task: pull Stitch tokens into designsystem package. Replace all
      raw hex colors in ui/ with semantic tokens: surface,
      onSurface, brandPrimary, likeActive, categoryChip,
      readTimeText. Formalize NyasaTypography + NyasaSpacing
      (4pt grid). Dark-mode audit all screens.
```

**Review gate**
1. Build passes
2. Zero raw hex in `ui/` (grep)
3. Dark mode: every screen reviewed on dark emulator — no invisible text
4. `NyasaTypography` used consistently — no hardcoded `sp`
5. `NyasaSpacing` 4pt grid — no hardcoded `dp` padding

---

### H4 — Comments persistence

**Session prompt**
```
Task: add CommentEntity(pk, post_slug FK, body, username,
      date_created) to Room with CASCADE on BlogPost deletion.
      Write-through optimistic insert + delete using
      OptimisticAction<S> helper in ui/components.
```

Unify Like and Bookmark onto `OptimisticAction` at the same time.

**Review gate**
1. Build passes
2. `CommentEntity` FK has CASCADE delete
3. `OptimisticAction` rollback fires on network error (disable wifi mid-comment)
4. `comment_count` on `BlogPost` stays in sync
5. Like + Bookmark now use `OptimisticAction` — no ad-hoc toggle logic

---

### H5 — Modularization

> **Must wait until H1–H4 are all merged to `Deploy_0.01`.** Moving classes while contracts are still changing means resolving the same conflicts twice.

**Extraction order (least → most risky)**
1. `:core:common` — `AppError`, `Resource`, `UiEvent`, dispatchers. No Hilt.
2. `:core:designsystem` — theme, tokens. No Hilt.
3. `:core:network` — Retrofit, OkHttp, `safeApiCall`. Hilt module moves too.
4. `:core:database` — Room, DAOs, entities. Hilt module moves too.
5. `:core:domain` — repository interfaces, use cases. No Hilt bindings.
6. `:core:data` — `RepositoryImpl`s, `RemoteMediator`s.
7. `:core:session` — `SessionManager`.
8. `:feature:auth` — first feature module (good test case).
9. `:feature:feed`, `:feature:post`, `:feature:create`, `:feature:profile`, `:feature:bookmarks`.

**Per-module session prompt**
```
Read ~/.claude/skills/module-extraction-checklist/SKILL.md.
Task: extract :core:common from :app.
Move: AppError, Resource, UiEvent, ConnectivityObserver, dispatchers.
Create: core/common/build.gradle.kts with correct dependencies.
Update: all import statements in :app.
Verify: ./gradlew assembleDebug passes.
```

> **Never move AND refactor in the same commit.** Move first → verify build → then refactor. Mixing them makes Hilt errors impossible to trace.

**Hilt-move rule**
1. Move the class file to the new module
2. Move the `@Module`/`@InstallIn` class with it in the **same commit**
3. Add required Hilt deps in the new module's `build.gradle.kts`
4. `./gradlew assembleDebug` immediately — do not batch multiple module moves

**Review gate**
1. Build passes (debug + release)
2. No feature imports another feature (grep)
3. Every feature has an `:api` submodule containing only its `Navigator` interface
4. `:core:data` is the only module that imports `:core:network` + `:core:database`
5. Detekt module-boundary rule added and green in CI

---

## Part 4 — Merging completed branches

### Merge sequence
Order matters — H1 establishes the contract everything else depends on.

| Step | Action | Branch |
|---|---|---|
| 1 | Merge H1 | `hardening/h1-error` |
| 2 | Rebase H2, H3, H4 onto `Deploy_0.01` | all three |
| 3 | Merge H2 | `hardening/h2-html-renderer` |
| 4 | Merge H3 | `hardening/h3-tokens` |
| 5 | Merge H4 | `hardening/h4-comments` |
| 6 | Rebase H5 onto `Deploy_0.01` | `hardening/h5-modularize` |
| 7 | Merge H5 | `hardening/h5-modularize` |
| 8 | H6–H9 sequentially, each rebased before merge | — |

### Merge commands (from main project root, not worktree)
```bash
git checkout Deploy_0.01
git merge --no-ff hardening/h1-error \
  -m "H1: AppError, safeApiCall, OkHttp retry, ImageLoader, Gson swap"

cd .claude/worktrees/h2-html-renderer && git rebase Deploy_0.01
cd .claude/worktrees/h3-design-tokens && git rebase Deploy_0.01
cd .claude/worktrees/h4-comments-room && git rebase Deploy_0.01

git checkout Deploy_0.01
git merge --no-ff hardening/h2-html-renderer -m "H2: PostBodyRenderer, Paparazzi goldens"
git merge --no-ff hardening/h3-tokens         -m "H3: Stitch tokens, semantic colors"
git merge --no-ff hardening/h4-comments       -m "H4: CommentEntity, OptimisticAction"

cd .claude/worktrees/h5-modularization && git rebase Deploy_0.01
git checkout Deploy_0.01
git merge --no-ff hardening/h5-modularize -m "H5: Multi-module split, Navigator contracts"
```

### Realistic conflict hotspots

| Conflict | Files | Resolution |
|---|---|---|
| H1 vs H3 | `ErrorState.kt`, any error-showing composable | Take H1 logic + H3 token names. Usually different lines. |
| H5 renames | Any file moved between packages | `git diff --diff-filter=R` shows renames Git tracked. |
| Hilt modules | `di/` files | Move `@Module` + `@InstallIn` together. Never split. |

For visual conflict resolution: **VCS → Git → Resolve Conflicts** (AS three-way merge editor).

### Cleanup
```bash
git worktree remove .claude/worktrees/hX-phase-name
git branch -d hardening/hX-branch
```
Close the AS window for that worktree **first**, else AS throws a confusing error.

---

## Part 5 — Android Studio + worktrees

### RAM budget
| Windows | RAM | Verdict |
|---|---|---|
| 1 (main only) | 3–4 GB | Always fine |
| 2 (main + 1 worktree) | 5–7 GB | Comfortable on 16 GB |
| 3 | 8–10 GB | Marginal on 16 GB |
| 4+ | 12+ GB | Don't. Rotate. |

**On an 8 GB Mac: one active worktree at a time.**

### What AS is good for
- `Git → Log` (works in worktree windows)
- Three-way merge editor
- Annotate / blame

### Do in the terminal, not AS
- `git fetch`/`pull`/`rebase`/`merge`
- `git worktree` operations
- Anything touching multiple branches

> Do **not** use AS's branch switcher to check out a branch already checked out in another worktree. Git refuses, AS shows a confusing error.

### File-sync trap
When Claude Code writes a file in a worktree, AS's filesystem watcher catches it — but there's a brief window where AS's in-memory state diverges from disk.

- Let Claude Code finish before editing in AS
- If AS shows stale content: **File → Synchronize** (or `⌘⇧A → "Synchronize"`)
- Never edit the same file in Claude Code terminal and AS at the same time

---

## Part 6 — Quick reference

### Git worktree commands
| Command | Action |
|---|---|
| `git worktree list` | Show worktrees + branches |
| `git worktree add <path> <branch>` | Create worktree + branch |
| `git worktree add <path> -b <branch> <base>` | From specific base |
| `git worktree remove <path>` | Remove (branch kept) |
| `git worktree prune` | Clean stale refs |
| `git branch -d <branch>` | Delete merged branch |

### Skills reference
| Skill | Use when |
|---|---|
| `nyasablog-architecture` | Start of every session |
| `nyasablog-hardening` | Start of every session |
| `module-extraction-checklist` | Every H5 module extraction |
| `android-code-review` | Pre-merge review on every diff |

### Open questions blocking phases
| Question | Blocks | Answer via |
|---|---|---|
| Body = HTML or Markdown? | H2 library | Fetch one post, inspect `body` |
| `/api/blog/list` = cursor or page? | H5 `RemoteMediator` | Check response for cursor fields |
| Stitch CLI available? | H3 automation | Stitch project settings |
| Follows/notification backend timeline? | H10 | Backend team |

---

_Last updated: 2026-04-15. Source of truth for hardening execution. Companion to `COMPOSE_REFACTOR.md` (architecture)._
