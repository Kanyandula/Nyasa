# H7 — Adaptive Tablet Layouts (Design Spec)

**Status**: design reference only. Phase 1 (`WindowClassifier` + `NyasaSideRail`) shipped 2026-04-28 in commit `3e9ae34`. This document scopes the visual contract for **Phases 2–4** (ListDetailPaneScaffold, Expanded grid + trending rail, modal Create sheet).

**Why text wireframes alongside Figma mockups**: this spec captures the design intent in code-reviewable form. Real pixel mockups also exist:

- **Figma file**: https://www.figma.com/design/uOzqW3F2w5lTCUaA4d0trH ("NyasaBlog — Adaptive Tablet (H7)") — built 2026-04-28 via Figma MCP `use_figma` Plugin API. Pages: Cover & Tokens, Phase 1 (8 frames: shell tabs × tablet+foldable), Phase 2 (2 frames: ListDetailPaneScaffold), Auth flow (8 frames: Welcome/Login/Register/Forgot × tablet+foldable). Phase 4 editor + Settings frames pending — blocked by Figma MCP rate limit on Collab seat; resume next billing window.
- **Stitch project (abandoned)**: `projects/7343295411818219574` — only a smoke-test screen succeeded; Stitch MCP timed out on every elaborate prompt (>2 min cap). Design system tokens were auto-generated there ("Editorial Excellence: The Warm Lithograph") and informed the Figma color values.

This spec is the durable design contract — Figma mockups can be regenerated from this any time the MCP rate limit resets.

---

## 1. Adaptive treatment philosophy

The `WindowClassifier` introduced in Phase 1 promotes the device to **Medium** when:
- portrait `containerSize.width ≥ 600.dp`, OR
- landscape `containerSize.height < 400.dp`.

When Medium:
- `NyasaBottomBar` is replaced by `NyasaSideRail` (80 dp wide, left edge, `surface_container_lowest`).
- Authentication screens (no nav surface) are unchanged in chrome but their content gets **centered with a max-width cap** instead of stretching edge-to-edge.
- Content-bearing screens (feed, profile, search) gain a **second column** or a **trending sidebar** so the wide canvas isn't wasted.
- Editor screens (Create / Edit Blog) get a **live preview pane** as a Phase 4 deliverable.
- Detail screens (Blog Detail) become a **`ListDetailPaneScaffold`** with the feed list on the left and the article on the right (Phase 2).

Tokens reused from Nyasa Horizon (no new tokens introduced):
- `surface` `#fef8f3` (canvas)
- `surface_container_low` `#f8f3ee` (sectioning track)
- `surface_container_lowest` `#ffffff` (cards)
- `primary` `#005275` / `primary_container` `#1B6B93` — 135° gradient on primary CTAs
- Sunset Orange `#E8883C` (`secondary_container`) — active-nav 6 dp dot indicator
- Newsreader (display, headline) + Plus Jakarta Sans (title, body, label)

Hard rules carried from Nyasa Horizon:
- **No 1 px borders** for sectioning — surface tier shifts only.
- **No drop shadows** on cards — tonal layering only.
- Body copy capped at **65 ch** even at Expanded width.

---

## 2. Wireframe legend

```
║  side-rail boundary       ┃  pane boundary in ListDetailPaneScaffold
══  app-bar (top)            ──  card / row separator-by-spacing (NOT a 1px line)
[N]  rail brand chip        (•)  Sunset Orange dot indicator under active item
✦   primary CTA gradient    ◆   hero / display headline (Newsreader)
🅵   filter chip pill        ⌖   meta (label-md, on_surface_variant)
```

Form-factor canvases used below:
- **Tablet landscape**: 1280 × 800 dp (Pixel Tablet, both dimensions ≥ 600 dp)
- **Foldable portrait**: 840 × 1080 dp (Pixel Fold inner, width ≥ 600 dp)

---

## 3. Auth flow (no rail)

### 3.1 Welcome

**Mobile source**: `1bd28921b15d427ea89f5f93b5ccb9ba`

