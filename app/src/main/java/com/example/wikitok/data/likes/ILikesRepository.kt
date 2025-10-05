package com.example.wikitok.data.likes

import com.example.wikitok.domain.Article
import kotlinx.coroutines.flow.Flow

interface ILikesRepository {
    suspend fun like(article: Article)
    suspend fun unlike(pageId: Long)
    fun isLiked(pageId: Long): Flow<Boolean>
    fun likedFeed(): Flow<List<Article>>
}
