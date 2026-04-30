# H7 Phase 2 List-Detail Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire `NavigableListDetailPaneScaffold` into the `BLOG_FEED` and `BLOG_SEARCH` routes so tablet (Medium / Expanded) users browse the feed and read posts side-by-side, while phone (Small) behavior remains unchanged.

**Architecture:** Option β (two-path) per the merged architecture spec at `docs/superpowers/specs/2026-04-28-h7-phase2-architecture-design.md`. On Small, taps drive the existing `NavController.navigate(blogDetail(slug))` to a full-screen `BLOG_DETAIL`. On Medium, a wrapper composable (`BlogListDetailScaffold`) intercepts `BlogFeedAction.BlogClicked`, drives the scaffold's internal `ThreePaneScaffoldNavigator`, and renders `BlogDetailRoute` directly inside the detail pane — `BLOG_DETAIL` is never entered. The scaffold's `BackHandler` collapses detail→list on system back; the wrapper supplies an `onClose` to the detail pane so top-bar back and delete-confirmed both call `navigator.navigateBack()` rather than popping `BLOG_GRAPH`.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose, `androidx.compose.material3.adaptive:adaptive-navigation:1.0.0` (new), Hilt 2.53.1, Navigation-Compose 2.7.7, Paging 3.3.6. Pre-commit runs `./gradlew detekt spotlessCheck lintDebug`. Project main branch: `Deploy_0.01`. Phase 1 predecessor: `b32beba` / PR #61.

---

## Deviations from the architecture spec (locked in here)

These three deviations land at plan time, not implementation time. Reviewers should know up front:

1. **No new `BlogViewModel.visibleSlugsFlow`.** The spec §4 added a flow on the VM; §8 explicitly approved a wrapper-local snapshot fallback as Plan B. We're taking Plan B because visibility is a UI concept and putting it in the VM creates UI→VM→UI coupling for one feature. Replaced by a new `onVisibleSlugsChanged: (Set<String>) -> Unit` callback parameter on `BlogFeedScreen` that lifts the snapshot to the wrapper. Removes the `BlogViewModelVisibleSlugsTest.kt` test from the test surface.

2. **`handleBlogFeedAction` is not refactored.** The wrapper intercepts `BlogFeedAction.BlogClicked` directly inside the `listPane` slot's `onAction` lambda; non-blog actions (`Search`, `Refresh`, `CreateClicked`, …) fall through to the existing `handleBlogFeedAction(vm, navController)` mapper unchanged. Smaller blast radius than re-parameterising the mapper.

3. **`FeedMode` enum values are `Home` and `Search`** (per `BlogFeedScreen.kt:77`), not `Feed` and `Search` as the architecture spec wrote. The plan uses `FeedMode.Home` and `FeedMode.Search`.

---

## File structure

| File | Status | Responsibility |
|---|---|---|
| `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/DetailPanePlaceholder.kt` | NEW (~20 LOC) | Empty-state composable shown in the detail pane when no slug is selected on Medium. |
| `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt` | NEW (~90 LOC) | Wrapper. On Small, renders `listPane(onBlogClicked = onNavigateToDetailFullScreen)` and exits. On Medium, mounts `NavigableListDetailPaneScaffold`, owns selection via `ThreePaneScaffoldNavigator`, supplies `onClose` to the detail slot, and runs the search detail-clear `LaunchedEffect`. |
| `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogFeedScreen.kt` | MODIFIED | Add `onVisibleSlugsChanged: (Set<String>) -> Unit = {}` param + `LaunchedEffect` that lifts `pagingItems.itemSnapshotList.items` to the caller. Default no-op preserves the existing `BlogFeedScreen` callers (previews, tests). |
| `app/src/main/java/com/kanyandula/nyasa/ui/navigation/MainNavGraph.kt` | MODIFIED | Wrap the `BlogFeedScreen` calls inside the `BLOG_FEED` and `BLOG_SEARCH` composables in `BlogListDetailScaffold(...)`. `BLOG_DETAIL` composable left untouched. |
| `app/build.gradle` | MODIFIED | One new dep: `androidx.compose.material3.adaptive:adaptive-navigation:1.0.0`. |
| `app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/DetailPanePlaceholderTest.kt` | NEW | Renders the placeholder, asserts the prompt string is shown. |
| `app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffoldTest.kt` | NEW | Three width buckets via overridden `LocalWindow` + `Modifier.size()`: Compact (scaffold not mounted), Medium (scaffold mounted, click → detail visible, `onNavigateToDetailFullScreen` not invoked), Search (visibleSlugs change → scaffold detail clears). Pre-existing test infra: `app/src/androidTest/java/com/kanyandula/nyasa/ui/theme/window/` mirrors. |

---

## Task 0: Branch + worktree confirmation

The impl worktree was created during plan authoring. Re-confirm before starting tasks.

- [ ] **Step 1: Verify worktree + branch**

Run: `git -C /Users/admin/StudioProjects/Nyasa worktree list`

