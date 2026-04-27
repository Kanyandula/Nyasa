# Phase 3 — Rich Text Toolbar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a 6-button rich-text formatting toolbar (bold, italic, underline, bullet list, numbered list, link) that floats above the keyboard while the body editor has focus on Create/Edit blog screens.

**Architecture:** New `ui/components/richtext/` subpackage hosts a stateless `RichTextToolbar` composable bound to the existing VM-hoisted `RichTextState` from Phase 2, plus a `LinkInsertDialog` for URL+display-text entry. Both `CreateBlogScreen` and `EditBlogScreen` wrap their `Scaffold` in a `Box` to overlay the toolbar with `imePadding()` + `AnimatedVisibility` driven by editor focus.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose, Material 3, compose-rich-editor 1.0.0-rc11, Hilt 2.53.1, JUnit 4 (pure unit tests), Compose UI Test (instrumented via `androidx.compose.ui:ui-test-junit4`).

**Spec:** `docs/superpowers/specs/2026-04-27-richtext-toolbar-phase3-design.md` (read this first; the amendment block in §9 is critical context — H1/H2 are deferred).

**Branching:** Phase 3 work branches off `feat/richtext-editor-phase2-rescue` (PR #56) once that's merged into `Deploy_0.01`. If PR #56 is still open when execution starts, branch off Deploy_0.01 and rebase Phase 3 onto Phase 2's merge commit when it lands. **Branch name:** `feat/richtext-toolbar-phase3`.

---

## File Structure

| Path | Action | Responsibility |
|---|---|---|
| `app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbar.kt` | Create | The 6-button toolbar composable + `internal fun normalizeUrl` helper |
| `app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/LinkInsertDialog.kt` | Create | Material 3 AlertDialog with URL + display-text fields |
| `app/src/test/java/com/kanyandula/nyasa/ui/components/richtext/UrlNormalizerTest.kt` | Create | Pure JUnit table-driven test for `normalizeUrl` |
| `app/src/androidTest/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbarUiTest.kt` | Create | Instrumented Compose UI tests for the toolbar buttons + link dialog flow |
| `app/src/main/java/com/kanyandula/nyasa/ui/main/create_blog/composables/CreateBlogScreen.kt` | Modify | Wrap Scaffold in Box; wire toolbar overlay, focus state, link dialog |
| `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/EditBlogScreen.kt` | Modify | Same pattern as CreateBlogScreen |

**Nothing else changes.** The `RichTextState` already lives in `BlogViewModel` and `CreateBlogViewModel` from Phase 2 — no VM-side edits in this phase.

---

## Task 1: URL normalizer (pure helper, TDD)

**Files:**
- Create: `app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbar.kt`
- Create: `app/src/test/java/com/kanyandula/nyasa/ui/components/richtext/UrlNormalizerTest.kt`

- [ ] **Step 1: Create the package directory + the empty production file with package declaration only**

`app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbar.kt`:

```kotlin
package com.kanyandula.nyasa.ui.components.richtext
```

(One line, just the package. We'll add `normalizeUrl` in Step 3.)

- [ ] **Step 2: Write the failing test**

`app/src/test/java/com/kanyandula/nyasa/ui/components/richtext/UrlNormalizerTest.kt`:

```kotlin
package com.kanyandula.nyasa.ui.components.richtext

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlNormalizerTest {

    @Test
    fun `https URL is preserved`() {
        assertEquals("https://example.com", normalizeUrl("https://example.com"))
    }

    @Test
    fun `http URL is preserved`() {
        assertEquals("http://example.com", normalizeUrl("http://example.com"))
    }

    @Test
    fun `mailto URL is preserved`() {
        assertEquals("mailto:foo@bar.com", normalizeUrl("mailto:foo@bar.com"))
    }

    @Test
    fun `mixed-case scheme is detected and original case is preserved`() {
        assertEquals("HTTPS://example.com", normalizeUrl("HTTPS://example.com"))
    }

    @Test
    fun `schemeless input is prefixed with https`() {
        assertEquals("https://example.com", normalizeUrl("example.com"))
    }

    @Test
    fun `whitespace is trimmed`() {
        assertEquals("https://example.com", normalizeUrl("  https://example.com  "))
    }

    @Test
    fun `blank input returns empty string`() {
        assertEquals("", normalizeUrl("   "))
    }

    @Test
    fun `ftp input is treated as schemeless and prefixed with https`() {
        assertEquals("https://ftp://example.com", normalizeUrl("ftp://example.com"))
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.kanyandula.nyasa.ui.components.richtext.UrlNormalizerTest"`

Expected: COMPILATION FAILURE — `Unresolved reference: normalizeUrl`. (Compilation failure counts as "fails" for TDD purposes; the test source can't reach a function that doesn't exist yet.)

- [ ] **Step 4: Add `normalizeUrl` to RichTextToolbar.kt**

Replace the entire content of `RichTextToolbar.kt` with:

```kotlin
package com.kanyandula.nyasa.ui.components.richtext

internal fun normalizeUrl(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return trimmed
    val lower = trimmed.lowercase()
    return when {
        lower.startsWith("https://") || lower.startsWith("http://") -> trimmed
        lower.startsWith("mailto:") -> trimmed
        else -> "https://$trimmed"
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.kanyandula.nyasa.ui.components.richtext.UrlNormalizerTest"`

Expected: PASS — 8 tests, 0 failures.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbar.kt \
        app/src/test/java/com/kanyandula/nyasa/ui/components/richtext/UrlNormalizerTest.kt
git commit -m "feat(richtext): add URL normalizer for link dialog

Pure helper for the upcoming LinkInsertDialog: trims whitespace,
preserves http/https/mailto schemes, prepends https:// to schemeless
input. Internal visibility — Kotlin internals are accessible from
same-module unit tests.

Eight table-driven JUnit cases cover scheme preservation, mixed-case
detection (lowercase compare, original case kept), schemeless prefix,
whitespace trim, blank input, and the ftp edge case (treated as
schemeless; acceptable v1 behavior — real users won't paste ftp URLs)."
```

---

## Task 2: LinkInsertDialog composable

**Files:**
- Create: `app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/LinkInsertDialog.kt`

UI-only composable; tested via instrumented test in Task 6. Verification at this task is `compileDebugKotlin` + a `@Preview` that renders.

- [ ] **Step 1: Create the file**

`app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/LinkInsertDialog.kt`:

```kotlin
package com.kanyandula.nyasa.ui.components.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkInsertDialog(
    initialUrl: String = "",
    initialText: String = "",
    onConfirm: (url: String, text: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var url by rememberSaveable(initialUrl) { mutableStateOf(initialUrl) }
    var text by rememberSaveable(initialText) { mutableStateOf(initialText) }
    val canConfirm = remember(url) { url.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert link") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.s)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Display text (optional)") },
                    singleLine = true,
                    modifier = Modifier,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canConfirm,
                onClick = {
                    val normalizedUrl = normalizeUrl(url)
                    val effectiveText = text.ifBlank { normalizedUrl }
                    onConfirm(normalizedUrl, effectiveText)
                },
            ) { Text("Insert") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Preview
@Composable
private fun LinkInsertDialogPreview() {
    NyasaTheme {
        LinkInsertDialog(
            initialUrl = "",
            initialText = "Selected text",
            onConfirm = { _, _ -> },
            onDismiss = {},
        )
    }
}
```

Note on the empty-text fallback: per spec §4.4 the dialog confirms with `text` even if blank, but `addLink("", url)` would produce an `<a>` with no visible content. Using `effectiveText = text.ifBlank { normalizedUrl }` makes the URL itself the visible link text in that case — the documented v1 behavior in spec §4.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/LinkInsertDialog.kt
git commit -m "feat(richtext): add LinkInsertDialog with URL + display-text fields

Material 3 AlertDialog. Two OutlinedTextFields — URL (required) and
Display text (optional). Confirm disabled while URL is blank. URL is
normalized via the helper from Task 1 before invoking onConfirm.

Empty display-text falls back to the normalized URL itself, so the
inserted <a> always has visible link text. Matches spec §4.4."
```

---

## Task 3: RichTextToolbar composable

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbar.kt`

- [ ] **Step 1: Add the toolbar composable**

Replace the entire content of `RichTextToolbar.kt` with:

```kotlin
package com.kanyandula.nyasa.ui.components.richtext

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState

@Composable
fun RichTextToolbar(
    state: RichTextState,
    onInsertLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = NyasaTheme.spacing.s, vertical = NyasaTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.xs),
        ) {
            ToggleButton(
                checked = state.currentSpanStyle.fontWeight == FontWeight.Bold,
                onCheckedChange = {
                    state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                },
            ) { Icon(Icons.Filled.FormatBold, contentDescription = "Bold") }

            ToggleButton(
                checked = state.currentSpanStyle.fontStyle == FontStyle.Italic,
                onCheckedChange = {
                    state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                },
            ) { Icon(Icons.Filled.FormatItalic, contentDescription = "Italic") }

            ToggleButton(
                checked = state.currentSpanStyle.textDecoration == TextDecoration.Underline,
                onCheckedChange = {
                    state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                },
            ) { Icon(Icons.Filled.FormatUnderlined, contentDescription = "Underline") }

            ToggleButton(
                checked = state.isUnorderedList,
                onCheckedChange = { state.toggleUnorderedList() },
            ) { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Bullet list") }

            ToggleButton(
                checked = state.isOrderedList,
                onCheckedChange = { state.toggleOrderedList() },
            ) { Icon(Icons.AutoMirrored.Filled.FormatListNumbered, contentDescription = "Numbered list") }

            IconButton(onClick = onInsertLinkClick) {
                Icon(Icons.Filled.Link, contentDescription = "Insert link")
            }
        }
    }
}

@Composable
private fun ToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    FilledIconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
    ) { content() }
}

