# H7 Phase 3a + 3b — Expanded-Class Editorial Layouts (Design Spec)

**Date:** 2026-05-02
**Status:** Approved (brainstorm transcript captured 2026-05-02)
**Predecessors:**
- H7 Phase 1 (`b32beba`, PR #61) — `WindowClassifier`, `LocalWindow`, `NyasaSideRail`, `MainNavItem`.
- H7 Phase 2 (`6f990f8`, PR #65) — `BlogListDetailScaffold`, `DetailPanePlaceholder`, `BlogFeedScreen.onVisibleSlugsChanged`.
- H7 Phase 2 cleanup (`fab0801`, PR #66) — `blogFeedRoute` and `BindCurrentUsernameToBlogVm` helpers.

**Companion doc:** `2026-04-28-h7-adaptive-tablet-layouts-design.md` — visual contract. This spec is the **architectural** contract for Phase 3a + 3b; it does not redefine pixel-level appearance.

---

## 1. Goal

On Expanded-class windows (≥ 840dp width per Material's `WindowSizeClass`), the `BLOG_FEED` route renders a full-width editorial layout — hero (one article) + 2×2 article grid + 320dp "Up next" rail — and the `BLOG_SEARCH` route renders a full-width 2-column results grid. On Compact (< 600dp) and Medium (600–839dp) behaviour is unchanged from Phase 2. After the user taps a post on Expanded, the layout transitions into Phase 2's existing dual-pane reading scaffold; pressing back returns to the editorial layout.

After Phase 3a + 3b ship, a Pixel Tablet landscape user opening the app sees an editorial home page with a featured hero, a 2×2 grid below, and an up-next rail on the right — instead of Phase 2's narrow list pane. Reading is still dual-pane.

## 2. Scope

### In scope (this PR)

| Surface | Phase 3 behaviour on Expanded |
|---|---|
| `BLOG_FEED` (Home) | Hero (full-width 16:9) + 2×2 `LazyVerticalGrid` + 320dp "Up next" rail. Tap → mounts the Phase 2 scaffold dual-pane and navigates the navigator into the slug. |
| `BLOG_SEARCH` | Sticky search top bar + filter chips + 2-column `LazyVerticalGrid` of results. No hero, no rail. Tap → same scaffold mount transition. |
| `BlogListDetailScaffold` (Phase 2 wrapper) | Gains an optional `expandedListPane` slot. When supplied AND the window is Expanded class AND no post is selected, the wrapper renders that slot directly and skips mounting `NavigableListDetailPaneScaffold`. |

### Out of scope (deferred)

- **Phase 3c**: Bookmarks Expanded layout (2-column horizontal-thumb cards). Separate spec / PR cycle.
- **Phase 3d**: Blog Detail three-pane (list + body + meta column). Visual spec §5.1 calls for it; ships in its own cycle.
- **Medium-class horizontal trending rail** (visual spec §4.1 "Foldable portrait" sub-treatment). Skipped per Q4 of brainstorm — most foldables actually report ≥ 840dp unfolded portrait (Pixel Fold reports `w852dp` per `dumpsys` Configuration), so they land in the Expanded path. Real Medium-class devices (small tablets in portrait, Chromebooks) keep Phase 2's single-column list. Revisit if real-world telemetry shows demand.
- **Editorial / curated rail content from a backend endpoint**. Deferred; the rail draws from the same `pagingDataFlow` (offset-sliced — see §3.1). Backend ranking endpoints (`-like_count`, `-bookmark_count`) remain unverified and unused.
- **Custom `PaneScaffoldDirective`**. The wrapper bypasses the scaffold entirely on Expanded-with-no-selection; mutating the directive post-mount would desynchronise the navigator's saver and is avoided.

## 3. Architecture

### 3.1 Single-source paging slicing

All three Home Expanded layout slots (hero, grid, rail) draw from the same `vm.pagingDataFlow.collectAsLazyPagingItems()` call:

| Slot | Index range | Composable |
|---|---|---|
| Hero | `pagingItems[0]` | Existing `FeedItem(showEditorPick = true)` |
| Grid | `pagingItems[1..4]` | Existing `FeedItem(showEditorPick = false)` |
| Rail | `pagingItems[5..9]` | New `UpNextCard` (compact horizontal — see §4) |

No new `Flow<List<BlogPost>>` is added to `BlogViewModel`. The same paging stream feeds all three slots. The first paging page is assumed to return ≥10 items so the hero + grid + rail populate without flicker; the implementation plan must verify the current `BlogPagingConstants.PAGE_SIZE` value before committing — see §8. Slots beyond the loaded items render the partial-load fallbacks in §6.2.

`BLOG_SEARCH` uses the same `pagingDataFlow`, no offset slicing — the grid renders all paging items via `LazyVerticalGrid` with infinite scroll, identical data shape to today's single-column list.

### 3.2 Window-class decision tree

```
Window class
    │
    ├─ Compact (< 600dp)   → Phase 2 wrapper passes through. Existing single-column.
    ├─ Medium (600–839dp)  → Phase 2 wrapper mounts scaffold (single-pane swap).
    └─ Expanded (≥ 840dp)
         │
         ├─ no selection   → Phase 3 bypass: render expandedListPane full-width.
         └─ has selection  → Phase 2 wrapper mounts scaffold (dual-pane reading).
```

The `Expanded` decision uses Material's `currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED`. Phase 1's `WindowClassifier.isMediumWindowAsState()` continues to gate the Phase 2 branch (it returns `true` for both Medium and Expanded), but the wrapper now distinguishes between them inside the Medium branch using Material's classifier.

### 3.3 Why bypass the scaffold rather than override the directive

Material's `PaneScaffoldDirective` is consumed by `rememberListDetailPaneScaffoldNavigator` at navigator construction. Mutating the directive post-mount (e.g., flipping `maxHorizontalPartitions` from 1 to 2 when a selection appears) can desynchronise the navigator's `Saver` and lead to "phantom" destinations across configuration changes. Bypassing the scaffold entirely on Expanded-with-no-selection avoids that class of bugs and keeps the scaffold's `BackHandler` semantics intact for the dual-pane state when it does mount.

### 3.4 Component diagram

```
┌─────────────────────────────────────────────────────────────────┐
│  MainActivity                                                   │
│    └─ NavHost                                                   │
│        └─ MAIN_GRAPH                                            │
│            └─ BLOG_GRAPH                                        │
│                ├─ BLOG_FEED  (blogFeedRoute helper from PR #66) │
│                │   └─ BlogListDetailScaffold                    │
│                │       ├─ Compact: passes through → BlogFeedScreen
│                │       ├─ Medium: scaffold dual-pane (Phase 2) │
│                │       └─ Expanded:                             │
│                │           ├─ no selection → HomeFeedExpanded   │
│                │           │   ├─ Hero (FeedItem showEditorPick)│
│                │           │   ├─ 2×2 LazyVerticalGrid (FeedItem)│
│                │           │   └─ Up next rail (5× UpNextCard)  │
│                │           └─ has selection → scaffold dual-pane│
│                │                              (Phase 2)         │
│                ├─ BLOG_SEARCH (same wrapper, mode=Search)       │
│                │   └─ Expanded no-selection → SearchFeedExpanded│
│                ├─ BLOG_DETAIL ── full-screen on Compact only;   │
│                │                   not entered on Medium/Expanded│
│                └─ BLOG_EDIT ──── full-screen all sizes          │
└─────────────────────────────────────────────────────────────────┘
```

## 4. Files

### NEW — `:app`

`app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/HomeFeedExpanded.kt` (~70 LOC)

```kotlin
@Composable
internal fun HomeFeedExpanded(
    pagingDataFlow: Flow<PagingData<BlogPost>>,
    state: BlogListUiState,
    onAction: (BlogFeedAction) -> Unit,
    onBlogClicked: (String) -> Unit,
    onVisibleSlugsChanged: (Set<String>) -> Unit = {},
)
```

Top-level `Row`:
- Left `Column(Modifier.weight(1f))` — `HomeTopBar` + `CategoryChipsRow` + hero `FeedItem` + `LazyVerticalGrid(GridCells.Fixed(2))` of 4 items.
- Right `Column(Modifier.width(320.dp))` — "Up next" header + 5 `UpNextCard`s in a `LazyColumn`. Background `surface_container_low`.

`onVisibleSlugsChanged` lifts `pagingItems.itemSnapshotList.items.map { it.slug }.toSet()` exactly as `BlogFeedScreen` does — see Phase 2's `BlogFeedScreen.kt:91`. Default no-op preserves call-sites that don't supply it.

`app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/SearchFeedExpanded.kt` (~40 LOC)

```kotlin
@Composable
internal fun SearchFeedExpanded(
    pagingDataFlow: Flow<PagingData<BlogPost>>,
    state: BlogListUiState,
    onAction: (BlogFeedAction) -> Unit,
    onBlogClicked: (String) -> Unit,
    onVisibleSlugsChanged: (Set<String>) -> Unit = {},
)
```

Top-level `Column`: `SearchTopBar` + `CategoryChipsRow` + `LazyVerticalGrid(GridCells.Fixed(2))` of all paging items rendered via `FeedItem(showEditorPick = false)`.

`app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/UpNextCard.kt` (~30 LOC)

```kotlin
@Composable
internal fun UpNextCard(
    blogPost: BlogPost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Horizontal `Row(modifier.fillMaxWidth().clickable(onClick))`:
- 80×80dp `AsyncImage` (Coil 3, `surface_container_lowest` placeholder, `RoundedCornerShape(NyasaTheme.spacing.s)`).
- `Column` with 2-line title (`MaterialTheme.typography.bodyMedium`, Newsreader, `maxLines = 2`, `TextOverflow.Ellipsis`) + byline (`MaterialTheme.typography.labelSmall`, `onSurfaceVariant`).
- Spacing: `NyasaTheme.spacing.m` between thumb and column; `s` between title and byline.

No card chrome (matches the rail-of-thumbnails treatment in visual spec §4.1).

### EDITED — `:app`

`app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt`

Add one new optional parameter to the existing signature:

```kotlin
internal fun BlogListDetailScaffold(
    onNavigateToDetailFullScreen: (slug: String) -> Unit,
    visibleSlugs: Set<String>,
    mode: FeedMode,
    listPane: @Composable (onBlogClicked: (String) -> Unit) -> Unit,
    detailPane: @Composable (slug: String, onClose: () -> Unit) -> Unit,
    expandedListPane: (@Composable (onBlogClicked: (String) -> Unit) -> Unit)? = null,  // NEW
)
```

Inside the existing Medium branch, before the navigator is constructed, add the bypass check:

```kotlin
val adaptiveInfo = currentWindowAdaptiveInfo()
val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
val scaffoldDirective = remember(adaptiveInfo) { calculatePaneScaffoldDirective(adaptiveInfo) }
val navigator = rememberListDetailPaneScaffoldNavigator<Any>(scaffoldDirective)
val hasSelection = navigator.currentDestination?.content != null

if (isExpanded && !hasSelection && expandedListPane != null) {
    // Bypass scaffold: render Phase 3 layout full-width
    expandedListPane { slug ->
        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, slug)
    }
    return
}

// existing Phase 2 NavigableListDetailPaneScaffold mount path
```

When `expandedListPane == null`, the wrapper behaves exactly as Phase 2 + the post-PR-#66 cleanup. Existing call-sites without the slot continue to work.

`app/src/main/java/com/kanyandula/nyasa/ui/navigation/MainNavGraph.kt:blogFeedRoute`

Inside `blogFeedRoute(route, mode, navController)`, pass an `expandedListPane` argument that branches on `mode`:

```kotlin
expandedListPane = { onBlogClicked ->
    when (mode) {
        FeedMode.Home -> HomeFeedExpanded(
            pagingDataFlow = vm.pagingDataFlow,
            state = state,
            onAction = { action ->
                when (action) {
                    is BlogFeedAction.BlogClicked -> onBlogClicked(action.slug)
                    else -> feedAction(action)
                }
            },
            onBlogClicked = onBlogClicked,
            onVisibleSlugsChanged = { visibleSlugs = it }
        )
        FeedMode.Search -> SearchFeedExpanded(
            pagingDataFlow = vm.pagingDataFlow,
            state = state,
            onAction = { action ->
                when (action) {
                    is BlogFeedAction.BlogClicked -> onBlogClicked(action.slug)
                    else -> feedAction(action)
                }
            },
            onBlogClicked = onBlogClicked,
            onVisibleSlugsChanged = { visibleSlugs = it }
        )
    }
}
```

The `onAction` lambda mirrors the existing Phase 2 listPane action interception — `BlogClicked` is intercepted, all other actions fall through to `feedAction` (which routes through `handleBlogFeedAction` for filter/category/refresh/etc.).

### NEW — tests (`:app/src/androidTest`)

`com/kanyandula/nyasa/ui/main/blog/composables/UpNextCardTest.kt` (~40 LOC) — 2 cases.

`com/kanyandula/nyasa/ui/main/blog/composables/HomeFeedExpandedTest.kt` (~80 LOC) — 2 cases (full data, partial data).

`com/kanyandula/nyasa/ui/main/blog/composables/SearchFeedExpandedTest.kt` (~50 LOC) — 1 case.

`BlogListDetailScaffoldTest.kt` (modified) — 2 new cases (`expandedWidth_noSelection_…`, `expandedWidth_withSelection_…`). Existing 2 Phase 2 cases preserved unchanged.

## 5. Existing utilities to reuse

- **`BlogFeedScreen.kt:81–94`** — single-column composable. Used as the Compact and Medium-class fallback inside the scaffold's listPane slot. Phase 3 does NOT touch this composable.
- **`FeedItem`** (`BlogFeedScreen.kt:232+`) — already supports `showEditorPick = true` for the hero treatment and `false` for grid cells. No new card variant needed for hero or grid.
- **`HomeTopBar`, `SearchTopBar`, `CategoryChipsRow`** (already in `BlogFeedScreen.kt`) — reused inside `HomeFeedExpanded` and `SearchFeedExpanded`.
- **`BlogViewModel.pagingDataFlow`** — single source for hero + grid + rail. No new flow.
- **`BlogListDetailScaffold`** Phase 2 contract — preserved. Phase 3 only adds an optional slot.

## 6. Behavioural rules

### 6.1 Initial state — Expanded with no selection

Wrapper detects Expanded class + null `currentDestination`. Renders `expandedListPane` directly. Scaffold not mounted; no `DetailPanePlaceholder`. Side rail (Phase 1) visible.

### 6.2 Empty / partial-load fallbacks (`HomeFeedExpanded` only)

| `pagingItems.itemCount` | Hero | Grid | Rail |
|---|---|---|---|
| 0, refresh `Loading` | Centered spinner spanning left column | hidden | hidden |
| 0, refresh `NotLoading` | Existing `FeedEmptyState` spanning left column | hidden | hidden |
| 1–4 | post 0 | posts 1..min(4, count-1); empty cells omitted | hidden |
| 5–9 | post 0 | posts 1..4 | posts 5..count-1 (1–4 cards) |
| ≥ 10 | post 0 | posts 1..4 | posts 5..9 |

`SearchFeedExpanded` uses `LazyVerticalGrid`'s natural empty handling — no explicit count guard.

### 6.3 Forward transition (no-selection → tap → dual-pane)

1. `expandedListPane` renders. User taps a list item, hero, or rail card.
2. `onBlogClicked(slug)` callback (provided by the wrapper) calls `navigator.navigateTo(Detail, slug)`.
3. `navigator.currentDestination` is now non-null. Compose recomposes the wrapper.
4. `hasSelection == true` so the bypass branch is skipped; `NavigableListDetailPaneScaffold` mounts with the navigator already pointing at the slug.

The structural change (bypass subtree → scaffold subtree) is a Compose recomposition without a built-in transition. `AnimatedPane` operates *inside* the scaffold once it's mounted; it does NOT mediate the no-scaffold ↔ scaffold structural swap. This may produce a perceptible flash on the first tap — see §8 Risk #2 for the mitigation path.

### 6.4 Backward transition (dual-pane → back → no-selection)

1. User presses system back inside the dual-pane scaffold.
2. Scaffold's `BackHandler` calls `navigator.navigateBack()`.
3. `navigator.currentDestination` becomes null. Recomposition.
4. `hasSelection == false && isExpanded` → bypass branch fires; `expandedListPane` renders.

### 6.5 Search detail-clear (Q4 D rule from Phase 2)

Continues to operate on the scaffold's navigator. When `expandedListPane` is rendered (no scaffold), the rule is naturally inactive. As soon as the user taps a search result on Expanded, the scaffold mounts and the existing `LaunchedEffect(visibleSlugs, selected, mode)` block runs — unchanged.

### 6.6 Edge cases

| Scenario | Expected behaviour |
|---|---|
| Rotation mid-selection on Expanded | Navigator's `Saver` preserves `currentDestination`; layout choice (bypass vs scaffold) consistent post-rotation. |
| Fold/unfold mid-selection on Pixel Fold | Width drops below 600dp (folded) or rises above 840dp (unfolded). Wrapper detects the new size class. Folded → Compact branch (early return; full-screen detail). Unfolded → Medium branch with `isExpanded` re-evaluated. Selection persists via the saver. |
| Window resize on a desktop-style host (Chromebook) | Same as fold transitions. Pure layout reflow; no data loss. |
| Process death | Saver does not survive process death (per Phase 2 §6.1). Selection lost; user lands on Expanded layout in no-selection state. Out of scope. |

## 7. Verification

### 7.1 Pre-merge automated gates

```bash
cd <impl worktree>   # created at implementation-plan time, e.g. .claude/worktrees/h7-phase3a-3b on branch hardening/h7-phase3a-3b
./gradlew detekt spotlessCheck lintDebug
./gradlew :app:test :app:connectedDebugAndroidTest
./gradlew :app:assembleDebug
```

### 7.2 Manual device verification

Mandatory before merge. Pattern matches Phase 2 Task 8.

| Device / mode | Expected |
|---|---|
| Pixel 7 (411dp) | Compact path unchanged: tap → full-screen detail → back → feed. No grid, no rail. |
| Pixel Fold folded (~391dp) | Identical to Pixel 7. |
| Pixel Fold unfolded portrait (~852dp = Expanded) | Hero + 2×2 grid + "Up next" rail visible. Tap a post → dual-pane scaffold mounts with detail. Back → returns to hero+grid+rail. Side rail visible throughout. |
| Pixel Fold unfolded landscape (~883dp+ = Expanded) | Same as above. |
| Pixel Tablet portrait (800dp = Medium under 840dp threshold) | Phase 2 single-pane swap unchanged. No editorial layout. |
| Pixel Tablet landscape (1280×800 = Expanded) | Hero+grid+rail. Same flow as Pixel Fold unfolded. |
| Search Expanded | Open search on Expanded → 2-column grid of results. Tap → dual-pane scaffold. |
| Search detail-clear | Type query A → tap result (mounts scaffold) → type query B that excludes A → detail pane clears to placeholder (Phase 2 rule). |
| Fold mid-read | Open post on unfolded → fold → unfold. Selection preserved via saver. No crash. |

### 7.3 Crashlytics + Timber

Already wired (H8). No new observability for Phase 3. If the bypass-scaffold path produces unexpected NPEs at the `currentWindowAdaptiveInfo()` boundary, Crashlytics will catch them.

## 8. Risks / open questions

- **`BlogPagingConstants.PAGE_SIZE` value.** §3.1 assumes the first paging page returns ≥10 items so hero + grid + rail can populate without flicker. The value must be verified during implementation; if it's < 10, raise it for Expanded routes only or accept the partial-load fallbacks in §6.2. **Risk: low** — small fix either way.
- **Layout reflow jankiness on the bypass ↔ scaffold transition.** First tap on Expanded changes the Compose subtree from `expandedListPane` to `NavigableListDetailPaneScaffold`. `AnimatedPane` cross-fades pane content but the structural change happens instantly. If the perceived jank is too high, fall back to a 200ms `AnimatedVisibility` wrapper around both branches. **Risk: medium** — needs device verification.
- **`HomeFeedExpanded` and `SearchFeedExpanded` test stability.** Phase 2's instrumented tests on the Pixel Fold AVD were intermittently flaky around `AnimatedPane` mount timing (resolved by uninstalling test packages between runs). Phase 3a's new tests inherit the same behaviour. Mitigation: same simplification pattern as Phase 2 — test layout presence (e.g., "hero title visible", "rail item N visible") rather than click-fires-callback choreography. **Risk: medium**.
- **Asymmetric paging consumption.** `HomeFeedExpanded` reads items 0–9 directly via `pagingItems[i]`. If paging hasn't pre-loaded item 9 by the time the rail composes, those slots show partial-load fallbacks. The existing `FeedItem` placeholder behaviour (gray box for unloaded items) carries over. **Risk: low** — visual only, no crash.

## 9. Visual-spec reconciliation

This spec deviates from `2026-04-28-h7-adaptive-tablet-layouts-design.md` in two places. Updates ship in the **same PR** (one-file-only doc edit, kept inline rather than a separate follow-up):

- **§4.1 Foldable portrait sub-treatment** — annotate as "deferred; foldables actually report Expanded class at unfolded portrait, so the editorial layout applies there too. Real Medium-class devices (small tablets in portrait) keep the Phase 2 single-column list."
- **§4.1 right-column label** — change "Editor's Picks" to "Up next" to match the smart-split data source (offset slice of the same paging stream). Editorial-curated rail content remains a Phase 4+ item.

## 10. Phase rollout (recap)

| Phase | Scope | Status |
|---|---|---|
| 1 | `WindowClassifier` + `NyasaSideRail` | ✅ shipped 2026-04-28 (`b32beba`, PR #61) |
| 2 | `NavigableListDetailPaneScaffold` for Feed + Search | ✅ shipped 2026-05-01 (`6f990f8`, PR #65) |
| 2-cleanup | `blogFeedRoute` + `BindCurrentUsernameToBlogVm` helpers | ✅ shipped 2026-05-02 (`fab0801`, PR #66) |
| **3a** | **Home Expanded — hero + grid + rail (this spec)** | **Pending** |
| **3b** | **Search Expanded — 2-column grid (this spec)** | **Pending** |
| 3c | Bookmarks Expanded — 2-column horizontal-thumb cards | Pending (separate spec) |
| 3d | Blog Detail three-pane (list + body + meta column) | Pending (separate spec) |
| 4 | Modal Create sheet w/ live preview | Pending |

---

**Approval:** brainstorm transcript captured 2026-05-02; four architectural decisions locked (scope split: 3a + 3b together; data source: smart-split single-source paging; scaffold integration: bypass-on-Expanded-with-no-selection; Medium-class fallback: skip). Next step: implementation plan via `superpowers:writing-plans`.
