# H3 — Design System Tokens + Dark Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Formalize `NyasaSpacing` (4pt grid), add semantic `NyasaColors`, implement dark mode with three-way preference (System / Light / Dark), and replace all hardcoded dp/hex values across composables.

**Architecture:** Add `NyasaSpacing` and `NyasaColors` as CompositionLocals alongside Material3. Theme preference stored in DataStore, collected in `MainActivity`, passed to `NyasaTheme()`. Dark color scheme defined alongside existing light scheme. All 23 composable files updated to use spacing tokens.

**Tech Stack:** Jetpack Compose Material3, DataStore Preferences, Hilt, CompositionLocal

**Worktree:** `/Users/admin/StudioProjects/Nyasa/.claude/worktrees/h3-design-tokens`
**Branch:** `hardening/h3-tokens`

---

## File Map

| Action | File | Responsibility |
|--------|------|----------------|
| Create | `ui/theme/Spacing.kt` | `NyasaSpacing` data class + `LocalNyasaSpacing` |
| Create | `ui/theme/NyasaColors.kt` | `NyasaColors` data class + light/dark instances + `LocalNyasaColors` |
| Create | `ui/theme/ThemePreference.kt` | `ThemePreference` enum + DataStore read/write helper |
| Modify | `ui/theme/Color.kt` | Add `darkColorScheme` |
| Modify | `ui/theme/Theme.kt` | Wire CompositionLocals, `darkTheme` param, `NyasaTheme` accessor object |
| Modify | `di/AppModule.kt` | Provide `DataStore<Preferences>` |
| Modify | `ui/main/MainActivity.kt` | Collect theme preference, pass to `NyasaTheme` |
| Modify | `ui/main/account/composables/AccountProfileScreen.kt` | Add Appearance selector |
| Modify | `ui/auth/composables/LoginScreen.kt` | Replace SunsetBanner hex with `NyasaTheme.colors` |
| Modify | 22 composable files | Replace hardcoded `dp` with `NyasaTheme.spacing.*` |
| Modify | `app/build.gradle` | Add DataStore dependency |

---

## Task 1: Add DataStore dependency + create ThemePreference

**Files:**
- Modify: `app/build.gradle`
- Create: `app/src/main/java/com/kanyandula/nyasa/ui/theme/ThemePreference.kt`
- Modify: `app/src/main/java/com/kanyandula/nyasa/di/AppModule.kt`

- [ ] **Step 1: Add DataStore dependency to app/build.gradle**

Add in the dependencies block after the existing AndroidX dependencies:

```groovy
implementation "androidx.datastore:datastore-preferences:1.1.1"
```

- [ ] **Step 2: Create ThemePreference.kt**

```kotlin
package com.kanyandula.nyasa.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
}

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_preferences",
)

object ThemePreferenceManager {

    private val THEME_KEY = stringPreferencesKey("theme_preference")

    fun themeFlow(dataStore: DataStore<Preferences>): Flow<ThemePreference> =
        dataStore.data.map { prefs ->
            val name = prefs[THEME_KEY] ?: ThemePreference.SYSTEM.name
            ThemePreference.valueOf(name)
        }

    suspend fun setTheme(
        dataStore: DataStore<Preferences>,
        preference: ThemePreference,
    ) {
        dataStore.edit { prefs ->
            prefs[THEME_KEY] = preference.name
        }
    }
}
```

- [ ] **Step 3: Provide DataStore in AppModule.kt**

Read the current `AppModule.kt`. Add this provider:

```kotlin
@Singleton
@Provides
fun provideThemeDataStore(application: Application): DataStore<Preferences> =
    application.themeDataStore
```

Add import: `import com.kanyandula.nyasa.ui.theme.themeDataStore`

- [ ] **Step 4: Verify build**

Run: `./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/build.gradle \
       app/src/main/java/com/kanyandula/nyasa/ui/theme/ThemePreference.kt \
       app/src/main/java/com/kanyandula/nyasa/di/AppModule.kt
git commit -m "hardening(h3): add DataStore dependency and ThemePreference"
```

---

