package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article
import java.util.ArrayDeque
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Источник данных для подкачки. Реализуйте через вашу сеть/репозиторий.
 */
interface ArticlesSource {
    suspend fun fetchBatch(limit: Int): List<Article>
}

class FeedBuffer(
    private val scope: CoroutineScope,
    private val source: ArticlesSource,
    private val prefetchSize: Int = 6,
    private val fetchBatchSize: Int = 6,
) : IFeedBuffer {

    private val queue = ArrayDeque<Article>()
    private val seenIds = LinkedHashSet<Long>()
    private val mutex = Mutex()

    @Volatile private var isPriming = false
    private var primeJob: Job? = null

    override suspend fun primeIfNeeded() {
        if (isPriming) return
        val needPrime = mutex.withLock { queue.size < prefetchSize }
        if (!needPrime) return

        isPriming = true
        primeJob?.cancel()
        primeJob = scope.launch(Dispatchers.IO) {
            try {
                var attempts = 0
                while (true) {
                    val hasEnough = mutex.withLock { queue.size >= prefetchSize }
                    if (hasEnough) break

                    val batch = runCatching { source.fetchBatch(fetchBatchSize) }.getOrElse { e ->
                        if (com.example.wikitok.BuildConfig.DEBUG) {
                            android.util.Log.w("FeedBuffer", "fetch failed: ${e.message}")
                        }
                        attempts++
                        delay((500L * attempts).coerceAtMost(3_000L))
                        emptyList()
                    }

                    if (batch.isEmpty()) {
                        delay(300L)
                        continue
                    }

                    mutex.withLock {
                        for (a in batch) {
                            if (a.pageId <= 0) continue
                            if (seenIds.add(a.pageId)) {
                                queue.addLast(a)
                                if (seenIds.size > 2000) {
                                    val iter = seenIds.iterator()
                                    repeat(500) { if (iter.hasNext()) { iter.next(); iter.remove() } }
                                }
                            }
                        }
                    }

                    if (com.example.wikitok.BuildConfig.DEBUG) {
                        val q = mutex.withLock { queue.size }
                        android.util.Log.d("FeedBuffer", "prime: ready=${q}")
                    }
                }
            } finally {
                isPriming = false
            }
        }
    }

    override suspend fun next(): Article? {
        val item = mutex.withLock { if (queue.isEmpty()) null else queue.removeFirst() }
        if (item != null && com.example.wikitok.BuildConfig.DEBUG) {
            android.util.Log.d("FeedBuffer", "next(): pop pageId=${item.pageId}")
        }

        if (!isPriming) {
            val need = mutex.withLock { queue.size < prefetchSize }
            if (need) {
                scope.launch { primeIfNeeded() }
            }
        }
        return item
    }
}
