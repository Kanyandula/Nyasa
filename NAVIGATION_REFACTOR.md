# Navigation Refactor Plan

## Why

The current navigation has accumulated three categories of bugs that no incremental patch is going to close:

1. **`Restore State failed`** crashes — surfaced in Crashlytics, partially patched in cca4c79 with a try/catch that drops saved state on retry. Symptom: tab state silently lost after a fast tab tap.
2. **Blank-screen dead-end** — `currentDestination` becomes `null` after certain pop sequences. Bottom-bar tabs then silently early-return on the `isInGraph(MAIN_GRAPH)` guard, stranding the user with no way to navigate. Reproduced live in this branch's investigation.
3. **Double-pop on 2+ deep stacks** — fast taps on the back arrow pop more than one entry whenever the parent isn't a graph start destination. Reproduced live: `Bookmarks → Detail → back-spam` skips Bookmarks and lands on Feed.

All three trace to the same architectural choice: the **multi-stack tab back-stack restoration pattern** (`popUpTo(BLOG_FEED) { saveState=true } + restoreState=true`) interacting badly with nested graph parents that have no Composable, plus tab routes that point at graph routes (`MainNavItem.Home.route = Routes.BLOG_GRAPH`).

## Strategic direction

**Stay on Navigation Compose 2.x with type-safe routes, drop multi-stack tab restoration, route every tab through a real screen destination.** Defer Navigation 3 (stable since AndroidX `navigation3-runtime:1.0.1` 2026-02-11) to a follow-up release once this work has baked in production. By then the route-typing migration is already done and the Nav 3 jump is mostly mechanical.

Reference shape: Android's official Navigation Compose docs and the type-safety guide:
- https://developer.android.com/develop/ui/compose/navigation
- https://developer.android.com/guide/navigation/design/type-safety

(Now in Android's `main` branch is no longer a clean reference — it migrated to Navigation 3 in late 2025. If we want to read code rather than docs, pin to NIA's last Nav-2.x commit before the migration.)

## Plan revision history

- **r3 (this revision)**: graphify-verified every concrete claim against the code. Corrections: `:core:ui` module does not exist (Phase 3c retargeted); the `ACCOUNT_GRAPH` nested wrapper mirrors `BLOG_GRAPH` and is left intentionally nested (noted in Phase 0/3a); `isInGraph` walks `parent.parent`, so Phase 3b's guard refinement can drop a level after flattening; the shared `BlogViewModel` spans **three** `getBackStackEntry(BLOG_GRAPH)` callsites covering four screens, not two (Phase 3d). All other claims confirmed accurate, including the exact `app/build.gradle:172` line.
- **r2**: incorporates Codex review feedback — Phase 0 expanded to all tabs + null recovery + regression test; Phase 3 explicitly addresses double-pop via per-entry lifecycle gate and Hilt re-scoping; `isInGraph` guard refined rather than dropped; NIA reference replaced with narrower pointer; tests moved into the phase that fixes the regression they cover.
- **r1**: initial draft.

## Phase plan

Six PRs, sequenced. Each is independently reviewable, independently revertable, and individually shippable. No phase creates a half-migrated state worse than what we have today.

### Phase 0 — Tab routes point to real screens + null recovery + regression test

Three small changes that ship together:

1. **All tabs target real screen routes, not graph wrappers.**
   - `MainNavItem.Home.route` → `Routes.BLOG_FEED` (was `Routes.BLOG_GRAPH`).
   - `MainNavItem.Profile.route` → `Routes.ACCOUNT_PROFILE` (was `Routes.ACCOUNT_GRAPH`). Note this points the tab at the leaf while the `ACCOUNT_GRAPH` nested wrapper (`MainNavGraph.kt:278`) stays in place — only `BLOG_GRAPH` is flattened (Phase 3a). The asymmetry is intentional: `ACCOUNT_GRAPH` scopes a shared `AccountViewModel` across profile/edit/change-password and is not implicated in any of the three bugs. Don't "fix" the leftover wrapper.
   - `Search` and `Bookmarks` already point at real routes — leave alone.
   - Update `mainNavItemForRoute` so tab-selection still highlights correctly when the user is deep inside a sub-route (e.g. `BLOG_DETAIL` → Home selected).
2. **Refine the `currentDestination` guard in `navigateToMainNavItem`** to distinguish "no destination yet / dead stack" from "destination is in another graph". Replace:
   ```kotlin
   if (!currentDestination.isInGraph(Routes.MAIN_GRAPH)) return
   ```
   with:
   ```kotlin
   val current = currentDestination
   // null → dead stack: fall through and let navigate() recover
   // not in MAIN_GRAPH (e.g. AUTH_GRAPH during logout race) → ignore tap
   if (current != null && !current.isInGraph(Routes.MAIN_GRAPH)) return
   ```
   This is the precise fix observed live in the BackTracker investigation: when `currentDestination` was null, the original guard silently swallowed every tab tap and stranded the user.
