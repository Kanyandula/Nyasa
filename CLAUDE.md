# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NyasaBlog is a native Android app (Kotlin) that interacts with the REST API at `nyasablog.com`. It is a blogging platform for Malawian content creators. The app supports authentication, blog CRUD with image uploads, account management, and offline caching.

**Status:** Phases 1–9 of the 10-phase modernization are complete (see `REFACTORING_PLAN.md`). UI is 100% Jetpack Compose — no Fragments, no layout XML, no navigation XML remain. Forward-looking hardening work (H1–H10) lives in `COMPOSE_REFACTOR.md` with the execution runbook in `WORKTREE_REFACTOR_GUIDE.md`.

## Build & Quality Commands

```bash
# Build
./gradlew clean assembleDebug

# Code quality (all three run in the pre-commit hook)
./gradlew detekt                # Static analysis → build/reports/detekt/
./gradlew spotlessCheck         # Formatting check (ktlint)
./gradlew spotlessApply         # Auto-fix formatting
./gradlew lintDebug             # Android lint → app/build/reports/lint-results-debug.html

# Tests
./gradlew test                  # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests (requires emulator/device)
```

Pre-commit hook runs detekt, spotlessCheck, and lintDebug. Fix spotless issues with `./gradlew spotlessApply`.

## Architecture

**Pattern**: MVVM with StateFlow, Use Cases, and Repository pattern (Clean Architecture). UI is 100% Jetpack Compose.

```
Composables (collectAsStateWithLifecycle) → ViewModel → UseCase → Repository Interface → RepositoryImpl → Room DAOs + Retrofit Services
```

**Key abstractions**:
- `BaseViewModel<ViewState>` — provides `StateFlow<ViewState>`, `StateFlow<Boolean>` for loading, and `SharedFlow<UiEvent>` for one-shot events
- `Resource<T>` — sealed class (`Loading`, `Success`, `Error`) emitted by repositories
- `UiEvent` — interface for one-shot UI events (`ShowToast`, `ShowErrorDialog`, `ShowSuccessDialog`); screen-specific events extend it (e.g. `BlogNavigationEvent`, `AuthUiEvent`, `AccountUiEvent`)
- Use cases — single-responsibility classes with `operator fun invoke()`, one per repository operation
- Repository interfaces in `domain/repository/`, implementations in `repository/` as `*Impl`
- `SessionManager` — singleton holding cached `AuthToken` as `StateFlow`, manages login/logout state

**Package layout** (`com.kanyandula.nyasa`):
- `api/` — Retrofit services (`auth/`, `main/`), interceptors, response models
- `di/` — Hilt modules (`AppModule`, `AuthModule`, `MainModule`); binds repository interfaces to implementations
- `domain/repository/` — Repository interfaces (`AuthRepository`, `BlogRepository`, `AccountRepository`, `CreateBlogRepository`, plus comment/category/profile)
- `domain/usecase/` — Use case classes organized by feature (`auth/`, `blog/`, `account/`, `createblog/`, `comment/`, `category/`, `profile/`)
- `models/` — Room entities (`AuthToken`, `AccountProperties`, `BlogPost`, `BlogRemoteKey`) + DTOs (`Category`, `Tag`, `Comment`, `UserProfile`, `LikeResult`, `ProfileUpdateRequest`)
- `persistance/` — Room database, DAOs, query utils
- `repository/` — Repository implementations (`*Impl`) returning `Flow<Resource<T>>`
- `session/` — `SessionManager`
- `ui/` — Compose screens, ViewModels, state classes:
  - `ui/BaseViewModel.kt`, `ui/UiEvent.kt` — shared abstractions at the package root
  - `ui/auth/{composables, state}`
  - `ui/main/MainActivity.kt`, `ui/main/MainRouteComposables.kt` — single activity hosting the root NavHost
  - `ui/main/blog/{composables, viewmodel, state}`
  - `ui/main/account/{composables, state}`
  - `ui/main/create_blog/{composables, state}` _(directory uses underscore; matching use-case package is `usecase/createblog/` without underscore — pre-existing inconsistency, do not "fix")_
  - `ui/navigation/` — `AuthNavGraph.kt`, `MainNavGraph.kt` (navigation-compose)
  - `ui/components/` — shared composables (`NyasaTopBar`, `NyasaBottomBar`, `ImagePickerBox`, `NyasaCategoryDropdown`)
  - `ui/theme/` — Compose theme
- `util/` — Constants, error handling, `safeApiCall`, `GenericApiResponse`, `Resource`

**Blog state**: Per-screen state classes — `BlogListUiState`, `ViewBlogUiState`, `UpdateBlogUiState`. `BlogViewModel` is shared across blog composables via `hiltViewModel()` scoped to the nav graph, exposing separate `StateFlow` for each screen.

## Key Technical Details

- **API base URL**: `https://nyasablog.com/api/` (defined in `util/Constants.kt`)
- **Auth**: Token-based via django-auth-token. Token stored in `EncryptedSharedPreferences` (AES256)
- **Retrofit services** use `suspend fun` returning `Response<T>`. Calls wrapped with `safeApiCall()` in `util/SafeApiCall.kt`
- **`GenericApiResponse`**: Sealed class (`ApiSuccessResponse`, `ApiErrorResponse`, `ApiEmptyResponse`) used to normalize API responses
- **Room DB**: entities — `AuthToken` (FK to `AccountProperties`), `AccountProperties`, `BlogPost`. DAOs return `Flow`. Paging 3 via `BlogRemoteMediator` + `RemoteKeys`
- **Navigation**: single `NavHost` with nested graphs — `AuthNavGraph`, `MainNavGraph`. Session-gated at the root. Hosted by `MainActivity`
- **Image uploads**: Multipart via `UploadStreamRequestBody`, with CanHub ImagePicker for selection/cropping and Compressor for size reduction
- **State observation**: Composables collect `StateFlow` via `collectAsStateWithLifecycle()`; one-shot events via `LaunchedEffect` on `SharedFlow`

## Build Configuration

- compileSdk/targetSdk: 35, minSdk: 24
- Kotlin 2.0.21 with KSP (not KAPT); Compose compiler via `org.jetbrains.kotlin.plugin.compose`
- Java 17 compatibility
- Hilt 2.53.1, Room 2.6.1, Retrofit 2.11.0, Navigation-Compose 2.7.7, Paging 3.3.6, Coil 3 (`io.coil-kt.coil3`)
- Detekt max line length: 120, max method length: 60, max cyclomatic complexity: 20
- Main branch: `Deploy_0.01`