## Task 2: Create NyasaSpacing

**Files:**
- Create: `app/src/main/java/com/kanyandula/nyasa/ui/theme/Spacing.kt`

- [ ] **Step 1: Create Spacing.kt**

```kotlin
package com.kanyandula.nyasa.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class NyasaSpacing(
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 16.dp,
    val l: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
)

val LocalNyasaSpacing = staticCompositionLocalOf { NyasaSpacing() }
```

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/theme/Spacing.kt
git commit -m "hardening(h3): add NyasaSpacing with 4pt grid"
```

---

## Task 3: Create NyasaColors with light/dark variants

**Files:**
- Create: `app/src/main/java/com/kanyandula/nyasa/ui/theme/NyasaColors.kt`

- [ ] **Step 1: Create NyasaColors.kt**

```kotlin
package com.kanyandula.nyasa.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class NyasaColors(
    val likeActive: Color,
    val categoryChip: Color,
    val readTimeText: Color,
    val sunsetSkyTop: Color,
    val sunsetSkyBottom: Color,
    val sunsetGradientStart: Color,
    val sunsetGradientEnd: Color,
    val sunsetTextHighlight: Color,
    val sunsetTextGlow: Color,
)

val lightNyasaColors = NyasaColors(
    likeActive = Color(0xFFE53935),
    categoryChip = Color(0xFF005275),
    readTimeText = Color(0xFF6B7280),
    sunsetSkyTop = Color(0xFF87CEEB),
    sunsetSkyBottom = Color(0xFFF4A460),
    sunsetGradientStart = Color(0xFFFDB777),
    sunsetGradientEnd = Color(0xFFE8883C),
    sunsetTextHighlight = Color(0xFFFFF3E0),
    sunsetTextGlow = Color(0xFFFFCC80),
)

val darkNyasaColors = NyasaColors(
    likeActive = Color(0xFFEF5350),
    categoryChip = Color(0xFF4FC3F7),
    readTimeText = Color(0xFF9CA3AF),
    sunsetSkyTop = Color(0xFF1A3A5C),
    sunsetSkyBottom = Color(0xFF8B5E3C),
    sunsetGradientStart = Color(0xFFB8864A),
    sunsetGradientEnd = Color(0xFFA0612A),
    sunsetTextHighlight = Color(0xFF3E2C1A),
    sunsetTextGlow = Color(0xFF5C4020),
)

val LocalNyasaColors = staticCompositionLocalOf { lightNyasaColors }
```

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/theme/NyasaColors.kt
git commit -m "hardening(h3): add NyasaColors with light/dark variants"
```

---

## Task 4: Add dark color scheme + wire NyasaTheme

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/theme/Color.kt`
- Rewrite: `app/src/main/java/com/kanyandula/nyasa/ui/theme/Theme.kt`

- [ ] **Step 1: Add dark color palette to Color.kt**

Read the current `Color.kt`. Add a `NyasaDarkColorScheme` below the existing `NyasaLightColorScheme`. Use Material3 dark theme conventions — lighter text on darker surfaces, desaturated primaries:

```kotlin
val NyasaDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8ECAE6),
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004D6B),
    onPrimaryContainer = Color(0xFFC0E8FF),
    secondary = Color(0xFFFFB871),
    onSecondary = Color(0xFF4E2600),
    secondaryContainer = Color(0xFF6F3800),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFFE2C28C),
    onTertiary = Color(0xFF3F2E04),
    tertiaryContainer = Color(0xFF584419),
    onTertiaryContainer = Color(0xFFFFDEA6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = Color(0xFF0D0D0D),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF005275),
    surfaceTint = Color(0xFF8ECAE6),
)
```

Also rename the existing `lightColorScheme(...)` assignment to `NyasaLightColorScheme` if it isn't already named that way.

- [ ] **Step 2: Rewrite Theme.kt**

Replace the entire file:

```kotlin
package com.kanyandula.nyasa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object NyasaTheme {
    val spacing: NyasaSpacing
        @Composable @ReadOnlyComposable
        get() = LocalNyasaSpacing.current

    val colors: NyasaColors
        @Composable @ReadOnlyComposable
        get() = LocalNyasaColors.current
}

