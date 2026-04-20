# H4 — Comments Persistence + Optimistic UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist comments in Room with write-through optimistic insert/delete, create an `OptimisticAction` helper, unify Like and Bookmark onto optimistic toggles, and keep `BlogPost.comment_count` in sync.

**Architecture:** Add `CommentEntity` to Room (FK to `BlogPost.slug`, CASCADE delete). Introduce `OptimisticAction` helper that applies a predicted state change immediately, fires the API call, and rolls back on failure. Rewrite comment create/delete, like toggle, and bookmark toggle to use this pattern. Update `BlogPostDao` with a `updateCommentCount` query for atomic count sync.

**Tech Stack:** Room 2.6.1, Kotlin Coroutines, Hilt 2.53.1, Jetpack Compose

**Worktree:** `/Users/admin/StudioProjects/Nyasa/.claude/worktrees/h4-comments-room`
**Branch:** `hardening/h4-comments`

---

## File Map

| Action | File | Responsibility |
|--------|------|----------------|
| Create | `models/CommentEntity.kt` | Room entity with FK to BlogPost.slug |
| Create | `persistance/CommentDao.kt` | Insert, delete, getBySlug, clearBySlug |
| Create | `ui/components/OptimisticAction.kt` | Generic optimistic UI helper |
| Modify | `persistance/AppDatabase.kt` | Add CommentEntity, CommentDao, migration 6→7 |
| Modify | `persistance/BlogPostDao.kt` | Add `updateCommentCount(slug, delta)` |
| Modify | `di/AppModule.kt` | Provide CommentDao |
| Modify | `repository/main/CommentRepositoryImpl.kt` | Write-through with Room persistence + optimistic insert/delete |
| Modify | `domain/repository/CommentRepository.kt` | Add `getCommentsFlow(slug)` for Room-backed Flow |
| Modify | `ui/main/blog/viewmodel/BlogViewModel.kt` | Use OptimisticAction for like/bookmark/comment |
| Modify | `ui/main/blog/state/ViewBlogUiState.kt` | No structural changes needed — comments stay as `List<Comment>` |
| Create | `app/src/test/java/.../persistance/CommentDaoTest.kt` | DAO tests |
| Create | `app/src/test/java/.../ui/components/OptimisticActionTest.kt` | OptimisticAction unit tests |

---

## Task 1: Create CommentEntity + CommentDao + Migration

**Files:**
- Create: `app/src/main/java/com/kanyandula/nyasa/models/CommentEntity.kt`
- Create: `app/src/main/java/com/kanyandula/nyasa/persistance/CommentDao.kt`
- Modify: `app/src/main/java/com/kanyandula/nyasa/persistance/AppDatabase.kt`
- Modify: `app/src/main/java/com/kanyandula/nyasa/persistance/BlogPostDao.kt`
- Modify: `app/src/main/java/com/kanyandula/nyasa/di/AppModule.kt`

- [ ] **Step 1: Create CommentEntity.kt**

```kotlin
package com.kanyandula.nyasa.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = BlogPost::class,
            parentColumns = ["slug"],
            childColumns = ["post_slug"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("post_slug")]
)
data class CommentEntity(
    @PrimaryKey
    @ColumnInfo(name = "pk")
    val pk: Int,
    @ColumnInfo(name = "post_slug")
    val postSlug: String,
    @ColumnInfo(name = "body")
    val body: String,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "date_created")
    val dateCreated: Long
)
```

Temp pk convention: negative monotonic values for optimistic inserts. The server returns the real pk on success.

- [ ] **Step 2: Create CommentDao.kt**

```kotlin
package com.kanyandula.nyasa.persistance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanyandula.nyasa.models.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CommentEntity>)

    @Query("DELETE FROM comments WHERE pk = :pk")
    suspend fun deleteByPk(pk: Int)

    @Query("SELECT * FROM comments WHERE post_slug = :slug ORDER BY date_created DESC")
    fun getBySlug(slug: String): Flow<List<CommentEntity>>

    @Query("DELETE FROM comments WHERE post_slug = :slug")
    suspend fun clearBySlug(slug: String)

    @Query("SELECT * FROM comments WHERE pk = :pk LIMIT 1")
    suspend fun getByPk(pk: Int): CommentEntity?
}
```