3. **Regression test** (instrumented or Robolectric, whichever ships first): drive the real `RootNavHost` into the dead-stack state and assert that a bottom-bar tap recovers to a valid destination. Same test fixture also asserts that a tab tap does not expose a graph parent route.

- **Why first:** structurally eliminates the blank-screen pathology and the dead-stack pathology in a single small PR. Both are reproducible from logs in this branch.
- **Risk:** low. Behavior change: tab-tap goes directly to leaf destinations.
- **Acceptance:**
  - `MainNavItem` tab routes are all leaf routes.
  - The `from=null` log seen in the BackTracker investigation now recovers via the navigate path instead of early-returning.
  - Regression test passes; confirmed manually by repeating the original blank-screen reproduction.
- **Branch:** `nav/phase-0-tab-to-real-screen`. Mergeable in a day.

### Phase 1 — Bump `navigation-compose` 2.7.7 → 2.9.x

Update `app/build.gradle:172` and any transitive constraints. No code changes beyond what the new version's API requires.

- **Why next:** type-safe routes (Phase 2) require 2.8.0+. 2.9.x is current per Android docs.
- **Risk:** low. Compose nav has been API-stable across 2.7→2.9 for our usage.
- **Acceptance:** clean build, all existing flows work on debug device.
- **Branch:** `nav/phase-1-bump-2.9`.

### Phase 2 — Type-safe routes

Replace string `Routes` constants with `@Serializable` Kotlin types:

```kotlin
@Serializable object BlogFeed
@Serializable data class BlogDetail(val slug: String)
@Serializable data class BlogEdit(val slug: String)
@Serializable data class AuthorProfile(val username: String)
// ...
```

All `composable("blog/feed")` → `composable<BlogFeed>`. All `navigate("blog/detail/$slug")` → `navigate(BlogDetail(slug))`. Replace `mainNavItemForRoute(currentRoute: String?)` with type-based dispatch via `NavDestination.hasRoute<T>()`.

- **Why next:** removes the entire string-based routing mess and the `startsWith` matching in `mainNavItemForRoute` that drives current bottom-bar selection. Once routes are typed, the rest of the refactor is obvious.
- **Risk:** medium — large mechanical change touching every nav callsite. No behavior change.
- **Acceptance:** every `Routes.*` string constant is gone; every `composable(...)` and `navigate(...)` uses typed keys.
- **Branch:** `nav/phase-2-type-safe-routes`.

### Phase 3 — Drop multi-stack restoration + flatten the graph + fix double-pop + re-scope shared VMs

The architectural fix. Four sub-tasks, all required because they're entangled. Ship as one PR with each sub-task as its own commit.

#### 3a. Flatten the graph

- Lift `BLOG_FEED`, `BLOG_SEARCH`, `BLOG_DETAIL`, `BLOG_EDIT` to be direct children of `MAIN_GRAPH`.
- Delete the nested `navigation(route = BLOG_GRAPH) { ... }` wrapper.

#### 3b. Drop multi-stack restoration; refine the guard rather than dropping it

In `navigateToMainNavItem` (renamed `navigateToTab`):

- Drop `saveState = true` and `restoreState = true`.
- Drop the try/catch recovery — no longer needed without `restoreState`.
- **Keep** the `isInGraph(MAIN_GRAPH)` guard, but in the precise form from Phase 0: `null → recover; in MAIN_GRAPH → navigate; in AUTH_GRAPH → ignore`. The session-swap race in the `LaunchedEffect(token)` block of `MainActivity` (~L93–99) still exists; removing the guard entirely re-opens it.
- `isInGraph` (`NavigationExtensions.kt`) currently walks `route == graphRoute || parent?.route || parent?.parent?.route` — three levels, because today the blog leaves sit at `MAIN_GRAPH › BLOG_GRAPH › BLOG_FEED`. After 3a flattening drops the `BLOG_GRAPH` tier, blog leaves are two levels deep, so the `parent?.parent?.route` branch is no longer needed for them (it stays valid but dead for the blog tree; `ACCOUNT_GRAPH` still needs two levels). Simplify or leave as-is, but don't assume three levels are load-bearing post-flatten.
- Use `popUpTo(navHostStartDestination) { inclusive = false }` + `launchSingleTop = true`.

Each tab has a single linear back stack. Tab switch resets the destination tab to its root. **Trade-off:** scroll position no longer survives a tab switch. Confirm with product before merging (see Open Questions).