@Composable
fun NyasaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NyasaDarkColorScheme else NyasaLightColorScheme
    val nyasaColors = if (darkTheme) darkNyasaColors else lightNyasaColors

    CompositionLocalProvider(
        LocalNyasaSpacing provides NyasaSpacing(),
        LocalNyasaColors provides nyasaColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NyasaTypography,
            shapes = NyasaShapes,
            content = content,
        )
    }
}
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/theme/Color.kt \
       app/src/main/java/com/kanyandula/nyasa/ui/theme/Theme.kt
git commit -m "hardening(h3): add dark color scheme, wire NyasaTheme with CompositionLocals"
```

---

## Task 5: Wire theme preference in MainActivity

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/main/MainActivity.kt`

- [ ] **Step 1: Update MainActivity to collect theme preference**

Read the current `MainActivity.kt`. Add:
- Inject `DataStore<Preferences>` via Hilt
- Collect `ThemePreferenceManager.themeFlow()` as state
- Map to `darkTheme` boolean
- Pass to `NyasaTheme(darkTheme = ...)`

The key change is in `setContent`:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var themeDataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themePreference by ThemePreferenceManager
                .themeFlow(themeDataStore)
                .collectAsStateWithLifecycle(
                    initialValue = ThemePreference.SYSTEM,
                )

            val darkTheme = when (themePreference) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            NyasaTheme(darkTheme = darkTheme) {
                // ... existing Scaffold and NavHost content unchanged
            }
        }
    }
}
```

Add imports: `ThemePreference`, `ThemePreferenceManager`, `isSystemInDarkTheme`, `collectAsStateWithLifecycle`, `DataStore`, `Preferences`.

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/MainActivity.kt
git commit -m "hardening(h3): wire theme preference in MainActivity"
```

---

## Task 6: Add Appearance selector to Account screen

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/main/account/composables/AccountProfileScreen.kt`

- [ ] **Step 1: Add Appearance setting UI**

Read the current `AccountProfileScreen.kt`. Find the Account Settings section (the card with Edit Profile and Change Password). Add an "Appearance" row above the existing items with a three-way segmented selector.

The composable needs access to `DataStore<Preferences>`. Since this is a screen composable, inject it through the ViewModel or pass it down. The simplest approach for now (pre-H5 modularization): use `LocalContext` to access the DataStore extension property.

Add an `AppearanceSelector` composable within the file:

```kotlin
@Composable
private fun AppearanceSelector() {
    val context = LocalContext.current
    val dataStore = context.themeDataStore
    val currentTheme by ThemePreferenceManager
        .themeFlow(dataStore)
        .collectAsStateWithLifecycle(initialValue = ThemePreference.SYSTEM)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(horizontal = NyasaTheme.spacing.m)) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(NyasaTheme.spacing.s))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ThemePreference.entries.forEachIndexed { index, preference ->
                SegmentedButton(
                    selected = currentTheme == preference,
                    onClick = {
                        scope.launch {
                            ThemePreferenceManager.setTheme(dataStore, preference)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemePreference.entries.size,
                    ),
                ) {
                    Text(
                        text = when (preference) {
                            ThemePreference.SYSTEM -> "System"
                            ThemePreference.LIGHT -> "Light"
                            ThemePreference.DARK -> "Dark"
                        },
                    )
                }
            }
        }
    }
}
```

Call `AppearanceSelector()` inside the Account Settings card, before the Edit Profile menu item.

Add imports: `SingleChoiceSegmentedButtonRow`, `SegmentedButton`, `SegmentedButtonDefaults`, `ThemePreference`, `ThemePreferenceManager`, `themeDataStore`, `rememberCoroutineScope`, `launch`, `collectAsStateWithLifecycle`.

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/account/composables/AccountProfileScreen.kt
git commit -m "hardening(h3): add Appearance selector (System/Light/Dark) to Account screen"
```

---

