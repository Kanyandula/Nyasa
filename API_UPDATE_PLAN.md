# NyasaBlog API Update — Phased Implementation Plan

> Bring the Android app in sync with the updated nyasablog.com API.
> Design system: **Nyasa Horizon** (Lake Blue, Newsreader + Plus Jakarta Sans).
> Each phase is independently shippable.

---

## API Gap Summary

### New Endpoints (not in app)

| Endpoint | Method | Auth | Feature |
|----------|--------|------|---------|
| `blog/categories/` | GET | Public | Category list |
| `blog/tags/` | GET | Public | Tag list |
| `blog/{slug}/` | GET | Required | Full blog detail |
| `blog/{slug}/comments/` | GET | Public | Comment list |
| `blog/{slug}/comments/create/` | POST | Required | Add comment |
| `blog/comments/{pk}/delete/` | DELETE | Required | Delete comment |
| `blog/{slug}/like/` | POST | Required | Toggle like |
| `blog/{slug}/bookmark/` | POST | Required | Toggle bookmark |
| `blog/bookmarks/` | GET | Required | User's bookmarks |
| `account/profile/{username}/` | GET | Public | Author profile |
| `account/profile/update/` | PUT | Required | Update bio, location, social links |

### Changed Endpoints (app needs update)

| Endpoint | What Changed |
|----------|-------------|
| `blog/list` | New query params: `category`, `status`. New ordering: `view_count` |
| `blog/create` | Now supports `category`, `tags` fields |
| `blog/{slug}/update` | Now supports `category`, `tags` fields |
| Blog responses | New fields: `category`, `tags`, `reading_time`, `view_count`, `like_count` |
| Account responses | New fields: `bio`, `location`, `website`, `twitter`, `facebook`, `instagram`, `linkedin` |

---

## Phase A: Update Existing Models & Responses

**Goal**: Make the app handle new API fields without crashing. No new screens.

### Tasks

- [x] Update `BlogPost` Room entity — add `category`, `tags`, `reading_time`, `view_count`, `like_count` (all nullable)
- [x] Update `BlogSearchResponse` — add matching `@Expose` fields
- [x] Update `BlogCreateUpdateResponse` — add matching fields
- [x] Update `BlogResponseMappers.kt` — map new fields in `toBlogPost()`
- [x] Update `AccountProperties` Room entity — add `bio`, `location`, `website`, `twitter`, `facebook`, `instagram`, `linkedin`, `profile_image`
- [x] Add Room migration (version 3 → 4) in `AppDatabase.kt`
- [x] Update `searchListBlogPosts()` — add `@Query("category")` and `@Query("status")` params
- [x] Add `getBlogPost(slug)` endpoint to `NyasaBlogApiMainService`
- [x] Create `BlogDetailResponse` model
- [x] Update test fakes with new fields (not needed — new fields have defaults)
- [x] Verify: `./gradlew clean assembleDebug` and `./gradlew test` pass

### Files

```
models/BlogPost.kt
models/AccountProperties.kt
persistance/AppDatabase.kt
api/main/NyasaBlogApiMainService.kt
api/main/responses/BlogSearchResponse.kt
api/main/responses/BlogCreateUpdateResponse.kt
api/main/responses/BlogResponseMappers.kt
```

---

## Phase B: Likes, Bookmarks & Comments

**Goal**: Add social interaction features with API integration.

### Tasks

- [x] Add endpoints to `NyasaBlogApiMainService`:
  - `POST blog/{slug}/like/`
  - `POST blog/{slug}/bookmark/`
  - `GET blog/bookmarks/`
  - `GET blog/{slug}/comments/`
  - `POST blog/{slug}/comments/create/`
  - `DELETE blog/comments/{pk}/delete/`
- [x] Create response models: `LikeResponse`, `BookmarkResponse`, `CommentResponse`, `CommentsListResponse`
- [x] Create `Comment` model (and optional Room entity)
- [x] Add methods to `BlogRepository` interface: `likeBlogPost()`, `bookmarkBlogPost()`, `getBookmarks()`
- [x] Create `CommentRepository` interface + `CommentRepositoryImpl`
- [x] Create use cases: `LikeBlogPostUseCase`, `BookmarkBlogPostUseCase`, `GetBookmarksUseCase`, `GetCommentsUseCase`, `CreateCommentUseCase`, `DeleteCommentUseCase`
- [x] Wire in `MainModule.kt`
- [x] Write unit tests for new use cases
- [x] Verify: API calls work, tests pass

### Files

```
api/main/NyasaBlogApiMainService.kt
api/main/responses/LikeResponse.kt (new)
api/main/responses/BookmarkResponse.kt (new)
api/main/responses/CommentResponse.kt (new)
models/Comment.kt (new)
domain/repository/BlogRepository.kt
domain/repository/CommentRepository.kt (new)
repository/main/BlogRepositoryImpl.kt
repository/main/CommentRepositoryImpl.kt (new)
domain/usecase/blog/ (new use cases)
domain/usecase/comment/ (new directory)
di/main/MainModule.kt
```

---

## Phase C: Categories, Tags & User Profiles

**Goal**: Add content organization and author profiles.

### Tasks

- [x] Add endpoints to `NyasaBlogApiMainService`:
  - `GET blog/categories/`
  - `GET blog/tags/`
  - `GET account/profile/{username}/`
  - `PUT account/profile/update/` (with `bio`, `location`, `website`, social fields)
