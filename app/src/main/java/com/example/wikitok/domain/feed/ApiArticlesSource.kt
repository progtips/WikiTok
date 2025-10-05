package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article
import com.example.wikitok.data.wiki.WikipediaApi
import com.example.wikitok.data.wiki.toDomain
import com.example.wikitok.data.prefs.ICategoryWeightsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

class ApiArticlesSource(private val api: WikipediaApi, private val store: ICategoryWeightsStore? = null) : ArticlesSource {
    override suspend fun fetchBatch(n: Int): List<Article> {
        val result = ArrayList<Article>(n)
        val seen = HashSet<Long>()
        val weights = runCatching { store?.observeWeights()?.first() ?: emptyMap() }.getOrDefault(emptyMap())
        val top3 = weights.entries.sortedByDescending { it.value }.take(3).map { it.key }

        var attempts = 0
        var rrIndex = 0
        while (result.size < n && attempts < n * 5) {
            attempts++
            try {
                val pickTop = Math.random() >= 0.7 && top3.isNotEmpty() // 30%
                val dto = if (pickTop) {
                    val cat = top3[rrIndex % top3.size]
                    rrIndex++
                    withTimeout(1500) { api.fetchByCategory(category = cat) }
                } else {
                    withTimeout(1500) { api.randomSummary() }
                }
                val a = dto.toDomain()
                if (a.pageId > 0 && seen.add(a.pageId)) {
                    result += a
                }
            } catch (_: Throwable) {
                // fallback: ничего не добавляем, просто продолжаем попытки
            }
        }
        if (com.example.wikitok.BuildConfig.DEBUG) {
            val ids = result.take(3).joinToString { it.pageId.toString() }
            android.util.Log.d("ArticlesSource", "fetchBatch: size=${result.size}, ids=${ids}")
        }
        return result
    }
}


