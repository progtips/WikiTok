package com.example.wikitok.data.likes

import com.example.wikitok.domain.Article
import com.example.wikitok.data.local.LikedArticleDao
import com.example.wikitok.domain.toDomain
import com.example.wikitok.domain.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LikesRepositoryImpl @Inject constructor(
    private val dao: LikedArticleDao
) : ILikesRepository {
    override suspend fun like(article: Article) {
        dao.insertOrIgnore(article.toEntity())
    }

    override suspend fun unlike(pageId: Long) {
        dao.deleteById(pageId)
    }

    override fun isLiked(pageId: Long): Flow<Boolean> = dao.isLiked(pageId)

    override fun likedFeed(): Flow<List<Article>> =
        dao.getAllLiked().map { list -> list.map { it.toDomain() } }
}
