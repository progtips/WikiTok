package com.example.wikitok.data.likes

import com.example.wikitok.domain.Article
import com.example.wikitok.data.local.LikedArticleDao
import com.example.wikitok.data.local.LikedArticleEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LikesRepositoryImplTest {

    @Test
    fun like_inserts_entity() = runTest {
        val dao: LikedArticleDao = mock()
        val repo = LikesRepositoryImpl(dao)
        val article = Article(
            pageId = 123L,
            title = "T",
            summary = null,
            imageUrl = null,
            categories = emptyList()
        )
        repo.like(article)
        verify(dao).insertOrIgnore(org.mockito.kotlin.any())
    }

    @Test
    fun unlike_calls_delete() = runTest {
        val dao: LikedArticleDao = mock()
        val repo = LikesRepositoryImpl(dao)
        repo.unlike(42L)
        verify(dao).deleteById(42L)
    }

    @Test
    fun isLiked_forwards_flow() = runTest {
        val dao: LikedArticleDao = mock()
        whenever(dao.isLiked(7L)).thenReturn(flowOf(true))
        val repo = LikesRepositoryImpl(dao)
        val first = repo.isLiked(7L)
        // Just ensure flow callable
        assertNotNull(first)
    }

    @Test
    fun likedFeed_maps_entities() = runTest {
        val dao: LikedArticleDao = mock()
        whenever(dao.getAllLiked()).thenReturn(flowOf(listOf(
            LikedArticleEntity(1L, "A", null, "[]", 0L)
        )))
        val repo = LikesRepositoryImpl(dao)
        val list = repo.likedFeed().first()
        assertEquals(1, list.size)
        assertEquals(1L, list.first().pageId)
    }
}