## Task 7: Replace SunsetBanner hardcoded hex with NyasaColors

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/auth/composables/LoginScreen.kt`

- [ ] **Step 1: Update SunsetBanner to use NyasaTheme.colors**

Read the current `LoginScreen.kt`. Find the `SunsetBanner` composable (around line 251). Replace all hardcoded `Color(0x...)` values with `NyasaTheme.colors.*` references:

| Old value | New reference |
|-----------|-------------|
| `Color(0xFF87CEEB)` | `NyasaTheme.colors.sunsetSkyTop` |
| `Color(0xFFF4A460)` | `NyasaTheme.colors.sunsetSkyBottom` |
| `Color(0xFFFDB777)` | `NyasaTheme.colors.sunsetGradientStart` |
| `Color(0xFFE8883C)` | `NyasaTheme.colors.sunsetGradientEnd` |
| `Color(0xFFFFF3E0)` | `NyasaTheme.colors.sunsetTextHighlight` |
| `Color(0xFFFFCC80)` | `NyasaTheme.colors.sunsetTextGlow` |

Also replace `SunsetOrange` and `Primary` direct token references in the SunsetBanner with `NyasaTheme.colors.sunsetGradientEnd` and `MaterialTheme.colorScheme.primary` respectively (if not already using the theme).

- [ ] **Step 2: Verify build + no raw hex in SunsetBanner**

Run: `./gradlew assembleDebug`

Then verify: `grep -n "Color(0x" app/src/main/java/com/kanyandula/nyasa/ui/auth/composables/LoginScreen.kt`

Expected: No results.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/auth/composables/LoginScreen.kt
git commit -m "hardening(h3): replace SunsetBanner hex with NyasaTheme.colors tokens"
```

---

## Task 8: Replace hardcoded dp in shared components (6 files)

**Files:**
- Modify: `ui/components/ProfileAvatar.kt`
- Modify: `ui/components/NyasaTextField.kt`
- Modify: `ui/components/NyasaButton.kt`
- Modify: `ui/components/NyasaBottomBar.kt`
- Modify: `ui/components/NyasaBlogCard.kt`
- Modify: `ui/components/LoadingOverlay.kt`
- Modify: `ui/components/ImagePickerBox.kt`

- [ ] **Step 1: Update shared components**

Read each file. Replace hardcoded spacing `dp` values with `NyasaTheme.spacing.*`:

| Hardcoded | Token |
|-----------|-------|
| `4.dp` | `NyasaTheme.spacing.xs` |
| `8.dp` | `NyasaTheme.spacing.s` |
| `16.dp` | `NyasaTheme.spacing.m` |
| `24.dp` | `NyasaTheme.spacing.l` |
| `32.dp` | `NyasaTheme.spacing.xl` |
| `48.dp` | `NyasaTheme.spacing.xxl` |

**Do NOT replace:**
- Component dimensions: icon sizes (`24.dp`), avatar sizes (`48.dp`, `72.dp`, `96.dp`), button heights (`52.dp`), image heights (`200.dp`), border widths (`1.dp`, `2.dp`)
- Corner radius values (already tokenized in `Shape.kt`)
- Odd values like `6.dp`, `10.dp`, `12.dp` in padding — round to nearest grid value (`NyasaTheme.spacing.s` for 6–8, `NyasaTheme.spacing.m` for 10–16) only if the visual result is acceptable. Use judgment — if `12.dp` is clearly a "medium-small" gap, use `NyasaTheme.spacing.s` (8dp). If it looks intentionally between `s` and `m`, keep `12.dp` as a literal.

Add import `com.kanyandula.nyasa.ui.theme.NyasaTheme` to each file.

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/components/
git commit -m "hardening(h3): replace hardcoded dp in shared components with NyasaTheme.spacing"
```

---

## Task 9: Replace hardcoded dp in auth screens (5 files)

**Files:**
- Modify: `ui/auth/composables/WelcomeScreen.kt`
- Modify: `ui/auth/composables/SocialAuthRow.kt`
- Modify: `ui/auth/composables/RegisterScreen.kt`
- Modify: `ui/auth/composables/LoginScreen.kt`
- Modify: `ui/auth/composables/ForgotPasswordScreen.kt`

- [ ] **Step 1: Update auth composables**

Same replacement rules as Task 8. Read each file, replace spacing dp values with `NyasaTheme.spacing.*`. Keep component dimensions as literals.

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/auth/
git commit -m "hardening(h3): replace hardcoded dp in auth screens with NyasaTheme.spacing"
```