Expected output includes:
```
/Users/admin/StudioProjects/Nyasa/.claude/worktrees/h7-phase2  <sha> [hardening/h7-phase2]
```

If absent:
```bash
git -C /Users/admin/StudioProjects/Nyasa worktree add -b hardening/h7-phase2 \
  /Users/admin/StudioProjects/Nyasa/.claude/worktrees/h7-phase2 Deploy_0.01
```

All subsequent commands run from `/Users/admin/StudioProjects/Nyasa/.claude/worktrees/h7-phase2`.

- [ ] **Step 2: Confirm clean baseline**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. If not, stop — fix the baseline before adding Phase 2 code.

---

## Task 1: Add `material3-adaptive-navigation` dependency

**Files:**
- Modify: `app/build.gradle` (dependencies block)

- [ ] **Step 1: Add the dependency**

Open `app/build.gradle`. In the `dependencies { ... }` block, immediately after the existing `androidx.compose.material3` line(s), add:

```groovy
implementation 'androidx.compose.material3.adaptive:adaptive-navigation:1.0.0'
```

(Group the line with other material3 deps for greppability. Use single quotes per the file's existing style.)

- [ ] **Step 2: Confirm the dep resolves**

Run: `./gradlew :app:dependencies --configuration debugCompileClasspath | grep adaptive-navigation`
Expected: a line ending in `androidx.compose.material3.adaptive:adaptive-navigation:1.0.0` (no `FAILED`).

- [ ] **Step 3: Build to verify no surface change**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Adding an unused dependency must not break the build.

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle
git commit -m "build(h7): add material3 adaptive-navigation dep for Phase 2"
```

---

## Task 2: `DetailPanePlaceholder` composable

The empty-state shown in the detail pane on Medium when no slug is selected. No state, no DI.

**Files:**
- Create: `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/DetailPanePlaceholder.kt`
- Test: `app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/DetailPanePlaceholderTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/DetailPanePlaceholderTest.kt`:

```kotlin
package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import org.junit.Rule
import org.junit.Test

class DetailPanePlaceholderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersSelectAPostPrompt() {
        composeRule.setContent {
            NyasaTheme {
                DetailPanePlaceholder()
            }
        }

        composeRule.onNodeWithText("Select a post to read").assertIsDisplayed()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:connectedDebugAndroidTest --tests com.kanyandula.nyasa.ui.main.blog.composables.DetailPanePlaceholderTest`
Expected: FAIL — unresolved reference `DetailPanePlaceholder` (compile error).

- [ ] **Step 3: Write minimal implementation**

Create `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/DetailPanePlaceholder.kt`:

```kotlin
package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
internal fun DetailPanePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(NyasaTheme.spacing.l),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.m)
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Select a post to read",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

(Spacing tokens come from `NyasaTheme.spacing` — same source used by `BlogFeedScreen` at line 192.)

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:connectedDebugAndroidTest --tests com.kanyandula.nyasa.ui.main.blog.composables.DetailPanePlaceholderTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/DetailPanePlaceholder.kt \
        app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/DetailPanePlaceholderTest.kt
git commit -m "feat(h7): add DetailPanePlaceholder for empty Medium detail pane"
```

---

## Task 3: `BlogFeedScreen` — `onVisibleSlugsChanged` callback

Lift the current paging snapshot's slug set up to the caller, so the wrapper can run the search detail-clear rule (Q4 D) without piping anything through `BlogViewModel`.

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogFeedScreen.kt:81-95` (signature + body)

- [ ] **Step 1: Add the parameter and `LaunchedEffect`**

In `BlogFeedScreen.kt`, change the function signature and body. Find the current declaration:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogFeedScreen(
    pagingDataFlow: Flow<PagingData<BlogPost>>,
    state: BlogListUiState,
    onAction: (BlogFeedAction) -> Unit,
    mode: FeedMode = FeedMode.Home
) {
    TrackScreen("BlogFeed")
    val pagingItems: LazyPagingItems<BlogPost> = pagingDataFlow.collectAsLazyPagingItems()
```

Replace with:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogFeedScreen(
    pagingDataFlow: Flow<PagingData<BlogPost>>,
    state: BlogListUiState,
    onAction: (BlogFeedAction) -> Unit,
    mode: FeedMode = FeedMode.Home,
    onVisibleSlugsChanged: (Set<String>) -> Unit = {}
) {
    TrackScreen("BlogFeed")
    val pagingItems: LazyPagingItems<BlogPost> = pagingDataFlow.collectAsLazyPagingItems()

    LaunchedEffect(pagingItems.itemSnapshotList) {
        onVisibleSlugsChanged(
            pagingItems.itemSnapshotList.items.map { it.slug }.toSet()
        )
    }
```

(The default `= {}` keeps every existing call-site compiling with no behavioural change.)

- [ ] **Step 2: Build + lint**

Run: `./gradlew :app:detekt :app:lintDebug :app:assembleDebug`
Expected: all `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run existing UI tests to confirm no regression**

Run: `./gradlew :app:connectedDebugAndroidTest --tests com.kanyandula.nyasa.ui.main.blog.composables.*`
Expected: existing tests pass; the new `DetailPanePlaceholderTest` from Task 2 also passes.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogFeedScreen.kt
git commit -m "feat(h7): lift BlogFeedScreen paging snapshot via onVisibleSlugsChanged"
```

---

## Task 4: `BlogListDetailScaffold` — Compact path (scaffold not mounted)

First slice: wrapper exists, on Small it just delegates clicks to the existing nav callback. No scaffold, no detail pane.

**Files:**
- Create: `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt`
- Test: `app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffoldTest.kt`

- [ ] **Step 1: Write the failing Compact-path test**

Create `app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffoldTest.kt`:

```kotlin
package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.window.LocalWindow
import com.kanyandula.nyasa.ui.theme.window.WindowClassifier
import com.kanyandula.nyasa.ui.theme.window.WindowSizeClass
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private class FakeWindow(private val sizeClass: WindowSizeClass) : WindowClassifier {
    @Composable override fun windowSizeClassAsState(): State<WindowSizeClass> =
        remember { mutableStateOf(sizeClass) }
    @Composable override fun isMediumWindowAsState(): State<Boolean> =
        remember { mutableStateOf(sizeClass == WindowSizeClass.Medium) }
    @Composable override fun isSmallWindowAsState(): State<Boolean> =
        remember { mutableStateOf(sizeClass == WindowSizeClass.Small) }
    @Composable override fun isLandscapeAsState(): State<Boolean> =
        remember { mutableStateOf(false) }
}

class BlogListDetailScaffoldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compactWidth_scaffoldNotMounted_clickFiresFullScreenNav() {
        var navigatedSlug: String? = null

        composeRule.setContent {
            CompositionLocalProvider(LocalWindow provides FakeWindow(WindowSizeClass.Small)) {
                NyasaTheme {
                    BlogListDetailScaffold(
                        onNavigateToDetailFullScreen = { slug -> navigatedSlug = slug },
                        visibleSlugs = emptySet(),
                        mode = FeedMode.Home,
                        listPane = { onBlogClicked ->
                            Box(Modifier) {
                                Text(
                                    text = "Tap me",
                                    modifier = Modifier
                                        .testTagOrSemantic("listItem")
                                        .clickableForTest { onBlogClicked("some-slug") }
                                )
                            }
                        },
                        detailPane = { _, _ -> Text("DETAIL_PANE") }
                    )
                }
            }
        }

        composeRule.onNodeWithText("Tap me").assertIsDisplayed()
        composeRule.onNodeWithText("DETAIL_PANE").assertDoesNotExist()
        composeRule.onNodeWithText("Tap me").performClick()
        assertEquals("some-slug", navigatedSlug)
    }
}
```

The test uses two trivial helpers (`testTagOrSemantic`, `clickableForTest`) — define them inline above the test class:

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
private fun Modifier.testTagOrSemantic(tag: String): Modifier =
    this.semantics { testTag = tag }
private fun Modifier.clickableForTest(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
```

(Helpers are private to the test file and exist purely so the test reads cleanly without a real composable. If you find these helpers awkward at review time, replace with a real `BlogFeedScreen` test double — but defer that until it actually pays off.)

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:connectedDebugAndroidTest --tests com.kanyandula.nyasa.ui.main.blog.composables.BlogListDetailScaffoldTest.compactWidth_scaffoldNotMounted_clickFiresFullScreenNav`
Expected: FAIL — unresolved reference `BlogListDetailScaffold` (compile error).

- [ ] **Step 3: Write the Compact-path-only implementation**

Create `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt`:

```kotlin
package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.kanyandula.nyasa.ui.theme.window.LocalWindow

/**
 * Adaptive list-detail wrapper for the blog feed and search routes.
 *
 * On Small windows, the scaffold is not mounted: the list pane runs as today and clicks
 * fire [onNavigateToDetailFullScreen]. On Medium windows, mounts a
 * `NavigableListDetailPaneScaffold` and routes selection through its internal
 * `ThreePaneScaffoldNavigator`. The Medium path is added in the next task.
 */
@Composable
internal fun BlogListDetailScaffold(
    onNavigateToDetailFullScreen: (slug: String) -> Unit,
    visibleSlugs: Set<String>,
    mode: FeedMode,
    listPane: @Composable (onBlogClicked: (String) -> Unit) -> Unit,
    detailPane: @Composable (slug: String, onClose: () -> Unit) -> Unit,
) {
    val isMedium by LocalWindow.current.isMediumWindowAsState()

    if (!isMedium) {
        listPane(onNavigateToDetailFullScreen)
        return
    }

    // Medium path lands in Task 5.
    listPane(onNavigateToDetailFullScreen)
}
```

The `visibleSlugs`, `mode`, and `detailPane` parameters are unused on this code path; detekt may complain. Suppress only on this composable, not file-wide:

```kotlin
@Suppress("UnusedParameter")
@Composable
internal fun BlogListDetailScaffold(
```

The suppression is removed in Task 5 once the params are used.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:connectedDebugAndroidTest --tests com.kanyandula.nyasa.ui.main.blog.composables.BlogListDetailScaffoldTest.compactWidth_scaffoldNotMounted_clickFiresFullScreenNav`
Expected: PASS.

- [ ] **Step 5: Run code-quality gates**

Run: `./gradlew :app:detekt :app:spotlessCheck :app:lintDebug`
Expected: all `BUILD SUCCESSFUL`. Fix spotless with `./gradlew :app:spotlessApply` if needed and re-run.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt \
        app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffoldTest.kt
git commit -m "feat(h7): scaffold wrapper — Compact path passes through to nav"
```

---

## Task 5: `BlogListDetailScaffold` — Medium path (scaffold mounted, click → detail visible)

Mount the `NavigableListDetailPaneScaffold` on Medium and intercept clicks. `onNavigateToDetailFullScreen` is **not** invoked. The detail pane shows `DetailPanePlaceholder` until a slug is selected.

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt`
- Modify: `app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffoldTest.kt`

- [ ] **Step 1: Write the failing Medium-path test**

Append to `BlogListDetailScaffoldTest.kt`:

```kotlin
@Test
fun mediumWidth_scaffoldMounted_clickShowsDetailPane_andDoesNotNavigateFullScreen() {
    var navigatedSlug: String? = null

    composeRule.setContent {
        CompositionLocalProvider(LocalWindow provides FakeWindow(WindowSizeClass.Medium)) {
            NyasaTheme {
                BlogListDetailScaffold(
                    onNavigateToDetailFullScreen = { slug -> navigatedSlug = slug },
                    visibleSlugs = setOf("post-1"),
                    mode = FeedMode.Home,
                    listPane = { onBlogClicked ->
                        Text(
                            text = "Tap post-1",
                            modifier = Modifier.clickableForTest { onBlogClicked("post-1") }
                        )
                    },
                    detailPane = { slug, _ -> Text("DETAIL[$slug]") }
                )
            }
        }
    }

    // No selection: placeholder visible, detail not visible, full-screen nav not invoked
    composeRule.onNodeWithText("Select a post to read").assertIsDisplayed()
    composeRule.onNodeWithText("DETAIL[post-1]").assertDoesNotExist()
    assertEquals(null, navigatedSlug)

    // Click → detail pane shows the selected slug, full-screen nav still not invoked
    composeRule.onNodeWithText("Tap post-1").performClick()
    composeRule.onNodeWithText("DETAIL[post-1]").assertIsDisplayed()
    assertEquals(null, navigatedSlug)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:connectedDebugAndroidTest --tests com.kanyandula.nyasa.ui.main.blog.composables.BlogListDetailScaffoldTest.mediumWidth_scaffoldMounted_clickShowsDetailPane_andDoesNotNavigateFullScreen`
Expected: FAIL — placeholder not displayed (Medium path is currently a stub).

- [ ] **Step 3: Implement the Medium path**

Replace `BlogListDetailScaffold.kt` body:

```kotlin
package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.kanyandula.nyasa.ui.theme.window.LocalWindow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun BlogListDetailScaffold(
    onNavigateToDetailFullScreen: (slug: String) -> Unit,
    visibleSlugs: Set<String>,
    mode: FeedMode,
    listPane: @Composable (onBlogClicked: (String) -> Unit) -> Unit,
    detailPane: @Composable (slug: String, onClose: () -> Unit) -> Unit,
) {
    val isMedium by LocalWindow.current.isMediumWindowAsState()

    if (!isMedium) {
        listPane(onNavigateToDetailFullScreen)
        return
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<String>(
        scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    )
    val scope = rememberCoroutineScope()
    val onClose: () -> Unit = { scope.launch { navigator.navigateBack() } }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                listPane { slug -> scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, slug) } }
            }
        },
        detailPane = {
            AnimatedPane {
                val slug = navigator.currentDestination?.contentKey
                if (slug != null) {
                    detailPane(slug, onClose)
                } else {
                    DetailPanePlaceholder()
                }
            }
        }
    )

    // visibleSlugs + mode are wired in Task 6 (search detail-clear).
    @Suppress("UnusedParameter") // remove suppression in Task 6
    val unused = visibleSlugs to mode
}
```

Drop the file-level `@Suppress("UnusedParameter")` since `detailPane` and the click flow are now used. Replace with a single local suppression on the dummy `unused` line until Task 6 wires the search rule.

- [ ] **Step 4: Run both scaffold tests**

Run: `./gradlew :app:connectedDebugAndroidTest --tests com.kanyandula.nyasa.ui.main.blog.composables.BlogListDetailScaffoldTest`
Expected: both `compactWidth_…` and `mediumWidth_…` PASS.

- [ ] **Step 5: Run code-quality gates**

Run: `./gradlew :app:detekt :app:spotlessCheck :app:lintDebug`
Expected: all `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt \
        app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffoldTest.kt