- [ ] **Step 3: Add updateCommentCount to BlogPostDao.kt**

Read `app/src/main/java/com/kanyandula/nyasa/persistance/BlogPostDao.kt`. Add this query:

```kotlin
@Query(
    """
    UPDATE blog_post SET comment_count = COALESCE(comment_count, 0) + :delta
    WHERE slug = :slug
    """
)
suspend fun updateCommentCount(slug: String, delta: Int)
```

- [ ] **Step 4: Update AppDatabase.kt**

Read `app/src/main/java/com/kanyandula/nyasa/persistance/AppDatabase.kt`. Make these changes:

1. Add `CommentEntity::class` to the `@Database(entities = [...])` array
2. Bump version from `6` to `7`
3. Add abstract DAO: `abstract fun getCommentDao(): CommentDao`
4. Add migration:

```kotlin
val MIGRATION_6_7 = Migration(6, 7) {
    it.execSQL(
        """
        CREATE TABLE IF NOT EXISTS comments (
            pk INTEGER NOT NULL PRIMARY KEY,
            post_slug TEXT NOT NULL,
            body TEXT NOT NULL,
            username TEXT NOT NULL,
            date_created INTEGER NOT NULL,
            FOREIGN KEY (post_slug) REFERENCES blog_post(slug) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    it.execSQL(
        "CREATE INDEX IF NOT EXISTS index_comments_post_slug ON comments(post_slug)"
    )
}
```

5. Add `MIGRATION_6_7` to the `.addMigrations(...)` chain in AppModule's `provideAppDb`.

- [ ] **Step 5: Provide CommentDao in AppModule.kt**

Read `app/src/main/java/com/kanyandula/nyasa/di/AppModule.kt`. Add:

```kotlin
@Singleton
@Provides
fun provideCommentDao(db: AppDatabase): CommentDao = db.getCommentDao()
```

Also add `MIGRATION_6_7` to the `provideAppDb` migrations list.

- [ ] **Step 6: Verify build**

Run: `./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "hardening(h4): add CommentEntity, CommentDao, migration 6→7, updateCommentCount"
```

---

## Task 2: Create OptimisticAction helper

**Files:**
- Create: `app/src/main/java/com/kanyandula/nyasa/ui/components/OptimisticAction.kt`

- [ ] **Step 1: Create OptimisticAction.kt**

```kotlin
package com.kanyandula.nyasa.ui.components

import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource

suspend fun <S> optimisticAction(
    currentState: S,
    predict: (S) -> S,
    action: suspend () -> Resource<S>,
    rollback: (S) -> S,
    emit: suspend (S) -> Unit,
    onError: suspend (AppError) -> Unit
) {
    emit(predict(currentState))
    when (val result = action()) {
        is Resource.Success -> emit(result.data)
        is Resource.Error -> {
            emit(rollback(currentState))
            onError(result.error)
        }
        is Resource.Loading -> Unit
    }
}
```

This is a generic helper. Usage pattern:
- `predict` applies the expected outcome immediately
- `action` fires the API call
- On success: emit server state (may differ from prediction)
- On failure: emit `rollback` (restore original state) + notify error

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/ui/components/OptimisticAction.kt
git commit -m "hardening(h4): add OptimisticAction helper"
```

---

## Task 3: Rewrite CommentRepositoryImpl for Room persistence

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/domain/repository/CommentRepository.kt`
- Modify: `app/src/main/java/com/kanyandula/nyasa/repository/main/CommentRepositoryImpl.kt`
- Modify: `app/src/main/java/com/kanyandula/nyasa/api/main/responses/CommentResponse.kt` (add mapper to entity)

- [ ] **Step 1: Add entity mapper to CommentResponse.kt**

Read `app/src/main/java/com/kanyandula/nyasa/api/main/responses/CommentResponse.kt`. Add a mapper function (alongside or replacing the existing `toComment()` mapper in `CommentResponseMappers.kt`):