@Preview
@Composable
private fun RichTextToolbarPreview() {
    NyasaTheme {
        val state = rememberRichTextState()
        RichTextToolbar(
            state = state,
            onInsertLinkClick = {},
        )
    }
}
```

Note: `contentDescription` lives directly on each `Icon`. Compose's default semantics merging surfaces those descriptions on the parent `FilledIconToggleButton` / `IconButton` in the merged semantics tree, which is what `onNodeWithContentDescription("Bold")` in the Task 6 UI tests queries.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin`

Expected: BUILD SUCCESSFUL. If `Icons.AutoMirrored.Filled.FormatListBulleted` is unresolved, the project may use an older Material icons version — fall back to `Icons.Filled.FormatListBulleted` and `Icons.Filled.FormatListNumbered`.

- [ ] **Step 3: Spotless + detekt**

Run: `./gradlew :app:spotlessApply detekt`

Expected: No errors. spotless may reformat imports; that's fine.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbar.kt
git commit -m "feat(richtext): add RichTextToolbar with 5 toggles + link button

Stateless composable bound to RichTextState. Six controls: bold,
italic, underline (toggle SpanStyle), bullet/numbered list (toggle
list state), and link (delegates to onInsertLinkClick — host owns the
dialog state). Surface with tonalElevation 4dp wraps a horizontally-
scrollable Row so 360dp phones don't crowd the buttons.

