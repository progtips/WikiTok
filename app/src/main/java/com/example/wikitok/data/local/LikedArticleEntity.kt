package com.example.wikitok.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_articles")
data class LikedArticleEntity(
    @PrimaryKey val pageId: Long,
    val title: String,
    val thumbnailUrl: String?,
    val categoriesJson: String,
    val likedAt: Long
)


