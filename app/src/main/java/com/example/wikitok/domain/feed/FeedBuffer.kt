package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class FeedBuffer(
    private val source: ArticlesSource,
    private val batchSize: Int = 10
) : IFeedBuffer {

    private val deque = ArrayDeque<Article>()
    private val mutex = Mutex()
    @Volatile private var hardExhausted = false

    override suspend fun primeIfNeeded() {
        if (hardExhausted) return
        mutex.withLock {
            if (hardExhausted || deque.isNotEmpty()) return
            val items = withContext(Dispatchers.IO) {
                runCatching { source.fetchBatch(batchSize) }.getOrElse { e ->
                    android.util.Log.e("FeedBuffer", "prime failed", e)
                    emptyList()
                }
            }
            if (items.isEmpty()) {
                hardExhausted = true
            } else {
                deque.addAll(items)
            }
        }
    }

    override suspend fun peekNext(): Article? {
        if (deque.isEmpty() && !hardExhausted) primeIfNeeded()
        return mutex.withLock { deque.firstOrNull() }
    }

    override suspend fun next(): Article? {
        if (deque.isEmpty() && !hardExhausted) primeIfNeeded()
        return mutex.withLock { if (deque.isEmpty()) null else deque.removeFirst() }
    }
}
