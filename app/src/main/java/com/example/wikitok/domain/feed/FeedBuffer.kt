package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article
import java.util.ArrayDeque
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

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
    // сигнал о добавлении новых элементов
    private val itemAddedSignal = Channel<Unit>(Channel.CONFLATED)

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

                    var added = 0
                    mutex.withLock {
                        for (a in batch) {
                            if (a.pageId <= 0) continue
                            if (seenIds.add(a.pageId)) {
                                queue.addLast(a)
                                added++
                            }
                        }
                        // ограничение памяти
                        if (seenIds.size > 2000) {
                            val iter = seenIds.iterator()
                            repeat(500) { if (iter.hasNext()) { iter.next(); iter.remove() } }
                        }
                    }
                    if (added > 0) {
                        itemAddedSignal.trySend(Unit)
                    }

                    if (com.example.wikitok.BuildConfig.DEBUG) {
                        val q = mutex.withLock { queue.size }
                        android.util.Log.d("FeedBuffer", "prime: added=${added}, ready=${q}")
                    }
                }
            } finally {
                isPriming = false
            }
        }
    }

    override suspend fun next(): Article? {
        // 1) быстрый pop
        mutex.withLock {
            if (queue.isNotEmpty()) {
                val it = queue.removeFirst()
                if (!isPriming && queue.size < prefetchSize) scope.launch { primeIfNeeded() }
                if (com.example.wikitok.BuildConfig.DEBUG) {
                    android.util.Log.d("FeedBuffer", "next(): pop pageId=${it.pageId}")
                }
                return it
            }
        }

        // 2) подождём сигнал о добавлении, но не дольше таймаута
        val signalled = withTimeoutOrNull(1500L) { itemAddedSignal.receive() } != null
        if (signalled) {
            mutex.withLock {
                if (queue.isNotEmpty()) {
                    val it = queue.removeFirst()
                    if (!isPriming && queue.size < prefetchSize) scope.launch { primeIfNeeded() }
                    if (com.example.wikitok.BuildConfig.DEBUG) {
                        android.util.Log.d("FeedBuffer", "next(): pop-after-wait pageId=${it.pageId}")
                    }
                    return it
                }
            }
        } else {
            // мягко подпинаем подкачку, если долго пусто
            scope.launch { primeIfNeeded() }
        }
        return null
    }
}
