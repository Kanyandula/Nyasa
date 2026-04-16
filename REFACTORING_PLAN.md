# Nyasa Blog App — Phased Refactoring Plan

> Modernize the codebase incrementally from 2018-era patterns to current Android best practices.
> Each phase leaves the app in a working, shippable state.

> **Status:** Phases 1–9 complete. Forward-looking hardening (H1–H10) lives in [`COMPOSE_REFACTOR.md`](COMPOSE_REFACTOR.md) with the execution runbook in [`WORKTREE_REFACTOR_GUIDE.md`](WORKTREE_REFACTOR_GUIDE.md).

## Current State

| Layer | Status | Current |
|-------|--------|---------|
| Build | ✅ Done (Phase 1) | compileSdk 35, KSP, R8 enabled |
| Networking | ✅ Done (Phase 2) | Retrofit `suspend` functions, `safeApiCall` |
| Repository | ✅ Done (Phase 3+5) | `Flow<Resource<T>>`, interfaces in `domain/repository/`, impls in `repository/` |
| State | ✅ Done (Phase 4) | Per-screen `UiState` with `StateFlow`, `SharedFlow<UiEvent>` for one-shot events |
| Architecture | ✅ Done (Phase 5) | ViewModel → UseCase → Repository (interface) |
| Navigation | ✅ Done (Phase 6, superseded by Phase 9) | `navigation-compose` NavHost; session-gated |
| Session | ✅ Done (Phase 7) | Reactive `ConnectivityObserver` with `NetworkCallback` + `StateFlow` token |
| Pagination | ✅ Done (Phase 8) | Paging 3: `RemoteMediator` + `paging-compose` + `Flow<PagingData<BlogPost>>` |
| UI | ✅ Done (Phase 9) | 100% Jetpack Compose — no Fragments, no layout XML |
| Testing | Pending (Phase 10) | Scaffold only — see H9 in `COMPOSE_REFACTOR.md` |

---

## Phase 1: Dependency Cleanup & SDK Upgrade

**Goal**: Remove redundant/conflicting dependencies, upgrade SDK and Kotlin, migrate KAPT to KSP, enable R8.

### What changes

- **Remove duplicates**: standalone Dagger (redundant with Hilt), duplicate Room versions (2.4.3 and 2.5.0-alpha02), duplicate Lifecycle versions, duplicate test deps, duplicate navigation-fragment
- **Remove unused**: Paging 3 (not wired up), WorkManager, `hilt-lifecycle-viewmodel` (deprecated), `hilt-work`, `legacy-support-v4`, `kotlin-stdlib-jdk8`
- **Pick one**: keep Glide (remove Coil), keep CanHub Image Cropper (remove other two ImagePicker libs)
- **Consolidate versions**: Room 2.6.1, Lifecycle 2.8.7, Coroutines 1.9.0, Hilt 2.53.1, Navigation 2.8.5
- **Upgrade SDK**: compileSdk/targetSdk to 35, minSdk to 24
- **Migrate KAPT to KSP** for faster annotation processing
- **Enable R8** with ProGuard rules for Retrofit, Gson, Glide, Hilt

### Files affected
- `build.gradle` (project), `app/build.gradle`, `app/proguard-rules.pro`

### Verification
- `./gradlew clean assembleDebug` compiles
- Run app: login, blog list, blog detail, create blog all work
- `./gradlew detekt` passes

---

## Phase 2: Retrofit Suspend Migration

**Goal**: Convert all Retrofit service methods from `LiveData<GenericApiResponse<T>>` to `suspend fun` returning `Response<T>`. Eliminates the custom LiveData adapter layer.

### What changes

- All methods in `NyasaBlogApiAuthService` and `NyasaBlogApiMainService` become `suspend fun`
- `NetworkBoundResource.createCall()` becomes a `suspend fun`
- All repository `createCall()` overrides updated
- `LiveDataCallAdapterFactory` removed from Retrofit builder in `AppModule`
- New `safeApiCall` utility wraps suspend calls with error handling

### Files deleted
- `LiveDataCallAdapter.kt`, `LiveDataCallAdapterFactory.kt`, `AbsentLiveData.kt`

### Verification
- All API flows work: login, register, search blogs, CRUD, account operations
- Error dialogs/toasts still display correctly

---

## Phase 3: Flow-Based Repository

**Goal**: Replace the abstract `NetworkBoundResource` class and `JobManager` with a simpler Flow-based approach. Repositories return `Flow<Resource<T>>`.

### What changes

- New `Resource` sealed class (Loading, Success, Error)
- New `networkBoundResource()` inline function for cache+network operations
- New `apiResource()` function for network-only operations
- Room DAOs return `Flow` instead of `LiveData`
- All repositories rewritten with the new utilities
- ViewModels use `.asLiveData()` bridge temporarily
- `JobManager` deleted (cancellation handled by `viewModelScope`)

### Files deleted
- `JobManager.kt`, old `NetworkBoundResource.kt`

### Verification
- Login, blog list (with offline cache), CRUD, account all work
- No `JobManager` references remain

---

## Phase 4: Split ViewState & Migrate to StateFlow

