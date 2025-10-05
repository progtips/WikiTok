package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article
import com.example.wikitok.domain.recommend.IRecommender
import com.example.wikitok.domain.history.IRecentHistory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ArticlesSource {
    suspend fun fetchBatch(n: Int): List<Article>
}

class FeedBuffer(
    private val source: ArticlesSource,
    private val recommender: IRecommender,
    private val capacity: Int = 10,
    private val recentHistory: IRecentHistory? = null
) : IFeedBuffer {
    private val mutex = Mutex()
    private val buffer = ArrayList<Article>()
    private val seenIds = HashSet<Long>()

    override suspend fun primeIfNeeded() = mutex.withLock {
        if (buffer.size < capacity / 2) {
            val need = capacity - buffer.size
            val batch = source.fetchBatch(need * 3) // запас, чтобы отфильтровать дубли и недавние
            for (a in batch) {
                val skipRecent = recentHistory?.wasRecentlyShown(a.pageId) == true
                if (!skipRecent && seenIds.add(a.pageId) && buffer.size < capacity) buffer += a
            }
        }
    }

    override suspend fun next(): Article? = mutex.withLock {
        if (buffer.isEmpty()) {
            primeIfNeeded()
            if (buffer.isEmpty()) return null
        }
        val epsilon = com.wikitok.settings.LocalSettingsRepository.current.settingsFlow
            .replayCache.firstOrNull()?.explorationEpsilon ?: 0.2f
        val picked = if (Math.random() < epsilon) {
            buffer.random()
        } else {
            val maxScore = buffer.maxOf { recommender.score(it) }
            val candidates = buffer.filter { recommender.score(it) == maxScore }
            if (candidates.isNotEmpty()) candidates.random() else buffer.random()
        }
        buffer.remove(picked)
        recentHistory?.let { kotlinx.coroutines.runBlocking { it.addShown(picked.pageId) } }
        if (com.example.wikitok.BuildConfig.DEBUG) {
            android.util.Log.d("Feed", "next pageId=" + picked.pageId + ", score=" + maxScore)
        }
        picked
    }
}