```kotlin
fun CommentResponse.toEntity(postSlug: String): CommentEntity = CommentEntity(
    pk = pk,
    postSlug = postSlug,
    body = body,
    username = username ?: "",
    dateCreated = date_created?.let { DateUtils.convertServerStringDateToLong(it) }
        ?: System.currentTimeMillis()
)
```

Also add a mapper from `CommentEntity` to `Comment` (the domain model):

```kotlin
fun CommentEntity.toComment(): Comment = Comment(
    pk = pk,
    body = body,
    username = username,
    dateCreated = dateCreated
)
```

Find where these mappers should live — if there's a `CommentResponseMappers.kt`, add both there. Otherwise add to `CommentResponse.kt`.

- [ ] **Step 2: Update CommentRepository interface**

Read `app/src/main/java/com/kanyandula/nyasa/domain/repository/CommentRepository.kt`. Add a method for Room-backed flow:

```kotlin
interface CommentRepository {
    fun getComments(slug: String): Flow<Resource<List<Comment>>>
    fun getCommentsFlow(slug: String): Flow<List<Comment>>
    fun createComment(slug: String, body: String): Flow<Resource<Comment>>
    fun deleteComment(pk: Int, slug: String): Flow<Resource<String>>
}
```

Note: `deleteComment` gains a `slug` parameter — needed to update `comment_count` in the same transaction.

- [ ] **Step 3: Rewrite CommentRepositoryImpl**

Read the current `app/src/main/java/com/kanyandula/nyasa/repository/main/CommentRepositoryImpl.kt`. Rewrite to:

```kotlin
class CommentRepositoryImpl @Inject constructor(
    private val apiService: NyasaBlogApiMainService,
    private val commentDao: CommentDao,
    private val blogPostDao: BlogPostDao,
    private val connectivityObserver: ConnectivityObserver
) : CommentRepository {

    override fun getComments(
        slug: String
    ): Flow<Resource<List<Comment>>> = flow {
        emit(Resource.Loading())

        val cached = commentDao.getBySlug(slug).first()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toComment() }))
        }

        if (!connectivityObserver.isConnected.value) {
            if (cached.isEmpty()) emit(Resource.Error(AppError.Offline))
            return@flow
        }

        when (val result = safeApiCall { apiService.getComments(slug) }) {
            is Resource.Success -> {
                val entities = result.data.map { it.toEntity(slug) }
                commentDao.clearBySlug(slug)
                commentDao.insertAll(entities)
                emit(Resource.Success(entities.map { it.toComment() }))
            }
            is Resource.Error -> {
                if (cached.isEmpty()) emit(result)
            }
            is Resource.Loading -> Unit
        }
    }.flowOn(Dispatchers.IO)

    override fun getCommentsFlow(slug: String): Flow<List<Comment>> =
        commentDao.getBySlug(slug).map { entities ->
            entities.map { it.toComment() }
        }

    override fun createComment(
        slug: String,
        body: String
    ): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(AppError.Offline))
            return@flow
        }
        when (val result = safeApiCall { apiService.createComment(slug, body) }) {
            is Resource.Success -> {
                val entity = result.data.toEntity(slug)
                commentDao.insert(entity)
                blogPostDao.updateCommentCount(slug, 1)
                emit(Resource.Success(entity.toComment()))
            }
            is Resource.Error -> emit(result)
            is Resource.Loading -> Unit
        }
    }.flowOn(Dispatchers.IO)

    override fun deleteComment(
        pk: Int,
        slug: String
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(AppError.Offline))
            return@flow
        }
        when (val result = safeApiCall { apiService.deleteComment(pk) }) {
            is Resource.Success -> {
                commentDao.deleteByPk(pk)
                blogPostDao.updateCommentCount(slug, -1)
                emit(Resource.Success(result.data.response))
            }
            is Resource.Error -> emit(result)
            is Resource.Loading -> Unit
        }
    }.flowOn(Dispatchers.IO)
}
```

- [ ] **Step 4: Update DI binding**