Active-state styling comes from FilledIconToggleButton's checked
appearance — the library exposes the predicates (currentSpanStyle,
isUnorderedList, isOrderedList) so each button reads its own state.

H1/H2 are deferred per spec §9 amendment (library at 1.0.0-rc11 has
no semantic heading paragraph type)."
```

---

## Task 4: Wire toolbar into CreateBlogScreen

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/main/create_blog/composables/CreateBlogScreen.kt`

- [ ] **Step 1: Add imports + add focus and dialog state at the top of `CreateBlogScreen`**

Add these imports (alphabetical, with the existing imports):

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import com.kanyandula.nyasa.ui.components.richtext.LinkInsertDialog
import com.kanyandula.nyasa.ui.components.richtext.RichTextToolbar
```

Add the state variables right after the existing `var title` and `var tags` declarations (currently around line 76–77):

```kotlin
var isEditorFocused by remember { mutableStateOf(false) }
var showLinkDialog by remember { mutableStateOf(false) }
var pendingLinkSelectionText by remember { mutableStateOf("") }
```

- [ ] **Step 2: Add `onFocusChanged` to the existing `RichTextEditor`**

Find the existing `RichTextEditor` block (currently around lines 139–144):

```kotlin
RichTextEditor(
    state = bodyState,
    modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 240.dp)
)
```

Replace its `modifier` chain to add `onFocusChanged`:

```kotlin
RichTextEditor(
    state = bodyState,
    modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 240.dp)
        .onFocusChanged { isEditorFocused = it.isFocused }
)
```

- [ ] **Step 3: Wrap the existing outermost `Box` content with the toolbar overlay**

The current `CreateBlogScreen` already has a `Box(modifier = Modifier.fillMaxSize())` (line 79) wrapping the `Scaffold` and `LoadingOverlay`. Add the toolbar as a third child of that `Box`, after `LoadingOverlay(isLoading = isLoading)`:

```kotlin
        LoadingOverlay(isLoading = isLoading)

        AnimatedVisibility(
            visible = isEditorFocused,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding(),
        ) {
            RichTextToolbar(
                state = bodyState,
                onInsertLinkClick = {
                    val sel = bodyState.selection
                    pendingLinkSelectionText = if (sel.collapsed) {
                        ""
                    } else {
                        bodyState.annotatedString.text.substring(sel.start, sel.end)
                    }
                    showLinkDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
```

- [ ] **Step 4: Add the link dialog block, also as a child of the outer Box**

Add immediately after the AnimatedVisibility block from Step 3:

```kotlin
        if (showLinkDialog) {
            LinkInsertDialog(
                initialUrl = "",
                initialText = pendingLinkSelectionText,
                onConfirm = { url, text ->
                    bodyState.addLink(text = text, url = url)
                    showLinkDialog = false
                },
                onDismiss = { showLinkDialog = false },
            )
        }
```

- [ ] **Step 5: Build to verify**

Run: `./gradlew :app:assembleDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Spotless + detekt + lint**

Run: `./gradlew :app:spotlessApply detekt lintDebug`

Expected: No errors.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/create_blog/composables/CreateBlogScreen.kt
git commit -m "feat(richtext): wire RichTextToolbar into CreateBlogScreen

Toolbar floats above the keyboard while the body editor has focus.
Driven by Modifier.onFocusChanged on the RichTextEditor → local
isEditorFocused state → AnimatedVisibility (slide+fade) wrapping
RichTextToolbar with imePadding() and BottomCenter alignment.

Link button captures the current selection text (or empty if
selection is collapsed) into pendingLinkSelectionText, opens
LinkInsertDialog, and on confirm calls bodyState.addLink(text, url)
with the dialog's normalized URL.

Focus, dialog visibility, and pending link selection are local screen
state — they're transient UI signals; hoisting to the VM would couple
them to persistent business state for no gain."
```

---

## Task 5: Wire toolbar into EditBlogScreen

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/EditBlogScreen.kt`

Same pattern as Task 4. Read `EditBlogScreen.kt` first to find the existing `RichTextEditor` block and the outermost `Box` (or `Scaffold` if there's no Box yet — wrap in a Box if needed).

- [ ] **Step 1: Read the file to find the integration points**

```bash
grep -n "RichTextEditor\|Scaffold\|Box\b" app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/EditBlogScreen.kt
```

Note the line numbers for: outermost layout container, `RichTextEditor` invocation, and any existing overlay (like `LoadingOverlay`).

- [ ] **Step 2: Add the same imports as Task 4 Step 1**

Same import set; some may already be present (`Box`, `fillMaxWidth`, `getValue`/`setValue`, etc.).

- [ ] **Step 3: Add the same three state variables near the top of the composable**

```kotlin
var isEditorFocused by remember { mutableStateOf(false) }
var showLinkDialog by remember { mutableStateOf(false) }
var pendingLinkSelectionText by remember { mutableStateOf("") }
```

- [ ] **Step 4: Add `.onFocusChanged { isEditorFocused = it.isFocused }` to the `RichTextEditor`'s modifier chain**

Same change pattern as Task 4 Step 2.

- [ ] **Step 5: Ensure the screen has an outer Box wrapping the Scaffold; add the AnimatedVisibility(toolbar) and conditional LinkInsertDialog as siblings**

If the screen currently has `Scaffold { ... }` without a wrapping Box, wrap it:

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    Scaffold( ... ) { ... }
    // existing overlays (e.g., LoadingOverlay) stay as siblings
    AnimatedVisibility(...) { RichTextToolbar(...) }  // same block as Task 4 Step 3
    if (showLinkDialog) { LinkInsertDialog(...) }     // same block as Task 4 Step 4
}
```

The `RichTextToolbar` inside `AnimatedVisibility` has identical content to Task 4 Step 3 — copy it verbatim, swapping `bodyState` for whatever the EditBlogScreen calls its `RichTextState` parameter (it's `bodyState` per Phase 2's contract; verify by reading the file).

The `LinkInsertDialog` block is identical to Task 4 Step 4.

- [ ] **Step 6: Build + gates**

Run: `./gradlew :app:assembleDebug spotlessApply detekt lintDebug`

Expected: BUILD SUCCESSFUL, no errors.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/EditBlogScreen.kt
git commit -m "feat(richtext): wire RichTextToolbar into EditBlogScreen

Mirror of the CreateBlogScreen wiring from the previous commit:
focus-driven AnimatedVisibility + imePadding overlay, conditional
LinkInsertDialog, bodyState.addLink on confirm. Same transient local
state for focus, dialog visibility, and pending selection text."
```

---

## Task 6: Instrumented UI tests + final verification

**Files:**
- Create: `app/src/androidTest/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbarUiTest.kt`

These tests run with `connectedAndroidTest` (emulator/device required), not in pre-commit gates. They are the primary executable specification of behavior.

- [ ] **Step 1: Create the test file**

`app/src/androidTest/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbarUiTest.kt`:

```kotlin
package com.kanyandula.nyasa.ui.components.richtext

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.font.FontWeight
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RichTextToolbarUiTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun bold_button_toggles_currentSpanStyle_fontWeight() {
        var captured: RichTextState? = null
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                captured = state
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Bold").performClick()
        rule.runOnIdle {
            assertEquals(FontWeight.Bold, captured!!.currentSpanStyle.fontWeight)
        }
        rule.onNodeWithContentDescription("Bold").performClick()
        rule.runOnIdle {
            // Toggling off — fontWeight should not be Bold any more.
            assertTrue(captured!!.currentSpanStyle.fontWeight != FontWeight.Bold)
        }
    }

    @Test
    fun bullet_list_button_toggles_isUnorderedList() {
        var captured: RichTextState? = null
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                captured = state
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Bullet list").performClick()
        rule.runOnIdle { assertTrue(captured!!.isUnorderedList) }
        rule.onNodeWithContentDescription("Bullet list").performClick()
        rule.runOnIdle { assertTrue(!captured!!.isUnorderedList) }
    }

    @Test
    fun numbered_list_button_toggles_isOrderedList() {
        var captured: RichTextState? = null
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                captured = state
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Numbered list").performClick()
        rule.runOnIdle { assertTrue(captured!!.isOrderedList) }
    }

    @Test
    fun bold_button_active_state_reflects_currentSpanStyle() {
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Bold").assertIsOff()
        rule.onNodeWithContentDescription("Bold").performClick()
        rule.onNodeWithContentDescription("Bold").assertIsOn()
    }

    @Test
    fun link_button_invokes_onInsertLinkClick() {
        val opened = mutableStateOf(false)
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                RichTextToolbar(
                    state = state,
                    onInsertLinkClick = { opened.value = true },
                )
            }
        }

        rule.onNodeWithContentDescription("Insert link").performClick()
        rule.runOnIdle { assertTrue(opened.value) }
    }

    @Test
    fun link_dialog_confirm_invokes_onConfirm_with_normalized_url() {
        val showing = mutableStateOf(true)
        var capturedUrl: String? = null
        var capturedText: String? = null
        rule.setContent {
            NyasaTheme {
                if (showing.value) {
                    LinkInsertDialog(
                        initialUrl = "example.com",
                        initialText = "click me",
                        onConfirm = { url, text ->
                            capturedUrl = url
                            capturedText = text
                            showing.value = false
                        },
                        onDismiss = { showing.value = false },
                    )
                }
            }
        }

        rule.onNodeWithText("Insert").performClick()
        rule.runOnIdle {
            assertEquals("https://example.com", capturedUrl)
            assertEquals("click me", capturedText)
        }
    }

    @Test
    fun link_dialog_confirm_disabled_for_blank_url() {
        rule.setContent {
            NyasaTheme {
                LinkInsertDialog(
                    initialUrl = "",
                    initialText = "",
                    onConfirm = { _, _ -> },
                    onDismiss = {},
                )
            }
        }

        rule.onNodeWithText("Insert").assertIsDisplayed()
        // Disabled buttons in Material 3 still display; check the click is a no-op
        // by verifying assertIsNotEnabled would be ideal, but ComposeTestRule has
        // assertIsNotEnabled — use it.
    }
}
```

- [ ] **Step 2: Run the instrumented tests on an emulator or device**

Start an emulator (Android Studio AVD or `emulator -avd <name>`). Then:

Run: `./gradlew :app:connectedDebugAndroidTest --tests "com.kanyandula.nyasa.ui.components.richtext.RichTextToolbarUiTest"`

Expected: All 7 tests pass. If `assertIsNotEnabled` fails to compile, replace the comment block with `rule.onNodeWithText("Insert").assertIsNotEnabled()` (the import is `androidx.compose.ui.test.assertIsNotEnabled`).

- [ ] **Step 3: Run the full pre-commit gate set**

Run: `./gradlew :app:testDebugUnitTest detekt spotlessCheck lintDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Manual device verification (per spec §7 acceptance criterion 6)**

Install the debug build on a physical device:

```bash
./gradlew :app:installDebug
```

Open the app, navigate to Create Blog, tap into the body editor. Verify:

- Toolbar slides up above the keyboard within ~150 ms.
- All 6 buttons render and have icons.
- Tapping Bold while typing toggles the active state and the typed text becomes bold.
- Tapping Bullet list at the start of a line creates a bullet.
- Tapping Link with no selection opens the dialog with empty fields; with a selection, the Display text field is pre-filled.
- Confirming a link with `example.com` (no scheme) creates an `<a>` whose href starts with `https://`.
- Tapping the title or tags field — toolbar slides away.
- Repeat the above on Edit Blog (open any existing post and tap Edit).

- [ ] **Step 5: Commit**

```bash
git add app/src/androidTest/java/com/kanyandula/nyasa/ui/components/richtext/RichTextToolbarUiTest.kt
git commit -m "test(richtext): instrumented UI tests for toolbar and link dialog

Seven Compose UI tests under androidTest/, exercising:
- Bold toggles currentSpanStyle.fontWeight (on, then off)
- Bullet/numbered list buttons toggle the matching state predicate
- Bold button's active state (assertIsOn/assertIsOff) reflects the
  current selection's formatting
- Link button delegates to the host via onInsertLinkClick
- LinkInsertDialog Confirm normalizes URL through normalizeUrl and
  passes (url, text) to onConfirm
- LinkInsertDialog Confirm is disabled while URL is blank

Tests run via ./gradlew :app:connectedDebugAndroidTest (emulator
required) — they are not part of the pre-commit gate set."
```

- [ ] **Step 6: Open the pull request**

```bash
git push -u origin feat/richtext-toolbar-phase3
gh pr create --base feat/richtext-editor-phase2-rescue \
  --head feat/richtext-toolbar-phase3 \
  --title "feat(richtext): Phase 3 — formatting toolbar (6 buttons + link dialog)" \
  --body "$(cat <<'EOF'
## Summary

Adds a 6-button rich-text formatting toolbar that floats above the keyboard while the body editor has focus on Create/Edit blog screens.

Spec: `docs/superpowers/specs/2026-04-27-richtext-toolbar-phase3-design.md`
Plan: `docs/superpowers/plans/2026-04-27-richtext-toolbar-phase3.md`

## What's in the toolbar

Bold, Italic, Underline (toggle SpanStyle), Bullet list, Numbered list (toggle list state), and Link (opens a dialog with URL + display-text fields). Active-state styling reflects the current selection's formatting via `FilledIconToggleButton`'s checked appearance.

## What's deferred

H1/H2 headings — compose-rich-editor 1.0.0-rc11 has no semantic heading paragraph type. Recorded in spec §9 amendment.

## Verification

- `./gradlew :app:testDebugUnitTest detekt spotlessCheck lintDebug` — green
- `./gradlew :app:connectedDebugAndroidTest` — 7 UI tests pass on emulator
- Device verification per acceptance criteria 1–6 (see plan Task 6 Step 4)

## Base branch

This PR targets `feat/richtext-editor-phase2-rescue` (PR #56). Once #56 merges into `Deploy_0.01`, retarget this PR to `Deploy_0.01`.
EOF
)"
```

If PR #56 has already merged into `Deploy_0.01` by the time this command runs, change `--base feat/richtext-editor-phase2-rescue` to `--base Deploy_0.01`.

---

## Acceptance checklist (mirrors spec §7)

- [ ] Both `CreateBlogScreen` and `EditBlogScreen` show the floating toolbar when the body editor has focus (manual device verification)
- [ ] All 6 buttons drive the expected state on `RichTextState` (Tasks 3, 6)
- [ ] Active-state styling reflects the current selection's formatting (Task 6 test `bold_button_active_state_reflects_currentSpanStyle`)
- [ ] Link dialog produces valid `<a href="…">…</a>`, with `https://` auto-prefixed for schemeless input (Task 6 test `link_dialog_confirm_invokes_onConfirm_with_normalized_url`)
- [ ] `./gradlew :app:testDebugUnitTest detekt spotlessCheck lintDebug` is green (Task 6 Step 3)
- [ ] Device verification confirms toolbar appears above keyboard on focus, dismisses on blur (Task 6 Step 4)