---

## Task 10: Replace hardcoded dp in blog screens (6 files)

**Files:**
- Modify: `ui/main/blog/composables/BlogFeedScreen.kt`
- Modify: `ui/main/blog/composables/BlogDetailScreen.kt`
- Modify: `ui/main/blog/composables/BlogFilterSheet.kt`
- Modify: `ui/main/blog/composables/BookmarksScreen.kt`
- Modify: `ui/main/blog/composables/EditBlogScreen.kt`
- Modify: `ui/main/blog/composables/AuthorProfileScreen.kt`

- [ ] **Step 1: Update blog composables**

Same replacement rules as Task 8. Read each file, replace spacing dp values with `NyasaTheme.spacing.*`.

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/blog/
git commit -m "hardening(h3): replace hardcoded dp in blog screens with NyasaTheme.spacing"
```

---

## Task 11: Replace hardcoded dp in remaining screens (4 files)

**Files:**
- Modify: `ui/main/account/composables/AccountProfileScreen.kt`
- Modify: `ui/main/account/composables/EditAccountScreen.kt`
- Modify: `ui/main/account/composables/ChangePasswordScreen.kt`
- Modify: `ui/main/create_blog/composables/CreateBlogScreen.kt`

- [ ] **Step 1: Update remaining composables**

Same replacement rules. Read each file, replace spacing dp values with `NyasaTheme.spacing.*`.

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/main/account/ \
       app/src/main/java/com/kanyandula/nyasa/ui/main/create_blog/
git commit -m "hardening(h3): replace hardcoded dp in account and create-blog screens with NyasaTheme.spacing"
```

---

## Task 12: Final verification + review gates

**Files:** No new files — verification only.

- [ ] **Step 1: Full clean build**

Run: `./gradlew clean assembleDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run all unit tests**

Run: `./gradlew test`

Expected: All tests pass.

- [ ] **Step 3: Run quality gates**

Run: `./gradlew detekt spotlessCheck`

Expected: All pass. If spotless fails, run `./gradlew spotlessApply` then re-run.

- [ ] **Step 4: Review gate — zero raw hex in ui/**

Run: `grep -r "Color(0x" app/src/main/java/com/kanyandula/nyasa/ui/`

Expected: No results. Every `Color(0x...)` literal should now be in `Color.kt` or `NyasaColors.kt`, not in composables.

- [ ] **Step 5: Review gate — verify hardcoded dp cleanup**

Run: `grep -rn "[0-9]\+\.dp" app/src/main/java/com/kanyandula/nyasa/ui/ | grep -v "theme/" | grep -v "\.size\|\.height\|\.width\|\.offset\|cornerRadius\|border\|elevation\|RoundedCorner" | head -20`

Review remaining matches — they should only be component-specific dimensions (avatar sizes, button heights, image dimensions), not spacing/padding.

- [ ] **Step 6: Commit any spotless fixes**

```bash
git add -A
git commit -m "hardening(h3): spotless formatting fixes"
```

(Skip if spotless had no changes.)

---

## Post-Implementation

After all tasks complete:

1. Run `/android-code-review` on the full diff against `Deploy_0.01`
2. Run `/simplify` after review passes
3. **Dark mode visual audit:** Test every screen on a dark-theme emulator — check for invisible text, poor contrast, mismatched surfaces
4. Final commit and push: `git push origin hardening/h3-tokens`
5. Merge from main project root (after H1 is merged):
   ```bash
   cd ~/StudioProjects/Nyasa
   git checkout Deploy_0.01
   git merge --no-ff hardening/h3-tokens -m "H3: NyasaSpacing, NyasaColors, dark mode, theme preference"
   ```