git commit -m "feat(h7): scaffold wrapper — Medium path mounts NavigableListDetailPaneScaffold"
```

---

## Task 6: `BlogListDetailScaffold` — search detail-clear (Q4 D rule)

When the user changes the search query in `BLOG_SEARCH` and the previously-selected post is no longer in the visible result set, collapse the scaffold's detail pane. Operates on the scaffold navigator only — never touches `NavController`. Does **not** apply when `mode == FeedMode.Home`.

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt`
- Modify: `app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffoldTest.kt`

- [ ] **Step 1: Write the failing search-clear test**

Append to `BlogListDetailScaffoldTest.kt`:

```kotlin
@Test
fun searchMode_visibleSlugsExcludeSelected_clearsDetailPaneToPlaceholder() {
    val visibleSlugsState = mutableStateOf(setOf("post-1", "post-2"))

    composeRule.setContent {
        CompositionLocalProvider(LocalWindow provides FakeWindow(WindowSizeClass.Medium)) {
            NyasaTheme {
                BlogListDetailScaffold(
                    onNavigateToDetailFullScreen = {},
                    visibleSlugs = visibleSlugsState.value,
                    mode = FeedMode.Search,
                    listPane = { onBlogClicked ->
                        Text(
                            text = "Tap post-1",
                            modifier = Modifier.clickableForTest { onBlogClicked("post-1") }
                        )
                    },
                    detailPane = { slug, _ -> Text("DETAIL[$slug]") }
                )
            }
        }
    }

    composeRule.onNodeWithText("Tap post-1").performClick()
    composeRule.onNodeWithText("DETAIL[post-1]").assertIsDisplayed()

    // User edits the query; visible result set no longer contains post-1
    composeRule.runOnUiThread {
        visibleSlugsState.value = setOf("post-3")
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithText("DETAIL[post-1]").assertDoesNotExist()
    composeRule.onNodeWithText("Select a post to read").assertIsDisplayed()
}

@Test
fun homeMode_visibleSlugsExcludeSelected_keepsDetailPane() {
    val visibleSlugsState = mutableStateOf(setOf("post-1", "post-2"))

    composeRule.setContent {
        CompositionLocalProvider(LocalWindow provides FakeWindow(WindowSizeClass.Medium)) {
            NyasaTheme {
                BlogListDetailScaffold(
                    onNavigateToDetailFullScreen = {},
                    visibleSlugs = visibleSlugsState.value,
                    mode = FeedMode.Home,
                    listPane = { onBlogClicked ->
                        Text(
                            text = "Tap post-1",
                            modifier = Modifier.clickableForTest { onBlogClicked("post-1") }
                        )
                    },
                    detailPane = { slug, _ -> Text("DETAIL[$slug]") }
                )
            }
        }
    }

    composeRule.onNodeWithText("Tap post-1").performClick()
    composeRule.onNodeWithText("DETAIL[post-1]").assertIsDisplayed()

    composeRule.runOnUiThread {
        visibleSlugsState.value = setOf("post-3") // post-1 paged out — Home should NOT clear detail
    }
    composeRule.waitForIdle()

    composeRule.onNodeWithText("DETAIL[post-1]").assertIsDisplayed()
}
```

