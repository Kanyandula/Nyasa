# H7 Phase 2 — List-Detail Architecture (Design Spec)

**Date:** 2026-04-28 (architecture corrected 2026-04-29)
**Status:** Approved — Option β (post-correction)
**Predecessors:** H7 Phase 1 (`b32beba`, PR #61) — `WindowClassifier`, `LocalWindow`, `NyasaSideRail`, `MainNavItem`.
**Companion doc:** `2026-04-28-h7-adaptive-tablet-layouts-design.md` — visual contract for Phases 2–4. This spec is the **architectural** contract for Phase 2; it does not redefine pixel-level appearance.

---

## 1. Goal

On Medium-class windows (≥ 600dp width per Phase 1's `WindowClassifier`), the `BLOG_FEED` and `BLOG_SEARCH` routes render as a list-and-detail layout via `androidx.compose.material3.adaptive:adaptive-navigation`'s `NavigableListDetailPaneScaffold`. On Small windows behavior is unchanged. Selection has **two backings**: on Small it lives in the `NavController` back stack (the existing `BLOG_DETAIL` route push, unchanged); on Medium it lives inside the scaffold's `ThreePaneScaffoldNavigator` (the `BLOG_DETAIL` route is not entered). The wrapper picks the path on `WindowClassifier.isMedium`; no other code branches on size.

After Phase 2, a tablet user reading on `nyasablog.com` can browse the feed and read posts side-by-side without losing list context. The bookmarks surface and all secondary destinations (Edit, Author, Create) stay full-screen at every size.

> **Architecture-correction note (2026-04-29):** an earlier draft of this spec described `NavigableListDetailPaneScaffold` as "tying the scaffold's pane navigator to a `NavController`." That is incorrect. The "Navigable" prefix names system-back integration via `BackHandler` only; the scaffold's `ThreePaneScaffoldNavigator` is independent of `NavController`. The original draft's §3.4 trick — having the `BLOG_DETAIL` composable render `Spacer` on Medium so the `BLOG_FEED` route's scaffold "showed underneath" — would never work, because `NavHost` only renders the top destination. This spec is the corrected design (Option β: nav on Small, scaffold-state on Medium).

## 2. Scope

### In scope

| Surface | Phase 2 behavior on Medium |
|---|---|
| `BLOG_FEED` | List + detail. Tap a post → on Medium, the wrapper updates the scaffold's internal slug and the detail pane fills; on Small, the existing nav to `BLOG_DETAIL` runs unchanged. |
| `BLOG_SEARCH` | Same wrapper; search-result-membership clear (see §6). |
| `BLOG_DETAIL` | Route body **unchanged**. On Small it renders full-screen as today. On Medium it is **never entered** — the wrapper intercepts list taps before `NavController.navigate()` fires and renders `BlogDetailRoute(slug)` directly inside the scaffold's detail pane. |

### Out of scope (deferred or unrelated)

- `BOOKMARKS` — stays single-pane on every size. Adoption deferred indefinitely; revisit only if usage data shows tablet users want list+detail bookmarks.
- `BLOG_EDIT` — stays full-screen on every size. Rich-text editor + image picker + form validation is too cramped at ~360dp pane width.
- `AUTHOR_PROFILE` — stays full-screen on every size. Logically a separate feed-like surface, not a peer of a single post.
- `CREATE` — stays full-screen on every size. Phase 4 owns "modal create on tablet" / "live preview pane" per the visual spec §5.2.
- `PostNavigator` contract — explicitly **not** introduced. The plan flagged it as "reassess when Phase 2 lands"; the two-path design routes through one wrapper that owns the branch, so there is still no second path worth abstracting over for one feature. Defer indefinitely.
- Process-death selection restore — selection survives rotation/fold via the scaffold navigator's default `Saver`; does not survive process death. Cheap to add later if users miss it; no `SavedStateHandle` plumbing in this phase.
- Three-pane layout (list + detail + meta column) — Phase 3, gated on Expanded class (≥ ~1100dp width). The visual spec §5.1 wireframe is the **Phase 3** target, not Phase 2.
- A custom `PaneScaffoldDirective` — Phase 2 uses Material's default `calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())`, which already gives the desired single-pane / dual-pane breakpoints (see §3.2).

## 3. Architecture

### 3.1 Core principle: two-path selection (nav on Small, scaffold-state on Medium)

`NavigableListDetailPaneScaffold` integrates with system back via `BackHandler`, **not** with `NavController`. Its `ThreePaneScaffoldNavigator` is a separate, independent state machine. The wrapper bridges these two state machines based on window size:

- **Small (< 600dp)**: scaffold not mounted. `BlogFeedAction.BlogClicked` propagates upward as today; the existing `navController.navigate(blogDetail(slug))` runs and `BLOG_DETAIL` renders full-screen. No behavior change vs. main.
- **Medium (≥ 600dp)**: scaffold mounted. The wrapper substitutes the list pane's `onBlogClicked` callback with one that calls `navigator.navigateTo(Detail, slug)` on the scaffold's internal navigator. The detail pane reads `navigator.currentDestination?.contentKey` and renders `BlogDetailRoute(slug)`. The existing `navController.navigate(blogDetail(slug))` is **not** called — the slug never enters the back stack on tablet.

The branch is a single `if (isMedium)` inside the wrapper. Phone and tablet share the upstream `BlogFeedAction.BlogClicked` event; the wrapper alone decides whether that event drives `NavController` or the scaffold navigator. There is no mirroring or syncing between the two state machines — only the wrapper picks one path or the other per render.

### 3.2 Material's default directive — three width tiers

| Width | `WindowSizeClass` | Scaffold behavior |
|---|---|---|
| < 600dp | `Compact` | Scaffold not mounted (early return). Existing phone path renders `BLOG_DETAIL` full-screen. |
| 600–839dp | `Medium` | Scaffold mounted; directive emits **single-pane**. List shows when no slug, detail shows when slug is set (visually swaps; identical UX to phone but with side rail). |
| ≥ 840dp | `Expanded` | Scaffold mounted; directive emits **dual-pane**. List + detail visible together. |

This means the Pixel Fold inner display (~840dp width) lands at the boundary and renders single-pane — matching the visual spec §5.1's "rail + single column reading view" — and the Pixel Tablet landscape (1280dp) renders dual-pane.

The `WindowClassifier` from Phase 1 (`isMedium` = width ≥ 600dp OR landscape height < 400dp) is consulted only as the gate for mounting the scaffold; the *intra-Medium* single-vs-dual decision is delegated to Material's directive.

### 3.3 Component diagram

```
┌─────────────────────────────────────────────────────────────────┐
│  MainActivity                                                   │
│    └─ NavHost                                                   │
│        └─ MAIN_GRAPH                                            │
│            └─ BLOG_GRAPH                                        │
│                ├─ BLOG_FEED                                     │
│                │   └─ BlogListDetailScaffold (isMedium →        │
│                │       intercepts BlogClicked, owns slug via    │
│                │       ThreePaneScaffoldNavigator)              │
│                │       ├─ listPane: BlogFeedScreen              │
│                │       └─ detailPane: BlogDetailRoute(slug)     │
│                ├─ BLOG_SEARCH ── (same wrapper)                 │
│                ├─ BLOG_DETAIL ── full-screen on Small only;     │
│                │                  not entered on Medium         │
│                └─ BLOG_EDIT ──── full-screen all sizes          │
└─────────────────────────────────────────────────────────────────┘
```

## 4. Files

### NEW — `:app`

`app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt`

Internal-visibility composable wrapper. Signature:

```kotlin
@Composable
internal fun BlogListDetailScaffold(
    onNavigateToDetailFullScreen: (slug: String) -> Unit,
    visibleSlugs: Set<String>,
    mode: FeedMode,
    listPane: @Composable (onBlogClicked: (String) -> Unit) -> Unit,
    detailPane: @Composable (slug: String, onClose: () -> Unit) -> Unit,
)
```

`onClose` is the wrapper-provided "leave the detail pane" callback — the caller's `detailPane` slot wires both `BlogDetailRoute.onNavigateBack` and `BlogDetailRoute.onDeleted` to it.

Behavior:
- If `!LocalWindow.current.isMediumWindowAsState().value` → renders `listPane(onBlogClicked = onNavigateToDetailFullScreen)`. No scaffold mounted; tap routes through `NavController` to `BLOG_DETAIL` exactly as today. The `detailPane` slot is **not** invoked on Small (the unchanged `BLOG_DETAIL` route renders the detail full-screen).
- Otherwise mounts `NavigableListDetailPaneScaffold` with `val navigator = rememberListDetailPaneScaffoldNavigator<String>(scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()))`. The scaffold navigator is the **single source of truth** for selection on Medium.
- List slot: `listPane(onBlogClicked = { slug -> scope.launch { navigator.navigateTo(Detail, slug) } })`. The caller's `onNavigateToDetailFullScreen` is intentionally **not** invoked on Medium.
- Detail slot: read `navigator.currentDestination?.contentKey`. If present, render `detailPane(slug, onClose = { scope.launch { navigator.navigateBack() } })`. If null, render `DetailPanePlaceholder()`. The caller wires `onNavigateBack = onClose` and `onDeleted = onClose` on the inner `BlogDetailRoute` so both top-bar back and delete-confirmed collapse the pane to placeholder rather than popping `BLOG_GRAPH`.
- Search detail-clear (§6.2) lives inside the wrapper and operates on `navigator` directly (no `NavController` interaction):
  ```kotlin
  val selected = navigator.currentDestination?.contentKey
  LaunchedEffect(visibleSlugs, selected, mode) {
      if (mode == FeedMode.Search && selected != null && selected !in visibleSlugs) {
          navigator.navigateBack()
      }
  }
  ```

`app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/DetailPanePlaceholder.kt`

~20 LOC composable. Vertically-centered icon + "Select a post to read" string. Uses `NyasaSpacing` tokens. No ViewModel dependency. No state.

### EDITED — `:app`

`app/src/main/java/com/kanyandula/nyasa/ui/navigation/MainNavGraph.kt` (lines 38–100)

Two small edits, no new routes, no removed routes:

1. **`BLOG_FEED` composable** — wrap the existing `BlogFeedScreen` call in `BlogListDetailScaffold(...)`. Pass `onNavigateToDetailFullScreen = { slug -> navController.navigate(Routes.blogDetail(slug)) }` (the existing nav call moves into this lambda). The `detailPane` slot reads `(slug, onClose) -> BlogDetailRoute(slug = slug, viewModel = vm, onNavigateBack = onClose, onEdit = { s -> navController.navigate(Routes.blogEdit(s)) }, onDeleted = onClose, onAuthorClick = { u -> navController.navigate(Routes.authorProfile(u)) })`. Pass `mode = FeedMode.Feed` and `visibleSlugs` collected from `vm.visibleSlugsFlow.collectAsStateWithLifecycle(emptySet()).value`.
2. **`BLOG_SEARCH` composable** — same wrapping pattern, `mode = FeedMode.Search`.

`BLOG_DETAIL` composable — **unchanged**. On Small, the existing route renders full-screen as today; on Medium, the wrapper intercepts taps so this route is never reached.

`app/src/main/java/com/kanyandula/nyasa/ui/main/blog/viewmodel/BlogViewModel.kt`

One new flow:

```kotlin
internal val visibleSlugsFlow: Flow<Set<String>>
```

Emits the set of slugs currently in the visible paging window. Implementation method deferred to the impl plan: either snoop `LazyPagingItems.itemSnapshotList` from the list pane, or hook a `RemoteMediator` callback. The spec commits to **the rule** (search detail-clear, §6), not the mechanism.

`app/build.gradle`

Add one dep:

```groovy
implementation 'androidx.compose.material3.adaptive:adaptive-navigation:1.0.0'
```

(Pin to whatever stable is current at implementation time; 1.0.0 is the floor — `NavigableListDetailPaneScaffold` is in 1.0.0+.)

### NEW — tests

`app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffoldTest.kt`

Compose UI tests, three cases (drive widths via `Modifier.size()` on a wrapping container):
- **411dp width (Compact):** scaffold not mounted; only `listPane` content rendered. Asserting that a fake `onNavigateToDetailFullScreen` is invoked when a list item is clicked verifies the Small path routes through `NavController`.
- **700dp width (Medium-narrow):** scaffold mounted single-pane; with no slug, list visible and `DetailPanePlaceholder` not shown; click a list item → wrapper updates the scaffold navigator, detail pane visible, list hidden (single-pane swap). `onNavigateToDetailFullScreen` is **not** invoked.
- **1280dp width (Expanded):** scaffold mounted dual-pane; with no slug, list + placeholder visible; click a list item → both panes show content; back press collapses to list + placeholder.

`app/src/test/java/com/kanyandula/nyasa/ui/main/blog/viewmodel/BlogViewModelVisibleSlugsTest.kt`

JVM unit test. Feed `BlogViewModel.pagingDataFlow` synthetic `PagingData<BlogPost>` via `paging-testing`; assert `visibleSlugsFlow` emits the expected slug set.

## 5. Existing utilities to reuse

- `core/designsystem/.../ui/theme/window/WindowClassifier.kt` — Phase 1 classifier; consumed via `LocalWindow.current.isMediumWindowAsState()`.
- `app/.../ui/main/blog/composables/BlogFeedScreen.kt`, `BlogDetailScreen.kt` — unchanged; rendered into scaffold slots.
- `app/.../ui/main/MainRouteComposables.kt:80–146` — `BlogDetailRoute` already takes `onNavigateBack: () -> Unit` and `onDeleted: () -> Unit` as parameters (it does **not** call `popBackStack` directly). On Small the unchanged `BLOG_DETAIL` route keeps wiring both to `navController.popBackStack()`. On Medium the wrapper's `onClose` (which calls `navigator.navigateBack()`) is wired to both. No refactor of `BlogDetailRoute` itself; only call-site rewiring.
- `app/.../ui/main/blog/viewmodel/BlogViewModel.kt` — already nav-graph-scoped to `BLOG_GRAPH`; both panes share it via `hiltViewModel(parentEntry)`. No re-scoping.
- The existing `BlogNavigationEvent.BlogDeleted → onDeleted()` handler at `MainRouteComposables.kt:101` is reused unchanged on Small. On Medium the wrapper's substituted callback (above) clears the scaffold's detail pane to placeholder.

## 6. Behavioral rules

### 6.1 Initial detail-pane state

When a tablet user lands on FEED or SEARCH and no slug is set in the scaffold's navigator, the detail pane shows `DetailPanePlaceholder` ("Select a post to read"). `ThreePaneScaffoldNavigator`'s default `Saver` keeps selection across configuration changes (rotation, fold/unfold). Selection does **not** survive process death — out of scope.

### 6.2 Search-specific detail-clear (Q4 D rule)

When the user changes the search query in `BLOG_SEARCH` and the previously-selected post is no longer in the new result set, the wrapper collapses the scaffold's detail pane (Medium only):

```kotlin
val selected = navigator.currentDestination?.contentKey
LaunchedEffect(visibleSlugs, selected, mode) {
    if (mode == FeedMode.Search && selected != null && selected !in visibleSlugs) {
        navigator.navigateBack()
    }
}
```

Operates on the scaffold navigator only — no `NavController` interaction. **Does not** apply to `BLOG_FEED`: scrolling the feed shouldn't clear the detail pane just because the visible window slid past the selected post. On Small the wrapper isn't mounted, so this rule is naturally inactive (the user is either still on `BLOG_SEARCH` or has navigated into a full-screen `BLOG_DETAIL` — the latter exits the search context entirely until back is pressed).

### 6.3 Edit / Author / Delete behavior on Medium

| Action | Behavior on Medium |
|---|---|
| Edit | `BlogDetailRoute.onEdit` calls `navController.navigate(blogEdit(slug))` → full-screen `EditBlogRoute`. Scaffold suspended; restored on back. |
| Author tap | `BlogDetailRoute.onAuthorClick` calls `navController.navigate(authorProfile(username))` → full-screen `AuthorProfileRoute`. Scaffold suspended; restored on back. |
| Delete confirmed | Wrapper-supplied `onClose` runs → `navigator.navigateBack()` collapses the detail pane to placeholder. List remains visible. |
| Top-bar back inside detail pane | Same `onClose` → collapses to placeholder. (System back is handled separately by the scaffold's `BackHandler` — see §6.4.) |

### 6.4 Back-press semantics

- **Medium with detail visible (single- or dual-pane):** the scaffold's `BackHandler` fires first and calls `navigator.navigateBack()` — the detail pane collapses to placeholder (or hides in single-pane mode, returning the list). No `NavController` interaction.
- **Medium list-only (no detail showing):** scaffold's `BackHandler` is inactive (nothing to pop in the navigator). Back falls through to `NavController` and pops `BLOG_GRAPH` exactly as on phone.
- **Small (`BLOG_DETAIL` full-screen):** scaffold isn't mounted; back pops `BLOG_DETAIL` via `NavController`. Unchanged from main.
- **Edit/Author/Create full-screen on any size:** back pops the full-screen route via `NavController`, returning the user to whatever scaffold state (or phone state) they came from.

## 7. Verification

### 7.1 Pre-merge automated gates (per project pre-commit hook)

```bash
cd .claude/worktrees/h7-phase2  # impl worktree
./gradlew detekt spotlessCheck lintDebug
./gradlew :app:test :app:connectedAndroidTest
./gradlew :app:assembleDebug
```

### 7.2 Manual device verification

Mandatory before merge. Identical pattern to Phase 1:

| Device / mode | Expected |
|---|---|
| Pixel 7 (411dp) | Phone path unchanged: tap → full-screen detail → back → feed. No scaffold artifacts. |
| Pixel Fold folded (~391dp) | Identical to Pixel 7. |
| Pixel Fold unfolded (~840dp) | Single-pane scaffold per directive. Tap → detail; back → list. Rotate fold/unfold mid-read → state preserved. |
| Pixel Tablet landscape (1280×800) | Dual-pane. Tap → detail pane fills; back collapses to list-only (placeholder visible); tap Edit → full-screen Edit; back → two-pane. |
| Pixel Tablet portrait (800×1280) | Single-pane (Medium under 840dp threshold); side rail visible; behaves like a phone with a rail. |
| Search query edit | Type query A → tap result → type query B that excludes A → detail pane clears to placeholder. Verify on tablet portrait + landscape. |
| Delete on tablet | Confirm dialog → after delete: detail pane shows placeholder, list still visible (Q3 nuance). |

### 7.3 Crashlytics + Timber

Already wired (H8). No new observability required for Phase 2.

## 8. Risks / open questions

- **`visibleSlugsFlow` implementation cost.** Snooping `LazyPagingItems` requires the list-pane composable to publish the visible slug set upward; `RemoteMediator` callback is cleaner but more code. The spec defers the choice to the implementation plan. **Risk: medium** — if neither approach is clean, fall back to recomputing membership inside the scaffold's `LaunchedEffect` from the most recent `PagingData` snapshot held by `BlogFeedScreen`'s `LazyPagingItems`.
- **`material3-adaptive-navigation` API stability.** Pin 1.0.0 stable at implementation time. **Risk: low** — the library has been stable since 2024-09; we use only `NavigableListDetailPaneScaffold` + `rememberListDetailPaneScaffoldNavigator`, both in 1.0.0. The scaffold's `BackHandler` integration is the only library-coordinated behavior; `NavController` interaction is hand-rolled in the wrapper.
- **Configuration-change ergonomics on the boundary.** A device hovering near 840dp (e.g., Pixel Fold) flipping between single- and dual-pane modes mid-read should preserve scroll position and selection. The scaffold navigator is `Saver`-backed and the list pane uses `LazyColumn` (already preserves scroll), so this should work — but worth verifying explicitly during device test.

## 9. Visual-spec reconciliation (follow-up doc PR)

This spec is paired with edits to the existing visual spec (`2026-04-28-h7-adaptive-tablet-layouts-design.md`):

- **§5.1** — clarify that the three-pane wireframe (list + body + meta) is the **Phase 3** target. Phase 2 ships two-pane (list + detail) on Expanded, single-pane on Medium below 840dp.
- **§7.2** — replace "selection in left pane → updates a `selectedPostId` `StateFlow` in the VM → detail pane recomposes" with: "On Medium, selection is owned by the scaffold's internal `ThreePaneScaffoldNavigator` (driven by `BlogListDetailScaffold`'s wrapper logic). On Small, selection is the existing `NavController.navigate(blogDetail(slug))` push to the unchanged `BLOG_DETAIL` route. The wrapper picks the path on `WindowClassifier.isMedium`; `NavigableListDetailPaneScaffold` integrates only with system back, not with `NavController`."

These two edits ship in a **separate doc-only PR** after this architecture spec is approved, to keep the diffs reviewable.

## 10. Phase rollout (recap from visual spec §9)

| Phase | Scope | Status |
|---|---|---|
| 1 | `WindowClassifier` + `NyasaSideRail` | ✅ shipped 2026-04-28 (`b32beba`, PR #61) |
| **2** | **`NavigableListDetailPaneScaffold` for Feed + Search** | **This spec** |
| 3 | Expanded grid + Editor's Picks / meta column | Pending |
| 4 | Modal Create sheet w/ live preview | Pending |

---

**Approval:** brainstorm transcript captured 2026-04-28; architecture corrected 2026-04-29 after self-review caught the `NavigableListDetailPaneScaffold` mischaracterization (Option β replacing the broken Option B). Next step: implementation plan via `superpowers:writing-plans`.