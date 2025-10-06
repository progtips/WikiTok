package com.example.wikitok.data.articles

import com.example.wikitok.domain.Article

/**
 * Заглушка репозитория статей. В реальном коде подключите сетевой/локальный источник.
 */
class ArticlesRepository {
    /**
     * Возвращает случайные статьи с учётом желаемых категорий (если заданы).
     * Здесь — простая заглушка, вернёт пустой список; замените на реальную имплементацию.
     */
    suspend fun getRandom(categories: List<String>?, count: Int): List<Article> {
        val now = System.currentTimeMillis()
        val list = ArrayList<Article>(count)
        repeat(count) { idx ->
            val id = now + idx
            val cats = categories ?: listOf("General", "Tech", "Science")
            list.add(
                Article(
                    pageId = id,
                    title = "Demo Article #${idx + 1}",
                    summary = "Это демо-статья для теста листания и буфера.",
                    imageUrl = null,
                    categories = cats
                )
            )
        }
        return list
    }
}


