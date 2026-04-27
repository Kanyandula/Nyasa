# Phase 3 — Rich Text Toolbar Design

**Date:** 2026-04-27
**Status:** Approved (brainstorm complete; awaiting user review of spec before implementation planning)
**Predecessors:** Phase 1 (`78eaf4c` — library + smoke test), Phase 2 (`de33af4`/`345d4b8` — VM-hoisted `RichTextState`, body field migrated; PR #56 rescue)
**Branch this targets:** `feat/richtext-editor-phase2-rescue` (Phase 3 work will branch off Phase 2's head)

---

## 1. Goal

Add a structured formatting toolbar to the body editor in `CreateBlogScreen` and `EditBlogScreen`. The toolbar replaces the legacy markdown-emitting `FormattingToolbar` deleted in Phase 2 and binds directly to `RichTextState` APIs from compose-rich-editor 1.0.0-rc11.

After Phase 3, authors will be able to apply bold, italic, underline, two heading levels, ordered/unordered lists, and links from a phone-friendly toolbar that floats above the keyboard while the body editor has focus.

## 2. Scope

### In scope

8 buttons, single horizontally-scrollable row:

| Order | Button | API |
|---|---|---|
| 1 | Bold | `state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))` |
| 2 | Italic | `state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))` |
| 3 | Underline | `state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))` |
| 4 | H1 | library heading toggle (exact API to be confirmed during implementation against compose-rich-editor 1.0.0-rc11; candidates: `toggleSpanStyle` with H1 fontSize/weight, `toggleParagraphStyle`, or a dedicated heading helper) |
| 5 | H2 | same approach as H1, with H2 sizing |
| 6 | Bullet list | `state.toggleUnorderedList()` |
| 7 | Numbered list | `state.toggleOrderedList()` |
| 8 | Link | opens `LinkInsertDialog` → `state.addLinkToSelection(text, url)` |

### Out of scope (deferred)

- Inline images, embeds, code blocks (multi-line), blockquotes, strikethrough, H3+ headings.
- Undo/redo (compose-rich-editor 1.0.0-rc11 does not expose an undo stack).
- "Clear formatting" button.
- Per-device IME edge cases (foldables, gesture vs. 3-button nav). Will surface in device verification, not at design time.

## 3. Architecture

### 3.1 New package

`app/src/main/java/com/kanyandula/nyasa/ui/components/richtext/`

Mirrors the existing `components/htmlrenderer/` precedent for richtext-themed grouping. Two files in v1:

- `RichTextToolbar.kt` — the toolbar composable
- `LinkInsertDialog.kt` — the link-insertion dialog

### 3.2 Component signatures

**`RichTextToolbar`**

```kotlin
@Composable
fun RichTextToolbar(
    state: RichTextState,
    onInsertLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Internals:

- `Surface` with `tonalElevation = 4.dp`.
- Horizontally-scrollable `Row` of 8 `IconToggleButton`s. Scroll prevents pixel-budget pressure on 360 dp phones.
- Each button reads its own predicate from `state` (e.g., `state.currentSpanStyle.fontWeight == FontWeight.Bold`) and calls the matching mutator.
- Active-state styling: filled tonal `containerColor` when the predicate is true; flat surface when false.

**`LinkInsertDialog`**

```kotlin
@Composable
fun LinkInsertDialog(
    initialUrl: String = "",
    initialText: String = "",
    onConfirm: (url: String, text: String) -> Unit,
    onDismiss: () -> Unit,
)
```

Internals:

- Material 3 `AlertDialog`.
- Two `OutlinedTextField`s: URL and Display text.
- Confirm button disabled until URL is non-blank.
- URL is normalized via `normalizeUrl` (see §4.3) before invoking `onConfirm`.

## 4. Data flow

### 4.1 State ownership

| State | Lives in | Reason |
|---|---|---|
| `RichTextState` | `BlogViewModel.editBodyState` / `CreateBlogViewModel.createBodyState` | Already there from Phase 2; toolbar mutates it directly. |
| `isEditorFocused: Boolean` | Local screen state (`remember { mutableStateOf(false) }`) | Transient UI signal; hoisting to VM would couple it to persistent business state for no gain. |
| `showLinkDialog: Boolean` | Local screen state | Same reason — local UI state. |
| `pendingLinkSelectionText: String` | Local screen state | Captured at the moment the link button is tapped, passed to dialog as `initialText`. |

### 4.2 Visibility lifecycle

```
RichTextEditor.onFocusChanged → screen.isEditorFocused
                              → AnimatedVisibility(visible = isEditorFocused) wraps RichTextToolbar
                              → Modifier.imePadding() lifts toolbar above the IME
                              → Modifier.align(Alignment.BottomCenter) inside an outer Box
```

Toolbar is rendered inside a `Box` that also wraps the `Scaffold`, so the toolbar floats over the bottom bar without affecting the scrollable column's layout. `AnimatedVisibility` (slide + fade, ~150 ms) avoids jarring pop-in when focus moves between title, body, and tags fields.

### 4.3 URL normalization

```kotlin
private fun normalizeUrl(input: String): String {
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

Pure function, lives in `RichTextToolbar.kt` as a top-level `internal` helper for testability.

### 4.4 Link insertion flow

1. User taps link button → screen captures current selection text via `state.annotatedString.text.substring(state.selection.start, state.selection.end)` into `pendingLinkSelectionText`.
2. `showLinkDialog = true`.
3. `LinkInsertDialog` renders with `initialText = pendingLinkSelectionText`.
4. On Confirm: dialog calls `onConfirm(normalizedUrl, displayText)` → screen calls `state.addLinkToSelection(text = displayText, url = normalizedUrl)` → `showLinkDialog = false`.
5. On Dismiss: `showLinkDialog = false`, no state change.

## 5. Error handling

Intentionally minimal:

- All formatting actions are idempotent toggles on `RichTextState` — the library guarantees no exceptions for valid state.
- The only real failure mode is malformed URL input, handled by `normalizeUrl` (always returns a usable string given non-blank input).
- Confirm is disabled when URL is blank, so the empty-input branch in `normalizeUrl` is unreachable from the UI but kept for testability and defensive correctness.
- No `try`/`catch`, no `AppError` mapping. Formatting is local UI state; it never crosses the network boundary.

## 6. Testing strategy

### 6.1 Unit tests

`RichTextToolbarUrlNormalizerTest.kt` — table-driven, covering:

- Already-`https://` URL preserved.
- Already-`http://` URL preserved.
- `mailto:foo@bar` preserved.
- Mixed-case scheme (`HTTPS://example.com`) preserved (lowercase-detected, original case preserved).
- Schemeless input (`example.com`) prefixed with `https://`.
- Leading/trailing whitespace trimmed.
- Blank input returns empty string.
- `ftp://example.com` — unprefixed (treated as schemeless and prefixed with `https://`). Acceptable v1 behavior; real users won't paste ftp links into a blog editor.

### 6.2 Compose UI tests

`RichTextToolbarUiTest.kt`:

- Bold button toggles `state.currentSpanStyle.fontWeight` between Bold and default.
- Italic and Underline parallel the Bold case.
- Bullet/numbered list buttons toggle `state.isUnorderedList` / `state.isOrderedList`.
- H1/H2 buttons apply the matching heading style to the current paragraph (exact predicate depends on the API chosen during implementation; the test asserts the resulting `state.toHtml()` contains `<h1>…</h1>` or `<h2>…</h2>` after the toggle).
- Active-state appearance: each toggle button reports `SemanticsProperties.Selected = true` when the underlying state predicate is true, `false` otherwise.
- Link dialog opens on link button tap; Confirm calls `state.addLinkToSelection`; resulting `state.toHtml()` contains `<a href="https://...">...</a>`.

### 6.3 Smoke test

Extend `RichTextEditorSmokeTest.kt` with one assertion: applying Bold via the toolbar survives a `state.toHtml()` round-trip (i.e., `<b>` or `<strong>` appears in the serialized output).

### 6.4 Gates (must remain green)

```
./gradlew :app:testDebugUnitTest detekt spotlessCheck lintDebug
```

Same expectations as Phase 2.

## 7. Acceptance criteria

The Phase 3 PR is mergeable when:

1. Both `CreateBlogScreen` and `EditBlogScreen` show the floating toolbar when the body editor has focus.
2. All 8 buttons toggle the expected state on `RichTextState`.
3. Active-state styling reflects the current selection's formatting.
4. Link dialog produces valid `<a href="…">…</a>` in `state.toHtml()`, with `https://` auto-prefixed for schemeless input.
5. Unit + UI tests pass; detekt + spotlessCheck + lintDebug + testDebugUnitTest all green.
6. Device verification confirms toolbar appears above keyboard on focus, dismisses on blur.

## 8. Open questions

None as of approval. Items deferred from §2 are explicit Phase 4+ work.

## 9. Decision log

Recorded in §1–§7. Brainstorm Q1–Q6 selected by user 2026-04-27:

1. **Scope**: Standard tier (8 buttons).
2. **Placement**: Floats above keyboard via IME insets.
3. **Heading shape**: Two distinct buttons (H1, H2).
4. **Link flow**: Dialog with URL + display text fields.
5. **Active state**: Buttons show pressed/selected appearance.
6. **Component placement**: New `ui/components/richtext/` subpackage.