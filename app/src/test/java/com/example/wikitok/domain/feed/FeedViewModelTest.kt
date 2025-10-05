package com.example.wikitok.domain.feed

import com.example.wikitok.data.likes.ILikesRepository
import com.example.wikitok.data.prefs.ICategoryWeightsStore
import com.example.wikitok.domain.Article
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.never

class FeedViewModelTest {

    @Test
    fun onLike_calls_repos_and_loads_next() = runTest {
        val likes: ILikesRepository = mock()
        val prefs: ICategoryWeightsStore = mock()
        val buf: IFeedBuffer = mock()
        whenever(buf.next()).thenReturn(
            Article(1,"t",null,null, listOf("science")),
            Article(2,"t2",null,null, listOf("history"))
        )
        whenever(likes.isLiked(1)).thenReturn(flowOf(false))
        val vm = FeedViewModel(likes, prefs, buf)
        vm.loadNext()
        vm.onLike()
        verify(likes).like(org.mockito.kotlin.any())
        verify(prefs).bumpPositive(org.mockito.kotlin.any())
    }

    @Test
    fun undo_like_calls_unlike() = runTest {
        val likes: ILikesRepository = mock()
        val prefs: ICategoryWeightsStore = mock()
        val buf: IFeedBuffer = mock()
        whenever(buf.next()).thenReturn(
            Article(1,"t",null,null, listOf("science"))
        )
        whenever(likes.isLiked(1)).thenReturn(flowOf(true))
        val vm = FeedViewModel(likes, prefs, buf)
        vm.loadNext()
        vm.unlike(1)
        verify(likes).unlike(1)
    }

    @Test
    fun onSkip_calls_prefs_and_loads_next() = runTest {
        val likes: ILikesRepository = mock()
        val prefs: ICategoryWeightsStore = mock()
        val buf: IFeedBuffer = mock()
        whenever(buf.next()).thenReturn(
            Article(1,"t",null,null, listOf("science")),
            Article(2,"t2",null,null, listOf("history"))
        )
        whenever(likes.isLiked(1)).thenReturn(flowOf(false))
        val vm = FeedViewModel(likes, prefs, buf)
        vm.loadNext()
        vm.onSkip()
        verify(prefs).bumpNegative(org.mockito.kotlin.any())
    }
}
