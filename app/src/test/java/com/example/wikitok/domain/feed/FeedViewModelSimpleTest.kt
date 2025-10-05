package com.example.wikitok.domain.feed

import app.cash.turbine.test
import com.example.wikitok.data.likes.ILikesRepository
import com.example.wikitok.data.prefs.ICategoryWeightsStore
import com.example.wikitok.domain.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

private class FakeBuffer(private val items: MutableList<Article>) : IFeedBuffer {
    override suspend fun primeIfNeeded() { /* no-op for test */ }
    override suspend fun next(): Article? = if (items.isEmpty()) null else items.removeAt(0)
}

private class NoopLikesRepo : ILikesRepository {
    override suspend fun like(article: Article) {}
    override suspend fun unlike(pageId: Long) {}
    override fun isLiked(pageId: Long): Flow<Boolean> = flowOf(false)
    override fun likedFeed(): Flow<List<Article>> = flowOf(emptyList())
}

private class NoopWeights : ICategoryWeightsStore {
    override suspend fun bumpPositive(categories: List<String>, delta: Float) {}
    override suspend fun bumpNegative(categories: List<String>, delta: Float) {}
    override fun observeWeights(): Flow<Map<String, Float>> = flowOf(emptyMap())
    override suspend fun getTopCategories(limit: Int): List<String> = emptyList()
    override suspend fun setWeight(category: String, weight: Float) {}
    override suspend fun resetAll() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelSimpleTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun current_changes_and_never_null_after_first_load() = runTest(testDispatcher) {
        val a1 = Article(pageId = 1, title = "A1", summary = "s1", imageUrl = null, categories = listOf("X"))
        val a2 = Article(pageId = 2, title = "A2", summary = "s2", imageUrl = null, categories = listOf("Y"))
        val vm = FeedViewModel(
            likesRepository = NoopLikesRepo(),
            preferencesStore = NoopWeights(),
            feedBuffer = FakeBuffer(mutableListOf(a1, a2))
        )

        // init triggers first load
        advanceUntilIdle()
        val firstId = vm.current.value?.pageId
        assertNotNull(firstId)
        assertEquals(1L, firstId)

        // Call quickly skip and like
        vm.onSkip()
        vm.onLike()
        advanceUntilIdle()

        val secondId = vm.current.value?.pageId
        assertNotNull(secondId)
        // moved to different article (a2), not null even though buffer is empty afterwards
        assertEquals(2L, secondId)
    }
}