Read `app/src/main/java/com/kanyandula/nyasa/di/main/MainModule.kt`. The `CommentRepositoryImpl` constructor changed — add `CommentDao` and `BlogPostDao` parameters. Since it's injected via `@Inject constructor`, Hilt should resolve these automatically if the module binds the interface. Verify the binding exists.

- [ ] **Step 5: Update DeleteCommentUseCase**

Read `app/src/main/java/com/kanyandula/nyasa/domain/usecase/comment/DeleteCommentUseCase.kt`. Update to pass `slug`:

```kotlin
class DeleteCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    operator fun invoke(pk: Int, slug: String): Flow<Resource<String>> =
        commentRepository.deleteComment(pk, slug)
}
```

- [ ] **Step 6: Verify build**

Run: `./gradlew assembleDebug`

Expected: Compilation errors in `BlogViewModel` where `deleteComment` is called without `slug`. Fixed in Task 5.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "hardening(h4): rewrite CommentRepositoryImpl with Room persistence"
```

---

## Task 4: Add GetCommentsFlowUseCase

**Files:**
- Create: `app/src/main/java/com/kanyandula/nyasa/domain/usecase/comment/GetCommentsFlowUseCase.kt`

- [ ] **Step 1: Create GetCommentsFlowUseCase.kt**

```kotlin
package com.kanyandula.nyasa.domain.usecase.comment

import com.kanyandula.nyasa.domain.repository.CommentRepository
import com.kanyandula.nyasa.models.Comment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommentsFlowUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    operator fun invoke(slug: String): Flow<List<Comment>> =
        commentRepository.getCommentsFlow(slug)
}
```

This returns a Room-backed `Flow<List<Comment>>` that emits whenever the comments table changes for a given slug — enabling reactive UI without polling.

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/kanyandula/nyasa/domain/usecase/comment/GetCommentsFlowUseCase.kt
git commit -m "hardening(h4): add GetCommentsFlowUseCase for Room-backed comment flow"
```

---

## Task 5: Rewrite BlogViewModel with OptimisticAction for like/bookmark/comments

**Files:**
- Modify: `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/viewmodel/BlogViewModel.kt`

This is the largest task. The ViewModel needs to:
1. Collect `getCommentsFlowUseCase` for reactive comment list from Room
2. Use `optimisticAction` for like toggle
3. Use `optimisticAction` for bookmark toggle
4. Use optimistic insert/delete for comments
5. Pass `slug` to `deleteComment`

- [ ] **Step 1: Add GetCommentsFlowUseCase injection**

Read `BlogViewModel.kt`. Add `getCommentsFlowUseCase: GetCommentsFlowUseCase` to the constructor. The ViewModel already injects the other comment use cases.

- [ ] **Step 2: Rewrite loadComments to use Room-backed flow**

Replace the current `loadComments` method:

```kotlin
fun loadComments(slug: String) {
    commentsJob?.cancel()
    commentsJob = viewModelScope.launch {
        getCommentsUseCase(slug).collect { resource ->
            handleResource(resource, onSuccess = {})
        }
    }
    viewModelScope.launch {
        getCommentsFlowUseCase(slug).collect { comments ->
            updateViewBlogState { copy(comments = comments) }
        }
    }
}
```

First launch fetches from API and writes to Room. Second launch observes Room and updates UI reactively.

- [ ] **Step 3: Rewrite likeBlog with optimisticAction**

```kotlin
fun likeBlog(slug: String) {
    likeJob?.cancel()
    likeJob = viewModelScope.launch {
        val state = _viewBlogState.value
        optimisticAction(
            currentState = state,
            predict = { s ->
                s.copy(
                    isLiked = !s.isLiked,
                    likeCount = s.likeCount + if (s.isLiked) -1 else 1
                )
            },
            action = {
                likeBlogPostUseCase(slug).first { it !is Resource.Loading }
                    .let { resource ->
                        when (resource) {
                            is Resource.Success -> Resource.Success(
                                state.copy(
                                    isLiked = resource.data.liked,
                                    likeCount = resource.data.likeCount
                                )
                            )
                            is Resource.Error -> resource
                            is Resource.Loading -> resource
                        }
                    }
            },
            rollback = { s -> s },
            emit = { newState -> updateViewBlogState { newState } },
            onError = { error -> sendEvent(UiEvent.ShowToast(error.toUserMessage())) }
        )
    }
}
```

