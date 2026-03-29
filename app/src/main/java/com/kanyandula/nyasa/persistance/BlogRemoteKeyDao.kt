package com.kanyandula.nyasa.persistance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanyandula.nyasa.models.BlogRemoteKey

@Dao
interface BlogRemoteKeyDao {

    @Query("SELECT * FROM blog_remote_keys WHERE queryKey = :queryKey")
    suspend fun getRemoteKey(queryKey: String): BlogRemoteKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKey: BlogRemoteKey)

    @Query("DELETE FROM blog_remote_keys WHERE queryKey = :queryKey")
    suspend fun deleteByQuery(queryKey: String)
}
