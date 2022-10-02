package com.kanyandula.nyasa.persistance


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.kanyandula.nyasa.models.BlogPost

@Dao
interface BlogPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blogPost: BlogPost): Long
}



