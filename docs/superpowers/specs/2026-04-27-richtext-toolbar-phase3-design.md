# Phase 3 — Rich Text Toolbar Design

**Date:** 2026-04-27
**Status:** Approved with amendment 2026-04-27 (H1/H2 deferred — see §9)
**Predecessors:** Phase 1 (`78eaf4c` — library + smoke test), Phase 2 (`de33af4`/`345d4b8` — VM-hoisted `RichTextState`, body field migrated; PR #56 rescue)
**Branch this targets:** `feat/richtext-editor-phase2-rescue` (Phase 3 work will branch off Phase 2's head)

---

## 1. Goal

Add a structured formatting toolbar to the body editor in `CreateBlogScreen` and `EditBlogScreen`. The toolbar replaces the legacy markdown-emitting `FormattingToolbar` deleted in Phase 2 and binds directly to `RichTextState` APIs from compose-rich-editor 1.0.0-rc11.

After Phase 3, authors will be able to apply bold, italic, underline, ordered/unordered lists, and links from a phone-friendly toolbar that floats above the keyboard while the body editor has focus. Heading buttons (H1/H2) are deferred — see §2 and §9.

## 2. Scope

### In scope

6 buttons, single horizontally-scrollable row:

| Order | Button | API |
|---|---|---|
| 1 | Bold | `state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))` |
| 2 | Italic | `state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))` |
| 3 | Underline | `state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))` |
| 4 | Bullet list | `state.toggleUnorderedList()` |
| 5 | Numbered list | `state.toggleOrderedList()` |
| 6 | Link | opens `LinkInsertDialog` → `state.addLink(text, url)` |

### Out of scope (deferred)

- **Headings (H1/H2).** compose-rich-editor 1.0.0-rc11 has no native heading paragraph type; the only available emulation is `toggleSpanStyle` with H1/H2-sized font, which serializes as `<span style="font-size: …">…</span>` rather than semantic `<h1>`/`<h2>`. The read-side renderer (`PostBodyRenderer.kt` / `HtmlParser.kt`) currently styles posts based on semantic heading tags, so non-semantic emulation would not render as section headings on the read side. Headings return in a follow-up phase, after either (a) a library upgrade that adds heading support, or (b) a paired editor + renderer change that recognizes the library's span-style heading output.
- Inline images, embeds, code blocks (multi-line), blockquotes, strikethrough.
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
- Horizontally-scrollable `Row` of 6 `IconToggleButton`s (the link button is non-toggle but uses the same icon-button affordance for visual consistency). Scroll prevents pixel-budget pressure on 360 dp phones.
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
4. On Confirm: dialog calls `onConfirm(normalizedUrl, displayText)` → screen calls `state.addLink(text = displayText, url = normalizedUrl)` → `showLinkDialog = false`.
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
- Active-state appearance: each toggle button reports `SemanticsProperties.Selected = true` when the underlying state predicate is true, `false` otherwise.
- Link dialog opens on link button tap; Confirm calls `state.addLink`; resulting `state.toHtml()` contains `<a href="https://...">...</a>`.

### 6.3 Gates (must remain green)

```
./gradlew :app:testDebugUnitTest detekt spotlessCheck lintDebug
```

Same expectations as Phase 2.

## 7. Acceptance criteria

The Phase 3 PR is mergeable when:

1. Both `CreateBlogScreen` and `EditBlogScreen` show the floating toolbar when the body editor has focus.
2. All 6 buttons drive the expected state on `RichTextState` (5 toggles + 1 link insertion).
3. Active-state styling reflects the current selection's formatting for the 5 toggle buttons.
4. Link dialog produces valid `<a href="…">…</a>` in `state.toHtml()`, with `https://` auto-prefixed for schemeless input.
5. Unit + UI tests pass; detekt + spotlessCheck + lintDebug + testDebugUnitTest all green.
6. Device verification confirms toolbar appears above keyboard on focus, dismisses on blur.

## 8. Open questions

None as of approval. Items deferred from §2 are explicit Phase 4+ work.

## 9. Decision log

Recorded in §1–§7. Brainstorm Q1–Q6 selected by user 2026-04-27:

1. **Scope**: Standard tier (originally 8 buttons; reduced to 6 — see amendment below).
2. **Placement**: Floats above keyboard via IME insets.
3. **Heading shape**: Two distinct buttons (H1, H2) — *deferred per amendment*.
4. **Link flow**: Dialog with URL + display text fields.
5. **Active state**: Buttons show pressed/selected appearance.
6. **Component placement**: New `ui/components/richtext/` subpackage.

### Amendment — 2026-04-27 (post-brainstorm, pre-plan)

While inspecting the compose-rich-editor 1.0.0-rc11 AAR ahead of writing the implementation plan, two facts emerged that contradicted spec assumptions:

1. **No semantic heading support in the library at this version.** `paragraph/type/` exposes only `DefaultParagraph`, `OrderedList`, `UnorderedList`. No `Heading` paragraph type, no `setHeadingStyle` API, and the HTML parser/encoder classes contain no `<h1>`/`<h2>` literals. The only emulation path — `toggleSpanStyle(SpanStyle(fontSize = …, fontWeight = Bold))` — serializes as `<span style="font-size: …">…</span>`, not as semantic heading tags. The read-side renderer (`PostBodyRenderer.kt` / `HtmlParser.kt`) styles headings by tag name, so non-semantic emulation would not render as section headings on the read side. Acceptance criterion 2 (`toHtml()` contains `<h1>…</h1>`) was therefore unachievable.

2. **Link API is `addLink(text, url)`, not `addLinkToSelection(text, url)`.** `addLinkToSelection` only takes a URL parameter; the two-field dialog flow requires `addLink(text, url)`, which exists on `RichTextState`.

Decision (user-approved 2026-04-27): drop H1/H2 from Phase 3 v1 → 6-button toolbar, defer headings to a follow-up phase pending library upgrade or a paired editor + renderer change. Correct the link API call to `state.addLink(text, url)`. Spec updated inline (§1, §2, §3.2, §4.4, §6.2, §7).

The original `RichTextEditorSmokeTest.kt` reference in §6.3 was also removed: that file is a `@Preview`-only composable in `main/java`, not a real test. The per-button UI tests in §6.2 supersede the round-trip assertion that §6.3 once described.