**Goal**: Break the monolithic `BlogViewState` into per-screen state classes. Migrate ViewModels from LiveData to StateFlow. Consolidate scattered extension files.

### What changes

- `BlogViewState` split into: `BlogListUiState`, `ViewBlogUiState`, `UpdateBlogUiState`
- `Getters.kt`, `Setters.kt`, `Pagination.kt` absorbed into `BlogViewModel` as member functions
- All ViewModels use `StateFlow` for state and `Channel` for one-shot events
- `BaseViewModel` simplified (remove `switchMap`-on-`stateEvent` pattern)
- Fragments collect `StateFlow` via `repeatOnLifecycle`
- `DataState` and `Event` wrapper replaced by `Resource` + `Channel`-based events

### Files deleted
- `Getters.kt`, `Setters.kt`, `Pagination.kt`, `BlogViewState.kt`, `DataState.kt`

### Verification
- Blog list renders, scrolls, paginates
- One-shot errors show once (not re-delivered on config change)
- View/Update/Delete flows work

---

## Phase 5: Domain Layer (Use Cases)

**Goal**: Add use cases between ViewModels and repositories. Define repository interfaces for testability.

### What changes

- Repository interfaces defined in `domain/repository/`
- Existing repositories renamed to `*Impl`, implement interfaces
- Use case classes created for each operation (single `invoke` function)
- DI modules bind interfaces to implementations
- ViewModels inject use cases instead of repositories directly

### Verification
- App functions identically
- No ViewModel directly imports a repository implementation class

---

## Phase 6: Navigation Improvements (SafeArgs)

**Goal**: Use type-safe navigation arguments. Pass blog slug between screens instead of sharing state via ViewModel.

### What changes

- Navigation graphs get `<argument>` declarations for blog slug
- Fragments navigate using generated `Directions` classes
- Destination fragments receive args via `navArgs()`
- Optional: split `BlogViewModel` into per-screen ViewModels

### Verification
- Navigate list to detail to update: correct post at each step
- Back navigation preserves state
- Process death + restore works (args survive in bundle)

---

## Phase 7: Session & Connectivity Modernization

**Goal**: Fix deprecated APIs, modernize connectivity checking, migrate session to StateFlow.

### What changes

- New reactive `ConnectivityObserver` using `NetworkCallback` + `StateFlow<Boolean>`
- `SessionManager.cachedToken` migrated from `LiveData` to `StateFlow`
- Force-unwrap (`!!`) in `logout()` fixed
- Storage permissions updated for API 33+ (`READ_MEDIA_IMAGES`)

### Verification
- Airplane mode toggle: app detects connectivity changes
- Login persists across process death
- Image picking works on API 33+ emulator

---

## Phase 8: Paging 3

**Goal**: Replace manual pagination with the Paging 3 library.

### What changes

- `BlogRemoteMediator` handles network fetch + Room caching
- `BlogPostDao` gets a `PagingSource` query
- Repository returns `Flow<PagingData<BlogPost>>`
- `BlogListAdapter` extends `PagingDataAdapter`
- `BlogLoadStateAdapter` shows loading/error footer
- Manual pagination infrastructure removed (`isQueryExhausted`, `isPaginationDone()`)

### Verification
- List loads first page, scroll triggers next page
- Loading indicator at bottom during fetch
- Pull-to-refresh and filter/search reset pagination

---

## Phase 9: Jetpack Compose (Complete)

**Goal**: Migrate all XML Views to Jetpack Compose. ✅ Complete — no Fragments, no layout XML, no navigation XML remain. Navigation handled by `navigation-compose` via `AuthNavGraph` + `MainNavGraph`, hosted by `MainActivity`.

Historical migration playbook (Stitch screen IDs, patterns used): `COMPOSE_MIGRATION.md`.

Forward-looking hardening and modularization work: `COMPOSE_REFACTOR.md`.

---

## Phase 10: Testing Infrastructure

**Goal**: Set up proper testing and write tests for critical paths. Can run in parallel with other phases.

### What to add

- **Test fakes**: FakeBlogRepository, FakeAuthRepository, FakeSessionManager
- **ViewModel tests**: BlogListViewModelTest, AuthViewModelTest
- **Repository tests**: BlogRepositoryTest (MockWebServer + in-memory Room)
- **UI tests**: LoginFlowTest, BlogListTest
- **Dependencies**: coroutines-test, turbine, mockk, truth, mockwebserver

### Verification
- `./gradlew test` passes
- `./gradlew connectedAndroidTest` passes

---

## Phase Dependency Graph

```
Phase 1 (deps/SDK)
    |
Phase 2 (suspend Retrofit)
    |
Phase 3 (Flow repositories)
    |
Phase 4 (StateFlow + split ViewState)
    |--- Phase 5 (domain layer)      <-- can parallel with 6
    |--- Phase 6 (SafeArgs)          <-- can parallel with 5
    |
Phase 7 (session) <-- can start after Phase 1, independent
    |
Phase 8 (Paging 3) <-- requires Phase 3+4
    |
Phase 9 (Compose) <-- requires Phase 4
    |
Phase 10 (Testing) <-- start alongside Phase 3, continue throughout
```
