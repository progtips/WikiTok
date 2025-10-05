package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article
import com.example.wikitok.domain.recommend.Recommender
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedRecommenderTest {

    @Test
    fun positive_weights_bias_selection() = runBlocking {
        val weights = mapOf("science" to 1.0f, "history" to 0.0f)
        val rec = Recommender(weights)
        val source = object : ArticlesSource {
            override suspend fun fetchBatch(n: Int): List<Article> {
                // 50/50 science vs history
                val list = mutableListOf<Article>()
                repeat(n) { idx ->
                    val isScience = (idx % 2 == 0)
                    list += Article(
                        pageId = idx.toLong() + 1,
                        title = if (isScience) "S$idx" else "H$idx",
                        summary = null,
                        imageUrl = null,
                        categories = if (isScience) listOf("science") else listOf("history")
                    )
                }
                return list
            }
        }
        val buf = FeedBuffer(source, rec, capacity = 10)
        // Подготовим буфер
        buf.primeIfNeeded()
        var science = 0
        var history = 0
        repeat(50) {
            buf.primeIfNeeded()
            val a = buf.next() ?: return@repeat
            if (a.categories.contains("science")) science++ else history++
        }
        assertTrue("Science should be preferred", science > history)
    }
}