- [x] Create models: `Category`, `Tag`, `UserProfile`
- [x] Create response models: `CategoryResponse`, `TagResponse`, `UserProfileResponse`
- [x] Create `CategoryRepository` + `ProfileRepository` (interfaces + impls)
- [x] Create use cases: `GetCategoriesUseCase`, `GetTagsUseCase`, `GetProfileUseCase`, `UpdateProfileUseCase`
- [x] Update `createBlog()` and `updateBlog()` to send `category` and `tags`
- [x] Wire in DI modules
- [x] Write unit tests
- [x] Verify: fetch categories, view profile, update profile with bio

### Files

```
api/main/NyasaBlogApiMainService.kt
api/main/responses/CategoryResponse.kt (new)
api/main/responses/TagResponse.kt (new)
api/main/responses/UserProfileResponse.kt (new)
models/Category.kt (new)
models/Tag.kt (new)
models/UserProfile.kt (new)
domain/repository/CategoryRepository.kt (new)
domain/repository/ProfileRepository.kt (new)
repository/main/CategoryRepositoryImpl.kt (new)
repository/main/ProfileRepositoryImpl.kt (new)
domain/usecase/category/ (new)
domain/usecase/profile/ (new)
di/main/MainModule.kt
```

---

## Phase D: Nyasa Horizon Design System

**Goal**: Implement the Nyasa Horizon theme in Compose.

### Design Tokens

| Token | Value |
|-------|-------|
| Primary | Lake Blue `#1B6B93` / container `#005275` |
| Secondary | Sunset Orange `#E8883C` / container `#954a00` |
| Tertiary | Amber `#6d4400` |
| Background | Warm off-white `#F5F0EB` |
| On-surface | Near-black `#1d1b19` (never pure black) |
| Headline font | Newsreader (serif) |
| Body/Label font | Plus Jakarta Sans (sans-serif) |
| Corner radius | 8dp |
| Cards | Tonal layering (no drop shadows) |
| Borders | Background color shifts (no 1px lines) |
| Input fields | Ghost borders at 20% opacity |
| Primary buttons | Pill-shaped with gradient fill |
| Bottom nav | Dot indicators (not pill highlights) |
| Blog images | 16:9 aspect ratio |
| Accents | Chitenje-inspired geometric patterns as watermarks |

### Tasks

- [x] Create Compose theme: `NyasaTheme` with Material 3 color scheme
- [x] Add Newsreader + Plus Jakarta Sans font families (Google Fonts)
- [x] Create reusable components: `NyasaBlogCard`, `NyasaButton`, `NyasaTextField`, `NyasaTopBar`, `NyasaBottomBar`
- [x] Apply theme to existing screens (both activities wrap in `NyasaTheme`)
- [x] Verify: all screens render with correct colors, fonts, spacing

### Files

```
ui/theme/Color.kt (new or update)
ui/theme/Type.kt (new or update)
ui/theme/Theme.kt (new or update)
ui/components/ (new shared composables)
```

---

## Phase E: New Screens

**Goal**: Build all missing screens from Stitch designs.

### Screens (in order)

| # | Screen | Data Source | Stitch Reference |
|---|--------|-------------|-----------------|
| 1 | Welcome Screen | None (onboarding) | Nyasa Horizon |
| 2 | Forgot Password | WebView → `password_reset/` | Nyasa Horizon |
| 3 | Blog Feed (updated) | Categories + reading time + like count | Nyasa Horizon |
| 4 | Blog Feed - Filter Dialog | Categories + ordering options | Nyasa Horizon |
| 5 | Blog Detail (updated) | Detail + comments + like/bookmark buttons | Nyasa Horizon |
| 6 | Create Blog (updated) | Category picker + tag input | Nyasa Horizon |
| 7 | Edit Blog (updated) | Pre-filled with category/tags | Nyasa Horizon |
| 8 | Author Profile | `account/profile/{username}/` | Nyasa Horizon |
| 9 | Bookmarks | `blog/bookmarks/` | Adapted from Heritage |
| 10 | Edit Account (updated) | Bio, location, social links | Nyasa Horizon |
| 11 | Search Results + Empty State | `blog/list?search=` | Nyasa Horizon |
| 12 | Change Password (updated) | `account/change_password/` | Nyasa Horizon |

### Tasks per screen

- [x] Create ViewModel + UiState (AuthorProfileViewModel, BookmarksViewModel + states)
- [x] Create Compose screen (all 12 screens implemented/updated)
- [x] Add navigation destination (Routes + NavHost composables)
- [x] Match Stitch design (layout, spacing, colors)
- [ ] Write ViewModel tests (existing tests updated; new VM tests pending)

### Navigation updates

- [x] Welcome as start destination (first launch only)
- [x] Forgot Password from Login screen
- [x] Bookmarks accessible from Account menu
- [x] Author Profile navigable from blog post author name
- [x] Filter Dialog from blog feed toolbar

---

## Phase Dependency Graph

```
Phase A (models + responses)
    │
    ├── Phase B (likes, bookmarks, comments)
    │
    ├── Phase C (categories, tags, profiles)
    │
    └── Phase D (design system) ←── can start in parallel with B/C
            │
            └── Phase E (new screens) ←── requires B + C + D
```

Phases B, C, and D can run in parallel after A is complete.
Phase E requires all others.