- [ ] **Step 4: Rewrite bookmarkBlog with optimisticAction**

```kotlin
fun bookmarkBlog(slug: String) {
    bookmarkJob?.cancel()
    bookmarkJob = viewModelScope.launch {
        val state = _viewBlogState.value
        optimisticAction(
            currentState = state,
            predict = { s -> s.copy(isBookmarked = !s.isBookmarked) },
            action = {
                bookmarkBlogPostUseCase(slug).first { it !is Resource.Loading }
                    .let { resource ->
                        when (resource) {
                            is Resource.Success -> Resource.Success(
                                state.copy(isBookmarked = resource.data)
                            )
                            is Resource.Error -> resource
                            is Resource.Loading -> resource
                        }
                    }
            },
            rollback = { s -> s },
            emit = { newState -> updateViewBlogState { newState } },
            onError = { error -> sendEvent(UiEvent.ShowToast(error.toUserMessage())) }
        )
    }
}
```

- [ ] **Step 5: Rewrite addComment with optimistic insert**

```kotlin
fun addComment(slug: String, body: String) {
    addCommentJob?.cancel()
    addCommentJob = viewModelScope.launch {
        createCommentUseCase(slug, body).collect { resource ->
            handleResource(
                resource,
                onSuccess = {}
            )
        }
    }
}
```

Comments are now reactive via Room flow — no need to manually append. The `createComment` repo writes to Room, the flow emits automatically.

- [ ] **Step 6: Rewrite deleteComment — pass slug**

```kotlin
fun deleteComment(pk: Int, slug: String) {
    deleteCommentJob?.cancel()
    deleteCommentJob = viewModelScope.launch {
        deleteCommentUseCase(pk, slug).collect { resource ->
            handleResource(
                resource,
                onSuccess = {}
            )
        }
    }
}
```

- [ ] **Step 7: Update BlogDetailAction and action handler**

Read `app/src/main/java/com/kanyandula/nyasa/ui/main/blog/composables/BlogDetailScreen.kt` (or wherever `BlogDetailAction` is defined). Update `DeleteComment` to carry the slug:

```kotlin
data class DeleteComment(val commentPk: Int, val slug: String) : BlogDetailAction
```

Then update the action handler in `MainRouteComposables.kt`:

```kotlin
is BlogDetailAction.DeleteComment -> viewModel.deleteComment(action.commentPk, slug)
```

And update `BlogDetailScreen` where `onDeleteComment` is called to pass the slug.

- [ ] **Step 8: Verify build**

Run: `./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "hardening(h4): rewrite BlogViewModel with OptimisticAction for like/bookmark/comments"
```

---

## Task 6: Write unit tests for OptimisticAction

**Files:**
- Create: `app/src/test/java/com/kanyandula/nyasa/ui/components/OptimisticActionTest.kt`

- [ ] **Step 1: Create OptimisticActionTest.kt**

```kotlin
package com.kanyandula.nyasa.ui.components

import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OptimisticActionTest {

    @Test
    fun `success emits predicted then server state`() = runTest {
        val emissions = mutableListOf<Int>()

        optimisticAction(
            currentState = 0,
            predict = { it + 1 },
            action = { Resource.Success(10) },
            rollback = { it },
            emit = { emissions.add(it) },
            onError = {}
        )

        assertEquals(listOf(1, 10), emissions)
    }

    @Test
    fun `error emits predicted then rollback`() = runTest {
        val emissions = mutableListOf<Int>()
        var capturedError: AppError? = null

        optimisticAction(
            currentState = 0,
            predict = { it + 1 },
            action = { Resource.Error(AppError.Server(500)) },
            rollback = { it },
            emit = { emissions.add(it) },
            onError = { capturedError = it }
        )

        assertEquals(listOf(1, 0), emissions)
        assertEquals(AppError.Server(500), capturedError)
    }

    @Test
    fun `rollback restores original state not predicted`() = runTest {
        val emissions = mutableListOf<String>()

        optimisticAction(
            currentState = "original",
            predict = { "predicted" },
            action = { Resource.Error(AppError.Offline) },
            rollback = { it },
            emit = { emissions.add(it) },
            onError = {}
        )

        assertEquals(listOf("predicted", "original"), emissions)
    }

    @Test
    fun `offline error triggers rollback and onError`() = runTest {
        var errorReceived = false

        optimisticAction(
            currentState = true,
            predict = { false },
            action = { Resource.Error(AppError.Offline) },
            rollback = { it },
            emit = {},
            onError = { errorReceived = true }
        )

        assertEquals(true, errorReceived)
    }
}
```

