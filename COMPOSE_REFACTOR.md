# NyasaBlog — Architecture Reference & Hardening Plan

Living reference for post-Phase-9 hardening, modularization, and feature expansion. Companion to `REFACTORING_PLAN.md`.

- **Status:** Compose migration **complete** (Phase 9 landed). Focus now: modularization, observability, HTML renderer, adaptive layout, tests.
- **Target branch:** `Deploy_0.01`
- **Backend:** `https://nyasablog.com/api/` (unchanged)
- **Min/Target SDK:** 24 / 35 · **Kotlin:** 1.9.24 · **Java:** 17

## Current State (verified 2026-04-15)

- **UI:** 100% Jetpack Compose. No layout XML, no Fragments, no navigation XML remain. Resource XML is limited to launcher icons, fonts, themes, colors, strings, dimens.
- **Navigation:** `navigation-compose` — `AuthNavGraph.kt`, `MainNavGraph.kt`, hosted by `MainActivity.kt`.
- **Module layout:** single `:app` module. Package structure under `com.kanyandula.nyasa`:
  ```
  ui/{auth, main/{blog, account, create_blog}, navigation, components, theme}
       each feature has composables/ + state/ subpackages
  domain/{repository, usecase/{auth, blog, account, createblog, comment, category, profile}}
  repository/{auth, main}     api/{auth, main}     persistance/
  di/{auth, main}             models/              session/    util/
  ```
