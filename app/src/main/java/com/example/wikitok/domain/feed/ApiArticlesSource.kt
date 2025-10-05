package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article
import com.example.wikitok.data.wiki.WikipediaApi
import com.example.wikitok.data.wiki.toDomain

class ApiArticlesSource(private val api: WikipediaApi) : ArticlesSource {
    override suspend fun fetchBatch(n: Int): List<Article> {
        val result = ArrayList<Article>(n)
        val seen = HashSet<Long>()
        var attempts = 0
        while (result.size < n && attempts < n * 3) {
            attempts++
            try {
                val dto = api.randomSummary()
                val a = dto.toDomain()
                if (a.pageId > 0 && seen.add(a.pageId)) {
                    result += a
                }
            } catch (_: Throwable) {
                // ignore network errors for batch fetch
            }
        }
        return result
    }
}