(Remember to import `mutableStateOf` from `androidx.compose.runtime.mutableStateOf` and `getValue`/`setValue` if needed.)

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :app:connectedDebugAndroidTest --tests com.kanyandula.nyasa.ui.main.blog.composables.BlogListDetailScaffoldTest.searchMode_visibleSlugsExcludeSelected_clearsDetailPaneToPlaceholder com.kanyandula.nyasa.ui.main.blog.composables.BlogListDetailScaffoldTest.homeMode_visibleSlugsExcludeSelected_keepsDetailPane`
Expected: search-clear FAILS (detail still visible); home-keep PASSES (no clear logic exists yet, so detail stays — but this guards against an over-eager future implementation).

- [ ] **Step 3: Add the search-clear `LaunchedEffect`**

In `BlogListDetailScaffold.kt`, replace the dummy `unused` line at the bottom of the Medium branch with a proper `LaunchedEffect`. Final body of the Medium branch:

```kotlin
    val navigator = rememberListDetailPaneScaffoldNavigator<String>(
        scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    )
    val scope = rememberCoroutineScope()
    val onClose: () -> Unit = { scope.launch { navigator.navigateBack() } }

    val selected = navigator.currentDestination?.contentKey
    LaunchedEffect(visibleSlugs, selected, mode) {
        if (mode == FeedMode.Search && selected != null && selected !in visibleSlugs) {
            navigator.navigateBack()
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                listPane { slug -> scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, slug) } }
            }
        },
        detailPane = {
            AnimatedPane {
                val slug = navigator.currentDestination?.contentKey
                if (slug != null) {
                    detailPane(slug, onClose)
                } else {
                    DetailPanePlaceholder()
                }
            }
        }
    )
}
```

(Add `import androidx.compose.runtime.LaunchedEffect`.)

Remove the `@Suppress("UnusedParameter")` and the dummy `unused` line.

- [ ] **Step 4: Run all scaffold tests**

Run: `./gradlew :app:connectedDebugAndroidTest --tests com.kanyandula.nyasa.ui.main.blog.composables.BlogListDetailScaffoldTest`
Expected: all four tests PASS.

- [ ] **Step 5: Code-quality gates**

Run: `./gradlew :app:detekt :app:spotlessCheck :app:lintDebug`
Expected: all `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffold.kt \
        app/src/androidTest/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogListDetailScaffoldTest.kt