- **Done:** StateFlow ViewModels, use cases, Paging 3 + RemoteMediator, reactive `ConnectivityObserver`, SafeArgs replaced by Compose nav, Compose screens across every feature.
- **Not yet done** (this document's scope): multi-module split, `AppError` unification, HTML body renderer, adaptive/tablet layout, WorkManager uploads, observability, Paparazzi/Turbine test stack.

---

## 1. Feature Inventory

Derived from the live site audit and the `com.kanyandula.nyasa` codebase.

| Group | Features | Status |
|---|---|---|
| Auth | Login, Register (email/username/password/password2), token session, Welcome | Built (XML) |
| Auth — gaps | Forgot-password backend wiring | **Deferred** |
| Feed | Paginated list (auth-gated), search, ordering, category filter, infinite scroll | Built (Paging 3) |
| Feed — additions | Trending ranked rail (mirrors web "Trending Now") | **New** |
| Taxonomy | 10 categories (Culture, Education, Entertainment, Entrepreneurship, Health, Lifestyle, Opinion, Sports, Technology, Tourism), tags | Built |
| Post | Detail: title, body (HTML), cover, read_time, view/like/comment counts, author avatar; author-only edit/delete | Built |
| Social | Like toggle, Bookmark toggle, Bookmarks list, flat comments (list/create/delete own) | Built |
| Profile | Public author profile by username, self account props, profile update, change password | Built |
| Create | Multipart create/update with image (CanHub picker + Compressor) | Built |
| Not present | Follows, notifications, nested comments, social auth, push | **Parked** |

---

## 2. Module & Dependency Graph

Multi-module Gradle. Dependency rule: `app → feature:* → core:domain + core:designsystem + core:ui`. Only `core:data` depends on `core:network` + `core:database`. **Feature modules never import each other** — cross-feature navigation goes through `*Navigator` contracts (§9A).

```
:app                      NyasaApplication, MainActivity, root NavHost, DI wiring
:core:designsystem        Compose theme, tokens (from Stitch), semantic colors
:core:ui                  Shared composables: PostCard, AuthorChip, EmptyState, ErrorState, PagingFooter
:core:common              Resource<T>, UiEvent, AppError, dispatchers, ConnectivityObserver
:core:network             Retrofit, OkHttp, AuthInterceptor, safeApiCall, DTOs, ImageLoader
:core:database            Room DB, DAOs, entities (AuthToken, AccountProperties, BlogPost, CommentEntity, RemoteKeys)
:core:datastore           DataStore Preferences (theme, settings); EncryptedSP for token only
:core:domain              Repository interfaces + UseCases
:core:data                RepositoryImpls, RemoteMediators, mappers
:core:session             SessionManager (StateFlow<AuthToken?>)
:core:work                WorkManager workers (upload, sync, push registration)
:core:analytics           AnalyticsTracker interface + Firebase impl
:feature:auth             welcome / login / register / forgot
:feature:feed             feed + trending + category chips
:feature:post             detail + comments + HTML renderer
:feature:create           create / edit
:feature:bookmarks
:feature:profile          account + public author profile + edit + change-password
```

Each feature module exposes an `:api` submodule containing only its `*Navigator` + `*EntryPoint` interface. `:app` provides implementations.

---

## 3. API Contract

Base `https://nyasablog.com/api/` — auth header: `Authorization: Token <token>`.

### Auth (`NyasaBlogApiAuthService`)
| Method | Path | Body | Returns |
|---|---|---|---|
| POST | `account/login` | username, password | `LoginResponse` (token) |
| POST | `account/register` | email, username, password, password2 | `RegistrationResponse` |

### Main (`NyasaBlogApiMainService`)
| Method | Path | Body / Query | Returns |
|---|---|---|---|
| GET | `account/properties` | — | `AccountProperties` |
| PUT | `account/properties/update` | email, username | `GenericResponse` |
| PUT | `account/change_password/` | old_password, new_password, confirm_new_password | `GenericResponse` |
| GET | `account/profile/{username}` | — | `UserProfileResponse` |
| PUT | `account/profile/update/` | bio, location, website, twitter, facebook, instagram, linkedin | `UserProfileResponse` |
| GET | `blog/categories/` | — | `List<CategoryResponse>` |
| GET | `blog/tags/` | — | `List<TagResponse>` |
| GET | `blog/list` | search, ordering, page, category?, status? | `BlogListSearchResponse` |
| GET | `blog/{slug}/` | — | `BlogSearchResponse` |
| GET | `blog/{slug}/is_author` | — | `GenericResponse` |
| POST | `blog/create` (multipart) | title, body, image?, category?, tags? | `BlogCreateUpdateResponse` |
| PUT | `blog/{slug}/update` (multipart) | title, body, image?, category?, tags? | `BlogCreateUpdateResponse` |
| DELETE | `blog/{slug}/delete` | — | `GenericResponse` |
| POST | `blog/{slug}/like/` | — | `LikeResponse` |
| POST | `blog/{slug}/bookmark/` | — | `BookmarkResponse` |
| GET | `blog/bookmarks/` | — | `List<BlogSearchResponse>` |
| GET | `blog/{slug}/comments/` | — | `List<CommentResponse>` |
| POST | `blog/{slug}/comments/create/` | body | `CommentResponse` |
| DELETE | `blog/comments/{pk}/delete/` | — | `GenericResponse` |

---

## 4. Data Models

### Room entities
- **AuthToken**(`account_pk` PK, `token`) — FK→AccountProperties CASCADE
- **AccountProperties**(`pk` PK, email, username, bio?, location?, website?, twitter?, facebook?, instagram?, linkedin?, profile_image?)
- **BlogPost**(`pk` PK, `slug` unique idx, title, body, image, date_updated, username, category?, tags?, reading_time?, view_count?, like_count?, comment_count?, author_avatar?)
- **CommentEntity**(`pk` PK, `post_slug` FK→BlogPost CASCADE idx, body, username, date_created) — **new**
- **RemoteKeys**(`slug` PK, prevKey?, nextKey?) — for Paging 3 RemoteMediator

### Non-persisted DTOs
`UserProfile`, `Category`, `Tag`, `LikeResult`, `BookmarkResponse`.

---

## 5. Screen Map & Navigation

Single root `NavHost` with nested graphs using Kotlin Serialization routes (Nav 2.8+). Session-gated.

```
RootGraph
├─ AuthGraph   Welcome → Login / Register / ForgotPassword
└─ MainGraph   (gated on SessionManager.token != null)
   ├─ FeedGraph    Feed → PostDetail(slug) → AuthorProfile(username)
   │                  Feed ↔ Bookmarks, Feed ↔ Search
   ├─ CreateGraph  CreatePost / EditPost(slug)
   └─ AccountGraph Account → EditAccount / ChangePassword / Settings
```

Bottom bar tabs: **Feed · Create · Account**. Bookmarks reached from Feed toolbar and Account.

### Deep links (App Links, verified via `assetlinks.json`)
- `https://nyasablog.com/post/{slug}`
- `https://nyasablog.com/u/{username}`

Custom scheme `nyasablog://` retained only as fallback for internal notifications.

---

## 6. State Management

- `BaseViewModel<S : UiState>` exposes `StateFlow<S>` + `SharedFlow<UiEvent>`; UDF via `onEvent(intent)`.
- **Feed**: Paging 3 `Pager<Int, BlogPost>` + existing `BlogRemoteMediator`. Filters:
  ```kotlin
  combine(query, category).distinctUntilChanged()
    .flatMapLatest { Pager(...).flow }
    .cachedIn(vm)
  ```
  `LazyListState` keyed on `(query, category)` so scroll resets correctly.
- **Post detail**: combined `Flow` of `(post, comments, isAuthor, isLiked, isBookmarked)` from Room; optimistic like/bookmark toggles.
- **Create/Edit**: `Saver`-backed form state; upload progress via `WorkInfo` flow (see §10).
- **Auth**: sealed per-field validation errors.
- **Session**: `SessionManager.token: StateFlow<AuthToken?>` gates nav at root. 401 from any call triggers `SessionManager.invalidate()`.

---

## 7. Error Handling

Unified model at the network boundary — never leak `HttpException`/`IOException` past `:core:data`.

```kotlin
sealed interface AppError {
  data object Offline : AppError       // ConnectivityObserver.isOnline = false
  data object Timeout : AppError
  data object Unauthorized : AppError  // 401 → SessionManager.invalidate()
  data object Forbidden : AppError     // 403
  data object NotFound : AppError      // 404
  data class Validation(val fields: Map<String, String>) : AppError  // 400 DRF
  data class Server(val code: Int) : AppError                        // 5xx
  data class Unknown(val cause: Throwable?) : AppError
}
```

### Retry policy
- OkHttp interceptor: exponential backoff (250ms → 1s → 4s, 3 attempts) **for idempotent GETs only** on `Timeout` / 5xx.
- **Never retry** POST/PUT/DELETE at the HTTP layer — avoids double-likes, double-comments.
- Upload retries live in WorkManager (§10).

UI consumes `AppError` via single `ErrorMessageMapper` in `:core:ui` → string resources.

---

## 8. Performance

### Image pipeline
Singleton `ImageLoader` in `:core:network` sharing OkHttp with Retrofit:

```kotlin
ImageLoader.Builder(ctx)
  .okHttpClient(sharedOkHttp)
  .memoryCache { MemoryCache.Builder(ctx).maxSizePercent(0.25).build() }
  .diskCache  { DiskCache.Builder().directory(ctx.cacheDir.resolve("img"))
                  .maxSizeBytes(100L * 1024 * 1024).build() }
  .respectCacheHeaders(false)
  .build()
```

### Feed prefetch
Observe `firstVisibleItemIndex + 10` in `LazyColumn`; enqueue Coil requests for upcoming covers with explicit `.size(w, h)` (feed card dimensions known — avoids decoding 4 MP originals).

---

## 9. Design System

Source of truth: Stitch tokens exported to Kotlin at build time. Exposed via `LocalNyasaTheme` composition local.

- **NyasaTypography** — display / headline / title / body / label (5 sizes each)
- **NyasaSpacing** — 4-pt grid: `xs 4, s 8, m 16, l 24, xl 32, xxl 48`
- **Semantic colors only** — `surface`, `onSurface`, `brandPrimary`, `likeActive`, `categoryChip`, `readTimeText`, etc. **Never reference hex from feature modules.**
- Dark mode first-class (matches web nav affordance).

### Stitch references
See `memory/reference_stitch_designs.md` for project ID + screen IDs.

---

## 10. Background Work & Notifications

`:core:work` module (WorkManager + Hilt):

| Worker | Trigger | Notes |
|---|---|---|
| `UploadBlogPostWorker` | create/edit submit | Resumable multipart; survives process death; screen dismissable during upload |
| `SyncBookmarksWorker` | periodic 6h, requires network | Refreshes bookmarks list |
| `PushTokenRegistrationWorker` | on login / token refresh | No-op until backend exists |

**FCM slot:** `:core:notifications` ships a no-op `PushMessageHandler` today; implementation swapped when backend is ready.

---

## 11. Tablet / Adaptive Layout

Material3 `WindowSizeClass` from `MainActivity`, propagated via `CompositionLocal`.

| Class | Feed | Post | Create |
|---|---|---|---|
| Compact (phone) | single-pane, bottom bar | full screen | full screen |
| Medium (foldable / small tablet) | list-detail pane scaffold, `NavigationRail` replaces bottom bar | pane 2 | modal bottom sheet |
| Expanded (tablet) | feed 2-col grid + detail + trending rail | pane 2 | side sheet |

Use `androidx.compose.material3.adaptive:adaptive-navigation` (`ListDetailPaneScaffold`). Post selection on tablet = pane switch, not nav event — `PostNavigator` contract abstracts over both.

---

## 12. HTML / Rich-Text Rendering

**Highest project risk.** Backend body contains `<p>`, `<h2>`, `<img>`, `<a>`, `<blockquote>`, `<pre><code>`, likely `<iframe>` (YouTube).

### Hybrid strategy

```
PostBodyRenderer(html):
  Jsoup → List<BlockNode>
  for each block:
    P / H* / BLOCKQUOTE / LIST / CODE → Compose AnnotatedString
    IMG                               → Coil AsyncImage
    IFRAME / embed / <table>          → AndroidView<WebView> sized by aspect ratio
```

WebView is **per-block**, never per-post. Library evaluation order:
1. `compose-richtext` (jeziellago) — if markdown-ish works
2. `Markwon` — if backend exposes markdown
3. Custom Jsoup + renderer — fallback

Ship with golden snapshot tests on 20 real production posts before M3 lands.

---

## 13. Optimistic UI Standard

Codified as `OptimisticAction<S>` helper in `:core:ui`:

```
1. Reduce state with predicted outcome  (isLiked = !isLiked, like_count ± 1)
2. Emit SideEffect → repository call
3. On success: no-op (already applied)
4. On failure: reduce inverse + emit ShowToast(AppError)
```

| Action | Strategy |
|---|---|
| Like | Optimistic toggle + rollback |
| Bookmark | Optimistic toggle + rollback |
| Comment create | Insert locally with temp pk, replace with server pk or rollback |
| Comment delete | Remove locally, restore on failure |
| Post edit | **Pessimistic** — too much state to roll back cleanly |

---

## 14. Observability

- **Firebase Crashlytics** — crash reporting
- **Firebase Analytics** — user behavior (single vendor; no Amplitude duplication)
- **Firebase Performance Monitoring** — cold start + feed-scroll jank (key retention metrics for a reading app)
- **Timber** — structured logging, `CrashlyticsTree` in release

Events flow through an `AnalyticsTracker` interface in `:core:analytics` — never called directly from ViewModels. Keeps VM tests Firebase-free and allows vendor swap.

---

## 15. Feature Flags

`FeatureFlags` interface, `BuildConfig`-backed for M1–M6. Migrate to **Firebase Remote Config** in M7 when rollout control is needed. Don't over-invest before there's a growth team to use it.

---

## 16. Testing

Phase 10 stack:

| Layer | Tool |
|---|---|
| Coroutines/Flow | `kotlinx-coroutines-test`, Turbine |
| Network | MockWebServer |
| Mocks | MockK |
| Assertions | Truth |
| ViewModel | Fakes + Turbine on `StateFlow` |
| Paging | `paging-testing` |
| Compose UI | `compose-ui-test-junit4` |
| Screenshots | Paparazzi (HTML renderer goldens are non-negotiable) |
| Leak detection | LeakCanary (debug) |

---

## 17. Implementation Order

### Shipped (Phases 1–9)
- Compose UI across all features · Paging 3 + RemoteMediator · SafeArgs → Compose nav · reactive `ConnectivityObserver` · StateFlow ViewModels · use-case layer · Hilt DI · Room caching · Auth/Feed/Detail/Create/Account/Bookmarks all functional.

### Next milestones
1. **H1 — Error & network hardening**: introduce `AppError`, rewrite `safeApiCall`, OkHttp retry interceptor (GETs only), 401 → `SessionManager.invalidate()` wiring, singleton `ImageLoader` sharing OkHttp. **Includes Gson → kotlinx-serialization swap** — gate on a DTO parse test against the live API before merge; silently breaks DTOs if a field is missed.
2. **H2 — HTML renderer**: `PostBodyRenderer` (Jsoup + Compose + per-block WebView fallback), 20-post Paparazzi golden suite. Highest-risk item — do first.
3. **H3 — Design-system tokens**: pull Stitch exports into `designsystem` package; replace raw colors with semantic tokens; formalize `NyasaTypography` + `NyasaSpacing`; dark-mode audit.
4. **H4 — Comments persistence**: add `CommentEntity` to Room; write-through optimistic insert/delete; `OptimisticAction` helper in `ui/components`.
5. **H5 — Modularization**: split `:app` into `:core:*` + `:feature:*` per §2. Do after H1–H4 stabilize so the contract surface is clear.
   - **Extraction order** (least to most risky): `:core:common` → `:core:designsystem` → `:core:network` → `:core:database` → `:core:domain` → `:core:data` → `:core:session` → `:feature:auth` → remaining features.
   - **Hilt-move rule:** move the `@Module`/`@InstallIn` class together with its bindings into the same new Gradle module in a **single commit**. Run `./gradlew assembleDebug` before moving the next module. Never move + refactor in the same commit — Hilt errors become untraceable.
6. **H6 — Background uploads**: `:core:work` + `UploadBlogPostWorker`; create/edit flow dismissable during upload.
7. **H7 — Adaptive layout**: `WindowSizeClass`, `ListDetailPaneScaffold` for tablet/foldable; `NavigationRail` replaces bottom bar on Medium+.
8. **H8 — Observability**: Crashlytics + Analytics + Performance behind `AnalyticsTracker` interface; Timber `CrashlyticsTree`.
9. **H9 — Test stack (Phase 10)**: Turbine, MockWebServer, MockK, Paparazzi, `paging-testing`, Compose UI tests.
10. **H10 — Growth (backend-dependent)**: forgot-password wiring, follows, FCM notifications, verified App Links.

---

## 18. Recommended Libraries

| Library | Justification |
|---|---|
| Compose BOM + Material3 | Target stack |
| `navigation-compose` 2.8 + kotlinx-serialization | Type-safe routes, replaces SafeArgs |
| Hilt + `hilt-navigation-compose` | Existing; scoped VMs per nav entry |
| Paging 3 + `paging-compose` | `BlogRemoteMediator` already built |
| Room 2.6 + KSP | Existing |
| Retrofit + OkHttp + `kotlinx-serialization-converter` | Replace Gson — smaller, faster, multiplatform-ready |
| Coil 2 | Compose-native, shares OkHttp |
| Jsoup (+ `compose-richtext` or Markwon) | HTML rendering (§12) |
| CanHub ImagePicker + zelory/Compressor | Proven in repo |
| DataStore Preferences | Replace raw SharedPrefs (EncryptedSP kept for token) |
| WorkManager (Hilt) | Upload + sync workers |
| Firebase Crashlytics / Analytics / Performance | Observability |
| Turbine, MockWebServer, MockK, Truth, Paparazzi | Phase 10 |
| Detekt + Spotless (ktlint) | Pre-commit enforced |
| Timber | Structured logging |
| LeakCanary | Debug-only |

---

## 19. Risk Register

| # | Risk | Mitigation |
|---|---|---|
| 1 | **HTML rendering** | §12 hybrid renderer + 20-post golden snapshot suite before M3 ship |
| 2 | **Session edge cases** (token expiry, logout consistency, 401 mid-scroll) | 401 interceptor → `SessionManager.invalidate()` → nav pops to Auth; instrumented test: token wiped mid-scroll must gracefully unmount feed |
| 3 | **Paging × filters** | `combine(query, category).distinctUntilChanged().flatMapLatest { Pager }.cachedIn(vm)`; explicit `pagingItems.refresh()` on filter change; `LazyListState` keyed on `(query, category)` |
| 4 | **Upload reliability** | WorkManager, not OkHttp retries; resumable multipart; surface `WorkInfo` progress |
| 5 | **Module boundary creep** | Feature modules forbidden from cross-importing; enforce via `:api` submodules + `*Navigator` contracts |

---

## 20. Open Questions

- Does backend emit markdown or rendered HTML for `body`? (Determines §12 library choice.)
- Is `read_time` computed server-side (confirmed in API) or should client fallback compute from word count?
- Does `/api/blog/list` support cursor-based pagination or page-only? (Affects `RemoteMediator` keying.)
- Timeline for backend follow / notification endpoints — blocks M8.
- Stitch export automation — CLI available, or manual JSON drop?

---

---

## 21. Appendix — Worktree Execution Workflow

Each hardening phase runs in an isolated git worktree so H1–H9 can progress in parallel without Gradle/AS interference. Full procedure lives in `NyasaBlog_Worktree_Refactor_Guide.docx` on Desktop; summary below.

### Setup (once)
```bash
mkdir -p .claude/worktrees
echo ".claude/worktrees/" >> .gitignore

git worktree add .claude/worktrees/h1-error-hardening  hardening/h1-error
git worktree add .claude/worktrees/h2-html-renderer    hardening/h2-html-renderer
git worktree add .claude/worktrees/h3-design-tokens    hardening/h3-tokens
git worktree add .claude/worktrees/h4-comments-room    hardening/h4-comments
git worktree add .claude/worktrees/h5-modularization   hardening/h5-modularize
# (+ h6–h9 as phases become active)

for dir in .claude/worktrees/*/; do cp local.properties "$dir"local.properties; done
```

In Android Studio: **File → Project Structure → Modules → `.claude` → Mark as Excluded** (stops double-indexing).

### Per-phase loop (enforced order)
1. **Implement** — Claude Code session in the worktree with architecture/hardening skills loaded.
2. **Code review** — `/android-code-review` against `Deploy_0.01`. Must return ✅ / 💬 Approved. 🔁 blocks the merge.
3. **Simplify** — `/simplify` **only after review passes** (cleans verified-correct code).
4. **Commit + push** — `hardening/hX-*` branch.
5. **Merge** — `git merge --no-ff` from main project root, not from the worktree.

**Rule:** review always precedes simplify. Reversing the order polishes code that may still need structural changes.

### Merge sequence
H1 first (establishes `AppError` contract) → rebase H2/H3/H4 onto updated `Deploy_0.01`, merge in any order → rebase H5 → merge H5 → H6–H9 sequentially, each rebasing before merge.

### Pre-merge automated gates
```bash
./gradlew assembleDebug
grep -r "HttpException\|IOException" app/src/main/java/com/kanyandula/nyasa/ui/   # expect none
grep -r "#[0-9A-Fa-f]\{6\}" app/src/main/java/com/kanyandula/nyasa/ui/            # expect none (post-H3)
grep -r "import com.kanyandula.nyasa.feature" app/src/main/java/com/kanyandula/nyasa/feature/  # expect none (post-H5)
```

### RAM budget (Android Studio)
| Windows open | RAM | Verdict |
|---|---|---|
| 1 (main) | 3–4 GB | Fine |
| 2 (main + 1 worktree) | 5–7 GB | Comfortable on 16 GB |
| 3 | 8–10 GB | Marginal |
| 4+ | 12+ GB | Don't — rotate |

Run the app / debugger from the **main project window only**. Worktree windows are for editing and review.

### Cleanup
```bash
git worktree remove .claude/worktrees/hX-phase-name
git branch -d hardening/hX-branch
```
Close the AS window for that worktree first.

### Supporting skills (create before H1)
- `nyasablog-architecture` — machine-readable version of this doc (module names, `AppError` contract, dependency rules, `Navigator` pattern)
- `nyasablog-hardening` — `AppError` mapping, `OptimisticAction` shape, `CommentEntity` schema, Hilt-safe module extraction
- `module-extraction-checklist` — repeatable per-module checklist used throughout H5

Session prompt template:
```
Read /mnt/skills/user/nyasablog-architecture/SKILL.md first.
Read /mnt/skills/user/nyasablog-hardening/SKILL.md.
Current state: single :app module, 100% Compose, no XML UI.
Task: [specific task]
Follow the hardening skill patterns.
```

---

_Last updated: 2026-04-15. Update this file as decisions are made; it is the source of truth for the Compose refactor._
