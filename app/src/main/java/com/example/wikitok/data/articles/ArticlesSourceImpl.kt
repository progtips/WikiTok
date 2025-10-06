package com.example.wikitok.data.articles

import com.example.wikitok.data.likes.ILikesRepository
import com.example.wikitok.data.prefs.ICategoryWeightsStore
import com.example.wikitok.domain.Article
import com.example.wikitok.domain.feed.ArticlesSource

class ArticlesSourceImpl(
    private val repo: ArticlesRepository, // <-- если у нас другой репозиторий, подставь актуальный
    private val likes: ILikesRepository,
    private val prefs: ICategoryWeightsStore,
) : ArticlesSource {

    override suspend fun fetchBatch(limit: Int): List<Article> {
        // Временная заглушка фильтрации: берём без категорий (если нужен фильтр — добавим реализацию позже)
        val raw = repo.getRandom(null, limit * 3)

        val result = raw
            .asSequence()
            .filter { it.pageId > 0 }
            .distinctBy { it.pageId }
            .take(limit)
            .toList()

        android.util.Log.d("ArticlesSource", "raw=${raw.size}, result=${result.size}")
        return result
    }
}