#### 3c. Fix double-pop via per-entry lifecycle gate

**Critical correction from the r1 plan.** Flattening does not prevent double-pop. The bug is that an outgoing composable's `IconButton` keeps receiving taps while its `AnimatedContent` exit animation runs. On a stack like `Feed → Bookmarks → Detail`, tap #1 pops Detail (lands on Bookmarks); tap #2 fires from the still-composed Detail IconButton and pops Bookmarks, skipping the user past it. The start-destination floor only protects the bottommost entry.

Fix: gate every back-press on the calling entry's lifecycle being `RESUMED`. There is no `:core:ui` module today — the module graph is `:core:{common, designsystem, network, database, domain, session, data, work}`, and `NyasaTopBar` still lives in `:app` at `ui/components/NyasaTopBar.kt`. Put the helper in `:app` alongside the nav code, or in `:core:designsystem` if it should be reusable; do not introduce a `:core:ui` module just for this. Add:

```kotlin
@Composable
fun rememberBackPressOnce(
    entry: NavBackStackEntry = LocalLifecycleOwner.current as NavBackStackEntry,
    action: () -> Unit
): () -> Unit = remember(entry, action) {
    {
        if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            action()
        }
    }
}
```

Or, less Compose-y but route-agnostic, an extension function:

```kotlin
fun NavController.popBackStackOnce(entry: NavBackStackEntry): Boolean =
    if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) popBackStack() else false
```

Wire every back-arrow callsite through one of these. NavController drops the leaving entry's lifecycle to `STARTED` the moment a navigation commits, so any tap during the exit animation is filtered out cleanly.

The Search tab's `navigateToMainNavItem(Home)` path needs the same gate — currently it can fire twice during the exit animation and silently drop saved state via the recovery branch. Once `restoreState` is gone, that recovery branch is dead code, but the leaked taps still hit `navigate()`. The lifecycle gate stops them at the source.

#### 3d. Re-scope shared `BlogViewModel` after deleting `BLOG_GRAPH`

**Critical correction from the r1 plan.** `BlogViewModel` is currently shared across feed / search / detail / edit via:

```kotlin
val parentEntry = remember(entry) { navController.getBackStackEntry(Routes.BLOG_GRAPH) }
val vm: BlogViewModel = hiltViewModel(parentEntry)
```

This pattern appears at **three** callsites covering four screens: `MainNavGraph.kt:173` (the `blogFeedRoute` helper, reused for both `BLOG_FEED` and `BLOG_SEARCH`), `:213` (detail), and `:235` (edit). All three break the moment `BLOG_GRAPH` is gone (3a) — `getBackStackEntry(Routes.BLOG_GRAPH)` throws and the shared-VM pattern breaks. The edit flow specifically depends on this — `editBodyState`, `updatedBlogTitle`, etc. are seeded on Detail and read on Edit.

Two viable re-scoping strategies, pick one in this PR:

- **Option A — Activity-scoped Blog state (simpler, slight memory cost).** Promote the shared fields into an `@ActivityRetainedScoped` Hilt-injected class. ViewModels fetch from it. Lives across logouts → must be invalidated on session change (already a pattern via `SessionManager`).
- **Option B — Typed argument passing (more refactor, cleaner state).** Remove `editBodyState` and the bridge fields from `BlogViewModel`. Pass the body HTML as a route argument from Detail → Edit (typed routes from Phase 2 make this clean — `BlogEdit(slug = ..., initialBody = ...)`). Edit screen owns its own `EditBlogViewModel` scoped to its own back-stack entry.

**Recommendation: Option B.** It eliminates the cross-screen mutable VM coupling that's been a recurring pain point, and typed routes make it easy. Cost: ~1 day of additional work in Phase 3, mostly mechanical.

- **Why this order:** with typed routes from Phase 2, every reference is mechanically updatable. With graph parents removed, the bottom-bar selection logic becomes a typed `when (route) { is BlogFeed -> Home; is BlogSearch -> Search; is BlogDetail -> Home; ... }`.
- **Risk:** medium-high. Behaviorally visible (scroll position not restored). Largest single PR in the plan; consider splitting 3a+3b from 3c+3d if review bandwidth is a concern.
- **Acceptance:**
  - No `saveState` / `restoreState` references anywhere.
  - No try/catch around `NavController.navigate`.
  - No `getBackStackEntry(Routes.BLOG_GRAPH)` references — replaced by per-entry VMs and either activity-scoped state (A) or typed args (B).
  - Every back-press callsite goes through `rememberBackPressOnce` or `popBackStackOnce`.
  - **Regression tests in same PR**: (a) `Feed → Bookmarks → Detail → tap-back twice within 200ms` lands on Bookmarks, not Feed. (b) Search back arrow tapped twice within 200ms produces exactly one `navigate(BLOG_FEED)`. (c) Tab tap during a deep-stack exit animation does not crash and lands on the tapped tab's root.
  - Manually exercise the test sequence from the BackTracker investigation: open detail, spam back; open from Bookmarks, spam back; rapid tab switches; rapid Search-back-arrow taps. None should produce blank screens, skipped pops, or stranded UI.
