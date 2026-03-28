# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NyasaBlog is a native Android app (Kotlin) that interacts with the REST API at `nyasablog.com`. It is a blogging platform for Malawian content creators. The app supports authentication, blog CRUD with image uploads, account management, and offline caching.

The codebase is undergoing a 10-phase modernization (see `REFACTORING_PLAN.md`). Phases 1-2 are complete; the current branch is `Phase-2-Retrofit-Suspend-Migration`.

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

**Pattern**: MVI (Model-View-Intent) with `NetworkBoundResource` and Repository pattern.

```
Fragments → ViewModel (StateEvent → DataState) → Repository (JobManager) → Room DAOs + Retrofit Services
```

**Key abstractions**:
- `BaseViewModel<StateEvent, ViewState>` — drives all ViewModels via `switchMap` on `stateEvent` LiveData
- `NetworkBoundResource<ResponseObject, CacheObject, ViewState>` — abstract class for cache-first network ops, emits `DataState` via `MediatorLiveData`
- `DataState<T>` — wraps loading/error/data with `Event<T>` for one-shot delivery
- `JobManager` — tracks/cancels coroutine jobs per repository method
- `SessionManager` — singleton holding cached `AuthToken`, manages login/logout state

**Package layout** (`com.kanyandula.nyasa`):
- `api/` — Retrofit services (`auth/`, `main/`), interceptors, response models
- `di/` — Hilt modules (`AppModule`, `AuthModule`, `MainModule`)
- `models/` — Room entities (`AuthToken`, `AccountProperties`, `BlogPost`)
- `persistance/` — Room database, DAOs, query utils
- `repository/` — `NetworkBoundResource`, `JobManager`, auth/main repositories
- `session/` — `SessionManager`
- `ui/` — Activities, Fragments, ViewModels, state classes (`auth/`, `main/blog/`, `main/account/`, `main/create_blog/`)
- `util/` — Constants, error handling, `safeApiCall`, `GenericApiResponse`

**Shared state quirk**: `BlogViewState` is a monolithic state class shared across `BlogFragment`, `ViewBlogFragment`, and `UpdateBlogFragment`. Its ViewModel logic is split across extension files: `Getters.kt`, `Setters.kt`, `Pagination.kt` in `ui/main/blog/viewmodel/`.

## Key Technical Details

- **API base URL**: `https://nyasablog.com/api/` (defined in `util/Constants.kt`)
- **Auth**: Token-based via django-auth-token. Token stored in `EncryptedSharedPreferences` (AES256)
- **Retrofit services** use `suspend fun` returning `Response<T>` (Phase 2 migration). Calls wrapped with `safeApiCall()` in `util/SafeApiCall.kt`
- **`GenericApiResponse`**: Sealed class (`ApiSuccessResponse`, `ApiErrorResponse`, `ApiEmptyResponse`) used to normalize API responses
- **Room DB**: 3 entities — `AuthToken` (FK to `AccountProperties`), `AccountProperties`, `BlogPost`. DAOs currently return `LiveData`
- **Navigation**: 4 separate nav graphs (`auth_nav_graph`, `nav_blog`, `nav_account`, `nav_create_blog`). Uses raw `R.id` actions, not SafeArgs
- **Image uploads**: Multipart via `UploadStreamRequestBody`, with CanHub ImagePicker for selection/cropping and Compressor for size reduction

## Build Configuration

- compileSdk/targetSdk: 35, minSdk: 24
- Kotlin 1.9.24 with KSP (not KAPT)
- Java 17 compatibility
- Hilt 2.51.1, Room 2.6.1, Retrofit 2.11.0, Navigation 2.7.7
- Detekt max line length: 120, max method length: 60, max cyclomatic complexity: 20
- Main branch: `Deploy_0.01`