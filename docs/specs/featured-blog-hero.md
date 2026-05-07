# Spec: Server-driven Editor's Pick (Featured Blog Hero)

| Field | Value |
|---|---|
| Status | Approved 2026-05-07 — implementation in progress |
| Owner | @Kanyandula |
| Created | 2026-05-07 |
| Source | [Notion ticket](https://www.notion.so/Android-replace-index-0-Editor-s-Pick-rule-with-is_featured-from-API-359728b1385d815f8178dd3280f384ed) |
| API ticket (Done) | [Add `is_featured` filter](https://www.notion.so/Add-is_featured-filter-to-BlogPost-API-list-endpoint-359728b1385d81b79935d44bc10801ee) |
| API commit | [`Kanyandula/BlogPost@0806afd`](https://github.com/Kanyandula/BlogPost/commit/0806afd) |

## Background

The Android home feed renders an "EDITOR'S PICK" badge on whichever blog
post happens to land at index 0 of the paged list (`BlogFeedScreen.kt:208`).
That post is determined by the active sort order — currently newest-first by
default — and has no editorial meaning. The web at `nyasablog.com` shows a
real curated **Featured Story** hero driven by `BlogPost.is_featured`, a
Django-admin-toggled boolean. Result: the two surfaces feature different
posts, and the Android badge silently lies.

The API now exposes `is_featured` on every `/api/blog/list` result and
supports `?is_featured=true` and `?page_size=1` filters. Verified against
production 2026-05-07: page-1 results carry the field; `?is_featured=true`
currently returns exactly 1 post (`pk=105`, "Malawi Cichlids: Jewels of
Lake Malawi"). The Android `BlogSearchResponse` DTO does not declare
`is_featured`, so kotlinx-serialization silently drops it.

## Goals

1. Editor's Pick on Android matches the web's Featured Story exactly.
2. Badge is driven by server-curated state, not list position.
3. Hero card renders only when a featured post genuinely exists; no silent
   fallback to "first post."

## Non-goals

- Multi-featured carousel. Web shows only `featured_posts.0`; we match.
- Editorial UI inside the app (toggling `is_featured` from Android).
- Offline-first hero (deferred — see Open decisions).
- Analytics dashboard or new event types beyond what already exists.

## User-visible behavior

**Home tab, no search query:**
- A hero "EDITOR'S PICK" card renders above the paged list whenever the
  server reports a featured post.
- Tapping the hero opens the post detail (same flow as a regular feed tap).
- If no featured post exists site-wide, no hero renders. The paged list
  shows everything as today.

**Home tab with active search query (`query.isNotBlank()`):**
- No hero. Search results occupy the screen.

**Search tab, profile screens, etc.:**
- Unchanged. The hero is a Home-only affordance.

## Technical approach

### 1. DTO — surface `is_featured`

- Add `val is_featured: Boolean` to `BlogResponseFields`
  (`core/network/.../api/main/responses/BlogResponseFields.kt`).
- Add `@SerialName("is_featured") override var is_featured: Boolean = false`
  to `BlogSearchResponse` and `BlogCreateUpdateResponse`.
- Update mappers (`BlogResponseMappers.kt`) to forward the field.

Default `false` keeps deserialization safe if the server omits it for any
older code path.

### 2. Room — persist `is_featured` on `BlogPost`

- Add `@ColumnInfo(name = "is_featured") var is_featured: Boolean = false`
  to `models/BlogPost.kt`.
- Bump `AppDatabase` from version 7 → 8.
- Add `MIGRATION_7_8` running
  `ALTER TABLE blog_post ADD COLUMN is_featured INTEGER NOT NULL DEFAULT 0`.

Persisting the flag costs one column and a one-line migration. Keeps the
DTO→Entity mapping symmetric and unlocks future features (e.g. offline
hero) without a follow-up migration.

### 3. Network — single hero query

Add to `NyasaBlogApiMainService`:

```kotlin
@GET("blog/list")
suspend fun getFeaturedBlogPost(
    @Query("is_featured") isFeatured: Boolean = true,
    @Query("status") status: String = "published",
    @Query("page_size") pageSize: Int = 1
): Response<BlogListSearchResponse>
```

Reuses the existing `BlogListSearchResponse` shape — server returns a DRF
paginated envelope containing 0 or 1 result.

**Why `status=published`:** the web view (`personal/views.py:70`) filters
`is_featured=True, status='published'`. Without this we risk surfacing a
draft featured post on Android.

**Why no `ordering` param:** the web orders by `-date_published`, which
Android does not currently expose at the entity level. With `page_size=1`
and one featured post in production, ordering only matters in the
multi-featured edge case; trust the DRF default. If the multi-featured case
becomes meaningful, add `ordering=-date_published` explicitly (verify the
API accepts that key first).

### 4. Repository / use case

- New repository method on `BlogRepository` (interface in `core/domain`):
  `fun getFeaturedBlogPost(): Flow<Resource<BlogPost?>>`
- Implementation in `BlogRepositoryImpl` wraps the service call with the
  existing `safeApiCall`, maps the first result via the response mapper, and
  emits `Resource.Success(null)` when the list is empty (i.e. nothing is
  featured).
- New use case `GetFeaturedBlogPostUseCase` returning the same flow.

### 5. ViewModel — `BlogViewModel`

- Inject `GetFeaturedBlogPostUseCase`.
- Add `featuredHero: StateFlow<BlogPost?>` collected from the use case;
  `null` when no hero is available or fetch is in flight.
- Trigger the fetch once on Home open (e.g. lazy `init` or first observer).
- Refresh on pull-to-refresh alongside the existing paging refresh.

### 6. UI — `BlogFeedScreen`

- Drop the `index == 0` branch in `FeedItem` — `showEditorPick` parameter
  goes away, every paged item renders as a regular `NyasaBlogCard`.
- Add a `LazyListScope` header `item { … }` above the paging items that
  composes `EditorPickCard(featuredHero, …)` only when `featuredHero != null`
  AND `mode == FeedMode.Home` AND `query.isBlank()`.
- The existing `EditorPickCard` composable stays — it just gets a
  different data source.

## Open decisions (need confirmation before coding)

### A. Does the hero respect the active **category filter**? — RESOLVED

**No.** Web parity confirmed at `personal/views.py:70`: `featured_posts`
runs against the global `BlogPost` table with `is_featured=True,
status='published'`, ordered by `-date_published`, no category filter
applied. The hero stays put when the user filters home by category.

### B. Cache the hero in Room or fetch fresh? — RESOLVED

**Fetch fresh** on Home enter and on pull-to-refresh; no Room-backed
offline hero in v1. The per-item `is_featured` column added to `BlogPost`
(section 2) gives implicit caching of which posts are featured — a future
iteration can derive the offline hero from
`SELECT * FROM blog_post WHERE is_featured = 1 LIMIT 1` without further
migrations.

### C. New analytics events for hero impressions / taps? — RESOLVED

**No new events.** Tag the existing `blog_clicked` event with an
`is_featured: Boolean` parameter when the hero is the click source.
Existing dashboard can then slice "featured vs. organic" without new
event names.

## Test plan

- Unit tests on `GetFeaturedBlogPostUseCase` and the repository method:
  - Happy path: server returns 1 result → emits `Success(BlogPost)`
  - Empty: server returns 0 results → emits `Success(null)`
  - Error: 5xx → emits `Error(AppError.Server)`
- ViewModel test: `featuredHero` updates after refresh.
- UI test (Compose):
  - Home + no query + non-null hero → `EditorPickCard` rendered with
    correct `pk` / title.
  - Home + no query + null hero → no `EditorPickCard` in the tree.
  - Search query non-blank → no `EditorPickCard`.
  - Non-Home mode → no `EditorPickCard`.
- Manual on emulator-5554:
  - Confirm hero shows pk=105 ("Malawi Cichlids") on Home.
  - Toggle the `is_featured` flag server-side via Django admin → pull to
    refresh on Android → hero updates to the new post.
  - Toggle all `is_featured` off → hero disappears, paged list unchanged.

## Acceptance criteria

- [ ] `BlogPost` DTO + entity carry `is_featured`, parsed from
      `is_featured` JSON field.
- [ ] Editor's Pick badge appears on `post.is_featured == true` posts only.
- [ ] No badge appears when no featured post is available.
- [ ] Badge gating still respects `mode == FeedMode.Home && query.isBlank()`.
- [ ] Tests added for the new badge logic and use case.
- [ ] Detekt + spotlessCheck + lintDebug + test all green.
- [ ] Manual parity check with `nyasablog.com`: hero post matches web hero.

## References

- Notion ticket: see top of doc
- API commit: `Kanyandula/BlogPost@0806afd`
- Android touch points:
  - `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogFeedScreen.kt:208`
  - `core/network/src/main/java/com/kanyandula/nyasa/api/main/responses/BlogResponseFields.kt`
  - `core/network/src/main/java/com/kanyandula/nyasa/api/main/responses/BlogSearchResponse.kt`
  - `core/network/src/main/java/com/kanyandula/nyasa/api/main/responses/BlogResponseMappers.kt`
  - `core/network/src/main/java/com/kanyandula/nyasa/api/main/NyasaBlogApiMainService.kt`
  - `core/database/src/main/java/com/kanyandula/nyasa/models/BlogPost.kt`
  - `core/database/src/main/java/com/kanyandula/nyasa/persistance/AppDatabase.kt`
- Web parity: `personal/views.py:70`, `personal/templates/personal/home.html:7-21`
  (Django source at `~/PycharmProjects/nyasablog/`)