**Tablet landscape (1280×800)** — centered card on full-bleed primary→primary_container 135° gradient:
```
┌──────────────────────────────────────────────────────────────────────┐
│                  ░░░ Lake-Malawi gradient ░░░                        │
│                                                                      │
│                  ┌───────────────── 720 dp ────────────────┐         │
│                  │  ◆ Nyasa Blog                          │         │
│                  │  Display-md Newsreader, white           │         │
│                  │                                         │         │
│                  │  Stories from the warm side of the      │         │
│                  │  internet.    (body-lg, 80% white)      │         │
│                  │                                         │         │
│                  │  ┌─────────────┐   ┌──────────────┐     │         │
│                  │  │ ✦ Get started│   │ I have an    │     │         │
│                  │  │   (gradient) │   │ account →    │     │         │
│                  │  └─────────────┘   └──────────────┘     │         │
│                  └─────────────────────────────────────────┘         │
│                                                                      │
│                  Chitenje pattern watermark @ 4% white               │
└──────────────────────────────────────────────────────────────────────┘
```

**Foldable portrait (840×1080)**: same hero card capped at 600 dp wide, CTAs **stacked** (gradient primary on top, ghost secondary below). Generous spacing-12 between elements; the watermark covers the lower third.

### 3.2 Login

**Mobile source**: `d00112e49dcf4f0391edff29605083c2`

**Tablet landscape (1280×800)** — two-column shell. Left = brand storytelling, right = compact form:
```
┌────────────── 640 dp ──────────────┬──────────────── 640 dp ────────────┐
│  primary→primary_container 135°    │  surface (#fef8f3)                  │
│  gradient bg                       │                                     │
│                                    │  ┌──────── 480 dp ────────┐         │
│  [N] Nyasa Blog                    │  │ ◆ Welcome back          │         │
│      headline-md serif white       │  │   headline-lg Newsreader│         │
│                                    │  │                         │         │
│  "Stories from the warm side of    │  │ Email                   │         │
│   the internet. Made in Malawi."   │  │ ┌─────────────────────┐ │         │
│   body-lg Newsreader white 80%     │  │ │ surface_container   │ │         │
│                                    │  │ └─────────────────────┘ │         │
│  Lake Malawi photo (full-bleed,    │  │                         │         │
│   warm-graded)                     │  │ Password   ⌖ forgot?   │         │
│                                    │  │ ┌─────────────────────┐ │         │
│                                    │  │ └─────────────────────┘ │         │
│                                    │  │                         │         │
│                                    │  │ ✦ Sign in (gradient,    │         │
│                                    │  │   pill, full-width)     │         │
│                                    │  │                         │         │
│                                    │  │ ⌖ New here? Register    │         │
│                                    │  └─────────────────────────┘         │
└────────────────────────────────────┴─────────────────────────────────────┘
```

**Foldable portrait (840×1080)**: single-column form max 560 dp centered. Brand chip at the top only (logo + wordmark, ~120 dp tall). Photo background dropped — solid `surface` only.

### 3.3 Register

**Mobile source**: `0c6b56c895254ac2bcc1a240a829f2ff`

Identical shell to Login. **Landscape**: two-column, brand panel left + form right (form gets +40 dp height for the extra password-confirm field). **Foldable**: single-column form max 560 dp, brand chip top.

### 3.4 Forgot Password

**Mobile source**: `bb194b0093384d19b2265c791592c6d6`

Same two-column shell. **Landscape** form has only Email + Reset button; left panel shows a Lake Malawi sunrise photo (different from Login) with the line "Resetting? We'll get you back to writing."

---

## 4. Shell tabs (rail visible)

The 4 shell tabs share the same chrome:

```
┌────┬──────────────────────────────────────────────────────────────────┐
│ [N]│ ═══════════ top app bar (sticky, surface_bright) ═══════════     │
│    │                                                                  │
│ ⌂  │   ─── content (varies per tab) ───                              │
│ (•)│                                                                  │
│    │                                                                  │
│ 🔍 │                                                                  │
│    │                                                                  │
│ ★  │                                                                  │
│    │                                                                  │
│ 👤 │                                                                  │
└────┴──────────────────────────────────────────────────────────────────┘
 80dp
```

