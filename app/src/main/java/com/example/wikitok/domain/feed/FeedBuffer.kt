package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article
import com.example.wikitok.domain.recommend.IRecommender
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ArticlesSource {
    suspend fun fetchBatch(n: Int): List<Article>
}

class FeedBuffer(
    private val source: ArticlesSource,
    private val recommender: IRecommender,
    private val capacity: Int = 10
) : IFeedBuffer {
    private val mutex = Mutex()
    private val buffer = ArrayList<Article>()
    private val seenIds = HashSet<Long>()

    override suspend fun primeIfNeeded() = mutex.withLock {
        if (buffer.size < capacity / 2) {
            val need = capacity - buffer.size
            val batch = source.fetchBatch(need * 2) // запас, чтобы отфильтровать дубли
            for (a in batch) {
                if (seenIds.add(a.pageId) && buffer.size < capacity) buffer += a
            }
        }
    }

    override suspend fun next(): Article? = mutex.withLock {
        if (buffer.isEmpty()) {
            primeIfNeeded()
            if (buffer.isEmpty()) return null
        }
        val maxScore = buffer.maxOf { recommender.score(it) }
        val candidates = buffer.filter { recommender.score(it) == maxScore }
        val picked = if (candidates.isNotEmpty()) candidates.random() else buffer.random()
        buffer.remove(picked)
        picked
    }
}
