package com.example.wikitok.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedArticleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(entity: LikedArticleEntity)

    @Query("DELETE FROM liked_articles WHERE pageId = :pageId")
    suspend fun deleteById(pageId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_articles WHERE pageId = :pageId)")
    fun isLiked(pageId: Long): Flow<Boolean>

    @Query("SELECT * FROM liked_articles ORDER BY likedAt DESC")
    fun getAllLiked(): Flow<List<LikedArticleEntity>>
}