git commit -m "feat(h7): scaffold wrapper — search detail-clear (Q4 D rule)"
```

---

## Task 7: Wire `BlogListDetailScaffold` into `BLOG_FEED` and `BLOG_SEARCH`

The wrapper exists and is fully tested. Now it replaces the bare `BlogFeedScreen` calls in the two feed routes. `BLOG_DETAIL` is **not** touched.

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/navigation/MainNavGraph.kt:38-71` (BLOG_FEED + BLOG_SEARCH composables)

- [ ] **Step 1: Add the wrapper imports**

At the top of `MainNavGraph.kt`, with the other `com.kanyandula.nyasa.ui.main.blog.composables` imports, add:

```kotlin
import com.kanyandula.nyasa.ui.main.blog.composables.BlogFeedAction
import com.kanyandula.nyasa.ui.main.blog.composables.BlogListDetailScaffold
```

Also add Compose state imports if not already present:

```kotlin
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
```

- [ ] **Step 2: Wrap the `BLOG_FEED` composable body**

Replace the current `BLOG_FEED` composable (lines 38–54):

```kotlin
            composable(Routes.BLOG_FEED) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                }
                val vm: BlogViewModel = hiltViewModel(parentEntry)
                val state by vm.viewState.collectAsStateWithLifecycle()

                val feedAction = remember(vm, navController) {
                    handleBlogFeedAction(vm, navController)
                }
                BlogFeedScreen(
                    pagingDataFlow = vm.pagingDataFlow,
                    state = state,
                    mode = FeedMode.Home,
                    onAction = feedAction
                )
            }
```

