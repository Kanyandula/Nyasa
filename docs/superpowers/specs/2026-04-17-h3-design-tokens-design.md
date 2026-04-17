# H3 — Design System Tokens + Dark Mode

## What we're building

A formalized design system for NyasaBlog: `NyasaSpacing` (4pt grid), semantic color tokens via `NyasaColors`, a dark color scheme with three-way theme preference (System / Light / Dark), and cleanup of all hardcoded dimensions and colors across composables.

## Current state

- **Color.kt**: 40+ Material3 color tokens defined. `lightColorScheme` only.
- **Type.kt**: 11 Material3 typography levels with Newsreader (serif) + Plus Jakarta Sans (sans-serif). Fully adopted.
- **Shape.kt**: 5 corner radius levels (4dp–28dp). Fully adopted.
- **Theme.kt**: `NyasaTheme()` composable wraps `MaterialTheme`. Light only, no CompositionLocals beyond Material3.
- **Composables**: 100% MaterialTheme adoption for colors/typography/shapes. Zero raw hex except SunsetBanner (8 gradient colors). ~50+ hardcoded `dp` spacings. A few inline `letterSpacing` overrides.
- **Dark mode**: Not implemented. No toggle. No dark color scheme.
- **Preference storage**: No DataStore. Token stored in EncryptedSharedPreferences.

## Design

### 1. NyasaSpacing

```kotlin
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

Replace all hardcoded padding/spacing `dp` values in composables with `NyasaTheme.spacing.*`. Values that don't map to the 4pt grid (e.g., `10.dp`, `14.dp`, `22.dp`) get rounded to the nearest grid value or kept as component-specific constants if they serve a specific layout purpose (e.g., icon sizes, avatar dimensions).

**Not tokenized:** Component dimensions like avatar sizes (`48.dp`, `72.dp`, `96.dp`), button height (`52.dp`), image heights. These are component-specific, not spacing.

### 2. NyasaColors — semantic color layer

```kotlin
@Immutable
data class NyasaColors(
    val likeActive: Color,
    val categoryChip: Color,
    val readTimeText: Color,
    val sunsetGradientStart: Color,
    val sunsetGradientEnd: Color,
    val sunsetSkyTop: Color,
    val sunsetSkyBottom: Color,
    val sunsetTextHighlight: Color,
    val sunsetTextGlow: Color,
)

val LocalNyasaColors = staticCompositionLocalOf { lightNyasaColors }
```

Light and dark variants defined. Standard Material3 slots (`primary`, `surface`, `error`, etc.) stay on `MaterialTheme.colorScheme` — no duplication. `NyasaColors` is only for app-specific semantic colors that Material3 doesn't cover.

### 3. NyasaTheme object

```kotlin
object NyasaTheme {
    val spacing: NyasaSpacing
        @Composable @ReadOnlyComposable
        get() = LocalNyasaSpacing.current

    val colors: NyasaColors
        @Composable @ReadOnlyComposable
        get() = LocalNyasaColors.current
}
```

Usage: `NyasaTheme.spacing.m`, `NyasaTheme.colors.likeActive`. Typography and shapes stay on `MaterialTheme.typography` and `MaterialTheme.shapes` — they're already complete and well-adopted.

### 4. Dark color scheme

Define `darkColorScheme` in Color.kt/Theme.kt alongside the existing `lightColorScheme`. Apply based on theme preference. The `NyasaTheme()` composable selects the right scheme:

```kotlin
@Composable
fun NyasaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme
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

### 5. Theme preference (DataStore)

```kotlin
enum class ThemePreference { SYSTEM, LIGHT, DARK }
```

Stored in `DataStore<Preferences>` (new dependency). Default: `SYSTEM`. Read in `MainActivity` and mapped:

```kotlin
val darkTheme = when (themePreference) {
    SYSTEM -> isSystemInDarkTheme()
    LIGHT -> false
    DARK -> true
}
```

DataStore provided via Hilt `@Singleton`. Preference flows as `StateFlow<ThemePreference>` collected in `MainActivity`.

### 6. Settings UI — Appearance selector

A new "Appearance" row in the Account screen. Three options: System, Light, Dark. Implemented as a simple segmented row or radio group. Selection writes to DataStore immediately — theme changes in real time, no restart.

### 7. SunsetBanner gradient tokens

Extract all 8 hardcoded hex colors from `LoginScreen.kt`'s `SunsetBanner` into `NyasaColors`. Provide light and dark variants (dark variant uses muted/deeper tones).

### 8. Letter-spacing cleanup

The 6 inline `.copy(letterSpacing = X.sp)` overrides stay as-is. They're localized cosmetic tweaks on specific labels ("DIGITAL BAOBAB", "EMAIL ADDRESS", etc.), not a systematic pattern worth tokenizing.

## Files affected

| Action | File |
|--------|------|
| Modify | `ui/theme/Color.kt` — add dark color palette, semantic color constants |
| Modify | `ui/theme/Theme.kt` — add `NyasaColors`, `NyasaSpacing`, `NyasaTheme` object, `darkTheme` parameter |
| Create | `ui/theme/Spacing.kt` — `NyasaSpacing` data class + `LocalNyasaSpacing` |
| Create | `ui/theme/NyasaColors.kt` — `NyasaColors` data class + `LocalNyasaColors` + light/dark instances |
| Create | `ui/theme/ThemePreference.kt` — enum + DataStore read/write |
| Modify | `di/AppModule.kt` — provide `DataStore<Preferences>` |
| Modify | `ui/main/MainActivity.kt` — collect theme preference, pass `darkTheme` to `NyasaTheme` |
| Modify | `ui/main/account/` — add Appearance selector |
| Modify | All ~30 composable files — replace hardcoded `dp` with `NyasaTheme.spacing.*` |
| Modify | `ui/auth/composables/LoginScreen.kt` — replace SunsetBanner hex with `NyasaTheme.colors.*` |
| Modify | `app/build.gradle` — add DataStore dependency |

## Review gates (from WORKTREE_REFACTOR_GUIDE)

1. Build passes
2. Zero raw hex in `ui/` — `grep -r "#[0-9A-Fa-f]\{6\}" ui/` returns nothing
3. Dark mode: every screen reviewed on dark emulator — no invisible text
4. `NyasaTypography` used consistently — no hardcoded `sp` (letter-spacing overrides excepted)
5. `NyasaSpacing` 4pt grid — no hardcoded `dp` padding in composables

## Out of scope

- Elevation/shadow tokens — not needed until H5 modularization
- Component tokens (button sizes, card specs) — premature until the component library stabilizes
- Stitch export automation — manual token definition for now
- XML resource cleanup (`colors.xml`, `themes.xml`) — legacy, only used for launcher/system chrome
