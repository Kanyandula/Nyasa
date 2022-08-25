package com.kanyandula.nyasa.persistance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanyandula.nyasa.models.AuthToken

@Dao
interface AuthTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(authToken: AuthToken): Long

    @Query("UPDATE auth_token SET token = null WHERE account_pk = :pk")
    fun nullifyToken(pk: Int): Int
}