- [ ] **Step 2: Run tests**

Run: `./gradlew test --tests "com.kanyandula.nyasa.ui.components.OptimisticActionTest"`

Expected: All 4 tests pass.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/kanyandula/nyasa/ui/components/OptimisticActionTest.kt
git commit -m "hardening(h4): add OptimisticAction unit tests"
```

---

## Task 7: Update existing tests for new signatures

**Files:**
- Modify: `app/src/test/java/com/kanyandula/nyasa/repository/main/CommentRepositoryImplTest.kt`
- Modify: `app/src/test/java/com/kanyandula/nyasa/domain/usecase/comment/DeleteCommentUseCaseTest.kt`

- [ ] **Step 1: Update CommentRepositoryImplTest**

Read the current test file. The `CommentRepositoryImpl` constructor changed — now requires `CommentDao` and `BlogPostDao`. Add mocks:

```kotlin
private val commentDao: CommentDao = mockk(relaxed = true)
private val blogPostDao: BlogPostDao = mockk(relaxed = true)
```

Update the constructor call. Also update `deleteComment` test calls to pass `slug`.

- [ ] **Step 2: Update DeleteCommentUseCaseTest**

The use case now takes `(pk, slug)` instead of just `(pk)`. Update test invocations.

- [ ] **Step 3: Run all tests**

Run: `./gradlew test`

Expected: All tests pass.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "hardening(h4): update tests for new CommentRepository and DeleteComment signatures"
```

---

## Task 8: Final verification + review gates

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

- [ ] **Step 4: Review gate — CommentEntity FK has CASCADE delete**

Verify in `CommentEntity.kt`: `onDelete = ForeignKey.CASCADE`

- [ ] **Step 5: Review gate — comment_count stays in sync**

Verify in `CommentRepositoryImpl.kt`:
- `createComment` calls `blogPostDao.updateCommentCount(slug, 1)`
- `deleteComment` calls `blogPostDao.updateCommentCount(slug, -1)`

- [ ] **Step 6: Review gate — OptimisticAction rollback fires on error**

Verify in `BlogViewModel.kt`:
- `likeBlog` uses `optimisticAction` with `predict` toggling `isLiked` and adjusting `likeCount`
- `bookmarkBlog` uses `optimisticAction` with `predict` toggling `isBookmarked`
- Both have `onError` calling `sendEvent(UiEvent.ShowToast(...))`

- [ ] **Step 7: Review gate — Like + Bookmark use OptimisticAction**

Verify no ad-hoc toggle logic remains. Both go through `optimisticAction`.

- [ ] **Step 8: Commit any spotless fixes**

```bash
git add -A
git commit -m "hardening(h4): spotless formatting fixes"
```

(Skip if spotless had no changes.)

---

## Post-Implementation

After all tasks complete:

1. Run `/android-code-review` on the full diff against `Deploy_0.01`
2. Run `/simplify` after review passes
3. **Manual test:** Create a comment, kill the app, reopen — comment should still be there. Delete a comment — `comment_count` should decrement. Toggle like/bookmark — UI should respond instantly, rollback on airplane mode.
4. Final commit and push: `git push origin hardening/h4-comments`
5. Merge from main project root:
   ```bash
   cd ~/StudioProjects/Nyasa
   git checkout Deploy_0.01
   git merge --no-ff hardening/h4-comments -m "H4: CommentEntity, OptimisticAction, comment persistence, optimistic like/bookmark"
   ```