With:

```kotlin
            composable(Routes.BLOG_FEED) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                }
                val vm: BlogViewModel = hiltViewModel(parentEntry)
                val state by vm.viewState.collectAsStateWithLifecycle()
                val accountVm: AccountViewModel = hiltViewModel()
                val accountState by accountVm.viewState.collectAsStateWithLifecycle()
                LaunchedEffect(accountState.accountProperties?.username) {
                    accountState.accountProperties?.username?.let { vm.setCurrentUsername(it) }
                }

                val feedAction = remember(vm, navController) {
                    handleBlogFeedAction(vm, navController)
                }

                var visibleSlugs by rememberSaveable { mutableStateOf(emptySet<String>()) }

                BlogListDetailScaffold(
                    onNavigateToDetailFullScreen = { slug ->
                        navController.navigate(Routes.blogDetail(slug))
                    },
                    visibleSlugs = visibleSlugs,
                    mode = FeedMode.Home,
                    listPane = { onBlogClicked ->
                        BlogFeedScreen(
                            pagingDataFlow = vm.pagingDataFlow,
                            state = state,
                            mode = FeedMode.Home,
                            onAction = { action ->
                                when (action) {
                                    is BlogFeedAction.BlogClicked -> onBlogClicked(action.slug)
                                    else -> feedAction(action)
                                }
                            },
                            onVisibleSlugsChanged = { visibleSlugs = it }
                        )
                    },
                    detailPane = { slug, onClose ->
                        BlogDetailRoute(
                            slug = slug,
                            viewModel = vm,
                            onNavigateBack = onClose,
                            onEdit = { blogSlug -> navController.navigate(Routes.blogEdit(blogSlug)) },
                            onDeleted = onClose,
                            onAuthorClick = { username ->
                                navController.navigate(Routes.authorProfile(username))
                            }
                        )
                    }
                )
            }
```

