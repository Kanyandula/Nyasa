# Phase 9: Jetpack Compose Migration Guide (ARCHIVED)

> **This document is historical.** Phase 9 is complete — the app is 100% Jetpack Compose with no Fragments or layout XML. Kept for the Stitch screen-ID mapping and migration patterns.
>
> **For active work:** see [`COMPOSE_REFACTOR.md`](COMPOSE_REFACTOR.md) (architecture) and [`WORKTREE_REFACTOR_GUIDE.md`](WORKTREE_REFACTOR_GUIDE.md) (execution).

---

## Original scope

> Migrate all XML Views to Jetpack Compose using Stitch UI designs as the visual source of truth.
> Incremental approach: ComposeView in Fragments first, then full Compose Navigation cutover.

## Table of Contents

1. [Stitch Design-to-Screen Mapping](#1-stitch-design-to-screen-mapping)
2. [Design System Token Reference](#2-design-system-token-reference)
3. [Migration Checklist](#3-migration-checklist)
4. [Code Patterns & Examples](#4-code-patterns--examples)
5. [Dependency Upgrades](#5-dependency-upgrades)
6. [Migration Phases](#6-migration-phases)

---

## 1. Stitch Design-to-Screen Mapping

**Stitch Project:** `projects/7258074762929658095`
**Design System:** "Organic Brutalism" / "The Digital Baobab"
**Verified:** 2026-03-29 — All 13 screens confirmed present in Stitch project

Fetch any screen's HTML/screenshot via Stitch MCP: `get_screen(projectId, screenId)`

### Auth Flow (Phase A)

| Screen | Stitch ID | Current XML | Current Fragment | Target Composable |
|--------|-----------|-------------|------------------|-------------------|
| Welcome | `1bd28921b15d427ea89f5f93b5ccb9ba` | `fragment_launcher.xml` | `LauncherFragment` | `ui/auth/composables/WelcomeScreen.kt` |
| Login | `d00112e49dcf4f0391edff29605083c2` | `fragment_login.xml` | `LoginFragment` | `ui/auth/composables/LoginScreen.kt` |
| Register | `0c6b56c895254ac2bcc1a240a829f2ff` | `fragment_register.xml` | `RegisterFragment` | `ui/auth/composables/RegisterScreen.kt` |
| Forgot Password | `bb194b0093384d19b2265c791592c6d6` | `fragment_forgot_password.xml` | `ForgotPasswordFragment` | `ui/auth/composables/ForgotPasswordScreen.kt` |

#### Welcome Screen Components
- **Heading:** "Welcome to NyasaBlog" (Newsreader headline)
- **Tagline:** "Discover the pulse of Malawian stories, where every voice finds its horizon." (Plus Jakarta Sans body, `on-surface-variant`)
- **Buttons:** Login (primary gradient), Register (secondary ghost), Forgot Password (text link)
- **Background:** `surface` (#fef8f3) with chitenje watermark pattern at 4% opacity
- **Shared components:** `NyasaButton` (primary + secondary variants)

#### Login Screen Components
- **Top bar:** Back arrow + "NyasaBlog" title + "Digital Baobab" subtitle
- **Hero image:** Decorative landscape banner (full-width)
- **Heading:** "Welcome back" (Newsreader), "Please enter your details to sign in" (Plus Jakarta Sans)
- **Form fields:**
  - Email — `mail` icon, label "Email Address"
  - Password — `lock` icon, label "Password", "Forgot password?" link
- **CTA:** "Login" + `arrow_forward` icon (primary gradient button)
- **Social auth:** "Or continue with" divider + Google/Apple icons
- **Footer:** "Don't have an account? Sign up" link
- **Shared components:** `NyasaTextField` (outlined + leading icon), `NyasaButton`, `SocialAuthRow`

#### Register Screen Components
- **Header:** "NyasaBlog" + "Join the Story" + "Create Account"
- **Subtitle:** "Start your journey into the digital landscapes of Malawi."
- **Form fields:**
  - Email — `alternate_email` icon + `check_circle` validation indicator
  - Username — `person` icon
  - Password — `lock` icon + `visibility_off` toggle
  - Confirm Password — `lock_reset` icon
  - Error message — `error` icon + "Passwords do not match" (`error` color)
- **Checkbox:** "I agree to the Terms of Service and Privacy Policy"
- **CTA:** "Register" + `arrow_forward` (primary gradient)
- **Social auth:** "or join with" + Google/Facebook icons
- **Footer:** "Already have an account? Sign In"
- **Shared components:** `NyasaTextField`, `NyasaButton`, `SocialAuthRow`, `PasswordStrengthIndicator`

#### Forgot Password Screen Components
- **Top bar:** `arrow_back` + `lock_reset` icons
- **Heading:** "Forgot Password?" (Newsreader headline, `primary` color)
- **Body:** "Enter your email to receive a reset link. We'll help you get back to your stories in no time."
- **Form field:** Email — `mail` icon, label "Email Address"
- **CTA:** "Send Reset Link" + `arrow_forward` (primary gradient)
- **Footer:** "Remember your password? Login" link
- **Background:** Chitenje pattern overlay at 4% opacity
- **Shared components:** `NyasaTextField`, `NyasaButton`

### Account Flow (Phase B)

| Screen | Stitch ID | Current XML | Current Fragment | Target Composable |
|--------|-----------|-------------|------------------|-------------------|
| Account Profile | `793717eb35824505918bd9192416e1cd` | `fragment_account.xml` | `AccountFragment` | `ui/main/account/composables/AccountProfileScreen.kt` |
| Edit Account | `36643e21bb574ad6b003620c824375e4` | `fragment_update_account.xml` | `UpdateAccountFragment` | `ui/main/account/composables/EditAccountScreen.kt` |
| Change Password | `971a0529ef6441a5adb9e786cc49872e` | `fragment_change_password.xml` | `ChangePasswordFragment` | `ui/main/account/composables/ChangePasswordScreen.kt` |

#### Account Profile Screen Components
- **Top bar:** Hamburger menu + "NyasaBlog" + search icon
- **Profile section:** Avatar image, "Creator Name", "@nyasa_creator" handle, email, `verified` badge, "Premium Storyteller" status
- **Stats row:** "24 Published Stories" | "1.2k Total Reads"
- **Menu items** (with chevron icons):
  - `edit_note` "Edit Profile" → navigates to EditAccountScreen
  - `lock_reset` "Change Password" → navigates to ChangePasswordScreen
  - `logout` "Logout" + `exit_to_app` icon
- **Bottom nav:** Home | Create (`add_circle`) | Account (active)
- **Shared components:** `NyasaTopBar`, `NyasaBottomBar`, `ProfileHeader`

#### Edit Account Screen Components
- **Top bar:** `arrow_back` + "Edit Profile" title + "Save" action (`check` icon)
- **Profile image:** Circular avatar + `photo_camera` overlay (image upload)
- **Form fields:**
  - Creator Name — displays "@nyasa_creator", label "USERNAME"
  - Email — `alternate_email` icon, label "EMAIL ADDRESS", helper "Used for account security and updates"
  - Bio — multiline text area with bio content
- **Footer:** `delete_forever` "Delete Account" (destructive button)
- **Shared components:** `NyasaTextField`, `NyasaButton` (destructive variant)

#### Change Password Screen Components
- **Top bar:** `arrow_back` icon
- **Header:** "Change Password" heading + `lock_reset` icon + "Secure Your Account" subtitle
- **Body:** "Ensure your new password is strong and contains at least 8 characters."
- **Form fields:**
  - Current Password — `password` icon
  - New Password — `vpn_key` icon + visibility toggle
  - Confirm New Password — `verified_user` icon
- **Password requirements checklist:**
  - `check_circle` "8+ Characters" (met)
  - `radio_button_unchecked` "1 Number" (unmet)
- **CTA:** "Update Password" (primary gradient)
- **Helper text:** "You will be logged out of other devices."
- **Shared components:** `NyasaTextField`, `NyasaButton`, `PasswordRequirementsList`

### Blog CRUD (Phase C)

| Screen | Stitch ID | Current XML | Current Fragment | Target Composable |
|--------|-----------|-------------|------------------|-------------------|
| Blog Detail | `ad234d2c39634fbf8c479aba83f9d027` | `fragment_view_blog.xml` | `ViewBlogFragment` | `ui/main/blog/composables/BlogDetailScreen.kt` |
| Create Blog | `0ca45af5f9c941868ecd9bc6ec171e84` | `fragment_create_blog.xml` | `CreateBlogFragment` | `ui/main/create_blog/composables/CreateBlogScreen.kt` |
| Edit Blog | `1f022f0564c049e0ab8712f7b04a556a` | `fragment_update_blog.xml` | `UpdateBlogFragment` | `ui/main/blog/composables/EditBlogScreen.kt` |

#### Blog Detail Screen Components
- **Top bar:** Back arrow + share/bookmark action icons
- **Hero image:** Full-width article banner (16:9, `rounded-lg` 16.dp)
- **Category tag:** "Travel & Culture" (`label-md`, Plus Jakarta Sans, all caps)
- **Title:** "Whispers of the Lake: A Journey into the Heart of Mangochi" (Newsreader `headline-lg`)
- **Author row:** Profile image + "Creator Name" + "May 24, 2024" + "8 min read" + Follow button
- **Article body:** Rich text with paragraphs (Newsreader body), pull quotes (`primary` color, italic), embedded images
- **Tags footer:** Hashtags (#Malawi, #TravelJournal, #Photography)
- **Owner actions:** Edit (`edit_note`) + Delete (`delete`) buttons (visible only if author)
- **Shared components:** `NyasaTopBar`, `NyasaBlogCard` (image component), `AuthorRow`

#### Create Blog Screen Components
- **Top bar:** "NyasaBlog - Create Story" + "Draft" status + Preview/Publish buttons
- **Image upload:** `add_a_photo` icon + "Select Photo" + "High resolution (16:9) recommended" helper
- **Form fields:**
  - Title — "Story Title" + character counter "0 / 60"
  - Content — "Your Story" multiline editor
- **Formatting toolbar:** `format_bold`, `format_italic`, `link`, `format_list_bulleted`, `format_quote`, `image`
- **Bottom nav:** Home | Create (active) | Profile
- **Shared components:** `NyasaButton`, `NyasaBottomBar`, `ImagePickerBox`, `RichTextToolbar`

#### Edit Blog Screen Components
- **Top bar:** Close (X) + "Edit Story" + Save (`check` icon)
- **Cover image:** Existing image preview + `add_a_photo` overlay + "Tap to change"
- **Form fields:**
  - Headline input — pre-filled article title
  - Body editor — pre-filled article content with text
- **Formatting toolbar:** `format_bold`, `format_italic`, `link`
- **Metadata:** Category dropdown (`expand_more` icon) + Reading Time (`schedule` icon)
- **Footer actions:** Image/Media | Tags | Preview | Delete/Discard
- **Shared components:** `NyasaButton`, `ImagePickerBox`, `RichTextToolbar`, `CategoryDropdown`

### Blog Feed (Phase D)

| Screen | Stitch ID | Current XML | Current Fragment | Target Composable |
|--------|-----------|-------------|------------------|-------------------|
| Blog Feed Home | `35a8e64f78384be885d1461339b631cf` | `fragment_blog.xml` | `BlogFragment` | `ui/main/blog/composables/BlogFeedScreen.kt` |
| Blog Feed Search | `64a6cd3b59204a39a313680de232f61d` | (same fragment, search state) | `BlogFragment` | `ui/main/blog/composables/BlogSearchBar.kt` |
| Blog Feed Filter | `d09362fc43ce4f1fb4ea78327d33f5f7` | `layout_blog_filter.xml` | `BlogFragment` | `ui/main/blog/composables/BlogFilterSheet.kt` |

#### Blog Feed Home Screen Components
- **Top bar:** Hamburger + "NyasaBlog" + search + filter + refresh icons
- **Editor's Pick card:** Large hero image + "Editor's Pick" label + title + author (initials avatar + name) + date + "Read More" CTA
- **Blog card list:** Each card has:
  - Thumbnail image (16:9, 16.dp rounded)
  - Headline (Newsreader `headline-sm`)
  - Author byline + read time
  - Bookmark icon
  - Cards separated by `spacing-6` (2rem / 32.dp), no divider lines
  - Card bg: `surface-container-lowest` on `surface-container-low`
- **End-of-feed:** Eco icon + "You've reached the roots" + "Refresh Feed" button
- **Bottom nav:** Home (active, Sunset Orange dot) | Create | Account
- **Shared components:** `NyasaTopBar`, `NyasaBottomBar`, `NyasaBlogCard`, `EditorPickCard`, `EndOfFeedMessage`

#### Blog Feed Search Components
- **Search bar:** Active search input with back/close controls
- **Filter chips:** "All Results" | "Travel" | "Culture" | "Photography" (horizontal scroll)
- **Results count:** "Found 24 Stories" + sort icon
- **Result cards:** Same as blog cards but with category label + relative timestamps ("Yesterday", "3h ago")
- **Shared components:** `BlogSearchBar`, `FilterChipRow`, `NyasaBlogCard`

#### Blog Feed Filter Dialog Components
- **Header:** "Filter stories" + "Customize your reading experience"
- **Filter sections:**
  - Filter by: Author, Date
  - Ordering: Ascending (`trending_up`) / Descending (`trending_down`)
- **Actions:** "Apply filters" (primary) + "Cancel" (secondary ghost)
- **Presentation:** ModalBottomSheet
- **Shared components:** `NyasaButton`, `FilterOptionRow`

### Shared Component Registry

Components used across 2+ screens, to be built in Phase 0:

| Component | Used In | File |
|-----------|---------|------|
| `NyasaButton` | All screens | `ui/components/NyasaButton.kt` |
| `NyasaTextField` | Auth, Account, Create/Edit Blog | `ui/components/NyasaTextField.kt` |
| `NyasaTopBar` | Feed, Detail, Account | `ui/components/NyasaTopBar.kt` |
| `NyasaBottomBar` | Feed, Create, Account | `ui/components/NyasaBottomBar.kt` |
| `NyasaBlogCard` | Feed Home, Feed Search | `ui/components/NyasaBlogCard.kt` |
| `SocialAuthRow` | Login, Register | `ui/auth/composables/SocialAuthRow.kt` |
| `ImagePickerBox` | Create Blog, Edit Blog | `ui/components/ImagePickerBox.kt` |
| `RichTextToolbar` | Create Blog, Edit Blog | `ui/components/RichTextToolbar.kt` |
| `LoadingOverlay` | All screens (via Scaffold) | `ui/components/LoadingOverlay.kt` |
| `ErrorDialog` | All screens (via UiEvent) | `ui/components/ErrorDialog.kt` |

### Extra Screen (Not Mapped)

| Screen | Stitch ID | Notes |
|--------|-----------|-------|
| Login (variant) | `45b09f45b99c4f99a0b7265696c64931` | Alternate Login layout (390x884). Use primary `d00112e49dcf4f0391edff29605083c2` for implementation. |

---

## 2. Design System Token Reference

Source: Stitch project design system — "Organic Brutalism" / "The Digital Baobab"

### Color Palette (Material 3 Tokens)

```
Primary:                #005275      On Primary:              #ffffff
Primary Container:      #1B6B93      On Primary Container:    #c7e7ff
Primary Fixed:          #c7e7ff      Primary Fixed Dim:       #8bcefb
On Primary Fixed:       #001e2e      On Primary Fixed Variant: #004c6c

Secondary:              #954a00      On Secondary:            #ffffff
Secondary Container:    #ff9b4e      On Secondary Container:  #6f3600
Secondary Fixed:        #ffdcc6      Secondary Fixed Dim:     #ffb784
On Secondary Fixed:     #301400      On Secondary Fixed Variant: #713700

Tertiary:               #6d4400      On Tertiary:             #ffffff
Tertiary Container:     #8b5a0d      On Tertiary Container:   #ffddb6
Tertiary Fixed:         #ffddb7      Tertiary Fixed Dim:      #fbba68
On Tertiary Fixed:      #2a1700      On Tertiary Fixed Variant: #653e00

Error:                  #ba1a1a      On Error:                #ffffff
Error Container:        #ffdad6      On Error Container:      #93000a

Background:             #fef8f3      On Background:           #1d1b19
Surface:                #fef8f3      On Surface:              #1d1b19
Surface Bright:         #fef8f3      Surface Dim:             #ded9d4
Surface Container Lowest: #ffffff    Surface Container Low:   #f8f3ee
Surface Container:      #f2ede8      Surface Container High:  #ece7e2
Surface Container Highest: #e6e2dd
Surface Variant:        #e6e2dd      On Surface Variant:      #40484e
Surface Tint:           #0e658c

Inverse Surface:        #32302d      Inverse On Surface:      #f5f0eb
Inverse Primary:        #8bcefb

Outline:                #70787f      Outline Variant:         #c0c7cf
```

### Typography

| Role | Font Family | Usage |
|------|-------------|-------|
| Display, Headline | **Newsreader** (serif) | Article titles, hero text, editorial content — anything "read" |
| Body, Label, Title | **Plus Jakarta Sans** (sans-serif) | Buttons, labels, form fields, navigation — anything "acted upon" |

**Font files needed:** Download from Google Fonts and place in `app/src/main/res/font/`:
- `newsreader_regular.ttf`, `newsreader_bold.ttf`, `newsreader_italic.ttf`
- `plus_jakarta_sans_regular.ttf`, `plus_jakarta_sans_medium.ttf`, `plus_jakarta_sans_semibold.ttf`, `plus_jakarta_sans_bold.ttf`

**Hierarchy tip:** Pair `headline-sm` (Newsreader) with `label-md` (Plus Jakarta Sans, all caps, 0.05rem letter spacing) for category tags.

### Shapes

| Element | Corner Radius |
|---------|---------------|
| Default (Cards, Inputs, Dialogs) | 8.dp |
| Buttons (Primary CTA) | Full (pill-shaped) |
| Blog Images | 16.dp (1rem) |

### Component Design Rules

**The "No-Line" Rule:**
> 1px solid borders for sectioning are **prohibited**. Boundaries must be defined solely through background color shifts. Example: transition from `surface` to `surface-container-low` to separate sections.

**Surface Hierarchy (Nesting):**
- **Base:** `surface` (#fef8f3) for main background
- **Sectioning:** `surface-container-low` (#f8f3ee) for background blocks (e.g., "Trending" section)
- **Elevation:** `surface-container-highest` (#e6e2dd) for most prominent cards

**Signature Gradient (Primary CTAs):**
- Linear gradient from `primary` (#005275) to `primary_container` (#1B6B93) at 135 degrees
- Used on primary buttons, hero sections

**Glassmorphism (Floating Overlays):**
- `surface_container_lowest` at 80% opacity with 16px backdrop blur
- Used on "Back to Top" buttons, floating overlays

**Buttons:**
- **Primary:** Filled with signature gradient, pill-shaped, `title-sm` (Plus Jakarta Sans)
- **Secondary:** Ghost style — no background, `secondary` (#954a00) text, 1.5px stroke at 40% opacity
- **Destructive:** Filled `error` (#ba1a1a), reserved for "Delete" / "Discard" only

**Blog Cards:**
- No divider lines — use `spacing-6` (2rem) vertical whitespace between cards
- Images: 16:9 aspect ratio, `rounded-lg` (16.dp) corners
- Card background: `surface-container-lowest` (#ffffff) on `surface-container-low` (#f8f3ee) for tonal lift

**Input Fields:**
- Outlined M3 style with `outline-variant` border
- Focus: border transitions to `primary` (2px) with `primary-container` outer glow (4px blur)
- Always use a leading icon

**Navigation:**
- **Top App Bar:** `surface_bright` background; on scroll transition to `surface_container_low` with backdrop blur
- **Bottom Navigation:** `surface_container_lowest` background; active state uses Sunset Orange (#E8883C) dot indicator below icon (not a background pill)

**Elevation:**
- Prefer tonal layering over shadows
- For floating elements (Bottom Nav, FAB): Y-offset 8dp, Blur 24dp, Spread -4dp, 10% `on_surface` tinted with `primary`
- **Ghost Border fallback:** `outline-variant` at 15% opacity (accessibility only)

**Typography Rules:**
- Never use pure black (#000000) — use `on_surface` (#1d1b19)
- Use asymmetrical margins for editorial tension (e.g., `spacing-8` left, `spacing-12` right on headlines)

---

## 3. Migration Checklist

### Prerequisites

- [x] Upgrade Kotlin 1.9.24 -> 2.0.21
- [x] Upgrade AGP 8.5.2 -> 8.7.3
- [x] Upgrade KSP 1.9.24-1.0.20 -> 2.0.21-1.0.28
- [x] Upgrade Hilt 2.51.1 -> 2.53.1
- [x] Add Compose BOM 2024.12.01 + dependencies
- [x] Add Coil 3.0.4 (coil-compose + coil-network-okhttp)
- [x] Add hilt-navigation-compose 1.2.0
- [x] Add paging-compose 3.3.6
- [x] Add `compose = true` to `buildFeatures`
- [x] Apply `org.jetbrains.kotlin.plugin.compose` plugin
- [x] ~~Download Newsreader font files to `res/font/`~~ Using Google Fonts provider instead
- [x] ~~Download Plus Jakarta Sans font files to `res/font/`~~ Using Google Fonts provider instead
- [x] Verify: `./gradlew clean assembleDebug` passes
- [x] Verify: `./gradlew detekt && ./gradlew spotlessCheck` pass

### Phase 0: Theme & Shared Components

- [x] `ui/theme/Color.kt` — All M3 color tokens from Section 2
- [x] `ui/theme/Type.kt` — Typography with Newsreader + Plus Jakarta Sans
- [x] `ui/theme/Shape.kt` — Shapes (8.dp default, pill buttons)
- [x] `ui/theme/Theme.kt` — `NyasaTheme` composable wrapping `MaterialTheme`
- [x] `ui/components/NyasaButton.kt` — Primary (gradient), Secondary (ghost), Destructive
- [x] `ui/components/NyasaTextField.kt` — Outlined with leading icon + focus glow
- [x] `ui/components/NyasaBlogCard.kt` — Card with 16:9 image, no borders, tonal layering
- [x] `ui/components/NyasaTopBar.kt` — Top bar with scroll-aware background
- [x] `ui/components/NyasaBottomBar.kt` — Bottom nav with Sunset Orange dot indicator
- [x] `ui/components/LoadingOverlay.kt` — Full-screen loading
- [x] `ui/components/ErrorDialog.kt` — Toast/Error/Success dialogs

### Phase A: Auth Screens

- [x] `ui/auth/composables/WelcomeScreen.kt`
- [x] `ui/auth/composables/LoginScreen.kt`
- [x] `ui/auth/composables/RegisterScreen.kt`
- [x] `ui/auth/composables/ForgotPasswordScreen.kt`
- [x] Refactor `LauncherFragment.kt` -> ComposeView shell
- [x] Refactor `LoginFragment.kt` -> ComposeView shell
- [x] Refactor `RegisterFragment.kt` -> ComposeView shell
- [x] Refactor `ForgotPasswordFragment.kt` -> ComposeView shell
- [x] Delete `fragment_launcher.xml`
- [x] Delete `fragment_login.xml`
- [x] Delete `fragment_register.xml`
- [x] Delete `fragment_forgot_password.xml`
- [x] Visual verification against Stitch designs

### Phase B: Account Screens

- [x] `ui/main/account/composables/AccountProfileScreen.kt`
- [x] `ui/main/account/composables/EditAccountScreen.kt`
- [x] `ui/main/account/composables/ChangePasswordScreen.kt`
- [x] Refactor `AccountFragment.kt` -> ComposeView shell
- [x] Refactor `UpdateAccountFragment.kt` -> ComposeView shell
- [x] Refactor `ChangePasswordFragment.kt` -> ComposeView shell
- [x] Delete `fragment_account.xml`
- [x] Delete `fragment_update_account.xml`
- [x] Delete `fragment_change_password.xml`
- [x] Visual verification against Stitch designs

### Phase C: Blog CRUD Screens

- [x] `ui/main/blog/composables/BlogDetailScreen.kt`
- [x] `ui/main/blog/composables/EditBlogScreen.kt`
- [x] `ui/main/create_blog/composables/CreateBlogScreen.kt`
- [x] Refactor `ViewBlogFragment.kt` -> ComposeView shell
- [x] Refactor `UpdateBlogFragment.kt` -> ComposeView shell
- [x] Refactor `CreateBlogFragment.kt` -> ComposeView shell
- [x] Delete `fragment_view_blog.xml`
- [x] Delete `fragment_update_blog.xml`
- [x] Delete `fragment_create_blog.xml`
- [x] Image picker integration via Fragment ActivityResultLauncher
- [x] Visual verification against Stitch designs

### Phase D: Blog Feed + Compose Navigation Cutover

- [x] `ui/main/blog/composables/BlogFeedScreen.kt` — LazyColumn + Paging 3
- [x] `ui/main/blog/composables/BlogSearchBar.kt`
- [x] `ui/main/blog/composables/BlogFilterSheet.kt` — ModalBottomSheet
- [x] Delete `fragment_blog.xml`, `layout_blog_filter.xml`, `layout_blog_list_item.xml`, `layout_blog_load_state.xml`
- [x] Add `navigation-compose` dependency
- [x] Replace Fragment NavHost with Compose `NavHost` in both Activities
- [x] Scope `BlogViewModel` to blog nav graph via `hiltViewModel(backStackEntry)`
- [x] Replace XML `BottomNavigationView` with Compose `NavigationBar`
- [x] Remove all Fragment classes
- [x] Remove all XML navigation graphs
- [x] Remove ViewBinding from `buildFeatures`

### Phase E: Cleanup

- [x] ~~Delete remaining XML layouts (`activity_auth.xml`, `activity_main.xml`)~~ Already deleted in Phase D
- [x] Remove Glide dependency (fully replaced by Coil)
- [x] Remove unused View dependencies: `constraintlayout`, `cardview`, `recyclerview`, `swiperefreshlayout`, `circleimageview`, `material-dialogs`
- [x] Remove `fragment-ktx` dependency (no more Fragments)
- [x] Consider single-Activity consolidation — deferred (two Activities work fine)
- [x] Final `./gradlew clean assembleDebug && ./gradlew detekt && ./gradlew spotlessCheck && ./gradlew lintDebug`
- [x] Full app walkthrough: auth -> blog feed -> detail -> create -> edit -> account -> password -> logout

---

## 4. Code Patterns & Examples

### Pattern A: Fragment-to-ComposeView Shell

Each Fragment's `onCreateView` is replaced with a `ComposeView`. The Fragment becomes a thin shell handling navigation callbacks and `ActivityResult` launchers.

```kotlin
class LoginFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                NyasaTheme {
                    val state by viewModel.viewState.collectAsStateWithLifecycle()
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                    LoginScreen(
                        state = state,
                        isLoading = isLoading,
                        onLogin = { email, password ->
                            viewModel.attemptLogin(email, password)
                        },
                        onForgotPassword = {
                            findNavController().navigate(
                                LoginFragmentDirections
                                    .actionLoginFragmentToForgotPasswordFragment()
                            )
                        },
                        onNavigateToRegister = {
                            findNavController().navigate(
                                LoginFragmentDirections
                                    .actionLoginFragmentToRegisterFragment()
                            )
                        }
                    )
                }
            }
        }
    }

    // Remove: onDestroyView, binding, subscribeObservers — no longer needed
}
```

### Pattern B: Collecting SharedFlow Events in Compose

One-shot events (toasts, navigation, error dialogs) are collected via `LaunchedEffect`:

```kotlin
@Composable
fun LoginScreen(
    state: AuthViewState,
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onForgotPassword: () -> Unit,
    onNavigateToRegister: () -> Unit,
    events: SharedFlow<UiEvent>? = null // passed from Fragment or ViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-shot events
    if (events != null) {
        LaunchedEffect(Unit) {
            events.collect { event ->
                when (event) {
                    is UiEvent.ShowToast -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                    is UiEvent.ShowErrorDialog -> {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                    is UiEvent.ShowSuccessDialog -> {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    // Screen content...
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        // ...
    }
}
```

### Pattern C: Paging 3 with LazyColumn (Blog Feed)

The existing `BlogViewModel.pagingDataFlow: Flow<PagingData<BlogPost>>` is consumed directly:

```kotlin
@Composable
fun BlogFeedScreen(
    pagingDataFlow: Flow<PagingData<BlogPost>>,
    onBlogClick: (String) -> Unit, // slug
    onSearch: (String) -> Unit,
    onFilterChange: (String, String) -> Unit
) {
    val pagingItems = pagingDataFlow.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            BlogSearchBar(onSearch = onSearch)
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(32.dp) // spacing-6 = 2rem
        ) {
            items(
                count = pagingItems.itemCount,
                key = pagingItems.itemKey { it.pk }
            ) { index ->
                pagingItems[index]?.let { blogPost ->
                    NyasaBlogCard(
                        blogPost = blogPost,
                        onClick = { onBlogClick(blogPost.slug) }
                    )
                }
            }

            // Load state footer
            item {
                when (val appendState = pagingItems.loadState.append) {
                    is LoadState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is LoadState.Error -> {
                        NyasaButton(
                            text = "Retry",
                            onClick = { pagingItems.retry() },
                            style = ButtonStyle.Secondary
                        )
                    }
                    else -> {}
                }
            }
        }

        // Pull-to-refresh
        PullToRefreshBox(
            isRefreshing = pagingItems.loadState.refresh is LoadState.Loading,
            onRefresh = { pagingItems.refresh() }
        )
    }
}
```

### Pattern D: Image Picker (Fragment Shell + Compose)

Image picking requires `ActivityResultLauncher`, which stays in the Fragment shell:

```kotlin
class CreateBlogFragment : BaseCreateBlogFragment() {

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setNewBlogFields(uri = uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                NyasaTheme {
                    val state by viewModel.viewState.collectAsStateWithLifecycle()
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                    CreateBlogScreen(
                        state = state,
                        isLoading = isLoading,
                        onPickImage = {
                            val intent = ImagePicker.with(requireActivity())
                                .crop()
                                .compress(1024)
                                .createIntent()
                            imagePickerLauncher.launch(intent)
                        },
                        onPublish = { title, body ->
                            viewModel.createNewBlogPost(title, body, state.blogFields.newImageUri)
                        }
                    )
                }
            }
        }
    }
}
```

### Pattern E: Compose Navigation (Phase D Cutover)

After all screens are Compose, replace Fragment navigation with Compose NavHost:

```kotlin
// In MainActivity
setContent {
    NyasaTheme {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                NyasaBottomBar(navController = navController)
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "blog",
                modifier = Modifier.padding(padding)
            ) {
                // Blog graph — BlogViewModel scoped to this nested graph
                navigation(startDestination = "blog/feed", route = "blog") {
                    composable("blog/feed") { backStackEntry ->
                        val blogGraphEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("blog")
                        }
                        val viewModel: BlogViewModel = hiltViewModel(blogGraphEntry)
                        BlogFeedScreen(
                            pagingDataFlow = viewModel.pagingDataFlow,
                            onBlogClick = { slug ->
                                navController.navigate("blog/detail/$slug")
                            }
                        )
                    }
                    composable("blog/detail/{slug}") { backStackEntry ->
                        val blogGraphEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("blog")
                        }
                        val viewModel: BlogViewModel = hiltViewModel(blogGraphEntry)
                        BlogDetailScreen(viewModel = viewModel)
                    }
                    composable("blog/edit/{slug}") { backStackEntry ->
                        val blogGraphEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("blog")
                        }
                        val viewModel: BlogViewModel = hiltViewModel(blogGraphEntry)
                        EditBlogScreen(viewModel = viewModel)
                    }
                }

                composable("create") {
                    CreateBlogScreen()
                }

                navigation(startDestination = "account/profile", route = "account") {
                    composable("account/profile") {
                        AccountProfileScreen()
                    }
                    composable("account/edit") {
                        EditAccountScreen()
                    }
                    composable("account/change-password") {
                        ChangePasswordScreen()
                    }
                }
            }
        }
    }
}
```

---

## 5. Dependency Upgrades

### Version Changes (Prerequisites PR)

| Dependency | Current | Target | Reason |
|------------|---------|--------|--------|
| Kotlin | 1.9.24 | 2.0.21 | Compose compiler plugin requires Kotlin 2.0+ |
| AGP | 8.5.2 | 8.7.3 | Compose build support |
| KSP | 1.9.24-1.0.20 | 2.0.21-1.0.28 | Must match Kotlin version |
| Hilt | 2.51.1 | 2.53.1 | Kotlin 2.0 compatibility |

### New Dependencies

```groovy
// build.gradle (root) — add plugin
id 'org.jetbrains.kotlin.plugin.compose' version '2.0.21' apply false

// app/build.gradle — apply plugin
id 'org.jetbrains.kotlin.plugin.compose'

// app/build.gradle — buildFeatures
buildFeatures {
    viewBinding = true   // keep during transition, remove in Phase E
    buildConfig = true
    compose = true
}

// app/build.gradle — dependencies
// Compose
implementation platform('androidx.compose:compose-bom:2024.12.01')
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.ui:ui-tooling-preview'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.compose.runtime:runtime'
implementation 'androidx.activity:activity-compose:1.9.3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6'
implementation 'androidx.lifecycle:lifecycle-runtime-compose:2.8.6'
implementation 'androidx.hilt:hilt-navigation-compose:1.2.0'
implementation 'androidx.paging:paging-compose:3.3.6'
debugImplementation 'androidx.compose.ui:ui-tooling'

// Coil (Compose-native image loading)
implementation 'io.coil-kt.coil3:coil-compose:3.0.4'
implementation 'io.coil-kt.coil3:coil-network-okhttp:3.0.4'

// Navigation Compose (add in Phase D)
implementation 'androidx.navigation:navigation-compose:2.7.7'
```

---

## 6. Migration Phases

### Phase 0: Theme & Shared Components (2-3 days)
Create `ui/theme/` and `ui/components/` packages with all design tokens and reusable composables.
**PR:** Standalone, no existing code changes.

### Phase A: Auth Flow (3-4 days)
Migrate Welcome, Login, Register, Forgot Password screens.
**PR:** Replaces 4 XML layouts with 4 Compose screens.

### Phase B: Account Flow (2-3 days)
Migrate Account Profile, Edit Account, Change Password screens.
**PR:** Replaces 3 XML layouts with 3 Compose screens.

### Phase C: Blog CRUD (3-4 days)
Migrate Blog Detail, Edit Blog, Create Blog screens. Includes image picker integration.
**PR:** Replaces 3 XML layouts with 3 Compose screens.

### Phase D: Blog Feed + Navigation Cutover (6-8 days)
Most complex phase. Migrate Blog Feed (LazyColumn + Paging 3 + search + filter).
Then replace Fragment Navigation with Compose Navigation across the entire app.
**PR:** Removes all Fragments and XML navigation graphs.

### Phase E: Cleanup (2-3 days)
Remove all dead XML layouts, unused dependencies, and consolidate to single Activity.
**PR:** Final cleanup, dependency reduction.

### Estimated Total: ~20-27 dev-days across 6 incremental PRs.
