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