(The `accountVm` block is hoisted from the existing `BLOG_DETAIL` route at lines 81–87 because `BlogDetailRoute` runs inside the scaffold detail pane on Medium and depends on `vm.setCurrentUsername` for the bookmark-as-author check. The same hoisting is duplicated into `BLOG_SEARCH` below — they cannot share via `rememberSaveable` because they're separate destinations.)

- [ ] **Step 3: Wrap the `BLOG_SEARCH` composable body**

Replace the current `BLOG_SEARCH` composable (lines 55–71) with:

```kotlin
            composable(Routes.BLOG_SEARCH) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                }
                val vm: BlogViewModel = hiltViewModel(parentEntry)
                val state by vm.viewState.collectAsStateWithLifecycle()
                val accountVm: AccountViewModel = hiltViewModel()
                val accountState by accountVm.viewState.collectAsStateWithLifecycle()
                LaunchedEffect(accountState.accountProperties?.username) {
                    accountState.accountProperties?.username?.let { vm.setCurrentUsername(it) }
                }

                val feedAction = remember(vm, navController) {
                    handleBlogFeedAction(vm, navController)
                }

                var visibleSlugs by rememberSaveable { mutableStateOf(emptySet<String>()) }

                BlogListDetailScaffold(
                    onNavigateToDetailFullScreen = { slug ->
                        navController.navigate(Routes.blogDetail(slug))
                    },
                    visibleSlugs = visibleSlugs,
                    mode = FeedMode.Search,
                    listPane = { onBlogClicked ->
                        BlogFeedScreen(
                            pagingDataFlow = vm.pagingDataFlow,
                            state = state,
                            mode = FeedMode.Search,
                            onAction = { action ->
                                when (action) {
                                    is BlogFeedAction.BlogClicked -> onBlogClicked(action.slug)
                                    else -> feedAction(action)
                                }
                            },
                            onVisibleSlugsChanged = { visibleSlugs = it }
                        )
                    },
                    detailPane = { slug, onClose ->
                        BlogDetailRoute(
                            slug = slug,
                            viewModel = vm,
                            onNavigateBack = onClose,
                            onEdit = { blogSlug -> navController.navigate(Routes.blogEdit(blogSlug)) },
                            onDeleted = onClose,
                            onAuthorClick = { username ->
                                navController.navigate(Routes.authorProfile(username))
                            }
                        )
                    }
                )
            }
```

The `BLOG_DETAIL` composable that follows (lines 72–100) stays exactly as it is.

- [ ] **Step 4: Build + lint**

Run: `./gradlew :app:detekt :app:spotlessCheck :app:lintDebug :app:assembleDebug`
Expected: all `BUILD SUCCESSFUL`. The `BLOG_GRAPH` parent-entry-scoped `BlogViewModel` is reachable from inside the scaffold detail slot because the `composable` block establishes the parent-entry context for the entire subtree.

- [ ] **Step 5: Run the full instrumented suite**

Run: `./gradlew :app:connectedDebugAndroidTest`
Expected: PASS — including pre-existing nav tests.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/navigation/MainNavGraph.kt
git commit -m "feat(h7): wire BlogListDetailScaffold into BLOG_FEED and BLOG_SEARCH"
```

---

## Task 8: Manual device verification

Mandatory before merge per the architecture spec §7.2. Compose UI tests verify the wrapper logic; only an emulator can verify Material's pane directive at real widths and the scaffold's `BackHandler` integration.

- [ ] **Step 1: Build the debug APK and install on each device/emulator below**

Run: `./gradlew :app:installDebug` for each AVD/device in turn.

- [ ] **Step 2: Verify the Phase 1 (Small) path is unchanged**

| Device | Steps | Expected |
|---|---|---|
| Pixel 7 (411dp) | Open feed → tap a post → press back → re-open. | Full-screen detail; back returns to feed. No scaffold artifacts (no two-pane layout, no placeholder). |
| Pixel Fold folded (~391dp) | Same as above. | Identical to Pixel 7. |

If either fails, the wrapper's `if (!isMedium) { listPane(onNavigateToDetailFullScreen); return }` early-return is broken. Stop and fix.

- [ ] **Step 3: Verify the Medium single-pane path (Pixel Fold unfolded ~840dp portrait, Pixel Tablet portrait)**

| Device | Steps | Expected |
|---|---|---|
| Pixel Fold unfolded portrait | Open feed → tap a post → press back. Repeat with `BLOG_SEARCH`. | Single-pane swap: list → detail → list. Side rail visible the whole time. Back collapses detail to list (placeholder NOT shown — Material's directive is single-pane on this width). |
| Pixel Tablet portrait (800dp) | Same. | Same as Pixel Fold unfolded portrait. |
| Either, with rotation | Tap a post on phone-width → rotate to landscape Medium → back. | Selection survives. Layout adjusts via the directive. |

- [ ] **Step 4: Verify the Medium dual-pane path (Pixel Tablet landscape, Pixel Fold landscape)**

| Device | Steps | Expected |
|---|---|---|
| Pixel Tablet landscape (1280×800) | Open feed → tap a post → press back. | Dual-pane: list always visible. Tap fills the detail pane. Back collapses to list + placeholder. |
| Pixel Tablet landscape | Tap Edit inside the detail pane. | Full-screen `EditBlogRoute`. Back returns to dual-pane with the same selection. |
| Pixel Tablet landscape | Tap the author link inside the detail pane. | Full-screen `AuthorProfileRoute`. Back returns to dual-pane with the same selection. |
| Pixel Tablet landscape | Trigger Delete → confirm. | Detail pane collapses to placeholder; list remains visible. **No** unexpected `BLOG_GRAPH` pop. |

- [ ] **Step 5: Verify search detail-clear (Q4 D rule)**

| Device | Steps | Expected |
|---|---|---|
| Pixel Tablet landscape | Open `BLOG_SEARCH` → query "lake" → tap a result → query "xyzzz" (no matches). | Detail pane clears to placeholder. List shows empty state. |
| Pixel Tablet landscape | Same flow but in `BLOG_FEED` (Home): scroll past the selected post until paging windows it out. | Detail pane stays — Home does NOT clear on scroll. |

- [ ] **Step 6: Verify configuration-change ergonomics on the boundary (Pixel Fold)**

| Device | Steps | Expected |
|---|---|---|
| Pixel Fold | Read on folded view → unfold → read on unfolded view → fold again. | Selection preserved. Scroll position preserved. No crashes. |

- [ ] **Step 7: Capture device screenshots**

Capture at least: phone full-screen detail, tablet landscape dual-pane (with selection), tablet landscape with placeholder, fold-unfold transition. Attach to the PR description.

- [ ] **Step 8: Final pre-PR gates**

```bash
./gradlew detekt spotlessCheck lintDebug
./gradlew :app:test :app:connectedDebugAndroidTest
./gradlew :app:assembleDebug
```

Expected: all `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit (manual verification log)**

```bash
git commit --allow-empty -m "test(h7): manual device verification — Phase 2 list-detail"
```

(`--allow-empty` because this commit records that step 2–7 were performed; no source change.)

---

## Task 9: Open the PR

- [ ] **Step 1: Push the branch**

```bash
git push -u origin hardening/h7-phase2
```

- [ ] **Step 2: Open the PR**

```bash
gh pr create --base Deploy_0.01 --head hardening/h7-phase2 \
  --title "feat(h7): Phase 2 list-detail — NavigableListDetailPaneScaffold for Feed + Search" \
  --body "$(cat <<'EOF'
## Summary

- Wires `NavigableListDetailPaneScaffold` into `BLOG_FEED` and `BLOG_SEARCH` per Option β in `2026-04-28-h7-phase2-architecture-design.md`.
- On Small, behavior is unchanged. On Medium, the wrapper intercepts `BlogFeedAction.BlogClicked` and drives the scaffold's internal navigator; `BLOG_DETAIL` is never entered.
- Search detail-clear (Q4 D rule) operates on the scaffold navigator only.

## Deviations from spec (intentional)

- Skipped `BlogViewModel.visibleSlugsFlow`. Used wrapper-local snapshot lift via a new `BlogFeedScreen.onVisibleSlugsChanged` callback (architecture spec §8 Plan B).
- `handleBlogFeedAction` not refactored — the wrapper intercepts `BlogClicked` inside the `listPane` slot.

## Test plan

- [x] Compose UI tests in `BlogListDetailScaffoldTest` (Compact, Medium, search-clear, home-keep) and `DetailPanePlaceholderTest`
- [x] Pixel 7 + Pixel Fold folded — phone path unchanged
- [x] Pixel Fold unfolded + Pixel Tablet portrait — single-pane swap
- [x] Pixel Tablet landscape — dual-pane, Edit/Author/Delete from detail pane behave correctly
- [x] Search query change clears detail; scroll in Home does NOT clear detail
- [x] Fold/unfold mid-read preserves selection and scroll
EOF
)"
```

- [ ] **Step 3: Watch CI**

Run: `gh pr checks --watch`
Expected: all checks PASS.

---

## Self-review checklist (used during plan authoring)

1. **Spec coverage:**
   - Architecture §3.1 two-path → Tasks 4 + 5 ✓
   - §3.2 directive tiers → Material default in Task 5 (no plan code; just the `calculatePaneScaffoldDirective` call) ✓
   - §4 file changes → Tasks 1 (gradle), 2 (placeholder), 3 (BlogFeedScreen lift), 5 (scaffold), 7 (MainNavGraph) ✓
   - §6.1 initial placeholder → Task 5 detail slot fallback ✓
   - §6.2 search detail-clear → Task 6 ✓
   - §6.3 Edit/Author/Delete on Medium → Task 7 wiring (`onClose` → `onNavigateBack` and `onDeleted`) ✓
   - §6.4 back-press → scaffold's built-in `BackHandler` (no plan code) + Task 8 device verification ✓
   - §7 verification → Task 8 ✓
   - §8 visibleSlugsFlow open question → resolved at plan time as "wrapper-local lift via `onVisibleSlugsChanged`" ✓
   - Visual spec §5.1 phase split → Phase 2 ships two-pane only; meta column is Phase 3, not in this plan ✓

2. **Placeholder scan:** No "TBD"/"add appropriate"/"similar to Task N" anywhere. Every code step shows the actual code.

3. **Type consistency:** `onClose: () -> Unit` reused identically in scaffold sig, detail slot type, and `MainNavGraph` wiring. `onVisibleSlugsChanged: (Set<String>) -> Unit` matches at call-site and definition. `FeedMode.Home` / `FeedMode.Search` (not `Feed`) used consistently.

---

**Plan complete.** Ready to execute.