Rail items (top→bottom): Home `⌂`, Search `🔍`, Bookmarks `★`, Profile `👤`. Active item gets a **6 dp Sunset Orange (#E8883C) dot** centered below the icon — no background pill, no surface tint. Brand chip `[N]` at top is a 32 dp circle filled with primary→primary_container 135° gradient.

### 4.1 Blog Feed Home

**Mobile source**: `35a8e64f78384be885d1461339b631cf`

**Tablet landscape (1280×800)** — hero spans content width, then 2×2 grid + 320 dp trending column:
```
┌────┬─────────────────────────────────────────────────────────┬────────┐
│ [N]│ ══ "Nyasa Blog" headline-md  ⌖ 🔍 ★ avatar ══════════   │        │
│    │                                                         │        │
│ ⌂  │  ┌────────────────── HERO 16:9 ───────────────────┐    │ Editor's│
│ (•)│  │ FEATURED · CULTURE                              │    │ Picks   │
│    │  │ ◆ The Lakeside Renaissance:                    │    │ headline│
│ 🔍 │  │   How Mangochi Artists Reclaim Their Voice     │    │  -sm    │
│    │  │                                                 │    │ ───────│
│ ★  │  │ — Tamanda Banda · 12 min read                   │    │ ▣ ▣ ▣  │
│    │  └────────────────────────────────────────────────┘    │ ▣ ▣    │
│ 👤 │                                                         │ (5     │
│    │  Trending  (headline-sm, spacing-12 above)              │  rows) │
│    │  ┌──────────────────┐  ┌──────────────────┐             │        │
│    │  │ 16:9 hero        │  │ 16:9 hero        │             │        │
│    │  │ TRAVEL           │  │ FOOD             │             │        │
│    │  │ ◆ Lake Malawi…   │  │ ◆ Nsima at dusk… │             │        │
│    │  │ excerpt 2 lines  │  │ excerpt 2 lines  │             │        │
│    │  │ ⌖ author · 4 min │  │ ⌖ author · 6 min │             │        │
│    │  └──────────────────┘  └──────────────────┘             │        │
│    │  ┌──────────────────┐  ┌──────────────────┐             │        │
│    │  │ POETRY           │  │ ARCHITECTURE     │             │        │
│    │  │ ◆ Chichewa rev.  │  │ ◆ Blantyre then  │             │        │
│    │  └──────────────────┘  └──────────────────┘             │        │
└────┴─────────────────────────────────────────────────────────┴────────┘
       ~880 dp (asymmetric, 64 dp gutters)                       320 dp
```

The right "Editor's Picks" column sits on `surface_container_low` so the surface shift defines the boundary — no border. Trending grid cards: `surface_container_lowest` over the page `surface`. **This is the Phase 3 deliverable** (`Expanded` window class).

**Foldable portrait (840×1080)**: rail still left, single-column feed. Hero (full width minus rail) → "Trending" header → **single column** of 4 article cards (no 2×2 grid; canvas is too narrow for that to breathe). "Editor's Picks" rendered below the fold as a horizontal scroll rail of 5 cards (240 dp wide each), tagged with section header "Editor's Picks →".

### 4.2 Blog Feed Search

**Mobile source**: `64a6cd3b59204a39a313680de232f61d`

**Tablet landscape**: rail left + sticky search bar full content-width + filter chip row + **2-column results grid (3 rows × 2)**:
```
┌────┬─────────────────────────────────────────────────────────────────┐
│ [N]│ ┌─ 🔍 "Lake Malawi tourism…"  outline_variant 20% ghost ─┐  ✕   │
│    │ └────────────────────────────────────────────────────────┘     │
│ ⌂  │                                                                 │
│    │ 🅵All  🅵Travel(•) 🅵Culture  🅵Food  🅵Politics  🅵Tech       │
│ 🔍 │                                                                 │
│ (•)│ 12 results for "Lake Malawi"               Most Recent ▾        │
│    │ ┌──────────────────┐  ┌──────────────────┐                       │
│ ★  │ │ TRAVEL           │  │ TRAVEL           │                       │
│    │ │ ◆ Cape Maclear…  │  │ ◆ Likoma Island… │                       │
│ 👤 │ │ excerpt with     │  │ excerpt with     │                       │
│    │ │ ░Lake Malawi░    │  │ ░Lake Malawi░    │  ░ = highlighted     │
│    │ │ highlight        │  │ highlight        │   tertiary_fixed_dim │
│    │ │ ⌖ byline · 4 min │  │ ⌖ byline · 8 min │                       │
│    │ └──────────────────┘  └──────────────────┘                       │
│    │  (4 more in 2×2 below)                                           │
└────┴─────────────────────────────────────────────────────────────────┘
```

**Foldable portrait**: rail left + search bar + chip row + **single column** of 6 results.

### 4.3 My Bookmarks

**Mobile source**: `92ac9920398d415a94941a8f138fbc6e`

**Tablet landscape**: rail left + 2-column grid of bookmark cards with horizontal layout (thumb 240×135 left + meta column right per card):
```
┌────┬────────────────────────────────────────────────────────────────┐
│ [N]│ ══ "My Bookmarks" headline-md  ⌖ 24 saved   ⋯ ══════════════   │
│    │                                                                │
│ ⌂  │ 🅵All  🅵Unread (8)  🅵Travel  🅵Culture  🅵Food  Sort: Recent ▾│
│    │                                                                │
│ 🔍 │ ┌──────────────────────────┐ ┌──────────────────────────┐      │
│    │ │ ▣ thumb │ TRAVEL       ★ │ │ ▣ thumb │ POETRY       ★ │      │
│ ★  │ │  240×   │ ◆ Cape Macl. │ │ │  240×   │ ◆ Chichewa…  │ │      │
│ (•)│ │  135    │ excerpt 2ln  │ │ │  135    │ excerpt 2ln  │ │      │
│    │ │         │ ⌖ saved 3d   │ │ │         │ ⌖ saved 1w   │ │      │
│ 👤 │ └──────────────────────────┘ └──────────────────────────┘      │
│    │ ┌──────────────────────────┐ ┌──────────────────────────┐      │
│    │ │ … 4 more rows of 2 …                                  │      │
│    │ └──────────────────────────┘                                   │
│    │                                                                │
│    │   Save more stories to read later  →  Browse the feed (ghost) │
└────┴────────────────────────────────────────────────────────────────┘
```

Bookmark icon top-right of each card filled in Sunset Orange (#E8883C). Subtle Chitenje pattern watermark behind the section at 4% primary opacity.

**Foldable portrait**: rail left + single-column list of bookmark cards (8 cards stacked, full width minus rail). Hero thumb on top of each card (16:9), not left-side.

### 4.4 Account Profile

**Mobile source**: `cf3de1b7bcca4b9b8c36d2417303ab4e`

**Tablet landscape** — two main columns: left bio panel (~520 dp), right activity feed (~600 dp):
```
┌────┬──────────────────────────────┬──────────────────────────────────┐
│ [N]│ ┌─────── 200 dp tall ───────┐│ Your Recent Posts (headline-sm) │
│    │ │ ░░ primary→primary_       ││                                 │
│ ⌂  │ │    container gradient ░░  ││ ┌─────────────────────────────┐ │
│    │ │       ⊙ (avatar 120dp)    ││ │ 16:9 hero                  │ │
│ 🔍 │ │   ◆ Tamanda Banda         ││ │ TRAVEL                     │ │
│    │ │     @tamanda · Blantyre  ││ │ ◆ Lake Malawi at sunrise  │ │
│ ★  │ └─────────────────────────────┘ │ │ ⌖ 1.2k views · 84 likes  │ │
│    │                              ││ │ ⌖ Published 3 days ago   │ │
│ 👤 │ Bio (4-line, body-md)        ││ └─────────────────────────────┘ │
│ (•)│                              ││ (3 more cards stacked)          │
│    │ ┌── 18 ──┬── 2.4k ──┬── 183 ┐││                                 │
│    │ │ Posts  │ Followers │ Follows││ Drafts (2)  🟧                │
│    │ └─────────┴───────────┴───────┘│ ─ "Untitled" · edited 2h ago  │
│    │                              ││ ─ "Mzuzu spotlight" · 5d ago  │
│    │ ✦ Edit Profile  · Settings   ││                                 │
│    │                              ││                                 │
│    │ Settings list:               ││                                 │
│    │ ─ ⚙ Account            ›     ││                                 │
│    │ ─ 🔔 Notifications      ›     ││                                 │
│    │ ─ 🔒 Privacy            ›     ││                                 │
│    │ ─ 🎨 Theme              ›     ││                                 │
│    │ ─ ↗ Sign Out           ›     ││                                 │
└────┴──────────────────────────────┴──────────────────────────────────┘
```

Settings rows separated by spacing-8 vertical white space, **never** by 1 px lines.

**Foldable portrait**: rail left + single column. Hero header full width, then bio card, then stat row (3 across), then CTAs, then settings list, then "Your Recent Posts" header + 4 cards stacked, then Drafts section.

---

## 5. Detail / editor screens

### 5.1 Blog Detail (Phases 2 + 3 — `ListDetailPaneScaffold`)

> **Phase split:** Phase 2 ships **two-pane** (list + detail) on Expanded and **single-pane** swap on Medium below 840dp. The **three-pane** wireframe below (list + body + meta column) is the **Phase 3** target, gated on Expanded class. Phase 2 hides the meta column entirely; comments and tags are appended below the body inside the detail pane. See `2026-04-28-h7-phase2-architecture-design.md` for the Phase 2 architectural contract.

**Mobile source**: `ad234d2c39634fbf8c479aba83f9d027`

**Tablet landscape (Phase 3 target)** — `ListDetailPaneScaffold` with feed list (360 dp) + article body (~600 dp) + meta column (~280 dp):
```
┌────┬────────────────┰────────────────────────────┰────────────────────┐
│ [N]│ Feed list      ┃  Article body              ┃  Meta              │
│    │ (360 dp,       ┃  (~600 dp, max-65ch        ┃  (~280 dp,         │
│ ⌂  │ surface_       ┃   reading column on        ┃   surface_         │
│    │ container_low) ┃   surface)                 ┃   container_low)   │
│ 🔍 │                ┃                            ┃                     │
│    │ ▣ THE LAKESIDE ┃ FEATURED · CULTURE         ┃  Tamanda Banda      │
│ ★  │ (selected,     ┃ ◆ The Lakeside             ┃  ⊙  ⌖ 12 min read │
│    │  surface_      ┃   Renaissance              ┃                     │
│ 👤 │  container_    ┃   display-lg Newsreader    ┃  Tags              │
│    │  highest)      ┃                            ┃  🅵Culture 🅵Travel│
│    │ ─────────────  ┃ ⌖ Tamanda · 12 min read   ┃                     │
│    │ ▣ COMING UP …  ┃ ──────────────────         ┃  Reading progress  │
│    │ ─────────────  ┃                            ┃  ▰▰▰▰▱▱▱▱  42%     │
│    │ ▣ MZUZU OPENS… ┃ Body-lg Newsreader 16/1.6 ┃                     │
│    │ ─────────────  ┃ on_surface — long-form    ┃  Comments (24)     │
│    │ ▣ NSIMA AT…    ┃ paragraphs flow here…     ┃  ─ "powerful…"      │
│    │ ─────────────  ┃                            ┃  ─ "what photo?"    │
│    │ ▣ CHICHEWA …   ┃ [Cultural divider:         ┃  ─ ✦ Add comment   │
│    │                ┃  Chitenje pattern @15%]    ┃                     │
│    │ (vertical      ┃                            ┃                     │
│    │  scroll)       ┃ More body…                ┃                     │
└────┴────────────────┸────────────────────────────┸────────────────────┘
```

Selecting a different article in the left list updates the body + meta panes without nav. A back-stack swipe collapses to feed view if width drops below Medium (state restoration through `ListDetailPaneScaffold`).

**Foldable portrait**: rail + single column reading view. Sticky reading-progress bar at top (~4 dp tall, primary fill). Body capped at 65 ch, centered with generous left/right margin. Comments live in a section below, not a sidebar.

### 5.2 Create Blog (Phase 4 — modal/preview pane)

**Mobile source**: `0ca45af5f9c941868ecd9bc6ec171e84`

**Tablet landscape** — rail left + form (~560 dp) + live preview pane (~520 dp):
```
┌────┬─────────────────────────────────┬──────────────────────────────┐
│ [N]│ ══ "Create Post" ═══ ⌖ Saved  ✦Publish══                       │
│    │ Form pane                       │ Preview pane                 │
│ ⌂  │ (surface)                       │ (surface_container_low —     │
│    │                                 │  the tonal shift is the      │
│ 🔍 │ Title                          │  separator, no 1px line)     │
│    │ ┌─────────────────────────────┐ │                              │
│ ★  │ │ The Lakeside Renaissance…  │ │ FEATURED · CULTURE           │
│    │ └─────────────────────────────┘ │ ◆ The Lakeside Renaissance: │
│ 👤 │                                 │   How Mangochi Artists      │
│    │ Category 🅵Culture (•)          │   Reclaim Their Voice        │
│    │                                 │                              │
│    │ Cover image  📸 Upload          │ ── Tamanda Banda · 12 min ── │
│    │ ┌────── ImagePickerBox ───────┐│                              │
│    │ │ 16:9 placeholder w/ hint   ││ Body Newsreader 16/1.6        │
│    │ └─────────────────────────────┘│ flowing live as user types…  │
│    │                                 │                              │
│    │ Body (RichTextEditor)           │ Tags shown                   │
│    │ ┌─ B I U H1 H2 ⛓ ── toolbar ─┐ │ Estimated read time updates │
│    │ │ Long-form Newsreader…      │ │ live (4 min, 5 min, …)       │
│    │ └─────────────────────────────┘ │                              │
└────┴─────────────────────────────────┴──────────────────────────────┘
```

Preview pane is **read-only**, scroll-synced loosely with the form (bidirectional sync is out-of-scope for Phase 4).

**Foldable portrait**: rail left + single-column form. Preview is a collapsible chip ("Show preview ▾") above the form; tapping expands a modal-bottom-sheet preview overlay.

### 5.3 Edit Blog

**Mobile source**: `1f022f0564c049e0ab8712f7b04a556a`

Same shell as Create Blog. Preview pane shows current draft (loaded from server). Top bar adds a "Discard changes" tertiary button next to "Save & Publish".

---

## 6. Settings + dialogs

### 6.1 Edit Account

**Mobile source**: `36643e21bb574ad6b003620c824375e4`

**Tablet landscape**: rail + form (560 dp centered) + sidebar (~280 dp on right) showing avatar preview live + bio character count:
```
┌────┬──────────────────────────────────────────┬────────────────┐
│ [N]│ ══ "Edit Account" ══ ✦ Save              │ Avatar preview │
│    │                                          │ ⊙ (live)        │
│ ⌂  │ Name      ┌──────────────────────┐        │                │
│    │           └──────────────────────┘        │ Bio: 142/500   │
│ 🔍 │                                          │ chars          │
│    │ Handle    ┌──────────────────────┐        │                │
│ ★  │           └──────────────────────┘        │ Joined         │
│    │                                          │ Mar 2024       │
│ 👤 │ Bio       ┌──────────────────────┐        │                │
│ (•)│           │ multi-line           │        │ ⌖ Last edited  │
│    │           └──────────────────────┘        │   3d ago       │
│    │                                          │                │
│    │ Avatar    📸 Change                      │                │
└────┴──────────────────────────────────────────┴────────────────┘
```

**Foldable portrait**: rail + single-column form max 600 dp. Avatar preview moves to top of form.

### 6.2 Change Password

**Mobile source**: `971a0529ef6441a5adb9e786cc49872e`

**Tablet landscape**: rail + form max 480 dp **centered** in the content area. Generous whitespace either side; no second column (the form is too short for a sidebar to make sense).

**Foldable portrait**: rail + form max 560 dp centered.

### 6.3 Blog Feed Filter (modal/sheet)

**Mobile source**: `d09362fc43ce4f1fb4ea78327d33f5f7`

**Tablet landscape**: when invoked from the search/feed screen, opens as a **right-edge side sheet** (480 dp wide) sliding over the content. Rail stays visible on the left; content behind the sheet dims to 40% black. Sheet contains:
- Categories (multi-select chip group)
- Date range picker (two date inputs)
- Author filter (search + multi-select)
- Sort by (radio: Recent / Most read / Oldest)
- Footer: ✦ Apply (gradient) · Clear all (tertiary)

**Foldable portrait**: opens as a **bottom sheet** (M3 standard) ~70% screen height, same content. Rail stays visible.

---

## 7. Cross-cutting implementation hints

### 7.1 NyasaSideRail (already shipped — Phase 1 reference)

Width 80 dp, `surface_container_lowest` background, vertical Box with weight=1 to push items into a column. Each item:
- 56 dp tall touch target
- 24 dp icon
- 6 dp Sunset Orange dot indicator centered below the icon, 4 dp gap
- No background pill, no surface tint on selected — the dot is the *only* selection indicator.
- Text label optional (recommend label-only on focus / accessibility, hidden by default to keep rail compact).

### 7.2 ListDetailPaneScaffold (Phase 2)

Use `androidx.compose.material3.adaptive:adaptive-navigation`'s `NavigableListDetailPaneScaffold`. Plumb the existing `BlogViewModel` (already hoisted at `Routes.BLOG_GRAPH` scope per H7 architecture survey) so list and detail panes share state without a `PostNavigator` contract. Selection has two backings: on Medium it is owned by the scaffold's internal `ThreePaneScaffoldNavigator` (driven by `BlogListDetailScaffold`'s wrapper logic); on Small it is the existing `NavController.navigate(blogDetail(slug))` push to the unchanged `BLOG_DETAIL` route. The wrapper picks the path on `WindowClassifier.isMedium`; `NavigableListDetailPaneScaffold` integrates only with system back, not with `NavController`. See `2026-04-28-h7-phase2-architecture-design.md` for the full architectural contract.

### 7.3 Trending column / Expanded grid (Phase 3)

Only render on Expanded class (`containerSize.width ≥ 840.dp`). Below that, fall back to single-column with horizontal-scroll trending rail at the bottom. Use `LazyVerticalGrid` with `GridCells.Fixed(2)` for the 2×2 article grid; the trending column is a regular `LazyColumn` inside a 320 dp `Modifier.width` Box.

### 7.4 Modal Create sheet (Phase 4)

Two-pane editor + preview only at Expanded. At Medium (foldable portrait), the preview is a `ModalBottomSheet` triggered by a "Show preview" chip — keeps the form roomy on a 840 dp canvas. Live preview re-renders the same `MarkdownRenderer` used by Blog Detail.

---

## 8. Design file pointers

### Figma (primary)
- **File**: https://www.figma.com/design/uOzqW3F2w5lTCUaA4d0trH
- **Title**: "NyasaBlog — Adaptive Tablet (H7)"
- **Built via**: Figma MCP `use_figma` Plugin API (full programmatic control, no prompt timeout)
- **Pages**: Cover & Tokens · Phase 1 Shell · Phase 2 Blog Detail · Phase 3 Expanded · Phase 4 Editor · Auth · Settings
- **Frames built**: 18 + cover. Pending: Phase 4 editor (×4) + Settings (×6) — rate-limited mid-build, resume next billing window.
- **To resume**: call `mcp__claude_ai_Figma__use_figma` with `fileKey: "uOzqW3F2w5lTCUaA4d0trH"`. The JS pattern from this spec's tablet/foldable wireframe sections drops directly into Plugin API code — see commits or earlier conversation for reference.

### Stitch (abandoned)
- **Project**: `projects/7343295411818219574`
- **Design system**: inline "Editorial Excellence: The Warm Lithograph" (Newsreader + Plus Jakarta Sans, #005275, Sunset Orange #E8883C, no 1 px borders, no shadows). Informed the Figma palette.
- **Why abandoned**: `mcp__stitch__generate_screen_from_text` consistently exceeds its 2-minute MCP timeout for any non-trivial layout. Don't retry via MCP; use the Stitch web UI if you want pixel-screens there too.

---

## 9. Phase rollout

| Phase | Scope | Screens affected | Status |
|-------|-------|-----------------|--------|
| 1 | WindowClassifier + NyasaSideRail | 4 shell tabs | ✅ shipped (3e9ae34) |
| 2 | ListDetailPaneScaffold for Blog Detail | Blog Detail | pending |
| 3 | Expanded grid + Editor's Picks column | Blog Feed Home, Search, Bookmarks | pending |
| 4 | Modal Create sheet w/ live preview | Create Blog, Edit Blog | pending |

Each phase ships independently. None block on Stitch mockups — this spec is the visual contract.
