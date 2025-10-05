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
    private var hasPrimed: Boolean = false

    override suspend fun primeIfNeeded() = mutex.withLock {
        if (buffer.size < capacity / 2) {
            val need = capacity - buffer.size

            // Быстрый путь на старте: попытаться взять 1 статью максимально быстро
            if (!hasPrimed && buffer.isEmpty()) {
                runCatching { source.fetchBatch(1) }.getOrNull()?.forEach { a ->
                    val skipRecent = recentHistory?.wasRecentlyShown(a.pageId) == true
                    if (!skipRecent && seenIds.add(a.pageId) && buffer.size < capacity) buffer += a
                }
            }

            // Основная догрузка (умеренная на старте, больше после)
            val batchSize = if (!hasPrimed) need else (need * 2)
            val batch = runCatching { source.fetchBatch(batchSize) }.getOrDefault(emptyList())
            for (a in batch) {
                val skipRecent = recentHistory?.wasRecentlyShown(a.pageId) == true
                if (!skipRecent && seenIds.add(a.pageId) && buffer.size < capacity) buffer += a
            }
            hasPrimed = true
        }
    }

    override suspend fun next(): Article? = mutex.withLock {
        if (buffer.isEmpty()) {
            primeIfNeeded()
            if (buffer.isEmpty()) return null
        }
        val epsilon = 0.2f // epsilon берём из VM/DI при необходимости; здесь по умолчанию
        val picked = if (Math.random() < epsilon) {
            buffer.random()
        } else {
            val maxScoreLocal = buffer.maxOf { recommender.score(it) }
            val candidates = buffer.filter { recommender.score(it) == maxScoreLocal }
            if (candidates.isNotEmpty()) candidates.random() else buffer.random()
        }
        buffer.remove(picked)
        recentHistory?.let { kotlinx.coroutines.runBlocking { it.addShown(picked.pageId) } }
        if (com.example.wikitok.BuildConfig.DEBUG) {
            val s = recommender.score(picked)
            android.util.Log.d("Feed", "next pageId=" + picked.pageId + ", score=" + s)
        }
        picked
    }
}