- **Branch:** `nav/phase-3-flatten-and-fix-pops`.

### Phase 4 — Centralize through Navigator callbacks

Per the H5 architecture contract: feature composables should not see `NavController`. Each feature gets a `*Navigator` interface:

```kotlin
interface BlogNavigator {
  fun openPost(slug: String)
  fun openAuthor(username: String)
  fun back()
}
```

Implementations live in `:app`, hold the `NavController`, and are injected into the `composable<>` block.

- **Why last:** until routes are typed (Phase 2) and the graph is flat (Phase 3), the Navigator surface area would constantly churn. Doing it after stabilizes the contract.
- **Risk:** medium. Touches every composable that currently takes `onNavigateBack`/`onEdit`/`onAuthorClick`. Mechanical.
- **Acceptance:** no `NavController` references inside `ui/main/blog/` or `ui/main/account/` composables.
- **Branch:** `nav/phase-4-navigator-pattern`.

### Phase 5 — Test harness + coverage gap-fill

Each fixing phase ships its own regression test (Phase 0 covers null-recovery; Phase 3 covers double-pop and exit-animation tap leakage). Phase 5 establishes the shared harness and fills remaining gaps.

- **Test the real `RootNavHost`, not a synthetic mirror.** Per Codex's review point: a fake graph that "mirrors" `MainNavGraph` can miss exactly the kind of graph-shape bug we hit. Drive the actual `RootNavHost` Composable with `TestNavHostController` so the graph under test is the production graph.
- Robolectric harness for JVM-runnable tests; instrumented Compose tests reserved for the smoke flow.
- Coverage targets:
  - Every back-press path produces at most one pop per click burst.
  - Every tab tap from any reachable destination lands on the tab's leaf route.
  - Process-death restore (simulated via state save/restore) lands on a valid destination.
  - Session swap (login → logout → login) does not race with bottom-bar taps.

- **Why last:** the architecture is stable; earlier-phase tests would need rewriting. Per-phase regression tests live in their own PR; this phase backfills systematic coverage.
- **Risk:** low.
- **Acceptance:** every navigation public API has a unit test; CI suite runs under 30s; tests fail if the Phase 3 fixes are reverted.
- **Branch:** `nav/phase-5-test-harness`.
- **Status:** ✅ Done (2026-06-22). The graph assembly is extracted into a shared `NavGraphBuilder.rootNavGraph(...)` builder used by both production `RootNavHost` and `NavigationGuardTest`, so the topology under test is the production topology — the hand-mirrored graph (and its `TODO(Phase 5)`) is gone. Two coverage gaps filled: process-death state save/restore lands on the pre-death leaf, and bottom-bar taps stay gated across a full login→logout→login swap. JVM/Robolectric only (instrumented smoke flow deferred, per the harness-scope decision). Full `:app` unit suite 227 tests green in ~28s.

### Phase 6 — Reassess Nav 3 (deferred)

Not in scope for this work. Re-evaluate once Phases 0-5 have shipped and run in production for one release cycle. By then the route migration is done and Nav 3's `navigation3-compose` migration is mostly removing `NavController` ceremony.

## What this work is NOT

- Not a Navigation 3 migration (deferred).
- Not a Voyager / Decompose adoption.
- Not adding `BackTracker` instrumentation back. Regression tests in Phases 0 and 3 cover the failure modes.

**Correction from r1:** the per-entry lifecycle gate (`Lifecycle.State.RESUMED` check on the calling `NavBackStackEntry`) IS required. r1 incorrectly claimed flattening eliminated it. See Phase 3c.

## Sequencing summary

| # | Branch | Risk | Mergeable | Depends on |
|---|---|---|---|---|
| 0 | `nav/phase-0-tab-to-real-screen` | low | day | — |
| 1 | `nav/phase-1-bump-2.9` | low | day | 0 |
| 2 | `nav/phase-2-type-safe-routes` | medium | days | 1 |
| 3 | `nav/phase-3-flatten-and-fix-pops` | medium-high | days | 2 |
| 4 | `nav/phase-4-navigator-pattern` | medium | days | 3 |
| 5 | `nav/phase-5-test-harness` | low | days | 3 (4 ideal) |

Phase 0 ships immediately. Phases 1-5 sequence over the next 1-2 sprint cycles.
